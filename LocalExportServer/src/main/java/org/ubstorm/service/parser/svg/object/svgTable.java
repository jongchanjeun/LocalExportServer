package org.ubstorm.service.parser.svg.object;

import java.util.ArrayList;
import java.util.List;

public class svgTable {
	private int width = 800;
	private int height = 600;
	
	List<svgGroup> groups = new ArrayList<svgGroup>();
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public List<svgGroup> getTexts() {
		return groups;
	}

	public void setGroups(List<svgGroup> texts) {
		this.groups = texts;
	}

	/**
	 * Table에 Group하나를 추가한다.
	 * 
	 * @param group
	 * @return 성공:Group갯수, 실패:-1
	 */
	public int addGroup(svgGroup group)
	{
		if(this.groups.add(group))
			return this.groups.size();
		else
			return -1;
	}
	
	/**
	 * Table에 저장된 Group갯수를 반환한다.
	 * 
	 * @return
	 */
	public int getGroupCount()
	{
		return this.groups.size();
	}
		
	/**
	 * Table에 저장된 Group을 반환한다.
	 * 
	 * @return
	 */
	public svgGroup getGroup(int idx)
	{
		return this.groups.get(idx);
	}	
	
	/**
	 * Table에 마지막으로 저장된 Group을 반환한다.
	 * 
	 * @return
	 */
	public svgGroup getLastGroup()
	{
		return this.groups.get(this.groups.size()-1);
	}
}
