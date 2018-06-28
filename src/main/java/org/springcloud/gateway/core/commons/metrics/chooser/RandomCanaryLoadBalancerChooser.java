package org.springcloud.gateway.core.commons.metrics.chooser;

import static java.util.Objects.isNull;
import static java.util.concurrent.ThreadLocalRandom.current;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats.InstanceStatus;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade.MetricsName;
import org.springcloud.gateway.core.commons.serv.CanaryLoadBalancerFilterFactory;
import org.springcloud.gateway.core.commons.serv.LoadBalancerUtil;

/**
 * Random Grayscale Load Balancer chooser based on random.
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public class RandomCanaryLoadBalancerChooser extends AbstractCanaryLoadBalancerChooser {

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.R;
    }

    @Override
    protected ServiceInstance doChooseInstance(
            CanaryLoadBalancerFilterFactory.Config config,
            ServerWebExchange exchange,
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances) {

        // return
        // candidateInstances.get(current().nextInt(0,candidateInstances.size()));

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

            int nextInstanceIndex = current().nextInt(0, avaCount);
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

        if (count >= config.getChoose().getMaxChooseTries()) {
            addCounterMetrics(config, exchange, MetricsName.CANARY_LB_CHOOSE_MAX_TRIES_TOTAL, serviceId);
            log.warn("No available alive servers after {} tries from load balancer loadBalancerStats: {}", count, stats);
        }
        return null;
    }

}