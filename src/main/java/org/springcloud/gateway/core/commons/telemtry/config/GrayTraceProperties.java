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
package org.springcloud.gateway.core.commons.telemtry.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import org.springcloud.gateway.core.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link GrayTraceProperties}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
@Validated
public class GrayTraceProperties {

    private boolean enabled = true;

    /**
     * Preferred to enable tracing samples match SPEL match expression. Default
     * by '${false}', which means never no match. </br>
     * </br>
     * Tip: The built-in support to get the current routeId, such as:
     * '#{routeId.get().test('my-service-route')}'
     */
    private String preferOpenMatchExpression = "#{true}";

    /**
     * Preferred to enable tracing samples match rule definitions.
     */
    private List<MatchHttpRequestRule> preferMatchRuleDefinitions = new ArrayList<>();

}
