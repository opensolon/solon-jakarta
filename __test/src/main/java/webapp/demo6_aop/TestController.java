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
package webapp.demo6_aop;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Inject;

import java.util.HashMap;
import java.util.Map;

@Controller
public class TestController extends TestControllerBase{

    @Inject("rs3")
    public Rockapi  rockapi13;

    @Inject //会自动生成
    public Rockservice2  rockapi2;


    @Inject
    public Rockapi  rockapi132;

    @Mapping("/demo6/aop")
    public Object test() throws Exception {
        Map<String, Object> map = new HashMap<>();

        map.put("rockapi11", rockapi11.test());
        map.put("rockapi12", rockapi12.test());

        map.put("rockapi2", rockapi2.test());

        map.put("rockapi132", rockapi132.test());

        TestModel tmp = Solon.context().getBeanOrNew(TestModel.class);
        if("12".equals(tmp.name) == false){
            return "出错了";
        }

        if("test".equals(Solon.context().getWrap(TestModel.class).tag()) == false){
            return "出错了";
        }

        return map;
    }

    @Mapping("/demo6/aop3")
    public Object test3() throws Exception {
        return rockapi13.test();
    }
}
