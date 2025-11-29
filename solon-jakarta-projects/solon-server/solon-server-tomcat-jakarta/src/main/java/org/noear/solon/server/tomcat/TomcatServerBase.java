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
package org.noear.solon.server.tomcat;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.ErrorReportValve;
import org.noear.solon.Utils;
import org.noear.solon.lang.Nullable;
import org.noear.solon.server.ServerConstants;
import org.noear.solon.server.ServerLifecycle;
import org.noear.solon.server.http.HttpServerConfigure;
import org.noear.solon.server.prop.impl.HttpServerProps;
import org.noear.solon.server.ssl.SslConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Yukai
 * @author noear
 * @since 2022/8/26 17:01
 * @since 3.6
 **/
public abstract class TomcatServerBase implements ServerLifecycle, HttpServerConfigure {
    static final Logger log = LoggerFactory.getLogger(TomcatServerBase.class);

    protected Tomcat _server;
    protected final HttpServerProps props;
    protected SslConfig sslConfig = new SslConfig(ServerConstants.SIGNAL_HTTP);

    protected Set<Integer> addHttpPorts = new LinkedHashSet<>();

    protected boolean enableHttp2 = false;

    public TomcatServerBase(HttpServerProps props) {
        this.props = props;
    }

    /**
     * 是否允许Ssl
     */
    @Override
    public void enableSsl(boolean enable, @Nullable SSLContext sslContext) {
        sslConfig.set(enable, sslContext);
    }

    @Override
    public boolean isSupportedHttp2() {
        return true;
    }

    @Override
    public void enableHttp2(boolean enable) {
        this.enableHttp2 = enable;
    }

    public boolean isEnableHttp2() {
        return enableHttp2;
    }

    /**
     * 添加 HttpPort（当 ssl 时，可再开个 http 端口）
     */
    @Override
    public void addHttpPort(int port) {
        addHttpPorts.add(port);
    }

    @Override
    public void setExecutor(Executor executor) {
        log.warn("Tomcat does not support user-defined executor");
    }

    public HttpServerProps getProps() {
        return props;
    }


    @Override
    public void start(String host, int port) throws Throwable {
        _server = new Tomcat();

        if (Utils.isNotEmpty(host)) {
            _server.setHostname(host);
        }

        //初始化上下文
        Context ctxt = initContext();

        //添加连接端口
        addConnector(port, true);

        //http add
        for (Integer portAdd : addHttpPorts) {
            addConnector(portAdd, false);
        }
        
        //jsp
        addJasperInitializerIfExists(ctxt);
//        errorReportValve(ctxt,false);
        
        _server.start();
    }


    @Override
    public void stop() throws Throwable {
        if (_server != null) {
            _server.destroy();
            _server = null;
        }
    }
    
    /**
     * 使用反射动态检查JasperInitializer类是否存在，并添加到StandardContext中
     */
    private void addJasperInitializerIfExists(Context ctxt) {
        try {
            // 1. 检查JasperInitializer类是否存在
            Class<?> jasperClass = Class.forName("org.apache.jasper.servlet.JasperInitializer");
            log.debug("JasperInitializer类存在，准备创建实例");
            
            addDefaultServlet(ctxt);
            addJspServlet(ctxt);
            // 2. 获取构造函数并创建实例
            Constructor<?> constructor = jasperClass.getDeclaredConstructor();
            Object jasperInstance = constructor.newInstance();
            // 3. 反射调用addServletContainerInitializer方法
            Method addMethod = Context.class.getMethod(
                "addServletContainerInitializer", 
                jakarta.servlet.ServletContainerInitializer.class, 
                java.util.Set.class
            );
            addMethod.invoke(ctxt, jasperInstance, null);
            
            log.debug("已成功添加JasperInitializer到ServletContext");
            // Configure the defaults and then tweak the JSP servlet settings
            // Note: Min value for maxLoadedJsps is 2
//            Tomcat.initWebappDefaults(ctxt);
        } catch (ClassNotFoundException e) {
        	log.debug("JasperInitializer类不存在: " + e.getMessage());
        	log.debug("当前环境可能不支持JSP");
//        	e.printStackTrace();
        } catch (Exception e) {
        	log.debug("执行过程中出现错误: " + e.getMessage());
//            e.printStackTrace();
        }
    }
    
    private void addDefaultServlet(Context context) {
		Wrapper defaultServlet = context.createWrapper();
		defaultServlet.setName("default");
		defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
		defaultServlet.addInitParameter("debug", "0");
		defaultServlet.addInitParameter("listings", "false");
		defaultServlet.setLoadOnStartup(1);
		
		defaultServlet.setOverridable(true);
		context.addChild(defaultServlet);
		context.addServletMappingDecoded("/", "default");
	}
    
    private void addJspServlet(Context context) {
		Wrapper jspServlet = context.createWrapper();
		jspServlet.setName("jsp");
		jspServlet.setServletClass("org.apache.jasper.servlet.JspServlet");
		jspServlet.addInitParameter("fork", "false");
		jspServlet.addInitParameter("xpoweredBy", "false");
		jspServlet.setLoadOnStartup(3);
		context.addChild(jspServlet);
		context.addServletMappingDecoded("*.jsp", "jsp");
		context.addServletMappingDecoded("*.jspx", "jsp");
	}
    

    protected abstract Context initContext() throws IOException;

    protected abstract void addConnector(int port, boolean isMain) throws IOException;
    
    protected void errorReportValve(Context ctxt,boolean flag) {
    	if(flag) {
    		return;
    	}
    	 //以下是屏蔽tomcat的版本信息和具体报错信息输出到页面
        ErrorReportValve valve = new ErrorReportValve();
		valve.setShowServerInfo(false);
		valve.setShowReport(false);
		ctxt.getParent().getPipeline().addValve(valve);
    }
}