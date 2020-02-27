package org.ubstorm.service.utils;

import java.text.ParseException; 
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

	private static final String dateToString(Calendar c){

		int y = c.get(Calendar.YEAR);
		int m = c.get(Calendar.MONTH)+1;
		int d = c.get(Calendar.DATE);

		return new StringBuffer(y).append(y).append("-").append(((m < 10 ) ? "0"+m : m+"" )).
								   append("-").append(((d < 10 ) ? "0"+d : d+"" )).toString();
	}

	public static final String getDate(){
		return dateToString(Calendar.getInstance());
	}

	public static String getDateString() {
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.KOREA);
		return formatter.format(new java.util.Date());
	}

	public static final String getBeforeMonth(){

		String YYYYMM = StringUtil.replace(getDate(),"-","");

		int year   = StringUtil.isInteger(YYYYMM.substring(0,4))?Integer.parseInt(YYYYMM.substring(0,4)):0;
		int month = StringUtil.isInteger(YYYYMM.substring(4,6))?Integer.parseInt(YYYYMM.substring(4,6)):0;

		String beforeMonth = "";

		if(year <= 0 || month <= 0 || month > 12) {
			return YYYYMM;
		}else{
			if (month == 1) {
				year -= 1;
				month = 12;
			}else{
				month -= 1 ;
			}
			if (month < 10) {
				beforeMonth = Integer.toString(year) + "0"+ Integer.toString(month);
			}else{
				beforeMonth = Integer.toString(year) + Integer.toString(month);
			}
		}

		return beforeMonth;
	}

	public static final String getBeforeMonth(String YYYYMM){

		int year   = StringUtil.isInteger(YYYYMM.substring(0,4))?Integer.parseInt(YYYYMM.substring(0,4)):0;
		int month = StringUtil.isInteger(YYYYMM.substring(4,6))?Integer.parseInt(YYYYMM.substring(4,6)):0;

		String beforeMonth = "";

		if(year <= 0 || month <= 0 || month > 12) {
			return YYYYMM;
		}else{
			if (month == 1) {
				year -= 1;
				month = 12;
			}else{
				month -= 1 ;
			}
			if (month < 10) {
				beforeMonth = Integer.toString(year) + "0"+ Integer.toString(month);
			}else{
				beforeMonth = Integer.toString(year) + Integer.toString(month);
			}
		}

		return beforeMonth;
	}

	public static final String getBeforeMonth(String YYYYMM,
	                                          int iBefore){

		int year   = StringUtil.isInteger(YYYYMM.substring(0,4))?Integer.parseInt(YYYYMM.substring(0,4)):0;
		int month = StringUtil.isInteger(YYYYMM.substring(4,6))?Integer.parseInt(YYYYMM.substring(4,6)):0;

		String beforeMonth = "";

		if(year <= 0 || month <= 0 || month > 12) {
			return YYYYMM;
		}else{
			int yearBefore = iBefore/12;
			int monthBefore = iBefore%12;
			year -= yearBefore;
			if (month <= monthBefore) {
				year -= 1;
				month =12-(monthBefore-month);
			}else{
				month -= monthBefore;
			}
			if (month < 10) {
				beforeMonth = Integer.toString(year) + "0"+ Integer.toString(month);
			}else{
				beforeMonth = Integer.toString(year) + Integer.toString(month);
			}
		}

		return beforeMonth;
	}

	public static final String getNextMonth(){

		String YYYYMM = StringUtil.replace(getDate(),"-","");

		int year   = StringUtil.isInteger(YYYYMM.substring(0,4))?Integer.parseInt(YYYYMM.substring(0,4)):0;
		int month = StringUtil.isInteger(YYYYMM.substring(4,6))?Integer.parseInt(YYYYMM.substring(4,6)):0;

		String nextMonth = "";

		if(year <= 0 || month <= 0 || month > 12) {
			return YYYYMM;
		}else{
			if (month == 12) {
				year += 1;
				month = 1;
			}else{
				month += 1 ;
			}
			if (month < 10) {
				nextMonth = Integer.toString(year) + "0"+ Integer.toString(month);
			}else{
				nextMonth = Integer.toString(year) + Integer.toString(month);
			}
		}

		return nextMonth;
	}

	public static final String getNextMonth(String YYYYMM){

		int year   = StringUtil.isInteger(YYYYMM.substring(0,4))?Integer.parseInt(YYYYMM.substring(0,4)):0;
		int month = StringUtil.isInteger(YYYYMM.substring(4,6))?Integer.parseInt(YYYYMM.substring(4,6)):0;

		String nextMonth = "";

		if(year <= 0 || month <= 0 || month > 12) {
			return YYYYMM;
		}else{
			if (month == 12) {
				year += 1;
				month = 1;
			}else{
				month += 1 ;
			}
			if (month < 10) {
				nextMonth = Integer.toString(year) + "0"+ Integer.toString(month);
			}else{
				nextMonth = Integer.toString(year) + Integer.toString(month);
			}
		}

		return nextMonth;
	}

	public final static String getDateAfter(String date, int plus) throws Exception {

		Calendar c = new java.util.GregorianCalendar(TimeZone.getTimeZone("JST"), Locale.KOREAN);
		c.setTime(new java.text.SimpleDateFormat("yyyy/MM/dd").parse(StringUtil.replace(date, "-", "/")));
		c.set(Calendar.DATE, c.get(Calendar.DAY_OF_MONTH) + plus);

		return dateToString(c);
	}

	public final static String getDateBefore(String date, int minus) throws Exception {

		Calendar c = new java.util.GregorianCalendar(TimeZone.getTimeZone("JST"), Locale.KOREAN);
		c.setTime(new java.text.SimpleDateFormat("yyyy/MM/dd").parse(StringUtil.replace(date, "-", "/")));
		c.set(Calendar.DATE, c.get(Calendar.DAY_OF_MONTH) - minus);

	return dateToString(c);
	}

	public final static int getDateDifference(String date1,
											  String date2)
											  throws Exception {
		long bigNum = new java.text.SimpleDateFormat("yyyy/MM/dd").parse(StringUtil.replace(date1, "-", "/")).getTime();
		long smallNum = new java.text.SimpleDateFormat("yyyy/MM/dd").parse(StringUtil.replace(date2, "-", "/")).getTime();

		/* 1일 = 86400000 ms */
		if(bigNum < smallNum) {
			return (int)((bigNum - smallNum)/86400000) * -1;
		} else {
			return (int)((bigNum - smallNum)/86400000);
		}
	}

	public final static String getDateFromTodayAfter(int plus) throws ParseException {

		Calendar c = new java.util.GregorianCalendar(TimeZone.getTimeZone("JST"), Locale.KOREAN);
		c.set(Calendar.DATE, c.get(Calendar.DAY_OF_MONTH) + plus);

		return dateToString(c);
	} 
 
	public final static String getDateFromTodayBefore(int minus) throws ParseException {

		Calendar c = new java.util.GregorianCalendar(TimeZone.getTimeZone("JST"), Locale.KOREAN);
		c.set(Calendar.DATE, c.get(Calendar.DAY_OF_MONTH) - minus);

		return dateToString(c);
	}

	public static final String getTime() {

		Calendar c = Calendar.getInstance();

			int yy = c.get(Calendar.YEAR);
			int mm = c.get(Calendar.MONTH)+1;
			int dd = c.get(Calendar.DATE);

			int ap = c.get(Calendar.AM_PM);
			int hh = c.get(Calendar.HOUR);
			int m  = c.get(Calendar.MINUTE);

		return new StringBuffer().append(yy).append("-").append(((mm < 10 ) ? "0"+mm : mm+"")).
							  append("-").append(((dd < 10 ) ? "0"+dd : dd+"")).
							  append(" ").append(((ap == Calendar.AM) ? "AM" : "PM")).
							  append(" ").append(((hh < 10) ? "0"+hh : hh+"")).
							  append(":").append(((m < 10) ? "0"+m : m+"")).toString();
	}

	public static final String getDateTime() {

		Calendar c = Calendar.getInstance();

			int yy = c.get(Calendar.YEAR);
			int mm = c.get(Calendar.MONTH)+1;
			int dd = c.get(Calendar.DATE);

			int hh = c.get(Calendar.HOUR);
			int m  = c.get(Calendar.MINUTE);
			int s  = c.get(Calendar.SECOND);

		return new StringBuffer().append(yy).append(((mm < 10 ) ? "0"+mm : mm+"")).
							  append(((dd < 10 ) ? "0"+dd : dd+"")).
							  append(((hh < 10) ? "0"+hh : hh+"")).
							  append(((m < 10) ? "0"+m : m+"")).
							  append(((s < 10) ? "0"+s : s+"")).toString();
	}
	
	public static Date getDate(String strDate) {
		if (strDate == null)
			return null;

		if (!(strDate.length() == 8 || strDate.length() == 14)) {
			return null;
		}
		Date date = null;
		
		try {
			String newDate = getDateTimeString(strDate, "-");
			String format = "yyyy-MM-dd";
			if (strDate.length() == 14) {
				format = "yyyy-MM-dd HH:mm:ss";
			}

			java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(format, java.util.Locale.KOREA);
			date = formatter.parse(newDate);

		} catch (Exception e) {
			return null;
		}
		return date;
	}

	public static String getDateTimeString(String strDate, String s) {
		if (strDate == null)
			return "";

		if (!(strDate.length() == 8 || strDate.length() == 14)) {
			return strDate;
		}

		StringBuffer sbufferFormattedDay = new StringBuffer("");

		sbufferFormattedDay.append(strDate.substring(0, 4));
		sbufferFormattedDay.append(s);

		String strMonth = strDate.substring(4, 6);
		sbufferFormattedDay.append(strMonth);
		sbufferFormattedDay.append(s);

		String strDay = strDate.substring(6, 8);
		sbufferFormattedDay.append(strDay);
		if (strDate.length() == 14) {
			sbufferFormattedDay.append(" ");
			sbufferFormattedDay.append(strDate.substring(8, 10));
			sbufferFormattedDay.append(":");
			sbufferFormattedDay.append(strDate.substring(10, 12));
			sbufferFormattedDay.append(":");
			sbufferFormattedDay.append(strDate.substring(12, 14));
		}

		return sbufferFormattedDay.toString();
	}

	public static String moveDay(String strCurDate, int day) throws IllegalAccessException {

		if (strCurDate == null || !(strCurDate.length() == 8 || strCurDate.length() == 14)) {
			throw new IllegalAccessException();
		}

		int iYear = Integer.parseInt(strCurDate.substring(0, 4));
		int iMonth = Integer.parseInt(strCurDate.substring(4, 6)) - 1; 
		int iDay = Integer.parseInt(strCurDate.substring(6, 8));

		int iHour = 0;
		int iMin = 0;
		int iSecond = 0;
		String pattern = "yyyyMMdd";
		if (strCurDate.length() == 14) {
			iHour = Integer.parseInt(strCurDate.substring(8, 10));
			iMin = Integer.parseInt(strCurDate.substring(10, 12));
			iSecond = Integer.parseInt(strCurDate.substring(12, 14));
			pattern = "yyyyMMddHHmmssSSS";
		}

		Calendar calCurDate = new GregorianCalendar();
		calCurDate.set(iYear, iMonth, iDay, iHour, iMin, iSecond);

		int iReturnMonth = calCurDate.get(Calendar.DATE) + day;

		calCurDate.set(Calendar.DATE, iReturnMonth);
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, java.util.Locale.KOREA);

		String strResult = formatter.format(calCurDate.getTime());

		if (strResult.length() >= strCurDate.length()) {
			strResult = strResult.substring(0, strCurDate.length());
		}

		return strResult;
	}

	public static String moveMonth(String strCurDate, int mon) throws IllegalAccessException {

		if (strCurDate == null || !(strCurDate.length() == 8 || strCurDate.length() == 14)) {
			throw new IllegalAccessException();
		}

		int iYear = Integer.parseInt(strCurDate.substring(0, 4));
		int iMonth = Integer.parseInt(strCurDate.substring(4, 6)) - 1;
		int iDay = Integer.parseInt(strCurDate.substring(6, 8));

		int iHour = 0;
		int iMin = 0;
		int iSecond = 0;
		String pattern = "yyyyMMdd";
		if (strCurDate.length() == 14) {
			iHour = Integer.parseInt(strCurDate.substring(8, 10));
			iMin = Integer.parseInt(strCurDate.substring(10, 12));
			iSecond = Integer.parseInt(strCurDate.substring(12, 14));
			pattern = "yyyyMMddHHmmssSSS";
		}

		Calendar calCurDate = new GregorianCalendar();
		calCurDate.set(iYear, iMonth, iDay, iHour, iMin, iSecond);

		int iReturnMonth = calCurDate.get(Calendar.MONTH) + mon;

		calCurDate.set(Calendar.MONTH, iReturnMonth);
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, java.util.Locale.KOREA);

		String strResult = formatter.format(calCurDate.getTime());

		if (strCurDate.length() == 8) {
			strResult = strResult.substring(0, 8);
		}

		return strResult;
	}

	public static Calendar getCalendar(String date) {
		return getCalendarFromString(date);
	}

	private static Calendar getCalendarFromString(String date) {
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(4, 6)) - 1, Integer.parseInt(date
				.substring(6, 8)));
		return cal;
	}
}