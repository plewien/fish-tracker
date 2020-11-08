package com.tracker.model;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class MotionVideoCapture extends VideoCapture {
	protected Mat frame;
	private Mat binary;
	private LinkedList<Mat> backgrounds = new LinkedList<Mat>();
	private Size frameSize;
	private Scalar colour = new Scalar(255,255,0);
	private List<MatOfPoint> contours = null;
	
	
	private final int GREY_BLUR = 21; //must be odd
	private final int FRAME_DELAY = 3, THRESHOLD = 15, DILATION_BLUR = 3;
	
	
	/* Constructors */
	/**
	 * 
	 * @param filename
	 * @param prev
	 * @param scale
	 */
	public MotionVideoCapture(String filename, MotionVideoCapture prev) {
		super(filename);
		this.frame = prev.frame;
		this.binary = prev.binary;
		this.frameSize = prev.frameSize;	// might need to rescale if videos are different sizes
		this.backgrounds = prev.backgrounds;
	}
	
	public MotionVideoCapture(String filename, double scale) {
		super(filename);
		frame = new Mat();
		frameSize = getScaledSize(scale);
		read(frame);
		
		backgrounds = new LinkedList<Mat>();
		binary = new Mat(); //first background
		greyBlur(frame, binary);	
		backgrounds.add(binary);
	}
	
	/**
	 * Constructor builds the capture under a default 0.8x scale.
	 * @param filename The path to the video to be captured.
	 */
	public MotionVideoCapture(String filename) {
		this(filename, 0.8);
	}
	
	/**
	 * Default constructor. Uses the default video and a 0.8x scale.
	 */
	public MotionVideoCapture() { 
		this("test1.mp4"); 
		System.err.println("Default video used: test1.mp4");
	}
	
	/* Getters & Setters */
	public List<MatOfPoint> getContours() { return contours; }
	public Mat getFrame() { return frame; }
	public Size getSize() {	return frameSize; }
	
	/**
	 * Converts an image to greyscale and blurs the result. Used in the process of generating a binary image for 
	 * motion tracking.
	 * Use greyBlur(from, from) to replace the original.
	 * @param from The original.
	 * @param to The destination.
	 * 
	 */
	public void greyBlur(Mat from, Mat to) {
		Imgproc.cvtColor(from, to, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(to, to, new Size(GREY_BLUR, GREY_BLUR), 0);
	}
	
	public synchronized void captureMotionInFrame(Mat frame, Mat binary) {
		//compute difference between oldest background and current frame
		Mat grey = new Mat();
		Mat frameDelta = new Mat();
		greyBlur(frame, grey);
		Core.absdiff(backgrounds.peek(), grey, frameDelta);
		Imgproc.threshold(frameDelta, binary, THRESHOLD, 255, Imgproc.THRESH_BINARY);
		Imgproc.dilate(binary, binary, new Mat(), new Point(-1, -1), DILATION_BLUR);
		
		//update background
		backgrounds.add(grey);
		while (backgrounds.size() > FRAME_DELAY) {
			backgrounds.remove();
		}	
	}
	
	/**
	 * Draws contours on the frame based on the borders from the binary Mat. This is done to ascertain the potential 
	 * locations of where the fish could have moved to.
	 * @param frame The 
	 * @param binary
	 * @return A list of all unique contours found.
	 */
	public synchronized List<MatOfPoint> drawBlobs(Mat frame, Mat binary) {
		contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(binary, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		Imgproc.drawContours(frame, contours, -1, colour);
		return contours;
	}
	
	public Size getScaledSize(double scale) {
		double width = scale*get(Videoio.CAP_PROP_FRAME_WIDTH);
		double height = scale*get(Videoio.CAP_PROP_FRAME_HEIGHT);
		Size scaledSize = new Size(new double[]{width, height});
		return scaledSize;
	}
	

	/**
	 * Reads the next frame from the VideoCapture stack. It resizes the image to match frameSize.
	 */
	@Override
	public synchronized boolean read(Mat frame) {
		boolean frameFound = super.read(frame);
		if (frameFound) { Imgproc.resize(frame, frame, frameSize, 0, 0, Imgproc.INTER_AREA); }
		return frameFound;
	}
	
	/**
	 * Reads the next scaled frame from the VideoCapture stack. It also draws blob outlines to register captured motion 
	 * in the frame.
	 * @return True, if there is a next frame in the stack.
	 */
	public synchronized boolean nextFrame() {
		boolean frameFound = read(frame);
		if (frameFound && !backgrounds.isEmpty()) {
			captureMotionInFrame(frame, binary);
			drawBlobs(frame, binary);
		}
		return frameFound;
	}
	
	public void linkToPrevious(MotionVideoCapture prev) {
		this.frame = prev.frame;
		this.binary = prev.binary;
		this.frameSize = prev.frameSize;	// might need to rescale if videos are different sizes
		this.backgrounds = prev.backgrounds;
	}
}
