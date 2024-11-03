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

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.boot.ServerLifecycle;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.util.ClassUtil;

import java.io.IOException;

class JettyServer extends JettyServerBase implements ServerLifecycle {
    protected Server _server = null;

    @Override
    public void start(String host, int port) throws Throwable {
        setup(Solon.app(), host, port);

        _server.start();
    }

    @Override
    public void stop() throws Throwable {
        if (_server != null) {
            _server.stop();
            _server = null;
        }
    }

    protected void setup(SolonApp app, String host, int port) throws IOException {
        Class<?> wsClz = ClassUtil.loadClass("org.eclipse.jetty.websocket.server.WebSocketHandler");


        QueuedThreadPool threadPool = new QueuedThreadPool(
                props.getMaxThreads(props.isIoBound()),
                props.getCoreThreads());

        _server = new Server(threadPool);


        //http or https
        _server.addConnector(getConnector(_server, host, port, true));

        //http add
        for (Integer portAdd : addHttpPorts) {
            _server.addConnector(getConnector(_server, host, portAdd, false));
        }

        //session 支持
        if (Solon.app().enableSessionState()) {
            _server.setSessionIdManager(new DefaultSessionIdManager(_server));
        }

        if (app.enableWebSocket() && wsClz != null) {
            _server.setHandler(new HandlerHub(buildHandler()));
        } else {
            //没有ws包 或 没有开启
            _server.setHandler(buildHandler());
        }

        //1.1:分发事件（充许外部扩展）
        EventBus.publish(_server);
    }

    /**
     * 获取Server Handler
     */
    protected Handler buildHandler() throws IOException {
        return getServletHandler();
    }
}
