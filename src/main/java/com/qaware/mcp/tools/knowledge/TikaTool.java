package com.qaware.mcp.tools.knowledge;

import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

/*
jmcp com.qaware.mcp.tools.knowledge.TikaTool
 */
enum TikaTool {

    ;


    public static void main(String[] args) throws Exception {
        System.out.println(parse(new FileInputStream("C:/Viechtbauer/Zeugs/Docs/QAware/AIR/SystemhandbuchAirBMW.docx")));
    }


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


    private static final EmbeddedDocumentExtractor DUMMY_EMBEDDED_DOCUMENT_EXTRACTOR = new EmbeddedDocumentExtractor() {

        @Override
        public boolean shouldParseEmbedded(Metadata metadata) {
            return false;
        }


        @Override
        public void parseEmbedded(InputStream stream, ContentHandler handler, Metadata metadata, boolean outputHtml) {
        }

    };


    private static class MyContentHandler extends DefaultHandler {

        private final StringBuilder writer = new StringBuilder();


        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) {
            String name = localName == null ? qName : localName;

            if ("p".equals(name) && writer.length() > 0 && writer.charAt(writer.length() - 1) == ' ') return;

            writer.append(TAGS.getOrDefault(name, ""));
        }


        @Override
        public void characters(char[] ch, int start, int length) {
            writer.append(ch, start, length);
        }


        @Override
        public String toString() {
            return writer.toString();
        }

    }


    static String parse(InputStream inputStream) {
        try {
            ContentHandler contentHandler = new MyContentHandler();

            ParseContext parseContext = new ParseContext();
            parseContext.set(EmbeddedDocumentExtractor.class, DUMMY_EMBEDDED_DOCUMENT_EXTRACTOR);

            try (inputStream) {
                new AutoDetectParser().parse(inputStream, contentHandler, new Metadata(), parseContext);
            }

            return contentHandler.toString().trim().replaceAll("\n\n\n+", "\n\n"); // geht effizienter, aber erstmal okay so

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
