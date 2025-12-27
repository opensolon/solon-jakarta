/*
 * Copyright 2017-2025 noear.org and authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.noear.solon.net.http.impl.jdk;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.util.IoUtil;
import org.noear.solon.core.util.MultiMap;
import org.noear.solon.exception.SolonException;
import org.noear.solon.net.http.HttpResponse;
import org.noear.solon.net.http.HttpResponseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Http 响应 JDK HttpClient 实现 (基于 Java 11+ java.net.http.HttpClient)
 *
 * @author noear
 * @since 3.8.1
 */
public class JdkHttpResponse implements HttpResponse {
    private final JdkHttpUtils utils;
    private final java.net.http.HttpResponse<byte[]> response;
    private final int statusCode;
    private final String statusMessage;
    private final MultiMap<String> headers;
    private MultiMap<String> cookies;
    private final InputStream body;

    public JdkHttpResponse(JdkHttpUtils utils, java.net.http.HttpResponse<byte[]> response) throws IOException {
        this.utils = utils;
        this.response = response;

        this.statusCode = response.statusCode();
        this.statusMessage = null;
        this.headers = new MultiMap<>();

        // 处理响应头
        for (Map.Entry<String, java.util.List<String>> kv : response.headers().map().entrySet()) {
            if (kv.getKey() != null && kv.getValue() != null) {
                headers.holder(kv.getKey()).setValues(kv.getValue());
            }
        }

        byte[] responseBody = response.body();
        if (responseBody == null || responseBody.length == 0) {
            // 当响应体为空时，给一个空的输入流
            body = new ByteArrayInputStream(new byte[0]);
        } else {
            // 检查 Content-Encoding
            String encoding = header("Content-Encoding");

            InputStream inputStream = new ByteArrayInputStream(responseBody);

            if (Utils.isNotEmpty(encoding)) {
                if ("gzip".equalsIgnoreCase(encoding)) {
                    inputStream = new GZIPInputStream(inputStream);
                } else if ("deflate".equalsIgnoreCase(encoding)) {
                    inputStream = new InflaterInputStream(inputStream, new Inflater(true));
                }
            }

            body = inputStream;
        }
    }

    private MultiMap<String> cookiesInit() {
        if (cookies == null) {
            cookies = new MultiMap<>();

            List<String> kvAry = headers("Set-Cookie");
            for (String kvStr : kvAry) {
                int eqIdx = kvStr.indexOf("=");
                int smIdx = kvStr.indexOf(";", eqIdx);

                String key = kvStr.substring(0, eqIdx);
                String value = smIdx > 0 ? kvStr.substring(eqIdx + 1, smIdx) : kvStr.substring(eqIdx + 1);

                cookies.add(key, value);
            }
        }

        return cookies;
    }

    @Override
    public Collection<String> headerNames() {
        return headers.keySet();
    }

    @Override
    public String header(String name) {
        List<String> values = headers(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    @Override
    public List<String> headers(String name) {
        return headers.getAll(name);
    }

    @Override
    public Collection<String> cookieNames() {
        return cookiesInit().keySet();
    }

    @Override
    public String cookie(String name) {
        return cookiesInit().get(name);
    }

    @Override
    public List<String> cookies(String name) {
        return cookiesInit().getAll(name);
    }

    @Override
    public Long contentLength() {
        String lengthHeader = header("Content-Length");
        if (lengthHeader != null) {
            try {
                return Long.parseLong(lengthHeader);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String contentType() {
        return header("Content-Type");
    }

    private Charset _contentCharset;
    @Override
    public Charset contentCharset() {
        if (_contentCharset == null) {
            _contentCharset = parseContentCharset(contentType());
        }

        return _contentCharset;
    }

    public static Charset parseContentCharset(String contentType) {
        //用于单测
        if (contentType != null) {
            int charsetIdx = contentType.indexOf("charset=");
            if (charsetIdx > 0) {
                String charset = contentType.substring(charsetIdx + 8);
                if (charset.indexOf(';') > 0) {
                    charset = charset.substring(0, charset.indexOf(';'));
                }
                return Charset.forName(charset.trim());
            }
        }

        return null;
    }

    @Override
    public List<String> cookies() {
        return headers("Set-Cookie");
    }

    @Override
    public int code() {
        return statusCode;
    }

    @Override
    public String message() {
        return statusMessage;
    }

    @Override
    public InputStream body() {
        return body;
    }

    @Override
    public byte[] bodyAsBytes() throws IOException {
        try {
            return IoUtil.transferToBytes(body());
        } finally {
            body().close();
        }
    }

    @Override
    public String bodyAsString() throws IOException {
        try {
            String encoding = response.headers().firstValue("Content-Encoding").orElse(null);
            if (Utils.isEmpty(encoding)) {
                if (utils.charset() == null) {
                    return IoUtil.transferToString(body(), Solon.encoding());
                } else {
                    return IoUtil.transferToString(body(), utils.charset().name());
                }
            } else {
                return IoUtil.transferToString(body(), encoding);
            }
        } finally {
            body().close();
        }
    }

    @Override
    public <T> T bodyAsBean(Type type) throws IOException {
        if (String.class == utils.serializer().dataType()) {
            return (T) utils.serializer().deserialize(bodyAsString(), type);
        } else if (byte[].class == utils.serializer().dataType()) {
            return (T) utils.serializer().deserialize(bodyAsBytes(), type);
        } else {
            throw new SolonException("Invalid serializer type!");
        }
    }

    @Override
    public HttpResponseException createError() {
        try {
            return new HttpResponseException(this, response.request().method(), response.request().uri().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, List<String>> headerMap;
    @Override
    public Map<String, List<String>> headerMap() {
        if (headerMap == null) {
            headerMap = new LinkedHashMap<>();
            for (String name : headerNames()) {
                headerMap.put(name, headers(name));
            }
        }

        return headerMap;
    }

    @Override
    public void close() throws IOException {
        body().close();
    }
}