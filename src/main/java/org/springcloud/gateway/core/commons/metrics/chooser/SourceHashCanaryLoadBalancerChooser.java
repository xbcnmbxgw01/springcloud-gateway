package org.springcloud.gateway.core.commons.metrics.chooser;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.net.InetSocketAddress;
import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats.InstanceStatus;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade.MetricsName;
import org.springcloud.gateway.core.commons.serv.CanaryLoadBalancerFilterFactory;
import org.springcloud.gateway.core.commons.serv.LoadBalancerUtil;

/**
 * Grayscale load balancer rule for based source address hashing. </br>
 * That is, the request from the same ip is sent to the same server in the
 * backend, if the backend server is working normally and not overloaded. This
 * can solve the problem of session sharing, but there is a problem here. Many
 * enterprises, communities, and schools share an IP, which will lead to uneven
 * distribution of requests.
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer}
 */
public class SourceHashCanaryLoadBalancerChooser extends AbstractCanaryLoadBalancerChooser {

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.SH;
    }

    @Override
    protected ServiceInstance doChooseInstance(
            CanaryLoadBalancerFilterFactory.Config config,
            ServerWebExchange exchange,
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances) {

        int count = 0;
        InstanceStatus chosenInstance = null;
        while (isNull(chosenInstance) && count++ < config.getChoose().getMaxChooseTries()) {
            List<InstanceStatus> allInstances = stats.getAllInstances(exchange);
            List<InstanceStatus> reachableInstances = stats.getReachableInstances(exchange);
            List<InstanceStatus> availableInstances = getAvailableInstances(reachableInstances, candidateInstances);

            int allCount = allInstances.size();
            int avaCount = availableInstances.size();

            if ((avaCount == 0) || (allCount == 0)) {
                log.warn("No up servers available from load balancer loadBalancerStats: {}", stats);
                return null;
            }

            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            if (nonNull(remoteAddress)) {
                int hash = remoteAddress.getHostString().hashCode();
                int nextInstanceIndex = avaCount % hash;

                chosenInstance = availableInstances.get(nextInstanceIndex);
                if (isNull(chosenInstance)) {
                    // Give up the opportunity for short-term CPU to give other
                    // threads execution, just like the sleep() method does not
                    // release the lock.
                    Thread.yield();
                    continue;
                }

                if (LoadBalancerUtil.isAlive(config, chosenInstance.getStats())) {
                    return chosenInstance.getInstance();
                }

                // Next.
                chosenInstance = null;
            }
        }

        if (count >= config.getChoose().getMaxChooseTries()) {
            addCounterMetrics(config, exchange, MetricsName.CANARY_LB_CHOOSE_MAX_TRIES_TOTAL, serviceId);
            log.warn("No available alive servers after {} tries from load balancer loadBalancerStats: {}", count, stats);
        }
        return null;
    }

}