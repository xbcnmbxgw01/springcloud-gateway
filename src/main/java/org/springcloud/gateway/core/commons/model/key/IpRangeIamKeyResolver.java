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
package org.springcloud.gateway.core.commons.model.key;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link IpRangeIamKeyResolver}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v1.0.0
 */
public class IpRangeIamKeyResolver extends AbstractIamKeyResolver<IpRangeIamKeyResolver.IpRangeKeyResolverStrategy> {

    @Override
    public KeyResolverProvider kind() {
        return KeyResolverProvider.IpRange;
    }

    @Override
    public Mono<String> resolve(IpRangeKeyResolverStrategy strategy, ServerWebExchange exchange) {
        return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }

    @Getter
    @Setter
    @ToString
    @Validated
    public static class IpRangeKeyResolverStrategy extends IamKeyResolver.KeyResolverStrategy {
    }

}