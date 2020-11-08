package com.tracker.model;
import java.awt.Color;
import java.awt.Graphics;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class Fish {
	public final static String[] NAMES = {"Female", "Male"};
	public final static Color[] COLOURS = {Color.PINK, Color.BLUE};
	public final static int NUM_FISH = 2;
	public final static int FEMALE = 0;
	public final static int MALE = 1;
	public final int MAX_SPEED = 20; //pixels per frame
	private static double pixelToMM = 5.0/24.0;
	private String name;
	private int index;
	private Point center;
	private Color colour;
	private double distanceTravelled;
	public List<Data> data;
	
	// TODO log fish movement in an array
	// TODO save/load functionality of fish positions
	
	/* Getters and Setters */
	public String getName() { return name; }
	public Point getCenter() { return center; }
	public int getIndex() { return index; }
	public double getDistanceTravelled() { return distanceTravelled; }
	public static double getPixelToMM() { return pixelToMM; }
	public static void setPixelToMM(double r) { pixelToMM = r; }
	
	/**
	 * Fish constructor, based on it's starting position and its relative index. The index is used to determine the 
	 * colour and name that the fish should have.
	 * @param pos An opencv point defining the initial position of the fish.
	 * @param index A 0-based index of the fish.
	 */
	public Fish(Point pos, int index) {
		center = pos;
		this.index = index;
		name = NAMES[index];
		colour = COLOURS[index];
		distanceTravelled = 0;
		data = new ArrayList<Data>();
	}
	
	/**
	 * Moves a fish's centre to the best candidate centre from a list of contours.
	 * <p> The list of contours comes from the differences between frames in the video. Since this signifies movement, 
	 * a fish is either inside one of these contours, or is staying still. 
	 * <p> This function finds the closest candidate centre. If that is still too far for the fish to move to in a 
	 * single frame, then the centre stays still.
	 * 
	 * @param contours A list of MatOfPoints defining the contours of movement in the current frame.
	 * @return The best candidate for the fish's new position.
	 */
	public Point moveToBestCenter(List<MatOfPoint> contours) {
		// check that there is a contour to move to, otherwise stay still
		if (contours.isEmpty()) {
			return center;
		}
		
		// cycle through contours to find closest
		Point bestCenter = getCenter(contours.get(0));
		double shortestDistanceSquared = distSquared(center, bestCenter);
		int bestIndex = 0;
		for (int i = 1; i < contours.size(); i++) {
			Point contourCenter = getCenter(contours.get(i));
			double distanceSquared = distSquared(center, contourCenter);
			if (distanceSquared < shortestDistanceSquared) {
				bestCenter = contourCenter;
				shortestDistanceSquared = distanceSquared;
				bestIndex = i;
			}
		}		
		// check that bestCenter is feasible
		if (isNearPoint(bestCenter)) {
			updateCenter(bestCenter);
			contours.remove(bestIndex); // so that the other fish don't take the same contour
		}
		return center;
	}
	
	/**
	 * Move the centre position to the location specified by pt and increment the distance travelled.
	 * @param pt The new centre position.
	 */
	public void updateCenter(Point pt) {
		distanceTravelled += Math.sqrt(Fish.distSquared(center, pt));
		center = pt;
	}
	
	/**
	 * Just move the centre position to the location specified by pt and don't increase the distance travelled.
	 * @param pt The new centre position.
	 */
	public void moveCenter(Point pt) {
		center = pt;
	}
	
	/**
	 * Draws the fish's centre on the graphics object in its location.
	 * @param g The video frame.
	 */
	public void drawCenter(Graphics g) {
		int radius = 5;
		g.setColor(colour);
		g.fillOval((int)center.x-radius, (int)center.y-radius, radius*2, radius*2);
	}
	
	/**
	 * Determines whether a certain point is in the neighbourhood of the fish's centre. Used for ascertaining if a
	 * nearby blob could potentially belong to the fish, or when 
	 * @param pt The point of concern.
	 * @return True if the distance to the point is less than the fish's max speed.
	 */
	public boolean isNearPoint(Point pt) {
		return distSquared(pt, center) < MAX_SPEED*MAX_SPEED;
	}
	

	//TODO comment
	public void addData(double timestamp) {
		data.add(new Data(this, timestamp));
	}
	
	/**
	 * Inner class used to describe the fish's data relevant for output to csv. This includes the index and position of
	 * the fish, and the video timestamp (in ms) associated with that data point. 
	 */
	public class Data {
		private int fishID;
		private String timestamp;
		private Point location;
		
		/**
		 * Constructor of the datapoint.
		 * @param fish The fish object.
		 * @param timeMillis Time from the video, obtained with mov.get(Videoio.CAP_PROP_POS_MSEC).
		 */
		public Data(Fish fish, double timeMillis) {
			fishID = fish.getIndex();
			timestamp = new SimpleDateFormat("mm:ss:SSS").format(new Date((long) timeMillis));
			location = fish.getCenter();
		}
		
		/**
		 * Converts a datapoint into a format that can be parsed by CSV. Used to save the data obtained by tracking.
		 * @return CSV string equivalent of the datapoint.
		 */
		public String toCSV() {
			double x = toMM(location.x);
			double y = toMM(location.y);
			// TODO check why timestamp isn't always working?
			return String.format("%d,%s,%.2f,%.2f", fishID, timestamp.toString(), x, y);
		}
	}
	
	/**
	 * Helper method to obtain the centre of a contour by calculating its 2D centre of mass.
	 * @param contour An opencv matrix of points dictating the boundary of a shape.
	 * @return An opencv point of the contour's centre.
	 */
	public static Point getCenter(MatOfPoint contour) {
		Moments M = Imgproc.moments(contour);
		return new Point(M.get_m10()/M.get_m00(), M.get_m01()/M.get_m00());
	}
	
	/**
	 * Helper method for calculating the distance between two opencv points. 
	 * @param from An opencv point.
	 * @param to Another opencv point.
	 * @return The distance between the two points, squared.
	 */
	public static double distSquared(Point from, Point to) {
		return (from.x - to.x)*(from.x - to.x) + (from.y - to.y)*(from.y - to.y);
	}
	
	/**
	 * Helper method for converting pixels to metric millimetres.
	 * @param n A number of pixels.
	 * @return The equivalent in mm.
	 */
	public static double toMM(double n) {
		return pixelToMM * n;
	}
}
