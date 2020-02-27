package org.ubstorm.service.parser.formparser;

import java.util.HashMap;

import org.ubstorm.service.parser.ItemPropertyProcess;
import org.ubstorm.service.parser.ItemPropertyVariable;
import org.w3c.dom.NodeList;

public class ItemMappingProcess {

	private ItemPropertyVariable mItemPropVar = new ItemPropertyVariable();
	private ItemPropertyProcess mPropertyFn = new ItemPropertyProcess();
	
	
	public HashMap<String, Object> getMappingItem( String _className , NodeList _Propertys , String _formType)
	{
		
		HashMap<String, Object> _propItem = mItemPropVar.getItemName(_className);
		
		
		
		
		
		
		return _propItem;
	}
	
	
	
	private HashMap<String, Object> getFreeFormMappingItem( NodeList _propertys)
	{
		
		
		return new HashMap<String, Object>();
	}
	
}
