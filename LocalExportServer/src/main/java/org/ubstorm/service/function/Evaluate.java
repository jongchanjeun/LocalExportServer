package org.ubstorm.service.function;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Evaluate {

	/**
	 * 합계 return
	 * */
	public static float sum( float[] _ar )
	{
		float _result=0;
		
		for( float _value : _ar ){
			_result += _value;
		}
		
		return _result;
	}
	
	
	public static BigDecimal sum2( String[] _ar )
	{
		BigDecimal d = new BigDecimal( 0 );
		
		for( String _value : _ar ){
			if(_value.equals("")) _value = "0";
			try {
				BigDecimal d2 = new BigDecimal( _value.trim() );
				d = d.add(d2);
			} catch (Exception e) {
				// TODO: handle exception
				d = d.add( new BigDecimal(0));
			}
			//_result += (double)_value;
		}
		return d;
	}
	
	
	/**
	 * 평균 return
	 * */
	public static BigDecimal avg( String[] _ar )
	{
//		float _result = sum(_ar)/_ar.length;
		
		BigDecimal _value = sum2(_ar);
		BigDecimal _divide=new BigDecimal( _ar.length );
		
		try {
			_value =_value.divide(_divide  );	
		} catch (Exception e) {
			_value =_value.divide(_divide , 2 , RoundingMode.CEILING  );	
		}
		
		return _value;
	}
	
	/**
	 * 큰값을 return
	 * */
	public static String max( String[] _ar )
	{
		String _result="0";
		
		for( String _value : _ar ){
			if( Double.valueOf( _result ) < Double.valueOf( _value ) ) _result =  _value;
		}
		
		return _result;
	}
	
	
	/**
	 * 작은값을 return
	 * */
	public static String min( String[] _ar )
	{
		String _result="0";
		
		for( String _value : _ar ){
			if( Double.valueOf(_result) == 0 || Double.valueOf(_result) > Double.valueOf(_value) ) _result = _value;
		}
		
		return _result;
	}
	
	/**
	 * 반올림 값을 반환
	 * */
	public static double round( double _value )
	{
		double _result=0;
		
		_result = Math.round(_value); 
		
		return _result;
	}
	
	/**
	 * 올림 값을 반환
	 * */
	public static double ceil( double _value )
	{
		double _result=0;
		
		_result = (double) Math.ceil(_value);
		
		return _result;
	}
	
	/**
	 * 내림값을 반환
	 * */
	public static double floor( double _value )
	{
		double _result=0;
		
		_result = (double) Math.floor(_value);
		
		return _result;
	}
	
	/**
	 * 문자열을 지정된 index만큼 잘라서 반환
	 * */
	public static String substr( String _str , int _start , int _count )
	{
		String _result="";

		int _startIndex=_start;
		//int _len= _startIndex + _count;	// 평화정공에서 안나와서 수정함.
		int _len=_count;
		
		if( _str.length() >= _len ){
			_result = _str.substring(_start, _len);
		}else{
			_result="";
		}
		
		return _result;
	}
	
	/**
	 * 문자열을 지정된 문자로 변환하여 반환
	 * */
	public static String replace( String _str , String _replaceStr , String _newStr )
	{
		String _result="";
		
		_result = _str.replace(_replaceStr, _newStr);
		
		return _result;
	}
	
	
	/**
	 * date format 을 java에 맞게 바꿔준다.
	 * */
	public static String convertDateFormatType( String _format )
	{
		String _result="";
		
		if( _format.equalsIgnoreCase("YYYY-MM-DD") ){
			_result	=	"yyyy-MM-dd";
		}else if( _format.equalsIgnoreCase("YYYYMMDD") ){
			_result	=	"yyyyMMdd";
		}else if( _format.equalsIgnoreCase("YYMMDD") ){
			_result	=	"yyMMdd";
		}else{
			// 지원하는 포멧인 아닙니다. default 지정
			_result	=	"yyyyMMdd";
		}
		
		return _result;
	}
	
	/**
	 * date format 을 java에 맞게 바꿔준다.
	 * */
	public static String convertDateFormatTypeOriginal( String _format )
	{
		String _result="";
		
		if( _format.equalsIgnoreCase("YYYY-MM-DD") ){
			_result	=	"yyyy-MM-dd";
		}else if( _format.equalsIgnoreCase("YYYYMMDD") ){
			_result	=	"yyyyMMdd";
		}else if( _format.equalsIgnoreCase("YYMMDD") ){
			_result	=	"yyMMdd";
		}else{
			// 지원하는 포멧인 아닙니다. default 지정
			_result	=	_format;
		}
		
		return _result;
	}
	
	
	/**
	 * 두개의 날짜값의 차이를 일수 단위로 반환
	 * */
	public static long dayDiff( String _start , String _end , String _format ) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format );
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );

	    Date beginDate=new Date();
	    Date endDate=new Date();
		try {
		    beginDate = formatter.parse(_start);
		    endDate = formatter.parse(_end);
		} catch (Exception e) {
			return 0;
		}

	 
	    long diff = endDate.getTime() - beginDate.getTime();
	    long diffDays = diff / (24 * 60 * 60 * 1000);
	 
	    return diffDays;
	}
	
	
	/**
	 * 두개의 날짜값의 차이를 초단위로 반환
	 * */
	public static long secondDiff( String _start , String _end , String _format ) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format );
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );
		 
		Date beginDate = new Date();
		Date endDate = new Date();
		
		try {
		    beginDate = formatter.parse(_start);
		    endDate = formatter.parse(_end);
		} catch (Exception e) {
			return 0;
		}
	 
	    long diff = endDate.getTime() - beginDate.getTime();
	    long diffDays = diff / ( 1000);
	 
	    return diffDays;
	}
	
	
	/**
	 * 두개의 날짜값의 차이를 분 단위로 반환
	 * */
	public static long minuteDiff( String _start , String _end , String _format ) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format );
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );
		 

	    Date beginDate = new Date();
	    Date endDate = new Date();

		try{
		    beginDate = formatter.parse(_start);
		    endDate = formatter.parse(_end);
		}catch(Exception e){
			return 0;
		}
	 
	    long diff = endDate.getTime() - beginDate.getTime();
	    long diffDays = diff / ( 1000 * 60);
	 
	    return diffDays;

	}
	
	/**
	 * 두개의 날짜값의 차이를 시간단위로 반환
	 * */
	public static long hourDiff(  String _start , String _end , String _format ) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format );
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );

	    Date beginDate = new Date();
	    Date endDate = new Date();

		try{
		    beginDate = formatter.parse(_start);
		    endDate = formatter.parse(_end);
		}catch(Exception e){
			return 0;
		}
	 
	    long diff = endDate.getTime() - beginDate.getTime();
	    long diffDays = diff / ( 1000 * 60 * 60);
	 
	    return diffDays;

	}
	
	/**
	 * 두개의 날짜값의 차이를 주 단위로 반환
	 * */
	public static long weekDiff(String _start , String _end , String _format ) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format );
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );

	    Date beginDate = new Date();
	    Date endDate = new Date();

		try{
		    beginDate = formatter.parse(_start);
		    endDate = formatter.parse(_end);
		}catch(Exception e){
			return 0;
		}
	 
	    long diff = endDate.getTime() - beginDate.getTime();
	    long diffDays = diff / ( 1000 * 60 * 60 * 24) / 7;
	 
	    return diffDays;
	}
	
	/**
	 * 두개의 날짜값의 차이를 월 단위로 반환
	 * */
	public static long monthDiff(String _start , String _end , String _format ) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format );
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );

	    Date beginDate = new Date();
	    Date endDate = new Date();

		try{
		    beginDate = formatter.parse(_start);
		    endDate = formatter.parse(_end);
		}catch(Exception e){
			return 0;
		}
	    
	    @SuppressWarnings("deprecation")
		int nYear	=endDate.getYear() - beginDate.getYear();
	    
	    @SuppressWarnings("deprecation")
		int nMonth	=endDate.getMonth() - beginDate.getMonth();
	    
	    @SuppressWarnings("deprecation")
		int nDay	=endDate.getDate()	- beginDate.getDate();
	    
	    long _ret	=(nYear * 12 + nMonth + (nDay >= 0 ? 0 : -1) );
	 
	    return _ret;
	}
	
	
	/**
	 * 두개의 날짜값의 차이를 년 단위로 반환
	 * */
	public static long yearDiff(String _start , String _end , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format );
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );

	    Date beginDate = new Date();
	    Date endDate = new Date();

		try{
		    beginDate = formatter.parse(_start);
		    endDate = formatter.parse(_end);
		}catch(Exception e){
			return 0;
		}
	    
	    @SuppressWarnings("deprecation")
		long _ret	= endDate.getYear() - beginDate.getYear();
	 
	    return _ret;
	}
	
	/**
	 * 이 인스턴스가 나타내는 일 수를 정수로 가져옵니다.
	 * */
	@SuppressWarnings("deprecation")
	public static long dayOfYear(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );

	    Date endDate = new Date();
	    Date beginDate = new Date();
		
	    try{
		    endDate = formatter.parse(_date);
		    beginDate = formatter.parse(_date);
	    }catch(Exception e){
	    	return 0;
	    }
	    beginDate.setMonth(0);
	    beginDate.setDate(1);

	    long diff = endDate.getTime() - beginDate.getTime();
	    long diffDays = diff / ( 1000 * 60 * 60 * 24);
	 
	    return diffDays;	
	}
	
	
	/**
	 * 이 인스턴스가 나타내는 주 수를 정수로 가져옵니다.
	 * */
	@SuppressWarnings("deprecation")
	public static long weekOfYear(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );
		
	    Date endDate = new Date();
	    Date beginDate = new Date();

		try{
		    endDate = formatter.parse(_date);
		    beginDate = formatter.parse(_date);
		}catch(Exception e){
			return 0;
		}
	    beginDate.setMonth(0);
	    beginDate.setDate(1);

	    long diff = endDate.getTime() - beginDate.getTime();
	    long diffDays = diff / ( 1000 * 60 * 60 * 24)/7;
	 
	    return diffDays;	
	}
	
	/**
	 * 년도를 반환
	 * */
	public static long year(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );
		 
		Date endDate = new Date();
		try{
			endDate = formatter.parse(_date);	
		}catch(Exception e){
			return 0;
		}
		
        Calendar c = Calendar.getInstance();
        c.setTime(endDate);
        
        int ret = c.get(Calendar.YEAR);

        return ret;	
	}
	
	/**
	 * _date의 월을 반환
	 * */
	public static long month(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );

		Date endDate = new Date();
		try{
			endDate = formatter.parse(_date);	
		}catch(Exception e){
			return 0;
		}
		
        Calendar c = Calendar.getInstance();
        c.setTime(endDate);
        
        int ret = c.get(Calendar.MONTH) + 1;

        return ret;	

	}
	
	
	
	/**
	 * _date의 월을 영문으로 반환
	 * */
	public static String monthEn(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );

		Date endDate = new Date();
		try{
			endDate = formatter.parse(_date);	
		}catch(Exception e){
			return "";
		}
		
        Calendar c = Calendar.getInstance();
        c.setTime(endDate);
        
        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};

        int _monthInt = c.get(Calendar.MONTH);
        
        String ret = monthNames[_monthInt];
        
        return ret;	

	}
	
	
        
	/**
	 * _date의 마지막 일을 반환
	 * */
	public static long dayoflast(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );
		 
	    Date endDate = new Date();
	    try{
	    	endDate = formatter.parse(_date);	
	    }catch(Exception e){
	    	return 0;
	    }
		
        Calendar c = Calendar.getInstance();
        c.setTime(endDate);
        
        int ret = c.getActualMaximum(Calendar.DAY_OF_MONTH);

        return ret;	

	}
        
	
	/**
	 * _date의 일을 반환
	 * */
	public static long day(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );
		 
	    Date endDate = new Date();
	    try{
	    	endDate = formatter.parse(_date);	
	    }catch(Exception e){
	    	return 0;
	    }
		
        Calendar c = Calendar.getInstance();
        c.setTime(endDate);
        
        int ret = c.get(Calendar.DAY_OF_MONTH);

        return ret;	

	}
	
	/**
	 * date의 요일을 반환
	 * */
	public static String week(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );

		Date endDate = new Date();
		try{
			endDate = formatter.parse(_date);	
		}catch(Exception e){
			return "";
		}
	    
	    @SuppressWarnings("deprecation")
		int _day	= endDate.getDay();
	    
	    String _ret="";
	    
	    switch( _day )
	    {
			case 0:
				_ret="Sunday";
				break;
			case 1:
				_ret="Monday";
				break;
			case 2:
				_ret="Tuesday";
				break;
			case 3:
				_ret="Wednesday";
				break;
			case 4:
				_ret="Thursday";
				break;
			case 5:
				_ret="Friday";
				break;
			case 6:
				_ret="Saturday";
				break;
			case 7:
				_ret="Sunday";
				break;
			default:
				_ret="fail";
				break;
	    }
		
		return _ret;
	}
	
	/**
	 * date의 요일을 반환
	 * */
	public static String weekKr(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );
		 
	    Date endDate = new Date();
	    try{
	    	endDate = formatter.parse(_date);	
	    }catch( Exception e ){
	    	return "";
	    }
	    
	    
	    @SuppressWarnings("deprecation")
		int _day	= endDate.getDay();
	    
	    String _ret="";
	    
	    switch( _day )
	    {
			case 0:
				_ret="일";
				break;
			case 1:
				_ret="월";
				break;
			case 2:
				_ret="화";
				break;
			case 3:
				_ret="수";
				break;
			case 4:
				_ret="목";
				break;
			case 5:
				_ret="금";
				break;
			case 6:
				_ret="토";
				break;
			case 7:
				_ret="일";
				break;
			default:
				_ret="fail";
				break;
	    }
		
		return _ret;
	}
	
	/**
	 * date의 시간을 반환
	 * */
	public static long time(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );
		 
	    Date endDate = new Date();
	    try{
	    	endDate = formatter.parse(_date);	
	    }catch(Exception e){
	    	return 0;
	    }
	    
	    @SuppressWarnings("deprecation")
		long _ret	=	endDate.getHours();	
		
		return _ret;
	}
	
	/**
	 * date의 분을 반환
	 * */
	public static long minute(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );
		 
	    Date endDate = new Date();
	    try{
	    	endDate = formatter.parse(_date);	
	    }catch(Exception e){
	    	return 0;
	    }
	    
	    @SuppressWarnings("deprecation")
		long _ret	=	endDate.getMinutes();
		
		return _ret;
	}
	
	/**
	 * date의 초를 반환
	 * */
	public static long second(String _date , String _format) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );
		 
	    Date endDate = new Date();
	    try{
	    	endDate = formatter.parse(_date);	
	    }catch( Exception e ){
	    	return 0;
	    }
	    
	    @SuppressWarnings("deprecation")
		long _ret	=	endDate.getSeconds();
		
		return _ret;
	}
	
	
	/**
	 * 현재 시간을 시분초(HHMMSS)로 변환
	 * */
	public static String now() throws ParseException
	{
		String _ret="";
	    Date endDate = new Date();
		String _hour=endDate.getHours() < 10 ? "0" + String.valueOf(endDate.getHours() ) : String.valueOf(endDate.getHours() );
		String _min=endDate.getMinutes() < 10 ? "0" + String.valueOf(endDate.getMinutes() ) : String.valueOf(endDate.getMinutes() );
		String _sec=endDate.getSeconds() < 10 ? "0" + String.valueOf(endDate.getSeconds() ) : String.valueOf(endDate.getSeconds() ); 
	    _ret = _hour + _min + _sec;
	    
		return _ret;
	}
	
	
	
	/**
	 * today
	 * */
	public static String toDay() throws ParseException
	{
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		Calendar calobj = Calendar.getInstance();
		
		String _ret="";
	    _ret = df.format(calobj.getTime());
	    
		return _ret;
	}
	
	
	/**
	 * _date의 일을 반환
	 * */
	public static String dateCalculation(String _date ,int _addDate, String _format, String _resultFormat, int _addType  ) throws ParseException
	{
		String _cFormat = convertDateFormatType( _format ); 
		String _rFormat = convertDateFormatType( _resultFormat ); 
		SimpleDateFormat formatter = new SimpleDateFormat( _cFormat );
		 
	    Date endDate = new Date();
	    try{
	    	endDate = formatter.parse(_date);	
	    }catch(Exception e){
	    	return "";
	    }
	    
        Calendar c = Calendar.getInstance();
        c.setTime(endDate);
        
        c.add(_addType, _addDate);
        
        DateFormat df = new SimpleDateFormat(_rFormat);
        String resultDate = df.format(c.getTime());

        return resultDate;	

	}
	
	
}
