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
package webapp.demo5_rpc.rpc_customer;

import org.noear.nami.Nami;
import webapp.demo5_rpc.RockApi;

public class Demo5Consume {
    public static void main(String[] args){
        RockApi client =  Nami.builder().create(RockApi.class);


        Object val = client.test1(12);
        if(val == null){
            return;
        }
    }
}
