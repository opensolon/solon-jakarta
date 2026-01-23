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
package org.noear.solon.serialization.jackson3.xml;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.util.LazyReference;
import org.noear.solon.core.wrap.MethodWrap;
import org.noear.solon.core.wrap.ParamWrap;
import org.noear.solon.serialization.AbstractStringEntityConverter;
import org.noear.solon.serialization.SerializerNames;
import org.noear.solon.serialization.jackson3.xml.impl.TypeReferenceImpl;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlReadFeature;
import tools.jackson.dataformat.xml.XmlWriteFeature;

import java.util.Collection;
import java.util.List;

/**
 * JacksonXml 实体转换器
 *
 * @author noear
 * @since 3.6
 */
public class Jackson3XmlEntityConverter extends AbstractStringEntityConverter<Jackson3XmlStringSerializer> {
    public Jackson3XmlEntityConverter(Jackson3XmlStringSerializer serializer) {
        super(serializer);
        XmlMapper deMapper = serializer.getDeserializeConfig().getMapper();
        serializer.getDeserializeConfig().setMapper(newDeMapper(deMapper));

        XmlMapper seMapper = serializer.getSerializeConfig().getMapper();
        serializer.getSerializeConfig().setMapper(newSeMapper(seMapper));
    }

    /**
     * 后缀或名字映射
     */
    @Override
    public String[] mappings() {
        return new String[]{SerializerNames.AT_XML};
    }

    /**
     * 初始化
     */
    public XmlMapper newSeMapper(XmlMapper mapper) {
        return mapper.rebuild()
                // xml声明处理
                .configure(XmlWriteFeature.WRITE_XML_DECLARATION, false)
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, true).build();
    }

    /**
     * 初始化
     */
    public XmlMapper newDeMapper(XmlMapper mapper) {
        return mapper.rebuild()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // 允许使用未带引号的字段名
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // xml空节点处理
                .configure(XmlReadFeature.EMPTY_ELEMENT_AS_NULL, true)
                .changeDefaultVisibility(vc -> vc.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY))
                .activateDefaultTypingAsProperty(mapper._deserializationContext().getConfig().getPolymorphicTypeValidator(),
                        DefaultTyping.JAVA_LANG_OBJECT, "@type").build();
    }

    /**
     * 转换 body
     *
     * @param ctx   请求上下文
     * @param mWrap 函数包装器
     */
    @Override
    protected Object changeBody(Context ctx, MethodWrap mWrap) throws Exception {
        return serializer.deserializeFromBody(ctx);
    }

    /**
     * 转换 value
     *
     * @param ctx     请求上下文
     * @param p       参数包装器
     * @param pi      参数序位
     * @param pt      参数类型
     * @param bodyRef 主体对象
     * @since 1.11 增加 requireBody 支持
     */
    @Override
    protected Object changeValue(Context ctx, ParamWrap p, int pi, Class<?> pt, LazyReference bodyRef) throws Throwable {
        if (p.spec().isRequiredPath() || p.spec().isRequiredCookie() || p.spec().isRequiredHeader()) {
            //如果是 path、cookie, header
            return super.changeValue(ctx, p, pi, pt, bodyRef);
        }

        if (p.spec().isRequiredBody() == false && ctx.paramMap().containsKey(p.spec().getName())) {
            //有可能是path、queryString变量
            return super.changeValue(ctx, p, pi, pt, bodyRef);
        }

        Object bodyObj = bodyRef.get();

        if (bodyObj == null) {
            return super.changeValue(ctx, p, pi, pt, bodyRef);
        }

        JsonNode tmp = (JsonNode) bodyObj;

        if (tmp.isObject()) {
            if (p.spec().isRequiredBody() == false) {
                //
                //如果没有 body 要求；尝试找按属性找
                //
                if (tmp.has(p.spec().getName())) {
                    JsonNode m1 = tmp.get(p.spec().getName());

                    return serializer.getDeserializeConfig().getMapper().readValue(serializer.getDeserializeConfig().getMapper().treeAsTokens(m1), new TypeReferenceImpl<>(p));
                }
            }

            //尝试 body 转换
            if (pt.isPrimitive() || pt.getTypeName().startsWith("java.lang.")) {
                return super.changeValue(ctx, p, pi, pt, bodyRef);
            } else {
                if (List.class.isAssignableFrom(pt)) {
                    return null;
                }

                if (pt.isArray()) {
                    return null;
                }

                //支持泛型的转换 如：Map<T>
                return serializer.getDeserializeConfig().getMapper().readValue(serializer.getDeserializeConfig().getMapper().treeAsTokens(tmp), new TypeReferenceImpl<>(p));
            }
        }

        if (tmp.isArray()) {
            //如果参数是非集合类型
            if (!Collection.class.isAssignableFrom(pt)) {
                return null;
            }

            return serializer.getDeserializeConfig().getMapper().readValue(serializer.getDeserializeConfig().getMapper().treeAsTokens(tmp), new TypeReferenceImpl<>(p));
        }

        //return tmp.val().getRaw();
        if (tmp.isValueNode()) {
            return serializer.getDeserializeConfig().getMapper().readValue(serializer.getDeserializeConfig().getMapper().treeAsTokens(tmp), new TypeReferenceImpl<>(p));
        } else {
            return null;
        }
    }
}