package org.ubstorm.service.parser.queue;

import java.util.NoSuchElementException;

public interface IQueue {
	public void resetJobCount();
	public void reduceJobCount() throws InterruptedException, NoSuchElementException;
	public int getJobCount();
	
	public String getName();
	public void clear();
	public void put(Object obj);
	public Object pop() throws InterruptedException, NoSuchElementException;
	public Object peek() throws InterruptedException, NoSuchElementException;
	public int size();
}
