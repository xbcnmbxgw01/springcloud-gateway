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
package org.springcloud.gateway.core.crypto.asymmetric;

import java.security.spec.KeySpec;

import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;

import org.springcloud.gateway.core.crypto.asymmetric.spec.ECCKeyPairSpec;

/**
 * Asymmetric algorithm implemented by ECC.
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @see
 */
public class ECCCryptor extends AbstractAsymmetricCryptor {

    public ECCCryptor() {
        super(1024);
    }

    public ECCCryptor(int keysize) {
        super(keysize);
    }

    @Override
    public String getAlgorithmPrimary() {
        return "ECC";
    }

    @Override
    public String getPadAlgorithm() {
        return "ECC/ECB/PKCS1Padding";
    }

    @Override
    protected Class<? extends KeySpec> getPublicKeySpecClass() {
        return ECPublicKeySpec.class;
    }

    @Override
    protected Class<? extends KeySpec> getPrivateKeySpecClass() {
        return ECPrivateKeySpec.class;
    }

    @Override
    protected ECCKeyPairSpec newKeySpec(String algorithm, KeySpec pubKeySpec, KeySpec keySpec) {
        return new ECCKeyPairSpec(algorithm, pubKeySpec, keySpec);
    }

}