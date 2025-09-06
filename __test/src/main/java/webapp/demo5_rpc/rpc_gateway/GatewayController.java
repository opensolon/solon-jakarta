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
package webapp.demo5_rpc.rpc_gateway;

import org.noear.nami.Nami;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Controller;
import org.noear.solon.core.handle.Context;

//用普通控制器，手动实现一个网关
@Controller
public class GatewayController {
    @Mapping("/demo51/{sev}/{fun**}")
    public void call(Context context, String sev, String fun) throws Exception {

        //根据sev发现服务->完成负载->产生url
        String url = "http://localhost:8080/test/";

        String rst = new Nami()
                .url(url, fun)
                .call(context.headerMap().toValueMap(), context.paramMap().toValuesMap())
                .getString();

        context.output(rst);
    }
}
