# MCP Server Kickstart 🚀

A minimalistic Java framework for quickly creating [MCP (Model Context Protocol)](https://modelcontextprotocol.io) servers without the hassle of dealing with Jetty configuration, JSON handling, or reflection magic.

## Features

- **Annotation-based tools** - Just annotate your methods with `@McpTool` and `@McpParam`
- **Automatic JSON schema generation** - No manual schema writing needed
- **Fluent Builder API** - Clean and readable server configuration
- **Zero configuration** - Sensible defaults, just add your tools and go
- **Robust reflection handling** - Supports arrays, collections, and complex types
- **Built-in Jetty server** - Production-ready HTTP server included
- **Graceful shutdown** - Proper cleanup on application termination

## Quick Start

### 1. Clone and Build

```bash
git clone https://github.com/qaware/mcp-server-kickstart.git
cd mcp-server-kickstart
./gradlew build
```

### 2. Create Your Tool Class

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

### 3. Start Your Server

```java
public class Server {

    public static void main(String[] args) throws Exception {
        McpServer.create()
                 .serverInfo("My MCP Server", "1.0.0")
                 .port(8090)
                 .addTool(new MyTools())
                 .start();
    }
}
```

### 4. Run

```
./gradlew run
```

Your MCP server will be available at http://localhost:8090/sse

### Debugging

The server logs all tool registrations and requests. Check the console output for:

```
INFO  - Creating MCP servlet 'My MCP Server' v1.0.0
INFO  - Registering tools from: MyTools
INFO  - MCP Server started successfully on http://localhost:8090
```

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

## Connecting to AI Tools

### Codeium (KiloCode) ✅ Tested

Add this to your KiloCode MCP configuration:

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

### General MCP Clients

Any MCP client should be able to connect to: http://localhost:8090/sse


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

## Building Fat JAR

```
./gradlew jar
java -jar build/libs/mcp-server-kickstart-1.0.0.jar
```

## Requirements

* Java 17+
* Gradle 8.14.2+ (included via wrapper)

## Dependencies

- **MCP SDK**: `io.modelcontextprotocol.sdk:mcp:0.10.0`
- **Jetty**: `org.eclipse.jetty:jetty-server:12.0.22`
- **Jackson**: `com.fasterxml.jackson.core:jackson-databind:2.16.1`
- **SLF4J**: `org.slf4j:slf4j-simple:2.0.9`

## License

MIT License - Feel free to use this in your projects!
