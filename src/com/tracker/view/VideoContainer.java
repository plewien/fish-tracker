package com.tracker.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.opencv.core.Core;

import com.tracker.model.TrackerModel;

public class VideoContainer extends JPanel implements Observer {
	private static final long serialVersionUID = -5809526235390531501L;
	private JLabel lblCommand, currentTimeLabel, totalTimeLabel;
	private JSlider movieSeekBar;
	private double fps;
	
	public VideoPanel videoPanel;
	
	/**
	 * Unit test for displaying the video container panel.
	 * @param args
	 */
	public static void main(String[] args) {
		// load library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrame frame = new JFrame();
					TrackerModel model = new TrackerModel(new File("test1.mp4"));
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.add(new VideoContainer(model));
					frame.pack();
					frame.setVisible(true);					
				} catch (Exception e) {
					System.err.println("Error: Unable to load panel");
					e.printStackTrace();
				}
			}
		});
	}
	
	public VideoContainer(TrackerModel model) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		model.addObserver(this);
		
		add(Box.createVerticalStrut(40));
		
		videoPanel = new VideoPanel(model);
		add(videoPanel);
		
		add(Box.createVerticalStrut(10));
		
		lblCommand = new JLabel(" ");
		lblCommand.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblCommand.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(lblCommand);
		
		add(Box.createVerticalStrut(10));
		
		JPanel timePanel = new JPanel(new BorderLayout(0, 0));
		fps = model.getFps();
		add(timePanel);
		
		movieSeekBar = new JSlider();
		movieSeekBar.setPaintLabels(true);
		movieSeekBar.setEnabled(false);
		movieSeekBar.setValue(0);
		movieSeekBar.setMaximum(model.getTotalFrames());
		timePanel.add(movieSeekBar, BorderLayout.CENTER);
		
		currentTimeLabel = new JLabel();
		timePanel.add(currentTimeLabel, BorderLayout.WEST);
		updateSeekTime(model.getCurrentFrameIndex());
		
		totalTimeLabel = new JLabel(toTime(model.getTotalFrames()/fps));
		timePanel.add(totalTimeLabel, BorderLayout.EAST);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof TrackerModel) {
			movieSeekBar.setValue(((TrackerModel) o).getCurrentFrameIndex());
			updateSeekTime(((TrackerModel) o).getCurrentFrameIndex());
			
			if (((TrackerModel) o).isPaused() == false) {
				resetCommand();
			}
		}
	}
	
	public void setCommand(String command) {
		lblCommand.setText(command);
	}
	
	public void resetCommand() {
		setCommand(" ");
	}
	
	public void updateSeekTime(int frameIndex) {
		String videoTime = toTime(frameIndex/fps);
		currentTimeLabel.setText(videoTime);
	}
	
	public String toTime(double seconds) {
		return new SimpleDateFormat(" mm:ss ").format(new Date((long) (1000.0*seconds)));
	}
}
