package org.ubstorm.server.commnuication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class UDPClient {
	
	// class
	private Logger log = Logger.getLogger(getClass());
		
	private String mUdpServerIp = "";
	private int mUdpServerPort = 0;
	private DatagramSocket mSocket = null;
	
	public UDPClient() {

		this.mUdpServerIp = "127.0.0.1";
		this.mUdpServerPort = 38000;
		
		try {
			this.mSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("SocketException:" + e.getMessage());
		}
	}
	
	public UDPClient(int port) {

		this.mUdpServerIp = "127.0.0.1";
		this.mUdpServerPort = port;
		
		try {
			this.mSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("SocketException:" + e.getMessage());
		}
	}
		
	public boolean sendMessage(String message)
	{
		boolean result = true;
		
		// 전송할  DatagramPacket 생성
		DatagramPacket packet = null;
		try {
			
			log.debug("UDP sendMessage()======>" + message);
			
			packet = new DatagramPacket(
						message.getBytes(), 
						message.getBytes().length, 
						InetAddress.getByName(this.mUdpServerIp), 
						this.mUdpServerPort
					);

			// DatagramPacket 전송
			mSocket.send(packet);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		}
		
		return result;
	}
	
}
