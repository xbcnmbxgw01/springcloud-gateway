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
package org.springcloud.gateway.core.remoting;

import org.springcloud.gateway.core.collection.multimap.MultiValueMap;
import org.springcloud.gateway.core.lang.ObjectUtils2;
import org.springcloud.gateway.core.remoting.standard.HttpHeaders;

/**
 * Represents an HTTP request or response entity, consisting of headers and
 * body.
 *
 * @see org.springcloud.gateway.core.remoting.RestClient
 * @see #getBody()
 * @see #getHeaders()
 */
public class HttpEntity<T> {

    private final HttpHeaders headers;

    private final T body;

    /**
     * Create a new, empty {@code HttpEntity}.
     */
    protected HttpEntity() {
        this(null, null);
    }

    /**
     * Create a new {@code HttpEntity} with the given body and no headers.
     * 
     * @param body
     *            the entity body
     */
    public HttpEntity(T body) {
        this(body, null);
    }

    /**
     * Create a new {@code HttpEntity} with the given headers and no body.
     * 
     * @param headers
     *            the entity headers
     */
    public HttpEntity(MultiValueMap<String, String> headers) {
        this(null, headers);
    }

    /**
     * Create a new {@code HttpEntity} with the given body and headers.
     * 
     * @param body
     *            the entity body
     * @param headers
     *            the entity headers
     */
    public HttpEntity(T body, MultiValueMap<String, String> headers) {
        this.body = body;
        HttpHeaders tempHeaders = new HttpHeaders();
        if (headers != null) {
            tempHeaders.putAll(headers);
        }
        this.headers = HttpHeaders.readOnlyHttpHeaders(tempHeaders);
    }

    /**
     * Returns the headers of this entity.
     */
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    /**
     * Returns the body of this entity.
     */
    public T getBody() {
        return this.body;
    }

    /**
     * Indicates whether this entity has a body.
     */
    public boolean hasBody() {
        return (this.body != null);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || other.getClass() != getClass()) {
            return false;
        }
        HttpEntity<?> otherEntity = (HttpEntity<?>) other;
        return (ObjectUtils2.nullSafeEquals(this.headers, otherEntity.headers)
                && ObjectUtils2.nullSafeEquals(this.body, otherEntity.body));
    }

    @Override
    public int hashCode() {
        return (ObjectUtils2.nullSafeHashCode(this.headers) * 29 + ObjectUtils2.nullSafeHashCode(this.body));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("<");
        if (this.body != null) {
            builder.append(this.body);
            if (this.headers != null) {
                builder.append(',');
            }
        }
        if (this.headers != null) {
            builder.append(this.headers);
        }
        builder.append('>');
        return builder.toString();
    }

    /**
     * The empty {@code HttpEntity}, with no body or headers.
     */
    public static final HttpEntity<?> EMPTY = new HttpEntity<Object>();

}