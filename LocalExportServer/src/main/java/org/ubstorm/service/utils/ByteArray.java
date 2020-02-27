package org.ubstorm.service.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class ByteArray
{
    // constructors...
    public ByteArray()
    {
        byteBuffer_ = null;
        length_ = 0;
        maxLength_ = 0;
        position_ = 0;
    }

    public ByteArray (int length) throws ArrayIndexOutOfBoundsException
    {
        byteBuffer_ = new byte[length];
        length_ = length;
        maxLength_ = length;
        position_ = 0;
    }

    public ByteArray(byte[] buffer)
    {
        setBuffer(buffer);
    }

    /////////////////////////////////////////////////////////////////////////////

    public byte[] getBuffer() {
        return byteBuffer_;
    }

    public void setBuffer(byte[] byteBuffer) {
        if (byteBuffer != null)
        {
            this.byteBuffer_ = byteBuffer;
            this.length_ = byteBuffer.length;
            position_ = 0;
            maxLength_ = length_;
        }
        else
        {
            byteBuffer_ = null;
            length_ = 0;
            position_ = 0;
            maxLength_ = 0;
        }
    }

    public int getLength() {
        return length_;
    }

    public void setLength(int length) {
        extendAs(length);
    }

    public int getPosition() {
        return position_;
    }

    public void setPosition(int position) {
        if (position < 0 || position >= length_)
            return ;
        this.position_ = position;
    }

    ////////////////////////////////////////////////////////////////////////////
    protected void extendAs(int newLength)
    {
        if (length_ == newLength) return;
        if (length_ < newLength)
        {
            if (maxLength_ < newLength)
            {
                byte[] temp = new byte[newLength];
                if (byteBuffer_ != null)
                    System.arraycopy(byteBuffer_, 0, temp, 0, length_);

                for (int i = length_; i < newLength; i++)
                {
                    temp[i] = 0;
                }
                maxLength_ = newLength;
                byteBuffer_ = temp;
            }
        }
        else
        {
            for (int i = newLength; i < length_; i++)
            {
                byteBuffer_[i] = 0;
            }
            position_ = newLength - 1;
        }

        length_ = newLength;
    }

    
    public boolean readBoolean() {
        int ch = (char)byteBuffer_[position_++];
        
        return (ch != 0);
    }

    public int readInt ()
    {
        int ret = ((char)byteBuffer_[position_++] & 0xff) << 24 |
                  ((char)byteBuffer_[position_++] & 0xff) << 16 |
                  ((char)byteBuffer_[position_++] & 0xff) << 8  |
                  ((char)byteBuffer_[position_++] & 0xff) ;

        return ret;
    }  

    public float readFloat ()
    {
        float ret = (float)
                  ( ((char)byteBuffer_[position_++] & 0xff) << 24 |
                    ((char)byteBuffer_[position_++] & 0xff) << 16 |
                    ((char)byteBuffer_[position_++] & 0xff) << 8  |
                    ((char)byteBuffer_[position_++] & 0xff) );

        return ret;
    }

    public long readBigInt ()
    {
        long ret = ((char)byteBuffer_[position_++] & 0xff) << 56 |
                   ((char)byteBuffer_[position_++] & 0xff) << 48 |
                   ((char)byteBuffer_[position_++] & 0xff) << 40 |
                   ((char)byteBuffer_[position_++] & 0xff) << 32 |
                   ((char)byteBuffer_[position_++] & 0xff) << 24 |
                   ((char)byteBuffer_[position_++] & 0xff) << 16 |
                   ((char)byteBuffer_[position_++] & 0xff) << 8  |
                   ((char)byteBuffer_[position_++] & 0xff) ;

        return ret;
    }

    public double readDouble ()
    {
        double ret = (double)
                 ( ((char)byteBuffer_[position_++] & 0xff) << 56 |
                   ((char)byteBuffer_[position_++] & 0xff) << 48 |
                   ((char)byteBuffer_[position_++] & 0xff) << 40 |
                   ((char)byteBuffer_[position_++] & 0xff) << 32 |
                   ((char)byteBuffer_[position_++] & 0xff) << 24 |
                   ((char)byteBuffer_[position_++] & 0xff) << 16 |
                   ((char)byteBuffer_[position_++] & 0xff) << 8  |
                   ((char)byteBuffer_[position_++] & 0xff) );

        return ret;
    }

    public short readShort ()
    {
        short ret = (short)( ((char)byteBuffer_[position_++] &0xff) << 8 |
                             ((char)byteBuffer_[position_++] & 0xff) );
        return ret;
    }

    public byte readByte ()
    {
        return byteBuffer_[position_++];
    }

    public String readUTFBytes (int length) throws UnsupportedEncodingException
    {
        byte [] temp = new byte[length];
        System.arraycopy(byteBuffer_, position_, temp, 0, length);

        return new String(temp, "UTF-8");
    }

    // write...
    public void writeInt (int value)
    {
        byteBuffer_[position_++] = (byte)((value >> 24));
        byteBuffer_[position_++] = (byte)((value >> 16));
        byteBuffer_[position_++] = (byte)((value >> 8) );
        byteBuffer_[position_++] = (byte)(value );
    }

    public void writeBigInt (long value)
    {
        byteBuffer_[position_++] = (byte)((value >> 56));
        byteBuffer_[position_++] = (byte)((value >> 48));
        byteBuffer_[position_++] = (byte)((value >> 40));
        byteBuffer_[position_++] = (byte)((value >> 32));
        byteBuffer_[position_++] = (byte)((value >> 24));
        byteBuffer_[position_++] = (byte)((value >> 16));
        byteBuffer_[position_++] = (byte)((value >> 8) );
        byteBuffer_[position_++] = (byte)(value );
    }

    public void writeDouble (double value)
    {
        long val = (long)value;
        byteBuffer_[position_++] = (byte)((val >> 56));
        byteBuffer_[position_++] = (byte)((val >> 48));
        byteBuffer_[position_++] = (byte)((val >> 40));
        byteBuffer_[position_++] = (byte)((val >> 32));
        byteBuffer_[position_++] = (byte)((val >> 24));
        byteBuffer_[position_++] = (byte)((val >> 16));
        byteBuffer_[position_++] = (byte)((val >> 8) );
        byteBuffer_[position_++] = (byte)(val );
    }

    public void writeFloat (float value)
    {
        long val = (long) value;
        val &= 0xffffffff;

        byteBuffer_[position_++] = (byte)((val >> 24));
        byteBuffer_[position_++] = (byte)((val >> 16));
        byteBuffer_[position_++] = (byte)((val >> 8) );
        byteBuffer_[position_++] = (byte)(val );
    }

    public void writeShort (int value)
    {
        byteBuffer_[position_++] = (byte)((value >> 8) );
        byteBuffer_[position_++] = (byte)(value );
    }

    public void writeByte (byte value)
    {
        byteBuffer_[position_++] = (byte)value;
    }

    public void writeUTFBytes (String value) throws UnsupportedEncodingException
    {
        byte[] temp = value.getBytes("UTF-8");

        int len = temp.length;

        if (position_ + len > getLength())
        {
            setLength(position_ + len);
        }

        System.arraycopy(temp, 0, byteBuffer_, position_, len);
    }

    public void writeUTFBytes (String value, int len)
                                            throws UnsupportedEncodingException
    {
        byte[] temp = value.getBytes("UTF-8");

        int copyLen = Math.min(len, temp.length);

        if (position_ + copyLen > getLength())
        {
            setLength(position_ + copyLen);
        }

        System.arraycopy(temp, 0, byteBuffer_, position_, copyLen);
    }

    ////////////////////////////////////////////////////////////////////////////
    public void readBytes(ByteArray byteArray, int offset, int len)
    {
        System.arraycopy(byteBuffer_, position_, byteArray.getBuffer(), offset, len);
        position_ += len;
    }

    public void writeBytes (ByteArray byteArray, int offset, int len)
    {
        if (length_ < position_ + len)
        {
            setLength(position_ + len);
        }

        System.arraycopy(byteArray.getBuffer(), offset, byteBuffer_, position_, len);
    }

    public void writeBytes (ByteArray byteArray)
    {
        writeBytes(byteArray, 0, byteArray.getLength());
    }


    /////////////////////////////////////////////////////////////////

    protected byte [] byteBuffer_ = null;
    public int position_ = 0;
    public int length_ = 0;
    protected int maxLength_ = 0;
}