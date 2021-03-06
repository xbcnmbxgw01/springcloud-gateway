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
package org.springcloud.gateway.core.web.embed;

import static org.springframework.util.StringUtils.*;

import org.springframework.web.bind.annotation.GetMapping;

import org.springcloud.gateway.core.resource.StreamResource;
import org.springcloud.gateway.core.resource.resolver.ClassPathResourcePatternResolver;
import org.springcloud.gateway.core.resource.resolver.ResourcePatternResolver;
import org.springcloud.gateway.core.web.BaseController;
import org.springcloud.gateway.core.web.embed.EmbedWebappAutoConfiguration.SimpleEmbedWebappProperties;
import org.springcloud.gateway.core.web.embed.WebResourceCache.*;

import static com.google.common.io.ByteStreams.*;
import static com.google.common.base.Charsets.UTF_8;
import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static org.springcloud.gateway.core.tools.JvmRuntimeTool.isJvmInDebugging;
import static org.springcloud.gateway.core.web.SystemHelperUtils2.*;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.util.Locale.US;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.*;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.endsWithAny;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.springframework.http.HttpStatus.*;

import java.io.InputStream;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple generic embedded webapp endpoint.
 *
 * @author springcloudgateway@gmail.com
 * @version v1.0.0
 * @since
 */
public class SimpleEmbedWebappEndpoint extends BaseController {

    /**
     * {@link DefaultWebAppControllerProperties}
     */
    protected final SimpleEmbedWebappProperties config;

    /**
     * Web file buffer cache
     */
    protected final WebResourceCache cache;

    /**
     * {@link ResourcePatternResolver}
     */
    protected final ResourcePatternResolver resolver = new ClassPathResourcePatternResolver();

    public SimpleEmbedWebappEndpoint(SimpleEmbedWebappProperties config) {
        this(config, new DefaultWebappsGuavaCache());
    }

    public SimpleEmbedWebappEndpoint(SimpleEmbedWebappProperties config, WebResourceCache cache) {
        notNullOf(config, "embeddedWebappControllerProperties");
        this.config = config;
        this.cache = cache;
    }

    /**
     * Reader web resource files
     *
     * @param filename
     * @param response
     */
    @GetMapping(path = "/**")
    public void doWebResources(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uri = request.getRequestURI();
        String filepath = uri.substring(uri.indexOf(config.getBaseUri()) + config.getBaseUri().length());
        // e.g: /iam-server/view/login.html;JSESSIONID=sid9abdfdefa0944d2e867f5e
        int partIndex = filepath.indexOf(";");
        if (partIndex > 0) {
            filepath = filepath.substring(0, partIndex);
        }
        doResponseFile(filepath, request, response);
    }

    /**
     * Response file
     *
     * @param filepath
     * @param response
     * @throws Exception
     */
    protected void doResponseFile(String filepath, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Check & Pre processing
        if (isBlank(filepath) || endsWithAny(filepath, "/") || !preResponesPropertiesSet(filepath, request)) {
            log.debug("Forbidden access root: {}", filepath);
            // Forbidden.
            write(response, FORBIDDEN.value(), TEXT_HTML_VALUE, "Forbidden".getBytes(UTF_8));
            return;
        }

        // Gets buffer from cache
        log.debug("Accessing file: {}", filepath);
        byte[] buf = cache.get(filepath + request.getQueryString());
        if (isNull(buf)) { // Caching file?
            try (InputStream in = getResourceAsStream(filepath)) {
                if (!isNull(in)) {
                    buf = toByteArray(in);
                }
            }
        }

        // Response resource
        if (nonNull(buf)) {
            // Decorate
            buf = decorateResource(filepath, request, buf);

            // Post processing.
            postResponsePropertiesSet(response);

            // Check cache?
            if (isCache(filepath, request)) {
                cache.put(filepath, buf);
            }

            write(response, OK.value(), getContentType(filepath), buf);
        } else { // Not found.
            write(response, NOT_FOUND.value(), TEXT_HTML_VALUE, "Not Found".getBytes(UTF_8));
        }

    }

    /**
     * Enable caching or not
     * 
     * @param filepath
     * @param request
     * @return
     */
    protected boolean isCache(String filepath, HttpServletRequest request) {
        return !isJvmInDebugging;
    }

    /**
     * Pre-processing response properties set.
     * 
     * @param filepath
     * @param request
     * @return
     */
    protected boolean preResponesPropertiesSet(String filepath, HttpServletRequest request) {
        return true;
    }

    /**
     * Decorate resources
     * 
     * @param filepath
     * @param request
     * @param fileBuf
     * @return
     */
    protected byte[] decorateResource(String filepath, HttpServletRequest request, byte[] fileBuf) {
        return fileBuf;
    }

    /**
     * Post response properties set.
     * 
     * @param response
     */
    protected void postResponsePropertiesSet(HttpServletResponse response) {
        response.setDateHeader("Expires", currentTimeMillis() + 600_000);
        response.addHeader("Pragma", "Pragma");
        response.addHeader("Cache-Control", "public");
        response.addHeader("Last-Modified", valueOf(currentTimeMillis()));
    }

    /**
     * Gets content type by file path.
     *
     * @param ext
     * @return
     */
    protected String getContentType(String filepath) {
        String ext = getFilenameExtension(filepath.toLowerCase(US));
        return isBlank(ext) ? TEXT_HTML_VALUE : config.getMimeMapping().getProperty(ext, TEXT_HTML_VALUE);
    }

    /**
     * Load resource input stream.
     *
     * @param path
     * @return
     * @throws Exception
     */
    protected InputStream getResourceAsStream(String path) throws Exception {
        if (startsWith(path, "/")) {
            path = path.substring(1);
        }
        String location = config.getWebappLocation() + "/" + cleanURI(path);
        Set<StreamResource> ress = resolver.getResources(location);
        return !isEmpty(ress) ? ress.iterator().next().getInputStream() : null;
    }

}