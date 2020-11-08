package com.tracker.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.opencv.core.Core;
import org.opencv.core.Point;

import com.tracker.model.Fish;
import com.tracker.model.TrackerModel;

public class StatisticsPanel extends JPanel {
	private static final long serialVersionUID = 4461113695422006986L;
	private TrackerModel model = null;
	private int statsWidth = 120;
	
	public static void main(String[] args) {
		// load library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrame frame = new JFrame();
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.add(new StatisticsPanel(new TrackerModel()));
					frame.pack();
					frame.setVisible(true);
				} catch (Exception e) {
					System.err.println("Error: Unable to load panel");
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Creates a new statistics panel using the given fish tracker model
	 * @param model the fish tracker model
	 */
	public StatisticsPanel(TrackerModel model) {
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.model = model;
		
		JLabel lblStatistics = new JLabel("Statistics");
		lblStatistics.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblStatistics.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatistics.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblStatistics.setPreferredSize(new Dimension(statsWidth, 30));
		add(lblStatistics);
		
		//JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		//lblStatistics.setLabelFor(tabbedPane);
		//add(tabbedPane);
		
		for (Fish fish: model.getFishes()) {
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			JComponent fishPanel = new FishPanel(fish);
			tabbedPane.add(fish.getName(), fishPanel);
			add(tabbedPane);
			
		}
	}
	
	public class FishPanel extends JPanel implements Observer {
		private static final long serialVersionUID = 5248113983508867780L;
		private JLabel lblDistance, lblPosition, lblHeading, lblCurrentSpeed, lblAverageSpeed;
		private Fish fish;
		
		/**
		 * Initialises the contents of a fish statistics panel
		 * @param fish a fish object
		 */
		public FishPanel(Fish fish) {
			super(new GridLayout(0, 2, 16, 2));
			model.addObserver(this);
			this.fish = fish;
			
			add(new JLabel("Distance Travelled: ", SwingConstants.TRAILING));
			lblDistance = new JLabel();
			add(lblDistance);
			
			add(new JLabel("Current Position: ", SwingConstants.TRAILING));
			lblPosition = new JLabel();
			add(lblPosition);
			
			add(new JLabel("Heading: ", SwingConstants.TRAILING));
			lblHeading = new JLabel();
			add(lblHeading);
			
			add(new JLabel("Current Speed: ", SwingConstants.TRAILING));
			lblCurrentSpeed = new JLabel();
			add(lblCurrentSpeed);
			
			add(new JLabel("Average Speed: ", SwingConstants.TRAILING));
			lblAverageSpeed = new JLabel();
			add(lblAverageSpeed);
			
			update(model, null);
		}


		@Override
		public void update(Observable o, Object arg) {
			if (model == o) {
				lblDistance.setText(String.format("%.1f mm", Fish.toMM(fish.getDistanceTravelled())));
				lblPosition.setText(pointToString(fish.getCenter()));
				// TODO Implement other statistics items
			}
		}
		
		

	}
	
	/**
	 * Converts an org.opencv.core.Point into a string for useful displaying in a user interface. The usual toString() 
	 * method has a large and unrestrictable length.
	 * @param pt point imported from org.opencv.core.Point
	 * @return string conversion of point input
	 */
	public static String pointToString(Point pt) {
		return String.format("(%.1f, %.1f) px", pt.x, pt.y);
	}
}
