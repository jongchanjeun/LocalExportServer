
package org.ubstorm.service.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileManager {


    private String sCurrentFile;

    private String sCurrentFileType;

    private String sFileEncoding = "";//ISO8859_1";

    private String sFileNameEncoding = "";

    private boolean DEFAULT_APPEND = true;

    private String sRoot;

    private String sDir;

    private String sCurrentPath;


    public FileManager()
    {
    }
    public FileManager(String sDir,
            String sFileName) throws IOException {
        this.sRoot = sRoot;

        if (sDir == null || sDir.equals("")) {
            throw new IOException("initDir() in DirectoryManager : Configuration Exception=> DIRECTORY was Not Set");
        }
        this.sDir = sDir;
        this.sCurrentPath = sDir;
        this.sFileEncoding = "UTF-8";
        this.sFileNameEncoding = "";
        initFile(sFileName);
    }


    private void initFile(String sFileName) throws IOException {
        if (sFileName == null || sFileName.equals("")) {
            throw new IOException("initFile(String) in FileManager : String sFileName Cannot Be Null");
        }
        setCurrentFile(sFileName);
    }


    public boolean isFile(String sFullPathName) throws IOException {
        if (new File(sFullPathName).exists()) {
            return true;
        } else {
            return false;
        }
    }

    public void setCurrentFile(String sFileName) throws IOException {
        
    	if (!sFileNameEncoding.equals("")) {
            this.sCurrentFile = new String(sFileName.getBytes(), sFileNameEncoding);
        //infoLog.log(2,new StringBuffer("sCurrentFile value was encoded as "+sFileNameEncoding+", Check ENCODING_SET_NAME value in config.properties"));
        } else {
            this.sCurrentFile = sFileName;
        }
        
    	try
    	{
	        if (!isFile(sCurrentPath + sCurrentFile)) {
	            if (!new File(sCurrentPath + sCurrentFile).createNewFile()) {
	                throw new IOException("initFile(String) in FileManager : Cannot Create New File(" + sCurrentPath + sCurrentFile + ")");
	            }
	        }
	        String sFileType = "";
	        
	        if (new File(sCurrentPath + sFileName).isFile()) {
	            sFileType = "N";
	        } else if (new File(sCurrentPath + sFileName).isFile()) {
	            sFileType = "F";
	        } else {
	        }
	        
	        this.sCurrentFileType = sFileType;
    	}
    	catch(IOException exp)
    	{
    		System.out.println(exp.getMessage());
    		//throw new IOException(exp);
    		IOException e = new IOException("IOException with nested IOException");
            e.initCause(exp);
            throw e;
    	}
    }

    
    public boolean write(StringBuffer sb) throws IOException {
        boolean bWriteOk = write(sb, DEFAULT_APPEND);
        return bWriteOk;
    }

    
    public boolean write(StringBuffer sb,
            boolean bAppend) throws IOException {

        boolean bWriteOk = false;
        FileOutputStream fos = new FileOutputStream(sCurrentPath + sCurrentFile, bAppend);
        BufferedOutputStream bos = new BufferedOutputStream(fos, 8 * 1024); // 8K
        byte[] bbuf = sb.toString().getBytes();
        try {
            bos.write(bbuf, 0, bbuf.length);
            bos.flush();
            bWriteOk = true;
        } catch (IOException ie) {
            throw new IOException("write(StringBuffer,String,String) in FileManager : Cannot Write at File(" + sCurrentPath + sCurrentFile + ":" + ie.getMessage() + ")");
        } finally {
            bos.close();
            fos.close();
        }

        return bWriteOk;
    }

    
	public StringBuffer read() throws IOException {
		StringBuffer sb = new StringBuffer();
		FileInputStream fis = new FileInputStream(sCurrentPath+sCurrentFile);
		BufferedInputStream bis= new BufferedInputStream(fis, 8 * 1024); // 8K
		byte[] bBuffer = new byte[100 * 1024];  // 100K
		int iReadLen = 0;
		int iReadLenTot = 0;

		try {
			while ((iReadLen = bis.read(bBuffer, 0, bBuffer.length)) != -1) {
				iReadLenTot += iReadLen;
				sb.append(new String(bBuffer, 0, iReadLen, sFileEncoding));
			}
		}catch (IOException ie) {
			 throw new IOException("read() in FileManager : Cannot Read File("+sCurrentPath+sCurrentFile+":"+ie.getMessage()+")");
		}finally{
			bis.close();
			fis.close();
		}
		return sb;
	}
}
