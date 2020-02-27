package org.ubstorm.service.utils;

public class ValueConverter {

	
	/**
	 * Object 를 String 으로 변경
	 * @param value
	 * @return
	 */
	public static final String getString(Object value)
	{
		return String.valueOf(value);
	}
	/**
	 * Int 를 String 으로 변경
	 * @param value
	 * @return
	 */
	public static final String getString(int value)
	{
		return String.valueOf(value);
	}
	/**
	 * Float 을 String 으로 변경
	 * @param value
	 * @return
	 */
	public static final String getString(float value)
	{
		return String.valueOf(value);
	}
	
	/**
	 * Object 를 Float 으로 변경
	 * @param value
	 * @return
	 */
	public static final float getFloat(Object value)
	{
		return Float.valueOf(getString(value));
	}
	
	/**
	 * String 을 Float 으로 변경
	 * @param value
	 * @return
	 */
	public static final float getFloat(String value)
	{
		return Float.valueOf(value);
	}
	
	/**
	 * Object 를 Int 으로 변경
	 * @param value
	 * @return
	 */
	public static final int getInteger(Object value)
	{
		return Integer.valueOf(getString(value));
	}
	
	/**
	 * String 을 Int 으로 변경
	 * @param value
	 * @return
	 */
	public static final int getInteger(String value)
	{
		return Integer.valueOf(value);
	}
	
	public static boolean isInt( String _value )
	{
		try {
			Integer.valueOf(_value);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}
		
	/**
	 * float 을 Int 으로 변경
	 * @param value
	 * @return
	 */
	public static final int getInteger(float value)
	{
		return Math.round(value);
	}
	
	/**
	 * Object 를 Short 로 변경
	 * @param value
	 * @return
	 */
	public static final short getShort(Object value)
	{
		return Short.valueOf(getString(value));
	}
	
	/**
	 * String 을 Short 로 변경
	 * @param value
	 * @return
	 */
	public static final short getShort(String value)
	{
		return Short.valueOf(value);
	}
	
	/**
	 * Object 를 Boolean 으로 변경
	 * @param value
	 * @return
	 */
	public static final boolean getBoolean(Object value)
	{
		try {
			return Boolean.valueOf(getString(value));
			
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Object 를 첫글자가 대문자인 String 으로 변경(HWP Align 에만 사용)
	 * @param value
	 * @return
	 */
	public static final String getUpperString( Object value )
	{
		String _txt = String.valueOf(value);
		
		_txt = _txt.substring(0, 1).toUpperCase() + _txt.substring(1, _txt.length());
		
		return _txt;
	}
	
}
