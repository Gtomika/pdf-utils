package com.gaspar.pdfutils.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * This panel displays a single immage, most likely a PDF page. Can also show page number, and selection.
 * {@link PageViewerDialog} displays these for each page in a grid.
 * @author Gáspár Tamás
 */
public class ImageDisplayPanel extends JPanel {
	
	/**
	 * True if this page is selected. This only matters if {@link #selectionAllowed} is true.
	 */
	private boolean selected;
	
	/**
	 * Constructor with all parameters specified.
	 * @param selectionAllowed Determines if selection is allowed.
	 * @param labelText Text that is shown above the image.
	 * @param image The image to be displayed.
	 */
	public ImageDisplayPanel(boolean selectionAllowed, String labelText, BufferedImage image) {
		selected = false; 
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(200,250));
		if(labelText != null) add(new JLabel(labelText), BorderLayout.PAGE_START); //add label
		
		Image scaled = new ImageIcon(image).getImage().getScaledInstance(180, 210, Image.SCALE_FAST);
		JLabel imageLabel = new JLabel(new ImageIcon(scaled));
		add(imageLabel, BorderLayout.CENTER); //add image
		
		if(selectionAllowed) { //add click listener
			final Color defaultColor = UIManager.getColor("Panel.background");
			final Color selectedColor = new Color(68, 85, 90);
			
			imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			imageLabel.addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseClicked(MouseEvent e) {
			        selected = !selected;
			        setBackground(selected ? selectedColor : defaultColor);
				}
			});
		}
		
	}
	
	public boolean isSelected() {
		return selected;
	}
}
