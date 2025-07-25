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
package org.noear.solon.web.webservices.integration;

import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.web.webservices.WebServiceReference;

import jakarta.jws.WebService;
import jakarta.servlet.ServletContainerInitializer;

/**
 * @author noear
 * @since 1.0
 */
public class WebServicePlugin implements Plugin {
    @Override
    public void start(AppContext context) throws Throwable {
        WebServiceBeanBuilder wsBeanBuilder = new WebServiceBeanBuilder();

        //注册 Bean
        context.wrapAndPut(ServletContainerInitializer.class, new WebServiceContainerInitializerImpl());
        context.wrapAndPut(WebServiceBeanBuilder.class, wsBeanBuilder);

        //添加 Anno 处理
        context.beanBuilderAdd(WebService.class, wsBeanBuilder);
        context.beanInjectorAdd(WebServiceReference.class, new WebServiceReferenceBeanInjector());
    }
}
