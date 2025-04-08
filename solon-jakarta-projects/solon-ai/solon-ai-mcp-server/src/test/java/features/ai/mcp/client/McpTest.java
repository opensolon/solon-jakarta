package features.ai.mcp.client;

import demo.ai.mcp.App;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.noear.solon.Solon;
import org.noear.solon.test.SolonTest;

import java.util.Map;


/**
 * @author noear 2025/4/8 created
 */
@Slf4j
@SolonTest(App.class)
public class McpTest {
    @Test
    public void case1() throws Exception {
        String baseUri = "http://localhost:" + Solon.cfg().serverPort();
        HttpClientSseClientTransport clientTransport = HttpClientSseClientTransport.builder(baseUri)
                .sseEndpoint("/sse")
                .build();

        McpSyncClient mcpClient = McpClient.sync(clientTransport).clientInfo(new McpSchema.Implementation("Sample " + "client", "0.0.0"))
                .build();

        mcpClient.initialize();

        McpSchema.CallToolRequest callToolRequest = new McpSchema.CallToolRequest("sum", Map.of("a", 1, "b", 2));
        McpSchema.CallToolResult response = mcpClient.callTool(callToolRequest);

        assert response != null;
        log.warn("{}", response);

        mcpClient.close();

    }
}