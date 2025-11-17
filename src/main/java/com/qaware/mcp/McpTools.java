package com.qaware.mcp;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult.Builder;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

/** Utility methods for MCP tools. */
enum McpTools {

    ;


    static final McpJsonMapper MCP_JSON_MAPPER = new JacksonMcpJsonMapper(new ObjectMapper());


    static final McpSchema.ServerCapabilities SERVER_CAPABILITIES = McpSchema.ServerCapabilities.builder()
            .tools(true)
            .resources(false, false)
            .prompts(false)
            .build();


    private static final Logger LOGGER = LoggerFactory.getLogger(McpTools.class);


    static Tool getTool(Method method, McpTool mcpTool) {
        String value = mcpTool.value();

        return Tool.builder()
                .annotations(newToolAnnotations(mcpTool))
                .name(method.getName())
                .description(value)
                .inputSchema(newJsonSchema(method))
                .build();
    }


    static <T> BiFunction<T, McpSchema.CallToolRequest, CallToolResult> getCallHandler(Object tool, Method method) {
        return (mcpSyncServerExchange, parameters) -> McpTools.invoke(tool, method, parameters.arguments());
    }


    @SuppressWarnings("java:S1181")
    private static CallToolResult invoke(Object instance, Method method, Map<String, Object> parameters) {
        try {
            Object result = Reflection.invokeMethod(method, instance, parameters);
            return newCallResult(result);

        } catch (Throwable throwable) {
            String msg = "Call failed: " + method.getName() + " " + parameters;
            LOGGER.error(msg, throwable);

            while (throwable.getCause() != null) throwable = throwable.getCause();

            return newCallResult(throwable);
        }
    }


    private static ToolAnnotations newToolAnnotations(McpTool mcpTool) {
        String title = mcpTool.title();

        return new ToolAnnotations(
            title.isBlank() ? mcpTool.value() : title,
            mcpTool.readOnlyHint   (),
            mcpTool.destructiveHint(),
            mcpTool.idempotentHint (),
            mcpTool.openWorldHint  (),
            mcpTool.returnDirect   ());
    }


    private static JsonSchema newJsonSchema(Method method) {
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

        return new JsonSchema("object", paramToInfo, new ArrayList<>(paramToInfo.keySet()), false, null, null);
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
