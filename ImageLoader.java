import java.awt.*;
import java.awt.datatransfer.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.*;
import java.lang.Math.*;
//import java.util.Random;
//import java.lang.Math;
//import java.lang.Integer;
//import java.awt.Image;
//import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.event.*;
import java.awt.image.*;
import java.applet.Applet;
import java.security.*;
import java.io.*;
import javax.sound.sampled.*;

class ImageLoader implements Runnable, ImageObserver
{
	public Image img;
	int[] pixels;
	int width, height;
	boolean allbits = false;
	public boolean loadedOK = false;
	boolean fresh = false;
	ImageObserver observer;
	MediaTracker trackydacks;
	Component imagemaster;
	boolean grabpixels;
	Tiler imagereciever;
	//Thread mythread??

	public ImageLoader()
	{
		//dummy constructor, then use load() to set params
		fresh = true; //dont start until load has been called...
	}

	public ImageLoader(Image i, int[] dest, int wit, int hig, Component master,boolean grabpix, Tiler tell)
	{
		imagereciever = tell;
		img = i;
		pixels = dest;
		imagemaster = master;
		grabpixels = grabpix;
		width = wit;
		height = hig;
		trackydacks = new MediaTracker(imagemaster);
		trackydacks.addImage(img,1);
		//tell.output("ImageLoader thread constructed ok");
	}

	public void load(String filename, int[] dest, int wit, int hig, Component master, boolean grabpix, Tiler tell)
	{
		if (fresh)
		{
			//this.getClass().
			//URL imgURL = Tiler.class.getResource(filename);
			Component parentComp = tell.parentComponent;
			//tell.output("Attempting to load:" + filename + ".");
			img = Toolkit.getDefaultToolkit().createImage(filename); //this will work in a JAR file
			//img = parentComp.getImage(getCodeBase(),filename);
			//tell.output("img = " + img + ".");
			imagereciever = tell;
			pixels = dest;
			imagemaster = master;
			grabpixels = grabpix;
			width = wit;
			height = hig;
			trackydacks = new MediaTracker(imagemaster);
			trackydacks.addImage(img,1);
			fresh = false;
		}
	}

	public void load(Image i, int[] dest, int wit, int hig, Component master,boolean grabpix, Tiler tell)
	{
		if (fresh)
		{
			imagereciever = tell;
			img = i;
			pixels = dest;
			imagemaster = master;
			grabpixels = grabpix;
			width = wit;
			height = hig;
			trackydacks = new MediaTracker(imagemaster);
			trackydacks.addImage(img,1);
			fresh = false;
	}   }

	public void run()
	{
		while (true) if (!fresh)
		{
			Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
			//output("ImageLoader thread starting");
			//output("final dimensions: " + height + "," + width);
			if (pixels == null) pixels = new int[height * width];
			//output("Pixel array is: " + pixels.length + " long");
			boolean interrupted = true;
			while (interrupted)
			{
				try
				{
					trackydacks.waitForAll();
					//output("finished waiting for image load");
				} catch (InterruptedException e) {System.err.println("Image Load Interrupted! retrying. . ."); interrupted = true; break;}

				Image scaledimage;
				if ((img.getWidth(this) != width) | (img.getHeight(this) != height))
				{
					scaledimage = img.getScaledInstance(width,height,Image.SCALE_SMOOTH);
					trackydacks.addImage(scaledimage,1);
					//int attemptcount = 0;
					//while ((scaledimage.getWidth(this) != width) || (scaledimage.getHeight(this) != height)
					try
					{
						trackydacks.waitForAll();
						imagereciever.output("finished waiting for image rescale");
						//attemptcount++;
						//if (attemptcount > 1) output("rescale attempt " + attemptcount);
						//output("image is now: " + scaledimage.getWidth(this) + "," + scaledimage.getHeight(this));

					} catch (InterruptedException e) {System.err.println("Image Scale Interrupted! retrying. . ."); interrupted = true; break;}

					img = scaledimage;
					//output("resized image to " + img.getWidth(this) + "," + img.getHeight(this) + " OK");

					//} else {
						//output("Error resizing image");
					//}
				} else { }//no resize neccesary.. do nuffin'

				if (grabpixels) try
				{
					if (pixels.length == height*width)
					{
						//output("grabbing pixels...");
						PixelGrabber grabula = new PixelGrabber(img,0,0,width,height,pixels,0,width);
						//boolean ok =
						grabula.grabPixels(); //wait for all.. catch the interrupted below..
						//if (!ok) System.err.println("Pixel grab failed."); //do something about this??
					} else {
						//output("pixel array was wrong length, resizing...");
						pixels = new int[width*height]; //this is probably a bad plan... but
						PixelGrabber grabula = new PixelGrabber(img,0,0,width,height,pixels,0,width);
						//boolean ok =
						grabula.grabPixels(); //wait for all..
						//if (!ok) { System.err.println("Pixel grab failed.");}//do something about this??
					}
				} catch (InterruptedException e) {System.err.println("Pixel Grab Interrupted! retrying. . ."); interrupted = true; break;}
				//else {} //not grabpixels
				interrupted = false;
				//} catch (InterruptedException e) {System.err.println("Image Load Interrupted! retrying. . ."); interrupted = true; break;}
			}
			loadedOK = true;
			if (grabpixels) imagereciever.freshenArray(pixels);
			imagereciever.cueImage(img);

			//when run exits what happens to the thread??
			fresh = true;
			//it dies I guess.. so while (true)
		} else {
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY); 
			Thread.currentThread().yield();
		}
	}

	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
	{
		//if ((infoflags & ImageObserver.ABORT) != 0)      output("ABORT flag registered in ImageUpdate");
		if ((infoflags & ImageObserver.ALLBITS) != 0)
		{
			//output("ALLBITS flag registered in ImageUpdate");
			allbits = true;
			return false;
		}
		return true;
	}

	public Image getImage()
	{
		if ((img != null) & (loadedOK)) return img;
		else return null;
	}

}
