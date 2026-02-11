package com.qaware.mcp.tools.knowledge;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/*
jmcp com.qaware.mcp.tools.knowledge.TikaTool C:\Viechtbauer\Zeugs\Docs\QAware\_Vorträge\2025-11-06-KnowledgeDB-MCP.pptx
 */

/**
 * Utility for extracting textual content from common office/document formats using Apache Tika.
 *
 * The implementation intentionally uses a minimal, conservative Tika configuration (see the embedded XML
 * {@code tikaConfig}) that only registers a small, safe set of pure-Java parsers. This prevents invocation of external
 * binaries or potentially unsafe parsers (OCR/Tesseract, ExternalParser/LibreOffice, etc.).
 *
 * Usage: call {@link #isSupported(String)} to check a filename's extension and {@link #parse(InputStream)}
 * to obtain extracted text. The returned text preserves simple markup (headings, paragraphs, table
 * cell separators) to keep structure useful for downstream processing.
 */
enum TikaTool {

    ;


    //XXX
    public static void main(String[] args) throws Exception {
        System.out.println(parse(new FileInputStream(args[0])));
    }
    //XXX


    private static final Map<String, String> START = Map.of(
        "b", "**",
        "i", "*",

        "p", "\n",

        "tr", "\n",
        "td", " | ",

        "h1", "\n\n# ",
        "h2", "\n\n## ",
        "h3", "\n\n### ",
        "h4", "\n\n#### ",
        "h5", "\n\n##### "
    );


    private static final Map<String, String> END = Map.of(
        "b", "**",
        "i", "*",

        "h1", "\n\n",
        "h2", "\n\n",
        "h3", "\n\n",
        "h4", "\n\n",
        "h5", "\n\n"
    );


    private static final TikaConfig tikaConfig;


    static {
        String xmlConfig =
            """
            <?xml version="1.0" encoding="UTF-8"?>

            <!--
              Minimal Tika config that only registers a small, safe set of parsers.
              Purpose: ensure no external binaries or external parsers (Tesseract, ExternalParser/LibreOffice, ...)
              are used.
            -->

            <properties>
              <!-- Only include parsers implemented in pure Java and commonly safe -->
              <parsers>
                <!-- plain text -->
                <parser class="org.apache.tika.parser.txt.TXTParser"/>

                <!-- PDF (Apache PDFBox) -->
                <parser class="org.apache.tika.parser.pdf.PDFParser"/>

                <!-- Microsoft OOXML (docx, xlsx, pptx) -->
                <parser class="org.apache.tika.parser.microsoft.ooxml.OOXMLParser"/>

                <!-- Legacy Microsoft Office (doc, xls, ppt) via Apache POI (pure Java) -->
                <parser class="org.apache.tika.parser.microsoft.OfficeParser"/>

                <!-- RTF -->
                <parser class="org.apache.tika.parser.rtf.RTFParser"/>

                <!-- XML/HTML -->
                <parser class="org.apache.tika.parser.xml.XMLParser"/>
                <parser class="org.apache.tika.parser.html.HtmlParser"/>

                <!-- EPUB -->
                <parser class="org.apache.tika.parser.epub.EpubParser"/>

                <!-- Generic composite parser fallback (keeps behavior conservative because we've enumerated allowed parsers) -->
                <parser class="org.apache.tika.parser.CompositeParser"/>
              </parsers>

              <!-- Do not register external parsers or OCR parsers here (Tesseract, ExternalParser, etc.) -->
              <!-- We intentionally leave detectors and other extensions at defaults. -->
            </properties>
            """;

        try (InputStream inputStream = new ByteArrayInputStream(xmlConfig.getBytes(StandardCharsets.UTF_8))) {
            tikaConfig = new TikaConfig(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static class MarkupContentHandler extends DefaultHandler {

        private final StringBuilder stringBuilder = new StringBuilder();


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            String name = localName == null ? qName : localName;

            if ("p".equals(name) && !stringBuilder.isEmpty() && stringBuilder.charAt(stringBuilder.length() - 1) == ' ') return;

            stringBuilder.append(START.getOrDefault(name, ""));
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String name = localName == null ? qName : localName;

            stringBuilder.append(END.getOrDefault(name, ""));
        }


        @Override
        public void characters(char[] chars, int start, int length) {
            stringBuilder.append(chars, start, length);
        }


        @Override
        public String toString() {
            return stringBuilder.toString();
        }

    }


    /**
     * Check whether the given filename has a supported extension (case-insensitive and accepts common office/document formats).
     */
    static boolean isSupported(String fileName) {
        String lowerCase = fileName.toLowerCase(Locale.ROOT);
        return     lowerCase.endsWith(".docx") || lowerCase.endsWith(".doc")
                || lowerCase.endsWith(".pdf")
                || lowerCase.endsWith(".pptx") || lowerCase.endsWith(".ppt")
                || lowerCase.endsWith(".xlsx") || lowerCase.endsWith(".xls");
    }


    /**
     * Parse the provided document input stream using a restricted, safe Tika configuration and
     * return extracted text.
     *
     * Security note: the embedded Tika configuration deliberately excludes external/unsafe parsers
     * to avoid invoking external binaries (OCR/Tesseract, ExternalParser/LibreOffice, etc.).
     *
     * The input stream will be closed by this method.
     *
     * @param inputStream the document input stream (will be closed)
     * @return extracted and trimmed text with simple markup preserved
     * @throws RuntimeException when parsing fails
     */
    static String parse(InputStream inputStream) {
        try {
            ContentHandler contentHandler = new MarkupContentHandler();

            try (inputStream) {
                AutoDetectParser autoDetectParser = new AutoDetectParser(tikaConfig);

                autoDetectParser.parse(inputStream, contentHandler, new Metadata(), new ParseContext());
            }

            return contentHandler.toString().trim().replaceAll("\n\n\n+", "\n\n"); // could be optimized, but fine for now

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
