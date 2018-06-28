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
package org.springcloud.gateway.core.common.utils;

import org.apache.commons.lang3.StringUtils;

import org.springcloud.gateway.core.codec.Base58;
import org.springcloud.gateway.core.common.subject.IamPrincipal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Charsets.UTF_8;
import static org.springcloud.gateway.core.collection.CollectionUtils2.safeList;
import static org.springcloud.gateway.core.lang.Assert2.notEmptyOf;
import static org.springcloud.gateway.core.lang.Assert2.notNull;
import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static org.springcloud.gateway.core.utils.web.WebUtils3.*;
import static org.springcloud.gateway.core.common.subject.IamPrincipal.OrganizationInfo;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * {@link IamOrganizationUtils}
 *
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @author vjay
 * @version v1.0.0
 * @since
 */
public abstract class IamOrganizationUtils {

    /**
     * Gets organizations from Session
     *
     * @return
     */
    public static List<OrganizationInfo> getSessionOrganizations() {
        IamPrincipal currentPrincipal = RpcContextIamSecurityUtils.currentIamPrincipal();
        notNullOf(currentPrincipal, "currentIamPrincipal");
        return safeList(currentPrincipal.getOrganization().getOrganizations());
    }

    /**
     * Gets session organization all tree.
     *
     * @return
     */
    public static List<OrganizationInfoTree> getOrganizationTrees() {
        List<OrganizationInfo> organs = getSessionOrganizations();

        List<OrganizationInfoTree> trees = new ArrayList<>();
        List<OrganizationInfo> parentOrgans = getParentOrganizations(organs);
        for (OrganizationInfo parent : parentOrgans) {
            OrganizationInfoTree tree = new OrganizationInfoTree(parent);
            addChildrenOrganizationTree(organs, tree);
            trees.add(tree);
        }

        return trees;
    }

    /**
     * Gets organization codes by current request.
     *
     * @return
     */
    public static List<String> getRequestOrganizationCodes() {
        String orgCode = getRequestParameter(PARAM_ORGANIZATION_CODE);
        orgCode = new String(Base58.decodeBase58(orgCode), UTF_8);
        if (isBlank(orgCode) || equalsAnyIgnoreCase(orgCode, "ALL", "*")) {
            List<OrganizationInfo> organs = getSessionOrganizations();
            return organs.stream().map(a -> a.getOrganizationCode()).collect(toList());
        } else {
            return getPermittedChildOrganCodes(orgCode);
        }
    }

    /**
     * Gets organization code by current request.
     *
     * @return
     */
    public static String getRequestOrganizationCode() {
        try {
            String orgCode = getRequestParameter(PARAM_ORGANIZATION_CODE);
            orgCode = new String(Base58.decodeBase58(orgCode), UTF_8);

            if (isBlank(orgCode) || "ALL".equalsIgnoreCase(orgCode)) {
                List<OrganizationInfo> organs = getSessionOrganizations();
                List<OrganizationInfo> parentOrgans = getParentOrganizations(organs);

                notEmptyOf(parentOrgans, "organizationCode");
                return parentOrgans.get(0).getOrganizationCode();
            } else {
                return orgCode;
            }
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * Gets permitted children organization codes by orgCode
     *
     * @param orgCode
     * @return
     */
    private static List<String> getPermittedChildOrganCodes(String orgCode) {
        List<OrganizationInfo> orgs;
        if (isBlank(orgCode)) {
            orgs = getSessionOrganizations();
        } else {
            orgs = getPermittedChildOrganizations(orgCode);
        }
        return safeList(orgs).stream().map(o -> o.getOrganizationCode()).collect(toList());
    }

    /**
     * Gets permitted children organizations by orgCode
     *
     * @param orgCode
     * @return
     */
    private static List<OrganizationInfo> getPermittedChildOrganizations(String orgCode) {
        List<OrganizationInfo> orgs = getSessionOrganizations();

        List<OrganizationInfo> childrens = new ArrayList<>();
        addChildrenOrganizations(orgs, orgCode, childrens);

        OrganizationInfo org = extOrganization(orgs, orgCode);
        notNull(org, "No found organization info with legal permissions by orgCode: %", orgCode);

        childrens.add(org);
        return childrens;
    }

    /**
     * Extract organization info by orgainzation code.
     * 
     * @param organs
     * @param orgCode
     * @return
     */
    private static OrganizationInfo extOrganization(List<OrganizationInfo> organs, String orgCode) {
        Optional<OrganizationInfo> opt = safeList(organs).stream()
                .filter(o -> StringUtils.equals(o.getOrganizationCode(), orgCode))
                .filter(o -> !isNull(o))
                .findFirst();
        return opt.orElse(null);
    }

    /**
     * Adds children organizations.
     * 
     * @param orgs
     * @param orgCode
     * @param childrens
     */
    private static void addChildrenOrganizations(List<OrganizationInfo> orgs, String orgCode, List<OrganizationInfo> childrens) {
        for (OrganizationInfo org : orgs) {
            String _orgCode = org.getOrganizationCode();
            String parent = org.getParent();
            if (StringUtils.equals(parent, orgCode)) {
                childrens.add(org);
                addChildrenOrganizations(orgs, _orgCode, childrens);
            }
        }
    }

    /**
     * Adds children organizations
     * 
     * @param orgs
     * @param tree
     */
    private static void addChildrenOrganizationTree(List<OrganizationInfo> orgs, OrganizationInfoTree tree) {
        for (OrganizationInfo org : orgs) {
            if (StringUtils.equals(tree.getOrganizationCode(), org.getParent())) {
                OrganizationInfoTree child = new OrganizationInfoTree(org);
                tree.getChildren().add(child);
                addChildrenOrganizationTree(orgs, child);
            }
        }
    }

    /**
     * Gets parent organizations
     * 
     * @param organs
     * @return
     */
    private static List<OrganizationInfo> getParentOrganizations(List<OrganizationInfo> organs) {
        List<OrganizationInfo> parentOrgans = new ArrayList<>();
        for (OrganizationInfo o : organs) {
            // Find parent organization
            Optional<OrganizationInfo> opt = organs.stream()
                    .filter(p -> StringUtils.equals(p.getOrganizationCode(), o.getParent()))
                    .findAny();
            if (!opt.isPresent()) {
                parentOrgans.add(o);
            }
        }
        return parentOrgans;
    }

    /**
     * {@link OrganizationInfoTree}
     *
     * @author springcloudgateway <springcloudgateway@gmail.com>
     * @version v1.0.0
     * @since
     */
    public static class OrganizationInfoTree extends OrganizationInfo {
        private static final long serialVersionUID = 7353905956153984552L;

        private List<OrganizationInfoTree> children = new ArrayList<>();

        public OrganizationInfoTree(OrganizationInfo organ) {
            super(organ.getOrganizationCode(), organ.getParent(), organ.getType(), organ.getName(), organ.getAreaId());
        }

        public List<OrganizationInfoTree> getChildren() {
            return children;
        }

        public void setChildren(List<OrganizationInfoTree> children) {
            this.children = children;
        }
    }

    /**
     * Request parameter organization code.
     */
    final private static String PARAM_ORGANIZATION_CODE = "organization_code";

}