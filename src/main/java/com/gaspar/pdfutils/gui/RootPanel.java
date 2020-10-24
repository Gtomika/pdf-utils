package com.gaspar.pdfutils.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.gaspar.pdfutils.PdfUtilsMain;

/**
 * This is the root layout of the {@link PdfUtilsMain#getFrame()}.
 * @author Gáspár Tamás
 */
public class RootPanel extends JPanel {

	/**
	 * Icons will be displayed in this size.
	 */
	public static final Dimension ICON_SIZE = new Dimension(50,50);
	
	/**
	 * Displays a title depending on the selected mode.
	 */
	private final JLabel titleLabel = new JLabel();
	
	/**
	 * This button is displayed at the bottom, and allows the user to navigate back. If the top level menu is 
	 * displayed then this button becomes an exit button.
	 */
	private final JButton backButton = new JButton("Exit");
	
	public RootPanel() {
		super();
		BorderLayout rootLayout = new BorderLayout(5,5);
		setLayout(rootLayout);
		titleLabel.setText("Select an option!"); //customize global components, they are added later
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		backButton.setMargin(new Insets(10, 10, 10, 10));
		backButton.addActionListener(Listeners.getExitListener());
		backButton.setFont(new Font("SansSerif",Font.PLAIN, 15));
		add(createNorthPanel(), BorderLayout.PAGE_START);
		add(createSouthPanel(), BorderLayout.PAGE_END);
		
		//set the central part as the mode selector
		add(new ModeSelectPanel(this), BorderLayout.CENTER);
	}
	
	/**
	 * Builds the panel that is always on display on the top of the frame. Shows the current title ({@link #titleLabel} and an 
	 * image button which links to the project's GitHub repository.
	 * @return North Panel
	 */
	private JPanel createNorthPanel() {
		BorderLayout nl = new BorderLayout(5, 5);
		JPanel northPanel = new JPanel(nl); //returned object
		
		Image githubIcon = new ImageIcon(getClass().getResource("/res/github_icon.png")).getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT);
		JButton githubButton = new JButton(new ImageIcon(githubIcon));
		githubButton.addActionListener(Listeners.getGithubOpenListener());
		githubButton.setBorder(new CompoundBorder(githubButton.getBorder(), new EmptyBorder(new Insets(10, 10, 10, 10))));
		githubButton.setPreferredSize(ICON_SIZE);
		JPanel wrapper = new JPanel(new FlowLayout()); //this must be used to have a margin...
		wrapper.add(githubButton);
		northPanel.add(wrapper, BorderLayout.LINE_END);
		
		FlowLayout flow = new FlowLayout(FlowLayout.LEFT);
		flow.setHgap(5);
		flow.setVgap(5);
		JPanel flowPanel = new JPanel(flow);
		JLabel pdfIcon = new JLabel(new ImageIcon(new ImageIcon(getClass().getResource("/res/icon.png")).getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT)));
		pdfIcon.setPreferredSize(ICON_SIZE);
		flowPanel.add(pdfIcon);
		flowPanel.add(titleLabel);
		northPanel.add(flowPanel, BorderLayout.LINE_START);
		return northPanel;
	}
	
	/**
	 * Builds the always visible south panel, which contains the {@link #backButton}.
	 * @return South Panel
	 */
	private JPanel createSouthPanel() {
		FlowLayout sl = new FlowLayout(5);
		sl.setAlignment(FlowLayout.LEFT);
		JPanel southPanel = new JPanel(sl);
		southPanel.add(backButton);
		return southPanel;
	}

	public JLabel getTitleLabel() {
		return titleLabel;
	}

	public JButton getBackButton() {
		return backButton;
	}
}
