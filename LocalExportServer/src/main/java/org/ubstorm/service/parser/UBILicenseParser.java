package org.ubstorm.service.parser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONObject;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.utils.common;

public class UBILicenseParser {

	/****************************************************************
	 * Function Name : licenseParser
	 * parameter	 : _license   (라이센스 스트링) , _ip (접속된 서버 ip address), _cpuCnt (cpu수), _userCnt(사용자수)
	 * return value  : Object
	 *                 result : success / fail  (파싱 처리 결과)
	 *                 fail_msg : fail 일때 사유 메세지  
	 *                 ubiform_type : all / report / eform
	 *                 license_type : trial / demo / free  --> 이 결과에 따라서 에디터와 뷰어의 바탕 워터마크 처리
	 *                 limit_type   : none / time / ip / user / all
	 *                 query_type : true/false
	 * *************************************************************/	
	public static JSONObject licenseParser(String _license, String _ip)
	{
		JSONObject _retObj = new JSONObject();
		String _decLicense = "";
		try {
			//복호화.
			//_license = zipDataUtil.decryptString(_license);
			_decLicense = common.base64_decode_uncompress(_license, "UTF-8");
			
			if(!"UBIFORM-LICENSE-UBSTORM-01038687454".equals(_decLicense.substring(0, 35)))
			{
				_retObj.put("result", "fail");
				_retObj.put("fail_msg", "정상적인 라이센스 파일이 아닙니다!-34");
				return _retObj;
			}
		} catch (Exception err) {
			_retObj.put("result", "fail");
			_retObj.put("fail_msg", "정상적인 라이센스 파일이 아닙니다!-39");
			return _retObj;
		}		
		
		String _list [] = {""};		
		_list = _decLicense.split("/");
					
		for(int i=0; i < _list.length ; i++)
		{
			switch(i)
			{
				case 1: //ip address
					_retObj.put("ip", _list[i].toString());
					break;
				case 2: //start date
					_retObj.put("startDate", _list[i].toString());
					break;
				case 3: //end date
					_retObj.put("endDate", _list[i].toString());
					break;
				case 4: //user count
					_retObj.put("userCount", _list[i].toString());
					break;
				case 5: //ubiform type
					_retObj.put("ubiform_type", _getUbiformType(_list[i].toString()));
					break;
				case 6: //license type
					_retObj.put("license_type", _getLicenseType(_list[i].toString()));
					break;
				case 7: //limit type
					_retObj.put("limit_type", _getLimitType(_list[i].toString()));
					break;
				case 8: //site name
					_retObj.put("site_name",  _list[i].toString());
					break;
				case 9: //담당자 명
					_retObj.put("user_name", _list[i].toString());
					break;
				case 10: //라이센스 키
					_retObj.put("key", _list[i].toString());
					break;
				case 11: //쿼리 에디터 사용 유무
					_retObj.put("query_type", _list[i].toString());
					break;

				case 12: //플래쉬 뷰어 사용 유무
					_retObj.put("flash_viewer", _list[i].toString());
					break;
				case 13: //HTML 뷰어 사용 유무
					_retObj.put("html_viewer", _list[i].toString());
					break;
				case 14: //모바일 서포트 사용 유무
					_retObj.put("mobile_support", _list[i].toString());
					break;
				case 15: //버전 정보
					_retObj.put("version", _list[i].toString());
					break;
				case 16: //발급일
					_retObj.put("regDate", _list[i].toString());
					break;
				case 17: //cpu count
					_retObj.put("cpuCount", _list[i].toString());
					break;	
			}
		}
		
		//시간 체크
		if( "9".equals(_retObj.get("limit_type")) || "1".equals(_retObj.get("limit_type")) )
		{
			//현재 시간 구하기
			String _nowDate = getCurrentDate();
			
			if( _nowDate.compareTo((String)_retObj.get("startDate")) < 0 || _nowDate.compareTo((String)_retObj.get("endDate")) > 0 )
			{
				_retObj.put("result", "fail");
				_retObj.put("fail_msg", "라이센스 적용 기간이 아닙니다!");	
				return _retObj;
			}
		}
		
		//ip 체크
		if( "9".equals(_retObj.get("limit_type")) || "2".equals(_retObj.get("limit_type")) )
		{
			if( !_ip.equals(_retObj.get("ip")) )
			{
				_retObj.put("result", "fail");
				_retObj.put("fail_msg","라이센스가 발급된 서버가 아닙니다!");	
				return _retObj;
			}
		}
		
		_retObj.put("result", "success");
		_retObj.put("fail_msg", "");
		
		
		return _retObj;
	}
	
	
	/****************************************************************
	 * Function Name : licenseParser2
	 * parameter	 : _license   (라이센스 스트링) , _ip (접속된 서버 ip address), _cpuCnt (cpu수), _userCnt(사용자수)
	 * return value  : Object
	 *                 result : success / fail  (파싱 처리 결과)
	 *                 fail_msg : fail 일때 사유 메세지  
	 *                 ubiform_type : all / report / eform
	 *                 license_type : trial / demo / free  --> 이 결과에 따라서 에디터와 뷰어의 바탕 워터마크 처리
	 *                 limit_type   : none / time / ip / user / all
	 *                 query_type : true/false
	 * *************************************************************/	
	public static JSONObject licenseParser2(String _license, String _ip, int _cpuCnt, int _userCnt)
	{
		JSONObject _retObj = new JSONObject();
		String _decLicense = "";
		try {
			//복호화.
			//_license = zipDataUtil.decryptString(_license);
			_decLicense = common.base64_decode_uncompress(_license, "UTF-8");
			
			if(!"UBIFORM-LICENSE-UBSTORM-01038687454".equals(_decLicense.substring(0, 35)))
			{
				_retObj.put("result", "fail");
				_retObj.put("fail_msg", "정상적인 라이센스 파일이 아닙니다!-34");
				return _retObj;
			}
		} catch (Exception err) {
			_retObj.put("result", "fail");
			_retObj.put("fail_msg", "정상적인 라이센스 파일이 아닙니다!-39");
			return _retObj;
		}		
		
		String _list [] = {""};		
		_list = _decLicense.split("/");
					
		for(int i=0; i < _list.length ; i++)
		{
			switch(i)
			{
				case 1: //ip address
					_retObj.put("ip", _list[i].toString());
					break;
				case 2: //start date
					_retObj.put("startDate", _list[i].toString());
					break;
				case 3: //end date
					_retObj.put("endDate", _list[i].toString());
					break;
				case 4: //user count
					_retObj.put("userCount", _list[i].toString());
					break;
				case 5: //ubiform type
					_retObj.put("ubiform_type", _getUbiformType(_list[i].toString()));
					break;
				case 6: //license type
					_retObj.put("license_type", _getLicenseType(_list[i].toString()));
					break;
				case 7: //limit type
					_retObj.put("limit_type", _getLimitType(_list[i].toString()));
					break;
				case 8: //site name
					_retObj.put("site_name",  _list[i].toString());
					break;
				case 9: //담당자 명
					_retObj.put("user_name", _list[i].toString());
					break;
				case 10: //라이센스 키
					_retObj.put("key", _list[i].toString());
					break;
				case 11: //쿼리 에디터 사용 유무
					_retObj.put("query_type", _list[i].toString());
					break;

				case 12: //플래쉬 뷰어 사용 유무
					_retObj.put("flash_viewer", _list[i].toString());
					break;
				case 13: //HTML 뷰어 사용 유무
					_retObj.put("html_viewer", _list[i].toString());
					break;
				case 14: //모바일 서포트 사용 유무
					_retObj.put("mobile_support", _list[i].toString());
					break;
				case 15: //버전 정보
					_retObj.put("version", _list[i].toString());
					break;
				case 16: //발급일
					_retObj.put("regDate", _list[i].toString());
					break;
				case 17: //cpu count
					_retObj.put("cpuCount", _list[i].toString());
					break;	
			}
		}
		
		//System.out.println("UBILicenseParser::licenseParser2()-limit_type=" + _retObj.get("limit_type"));
		
		//시간 체크
		if( "All".equals(_retObj.get("limit_type")) || "Time".equals(_retObj.get("limit_type")) )
		{
			//현재 시간 구하기
			String _nowDate = getCurrentDate();
			
			if( _nowDate.compareTo((String)_retObj.get("startDate")) < 0 || _nowDate.compareTo((String)_retObj.get("endDate")) > 0 )
			{
				_retObj.put("result", "fail");
				String _stDateStr = _retObj.get("startDate").toString();
				String _edDateStr = _retObj.get("endDate").toString();
				
				//일자 Formatt변경
				try {
					SimpleDateFormat inputDt = new SimpleDateFormat("yyyymmdd");
					SimpleDateFormat outputDt = new SimpleDateFormat("yyyy-mm-dd");
					Date _stDate = inputDt.parse(_stDateStr);
					Date _edDate = inputDt.parse(_edDateStr);
					
					_stDateStr = outputDt.format(_stDate);
					_edDateStr = outputDt.format(_edDate);
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				String _dateStr = "("+ _stDateStr + "~" + _edDateStr +")";		// 라이센스 기간을 메시지에 함께 포함하여 전달하도록 가공
				//_retObj.put("fail_msg", "라이센스 적용 기간이 아닙니다!");	
				_retObj.put("fail_msg",Log.getMessage(Log.MSG_LICENSE_SERVER_DATE_INVALID) + "\n" + _dateStr); 
				return _retObj;
			}
		}
		
		//ip 체크
		if( "All".equals(_retObj.get("limit_type")) || "Ip".equals(_retObj.get("limit_type")) )
		{
			if( !_ip.equals(_retObj.get("ip")) )
			{
				_retObj.put("result", "fail");
				//_retObj.put("fail_msg","라이센스가 발급된 서버가 아닙니다!");	
				_retObj.put("fail_msg",Log.getMessage(Log.MSG_LICENSE_SERVER_IP_INVALID));	
				return _retObj;
			}
		}
		
		//사용자수 체크
		if( "All".equals(_retObj.get("limit_type")) || "User".equals(_retObj.get("limit_type")) )
		{
			int _LicenseUserCount = Integer.parseInt((String)_retObj.get("userCount")); 
			if( _LicenseUserCount > 0 && _userCnt > _LicenseUserCount )
			{
				_retObj.put("result", "fail");
				//_retObj.put("fail_msg","허가된 라이선스 사용자수를 초과하였습니다.");
				_retObj.put("fail_msg",Log.getMessage(Log.MSG_LICENSE_SERVER_USER_COUNT_INVALID));	
				return _retObj;
			}
		}
		
		//cpu수 체크
		if( "All".equals(_retObj.get("limit_type")) )
		{
			int _LicenseCpuCount = Integer.parseInt((String)_retObj.get("cpuCount")); 
			if( _LicenseCpuCount > 0 && _cpuCnt > _LicenseCpuCount)
			{
				_retObj.put("result", "fail");
				//_retObj.put("fail_msg","허가된 라이선스 CPU수를 초과하였습니다.");
				_retObj.put("fail_msg",Log.getMessage(Log.MSG_LICENSE_SERVER_CPU_COUNT_INVALID));	
				return _retObj;
			}
		}
				
		_retObj.put("result", "success");
		_retObj.put("fail_msg", "");
		
		
		return _retObj;
	}
	
	
	/****************************************************************
	 * Function Name : licenseParser2
	 * parameter	 : _license   (라이센스 스트링) , _instance_id (EC2 인스턴스 아이디), _account_id (인스턴스 사용자), _userCnt(사용자수)
	 * return value  : Object
	 *                 result : success / fail  (파싱 처리 결과)
	 *                 fail_msg : fail 일때 사유 메세지  
	 *                 ubiform_type : all / report / eform
	 *                 license_type : trial / demo / free  --> 이 결과에 따라서 에디터와 뷰어의 바탕 워터마크 처리
	 *                 limit_type   : none / time / ip / user / all
	 *                 query_type : true/false
	 * *************************************************************/	
	public static JSONObject licenseParser3(String _license, String _instance_id, String _account_id, int _userCnt)
	{
		JSONObject _retObj = new JSONObject();
		String _decLicense = "";
		try {
			//복호화.
			//_license = zipDataUtil.decryptString(_license);
			_decLicense = common.base64_decode_uncompress(_license, "UTF-8");
			
			if(!"UBIFORM-LICENSE-UBSTORM-01038687454".equals(_decLicense.substring(0, 35)))
			{
				_retObj.put("result", "fail");
				_retObj.put("fail_msg", "정상적인 라이센스 파일이 아닙니다!-34");
				return _retObj;
			}
		} catch (Exception err) {
			_retObj.put("result", "fail");
			_retObj.put("fail_msg", "정상적인 라이센스 파일이 아닙니다!-39");
			return _retObj;
		}		
		
		String _list [] = {""};		
		_list = _decLicense.split("/");
					
		for(int i=0; i < _list.length ; i++)
		{
			switch(i)
			{
				case 1: //ip address
					_retObj.put("ip", _list[i].toString());
					break;
				case 2: //start date
					_retObj.put("startDate", _list[i].toString());
					break;
				case 3: //end date
					_retObj.put("endDate", _list[i].toString());
					break;
				case 4: //user count
					_retObj.put("userCount", _list[i].toString());
					break;
				case 5: //ubiform type
					_retObj.put("ubiform_type", _getUbiformType(_list[i].toString()));
					break;
				case 6: //license type
					_retObj.put("license_type", _getLicenseType(_list[i].toString()));
					break;
				case 7: //limit type
					_retObj.put("limit_type", _getLimitType(_list[i].toString()));
					break;
				case 8: //site name
					_retObj.put("site_name",  _list[i].toString());
					break;
				case 9: //담당자 명
					_retObj.put("user_name", _list[i].toString());
					break;
				case 10: //라이센스 키
					_retObj.put("key", _list[i].toString());
					break;
				case 11: //쿼리 에디터 사용 유무
					_retObj.put("query_type", _list[i].toString());
					break;

				case 12: //플래쉬 뷰어 사용 유무
					_retObj.put("flash_viewer", _list[i].toString());
					break;
				case 13: //HTML 뷰어 사용 유무
					_retObj.put("html_viewer", _list[i].toString());
					break;
				case 14: //모바일 서포트 사용 유무
					_retObj.put("mobile_support", _list[i].toString());
					break;
				case 15: //버전 정보
					_retObj.put("version", _list[i].toString());
					break;
				case 16: //발급일
					_retObj.put("regDate", _list[i].toString());
					break;
				case 17: //cpu count
					_retObj.put("cpuCount", _list[i].toString());
					break;
				case 18: //instance id
					_retObj.put("instance_id", _list[i].toString());
					break;
				case 19: //account id
					_retObj.put("account_id", _list[i].toString());	
					break;	
			}
		}
		
		//System.out.println("UBILicenseParser::licenseParser3() - limit_type=" + _retObj.get("limit_type"));
		
		//시간 체크
		if( "All".equals(_retObj.get("limit_type")) || "Time".equals(_retObj.get("limit_type")) )
		{
			//현재 시간 구하기
			String _nowDate = getCurrentDate();
			
			if( _nowDate.compareTo((String)_retObj.get("startDate")) < 0 || _nowDate.compareTo((String)_retObj.get("endDate")) > 0 )
			{
				_retObj.put("result", "fail");
				//_retObj.put("fail_msg", "라이센스 적용 기간이 아닙니다!");	
				_retObj.put("fail_msg",Log.getMessage(Log.MSG_LICENSE_SERVER_DATE_INVALID));	
				return _retObj;
			}
		}
		
		//instance_id 체크
		if( "All".equals(_retObj.get("limit_type")) || "InstanceID".equals(_retObj.get("limit_type")) )
		{
			if( !_instance_id.equals(_retObj.get("instance_id")) )
			{
				_retObj.put("result", "fail");
				//_retObj.put("fail_msg","라이센스가 발급된 서버가 아닙니다!");	
				_retObj.put("fail_msg",Log.getMessage(Log.MSG_LICENSE_SERVER_INSTANCE_INVALID) + "(license:" + _retObj.get("instance_id") + ", server:" + _instance_id + ")");	
				return _retObj;
			}
		}
		
		//account_id 체크
		if( "All".equals(_retObj.get("limit_type")) || "AccountID".equals(_retObj.get("limit_type")) )
		{
			if( !_account_id.equals(_retObj.get("account_id")) )
			{
				_retObj.put("result", "fail");
				//_retObj.put("fail_msg","라이센스가 발급된 서버가 아닙니다!");	
				_retObj.put("fail_msg",Log.getMessage(Log.MSG_LICENSE_SERVER_ACCOUNT_INVALID) + "(license:" + _retObj.get("account_id") + ", server:" + _account_id + ")");	
				return _retObj;
			}
		}
		
		//사용자수 체크
		if( "All".equals(_retObj.get("limit_type")) || "User".equals(_retObj.get("limit_type")) )
		{
			int _LicenseUserCount = Integer.parseInt((String)_retObj.get("userCount")); 
			if( _LicenseUserCount > 0 && _userCnt > _LicenseUserCount )
			{
				_retObj.put("result", "fail");
				//_retObj.put("fail_msg","허가된 라이선스 사용자수를 초과하였습니다.");
				_retObj.put("fail_msg",Log.getMessage(Log.MSG_LICENSE_SERVER_USER_COUNT_INVALID));	
				return _retObj;
			}
		}
				
		_retObj.put("result", "success");
		_retObj.put("fail_msg", "");
		
		
		return _retObj;
	}
	
	
	
	private static String _getUbiformType(String type)
	{
		String ubiformType = "";
		if("0".equals(type))
			ubiformType = "Report";
		else if("1".equals(type))
			ubiformType = "eForm";
		else if("9".equals(type))
			ubiformType = "All";
		
		return ubiformType;
	}
	
	private static String _getLicenseType(String type)
	{
		String licenseType = "";
		if("0".equals(type))
			licenseType = "Trial";
		else if("1".equals(type))
			licenseType = "Demo";
		else if("9".equals(type))
			licenseType = "Free";
		
		return licenseType;
	}
	
	private static String _getLimitType(String type)
	{
		String limitType = "";
		if("0".equals(type))
			limitType = "None";
		else if("1".equals(type))
			limitType = "Time";
		else if("2".equals(type))
			limitType = "User";
		else if("3".equals(type))
			limitType = "Ip";
		else if("4".equals(type))
			limitType = "InstanceID";
		else if("9".equals(type))
			limitType = "All";
		
		return limitType;
	}
	
	
	public static String getCurrentDate() {
		String currDate   = "";
		Date nowDate        = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		
		String currYear      = String.valueOf(cal.get(Calendar.YEAR));  // Year
		String currMonth    = String.valueOf(cal.get(Calendar.MONTH)+1); // Month
		String currDay      = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));  // Day
		
		if(currDay.length() == 1)         { currDay = "0"+currDay; }
		if(currMonth.length() == 1)       { currMonth = "0"+currMonth; }
		
		currDate = currYear+currMonth+currDay;
		return currDate;
	}
	
	public static String getKey(String _idx) {
		String currDate = "";
		Date nowDate        = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowDate);
		
		String currYear      = String.valueOf(cal.get(Calendar.YEAR));  // Year
		String currMonth    = String.valueOf(cal.get(Calendar.MONTH)+1); // Month
		String currDay      = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));  // Day
		String currHours 	= String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
		String currMinutes 	= String.valueOf(cal.get(Calendar.MINUTE));
		String currSeconds 	= String.valueOf(cal.get(Calendar.SECOND));
		
		if(currDay.length() == 1)         { currDay = "0"+currDay; }
		if(currMonth.length() == 1)       { currMonth = "0"+currMonth; }

		currDate = currYear+currMonth+currDay + currHours + currMinutes + currSeconds + "UB" + _idx;
			
		return currDate;
	}
}
