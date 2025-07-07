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

import jakarta.jws.WebService;
import org.noear.solon.core.BeanBuilder;
import org.noear.solon.core.BeanWrap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author noear
 * @since 1.0
 */
public class WebServiceBeanBuilder implements BeanBuilder<WebService> {
    private List<BeanWrap> wsBeanWarps = new ArrayList<>();

    public List<BeanWrap> getWsBeanWarps() {
        return wsBeanWarps;
    }

    @Override
    public void doBuild(Class<?> clz, BeanWrap bw, WebService anno) throws Throwable {
        if (clz.isInterface() == false) {
            wsBeanWarps.add(bw);
        }
    }
}
