package org.ubstorm.server.printable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.utils.Base64Coder;
import org.ubstorm.service.utils.common;
import org.ubstorm.service.utils.compress.ZipCompress;

public class FORMFile {
	
	private int mPageCount = 0;
	//private HashMap<String, Object> mPageMapInfo;
	private JSONObject mPageMapInfo;
	private String mCacheOutFilePath = "";
	private BufferedOutputStream mCacheOutStream;
	private String mCacheMapInfoOutFilePath = "";
	private BufferedOutputStream mCacheMapInfoOutStream;
	
	private String mCacheProjectInfoOutFilePath = "";
	private BufferedOutputStream mCacheProjectInfoOutStream;
	//private HashMap<String, Object> mProjectInfo;
	private JSONObject mProjectInfo;
	
	private String mCacheOutFileDirectory = "";
	
	public FORMFile(String cfilePath) {
		//this.mPageMapInfo = new HashMap<String, Object>();
		this.mPageMapInfo = new JSONObject();
		
		this.mPageCount = 0;
		if(Log.previewFlag){
			if(Log.currPageIdx<Log.prePageBuff){
				this.mPageCount = 0;
			}else{
				this.mPageCount = Log.currPageIdx - Log.prePageBuff;
			}
			//this.mPageCount = Log.currPageIdx;
		}else if(Log.execPrintFlag){
			this.mPageCount = Integer.parseInt(Log.execPrintPage.split("-")[0])-1;
		}
		
		try {
			
			//디렉토리 생성
			File _directory = new File((new File( cfilePath )).getParent());
			if(!_directory.exists()){
				_directory.mkdirs(); 
			}
			
			this.mCacheOutFilePath = cfilePath;
			this.mCacheOutStream = new BufferedOutputStream(new FileOutputStream(cfilePath));
			
			this.mCacheOutFileDirectory = _directory.getAbsolutePath();					
			this.mCacheMapInfoOutFilePath = _directory.getAbsolutePath()+"/tmpFormFilePageMapInfo.txt";
			this.mCacheMapInfoOutStream = new BufferedOutputStream(new FileOutputStream(mCacheMapInfoOutFilePath));
			
			this.mCacheProjectInfoOutFilePath = _directory.getAbsolutePath()+"/tmpFormFileProjectInfo.txt";
			this.mCacheProjectInfoOutStream = new BufferedOutputStream(new FileOutputStream(mCacheProjectInfoOutFilePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	  
    public int getNumPages() {
    	return mPageCount;
    }
    
    public String getOutFileDirectory()
    {
    	return this.mCacheOutFileDirectory;
    }
        
    public JSONObject getPage(int pageindex) throws IOException, ParseException, DataFormatException {
    	byte[] line = searchPageData(pageindex);
    	JSONObject _pageData = getPageData(line, "" + pageindex);
        return _pageData;
    }
    
    private JSONObject getPageData(byte[] _pageItem, String _pageNo) throws ParseException, IOException, DataFormatException
	{
		JSONObject objPageData = null;
	
		//if(properties != null && properties.containsKey("page" + _pageNo))
    	{
    		//String pageItem = (String) properties.get("page" + _pageNo);
    		byte[] inflate = common.decompress(_pageItem);
    		String resdata = new String(inflate, "utf-8");
    		
    		JSONObject objPageObjects = (JSONObject) JSONValue.parseWithException((String)resdata);	
    		JSONObject objPageInfo = (JSONObject) objPageObjects.get("pageInfo");
    		//objPageData = (JSONObject) objPageInfo.get("" + (Integer.valueOf(_pageNo)));
    		String _sPageIdx = (String) this.mPageMapInfo.get(_pageNo);
    		objPageData = (JSONObject) objPageInfo.get(_sPageIdx);
    	}
		
		return objPageData;
	}    
    
    private byte[] searchPageData(int pageindex) throws IOException
    {
    	InputStream in = new FileInputStream(new File(this.mCacheOutFilePath));
    	BufferedInputStream bis = new BufferedInputStream(in);
    	
    	long nOffset = Long.parseLong(this.mPageMapInfo.get(pageindex + "_offset").toString());
    	int nSize = Integer.parseInt( this.mPageMapInfo.get(pageindex + "_size").toString());
    	
    	System.out.println(getClass().getName() + "::" + "searchPageData...pageindex=" + pageindex + ", offset=" + nOffset + ", size=" + nSize);   
    	
    	byte[] buffer = new byte[nSize];   	
        
    	try
    	{
    		bis.skip(nOffset);
	    	int len = bis.read(buffer, 0, nSize);
    	}
    	catch(Exception exp)
    	{
    		exp.printStackTrace();
    	}
    	finally
    	{
    		in.close();
    		bis.close();
    	}
    	
        return buffer;
    }
    
    public void addProjectInfo(JSONObject projInfo) {
    	this.mProjectInfo = projInfo;
    }
    
    public JSONObject getProjectInfo() {
    	return this.mProjectInfo;
    }
    
    
    public void addPageB64(int curPageNo, byte[] oResultByte) {
    	try {
    		if(curPageNo < 0)
    		{
    			// Project Info page
    			System.out.println(getClass().getName() + "::" + "addPageB64... Project Info page skipped!!!");       			
    			return;
    		}    	
    		
    		this.mPageMapInfo.put("" + mPageCount, "" + curPageNo);
   		
			System.out.println(getClass().getName() + "::" + "addPageB64()...pageCount=" + mPageCount + " byte[] unCompressedDataLength=" + oResultByte.length);
			
	       	ZipCompress compressor = new ZipCompress();
	       	oResultByte = compressor.compress(oResultByte);
		       	
			System.out.println(getClass().getName() + "::" + "addPageB64()...pageCount=" + mPageCount + " oResultSet CompressedDataLength=" + oResultByte.length);

    		this.mPageMapInfo.put(mPageCount + "_offset", mPageCount==0 ?  0 : Long.parseLong(this.mPageMapInfo.get((mPageCount-1) + "_offset").toString()) +  Integer.parseInt(this.mPageMapInfo.get((mPageCount-1) + "_size").toString()));
    		this.mPageMapInfo.put(mPageCount + "_size", oResultByte.length);
 			
    		this.mCacheOutStream.write(oResultByte);
			this.mCacheOutStream.flush();
	    	mPageCount++;
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
    public void addPageB64One(int curPageNo, byte[] oResultByte,Boolean isOnePage ) {
    	try {
    		if(curPageNo < 0)
    		{
    			// Project Info page
    			System.out.println(getClass().getName() + "::" + "addPageB64One... Project Info page skipped!!!");       			
    			return;
    		}    	
    		
    		this.mPageMapInfo.put("" + mPageCount, "" + curPageNo);
   		
			System.out.println(getClass().getName() + "::" + "addPageB64One()...pageCount=" + mPageCount + " byte[] unCompressedDataLength=" + oResultByte.length);
			
	       	ZipCompress compressor = new ZipCompress();
	       	oResultByte = compressor.compress(oResultByte);
		       	
			System.out.println(getClass().getName() + "::" + "addPageB64One()...pageCount=" + mPageCount + " oResultSet CompressedDataLength=" + oResultByte.length);

			//if(isOnePage){
			//	this.mPageMapInfo.put(mPageCount + "_offset", 0);
			//}
			//else
			//{
				if(mPageCount == 0 || this.mPageMapInfo.get((mPageCount-1) + "_offset") == null){
					this.mPageMapInfo.put(mPageCount + "_offset",0);
				}else {
					this.mPageMapInfo.put(mPageCount + "_offset", Long.parseLong(this.mPageMapInfo.get((mPageCount-1) + "_offset").toString()) +  Integer.parseInt(this.mPageMapInfo.get((mPageCount-1) + "_size").toString()));
				}				
			//}
    		this.mPageMapInfo.put(mPageCount + "_size", oResultByte.length); 			
    		
    		
    		this.mCacheOutStream.write(oResultByte);
			this.mCacheOutStream.flush();
	    	mPageCount++;
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
    
    
    public void close()
    {
    	if(this.mCacheOutStream != null)
			try {
				this.mCacheOutStream.close();
				
				this.mCacheMapInfoOutStream.write(this.mPageMapInfo.toJSONString().getBytes("UTF-8"));
				this.mCacheMapInfoOutStream.flush();				
				this.mCacheMapInfoOutStream.close();
				
				//this.mCacheProjectInfoOutStream.write(this.mProjectInfo.toJSONString().getBytes("UTF-8"));
				//this.mCacheProjectInfoOutStream.flush();				
				//this.mCacheProjectInfoOutStream.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    
    public void clear()
    {
    	if(this.mCacheOutStream != null) {
			try {
				this.mCacheOutStream.close();
				File f = new File( this.mCacheOutFilePath );
				f.delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
    	}
    	
    	this.mPageMapInfo.clear();
    	
    	if(this.mCacheMapInfoOutStream != null) {
			try {
				this.mCacheMapInfoOutStream.close();
				File f = new File( this.mCacheMapInfoOutFilePath );
				f.delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
    	}
    }

}
