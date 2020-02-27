package org.ubstorm.service.data.packet;

import org.ubstorm.service.utils.ByteArray;

public class PKDataStream {
	public int pk_offset = 0;
	public ByteArray pk_buffer = null;
	public int m_nTotRowCount = 0;

	public PKDataStream()
	{
		pk_buffer = new ByteArray();
		this.pk_offset = 0;
		this.m_nTotRowCount = 0;
	}
	
	public void parsePkData(byte[] inBuff)
	{
		ByteArray tmpBuffer = new ByteArray(inBuff);
		
		pk_buffer.position_ = pk_buffer.length_;
		pk_buffer.writeBytes(tmpBuffer);
	}
	
	public void clearBuffer()
	{
		pk_buffer = new ByteArray();
		pk_offset = 0;
		m_nTotRowCount = 0;
	}

	
}
