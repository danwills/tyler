import java.awt.*;
import java.util.*;
import java.awt.image.*;
//import DirectCanvas;

// Lots of useful pixel routines
// (c) inkling 2000. <inkling@camtech.net.au>

public final class PixelTools implements ImageObserver
{
	static int rw,gw,bw,vw,lw,tw1,tw2,tw3; //working variables
	static int[] pixelgrabdest;
	
	public static void unpack(int p, int[] up)
	{ 
		up[0] = (p >> 24) & 0xff; //a
		up[1] = (p >> 16) & 0xff; //r
		up[2] = (p >>  8) & 0xff; //g
		up[3] = (p      ) & 0xff; //b
	}
	
	public static void unpackNoAlpha(int p, int[] up)
	{
		up[0] = (p >> 16) & 0xff; //r
		up[1] = (p >>  8) & 0xff; //g
		up[2] = (p      ) & 0xff; //b
	}

	// some notes from a graphics gem by Ken Fishkin of Pixar Inc., January 1989.
	// MIN(a,b)	(((a)<(b))?(a):(b))
	// MAX(a,b)	(((a)>(b))?(a):(b))	
	// SWAP(a,b)	{ a^=b; b^=a; a^=b; }
	// linear interpolation from l (when a=0) to h (when a=1)*/
	// (equal to (a*h)+((1-a)*l) */
	// LERP(a,l,h)	((l)+(((h)-(l))*(a)))
	// CLAMP(v,l,h)	((v)<(l) ? (l) : (v) > (h) ? (h) : v)
	//
	// clipper: clip(x,min,max) (x = ((x<min)?min:(x<max)?x:max) )
	//
	
	//pack an int array into a packed int pixel
	public static int pack(int[] tp)
	{
		//loop unrolled for speed
		//clip all values to 0..255
		tp[0] = ((tp[0] < 0) ? 0 : ((tp[0] > 0xff) ? 0xff : tp[0]));
		tp[1] = ((tp[1] < 0) ? 0 : ((tp[1] > 0xff) ? 0xff : tp[1]));
		tp[2] = ((tp[2] < 0) ? 0 : ((tp[2] > 0xff) ? 0xff : tp[2]));
		tp[3] = ((tp[3] < 0) ? 0 : ((tp[3] > 0xff) ? 0xff : tp[3]));
		return (tp[0] << 24) | (tp[1] << 16) | (tp[2] << 8) | tp[3];
	}
	
	public static int pack(int a, int r, int g, int b)
	{
		//clip all values to 0..255	
		a = ((a < 0) ? 0 : ((a > 0xff) ? 0xff : a));
		r = ((r < 0) ? 0 : ((r > 0xff) ? 0xff : r));
		g = ((g < 0) ? 0 : ((g > 0xff) ? 0xff : g));
		b = ((b < 0) ? 0 : ((b > 0xff) ? 0xff : b));
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	public static int pack(int r, int g, int b)
	{
		//clip all values to 0..255
		r = ((r < 0) ? 0 : ((r > 0xff) ? 0xff : r));
		g = ((g < 0) ? 0 : ((g > 0xff) ? 0xff : g));
		b = ((b < 0) ? 0 : ((b > 0xff) ? 0xff : b));
		return 0xff000000 | (r << 16) | (g << 8) | b;
	}
	
	//use when the ints are unlikely to be much over 255 if at all, and not negative
	public static int quickPack(int[] tp)
	{
		//clip all values to 0..255
		tp[0] = tp[0] & 0xff;
		tp[1] = tp[1] & 0xff;
		tp[2] = tp[2] & 0xff;
		tp[3] = tp[3] & 0xff;
		return (tp[0] << 24) | (tp[1] << 16) | (tp[2] << 8) | tp[3];
	}
	
	public static int quickPack(int a, int r, int g, int b)
	{
		//and all values within 0..255
		return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	}
	
	//assumes an array 3 ints long
	public static int packIgnoreAlpha(int[] tp)
	{
		//clip values to 0..0xff
		tp[0] = ((tp[0] < 0) ? 0 : ((tp[0] > 0xff) ? 0xff : tp[0]));
		tp[1] = ((tp[1] < 0) ? 0 : ((tp[1] > 0xff) ? 0xff : tp[1]));
		tp[2] = ((tp[2] < 0) ? 0 : ((tp[2] > 0xff) ? 0xff : tp[2]));
		return (int) (0xff << 24) + (tp[0] << 16) + (tp[1] << 8) + tp[2];
	}
	
	//make a packed pixel int from the values in the given int array
	public static int packNoClipOrAlpha(int[] tp)
	{
		return (int) (0xff << 24) + (tp[0] << 16) + (tp[1] << 8) + tp[2];
	}
	
	//get max(r,g,b) in unpacked array, ignore alpha. ie position [0]
	public static int max(int[] test)
	{
		int tst = test[1];
		if (test[2] > tst) tst = test[2];
		if (test[3] > tst) tst = test[3];
		return tst;
	}
	
	//get min(r,g,b) in unpacked array, ignore alpha in position 0.
	public static int min(int[] test)
	{
		int tst = test[1];
		if (test[2] < tst) tst = test[2];
		if (test[3] < tst) tst = test[3];
		return tst;
	}
	
	//get min(r,g,b) in packed int, ignore alpha.
	//min reflects the relative proportion of white in the color
	public static int min(int test)
	{
		int tst = Integer.MAX_VALUE;
		int unp = ((test >> 16) & 0xff);
		if (unp < tst) tst = unp;
		unp = ((test >> 8) & 0xff);
		if (unp < tst) tst = unp;
		unp = (test & 0xff);
		if (unp < tst) tst = unp;
		return tst;
	}
	
	//get average of r g and b, alpha is ignored.
	public static int avg(int[] test)
	{
		return (test[1]+test[2]+test[3])/3;
	}
	
	public static int saturation(int of)
	{
		//return 255 - min(of);
		//use the Color API version:
		float tmp[] = new float[3];
		tmp = Color.RGBtoHSB((of>>16)&0xff,(of>>8)&0xff,(of)&0xff,tmp);
		return (int) (tmp[1]*255.0f);
	}
	
	public static int hue(int of)
	{
		float tmp[] = new float[3];
		tmp = Color.RGBtoHSB((of>>16)&0xff,(of>>8)&0xff,(of)&0xff,tmp);
		return (int) (tmp[0]*255.0f);
		
		//int white = min(of);
		//int rs = (((of >> 16) & 0xff) - white)/6; //scales values to 0..42, a sixth of the color circle
		//int gs = (((of >>  8) & 0xff) - white)/6;
		//int bs = (((of      ) & 0xff) - white)/6;
		//if (rs == 0) white = (128 - gs) + bs;
		//else if (gs == 0) white = (213 - bs) + rs;
		//else if (bs == 0) white = (42 - rs) + gs;
		//return white;
		//eg pure red: 255,0,0, white is zero, result: 213+42 = 255 the 'last' color
	}
	
	public static int value(int of)
	{
		//like max
		float tmp[] = new float[3];
		tmp = Color.RGBtoHSB((of>>16)&0xff,(of>>8)&0xff,(of)&0xff,tmp);
		return (int) (tmp[2]*255.0f);
		//int t1 = ((of >> 16) & 0xff); //get red
		//int t2 = ((of >> 8) & 0xff);  //get green
		//if (t2 > t1) t1 = t2;         //test
		//t2 = (of & 0xff);             //get blue
		//if (t2 > t1) t1 = t2;         //test
		//return t1;
	}
	
	//next two methods need renaming
	//averages the red green and blue components of a color
	public static int getGrey(int p)
	{ 
		return ( ((p >> 16) & 0xff)+
			     ((p >>  8) & 0xff)+
			     ((p      ) & 0xff) )/3;
	}
	
	public static float getFloatGrey(int p)
	{ 
		return ((float)((p >> 16) & 0xff)+ 
		        (float)((p >>  8) & 0xff)+
		        (float)((p      ) & 0xff) )/3.0f;
	}
	
	//Make an rgb grey out of one int.
	//this method could also take an alpha value
	public static int makePackedGrey(int p)
	{ 
		p = ((p < 0) ? 0 : ((p > 0xff) ? 0xff : p));
		return (0xff000000) | (p << 16) | (p << 8) | p; //full alpha
	}
	
	//Make an rgb grey out of a packed color.
	//change this so that it doesnt destroy the alpha.
	public static int removeColor(int p)
	{ 
		int val =( ((p >> 16) & 0xff)+
			       ((p >>  8) & 0xff)+
			       ((p      ) & 0xff) )/3;		     
		return (int) (p & 0xff000000) + (val << 16) + (val << 8) + val; //full alpha
	}
	
	//Multiply a packed color by a scalar value, returns a packed color.
	// (Fixed point)
	public static int mulColor(int packedcol,int p)
	{ 
		p = ((p < 0) ? 0 : ((p > 0xff) ? 0xff : p));
		
		int redbit =   (((packedcol >> 16) & 0xff) * p) >> 8;
		int greenbit = (((packedcol >> 8 ) & 0xff) * p) >> 8;
		int bluebit =  (((packedcol      ) & 0xff) * p) >> 8;
		
		return (int) (packedcol & 0xff000000) + (redbit << 16) + (greenbit << 8) + bluebit; //full alpha
	}
	
	//Standard alpha blend with only a single alpha, the source strength.
	public static int blendColors(int s,int d,int srcamount)
	{
		int invsrc = 0x100 - srcamount;
		//0xff-srcamount ?
		
		int redbit =   ((((s >> 16) & 0xff) * srcamount) >> 8) + ((((d >> 16) & 0xff) * invsrc) >> 8);
		int greenbit = ((((s >> 8 ) & 0xff) * srcamount) >> 8) + ((((d >> 8 ) & 0xff) * invsrc) >> 8);
		int bluebit =  ((((s      ) & 0xff) * srcamount) >> 8) + ((((d      ) & 0xff) * invsrc) >> 8);	
		return (int) (0xff000000) | (redbit << 16) | (greenbit << 8) | bluebit; //full alpha
	}
	
	//Blends 2 argb colors and averages their alpha's.
	public static int pixelAlphaBlend(int s,int d)
	{	
		//if (p > 255) p = 255;
		//else if (p < 0) p = 0;
		int srcalpha = (s >> 24) & 0xff;
		int invsrc = 0x100 - srcalpha;
		int redbit =   ((((s >> 16) & 0xff) * srcalpha) >> 8) + ((((d >> 16) & 0xff) * invsrc) >> 8);
		int greenbit = ((((s >> 8 ) & 0xff) * srcalpha) >> 8) + ((((d >> 8 ) & 0xff) * invsrc) >> 8);
		int bluebit =  ((((s      ) & 0xff) * srcalpha) >> 8) + ((((d      ) & 0xff) * invsrc) >> 8);
		srcalpha += (d >> 24) & 0xff;
		srcalpha = srcalpha >> 1;
		return (int) (srcalpha << 24) + (redbit << 16) + (greenbit << 8) + bluebit; //full alpha
	}
	
	// Pinched from ImageJ, a brilliant java image viewer/processor:
	/* Uses bilinear interpolation to find the pixel value at real coordinates (x,y). */
	private final int getInterpolatedPixel(double x, double y, int[] pixels, int width, int height) 
	{
		int xbase = (int)x;
		int ybase = (int)y;
		double xFraction = x - xbase;
		double yFraction = y - ybase;
		int offset = ybase * width + xbase;
		
		int lowerLeft = pixels[offset];
		if ((xbase>=(width-1))||(ybase>=(height-1)))
			return lowerLeft;
		int rll = (lowerLeft&0xff0000)>>16;
		int gll = (lowerLeft&0xff00)>>8;
		int bll = lowerLeft&0xff;
		
		int lowerRight = pixels[offset + 1];
		int rlr = (lowerRight&0xff0000)>>16;
		int glr = (lowerRight&0xff00)>>8;
		int blr = lowerRight&0xff;

		int upperRight = pixels[offset + width + 1];
		int rur = (upperRight&0xff0000)>>16;
		int gur = (upperRight&0xff00)>>8;
		int bur = upperRight&0xff;

		int upperLeft = pixels[offset + width];
		int rul = (upperLeft&0xff0000)>>16;
		int gul = (upperLeft&0xff00)>>8;
		int bul = upperLeft&0xff;
		
		int r, g, b;
		double upperAverage, lowerAverage;
		upperAverage = rul + xFraction * (rur - rul);
		lowerAverage = rll + xFraction * (rlr - rll);
		r = (int)(lowerAverage + yFraction * (upperAverage - lowerAverage)+0.5);
		upperAverage = gul + xFraction * (gur - gul);
		lowerAverage = gll + xFraction * (glr - gll);
		g = (int)(lowerAverage + yFraction * (upperAverage - lowerAverage)+0.5);
		upperAverage = bul + xFraction * (bur - bul);
		lowerAverage = bll + xFraction * (blr - bll);
		b = (int)(lowerAverage + yFraction * (upperAverage - lowerAverage)+0.5);

		return 0xff000000 | ((r&0xff)<<16) | ((g&0xff)<<8) | b&0xff;
	}
	
	//set the luminosity of one packed pixel array using that of another
	//assumes the two arrays are correct length
	//destroys the alpha channel, fix...
	public static void setLuminosity(int[] lumi, int[] of)
	{
		int inpixun[] = new int[4];
		
		for (int i = 0; i < of.length; i++)
		{	
			//get the grey to multiply with
			unpack(lumi[i],inpixun);
			
			//use max as luminosity [some say to use average]
			int multiplier = max(inpixun); 
			of[i] = mulColor(of[i],multiplier);
			
			//get the color to multiply with
			//unpack(,inpixun);
			
			//inpixun[0] = 256; //ignore alpha
			//inpixun[1] = (inpixun[1] * multiplier) >> 8; //multiply by the lumi
			//inpixun[2] = (inpixun[2] * multiplier) >> 8;
			//inpixun[3] = (inpixun[3] * multiplier) >> 8;
			
			//of[i] = pack(inpixun); //repack
		}
	}
	
	//Fill the alpha channel of an int array with a single value
	public static void fillAlphaChannel(int alpha, int[] of)
	{	
		alpha = ((alpha < 0) ? 0 : ((alpha > 0xff) ? 0xff : alpha));
		for (int i = 0; i < of.length; i++)
		{of[i] = (of[i] & 0x00ffffff) | (alpha << 24);}
	}
	
	//Add a value to the alpha channel, clipped 
	public static void addAlphaChannel(int[] dest,int add)
	{	
		//if (alpha > 256) alpha = 256;
		//if (alpha < 0) alpha = 0;
		
		for (int i = 0; i < dest.length; i++)
		{
			int alpha = (dest[i] >> 24) & 0xff;
			alpha += add;
			alpha = ((alpha < 0) ? 0 : ((alpha > 0xff) ? 0xff : alpha));
			dest[i] = (dest[i] & 0x00ffffff) | (alpha << 24);
		}
	}
	
	//Set the alpha of a packedpixel array using an unpacked array.
	public static void setAlphaChannel(int[] alpha, int[] of)
	{
		int inpixun[] = new int[4];
		
		for (int i = 0; i < of.length; i++)
		{	
			alpha[i] = ((alpha[i] < 0) ? 0 : ((alpha[i] > 0xff) ? 0xff : alpha[i]));
			of[i] = (of[i] & 0x00ffffff) | (alpha[i] << 24);
		}
	}
	
	//Set the alpha channel of a packedpixel array using the luminosity of another.
	public static void setAlphaToLumi(int[] lumi, int[] of)
	{
		int inpixun[] = new int[4];
		
		for (int i = 0; i < of.length; i++)
		{	
			unpack(lumi[i],inpixun);
			//use avg as luminosity [some say to use max]
			int alpha = max(inpixun); 
			alpha = ((alpha < 0) ? 0 : ((alpha > 0xff) ? 0xff : alpha));
			of[i] = (of[i] & 0x00ffffff) | (alpha << 24);
		}
	}
	
	//Multiply source image by multiplier and store in dest.
	public static void multiply(int[] src, int[] dest, int multiplier)
	{
		if (src.length == dest.length)
		{
			int r, g ,b, a, inp;
			for (int i = 0; i < src.length; i++)
			{
				//get the color to multiply with
				inp = src[i];
				a = (inp >> 24) & 0xff;
				r = (((inp >> 16) & 0xff) * multiplier) >> 8;
				g = (((inp >>  8) & 0xff) * multiplier) >> 8;
				b = (((inp      ) & 0xff) * multiplier) >> 8;
				dest[i] = pack(a,r,g,b); //repack
			}
		} else { System.err.println("Tried to multiply two different sized images."); }
	}
	
	//add a value to each channel of a pixel array and store in dest.
	public static void add(int[] src, int[] dest, int amount)
	{
		if (src.length == dest.length)
		{
			int a, r, g ,b, inp;
			for (int i = 0; i < src.length; i++)
			{
				//get the color to add to (can be negative)
				inp = src[i];
				a = ((inp >> 24) & 0xff);
				r = ((inp >> 16) & 0xff) + amount;
				g = ((inp >>  8) & 0xff) + amount;
				b = ((inp      ) & 0xff) + amount;
				dest[i] = pack(a,r,g,b); //repack
			}
		} else { System.err.println("Tried to multiply two different sized images."); }
	}
	
	//set pixels with lumi above threshold to abovelevel, others to belowlevel.
	public static void threshold(int[] src, int[] dest, int threshold, int abovelevel, int belowlevel)
	{
		if (src.length == dest.length)
		{
			int inp;
			for (int i = 0; i < src.length; i++)
			{
				//get the color to add to (can be negative)
				inp = getGrey(src[i]);
				
				if (inp < threshold)
				{
					dest[i] = belowlevel;
				} else { 
					dest[i] = abovelevel;
				}
			}
		} else { System.err.println("Tried to multiply two different sized images."); }
	}
	
	//Invert the source image and store in dest.
	public static void invert(int[] src, int[] dest, boolean invertalpha)
	{
		if (src.length == dest.length)
		{
			int a, r, g ,b, inp;
			for (int i = 0; i < src.length; i++)
			{
				//get the color to add to (can be negative)
				inp = src[i];
				if (invertalpha) a = 0xff - ((inp >> 24) & 0xff); else a = 0xff;
				r = 0xff - ((inp >> 16) & 0xff);
				g = 0xff - ((inp >>  8) & 0xff);
				b = 0xff - ((inp      ) & 0xff);
				dest[i] = pack(a,r,g,b); //repack
			}
		} else { System.err.println("Tried to invert with different sized images."); }
	}
	
	//Combine 2 argb packed pixel images by multiplying their pixel values together
	//ignores the alpha channel.
	public static void imageMultiply(int[] src1, int[] src2, int[] dest)
	{
		if ((src1.length == src2.length) && (src2.length == dest.length))
		{
			int sp,sr,sg,sb,dp,dr,dg,db;
			
			for (int i = 0; i < src1.length; i++)
			{
				//get the color to multiply with
				sp = src1[i];
				dp = src2[i];
				sr = (((sp >> 16) & 0xff) * ((dp >> 16) & 0xff)) >> 8;
				sg = (((sp >>  8) & 0xff) * ((dp >>  8) & 0xff)) >> 8;
				sb = (((sp      ) & 0xff) * ((dp      ) & 0xff)) >> 8;
				dest[i] = pack(sr,sg,sb); //repack
			}
		} else { System.err.println("Tried to combine two different sized images."); }
	}
	
	//Mix 2 source images using split and store in dest.
	public static void imageMixByScalar(int[] src1, int[] src2, int split, int[] dest)
	{
		if ((src1.length == src2.length) && (src2.length == dest.length))
		{
			//if (split > 255) split = 255;
			int src1pixel, src2pixel;
			int redbit, greenbit, bluebit, alf;
			int invsplit = 0xff-split;
			
			for (int i = 0; i < src1.length; i++)
			{
				//get the color to multiply with
				src1pixel = src1[i];
				src2pixel = src2[i];
				
				alf = ((src1pixel >> 24) & 0xff);// + (invsplit * (src2pixel << 24) & 0xff)          ) >> 8;
				redbit =   ( ( split * ((src1pixel >> 16) & 0xff) ) + (invsplit * ((src2pixel >> 16) & 0xff) ) ) >> 8;
				greenbit = ( ( split * ((src1pixel >>  8) & 0xff) ) + (invsplit * ((src2pixel >>  8) & 0xff) ) ) >> 8;
				bluebit =  ( ( split * ((src1pixel      ) & 0xff) ) + (invsplit * ((src2pixel      ) & 0xff) ) ) >> 8;
				
				dest[i] = pack(alf,redbit,greenbit,bluebit); //repack
			}
		} else { System.err.println("Tried to combine two different sized images."); }
	}
	
	//Copy the pixel values from one int array to another.
	//Mainly used to avoid using clone().
	public static void imageCopy(int[] src, int[] dest)
	{
		if (src.length == dest.length)
		{
			System.arraycopy(src, 0, dest, 0, src.length);
		} else { System.err.println("Tried to imageCopy into a different sized image."); }
	}
	
	//reinventing the wheel once more. . .
	//draws an image on dest[] by drawing the dest pixel inverted by
	//the grey level of the pixel to draw.
	public static void drawXORMode(int xpos, int ypos, 
	                      int[] dest, int destwidth, int destheight, 
	                      int[] draw, int drawwidth, int drawheight )
	{
		//some primitive clipping may occur ;)
		int[] unpak = new int[4];
		int[] unpaktwo = new int[4];
		
		int drawstartoffset = 0;
		int linecount = 0;
		int linecountmul = 0;
		int clippedrasterlength;
		int destpixel,srcpixel;
		int redbit, greenbit, bluebit;
		int redbit2, greenbit2, bluebit2;
		int mix, invmix;
		if (xpos < 0)
		{
			drawstartoffset += Math.abs(xpos);
			//get real dest startpos and increment
			//get real draw startpos, length and increment
			//set a flag perhaps...
		}
		
		if (ypos < 0) 
		{
			//drawstartoffset += Math.abs(ypos)*drawwidth;
			linecount = Math.abs(ypos);
		}
		
		if (xpos < 0) xpos = 0;
		if (ypos < 0) ypos = 0;
		
		if ((ypos < destheight) & (xpos < destwidth))
		{
			/*
			if (ypos < 0) linecount = - ypos;
			if ((xpos+drawwidth > destwidth) || (ypos+drawheight > destheight))
			*/
			int startpos = xpos + ypos * destwidth;
			
			for (int i = startpos; linecount < drawheight; i += destwidth)
			{
				for (int j = drawstartoffset; j < drawwidth; j++)
				{
					int destpos = i+j;
					if ((destpos < dest.length) && (j + xpos < destwidth))
					{
						destpixel = dest[destpos];
						srcpixel = draw[j + linecountmul];
						
						// mix is the amount of inverted color to blend
						// invmix is 256-mix, the amount of the existing color to use.
						
						mix = ((srcpixel >> 16) & 0xff); //rmix
						invmix = 0xff - mix;
						
						redbit = ((destpixel >> 16) & 0xff);
						redbit2 = ((redbit * invmix) >> 8) + (( (0xff-redbit) * mix) >> 8);
						
						mix = ((srcpixel >> 8) & 0xff); //gmix
						invmix = 0xff - mix;
						
						greenbit = ((destpixel >>  8) & 0xff);
						greenbit2 = ((greenbit * invmix) >> 8) + (((0xff-greenbit)*mix) >> 8);
						
						mix = ((srcpixel     ) & 0xff); //bmix
						invmix = 0xff - mix;
						
						bluebit = ((destpixel      ) & 0xff);
						bluebit2 = ((bluebit * invmix) >> 8) + (((0xff-bluebit) * mix) >> 8);
						
						dest[destpos] = pack(0xff,redbit2,greenbit2,bluebit2);
					}
				}
				linecount++;
				linecountmul += drawwidth;
			}
		}
	}
	
	//draws an image using its alpha channel to blend
	public static void drawWithAlpha(int xpos, int ypos, 
	                      int[] dest, int destwidth, int destheight, 
	                      int[] draw, int drawwidth, int drawheight )
	{
		//some primitive clipping may occur ;)
		
		int rasterstartoffset = 0;
		int rasterlength = drawwidth;
		
		int firstraster = 0;
		int maxraster = drawheight;
		
		//if a portion of the img to draw is _inside the destination surface
		if ( (xpos < destwidth) & (ypos < destheight) &
		     (xpos + drawwidth > 0) & (ypos + drawheight  >0) )
		{
			if (xpos < 0)
			{
				rasterstartoffset = Math.abs(xpos);
				rasterlength = drawwidth - rasterstartoffset;
				xpos = 0;
			}
		
			if (ypos < 0) 
			{
				firstraster = Math.abs(ypos);
				//maxraster = drawheight-Math.abs(ypos);
				ypos = 0;
			}
			
			if (xpos+drawwidth > destwidth)
			{
				rasterlength = destwidth - xpos;
			}
			
			if (ypos+drawheight > destheight)
			{
				maxraster = destheight - ypos;
			}
			
			
			//int startpos = xpos + ypos * destwidth;
			int destpos = xpos + ypos * destwidth;
			int srcpos = rasterstartoffset + firstraster * drawwidth;
			
			for (int i = firstraster; i < maxraster; i++)
			{
				for (int j = 0; j < rasterlength; j++)
				{
					int destpixel = dest[destpos];
					int srcpixel = draw[srcpos]; //j + i * drawwidth];
					
					int alpha = ((srcpixel >> 24) & 0xff);
					int invalpha = 0xff - alpha;
					int redbit   = ((((srcpixel >> 16) & 0xff) * alpha) >> 8) + ((((destpixel >> 16) & 0xff) * invalpha) >> 8);
					int greenbit = ((((srcpixel >>  8) & 0xff) * alpha) >> 8) + ((((destpixel >>  8) & 0xff) * invalpha) >> 8);
					int bluebit  = ((((srcpixel      ) & 0xff) * alpha) >> 8) + ((((destpixel      ) & 0xff) * invalpha) >> 8);
					dest[destpos] = quickPack(0xff,redbit,greenbit,bluebit);	
					destpos++;
					srcpos++;
				}
				destpos += destwidth - rasterlength;
				srcpos += drawwidth - rasterlength;
			}
		}
	}
	
	//draw only the pixels over a given alpha threshold.
	//alphamap should be packed and samesize as tile
	public static void drawWithThresholdAlpha(int xpos, int ypos, 
	                      int[] dest, int destwidth, int destheight, 
	                      int[] tiledraw, int drawwidth, int drawheight,
	                      int[] alphamap, int alphathreshold)
	                      
	{
		//some primitive clipping may occur ;)
		
		int rasterstartoffset = 0;
		int rasterlength = drawwidth;
		
		int firstraster = 0;
		int maxraster = drawheight;
		
		//if a portion of the img to draw is _inside the destination surface
		if ( (xpos < destwidth) & (ypos < destheight) &
		     (xpos + drawwidth > 0) & (ypos + drawheight  >0) )
		{
			if (xpos < 0) { rasterstartoffset = Math.abs(xpos); rasterlength = drawwidth - rasterstartoffset; xpos = 0; }
			if (ypos < 0) { firstraster = Math.abs(ypos);ypos = 0; }
			if (xpos+drawwidth > destwidth)	rasterlength = destwidth - xpos;
			if (ypos+drawheight > destheight) maxraster = destheight - ypos;
			
			int destpos = xpos + ypos * destwidth;
			int srcpos = rasterstartoffset + firstraster * drawwidth;
			int srcpixel;
			
			for (int i = firstraster; i < maxraster; i++)
			{
				for (int j = 0; j < rasterlength; j++)
				{
					srcpixel = tiledraw[srcpos];
					if (getGrey(alphamap[srcpos]) > alphathreshold)
					{dest[destpos] = srcpixel;}
					destpos++;
					srcpos = (srcpos+1) % tiledraw.length;
				}
				destpos += destwidth - rasterlength;
				srcpos = (srcpos + drawwidth - rasterlength) % tiledraw.length;
			}
		}
	}
	
	//Draws an image onto dest[] with its lumi as the alpha and a multiplier, 
	//so eg with the multiplier 0x88, the alpha is about half what it would've been.
	public static void drawWithLuminosityAsAlphaAndAConstantMultiplier(int xpos, int ypos, 
	                      int[] dest, int destwidth, int destheight, 
	                      int[] draw, int drawwidth, int drawheight,
	                      int constmul)
	{
		//some primitive clipping may occur ;)
		
		int rasterstartoffset = 0;
		int rasterlength = drawwidth;
		
		int firstraster = 0;
		int maxraster = drawheight;
		
		//if a portion of the img to draw is _inside the destination surface
		if ( (xpos < destwidth) & (ypos < destheight) &
		     (xpos + drawwidth > 0) & (ypos + drawheight  > 0) )
		{
			if (xpos < 0) { rasterstartoffset = Math.abs(xpos); rasterlength = drawwidth - rasterstartoffset; xpos = 0; }
			if (ypos < 0) { firstraster = Math.abs(ypos); ypos = 0; }
			if (xpos+drawwidth > destwidth) rasterlength = destwidth - xpos;
			if (ypos+drawheight > destheight) maxraster = destheight - ypos;
			
			int destpos = xpos + ypos * destwidth;
			int srcpos = rasterstartoffset + firstraster * drawwidth;
			
			for (int i = firstraster; i < maxraster; i++)
			{
				for (int j = 0; j < rasterlength; j++)
				{
					int destpixel = dest[destpos];
					int srcpixel = draw[srcpos];
					int alpha = getGrey(srcpixel);
					alpha = (alpha * constmul) >> 8;
					int invalpha = 0xff - alpha;
					int redbit   = ((((srcpixel >> 16) & 0xff) * alpha) >> 8) + ((((destpixel >> 16) & 0xff) * invalpha) >> 8);
					int greenbit = ((((srcpixel >>  8) & 0xff) * alpha) >> 8) + ((((destpixel >>  8) & 0xff) * invalpha) >> 8);
					int bluebit  = ((((srcpixel      ) & 0xff) * alpha) >> 8) + ((((destpixel      ) & 0xff) * invalpha) >> 8);
					dest[destpos] = pack(0xff,redbit,greenbit,bluebit);	
					destpos++;
					srcpos++;
				}
				destpos += destwidth - rasterlength;
				srcpos += drawwidth - rasterlength;
			}
		}
	}
	
	//Just draw an image to dest[] ignoring alpha information.
	public static void drawWithoutAlpha(int xpos, int ypos, 
	                      int[] dest, int destwidth, int destheight, 
	                      int[] draw, int drawwidth, int drawheight )
	{
		//some primitive clipping may occur ;)
		int drawstartoffset = 0;
		int linecount = 0;
		int linecountmul = 0;
		int clippedrasterlength;
		
		if (xpos < 0) drawstartoffset += Math.abs(xpos);
		//get real dest startpos and increment
		//get real draw startpos, length and increment
		if (ypos < 0) linecount = Math.abs(ypos);
		if (xpos < 0) xpos = 0;
		if (ypos < 0) ypos = 0;
		
		if ((ypos < destheight) & (xpos < destwidth))
		{
			int startpos = xpos + ypos * destwidth;
			
			for (int i = startpos; linecount < drawheight; i += destwidth)
			{
				for (int j = drawstartoffset; j < drawwidth; j++)
				{
					int destpos = i+j;
					if ((destpos < dest.length) && (j + xpos < destwidth))
					{dest[destpos] = draw[j + linecountmul];}
				}
				linecount++;
				linecountmul += drawwidth;
			}
		}
	}
	
	//Draw one image onto another, using the source alpha, assumes the two 
	//images are the same size
	public static void drawWithAlpha(int[] dest,int[] draw)
	{	
		int redbit;
		int greenbit;
		int bluebit;
		int destpixel;
		int srcpixel;
		int alpha;
		int invalpha;
		
		for (int i = 0; i < dest.length; i++)
		{
			destpixel = dest[i];
			srcpixel = draw[i];
			
			alpha = ((srcpixel >> 24) & 0x000000ff);
			invalpha = 0xff - alpha;
			
			redbit   = ((((srcpixel >> 16) & 0xff) * alpha) >> 8) + ((((destpixel >> 16) & 0xff) * invalpha) >> 8);
			greenbit = ((((srcpixel >>  8) & 0xff) * alpha) >> 8) + ((((destpixel >>  8) & 0xff) * invalpha) >> 8);
			bluebit  = ((((srcpixel      ) & 0xff) * alpha) >> 8) + ((((destpixel      ) & 0xff) * invalpha) >> 8);
			dest[i] = ((0xff << 24) | (redbit << 16) | (greenbit << 8) | bluebit);	
		}
	}
	
	//this method assumes the three images are the same size
	public static void drawWithOffsetAlpha(int[] dest, int[] drawbg, int[] drawfg, int[] lumialpha, int alphaoffset)
	{	
		int redbit;
		int greenbit;
		int bluebit;
		int bgpixel;
		int fgpixel;
		int alpha;
		int invalpha;
		
		for (int i = 0; i < dest.length; i++)
		{
			bgpixel = drawbg[i];
			fgpixel = drawfg[i];
			
			alpha = getGrey(lumialpha[i]) + alphaoffset;
			if (alpha < 0) alpha = 0; else if (alpha > 0xff) alpha = 0xff;
			invalpha = 0xff - alpha;
			redbit   = ((((fgpixel >> 16) & 0xff) * alpha) >> 8) + ((((bgpixel >> 16) & 0xff) * invalpha) >> 8);
			greenbit = ((((fgpixel >>  8) & 0xff) * alpha) >> 8) + ((((bgpixel >>  8) & 0xff) * invalpha) >> 8);
			bluebit  = ((((fgpixel      ) & 0xff) * alpha) >> 8) + ((((bgpixel      ) & 0xff) * invalpha) >> 8);
			dest[i] = pack(0xff,redbit,greenbit,bluebit);
		}
	}
	
	public static void drawPixelWithAlpha(int x, int y, int[] dest, int width, int color)
	{	
		if ((x >= 0) & (x < width))
		{
			int i = x + y * width;
			if ((y >= 0) & (i < dest.length))
			{
				int destpixel = dest[i];
				int alpha = ((color >> 24) & 0x000000ff);
				int invalpha = 0xff - alpha;
				int redbit   = ((((color >> 16) & 0xff) * alpha) >> 8) + ((((destpixel >> 16) & 0xff) * invalpha) >> 8);
				int greenbit = ((((color >>  8) & 0xff) * alpha) >> 8) + ((((destpixel >>  8) & 0xff) * invalpha) >> 8);
				int bluebit  = ((((color      ) & 0xff) * alpha) >> 8) + ((((destpixel      ) & 0xff) * invalpha) >> 8);
				dest[i] = ((0xff << 24) | (redbit << 16) | (greenbit << 8) | bluebit);
			}
		}
	}
	
	public static void fill(int[] dest, int color)
	{for (int i = 0; i <dest.length ; i++) {dest[i] = color;}	}
	
	public static void fillRect(int[] dest, int destwidth, int destheight, int x, int y, int w, int h, int color, boolean alphablend)
	{
		int rasterstartoffset = 0;
		int rasterlength = w;
		
		int firstraster = 0;
		int maxraster = h;
		
		//if a portion of the img to draw is _inside the destination surface
		if ( (x < destwidth) & (y < destheight) &
		     (x + w > 0) & (y + h  > 0) )
		{
			if (x < 0) { rasterstartoffset = Math.abs(x); rasterlength = w - rasterstartoffset; x = 0; }
			if (y < 0) { firstraster = Math.abs(y); y = 0; }
			if (x + w > destwidth) rasterlength = destwidth - x;
			if (y + h > destheight) maxraster = destheight - y;
			
			int destpos = x + y * destwidth;
			int srcpos = rasterstartoffset + firstraster * w;
			
			for (int i = firstraster; i < maxraster; i++)
			{
				for (int j = 0; j < rasterlength; j++)
				{
					if (alphablend)
					{
						int destpixel = dest[destpos];
						int alpha = (color >> 24) & 0xff;
						int invalpha = 0xff - alpha;
						dest[destpos] = pack(0xff,((((color >> 16) & 0xff) * alpha) >> 8) + ((((destpixel >> 16) & 0xff) * invalpha) >> 8),
					    	                      ((((color >>  8) & 0xff) * alpha) >> 8) + ((((destpixel >>  8) & 0xff) * invalpha) >> 8),
					        	                  ((((color      ) & 0xff) * alpha) >> 8) + ((((destpixel      ) & 0xff) * invalpha) >> 8) );
					} else {
						dest[destpos] = color;
					}
					    	             
					destpos++;
					srcpos++;
				}
				destpos += destwidth - rasterlength;
				srcpos += h - rasterlength;
			}
		}
	}
	
	//Draw a circle at (mx,my) with the given radius and color
    /*public static void drawCircle(int mx, int my, int radius, DirectCanvas where, int color) 
    {
        int x,y,d,deltaE,deltaSE;
        int[] pixarray = where.getPixelArray();
        
		x=0;
		y=radius;
		d=1-radius;
		deltaE=3;
		deltaSE=5-radius*2;
		// 32-BIT
	    drawPixelWithAlpha(mx,my+radius,pixarray,where.width,color);
	    drawPixelWithAlpha(mx+radius,my,pixarray,where.width,color);
	    drawPixelWithAlpha(mx,my-radius,pixarray,where.width,color);
	    drawPixelWithAlpha(mx-radius,my,pixarray,where.width,color);
	    while(y>x)
	    {
	    	if(d<0)
	    	{
	    		d+=deltaE;
	    		deltaE+=2;
	    		deltaSE+=2;
	    		x++;
	    	} else {
	    		d+=deltaSE;
	    		deltaE+=2;
	    		deltaSE+=4;
	    		x++;
	    		y--;
	    	}
	    	// draws lower part
			drawPixelWithAlpha(mx+x,my+y,pixarray,where.width,color);
			drawPixelWithAlpha(mx-x,my+y,pixarray,where.width,color);
			drawPixelWithAlpha(mx+y,my+x,pixarray,where.width,color);
			drawPixelWithAlpha(mx-y,my+x,pixarray,where.width,color);
			// draws upper part
			drawPixelWithAlpha(mx+x,my-y,pixarray,where.width,color);
			drawPixelWithAlpha(mx-x,my-y,pixarray,where.width,color);
			drawPixelWithAlpha(mx+y,my-x,pixarray,where.width,color);
    		drawPixelWithAlpha(mx-y,my-x,pixarray,where.width,color);
    	}	
    }
	*/
	
	//make a tileRotated version of the input image, as shown.
	public static void makeRotated(int[] src, int[] dest, int xdim, int ydim, 
	                                                      int xrot, int yrot)
	{
		/*
		  do this to the image in src[] and store in dest[]
		  
		  a|b    d|c 
		  -+- -> -+- 
		  c|d    b|a   where the + is Point rot
		  
		  dim.x is the tilewidth
		  dim.y is the tileheight
		*/
		
		int rotoffs = (xrot % xdim) + (yrot % ydim) * xdim;
		for (int i = 0; i < src.length; i++)
		{
			dest[i] = src[(i + rotoffs) % src.length];
		}
	}
	                   
	public static void makeRotatedbyArrayCopy(int[] src, int[] dest, int xdim, int ydim, 
	                                                      int xrot, int yrot)
	{
		/*
		  do this to the image in src[] and store in dest[]
		  
		  a|b    d|c 
		  -+- -> -+- 
		  c|d    b|a   where the + is Point rot
		  
		  dim.x is the tilewidth
		  dim.y is the tileheight
		*/
		
		int rotoffs = (xrot % xdim) + (yrot % ydim) * xdim;
		if (rotoffs >= 0)
		{
			//syntacks; System.arraycopy(src,srcpos,dest,destpos,leng)
			System.arraycopy(src, rotoffs, dest, 0,                    src.length - rotoffs);
			System.arraycopy(src, 0,       dest, src.length - rotoffs, rotoffs);
		} else {
			rotoffs = src.length + (rotoffs%src.length);
			System.arraycopy(src, rotoffs, dest, 0,                    src.length - rotoffs);
			System.arraycopy(src, 0,       dest, src.length - rotoffs, rotoffs);
		}
	}
	
	//Allocate an int[] array and fill it with pixels from a given location in the src image.
	public static int[] getSubMap(int src[], int srcwidth, int srcheight, 
	int xloc, int yloc, int subw, int subh)
	{
		//not clipped in any way;
		
		int[] tempo = new int[subw*subh];
		
		int startloc = xloc + yloc * srcwidth;
		int destloc = 0;
		
		for (int i = 0; i < subw; i++)
		{
			for (int j = 0; j < subh; j++)
			{
				tempo[destloc] = src[startloc];
				startloc = (startloc+1) % src.length;
				destloc = (destloc+1) % tempo.length;
			}
			startloc = (startloc + (srcwidth - subw)) % src.length;
		}
		return tempo;
	}
	
	public static void shake(int[] pixels, int[] tmppixels, int height, int width, int border, int bordercolor, int shakex, int shakey, Random r)
	{
		int shakexpos = Math.abs(r.nextInt() % shakex);
		int shakeypos = Math.abs(r.nextInt() % shakey);
		
		makeRotatedbyArrayCopy(pixels, tmppixels,height,width,shakex,shakey);
		
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				if ((i < border) | (j < border))
				{tmppixels[i*width +j] = bordercolor;}
			}
		}
		makeRotatedbyArrayCopy(tmppixels, pixels,height,width,border/2,border/2);
	}
	
	public static void blur(int[] what, int[] dest, int width, int height, boolean bluralpha, int blend)
	{
		int pos, grabber;
		int rbit,gbit,bbit,abit,invalph;
		int invblend = 0xff-blend;
		
		for (int i = 1; i < height-1; i++)
		{
			for (int j = 1; j < width-1; j++)
			{
				pos = j+i*width;
				grabber = what[(pos-width)-1];
				abit = ((grabber >> 24) & 0xff);
				rbit = ((grabber >> 16) & 0xff);
				gbit = ((grabber >>  8) & 0xff);
				bbit = ((grabber      ) & 0xff);
				grabber = what[(pos-width)];
				abit += ((grabber >> 24) & 0xff);
				rbit += ((grabber >> 16) & 0xff);
				gbit += ((grabber >>  8) & 0xff);
				bbit += ((grabber      ) & 0xff);
				grabber = what[(pos-width)+1];
				abit += ((grabber >> 24) & 0xff);
				rbit += ((grabber >> 16) & 0xff);
				gbit += ((grabber >>  8) & 0xff);
				bbit += ((grabber      ) & 0xff);
				grabber = what[pos-1];
				abit += ((grabber >> 24) & 0xff);
				rbit += ((grabber >> 16) & 0xff);
				gbit += ((grabber >>  8) & 0xff);
				bbit += ((grabber      ) & 0xff);
				grabber = what[pos];
				abit += ((grabber >> 24) & 0xff);
				rbit += ((grabber >> 16) & 0xff);
				gbit += ((grabber >>  8) & 0xff);
				bbit += ((grabber      ) & 0xff);
				grabber = what[pos+1];
				abit += ((grabber >> 24) & 0xff);
				rbit += ((grabber >> 16) & 0xff);
				gbit += ((grabber >>  8) & 0xff);
				bbit += ((grabber      ) & 0xff);
				grabber = what[(pos+width)-1];
				abit += ((grabber >> 24) & 0xff);
				rbit += ((grabber >> 16) & 0xff);
				gbit += ((grabber >>  8) & 0xff);
				bbit += ((grabber      ) & 0xff);
				grabber = what[(pos+width)];
				abit += ((grabber >> 24) & 0xff);
				rbit += ((grabber >> 16) & 0xff);
				gbit += ((grabber >>  8) & 0xff);
				bbit += ((grabber      ) & 0xff);
				grabber = what[(pos+width)+1];
				abit += ((grabber >> 24) & 0xff);
				rbit += ((grabber >> 16) & 0xff);
				gbit += ((grabber >>  8) & 0xff);
				bbit += ((grabber      ) & 0xff);
				abit /= 9;
				rbit /= 9;
				gbit /= 9;
				bbit /= 9;
				grabber = dest[pos];
				abit = ( (((grabber >> 24) & 0xff) * invblend) >> 8) + ((abit * blend) >> 8);
				rbit = ( (((grabber >> 16) & 0xff) * invblend) >> 8) + ((rbit * blend) >> 8);
				gbit = ( (((grabber >>  8) & 0xff) * invblend) >> 8) + ((gbit * blend) >> 8);
				bbit = ( (((grabber      ) & 0xff) * invblend) >> 8) + ((bbit * blend) >> 8);
				
				if (!bluralpha) abit = ((grabber >> 24) & 0xff);
				dest[pos] = pack(abit,rbit,gbit,bbit);
			}
		}
	}
	
	//Grab pixels from a java Image object into an int[] array
	public static void grabPixels(Image img, int[] dest)
	{
		if( img == null ) return;
		pixelgrabdest = dest;
		
		int w = img.getWidth(new PixelTools());
		int h = img.getHeight(new PixelTools());
		
		if ((w>0) & (h>0))
		{
			PixelGrabber pixelmaker = new PixelGrabber(img, 0, 0, w, h, dest, 0, w );
			try
			{pixelmaker.grabPixels();} 
			catch (InterruptedException e) 
			{
				System.err.println("Pixel grab failed or was interrupted.");
	    		return;
	    	}
	    	
	    	if ((pixelmaker.getStatus() & ImageObserver.ABORT) != 0) 
	    	{
	    		System.err.println("Failure during pixel grab of image, load aborted.");
	    		return;
			}
		} else {
			//wait for the image to be loaded
		}
	}
	
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
	{
		if ((infoflags & ImageObserver.ALLBITS) != 0) System.out.println("ALLBITS flag registered in ImageUpdate");
		
		if ( ((infoflags & ImageObserver.WIDTH) != 0) && ((infoflags & ImageObserver.HEIGHT) != 0) )
		{
			grabPixels(img,pixelgrabdest);
			return false; //stop notifying
		} else {
			if ((infoflags & ImageObserver.ABORT) != 0) System.err.println("Error, could not grab pixels, Image load/production was aborted.");
			return true; //keep waiting and keep notifying
		}
	}
}
