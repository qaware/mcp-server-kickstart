package com.qaware.mcp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter for MCP tool parameter binding.
 * <p>
 * Used to specify the parameter name and optional description for JSON schema generation and parameter mapping when
 * invoking MCP tools. The parameter name is used to map JSON input parameters to Java method arguments.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;McpDesc("Analyzes Java classes")
 * Map&lt;String, String&gt; analyze(
 *     &#64;McpParam(name="classes", description="Fully qualified class names") String[] classes,
 *     &#64;McpParam(name="includePrivate") boolean includePrivate
 * ) { ... }
 * </pre>
 *
 * @see McpTool
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpParam {
    String name();

    String description() default "";
}