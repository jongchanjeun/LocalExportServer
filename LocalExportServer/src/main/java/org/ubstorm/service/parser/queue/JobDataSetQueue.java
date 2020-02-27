package org.ubstorm.service.parser.queue;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class JobDataSetQueue implements IQueue {
	private static final String NAME = "JOB QUEUE";
	private static final Object monitor = new Object();
	
	private LinkedList jobs = new LinkedList();
	private int nJobCount = 0;
	
	//private static JobDataSetQueue instance = new JobDataSetQueue();
	//private JobDataSetQueue() {}
	
	/*
	public static JobDataSetQueue getInstance() {
		if(instance == null) {
			synchronized (JobDataSetQueue.class) {
				instance = new JobDataSetQueue();
			}
		}			
		
		return instance;
	}
	*/
	
	public LinkedList getLinkedList() {
		return jobs;
	}
	
	@Override
	public void resetJobCount() {
		// TODO Auto-generated method stub
		synchronized (monitor) {
			nJobCount = 0;
		}
	}

	@Override
	public void reduceJobCount() throws InterruptedException,
			NoSuchElementException {
		// TODO Auto-generated method stub
		synchronized (monitor) {
			nJobCount--;
		}
	}

	@Override
	public int getJobCount() {
		// TODO Auto-generated method stub
		return nJobCount;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return NAME;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		synchronized (monitor) {
			jobs.clear();
			nJobCount = 0;
		}
	}

	@Override
	public void put(Object obj) {
		// TODO Auto-generated method stub
		synchronized (monitor) {
			nJobCount++;
			jobs.addLast(obj);
			// 새로운 데이터가 추가되면 대기중인 스레드를 깨운다.
			monitor.notify();
		}
	}

	@Override
	public Object pop() throws InterruptedException, NoSuchElementException {
		// TODO Auto-generated method stub
		Object _obj = null;
		synchronized (monitor) {
			// 더 이상 큐에 데이터가 없으면 스레드를 대기 시킨다.
			if(jobs.isEmpty()) {
				monitor.wait();
			}
			_obj = jobs.removeFirst();
		}
		if(_obj == null) throw new NoSuchElementException();
		return _obj;
	}

	@Override
	public Object peek() throws InterruptedException, NoSuchElementException {
		// TODO Auto-generated method stub
		Object _obj = null;
		synchronized (monitor) {
			// 더 이상 큐에 데이터가 없으면 스레드를 대기 시킨다.
			if(jobs.isEmpty()) {
				monitor.wait();
			}
			_obj = jobs.peekFirst();
		}
		if(_obj == null) throw new NoSuchElementException();
		return _obj;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return jobs.size();
	}

}
