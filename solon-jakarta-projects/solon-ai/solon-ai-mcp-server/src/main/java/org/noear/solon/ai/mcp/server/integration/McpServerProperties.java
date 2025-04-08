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
    private String name = "solon-ai-mcp-server";
    private String version = "1.0.0";
    private String messageEndpoint = "/mcp/message";
    private String sseEndpoint = "/mcp/sse";

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