package org.ubstorm.service.dictionary;

import java.util.HashMap;
import java.util.Map;

import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;

import com.lowagie.text.Image;

public class ImageDictionary {

	private HashMap<String, ImageDictionaryVO> mImageMap;
	
	
	/**
	 * 초기화
	 * */
	public void initialize()
	{
		mImageMap = new HashMap<String, ImageDictionaryVO>();
	}
	 
	
	/**
	 * 해제
	 * */
	public void destroy()
	{
		if( mImageMap != null ){
			
			for( Map.Entry<String, ImageDictionaryVO> elem : mImageMap.entrySet() ){
				
				ImageDictionaryVO vo =elem.getValue();
				vo.destroy();
				vo=null;
	        }
		}
		
		mImageMap =null;
	}
	
	/**
	 * 동일한 이미지 data vo를 반환한다.
	 * */
	public ImageDictionaryVO getDictionaryData( String _data )
	{
		ImageDictionaryVO _result=null;
		
		if( mImageMap == null ){
			initialize();
		}
		
		HashMap<String, ImageDictionaryVO> test =mImageMap;
		
		ImageDictionaryVO _item=null;
		
		Boolean _hasItem=false;
		
		for( int i=0; i<test.size(); i++ ){
			_item = test.get("IM"+i);
			if( _item.getmImageData().equals(_data) ){
				_hasItem=true;
				break;
			}
		}
		
		
		if( _hasItem ){
			_result = _item;
		}
		
		
		return _result;
	}
	
	
	/**
	 * image dictionary 객체를 만든다.
	 * */
	public ImageDictionaryVO createDictionaryData( String _imgData , BinaryPartAbstractImage _docImg  )
	{
		if( mImageMap == null ){
			initialize();
		}
		
		ImageDictionaryVO vo=new ImageDictionaryVO();
		vo.setmImageData(_imgData);
		vo.setmDocImage(_docImg);
		vo.setmIsCreate(true);
		
		String _key="IM"+mImageMap.size();
		
		mImageMap.put(_key, vo);
		
		return vo;
	}
	
	
	/**
	 * image dictionary 객체를 만든다.
	 * */
	public ImageDictionaryVO createPDFDictionaryData( String _imgData , Image _pdfImg  )
	{
		if( mImageMap == null ){
			initialize();
		}
		
		
		ImageDictionaryVO vo=new ImageDictionaryVO();
		vo.setmImageData(_imgData);
		vo.setmPDFImage(_pdfImg);
		vo.setmIsCreate(true);
		
		String _key="IM"+mImageMap.size();
		
		mImageMap.put(_key, vo);
		
		return vo;
	}


}
