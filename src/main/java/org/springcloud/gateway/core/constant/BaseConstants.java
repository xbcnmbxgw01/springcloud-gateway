/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <springcloudgateway@gmail.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://springcloud.gateway.com
 */
package org.springcloud.gateway.core.constant;

import static org.springcloud.gateway.core.lang.TypeConverts.parseIntOrNull;
import static org.springcloud.gateway.core.lang.TypeConverts.parseLongOrNull;
import static java.lang.System.getProperty;
import static java.util.Objects.nonNull;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * {@link BaseConstants}
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @sine v1.0
 * @see
 */
public abstract class BaseConstants {

    /** OS environment map cache. */
    public static final Map<String, String> ENV = Collections.unmodifiableMap(System.getenv());

    public static String getStringProperty(@NotNull String key, @Nullable String defaultValue) {
        return getProperty(key, defaultValue);
    }

    public static Long getLongProperty(@NotNull String key, @Nullable Long defaultValue) {
        Long value = parseLongOrNull(getProperty(key));
        return nonNull(value) ? value : defaultValue;
    }

    public static Integer getIntegerProperty(@NotNull String key, @Nullable Integer defaultValue) {
        Integer value = parseIntOrNull(getProperty(key));
        return nonNull(value) ? value : defaultValue;
    }

}
