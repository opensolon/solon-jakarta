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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.jasper.servlet.JasperInitializer;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.AppClassLoader;
import org.noear.solon.core.runtime.NativeDetector;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.server.prop.impl.HttpServerProps;
import org.noear.solon.server.tomcat.jsp.JspTldLocator;

import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.descriptor.TaglibDescriptor;
import org.noear.solon.server.util.DebugUtils;

/**
 * @author noear 2025/11/29 created
 */
public class TomcatServerJsp extends TomcatServer {
    public TomcatServerJsp(HttpServerProps props) {
        super(props);
    }

    @Override
    protected Context initContext() throws IOException {
        Context ctx = getContext();
        
        //jsp
        try {
        	String resRoot = getResourceRoot();
            ctx.setDocBase(resRoot);
        	addJspSupport(ctx);
        	addTldSupport(ctx);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
        return ctx;
    }

    
    private void addJspSupport(Context context) throws IOException {
        // 注册JSP Servlet（Tomcat需要显式注册JspServlet）
        Wrapper jspServlet = Tomcat.addServlet(context, "jsp", "org.apache.jasper.servlet.JspServlet");
        jspServlet.addInitParameter("fork", "false");
        jspServlet.addInitParameter("xpoweredBy", "false");
        jspServlet.setLoadOnStartup(3);
//        jspServlet.addInitParameter("development", "true"); // 开发模式，便于调试
        
        // 设置JSP文件映射
        context.addServletMappingDecoded("*.jsp", "jsp");
        context.addServletMappingDecoded("*.jspx", "jsp");
        
        JasperInitializer jasperInstance = new JasperInitializer();
        context.addServletContainerInitializer(jasperInstance, null);
    }
    
    private void addTldSupport(Context ctx) throws IOException {
        // 扫描TLD文件并注册
        Map<String, TaglibDescriptor> tagLibInfos = JspTldLocator.createTldInfos("WEB-INF", "templates");
        
        if (tagLibInfos.size()>0) {
        	List<TaglibDescriptor> taglibs = new ArrayList<>(tagLibInfos.values());
            JspConfigDescriptor jspConfigDescriptor = new org.apache.tomcat.util.descriptor.web.JspConfigDescriptorImpl(
                new ArrayList<>(), taglibs);
            ctx.setJspConfigDescriptor(jspConfigDescriptor);
//            ctx.getServletContext().setAttribute("jakarta.servlet.context.tldScan", true);
        }
    }

    private String getResourceRoot() throws FileNotFoundException {
        URL rootURL = getRootPath();

        if (rootURL == null) {
            if (NativeDetector.inNativeImage()) {
                return "";
            }

            throw new FileNotFoundException("Unable to find root");
        }

        if (Solon.cfg().isDebugMode() && Solon.cfg().isFilesMode()) {
            File dir = DebugUtils.getDebugLocation(AppClassLoader.global(), "/");
            if (dir != null) {
                return dir.toURI().getPath();
            }
        }

        return rootURL.getPath();
    }

    private URL getRootPath() {
        URL root = ResourceUtil.getResource("/");
        if (root != null) {
            return root;
        }

        try {
            URL temp = ResourceUtil.getResource(""); //有些环境，/ 取不到根
            if (temp == null) {
                return null;
            }

            String path = temp.toString();
            if (path.startsWith("jar:")) {
                int endIndex = path.indexOf("!");
                path = path.substring(0, endIndex + 1) + "/";
            } else {
                return null;
            }
            return new URL(path);
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
}