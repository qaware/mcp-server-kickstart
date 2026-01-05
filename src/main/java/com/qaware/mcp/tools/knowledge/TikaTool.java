package com.qaware.mcp.tools.knowledge;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/*
jmcp com.qaware.mcp.tools.knowledge.TikaTool C:/Viechtbauer/Zeugs/Docs/QAware/AIR/SystemhandbuchAirBMW.docx
jmcp com.qaware.mcp.tools.knowledge.TikaTool C:\Viechtbauer\Zeugs\Docs\QAware\_Vorträge\2025-11-06-KnowledgeDB-MCP.pptx
 */
enum TikaTool {

    ;


    //XXX
    public static void main(String[] args) throws Exception {
        System.out.println(parse(new FileInputStream(args[0])));
    }
    //XXX


    private static final Map<String, String> TAGS = Map.of(
            "p", "\n",
            "tr", "\n",
            "td", " | ",
            "h1", "\n\n# ",
            "h2", "\n\n## ",
            "h3", "\n\n### ",
            "h4", "\n\n#### ",
            "h5", "\n\n##### "
    );


    private static final TikaConfig tikaConfig;


    static {
        String xmlConfig =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            
            <!--
              Minimal Tika config that only registers a small, safe set of parsers.
              Purpose: ensure no external binaries or external parsers (Tesseract, ExternalParser/LibreOffice, Solr, ...) are used.
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

        private final StringBuilder writer = new StringBuilder();


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            String name = localName == null ? qName : localName;

            if ("p".equals(name) && writer.length() > 0 && writer.charAt(writer.length() - 1) == ' ') return;

            writer.append(TAGS.getOrDefault(name, ""));
        }


        @Override
        public void characters(char[] chars, int start, int length) {
            writer.append(chars, start, length);
        }


        @Override
        public String toString() {
            return writer.toString();
        }

    }


    static String parse(InputStream inputStream) {
        try {
            ContentHandler contentHandler = new MarkupContentHandler();

            try (inputStream) {
                AutoDetectParser autoDetectParser = new AutoDetectParser(tikaConfig);

                autoDetectParser.parse(inputStream, contentHandler, new Metadata(), new ParseContext());
            }

            return contentHandler.toString().trim().replaceAll("\n\n\n+", "\n\n"); // geht effizienter, aber erstmal okay so

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
