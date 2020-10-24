package com.gaspar.pdfutils.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.gaspar.pdfutils.PdfUtilsMain;
import com.gaspar.pdfutils.modes.ModeExtractToImages;

/**
 * This panel displays options for the user to enter details about the 
 * image extraction, such as file path.
 * @author Gáspár Tamás
 */
public class ModeExtractToImagesPanel extends JPanel {

	/**
	 * Contains the path of the source PDF.
	 */
	private final JTextField sourcePathField = new JTextField();
	/**
	 * Contains the path of the folder where the images will be placed.
	 */
	private final JTextField destPathField = new JTextField();
	/**
	 * Optionally, enter the password for the PDF here.
	 */
	private final JPasswordField passwordField = new JPasswordField();
	
	public ModeExtractToImagesPanel() {
		final JPanel container = new JPanel();
		container.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		final BoxLayout gl = new BoxLayout(container, BoxLayout.Y_AXIS);
		container.setAlignmentX(LEFT_ALIGNMENT);
		container.setLayout(gl);
		
		final Font font = new Font("SansSerif", Font.PLAIN, 15);
		addSourceDestInputs(container, font);
		
		addPageSelectingTools(container, font);
		
		add(container);
	}
	
	/**
	 * Add tools to select source and destination path. Password for the PDF must be inputed here.
	 * @param container Will be added to this.
	 * @param font Texts will have this font.
	 */
	private void addSourceDestInputs(final JPanel container, final Font font) {
		JLabel sourceDesc = new JLabel("Select the source PDF file:");
		sourceDesc.setBorder(new EmptyBorder(new Insets(0, 20, 0, 20)));
		sourceDesc.setFont(font);
		container.add(sourceDesc);
		
		final FlowLayout fl = new FlowLayout(20);
		final JPanel sourcePanel = new JPanel(fl);
		JLabel pathLabel = new JLabel("Path:");
		pathLabel.setFont(font);
		sourcePanel.add(pathLabel);
		sourcePathField.setColumns(30);
		sourcePanel.add(sourcePathField);
		final JButton selectSourceButton = new JButton("Browse");
		selectSourceButton.setFont(font);
		selectSourceButton.addActionListener(Listeners.openPdfFileSelector(sourcePathField));
		sourcePanel.add(selectSourceButton);
		container.add(sourcePanel);
		
		JPanel passwordFlow = new JPanel(new FlowLayout(20));
		JLabel pwdLabel = new JLabel("Password for PDF:");
		pwdLabel.setFont(font);
		passwordFlow.add(pwdLabel);
		passwordField.setToolTipText("Enter password here if the PDF is protected. Leave it empty if it has no password!");
		passwordField.setColumns(10);
		passwordFlow.add(passwordField);
		container.add(passwordFlow);
		
		JLabel destDesc = new JLabel("Select the destination folder for the images:");
		destDesc.setBorder(new EmptyBorder(new Insets(0, 20, 0, 20)));
		destDesc.setFont(font);
		container.add(destDesc);
		
		final JPanel destPanel = new JPanel(fl);
		JLabel pathLabel2 = new JLabel("Path:");
		pathLabel2.setFont(font);
		destPanel.add(pathLabel2);
		destPathField.setColumns(30);
		destPanel.add(destPathField);
		final JButton selectDestButton = new JButton("Browse");
		selectDestButton.setFont(font);
		selectDestButton.addActionListener(Listeners.openFolderSelector(destPathField));
		destPanel.add(selectDestButton);
		container.add(destPanel);
	}
	
	private static final String PAGE_RANGE = "pr", INDIVIDUAL_PAGES = "ip";
	
	/**
	 * Add tools to select pages to be extracted.
	 * @param container Will be added to this.
	 * @param font Texts will have this font.
	 */
	private void addPageSelectingTools(JPanel container, Font font) {
		JLabel pagesDesc = new JLabel("Choose the pages to extract:");
		pagesDesc.setBorder(new EmptyBorder(new Insets(0, 20, 0, 20)));
		pagesDesc.setFont(font);
		container.add(pagesDesc);
		
		final CardLayout cardLayout = new CardLayout();
		final JPanel pageSelectCards = new JPanel(cardLayout);
		
		FlowLayout fl = new FlowLayout(20);
		JPanel radioButtons = new JPanel(fl);
		final JRadioButton radioButtonPageRange = new JRadioButton("Range of pages");
		radioButtonPageRange.setSelected(true);
		radioButtonPageRange.setFont(font);
		radioButtonPageRange.addActionListener(e -> cardLayout.show(pageSelectCards, PAGE_RANGE));
		radioButtons.add(radioButtonPageRange);
		final JRadioButton radioButtonIndividualPages = new JRadioButton("Individual pages");
		radioButtonIndividualPages.addActionListener(e -> cardLayout.show(pageSelectCards, INDIVIDUAL_PAGES));
		radioButtonIndividualPages.setFont(font);
		radioButtons.add(radioButtonIndividualPages);
		ButtonGroup radioGroup = new ButtonGroup(); //group them
		radioGroup.add(radioButtonIndividualPages);
		radioGroup.add(radioButtonPageRange);
		container.add(radioButtons);
		
		//fill the card layout
		pageSelectCards.add(createPageRangePanel(font), PAGE_RANGE);
		pageSelectCards.add(createIndividualRangePanel(font), INDIVIDUAL_PAGES);
		container.add(pageSelectCards);
		cardLayout.show(pageSelectCards, PAGE_RANGE);
	}

	/**
	 * Create the panel that allows the user to select pages with a range.
	 * @param Font Applied to the texts.
	 * @return The panel.
	 */
	private JPanel createPageRangePanel(final Font font) {
		JPanel pageRangePanel = new JPanel(new GridLayout(0,1));
		
		JPanel pageRangeFlow = new JPanel(new FlowLayout(20));
		
		JLabel fromLabel = new JLabel("From this page:");
		fromLabel.setFont(font);
		fromLabel.setToolTipText("Extraction will start at this page. Must be a valid page number for the selected PDF.");
		pageRangeFlow.add(fromLabel);
		JTextField fromField = new JTextField();
		fromField.setColumns(2);
		pageRangeFlow.add(fromField);
		JLabel toLabel = new JLabel("To this page:");
		toLabel.setFont(font);
		toLabel.setToolTipText("Pages will be extracted up to this page (inclusive). Must be a valid page number for the selected PDF.");
		pageRangeFlow.add(toLabel);
		JTextField toField = new JTextField();
		toField.setColumns(2);
		pageRangeFlow.add(toField);
		
		pageRangePanel.add(pageRangeFlow);
		
		JPanel pageRangeFlow2 = new JPanel(new FlowLayout(20)); //prefix
		JLabel prefixLabel = new JLabel("Image prefix: ");
		pageRangeFlow2.add(prefixLabel);
		JTextField prefixField = new JTextField("img_");
		prefixField.setToolTipText("Generated images will be prefixed with this. Must not be empty.");
		prefixField.setColumns(5);
		pageRangeFlow2.add(prefixField);
		pageRangePanel.add(pageRangeFlow2);
		
		JButton executeOperationButton = new JButton("Extract pages");
		executeOperationButton.setFont(font);
		executeOperationButton.addActionListener(e ->
			ModeExtractToImages.attemptImageExtraction(fromField.getText(),
													   toField.getText(),
													   prefixField.getText(),
													   sourcePathField.getText(),
													   destPathField.getText(),
													   new String(passwordField.getPassword()))
		);
		
		final JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
		wrapper.add(executeOperationButton);
		pageRangePanel.add(wrapper);
		
		return pageRangePanel;
	}
	
	/**
	 * Create the panel that allows the user to select individual pages. This happens with a dialog that displays the pages.
	 * @param Font Applied to the texts.
	 * @return The panel.
	 */
	private JPanel createIndividualRangePanel(final Font font) {
		JPanel individualPagesPanel = new JPanel(new GridLayout(0,1));
		
		return individualPagesPanel;
	}
}
