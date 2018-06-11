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
package org.springcloud.gateway.core.lang;

import static org.springcloud.gateway.core.lang.StringUtils2.eqIgnCase;
import static org.springcloud.gateway.core.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springcloud.gateway.core.log.SmartLogger;

/**
 * {@link HostUtils}
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @since v1.0.0
 */
public abstract class HostUtils {
    protected static final SmartLogger log = getLogger(HostUtils.class);

    public static boolean isSameHost(String host1, String host2) {
        if (eqIgnCase(host1, host2)) {
            return true;
        }
        try {
            InetAddress h1 = InetAddress.getByName(host1);
            InetAddress h2 = InetAddress.getByName(host2);
            if (eqIgnCase(h1.getHostName(), h2.getHostName())) {
                return true;
            } else {
                return eqIgnCase(h1.getCanonicalHostName(), h2.getCanonicalHostName());
            }
        } catch (UnknownHostException e) {
            return false;
        } catch (Exception e) {
            log.warn(format("Unable to compare hosts. '%s' and '%s', reason: %s", host1, host2, e.getMessage()));
        }
        return false;
    }

}
