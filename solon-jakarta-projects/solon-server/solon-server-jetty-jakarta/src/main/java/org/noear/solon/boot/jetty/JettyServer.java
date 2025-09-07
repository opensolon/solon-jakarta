/*
 * Copyright 2017-2024 noear.org and authors
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
package org.noear.solon.boot.jetty;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.session.DefaultSessionIdManager;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler;
import org.noear.solon.boot.ServerLifecycle;
import org.noear.solon.boot.jetty.websocket.WebSocketCreatorImpl;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.util.ClassUtil;

import java.io.IOException;

public class JettyServer extends JettyServerBase implements ServerLifecycle {
    protected Server real = null;

    protected boolean enableWebSocket;

    public void enableWebSocket(boolean enableWebSocket) {
        this.enableWebSocket = enableWebSocket;
    }

    @Override
    public void start(String host, int port) throws Throwable {
        setup(host, port);

        real.start();
    }

    @Override
    public void stop() throws Throwable {
        if (real != null) {
            real.stop();
            real = null;
        }
    }

    protected void setup(String host, int port) throws IOException {
        QueuedThreadPool threadPool = new QueuedThreadPool(
                props.getMaxThreads(props.isIoBound()),
                props.getCoreThreads());

        real = new Server(threadPool);


        //http or https
        real.addConnector(getConnector(real, host, port, true));

        //http add
        for (Integer portAdd : addHttpPorts) {
            real.addConnector(getConnector(real, host, portAdd, false));
        }

        //session 支持
        if (enableSessionState) {
            real.addBean(new DefaultSessionIdManager(real));
        }

        ServletContextHandler contextHandler = buildHandler();
        real.setHandler(contextHandler);

        if (enableWebSocket && ClassUtil.hasClass(() -> UpgradeRequest.class)) {
            //real.setHandler(new HandlerHub(buildHandler()));
            WebSocketUpgradeHandler wsHandler = WebSocketUpgradeHandler.from(real, contextHandler);
            wsHandler.getServerWebSocketContainer().addMapping("/*", new WebSocketCreatorImpl());
            //real.setHandler(wsHandler);
        }

        //1.1:分发事件（充许外部扩展）
        EventBus.publish(real);
    }

    /**
     * 获取Server Handler
     */
    protected ServletContextHandler buildHandler() throws IOException {
        return getServletHandler();
    }
}
