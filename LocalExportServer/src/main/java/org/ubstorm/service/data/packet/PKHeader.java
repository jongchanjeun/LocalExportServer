package org.ubstorm.service.data.packet;

import org.ubstorm.service.utils.ByteArray;

public class PKHeader {
	private ByteArray _buffer;
	private int baseOffset;
	
	public PKHeader(ByteArray inBuffer)
	{
		_buffer = new ByteArray();
		if (inBuffer != null)
		{
			_buffer.writeBytes(inBuffer, 0, inBuffer.getLength());
		}
	}
	
	public static int getHeaderSize() 
	{
		return 16;
	}
	
	public boolean getIsSuccess()
	{
		_buffer.position_ = baseOffset + 0;
		return _buffer.readBoolean();
	}
	
	public boolean getIsFirst()
	{
		_buffer.position_ = baseOffset + 1;
		return _buffer.readBoolean();
	}
	
	public boolean getIsCompress()
	{
		_buffer.position_ = baseOffset + 2;
		return _buffer.readBoolean();
	}
	
	public boolean getIsEncoding()
	{
		_buffer.position_ = baseOffset + 3;
		return _buffer.readBoolean();
	}
	
	public int getDataSize()
	{
		_buffer.position_ = baseOffset + 4;
		return _buffer.readInt(); 
	}
	
	public int getOrgDataSize()
	{
		_buffer.position_ = baseOffset + 8;
		return _buffer.readInt(); 
	}
	
	public int getTotRowCount()
	{
		_buffer.position_ = baseOffset + 12;
		return _buffer.readInt(); 
	}
}
