# MCP Server Kickstart 🚀

A minimalistic and flexible Java framework for quickly creating
[MCP (Model Context Protocol)](https://modelcontextprotocol.io) servers —
supporting **Streaming**, **SSE**, and **STDIO** transports
without the hassle of dealing with Jetty configuration, JSON handling, or reflection magic.

## Features

- **Annotation-based tools** — Simply annotate your methods with `@McpTool` and `@McpParam`
- **Automatic JSON schema generation** - No manual schema writing needed
- **Three transport options**
    - **Streaming** (default)
    - **SSE**
    - **STDIO**
- **Fluent Builder API** - Clean and readable server configuration
- **Robust reflection handling** - Supports arrays, collections, and complex types
- **Built-in Jetty server** - Production-ready HTTP server included (SSE/streaming modes)
- **STDIO server** (Claude Desktop, Cline)
- **Graceful shutdown** - Proper cleanup on application termination
- **Zero or minimal configuration** - Sensible defaults, just add your tools and go

## Supported Transports

This server supports **three MCP transport modes**:

| Transport | Flag | Recommended For |
|----------|------|------------------|
| **Streaming (default)** | *(no flag)* or `--streaming` | GitHub Copilot (IntelliJ), Claude Code |
| **SSE** | `--sse` | Codeium / KiloCode |
| **STDIO** | `--stdio` | Claude Desktop, Cline |

If no flag is specified, the server runs in **Streaming mode**.


# Quick Start

## 1. Clone and Build

```bash
git clone https://github.com/qaware/mcp-server-kickstart.git
cd mcp-server-kickstart
./gradlew build
```

## 2. Create Your Tool Class

```java
public class MyTools {
    
    @McpTool("Adds two numbers together")
    public int add(@McpParam(name = "a", description = "First number") int a,
                   @McpParam(name = "b", description = "Second number") int b) {
        return a + b;
    }
    
    @McpTool("Gets information about files in a directory")
    public List<String> listFiles(@McpParam(name = "directory", description = "Directory path") String directory) {
        return Arrays.stream(new File(directory).listFiles())
                     .map(File::getName)
                     .collect(Collectors.toList());
    }
}
```


## 3. Run

### 3a. Start server "manually"

```java
public class MyServer {

    public static void main(String[] args) throws Exception {
        McpServer.create()
                 .serverInfo("My MCP Server", "1.0.0")
                 .port(8090)
                 .addTool(new MyTools())
                 .start();
    }
}
```

### 3b. Run via gradle

```
./gradlew run
```

Your MCP server will be available at http://localhost:8090/mcp

If you want to expose different tool(s), use

```
./gradlew run --args com.qaware.mcp.tools.McpSourceTool
```

You can provide multiple class names.

### 3c. Run via far jar

```
java -jar mcp-server-kickstart.jar
```

Starts in Streaming mode
Loads default HelloWorldTools

If you want to expose different tool(s), use

```
java -jar mcp-server-kickstart.jar --sse com.qaware.mcp.tools.McpSourceTool
```

### 3d. Run with Docker

```bash
docker build -t mcp-server-kickstart .
docker run -i --rm mcp-server-kickstart
```

The server uses STDIO transport and communicates via standard input/output. You can provide multiple tool class names as arguments.

#### How STDIO Transport Works

In docker, the MCP server is set to use **STDIO (Standard Input/Output) transport** instead of HTTP/SSE. This means:

1. **Process-based communication**: MCP clients spawn your server as a subprocess
2. **Simple integration**: No need to manage ports, URLs, or network configuration
3. **Secure**: Communication happens within the same machine via pipes
4. **Dynamic startup**: Each MCP client session starts a fresh server instance
5. **Docker-friendly**: Easy to containerize and run in isolation

The server reads MCP protocol messages from `stdin` and writes responses to `stdout`, making it compatible with any MCP client that supports STDIO transport (like Claude Desktop, Cline, etc.).

## Debugging

The server logs all tool registrations and requests.

In Streaming or SSE mode you will see output like:

```
INFO  - Creating MCP servlet 'My MCP Server' v1.0.0
INFO  - Registering tools from: MyTools
INFO  - MCP Server started successfully on http://localhost:8090
```


In STDIO mode

```
INFO  - Starting MCP server (STDIO)
INFO  - Registering tools from: MyTools
INFO  - Ready to receive MCP protocol messages
```

**Note**: When spawned by an MCP client (like Claude Desktop), logs are typically written to the client's log directory, not to your console. Check your MCP client's documentation for log locations.


### Supported Types

The framework automatically handles JSON schema generation for:

* Primitives: int, long, double, float, boolean
* Strings: String
* Arrays: int[], String[], etc.
* Collections: List<T>, Set<T>, Collection<T>

## Configuration

### Server Configuration

```java
McpServer.create()
    .serverInfo("My Server", "2.0.0")  // Server name and version
    .port(8080)                        // HTTP port (default: 8090)
    .addTool(new MyTools())            // Add tool instances
    .addTool(new MoreTools())          // Add multiple tools
    .start();
```

## Tool Methods

Methods must be annotated with @McpTool("description")

All parameters must be annotated with @McpParam(name = "paramName", description = "...")

Return types are automatically JSON-serialized

Exceptions are automatically caught and returned as error responses

## Integration with MCP Clients

### Codeium (KiloCode) ✅ Tested

Add this to your KiloCode MCP configuration (you need to use SSE!):

```json
{
  "mcpServers": {
    "java-kickstart-server": {
      "url": "http://localhost:8090/sse",
      "headers": {
        "Authorization": "Bearer your-token-here"
      },
      "alwaysAllow": ["hello", "add", "getItems"],
      "disabled": false
    }
  }
}
```

### IntelliJ ✅ Tested

Location:

* macOS: ~/Library/Application Support/github-copilot/intellij/mcp.json
* Windows: %APPDATA%\AppData\Local\github-copilot\intellij\mcp.json

Configuration:

```json
{
    "servers": {
        "my-local-server": {
            "url": "http://localhost:8090/mcp",
            "requestInit": {
                "headers": {
                    "Authorization": "Bearer XYZ!"
                }
            }
        }
    }
}
```

### Claude Desktop (Anthropic) ⚠️ Untested

Based on documentation, this should work for Claude Desktop:

Location:

* macOS: ~/Library/Application Support/Claude/claude_desktop_config.json
* Windows: %APPDATA%\Claude\claude_desktop_config.json

Configuration:

```json
{
  "mcpServers": {
    "my-java-server": {
      "command": "node",
      "args": ["path/to/your/mcp-server"],
      "env": {
        "SERVER_URL": "http://localhost:8090/sse"
      }
    }
  }
}
```

Note: Claude Desktop configuration may differ - please check the official Claude MCP documentation for the exact format.

### Docker execution

```json
{
  "mcpServers": {
    "mcp-knowledge-server": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "mcp-server-kickstart:latest",
        "com.qaware.mcp.tools.knowledge.McpKnowledgeTool"
      ]
    }
  }
}
```

### Docker with volume mount for documentation
```json
{
  "mcpServers": {
    "mcp-knowledge-server": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-v",
        "/path/to/your/docs:/data:ro",
        "mcp-server-kickstart:latest",
        "com.qaware.mcp.tools.knowledge.McpKnowledgeTool"
      ]
    }
  }
}
```

### Other MCP Clients

Any MCP client should be able to connect to: http://localhost:8090/sse

Any MCP client that supports STDIO transport can connect by spawning the process:
```bash
java -jar mcp-server-kickstart-all-1.0.0.jar [ToolClassName]
```

See `mcp-config-examples.json` for more configuration examples.

## Examples

### Simple Calculator

```java
public class Calculator {
    
    @McpTool("Performs basic arithmetic operations")
    public double calculate(@McpParam(name = "operation", description = "Operation: +, -, *, /") String op,
                           @McpParam(name = "a") double a,
                           @McpParam(name = "b") double b) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            default -> throw new IllegalArgumentException("Unknown operation: " + op);
        };
    }
}
```

### File Operations

```java
public class FileTools {
    
    @McpTool("Reads content from a file")
    public String readFile(@McpParam(name = "path", description = "File path") String path) throws IOException {
        return Files.readString(Paths.get(path));
    }
    
    @McpTool("Lists files in directory")
    public List<String> listDirectory(@McpParam(name = "path") String path) {
        File dir = new File(path);
        return dir.isDirectory() ? Arrays.asList(dir.list()) : List.of();
    }
}
```

### Working with Collections

```java
public class DataTools {
    
    @McpTool("Filters a list of numbers")
    public List<Integer> filterNumbers(@McpParam(name = "numbers") List<Integer> numbers,
                                       @McpParam(name = "threshold") int threshold) {
        return numbers.stream()
                     .filter(n -> n > threshold)
                     .collect(Collectors.toList());
    }
}
```

## Building and Deployment

### Fat JAR
```bash
./gradlew fatJar
java -jar build/libs/mcp-server-kickstart-all-1.0.0.jar
```

### Docker Image
```bash
# Build the image
docker build -t mcp-server-kickstart:latest .

# Run with default tools
docker run -i --rm mcp-server-kickstart:latest

# Run with custom tool
docker run -i --rm mcp-server-kickstart:latest com.example.MyCustomTool

# Run with volume mount
docker run -i --rm -v /path/to/docs:/docs:ro mcp-server-kickstart:latest
```

## Requirements

* Java 17+
* Gradle 8.14.2+ (included via wrapper)

## Dependencies

- **MCP SDK**: `io.modelcontextprotocol.sdk:mcp:0.14.1` - Core MCP protocol support
- **Jackson**: `com.fasterxml.jackson.core:jackson-databind:2.17.0` - JSON serialization
- **Log4j**: `org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3` - Logging
- **HPPC**: `com.carrotsearch:hppc:0.10.0` - High-performance primitive collections

Note: Jetty dependencies can be removed from build.gradle as they're no longer needed for STDIO transport.

## License

MIT License - Feel free to use this in your projects!
