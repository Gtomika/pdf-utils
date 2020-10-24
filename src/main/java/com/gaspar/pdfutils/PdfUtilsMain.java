package com.gaspar.pdfutils;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.apache.pdfbox.contentstream.operator.graphics.LegacyFillNonZeroRule;

import com.gaspar.pdfutils.gui.Listeners;
import com.gaspar.pdfutils.gui.RootPanel;
import com.gaspar.pdfutils.modes.Mode;
import com.gaspar.pdfutils.modes.ModeExtractToImages;
import com.gaspar.pdfutils.modes.ModeExtractToPdf;
import com.gaspar.pdfutils.modes.ModeImagesToPdf;

public class PdfUtilsMain {
	
	/**
	 * Main frame of the GUI. Null if we are in legacy mode.
	 */
	private static JFrame frame;
	
	/**
	 * Entry point. If there are command line arguments if will start in legacy (console) mode. Otherwise, there will be a GUI.
	 * @param args Optional, if present then first parameter must be the mode (from {@link Mode}), then the arguments for this mode.
	 * @throws IOException Some file access error.
	 */
	public static void main(String[] args) throws IOException {
		if(args.length > 0) { //start in legacy mode
			Mode mode = parseMode(args);
			String source, dest;
			try(Scanner scanner = new Scanner(System.in)) {
				System.out.println("Enter the source file path: ");
				source = scanner.nextLine();
				System.out.println("Enter the path where the result should be placed: ");
				dest = scanner.nextLine();
			}
			System.out.println("Depending on the PDF size, this may take some time. Please wait...");
			mode.execute(source, dest);
			System.out.println("Operation completed.");
		} else { //create GUI
			frame = new JFrame("PDF Utilities");
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //closing is handles in the window close listener
			frame.addWindowListener(Listeners.getWindowCloseListener());
	        frame.setLocationRelativeTo(null);
	        frame.setIconImage(new ImageIcon(PdfUtilsMain.class.getResource("/res/icon.png")).getImage());
	        frame.getContentPane().add(new RootPanel(), BorderLayout.CENTER); //RootPanel will take care of the GUI
	        frame.pack();
	        frame.setVisible(true);
		}
	}
	
	/**
	 * Determines and creates the {@link Mode} from the command line arguments.
	 * @param args Command line arguments.
	 * @return The parsed mode.
	 */
	private static Mode parseMode(String[] args) {
		Mode mode = null;
		if(args.length == 0) throw new IllegalArgumentException("No mode specified!");
		String modeNameInput = args[0];
		
		switch (modeNameInput) {
		case Mode.MODE_EXTRACT_TO_IMAGES_LEGACY:
			try {
				int fromPage = Integer.parseInt(args[1]);
				int toPage = Integer.parseInt(args[2]);
				if(args.length > 3) { //custom image prefix
					mode = new ModeExtractToImages(fromPage, toPage, args[3]);
				} else {
					mode = new ModeExtractToImages(fromPage, toPage);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid extract to image mode arguments! They must be: pageFrom, pageTo, [optional imagePrefix].");
			}
			break;
		case Mode.MODE_EXTRACT_TO_PDF_LEGACY:
			try {
				int fromPage = Integer.parseInt(args[1]);
				int toPage = Integer.parseInt(args[2]);
				String fileName = args[3];
				mode = new ModeExtractToPdf(fromPage, toPage, fileName);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid extract to PDF mode arguments! They must be: pageFrom, pageTo, fileName.");
			}
			break;
		case Mode.MODE_IMAGES_TO_PDF_LEGACY:
			try {
				String fileName = args[1];
				if(args.length > 2) { //custom prefix
					mode = new ModeImagesToPdf(fileName, args[2]);
				} else { //no prefix
					mode = new ModeImagesToPdf(fileName);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid combine images mode arguments! They must be: fileName, [optional imagePrefix].");
			}
			break;
		default:
			throw new IllegalArgumentException("Unrecognized mode: " + modeNameInput);
		}
		return mode;
	}
	
	public static JFrame getFrame() {
		return frame;
	}
}
