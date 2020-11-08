package com.tracker.view;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import com.tracker.model.Fish;
import com.tracker.model.TrackerModel;

public class VideoPanel extends JPanel implements Observer {
		private static final long serialVersionUID = 140178207667905157L;
		private TrackerModel model = null;
		private BufferedImage image;
		private Dimension movieDimension;
		private Point calibratingPoint = null;
		private Fish calibratingFish = null;

		public void setCalibratingPoint(Point calibratingPoint) {
			this.calibratingPoint = calibratingPoint;
		}

		public void setCalibratingFish(Fish calibratingFish) {
			this.calibratingFish = calibratingFish;
		}

		public VideoPanel(TrackerModel model) {
        	super(new FlowLayout());
        	model.addObserver(this);
        	this.model = model;
        	movieDimension = sizeToDimension(model.getFrameSize()); // default scale is 0.8
        	image = Mat2BufferedImage(model.getCurrentFrame());
        }
                
        public void initialize(Mat initialFrame) {
        	movieDimension = sizeToDimension(initialFrame.size());
        	image = Mat2BufferedImage(initialFrame);
        }
        
        @Override
        public Dimension getPreferredSize() {
           return movieDimension;
        }
        
        @Override
        public Dimension getMaximumSize() {
        	return movieDimension;
        }
        
        @Override
        public Dimension getMinimumSize() {
        	return movieDimension;
        }
        
        @Override
        public synchronized void paintComponent(Graphics g) {
        	super.paintComponent(g);
        	if (image != null) {
        		g.drawImage(image, 0, 0, this);
        	}
        	for (Fish fish: model.getFishes()) {
        		if (!fish.equals(calibratingFish)) {
            		fish.drawCenter(g);
        		}
        	}
        	if (calibratingPoint != null) {
        		new Fish(calibratingPoint, calibratingFish.getIndex()).drawCenter(g);
        	}
        }
        
    	/** 
    	 * Converts a Mat to Buffered Image for displaying inside a JFrame. This is the primary method for showing 
    	 * videos inside the gui.
    	 */
    	public static BufferedImage Mat2BufferedImage(Mat m) {
    	    int type = BufferedImage.TYPE_BYTE_GRAY;
    	    if (m.channels() > 1) {
    	        type = BufferedImage.TYPE_3BYTE_BGR;
    	    }
    	    int bufferSize = m.channels()*m.cols()*m.rows();
    	    byte[] b = new byte[bufferSize];
            m.get(0,0,b); // get all the pixels
            BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
    	    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    	    System.arraycopy(b, 0, targetPixels, 0, b.length);  
    	    return image;
    	}
    	
    	public static Dimension sizeToDimension(Size sz) {
    		int width = (int)sz.width;
    		int height = (int)sz.height;
    		return new Dimension(width, height);
    	}

		@Override
		public void update(Observable o, Object frame) {
			if (model == o) {
				image = Mat2BufferedImage(model.getCurrentFrame());
	        	repaint();
			}
		}	
    }