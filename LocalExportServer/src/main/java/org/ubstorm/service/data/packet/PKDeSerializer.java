package org.ubstorm.service.data.packet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.ubstorm.service.utils.ByteArray;
import org.ubstorm.service.utils.compress.ZipCompress;
import org.ubstorm.service.utils.crypto.AesCrypto;
import org.ubstorm.service.utils.crypto.DesCrypto;

public class PKDeSerializer {
	
	private Logger log = Logger.getLogger(getClass());
	
	public boolean isUsefulPacket = false;

	private PKHeader m_header;
	private boolean m_isDeserializing;
	private String m_errorMsg;
	
	private ArrayList<HashMap<String, Object>> _dataset;

	public PKDeSerializer()
	{
		m_header = null;
		m_isDeserializing = false;
		m_errorMsg = null;
		_dataset = new ArrayList<HashMap<String, Object>>();
	}
	
	public boolean isDeserializing() {
		return m_isDeserializing;
	}

	public String getErorMsg() {
		return m_errorMsg;
	}
	
	public void beginDeserialization()
	{
		m_header = null;
		m_isDeserializing = true;
		m_errorMsg = null;
	}
	
	public boolean validateDeserializable(ByteArray buffer)
	{
		m_header = null;
		
		if (buffer.length_ - buffer.position_ < PKHeader.getHeaderSize())
			return false;
		
		ByteArray headBuffer = new ByteArray(PKHeader.getHeaderSize());
		headBuffer.length_ = PKHeader.getHeaderSize();
		buffer.readBytes(headBuffer, 0, PKHeader.getHeaderSize());
		
		m_header = new PKHeader(headBuffer);
		
		return true;
	}
	
	// return bytes size of deSerialized.
	public int deSerialize(PKDataStream stream) throws Exception
	{
		ByteArray buffer = stream.pk_buffer;
		int offset = stream.pk_offset;
		
		buffer.position_ = offset;
		if (!validateDeserializable(buffer))
		{
			isUsefulPacket = false;
			return offset;
		}
		
		if(m_header.getIsFirst())
		{
			stream.m_nTotRowCount = m_header.getTotRowCount();
		}
		
		int dataSize = buffer.getBuffer().length;
		//if (buffer.length_ < m_header.getDataSize()){
		if (buffer.length_ < m_header.getDataSize()+buffer.position_ || m_header.getDataSize() <= 0 ){	
			isUsefulPacket = false;
			return offset;
		}
		ByteArray rawPacket = new ByteArray(m_header.getDataSize()); 
		buffer.readBytes(rawPacket, 0, m_header.getDataSize());
		
		if (m_header.getIsCompress())
		{
			ByteArray dataUncompressed = new ByteArray(m_header.getOrgDataSize());
			ZipCompress comp = new ZipCompress();
			comp.deprocess2(rawPacket, dataUncompressed, m_header.getOrgDataSize());
			rawPacket = dataUncompressed;
		}
		
		if (m_header.getIsEncoding())
		{
			//byte [] baDecrypted = AesCrypto.decryptToByteArray(rawPacket.getBuffer(), null);
			byte [] baDecrypted = DesCrypto.decryptToByteArray(rawPacket.getBuffer(), null);
			
			ByteArray dataDecrypted = new ByteArray(baDecrypted);
			rawPacket = dataDecrypted;
		}
		
		if(readPacketBody(rawPacket))
			isUsefulPacket = true;
		else
			isUsefulPacket = false;
			
		return offset + m_header.getDataSize() + PKHeader.getHeaderSize();
	}
	
	
	// read Packet Body
	protected boolean readPacketBody(ByteArray buffer) throws UnsupportedEncodingException, ParseException
	{
		buffer.position_ = 0;
		
		String strResult = buffer.readUTFBytes(m_header.getOrgDataSize());
	
		//System.out.println("readPacketBody::bodyData=" + strResult);
		log.debug("readPacketBody::bodyData.length=" + strResult.length());
		
		Object jsonObject = JSONValue.parseWithException(strResult);
		if(jsonObject != null)
		{
			JSONObject ubobj= (JSONObject) jsonObject;	
			
			if(m_header.getIsSuccess())
			{			
				// jsonObject를  array로 casting
				JSONArray jsonArray = (JSONArray) ubobj.get("DATA");
				
				for(int i=0; i< jsonArray.size(); i++)
				{
					if(m_header.getIsFirst() && i == 0)	// Header Row는 생략한다.
					{
						continue;
					}

					JSONObject row = (JSONObject) jsonArray.get(i);
					_dataset.add(row);	
				}
			}
			else
			{
				//var jsonResult:String = jsonObject[0][0].RESULT as String;
				String jsonResult = (String) ubobj.get("RESULT");		
				m_errorMsg = jsonResult;
				return false;
			}
		}
				
		return true;
	}
		
	public ArrayList<HashMap<String, Object>> getDataSet()
	{
		return _dataset;
	}
}
