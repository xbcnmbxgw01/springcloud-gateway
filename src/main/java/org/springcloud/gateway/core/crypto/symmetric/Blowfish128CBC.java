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
package org.springcloud.gateway.core.crypto.symmetric;

import com.google.common.annotations.Beta;

/**
 * {@link Blowfish128CBC}
 *
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 */
@Beta
public class Blowfish128CBC extends JdkCryptorSupport {

    public Blowfish128CBC() {
        // blocksize 4-56bytes
        super(new AlgorithmSpec("Blowfish", "Blowfish/CBC/PKCS5Padding", true, 128, 16, 16, 8));
    }

}