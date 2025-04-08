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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.WebRxSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.noear.solon.ai.chat.annotation.FunctionMapping;
import org.noear.solon.ai.chat.function.MethodChatFunction;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanExtractor;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Plugin;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author noear
 * @since 3.1
 */
public class McpServerPlugin implements Plugin {
    @Override
    public void start(AppContext context) throws Throwable {
        McpServerProperties serverProperties = context.beanMake(McpServerProperties.class).get();

        WebRxSseServerTransportProvider mcpTransportProvider = WebRxSseServerTransportProvider.builder()
                .messageEndpoint(serverProperties.getMessageEndpoint())
                .sseEndpoint(serverProperties.getSseEndpoint())
                .objectMapper(new ObjectMapper())
                .build();

        McpServer.AsyncSpecification mcpServerSpec = McpServer.async(mcpTransportProvider)
                .serverInfo(serverProperties.getName(), serverProperties.getVersion());

        context.beanExtractorAdd(FunctionMapping.class, new BeanExtractor<FunctionMapping>() {
            @Override
            public void doExtract(BeanWrap bw, Method method, FunctionMapping anno) throws Throwable {
                MethodChatFunction chatFunction = new MethodChatFunction(bw.raw(), method);
            }
        });

        mcpServerSpec.build();
    }

    private void addToolSpec(McpServer.AsyncSpecification mcpServerSpec, MethodChatFunction chatFunction) {
        McpServerFeatures.AsyncToolSpecification toolSpec = new McpServerFeatures.AsyncToolSpecification(
                new McpSchema.Tool(chatFunction.name(), chatFunction.description(), ""),
                (exchange, request) -> {
                    McpSchema.CallToolResult toolResult = null;
                    try {
                        String rst = chatFunction.handle(request);
                        toolResult = new McpSchema.CallToolResult(Arrays.asList(new McpSchema.TextContent(rst)), false);
                    } catch (Throwable ex) {
                        toolResult = new McpSchema.CallToolResult(Arrays.asList(new McpSchema.TextContent(ex.getMessage())), true);
                    }

                    return Mono.just(toolResult);
                });

        mcpServerSpec.tools(toolSpec);
    }
}