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
package org.springcloud.gateway.core.common.bean;

import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static org.springcloud.gateway.core.modelseri.JacksonUtils.toJSONString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

import org.springframework.util.CollectionUtils;

/**
 * Social provider authorized info
 * 
 * @author springcloudgateway@gmail.com
 * @version v1.0.0
 * @since
 */
public class SocialAuthorizeInfo implements Serializable {
    private static final long serialVersionUID = -434937172743067451L;

    /**
     * social networking service provider ID
     */
    private String provider;

    /**
     * social networking provider open-id
     */
    private String openId;

    /**
     * social networking provider union-id.(If exist)
     */
    private String unionId;

    /**
     * Oauth2 authorized user information
     */
    private Map<String, Object> userProfile = new HashMap<>();

    public SocialAuthorizeInfo() {
        super();
    }

    public SocialAuthorizeInfo(@NotBlank String provider, @Nullable String openId) {
        this(provider, openId, null);
    }

    public SocialAuthorizeInfo(@NotBlank String provider, @Nullable String openId, @Nullable String unionId) {
        this(provider, openId, unionId, null);
    }

    public SocialAuthorizeInfo(@NotBlank String provider, @Nullable String openId, @Nullable String unionId,
            @Nullable Map<String, Object> userProfile) {
        this.provider = notNullOf(provider, "provider");
        this.openId = openId;
        this.unionId = unionId;
        if (!CollectionUtils.isEmpty(userProfile)) {
            this.userProfile.putAll(userProfile);
        }
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String providerId) {
        this.provider = providerId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public Map<String, Object> getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(Map<String, Object> userProfile) {
        this.userProfile = userProfile;
    }

    /**
     * As json string.
     * 
     * @return
     */
    public String asJsonString() {
        return toJSONString(this);
    }

    @Override
    public String toString() {
        return asJsonString();
    }

}