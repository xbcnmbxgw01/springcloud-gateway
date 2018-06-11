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

import static org.springcloud.gateway.core.lang.Assert2.hasTextOf;
import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static org.springcloud.gateway.core.log.SmartLoggerFactory.getLogger;
import static java.util.Locale.US;
import static java.util.Objects.isNull;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import javax.annotation.Nullable;
import org.springcloud.gateway.core.io.CompressUtils;
import org.springcloud.gateway.core.io.FileIOUtils;
import org.springcloud.gateway.core.log.SmartLogger;
import org.springcloud.gateway.core.web.SystemHelperUtils2;

/**
 * Basic abstract controller
 * 
 * @author springcloudgateway@gmail.com
 * @version v1.0.0
 * @since
 */
public abstract class BaseController {

    protected final SmartLogger log = getLogger(getClass());

    @Autowired
    protected Validator validator;

    /**
     * Write response JSON message
     * 
     * @param response
     * @param json
     * @throws IOException
     */
    protected void writeJson(@NotNull HttpServletResponse response, @NotBlank String json) throws IOException {
        hasTextOf(json, "json");
        SystemHelperUtils2.writeJson(response, json);
    }

    /**
     * Output data with {@link HttpServletResponse}.
     * 
     * @param response
     * @param status
     * @param contentType
     * @param body
     * @throws IOException
     */
    protected void write(@NotNull HttpServletResponse response, int status, @NotBlank String contentType, @Nullable byte[] body)
            throws IOException {
        SystemHelperUtils2.write(response, status, contentType, body);
    }

    /**
     * Output zipfile stream with {@link HttpServletResponse}.
     * 
     * @param response
     * @param srcDir
     * @throws IOException
     */
    protected void writeZip(@NotNull HttpServletResponse response, @NotNull String srcDir, @NotBlank String filename)
            throws IOException {
        notNullOf(response, "response");
        hasTextOf(srcDir, "srcDir");
        hasTextOf(filename, "filename");

        response.setHeader("Content-Type", "application/octet-stream");
        response.setCharacterEncoding("utf-8");
        // Cleanup filename ".zip" suffix
        int index = filename.toLowerCase(US).lastIndexOf(".zip");
        if (index > 0) {
            filename = filename.substring(0, index);
        }
        response.setHeader("Content-Disposition", "attachment;filename=".concat(filename).concat(".zip"));
        CompressUtils.zip(srcDir, response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * Output file stream with {@link HttpServletResponse}.
     * 
     * @param response
     * @param srcFile
     * @param filename
     * @throws IOException
     */
    protected void writeFile(@NotNull HttpServletResponse response, @NotNull File srcFile, @NotBlank String filename)
            throws IOException {
        writeFile(response, srcFile, filename, null);
    }

    /**
     * Output file stream with {@link HttpServletResponse}.
     * 
     * @param response
     * @param srcFile
     * @param filename
     * @param headers
     * @throws IOException
     */
    protected void writeFile(@NotNull HttpServletResponse response, @NotNull File srcFile, @NotBlank String filename,
            @Nullable HttpHeaders headers) throws IOException {
        notNullOf(response, "response");
        notNullOf(srcFile, "srcDir");
        hasTextOf(filename, "filename");

        response.setCharacterEncoding("utf-8");
        if (!isNull(headers)) {
            headers.keySet().forEach(name -> response.setHeader(name, headers.getFirst(name)));
        } else { // By default download
            response.setHeader("Content-Type", "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=".concat(filename));
        }
        FileIOUtils.copyFile(srcFile, response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * SpringMVC controller redirection prefix.
     */
    public static final String REDIRECT_PREFIX = "redirect:";

}