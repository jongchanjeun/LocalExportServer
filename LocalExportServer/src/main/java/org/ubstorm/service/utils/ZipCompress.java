package org.ubstorm.service.utils;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.ubstorm.service.utils.ByteArray;

public class ZipCompress {

	public ZipCompress(){		
	}

	public int deprocess(ByteArray inData, ByteArray outData){
                outData.setBuffer ( uncompress(inData.getBuffer ()) );
                return outData.getLength ();
	}
	
	public int deprocess2(ByteArray inData, ByteArray outData, int nOrgSize){
                outData.setBuffer ( uncompress2(inData.getBuffer (), nOrgSize) );
                return outData.getLength ();
	}

	public int process(ByteArray inData, ByteArray outData){
                outData.setBuffer ( compress(inData.getBuffer ()) );
                return outData.getLength ();
	}
        
    public byte [] compress(byte [] inData)
    {
        Deflater deflater_ = new Deflater(); 
        deflater_.setInput(inData,0,inData.length);
        deflater_.finish ();

        byte [] outData = new byte [inData.length *2 ];
        int outDataLength = deflater_.deflate (outData,0,outData.length);

        byte [] ret = new byte[outDataLength];

        System.arraycopy (outData,0,ret,0,outDataLength);

        return ret;
    }
       
    public byte [] uncompress(byte [] inData)
    {                    
        Inflater inflater_ = new Inflater(); 
        inflater_.setInput(inData,0,inData.length);

        byte [] outData = new byte[inData.length * 2 + 1024];	
        int outDataLength = 0 ;
        try
        {
            outDataLength = inflater_.inflate(outData, 0, outData.length);
        } catch (DataFormatException ex)
        {
            ex.printStackTrace();
            return null;
        }

        byte [] ret = new byte[outDataLength];
        System.arraycopy (outData,0,ret,0,outDataLength);
        return ret;
    }
        
    public byte [] uncompress2(byte [] inData, int bufsize)
    {                    
        Inflater inflater_ = new Inflater(); 
        inflater_.setInput(inData,0,inData.length);

        byte [] outData = new byte[bufsize];	
        int outDataLength = 0 ;
        try
        {
            outDataLength = inflater_.inflate(outData, 0, outData.length);
        } catch (DataFormatException ex)
        {
            ex.printStackTrace();
            return null;
        }

        byte [] ret = new byte[outDataLength];
        System.arraycopy (outData,0,ret,0,outDataLength);
        return ret;
    }    
}