package org.ubstorm.server.commnuication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.ubstorm.service.logger.Log;

public class UDPServer implements Runnable {

	private Logger log = Logger.getLogger(getClass());
	
	private static UDPServer instance = null;
	private DatagramSocket socket = null;

	public UDPServer(int port) throws SocketException { 
		
		try
		{
			// DatagramPacket을 받기 위한 Socket 생성 
			socket = new DatagramSocket(port); 
		}
		catch(SocketException e)
		{
			e.printStackTrace();
			log.error("UDPServer::SocketException-" + e.getMessage());
		}
	}
	
	public static UDPServer getInstance(int port) throws SocketException {
		if (instance == null) {
			instance = new UDPServer(port);
		}
		return instance;
	}	
	
	
	public void closeSocket()
	{
		log.info("UDPServer closeSocket!!!");
		
		if(socket != null)
		{
			socket.close();
		}
	}
	
	@Override
	public void run() {
		
		log.info("UDPServer Start!!!...");
		
		while (!Thread.interrupted()) { 
			try { 
				// 데이터를 받을 버퍼 
				byte[] inbuf = new byte[256]; 
				// 데이터를 받을 Packet 생성 
				DatagramPacket packet = new DatagramPacket(inbuf, inbuf.length); 
				// 데이터 수신
				// 데이터가 수신될 때까지 대기됨 
				socket.receive(packet); 
				// 수신된 데이터 출력 
				
				String stMessage = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
				log.debug("received length : " + packet.getLength() + ", received data : " + stMessage); 
				
				Object ubObj;
	    	   	JSONObject ubobjParam;
	    	   	
	    	   	if(stMessage instanceof String) {
	    	   		
					ubObj = JSONValue.parseWithException(stMessage);
					ubobjParam = (JSONObject)ubObj;
					
					String reqCmdValue = (String) ubobjParam.get("PRINT_COMMAND");
					String reqMsgValue = "";
					if(ubobjParam.containsKey("PRINT_MSG")){
						reqMsgValue = (String) ubobjParam.get("PRINT_MSG");
					}
					
					log.debug("UDPMessage::PRINT_COMMAND=" + reqCmdValue);

					if(Log.gConSocketAddress != null)
					{
						if(reqCmdValue != null && reqCmdValue.equalsIgnoreCase("PREVIEW_PRINT_COMPLETED")){
							Log.previewPrintCompleated = true;							
						}						
						if(reqCmdValue != null && reqCmdValue.startsWith("PREVIEW|")){
							Log.previewFlag = true;
							Log.currPageIdx = Integer.parseInt(reqCmdValue.replace("PREVIEW|", ""));	
						}
						if(reqCmdValue != null && reqCmdValue.equals("PREVIEW_PRINT_STOP")){
							Log.previewPrintStop = true;
							Log.printStop = true;
	        		        Log.printCancel = true;    
						}
						if(reqCmdValue != null && reqCmdValue.startsWith("PREVIEW_CLOSE")){
							Log.previewClose = true;							
						}
						if(reqCmdValue != null && reqCmdValue.startsWith("PRINT|")){
							Log.execPrintFlag = true;
							Log.execPrintPage = reqCmdValue.replace("PRINT|", "");
						}
						if(reqCmdValue != null && reqCmdValue.startsWith("PDF_DOWN")){
							Log.pdfDownFlag = true;							
						}	
						if(reqCmdValue != null && reqCmdValue.startsWith("EXCEL_DOWN")){
							Log.excelDownFlag = true;	
							Log.excelDownOption = reqMsgValue;
						}						
						if(reqCmdValue != null && reqCmdValue.equalsIgnoreCase("STOP"))
							Log.printStop = true;
						
						if(reqCmdValue != null && reqCmdValue.equalsIgnoreCase("COMPLETED"))
							Log.printCompleted = true;						
						
					}
				}
			} 
			//catch (IOException | ParseException e) 
			catch (Exception e) 
			{ 
				e.printStackTrace(); 
			} 
	    }
		
		log.debug("UDPServer Thread Interrupted~~~~~~~~~."); 
		socket.close();
	}

}
