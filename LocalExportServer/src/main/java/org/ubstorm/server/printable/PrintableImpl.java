package org.ubstorm.server.printable;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.SheetCollate;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.ubstorm.server.commnuication.UDPClient;
import org.ubstorm.service.logger.Log;
import org.ubstorm.service.parser.Json2PrintParser;
import org.ubstorm.service.utils.Base64Coder;

import sun.misc.BASE64Decoder;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

public class PrintableImpl implements Printable, Pageable {

	private Logger log = Logger.getLogger(getClass());
	
	//--- Private instances declarations
	private final Json2PrintParser j2pp;
	private final FORMFile formInfos;
	
	private PageFormat pageFormat;
	private PrinterJob printJob;
	
	private int mBeforPageIndex;
	private int mNtimesCalled;
	
	private JSONObject mPage;
	
	private UDPClient mUdpClient;
	
	private int mPageRanges = 1;
	
	/*
	 * 72(dpi) / 25 (mm/inch) = 0.352 mm/px = (default value of transcoder) 
	 */ 
	private final double ppmm = 72 / 25.4; 
	
	/**
	 * Create a new Printable pdf
	 * @param pdf The PDFFile to print
	 * @param a page format (or null for default)
	 */
	public PrintableImpl(UDPClient udpClient, PrinterJob prntJob, FORMFile formInfos, PageFormat pageFormat) {

		this.mUdpClient = udpClient;
		this.j2pp = new Json2PrintParser();
		this.formInfos = formInfos;

		// create a new print job
		this.printJob = (prntJob == null) ? PrinterJob.getPrinterJob() : prntJob;
		// use the default page if we have a null format
		this.pageFormat = (pageFormat == null) ? printJob.defaultPage() : pageFormat;
		
		printJob.setPageable(this);
		printJob.setPrintable(this, this.pageFormat);
		printJob.setJobName("UBIFORM Report Job");
	}

	/**
	 * Get the current pageFormat
	 * @return the pageFormat
	 */
	public PageFormat getPageFormat() {
		return pageFormat;
	}
	
	/**
	 * Change the associated page format
	 * @param pageFormat
	 */
	public void setPageFormat(PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}
	
	/**
	 * Shortcut method for changing page orientation
	 * Works the same as calling getPageFormat().setOrientation(orientation)
	 * @see PageFormat#setOrientation(int)
	 * @param orientation one of PageFormat.LANDSCAPE, PageFormat.PORTRIAT, PageFormat.REVERSE_LANDSCAPE, etc
	 */
	public void setPageOrientation(int orientation) {
		pageFormat.setOrientation(orientation);
	}
	
	/**
	 * @return A page format that has the minimum acceptable margins for the current printer
	 */
	private PageFormat minimumMarginsFormat(PageFormat format) {
		// get the paper
		Paper paper = format.getPaper();
		
		// set the paper to no margins
		paper.setImageableArea(paper.getImageableX(), paper.getImageableY(), paper.getImageableWidth(), paper.getImageableHeight());
		
		// re-set with the marginless paper
		format.setPaper(paper);

		// validate for the chosen printer in this printjob
		return printJob.validatePage(format);
	}
	
	/**
	 * Actually print the PDF
	 * @throws PrinterException
	 */
	public boolean print(boolean showDialog) throws PrinterException {
		
		boolean isError = false;
		
		Paper paper = pageFormat.getPaper();
		
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();		
		
		
//		if(Log.pdfExportPrinterType != null && Log.pdfExportPrinterType.length()>0){//labelPrinter
//			aset.add(PrintQuality.HIGH);			
//			aset.add(Chromaticity.MONOCHROME);		
//		}
		 
		float imageableX = (float) (paper.getImageableX() / ppmm);
		float imageableY = (float) (paper.getImageableY() / ppmm);
		float imageableW = (float) (paper.getImageableWidth() / ppmm);
		float imageableH = (float) (paper.getImageableHeight() / ppmm);
		
		aset.add(new MediaPrintableArea(imageableX, imageableY, imageableW, imageableH, MediaPrintableArea.MM));

		if(pageFormat.getOrientation()==pageFormat.LANDSCAPE)
			aset.add(OrientationRequested.LANDSCAPE);
		else if(pageFormat.getOrientation()==pageFormat.REVERSE_LANDSCAPE)
			aset.add(OrientationRequested.REVERSE_LANDSCAPE);
		else
			aset.add(OrientationRequested.PORTRAIT);
		
		aset.add(new PageRanges(1, getNumberOfPages()));
		aset.add(SheetCollate.UNCOLLATED);
		//인쇄 페이지 범위 기본값 저장
		mPageRanges = getNumberOfPages();
		if(!showDialog || printJob.printDialog(aset)) {
			
			OrientationRequested pageOrientation 
				=  (OrientationRequested)aset.get(OrientationRequested.class);
			log.debug("pageFormat.Orientation=" + pageFormat.getOrientation() + ", pageOrientation=" + pageOrientation.getValue());
			
			SheetCollate sheetCollate 
				=  (SheetCollate)aset.get(SheetCollate.class);
			if(sheetCollate != null)
				log.debug("sheetCollate=" + sheetCollate.getValue());
			
			if(pageOrientation != null)
			{
				int nPageFormatOrientation = pageFormat.getOrientation();	// 0 : landscape, 1: portrate, 2: reverseLandscape
				int nPaperOrientation = pageOrientation.getValue();			// 4 : landscape, 3: portrate, 2: reverseLandscape
				
				switch(nPageFormatOrientation)
		        {
		        	case PageFormat.LANDSCAPE:
		        		if(nPaperOrientation != OrientationRequested.LANDSCAPE.getValue())
		        		{
		        			//paper.setSize(paper.getHeight(), paper.getWidth());
		        			if(nPaperOrientation == OrientationRequested.PORTRAIT.getValue())
		        				pageFormat.setOrientation(PageFormat.PORTRAIT);
		        			else if(nPaperOrientation == OrientationRequested.REVERSE_LANDSCAPE.getValue())
		        				pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
		        		}
		        		break;
		        	case PageFormat.PORTRAIT:
		        		if(nPaperOrientation != OrientationRequested.PORTRAIT.getValue())
		        		{
		        			//paper.setSize(paper.getHeight(), paper.getWidth());
		        			if(nPaperOrientation == OrientationRequested.LANDSCAPE.getValue())
		        				pageFormat.setOrientation(PageFormat.LANDSCAPE);
		        			else if(nPaperOrientation == OrientationRequested.REVERSE_LANDSCAPE.getValue())
		        				pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
		        		}
		        		break;
		        	case PageFormat.REVERSE_LANDSCAPE:
		        		if(nPaperOrientation != OrientationRequested.REVERSE_LANDSCAPE.getValue())
		        		{
		        			//paper.setSize(paper.getHeight(), paper.getWidth());
		        			if(nPaperOrientation == OrientationRequested.LANDSCAPE.getValue())
		        				pageFormat.setOrientation(PageFormat.LANDSCAPE);
		        			else if(nPaperOrientation == OrientationRequested.PORTRAIT.getValue())
		        				pageFormat.setOrientation(PageFormat.PORTRAIT);
		        		}
		        		break;
			    }
			}
			
	        MediaPrintableArea pagePrintableArea 
				=  (MediaPrintableArea)aset.get(MediaPrintableArea.class);		    
	        if(pagePrintableArea != null)
		    {
		        log.debug("pagePrintableArea.x=" + pagePrintableArea.getX(MediaSize.MM) + ", pagePrintableArea.y=" + pagePrintableArea.getY(MediaSize.MM));

	        	double marginX = pagePrintableArea.getX(MediaSize.MM)*ppmm;
		        double marginY = pagePrintableArea.getY(MediaSize.MM)*ppmm;
		        double marginWidth = pagePrintableArea.getWidth(MediaSize.MM)*ppmm;
		        double marginHeight = pagePrintableArea.getHeight(MediaSize.MM)*ppmm;	        
		        paper.setImageableArea(marginX, marginY, marginWidth, marginHeight);
		        pageFormat.setPaper(paper);
		    }
	        
			// set the minimum margins for this paper (java defaults to 1"... wtf?)
			pageFormat = minimumMarginsFormat(pageFormat);
			//인쇄 팝업에서 설정한 인쇄 페이지 범위 값을 상태 메시지에 반영하도록 저장
			PageRanges prs = (PageRanges)aset.get(PageRanges.class);
			if(prs != null){
				mPageRanges = prs.getMembers()[0][1];
			}			
			try {
				 this.mUdpClient.sendMessage(JSONConverter("SUCCESS", "PRINT_START", Log.MSG_LP_START_PRINT, Log.getMessage(Log.MSG_LP_START_PRINT)));

				 printJob.print(aset);;
            } catch (PrinterAbortException ex) {
                String msg = ex.getLocalizedMessage();
                if (msg.length() == 0) {
                    msg = "Printing has been cancelled.";
                }
                log.error("PrinterAbortException:" + msg);
                isError = true;
            } catch (PrinterException ex) {
                String msg = ex.getLocalizedMessage();
                if (msg == null || msg.length() == 0) {
                    msg = "Printing has failed.";
                }
                log.error("PrinterException:" + msg);
                isError = true;
            }
			
			if(isError==false)
				log.info("Print ok!");	
		}
		else
		{
			log.info("Print cancel!");
			isError = true;
		}
		
		return isError;
	}

	public int getNumberOfPages() {
		return formInfos.getNumPages();
	}

	public PageFormat getPageFormat(int pageIndex)
			throws IndexOutOfBoundsException {	
		return pageFormat;
	}

	public Printable getPrintable(int pageIndex)
			throws IndexOutOfBoundsException {
		return this;
	}

	
	private String JSONConverter(String type, String command, String code, String message) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("port", Log.wsPort);
		jsonObject.put("type", type);
		jsonObject.put("command", command);
		jsonObject.put("code", code);
		jsonObject.put("message", message);
		return jsonObject.toString();
	}	
	
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
	
		// Check for a page
		if(pageIndex >= formInfos.getNumPages() )
			return NO_SUCH_PAGE;
		
		if(Log.printStop)
		{
			log.debug("Log.printStop=true ~~~~~~~~~~~~~ Now Print job is stopping....");
			Log.printStop = false;
			
			throw new PrinterAbortException("Print job is aborted.");
			//return NO_SUCH_PAGE;
		}
		
		Graphics2D g2 = (Graphics2D) graphics;
		
		if(this.mBeforPageIndex != pageIndex)
    	{
    		this.mBeforPageIndex = pageIndex;
    		this.mNtimesCalled = 1;
    	}
    	else
    	{
    		this.mNtimesCalled++;
    	}
		
		log.debug("curPageIndex=" + pageIndex + "===============================> mNtimesCalled=" + mNtimesCalled);
		
		//margin값으로 스케일 구하기
		double printScale = 1;	
		double printMarginXValue = pageFormat.getImageableX();	
		double printMarginYValue =  pageFormat.getImageableY();		

		log.debug("curPageIndex=" + pageIndex + "==> getImageableWidth=" + pageFormat.getImageableWidth() + ",pageFormatWidth=" + pageFormat.getWidth() + ", printMarginXValue=" + printMarginXValue + ", printMarginYValue=" + printMarginYValue);
		
		//this.mUdpClient.sendMessage(JSONConverter("SUCCESS", "PRINT", "Printing : " + (pageIndex+1) + "/" + formInfos.getNumPages()));
		//인쇄 페이지 범위 - 인쇄 팝업에서 설정한 값으로 변경하여 상태 메시지 전달
		this.mUdpClient.sendMessage(JSONConverter("SUCCESS", "PRINT", "99999", "Printing : " + (pageIndex+1) + "/" + mPageRanges));
		
		// is this necessary? 
		//g2.translate(5, 5);//프린트 기본 여백 임의 지정
		g2.translate(0, 0);//프린트  시작점 지정
		
		if(Log.pdfExportPrinterType != null && Log.pdfExportPrinterType.length()>0){//labelPrinter
			printScale = 0.95;//(pageFormat.getWidth() - (pageFormat.getImageableX() *2))/ pageFormat.getWidth();	
		}else{
			printScale =  pageFormat.getImageableWidth() < pageFormat.getImageableHeight() ? 
					 (pageFormat.getImageableWidth() / pageFormat.getWidth()) :  (pageFormat.getImageableHeight() / pageFormat.getHeight());
		}		
	    
		if(pageIndex == 0)
		{			
			String waterMarkStr = "";
			waterMarkStr = this.formInfos.getProjectInfo() != null ? this.formInfos.getProjectInfo().get("waterMark").toString() : "";
			this.j2pp.init(waterMarkStr, Log.clientLicenseType , "" + pageFormat.getWidth(), "" + pageFormat.getHeight(), printScale, printMarginXValue, printMarginYValue);
		}
		 
		try {
			if(this.mNtimesCalled==1)
				mPage = formInfos.getPage(pageIndex);
		
			if(mPage != null)
			{
				this.j2pp.makePdfPage(g2, mPage, "" + pageIndex, mNtimesCalled);
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return PAGE_EXISTS;
	}

	@Override
	protected void finalize() {
	
	}

}
