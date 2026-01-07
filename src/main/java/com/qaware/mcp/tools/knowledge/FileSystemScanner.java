package com.qaware.mcp.tools.knowledge;

import com.qaware.mcp.tools.McpSourceTool;
import com.qaware.mcp.tools.knowledge.nlp.BytesDecoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Consumer;

class FileSystemScanner implements Consumer<Consumer<Location>> {

    private final String[] roots;


    public FileSystemScanner(String... roots) {
        this.roots = roots;
    }


    @Override
    public void accept(Consumer<Location> locationConsumer) {
        for (String root : roots) {
            McpSourceTool.scan(Path.of(root), FileSystemScanner::isSupported, path -> {

                String location = path.toAbsolutePath().toString().replace('\\', '/');

                locationConsumer.accept(

                        new Location() {

                            private byte[] bytesCached;

                            private String tikaCached;


                            @Override
                            public String getId() {
                                return location;
                            }

                            @Override
                            public long getVersion() {
                                return getLastMod(path);
                            }

                            @Override
                            public synchronized CharSequence getChars() {
                                if (bytesCached == null) bytesCached = McpSourceTool.readBytes(McpSourceTool.toURL(path).toString());

                                if (! TikaTool.isSupported(location)) return new BytesDecoder().reset(bytesCached);

                                if (tikaCached == null) tikaCached = TikaTool.parse(new ByteArrayInputStream(bytesCached));
                                return tikaCached;
                            }
                        }
                );


            });
        }

    }


    private static long getLastMod(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return System.currentTimeMillis();
        }
    }


    private static boolean isSupported(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".md") || fileName.endsWith(".adoc") || fileName.endsWith(".txt") || TikaTool.isSupported(fileName);
    }

}
