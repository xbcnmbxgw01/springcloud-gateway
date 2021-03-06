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
package org.springcloud.gateway.core.function;

/**
 * Generic processor of function, compared with
 * {@link java.util.function.Function} and {@link java.util.function.Supplier},
 * support for throwing exception was added.
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 */
@FunctionalInterface
public interface ProcessFunction<T, R> {

    /**
     * Do processing.
     * 
     * @param t
     * @throws Exception
     */
    R process(T t) throws Exception;

}