package com.gaspar.pdfutils;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.gaspar.pdfutils.gui.Listeners;
import com.gaspar.pdfutils.gui.RootPanel;

public class PdfUtilsMain {
	
	/**
	 * Main frame of the GUI. Null if we are in legacy mode.
	 */
	private static JFrame frame;
	
	/**
	 * Entry point. There will be a GUI to specify what operation to do.
	 * @param args Does not matter.
	 */
	public static void main(String[] args) {
		frame = new JFrame("PDF Utilities");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //closing is handles in the window close listener
		frame.addWindowListener(Listeners.getWindowCloseListener());
        frame.setLocationRelativeTo(null);
        frame.setIconImage(new ImageIcon(PdfUtilsMain.class.getResource("/res/icon.png")).getImage());
        
        final RootPanel root = RootPanel.getInstance();
        frame.getContentPane().add(root, BorderLayout.CENTER); //RootPanel will take care of the GUI
        frame.pack();
        frame.setVisible(true);
	}
	
	public static JFrame getFrame() {
		return frame;
	}
}
