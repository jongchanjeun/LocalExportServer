package org.ubstorm.service.parser.formparser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.codec.binary.Base64;

public class ZIPThenBase64Test {

    public static byte[] compress(byte[] str) throws IOException {
        System.out.println("String length : " + str.length);
        ByteArrayOutputStream out = new ByteArrayOutputStream(str.length);
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        try {
            gzip.write(str);
        } finally {
            gzip.close();
        }
        out.close();
        byte ret[] = out.toByteArray();
        return ret;
     }

    public static byte[] decompress(byte[] str) throws IOException {
        System.out.println("Input String length : " + str.length);
        byte[] buf = new byte[str.length*10];
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str));
        int len;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((len = gis.read(buf)) > 0) { 
            out.write(buf, 0, len);
        }
        return out.toByteArray();
     }

    public static String base64Encoding(byte[] str) throws IOException {
        byte[] encoded = Base64.encodeBase64(str);     
        String outStr = new String(encoded);
        return outStr;  
    }
    
    public static byte[] base64Decoding(String str) {
        byte[] decoded = Base64.decodeBase64(str.getBytes());
        return decoded;
    }
    
    private static String encodeCompressString( String jsonStr ) throws IOException {
    	 // Encode
        byte[] b = jsonStr.getBytes(Charset.forName("UTF-8"));
        byte[] compressed = compress(b);
        String encoded = base64Encoding(compressed);
        
        return encoded;
    }

    private static String decodeCompressString( String encodeStr ) throws IOException {
       
       // Decode
       byte decoded[] = base64Decoding(encodeStr);
       byte uncompressed [] = decompress(decoded);
       
       return uncompressed.toString();
   }

}