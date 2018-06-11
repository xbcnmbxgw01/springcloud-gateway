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
package org.springcloud.gateway.core.commons.model.interceptor.quota;

import javax.validation.constraints.Min;

import org.springcloud.gateway.core.commons.model.interceptor.RequestLimiterStrategy;
import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class RedisQuotaRequestLimiterStrategy extends RequestLimiterStrategy {

    /**
     * The number of total maximum allowed requests capacity.
     */
    private @Min(0) Long requestCapacity = 1000L;

    /**
     * The date pattern of request quota limit calculation cycle.
     */
    private String cycleDatePattern = "yyMMdd";
}