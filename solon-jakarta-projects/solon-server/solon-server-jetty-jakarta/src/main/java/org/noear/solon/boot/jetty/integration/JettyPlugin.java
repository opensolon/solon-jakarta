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
package org.noear.solon.boot.jetty.integration;

import org.eclipse.jetty.ee11.jsp.JettyJspServlet;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.boot.ServerConstants;
import org.noear.solon.boot.ServerProps;
import org.noear.solon.boot.jetty.JettyServer;
import org.noear.solon.boot.jetty.JettyServerAddJsp;
import org.noear.solon.boot.prop.impl.HttpServerProps;
import org.noear.solon.boot.prop.impl.WebSocketServerProps;
import org.noear.solon.core.*;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.util.ClassUtil;

import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JettyPlugin implements Plugin {
    static final Logger log = LoggerFactory.getLogger(JettyPlugin.class);

    private static Signal _signal;

    public static Signal signal() {
        return _signal;
    }

    private JettyServer _server = null;


    public static String solon_boot_ver() {
        return "jetty 12.1/" + Solon.version();
    }


    @Override
    public void start(AppContext context) {
        if (context.app().enableHttp() == false) {
            return;
        }

        context.beanBuilderAdd(WebFilter.class, (clz, bw, ano) -> {
        });
        context.beanBuilderAdd(WebServlet.class, (clz, bw, ano) -> {
        });
        context.beanBuilderAdd(WebListener.class, (clz, bw, ano) -> {
        });

        context.lifecycle(ServerConstants.SIGNAL_LIFECYCLE_INDEX, () -> {
            start0(context);
        });
    }

    private void start0(AppContext context) throws Throwable {
        //初始化属性
        ServerProps.init();

        if (org.noear.solon.server.ServerProps.request_maxBodySize > 0) {
            System.setProperty("org.eclipse.jetty.server.Request.maxFormContentSize",
                    String.valueOf(org.noear.solon.server.ServerProps.request_maxBodySize));
        }

        if (ClassUtil.hasClass(() -> JettyJspServlet.class)) {
            _server = new JettyServerAddJsp();
        } else {
            _server = new JettyServer();
        }

        _server.enableWebSocket(context.app().enableWebSocket());
        _server.enableSessionState(context.app().enableSessionState());

        HttpServerProps props = _server.getProps();
        final String _host = props.getHost();
        final int _port = props.getPort();
        final String _name = props.getName();

        long time_start = System.currentTimeMillis();

        EventBus.publish(_server);
        _server.start(_host, _port);


        final String _wrapHost = props.getWrapHost();
        final int _wrapPort = props.getWrapPort();
        _signal = new SignalSim(_name, _wrapHost, _wrapPort, "http", SignalType.HTTP);

        context.app().signalAdd(_signal);

        long time_end = System.currentTimeMillis();

        String connectorInfo = "Connector:main: jetty: Started ServerConnector@{HTTP/1.1,[http/1.1]";
        if (context.app().enableWebSocket()) {
            //有名字定义时，添加信号注册
            WebSocketServerProps wsProps = WebSocketServerProps.getInstance();
            if (Utils.isNotEmpty(wsProps.getName())) {
                SignalSim wsSignal = new SignalSim(wsProps.getName(), _wrapHost, _wrapPort, "ws", SignalType.WEBSOCKET);
                context.app().signalAdd(wsSignal);
            }

            String wsServerUrl = props.buildWsServerUrl(_server.isSecure());
            log.info(connectorInfo + "[WebSocket]}{" + wsServerUrl + "}");
        }

        String httpServerUrl = props.buildHttpServerUrl(_server.isSecure());
        log.info(connectorInfo + "}{" + httpServerUrl + "}");
        log.info("Server:main: jetty: Started (" + solon_boot_ver() + ") @" + (time_end - time_start) + "ms");
    }

    @Override
    public void stop() throws Throwable {
        if (_server != null) {
            _server.stop();
            _server = null;

            log.info("Server:main: jetty: Has Stopped (" + solon_boot_ver() + ")");
        }
    }
}
