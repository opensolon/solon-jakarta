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
package org.noear.solon.server.undertow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.tomcat.InstanceManager;
import org.noear.solon.core.AppClassLoader;
import org.noear.solon.server.prop.impl.HttpServerProps;
import org.noear.solon.server.undertow.http.UtHttpContextServletHandler;
import org.noear.solon.server.undertow.jsp.JspResourceManager;
import org.noear.solon.server.undertow.jsp.JspServletEx;
import org.noear.solon.server.undertow.jsp.JspTldLocator;

import io.undertow.jsp.HackInstanceManager;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.api.ServletInfo;

/**
 * @author by: Yukai
 * @since: 2019/3/28 15:50
 */
public class UndertowServerAddJsp extends UndertowServer {

    public UndertowServerAddJsp(HttpServerProps props) {
        super(props);
    }

    @Override
    protected HttpHandler buildHandler() throws Exception {
        DeploymentInfo builder = initDeploymentInfo();

        //添加jsp处理
        String fileRoot = getResourceRoot();
        ServletInfo jspServlet = JspServletEx.createServlet("JSPServlet", "*.jsp");
        jspServlet.addInitParam("fork", "false");
        jspServlet.addInitParam("xpoweredBy", "false");
        jspServlet.addInitParam("development", "false"); 
        
        builder.setResourceManager(new JspResourceManager(AppClassLoader.global(), fileRoot))
                .addServlet(new ServletInfo("ACTServlet", UtHttpContextServletHandler.class).addMapping("/").setAsyncSupported(true))
                .addServlet(jspServlet);


        //添加taglib支持
        Map<String, TagLibraryInfo> tagLibraryMap = JspTldLocator.createTldInfos("WEB-INF", "templates");
        setupDeployment(builder, new HashMap<String, JspPropertyGroup>(), tagLibraryMap, new HackInstanceManager());


        //开始部署
        final ServletContainer container = ServletContainer.Factory.newInstance();
        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();

        return manager.start();
    }
    
    private static void setupDeployment(final DeploymentInfo deploymentInfo, final Map<String, JspPropertyGroup> propertyGroups, final Map<String, TagLibraryInfo> tagLibraries, final InstanceManager instanceManager) {
        deploymentInfo.addServletContextAttribute("org.apache.jasper.SERVLET_VERSION", deploymentInfo.getMajorVersion() + "." + deploymentInfo.getMinorVersion());
        deploymentInfo.addServletContextAttribute("org.apache.jasper.JSP_PROPERTY_GROUPS", propertyGroups);
        deploymentInfo.addServletContextAttribute("org.apache.jasper.JSP_TAG_LIBRARIES", tagLibraries);
        deploymentInfo.addServletContextAttribute(InstanceManager.class.getName(), instanceManager);
        deploymentInfo.addServletContainerInitializers(new ServletContainerInitializerInfo(
                JasperInitializer.class,
                Collections.emptySet()
        ));
    }
}
