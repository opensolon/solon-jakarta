/*
 * Copyright 2017-2024 noear.org and authors
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
package ch.qos.logback.solon;

import ch.qos.logback.core.model.NamedModel;

/**
 * @author noear
 * @since 2.3
 */
public class SolonPropertyModel extends NamedModel {

    private String scope;

    private String defaultValue;

    private String source;

    String getScope() {
        return this.scope;
    }

    void setScope(String scope) {
        this.scope = scope;
    }

    String getDefaultValue() {
        return this.defaultValue;
    }

    void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    String getSource() {
        return this.source;
    }

    void setSource(String source) {
        this.source = source;
    }

}
