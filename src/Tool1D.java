import java.awt.*;
import java.applet.*;
//import MiniPixelTools;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.*;
//import EditWidget;


class Tool1D implements MouseListener, MouseMotionListener, KeyListener
 {
 	/* this oldskewl enum is obfuscated for the moment anyway so I wont bother converting it to a 'proper' enum just yet ... */
 	
	public static final int DRAW_WAVE         =  0;
	public static final int DRAW_RGBGRAD      =  1;
	public static final int DRAW_HSVGRAD      =  2; //*
	public static final int DRAW_SEX          =  3;
	public static final int DRAW_SEXX         =  4;
	public static final int DRAW_DEFAULTSOFT  =  5;
	public static final int DRAW_DEFAULTDITHER=  6;
	public static final int DRAW_DEFAULTSOFTTWO= 7;
	public static final int DRAW_RGBBANDS     =  8;
	public static final int DRAW_RGBLINES     =  9;
	public static final int DRAW_RGBRATIOS    =  10;
	public static final int DRAW_RGBSOFTLINES =  11;
	public static final int DRAW_HSVLINES     =  12;
	public static final int DRAW_HSVSOFTLINES =  13;
	public static final int DRAW_HSVRATIOS    =  14;
	public static final int DRAW_PIXELDEFAULT =  15;
	public static final int DRAW_OVALS        =  16;
	public static final int NUM_DRAWMODES     =  17;
	
	public static final String[] DRAWMODE_NAMES = 
	                               {"Wave",
									"RGB Gradient",
									"HSV Gradient",
									"Difference Wave",
									"Bound Wave",
									"Softened Default",
									"Dithered Default",
									"Softened Default 2",
									"RGB Bands",
									"RGB Lines",
									"RGB Ratios",
									"RGB Soft Lines",
									"HSV Lines",
									"HSV Soft Lines",
									"HSV Ratios",
									"Fast PixelDefault",
									"Default Radius Ovals"};
	//public final harvestmethods
	//line, circle, square, L-System (nonbranching is simplest)
	
	//spacetiling subpixel sampling and interpolation
	
	//these points should be double precision eventually
	Point src;
	Point dest;
	Point offset;
	public Dimension tooldrawspace;
	public Dimension toolsamplespace;
	
	public Point tooldrawposition;
	public Dimension tooldrawsize;
	
	public boolean toolalwaysharvests = true;
	public boolean tooleditmode = false;
	public float lastdrawindex = -100000001;
	public float lastdrawvalue = 1.0f;
	
	public double toolstrength = 1.0;
	public float softlevel = 0.06f;
	public int drawalpha = 0xff;
	int buffer[];
	int bufferlength;
	int harvestlength;
	
	float verticalscale = 2.0f;
	
	boolean drawbackground = false;
	
	float drawresolution = 512.0f;
	
	int defaultdraw = DRAW_DEFAULTSOFT;
	
	float toolstrokewidth = 3;
	int toolstrokealpha = 0xff;
	boolean isVisible = false;
	boolean adaptivebuffer = true;
	
	float xreference = 0;
	float yreference = 0;
	
	float referenceOffset = 0;
	int referenceRotation = 0;
	
	float ruleMultiplier = 255.0f;
	boolean normalizeRuleRange = false;
	float ruleOffset = 0;
	int ruleColorOffset = 0;
	int ruleRotation = 0;
	int ruleColorRotation = 0;
	public int[] harvest;
	public float[] harvestf;
	private boolean marvestripe = false;
	private int [] lastmarvest;
	private boolean marvestripefloat = false;
	private float [] lastfloatmarvest;
	
	public boolean ripe = false;
	public boolean fullstatus = false;
	Vector<EditWidget> childWidges;
	Tiler context;
	Random randex;
	int mousebutton = 0;
	
	static Color normalcolor = Color.darkGray;
	static Color highlightcolor = Color.lightGray;
	static Color selectedcolor = Color.white;
	
	//this tool assumes that the buffer is an int[spacewidth*spaceheight].
	Tool1D(int harvlength, int samplespacewidth, int samplespaceheight, int[] elements, Tiler contex, int drawspacewidth, int drawspaceheight)
	{
		tooldrawspace = new Dimension(drawspacewidth, drawspaceheight);
		toolsamplespace = new Dimension(samplespacewidth, samplespaceheight);
		
		tooldrawposition = new Point(drawspacewidth/2,drawspaceheight/2);
		tooldrawsize = new Dimension(drawspacewidth,drawspaceheight);
	
		context = contex;
		contex.addMouseListener(this);
		contex.addMouseMotionListener(this);
		contex.addKeyListener(this);
		
		src = new Point(samplespacewidth/2,samplespaceheight/2);
		dest = new Point(100,100);
		//verticalscale = 1;
		offset = new Point(0,0);
		
		randex = new Random();
		buffer = elements;
		harvestlength = harvlength;
		harvest = new int[harvestlength];
		harvestf = new float[harvestlength];
		//context.showStatus("Created harvestTool with " + harvlength + " sample buffer, in a space [" + spacewidth + "x" + spaceheight + "]");
	}
	
	public void doHarvest()
	{
		//
		//detect repetition in value and adjust
		
		//use src and dest to grab values from buffer into harvested
		//if (!ripe)
		//{	
		if (harvest.length != harvestlength) {harvest = new int[harvestlength];}
		for (int i = 0; i < harvestlength; i++)
		{
			float ratio = (float)i/(float)harvestlength;
			float[] tweenpoint = new float[2];
			
			tweenpoint[0] = ratio*src.x + (1.0f-ratio)*dest.x;
			tweenpoint[1] = ratio*src.y + (1.0f-ratio)*dest.y;
			
			//Point tweenpoint = new Point( (int) (  ),
			//                              (int) (  ) );
			                              
			//lets assume everything is positive for the moment...
			float xbit = ( tweenpoint[0] + offset.x ) % toolsamplespace.width;
			float ybit = ( tweenpoint[1] + offset.y ) % toolsamplespace.height;
			
			//just in case line was negative inn any way..
			xbit = (xbit >= 0) ? xbit : xbit + toolsamplespace.width;
			ybit = (ybit >= 0) ? ybit : ybit + toolsamplespace.height;
			
			harvest[i] = MiniPixelTools.getInterpolatedPixel( xbit, ybit, buffer, toolsamplespace.width, toolsamplespace.height);
			
			//int pixelselector = xbit+ybit*toolsamplespace.width;
			//pixelselector %= buffer.length;
			//if (pixelselector < 0) pixelselector += buffer.length;
			//harvest[i] = buffer[pixelselector];
			
			harvestf[i] = MiniPixelTools.getDefaultF( harvest[i] );
		}
		ripe = true;
		marvestripe = false;
		marvestripefloat = false;
		//} else {
		//	}
	}
	
	public void setToolStrokeWidth(float towhat)
	{ toolstrokewidth = towhat; }
	
	public void setToolIsVisible( boolean towhat )
	{ isVisible = towhat; }
	
	public void setVerticalScale(float towhat)
	{verticalscale = towhat;}
	
	public void setToolAlwaysHarvests(boolean towhat)
	{toolalwaysharvests = towhat;}
	
	public void setToolEditMode(boolean towhat)
	{ tooleditmode = towhat; }
	
	public void resizeHarvest(int howlong)
	{
		//context.showStatus("Tool resizing");
		ripe = false;
		harvestlength = howlong;
		doHarvest();
		//context.showStatus("Tool resized to " + howlong);
	}
	
	public void setLine(int[] towhat)
	{
		ripe = false;
		if (towhat.length >= 4) //no exceptions thanks
		{
			src.move(towhat[0],towhat[1]);
			dest.move(towhat[2],towhat[3]);
		}
	}	
	
	public void setDrawBackground( boolean towhat )
	{
		drawbackground = towhat;
	}
	
	public void setDrawResolution( float towhat )
	{
		drawresolution = Math.max( 1.0f, towhat );
	}
	
	public void movesrc(int x,int y)
	{
		ripe = false;
		src.move(x,y);
		//context.showStatus("Tool source set to " + x + "," + y);
	}
	
	public void movedest(int x,int y)
	{
		ripe = false;
		dest.move(x,y);
		//context.showStatus("Tool destination set to " + x + "," + y);
	}
	
	public void setOffset(int x,int y)
	{
		ripe = false;
		offset.move(x,y);
	}
	
	public int getColorInHarvest( int index )
	{
		//this is cached so it should only take a while one time after the array has become unripe
		int marvest[] = getColorHarvest();
		int ix =  index % marvest.length;
		if ( ix < 0 ) index += marvest.length;
		return marvest[ ix ];
	}
	
	public float getFloatInHarvest( int index )
	{
		//this is cached so it should only take a while one time after the array has become unripe
		float marvestf[] = getFloatHarvest();
		int ix =  index % marvestf.length;
		if ( ix < 0 ) index += marvestf.length;
		return marvestf[ ix ];
	}
	
	public int[] getColorHarvest()
	{
		if (marvestripe)
		{ 
			return lastmarvest;
		} else {
			int marvest[] = new int[harvest.length];
			
			for (int q = 0; q < marvest.length; q++ )
			{ 
				int l = (q + ruleColorRotation) % marvest.length;
				if (l < 0) l += marvest.length;
				int rulecolor = harvest[ l ]; 
				int a = rulecolor & 0xff000000;
				int r = ((rulecolor >> 16) & 0xff) + ruleColorOffset;
				int g = ((rulecolor >>  8) & 0xff) + ruleColorOffset;
				int b = ((rulecolor      ) & 0xff) + ruleColorOffset;
				r = Math.max( 0, Math.min( 0xff, r ) );
				g = Math.max( 0, Math.min( 0xff, g ) );
				b = Math.max( 0, Math.min( 0xff, b ) );
				marvest[q] = (a) + 
							( ( r  ) << 16 ) +
							( ( g ) <<  8 ) +
							( ( b ) );;
			}
			lastmarvest = marvest;
			marvestripe = true;
			return marvest;
		}
	}
	
	public float[] getFloatHarvest()
	{
		if (marvestripefloat)
		{
			return lastfloatmarvest;
		} else {
			
			float marvest[] = new float[harvestf.length];
			
			for (int q = 0; q < marvest.length; q++ )
			{ 
				int l = (q + ruleRotation) % marvest.length;
				if (l < 0) l += marvest.length;
				marvest[q] = harvestf[ l ];
			}
			
			float rulemin = 1000000000;
			float rulemax = -1000000000;
			
			if (normalizeRuleRange)
			{
				for (int q = 0; q < marvest.length; q++ )
				{
					if (marvest[q] > rulemax) rulemax = marvest[q];
					if (marvest[q] < rulemin) rulemin = marvest[q];
				}
				if ( rulemax != rulemin )
				{
					//hmm keepin' the rule all integer on yau ass here might not be the finest plan ever..
					for (int q = 0; q < marvest.length; q++ )
					{
						marvest[q] =  (marvest[q] - rulemin) / (rulemax - rulemin);
					}
				}
			}
			
			for (int q = 0; q < marvest.length; q++ )
			{
				marvest[q] = ( marvest[ q ] + ruleOffset ) * ruleMultiplier;
			}
			
			lastfloatmarvest = marvest;
			marvestripefloat = true;
			return marvest;
		}
	}
	
	public int getDefaultDrawMode()
	{ return defaultdraw; }
	
	public void setDefaultDrawMode(int mode)
	{ 
		defaultdraw = mode;
		defaultdraw %= NUM_DRAWMODES; //wrap overfloo
		if (defaultdraw < 0) defaultdraw += NUM_DRAWMODES;
		//context.showStatus("Draw mode " + defaultdraw + " selected.");
	}
	
	public void setSoftLevel(float to)
	{
		softlevel = (to < 0) ? 0 : (to > 1) ? 1 : to;
	}
	
	public void setToolDrawAlpha( int to )
	{
		drawalpha = to & 0xff;
	}
	
	
	public void drawTool( Graphics2D g )
	{
		//
		
		//new draw method:
		// fit the widget always within it's drawArea
		// pos-dim/2 to pos+dim/2 in each dimension
		// use a detail level variable to set the number of poly's used inside the tool drawspace (max at the width of the tool in pixels)
		// separate zoom level and panning control for finer rule editing
		//
		// rule interpolation - have an interpolation mode (filter) that can be selected
		// 
		// 	nearest cell, 
		// 	linear,
		// 	bilinear
		// 	spline
		// 	sinc
		// 	dither
		// 	"lookup frequency weighted" dither,
		//
		// get a proper filterwidth and filterscale going on lookups
		
		//
		// more RGB drawmodes 
		//	- draw all 3 channels additively on black then comp over - should get the actual color in the intersection region
		//	- draw max(rgb) in the whole color and add a little indicator line at the r,g,b levels
		//	- figure out how to draw gouraud polygons and draw 'em with appropriate values at the verts
		
		if (isVisible)
		{
			//push graphics state first.. then pop it
			
			Rectangle clipbounds = g.getClipBounds();
			//g.setColor(Color.white);
			//g.drawLine(src.x,src.y,dest.x,dest.y);
			if ( ( (!ripe) & ( toolalwaysharvests | context.draggingTilePosition) ) & (!tooleditmode) ) doHarvest();
			
			//g.setColor(Color.black);
			//g.drawLine(0,height/2,width,height/2);
			//g.drawLine(width/2,0,width/2,height);
			
			g.setPaintMode();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            		
			//draw rectangle
			g.setColor( normalcolor );
			
			AffineTransform currentAffineTransform = g.getTransform();
			g.setTransform( AffineTransform.getTranslateInstance( tooldrawposition.x, tooldrawposition.y ) );
			
			int bgHeightFactor = (int) ( verticalscale * tooldrawsize.height );
			if (drawbackground) g.fill3DRect( -tooldrawsize.width/2, -bgHeightFactor/2, tooldrawsize.width, bgHeightFactor ,true);
			
			//if (drawbackground) g.fill3DRect( -tooldrawsize.width/2, -tooldrawsize.height/2, tooldrawsize.width, tooldrawsize.height ,true);

			//g.fill3DRect( tooldrawposition.x - tooldrawsize.width/2, tooldrawposition.y - tooldrawsize.height/2, tooldrawsize.width, tooldrawsize.height,true);
			
			int actualslices = (int) Math.min( Math.abs( drawresolution ), tooldrawsize.width );
			
			float marvest[] = getFloatHarvest();
			int karvest[] = getColorHarvest();
			
			for (int p = 0; p < actualslices; p++)
            {
				float slicewidth = Math.max(1.5f, (float)tooldrawsize.width/(float)actualslices );
				
				float fraction = (float)p / (float)actualslices;
				float nextfraction = (float)(p + 1.0f) / (float)actualslices;
				
				float slicepos =(float)tooldrawsize.width * (fraction - 0.5f);
				
				float harvestselector = fraction * (float)harvest.length;
				int selector = Math.abs(Math.round(harvestselector));
				selector %= harvest.length;
				int harvalue = karvest[selector];
				float defaultval = Math.abs( (marvest[selector]/255.0f) * tooldrawsize.height/2 * verticalscale);
				
				float nextharvestselector = nextfraction * (float)harvest.length;
				int nextselector = Math.abs(Math.round(nextharvestselector));
				nextselector %= harvest.length;
				
				float nextdefaultval = Math.abs( (marvest[nextselector]/255.0f) * tooldrawsize.height/2 * verticalscale);
				
				if ( ( marvest[selector ] >= 0 ) | ( marvest[nextselector] >= 0) )
				{
					g.setColor( new Color(  ( (harvalue & 0x00ff0000) >> 16)/255.0f,  ( ( harvalue & 0x0000ff00) >> 8)/255.0f, ( ( harvalue & 0x000000ff ) )/255.0f, Math.min(drawalpha,255)/255.0f) );
				} else {
					g.setColor( new Color(  1.0f - ( (harvalue & 0x00ff0000) >> 16)/255.0f, 1.0f - ( ( harvalue & 0x0000ff00) >> 8)/255.0f, 1.0f - ( ( harvalue & 0x000000ff ) )/255.0f, Math.min(drawalpha,255)/255.0f) );
				}
				
				//(harvalue & 0x00ffffff) + (drawalpha << 24) ));
				
				int xs[] = new int[4];
				int ys[] = new int[4];
				
				//need to clip all this stuff in the viewing frustrum too.. else it seems to get pretty slow..
				
				xs[0] = Math.round( slicepos );
				ys[0] = (int)Math.min( (tooldrawsize.height/2 - 1), Math.round( defaultval * verticalscale ) );
				
				xs[1] = Math.round( slicepos + slicewidth );
				ys[1] = (int)Math.min( (tooldrawsize.height/2 - 1), Math.round( nextdefaultval * verticalscale ) );
				
				xs[2] = Math.round( slicepos + slicewidth );
				ys[2] = (int)Math.max( (-tooldrawsize.height/2 + 1), Math.round( -nextdefaultval * verticalscale ) );
				
				xs[3] = Math.round( slicepos );
				ys[3] = (int)Math.max( (-tooldrawsize.height/2 + 1), Math.round( -defaultval * verticalscale ) );
				
				g.fillPolygon(xs,ys,4);
				
				//have an EQ-style mode that draws bars of a chosen width, centered around the middle of each slice
				//int sliceposition = (int) ( (float)tooldrawsize.width * ( ( (float)p + 0.5f ) / (float)actualslices ); 
				//have a polar mode also - for circular rules
				
				//float harvestselector = ((float)i/(float)width)*(float)harvest.length;
				//int selector = Math.abs(Math.round(harvestselector));
				//selector %= harvest.length;
				//if (selector < 0) selector += harvest.length;					
				//int harvalue = harvest[selector];
				
				//int defaultval = (int) ( (harvestf[selector]/255.0f) * verticalscale);
				//g.drawLine(i,(height/2 + harvalue)-10,i,(height/2+harvalue)+10);
				//g.drawLine(i,height/2-10,i,height/2+10);
				//g.setColor(new Color(harvalue & 0x00ffffff | (alpha << 24) ));
				//g.drawLine(i,height/2 - defaultval,i,height/2 + defaultval);
            }
            		
            g.setTransform( currentAffineTransform );
            	
		} // else  - wasn't visible.. don't draw..
	}
	
	void fillRuleArea( int min, int max, float eydelta, float lastdrawvalue )
	{
		if ( Math.abs( min - max) < ( harvestf.length/2 ) ) //fuck.. just refuse to draw it when its too big then!
		{
			for (int b = min; b <= max; b++)
			{
				if (max != min)
				{
					float cin = (float)( b - min  + 1.0f )  / (float) ( max - min );
					harvestf[b] = cin * lastdrawvalue + (1.0f - cin) * eydelta;
					//System.out.println("min = " + min + " max = " + max + " lastdrawvalue: " + lastdrawvalue + " value: " + harvestf[b]);
				} else {
					//System.out.println("SAME! min = " + min + " max = " + max + " lastdrawvalue: " + lastdrawvalue + " value: " + harvestf[b]);
					harvestf[b] = eydelta;
				}
			}
			/*{
				
			} else {
				harvestf[b] = (1.0f - cin) * lastdrawvalue + cin * eydelta;
			}*/
			marvestripefloat =false;
		}
	}
	
	//should buffer this.. dammit
	// box filter 5 cells centered on the self-cell
	void softenRuleArea( int min, int max )
	{
		for (int b = min; b <= max; b++)
		{
			float cin = (float)( b - min  + 1.0f )  / (float) ( max - min );
			
			cin = (cin <=0.5) ? cin * 2 : (1 - cin) * 2;
			
			int prev = (b-1) % harvestlength;
			int prevprev = (b-2) % harvestlength;
			int next = (b+1) % harvestlength;
			int nextnext = (b+2) % harvestlength;
			
			if (prev < 0) prev += harvestlength;
			if (prevprev < 0) prevprev += harvestlength;
			if (next < 0) next += harvestlength;
			if (nextnext < 0) nextnext += harvestlength;
			
			float ruleaverage = ( harvestf[prevprev] + harvestf[prev] + harvestf[b] + harvestf[next] + harvestf[nextnext] ) / 5.0f;
			harvestf[b] = ruleaverage;
			//cin * ruleaverage + (1.0f - cin) * harvestf[b];
			
		}
		marvestripefloat = false;
	}
	
	public void rotateAndOffsetRuleWithCoordinates( float ex, float ey, boolean doRotation, boolean doOffset, boolean colorOrFloat )
	{
		if ( doRotation )
		{
			float exdelta = (xreference - ex); //- ( tooldrawposition.x - tooldrawsize.width/2);
			exdelta /= (float) tooldrawsize.width;
			exdelta *= (harvestlength - 1);
			if (colorOrFloat)
			{
				ruleColorRotation = referenceRotation + (int) exdelta;
				marvestripe = false;
			} else {
				ruleRotation = referenceRotation + (int) exdelta;
				marvestripefloat = false;
			}
			
		}
		if ( doOffset )
		{
			float eydelta = yreference - ey; //tooldrawposition.y
			eydelta /= (float) tooldrawsize.height/2;
			eydelta /= (verticalscale * verticalscale);
			if (colorOrFloat)
			{
				ruleColorOffset = (int) ( eydelta * 64.0f );
				marvestripe = false;
			} else {
				ruleOffset = referenceOffset + eydelta;
				marvestripefloat = false;
			}
			
		}
	}
		
		
	public void drawToRuleWithCoordinates( float ex, float ey )
	{
		float exdelta = ex - ( tooldrawposition.x - tooldrawsize.width/2);
		exdelta /= (float) tooldrawsize.width;
		exdelta *= harvestlength;
		exdelta += ruleRotation;
		
		float eydelta = tooldrawposition.y - ey;
		eydelta /= (float) tooldrawsize.height/2;
		eydelta /= (verticalscale * verticalscale);
		eydelta -= ruleOffset;
		//if (ruleMultiplier != 0) eydelta /= (ruleMultiplier / 255.0f);
		
		//eydelta *=256.0f;

		int xdi = (int) exdelta % harvestlength;
		if (xdi < 0) xdi += harvestlength;
		
		int xlastdi = 0;
		
		boolean crossingzero = false;
		
		if (lastdrawindex <= -100000000) // init mode, or single mode, or whatever :D
		{
			lastdrawindex = exdelta;
			xlastdi = xdi;
			
		} else {
			
			xlastdi = (int) lastdrawindex % harvestlength;
			if (xlastdi < 0) xlastdi += harvestlength;
			
			if (  ( (int)exdelta ) / harvestlength != ( (int)lastdrawindex ) / harvestlength ) crossingzero = true;
			
			if ( ( ( exdelta > 0 ) & ( lastdrawindex < 0) ) | ( ( exdelta < 0 ) & ( lastdrawindex > 0) )  ) crossingzero = true;
			
		}
		
		//if not initialised, just draw the one value..
		
		boolean forwards = false;
		if (xdi > xlastdi) forwards = true;
		
		//System.out.println("ypos " + e.getY() + " toolpos " + tooldrawposition.y + " toolheight/2: " + (tooldrawsize.height/2) );
		//System.out.println("existing value at destination in rule is : " + harvestf[ xdi ] );
		
		//System.out.println("got);
		int min = Math.min( xdi, xlastdi );
		int max = Math.max( xdi, xlastdi );
		
		
		//this is a tad rough.. but you could expect people to be rough with this kind of tool.., :D
		if (  ( max >= harvestlength - 5 ) & ( min <= 5 ) )  crossingzero = true;	
		
		if (!crossingzero)
		{
			//System.out.println("NOT crossingzero, current value to write is: " + eydelta);
			if ( xdi != xlastdi ) // not single mode
			{
				if (!forwards) fillRuleArea( min, max, eydelta, lastdrawvalue );
				else fillRuleArea( min, max, lastdrawvalue, eydelta );
			} else {
				harvestf[ xdi ] = eydelta;
				marvestripefloat = false;
			}
		} else {
			//System.out.println("crossingzero, current value to write is: " + eydelta);
			float tweenvalue = (float)(harvestlength - max)/(float) ( (min+harvestlength) - max );
			if (!forwards)
			{
				float intervalue = ( tweenvalue * lastdrawvalue ) + (1 - tweenvalue) * eydelta;
				fillRuleArea( max, harvestlength - 1, lastdrawvalue, intervalue );
				fillRuleArea( 0, min, intervalue, eydelta );
			} else {
				float intervalue = ( tweenvalue *  lastdrawvalue) + (1 - tweenvalue) * eydelta;
				fillRuleArea( max, harvestlength - 1, eydelta, intervalue );
				fillRuleArea( 0, min, intervalue, lastdrawvalue );
			}
		}
		//(float) ( Math.random() - 0.5) * 2 * 255.0f;
		
		// get value from e.getY()
		
		//System.out.println("doing a tool draw ending at harvest index: " + xdi + " with value: " + eydelta );
		
		lastdrawindex = exdelta;
		lastdrawvalue = eydelta;
	}	
	
	
	public void softenRuleWithCoordinates( float ex, float ey )
	{
		float exdelta = ex - ( tooldrawposition.x - tooldrawsize.width/2);
		exdelta /= (float) tooldrawsize.width;
		exdelta *= harvestlength;
		exdelta += ruleRotation;
		
		float eydelta = tooldrawposition.y - ey;
		eydelta /= (float) tooldrawsize.height/2;
		eydelta /= (verticalscale * verticalscale);
		eydelta -= ruleOffset;
		//if (ruleMultiplier != 0) eydelta /= (ruleMultiplier / 255.0f);
		
		//eydelta *=256.0f;

		int xdi = (int) exdelta % harvestlength;
		if (xdi < 0) xdi += harvestlength;
		
		int xlastdi = 0;
		
		boolean crossingzero = false;
		
		if (lastdrawindex <= -100000000) // init mode, or single mode, or whatever :D
		{
			lastdrawindex = exdelta;
			xlastdi = xdi;
			
		} else {
			
			xlastdi = (int) lastdrawindex % harvestlength;
			if (xlastdi < 0) xlastdi += harvestlength;
			
			if (  ( (int)exdelta ) / harvestlength != ( (int)lastdrawindex ) / harvestlength ) crossingzero = true;
			
			if ( ( ( exdelta > 0 ) & ( lastdrawindex < 0) ) | ( ( exdelta < 0 ) & ( lastdrawindex > 0) )  ) crossingzero = true;
			
		}
		
		//if not initialised, just draw the one value..
		
		boolean forwards = false;
		if (xdi > xlastdi) forwards = true;
		
		//System.out.println("ypos " + e.getY() + " toolpos " + tooldrawposition.y + " toolheight/2: " + (tooldrawsize.height/2) );
		//System.out.println("existing value at destination in rule is : " + harvestf[ xdi ] );
		
		//System.out.println("got);
		int min = Math.min( xdi, xlastdi );
		int max = Math.max( xdi, xlastdi );
		
		
		//this is a tad rough.. but you could expect people to be rough with this kind of tool.., :D
		if (  ( max >= harvestlength - 2 ) & ( min <= 2 ) )  crossingzero = true;	
		
		if (!crossingzero)
		{
			softenRuleArea( min, max );
			softenRuleArea( min, max );
		} else {
			softenRuleArea( max, harvestlength - 1 );
			softenRuleArea( max, harvestlength - 1 );
			softenRuleArea( 0, min );
			softenRuleArea( 0, min );
		}
		//(float) ( Math.random() - 0.5) * 2 * 255.0f;
		
		// get value from e.getY()
		
		//System.out.println("doing a tool draw ending at harvest index: " + xdi + " with value: " + eydelta );
		
		lastdrawindex = exdelta;
		lastdrawvalue = eydelta;
	}
	
	public void mousePressed(MouseEvent e)
	{
		//System.out.println("mousePressed on tool");
		if (isVisible)
		{
			int mods = e.getModifiers();
			if ((mods & InputEvent.BUTTON3_MASK) != 0) mousebutton = 3;
			if ((mods & InputEvent.BUTTON2_MASK) != 0) mousebutton = 2;
			if ((mods & InputEvent.BUTTON1_MASK) != 0) mousebutton = 1;
			
			/*
			if ((mods & KeyEvent.CTRL_MASK) != 0) 
			{
				if ((mods & KeyEvent.SHIFT_MASK) != 0)  //ctrl and shift
				{
					//System.out.println("e.getX() = " + e.getX() + " e.getY() = " + e.getY() );
					tooldrawposition = new Point( e.getX(), e.getY() );
				} else  { //just control
					//System.out.println(" width = " + Math.abs( tooldrawposition.x - e.getX() ) + " height = " + Math.abs( tooldrawposition.y - e.getY() ) );
					tooldrawsize = new Dimension( 2 * Math.abs(tooldrawposition.x - e.getX() ), 2 * Math.abs( tooldrawposition.y - e.getY() ) );		
				}	
			}*/
			
			if (tooleditmode)
			{
				if ( ( mousebutton == 1 ) & ((mods & KeyEvent.SHIFT_MASK) != 0) & ((mods & KeyEvent.ALT_MASK) != 0) )
				{
					lastdrawindex = -100000001;
					// draw to tool float buffer with lmb
					// respect any scaling of the draw space that is in effect..

					// track lastdrawindex so that the inbetween can be filled in if neccesary.. lerp it up

					// smoothing with some other crazymodifier - rightmouse or summink
					// then maybe do somekinda scroll offset setting with middlemouse

					//get array index from e.getX() by interping between -width and width

					drawToRuleWithCoordinates( e.getX(), e.getY() );
				} else if ( ( mousebutton == 2) ) {

					//reset rotation
					xreference = e.getX();
					yreference = e.getY();
					referenceRotation = ruleRotation;
					referenceOffset = ruleOffset;
				} else if ( (mousebutton == 3) ) {
					xreference = e.getX();
					yreference = e.getY();
					referenceRotation = ruleColorRotation;
					referenceOffset = ruleColorOffset;
				}
			}
			//deliver(false);
			e.consume();
		}
	}

	public void mouseReleased(MouseEvent e)
	{
		//System.out.println("mouseReleased on tool");
		if (isVisible)
		{
			int mods = e.getModifiers();
			if ((mods & InputEvent.BUTTON3_MASK) != 0) mousebutton = 3;
			if ((mods & InputEvent.BUTTON2_MASK) != 0) mousebutton = 2;
			if ((mods & InputEvent.BUTTON1_MASK) != 0) mousebutton = 1;
			
			if ((mods & KeyEvent.CTRL_MASK) != 0) 
			{
				//if ((mods & KeyEvent.SHIFT_MASK) != 0)  //ctrl and shift
				//{
					//System.out.println("e.getX() = " + e.getX() + " e.getY() = " + e.getY() );
				//	tooldrawposition = new Point( e.getX(), e.getY() );
				//} else  { //just control
					//System.out.println(" width = " + Math.abs( tooldrawposition.x - e.getX() ) + " height = " + Math.abs( tooldrawposition.y - e.getY() ) );
				//	tooldrawsize = new Dimension( 2 * Math.abs(tooldrawposition.x - e.getX() ), 2 * Math.abs( tooldrawposition.y - e.getY() ) );		
				//}	
			}
			e.consume();
		}
	}
	
	public void mouseMoved(MouseEvent e)
	{
		//System.out.println("mouseMoved on tool");
		if (isVisible)
		{
			int mods = e.getModifiers();
			int mousebutton = 0;
			if ((mods & InputEvent.BUTTON3_MASK) != 0) mousebutton = 3;
			if ((mods & InputEvent.BUTTON2_MASK) != 0) mousebutton = 2;
			if ((mods & InputEvent.BUTTON1_MASK) != 0) mousebutton = 1;
			
			//if shift is down, draw to the rule on every mousemove event
			
			e.consume();
		}
	}
	
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	
	public void mouseDragged(MouseEvent e)
	{
		//System.out.println("mouseDragged on tool");
		if (isVisible)
		{
			int mods = e.getModifiers();
			int mousebutton = 0;
			if ((mods & InputEvent.BUTTON3_MASK) != 0) mousebutton = 3;
			if ((mods & InputEvent.BUTTON2_MASK) != 0) mousebutton = 2;
			if ((mods & InputEvent.BUTTON1_MASK) != 0) mousebutton = 1;
			
			boolean ctrl = ((mods & KeyEvent.CTRL_MASK) != 0);
			boolean alt = ((mods & KeyEvent.ALT_MASK) != 0);
			boolean shift = ((mods & KeyEvent.SHIFT_MASK) != 0);
			
			//System.out.println( "mousebutton = " + mousebutton + " ctrl = " + ctrl + " alt = " + alt + " shift = " + shift + ".");
			
			if ( tooleditmode )
			{
				if ( mousebutton == 1 ) 
				{
					if ( shift & alt & (!ctrl) )
					{
						// draw to tool float buffer with lmb
						// respect any scaling of the draw space that is in effect..
						
						// track lastdrawindex so that the inbetween can be filled in if neccesary.. lerp it up
						
						// smoothing with some other crazymodifier - rightmouse or summink
						// then maybe do somekinda scroll offset setting with middlemouse
						
						//get array index from e.getX() by interping between -width and width
						
						drawToRuleWithCoordinates( e.getX(), e.getY() );
						
					}
				} else if ( mousebutton == 2) {
					
					//ALT IS ALWAYS TRUE FOR MOUSEBUTTON 2 .... wierd.. not sure where this is coming from but anyway..
					//alty and mousebutton 3 also gives an  alt + button2  event.. hmmm
					
					if( shift & ( !ctrl ) ) {
					
						//rotate the float rule lookup space in a tiling fashion
						rotateAndOffsetRuleWithCoordinates( e.getX(), e.getY() ,true, false , false);
						//System.out.println( "float rotate");
					
					} else if ( ( ctrl ) & ( !shift ) ) {
					
						//offset the float rule space in value
						rotateAndOffsetRuleWithCoordinates( e.getX(), e.getY(), false, true , false);
						//System.out.println( "float offset");
					
					} 
				
				} else if ( mousebutton == 3) {
				
					//alt can never be true
					
					if (shift  & !ctrl ) 
					{
						//System.out.println( "color rotate");
						//rotate the color rule lookup space in a tiling fashion
						rotateAndOffsetRuleWithCoordinates( e.getX(), e.getY(), true, false, true );
						
					} else if ( !shift & ctrl ) {
						
						//offset the color rule space in value (all 3 channels atm)
						//could look into making this a sampleable triple from the simulation... hmm that would be fnkewl
						//System.out.println( "color offset");
						rotateAndOffsetRuleWithCoordinates( e.getX(), e.getY(), false, true , true );
						
					} else if ( shift & ctrl )  {
						
						// System.out.println( "soften");
						// aka smoothing or blur
						softenRuleWithCoordinates( e.getX(), e.getY() );
						
					}
				}			
			}
			
			if ( mousebutton == 1) 
			{
				if ( ctrl & shift & !alt ) 
				{
						
					//tooldrawposition = new Point( e.getX(), e.getY() );
					tooldrawposition = new Point( tooldrawspace.width/2, e.getY() );
					
				} else if (ctrl & shift & alt) {
					
					tooldrawsize = new Dimension( 2 * Math.abs(tooldrawposition.x - e.getX() ), 2 * Math.abs( tooldrawposition.y - e.getY() ) );		
					
				}
			}
			//deliver(false); //with mouse down message is always delivered
			e.consume();
		}
	}       
	        
	public void mouseClicked(MouseEvent e)
	{
		//System.out.println("mouseClicked on tool");
		if (isVisible)
		{
			int mods = e.getModifiers();
			
			if ((mods & InputEvent.BUTTON3_MASK) != 0) mousebutton = 3;
			if ((mods & InputEvent.BUTTON2_MASK) != 0) mousebutton = 2;
			if ((mods & InputEvent.BUTTON1_MASK) != 0) mousebutton = 1;
			
			//if (mousebutton != 1) {killWidget(); return;}
			
			//draw a soft 'filterwidth' in the current 'filtershape" with a value formed from the relative mouseposition to the rule axis
			
			e.consume();
		}
	}
	
	public void keyPressed(KeyEvent k)
	{
		//System.out.println("keyPressed on tool");
		if (isVisible)
		{
			int keycode = k.getKeyCode();
			int mods = k.getModifiers();
			
			switch (keycode)
			{	
				case KeyEvent.VK_M:
				{
					//switch drawmode -> ctrl-m (or by widget (+shift))
					if ((mods & KeyEvent.CTRL_MASK) != 0) 
					{
						if ((mods & KeyEvent.SHIFT_MASK) != 0) 
						{
							//if (childWidges == null) childWidges = new Vector<EditWidget>();
							//childWidges.add(new EditWidget(EditWidget.CHOICE_WIDGET,"setDefaultDrawMode",this,
							//(Object[]) DRAWMODE_NAMES, xloc + horizontalChildSpacing, yloc, drawmode, resultscale, normalcolor, highlightcolor, selectedcolor, eventsrc) );
							
						}
					}
					break;
				}
				case KeyEvent.VK_N:
				{
					//switch default drawmode -> ctrl-n
					if ((mods & KeyEvent.CTRL_MASK) != 0) 
					{
						if ((mods & KeyEvent.SHIFT_MASK) != 0) 
						{
							//defaultdrawmode = (defaultdrawmode+1) % NUM_WIDGET_DRAWMODES;
							//System.out.println("widget class defaultDrawMode is now: "  + drawmode + ".");
							
						} else {
							
							//if (childWidges == null) childWidges = new Vector<EditWidget>();
							//childWidges.add(new EditWidget(EditWidget.CHOICE_WIDGET,"setDefaultDrawMode",this,
							//(Object[]) widgetDrawModes, xloc + horizontalChildSpacing, yloc, drawmode, resultscale, normalcolor, highlightcolor, selectedcolor, eventsrc) );
							
						}
					}
					break;
				}
				
				case KeyEvent.VK_DOWN : 
				{ 
					if ((mods & KeyEvent.CTRL_MASK) != 0) 
					{
						//if (floatvar != 0) floatvar = (float)Math.sqrt(floatvar);
					} //else floatvar -= floatvar/2;
					break;		
				}
					
				case KeyEvent.VK_UP   : 
				{ 
					break;
				}
				
				case KeyEvent.VK_RIGHT   : 
				{ 
					break;
				}
				
				case KeyEvent.VK_LEFT   : 
				{ 
					break;
				}
				/*case KeyEvent.VK_1    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) floatvar = 1.0f;  else floatvar = 0.1f; break;}
				case KeyEvent.VK_2    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) floatvar = 2.0f;  else floatvar = 0.2f; break;}
				case KeyEvent.VK_3    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) floatvar = 3.0f;  else floatvar = 0.3f; break;}
				case KeyEvent.VK_4    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) floatvar = 4.0f;  else floatvar = 0.4f; break;}
				case KeyEvent.VK_5    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) floatvar = 5.0f;  else floatvar = 0.5f; break;}
				case KeyEvent.VK_6    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) floatvar = 6.0f;  else floatvar = 0.6f; break;}
				case KeyEvent.VK_7    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) floatvar = 7.0f;  else floatvar = 0.7f; break;}
				case KeyEvent.VK_8    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) floatvar = 8.0f;  else floatvar = 0.8f; break;}
				case KeyEvent.VK_9    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) floatvar = 9.0f;  else floatvar = 0.9f; break;}
				case KeyEvent.VK_0    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) floatvar = 10.0f; else floatvar = 0.0f; break;}
				case KeyEvent.VK_MINUS    : { floatvar = -floatvar; break;}					
				case KeyEvent.VK_INSERT    : { floatvar += 0.01f; break;}
				case KeyEvent.VK_DELETE    : { floatvar -= 0.01f; break;}
				case KeyEvent.VK_HOME      : { floatvar += 0.1f;  break;}
				case KeyEvent.VK_END       : { floatvar -= 0.1f;  break;}
				case KeyEvent.VK_PAGE_UP   : { if ((mods & KeyEvent.CTRL_MASK) != 0) floatvar += 10.0f; else floatvar += 1.0f; break;}
				case KeyEvent.VK_PAGE_DOWN : { if ((mods & KeyEvent.CTRL_MASK) != 0) floatvar -= 10.0f; else floatvar -= 1.0f; break;}*/
			}
			k.consume();
		}
	}           
	
	public void keyReleased(KeyEvent k)
	{
		//System.out.println("keyReleased on tool");
		if (isVisible)
		{
			int keycode = k.getKeyCode();
			int mods = k.getModifiers();
					
			/*switch (keycode)
			{
				case KeyEvent.VK_ENTER :
				{	
					if ( (mods & KeyEvent.SHIFT_MASK) == 0)
					{
						deliver(true); 
						killWidget(); 
					} else {
						deliver(false);
					}
					break;
				}
				case KeyEvent.VK_ESCAPE :
				{
					//killWidget(); 
					break;
				}
				
				//case KeyEvent.VK_1    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) intvar = 10;  else intvar = 1; break;}
				//case KeyEvent.VK_2    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) intvar = 20;  else intvar = 2; break;}
				//case KeyEvent.VK_3    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) intvar = 30;  else intvar = 3; break;}
				//case KeyEvent.VK_4    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) intvar = 40;  else intvar = 4; break;}
				//case KeyEvent.VK_5    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) intvar = 50;  else intvar = 5; break;}
				//case KeyEvent.VK_6    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) intvar = 60;  else intvar = 6; break;}
				//case KeyEvent.VK_7    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) intvar = 70;  else intvar = 7; break;}
				//case KeyEvent.VK_8    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) intvar = 80;  else intvar = 8; break;}
				//case KeyEvent.VK_9    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) intvar = 90;  else intvar = 9; break;}
				//case KeyEvent.VK_0    : {if ((mods & KeyEvent.SHIFT_MASK) != 0) intvar = 100; else intvar = 0; break;}
				
				case KeyEvent.VK_MINUS    : { if ((mods & KeyEvent.SHIFT_MASK) != 0) deliver(false); break;}
				case KeyEvent.VK_INSERT    : { if ((mods & KeyEvent.SHIFT_MASK) != 0) deliver(false); break;}
				case KeyEvent.VK_DELETE    : { if ((mods & KeyEvent.SHIFT_MASK) != 0) deliver(false); break;}
				case KeyEvent.VK_HOME      : { if ((mods & KeyEvent.SHIFT_MASK) != 0) deliver(false);; break;}
				case KeyEvent.VK_END       : { if ((mods & KeyEvent.SHIFT_MASK) != 0) deliver(false);; break;}
				case KeyEvent.VK_PAGE_UP   : { if ((mods & KeyEvent.SHIFT_MASK) != 0) deliver(false); break;}
				case KeyEvent.VK_PAGE_DOWN : { if ((mods & KeyEvent.SHIFT_MASK) != 0) deliver(false); break;}
				
				//case KeyEvent.VK_ENTER : {deliver(true); killWidget(); break;}
				
				case KeyEvent.VK_DOWN : 
				{
					if ((mods & KeyEvent.SHIFT_MASK) != 0) deliver(false);
					break;
				}
				case KeyEvent.VK_UP   :
				{ 
					if ((mods & KeyEvent.SHIFT_MASK) != 0) deliver(false);
					break;
				}
				
				
				case KeyEvent.VK_RIGHT   : 
				{ 
					if ((mods & KeyEvent.SHIFT_MASK) != 0) deliver(false);
					break;
				}
				
				case KeyEvent.VK_LEFT   : 
				{ 
					if ((mods & KeyEvent.SHIFT_MASK) != 0) deliver(false);
					break;
				}
			}*/
			k.consume();
		}
	}
	
	public void keyTyped(KeyEvent k)
	{
		//System.out.println("keyTyped on tool");
		if (isVisible)
		{
			int mods = k.getModifiers();
			char c = k.getKeyChar();
			char[] cArr = {c};
			String cStr = new String(cArr);
			k.consume();
			
			int keykode = k.getKeyCode();
			switch (keykode)
			{
				
				case KeyEvent.VK_V:
				{
					if ((mods & KeyEvent.SHIFT_MASK) != 0)
					{
						System.out.println("Noticed shift-v in tool..!");
						break;
					}
				}
				
			}
		}
	}
	
}
