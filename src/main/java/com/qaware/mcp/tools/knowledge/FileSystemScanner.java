package com.qaware.mcp.tools.knowledge;

import com.qaware.mcp.tools.McpSourceTool;
import com.qaware.mcp.tools.knowledge.nlp.BytesDecoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

class FileSystemScanner implements Consumer<Consumer<Location>> {

    private final String[] roots;


    public FileSystemScanner(String... roots) {
        this.roots = roots;
    }


    @Override
    public void accept(Consumer<Location> locationConsumer) {
        for (String root : roots) {
            McpSourceTool.scan(Path.of(root), FileSystemScanner::isTextFile, path -> {

                String location = path.toAbsolutePath().toString();

                locationConsumer.accept(

                        new Location() {

                            private byte[] bytes;

                            private String string;


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
                                if (bytes == null) bytes = McpSourceTool.readBytes(McpSourceTool.toURL(path).toString());

                                if (! isTika(location)) return new BytesDecoder().reset(bytes);
// doppeltes parsen vermeiden
                                if (string == null) string = TikaTool.parse(new ByteArrayInputStream(bytes));
                                return string;
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
            return -1;
        }
    }


    private static boolean isTextFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".md") || fileName.endsWith(".adoc") || fileName.endsWith(".txt") || isTika(fileName);
    }


    private static boolean isTika(String fileName) {
        return fileName.endsWith(".docx") || fileName.endsWith(".doc") || fileName.endsWith(".pdf") || fileName.endsWith(".pptx") || fileName.endsWith(".ppt") || fileName.endsWith(".xlsx") || fileName.endsWith(".xls");
    }

}
