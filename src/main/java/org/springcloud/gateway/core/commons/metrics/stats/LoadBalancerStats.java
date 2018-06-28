/*
 * Copyright 2017 ~ 2025 the original author or authors.<springcloudgateway@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springcloud.gateway.core.commons.metrics.stats;

import static org.springcloud.gateway.core.lang.Assert2.notNullOf;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springcloud.gateway.core.commons.serv.CanaryLoadBalancerFilterFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

/**
 * {@link LoadBalancerStats}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public interface LoadBalancerStats {

    /**
     * Update or register all instances pulled from the discovery server to the
     * statistic probe registry.
     * 
     * @param callback
     */
    void registerAllRouteServices(@Nullable Runnable callback);

    void restartProbeTask(@Nullable String... routeIds);

    int connect(ServerWebExchange exchange, ServiceInstance instance);

    int disconnect(ServerWebExchange exchange, ServiceInstance instance);

    default List<InstanceStatus> getReachableInstances(@NotNull ServerWebExchange exchange) {
        notNullOf(exchange, "exchange");
        Route route = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        return getReachableInstances(route.getId());
    }

    List<InstanceStatus> getReachableInstances(@NotBlank String routeId);

    default List<InstanceStatus> getAllInstances(@NotNull ServerWebExchange exchange) {
        notNullOf(exchange, "exchange");
        Route route = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        return getAllInstances(route.getId());
    }

    List<InstanceStatus> getAllInstances(@NotBlank String routeId);

    @NotNull
    Map<String, RouteServiceStatus> getAllRouteServices();

    @Getter
    @Setter
    @ToString
    public static class RouteServiceStatus {
        private String routeId;
        private CanaryLoadBalancerFilterFactory.Config config;
        private Map<String, InstanceStatus> instances = Maps.newConcurrentMap();
    }

    @Getter
    @Setter
    @ToString
    public static class InstanceStatus {
        private ServiceInstance instance;
        private Stats stats = new Stats();

        public InstanceStatus(ServiceInstance instance) {
            this.instance = notNullOf(instance, "instance");
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Stats {
        private AtomicInteger connections = new AtomicInteger(0);
        private Deque<ActiveProbe> activeProbes = Queues.newArrayDeque();
        private Deque<PassiveProbe> passiveProbes = Queues.newArrayDeque();
        private Boolean alive;
        private double latestCostTime;
        private double oldestCostTime;
        private double maxCostTime;
        private double minCostTime;
        private double avgCostTime;
    }

    /**
     * Active probe result.
     */
    @With
    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class ActiveProbe {
        private long timestamp;
        private boolean isTimeout;
        private Boolean errorOrCancel;
        private HttpResponseStatus responseStatus;
        private String responseBody;
    }

    /**
     * Passive probe result.
     */
    @With
    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class PassiveProbe {
        private long costTime;
        private Boolean errorOrCancel;
        // private HttpResponseStatus responseStatus;
    }

    public static final String KEY_COST_TIME = LoadBalancerStats.class.getName().concat(".costTime");

}
