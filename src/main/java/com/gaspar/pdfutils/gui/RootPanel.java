package com.gaspar.pdfutils.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.gaspar.pdfutils.PdfUtilsMain;

/**
 * This is the root layout of the {@link PdfUtilsMain#getFrame()}. This is a singleton
 * @author Gáspár Tamás
 */
public class RootPanel extends JPanel {
	
	/**
	 * Singleton instance.
	 */
	private static final RootPanel instance = new RootPanel();
	/**
	 * Get the instance of the root panel.
	 * @return The instance.
	 */
	public static synchronized RootPanel getInstance() {
		return instance;
	}

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
	
	private RootPanel() {
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
		add(new ModeSelectPanel(), BorderLayout.CENTER);
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
		wrapper.setPreferredSize(new Dimension(60,60));
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
	 * Builds the always visible south panel, which contains the {@link #backButton} and {@link #operationPanel}.
	 * @return South Panel
	 */
	private JPanel createSouthPanel() {
		JPanel southPanel = new JPanel(new BorderLayout());
		JPanel wrapper = new JPanel(new FlowLayout(5));
		wrapper.add(backButton);
		southPanel.add(wrapper, BorderLayout.LINE_START);
		
		southPanel.add(createOperationProgressPanel(), BorderLayout.LINE_END);
		return southPanel;
	}
	
	/**
	 * Card layout ID to show no operation text.
	 */
	public static final String NO_OPERATION = "no_op";
	/**
	 * Card layout ID to show operation done text.
	 */
	public static final String OPERATION_DONE = "op_done";
	/**
	 * Card layout ID to show progress bar.
	 */
	public static final String OPERATION_IN_PROGRESS = "op_in_prog";
	/**
	 * Controls {@link #operationPanel}. Use {@link #changeOperationPanel(String)} to update it.
	 */
	private final CardLayout operationCards = new CardLayout();
	/**
	 * Shows the operation status. Use {@link #changeOperationPanel(String)} 
	 * and {@link #updateOperationProgress(int)} to control it.
	 */
	private final JPanel operationPanel = new JPanel(operationCards);
	/**
	 * Displays the progress of an ongoing operation. Only shown when {@link #operationCards} is showing {@link #OPERATION_IN_PROGRESS}.
	 * Use {@link #updateOperationProgress(int)} to update the value.
	 */
	private JProgressBar operationBar = new JProgressBar(0,100);
	
	/**
	 * Builds {@link #operationPanel} that displays the ongoing operation. This is a card layout panel. Use {@link #changeOperationPanel(String)} 
	 * and {@link #updateOperationProgress(int)} to control it.
	 * @return Operation panel
	 */
	private JPanel createOperationProgressPanel() {
		final Font f = new Font("SansSerif",Font.PLAIN,15);
		operationPanel.setAlignmentY(RIGHT_ALIGNMENT);
		JLabel noOpLabel = new JLabel("No ongoing operation.");
		noOpLabel.setFont(f);
		operationPanel.add(noOpLabel, NO_OPERATION);
		JLabel opDoneLabel = new JLabel("Operation complete.");
		opDoneLabel.setFont(f);
		operationPanel.add(opDoneLabel, OPERATION_DONE);
		
		final FlowLayout fl = new FlowLayout(20);
		fl.setAlignment(FlowLayout.RIGHT);
		JPanel progressPanel = new JPanel(fl);
		JLabel l = new JLabel("Operation in progress");
		l.setFont(f);
		progressPanel.add(l);
		operationBar.setValue(0);
		progressPanel.add(operationBar);
		operationPanel.add(progressPanel, OPERATION_IN_PROGRESS);
		
		operationCards.show(operationPanel, NO_OPERATION); //no operations happening on start
		return operationPanel;
	}
	
	/**
	 * Updates the operation panel to display something else.
	 * @param status Should be one of {@link #NO_OPERATION}, {@link #OPERATION_DONE} or {@link #OPERATION_IN_PROGRESS}.
	 */
	public void changeOperationPanel(String status) {
		operationCards.show(operationPanel, status);
	}
	
	/**
	 * Updates {@link #operationBar}.
	 * @param progress The new progress. Must be between 0 and 100.
	 */
	public void updateOperationProgress(int progress) {
		operationBar.setValue(progress);
	}

	public JLabel getTitleLabel() {
		return titleLabel;
	}

	public JButton getBackButton() {
		return backButton;
	}
}
