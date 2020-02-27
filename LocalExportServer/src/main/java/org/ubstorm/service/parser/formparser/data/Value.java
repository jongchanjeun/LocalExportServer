package org.ubstorm.service.parser.formparser.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Value {
    public enum Type {
        STRING, INTEGER, FLOAT, BOOLEAN, OBJECT,ELEMENT,ARRAYSTRING,ARRAYINT,ARRAYCOLLECTION,MAP,NODELIST;
    }

    private Type type;
    private Object value;
    
    // 넘어온 데이터의 타입을 체트하여 타입에 담아두기
    public void typeCheck( String _type)
    {
    	if( _type.equals("string") ){
    		this.type = Type.STRING;
    	}else if( _type.equals("number") ){
    		this.type = Type.FLOAT;
    	}else if( _type.equals("int") ){
    		this.type = Type.INTEGER;
    	}else if( _type.equals("boolean") ){
    		this.type = Type.BOOLEAN;
    	}else if( _type.equals("object") ){
    		this.type = Type.OBJECT;
    	}else if( _type.equals("arraystr") ){
    		this.type = Type.ARRAYSTRING;
    	}else if( _type.equals("array") ){
    		this.type = Type.ARRAYSTRING;
    	}else if( _type.equals("arrayint") ){
    		this.type = Type.ARRAYINT;
    	}else if( _type.equals("ArrayCollection") ){
    		this.type = Type.ARRAYCOLLECTION;
    	}else if( _type.equals("map") ){
    		this.type = Type.MAP;
    	}else if( _type.equals("element")){
    		this.type = Type.ELEMENT;
    	}else if( _type.equals("nodelist")){
    		this.type = Type.NODELIST;
    	}else{
    		this.type = Type.STRING;
    	}
    }
    
    public Value(Object value ) {
        typeCheck("string");
        if( this.type == Type.ARRAYCOLLECTION )
        {
        	value = JSONValue.parse(value.toString());
        	JSONArray jsonArray = (JSONArray) value;
        	this.value = jsonArray2List( jsonArray );
        }
        else
        {
        	this.value = value;
        }
    }

    public Value(Object value, String type) {
        typeCheck(type);
        if( this.type == Type.ARRAYCOLLECTION )
        {
        	value = JSONValue.parse(value.toString());
        	JSONArray jsonArray = (JSONArray) value;
        	this.value = jsonArray2List( jsonArray );
        }
        else
        {
        	this.value = value;
        }
    }
    
    public Object getValue()
    {
    	return this.value;
    }
    
    public static Value fromString(String s) {
        return new Value(s, "string");
    }

    public static Value fromInteger(Float i) {
        return new Value(i, "number");
    }
    
    public static Value fromBoolean(Boolean i) {
        return new Value(i, "boolean");
    }
    
    public static Value fromObject(Object i) {
        return new Value(i, "object");
    }
    public static Value fromArrayString(ArrayList<String> i) {
        return new Value(i, "arraystr");
    }
    public static Value fromArrayInteger(ArrayList<Integer> i) {
        return new Value(i, "arrayint");
    }
    public static Value fromArrayBoolean(ArrayList<Boolean> i) {
        return new Value(i, "arrayboolean");
    }
    
    public Type getType() {
        return this.type;
    }
    
    public String getStringValue() {
    	if( this.type.equals(Type.ARRAYCOLLECTION) == false && this.type.equals(Type.ARRAYINT) == false  && this.type.equals(Type.ELEMENT) == false  )
    	{
    		return String.valueOf( value );
    	}
    	else
    	{
    		return "";
    	}
    }
    
    public Float getIntegerValue() {
        return (Float) Float.valueOf( String.valueOf( value ) );
    }
    
    public Integer getIntValue() {
        return (int) Integer.valueOf( String.valueOf( value ) );
    }
    
    public Element getElementValue(){
    	return (Element) value;
    }
    
    public NodeList getNodeListValue(){
    	return (NodeList) value;
    }
    
    public HashMap getMapValue(){
    	return (HashMap) value;
    }
    
    public Boolean getBooleanValue() {
    	if( String.valueOf(value).toUpperCase().equals("TRUE")) value = "True";
    	else  value = "false";
        return (Boolean) Boolean.parseBoolean( (String) value );
    }
    
    public void setBooleanValue(Boolean _boolean) {
    	this.type = Type.BOOLEAN;
    	value = _boolean;
    }
    
    public Object getObjectValue() {
        return (Object) value;
    }
    public ArrayList<String> getArrayStringValue() {
        return (ArrayList<String>) value;
    }
    
    public ArrayList<Integer> getArrayIntegerValue(){
    	return (ArrayList<Integer>) value;
    }
    // equals, hashCode
    
    public ArrayList<Boolean> getArrayBooleanValue() {
        return (ArrayList<Boolean>) value;
    }
    
    public List<Object> getArrayCollection(){
    	return (List<Object>) value;
    }
    
    public static List<Object> jsonArray2List( JSONArray arrayOFKeys ){
    	
    	if( arrayOFKeys != null )
    	{
    		List<Object> array2List = new ArrayList<Object>();
    		
    		for ( int i = 0; i < arrayOFKeys.size(); i++ )  {
    			if ( arrayOFKeys.get(i) instanceof JSONObject ) {
    				HashMap<String, String> subObj2Map = jsonString2Map(arrayOFKeys.get(i).toString());
    				array2List.add(subObj2Map);
    			}else if ( arrayOFKeys.get(i) instanceof JSONArray ) {
    				List<Object> subarray2List = jsonArray2List((JSONArray) arrayOFKeys.get(i));
    				array2List.add(subarray2List);
    			}else {
    				array2List.add( arrayOFKeys.get(i) );
    			}
    		}
    		
    		return array2List;      
    	}
    	else
    	{
    		return null; 
    	}
    	
    }
    
    public static HashMap<String, String> jsonString2Map( String jsonString ){

        Object _data = JSONValue.parse(jsonString);
        JSONObject _jsonObj = (JSONObject) _data;
        
        Iterator<String> nameItr = (Iterator)_jsonObj.keySet().iterator();
        HashMap<String, String> outMap = new HashMap<String, String>();
        while(nameItr.hasNext()) {
            String name = nameItr.next();
            outMap.put(name, _jsonObj.get(name).toString() );
        }
        
        return outMap;
    }
    
    /**
	 * String인 borderSide 를 ArrayList로 변환. 
	 * @return ArrayList _borderSideAr
	 * 
	 */
	public static ArrayList<String> setArrayString(String _bSide)
	{
		ArrayList<String> _borderSideAr = new ArrayList<String>();

		String[] _strAr = _bSide.split(",");

		for(String _bStr : _strAr )
		{
			_borderSideAr.add(_bStr);
		}

		return _borderSideAr;
	}
    
	   /**
		 * String인 borderSide 를 ArrayList로 변환. 
		 * @return ArrayList _borderSideAr
		 * 
		 */
		public static ArrayList<Float> convertStringToArrayFloat(String _bSide)
		{
			ArrayList<Float> _borderSideAr = new ArrayList<Float>();

			String[] _strAr = _bSide.split(",");

			for(String _bStr : _strAr )
			{
				_borderSideAr.add( Float.parseFloat(_bStr) );
			}

			return _borderSideAr;
		}
	
}
