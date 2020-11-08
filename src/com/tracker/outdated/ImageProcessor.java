package com.tracker.outdated;
import java.util.LinkedList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public interface ImageProcessor {
	final int GREY_BLUR = 21; //must be odd
	final int FRAME_DELAY = 3, THRESHOLD = 15, DILATION_BLUR = 3;
	LinkedList<Mat> backgrounds = new LinkedList<Mat>();
	
	public default void greyBlur(Mat from, Mat to, int size) {
		Imgproc.cvtColor(from, to, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(to, to, new Size(size, size), 0);
	}
	
	public default void greyBlur(Mat from, Mat to) {
		greyBlur(from, to, GREY_BLUR);
	}
	
	public default void captureMotionInFrame(Mat frame, Mat binary) {
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
	
	public default Size getScaledSize(double scale, Size size) {
		double width = scale*size.width;
		double height = scale*size.height;
		Size scaledSize = new Size(new double[]{width, height});
		return scaledSize;
		
	}
}
