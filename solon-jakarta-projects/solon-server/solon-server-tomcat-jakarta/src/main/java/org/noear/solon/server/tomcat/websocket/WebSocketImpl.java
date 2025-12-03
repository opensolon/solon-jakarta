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
package org.noear.solon.server.tomcat.websocket;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import org.noear.solon.net.websocket.WebSocketBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 *
 * @author 小xu中年
 * @since 3.7
 * */
public class WebSocketImpl extends WebSocketBase {
    private static final Logger log = LoggerFactory.getLogger(WebSocketImpl.class);
    private Session real;

    public WebSocketImpl(Session real) {
        this.real = real;
        this.init(real.getRequestURI());
    }

    @Override
    public boolean isValid() {
        return isClosed() == false && real.isOpen();
    }

    @Override
    public boolean isSecure() {
        return real.isSecure();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        // Tomcat的Session不直接提供远程地址，这里返回null
        return null;
    }

    @Override
    public InetSocketAddress localAddress() {
        // Tomcat的Session不直接提供本地地址，这里返回null
        return null;
    }

    @Override
    public long getIdleTimeout() {
        return real.getMaxIdleTimeout();
    }

    @Override
    public void setIdleTimeout(long idleTimeout) {
        real.setMaxIdleTimeout(idleTimeout);
    }

    @Override
    public Future<Void> send(String text) {
        return real.getAsyncRemote().sendText(text);
    }

    @Override
    public Future<Void> send(ByteBuffer binary) {
        return real.getAsyncRemote().sendBinary(binary);
    }

    @Override
    public void close() {
        super.close();
        try {
            real.close();
        } catch (Throwable ignore) {
            if (log.isDebugEnabled()) {
                log.debug("Close failure: {}", ignore.getMessage());
            }
        }
    }

    @Override
    public void close(int code, String reason) {
        super.close(code, reason);
        try {
            real.close(new CloseReason(CloseReason.CloseCodes.getCloseCode(code), reason));
        } catch (Throwable ignore) {
            if (log.isDebugEnabled()) {
                log.debug("Close failure: {}", ignore.getMessage());
            }
        }
    }
}