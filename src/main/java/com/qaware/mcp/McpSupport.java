package com.qaware.mcp;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult.Builder;
import jakarta.servlet.Servlet;

enum McpSupport {

    ; // nänänä 🤪


    private static final Logger log = LoggerFactory.getLogger(McpServer.class);


    static Servlet newMCPServlet(String serverName, String serverVersion, Object... tools) {
        log.info("Creating MCP servlet '{}' v{}", serverName, serverVersion);

        HttpServletSseServerTransportProvider transportProvider = new HttpServletSseServerTransportProvider(
            new ObjectMapper(), "/");

        McpSyncServer mcpSyncServer = io.modelcontextprotocol.server.McpServer.sync(transportProvider)
            .serverInfo(serverName, serverVersion)
            .capabilities(McpSchema.ServerCapabilities.builder()
                .tools(true)
                .resources(false, false)
                .prompts(false)
                .build())
            .build();

        for (Object tool : tools) {
            log.info("Registering tools from: {}", tool.getClass().getSimpleName());
            addTool(mcpSyncServer, tool);
        }

        return transportProvider;
    }


    private static void addTool(McpSyncServer mcpSyncServer, Object instance) {
        Map<Method, McpTool> mcpMethods = Reflection.getMethodsWithAnnotations(instance.getClass(), McpTool.class);

        mcpMethods.forEach((method, mcpDesc) -> mcpSyncServer.addTool(newSyncToolSpecification(instance, method, mcpDesc)));
    }


    private static SyncToolSpecification newSyncToolSpecification(Object instance, Method method, McpTool mcpDesc) {
        Tool tool = Tool.builder()
            .name(method.getName())
            .description(mcpDesc.value())
            .inputSchema(newInputSchema(method))
            .build();

        return SyncToolSpecification.builder()
            .tool(tool)
            .callHandler((mcpSyncServerExchange, parameters) -> invoke(instance, method, parameters.arguments()))
            .build();
    }


    private static String newInputSchema(Method method) {
        Map<String, Object> paramToInfo = new LinkedHashMap<>();

        for (Parameter param : method.getParameters()) {
            McpParam mcpParam = param.getAnnotation(McpParam.class);
            if (mcpParam == null) {
                throw new IllegalArgumentException("Parameter " + param.getName() + " missing @McpParam annotation");
            }

            Map<String, Object> property = new LinkedHashMap<>();

            String type = Json.getJsonType(param.getType());
            property.put("type", type);

            if (type.equals("array")) {
                property.put("items", Map.of("type", Json.getJsonType(Reflection.getInnerType(param))));
            }

            if (!mcpParam.description().isEmpty()) {
                property.put("description", mcpParam.description());
            }

            paramToInfo.put(mcpParam.name(), property);
        }

        return Json.toJson(Map.of(
                "type"      , "object",
                "properties", paramToInfo,
                "required"  , paramToInfo.keySet()
            ));
    }


    @SuppressWarnings("java:S1181")
    private static CallToolResult invoke(Object instance, Method method, Map<String, Object> parameters) {
        try {
            Object result = Reflection.invokeMethod(method, instance, parameters);
            return newCallResult(result);

        } catch (Throwable throwable) {
            log.error("Call failed: " + method.getName() + " " + parameters, throwable);

            while (throwable.getCause() != null) throwable = throwable.getCause();

            return newCallResult(throwable);
        }
    }


    private static CallToolResult newCallResult(Object result) {
        Builder builder = CallToolResult.builder();

        if (result instanceof Throwable throwable) {
            builder.isError(true);
            result = throwable.toString();
        }

        if (result instanceof Iterable<?> iterable) {
            for (Object object : iterable) addTextContent(builder, object);
        } else {
            addTextContent(builder, result);
        }

        return builder.build();
    }


    private static void addTextContent(Builder builder, Object result) {
        builder.addTextContent(result instanceof String string ? string : Json.toJson(result));
    }

}
