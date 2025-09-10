package com.qaware.mcp.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaware.mcp.McpParam;
import com.qaware.mcp.McpTool;

/**
 * A Model Context Protocol (MCP) tool that serves Java source code
 * from Maven and Gradle local repositories. <br>
 *
 * <p>At startup, the server scans the Maven local repository
 * (<code>~/.m2/repository</code>) and the Gradle cache
 * (<code>~/.gradle/caches/modules-2/files-2.1</code>, or the directory specified
 * by <code>GRADLE_USER_HOME</code>), looking for <code>-sources.jar</code> archives.
 * Each archive is opened, and all contained <code>.java</code> source entries are
 * indexed. The index maps both the fully qualified class name (derived from the
 * entry path) and the simple class name (derived from the file name) to a URL
 * that allows later retrieval of the file contents.</p>
 *
 * <p>Indexed source files can then be accessed via the
 * {@link #getSource(String)} MCP tool method, which returns the complete source
 * code of the requested class as a string.</p>
 */
public class McpSourceTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpSourceTool.class);

    private final Map<String, String> map = new ConcurrentHashMap<>();


    /**
     * Creates a new {@code McpSourceServer} and scans both Maven and Gradle
     * local repositories for {@code -sources.jar} files. All discovered
     * Java source entries are indexed for later lookup.
     */
    public McpSourceTool() {
        long startNano = System.nanoTime();

        String userHome   = System.getProperty("user.home");
        String gradleHome = System.getenv("GRADLE_USER_HOME");

        scan(Path.of(userHome, ".m2", "repository"));
        scan((gradleHome == null ? Path.of(userHome, ".gradle") : Path.of(gradleHome)).resolve("caches/modules-2/files-2.1"));

        LOGGER.info("Scanned maven and gradle repos in '{}'ms", (System.nanoTime() - startNano) / 1_000_000f);
    }


    /**
     * Retrieves the Java source code for a given class name.
     *
     * @param name fully qualified class name (preferred) or simple class name
     * @return the source code of the requested class as a UTF-8 string
     * @throws FileNotFoundException if the class could not be found in the indexed sources
     */
    @McpTool("Gets the source code for a full qualified class name (fqcn) (preferred) or just a class name - use if you need source code that is not available in your current project")
    public String getSource(@McpParam(name = "name", description = "fqcn or class name") String name) throws FileNotFoundException {
        name = normalizeName(name);

        String url = map.get(name);
        if (url == null) throw new FileNotFoundException("Could not find the source for: " + name);

        return readAll(url);
    }


    /**
     * Recursively scans a repository directory for {@code -sources.jar} files
     * and processes each archive.
     *
     * @param repoPath the root path of the repository to scan
     */
    private void scan(Path repoPath) {
        try (Stream<Path> stream = Files.find(repoPath,
            Integer.MAX_VALUE,
            (p, a) -> a.isRegularFile() && isSourcesJar(p.getFileName().toString()))) {
            stream.parallel().forEach(this::visitZip);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }


    /**
     * Opens a {@code -sources.jar} file, inspects all entries, and registers
     * every Java source file found in the index.
     *
     * @param path the path to the {@code -sources.jar} file
     */
    @SuppressWarnings("java:S135")
    private void visitZip(Path path) {
        LOGGER.info("Inspecting '{}'", path);

        String urlBase = "jar:" + toURL(path)  + "!/";

        try (ZipFile zipFile = new ZipFile(path.toFile(), StandardCharsets.UTF_8)) {
            zipFile.stream()
            .filter(entry -> !entry.isDirectory() && isSource(entry.getName()))
            .forEach(entry -> {

                String fullPath = entry.getName();
                String fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);

                String url = urlBase + normalizePath(fullPath);

                map.put(normalizeName(fileName), url);
                map.put(normalizeName(fullPath), url);

                LOGGER.trace("Found '{}'", fullPath);
            });

        } catch (IOException ioe) {
            LOGGER.warn("Failed to process zip {}", path, ioe);
        }
    }


    private static URL toURL(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }


    private static String normalizeName(String path) {
        return path.replaceAll("\\.java$", "").replace('/', '.');
    }


    private static String normalizePath(String path) {
        return path.replace('\\', '/');
    }


    private static String readAll(String url)   {
        try (InputStream openStream = new URL(url).openStream()) {
            return new String(openStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }


    private static boolean isSource(String name) {
        return name.endsWith(".java");
    }


    private static boolean isSourcesJar(String name) {
        return name.endsWith("-sources.jar");
    }

}
