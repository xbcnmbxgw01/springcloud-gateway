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
package org.springcloud.gateway.core.web.rest;

import org.springcloud.gateway.core.web.rest.RespBase.RetCode;

/**
 * Business logic restriction exception, When this exception API is caught, the
 * response code is {@link HttpStatus.PRECONDITION_FAILED}
 * 
 * @author springcloudgateway
 * @version v1.0.0
 * @since
 */
public interface FunctionalRuleRestrictException extends RESTfulException {

    /**
     * Get exception response code.
     * 
     * @return
     */
    @Override
    default RetCode getCode() {
        return RetCode.PRECONDITITE_LIMITED;
    }

}