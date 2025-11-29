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

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.jasper.servlet.JspServlet;
import org.noear.solon.server.prop.impl.HttpServerProps;

import java.io.IOException;

/**
 * @author noear 2025/11/29 created
 */
public class TomcatServerJsp extends TomcatServer {
    public TomcatServerJsp(HttpServerProps props) {
        super(props);
    }

    @Override
    protected void initContext() throws IOException {
        Context ctx = getContext();

        //jsp
        try {
            addJspServlet(ctx);

            JasperInitializer jasperInstance = new JasperInitializer();
            ctx.addServletContainerInitializer(jasperInstance, null);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    private void addJspServlet(Context context) {
        Wrapper jspServlet = context.createWrapper();
        jspServlet.setName("jsp");
        jspServlet.setServlet(new JspServlet());
        jspServlet.addInitParameter("fork", "false");
        jspServlet.addInitParameter("xpoweredBy", "false");
        jspServlet.setLoadOnStartup(3);

        context.addChild(jspServlet);
        context.addServletMappingDecoded("*.jsp", "jsp");
        context.addServletMappingDecoded("*.jspx", "jsp");
    }
}