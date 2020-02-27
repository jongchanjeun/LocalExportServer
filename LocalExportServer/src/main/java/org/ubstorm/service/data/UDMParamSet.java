package org.ubstorm.service.data;

import org.ubstorm.server.commnuication.UDPClient;

public class UDMParamSet {

	private UDPClient mUdpClient;
	private UDMReqInfo REQ_INFO;
	private UDMDataQuery DATA_QUERY;
	
	public UDMParamSet()
	{
		REQ_INFO = new UDMReqInfo();
		DATA_QUERY = new UDMDataQuery();
	}
	
	public UDMReqInfo getREQ_INFO() {
		return REQ_INFO;
	}
	public void setREQ_INFO(UDMReqInfo rEQ_INFO) {
		REQ_INFO = rEQ_INFO;
	}
	public UDMDataQuery getDATA_QUERY() {
		return DATA_QUERY;
	}
	public void setDATA_QUERY(UDMDataQuery dATA_QUERY) {
		DATA_QUERY = dATA_QUERY;
	}
	
	public UDPClient getUDPClient() {
		return mUdpClient;
	}
	public void setUDPClient(UDPClient rmUdpClient) {
		mUdpClient = rmUdpClient;
	}

}
