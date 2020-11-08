package com.tracker.view;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.tracker.model.TrackerModel;

public class VideoControlPanel extends JPanel implements Observer {
	private static final long serialVersionUID = -8130131613272786062L;
	private JButton playButton, pauseButton;
	
	/**
	 * Constructs a control panel for managing the playing/pausing of the movie.
	 * @param model the fish tracker model.
	 */
	public VideoControlPanel(TrackerModel model) {
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{60, 60, 60, 60, 0};
		gbl_panel.rowHeights = new int[]{30, 25, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gbl_panel);
		
		JLabel lblVideoControls = new JLabel("Video Controls");
		lblVideoControls.setHorizontalAlignment(SwingConstants.CENTER);
		lblVideoControls.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblVideoControls = new GridBagConstraints();
		gbc_lblVideoControls.insets = new Insets(0, 0, 5, 0);
		gbc_lblVideoControls.gridwidth = 4;
		gbc_lblVideoControls.fill = GridBagConstraints.BOTH;
		gbc_lblVideoControls.gridx = 0;
		gbc_lblVideoControls.gridy = 0;
		add(lblVideoControls, gbc_lblVideoControls);
		
		playButton = new JButton("Play");
		GridBagConstraints gbc_playButton = new GridBagConstraints();
		gbc_playButton.insets = new Insets(0, 0, 0, 5);
		gbc_playButton.fill = GridBagConstraints.BOTH;
		gbc_playButton.gridx = 1;
		gbc_playButton.gridy = 1;
		add(playButton, gbc_playButton);
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.resumeMovie();
			}
		});

		pauseButton = new JButton("Pause");
		GridBagConstraints gbc_pauseButton = new GridBagConstraints();
		gbc_pauseButton.insets = new Insets(0, 0, 0, 5);
		gbc_pauseButton.fill = GridBagConstraints.BOTH;
		gbc_pauseButton.gridx = 2;
		gbc_pauseButton.gridy = 1;
		add(pauseButton, gbc_pauseButton);
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.pauseMovie();
			}
		});		
		
		model.addObserver(this);
		update(model, null);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof TrackerModel) {
			if (((TrackerModel) o).isPaused()) {
				pauseButton.setEnabled(false);
				playButton.setSelected(false);
				playButton.setEnabled(true);
			} else {
				playButton.setEnabled(false);
				pauseButton.setSelected(false);
				pauseButton.setEnabled(true);
			}
		}
	}	
}
