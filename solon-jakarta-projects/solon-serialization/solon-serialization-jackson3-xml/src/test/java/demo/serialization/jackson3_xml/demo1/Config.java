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
package demo.serialization.jackson3_xml.demo1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Managed;
import org.noear.solon.serialization.jackson3.xml.Jackson3XmlActionExecutor;
import org.noear.solon.serialization.jackson3.xml.Jackson3XmlRenderFactory;


import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

@Configuration
public class Config {

    /**
     * 示例配置,同Jackson配置一样,只不过是对XML进行处理
     * 下面示例返回:
     * <pre>
     *
     *   <LinkedHashMap><time1>2024-05-25 21:57</time1><time2>2024-05-25</time2><time3>1716645462333</time3></LinkedHashMap>
     *
     * </pre>
     * @param factory
     * @param executor
     */
    @Managed
    public void jsonInit(@Inject Jackson3XmlRenderFactory factory, @Inject Jackson3XmlActionExecutor executor){
        //通过转换器，做简单类型的定制
        factory.addConvertor(Date.class, s -> s.getTime());

        factory.addConvertor(LocalDate.class, s -> s.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        factory.addConvertor(LocalDateTime.class, s -> s.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        factory.addEncoder(Date.class, new ValueSerializer<Date>() {
            @Override
            public void serialize(Date date, JsonGenerator out, SerializationContext sp) throws JacksonException {
                out.writeNumber(date.getTime());
            }
        });
    }
}
