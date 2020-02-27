package org.ubstorm.service.utils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.ubstorm.service.logger.Log;

public class DownloadUtil {
	
	/** 다운로드 버퍼 크기 */
	  private static final int BUFFER_SIZE = 8192; // 8kb
	 
	  /** 문자 인코딩 */
	  private static final String CHARSET = "euc-kr";
	 
	  private DownloadUtil() {}
	 
	  /**
	   * 파일 다운로드
	   * 
	   * @param request
	   * @param response
	   * @param file
	   *            다운로드할 파일
	   * 
	   * @throws ServletException
	   * @throws IOException
	   */
	  public static void download(HttpServletRequest request, HttpServletResponse response, File file)throws ServletException, IOException {
	 
	    String mimetype = request.getSession().getServletContext().getMimeType(file.getName());
	    String _fileName = file.getName();
	    
	    if (file == null || !file.exists() || file.length() <= 0 || file.isDirectory()) {
	      throw new IOException("Null File or Not File");
	    }
	    
	    InputStream is = null;
	    
	    if( request.getParameter("SAVE_FILE_NAME") != null && request.getParameter("SAVE_FILE_NAME").equals("")==false )
	    {
	    	
	    	int _pos = _fileName.lastIndexOf(".");
	    	if( _pos != -1 ) _fileName = URLDecoder.decode( request.getParameter("SAVE_FILE_NAME"), "UTF-8" ).replaceAll("[+]", " ") + _fileName.substring(_pos);
	    }
	    
	    try {
	      is = new FileInputStream(file);
	      download(request, response, is, _fileName, file.length(), mimetype);
	    } catch (Exception ex) {
	      
	    } finally {
	      try {
	        is.close();
	      } catch (Exception ex) {
	      }
	    }
	  }
	 
	  /**
	   * 
	   */
	  public static void download(HttpServletRequest request, HttpServletResponse response, InputStream is, String filename, long filesize, String mimetype) throws ServletException, IOException 
	  {
		  String mime = mimetype;
		 
		  if (mimetype == null || mimetype.length() == 0) {
			  mime = "application/octet-stream;";
		  }
	 
		  byte[] buffer = new byte[BUFFER_SIZE];
		  response.setContentType(mime + "; charset=" + CHARSET);
		  String userAgent = request.getHeader("User-Agent");
		 
		    
		  // browser별 테스트 필요
		  if (userAgent != null && userAgent.indexOf("MSIE 5.5") > -1) { // MS IE 5.5 이하
			  response.setHeader("Content-Disposition", "filename=" + URLEncoder.encode(filename, "UTF-8") + ";");
		  } else if (userAgent != null && userAgent.indexOf("MSIE") > -1) { // MS IE (보통은 6.x 이상 가정)
			  response.setHeader("Content-Disposition", "attachment; filename="+ java.net.URLEncoder.encode(filename, "UTF-8") + ";");
		  } else { // opera or mozila
			  response.setHeader("Content-Disposition", "attachment; filename="+ new String(filename.getBytes(CHARSET), "latin1") + ";");
		  }
		   
		  if (filesize > 0) {
			  response.setHeader("Content-Length", "" + filesize);
		  }

		  BufferedInputStream fin			= null;
		  BufferedOutputStream outs	= null;
		  
		  try {
			  fin		= new BufferedInputStream(is);
			  outs		= new BufferedOutputStream(response.getOutputStream());
			  int read	= 0;
			  
			  while ((read = fin.read(buffer)) != -1) {
				  outs.write(buffer, 0, read);
			  }
		   
		  } catch (IOException ex) {
		   
		  } finally {
			  try {
				  outs.close();
			  } catch (Exception ex1) {
			  }
			  try {
				  fin.close();
			  } catch (Exception ex2) {
			  }
		  } 
		 
	  }
	  
	  
	  public static void sendHeaderFailMessage(HttpServletRequest request, HttpServletResponse response, String _code ) throws ServletException, IOException 
	  {
		  response.setStatus( HttpServletResponse.SC_NOT_FOUND );
		  String _message = _code;
		  if(Log.getMessage(_code) != "")
		  {
			  _message = URLEncoder.encode( Log.getMessage(_code), "UTF-8");
		  }
		  
		  response.setHeader("Content-Length", ""+_message.getBytes().length);
		  response.getOutputStream().println( _message );
	  }
}
