package com.gaspar.pdfutils.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.gaspar.pdfutils.DialogFillerThread;
import com.gaspar.pdfutils.PdfUtilsMain;

/**
 * This custom dialog shows pages from a selected document. It can allow to select and deselect individual pages.
 * Pages are shown using {@link ImageDisplayPanel}s.
 * @author Gáspár Tamás
 */
public class PageViewerDialog extends JDialog {

	/**
	 * Determines if the user can select or unselect pages.
	 */
	private final boolean selectAllowed;
	/**
	 * Path to the document.
	 */
	private final String path;
	/**
	 * Page numbers that needs to be shown. These are from the UI and so they are 1 based! 
	 * This can be null, which means all pages has to be shown.
	 */
	private final List<Integer> pageNumbers;
	/**
	 * Stores all page display panels so they can be iterated in the end to see which is selected. 
	 * Page numbers stored in this are 1 based!
	 */
	private final Map<Integer, ImageDisplayPanel> imagePanels = new HashMap<>();
	/**
	 * This panel indicates loading until the pages are actually loaded. The second component of this is the 
	 * actual progress bar!
	 */
	private final JPanel progressIndicatorPane = new JPanel();
	/**
	 * This is used to check if the dialog is cancelled, so the background thread can stop.
	 */
	private volatile boolean disposed;
	/**
	 * Optional password for the PDF file.
	 */
	private String password;
	
	/**
	 * Create a dialog. If there is an exception during opening the required pages, that that will be shown instead of the pages. Use 
	 * {@link #startFilling()} to start loading the pages.
	 * @param pageNumbers Shows these pages from the document. These are 1 based!
	 * @param path Path to the document.
	 * @param selectAllowed Allow or disallow selection.
	 */
	public PageViewerDialog(List<Integer> pageNumbers, String path, boolean selectAllowed) {
		super(PdfUtilsMain.getFrame(), selectAllowed ? "Select pages" : "Preview pages");
		this.selectAllowed = selectAllowed;
		this.path = path;
		this.pageNumbers = pageNumbers;
		
		setFont(new Font("SansSerif", Font.PLAIN, 15));
		setLayout(new BorderLayout());
		setResizable(false);
		setLocationRelativeTo(null);
		//if there is a selection, make it block so we can get the result
		setModalityType(selectAllowed ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
		
		getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		progressIndicatorPane.setLayout(new BorderLayout()); //set up loading indicator
		progressIndicatorPane.setBorder(new EmptyBorder(new Insets(30, 30, 30, 30)));
		JProgressBar bar = new JProgressBar(0,100);
		bar.setToolTipText("Loading images...");
		bar.setValue(0);
		
		progressIndicatorPane.add(new JLabel("Loading pages, please wait..."), BorderLayout.PAGE_START);
		progressIndicatorPane.add(bar, BorderLayout.PAGE_END);
		add(progressIndicatorPane, BorderLayout.PAGE_START);
		
		JButton okButton = new JButton("OK");
		okButton.setPreferredSize(new Dimension(100,50));
		okButton.addActionListener(e -> {
			disposed = true; //background thread will check this if it's still running
			dispose();
		});
		JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
		wrapper.add(okButton);
		add(wrapper, BorderLayout.PAGE_END);
		pack();
	}
	
	/**
	 * Create a dialog. If there is an exception during opening the required pages, that that will be shown instead of the pages. 
	 * All pages of the document will be selected.
	 * @param path Path to the document.
	 * @param selectAllowed Allow or disallow selection.
	 */
	public PageViewerDialog(String path, boolean selectAllowed) {
		this(null, path, selectAllowed);
	}
	
	/**
	 * Attempts to build the panel displaying the selected pages. This is run on a background thread using {@link DialogFillerThread}.
	 * @param document The document to build from.
	 * @return The panel.
	 * @throws IOException If the document becomes unavailable or the requested pages do not exist.
	 * @throws InterruptedException If the dialog is disposed and the background thread needs to stop as well.
	 */
	public JPanel buildPageViewerPane(final PDDocument document) throws IOException, InterruptedException {
		JPanel pageViewPanel = new JPanel(new GridLayout(0, 5, 10, 10));
		JScrollPane scroller = new JScrollPane(pageViewPanel);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		if(disposed) throw new InterruptedException();
		final PDFRenderer pdfRenderer = new PDFRenderer(document);
		if(pageNumbers != null) { //show only some pages
			int counter = 0;
			for(int pageNumber: pageNumbers) { //page numbers are from the UI and are 1 based!
				if(disposed) throw new InterruptedException();
				final BufferedImage image = pdfRenderer.renderImageWithDPI(pageNumber-1, 50, ImageType.RGB);
				final ImageDisplayPanel displayPanel = new ImageDisplayPanel(selectAllowed, "Page " + pageNumber, image); //use image display panel
				imagePanels.put(pageNumber, displayPanel);
				pageViewPanel.add(displayPanel);
				
				final int fCounter = counter++; //update progress bar
				SwingUtilities.invokeLater(() -> {
					JProgressBar bar = (JProgressBar)progressIndicatorPane.getComponent(1);
					int newPercent = (int)(100*(Double.valueOf(fCounter)/pageNumbers.size()));
					bar.setValue(newPercent);
				});
			}
		} else { //show all pages in the document
			for(int pageNumber=0; pageNumber<document.getNumberOfPages(); pageNumber++) { //page numbers are from PDFBox and are 0 based!
				if(disposed) throw new InterruptedException();
				final BufferedImage image = pdfRenderer.renderImageWithDPI(pageNumber, 50, ImageType.RGB);
				//page numbers are 0 based here, but we pass 1 based numbers in
				final ImageDisplayPanel displayPanel = new ImageDisplayPanel(selectAllowed, "Page " + (pageNumber+1), image); //use image display panel
				imagePanels.put(pageNumber + 1, displayPanel); //make it 1 based in the map
				pageViewPanel.add(displayPanel);
				
				final int fCounter = pageNumber+1; //update progress bar
				SwingUtilities.invokeLater(() -> {
					JProgressBar bar = (JProgressBar)progressIndicatorPane.getComponent(1);
					int newPercent = (int)(100*(Double.valueOf(fCounter)/document.getNumberOfPages()));
					bar.setValue(newPercent);
				});
			}
		}
		
		return pageViewPanel;
	}
	
	/**
	 * Shows the dialog. This should be used where we don't care about the result of the user selecting pages, 
	 * or when selection is not allowed.
	 */
	public void showPages() {
		pack();
		setVisible(true);
	}
	
	/**
	 * Shows the dialog. This should be used when the user's selection is important.
	 * @return The selected pages.
	 */
	public List<Integer> showPagesForResult() {
		pack();
		setVisible(true);
		List<Integer> selectedPages = new ArrayList<>();
		for(int pageNumber: imagePanels.keySet()) {
			if(imagePanels.get(pageNumber).isSelected()) selectedPages.add(pageNumber);
		}
		return selectedPages; //has 1 based numbers, ready to display in the UI
	}
	
	/**
	 * Starts loading the pages/images into the dialog using a {@link DialogFillerThread}. Must be called only after 
	 * all paramters of the {@link PageViewerDialog} are set.
	 */
	public void startFilling() {
		final DialogFillerThread fillerThread = new DialogFillerThread(this); //this will load in the pages and show them when done
		fillerThread.start();
		
	}
	
	public String getPath() {
		return path;
	}
	
	public Component getLoadingIndicator() {
		return progressIndicatorPane;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}
}
