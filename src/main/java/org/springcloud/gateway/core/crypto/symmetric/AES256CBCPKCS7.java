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

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * {@link AES256CBCPKCS7}
 *
 * Note: Unlimited Oracle JCE support required.
 *
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 */
public class AES256CBCPKCS7 extends JdkCryptorSupport {

    static {
        // Let java support pkcs7padding
        Security.addProvider(new BouncyCastleProvider());
    }

    public AES256CBCPKCS7() {
        super(new AlgorithmSpec("AES", "AES/CBC/PKCS7Padding", true, 256, 32, 32, 32));
    }

}