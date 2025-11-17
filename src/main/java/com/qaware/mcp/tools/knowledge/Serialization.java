package com.qaware.mcp.tools.knowledge;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Utility for compressed Java object serialization.
 *
 * <p><b>Security Warning:</b> Only deserialize data from trusted sources!
 * Java deserialization can execute arbitrary code.
 *
 * <p><b>Performance:</b> Optimized for maximum compression. Serialization
 * is slow (~10ms for typical objects). Use sparingly.
 *
 * <p><b>Thread Safety:</b> All methods are thread-safe (stateless).
 *
 * Idea: keep it simple and avoid external dependencies.
 */
public enum Serialization {

    ;


    public static <T> T readObject(String resourceName)  {
        try (InputStream inputStream = getClassLoader().getResourceAsStream(resourceName)) {
            return Serialization.fromBytes(inputStream.readAllBytes());

        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }


    public static void writeObject(String fileName, Object object)  {
        try (OutputStream outputStream = new FileOutputStream(fileName)) {
            outputStream.write(Serialization.toBytes(object));

        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }


    @SuppressWarnings("java:S4087")
    static byte[] toBytes(Object object) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater);
             ObjectOutputStream   oos = new ObjectOutputStream(dos)) {
            oos.writeObject(object);
            oos.close();
            return baos.toByteArray();

        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);

        } finally {
            deflater.end();
        }
    }


    @SuppressWarnings({"unchecked", "java:S112"})
    static <T> T fromBytes(byte... bytes) {
        Inflater inflater = new Inflater(true);

        try (InflaterInputStream dis = new InflaterInputStream(new ByteArrayInputStream(bytes), inflater);
             ObjectInputStream   ois = new ObjectInputStream(dis);) {
            return (T) ois.readObject();

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            inflater.end();
        }
    }


    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
