package com.gaspar.pdfutils;

/**
 * This thread executes PDF operations in the background. When we are in legacy mode, this is not used, and 
 * operations are done on the main thread. In GUI mode, however, this is used to maintain a responsive UI.
 * @author Gáspár Tamás
 */
public class OperationThread extends Thread {

	/**
	 * Stores if there is an ongoing operation. Checked before exiting the application.
	 */
	private static volatile boolean operationOngoing = false;
	
	private Runnable r;
	
	public OperationThread(Runnable r) {
		setDaemon(true);
		this.r = r;
	}
	
	@Override
	public void run() {
		setOperationOngoing(true);
		r.run();
		setOperationOngoing(false);
	}

	public static synchronized boolean isOperationOngoing() {
		return operationOngoing;
	}
	
	private static synchronized void setOperationOngoing(boolean ongoing) {
		operationOngoing = ongoing;
	}
}
