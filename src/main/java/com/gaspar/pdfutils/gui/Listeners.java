package com.gaspar.pdfutils.gui;

import java.awt.Desktop;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.gaspar.pdfutils.OperationThread;
import com.gaspar.pdfutils.PdfUtilsMain;
import com.gaspar.pdfutils.modes.ModeImagesToPdf;

/**
 * Contains listeners for buttons so they dont pollute the code.
 * @author Gáspár Tamás
 */
public abstract class Listeners {

	/**
	 * Helper method that asks for confirmation before exiting if there is an ongoing process. Used by other listeners.
	 * @see #getWindowCloseListener()
	 * @see #getExitListener()
	 */
	private static void confirmExit() {
		if(OperationThread.isOperationOngoing()) { //a process is ongoing
			int choice = JOptionPane.showConfirmDialog(PdfUtilsMain.getFrame(), "Exiting now will cancel the ongoing operation. Are you sure?", 
					"Operation ongoing", JOptionPane.WARNING_MESSAGE);
			if(choice == JOptionPane.OK_OPTION) {
				PdfUtilsMain.getFrame().dispose();
			}
		} else { //nothing happening, exit is safe
			PdfUtilsMain.getFrame().dispose();
		}
	}
	
	/**
	 * This listener can be added to a frame, and it will ask for confirmation before quitting.
	 * @see Listeners#confirmExit()
	 * @return The window listener.
	 */
	public static WindowListener getWindowCloseListener() {
		return new WindowListener() {
			@Override public void windowOpened(WindowEvent e) {}
			@Override public void windowIconified(WindowEvent e) {}
			@Override public void windowDeiconified(WindowEvent e) {}
			@Override public void windowDeactivated(WindowEvent e) {}
			@Override public void windowClosed(WindowEvent e) {}
			@Override public void windowActivated(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {
				confirmExit();
			}
		};
	}
	
	/**
	 * This listener can be added to a button and it asks for confirmation before exiting if there is an ongoing process.
	 * @return The action listener
	 */
	public static ActionListener getExitListener() {
		return e -> confirmExit();
	}
	
	/**
	 * This listener can be added to a button and it opens up the project GitHub repository, 
	 * if this is possible.
	 * @return The action listener.
	 */
	public static ActionListener getGithubOpenListener() {
		return e -> {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			    try {
					Desktop.getDesktop().browse(new URI("https://github.com/Gtomika/pdf-utils"));
				} catch (IOException | URISyntaxException e1) {
					JOptionPane.showMessageDialog(PdfUtilsMain.getFrame(), "Link: https://github.com/Gtomika/pdf-utils", "Failed to open", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
	}

	/**
	 * This listener can be added to buttons and it will open a file selector to choose a pdf file.
	 * @param textField The selected file path will be displayed in this.
	 * @return The action listener.
	 */
	public static ActionListener openPdfFileSelector(final JTextField textField) {
		return e -> {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileFilter() {
				@Override public String getDescription() {return null;}
				@Override
				public boolean accept(File f) {
					return f.getPath().endsWith(".pdf") || f.isDirectory();
				}
			});
			fileChooser.setDialogTitle("Select a PDF file!");
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setAcceptAllFileFilterUsed(false);
			int result = fileChooser.showOpenDialog(PdfUtilsMain.getFrame());
			if(result == JFileChooser.APPROVE_OPTION) {
				textField.setText(fileChooser.getSelectedFile().getPath());
			}
		};
	}

	/**
	 * This listener can be added to buttons and it will open a file selector to choose a FOLDER.
	 * @param textField The selected folder path will be displayed in this.
	 * @return The action listener.
	 */
	public static ActionListener openFolderSelector(final JTextField textField) {
		return e -> {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Select a folder!");
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fileChooser.showOpenDialog(PdfUtilsMain.getFrame());
			if(result == JFileChooser.APPROVE_OPTION) {
				textField.setText(fileChooser.getSelectedFile().getPath());
			}
		};
	}

	/**
	 * This listener can be added to buttons and it will open up a {@link JFileChooser} to open 
	 * images from a folder.
	 * @param images Save the result in this list, without removing its contents.
	 * @param resultField Show the selected files in this field (all of them, not only the ones selected just now).
	 * @return The listener.
	 */
	public static ActionListener openImageSelector(final List<File> images, final JTextArea resultField) {
		return e -> {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Select the images");
			fileChooser.setMultiSelectionEnabled(true);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(new FileFilter() {
				@Override public String getDescription() { return null; }
				@Override
				public boolean accept(File f) {
					if(f.isDirectory()) return true;
					//supported image formats: JPG, JPEG, TIF, TIFF, GIF, BMP and PNG
					String fileName = Paths.get(f.getPath()).getFileName().toString();
					for(String supported: ModeImagesToPdf.SUPPORTED_FORMATS) {
						if(fileName.endsWith("." + supported)) return true;
					}
					return false;
				}
			});
			int result = fileChooser.showOpenDialog(PdfUtilsMain.getFrame());
			if(result == JFileChooser.APPROVE_OPTION) {
				images.addAll(Arrays.asList(fileChooser.getSelectedFiles()));
				//update text field
				StringBuilder sb = new StringBuilder();
				for(int i=0; i<images.size(); i++) {
					sb.append(Paths.get(images.get(i).getPath()).getFileName().toString());
					if(i < images.size() - 1) sb.append(", ");
				}
				resultField.setText(sb.toString());
				resultField.revalidate();
				resultField.repaint();
			}
		};
	}
}
