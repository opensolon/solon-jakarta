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
package org.noear.solon.ai.mcp.server.integration;

import org.noear.solon.annotation.BindProps;
import org.noear.solon.annotation.Configuration;

/**
 * @author noear
 * @since 3.1
 */
@BindProps(prefix="solon.ai.mcp.server")
@Configuration
public class McpServerProperties {
    private boolean enabled = true;
    private String name = "solon-ai-mcp-server";
    private String version = "1.0.0";
    private String messageEndpoint = "/mcp/message";
    private String sseEndpoint = "/mcp/sse";

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getMessageEndpoint() {
        return messageEndpoint;
    }

    public String getSseEndpoint() {
        return sseEndpoint;
    }
}