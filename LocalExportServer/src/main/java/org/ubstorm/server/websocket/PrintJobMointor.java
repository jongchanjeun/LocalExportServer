package org.ubstorm.server.websocket;

import java.util.Date;

import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

public class PrintJobMointor {
	
    // True iff it is safe to close the print job's input stream
    private boolean done = false;
    private DocPrintJob myJob = null;
    private PrintService mMySrvice = null;
    
     PrintJobMointor(DocPrintJob job, PrintService service) {
    	 this.myJob = job;
    	 this.mMySrvice = service;
    	 
    	 // Add a listener to the print job
        job.addPrintJobListener(new PrintJobAdapter() {
            public  void printJobCanceled(PrintJobEvent printJobEvent) {
                System.out.println("Print job canceled");
            	allDone();
            }
            
            public  void printJobCompleted(PrintJobEvent printJobEvent) {
            	System.out.println("Print job completed");
                allDone();
            }
            
            public  void printJobFailed(PrintJobEvent printJobEvent) {
            	System.out.println("Print job failed");
                allDone();
            }

            public  void printJobNoMoreEvents(PrintJobEvent printJobEvent) {
                allDone();
            }

             void allDone() {
                synchronized (PrintJobMointor.this) {
                    done = true;
                    PrintJobMointor.this.notify();
                }
            }
        });
    }

    /**
     * Waits for print job
     *
     */
    public synchronized  void waitForPrintJob() {
        try {
        	System.out.println("Waiting for print job...");
            while (!done) {
            	wait();
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            System.out.println("=== Polling: I'm alive and it's " + new Date());
            System.out.println("Job attributes");
            for (Attribute attribute : this.myJob.getAttributes().toArray()) {
                System.out.println((attribute.getCategory().getName() + "/"
                        + attribute.getName() + " = " + attribute.toString()));
            }
            
            if(this.mMySrvice != null)
            {
	            System.out.println("Service attributes");
	            for (Attribute attribute : this.mMySrvice.getAttributes().toArray()) {
	                System.out.println((attribute.getCategory().getName() + "/"
	                        + attribute.getName() + " = " + attribute.toString()));
	            }
            }
            System.out.println("Finished waiting for print");
        } catch (InterruptedException e) {
        	System.out.println("Failed waiting for print job: " + e.getMessage());
        }
    }
}
