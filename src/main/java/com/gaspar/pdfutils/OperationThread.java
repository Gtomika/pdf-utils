package com.gaspar.pdfutils;

import java.awt.Cursor;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.gaspar.pdfutils.gui.RootPanel;

/**
 * This thread executes PDF operations in the background to maintain responsive UI. Operation status is displayed on the root panel, 
 * and is manages with {@link RootPanel#changeOperationPanel(String)} and {@link RootPanel#updateOperationProgress(int)}.
 * <p>
 * It's important to use this for the actual operations, because this updates the UI to show progress and updates ongoing operation flag.
 * <p>
 * Only one operation is allowed at any time, otherwise the GUI progress would be messed up. If a second operation is launched, this thread 
 * instead displays a dialog informing the user about this.
 * @author Gáspár Tamás
 */
public class OperationThread extends Thread {

	/**
	 * Stores if there is an ongoing operation. Checked before exiting the application.
	 */
	private static volatile boolean operationOngoing = false;
	/**
	 * Stores the operation.
	 */
	private Runnable r;
	/**
	 * Create an operation thread.
	 * @param r The operation.
	 */
	public OperationThread(Runnable r) {
		setDaemon(true);
		this.r = r;
	}
	
	/**
	 * Executes the operation. Before executing it updates the GUI to show that an operation is in progress and sets the operation flag to true.
	 * After the operation is done, it sets the flag to false and updates the GUI.
	 */
	@Override
	public void run() {
		if(operationOngoing) { //attempt to run another operation
			JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), "An other operation is already ongoing. Please wait until it finishes!",
					"Operation ongoing", JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			setOperationOngoing(true);
			SwingUtilities.invokeLater(() -> {
				RootPanel.getInstance().changeOperationPanel(RootPanel.OPERATION_IN_PROGRESS); //update GUI to show ongoing operation
				PdfUtilsMain.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			});
			r.run(); //what is inside this method will update the progress on the UI
		} finally {
			SwingUtilities.invokeLater(() -> {
				RootPanel.getInstance().changeOperationPanel(RootPanel.NO_OPERATION);
				PdfUtilsMain.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			});
			setOperationOngoing(false);
		}
	}

	public static synchronized boolean isOperationOngoing() {
		return operationOngoing;
	}
	
	private static synchronized void setOperationOngoing(boolean ongoing) {
		operationOngoing = ongoing;
	}
}
