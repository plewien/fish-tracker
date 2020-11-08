package com.tracker.view;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;

public class Test extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4180855866865544759L;

	/**
	 * Create the panel.
	 */
	public Test() {
		setLayout(new BorderLayout(0, 0));
		
		JLabel label = new JLabel("00:20");
		add(label, BorderLayout.WEST);
		
		JLabel label_1 = new JLabel("20:00");
		add(label_1, BorderLayout.EAST);

	}

}
