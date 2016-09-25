import java.awt.*;
import java.util.*;
import java.awt.image.*;
import java.applet.*;

// a few useful pixel routines
//
// inkling 2001. <daniel.wills@student.adelaide.edu.au>
// enkling 2004. <dan@rsp.com.au>

public final class MiniPixelTools implements ImageObserver
{
	//pixel extractor selector constants:
	
	public enum pixelChannel
	{
		Red,
		Green,
		Blue,
		Alpha,
		DifferenceRGB,
		AverageRGB,
		MaximumRGB,
		MinimumRGB,
		DifferenceMaxMin,
		Hue,
		Saturation,
		Luminosity,
		Pseudorandom,
		Gaussrandom,
		RuleValue,
		StaticValue,
		Sine,
		Tangent,
		BitwiseAND,
		BitwiseOR,
		BitwiseXOR,
		Multiply,
		ScreenMultiply,
		ShrinkBits,
		CountBits,
		ModuloAdd,
		ModuloMult,
		Harmony,
		Noise3D,
		Noise1D,
		SinesThingy;
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( pixelChannel p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}
	
	/*final static int RED          =  0;
	final static int GREEN        =  1;
	final static int BLUE         =  2;
	final static int AVG          =  3;
	final static int MAX          =  4;
	final static int MIN          =  5;
	final static int HUE          =  6;
	final static int SATURATION   =  7;
	final static int LUMI         =  8;
	final static int PSEUDORANDOM =  9;
	final static int GAUSSRANDOM  =  10;
	final static int TABLE        =  11;
	final static int SINE         =  12;
	final static int TANGENT      =  13;
	final static int AND          =  14;
	final static int OR           =  15;
	final static int XOR          =  16;
	final static int MUL          =  17;
	final static int SHRINKBIT    =  18;
	final static int COUNTBIT     =  19;
	final static int MODULO       =  20;
	final static int HARMONY      =  21;
	final static int HASHCODE     =  22;
	final static int NUMOPS       =  23;
	final static int STATICLEVEL  =  24;
	
	final static int NUMMODES = 25;
	public final static String[] CHANNELNAMES = {"Red",
											     "Green",
											     "Blue",
											     "Average RGB",
											     "Maximum RGB",
											     "Minimum RGB",
											     "Hue",
											     "Saturation",
											     "Luminosity",
											     "Pseudorandom",
											     "GaussRandom",
											     "Table",
											     "Sine",
											     "Tangent",
											     "Bitwise And",
											     "Bitwise Or",
											     "Bitwise Xor",
											     "Multiply",
											     "Shrink Bits",
											     "Count Bits",
											     "Modulo",
											     "Harmony",
											     "Hashcode",
											     "NumOps",
											     "StaticLevel"};
											     
	*/
	//faster HSV transform
	//bizzare bitwise blends/interleaves
	//ALPHA MEDIAN L A B C M Y BLACK WHITE XOR AND OR MUL etc
	
	//more than one pixel blend modes:
	//ALPHA_BLEND_FORWARD
	//ALPHA_BLEND_BACKWARD
	//MULTIPLY
	//MAX MIN MED AND OR XOR
	//CLOSEST_TO_CURRENTCOLOR
	//MULTIPLY_INVERTED
	//RED GREEN BLUE HUE SAT LUMI DEFAULT
	//GRADIENTLOOKUP TEXTURELOOKUP EXPRESSION
	
	//pixel interpolator selector constants:
	final static int NEAREST          = 0;
	final static int LINEAR           = 1;
	final static int BILINEAR         = 2;
	final static int TRILINEAR        = 3;
	final static int BSPLINE          = 4;
	final static int FRACTALTILE      = 5;
	final static int NUMINTERPOLATERS = 6;
	
	
	static pixelChannel defaultchannel = pixelChannel.Saturation;
	static float channeloffset = 0;
	static float channelmultiplier = 0.001f;
	
	static int rw,gw,bw,vw,lw,tw1,tw2,tw3; //working variables
	static long opcount = 0;
	static int[] unpacker;
	static float[] funpacker;
	static int[] pixelgrabdest;
	static MemoryImageSource memimgsrc;
	//static Applet context;
	static float[] ruleFloatArray;
	static Color currentcolor;
	static int currentdefault;
	static boolean lastdefaultisripe = false;
	static float lastdefaultfloat = 0;
	static int lastdefaultinput = 0;
	
	static float currentdefaultgain = 3.0f;
	static float currentdefaultpower = 5.0f;
	static int currentdefaultclamp = 0xff;
	static float currentdefloat;
	static float baseLFOFreq = 0.01f;
	public static Random randex = new Random();
	//public static Class classType()
	//{
	//	return getClass();	
	//}
	
	
	public final static void unpack(int p, int[] up)
	{ 
		up[0] = (p >> 24) & 0xff; //a
		up[1] = (p >> 16) & 0xff; //r
		up[2] = (p >>  8) & 0xff; //g
		up[3] = (p      ) & 0xff; //b
	}
	
	public final static void unpackNoAlpha(int p, int[] up)
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
	
	//pack an int array into a packed int pixel
	public final static int pack(int[] tp)
	{
		//loop unrolled for speed
		//clip all values to 0..255
		tp[0] = ((tp[0] < 0) ? 0 : ((tp[0] > 0xff) ? 0xff : tp[0]));
		tp[1] = ((tp[1] < 0) ? 0 : ((tp[1] > 0xff) ? 0xff : tp[1]));
		tp[2] = ((tp[2] < 0) ? 0 : ((tp[2] > 0xff) ? 0xff : tp[2]));
		tp[3] = ((tp[3] < 0) ? 0 : ((tp[3] > 0xff) ? 0xff : tp[3]));
		return (tp[0] << 24) | (tp[1] << 16) | (tp[2] << 8) | tp[3];
	}
	
	public final static int pack(int a, int r, int g, int b)
	{
		//clip all values to 0..255	
		a = ((a < 0) ? 0 : ((a > 0xff) ? 0xff : a));
		r = ((r < 0) ? 0 : ((r > 0xff) ? 0xff : r));
		g = ((g < 0) ? 0 : ((g > 0xff) ? 0xff : g));
		b = ((b < 0) ? 0 : ((b > 0xff) ? 0xff : b));
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	public final static int pack(int r, int g, int b)
	{
		//clip all values to 0..255
		r = ((r < 0) ? 0 : ((r > 0xff) ? 0xff : r));
		g = ((g < 0) ? 0 : ((g > 0xff) ? 0xff : g));
		b = ((b < 0) ? 0 : ((b > 0xff) ? 0xff : b));
		return 0xff000000 | (r << 16) | (g << 8) | b;
	}
	
	//use when the ints are unlikely to be much over 255 if at all, and not negative
	public final static int quickPack(int[] tp)
	{
		//clip all values to 0..255
		tp[0] = tp[0] & 0xff;
		tp[1] = tp[1] & 0xff;
		tp[2] = tp[2] & 0xff;
		tp[3] = tp[3] & 0xff;
		return (tp[0] << 24) | (tp[1] << 16) | (tp[2] << 8) | tp[3];
	}
	
	public final static int quickPack(int a, int r, int g, int b)
	{
		//and all values within 0..255
		return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	}
	
	//assumes an array 3 ints long
	public final static int packIgnoreAlpha(int[] tp)
	{
		//clip values to 0..0xff
		tp[0] = ((tp[0] < 0) ? 0 : ((tp[0] > 0xff) ? 0xff : tp[0]));
		tp[1] = ((tp[1] < 0) ? 0 : ((tp[1] > 0xff) ? 0xff : tp[1]));
		tp[2] = ((tp[2] < 0) ? 0 : ((tp[2] > 0xff) ? 0xff : tp[2]));
		return (int) (0xff << 24) + (tp[0] << 16) + (tp[1] << 8) + tp[2];
	}
	
	//make a packed pixel int from the values in the given int array
	public final static int packNoClipOrAlpha(int[] tp)
	{
		return (int) (0xff << 24) + (tp[0] << 16) + (tp[1] << 8) + tp[2];
	}
	
	//get max(r,g,b) in unpacked array, ignore alpha. ie position [0]
	public final static int max(int[] test)
	{
		int tst = test[1];
		if (test[2] > tst) tst = test[2];
		if (test[3] > tst) tst = test[3];
		return tst;
	}
	
	public final static int max(int tst) //operates on 24bit packed RGB's
	{
		//what speed do conditional assignments go at?
		//would "if (i>tst) {doit}" be faster?
		int test = (tst & 0xff); //set to blue first
		int i = (tst >> 8) & 0xff;
		test = (i > test) ? i : test; //set to green if larger
		i = (tst >> 16) & 0xff;
		test = (i > test) ? i : test; //set to red if larger
		return tst;
	}
	
	//get min(r,g,b) in unpacked array, ignore alpha in position 0.
	public final static int min(int[] test)
	{
		int tst = test[1];
		if (test[2] < tst) tst = test[2];
		if (test[3] < tst) tst = test[3];
		return tst;
	}
	
	//get min(r,g,b) in packed int, ignore alpha.
	//min reflects the relative proportion of white in the color
	public final static int min(int test)
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
	public final static int avg(int[] test)
	{
		return (test[1]+test[2]+test[3])/3;
	}
	
	public final static int saturation(int of)
	{
		//return 255 - min(of);
		//use the Color API version:
		float tmp[] = new float[3];
		tmp = Color.RGBtoHSB((of>>16)&0xff,(of>>8)&0xff,(of)&0xff,tmp);
		return (int) (tmp[1]*255.0f);
	}
	
	public final static int hue(int of)
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
	
	public final static int value(int of)
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
	
	public final static int getDefault(int p)
	{
		int k = (int) getDefaultF(p);
		return (k > 0) ? ((k < 0xff) ? k : 0xff) : 0;
	}
		
	public final static float getDefaultF(int p)
	{
		//cache this transformation
		if ( ( p == lastdefaultinput ) && lastdefaultisripe ) return lastdefaultfloat;
		
		lastdefaultinput = p;
		
		float returned = p;
		
		switch( defaultchannel )
		{
			case Red :
			{
				returned = (p >> 16) & 0xff;
				break;
			}
			
			case Green :
			{
				returned = (p >> 8) & 0xff;
				break;
			}
			
			case Blue :
			{
				returned = (p & 0xff);
				break;
			}
			
			case Alpha :
			{
				returned = (p >> 24) & 0xff;
				break;
			}
			
			case DifferenceRGB:
			{
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;
				returned = Math.abs( r - b) + Math.abs( r - g ) + Math.abs( g - b );
			}
			
			case AverageRGB :
			{
				returned = ( ((p >> 16) & 0xff)+
			    		     ((p >>  8) & 0xff)+
			    		     ((p      ) & 0xff) )/3;
			    break;
			}
			
			case MaximumRGB :
			{
				int test = (p & 0xff); //set to blue first
				int i = (p >> 8) & 0xff;
				test = (i > test) ? i : test; //set to green if larger
				i = (p >> 16) & 0xff;
				test = (i > test) ? i : test; //set to red if larger
				returned = test;
				break;
			}
			
			case MinimumRGB :
			{
				int tst = Integer.MAX_VALUE;
				int i = ((p >> 16) & 0xff);
				if (i < tst) tst = i;
				i = ((p >> 8) & 0xff);
				if (i < tst) tst = i;
				i = (p & 0xff);
				if (i < tst) tst = i;
				returned = tst;
				break;
			}
			
			case DifferenceMaxMin :
			{
				int test = (p & 0xff); //set to blue first
				int i = (p >> 8) & 0xff;
				test = (i > test) ? i : test; //set to green if larger
				i = (p >> 16) & 0xff;
				test = (i > test) ? i : test; //set to red if larger
				
				int tst = Integer.MAX_VALUE;
				int j = ((p >> 16) & 0xff);
				if (j < tst) tst = j;
				j = ((p >> 8) & 0xff);
				if (j < tst) tst = j;
				j = (p & 0xff);
				if (j < tst) tst = j;
				
				returned = Math.abs( test - tst );
				break;
			}
			
			case Hue : 
			{
				float tmp[] = new float[3];
				tmp = Color.RGBtoHSB((p >> 16)&0xff,(p >> 8)&0xff,p&0xff,tmp);
				returned = (int) (tmp[0]*255.0f);
				break;
			}
			
			case Saturation : 
			{
				float tmp[] = new float[3];
				tmp = Color.RGBtoHSB((p>>16)&0xff,(p>>8)&0xff,(p)&0xff,tmp);
				returned = (int) (tmp[1]*255.0f);
				break;
			}
			
			case Luminosity : 
			{
				float tmp[] = new float[3];
				tmp = Color.RGBtoHSB((p>>16)&0xff,(p>>8)&0xff,(p)&0xff,tmp);
				returned = (int) ( tmp[2] * 255.0f ) ;
				break;
			}
			
			case Pseudorandom : 
			{ 
				returned = randex.nextInt()%0xff; returned = (returned < 0) ? -returned : returned; break;
			}
			
			case Gaussrandom  : 
			{
				returned = (int)( randex.nextGaussian() * 0xff ); 
				returned = (returned < 0) ? 0 : (returned > 0xff) ? 0xff : returned; 
				break;
			}
			
			case RuleValue:
			{
				if (ruleFloatArray != null)
				{
					currentdefloat = currentdefloat + baseLFOFreq;
					float modix = currentdefloat % 1.0f;
					if (modix < 0) modix += 1.0f;
					int ruleix = (int) ( modix * ( ruleFloatArray.length - 1) );
					returned = (int) ( ruleFloatArray[ ruleix ] * 255.0f );
				}
				break;
			}
			
			case StaticValue :
			{
				returned = currentdefault;
				break;
			}
			case Sine : 
			{
				currentdefloat = currentdefloat + baseLFOFreq;
				returned = (int)( ( Math.sin(currentdefloat)/2.0f + 0.5f ) * 0xff );
				returned = (returned < 0) ? 0 : (returned > 0xff) ? 0xff : returned; 
				break;
			}
			case Tangent : 
			{
				currentdefloat = currentdefloat + baseLFOFreq;
				returned = (int)( ( Math.tan(currentdefloat)/2.0f + 0.5f ) * 0xff );
				returned = (returned < 0) ? 0 : (returned > 0xff) ? 0xff : returned; 
				break;
			}
			
			case BitwiseAND : 
			{
				returned = ((p&0x00ff0000) >> 16) & ((p&0x0000ff00) >> 8) & (p&0x000000ff);
				break;
			}
			
			case BitwiseOR : 
			{
				returned = ((p&0x00ff0000) >> 16) | ((p&0x0000ff00) >> 8) | (p&0x000000ff);
				break;
			}
			
			case BitwiseXOR : 
			{
				returned = ((p&0x00ff0000) >> 16) ^ ((p&0x0000ff00) >> 8) ^ (p&0x000000ff);
				break;
			}
			
			case Multiply : 
			{
				//returned = (((((p&0x00ff0000) >> 16) * ((p&0x0000ff00) >> 8)) >> 8) * (p&0x000000ff)) >> 8; //pretend its fixed point
				int r = ( p & 0x00ff0000 ) >> 16;
				int g = ( p & 0x0000ff00 ) >> 8;
				int b = p & 0x000000ff;
				
				returned = (  (  (  r  * g  ) >> 8 ) * b ) >> 8; //pretend its fixed point ( ( R * G >> 8 ) * B ) >> 8
				break;
			}
			
			case ScreenMultiply : 
			{
				int r = 0xff - ( ( p & 0x00ff0000 ) >> 16 );
				int g = 0xff - ( ( p & 0x0000ff00 ) >> 8 );
				int b = 0xff - ( p & 0x000000ff );		
				returned = 0xff - (  (  (  r  * g  ) >> 8 ) * b ) >> 8; //pretend its fixed point ( ( R * G >> 8 ) * B ) >> 8
				break;
			}
			
			case ShrinkBits    : 
			{
				int tmp = p;
				int count = 0;
				for (int i = 0; i < 32; i++)
				{
					if ((tmp & 0x00000001) != 0) count = (count << 1)|0x00000001;
					tmp = tmp >> 1;
				}
				returned = count;
				break;
			}
			
			case CountBits    : 
			{
				int tmp = p;
				int count = 0;
				for (int i = 0; i < 32; i++)
				{
					if ((tmp & 0x00000001) != 0) count++;
					tmp = tmp >> 1;
				}
				returned = count;
				break;
			}
			
			case ModuloAdd  : 
			{
				returned = ( ((p & 0x00ff0000) >> 16) + ((p & 0x0000ff00) >> 8) + (p & 0x000000ff)) % 0xff;
				break;
			}
			
			case ModuloMult : 
			{
				returned = ( ((p & 0x00ff0000) >> 16) * ((p & 0x0000ff00) >> 8) * (p & 0x000000ff)) % 0xff;
				break;
			}
			
			case Harmony : 
			{
				int r = (p&0x00ff0000) >> 16;
				int g = (p&0x0000ff00) >> 8;
				int b = (p&0x000000ff);
				returned =  (int)Math.sqrt(r*r + g*g + b*b);
				break;
			}
			
			case Noise1D :
			{
				currentdefloat = currentdefloat + baseLFOFreq;
				returned = (int)  ( improvedPerlinNoise.improvedPerlinNoiseThree(currentdefloat, -150.52, 7.47 ) * 0xff );
				returned = (returned < 0) ? 0 : (returned > 0xff) ? 0xff : returned; 
				break;
			}
			case Noise3D:
			{
				double r = (double) ( ( p & 0x00ff0000 ) >> 16 )/255.0;
				double g = (double) ( ( p & 0x0000ff00 ) >> 8 )/255.0;
				double b = (double) ( ( p & 0x000000ff ) )/255.0;
				returned = (int)( 256.0 * improvedPerlinNoise.improvedPerlinNoiseThree( r * 14.0 * baseLFOFreq, g * 14.0 * baseLFOFreq, b * 14.0 * baseLFOFreq) );
				break;
			}
			case SinesThingy :
			{
				double r = (double) ( ( p & 0x00ff0000 ) >> 16 )/255.0;
				double g = (double) ( ( p & 0x0000ff00 ) >> 8 )/255.0;
				double b = (double) ( p & 0x000000ff ) / 255.0;
				returned = (float) ( 85.0 * Math.sin( r * 8.341235 + 46.22) +  85.0 * Math.sin( g * 11.55033 - 206.8987) + 85.0 * Math.sin( b * 9.341235 + 74.18531 )  ) ; //a third of 255 each.. somewierdmapping into sineland..
				break;
			}
			
			//case HASHCODE     : {returned = (new Color(p)).hashCode() % 0xff; break;}
			//case NUMOPS       : {returned = (int)opcount % 0xff; break;}
			//case STATICLEVEL  : {returned = currentdefault; break;}

		}
		returned = (float)Math.pow((returned/255.0f),currentdefaultpower)*255.0f;
		returned += channeloffset;
		
		if (channelmultiplier != 1.0f) returned = (channelmultiplier * returned);
		//if (returned > 0xff) returned = 0xff;
		//if (returned < 0) returned = 0;
		
		//save the cache
		lastdefaultisripe = true;
		lastdefaultfloat = returned;
		return returned;
	}
	
	public final static void setChannelMultiplier( float to )
	{
		if (channelmultiplier != to) lastdefaultisripe = false;
		channelmultiplier = to; }
	
	public final static void setChannelOffset( float to )
	{
		if (channeloffset != to) lastdefaultisripe = false;
		channeloffset = to;
	}
	
	public final static void setChannelPower( float to )
	{
		if (currentdefaultpower != to) lastdefaultisripe = false;
		currentdefaultpower = to;
	}
	
	public final static void setChannelGain( float to )
	{
		if (currentdefaultgain != to) lastdefaultisripe = false;
		currentdefaultgain = to;
	}
	
	public final static void setStaticChannel( int to )
	{
		if (currentdefaultgain != to) lastdefaultisripe = false;
		currentdefault = to;
	}
	
	public final static void setBaseLFOFreq( float to )
	{ 
		if (baseLFOFreq != to) lastdefaultisripe = false;
		baseLFOFreq = to; 
	}
	
	public final static void setFloatRuleArray( float to[] )
	{ 
		//treat this as saying it's changed?!? not sure.. better be safe 'n' slow I guess
		lastdefaultisripe = false;
		ruleFloatArray = to; 
	}
	
	public final static void setChannelTable( int[] to )
	{ 
		lastdefaultisripe = false;
		unpacker = to; 
	}
	
	public final static void init( ) //Applet a
	{
		//generate some stuff
		unpacker = new int[3];
		funpacker = new float[3];
		//setContext(a);
		//context.showStatus("PixelTools Initialised...");
	}
	
	//needs to be called at initialisation time.. a static constructor perhaps??
	//public final static void setContext(Applet a)
	//{context = a;}
	
	public final static void setDefaultChannel(int towhat)
	{
		lastdefaultisripe = false;
		defaultchannel = pixelChannel.values()[towhat];
	}
	
	public final static pixelChannel getDefaultChannel()
	{ return defaultchannel; }
	
	
	//next two methods need renaming
	//averages the red green and blue components of a color
	public final static int getGrey(int p)
	{ 
		return ( ((p >> 16) & 0xff)+
			     ((p >>  8) & 0xff)+
			     ((p      ) & 0xff) )/3;
	}
	
	public final static float getFloatGrey(int p)
	{ 
		return ((float)((p >> 16) & 0xff)+ 
		        (float)((p >>  8) & 0xff)+
		        (float)((p      ) & 0xff) )/3.0f;
	}
	
	//Make an rgb grey out of one int.
	public final static int makePackedGrey(int p)
	{ 
		p = ((p < 0) ? 0 : ((p > 0xff) ? 0xff : p));
		return (0xff000000) | (p << 16) | (p << 8) | p; //full alpha
	}
	
	//Make an rgb grey out of a packed color.
	public final static int removeColor(int p)
	{ 
		int val =( ((p >> 16) & 0xff)+
			       ((p >>  8) & 0xff)+
			       ((p      ) & 0xff) )/3;		     
		return (int) (p & 0xff000000) + (val << 16) + (val << 8) + val; //full alpha
	}
	
	//Multiply a packed color by a scalar value, returns a packed color.
	public final static int mulColor(int packedcol,int p)
	{ 
		p = ((p < 0) ? 0 : ((p > 0xff) ? 0xff : p));
		
		int redbit =   (((packedcol >> 16) & 0xff) * p) >> 8;
		int greenbit = (((packedcol >> 8 ) & 0xff) * p) >> 8;
		int bluebit =  (((packedcol      ) & 0xff) * p) >> 8;
		
		return (int) (packedcol & 0xff000000) + (redbit << 16) + (greenbit << 8) + bluebit; //full alpha
	}
	
	//divide each channel of a packed color by a scalar value
	public final static int divScalarColor(int packedcol,int p)
	{ 
		p = ((p < 0) ? 0 : ((p > 0xff) ? 0xff : p));
		
		int redbit =   (((packedcol >> 16) & 0xff) / p) & 0xff;
		int greenbit = (((packedcol >> 8 ) & 0xff) / p) & 0xff;
		int bluebit =  (((packedcol      ) & 0xff) / p) & 0xff;
		return (packedcol & 0xff000000) + (redbit << 16) + (greenbit << 8) + bluebit;
	}
	
	
	//add a scalar value to each channel of a packedpixel
	public final static int addScalarColor(int packedcol,int p)
	{ 
		p = ((p < 0) ? 0 : ((p > 0xff) ? 0xff : p)); //clip p
		int redbit =   ( ((packedcol & 0x00ff0000) >> 16) + p);
		int greenbit = ( ((packedcol & 0x0000ff00) >> 8 ) + p);
		int bluebit =  (  (packedcol & 0x000000ff       ) + p);
		redbit = ((redbit < 0) ? 0 : ((redbit > 0xff) ? 0xff : redbit));         //clip red
		greenbit = ((greenbit < 0) ? 0 : ((greenbit > 0xff) ? 0xff : greenbit)); //clip green
		bluebit = ((bluebit < 0) ? 0 : ((bluebit > 0xff) ? 0xff : bluebit));     //clip blue
		return (packedcol & 0xff000000) | (redbit << 16) | (greenbit << 8) | bluebit;
	}
	
	//Standard alpha blend with only a single alpha, the source strength.
	public final static int blendColors(int s,int d,int srcamount)
	{
		int invsrc = 0xff - srcamount;
		int redbit =   ((((s >> 16) & 0xff) * srcamount) >> 8) + ((((d >> 16) & 0xff) * invsrc) >> 8);
		int greenbit = ((((s >> 8 ) & 0xff) * srcamount) >> 8) + ((((d >> 8 ) & 0xff) * invsrc) >> 8);
		int bluebit =  ((((s      ) & 0xff) * srcamount) >> 8) + ((((d      ) & 0xff) * invsrc) >> 8);	
		return (int) (0xff000000) | (redbit << 16) | (greenbit << 8) | bluebit; //full alpha
	}
	
	//Blends 2 argb colors and averages their alpha's.
	public final static int pixelAlphaBlend(int s,int d)
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
	public final static int getInterpolatedPixel(double x, double y, int[] pixels, int width, int height)
	{
		int xbase = (int)x;
		int ybase = (int)y;
		double xFraction = x - xbase;
		double yFraction = y - ybase;
		int offset = ybase * width + xbase;
		
		if (offset < 0) { offset = 0; }
		//if (offset > pixels.size) { offset = 0; }
		
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
	//destroys the alpha channel
	public final static void setLuminosity(int[] lumi, int[] of)
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
	public final static void fillAlphaChannel(int alpha, int[] of)
	{	
		alpha = ((alpha < 0) ? 0 : ((alpha > 0xff) ? 0xff : alpha));
		for (int i = 0; i < of.length; i++)
		{of[i] = (of[i] & 0x00ffffff) | (alpha << 24);}
	}
	
	//Add a value to the alpha channel
	public final static void addAlphaChannel(int[] dest,int add)
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
	public final static void setAlphaChannel(int[] alpha, int[] of)
	{
		int inpixun[] = new int[4];
		
		for (int i = 0; i < of.length; i++)
		{	
			alpha[i] = ((alpha[i] < 0) ? 0 : ((alpha[i] > 0xff) ? 0xff : alpha[i]));
			of[i] = (of[i] & 0x00ffffff) | (alpha[i] << 24);
		}
	}
	
	//Set the alpha channel of a packedpixel array using the luminosity of another.
	public final static void setAlphaToLumi(int[] lumi, int[] of)
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
	public final static void multiply(int[] src, int[] dest, int multiplier)
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
	public final static void add(int[] src, int[] dest, int amount)
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
	public final static void threshold(int[] src, int[] dest, int threshold, int abovelevel, int belowlevel)
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
	public final static void invert(int[] src, int[] dest, boolean invertalpha)
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
	public final static void imageMultiply(int[] src1, int[] src2, int[] dest)
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
	public final static void imageMixByScalar(int[] src1, int[] src2, int split, int[] dest)
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
	public final static void imageCopy(int[] src, int[] dest)
	{
		if (src.length == dest.length)
		{
			System.arraycopy(src, 0, dest, 0, src.length);
		} else { System.err.println("Tried to imageCopy into a different sized image."); }
	}
	
	//reinventing the wheel once more. . .
	//draws an image on dest[] by drawing the dest pixel inverted by
	//the grey level of the pixel to draw.
	public final static void drawXORMode(int xpos, int ypos, 
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
			drawstartoffset += -xpos;
			//get real dest startpos and increment
			//get real draw startpos, length and increment
			//set a flag perhaps...
		}
		
		if (ypos < 0) 
		{
			//drawstartoffset += Math.abs(ypos)*drawwidth;
			linecount = -ypos;
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
	
	//public final static void drawDefaultBlendMode(int xpos, int ypos, 
	//                      int[] dest, int destwidth, int destheight, 
	//                      int[] draw, int drawwidth, int drawheight )
	
	//draws an image using its alpha channel to blend
	public final static void drawWithAlpha(int xpos, int ypos, 
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
				rasterstartoffset = -xpos;
				rasterlength = drawwidth - rasterstartoffset;
				xpos = 0;
			}
		
			if (ypos < 0) 
			{
				firstraster = -ypos;
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
					
					int alpha = (srcpixel & 0xff000000) >>> 24;
					//System.out.println("alpha: " + alpha + ".");
					//int alpha = 0x88;
					
					if (alpha != 0)
					{
						int invalpha = 0xff - alpha;
						
						int redbit   = ( (((srcpixel & 0x00ff0000) >> 16) * alpha) >> 8) + ( ( ((destpixel & 0x00ff0000) >> 16) * invalpha) >> 8 );
						int greenbit = ( (((srcpixel & 0x0000ff00) >> 8)  * alpha) >> 8) + ( ( ((destpixel & 0x0000ff00) >> 8)  * invalpha) >> 8 );
						int bluebit  = ( ( (srcpixel & 0x000000ff)        * alpha) >> 8) + ( (  (destpixel & 0x000000ff)        * invalpha) >> 8 );
						
						redbit =   (redbit   > 0xff) ? 0xff : (redbit   < 0) ? 0 : redbit;
						greenbit = (greenbit > 0xff) ? 0xff : (greenbit < 0) ? 0 : greenbit;
						bluebit =  (bluebit  > 0xff) ? 0xff : (bluebit  < 0) ? 0 : bluebit;

						//dest[destpos] = (0xff000000) | srcpixel & 0x00ffffff;
						dest[destpos] = (0xff000000) | (redbit << 16) | (greenbit << 8) | (bluebit);
						
					}
						//quickPack(0xff,redbit,greenbit,bluebit);	
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
	public final static void drawWithThresholdAlpha(int xpos, int ypos, 
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
			if (xpos < 0) { rasterstartoffset = -xpos; rasterlength = drawwidth - rasterstartoffset; xpos = 0; }
			if (ypos < 0) { firstraster = -ypos; ypos = 0; }
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
	public final static void drawWithLuminosityAsAlphaAndAConstantMultiplier(int xpos, int ypos, 
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
			if (xpos < 0) { rasterstartoffset = -xpos; rasterlength = drawwidth - rasterstartoffset; xpos = 0; }
			if (ypos < 0) { firstraster = -ypos; ypos = 0; }
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
	public final static void drawWithoutAlpha(int xpos, int ypos, 
	                      int[] dest, int destwidth, int destheight, 
	                      int[] draw, int drawwidth, int drawheight )
	{
		//some primitive clipping may occur ;)
		int drawstartoffset = 0;
		int linecount = 0;
		int linecountmul = 0;
		int clippedrasterlength;
		
		if (xpos < 0) drawstartoffset += -xpos;
		//get real dest startpos and increment
		//get real draw startpos, length and increment
		if (ypos < 0) linecount = -ypos;
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
	
	public final static void drawSingleChannelAsRGB(int dest[], int[] src,int mapmul,int mapoffset, int w, int h)
	{
		int pos;
		for (int i = 0; i < h; i++)
		{
			for (int j = 0; j < w; j++)
			{
				pos = j + i * w;
				int clampedval = src[pos] * mapmul + mapoffset;
				clampedval = (clampedval < 0) ? 0 : (clampedval > 0xff) ? 0xff : clampedval;
				clampedval = clampedval & 0xff;
				dest[pos] = (clampedval << 16) + (clampedval << 8) + (clampedval);
			}
		}
	}
	
	//Draw one image onto another, using the source alpha, assumes the two 
	//images are the same size
	public final static void drawWithAlpha(int[] dest,int[] draw)
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
	
	//Draw one image onto another, using the source alpha, assumes the two 
	//images are the same size
	public final static void drawWithAlphaAndOffset(int[] dest,int[] draw, int offset)
	{	
		int redbit;
		int greenbit;
		int bluebit;
		int destpixel;
		int srcpixel;
		int alpha;
		int invalpha;
		int destlength = dest.length;
		
		for (int i = 0; i < destlength; i++)
		{
			destpixel = dest[i];
			
			int srcoffs = (i+offset) % destlength;
			
			if (srcoffs < 0) srcoffs += destlength;
			
			srcpixel = draw[srcoffs];
			
			alpha = ((srcpixel >> 24) & 0x000000ff);
			invalpha = 0xff - alpha;
			
			redbit   = ((((srcpixel >> 16) & 0xff) * alpha) >> 8) + ((((destpixel >> 16) & 0xff) * invalpha) >> 8);
			greenbit = ((((srcpixel >>  8) & 0xff) * alpha) >> 8) + ((((destpixel >>  8) & 0xff) * invalpha) >> 8);
			bluebit  = ((((srcpixel      ) & 0xff) * alpha) >> 8) + ((((destpixel      ) & 0xff) * invalpha) >> 8);
			dest[i] = ((0xff << 24) | (redbit << 16) | (greenbit << 8) | bluebit);	
		}
	}
	
	//max-composites one image onto another, using the source alpha, assumes the two 
	//images are the same size
	public final static void maxWithAlphaAndOffset(int[] dest,int[] draw, int offset)
	{	
		int redbit;
		int greenbit;
		int bluebit;
		int destpixel;
		int destpixelr;
		int destpixelg;
		int destpixelb;
		int srcpixel;
		int srcpixelr;
		int srcpixelg;
		int srcpixelb;
		int alpha;
		int invalpha;
		int destlength = dest.length;
		
		for (int i = 0; i < destlength; i++)
		{
			destpixel = dest[i];
			
			int srcoffs = (i+offset) % destlength;
			
			if (srcoffs < 0) srcoffs += destlength;
			
			srcpixel = draw[srcoffs];
			
			srcpixelr = ((srcpixel >> 16) & 0xff);
			srcpixelg = ((srcpixel >>  8) & 0xff);
			srcpixelb = ((srcpixel      ) & 0xff);
			
			destpixelr = ((destpixel >> 16) & 0xff);
			destpixelg = ((destpixel >>  8) & 0xff);
			destpixelb = ((destpixel      ) & 0xff);
			
			alpha = ((srcpixel >> 24) & 0x000000ff);
			invalpha = 0xff - alpha;
			
			redbit   = (destpixelr > srcpixelr) ? destpixelr : ((srcpixelr * alpha) >> 8) + ((destpixelr * invalpha) >> 8);
			greenbit = (destpixelg > srcpixelg) ? destpixelg : ((srcpixelg * alpha) >> 8) + ((destpixelg * invalpha) >> 8);
			bluebit  = (destpixelb > srcpixelb) ? destpixelb : ((srcpixelb * alpha) >> 8) + ((destpixelb * invalpha) >> 8);
			
			dest[i] = ((0xff << 24) | (redbit << 16) | (greenbit << 8) | bluebit);	
		}
	}
	
	//max-composites one image onto another, using the source alpha, assumes the two 
	//images are the same size
	public final static void minWithAlphaAndOffset(int[] dest,int[] draw, int offset)
	{	
		int redbit;
		int greenbit;
		int bluebit;
		int destpixel;
		int destpixelr;
		int destpixelg;
		int destpixelb;
		int srcpixel;
		int srcpixelr;
		int srcpixelg;
		int srcpixelb;
		int alpha;
		int invalpha;
		int destlength = dest.length;
		
		for (int i = 0; i < destlength; i++)
		{
			destpixel = dest[i];
			
			int srcoffs = (i+offset) % destlength;
			
			if (srcoffs < 0) srcoffs += destlength;
			
			srcpixel = draw[srcoffs];
			
			srcpixelr = ((srcpixel >> 16) & 0xff);
			srcpixelg = ((srcpixel >>  8) & 0xff);
			srcpixelb = ((srcpixel      ) & 0xff);
			
			destpixelr = ((destpixel >> 16) & 0xff);
			destpixelg = ((destpixel >>  8) & 0xff);
			destpixelb = ((destpixel      ) & 0xff);
			
			alpha = ((srcpixel >> 24) & 0x000000ff);
			invalpha = 0xff - alpha;
			
			redbit   = (destpixelr < srcpixelr) ? destpixelr : ((srcpixelr * alpha) >> 8) + ((destpixelr * invalpha) >> 8);
			greenbit = (destpixelg < srcpixelg) ? destpixelg : ((srcpixelg * alpha) >> 8) + ((destpixelg * invalpha) >> 8);
			bluebit  = (destpixelb < srcpixelb) ? destpixelb : ((srcpixelb * alpha) >> 8) + ((destpixelb * invalpha) >> 8);
			
			dest[i] = ((0xff << 24) | (redbit << 16) | (greenbit << 8) | bluebit);	
		}
	}
	
	public final static void absSubWithAlphaAndOffset(int[] dest,int[] draw, int offset)
	{	
		int redbit;
		int greenbit;
		int bluebit;
		int destpixel;
		int destpixelr;
		int destpixelg;
		int destpixelb;
		int srcpixel;
		int srcpixelr;
		int srcpixelg;
		int srcpixelb;
		
		int srcdiffr;
		int srcdiffg;
		int srcdiffb;
		
		int alpha;
		int invalpha;
		int destlength = dest.length;
		
		for (int i = 0; i < destlength; i++)
		{
			destpixel = dest[i];
			
			int srcoffs = (i+offset) % destlength;
			
			if (srcoffs < 0) srcoffs += destlength;
			
			srcpixel = draw[srcoffs];
			
			srcpixelr = ((srcpixel >> 16) & 0xff);
			srcpixelg = ((srcpixel >>  8) & 0xff);
			srcpixelb = ((srcpixel      ) & 0xff);
			
			destpixelr = ((destpixel >> 16) & 0xff);
			destpixelg = ((destpixel >>  8) & 0xff);
			destpixelb = ((destpixel      ) & 0xff);
			
			alpha = ((srcpixel >> 24) & 0x000000ff);
			invalpha = 0xff - alpha;
			
			srcdiffr = (destpixelr < srcpixelr) ? srcpixelr - destpixelr : destpixelr - srcpixelr;
			srcdiffg = (destpixelg < srcpixelg) ? srcpixelg - destpixelg : destpixelg - srcpixelg;
			srcdiffb = (destpixelb < srcpixelb) ? srcpixelb - destpixelb : destpixelb - srcpixelb;
			
			redbit   = ((srcdiffr * alpha) >> 8) + ((destpixelr * invalpha) >> 8);
			greenbit = ((srcdiffg * alpha) >> 8) + ((destpixelg * invalpha) >> 8);
			bluebit  = ((srcdiffb * alpha) >> 8) + ((destpixelb * invalpha) >> 8);
			
			dest[i] = ((0xff << 24) | (redbit << 16) | (greenbit << 8) | bluebit);	
		}
	}
	
	//now something strange
	public final static void absAddWithAlphaAndOffset(int[] dest,int[] draw, int offset)
	{	
		int redbit;
		int greenbit;
		int bluebit;
		int destpixel;
		int destpixelr;
		int destpixelg;
		int destpixelb;
		int srcpixel;
		int srcpixelr;
		int srcpixelg;
		int srcpixelb;
		
		int srcdiffr;
		int srcdiffg;
		int srcdiffb;
		
		int alpha;
		int invalpha;
		int destlength = dest.length;
		
		for (int i = 0; i < destlength; i++)
		{
			destpixel = dest[i];
			
			int srcoffs = (i+offset) % destlength;
			
			if (srcoffs < 0) srcoffs += destlength;
			
			srcpixel = draw[srcoffs];
			
			srcpixelr = ((srcpixel >> 16) & 0xff);
			srcpixelg = ((srcpixel >>  8) & 0xff);
			srcpixelb = ((srcpixel      ) & 0xff);
			
			destpixelr = ((destpixel >> 16) & 0xff);
			destpixelg = ((destpixel >>  8) & 0xff);
			destpixelb = ((destpixel      ) & 0xff);
			
			alpha = ((srcpixel >> 24) & 0x000000ff);
			invalpha = 0xff - alpha;
			
			srcdiffr = (srcpixelr - 0x80) + destpixelr;
			srcdiffg = (srcpixelg - 0x80) + destpixelg;
			srcdiffb = (srcpixelb - 0x80) + destpixelb;
			
			srcdiffr = (srcdiffr > 0xff) ? 0xff : (srcdiffr < 0) ? 0 : srcdiffr;
			srcdiffg = (srcdiffg > 0xff) ? 0xff : (srcdiffg < 0) ? 0 : srcdiffg;
			srcdiffb = (srcdiffb > 0xff) ? 0xff : (srcdiffb < 0) ? 0 : srcdiffb;
			
			//if (srcdiffr > 0xff) srcdiffr = 0xff - srcdiffr;
			//if (srcdiffg > 0xff) srcdiffg = 0xff - srcdiffg;
			//if (srcdiffb > 0xff) srcdiffb = 0xff - srcdiffb;
			
			redbit   = ((srcdiffr * alpha) >> 8) + ((destpixelr * invalpha) >> 8);
			greenbit = ((srcdiffg * alpha) >> 8) + ((destpixelg * invalpha) >> 8);
			bluebit  = ((srcdiffb * alpha) >> 8) + ((destpixelb * invalpha) >> 8);
			
			dest[i] = ((0xff << 24) | (redbit << 16) | (greenbit << 8) | bluebit);	
		}
	}
	
	public final static void multiplyWithAlphaAndOffset(int[] dest,int[] draw, int offset)
	{	
		int redbit;
		int greenbit;
		int bluebit;
		
		int destpixel;
		//int destlumi;
		int destpixelr;
		int destpixelg;
		int destpixelb;
		
		int srcpixel;
		//int srclumi;
		int srcpixelr;
		int srcpixelg;
		int srcpixelb;
		
		int srcdiffr;
		int srcdiffg;
		int srcdiffb;
		
		int alpha;
		int invalpha;
		int destlength = dest.length;
		
		for (int i = 0; i < destlength; i++)
		{
			destpixel = dest[i];
			int srcoffs = (i + offset) % destlength;
			if (srcoffs < 0) srcoffs += destlength;
			
			srcpixel = draw[srcoffs];
			
			destpixelr = ((destpixel >> 16) & 0xff);
			destpixelg = ((destpixel >>  8) & 0xff);
			destpixelb = ((destpixel      ) & 0xff);
			//destlumi = (destpixelr + destpixelg + destpixelb)/3;
			
			srcpixelr = (((srcpixel >> 16) & 0xff) * destpixelr) >> 8;
			srcpixelg = (((srcpixel >>  8) & 0xff) * destpixelg) >> 8;
			srcpixelb = (((srcpixel      ) & 0xff) * destpixelb) >> 8;
			//srclumi =  (srcpixelr + srcpixelg + srcpixelb)/3;
						
			alpha = ((srcpixel >> 24) & 0x000000ff);
			invalpha = 0xff - alpha;
			
			redbit   = ((srcpixelr * alpha) >> 8) + ((destpixelr * invalpha) >> 8);
			greenbit = ((srcpixelg * alpha) >> 8) + ((destpixelg * invalpha) >> 8);
			bluebit  = ((srcpixelb * alpha) >> 8) + ((destpixelb * invalpha) >> 8);
			
			dest[i] = ((0xff << 24) | (redbit << 16) | (greenbit << 8) | bluebit);	
		}
	}
	
	public final static void lumiDiffMixWithAlphaAndOffset(int[] dest,int[] draw, int offset)
	{	
		int redbit;
		int greenbit;
		int bluebit;
		
		int destpixel;
		int destlumi;
		int destpixelr;
		int destpixelg;
		int destpixelb;
		
		int srcpixel;
		int srclumi;
		int srcpixelr;
		int srcpixelg;
		int srcpixelb;
		
		int lumidiff;
		int srcdiffr;
		int srcdiffg;
		int srcdiffb;
		
		int alpha;
		int invalpha;
		int destlength = dest.length;
		
		for (int i = 0; i < destlength; i++)
		{
			destpixel = dest[i];
			int srcoffs = (i + offset) % destlength;
			if (srcoffs < 0) srcoffs += destlength;
			
			srcpixel = draw[srcoffs];
			
			destpixelr = ((destpixel >> 16) & 0xff);
			destpixelg = ((destpixel >>  8) & 0xff);
			destpixelb = ((destpixel      ) & 0xff);
			destlumi = (destpixelr + destpixelg + destpixelb)/3;
			
			srcpixelr = ((srcpixel >> 16) & 0xff);
			srcpixelg = ((srcpixel >>  8) & 0xff);
			srcpixelb = ((srcpixel      ) & 0xff);
			
			srclumi =  (srcpixelr + srcpixelg + srcpixelb)/3;
			
			lumidiff = (srclumi < destlumi) ? destlumi - srclumi : srclumi - destlumi;
			
			//mix by difference in lumi
			
			srcpixelr = ((lumidiff * srcpixelr) >> 8) + (((0xff - lumidiff) * destpixelr) >> 8);
			srcpixelg = ((lumidiff * srcpixelg) >> 8) + (((0xff - lumidiff) * destpixelg) >> 8);
			srcpixelb = ((lumidiff * srcpixelb) >> 8) + (((0xff - lumidiff) * destpixelb) >> 8);
			
			alpha = ((srcpixel >> 24) & 0x000000ff);
			invalpha = 0xff - alpha;
			
			redbit   = ((srcpixelr * alpha) >> 8) + ((destpixelr * invalpha) >> 8);
			greenbit = ((srcpixelg * alpha) >> 8) + ((destpixelg * invalpha) >> 8);
			bluebit  = ((srcpixelb * alpha) >> 8) + ((destpixelb * invalpha) >> 8);
			
			dest[i] = ((0xff << 24) | (redbit << 16) | (greenbit << 8) | bluebit);	
		}
	}
	
	//this method assumes the three images are the same size
	public final static void drawWithOffsetAlpha(int[] dest, int[] drawbg, int[] drawfg, int[] lumialpha, int alphaoffset)
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
	
	public final static void drawPixelWithAlpha(int x, int y, int[] dest, int width, int color)
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
	
	public final static void fill(int[] dest, int color)
	{for (int i = 0; i <dest.length ; i++) {dest[i] = color;}	}
	
	public final static void fillRect(int[] dest, int destwidth, int destheight, int x, int y, int w, int h, int color, boolean alphablend)
	{
		int rasterstartoffset = 0;
		int rasterlength = w;
		
		int firstraster = 0;
		int maxraster = h;
		
		//if a portion of the img to draw is _inside the destination surface
		if ( (x < destwidth) & (y < destheight) &
		     (x + w > 0) & (y + h  > 0) )
		{
			if (x < 0) { rasterstartoffset = -x; rasterlength = w - rasterstartoffset; x = 0; }
			if (y < 0) { firstraster = -y; y = 0; }
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
	
	//make a tileRotated version of the input image, as shown.
	public final static void makeRotated(int[] src, int[] dest, int xdim, int ydim, 
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
	                   
	public final static void makeRotatedbyArrayCopy(int[] src, int[] dest, int xdim, int ydim, 
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
			rotoffs = src.length + (rotoffs % src.length);
			System.arraycopy(src, rotoffs, dest, 0,                    src.length - rotoffs);
			System.arraycopy(src, 0,       dest, src.length - rotoffs, rotoffs);
		}
	}
	
	public static void makeSparkleMap(float[] map, int width, int height)
	{
		//get a float per degree for mapping the radial sparkles
		final int randlength = 360;
		float randsparkles[] = new float[randlength];
		int m = 0;
		while (m < randlength)
		{	
			if (randex == null) {randex = new Random();}
			//fill the array with random length blocks of gaussrandom value	
			int blocklength = (int) ((Math.abs(randex.nextFloat())) * 15.0);
			float blockval = (float) randex.nextGaussian();
			int j;
			for (j = m; (j < m + blocklength) && (j < randlength); j++)
			{ randsparkles[j] = blockval; }
			m = j;
		}
		
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				int address = j + i * width;
				
				//get deltas relative to center of map
				double dx = (double) ((width/2) - j);
				double dy = (double) ((height/2) - i);
				
				//gives us a num between -pi and pi
				double theta = Math.atan2(dy,dx);
				//int sparklepos = (int) Math.toDegrees(theta);
				int sparklepos = (int) ( ( (theta + Math.PI)/(2.0 * Math.PI) ) * 360.0);
				sparklepos %= randlength;
				if (sparklepos < 0) sparklepos += randlength;
				
				double len = Math.sqrt(dx*dx + dy*dy);
				if (len <= 0) len = 0.00001;
				len /= 128.0; //should make len roughly between 0.0 and 2.0.
				len = (1.0/len) * 4.0; //will make len really big in the center with inverse falloff
				
				map[address] = (float) len;// * randsparkles[sparklepos];
			}
		}
	}
	
	public static void renderFalloffMap(float[] map, int width, int height,float adjuster)
	{	
		if (map.length == width*height)
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				int address = j + i * width;
				//get deltas relative to center of map
				double dx = (double) ((width/2) - j);
				double dy = (double) ((height/2) - i);
				//gives us a num between -pi and pi
				//double theta = Math.atan2(dy,dx);
				double len = Math.sqrt(dx*dx + dy*dy);
				if (len <= 0) len = 0.00001;
				len /= 128.0; //should make len roughly between 0.0 and 2.0.
				len = (1.0/len) * 4.0; //will make len really big in the center with inverse falloff
				map[address] = (float) len * adjuster;
			}
		}
	}
	
	public static void renderFalloffMap(int[] map, int width, int height, float adjust)
	{	
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				int address = j + i * width;
				//get deltas relative to center of map
				double dx = (double) ((width/2) - j);
				double dy = (double) ((height/2) - i);
				//gives us a num between -pi and pi
				//double theta = Math.atan2(dy,dx);
				double len = Math.sqrt(dx*dx + dy*dy);
				//len /= (width+height)/4.0; //128.0; 
				len /= (width+height)/4; //should make len between 0.0 and 1.0.
				int intlen = 0;
				if (len > 0.1) {
					len = (1.0/len); //will make len really big in the center with inverse falloff
					len -= 2.0; //so its definately zero at the edges
					len *= adjust;
					intlen = (int) len;
					intlen = (intlen < 0) ? 0 : (intlen >= 0xff) ? 0xfe : intlen;
				} else intlen = 0xfe;
				//len *= adjust;
				//-128.0
				map[address] = intlen;
			}
		}
	}
	
	public static void renderLinearFalloffMap(int[] map, int width, int height, float adjust)
	{	
		double smallrad = (height <= width) ? height/2.0 : width/2.0;
		Math.sqrt( (height*height)/4 + (width*width)/4 );
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				int address = j + i * width;
				//get deltas relative to center of map
				double dx = (double) ((width/2) - j);
				double dy = (double) ((height/2) - i);
				
				//this gives us values for dx and dy in [-dim/w .. dim/2)
				
				double norlen = (smallrad - Math.sqrt(dx*dx + dy*dy))/smallrad;
				
				//norlen should now have a value of 1 in the center of the map, 
				//and cross zero at the smallest radius of the circle that fits inside the map
				
				norlen = (norlen < 0) ? 0 : norlen;
				norlen *= adjust;
				
				//System.out.println("i: " + i + " j: " + j + " norlen: " + norlen);
				//len -= 2.0f;
				int intlen = (int) norlen;
				intlen = (intlen < 0) ? 0 : (intlen > 0xff) ? 0xff : intlen;
				//-128.0
				map[address] = intlen;
			}
		}
	}
	
	
	public static void renderLinearFalloffMapAt(int[] map, int width, int height, float adjust, float magnitude, float slope, float power, float xloc, float yloc, int pixmapw, int pixmaph)
	{	
		//double smallerdim = (height <= width) ? height/2.0 : width/2.0;
		
		//Math.sqrt( (height*height)/4 + (width*width)/4 );
		int xloci = (int) xloc;
		int yloci = (int) yloc;
		
		//no loops over pixels outside image data.. yet
		// 
		for (int i = Math.max(0,yloci - pixmaph/2); ((i < yloci + pixmaph/2) && (i < height)); i++)
		{
			for (int j = Math.max(0,xloci - pixmapw/2); ((j < yloci + pixmapw/2) && (j < width)); j++)
			{
				int address = j + i * width;
				//get deltas relative to center of map
				double dx = (double) (xloc - j);
				double dy = (double) (yloc - i);
				
				//this gives us values for dx and dy in [-dim/w .. dim/2)
				
				double norlen = Math.max(0 , Math.pow( Math.sqrt(dx*dx + dy*dy) * -slope + magnitude, power) );
				
				//norlen should now have a value of pow(magnitude) in the center of the map, 
				//and cross zero at the smallest radius of the circle that fits inside the map
				
				//norlen = (norlen < 0) ? 0 : norlen;
				norlen *= adjust;
				
				//System.out.println("i: " + i + " j: " + j + " norlen: " + norlen);
				//len -= 2.0f;
				int intlen = (int) norlen;
				intlen = (intlen < 0) ? 0 : (intlen > 0xff) ? 0xff : intlen;
				//-128.0
				map[address] = intlen;
			}
		}
	}
	
	//public static void renderRadialMap(float[] map, int width, int height,float radius, float fringe)
	
	//if pixel is true the map is created as the alpha channel in a packed pixel array
	public static void makeSparkleMap(int[] map, int width, int height, float mulfactor, 
	boolean radial, int blockleng, double randscale, boolean pixel)
	{
		//get a float per degree for mapping the radial sparkles
		final int randlength = 360;
		float randsparkles[] = new float[randlength];
		
		if (radial)
		{
			if (randex == null) {randex = new Random();}
			//randsparkles = 
			int m = 0;
			while (m < randlength)
			{	
				//fill the array with random length blocks of gaussrandom value	
				int blocklength = (int) ((Math.abs(randex.nextFloat())) * (float) blockleng);
				float blockval = (float) (randex.nextGaussian() * randscale);
				int j;
				for (j = m; (j < m + blocklength) && (j < randlength); j++)
				{ randsparkles[j] = blockval; }
				m = j;
			}
		}
		
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				int address = j + i * (width);
				double dx = (double) (width/2 - j);
				double dy = (double) (height/2 - i);
				double len = Math.sqrt(dx*dx + dy*dy);
				len /= 128.0; //should make len roughly between 0.0 and 2.0.
				//avoid div by zero?
				//if (len == 0.0) len = 0.0000001;
				len = (1.0/len); //will make len really big in the center with inverse falloff
				
				len *= mulfactor;
				
				if (radial)
				{
					//gives us a num between -pi and pi
					double theta = Math.atan2(dy,dx);		
					//int sparklepos = (int) Math.toDegrees(theta);
					int sparklepos = (int) ( ( (theta + Math.PI)/(2.0 * Math.PI) ) * 360.0);
					sparklepos %= randlength;
					if (sparklepos < 0) sparklepos += randsparkles.length;
					len *= randsparkles[sparklepos];
				}
				if (pixel)
				{
					int togo = (int) (len * 256.0);
					if (togo>256) len = 256; if (len<0) len=0;
					map[address] = ((togo<<24) | (map[address] & 0x00ffffff));
				} else {
					int writeme = (int) (len * 256.0);
					map[address] = writeme;
				}
			}
		}
		//fix zero length pixel
		map[width/2 + width * (height/2)] = 100000;
	}
	
	public final static void shearMapped(int[] shearlevels, int[] shearable, int[] sheardest, int rasterlength, boolean horizontal)
	{
		//shears an input image using the provided shear map [signed integers]
		//it assumes negative shearvalues slide the pixels to the left.
		int vrasterlength = shearable.length / rasterlength; //this is obviously no good when rasterlength = 0.
		
		//equals would be safer
		//if ( (shearlevels.length * rasterlength) = shearable.length )
		if ( (shearlevels.length * rasterlength) < shearable.length )
		if (horizontal)
		{
			int rasterstart = 0;
			for (int i = 0; i < shearlevels.length; i++)
			{
				int theshear = -shearlevels[i]; //negative slides left, source of pixel is higher address in array
				int adjusted;
				for (int j = 0; j < rasterlength; j++)
				{
					adjusted = (j + theshear) % rasterlength;
					if (adjusted < 0) adjusted = adjusted + (rasterlength - 1);
					sheardest[rasterstart + j] = shearable[rasterstart + adjusted];
				}
				rasterstart += rasterlength;
			}
		} else {
			int vrasterstart = 0;
			for (int i = 0; i < shearlevels.length; i++)
			{
				int theshear = -shearlevels[i];
				int addresshear = theshear * rasterlength;
				int adjusted;
				int vpos = 0;
				for (int j = 0; j < vrasterlength; j++)
				{
					adjusted = (vpos + addresshear) % shearable.length;
					if (adjusted < 0) adjusted = adjusted + (shearable.length - 1);
					sheardest[vpos + i] = shearable[adjusted];
					vpos += rasterlength;
				}
				vrasterstart++;
			}
			//shear vertically
		}
		
	}
	
	//Allocate an int[] array and fill it with pixels from a given location in the src image.
	public final static int[] getSubMap(int src[], int srcwidth, int srcheight, 
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
	
	//ijok
	public int[] getHistogram(int pixels[], int w, int h) 
	{
		int c, r, g, b, v;
		int[] histogram = new int[256];
		for (int y = 0; y < h; y++) {
			int i = y * w;
			for (int x = 0; x < w; x++) {
				c = pixels[i++];
				r = (c&0xff0000)>>16;
				g = (c&0xff00)>>8;
				b = c&0xff;
				v = (int)(r*0.30 + g*0.59 + b*0.11); //weighted luminance... hmmm
				histogram[v]++;
			}
		}
		return histogram;
	}
	
	public final static void getSubMap(int src[], int srcwidth, int srcheight, 
	int[] dest, int xloc, int yloc, int subw, int subh)
	{
		//dest should be subw*subh values.
		
		int startloc = xloc + yloc * srcwidth;
		int destloc = 0;
		
		for (int i = 0; i < subw; i++)
		{
			for (int j = 0; j < subh; j++)
			{
				dest[destloc] = src[startloc];
				startloc = (startloc+1) % src.length;
				destloc = (destloc+1) % dest.length;
			}
			startloc = (startloc + (srcwidth - subw)) % src.length;
		}
	}
	
	public final static void shake(int[] pixels, int[] tmppixels, int height, int width, int border, int bordercolor, int shakex, int shakey, Random r)
	{
		int shakexpos = r.nextInt() % shakex;
		shakexpos = (shakexpos < 0) ? -shakexpos : shakexpos;
		int shakeypos = r.nextInt() % shakey;
		shakeypos = (shakeypos < 0) ? -shakeypos : shakeypos;
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
	
	public final static void blur(int[] what, int[] dest, int width, int height, boolean bluralpha, int blend)
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
	public final static void grabPixelRectangle(Image img, int[] dest, int xgrab, int ygrab, int grabwidth, int grabheight, MemoryImageSource memsrc)
	{
		if( img == null ) return;
		pixelgrabdest = dest;
		memimgsrc = memsrc;
		
		int w = img.getWidth(new MiniPixelTools());
		int h = img.getHeight(new MiniPixelTools());
		
		if ((w>0) & (h>0))
		{
			PixelGrabber pixelmaker = new PixelGrabber(img, xgrab, ygrab, grabwidth, grabheight, dest, 0, grabwidth );
			// new PixelGrabber(img, xgrab, ygrab, grabwidth, grabheight, dest, 0, grabwidth );
			try
			{pixelmaker.grabPixels();} 
			catch (InterruptedException e) 
			{
			System.err.println("Pixel rectangle grab failed or was interrupted.");
	    		return;
	    	}
	    	
	    	if ((pixelmaker.getStatus() & ImageObserver.ABORT) != 0) 
	    	{
	    		System.err.println("Failure during rectangle pixel grab of image, load aborted.");
	    		return;
			}
		} else {
			
		}
		memsrc.newPixels(0,0,grabwidth,grabheight);
	}
	
	//Grab pixels from a java Image object into an int[] array
	public final static void grabPixels(Image img, int[] dest, MemoryImageSource memsrc)
	{
		if( img == null ) return;
		pixelgrabdest = dest;
		memimgsrc = memsrc;
		
		int w = img.getWidth(new MiniPixelTools());
		int h = img.getHeight(new MiniPixelTools());
		
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
			
		}
		memsrc.newPixels(0,0,w,h);
	}
	
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
	{
		if ((infoflags & ImageObserver.ALLBITS) != 0) System.out.println("ALLBITS flag registered in ImageUpdate");
		
		if ( ((infoflags & ImageObserver.WIDTH) != 0) && ((infoflags & ImageObserver.HEIGHT) != 0) )
		{
			grabPixels(img,pixelgrabdest,memimgsrc);
			return false; //stop notifying
		} else {
			if ((infoflags & ImageObserver.ABORT) != 0) System.err.println("Error, could not grab pixels, Image load/production was aborted.");
			return true; //keep waiting and keep notifying
		}
	}
	
	public static void grabDefaultMapFromPixels(int pixels[], int map[], float highdefmap[], int w, int h)
	{
		int grabber = 0;
		int pos = 0;
		//vague sanity check:
		int len = w*h;
		if ( (pixels.length == len) && (highdefmap.length == len) && (map.length == len) )
		for (int i = 0; i < h; i++)
		{
			for (int j = 0; j < w; j++)
			{
				pos = j + i * w;
				grabber = pixels[pos];
				float defloat = getDefaultF(grabber);
				int k = (int) defloat;
				map[pos] = k; // > 0) ? ((k < 0xff) ? k : 0xff) : 0;
				highdefmap[pos] = defloat;
			}
		}
	}
}
