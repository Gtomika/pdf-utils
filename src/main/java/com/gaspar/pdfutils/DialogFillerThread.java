package com.gaspar.pdfutils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import com.gaspar.pdfutils.gui.PageViewerDialog;

/**
 * This thread loads images/PDF pages into an {@link PageViewerDialog}. When it finishes, it will update the 
 * GUI to show the images/pages. It basically calls {@link PageViewerDialog#buildPageViewerPane(org.apache.pdfbox.pdmodel.PDDocument)} in the background.
 * <p>
 * This is differemt from {@link OperationThread}, this won't count as an operation, so the program can close if this is 
 * working.
 * @author Gáspár Tamás
 */
public class DialogFillerThread extends Thread {

	private final PageViewerDialog dialog;
	
	public DialogFillerThread(final PageViewerDialog dialog) {
		setDaemon(true);
		this.dialog = dialog;
	}
	
	@Override
	public void run() {
		boolean interrupted = false;
		JComponent component = null; //this will be added to the dialog
		String path = dialog.getPath();
		//attempt to open given PDF file
		try(PDDocument document = dialog.getPassword()==null ? PDDocument.load(new File(path)) : PDDocument.load(new File(path), dialog.getPassword())) {
			component = dialog.buildPageViewerPane(document);
		} catch(InvalidPasswordException e) {
			//password incorrect
			component = new JPanel(new FlowLayout(FlowLayout.CENTER));
			component.setPreferredSize(new Dimension(300,50));
			JLabel label = new JLabel("Incorrect password for this PDF file!");
			component.add(label);
		} catch(IOException e) {
			//PDF not found, requested pages not found, ...
			component = new JPanel(new FlowLayout(FlowLayout.CENTER));
			component.setPreferredSize(new Dimension(500,50));
			JLabel label = new JLabel("Failed to open pages. Check if selected file exists and you have permissions to open it!");
			component.add(label);
		} catch (InterruptedException e) {
			interrupted = true;
		}
		if(!interrupted) { //if it was interrupted, then the dialog is disposed, and this is not important anymore
			final Component compCopy = component;
			SwingUtilities.invokeLater(() -> { //will be added on the main thread
				dialog.remove(dialog.getLoadingIndicator());
				dialog.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				dialog.add(compCopy, BorderLayout.PAGE_START);
				dialog.revalidate();
				dialog.pack();
			});
		}
	}
	
	public void kill() {
		
	}
}
