package com.qaware.mcp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an MCP (Model Context Protocol) tool.
 * <p>
 * Methods annotated with this annotation are automatically registered as callable MCP tools. The method name becomes
 * the tool name, and the annotation value provides the tool description. JSON schema is auto-generated from method
 * parameters annotated with {@link McpParam}.
 * <p>
 * The method will be invoked when the MCP tool is called, with parameters automatically mapped from JSON input to Java
 * method arguments.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;McpTool("Gets class summaries for specified packages")
 * public Map&lt;String, String&gt; getClassSummaries(
 *     &#64;McpParam(name = "packages", description = "Package names to analyze") String... packages) {
 *     // implementation
 * }
 * </pre>
 *
 * @see McpParam
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpTool {

    String  value(); // LLM and human-readable description

    String  title()           default "";    // human-readable title for the tool - for display purpose only - defaults to value

    boolean readOnlyHint()    default true;  // modifies its environment
    boolean destructiveHint() default false; // may perform destructive updates
    boolean idempotentHint()  default true;  // repeated calls with same args have no additional effect
    boolean openWorldHint()   default false; // interacts with external entities
    boolean returnDirect()    default false; // response should directly go to user

}