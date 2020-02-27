package org.ubstorm.service.formatter;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ubstorm.service.utils.StringUtil;
import org.ubstorm.service.utils.common;

public class UBFormatter {
	
	/**
	 * format
	 *
	 * 지원 포멧
	 * 	YYYY-MM-DD
	 *  YYYYMMDD
	 *  YYMMDD
	 * @throws ParseException 
	 *  
	 * */
	public static String dateFormat( String _formatString , String _value ) throws ParseException
	{
		String _result="";

		// flex에서 표현하는 format String과 java에서 표현하는 format String이 다르기 때문에 바꿔준다.
		String _format = "yyyyMMdd";
		
		//_value값이 yyyyMMdd형태가 아닐경우가 있으므로 사전에 숫자가 아닌값은 모두 제거하여 진행한다.
//		_value = _value.replaceAll("[^\\d.-]", "");
		_value = _value.replaceAll("[:/-]", "");
		
		SimpleDateFormat format = new SimpleDateFormat( _format );
		Date _date = format.parse(_value);
		
		if( _formatString.equals("YYYY-MM-DD") ){
//			_format	=	"yyyy-MM-dd";
			_result = String.format("%1$tY-%1$tm-%1$td", _date);
		}else if( _formatString.equals("YYYYMMDD") ){
//			_format	=	"yyyyMMdd";
			_result = String.format("%1$tY%1$tm%1$td", _date);
		}else if( _formatString.equals("YYMMDD") ){
//			_format	=	"yyMMdd"; 
			_result = String.format("%1$ty%1$tm%1$td", _date);
		}else{
			// 지원하는 포멧인 아닙니다.
		}
		
//		_result = format.format(_date); 
		
		return _result;
	}
	
	
	/**
	 * _formatString		: 포멧형식
	 * _decimalPoint		: 소숫점 자리수
	 * _thousandComma		: 천단위 콤마
	 * _isDemecal			: 소숫점 표현
	 * 
	 * */
	public static String maskNumberFormat( String _formatString , int _decimalPoint , Boolean _thousandComma ,  Boolean _isDemecal , String _value ) throws ParseException
	{
		String _result="";
		
		_result = maskNumberFormatBigDecimal(_formatString, _decimalPoint, _thousandComma, _isDemecal, _value );
		return _result;
		
		/**
		double _valueInt=0;
		
		if( _value.equalsIgnoreCase("") ){
			return _result;
		}
		
		if( StringUtil.isDouble(_value) == false )
		{
			return _value;
		}
		
		if(_decimalPoint > 0)
		{
			String _decimalFormat = "####0.";
			if(_thousandComma)   _decimalFormat = "#,##0.";
				
			String _str = "";
			if(_isDemecal)
			{
				_str = "0";
			}
			else
			{
				_str = "#";
			}
			
			for (int i = 0; i < _decimalPoint; i++) {
				_decimalFormat = _decimalFormat + _str;
			}
			
			DecimalFormat df=new DecimalFormat( _decimalFormat );
			
			try {
				_valueInt = Double.parseDouble(_value);
			} catch (Exception e) {
			}
			_result = df.format( _valueInt  ); 
			
			df = null;
		}
		else
		{
			NumberFormat nf = NumberFormat.getNumberInstance();
			
			int _maximum = _formatString.length();
			if( _formatString.length() > _value.length() ){
				_maximum = _value.length();
				
				double _maxLenValue=Double.parseDouble(_value);
				int _maxLenValueint=(int) _maxLenValue;
				String _maxLenStr=String.valueOf(_maxLenValueint);
				_maximum=_maxLenStr.length();
			}
			
			if( _maximum > 0 ){
				nf.setMaximumIntegerDigits( _maximum );	
			}
				
			nf.setGroupingUsed(_thousandComma);
			
			if( _isDemecal == true ){
				nf.setMinimumIntegerDigits(_maximum);
			}
			
			try {
				_valueInt = Double.parseDouble(_value);
			} catch (Exception e) {
			}
			_result = nf.format( _valueInt  );
		}

		_result =maskStringFormat(_formatString , _result);
		
		return _result;
		
		*/
	}
	
	public static String maskNumberFormatBigDecimal( String _formatString , int _decimalPoint , Boolean _thousandComma ,  Boolean _isDemecal , String _value ) throws ParseException
	{
		String _formatStr = "";
		String _result  = "";
		int _decimalValue = 0;
		if( _thousandComma )
		{
			_formatStr = "#,##0";
		}
		else
		{
			_formatStr = "0";
		}
		
		if(_value.indexOf(".") > -1)
		{
			_decimalValue= _value.length() - _value.indexOf(".")-1;
		}
		else
		{
			_decimalValue = 0;
		}
		
//		if( !_isDemecal )
//		{
//			_decimalPoint = _decimalValue;
//		}
		
		if( _decimalPoint > 0)
		{
			_formatStr = _formatStr + ".";
			for( int i = 0; i < _decimalPoint; i++ )
			{
				if( _isDemecal ) _formatStr = _formatStr + "0";
				else _formatStr = _formatStr + "#";
			}
		}
		
		try {
			
			if(_value.trim().equals("") == false )
			{
				DecimalFormat _decFm = new DecimalFormat(_formatStr);
				BigDecimal _bigDecimalValue = new BigDecimal( _value.trim() );
				_result = _decFm.format(_bigDecimalValue);
				
				if( _formatString.equals("") == false ) _result =maskStringFormat(_formatString , _result);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
//			e.printStackTrace();
			_result = _value;
			if( _formatString.equals("") == false ) _result =maskStringFormat(_formatString , _result);
			
		}
		
		return _result;
	}
	
	/**
	 * _formatString : format 
	 * _value : data
	 * */
	public static String maskStringFormat( String _formatString , String _value ) throws ParseException
	{
		String _result="";
		
		String[] _valueTemp	= _value.split("");
		String[] _maskTemp	= _formatString.split(""); 
		int _maskLength		= _maskTemp.length;
		int _valueIdx= (_valueTemp[0].equals(""))?1:0;
		
		int _st = (_maskTemp[0].equals(""))?1:0; 
		int i;
		
		Boolean isMasking=false;
		
		for( i=_st; i< _maskLength; i++ ){
			
			if( isMasking == true ){
				isMasking = false;
				_valueIdx++;
			}else{

				
				if( _maskTemp[i].equals("#") ){
					
					if( _valueTemp.length > 0 && _valueTemp.length > _valueIdx ){
						_maskTemp[i] = _valueTemp[_valueIdx];
						_valueIdx++;
					}else{
						_maskTemp[i]="";
					}
				}else if( _maskTemp[i].equals("\\") ){
					isMasking=true;
				}

			}
			
		}
		
		for( String _char : _maskTemp  ){
			if( _char.equalsIgnoreCase("\\") == false ){
				_result += _char;	
			}
		}
		
		return _result;
	}
	
	
	/**
	 * _formatString: format
	 * _nation: 금액 단위
	 * _align: 금액 단위 표시위치
	 * _value: data
	 * */
	public static String currencyFormat( String _formatString , String _nation , String _align , String _value ) throws ParseException
	{
		String _result="";
		
		// data에  ',' 가 존재할 경우 제거.
		String[] _values		= _value.split(",");
		
		String _inputValue	= join(_values, "");
		
		// nation remove
		_inputValue = _inputValue.replace(_nation, "");
		
		// 3자리 ',' 붙이기.
		NumberFormat nf = NumberFormat.getNumberInstance();
		
		int _inputValueInt = 0;
		long _inputValueLong = 0;
		try {
//			_inputValueInt = Integer.parseInt(_inputValue);
//			_inputValueInt = new BigDecimal(_inputValue).intValue();
			_inputValueLong = new BigDecimal(_inputValue).longValue();	// intValue로는 100억 이상 단위를 표현하지 못하므로 long으로 변경함.
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		//_result = nf.format(  _inputValueInt );
		_result = nf.format(  _inputValueLong ); // intValue로는 100억 이상 단위를 표현하지 못하므로 long으로 변경함.
		
		if( _align.equalsIgnoreCase("left") ){
			_result = _nation + _result;
		}else{
			_result =  _result + _nation;
		}
		
	
		return _result;
	}
	
	/**
	 * 
	 * 배열의 요소를 문자열로 변환하고, 
	 * 지정된 분리 기호를 요소 사이에 삽입하고, 
	 * 요소를 서로 연결한 후 결과 문자열을 반환합니다.
	 * 
	 * */
	public static String join( String[] _array , String _sep )
	{
		String _result = "";
		
		int i;
		int _length=_array.length;
		
		for ( i=0; i<_length; i++ ){
			if(  i > 0 ){
				_result += (_sep +_array[i] );	
			}else{
				_result += _array[i];	
			}
		}
		return _result;
	}
	
	
	
	
	
	/**
	 * _formatString : format 
	 * _value : data
	 * */
	public static String maskStringEditFormat( String _formatString , String _value , String _maskChar ) throws ParseException
	{
		String _result="";
		
		String[] _valueTemp	= _value.split("");
		String[] _maskTemp	= _formatString.split(""); 
		int _maskLength		= _maskTemp.length;
		int _valueIdx= (_valueTemp[0].equals(""))?1:0;
		
		int _st = (_maskTemp[0].equals(""))?1:0; 
		int i;
		for( i=_st; i< _maskLength; i++ ){
			
			if( _maskTemp[i].equals("0") || _maskTemp[i].equals("9") || _maskTemp[i].equals("A") || _maskTemp[i].equals("S") ){
//			if( _maskTemp[i].equals(_maskChar) ){ 	// client formatter 변경으로 수정됨. 20171115 kyh
				
				if( _valueTemp.length > 0 && _valueTemp.length > _valueIdx ){
					_maskTemp[i] = _valueTemp[_valueIdx];
					_valueIdx++;
				}else{
					_maskTemp[i]="";
				}
			}
		}
		
		for( String _char : _maskTemp  ){
			_result += _char;
		}
		
		return _result;
	}
	
//	
//	public static String getOnlyData( String _formatString , String _value )
//	{
//		String _result="";
//		
//		String _tempStr="";
//		String _maskStr="";
//		  for( int i=0; i< _formatString.length(); i++ ){
//			  _maskStr = _formatString.substring(i,i+1);
//			  if( _maskStr.equals("*") || _maskStr.equals("a") || _maskStr.equals("9") || _maskStr.equals("A") || _maskStr.equals("0") || _maskStr.equals("S") ){
//				  _tempStr += _value.substring(i,i+1);
//			  }
//		  }
//		  _result=_tempStr;
//		
//		return _result;
//		
//	}
	
	
	
	
	/**
	 * _formatString		: 포멧형식
	 * _decimalPoint		: 소숫점 자리수
	 * _thousandComma		: 천단위 콤마
	 * _isDemecal			: 소숫점 표현
	 * 
	 * */
	public static String maskNumberEditFormat( String _formatString , int _decimalPoint , Boolean _thousandComma ,  Boolean _isDemecal , String _value ) throws ParseException
	{
		String _result="";
		String _changeFormatString="";
		
		_changeFormatString = _formatString.replaceAll("9", "#");
		_result = UBFormatter.maskNumberFormat(_changeFormatString,_decimalPoint,_thousandComma,_isDemecal,_value);
//		
//		
//		
//		double _valueInt=0;
//		
//		if( _value.equalsIgnoreCase("") ){
//			return _result;
//		}
//		
//		if( StringUtil.isDouble(_value) == false )
//		{
//			return _value;
//		}
//		
//		if(_decimalPoint > 0)
//		{
//			String _decimalFormat = "99990.";
//			if(_thousandComma)   _decimalFormat = "9,990.";
//				
//			String _str = "";
//			if(_isDemecal)
//			{
//				_str = "0";
//			}
//			else
//			{
//				_str = "9";
//			}
//			
//			for (int i = 0; i < _decimalPoint; i++) {
//				_decimalFormat = _decimalFormat + _str;
//			}
//			
//			DecimalFormat df=new DecimalFormat( _decimalFormat );
//			
//			try {
//				_valueInt = Double.parseDouble(_value);
//			} catch (Exception e) {
//			}
//			_result = df.format( _valueInt  ); 
//			
//			df = null;
//		}
//		else
//		{
//			NumberFormat nf = NumberFormat.getNumberInstance();
//			
//			int _maximum = _formatString.length();
//			if( _formatString.length() > _value.length() ){
//				_maximum = _value.length();
//				
//				double _maxLenValue=Double.parseDouble(_value);
//				int _maxLenValueint=(int) _maxLenValue;
//				String _maxLenStr=String.valueOf(_maxLenValueint);
//				_maximum=_maxLenStr.length();
//			}
//			
//			if( _maximum > 0 ){
//				nf.setMaximumIntegerDigits( _maximum );	
//			}
//				
//			nf.setGroupingUsed(_thousandComma);
//			
//			if( _isDemecal == true ){
//				nf.setMinimumIntegerDigits(_maximum);
//			}
//			
//			try {
//				_valueInt = Double.parseDouble(_value);
//			} catch (Exception e) {
//			}
//			_result = nf.format( _valueInt  );
//		}
//
//		_result =maskStringEditFormat(_formatString , _result , "9");
		
		return _result;
	}
	
	public static String customDateFormatter( String _inputFormatStr , String _outputFormatStr , String _value ) throws UnsupportedEncodingException
	{
		String _result = "";
		
		if( _value == null || _value.equals("") ) return "";
		
		try {
			SimpleDateFormat format = new SimpleDateFormat( _inputFormatStr );
			Date _date;
			_date = format.parse(_value);
			
			// Excel에서 사용되는 시간 전체 표시 포맷을 일반 포맷형태로 변경작업
			Pattern _pattern = Pattern.compile("\\[(h*)\\]");
			Matcher _m = _pattern.matcher(_outputFormatStr);
			
			if( _m.find() )
			{
				// "[h] 가 존재할경우 해당 date를 calender객체를 이용하여 hour:"
				long _milTime = _date.getTime() +  format.getTimeZone().getRawOffset();
				long _hour = _milTime / 1000 / 60 / 60;
				
				String _patStr = _m.group();
//				String _replaceStr = _patStr.replace("[", "").replace("]", "").toUpperCase();
				String _replaceStr = String.valueOf(_hour);
				
				if( _replaceStr.equals("") == false ) _outputFormatStr = _outputFormatStr.replace( _patStr , _replaceStr );
			}
//			if( _outputFormatStr.indexOf("[h]") != -1 )
//			{
//				_outputFormatStr = _outputFormatStr.replace("[h]", "H");
//			}
//			else if( _outputFormatStr.indexOf("[hh]") != -1 )
//			{
//				_outputFormatStr = _outputFormatStr.replace("[hh]", "HH");
//			}
			
			SimpleDateFormat _outputDateForamt = new SimpleDateFormat(_outputFormatStr);
			_result = _outputDateForamt.format(_date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return _result;
	}
	
	
}
