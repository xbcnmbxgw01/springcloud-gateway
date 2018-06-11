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
package org.springcloud.gateway.core.bean;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springcloud.gateway.core.modelseri.JacksonUtils.toJSONString;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springcloud.gateway.core.bridge.RpcContextHolderBridges;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModelProperty.AccessMode;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * DB based bean entity.
 *
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @since
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseBean implements Serializable {
    private static final long serialVersionUID = 8940373806493080114L;

    /**
     * Unique primary key ID
     */
    @ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
    @ApiParam(readOnly = true, hidden = true)
    // Because feign remote call requires readability and writability, while
    // swagger requires read-only, there is a conflict, so we should solve this
    // problem on the swagger side.
    // @JsonIgnoreProperties(allowGetters = true, allowSetters = true)
    private Long id;

    private Integer enable;

    /**
     * for control queries data permission scope.
     */
    private String organizationCode;

    private String remark;

    @ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
    @ApiParam(readOnly = true, hidden = true)
    // Because feign remote call requires readability and writability, while
    // swagger requires read-only, there is a conflict, so we should solve this
    // problem on the swagger side.
    // @JsonIgnoreProperties(allowGetters = true, allowSetters = false)
    private Long createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
    @ApiParam(readOnly = true, hidden = true)
    // Because feign remote call requires readability and writability, while
    // swagger requires read-only, there is a conflict, so we should solve this
    // problem on the swagger side.
    // @JsonIgnoreProperties(allowGetters = true, allowSetters = false)
    private Date createDate;

    @ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
    @ApiParam(readOnly = true, hidden = true)
    // Because feign remote call requires readability and writability, while
    // swagger requires read-only, there is a conflict, so we should solve this
    // problem on the swagger side.
    // @JsonIgnoreProperties(allowGetters = true, allowSetters = false)
    private Long updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
    @ApiParam(readOnly = true, hidden = true)
    // Because feign remote call requires readability and writability, while
    // swagger requires read-only, there is a conflict, so we should solve this
    // problem on the swagger side.
    // @JsonIgnoreProperties(allowGetters = true, allowSetters = false)
    private Date updateDate;

    /**
     * Logistic delete status. </br>
     * </br>
     * Note: In order to be compatible with the different usages of the
     * annotations of swagger 2.x and 3.x, the safest way is to add all possible
     * ways that will work.
     */
    // @JsonIgnore
    @ApiModelProperty(readOnly = true, hidden = true)
    @ApiParam(hidden = true, readOnly = true)
    // Because feign remote call requires readability and writability, while
    // swagger requires read-only, there is a conflict, so we should solve this
    // problem on the swagger side.
    // @JsonIgnoreProperties(allowGetters = false, allowSetters = false)
    private Integer delFlag;

    //
    // --- Temporary fields. ---
    //

    /**
     * Human creation date.</br>
     * </br>
     * Note: In order to be compatible with the different usages of the
     * annotations of swagger 2.x and 3.x, the safest way is to add all possible
     * ways that will work.
     */
    @ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
    @ApiParam(readOnly = true, hidden = true)
    // Because feign remote call requires readability and writability, while
    // swagger requires read-only, there is a conflict, so we should solve this
    // problem on the swagger side.
    // @JsonIgnoreProperties(allowGetters = true, allowSetters = false)
    private transient String humanCreateDate;

    /**
     * Human update date.</br>
     * </br>
     * Note: In order to be compatible with the different usages of the
     * annotations of swagger 2.x and 3.x, the safest way is to add all possible
     * ways that will work.
     */
    @ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
    @ApiParam(readOnly = true, hidden = true)
    // Because feign remote call requires readability and writability, while
    // swagger requires read-only, there is a conflict, so we should solve this
    // problem on the swagger side.
    // @JsonIgnoreProperties(allowGetters = true, allowSetters = false)
    private transient String humanUpdateDate;

    /**
     * Execute method before inserting, need to call manually
     * 
     * @return return current preparing insert generated id.
     */
    public void preInsert() {
        // @see:org.springcloudgatewayinxcnm.data.mybatis.mapper.PreparedBeanMapperInterceptor#preInsert
        // setCreateBy(UNKNOWN_USER_ID);
        setCreateDate(isNull(getCreateDate()) ? new Date() : getCreateDate());
        setUpdateDate(isNull(getUpdateDate()) ? getCreateDate() : getUpdateDate());
        setDelFlag(isNull(getDelFlag()) ? DEL_FLAG_NORMAL : getDelFlag());
        setEnable(isNull(getEnable()) ? ENABLED : getEnable());
    }

    /**
     * Execute method before inserting, need to call manually
     *
     * @param organizationCode
     * @return return current preparing insert generated id.
     */
    public void preInsert(String organizationCode) {
        if (isBlank(getOrganizationCode())) {
            setOrganizationCode(organizationCode);
        }
    }

    /**
     * Execute method before update, need to call manually
     */
    public void preUpdate() {
        // @see:org.springcloudgatewayinxcnm.data.mybatis.mapper.PreparedBeanMapperInterceptor#preUpdate
        // setUpdateBy(UNKNOWN_USER_ID);
        setUpdateDate(new Date());
    }

    public BaseBean withId(Long id) {
        this.id = id;
        return this;
    }

    public BaseBean withEnable(Integer enable) {
        setEnable(enable);
        return this;
    }

    public BaseBean withOrganizationCode(String organizationCode) {
        setOrganizationCode(organizationCode);
        return this;
    }

    public BaseBean withRemark(String remark) {
        setRemark(remark);
        return this;
    }

    public BaseBean withCreateBy(Long createBy) {
        setCreateBy(createBy);
        return this;
    }

    public BaseBean withCreateDate(Date createDate) {
        setCreateDate(createDate);
        return this;
    }

    public BaseBean withUpdateBy(Long updateBy) {
        setUpdateBy(updateBy);
        return this;
    }

    public BaseBean withUpdateDate(Date updateDate) {
        setUpdateDate(updateDate);
        return this;
    }

    public BaseBean withDelFlag(Integer delFlag) {
        setDelFlag(delFlag);
        return this;
    }

    public BaseBean deleted() {
        setDelFlag(DEL_FLAG_DELETE);
        return this;
    }

    public BaseBean undelete() {
        setDelFlag(DEL_FLAG_NORMAL);
        return this;
    }

    // --- Function's. ---

    @Override
    public String toString() {
        return getClass().getSimpleName().concat("<").concat(toJSONString(this)).concat(">");
    }

    /**
     * Internal utility for {@link BaseBean}
     */
    public static final class Util {

        public static void bind(BaseBean bean) {
            localCurrentBean.set(bean);
        }

        public static void update() {
            BaseBean lBean = localCurrentBean.get();
            if (RpcContextHolderBridges.hasRpcContextHolderClass()) { // Distributed-mode?
                Long rBeanId = (Long) RpcContextHolderBridges.invokeGet(true, CURRENT_BEANID_KEY, Long.class);
                if (nonNull(rBeanId) && nonNull(lBean)) {
                    lBean.setId(rBeanId);
                }
            }

            // Remove object reference from local.
            localCurrentBean.remove();

            // Remove from RPC origin context.
            if (RpcContextHolderBridges.hasRpcContextHolderClass()) { // Distributed-mode?
                // It's cleanup too:
                // @see:org.springcloudgatewayinxcnm.integration.feign.core.context.interceptor.RpcContextProviderProxyInterceptor#postHandle
                RpcContextHolderBridges.invokeRemoveAttachment(true, CURRENT_BEANID_KEY);
            }
        }

        /**
         * Distributed mode, current inserted bean.id RPC context key.
         */
        public static transient final String CURRENT_BEANID_KEY = "currentInsertionBean";
        private static transient final ThreadLocal<BaseBean> localCurrentBean = new ThreadLocal<>();
    }

    /**
     * Generic Status: enabled
     */
    public static transient final int ENABLED = 1;

    /**
     * Generic Status: disabled
     */
    public static transient final int DISABLED = 0;

    /**
     * Generic Status: normal (not deleted)
     */
    public static transient final int DEL_FLAG_NORMAL = 0;

    /**
     * Generic Status: deleted
     */
    public static transient final int DEL_FLAG_DELETE = 1;

    /**
     * Unknown user ID.
     */
    public static transient final long UNKNOWN_USER_ID = -1;

    /*
     * Default super administrator user name.
     */
    public static transient final String DEFAULT_SUPER_USER = "root";

}