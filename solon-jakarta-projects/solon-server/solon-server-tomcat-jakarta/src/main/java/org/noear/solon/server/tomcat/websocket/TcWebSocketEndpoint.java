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

import java.nio.ByteBuffer;

import org.noear.solon.net.websocket.WebSocket;
import org.noear.solon.net.websocket.WebSocketRouter;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;

/**
 * Tomcat WebSocket端点实现
 *
 * @author 小xu中年
 * @since 3.7
 */
public class TcWebSocketEndpoint extends Endpoint {
    private final String SESSION_KEY = "session";
    private static final WebSocketRouter webSocketRouter = WebSocketRouter.getInstance();

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        WebSocket socket = new WebSocketImpl(session);
        session.getUserProperties().put(SESSION_KEY, socket);
        session.addMessageHandler(new BufferMessageHandler(socket));
        session.addMessageHandler(new TextMessageHandler(socket));
        
        // 触发onOpen事件
        webSocketRouter.getListener().onOpen(socket);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        WebSocket socket = (WebSocket) session.getUserProperties().get(SESSION_KEY);
        if (socket != null) {
            webSocketRouter.getListener().onClose(socket);
        }
    }

    @Override
    public void onError(Session session, Throwable thr) {
        WebSocket socket = (WebSocket) session.getUserProperties().get(SESSION_KEY);
        if (socket != null) {
            webSocketRouter.getListener().onError(socket, thr);
        }
    }

    private static class TextMessageHandler implements MessageHandler.Whole<String> {
        private final WebSocket socket;

        public TextMessageHandler(WebSocket socket) {
            this.socket = socket;
        }

        @Override
        public void onMessage(String s) {
            try {
                webSocketRouter.getListener().onMessage(socket, s);
            } catch (Throwable e) {
                webSocketRouter.getListener().onError(socket, e);
            }
        }
    }

    private static class BufferMessageHandler implements MessageHandler.Whole<ByteBuffer> {
        private final WebSocket socket;

        public BufferMessageHandler(WebSocket socket) {
            this.socket = socket;
        }

        @Override
        public void onMessage(ByteBuffer s) {
            try {
                webSocketRouter.getListener().onMessage(socket, s);
            } catch (Throwable e) {
                webSocketRouter.getListener().onError(socket, e);
            }
        }
    }
}