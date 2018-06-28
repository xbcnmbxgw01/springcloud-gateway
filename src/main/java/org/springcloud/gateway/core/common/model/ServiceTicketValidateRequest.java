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
package org.springcloud.gateway.core.common.model;

import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.springcloud.gateway.core.lang.StringUtils2;

/**
 * {@link ServiceTicketValidateRequest}
 *
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 */
public final class ServiceTicketValidateRequest extends BaseValidateModel {
    private static final long serialVersionUID = 1383145313778896117L;

    /**
     * Ticket may be empty when the first access is not logged-in<br/>
     * {@link org.springcloudgatewaydevops.iam.web.IamServerController#validate}
     */
    private String ticket;

    /**
     * Currently validating IAM client sessionId.
     */
    @NotBlank
    private String sessionId;

    /**
     * Currently validating additional extra parameters.
     */
    private Map<String, String> extraParameters = new HashMap<>();

    public ServiceTicketValidateRequest() {
        super();
    }

    public ServiceTicketValidateRequest(String ticket, String application, String sessionId) {
        super(application);
        setTicket(ticket);
        setSessionId(sessionId);
    }

    public final String getTicket() {
        return ticket;
    }

    public final ServiceTicketValidateRequest setTicket(String ticket) {
        if (!StringUtils2.isEmpty(ticket) && !"NULL".equalsIgnoreCase(ticket)) {
            this.ticket = ticket;
        }
        return this;
    }

    public final String getSessionId() {
        return sessionId;
    }

    public final ServiceTicketValidateRequest setSessionId(String sessionId) {
        if (!StringUtils2.isEmpty(sessionId) && !"NULL".equalsIgnoreCase(sessionId)) {
            this.sessionId = sessionId;
        }
        return this;
    }

    public Map<String, String> getExtraParameters() {
        return extraParameters;
    }

    public void setExtraParameters(Map<String, String> extraParameters) {
        if (nonNull(extraParameters)) {
            this.extraParameters = extraParameters;
        }
    }

    public ServiceTicketValidateRequest withExtraParameters(Map<String, String> extraParameters) {
        setExtraParameters(extraParameters);
        return this;
    }

}