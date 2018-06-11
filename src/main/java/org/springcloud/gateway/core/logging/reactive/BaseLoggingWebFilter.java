/*
 * Copyright 2017 ~ 2025 the original author or authors. <springcloudgateway@gmail.com>
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
package org.springcloud.gateway.core.logging.reactive;

import static com.google.common.base.Charsets.UTF_8;
import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static org.springcloud.gateway.core.lang.FastTimeClock.currentTimeMillis;

import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import org.springcloud.gateway.core.constant.CoreInfraConstants;
import org.springcloud.gateway.core.logging.LoggingMessageUtil;
import org.springcloud.gateway.core.logging.config.LoggingMessageProperties;
import org.springcloud.gateway.core.utils.web.ReactiveRequestExtractor;
import org.springcloud.gateway.core.web.matcher.SpelRequestMatcher;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link BaseLoggingWebFilter}
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
@Slf4j
public abstract class BaseLoggingWebFilter implements WebFilter, Ordered {

    protected final LoggingMessageProperties loggingConfig;
    protected final Environment environment;
    protected final SpelRequestMatcher requestMatcher;

    public BaseLoggingWebFilter(LoggingMessageProperties loggingConfig, Environment environment) {
        this.loggingConfig = notNullOf(loggingConfig, "loggingConfig");
        this.environment = notNullOf(environment, "environment");
        // Build gray request matcher.
        this.requestMatcher = new SpelRequestMatcher(loggingConfig.getPreferMatchRuleDefinitions());
    }

    /**
     * @see {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#loadFilters()}
     */
    @Override
    public int getOrder() {
        return loggingConfig.getFilterOrder();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Check if filtering flight logging is enabled.
        if (!isLoggingRequest(exchange)) {
            if (log.isDebugEnabled()) {
                ServerHttpRequest request = exchange.getRequest();
                log.debug("Not to meet the conditional rule to enable logging. - uri: {}, headers: {}, queryParams: {}",
                        request.getURI(), request.getHeaders(), request.getQueryParams());
            }
            return chain.filter(exchange);
        }

        final long beginTime = currentTimeMillis();
        exchange.getAttributes().put(LoggingMessageUtil.KEY_START_TIME, beginTime);
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        // Determine dyeing logs level.
        int verboseLevel = determineRequestVerboseLevel(exchange);
        if (verboseLevel <= 0) { // is disabled?
            return chain.filter(exchange);
        }
        String traceId = headers.getFirst(CoreInfraConstants.TRACE_REQUEST_ID_HEADER);
        String requestMethod = request.getMethodValue();

        return doFilterInternal(exchange, chain, headers, traceId, requestMethod);
    }

    protected abstract Mono<Void> doFilterInternal(
            ServerWebExchange exchange,
            WebFilterChain chain,
            HttpHeaders headers,
            String traceId,
            String requestMethod);

    /**
     * Check if enable print logs needs to be filtered
     * 
     * @param exchange
     * @return
     */
    protected boolean isLoggingRequest(ServerWebExchange exchange) {
        if (!loggingConfig.isEnabled()) {
            return false;
        }
        return requestMatcher.matches(new ReactiveRequestExtractor(exchange.getRequest()),
                loggingConfig.getPreferOpenMatchExpression());
    }

    /**
     * Determine request verbose logging level.
     * 
     * @param exchange
     * @return
     */
    protected int determineRequestVerboseLevel(ServerWebExchange exchange) {
        int verboseLevel = LoggingMessageUtil.determineRequestVerboseLevel(loggingConfig,
                new ReactiveRequestExtractor(exchange.getRequest()));
        exchange.getAttributes().put(LoggingMessageUtil.KEY_VERBOSE_LEVEL, verboseLevel);
        return verboseLevel;
    }

    /**
     * Reading to logging characters from request body stream segment or
     * response body stream segment, and add the log suffix '...' if necessary.
     * 
     * @param bodySegment
     * @param expectMaxLen
     * @return
     */
    public static String readToLogString(byte[] bodySegment, int expectMaxLen) {
        int readLen = Math.min(bodySegment.length, expectMaxLen);
        String logString = new String(bodySegment, 0, readLen, UTF_8);
        // Check if the readable data length is greater than the expected read
        // length. When the readable data length is greater than the maximum
        // read data length, add the log suffix '...'.
        boolean flag = (bodySegment.length > expectMaxLen);
        return flag ? logString.concat(" ...") : logString;
    }

    /**
     * Check if the specified flight log level range is met.
     * 
     * @param exchange
     * @param lower
     * @param upper
     * @return
     */
    public static boolean isLoglevelRange(ServerWebExchange exchange, int lower, int upper) {
        int verboseLevel = exchange.getAttribute(LoggingMessageUtil.KEY_VERBOSE_LEVEL);
        return LoggingMessageUtil.isLoglevelRange(verboseLevel, lower, upper);
    }

}
