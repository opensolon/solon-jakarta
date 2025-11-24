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
package org.noear.solon.serialization.jackson3.xml.integration;

import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.serialization.SerializerNames;
import org.noear.solon.serialization.jackson3.xml.*;
import org.noear.solon.serialization.prop.JsonProps;

/**
 * Xml XPluginImp
 *
 * @author painter
 * @since 2.8
 */
public class SerializationJackson3XmlPlugin implements Plugin {

    @Override
    public void start(AppContext context) {
        JsonProps jsonProps = JsonProps.create(context);

        //::serializer
        Jackson3XmlStringSerializer serializer = new Jackson3XmlStringSerializer(jsonProps);
        context.wrapAndPut(Jackson3XmlStringSerializer.class, serializer); //用于扩展
        context.app().serializers().register(SerializerNames.AT_XML, serializer);

        //::entityConverter
        Jackson3XmlEntityConverter entityConverter = new Jackson3XmlEntityConverter(serializer);
        context.wrapAndPut(Jackson3XmlEntityConverter.class, entityConverter); //用于扩展

        //会自动转为 executor, renderer
        context.app().chains().addEntityConverter(entityConverter);


        //===> 以下将弃用 v3.6

        //::renderFactory
        //绑定属性
        Jackson3XmlRenderFactory renderFactory = new Jackson3XmlRenderFactory(entityConverter);
        context.wrapAndPut(Jackson3XmlRenderFactory.class, renderFactory); //用于扩展

        //支持 xml 内容类型执行
        Jackson3XmlActionExecutor actionExecutor = new Jackson3XmlActionExecutor(entityConverter);
        context.wrapAndPut(Jackson3XmlActionExecutor.class, actionExecutor); //用于扩展


        //::renderTypedFactory
        Jackson3XmlRenderTypedFactory renderTypedFactory = new Jackson3XmlRenderTypedFactory();
        context.wrapAndPut(Jackson3XmlRenderTypedFactory.class, renderTypedFactory); //用于扩展
        context.app().renders().register(renderTypedFactory);
        context.app().serializers().register(SerializerNames.AT_XML_TYPED, renderTypedFactory.getSerializer());
    }
}