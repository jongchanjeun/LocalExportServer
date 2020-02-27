package org.ubstorm.service.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.text.html.StyleSheet;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.tools.ant.filters.StringInputStream;
import org.jdom2.input.SAXBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
import org.ubstorm.service.barcode.Barcode128;
import org.ubstorm.service.barcode.Barcode128B;
import org.ubstorm.service.barcode.Barcode128C;
import org.ubstorm.service.barcode.Barcode39;
import org.ubstorm.service.barcode.Barcode93;
import org.ubstorm.service.barcode.BarcodeCodabar;
import org.ubstorm.service.barcode.BarcodeEan128;
import org.ubstorm.service.barcode.BarcodeEan13;
import org.ubstorm.service.barcode.BarcodeInt25;
import org.ubstorm.service.barcode.BarcodeStd25;
import org.ubstorm.service.barcode.BarcodeUPCA;
import org.ubstorm.service.barcode.BaseBarcode;
import org.ubstorm.service.barcode.qrgen.DataMatrix;
import org.ubstorm.service.barcode.qrgen.Ean8Code;
import org.ubstorm.service.barcode.qrgen.ITFCode;
import org.ubstorm.service.barcode.qrgen.QRCode;
import org.ubstorm.service.chart.AreaChart;
import org.ubstorm.service.chart.BarChart;
import org.ubstorm.service.chart.BaseChart;
import org.ubstorm.service.chart.BubbleChart;
import org.ubstorm.service.chart.CandleStickChart;
import org.ubstorm.service.chart.ColumnChart;
import org.ubstorm.service.chart.CombinedColumnChart;
import org.ubstorm.service.chart.DialChart;
import org.ubstorm.service.chart.LineChart;
import org.ubstorm.service.chart.PieChart;
import org.ubstorm.service.chart.PlotChart;
import org.ubstorm.service.chart.PointChart;
import org.ubstorm.service.chart.RadarChart;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.xmlLoadParser;
import org.ubstorm.service.parser.xmlToUbForm;
import org.ubstorm.service.parser.formparser.data.GlobalVariableData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
//import org.ubstorm.service.method.ViewerInfo5;

import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfWriter;
import com.oreilly.servlet.Base64Encoder;

public class common {

	private static final Object monitor = new Object();
	private static final org.slf4j.Logger log = LoggerFactory.getLogger("org.ubstorm.service.utils.common");
	
	public static StringBuffer mybatisConfigDTD = null;
	public static StringBuffer mybatisMapperDTD = null;	
	
	// parameterMap에 넘어온 배열현태의 값에서 첫번째 파라미터만을 꺼낸다.
	public static Map<String, String> flatten(Map<String, String[]> arrayMap){
		Map<String, String> r = new HashMap<String, String>();
		for (Map.Entry<String, String[]> entry: arrayMap.entrySet()){
			String[] value = entry.getValue();
			if (value !=null && value .length>0) 
			{
				//r.put(entry.getKey(), value[0]);
				try {
					r.put(entry.getKey(), new String(value[0].getBytes("8859_1"),"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	  return r;
	}
	
	public static String toEscapeChar(String src) {
		
		String strReplace = src;
		
		if(src != null)
		{
			strReplace = src.replace("\\", "\\\\").replace("\'", "\\\'").replace("\"", "\\\"");		
		}
		
		return strReplace;
	}
	
	public static String flexEscapeChar(String src) {
		
		String strReplace = src;
		
		if(src != null)
		{
			strReplace = src.replace("\\r\\n", "&#13;").replace("\\n\\r", "&#13;").replace("\'", "\\\'").replace("\"", "\\\"");
			//log.debug("common::flexEscapeChar=[" + strReplace + "]");
		}
		
		return strReplace;
	}
	
	
	public static String htmlspecialchars(String src) {
		String nl2Br = "";

		String htmlSpecialChars = "";
		StringBuffer sb = new StringBuffer();
		
		for(int i=0; i<src.length(); i++) {
			char c = src.charAt(i);
			switch (c) {
			 case '<' : 
			   sb.append("&lt;");
			   break;
			case '>' : 
			   sb.append("&gt;");
			   break;
			case '&' :
			   sb.append("&amp;");
			   break;
			case '"' :
			   sb.append("&quot;");
			   break;
			case '\'' :
			   sb.append("&apos;");
			   break;
			default:
			   sb.append(c);
			}
		}
		htmlSpecialChars = sb.toString();
		nl2Br = htmlSpecialChars.replaceAll("\r\n","<br>");
		
		return nl2Br;
	}
	
	public static String htmlspecialcharsamp(String src) {
		String nl2Br = "";

		String htmlSpecialChars = "";
		StringBuffer sb = new StringBuffer();
		
		for(int i=0; i<src.length(); i++) {
			char c = src.charAt(i);
			switch (c) {
			 case '&' :
			   sb.append("&amp;");
			   break;
			default:
			   sb.append(c);
			}
		}
		htmlSpecialChars = sb.toString();
//		nl2Br = htmlSpecialChars.replaceAll("\r\n","<br>");
		nl2Br = htmlSpecialChars;
		
		return nl2Br;
	}

	public static Array objectToArray(Object d) throws Exception {
		Array ad = null;
		if(d instanceof Object) {
			//ad = get_object_vars(d);
		}
		
		if(d instanceof Array) {
			ad = objectToArray(d);
		}
		
		return ad;
	}

	/*
	function objectToArray($d) {
		if (is_object($d)) {
			// Gets the properties of the given object
			// with get_object_vars function
			$d = get_object_vars($d);
		}

		if (is_array($d)) {
			/--*
			* Return array converted to object
			* Using __FUNCTION__ (Magic constant)
			* for recursive call
			*--/
			return array_map(objectToArray, $d);
		}
		else {
			// Return array
			return $d;
		}
	}
	*/

	public static String getMilSecond() throws Exception {
		String time = "";
		long microtime = System.currentTimeMillis();
		
		SimpleDateFormat sdf = new SimpleDateFormat("hhmmss");
		Date dt = new Date();
		time = sdf.format(dt);
		
		return time + ":" + Long.toString(microtime).substring(0, 3);
	}

    
	/*
	 * BASE64 Encoder
	 * @param str
	 * @return
	 * @throws java.io.IOException
	 */
	public static String base64_encode(String str)  throws java.io.IOException {
		byte[] b1 = str.getBytes();
		String result = Base64Coder.encodeLines(b1);
		return result ;
	}
	
	public static String base64_encode_byte(byte[] b1)  throws java.io.IOException {
		String result = Base64Coder.encodeLines2(b1);
		return result ;
	}

	public static String base64_encode(String str, String encoding)  throws java.io.IOException {
		byte[] b1 = str.getBytes(encoding);
		String result = Base64Coder.encodeLines(b1);
		return result ;
	}
	
	/**
	 *  압축 인코딩
	 *  */
	public static String base64_encode_encrypt(String str, String encoding)  throws java.io.IOException {
		
		byte[] input = str.getBytes(encoding);
		   
		  // Compressor with highest level of compression
		  Deflater compressor = new Deflater();
		  compressor.setLevel(Deflater.BEST_COMPRESSION);
		   
		  // Give the compressor the data to compress
		  compressor.setInput(input);
		  compressor.finish();
		   
		  // Create an expandable byte array to hold the compressed data.
		  // It is not necessary that the compressed data will be smaller than
		  // the uncompressed data.
		  ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
		   
		  // Compress the data
		  byte[] buf = new byte[1024];
		  while (!compressor.finished()) {
		      int count = compressor.deflate(buf);
		      bos.write(buf, 0, count);
		  }
		  try {
		      bos.close();
		  } catch (IOException e) {
		  }
		   
		  // Get the compressed data
		  byte[] compressedData = bos.toByteArray();
		  
		return Base64Coder.encodeLines(compressedData) ;
	}

	public static String base64_encode_encrypt2(String str, String encoding)  throws java.io.IOException {
		
		  byte[] input = str.getBytes(encoding);
		   
		  // Compressor with highest level of compression
		  Deflater compressor = new Deflater();
		  compressor.setLevel(Deflater.BEST_COMPRESSION);
		   
		  // Give the compressor the data to compress
		  compressor.setInput(input);
		  compressor.finish();
		   
		  // Create an expandable byte array to hold the compressed data.
		  // It is not necessary that the compressed data will be smaller than
		  // the uncompressed data.
		  ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
		   
		  // Compress the data
		  byte[] buf = new byte[1024];
		  while (!compressor.finished()) {
		      int count = compressor.deflate(buf);
		      bos.write(buf, 0, count);
		  }
		  try {
		      bos.close();
		  } catch (IOException e) {
		  }
		   
		  // Get the compressed data
		  byte[] compressedData = bos.toByteArray();
		  
		return Base64Coder.encodeLines2(compressedData) ;
	}
	
	
	/**
	 * BASE64 Decoder
	 * @param str
	 * @return
	 * @throws java.io.IOException
	 */
	public static String base64_decode(String str)  throws java.io.IOException {
		//byte[] b1 = Base64Coder.decode(str);
		byte[] b1 = Base64Coder.decodeLines(str);
		String result = new String(b1);
		return result ;
	}

	public static String base64_decode(String str, String encoding)  throws java.io.IOException {
		//byte[] b1 = Base64Coder.decode(str);
		byte[] b1 = Base64Coder.decodeLines(str);
		String result = new String(b1, encoding);
		return result ;
	}
	
	public static String base64_decode_uncompress(String str, String encoding)  throws java.io.IOException , DataFormatException {
		//byte[] b1 = Base64Coder.decode(str);
		byte[] b1 = Base64Coder.decodeLines(str);
		
		b1 = decompress(b1);
		
		String result = new String(b1, encoding);
		return result ;
	}
	
	public static String base64_decode_uncompress2(String str, String encoding)  throws java.io.IOException , DataFormatException {
		byte[] b1 = Base64Coder.decode(str);
		
		b1 = decompress(b1);
		
		String result = new String(b1, encoding);
		return result ;
	}
	
	
	public static byte[] decompress(byte[] data) throws IOException, DataFormatException {  
		Inflater inflater = new Inflater();   
		inflater.setInput(data);  

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);  
		byte[] buffer = new byte[1024];  
		while (!inflater.finished()) {  
			int count = inflater.inflate(buffer);  
			outputStream.write(buffer, 0, count);  
		}  
		outputStream.close();  
		byte[] output = outputStream.toByteArray();  

		inflater.end();

		return output;  
	}  
	

	public static boolean isEmptyFile(String fileName) throws java.io.IOException {
		boolean bResult = true;
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(fileName));     
			if (br.readLine() == null) {
			    System.out.println("No errors, and file empty");
			    bResult = true;
			}
			else
			{
				bResult = false;
			}
		}
		catch(IOException e) 
		{
			bResult = true;
		}
		finally
		{
			if(br != null) br.close();
		}
		
		return bResult;
	}
	
	
	public static String file_get_contents(String fileName)  throws java.io.IOException {
		File sFile = new File(fileName);
		return file_get_contents(sFile) ;
	}

	public static String file_get_contents(File file)  throws java.io.IOException {
		String fileContents = "";
		fileContents = file_get_contents(file, null);
		
		return fileContents;
	}

	public static String file_get_contents(File file, String encoding)  throws java.io.IOException {
		String line = null;
		int idx=0;
		FileInputStream is = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		
		StringBuffer strBuffer = new StringBuffer();
		
		try {
		 	if(file.isFile()){
		 		is = new FileInputStream(file);
		 		
		 		if(encoding != null)
		 			isr = new InputStreamReader(is, encoding);
		 		else
		 			isr = new InputStreamReader(is);

		 		reader = new BufferedReader(isr);
		 		
		 		while((line = reader.readLine()) != null) {
		 	        if(idx == 0) {
		 	        	strBuffer.append(line);
		 	        }
		 	        else {
		 	        	strBuffer.append("\r\n").append(line);
		 	        }
		 	        idx++;
		 	    }   
		 	}
		}
		catch(IOException e) 
		{
			throw e;
		}
		finally
		{
			if(reader != null) reader.close();
			if(isr != null) isr.close();
			if(is != null) is.close(); 
		}
		 
		return strBuffer.toString();
	}

	public static String file_get_contents(URL url)  throws java.io.IOException {
		String fileContents = "";
		fileContents = file_get_contents(url, null);
		
		return fileContents;
	}
	 
	public static String file_get_contents(URL url, String encoding) throws java.io.IOException  {
		String s = "";
		StringBuffer fileContents = new StringBuffer();
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader r = null;

		try {
			is = url.openStream();
			isr = new InputStreamReader(is);
			r = new BufferedReader(isr);
			do {
				s = r.readLine();
				if (s != null) {
					log.debug(s);
					fileContents.append(s);
				}
			} while (s != null);
			
			//r.close();
		}
		catch(IOException e) 
		{
			throw e;
		}
		finally
		{
			if(r != null) r.close();
			if(isr != null) isr.close();
			if(is != null) is.close(); 
		}

		return fileContents.toString();
	}

	public static String gzcompress(String str) throws IOException {
		return gzcompress(str, null);
	}

	public static String gzcompress(String str, String encoding) throws IOException {
		String outputString = null;
		try {
		     // Encode a String into bytes
		     byte[] input = null;
		     
		     if(null != encoding) input = str.getBytes(encoding);
		     else input = str.getBytes();

		     // Compress the bytes
		     byte[] output = new byte[1024 * 1024 * 100];
		     Deflater compresser = new Deflater();
		     
		     compresser.setInput(input);
		     compresser.finish();
		     
		     int compressedDataLength = compresser.deflate(output);
		     compresser.end();
		     
		     if(null != encoding) outputString = new String(output, 0, compressedDataLength, encoding);
		     else outputString = new String(output, 0, compressedDataLength);

		}
		catch(Exception e) {
			log.error(e.toString());
		}
		
		return outputString;
	 }

	public static String gzdecompress(String str) throws IOException {
		return gzdecompress(str, null);
	}

	public static String gzdecompress(String str, String encoding) throws IOException {
		byte[] input = null;
		String outputString = null;
		
		try {
			if(null != encoding) input = str.getBytes(encoding);
			else input = str.getBytes();
			
			// Compress the bytes
		    byte[] output = new byte[1024 * 1024 * 100];
		    Inflater decompresser = new Inflater();
		    decompresser.setInput(output, 0, input.length);
		    byte[] result = new byte[10240];
		    int resultLength = decompresser.inflate(result);
		    decompresser.end();
		
		    // Decode the bytes into a String
		    if(null != encoding) outputString = new String(result, 0, resultLength, encoding);
		    else  outputString = new String(result, 0, resultLength);
		}
		catch(Exception e) {
			log.error(e.toString());
		}
		
	    return outputString;
	 }
	
	
	public static Boolean EmptyDir(String dir){
		Boolean rtnValue = false; 
		
		File[] listFile = new File(dir).listFiles(); 
		 try{
			 if(listFile.length > 0){
				 for(int i = 0 ; i < listFile.length ; i++){
					 if(listFile[i].isDirectory()){
						 log.debug("listFile[i].getPath() >>>>> " + listFile[i].getPath());
						 if(EmptyDir(listFile[i].getPath())==false)
						 {
							 rtnValue = false; 
							 break;
						 }
					 }
					 /*
					 if(listFile[i].delete())
						 rtnValue = true; 
					 else
						 rtnValue = false; 
						 */
					 FileDeleteStrategy.FORCE.delete(listFile[i]);

				 }
			 }
		 }catch(Exception e){
			 e.printStackTrace();
			 rtnValue = false;
		 }
		 
		 return rtnValue;
	}
	
	public static ArrayList get_file_list(String dir, ArrayList arr) {
		return get_file_list(dir, arr, null);
	}
	public static ArrayList get_file_list(String dir, ArrayList arr, String option) {
		File[] listFile = new File(dir).listFiles(); 

		try{
			if(listFile != null && listFile.length > 0){
				for(int i = 0 ; i < listFile.length ; i++){
					if(option != null && "history".equals(option) && listFile[i].isDirectory() && "save".equals(listFile[i].getName())) {
						continue;
					}
					else if(listFile[i].isDirectory()) {
						arr = get_file_list(dir+"/"+listFile[i].getName(), arr, option);
					}
					else if(listFile[i].isFile()){
						String filename = listFile[i].getName(); 
						 
						//if(!(filename.equals("Mview.xml") || filename.equals("info.xml") || filename.equals("index.html"))) {
						if(!(filename.equals("Mview.ubx") || filename.equals("info.xml") || filename.equals("index.html"))) {	
							arr.add(filename);
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		 
		return arr;
	}
	
	
	/******************************************************
	Array sort
	******************************************************/
	public static JSONArray merge_sorted_arrays_by_field(JSONArray merge_arrays, String sort_field)  {
		return merge_sorted_arrays_by_field(merge_arrays, sort_field, false, 0);
	}

	public static JSONArray merge_sorted_arrays_by_field(JSONArray merge_arrays, String sort_field, boolean sort_desc)  {
		return merge_sorted_arrays_by_field(merge_arrays, sort_field, sort_desc, 0);
	}

	public static JSONArray merge_sorted_arrays_by_field(JSONArray merge_arrays, String sort_field, int limit)  {
		return merge_sorted_arrays_by_field(merge_arrays, sort_field, false, limit);
	}

	public static JSONArray merge_sorted_arrays_by_field(JSONArray merge_arrays, String sort_field, boolean sort_desc, int limit)  {
		JSONArray results =  new JSONArray();
		int array_count = 0;

		array_count = merge_arrays.size();
		
		//sort_desc false:asc, true:desc
		if(sort_desc) {
			for(int i=0; i < array_count; i++) {
				HashMap tempMerge = (HashMap)merge_arrays.get(i);
				String fieldMerge = (String)tempMerge.get(sort_field);
				
				int results_count = results.size();
				int idx = 0;
				
				for(int j=0; j < results_count; j++) {
					HashMap tempResults = (HashMap)results.get(j);
					String fieldResults = (String)tempResults.get(sort_field);
					
					if(fieldMerge.compareTo(fieldResults) < 0) {
						idx++;
					}
					else {
						break;
					}
				}
				
				results.add(idx, tempMerge);
			}
		}
		else {
			for(int i=0; i < array_count; i++) {
				HashMap tempMerge = (HashMap)merge_arrays.get(i);
				String fieldMerge = (String)tempMerge.get(sort_field);
				
				int results_count = results.size();
				int idx = 0;
				
				for(int j=0; j < results_count; j++) {
					HashMap tempResults = (HashMap)results.get(j);
					String fieldResults = (String)tempResults.get(sort_field);
					
					if(fieldMerge.compareTo(fieldResults) >= 0) {
						idx++;
					}
					else {
						break;
					}
				}
				
				results.add(idx, tempMerge);
			}
		}
		
		return results;
	}

	
	public static String getLocalServerIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		}
		catch (SocketException ex) {}
		
		return "";
	}

	public static String get_file_time(File file)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		Date dt = null;
		String retDate = "";
		
		if(file.isFile()) {
			dt = new Date(file.lastModified());
			retDate = sdf.format(dt);
		}
		
		return retDate;
	}
	
	
	public static Integer getIntClor(String _value) 
	{
		int _result;

		StyleSheet s = new StyleSheet();
		
		// color값이 'red' , 'blue' 처럼 문자로 되어있다면 아래 함수로 color를 뽑아낸다.
		Color _color=s.stringToColor(_value);
		
		if( _color != null ){
			_result = (65536 *_color.getRed()) + (256*_color.getGreen()) + _color.getBlue();
		}else{
			try {
				_result= Integer.valueOf( _value.substring(2, _value.length() ).trim() , 16);
			} catch (Exception e) {
				_result = 16777215; // FFFFFF
			}
		}
		
        return _result;
	}
	
	
	/**
	 * BufferedImage로 부터 base64로 인코딩된 이미지데이터를 얻는다.
	 * @param bi
	 * @return
	 */
	public static String getBase64String(BufferedImage bi)
	{
		String result = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(bi, "png", os);
			result = Base64Encoder.encode(os.toByteArray());
			result = result.replaceAll("(\\r|\\n)", "");			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (IOException e) {}
		}
		
		return result;
	}
	
	
	/**
	 * 입력받은 FontFamily값이 유효한 값인지 여부를 체크한다.
	 * @param fontFamily
	 * @return
	 */
	public static boolean isValidateFontFamily(String ftFamily)
	{
		if(ftFamily == null || "".equals(ftFamily))
			return false;
		
		String data = ftFamily.substring(0, 1);
		if (Pattern.matches("^[0-9a-zA-Z가-힣]*$", data))
			return true;
		else
			return false;
	}
	
	
	/**
	 * properties 파일로 부터 Export시 사용하는 공통파일저장소의 디렉토리경로를 얻는다.
	 * @param void
	 * @return
	 */
	public static String getExportDirPath()
	{
		String _path = "";
		Properties properties = new Properties();
    	
    	try {
			properties.load(new FileInputStream(Log.basePath + "/WEB-INF/classes/ubiform.properties"));
			_path = properties.getProperty("export.SaveAsDir");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		//파일 경로 지정
    	
    	return _path;
	}

	
	
	/**
	 * properties 파일로 부터 Export시 사용하는 공통파일저장소의 디렉토리경로를 얻는다.
	 * @param void
	 * @return
	 */
	public static String getUBFDefaultDirPath()
	{
		String _path = "";
		Properties properties = new Properties();
    	
		_path = getPropertyValue("ubform.DefaultDir");
    	
    	return _path;
	}
	
	
	/**
	 * properties 파일로 부터 특정 속성값을 얻는다.
	 * @param proprtyName (예 : export.SaveAsDir)
	 * @return proprtyName에 해당하는 값
	 */
	public static String getPropertyValue(String proprtyName)
	{
		String propValue = "";
		
		Properties properties = Log.ubiformProps;
		propValue = properties.getProperty(proprtyName);
		
    	return propValue;
	}
	
	public static Object getColumnData( Object _value, boolean _useNull )
	{
		if( !_useNull && _value == null )
		{
			return "";
		}
		return _value;
	}
	
	public static HashMap<String,Float> getOriginSize(Object width,Object height , BufferedImage img ){
		HashMap<String,Float> returnMap = new HashMap<String,Float>();
		
		float _w = Float.valueOf(width.toString());
		float _h = Float.valueOf(height.toString());		
		
		float _reW = 0;
		float _reH = 0;
		float _reWRate = 0;
		float _reHRate = 0;
		
		float _marginX = 0;
		float _marginY = 0;		
		
		float _originW = img.getWidth();
		float _originH = img.getHeight();		
		float _ratio = 0;
		
		_reHRate = _h /_originH;
		_reWRate = _w /_originW;
		
		if(_reHRate > _reWRate) _ratio =  _reWRate;
		else _ratio =  _reHRate;		
		
		_reH = Double.valueOf(Math.floor( _originH *_ratio)).floatValue();
		_reW = Double.valueOf(Math.floor( _originW *_ratio)).floatValue();
		_reHRate = _ratio;
		_reWRate = _ratio;
		_marginX = (_w -_reW) / 2;  
		_marginY = (_h - _reH) / 2;   		
		
		returnMap.put("width", _reW);
		returnMap.put("height", _reH);
		returnMap.put("widthRate", _reWRate);
		returnMap.put("heightRate", _reHRate);
		returnMap.put("marginX", _marginX);
		returnMap.put("marginY", _marginY);		
		return returnMap;
	}
	
	public static HashMap<String,Float> getOriginSize(Object width,Object height , Image img ){
		HashMap<String,Float> returnMap = new HashMap<String,Float>();
		
		float _w = Float.valueOf(width.toString());
		float _h = Float.valueOf(height.toString());		
		
		float _reW = 0;
		float _reH = 0;
		float _reWRate = 0;
		float _reHRate = 0;
		
		float _marginX = 0;
		float _marginY = 0;		
		
		float _originW = img.getWidth();
		float _originH = img.getHeight();		
		float _ratio = _originH / _originW;
		
		_reHRate = _h /_originH;
		_reWRate = _w /_originW;
		
		if(_reHRate > _reWRate) _ratio =  _reWRate;
		else _ratio =  _reHRate;		
		
		_reH = _originH *_ratio;
		_reW = _originW *_ratio;	
		_reHRate = _ratio;
		_reWRate = _ratio;
		_marginX = (_w -_reW) / 2;  
		_marginY = (_h - _reH) / 2;   		

		returnMap.put("width", _reW);
		returnMap.put("height", _reH);
		returnMap.put("widthRate", _reWRate);
		returnMap.put("heightRate", _reHRate);
		returnMap.put("marginX", _marginX);
		returnMap.put("marginY", _marginY);		
		return returnMap;
	}
	
	
	public static HashMap getUrlVars(String _imageUrl) {
		
		HashMap vars = new HashMap();
	    String hashes[] = _imageUrl.substring(_imageUrl.indexOf('?') + 1).split("&");
	    String hash[] = null;
        if(hashes.length == 1 && "http".equals(hashes[0].substring(0,4))) return vars;
        
        for(int i = 0; i < hashes.length; i++) {
            hash = hashes[i].split("=");
            if(hash.length == 2)
            	vars.put(hash[0], hash[1]);
            else if(hash.length == 1)
            	vars.put(hash[0], "");
        }

        return vars;
}
	
	
	// 원격지에 있는 HTTP 서비스를 호출하여 이미지 데이터를 받아온다.
	public static byte[] getBytesRemoteImageFile(String _imageUrl)
	{
		
		if("".equals(_imageUrl) ) return null;
		
//		String _httpCheck=_imageUrl.substring(0, 4);
		String _httpCheck=_imageUrl;
		if( _imageUrl != null && _imageUrl.length() > 4)
		{
			_httpCheck = _imageUrl.substring(0, 4);
		}
		
		if( _httpCheck.equalsIgnoreCase("http") == false ){
			_imageUrl = Log.dataSetURL+_imageUrl;
		}
		
		
		if( _imageUrl.indexOf("[")  > -1 ||  _imageUrl.indexOf("]")  > -1 ){
			
			_imageUrl = _imageUrl.replace("[", "%5B" );
			_imageUrl = _imageUrl.replace("]", "%5D" );
		}
		
//			String _url =  _imageUrl.substring( 0, _imageUrl.lastIndexOf("/")+1 );
//			String _lastUrl = _imageUrl.substring( _imageUrl.lastIndexOf("/")+1, _imageUrl.length() );
//			_imageUrl = _url +  URLEncoder.encode(_lastUrl,"UTF-8").replaceAll("\\+","%20");
		_imageUrl = _imageUrl.replaceAll(" ","%20");
		
		boolean bSSL = _imageUrl.startsWith("https://");
		byte[] bAr = null;
		HttpGet httpget = new HttpGet(_imageUrl);		
		HttpClient httpclient = null;
		
		HttpClientBuilder _httpBuilder = HttpClientBuilder.create();
		
		// SSL 요청을 위한 TrustManager
		TrustManager easyTrustManager = new X509TrustManager() {

            public X509Certificate[] getAcceptedIssuers() {
                // no-op
                return null;
            }

            public void checkServerTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            public void checkClientTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }
        };
				
		try
		{
			if(bSSL)
			{
			  SSLContext sslcontext = SSLContext.getInstance("TLS");
			        sslcontext
			                .init(null, new TrustManager[] { easyTrustManager }, null);
			        
		        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		        _httpBuilder.setSSLSocketFactory(sslConnectionFactory);
			     
		        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
		                .register("https", sslConnectionFactory)
		                .build();

		        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);

		        _httpBuilder.setConnectionManager(ccm);
		        
			        
			}
			
			String _userAgent = "UBFORM";
			
			_httpBuilder.setUserAgent(_userAgent);
			httpclient = _httpBuilder.build();
			log.debug( " httpget.getURI() " + httpget.getURI() );
		    HttpResponse response = httpclient.execute(httpget); 
	        HttpEntity entity = response.getEntity();
	        //m_reqManager.getServiceManager().getHttpRequest().getSession().getId()
	        //String responseBody = EntityUtils.toString(entity, "UTF-8");
            //System.out.println("https request response body================>[" + responseBody + "]");
	        
	        if (entity != null) {
	            InputStream inputStream = entity.getContent();
	
	            BufferedImage bimg = ImageIO.read(inputStream);
	            if(bimg != null)
	            {
	            	String _imageType = "jpg";
	            	if( bimg.getColorModel().hasAlpha() ){
	            		_imageType = "png";
	            	}
	            	
	            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            	ImageIO.write( bimg, _imageType, baos );
	            	bAr = baos.toByteArray();
	            	baos.close();
	            }
	        }	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} 
		finally
		{
			 httpget.abort();
	         httpclient.getConnectionManager().shutdown();
	         httpclient = null;
	         _httpBuilder = null;
		}
        
        return bAr;
	}
	
	private static String inputStreamToString(InputStream is)
	{

	    String line = "";
	    StringBuilder total = new StringBuilder();
	    // Wrap a BufferedReader around the InputStream
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is), 1024 * 4);
	    // Read response until the end
	    try
	    {

	        while ((line = rd.readLine()) != null)
	        {
	            total.append(line);
	        }
	    } catch (IOException e)
	    {
	        
	    }
	    // Return full string
	    return total.toString();
	}
	
	// 로컬에 있는 서블릿 메소드를 호출하여 이미지 데이터를 받아온다.
	public static byte[] getBytesLocalImageFile(String _imageUrl)
	{
		byte[] bAr = null;
		
		// URL이 빈값일 경우 리턴처리 
		if("".equals(_imageUrl) ) return null;
		
		if( _imageUrl.startsWith("http") ){
			HashMap httpparams = getUrlVars(_imageUrl);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String METHOD_NAME = "";
			
			String IMG_TYPE = "";
			String FILE_NAME = "";
			String PROJECT_NAME = "";
			String FOLDER_NAME = "";
			String DATA_ID = "";
			String PARAM = "";	// , 문자로 구분된 차트생성을 위한 파라미터들(배열형태의 값은 ~로 구분하여 연결함)
			String MODEL_TYPE = "";	// AreaChart, LineChart, Barcode model type
			String SHOW_LABEL = "";
			String FILE_CONTENT = "";
			
			try
			{
				METHOD_NAME = (String) httpparams.get("METHOD_NAME");  
				
			    //ViewerInfo5 vi5 = new ViewerInfo5();
			    if("getChartImage".equals(METHOD_NAME))
			    {
			    	IMG_TYPE = (String) httpparams.get("IMG_TYPE");
					FILE_NAME = (String) httpparams.get("FILE_NAME");
					PROJECT_NAME = (String) httpparams.get("PROJECT_NAME");
					FOLDER_NAME = (String) httpparams.get("FORM_ID");
					DATA_ID = (String) httpparams.get("DATASET");
					PARAM = (String) httpparams.get("PARAM");
					MODEL_TYPE = (String) httpparams.get("MODEL_TYPE");
					
			    	//vi5.getLocalChartImage(baos, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, DATA_ID, PARAM, MODEL_TYPE);
			    	getLocalChartImage(baos, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, DATA_ID, PARAM, MODEL_TYPE);
			    	bAr = baos.toByteArray();
			    }
			    else if("getBarcodeImage".equals(METHOD_NAME))
			    {
			    	SHOW_LABEL = (String) httpparams.get("SHOW_LABEL");
			    	IMG_TYPE = (String) httpparams.get("IMG_TYPE");
			    	MODEL_TYPE = (String) httpparams.get("MODEL_TYPE");
			    	FILE_CONTENT = (String) httpparams.get("FILE_CONTENT");
			    	
			    	//vi5.getLocalBarcodeImage(baos, SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
			     	getLocalBarcodeImage(baos, SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
			     	bAr = baos.toByteArray();
			    }
			    else
			    {
			    	bAr = getBytesRemoteImageFile(_imageUrl);
			    }
			}
			catch(Exception e)
			{
				e.printStackTrace();
			} 
			finally
			{
				try {
					baos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}
		else
		{
			if(_imageUrl.indexOf("+") != -1 )
			{
				bAr = org.apache.commons.codec.binary.Base64.decodeBase64(_imageUrl.getBytes());
			}
			else
			{
				//bAr = Base64Coder.decode(URLDecoder.decode(_imageUrl));
				bAr = org.apache.commons.codec.binary.Base64.decodeBase64(URLDecoder.decode(_imageUrl).getBytes());
			}
		}
        
        return bAr;
	}
	
	
	public static void getLocalBarcodeImage(OutputStream out, String SHOW_LABEL, String IMG_TYPE, String MODEL_TYPE, String FILE_CONTENT) throws Exception
	{
		BaseBarcode barcode = null;
		String ba64Data = "";
		boolean isShowLabel = "true".equals(SHOW_LABEL);
		
		log.info("Call getLocalBarcodeImage...type=" + MODEL_TYPE);
		
		if( FILE_CONTENT.equals("") )
		{
			return;
		}
		
		if("barcode".equals(IMG_TYPE))
		{	
			if(MODEL_TYPE.startsWith("datamatrix"))
			{
				DataMatrix dmatrix = new DataMatrix();
				if("datamatrix2".equalsIgnoreCase(MODEL_TYPE))
					dmatrix.makeBarcode2(FILE_CONTENT, 100);
				else
					dmatrix.makeBarcode(FILE_CONTENT, 100);
				dmatrix.writeToStream(out);
			}	
			else
			{	
				if("code128".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new Barcode128();
					barcode.makeBarcode(FILE_CONTENT, -1, -1, isShowLabel);
					barcode.writeToStream(out);
				}
				else if("code128B".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new Barcode128B();
					barcode.makeBarcode(FILE_CONTENT, -1, -1, isShowLabel);
					barcode.writeToStream(out);
				}
				else if("code128C".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new Barcode128C();
					barcode.makeBarcode(FILE_CONTENT, -1, -1, isShowLabel);
					barcode.writeToStream(out);
				}
				else if("code39".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new Barcode39();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
					barcode.writeToStream(out);
				}
				else if("code93".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new Barcode93();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
					barcode.writeToStream(out);
				}
				else if("codabar".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeCodabar();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
					barcode.writeToStream(out);
				}
				else if("std25".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeStd25();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
					barcode.writeToStream(out);
				}
				else if("i2of5".equalsIgnoreCase(MODEL_TYPE) || "int25".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeInt25();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
					barcode.writeToStream(out);
				}
				else if("ean128".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeEan128();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
					barcode.writeToStream(out);
				}
				else if("ean13".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeEan13();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
					barcode.writeToStream(out);
				}
				else if("upc".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeUPCA();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
					barcode.writeToStream(out);
				}
				else if("ean8".equalsIgnoreCase(MODEL_TYPE))
				{
					Ean8Code eancode = new Ean8Code();
					eancode.makeBarcode(FILE_CONTENT, 2, 80);
					eancode.writeToStream(out);
					
					/*
					ba64Data = eancode.getBase64String();
					System.out.println("eancodeImage::b64=[" + ba64Data + "]");
					*/
				}
				else if("itf14".equalsIgnoreCase(MODEL_TYPE))
				{
					ITFCode itfcode = new ITFCode();
					itfcode.makeBarcode(FILE_CONTENT, 2, 80);
					itfcode.writeToStream(out);
				}
			}
		}
		else if("qrcode".equals(IMG_TYPE))
		{
			QRCode qrcode = new QRCode();
			qrcode.makeBarcode(FILE_CONTENT, 100);
			qrcode.writeToStream(out);
			
			/*
			File file = new File("C:\\Users\\User\\Downloads\\qrcode.png");
			qrcode.writeToFile(file);
			
			ba64Data = qrcode.getBase64String();
			System.out.println("qrcodeImage::b64=[" + ba64Data + "]");
			*/
		}
//		else if("ean8".equals(IMG_TYPE))
//		{
//			Ean8Code eancode = new Ean8Code();
//			eancode.makeBarcode(FILE_CONTENT, 2, 80);
//			eancode.writeToStream(out);
//			
//			/*
//			ba64Data = eancode.getBase64String();
//			System.out.println("eancodeImage::b64=[" + ba64Data + "]");
//			*/
//		}
		
		if(barcode != null)
		{
			//ba64Data = barcode.getBase64String();
			//System.out.println("BarcodeImage::b64=[" + ba64Data + "]");
		}
	}
	
	
	public static String getLocalBarcodeImageToBase64(String SHOW_LABEL, String IMG_TYPE, String MODEL_TYPE, String FILE_CONTENT) throws Exception
	{
		BaseBarcode barcode = null;
		String ba64Data = "";
		boolean isShowLabel = "true".equals(SHOW_LABEL);
		
		log.info("Call getLocalBarcodeImageToBase64..IMG_TYPE=" + IMG_TYPE + ", modeltype=" + MODEL_TYPE);
		
		if( FILE_CONTENT.equals("") )
		{
			return ba64Data;
		}
		
		if("barcode".equals(IMG_TYPE))
		{	
			if(MODEL_TYPE.startsWith("datamatrix"))
			{
				DataMatrix dmatrix = new DataMatrix();
				if("datamatrix2".equalsIgnoreCase(MODEL_TYPE))
					dmatrix.makeBarcode2(FILE_CONTENT, 100);
				else
					dmatrix.makeBarcode(FILE_CONTENT, 100);

				ba64Data = dmatrix.getBase64String();
				
				dmatrix = null;
				
			}	
			else
			{	
				if("code128".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new Barcode128();
					barcode.makeBarcode(FILE_CONTENT, -1, -1, isShowLabel);
				}
				else if("code128B".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new Barcode128B();
					barcode.makeBarcode(FILE_CONTENT, -1, -1, isShowLabel);
				}
				else if("code128C".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new Barcode128C();
					barcode.makeBarcode(FILE_CONTENT, -1, -1, isShowLabel);
				}
				else if("code39".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new Barcode39();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
				}
				else if("code93".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new Barcode93();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
				}
				else if("codabar".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeCodabar();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
				}
				else if("std25".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeStd25();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
				}
				else if("i2of5".equalsIgnoreCase(MODEL_TYPE) || "int25".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeInt25();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
				}
				else if("ean128".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeEan128();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
				}
				else if("ean13".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeEan13();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
				}
				else if("upc".equalsIgnoreCase(MODEL_TYPE))
				{
					barcode = new BarcodeUPCA();
					barcode.makeBarcode(FILE_CONTENT, 2, 80, isShowLabel);
				}
				else if("ean8".equalsIgnoreCase(MODEL_TYPE))
				{
					Ean8Code eancode = new Ean8Code();
					eancode.makeBarcode(FILE_CONTENT, 2, 80);
					ba64Data = eancode.getBase64String();
				}
				else if("itf14".equalsIgnoreCase(MODEL_TYPE))
				{
					ITFCode itfcode = new ITFCode();
					itfcode.makeBarcode(FILE_CONTENT, 2, 80);
					ba64Data = itfcode.getBase64String();
				}
				
				if(barcode != null)
				{
					ba64Data = barcode.getBase64String();
					barcode = null;
				}
			}
		}
		else if("qrcode".equals(IMG_TYPE))
		{
			QRCode qrcode = new QRCode();
			qrcode.makeBarcode(FILE_CONTENT, 200);
			ba64Data = qrcode.getBase64String();
			qrcode = null;
		}
//		else if("ean8".equals(IMG_TYPE))
//		{
//			Ean8Code eancode = new Ean8Code();
//			eancode.makeBarcode(FILE_CONTENT, 2, 80);
//			ba64Data = eancode.getBase64String();
//		}
		
		return ba64Data;
	}
	
	
	public static void getLocalChartImage(OutputStream out, String IMG_TYPE, String FILE_NAME, String PROJECT_NAME, String FOLDER_NAME, String DATA_ID, String PARAM, String MODEL_TYPE) throws Exception
	{
		BaseChart chart = null;
		String ba64Data = "";
	
		log.info("common" + "::" + "Call getLocalChartImage...type=" + IMG_TYPE);
		
		String _seriesXField = null;
		String[] _yFieldName = null;
		String[] _yFieldDisplayName = null;
		boolean _crossTab = false; 
		String _form = "segment";
		boolean _gridLIne = true; 
		int _gridLineWeight = 1;
		String _gridLIneDirection = "both";
		int _gridLIneColor = 0xd8d3d3;
		String _legendDirection = "vertical";
		String _legendLabelPlacement = "right";
		int _legendMarkHeight = 10;
		int _legendMarkWeight = 10;
		String _legendLocation = "bottom";
		String _dataLabelPosition = "inside";
		boolean _DubplicateAllow = false; 
		int [] _yFieldFillColor = null;
		String _seriexCloseField = null;
		String _seriesHighField = null;
		String _seriesLowField = null;
		String _seriesOpenField = null;
		
		boolean _isMakeSuccess = false; 
		
		String [] _params = PARAM.split(",");
		if(_params != null && (_params.length == 17 || _params.length == 21) )
		{
			_seriesXField = "".equals(_params[0]) ? null : _params[0];
			_yFieldName = "".equals(_params[1]) ? null : _params[1].split("~");
			_yFieldDisplayName = "".equals(_params[2]) ? null : _params[2].split("~");
			_crossTab = "".equals(_params[3]) ? false : Boolean.valueOf(_params[3]); 
			_form = "".equals(_params[4]) ? "segment" : _params[4];
			_gridLIne = "".equals(_params[5]) ? true : Boolean.valueOf(_params[5]); 
			_gridLineWeight = "".equals(_params[6]) ? 1 : Integer.valueOf(_params[6]);
			_gridLIneDirection = "".equals(_params[7]) ? "both" : _params[7];
			_gridLIneColor = "".equals(_params[8]) ? 0xd8d3d3 : Integer.decode(_params[8]);
			_legendDirection = "".equals(_params[9]) ? "vertical" : _params[9];
			_legendLabelPlacement = "".equals(_params[10]) ? "right" : _params[10];
			_legendMarkHeight = "".equals(_params[11]) ? 10 : Integer.valueOf(_params[11]);
			_legendMarkWeight = "".equals(_params[12]) ? 10 : Integer.valueOf(_params[12]);
			_legendLocation = "".equals(_params[13]) ? "bottom" : _params[13];
			_dataLabelPosition = "".equals(_params[14]) ? "inside" : _params[14];
			_DubplicateAllow = "".equals(_params[15]) ? false : Boolean.valueOf(_params[15]); 
			_yFieldFillColor = null;
			if(!"".equals(_params[16]))
			{			
				String[] _staYFC = _params[16].split("~");
				_yFieldFillColor = new int[_staYFC.length];
	            for(int i=0; i< _staYFC.length; i++)
	            {
	            	_yFieldFillColor[i] = Integer.decode(_staYFC[i]);
	            }
			}
			
			if(_params.length == 21)
			{
				_seriexCloseField = "".equals(_params[17]) ? null : _params[17];
				_seriesHighField = "".equals(_params[18]) ? null : _params[18];
				_seriesLowField = "".equals(_params[19]) ? null : _params[19];
				_seriesOpenField = "".equals(_params[20]) ? null : _params[20];
			}
		}
		
		ArrayList<ArrayList<HashMap<String, Object>>> _dataAcList = null;
		ArrayList<HashMap<String, Object>> _dataAC = null;
		
		if("combcolumn".equals(IMG_TYPE))
		{
			_dataAcList = new ArrayList<ArrayList<HashMap<String, Object>>>();
			
			String [] arrDataId = DATA_ID.split(":");
			
			for(int i=0; i< arrDataId.length; i++)
			{
				ArrayList<HashMap<String, Object>> _tmpdataAC = GetChartData(PROJECT_NAME, FOLDER_NAME, FILE_NAME, arrDataId[i]);
				_dataAcList.add(_tmpdataAC);
			}
		}
		else
		{
			_dataAC = new ArrayList<HashMap<String, Object>>();
			/*
			for(int i=0; i<5; i++)
			{
				HashMap<String, Object> _map = new HashMap<String, Object>();
				_map.put("col_1", "Type" + i);
				for(int j=1; j<= 3; j++)
				{
					_map.put("col_2", i*10 + j);
					_map.put("col_3", i*8 + j);
					_map.put("col_4", i*6 + j);
					_map.put("col_5", i*4 + j);
					_map.put("col_6", i*2 + j);
				}
				
				_dataAC.add(_map);
			}
			*/
			_dataAC = GetChartData(PROJECT_NAME, FOLDER_NAME, FILE_NAME, DATA_ID);
		}
		
		if("pie".equals(IMG_TYPE))
		{
			chart = new PieChart(400, 300);
			/*
			_isMakeSuccess = chart.setGraphData(_dataAC, null, 
					"col_2", new String[]{"col_4"}, new String[]{"series1"}, false,
					"segment", true, 1, "both", 0x000000, "vertical", "right", 10, 10, "right", "inside", false, new int[]{0xFF00FF});
			*/
			_isMakeSuccess = chart.setGraphData(_dataAC, null, 
					_seriesXField, _yFieldName, _yFieldDisplayName, _crossTab,
					_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, 
					_legendLocation, _dataLabelPosition, _DubplicateAllow, _yFieldFillColor);			
		}
		else if("bar".equals(IMG_TYPE))
		{	
			chart = new BarChart(400, 300, MODEL_TYPE);
			/*
			chart.setGraphData(_dataAC, null, 
					"col_1", new String[]{"col_2","col_3","col_4"}, new String[]{"series1","series2","series3"}, false,
					"segment", true, 1, "both", 0x000000, "vertical", "right", 10, 10, "bottom", "inside", false, new int[]{0xFF00FF,0xFF0000,0x0000FF});
			*/
			_isMakeSuccess = chart.setGraphData(_dataAC, null, 
					_seriesXField, _yFieldName, _yFieldDisplayName, _crossTab,
					_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, 
					_legendLocation, _dataLabelPosition, _DubplicateAllow, _yFieldFillColor);
		}
		else if("column".equals(IMG_TYPE))
		{
			chart = new ColumnChart(400, 300, MODEL_TYPE);
			/*
			chart.setGraphData(_dataAC, null,  
					"col_1", new String[]{"col_2","col_3","col_4"}, new String[]{"series1","series2","series3"}, false,
					"segment", true, 1, "both", 0x000000, "vertical", "right", 10, 10, "bottom", "inside", false, new int[]{0xFF00FF,0xFF0000,0x0000FF});
			*/
			_isMakeSuccess = chart.setGraphData(_dataAC, null, 
					_seriesXField, _yFieldName, _yFieldDisplayName, _crossTab,
					_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, 
					_legendLocation, _dataLabelPosition, _DubplicateAllow, _yFieldFillColor);
		}
		else if("line".equals(IMG_TYPE))
		{
			chart = new LineChart(400, 300);
			/*
			chart.setGraphData(_dataAC, null,  
					"col_1", new String[]{"col_2","col_3","col_4"}, new String[]{"series1","series2","series3"}, false,
					"segment", true, 1, "both", 0x000000, "vertical", "right", 10, 10, "bottom", "inside", false, new int[]{0xFF00FF,0xFF0000,0x0000FF});
			*/
			_isMakeSuccess = chart.setGraphData(_dataAC, null, 
					_seriesXField, _yFieldName, _yFieldDisplayName, _crossTab,
					_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, 
					_legendLocation, _dataLabelPosition, _DubplicateAllow, _yFieldFillColor);
		}
		else if("bubble".equals(IMG_TYPE))
		{
			chart = new BubbleChart(400, 300);
			/*
			chart.setGraphData(_dataAC, null,  
					"col_1", new String[]{"col_2","col_3","col_4"}, new String[]{"series1","series2","series3"}, false,
					"segment", true, 1, "both", 0x000000, "vertical", "right", 10, 10, "bottom", "inside", false, new int[]{0xFF00FF,0xFF0000,0x0000FF});
			*/
			_isMakeSuccess = chart.setGraphData(_dataAC, null, 
					_seriesXField, _yFieldName, _yFieldDisplayName, _crossTab,
					_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, 
					_legendLocation, _dataLabelPosition, _DubplicateAllow, _yFieldFillColor);
		}
		else if("point".equals(IMG_TYPE))
		{
			chart = new PointChart(400, 300);
			/*
			chart.setGraphData(_dataAC, null,  
					"col_1", new String[]{"col_2","col_3","col_4"}, new String[]{"series1","series2","series3"}, false,
					"segment", true, 1, "both", 0x000000, "vertical", "right", 10, 10, "none", "inside", false, new int[]{0xFF00FF,0xFF0000,0x0000FF});
			*/
			_isMakeSuccess = chart.setGraphData(_dataAC, null, 
					_seriesXField, _yFieldName, _yFieldDisplayName, _crossTab,
					_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, 
					_legendLocation, _dataLabelPosition, _DubplicateAllow, _yFieldFillColor);
		}
		else if("area".equals(IMG_TYPE))
		{
			chart = new AreaChart(400, 300, MODEL_TYPE);
			/*
			chart.setGraphData(_dataAC, null,  
					"col_2", new String[]{"col_4","col_5","col_6"}, new String[]{"series1","series2","series3"}, false,
					"segment", true, 1, "both", 0x000000, "vertical", "right", 10, 10, "bottom", "inside", false, new int[]{0xFF00FF,0xFF0000,0x0000FF});
			*/
			_isMakeSuccess = chart.setGraphData(_dataAC, null, 
					_seriesXField, _yFieldName, _yFieldDisplayName, _crossTab,
					_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, 
					_legendLocation, _dataLabelPosition, _DubplicateAllow, _yFieldFillColor);
		}
		else if("candle".equals(IMG_TYPE))
		{
			chart = new CandleStickChart(400, 300);
			/*
			chart.setGraphData(_dataCandleAC, null,  
					"col_1", "col_2", "col_3", "col_4", "col_5",
					null, null, false,
					"segment", true, 1, "both", 0x000000, "vertical", "right", 10, 10, "bottom", "inside", false, null);
			*/
		
			_isMakeSuccess = chart.setGraphData(_dataAC, null, 
					_seriesXField, _seriexCloseField, _seriesHighField, _seriesLowField, _seriesOpenField,
					_yFieldName, _yFieldDisplayName, _crossTab,
					_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, 
					_legendLocation, _dataLabelPosition, _DubplicateAllow, _yFieldFillColor);
		}
		else if("combcolumn".equals(IMG_TYPE))
		{
			chart = new CombinedColumnChart(400, 300, MODEL_TYPE);
			
			_isMakeSuccess = chart.setGraphData(_dataAcList, null, 
					_seriesXField, _yFieldName, _yFieldDisplayName, _crossTab,
					_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, 
					_legendLocation, _dataLabelPosition, _DubplicateAllow, _yFieldFillColor, true);
		}
		
		if( _isMakeSuccess == false )
		{
			log.error("ViewerInfo5::getLocalChartImage()...Chart image릉 얻는데 실패하였습니다.");
		}
		else
		{
			if(chart != null)
				chart.writeChartToStream(out);
		}
	}
	
	public static String getLocalChartImageToBase64(ArrayList<HashMap<String, Object>> _dataAC, int itemWidth, int itemHeight, String IMG_TYPE, String FILE_NAME, String PROJECT_NAME, String FOLDER_NAME, String PARAM, String MODEL_TYPE) throws Exception
	{
		BaseChart chart = null;
		String ba64Data = "";
	
		log.debug("common" + "::" + "Call getLocalChartImageToBase64...type=" + IMG_TYPE);
		
		int _chartWidth = itemWidth > 0 ? itemWidth : 400;
		int _chartHeight = itemHeight > 0 ? itemHeight : 300;
		
		boolean _isMakeSuccess = false;
		
		
		String [] _params = PARAM.split(",");
		
		
		if("pie".equals(IMG_TYPE))
		{
			chart = new PieChart(_chartWidth, _chartHeight);
			_isMakeSuccess = chart.setGraphData(_dataAC, _params );
		}
		else if("bar".equals(IMG_TYPE))
		{	
			chart = new BarChart(_chartWidth, _chartHeight, MODEL_TYPE);
			_isMakeSuccess = chart.setGraphData(_dataAC, _params );
		}
		else if("column".equals(IMG_TYPE))
		{
			chart = new ColumnChart(_chartWidth, _chartHeight, MODEL_TYPE);
			_isMakeSuccess = chart.setGraphData(_dataAC, _params );
		}
		else if("line".equals(IMG_TYPE))
		{
			chart = new LineChart(_chartWidth, _chartHeight);
			_isMakeSuccess = chart.setGraphData(_dataAC, _params );
		}
		else if("bubble".equals(IMG_TYPE))
		{
			chart = new BubbleChart(_chartWidth, _chartHeight);
			_isMakeSuccess = chart.setGraphData(_dataAC, _params );
		}
		else if("plot".equals(IMG_TYPE))
		{
			chart = new PlotChart(_chartWidth, _chartHeight);
			_isMakeSuccess = chart.setGraphData(_dataAC, _params );
		}
		else if("point".equals(IMG_TYPE))
		{
			chart = new PointChart(_chartWidth, _chartHeight);
			_isMakeSuccess = chart.setGraphData(_dataAC, _params );
		}
		else if("area".equals(IMG_TYPE))
		{
			chart = new AreaChart(_chartWidth, _chartHeight, MODEL_TYPE);
			_isMakeSuccess = chart.setGraphData(_dataAC, _params );
			
		}
		else if("candle".equals(IMG_TYPE))
		{
			chart = new CandleStickChart(_chartWidth, _chartHeight);
			_isMakeSuccess = chart.setGraphData(_dataAC, _params );
		}
		else if("taximeter".equals(IMG_TYPE))
		{
			chart = new DialChart(_chartWidth, _chartHeight);
			_isMakeSuccess = chart.setGraphData(_dataAC, _params );
		}
		
		
		else if("radar".equals(IMG_TYPE))
		{
			chart = new RadarChart(_chartWidth, _chartHeight);
			_isMakeSuccess = chart.setGraphData(_dataAC, _params );
		}
		
		if( _isMakeSuccess == false )
		{
			log.error("ViewerInfo5::getLocalChartImage()...Chart image릉 얻는데 실패하였습니다.");
			return "";
		}
		else
		{
			ba64Data = chart.getBase64String();
			//ba64Data = chart.getSvgData();
		}
		chart = null;
		return ba64Data;
	}
	
	public static String getLocalChartImageToBase64M(ArrayList<ArrayList<HashMap<String, Object>>> _dataACL, int itemWidth, int itemHeight, String IMG_TYPE, String FILE_NAME, String PROJECT_NAME, String FOLDER_NAME, HashMap<Integer, String> displayNamesMap, String MODEL_TYPE) throws Exception
	{
		//float _rangeMax=0;
		BaseChart chart = null;
		String ba64Data = "";
	
		log.info("common" + "::" + "Call getLocalChartImageToBase64...type=" + IMG_TYPE);
		
		int _chartWidth = itemWidth > 0 ? itemWidth : 400;
		int _chartHeight = itemHeight > 0 ? itemHeight : 300;
		
//		String[] _seriesXField = null;
//		String[] _yFieldName = null;
//		String[] _yFieldDisplayName = null;
//		boolean _crossTab = false; 
//		String _form = "segment";
//		boolean _gridLIne = true; 
//		int _gridLineWeight = 1;
//		String _gridLIneDirection = "both";
//		int _gridLIneColor = 0xd8d3d3;
//		String _legendDirection = "vertical";
//		String _legendLabelPlacement = "right";
//		int _legendMarkHeight = 10;
//		int _legendMarkWeight = 10;
//		String _legendLocation = "bottom";
//		String _dataLabelPosition = "inside";
//		boolean _DubplicateAllow = false; 
//		int [] _yFieldFillColor = null;
//		String _seriexCloseField = null;
//		String _seriesHighField = null;
//		String _seriesLowField = null;
//		String _seriesOpenField = null;
//		
		boolean _isMakeSuccess = false; 
//		
//		String [] _params;
//		
//		HashMap<Integer, String[]> _yFieldNames = new HashMap<Integer, String[]>();
//		HashMap<Integer, String[]> _yFieldDisplayNames = new HashMap<Integer, String[]>();
//		HashMap<Integer, int []> _yFieldFillColors = new HashMap<Integer, int[]>();
//		
//		String _dispNamesValue="";
//		
//		for( int dispIndex=0; dispIndex<displayNamesMap.size(); dispIndex++ ){
//			
//			_dispNamesValue = displayNamesMap.get(dispIndex);
//			_params= _dispNamesValue.split(",");
//			
//			if(_params != null && (_params.length == 17 || _params.length == 23 || _params.length == 27 || _params.length == 32))
//			{
//				_seriesXField = "".equals(_params[0]) ? null : _params[0].split("~");
//				_yFieldName = "".equals(_params[1]) ? null : _params[1].split("~");
//				_yFieldDisplayName = "".equals(_params[2]) ? null : _params[2].split("~");
//				_crossTab = "".equals(_params[3]) ? false : Boolean.valueOf(_params[3]); 
//				_form = "".equals(_params[4]) ? "segment" : _params[4];
//				_gridLIne = "".equals(_params[5]) ? true : Boolean.valueOf(_params[5]); 
//				_gridLineWeight = "".equals(_params[6]) ? 1 : Integer.valueOf(_params[6]);
//				_gridLIneDirection = "".equals(_params[7]) ? "both" : _params[7];
//				_gridLIneColor = "".equals(_params[8]) ? 0xd8d3d3 : Integer.decode(_params[8]);
//				_legendDirection = "".equals(_params[9]) ? "vertical" : _params[9];
//				_legendLabelPlacement = "".equals(_params[10]) ? "right" : _params[10];
//				_legendMarkHeight = "".equals(_params[11]) ? 10 : Integer.valueOf(_params[11]);
//				_legendMarkWeight = "".equals(_params[12]) ? 10 : Integer.valueOf(_params[12]);
//				_legendLocation = (_params.length==23 &"".equals(_params[22])) ? "bottom" : _params[22];
//				_dataLabelPosition = "".equals(_params[13]) ? "inside" : _params[13];
//				_DubplicateAllow = "".equals(_params[15]) ? false : Boolean.valueOf(_params[15]); 
//				_yFieldFillColor = null;
//				if(!"".equals(_params[16]))
//				{			
//					String[] _staYFC = _params[16].split("~");
//					_yFieldFillColor = new int[_staYFC.length];
//		            for(int i=0; i< _staYFC.length; i++)
//		            {
//		            	_yFieldFillColor[i] = Integer.decode(_staYFC[i]);
//		            }
//				}
//				
//				if(_params.length == 21)
//				{
//					_seriexCloseField = "".equals(_params[17]) ? null : _params[17];
//					_seriesHighField = "".equals(_params[18]) ? null : _params[18];
//					_seriesLowField = "".equals(_params[19]) ? null : _params[19];
//					_seriesOpenField = "".equals(_params[20]) ? null : _params[20];
//				}		
//				if( _params.length == 27 ){					
//					_rangeMax			="".equals(_params[26]) ? 0 : common.ParseFloatNullChk(_params[26],0);					
//				}
//				if(_params.length == 32){
//					
//				}
//				
//			}			
//			_yFieldNames.put(dispIndex, _yFieldName);
//			_yFieldDisplayNames.put(dispIndex, _yFieldDisplayName);
//			_yFieldFillColors.put(dispIndex, _yFieldFillColor);
//		}
		
		//ArrayList<HashMap<String, Object>> _dataAC = GetChartData(PROJECT_NAME, FOLDER_NAME, FILE_NAME, DATA_ID);
		
		if("combcolumn".equals(IMG_TYPE))
		{
			chart = new CombinedColumnChart(_chartWidth, _chartHeight, MODEL_TYPE);

			_isMakeSuccess = chart.setGraphData(_dataACL, displayNamesMap);

//			_isMakeSuccess = chart.setGraphData(_dataACL, null, 
//					_seriesXField, _yFieldNames, _yFieldDisplayNames, _crossTab,
//					_form, _gridLIne, _gridLineWeight, _gridLIneDirection, _gridLIneColor, 
//					_legendDirection, _legendLabelPlacement, _legendMarkHeight, _legendMarkWeight, 
//					_legendLocation, _dataLabelPosition, _DubplicateAllow, _yFieldFillColors, true , _rangeMax);
		}
		
		if( _isMakeSuccess == false )
		{
			log.error("ViewerInfo5::getLocalChartImage()...Chart image릉 얻는데 실패하였습니다.");
			return "";
		}
		else
		{
			ba64Data = chart.getBase64String();
			//ba64Data = chart.getSvgData();
		}
		
		return ba64Data;
	}
	
	
	
	public static ArrayList<HashMap<String, Object>> GetChartData(String projName, String formName, String fileName, String dataID) throws Exception {
		ArrayList<HashMap<String, Object>> outList = null;
		
		String file_path = "";
		
		ObjectInputStream ois = null;

		log.info("Call GetChartData()...dataID=" + dataID);
		try {
			outList = new ArrayList<HashMap<String, Object>>();
			
			file_path = Log.ufilePath + "UFile/project/" + projName + "/" + formName + "/" + fileName;
			log.debug("common::GetChartData" + "::" +  "filePath >>>>> " + file_path);
			
			ois = new ObjectInputStream(new FileInputStream(new File(file_path)));
			HashMap hmDataSet = (HashMap) ois.readObject();
			outList = (ArrayList<HashMap<String, Object>>) hmDataSet.get(dataID);	
		}
		catch(Exception e) {
			e.printStackTrace();	
		} finally {
			if(ois != null) ois.close();
		}
		
		return outList;
	}
	
	
	// 로컬에 있는 서블릿 메소드를 호출하여 이미지 데이터를 받아온다.
	public static Image getLocalImageFile(String _imageUrl, String _sessionID)
	{
		Image _image = null;
		byte[] bAr = null;
		
		if(_imageUrl == null || "".equals(_imageUrl))
			return null;
		
		if( _imageUrl.startsWith("http") ){

			HashMap httpparams = getUrlVars(_imageUrl);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String METHOD_NAME = "";
				
			String IMG_TYPE = "";
			String FILE_NAME = "";
			String PROJECT_NAME = "";
			String FOLDER_NAME = "";
			String DATA_ID = "";
			String PARAM = "";	// , 문자로 구분된 차트생성을 위한 파라미터들(배열형태의 값은 ~로 구분하여 연결함)
			String MODEL_TYPE = "";	// AreaChart, LineChart, Barcode model type
			String SHOW_LABEL = "";
			String FILE_CONTENT = "";
			
			try
			{
				METHOD_NAME = (String) httpparams.get("METHOD_NAME");  
				 
			    //ViewerInfo5 vi5 = new ViewerInfo5();
			    if("getChartImage".equals(METHOD_NAME))
			    {
			    	IMG_TYPE = (String) httpparams.get("IMG_TYPE");
					FILE_NAME = (String) httpparams.get("FILE_NAME");
					PROJECT_NAME = (String) httpparams.get("PROJECT_NAME");
					FOLDER_NAME = (String) httpparams.get("FORM_ID");
					DATA_ID = (String) httpparams.get("DATASET");
					PARAM = (String) httpparams.get("PARAM");
					MODEL_TYPE = (String) httpparams.get("MODEL_TYPE");
					
			    	//vi5.getLocalChartImage(baos, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, DATA_ID, PARAM, MODEL_TYPE);
			    	getLocalChartImage(baos, IMG_TYPE, FILE_NAME, PROJECT_NAME, FOLDER_NAME, DATA_ID, PARAM, MODEL_TYPE);
			    	
			    	 bAr = baos.toByteArray();
//			        _image = Image.getInstance(bAr);
			    }
			    else if("getBarcodeImage".equals(METHOD_NAME))
			    {
			    	SHOW_LABEL = (String) httpparams.get("SHOW_LABEL");
			    	IMG_TYPE = (String) httpparams.get("IMG_TYPE");
			    	MODEL_TYPE = (String) httpparams.get("MODEL_TYPE");
			    	FILE_CONTENT = (String) httpparams.get("FILE_CONTENT");
			    	
			     	//vi5.getLocalBarcodeImage(baos, SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
			     	getLocalBarcodeImage(baos, SHOW_LABEL, IMG_TYPE, MODEL_TYPE, FILE_CONTENT);
			     	
			     	 bAr = baos.toByteArray();
//			        _image = Image.getInstance(bAr);
			    }
			    else
			    {
//			    	_image = getRemoteImageFile(_imageUrl);
//			    	bAr = getBytesRemoteImageFile(_imageUrl); 
			    	bAr = getBytesRemoteImageFileSession(_imageUrl, _sessionID); 
//			    	_image = Image.getInstance(bAr);
			    	
			    	log.debug("SESSION_ID : " + _sessionID);
			    }
			    
			    if( bAr != null ) _image = Image.getInstance(bAr);
			    
			}
			catch(Exception e)
			{
				e.printStackTrace();
			} 
			finally
			{
				try {
					baos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
    	}
		else
		{
			try {
				bAr = Base64Coder.decode( URLDecoder.decode(_imageUrl, "UTF-8").replace(" ", "+") );
			}catch (IllegalArgumentException e) {
				try {
					bAr = org.apache.commons.codec.binary.Base64.decodeBase64(URLDecoder.decode(_imageUrl, "UTF-8").getBytes());
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (Exception e) {
				
			}
			
		    try {
		    	
		    	if( bAr != null ) _image = Image.getInstance(bAr);
		    } catch (Exception e) {
				e.printStackTrace();
			}
    	}
        
        return _image;
	}
	
	
	// 원격지에 있는 HTTP 서비스를 호출하여 이미지 데이터를 받아온다.
	public static byte[] getBytesRemoteImageFileSession(String _imageUrl, String _sessionID)
	{
		if("".equals(_imageUrl) ) return null;
		
		if( _imageUrl.indexOf("ubiform.do?") != -1)
		{
			_imageUrl = _imageUrl.substring( _imageUrl.indexOf( "IMG_URL=" )+ 8, _imageUrl.length() );
		}
		
		String _httpCheck=_imageUrl;
		
		if( _imageUrl != null && _imageUrl.length() > 4)
		{
			_httpCheck = _imageUrl.substring(0, 4);
		}
		
		if( _httpCheck.equalsIgnoreCase("http") == false ){
			_imageUrl = Log.dataSetURL+_imageUrl;
		}
		
		try {
			_imageUrl = URLDecoder.decode(_imageUrl, "UTF-8");
			_imageUrl = _imageUrl.replaceAll(" ", "%20");
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		
		// decode된 Image의 URL에 [,] 문자가 있을경우 uri파싱 오류가 발생하여 해당 문자를 변경하여 호출하도록 처리
		if( _imageUrl.indexOf("[")  > -1 ||  _imageUrl.indexOf("]")  > -1 ){
			
			_imageUrl = _imageUrl.replace("[", "%5B" );
			_imageUrl = _imageUrl.replace("]", "%5D" );
		}
		
		boolean bSSL = _imageUrl.startsWith("https://");
		byte[] bAr = null;
		HttpGet httpget = new HttpGet(_imageUrl);		
//		HttpClient httpclient = new DefaultHttpClient();
//		DefaultHttpClient httpclient = new DefaultHttpClient();
			
		HttpClient httpclient  = null;
		
	    BasicCookieStore cookieStore = new BasicCookieStore();
	    BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", _sessionID);
	    InetAddress _inet;
		try {
			_inet = InetAddress.getLocalHost();
			cookie.setDomain( _inet.getHostAddress() );
			cookie.setPath("/");
			cookieStore.addCookie(cookie);
			
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		HttpClientBuilder _httpBuilder = HttpClientBuilder.create();
		_httpBuilder.setDefaultCookieStore(cookieStore);
		
//	    HttpClient httpclient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

		
		// SSL 요청을 위한 TrustManager
		TrustManager easyTrustManager = new X509TrustManager() {

            public X509Certificate[] getAcceptedIssuers() {
                // no-op
                return null;
            }

            public void checkServerTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            public void checkClientTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }
        };
				
		try
		{
			if(bSSL)
			{
			  SSLContext sslcontext = SSLContext.getInstance("TLS");
			        sslcontext
			                .init(null, new TrustManager[] { easyTrustManager }, null);

//			        SSLSocketFactory socketFactory = new SSLSocketFactory(sslcontext,
//			              SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//			        
//			        String _sslPortStr = getPropertyValue("ssl.port");
//			        int _sslPort = Integer.valueOf( (_sslPortStr=="")?"443":_sslPortStr );
//			        
//			        Scheme sch = new Scheme("https", _sslPort, (SchemeSocketFactory) socketFactory);
//			        httpclient.getConnectionManager().getSchemeRegistry().register(sch);	
			        
		        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		        _httpBuilder.setSSLSocketFactory(sslConnectionFactory);
			     
		        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
		                .register("https", sslConnectionFactory)
		                .build();

		        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);

		        _httpBuilder.setConnectionManager(ccm);
		        
			        
			}
			
			String _userAgent = "UBFORM";
			_httpBuilder.setUserAgent(_userAgent);

			httpclient = _httpBuilder.build();
			log.debug( " httpget.getURI() " + httpget.getURI() );
		    HttpResponse response = httpclient.execute(httpget); 
	        HttpEntity entity = response.getEntity();
	        //m_reqManager.getServiceManager().getHttpRequest().getSession().getId()
	        //String responseBody = EntityUtils.toString(entity, "UTF-8");
            //System.out.println("https request response body================>[" + responseBody + "]");
	        
	        if (entity != null) {
	            InputStream inputStream = entity.getContent();
	
	            BufferedImage bimg = ImageIO.read(inputStream);
	            if(bimg != null)
	            {
	            	String _imageType = "jpg";
	            	if( bimg.getColorModel().hasAlpha() ){
	            		_imageType = "png";
	            	}
	            	
	            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            	ImageIO.write( bimg, _imageType, baos );
	            	bAr = baos.toByteArray();
	            	baos.close();
	            }
	        }	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} 
		finally
		{
			 httpget.abort();
	         httpclient.getConnectionManager().shutdown();
	         httpclient = null;
	         _httpBuilder = null;
		}
        
        return bAr;
	}
	
	
	  
	  
    public static byte[] readBytesFromFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;
    }
    
    public static ArrayList<String> loadFontList( String _loc ) throws FontFormatException, IOException
    {
    	String _path = Log.ufilePath + "UFile/sys/fonts/";
    	ArrayList<String> _resultList = new ArrayList<String>();
    	
		File dirFile=new File(_path);
		
		File []fileList=dirFile.listFiles();
		
		String _fontLocale = getPropertyValue("ubiform.editorFontLocale");
		String[] _fontLocaleList = (_fontLocale != null )?_fontLocale.split(","):null;
		
		// 실제 폰트명을 담아둘 객체 
		GlobalVariableData.M_FONTNAME_LIST = new ArrayList<String>();
		// local별로 fontFamily를 담아둘 객체 
		GlobalVariableData.M_FONTFAMILY_LIST = new HashMap<String, ArrayList<String>>();
	    String _fontFamilyName = "";
		
		for(File tempFile : fileList) {
		  if(tempFile.isFile()) {
		    String tempPath=tempFile.getParent();
		    String tempFileName=tempFile.getName();
		    
		    
		    InputStream in = new FileInputStream(tempFile);
		    Font _font = Font.createFont(Font.TRUETYPE_FONT, in);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(_font);
		    
		    // local값이 없을경우 
		    if(_loc.equals(""))
		    {
		    	
			    GlobalVariableData.M_FONTNAME_LIST.add(tempFileName);

			    ArrayList<String> _localeAr;
			    
			    if(_fontLocaleList != null && _fontLocaleList.length > 0 )
			    {
			    	for(String _locale : _fontLocaleList) {
			    		
			    		if( GlobalVariableData.M_FONTFAMILY_LIST.containsKey(_locale) == false )
			    		{
			    			_localeAr = new ArrayList<String>();
			    			GlobalVariableData.M_FONTFAMILY_LIST.put(_locale,_localeAr );
			    		}
			    		else
			    		{
			    			_localeAr = GlobalVariableData.M_FONTFAMILY_LIST.get(_locale);
			    		}
			    		_fontFamilyName = _font.getFamily( Locale.forLanguageTag(_locale)  );
			    		if( _localeAr.indexOf(_fontFamilyName) == -1 ) _localeAr.add( _fontFamilyName );
			    		
			    	}
			    }
		    }
		    else
		    {
		    	_fontFamilyName = _font.getFamily( Locale.forLanguageTag(_loc)  );
	    		if( _resultList.indexOf(_fontFamilyName) == -1 ) _resultList.add( _fontFamilyName );
		    }
		    
		  }
		}
		
		
		if(_loc.equals(""))
		{
			// 신촌 세브란스 병원의 경우 Flash 버전을 사용. itext.jar파일이 존재 하지 않아 기동시 오류가 발생함 class가 없을경우 처리 하지 않도록 지정해둠. 2016-05-24 
			try {
				Class.forName("com.lowagie.text.FontFactory"); 
				FontFactory.registerDirectory(_path);
			} catch (Exception e) {
				log.debug("com.lowagie.text.FontFactory not USED");
			} 
		}
		
		return _resultList;
    }
    
    public static ArrayList<File> getFontFiles( String _fontName, String _locale ) throws FontFormatException, IOException
    {
    	String _path = Log.ufilePath + "UFile/sys/fonts/";
    	ArrayList<File> _resultList = new ArrayList<File>();
    	
		File dirFile=new File(_path);
		
		File []fileList=dirFile.listFiles();
		String _fontFamilyName = "";
		
		for(File tempFile : fileList) {
		  if(tempFile.isFile()) {
		    String tempPath=tempFile.getParent();
		    String tempFileName=tempFile.getName();
		    
		    InputStream in = new FileInputStream(tempFile);
		    Font _font = Font.createFont(Font.TRUETYPE_FONT, in);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(_font);
		    
		    _fontFamilyName = _font.getFamily( Locale.forLanguageTag(_locale)  );
		    
		    if(_fontFamilyName.equals(_fontName))
		    {
		    	_resultList.add(tempFile);
		    }
		    
		  }
		}
		
		return _resultList;
    }
    
    
    public static int ParseIntNullChk(String _value, int _baseInt )
    {
    	int _retInt = _baseInt;
    	
    	try {
    		_retInt = Integer.parseInt( _value);
		} catch (Exception e) {
			
		}
    	
    	
    	return _retInt;
    }
    

    public static Float ParseFloatNullChk(String _value, int _baseInt )
    {
    	float _retInt = _baseInt;
    	
    	try {
    		_retInt = Float.parseFloat( _value);
    	} catch (Exception e) {
    		
    	}
    	
    	return _retInt;
    }
    
	public static String readJsonReader( FileInputStream fis,  String encoding ) throws IOException, DataFormatException 
	{
	    int intValueOfChar;
	    String targetString = "";
	    
	    targetString = getFileContent(fis, encoding);
	    
		if( targetString.substring(0, 1).hashCode() == 65279)
		{
			targetString = targetString.substring(1, targetString.length());
		}
		
		targetString = common.base64_decode_uncompress(targetString, "UTF-8"); 

	    return targetString;
	}
	
	public static String getFileContent(
			   FileInputStream fis,
			   String          encoding ) throws IOException
	 {
		StringBuilder sb = new StringBuilder();
		
	    BufferedReader br = new BufferedReader( new InputStreamReader(fis, encoding ));
	   
	      String line;
	      while(( line = br.readLine()) != null ) {
	         sb.append( line );
	         sb.append( '\n' );
	      }
	      return sb.toString();
	}
	
	public static String getUBFormXmlData( String PROJECT_NAME, String FOLDER_NAME, String FILE_NAME, xmlToUbForm _xmlUbForm, String _multiFormInfo, HashMap<String, Object> _param, String _openerLocation, String _client_ssid, String _multiFormType, String _projectId ) throws IOException, DataFormatException, SAXException, ParserConfigurationException
	{
		String XML_DATA = "";
		
		if( _multiFormInfo != null && "".equals(_multiFormInfo) == false )
		{
			_multiFormInfo = _multiFormInfo.replaceAll("&#039;", "\\\\\"").replaceAll("&quot;", "'").replaceAll("&#047;", "/");;
			
			if( _multiFormType != null && _multiFormType.equals("S") )
			{
				xmlLoadParser _xmlLoadParser = new xmlLoadParser();
				XML_DATA = _xmlLoadParser.mergeMultiFormFile(_xmlUbForm, _param, _multiFormInfo, FILE_NAME, _openerLocation, _client_ssid, _projectId);
			}
			else
			{
				try {
					/** 여러개의 Form 을 읽어들이도록 지정하는 기능*/
					ArrayList<String> _doc = common.getUBFXmlDatas(_multiFormInfo,FILE_NAME,_xmlUbForm, _param, _openerLocation, _client_ssid, _projectId);
					
					if(_doc == null ) return null;
					
					_xmlUbForm.setDocument(_doc);
				} catch (Exception e) {
					// TODO: handle exception
					log.info("getUBFormXmlData || " + Log.MSG_NOT_EXISTS_FORM_INFO_FILE  );
					return null;
				}
			}
		}
		else
		{
			XML_DATA = common.getUBFXmlData(PROJECT_NAME,FOLDER_NAME,FILE_NAME, _xmlUbForm, _openerLocation,"","", _client_ssid, _projectId);
		}
		
		return XML_DATA;
	}
	
	
	public static ArrayList<String> getUBFXmlDatas(  String _multiFormInfo, String FILE_NAME, xmlToUbForm _xmlUbForm, HashMap<String, Object> _params, String _openerLocation, String _client_ssid, String _projectId ) throws IOException, DataFormatException, SAXException, ParserConfigurationException, ParseException
	{
		String TMP_FILE_PATH = "";
		String XML_DATA = "";
		String filePath = "";
		String fileContents = "";
		String PARAMS = "";
		int PAGE_NUM = 0;
		File sFile = null;
		
		ArrayList<HashMap<String, String>> formNames = new ArrayList<HashMap<String, String>>();
		ArrayList<JSONObject> _docParams = new ArrayList<JSONObject>();
		
		JSONObject _defParams = null;				// 사용자입력 파라미터 값
		
		if( _params.containsKey("PARAMS") && "".equals( _params.get("PARAMS") ) == false )
		{
			_defParams = (JSONObject) JSONValue.parseWithException( _params.get("PARAMS").toString() );
		}
		
		// URIEncode 되어있을경우 처리
		_multiFormInfo = _multiFormInfo.replaceAll("\\\\\"", "\"");
		_multiFormInfo = URLDecoder.decode(_multiFormInfo, "UTF-8");
		// _jsonArr : [ { projectName:'', formName:'', parameter:{} } ]
		JSONArray _jsonArr = (JSONArray) JSONValue.parseWithException(_multiFormInfo);
		int _formLenght = _jsonArr.size();
		
		for (int i = 0; i < _formLenght; i++) {
			
			HashMap<String, Object> _formInfo = (HashMap<String, Object> ) _jsonArr.get(i);
			
			HashMap<String, String> _tempMap = new HashMap<String, String>();
			_tempMap.put("PROJECT_NAME", _formInfo.get("projectName").toString() );
			_tempMap.put("FORM_NAME", _formInfo.get("formName").toString() );
			
			JSONObject _paramObj = new JSONObject();
			
			// 사용자에게 입력받은 파라미터를 이용하기 위해 파라미터로 전달받은 파라미터로 준비
			if( _defParams != null ) _paramObj = (JSONObject) _defParams.clone();
			
			if( _formInfo.containsKey("parameter") )
			{
				JSONObject _param = (JSONObject) _formInfo.get("parameter");
				
				//{parameter:"",type:"string"};
				for (Object key : _param.keySet()) {
					String keyStr = (String)key;
					Object keyvalue = _param.get(keyStr);
					JSONObject _paramData = new JSONObject();
					_paramData.put("parameter", keyvalue);
					_paramData.put("type", "string");
					_paramObj.put(keyStr, _paramData);
				}
				
			}
			 
			_docParams.add( _paramObj );
			formNames.add(_tempMap);
		}
		
//		ArrayList<Document> _retDocAr = new ArrayList<Document>();
		ArrayList<String> _retDocAr = new ArrayList<String>();
		ArrayList<HashMap<String, String>> _retMultiProjectList = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> _reuMultiProjectInfo = new HashMap<String, String>();
		Document _result = null;
		Element _pageEl = null;
		
		String _PROJECT_NAME = ""; 
		String _FOLDER_NAME = "";
		Element _argoPage;
		int _cnt = formNames.size();
		
		String _realPath = common.getPropertyValue("ubform.realDir");
		String _realPathStr = "";
		String _tempXmlData = "";
		
		if( _realPath != null && _realPath.equals("") == false && _openerLocation != null &&  _openerLocation.indexOf("/UEditor") == -1 )
		{
			_realPathStr = _realPath;
		}
				
		for (int i = 0; i < _cnt; i++) {
			
			_PROJECT_NAME = formNames.get(i).get("PROJECT_NAME");
			_FOLDER_NAME = formNames.get(i).get("FORM_NAME");
			
			sFile = common.getProjectFilePath(_PROJECT_NAME, _FOLDER_NAME, FILE_NAME,  _realPathStr,"","", _openerLocation, _client_ssid, _projectId );
			
			if(sFile.isFile()){
				fileContents = file_get_contents(sFile);
			}
			else {
				return null;
			}
			
			_xmlUbForm.mPageSize = (float) Math.round( Float.valueOf(sFile.length())/Float.valueOf(1024)*100)/100;	// 파일 size값을 넘기기
			
			_tempXmlData = base64_decode_uncompress(fileContents, "UTF-8"); 
			
			if( i == 0 )
			{
				HashMap<String, Object> _projectInfo = getProjectInfo(_tempXmlData, _PROJECT_NAME, _FOLDER_NAME );
				_xmlUbForm.setProjectInfo(_projectInfo);
			}
			
			if( _retDocAr.indexOf(_tempXmlData) != -1 )
			{
				XML_DATA = _retDocAr.get(_retDocAr.indexOf(_tempXmlData));
			}
			else
			{
				XML_DATA = _tempXmlData;
				_retDocAr.add( XML_DATA );
			}
			
			// page 태그에 project 합쳐서 xml 만들기
			/** 
			InputSource _is = new InputSource(new StringReader(XML_DATA));
			Document _document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_is);
			
			NodeList _pjList = _document.getElementsByTagName("project");
			Element _pj = (Element) _pjList.item(0);
			_pj.setAttribute("PROEJCT", _PROJECT_NAME);
			_pj.setAttribute("FORMID", _FOLDER_NAME);
//			_retDocAr.add( _document );
			_document =  null;
			*/
			
			_reuMultiProjectInfo = new HashMap<String, String>();
			_reuMultiProjectInfo.put("PROEJCT", _PROJECT_NAME);
			_reuMultiProjectInfo.put("FORMID", _FOLDER_NAME);
			_reuMultiProjectInfo.put("IDX", String.valueOf( _retDocAr.indexOf(XML_DATA) ) );
			_retMultiProjectList.add(_reuMultiProjectInfo);
			
			System.gc();
			
		}
		
		_xmlUbForm.setDocumentParams(_docParams);
		_xmlUbForm.setDocumentInfos(_retMultiProjectList);
		
		return _retDocAr;
	}
	
	  public static HashMap<String, Object> getProjectInfo(String _xmlData, String _projectName, String _formName )
	  {
		  	HashMap<String, Object> _projectInfo = new HashMap<String, Object>();
		  	String _description = "";
		  	HashMap<String, String> _waterMarkInfo = new HashMap<String, String>();
		  	
		  	try {
		  		SAXBuilder oBuilder = new SAXBuilder();
		  		org.jdom2.Document oDoc = null;
		  		oDoc = oBuilder.build( new StringInputStream(_xmlData,"UTF-8") );
		  		
		  		org.jdom2.Element _projectEl = oDoc.getRootElement();
		  		_description = _projectEl.getAttribute("desc").getValue();
		  		
		  		org.jdom2.Element _waterMark = _projectEl.getChild("watermark");
		  		
		  		if( _waterMark != null && _waterMark.getChildren("property") != null && _waterMark.getChildren("property").size() > 0 )
		  		{
		  			List<org.jdom2.Element> _waterProperties = _waterMark.getChildren("property");
		  			int _proSize = _waterProperties.size();
		  			
		  			for (int i = 0; i < _proSize; i++) {
		  				org.jdom2.Element _properties = _waterProperties.get(i);
						_waterMarkInfo.put(_properties.getAttributeValue("name"), _properties.getAttributeValue("value"));
					}
		  			
		  		}
		  		
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			//desc 가져오기
			_projectInfo.put("DESC", _description);
			_projectInfo.put("PROJECT_NAME", _projectName);
			_projectInfo.put("FORM_NAME", _formName);
			_projectInfo.put("WATER_MARK", _waterMarkInfo);
//			
//			List<Attribute> _list =  oDoc.getRootElement().getAttributes();
//			int _size = _list.size();
//			
//			for (int i = 0; i < _size; i++) {
//				
//				_projectInfo.put(_list.get(i).getName(), _list.get(i).getValue());
//				
//			}
			
		  return _projectInfo;
	  }
	  
		public static String getUBFXmlData( String PROJECT_NAME, String FOLDER_NAME, String FILE_NAME, xmlToUbForm _xmlUbForm, String _openerLocation ,String _loadTarget, String _userName, String _client_ssid, String _projectId ) throws IOException, DataFormatException
		{
			String TMP_FILE_PATH = "";
			String XML_DATA = "";
			String filePath = "";
			String fileContents = "";
			String PARAMS = "";
			int PAGE_NUM = 0;
			File sFile = null;
			
			PROJECT_NAME = PROJECT_NAME != null ? URLDecoder.decode(PROJECT_NAME,"UTF-8") : "";
			FOLDER_NAME = FOLDER_NAME != null ? URLDecoder.decode(FOLDER_NAME,"UTF-8") : "";
			FILE_NAME = FILE_NAME != null ? URLDecoder.decode(FILE_NAME,"UTF-8") : "";
			
			String _realPath = common.getPropertyValue("ubform.realDir");
			String _realPathStr = "";
			if( _realPath != null && _realPath.equals("") == false && _openerLocation != null &&  _openerLocation.indexOf("/UEditor") == -1 )
			{
				_realPathStr = _realPath;
			}
			
			sFile = getProjectFilePath(PROJECT_NAME, FOLDER_NAME, FILE_NAME, _realPathStr ,_loadTarget, _userName, _openerLocation, _client_ssid, _projectId );
			
			if(sFile.isFile()){
				fileContents = file_get_contents(sFile);
			}
			else {
				return null;
			}
			
			if(_xmlUbForm != null) _xmlUbForm.mPageSize = (float) Math.round( Float.valueOf(sFile.length())/Float.valueOf(1024)*100)/100;	// 파일 size값을 넘기기
			
			XML_DATA = base64_decode_uncompress(fileContents, "UTF-8"); 
			
			// xml_data의 attribute를 추출하여 formid/projectname/desc를 담아둔다. 
			
			if(_xmlUbForm != null)
			{
				HashMap<String, Object> _projectInfo = getProjectInfo(XML_DATA, PROJECT_NAME, FOLDER_NAME );
				_xmlUbForm.setProjectInfo(_projectInfo);
			}
			
			return XML_DATA;
		}
		
		public static File getProjectFilePath(String _projectName, String _forlderName, String _fileName, String _realPath ,String _loadTarget, String _userName, String _openerLocation, String _client_ssid, String _projectId )
		{
			String TMP_FILE_PATH = "";
			
//			String _formLocationPath = common.getPropertyValue("ubform.formLocationPath");
			String _formLocationPath = getFormLocationPath(_projectName, _forlderName);
			
			String _projectPath = "";

			if( ( _openerLocation == null ||  _openerLocation.indexOf("/UEditor") == -1 ) && _formLocationPath.equals("") == false )
			{
//				_formLocationPath = _formLocationPath + _projectName + "/report/";
				// 프로젝트명  문서명 Mview.ubx 형태의 파일명을 만들기
				TMP_FILE_PATH = _formLocationPath + _forlderName + ((_fileName.equals("form.json")||_fileName.equals("simpleForm.json"))?".json":".ubx");
			}
			else
			{
				if( _loadTarget != null && _loadTarget.equals("user") ){
					
					_projectPath = getExternalProjectPath(_projectId);
					if(_projectPath.equals(""))	_projectPath = Log.ufilePath + "UFile/personalProject/";
					else _projectPath = _projectPath + "personalProject/";
					
					TMP_FILE_PATH = _projectPath + _userName + "/" + _projectName + "/" + _forlderName;
					
				}else{
					
					if( _realPath != null && _realPath.equals("") == false )
					{
						TMP_FILE_PATH = _realPath + _projectName + "/" + _forlderName;
					}
					else
					{
						if(_projectName.indexOf("/sample/") >= 0)
						{
							TMP_FILE_PATH = Log.ufilePath + "UFile/sys" + _projectName;
						}
						else
						{
							_projectPath = getProjectPath(_projectId, GlobalVariableData.TYPE_PROJECT, "") + "/";
							//_projectPath = Log.ufilePath + "UFile/project/";
							
							TMP_FILE_PATH = _projectPath + _projectName + "/" + _forlderName;
						}
					}
				}
				
				if(_fileName.lastIndexOf(".ubs5") > 0)
					//TMP_FILE_PATH = TMP_FILE_PATH + "/save5/" + _fileName;
					TMP_FILE_PATH = TMP_FILE_PATH + "/Mview.ubx";
				else if(_fileName.lastIndexOf(".ubs") > 0)
					TMP_FILE_PATH = TMP_FILE_PATH + "/" + _fileName;
				else if("".equals(_fileName) == false && _fileName.contains(".") == false )
					TMP_FILE_PATH = TMP_FILE_PATH + "/" + _fileName+".ubx";
				else if( "".equals(_fileName) == false )
					TMP_FILE_PATH = TMP_FILE_PATH + "/" + _fileName;
				else 
					TMP_FILE_PATH = TMP_FILE_PATH + "/Mview.ubx";
				
			}
			
			
			File sFile = new File(TMP_FILE_PATH);
			
			if( _realPath != null && _realPath.equals("") == false && !sFile.isFile() )
			{
				sFile = common.getProjectFilePath(_projectName, _forlderName, _fileName, "","","", _openerLocation, _client_ssid, _projectId );
			}
			
			log.info("[" + _client_ssid + "^" + _projectName + "/" + _forlderName + "] UBX FILE PATH	:	" + TMP_FILE_PATH );
			
			return sFile;
		}
		
		  public static String getFormLocationPath( String _projectName, String _forlderName )
		  {
			 
			  String _formLocationPath = common.getPropertyValue("ubform.formLocationPath");
			  String _resultFileName = "";
			  //  {PROJECT_NAME} , {FORM_NAME}으로 지정되도록 변경 
			  
			  if( _formLocationPath != null && _formLocationPath.equals("") == false )
			  {
				 _formLocationPath = _formLocationPath.replaceAll("\"", "'");
				 String[] _exportFileTypes =  _formLocationPath.split("[+]");
				 
				 int _size = _exportFileTypes.length;
				 
				 for (int i = 0; i < _size; i++) {
					
					 String _temp = _exportFileTypes[i].trim();
					 String _name = "";
					 
					 if( _temp.contains("{") )
					 {
					
						 _name = _temp.replaceAll("[{]|[}]", "");
						 if( _name.equals("PROJECT_NAME") )
						 {
							 _temp = _projectName;
						 }
						 else if( _name.equals("FORM_NAME") )
						 {
							 _temp = _forlderName;
						 }

					 }
					 else if( _temp.contains("'") )
					 {
						 _temp = _temp.replaceAll("'", "");
					 }
					 
					 _resultFileName = _resultFileName + _temp;
				 }
					 
			  }
			  
			  
			  return _resultFileName;
		  }
		  
		  
		    
		    /**
			 * 
			 * */
			private static float convertDpiFloat( Object value)
			{
				return (Math.round(((ValueConverter.getFloat(value) / 96f ) * 72f ) * 10f))/10f;
			}
			
		    public static String setPdfImageType(String _filePath, String _imgType, float _dpi) throws InvalidPasswordException, IOException, DocumentException
		    {
		    	String pdfImgPath = _filePath.substring(0, _filePath.lastIndexOf(".")) + "_img.pdf";
		    	com.lowagie.text.Document mDocument;
		    	PdfWriter mWriter;
		    	FileOutputStream mBaos = new FileOutputStream(pdfImgPath);
		    		    	
		    	File file = new File(_filePath);
				PDDocument document = PDDocument.load(file);
	            org.apache.pdfbox.rendering.PDFRenderer pdfRenderer = new org.apache.pdfbox.rendering.PDFRenderer(document);
				
	            PDPage page0 = document.getPage(0);
	            
	            //float _pWidth = convertDpiFloat(page0.getBBox().getWidth());
	            //float _pHeight = convertDpiFloat(page0.getBBox().getHeight());
	            float _pWidth = page0.getBBox().getWidth();
	            float _pHeight = page0.getBBox().getHeight();
	            
	            Rectangle _pageSize = new Rectangle(_pWidth, _pHeight);
				_pageSize.setBackgroundColor(Color.WHITE);
				mDocument = new com.lowagie.text.Document(_pageSize, 0, 0, 0, 0);
				mWriter = PdfWriter.getInstance(mDocument, mBaos);
		    	
		    	mDocument.open();
	            
	            int pageNumber = 1;
	            for (PDPage page : document.getPages()) {
	            	BufferedImage bim = pdfRenderer.renderImageWithDPI(pageNumber-1, _dpi, ImageType.RGB);
	            	
	            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            	ImageIO.write( bim, _imgType, baos );
	            	baos.flush();
	            	
	            	byte[] imageInByte = baos.toByteArray();
	            	baos.close();
	            	
	            	//_pWidth = convertDpiFloat(page.getBBox().getWidth());
	            	//_pHeight = convertDpiFloat(page.getBBox().getHeight());
	            	_pWidth = page.getBBox().getWidth();
	            	_pHeight = page.getBBox().getHeight();
	            	
	            	System.out.println("setPdfImageType::~~~~~~~~~~~~~~pWidht=" + _pWidth + ", pHeight=" + _pHeight  + ", imageInByte.size=" + imageInByte.length);
	            	
	            	CreatePdfImage(mWriter, _pWidth, _pHeight, imageInByte);
	            	
	            	Rectangle _newPSize = new Rectangle(_pWidth, _pHeight);
	    			
	    			mDocument.setPageSize(_newPSize);
	    			mDocument.setMargins(0, 0, 0, 0);
	    			mDocument.newPage();
	    			
	            	pageNumber++;
	            }
	            
	            //Closing the document
				document.close();
				
				mDocument.close();
				mBaos.close();
				
				file.delete();
				
				return pdfImgPath;
		    }
		   	
			private static void CreatePdfImage( PdfWriter pWriter, float imgWidth, float imgHeight, byte[] imgData ) throws MalformedURLException, IOException, DocumentException
			{
				float _x = 0;
				float _y = 0;
				float _w = imgWidth;
				float _h = imgHeight;
				float _itemAlpha =  1;
				String _imageUrl = "";		
			
				PdfContentByte _contentByte = pWriter.getDirectContent();
				PdfGState _alpha = new PdfGState();
				_contentByte.saveState();
				
				Image _image = Image.getInstance(imgData);
		   		if(_image != null)
		   		{
					_image.scalePercent(10, 10);
					_alpha.setFillOpacity(_itemAlpha);	
					_contentByte.setGState(_alpha);
					_contentByte.addImage(_image, _w, 0, 0, _h, _x, _y);
					_contentByte.restoreState();
		   		}
		   		else
		   		{
		   			_contentByte.restoreState();
		   		}
			}
		    
			
			public static String getUBFormXmlData( String PROJECT_NAME, String FOLDER_NAME, String FILE_NAME, xmlToUbForm _xmlUbForm, String _multiFormInfo, HashMap<String, Object> _param, String _openerLocation ,String _loadTarget, String _userName, String _client_ssid, String _multiFormType, String _projectId ) throws IOException, DataFormatException, SAXException, ParserConfigurationException
			{
				String XML_DATA = "";
				
				if( _multiFormInfo != null && "".equals(_multiFormInfo) == false )
				{
					_multiFormInfo = common.convertUBParamString(_multiFormInfo);
					
					if( _multiFormType != null && _multiFormType.equals("S") )
					{
						xmlLoadParser _xmlLoadParser = new xmlLoadParser();
						XML_DATA = _xmlLoadParser.mergeMultiFormFile(_xmlUbForm, _param, _multiFormInfo, FILE_NAME, _openerLocation, _client_ssid, _projectId);
					}
					else
					{
						try {
							/** 여러개의 Form 을 읽어들이도록 지정하는 기능*/
							ArrayList<String> _doc = common.getUBFXmlDatas(_multiFormInfo,FILE_NAME,_xmlUbForm, _param, _openerLocation, _client_ssid, _projectId );
							
							if(_doc == null ) return null;
							
							_xmlUbForm.setDocument(_doc);
						} catch (Exception e) {
							// TODO: handle exception
							log.info("getUBFormXmlData || " + Log.MSG_NOT_EXISTS_FORM_INFO_FILE  );
							return null;
						}
					}
				}
				else
				{
					XML_DATA = common.getUBFXmlData(PROJECT_NAME,FOLDER_NAME,FILE_NAME, _xmlUbForm, _openerLocation,_loadTarget, _userName, _client_ssid, _projectId );
				}
				
				return XML_DATA;
			}
			
		    
			public static String decodeHtmlSpecialChars( String src )
			{
//				pvalue = pvalue.replaceAll("&", "&#38;");
//				pvalue = pvalue.replaceAll("<", "&lt;").replaceAll(">", "&gt;");	// 보안취약점패치 (XSS)
//				pvalue = pvalue.replaceAll("\\.\\./", "").replaceAll("\\./", "").replaceAll("\\\\", "");
//				pvalue = pvalue.replaceAll("\\\"", "&#039;").replaceAll("'", "&quot;").replaceAll("/", "&#047;");
				
				return src.replaceAll("&#38;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&#039;", "\\\"").replaceAll("&quot;", "'").replaceAll("&#047;", "/");
			}
			
			/**
			 *	보안 취약성으로 뷰어에서 변환한 캐릭터를 
			 *	서버에서 다시 원복 처리  
			 **/
			public static String convertUBParamString( String _ubParams )
			{
				if( _ubParams == null ) return null;
				
				return _ubParams.replaceAll("&#039;", "\\\\\"").replaceAll("&quot;", "'").replaceAll("&#047;", "/").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&#38;", "&");
			}
			
			public static Point rotationPosition( float _x, float _y,int _rotation )
			{
				Point _point = new Point();
				HashMap<String, Float> _ret = new HashMap<String, Float>();
				double pi = (_rotation * Math.PI / 180);
				
				double _retX = 0;
				double _retY = 0;
				
				BigDecimal _cos = new BigDecimal(Math.cos(pi));
				BigDecimal _sin = new BigDecimal(Math.sin(pi));
				
				double _cosNum = _cos.setScale(3, BigDecimal.ROUND_DOWN).doubleValue();
				double _sinNum = _sin.setScale(3, BigDecimal.ROUND_DOWN).doubleValue();

				_retX = ( _x * _cosNum ) - ( _y * _sinNum );
				_retY = ( _x * _sinNum ) + ( _y * _cosNum );
				
				_point.x = Float.valueOf(Math.round(_retX)).intValue();
				_point.y = Float.valueOf(Math.round(_retY)).intValue();
				
				return _point;
			}
			
			
			public static String checkMemory(String _title)
			{
				long _preUseMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				long _totalMemory = Runtime.getRuntime().totalMemory();
				long _freeMemory = Runtime.getRuntime().freeMemory();

				return "CURRENT MEMORY " + _title + "	:	" + _preUseMemory + "	total memory : " + _totalMemory + "	FREE MEMORY : " +  _freeMemory;
			}
			
			
			public static String encodeURICompnent( String _value )
			{
				try {
					_value = URLEncoder.encode(_value, "UTF-8").replaceAll("\\+", "%20");
//					URLEncoder.encode(_value, "UTF-8").replaceAll("\\+", "%20").replaceAll("\\%21", "!").replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")").replaceAll("\\%7E", "~")
		            
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return _value;
			}
			
			
			public static String getExternalProjectPath( String _projectID )
			{
				if( Log.externalPathInfo == null || Log.externalPathInfo.isEmpty() || _projectID == null ) return "";
				
				String _proejctPath = Log.externalPathInfo.get(_projectID);
				
				if( _proejctPath == null )
				{
					_proejctPath = Log.externalPathInfo.get("DEFAULT");
				}
				
				if( _proejctPath == null ) _proejctPath = "";
				
				return _proejctPath;
			}
			
			
			/**
			 * 
			 * @param _projectID	:  프로젝트 키값 ( 여러개의 프로젝트리스트중 어떤 프로젝트를 사용할지 키값 )
			 * @param _type			:	프로젝트 폴더/데이터셋 폴더 여부 
			 * @param _defaultPath	:	프로젝트 값이없을때 리턴할 경로
			 * @return
			 */
			public static String getProjectPath( String _projectCode, String _type,  String _defaultPath )
			{
				String _projectPath = "";
				
//				if( Log.externalPathInfo == null || Log.externalPathInfo.isEmpty() || _projectID == null || _projectID.equals("") )
				if( Log.externalPathInfo == null || Log.externalPathInfo.isEmpty() || _projectCode == null )
				{
					if( _defaultPath.equals(""))
					{
						if(_type.equals(GlobalVariableData.TYPE_PROJECT))
						{
							_projectPath =  Log.ufilePath + "UFile/project";
						}
						else
						{
							_projectPath =  Log.ufilePath + "UFile/sys/qDir/save/dataset";
						}
					}
					else
					{
						_projectPath = _defaultPath;
					}
				}
				else
				{
					if( _projectCode.equals("") )
					{
						_projectPath = Log.externalPathInfo.get("DEFAULT");
					}
					else
					{
						_projectPath = Log.externalPathInfo.get(_projectCode);
					}
					
					if( _projectPath != null && _projectPath.equals("") == false )
					{
						if(_type.equals(GlobalVariableData.TYPE_PROJECT))
						{
							_projectPath = _projectPath + GlobalVariableData.M_EXTERNAL_PROJECT_FOLDERNAME;
						}
						else
						{
							_projectPath = _projectPath + GlobalVariableData.M_EXTERNAL_DATASET_FOLDERNAME;
						}
					}
					else
					{
						_projectPath = "";
					}
					
				}
				
//				if( _projectPath.equals("") )
//				{
//					if( _defaultPath.equals(""))
//					{
//						if(_type.equals(GlobalVariableData.TYPE_PROJECT))
//						{
//							_projectPath =  Log.ufilePath + "UFile/project";
//						}
//						else
//						{
//							_projectPath =  Log.ufilePath + "UFile/sys/qDir/save/dataset";
//						}
//					}
//					else
//					{
//						_projectPath = _defaultPath;
//					}
//				}
				
				return _projectPath;
			}
}
