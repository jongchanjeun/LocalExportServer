package org.ubstorm.service.parser.thread;

import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {

    public final static ThreadFactory instance = 
            new DaemonThreadFactory();

	@Override
	public Thread newThread(Runnable arg0) {
        Thread t = new Thread(arg0);
        t.setDaemon(true);
        return t;
	}

}
