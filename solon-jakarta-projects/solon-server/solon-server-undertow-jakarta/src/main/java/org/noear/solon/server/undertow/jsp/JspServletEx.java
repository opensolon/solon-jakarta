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
package org.noear.solon.server.undertow.jsp;

import java.io.IOException;

import org.apache.jasper.servlet.JspServlet;
import org.noear.solon.core.handle.Context;

import io.undertow.servlet.api.ServletInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class JspServletEx extends JspServlet {

    public static ServletInfo createServlet(String name, String path) {
        ServletInfo servlet = new ServletInfo(name, JspServletEx.class);
        servlet.addMapping(path);
        servlet.setRequireWelcomeFileMapping(true);
        return servlet;
    }


    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        if(Context.current() == null){
            return;
        }

        super.service(req, res);
    }
}
