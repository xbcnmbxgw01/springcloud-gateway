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
package org.springcloud.gateway.core.remoting.parse;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.annotation.Nullable;
import org.springcloud.gateway.core.remoting.ClientHttpResponse;

/**
 * Generic callback interface u sed by {@link RestTemplate}'s retrieval methods
 * Implementations of this interface perform the actual work of extracting data
 * from a {@link ClientHttpResponse}, but don't need to worry about exception
 * handling or closing resources.
 *
 * <p>
 * Used internally by the {@link RestTemplate}, but also useful for application
 * code. There is one available factory method, see
 * {@link RestTemplate#responseEntityExtractor(Type)}.
 *
 * @param <T>
 *            the data type
 * @see RestTemplate#execute
 */
@FunctionalInterface
public interface ResponseExtractor<T> {

    /**
     * Extract data from the given {@code ClientHttpResponse} and return it.
     * 
     * @param response
     *            the HTTP response
     * @return the extracted data
     * @throws IOException
     *             in case of I/O errors
     */
    @Nullable
    T extractData(ClientHttpResponse response) throws IOException;

}