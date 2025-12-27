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

import org.noear.solon.Utils;
import org.noear.solon.core.util.*;
import org.noear.solon.net.http.HttpException;
import org.noear.solon.net.http.HttpResponse;
import org.noear.solon.net.http.HttpUtils;
import org.noear.solon.net.http.impl.AbstractHttpUtils;
import org.noear.solon.net.http.impl.HttpSslSupplierDefault;
import org.noear.solon.net.http.impl.HttpStream;
import org.noear.solon.net.http.impl.HttpUploadFile;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

// Java 11+ HTTP Client imports
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import javax.net.ssl.SSLContext;

/**
 * Http 工具 JDK HttpClient 实现 (基于 Java 11+ java.net.http.HttpClient)
 *
 * @author noear
 * @since 3.8.1
 */
public class JdkHttpUtils extends AbstractHttpUtils implements HttpUtils {
    static final Set<String> METHODS_NOBODY;

    static {
        METHODS_NOBODY = new HashSet<>(3); //es-GET 会有 body
        METHODS_NOBODY.add("HEAD");
        METHODS_NOBODY.add("TRACE");
        METHODS_NOBODY.add("OPTIONS");
    }

    protected static final JdkHttpDispatcherLoader dispatcherLoader = new JdkHttpDispatcherLoader(); //这是连接池定义

    public JdkHttpUtils(String url) {
        super(url);
    }

    private HttpStream _bodyRaw;

    /**
     * 设置 BODY txt 及内容类型
     */
    @Override
    public HttpUtils body(String txt, String contentType) {
        if (txt != null) {
            body(txt.getBytes(_charset), contentType);
        }

        return this;
    }

    @Override
    public HttpUtils bodyOfBean(Object obj) throws HttpException {
        Object tmp;
        try {
            tmp = serializer().serialize(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        if (tmp instanceof String) {
            body((String) tmp, serializer().mimeType());
        } else if (tmp instanceof byte[]) {
            body((byte[]) tmp, serializer().mimeType());
        } else {
            throw new IllegalArgumentException("Invalid serializer type!");
        }

        return this;
    }

    @Override
    public HttpUtils body(byte[] bytes, String contentType) {
        if (bytes != null) {
            body(new ByteArrayInputStream(bytes), contentType);
        }

        return this;
    }

    @Override
    public HttpUtils body(InputStream raw, String contentType) {
        if (raw != null) {
            _bodyRaw = new HttpStream(raw, contentType);
        }

        return this;
    }

    @Override
    protected HttpResponse execDo(String _method, CompletableFuture<HttpResponse> future) throws IOException {
        String method = _method.toUpperCase();
        String newUrl = urlRebuild(method, _url, _charset);

        // 创建 HttpClient
        HttpClient client = getClient();

        // 构建 HttpRequest
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(newUrl))
                .timeout(java.time.Duration.ofMillis(getTimeoutMillis()))
                .method(method, buildRequestBody(method));

        // 添加 headers
        if (_headers != null) {
            for (KeyValues<String> kv : _headers) {
                for (String val : kv.getValues()) {
                    requestBuilder.header(kv.getKey(), val);
                }
            }
        }

        // 添加 cookies
        if (_cookies != null) {
            requestBuilder.header("Cookie", getRequestCookieString(_cookies));
        }

        HttpRequest request = requestBuilder.build();

        if (future == null) {
            return request(client, request, method);
        } else {
            // 异步执行
            dispatcherLoader.getDispatcher().submit(() -> {
                try {
                    HttpResponse resp = request(client, request, method);
                    future.complete(resp);
                } catch (IOException | RuntimeException e) {
                    future.completeExceptionally(e);
                }
            });

            return null;
        }
    }

    protected HttpClient getClient() throws IOException {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL);

        if (_timeout != null) {
            // 连接超时在请求级别设置，这里设置连接池等
        }

        if (_sslSupplier == null) {
            _sslSupplier = HttpSslSupplierDefault.getInstance();
        }

        if (_proxy != null) {
            builder.proxy(ProxySelector.of((InetSocketAddress) _proxy.address()));
        }

        // 设置 SSL 配置
        if (_sslSupplier != null) {
            builder.sslContext(_sslSupplier.getSslContext());
        }

        return builder.build();
    }

    private long getTimeoutMillis() {
        if (_timeout != null && _timeout.getReadTimeout() != null) {
            return _timeout.getReadTimeout().toMillis();
        }
        return 60000; // 默认60秒
    }

    private BodyPublisher buildRequestBody(String method) throws IOException {
        if (METHODS_NOBODY.contains(method)) {
            return BodyPublishers.noBody();
        }

        if (_bodyRaw != null) {
            // 使用 bodyRaw
            byte[] content = IoUtil.transferToBytes(_bodyRaw.getContent());
            return BodyPublishers.ofByteArray(content);
        } else if (_multipart) {
            // 构建 multipart body
            return buildMultipartBody();
        } else if (Utils.isEmpty(_params) == false && "GET".equals(method) == false) {
            // 构建 form body
            return buildFormBody();
        } else {
            return BodyPublishers.noBody();
        }
    }

    private BodyPublisher buildFormBody() throws IOException {
        if (_params == null || _params.isEmpty()) {
            return BodyPublishers.noBody();
        }

        StringBuilder builder = new StringBuilder();
        for (KeyValues<String> kv : _params) {
            for (Object val : kv.getValues()) {
                if (builder.length() > 0) {
                    builder.append("&");
                }
                try {
                    builder.append(HttpUtils.urlEncode(kv.getKey(), _charset.name()));
                    builder.append("=");
                    builder.append(HttpUtils.urlEncode(String.valueOf(val), _charset.name()));
                } catch (UnsupportedEncodingException e) {
                    // This should never happen as charset names are validated
                    throw new RuntimeException(e);
                }
            }
        }

        String data = builder.toString();
        return BodyPublishers.ofString(data);
    }

    private BodyPublisher buildMultipartBody() throws IOException {
        if (_files == null && (_params == null || _params.isEmpty())) {
            return BodyPublishers.noBody();
        }

        // 创建 boundary
        String boundary = Long.toHexString(System.currentTimeMillis());

        // 构建 multipart 内容
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, _charset), true);

        if (_files != null) {
            for (KeyValues<HttpUploadFile> kv : _files) {
                for (HttpUploadFile val : kv.getValues()) {
                    appendPartFile(writer, outputStream, kv.getKey(), val, boundary);
                }
            }
        }

        if (_params != null) {
            for (KeyValues<String> kv : _params) {
                for (String val : kv.getValues()) {
                    appendPartText(writer, kv.getKey(), val, boundary);
                }
            }
        }

        writer.append("--").append(boundary).append("--").append("\r\n").flush();

        byte[] content = outputStream.toByteArray();
        return BodyPublishers.ofByteArray(content);
    }

    private void appendPartFile(PrintWriter writer, ByteArrayOutputStream outputStream, String key, HttpUploadFile value, String boundary) throws IOException {
        writer.append("--").append(boundary).append("\r\n");
        try {
            writer.append(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"", 
                    HttpUtils.urlEncode(key, _charset.name()), value.fileName));
        } catch (UnsupportedEncodingException e) {
            // This should never happen as charset names are validated
            throw new RuntimeException(e);
        }
        writer.append("\r\n");
        if (value.fileStream.getContentType() != null) {
            writer.append("Content-Type: ").append(value.fileStream.getContentType()).append("\r\n");
        }
        writer.append("Content-Transfer-Encoding: binary").append("\r\n");
        writer.append("\r\n").flush();

        // 写入文件内容
        try (InputStream ins = value.fileStream.getContent()) {
            byte[] fileContent = IoUtil.transferToBytes(ins);
            outputStream.write(fileContent);
        }

        writer.append("\r\n").flush();
    }

    private void appendPartText(PrintWriter writer, String key, String value, String boundary) throws IOException {
        writer.append("--").append(boundary).append("\r\n");
        try {
            writer.append(String.format("Content-Disposition: form-data; name=\"%s\"", HttpUtils.urlEncode(key, _charset.name())));
        } catch (UnsupportedEncodingException e) {
            // This should never happen as charset names are validated
            throw new RuntimeException(e);
        }
        writer.append("\r\n");
        writer.append("\r\n").flush();

        writer.append(value).flush();

        writer.append("\r\n").flush();
    }

    protected HttpResponse request(HttpClient client, HttpRequest request, String method) throws IOException {
        try {
            java.net.http.HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());
            return new JdkHttpResponse(this, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    protected String urlRebuild(String method, String url, Charset charset) {
        int pathOf = url.indexOf("://");
        int queryOf = url.indexOf("?");

        String schema = url.substring(0, pathOf + 3);
        String hostAndPath = queryOf > 0 ? url.substring(pathOf + 3, queryOf) : url.substring(pathOf + 3);
        String query = queryOf > 0 ? url.substring(queryOf) : "";

        if (hostAndPath.length() > 0) {
            try {
                String hostAndPath0 = URLDecoder.decode(hostAndPath, charset.name());

                if (hostAndPath.equals(hostAndPath0)) {
                    hostAndPath = HttpUtils.urlEncode(hostAndPath, charset.name());
                    hostAndPath = hostAndPath.replace("%2F", "/").replace("%3A", ":");
                }
            } catch (UnsupportedEncodingException e) {
                // This should never happen as charset names are validated
                throw new RuntimeException(e);
            }
        }

        if (query.length() > 0) {
            try {
                String query0 = URLDecoder.decode(query, charset.name());
                if (query.equals(query0)) {
                    query = HttpUtils.urlEncode(query, charset.name());
                    query = query.replace("%3F", "?")
                            .replace("%2F", "/")
                            .replace("%3A", ":")
                            .replace("%3D", "=")
                            .replace("%26", "&")
                            .replace("%40", "@")
                            .replace("%23", "#");
                }
            } catch (UnsupportedEncodingException e) {
                // This should never happen as charset names are validated
                throw new RuntimeException(e);
            }
        }

        StringBuilder newUrl = new StringBuilder();
        newUrl.append(schema);
        newUrl.append(hostAndPath);
        newUrl.append(query);

        if (_params != null && "GET".equals(method)) {
            for (KeyValues<String> kv : _params) {
                String key;
                try {
                    key = HttpUtils.urlEncode(kv.getKey(), charset.name());
                } catch (UnsupportedEncodingException e) {
                    // This should never happen as charset names are validated
                    throw new RuntimeException(e);
                }
                for (String val : kv.getValues()) {
                    if (val != null) {
                        if (newUrl.indexOf("?") < 0) {
                            newUrl.append("?");
                        } else {
                            newUrl.append("&");
                        }
                        try {
                            newUrl.append(key).append("=").append(HttpUtils.urlEncode(val, charset.name()));
                        } catch (UnsupportedEncodingException e) {
                            // This should never happen as charset names are validated
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            _params.clear();
        }

        return newUrl.toString();
    }
}