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
package org.springcloud.gateway.core.web;

import static org.springcloud.gateway.core.lang.Assert2.notNull;
import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static org.springcloud.gateway.core.lang.StringUtils2.isDomain;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Locale.US;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.replaceIgnoreCase;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.google.common.annotations.Beta;
import org.springcloud.gateway.core.collection.CollectionUtils2;
import org.springcloud.gateway.core.lang.Assert2;
import org.springcloud.gateway.core.lang.StringUtils2;

/**
 * {@link WebUtils}
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public abstract class WebUtils {

    /**
     * URL encode by UTF-8
     * 
     * @param url
     *            plain URL
     * @return
     */
    public static String safeEncodeURL(String url) {
        try {
            if (!contains(trimToEmpty(url).toLowerCase(US), URL_SEPAR_SLASH)) {
                return URLEncoder.encode(url, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return url;
    }

    /**
     * URL decode by UTF-8
     * 
     * @param url
     *            encode URL
     * @return
     */
    public static String safeDecodeURL(String url) {
        try {
            if (containsAny(trimToEmpty(url).toLowerCase(US), URL_SEPAR_SLASH, URL_SEPAR_QUEST, URL_SEPAR_COLON)) {
                return URLDecoder.decode(url, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return url;
    }

    /**
     * Extract top level domain string. </br>
     * 
     * <pre>
     *extDomainString("my.springcloud.gateway.com")                         =>  my.springcloud.gateway.com
     *extDomainString("//my.springcloud.gateway.com/myapp1")                =>  my.springcloud.gateway.com
     *extDomainString("/myapp1/api/v2/list")                 =>  ""
     *extDomainString("http://my.springcloud.gateway.com.cn/myapp1")        =>  my.springcloud.gateway.com.cn
     *extDomainString("https://my2.my1.springcloud.gateway.com:80/myapp1")  =>  my2.my1.springcloud.gateway.com
     * </pre>
     * 
     * @param hostOrUri
     * @return
     */
    public static String extDomainString(String hostOrUri) {
        if (isBlank(hostOrUri)) {
            return hostOrUri;
        }
        hostOrUri = safeDecodeURL(hostOrUri);
        String domain = hostOrUri; // Is host?
        if (containsAny(hostOrUri, '/')) { // Is URI?
            domain = URI.create(hostOrUri).getHost();
        }
        // Check domain available?
        // isTrueOf(isDomain(domain), format("hostOrUri: %s", hostOrUri));
        if (!isDomain(domain) || isBlank(domain)) {
            return EMPTY;
        }
        return domain;
    }

    /**
     * Extract top level domain string. </br>
     * 
     * <pre>
     *extTopDomainString("my.springcloud.gateway.com")                         =>  springcloud.gateway.com
     *extTopDomainString("//my.springcloud.gateway.com/myapp1")                =>  springcloud.gateway.com
     *extTopDomainString("/myapp1/api/v2/list")                 =>  ""
     *extTopDomainString("http://my.springcloud.gateway.com.cn/myapp1")        =>  springcloud.gateway.com.cn
     *extTopDomainString("https://my2.my1.springcloud.gateway.com:80/myapp1")  =>  springcloud.gateway.com
     * </pre>
     * 
     * @param hostOrUri
     * @return
     */
    public static String extTopDomainString(String hostOrUri) {
        String domain = extDomainString(hostOrUri);
        if (isBlank(domain)) { // Available?
            return EMPTY;
        }
        String[] parts = split(domain, ".");
        int endIndex = 2;
        if (domain.endsWith("com.cn")) { // Special parse
            endIndex = 3;
        }
        StringBuffer topDomain = new StringBuffer();
        for (int i = 0; i < parts.length; i++) {
            if (i >= (parts.length - endIndex)) {
                topDomain.append(parts[i]);
                if (i < (parts.length - 1)) {
                    topDomain.append(".");
                }
            }
        }
        return topDomain.toString();
    }

    /**
     * Check whether the URI is relative to the path.
     * 
     * e.g.</br>
     * 
     * <pre>
     * isRelativeUri("//my.springcloud.gateway.com/myapp1") = false </br>
     * isRelativeUri("http://my.springcloud.gateway.com/myapp1") = false </br>
     * isRelativeUri("https://my.springcloud.gateway.com:80/myapp1") = false </br>
     * isRelativeUri("/myapp1/api/v2/list") = true </br>
     * </pre>
     * 
     * @param uri
     * @return
     */
    public static boolean isRelativeUri(String uri) {
        if (isBlank(uri))
            return false;
        return isBlank(URI.create(safeDecodeURL(uri)).getScheme()) && !uri.startsWith("//");
    }

    /**
     * Domain names equals two URIs are equal (including secondary and tertiary
     * domain names, etc. Exact matching)
     * 
     * e.g.</br>
     * 
     * <pre>
     * isEqualWithDomain("http://my.springcloud.gateway.com/myapp1","http://my.springcloud.gateway.com/myapp2")=true
     * isEqualWithDomain("http://my1.domin.com/myapp1","http://my.springcloud.gateway.com/myapp2")=false
     * isEqualWithDomain("http://my.springcloud.gateway.com:80/myapp1","http://my.springcloud.gateway.com:8080/myapp2")=true
     * isEqualWithDomain("https://my.springcloud.gateway.com:80/myapp1","http://my.springcloud.gateway.com:8080/myapp2")=true
     * isEqualWithDomain("http://localhost","http://localhost:8080/myapp2")=true
     * isEqualWithDomain("http://127.0.0.1","http://127.0.0.1:8080/myapp2")=true
     * </pre>
     * 
     * @param uria
     * @param urib
     * @return
     */
    public static boolean isEqualWithDomain(String uria, String urib) {
        if (isNull(uria) || isNull(urib)) {
            return false;
        }
        return URI.create(safeDecodeURL(uria)).getHost().equals(URI.create(safeDecodeURL(urib)).getHost());
    }

    /**
     * Check whether the wildcard domain uri belongs to the same origin. </br>
     * 
     * e.g:
     * 
     * <pre>
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com/API/v2", "http://bb.aa.domain.com/API/v2", true) == true
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com/API/v2", "https://bb.aa.domain.com/API/v2", true) == false
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com/api/v2/", "http://bb.aa.domain.com/API/v2", true) == true
     * {@link #isSameWildcardOrigin}("http://bb.*.domain.com", "https://bb.aa.domain.com", false) == true
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com", "https://bb.aa.domain.com", true) == false
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com:8080", "http://bb.aa.domain.com:8080/", true) == true
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com:8080", "http://bb.aa.domain.com:8443/v2/xx", true) == true
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com:*", "http://bb.aa.domain.com:8443/v2/xx", true) == true
     * </pre>
     * 
     * @param defWildcardUri
     *            Definition wildcard URI
     * @param requestUri
     * @param checkScheme
     * @return
     */
    public static boolean isSameWildcardOrigin(String defWildcardUri, String requestUri, boolean checkScheme) {
        if (isBlank(defWildcardUri) || isBlank(requestUri))
            return false;
        if (defWildcardUri.equals(requestUri)) // URL equaled?
            return true;

        // Scheme matched?
        URI uri1 = URI.create(defWildcardUri);
        URI uri2 = URI.create(requestUri);
        final boolean schemeMatched = uri1.getScheme().equalsIgnoreCase(uri2.getScheme());
        if (checkScheme && !schemeMatched)
            return false;

        // Hostname equaled?
        String hostname1 = extractWildcardEndpoint(defWildcardUri);
        String hostname2 = extractWildcardEndpoint(requestUri);
        if (equalsIgnoreCase(hostname1, hostname2))
            return true;

        // Hostname wildcard matched?
        boolean wildcardHostnameMatched = false;
        String[] parts1 = split(hostname1, ".");
        String[] parts2 = split(hostname2, ".");
        for (int i = 0; i < parts1.length; i++) {
            if (equalsIgnoreCase(parts1[i], "*")) {
                if (i < (hostname1.length() - 1) && i < (hostname2.length() - 1)) {
                    String compare1 = join(parts1, ".", i + 1, parts1.length);
                    String compare2 = join(parts2, ".", i + 1, parts2.length);
                    if (equalsIgnoreCase(compare1, compare2)) {
                        wildcardHostnameMatched = true;
                        break;
                    }
                }
            }
        }
        // Check scheme matched.
        if (checkScheme && wildcardHostnameMatched) {
            return schemeMatched;
        }

        return wildcardHostnameMatched;
    }

    /**
     * Extract domain text from {@link URI}. </br>
     * Uri resolution cannot be used here because it may fail when there are
     * wildcards, e.g,
     * {@link URI#create}("http://*.aa.domain.com/api/v2/).getHost() is
     * null.</br>
     * 
     * <pre>
     * {@link #extractWildcardHostName}("http://*.domain.com/v2/xx") --> *.domain.com
     * {@link #extractWildcardHostName}("http://*.aa.domain.com:*") --> *.aa.domain.com
     * {@link #extractWildcardHostName}("http://*.bb.domain.com:8080/v2/xx") --> *.bb.domain.com
     * </pre>
     * 
     * @param wildcardUri
     * @return
     */
    public static String extractWildcardEndpoint(String wildcardUri) {
        if (isEmpty(wildcardUri))
            return EMPTY;

        wildcardUri = trimToEmpty(safeEncodeURL(wildcardUri)).toLowerCase(US);
        String noPrefix = wildcardUri.substring(wildcardUri.indexOf(URL_SEPAR_PROTO) + URL_SEPAR_PROTO.length());
        int slashIndex = noPrefix.indexOf(URL_SEPAR_SLASH);
        String serverName = noPrefix;
        if (slashIndex > 0) {
            serverName = noPrefix.substring(0, slashIndex);
        }

        // Check domain valid
        // e.g:
        // http://*.domain.com:8080 [allow]
        // http://*.domain.com:* [allow]
        // http://*.aa.*.domain.com [not allow]
        String hostname = serverName;
        if (serverName.contains(URL_SEPAR_COLON)) {
            hostname = serverName.substring(0, serverName.indexOf(URL_SEPAR_COLON));
        }
        Assert2.isTrue(hostname.indexOf("*") == hostname.lastIndexOf("*"), "Illegal serverName: %s, contains multiple wildcards!",
                serverName);
        return safeDecodeURL(hostname);
    }

    /**
     * Determine whether the requested URL belongs to the domain. e.g:</br>
     * withInDomain("my.domain.com","http://my.domain.com/myapp") = true </br>
     * withInDomain("my.domain.com","https://my.domain.com/myapp") = true </br>
     * withInDomain("my.domain.com","https://my1.domain.com/myapp") = false
     * </br>
     * withInDomain("*.domain.com", "https://other1.domain.com/myapp") = true
     * </br>
     * 
     * @param domain
     * @param url
     * @return
     */
    public static boolean withInDomain(String domain, String url) {
        notNull(domain, "'domain' must not be null");
        notNull(url, "'requestUrl' must not be null");
        try {
            String hostname = new URI(safeDecodeURL(cleanURI(url))).getHost();
            if (!domain.contains("*")) {
                Assert2.isTrue(isDomain(domain), String.format("Illegal domain[%s] name format", domain));
                return equalsIgnoreCase(domain, hostname);
            }
            if (domain.startsWith("*")) {
                return equalsIgnoreCase(domain.substring(1), hostname.substring(hostname.indexOf(".")));
            }
            return false;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Determine whether the requested URL belongs to the base URI. e.g:</br>
     * withInURL("https://domain/myapp/login","https://domain/myapp/login?t=123")
     * == true </br>
     * withInURL("https://domain/myapp","https://domain/myapp/login?t=123") ==
     * true </br>
     * withInURL("https://domain/myapp/login?r=abc","https://domain/myapp/login?t=123")
     * == true </br>
     * withInURL("https://domain/myapp/login?r=abc","http://domain/myapp/login?t=123")
     * == false </br>
     * </br>
     * 
     * @param baseUrl
     * @param url
     * @return
     */
    public static boolean withInURL(String baseUrl, String url) {
        if (baseUrl == null || url == null) {
            return false;
        }
        try {
            // If it's a URL in decoding format
            URI baseUrl0 = new URI(safeDecodeURL(cleanURI(baseUrl)));
            URI uri0 = new URI(safeDecodeURL(cleanURI(url)));
            return (StringUtils.startsWithIgnoreCase(uri0.getRawPath(), baseUrl0.getRawPath())
                    && StringUtils.equalsIgnoreCase(uri0.getScheme(), baseUrl0.getScheme())
                    && StringUtils.equalsIgnoreCase(uri0.getHost(), baseUrl0.getHost()) && uri0.getPort() == baseUrl0.getPort());
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    /**
     * Obtain base URI for default. </br>
     * 
     * <pre>
     * getBaseURIForDefault("http", "my.com", 8080) == "http://my.com:8080"
     * getBaseURIForDefault("http", "my.com", 80) == "http://my.com"
     * getBaseURIForDefault("https", "my.com", 443) == "https://my.com"
     * getBaseURIForDefault("https", "my.com", -1) == "https://my.com"
     * </pre>
     * 
     * @param scheme
     * @param serverName
     * @param port
     * @return
     */
    public static String getBaseURIForDefault(String scheme, String serverName, int port) {
        notNull(scheme, "Http request scheme must not be empty");
        notNull(serverName, "Http request serverName must not be empty");
        StringBuffer baseUri = new StringBuffer(scheme).append("://").append(serverName);
        if (port > 0) {
            Assert2.isTrue((port > 0 && port < 65536), "Http server port must be greater than 0 and less than 65536");
            if (!((equalsIgnoreCase(scheme, "HTTP") && port == 80) || (equalsIgnoreCase(scheme, "HTTPS") && port == 443))) {
                baseUri.append(":").append(port);
            }
        }
        return baseUri.toString();
    }

    /**
     * Clean request URI. </br>
     * 
     * <pre>
     * cleanURI("https://my.domain.com//myapp///index?t=123") => "https://my.domain.com/myapp/index?t=123"
     * </pre>
     * 
     * @param uri
     * @return
     */
    @Beta
    public static String cleanURI(String uri) {
        if (isBlank(uri)) {
            return uri;
        }

        // Check syntax
        uri = URI.create(uri).toString();

        /**
         * Cleaning.</br>
         * Note: that you cannot change the original URI case.
         */
        try {
            String encodeUrl = safeEncodeURL(uri);
            String pathUrl = encodeUrl, schema = EMPTY;
            if (encodeUrl.toLowerCase(US).contains(URL_SEPAR_PROTO)) {
                // Start from "://"
                int startIndex = encodeUrl.toLowerCase(US).indexOf(URL_SEPAR_PROTO);
                schema = encodeUrl.substring(0, startIndex) + URL_SEPAR_PROTO;
                pathUrl = encodeUrl.substring(startIndex + URL_SEPAR_PROTO.length());
            }

            // Cleanup for: '/shopping/order//list' => '/shopping/order/list'
            String lastCleanUrl = pathUrl;
            for (int i = 0; i < 256; i++) { // https://www.ietf.org/rfc/rfc2616.txt#3.2.1
                String cleanUrl = replaceIgnoreCase(lastCleanUrl, (URL_SEPAR_SLASH2).toUpperCase(), URL_SEPAR_SLASH);
                if (StringUtils.equals(cleanUrl, lastCleanUrl)) {
                    break;
                } else {
                    lastCleanUrl = cleanUrl;
                }
            }
            return safeDecodeURL(schema + lastCleanUrl);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * To query URL parameters.
     * 
     * <pre>
     * toQueryParams("application=iam-example&redirect_url=http://my.com/index") == {application->iam-example, redirect_url=>http://my.com/index}
     * toQueryParams("application=iam-example&redirect_url=http://my.com/index/#/me") == {application->iam-example, redirect_url=>http://my.com/index/#/me}
     * </pre>
     * 
     * @param urlQuery
     * @return
     */
    public static Map<String, String> toQueryParams(@Nullable String urlQuery) {
        Map<String, String> parameters = new LinkedHashMap<>(4);
        if (isBlank(urlQuery))
            return parameters;
        try {
            // Remove the character to the left of the '?'
            int separQuestIndex = urlQuery.lastIndexOf("?");
            if (separQuestIndex > 0) {
                urlQuery = urlQuery.substring(separQuestIndex + 1);
            }

            String[] paramPairs = urlQuery.split("&");
            for (int i = 0; i < paramPairs.length; i++) {
                String[] parts = trimToEmpty(paramPairs[i]).split("=");
                if (parts.length >= 2) {
                    parameters.put(parts[0], parts[1]);
                }
            }
            return parameters;
        } catch (Exception e) {
            throw new IllegalArgumentException(format("Illegal parameter format. '%s'", urlQuery), e);
        }
    }

    /**
     * Gets multi map first value.
     * 
     * @param params
     * @return
     */
    public static String getMultiMapFirstValue(@Nullable Map<String, List<String>> params, String name) {
        if (isNull(params)) {
            return null;
        }
        return params.entrySet()
                .stream()
                .filter(e -> equalsIgnoreCase(e.getKey(), name))
                .map(e -> CollectionUtils2.isEmpty(e.getValue()) ? e.getValue().get(0) : null)
                .filter(e -> !isNull(e))
                .findFirst()
                .orElse(null);
    }

    /**
     * Map to query URL
     * 
     * @param uri
     * @param queryParams
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static String applyQueryURL(@Nullable String uri, @Nullable Map queryParams) {
        if (CollectionUtils2.isEmpty(queryParams) || isBlank(uri)) {
            return uri;
        }

        URI _uri = URI.create(uri);
        // Merge origin-older uri query parameters.
        Map<String, String> mergeParams = new HashMap<>(toQueryParams(_uri.getQuery()));
        mergeParams.putAll(queryParams);
        // Gets base URI.
        StringBuffer url = new StringBuffer(uri); // Relative path?
        if (!isAnyBlank(_uri.getScheme(), _uri.getHost())) {
            url.setLength(0); // Reset
            url.append(getBaseURIForDefault(_uri.getScheme(), _uri.getHost(), _uri.getPort()));
            url.append(_uri.getPath());
        }
        if (url.lastIndexOf("?") == -1) {
            url.append("?");
        }

        // To URI parameters string
        for (Iterator<?> it = mergeParams.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            url.append(key);
            url.append("=");
            // Prevents any occurrence of a value string null
            Object value = mergeParams.get(key);
            if (value != null) {
                url.append(value); // "null"
            }
            if (it.hasNext()) {
                url.append("&");
            }
        }
        return url.toString();
    }

    /**
     * Is true </br>
     * 
     * @param value
     * @return Return TRUE with true/t/y/yes/on/1/enabled
     */
    public static boolean isTrue(String value) {
        return isTrue(value, false);
    }

    /**
     * Is true </br>
     * 
     * @param value
     * @param defaultValue
     * @return Return TRUE with true/t/y/yes/on/1/enabled
     */
    public static boolean isTrue(String value, boolean defaultValue) {
        return StringUtils2.isTrue(value, defaultValue);
    }

    /**
     * Check that the requested resource is a base media file?
     * 
     * @param path
     * @return
     */
    public static boolean isMediaRequest(String path) {
        String ext = StringUtils2.getFilenameExtension(path);
        for (String media : MEDIA_BASE) {
            if (equalsIgnoreCase(ext, media)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is XHR Request
     * 
     * @param extractor
     * @return
     */
    public static boolean isXHRRequest(@NotNull WebRequestExtractor extractor) {
        notNullOf(extractor, "extractor");
        return equalsIgnoreCase(extractor.getHeaderValue("X-Requested-With"), "XMLHttpRequest");
    }

    /**
     * Web Request extractor wrapper, It is mainly to solve the request types of
     * different models or protocols, such as:
     * Xxx{@link javax.servlet.ServletRequest} or {@link HttpServletRequest} or
     * {@link org.springframework.web.servlet.function.ServerRequest}(reactive)
     * etc
     * 
     * @author springcloudgateway <springcloudgateway@gmail.com>
     * @version v1.0.0
     * @since
     */
    public static interface WebRequestExtractor {

        /**
         * Gets request Id. </br>
         * 
         * Spring reactor HTTP request refer to:
         * {@link org.springframework.web.server.ServerWebExchange.LOG_ID}
         * 
         * @return
         */
        default @Nullable String getRequestId() {
            return null;
        }

        /**
         * Gets web request URI.
         * 
         * @return
         */
        default @Nullable URI getRequestURI() {
            return null;
        }

        /**
         * Gets web request authenticated principal.
         * 
         * @return
         */
        default @Nullable Principal getPrincipal() {
            return null;
        }

        /**
         * Gets web request method.
         * 
         * @return
         */
        default String getMethod() {
            return null;
        }

        /**
         * Gets web request scheme.
         * 
         * @return
         */
        default String getScheme() {
            return null;
        }

        /**
         * Gets web request remote host.
         * 
         * @return
         */
        default String getHost() {
            return null;
        }

        /**
         * Gets web request port.
         * 
         * @return
         */
        default Integer getPort() {
            return null;
        }

        /**
         * Gets web request path.
         * 
         * @return
         */
        default String getPath() {
            return null;
        }

        /**
         * Gets request an collection of all the query parameter names this
         * request contains. If the request has no query parameter, this method
         * returns an null collection.
         * 
         * @return
         */
        default Collection<String> getQueryNames() {
            return null;
        }

        /**
         * Gets query parameter by name.
         * 
         * @param name
         * @return
         */
        default @Nullable String getQueryValue(String name) {
            return null;
        }

        /**
         * Gets headers parameter by name.
         * 
         * @param name
         * @return
         */
        default @Nullable String getHeaderValue(String name) {
            return null;
        }

        /**
         * Returns an collection of all the header names this request contains.
         * If the request has no headers, this method returns an null
         * collection.
         * 
         * @return
         */
        default Collection<String> getHeaderNames() {
            return null;
        }

        /**
         * Gets cookie value by name.
         * 
         * @param name
         * @return
         */
        default @Nullable String getCookieValue(String name) {
            return null;
        }

        /**
         * Gets cookie names.
         * 
         * @return
         */
        default @Nullable Collection<String> getCookieNames() {
            return null;
        }

    }

    /**
     * URL scheme(HTTPS)
     */
    public static final String URL_SCHEME_HTTPS = "https";

    /**
     * URL scheme(HTTP)
     */
    public static final String URL_SCHEME_HTTP = "http";

    /**
     * URL separator(/)
     */
    public static final String URL_SEPAR_SLASH = "%2f";

    /**
     * URL double separator(//)
     */
    public static final String URL_SEPAR_SLASH2 = URL_SEPAR_SLASH + URL_SEPAR_SLASH;

    /**
     * URL separator(?)
     */
    public static final String URL_SEPAR_QUEST = "%3f";

    /**
     * URL colon separator(:)
     */
    public static final String URL_SEPAR_COLON = "%3a";

    /**
     * Protocol separators, such as
     * https://my.domain.com=>https%3A%2F%2Fmy.domain.com
     */
    public static final String URL_SEPAR_PROTO = URL_SEPAR_COLON + URL_SEPAR_SLASH + URL_SEPAR_SLASH;

    /**
     * Request the header key name of real client IP. </br>
     * 
     * <pre>
     *  一、没有使用代理服务器的情况：
     *        REMOTE_ADDR = 您的 IP
     *        HTTP_VIA = 没数值或不显示
     *        HTTP_X_FORWARDED_FOR = 没数值或不显示
     *  二、使用透明代理服务器的情况：Transparent Proxies
     *        REMOTE_ADDR = 最后一个代理服务器 IP 
     *        HTTP_VIA = 代理服务器 IP
     *        HTTP_X_FORWARDED_FOR = 您的真实 IP ，经过多个代理服务器时，这个值类似如下：203.98.182.163, 203.98.182.163, 203.129.72.215。
     *     这类代理服务器还是将您的信息转发给您的访问对象，无法达到隐藏真实身份的目的。
     *  三、使用普通匿名代理服务器的情况：Anonymous Proxies
     *        REMOTE_ADDR = 最后一个代理服务器 IP 
     *        HTTP_VIA = 代理服务器 IP
     *        HTTP_X_FORWARDED_FOR = 代理服务器 IP ，经过多个代理服务器时，这个值类似如下：203.98.182.163, 203.98.182.163, 203.129.72.215。
     *     隐藏了您的真实IP，但是向访问对象透露了您是使用代理服务器访问他们的。
     *  四、使用欺骗性代理服务器的情况：Distorting Proxies
     *        REMOTE_ADDR = 代理服务器 IP 
     *        HTTP_VIA = 代理服务器 IP 
     *        HTTP_X_FORWARDED_FOR = 随机的 IP ，经过多个代理服务器时，这个值类似如下：203.98.182.163, 203.98.182.163, 203.129.72.215。
     *     告诉了访问对象您使用了代理服务器，但编造了一个虚假的随机IP代替您的真实IP欺骗它。
     *  五、使用高匿名代理服务器的情况：High Anonymity Proxies (Elite proxies)
     *        REMOTE_ADDR = 代理服务器 IP
     *        HTTP_VIA = 没数值或不显示
     *        HTTP_X_FORWARDED_FOR = 没数值或不显示 ，经过多个代理服务器时，这个值类似如下：203.98.182.163, 203.98.182.163, 203.129.72.215。
     * </pre>
     */
    public static final String[] HEADER_REAL_IP = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "X-Real-IP",
            "REMOTE_ADDR", "Remote-Addr", "RemoteAddr", // RemoteAddr
            "REMOTE_IP", "Remote-Ip", "RemoteIp", // RemoteIp: Aliyun-SLB
            "HTTP_X_FORWARDED_FOR", "Http-X-Forwarded-For", "HttpXForwardedFor", // HttpXForwardedFor
            "HTTP_X_FORWARDED", "Http-X-Forwarded", "HttpXForwarded", // HttpXForwarded
            "HTTP_Client_IP", "Http-Client-Ip", "HttpClientIp", // HttpClientIp
            "HTTP_X_CLUSTER_CLIENT_IP", "Http-X-Cluster-Client-Ip", "HttpXClusterClientIp", // HttpXClusterClientIp
            "HTTP_FORWARDED_FOR", "Http-Forwarded-For", "HttpForwardedFor", // HttpForwardedFor
            "HTTP_VIA ", "Http-Via", "HttpVia" }; // HttpVia

    /**
     * Request the header key name of real protocol scheme.
     */
    public static final String[] HEADER_REAL_PROTOCOL = { "X-Forwarded-Proto" };

    /**
     * Request the header key name of real host
     */
    public static final String[] HEADER_REAL_HOST = { "Host" };

    /**
     * Common media file suffix definitions
     */
    public static final String[] MEDIA_BASE = new String[] { "ico", "icon", "css", "js", "html", "shtml", "htm", "jsp", "jspx",
            "jsf", "aspx", "asp", "php", "jpeg", "jpg", "png", "bmp", "gif", "tif", "pic", "swf", "svg", "ttf", "eot", "eot@",
            "woff", "woff2", "wd3", "txt", "doc", "docx", "wps", "ppt", "pptx", "pdf", "excel", "xls", "xlsx", "avi", "wav",
            "mp3", "amr", "mp4", "aiff", "rar", "tar.gz", "tar", "zip", "gzip", "ipa", "plist", "apk", "7-zip" };

    /**
     * Unified exception handling stack trace parameter name.
     */
    public static final String PARAM_STACKTRACE = getenv().getOrDefault("INFRA_REQUEST_STACKTRACE_PARAM", "x-stacktrace");

    public static final Predicate<String> defaultStringAnyFilter = name -> true;

}
