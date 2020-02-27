package org.ubstorm.service.dictionary;

import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;

import com.lowagie.text.Image;

public class ImageDictionaryVO {

	private String mImageData;
	
	private Boolean mIsCreate=false;
	
	private BinaryPartAbstractImage mDocImage=null;

	private Image mPDFImage=null;

	private String mEmbedID="";
	
	public void destroy()
	{
		mImageData=null;
		mIsCreate=null;
		mDocImage=null;
		mPDFImage=null;
		mEmbedID=null;
	}
	
	public String getmImageData() {
		return mImageData;
	}

	public void setmImageData(String mImageData) {
		this.mImageData = mImageData;
	}

	public Boolean getmIsCreate() {
		return mIsCreate;
	}

	public void setmIsCreate(Boolean mIsCreate) {
		this.mIsCreate = mIsCreate;
	}

	public BinaryPartAbstractImage getmDocImage() {
		return mDocImage;
	}

	public void setmDocImage(BinaryPartAbstractImage mDocImage) {
		this.mDocImage = mDocImage;
	}

	public String getmEmbedID() {
		return mEmbedID;
	}

	public void setmEmbedID(String mEmbedID) {
		this.mEmbedID = mEmbedID;
	}

	public Image getmPDFImage() {
		return mPDFImage;
	}

	public void setmPDFImage(Image mPDFImage) {
		this.mPDFImage = mPDFImage;
	}
	
}
