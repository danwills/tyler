// tiler, tylers guts and medula 0.0blongata
// its a [pixelmachine]
// by Dan Wills
// gdanzo@gmail.com
//

import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.text.DateFormat;
import java.lang.reflect.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;


//jpeg writing only for now poo
//import com.sun.image.codec.jpeg.*;
import javax.imageio.ImageIO;
import java.util.Collections;
//
// tylerEngine - a node-based game or demo editing engine:
//
// Network editor thoughts:
// * Essentially like Houdini - nodes with categories (eg processes-geometry, creates-and-attaches solver ensembles, processes pixels etc). 
// * Subnetworks and packaged subnetworks (like HDAs).
// * Kissing node connections!
// * Gestures to create nodes.
// * Modifier-click to delete nodes or links.
// * Modifier-drag to break connections.
// * Possibility for dynamic networks.
// * In-place networks (ie the net view becomes the game view when the setting for UI-ness is reduced.)
// 
// Other thoughts:
// * Multires (gigapixel) tileset-image loading, and tileset generation.
// * Graphical widget types (still staying similar to editWidget in spirit).
// * Scene/scriptfile save and load.
// * Undo.
// * Some basic 3d stuff.
// * Audio/music, including prebaked animcurves from FFT separations.
// * GPU-accelerated pixel FFT
// * pixelshader stuff including fractal rendering.


// todo:

// Figure out and fix keeping the current scroll-position when changing defaultLevel!
// Have a default currentLevel time-blend-rate so that currentLevel may change gradually to a new value.

// Multi-centers editWidget, so we don't need to use particle positions any more.
//     The ability to snap to particle positions could be added later.
//     Numcenters slider, ability to advect in various ways
//     Per-center draggable position, radius and power.

// Option to work in mousedown-paint mode!!.
// Paint via hotkey-drag.. with nicely time-interpolated paint.
// Topology draw to pixels and to topology buffer(s).. even very simple ones (shift N,S,E,W, swap/clone, average, rotate around point, attract, repel etc
//    Blend towards furthest/nearest, paint/reveal generator)
// setEveryFrameSave(bool) [done] grabEveryFrame(bool) [done]
// More fractal topologygenerator modes [keep em coming]
// Add power control to more generators [more]
// Particle/convolve/map driven topology generation/modification
// Automata modes (runtime rule sampling and universe lookups)_
// LString generator nodes
// Oversampled conditional derivative/filter driven particle motion
//



// Use somekinda ordering/slicing map to divide up layers of topology and ditheredly set neighbors to blend.

// Could try standard linear blending on the 1D addresses .. though of course it won't interpolate geometrically
//     and would probably alias like a bastard
// Or, since the floatTopologies both should still exist, I could linearly blend those geometrically.. but 
//     then I'd have to re-rasterize every step.. which might not be very fast.. but it'd sure look cool..
//     maybe the whole raster thing might be able to be skipped and still be fast?
//     need well-filtered, fast (SIMD?) point sampling on images at Float2D-locations.

// Runtime settings/img loading [done]
// Fading between tiles [done] with different blend modes [node]
// Main buffer/accumulator can work in feedback mode or otherwise, [node]
// Zoomable tile draw with option of snap-to 1x 2x 3x etc..
// Tile draw of tiles processed with (tiling) topologies.
// Stochiastic and raycast-esque topology draw modes.

// Sleep delay parameter [done]
// Java2D version (bufferedimage,alpha+antialias) [done]
// Conditional and assignment l-system atoms
// Subsurface scattering/glossy ref[lec|rac]tion and boiling/energy reaction/diffusion ~style effects.

// Fix bowtie and zap drawmodes to work properly with particleSize.



// Work on pixel mode to add brush size modulation, warp modes, brush shape parameters, AA alpha poly/func mode, brush blend modes etc
// Add a pixel mode that doesn't feed back (for nondestructive pixelmode resampling)
// Fix the buffer swapping/grabbing code
//
// >> *!* preset/script saving/loading*!* << this needs major code cleanup to be implemented properly...
// ~* image saving/loading
//
// Full key documentation.. very important or no-one else will ever be able to use it! :(
//
// alpha soft clear on/off
//
// setNumParticles() [done]
// Noise implementation [done]
// Noisey tilescroll direction - that also takes effect on lostfocus

public class Tiler extends Canvas implements Runnable, KeyEventDispatcher, KeyListener, MouseListener, MouseMotionListener, FocusListener, ImageObserver
{
	Tiler( GraphicsConfiguration g )
	{
		graphicsConfig = g;
	}
	
	//declare an inner class for convenient minimal 2D vector ops
	class Float2D
	{
		public float x;
		public float y;

		public Float2D(float a, float b)
		{
			x = a;
			y = b;
		}

		public synchronized void translate(float dx, float dy)
		{
			x += dx;
			y += dy;
		}

		public synchronized void scale(float d)
		{
			x *= d;
			y *= d;
		}
		
		public synchronized void set(float tx, float ty)
		{
			x = tx;
			y = ty;
		}

		public synchronized void absoluteLoop(float xloop, float yloop)
		{
			x = ( (x>0) ? (x % xloop) : ((x % xloop) + xloop));
			y = ( (y>0) ? (y % yloop) : ((y % yloop) + yloop));
		}
		
		public void rotate(float theta)
		{
			float tmpdelta[] = new float[2];
			//standard vector rotation
			// x' = x*cos(theta) - y*sin(theta)
			// y' = x*sin(theta) + y*cos(theta)
			
			tmpdelta[0] = (float) (x * (float) Math.cos(theta)) - 
			                      (y * (float) Math.sin(theta));
			              
			tmpdelta[1] = (float) (x * (float) Math.sin(theta)) + 
			                      (y * (float) Math.cos(theta));
			                      
			x = tmpdelta[0];
			y = tmpdelta[1];
		}
		
	}
	
	GraphicsConfiguration graphicsConfig;
	
	Image tile;
	//Point tilesize;

	Float2D flooppos;
	int wiggleJumpFrameRate = 15;
	Point wigglepos;
	
	Point pixelWiggleAmp;
	
	Float2D draggingPos;
	Point looppos;
	Float2D delta;
	float deltaAngle = 0.0F;
	float pixelDeltaAngle = 0.0F;
	float deltaAmp = 1.0f;
	float topologyresfactor = 2.0f;
    float overallresfactor = 2.0f;
	float scalepixelbuffer = 2.0f;
    String saveFormatStr = "png";
    //String saveFormatStr = "jpg";
    
	//private MouseGestures mouseGestures = new MouseGestures();
	
	public enum MouseCursorMode
	{
		defaultSystem,
		hand,
		crosshair,
		hidden;
		
		private String text;
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( MouseCursorMode p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
		
		public static MouseCursorMode fromString( String text ) 
		{
	    	
            if ( defaultSystem.name().equalsIgnoreCase( text ) )
            {
                return defaultSystem;

            } else if ( hand.name().equalsIgnoreCase( text ) ) {
                return hand;

            } else if ( crosshair.name().equalsIgnoreCase( text ) ) {
                return crosshair;

            } else if ( hidden.name().equalsIgnoreCase( text ) ) {
                return hidden;
            }

            // if the enum string was borked, default!
            return defaultSystem;
    	
		}
        
        public static MouseCursorMode fromInteger(int x) 
        {
            switch(x) 
            {
                case 0:
                    return defaultSystem;
                case 1:
                    return hand;
                case 2:
                    return hand;
                case 3:
                    return crosshair;
            }
            return null;
        }
    }
	
	/* declare enum for particle paing modes */
	public enum ParticlePaintMode 
	{
		particle, //draw one frame of each particle's life each frame
		particleTrace, //
		neighbors;
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( ParticlePaintMode p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}
	
	ParticlePaintMode paintmode = ParticlePaintMode.particleTrace;
	
	public enum PixelBlendMode 
	{
		alpha, 
		max,
		min, 
		absoluteSubtract, 
		absoluteAdd,
		multiply,
		lumiDiffMix;
		//screen, xor, lumi, hueCycle
	
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( PixelBlendMode p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}
	
	PixelBlendMode pixelblendmode = PixelBlendMode.lumiDiffMix;
	
	int PIXELAREAWIDTH = 512;
	int PIXELAREAHEIGHT = 512;
	
	//speed up particles then make them default-on again.
	float particlerate = 0.0f;//13.0f; //-1.0f
	float locrandom = 1;
	float speedmultiplier = -0.2f;
	double speedrandom = 24.6;
	double spin = 0.0;
	double spinrandom = 0.082; //gaussrandom on the spin
	double spinprobability = -0.756451; //gaussrandom on the spin
	float drag = 0.0143f;
	double dragrandom = 0.0001; //gaussrandom on the drag
	
	public int nextbooleanmatrixsize = 11;
	boolean[] currentbooleanmatrix;
	
	float currentlevel = 0.1f;
	float currenttopologyparam = 0.3f;
	float currenttopologypower = 1.0f;
	float topologysquishvalue = 0.2f;
	boolean generatetopologymapped = false;
	boolean multipointfromparticle = true;
	int numcentersmultipoint = 5;
	
	int interactiveMode = 0;
	float speedNoiseAmp = 3.8f;
	float speedNoiseFreq = 0.03128637f;
	boolean speedNoiseSigned = true;
	float rotNoiseAmp = 6.0f;
	float rotNoiseFreq = 0.02824f;
	float pixelRotNoiseFreq = 0.01224f;
	float rotNoisePow = 3.0f;
	
	int glide_length = 10; //10 frames .. not used yet
	public boolean draggingTilePosition = false;
	boolean mousedown = false;
	boolean ctrldown = false;
	MouseCursorMode mousecursormode = MouseCursorMode.defaultSystem;
	//float menuspeed = (float)Math.PI/16.0f;;
	float menuspeed = (float)Math.PI/4.0f;
	Point clickloc;
	Point mouseloc;
	Point lastemitloc;
	float[] lastemitveloc;
	Point[] mousebuffer;
	Point pixelbufferloc;
	
	int mousebufferlength = 16;
	int currmouse = 0;
	Float2D floatingclick; //should be in a ~menu class
	Float2D clicklooppos;
	boolean allbits = false;
	int imagestatus = 0;
	boolean debug = false;
	long framenumber = 0;
	long constructTime = ( new java.util.Date() ).getTime();
	
	int numparticles;
	int pbuffersize = 20;
	int currentparticlenum = 0;
	boolean particleson = false;
	ParticleSystem particluster;
	Particle[] dust;
	Particle lastEmittedParticle;
	boolean connectEmission = false;

	boolean toolalwaysharvests = true;
	Thread motionthread;
	Thread imageloaderthread;
	ImageLoader iloader;
	int tileLoadCounter = -1;
	int tileLoadAlpha = 12;
	//int tileLoadCount = 256/tileLoadAlpha;
	int tileLoadAlphaAccum = 0;
	int tileLoadRate = 1;
	boolean imageCurrentlyLoading = false;
	Tool1D toolrule;
	//tylersEars tilersEars;
	float audioAmpSize = 300;
	
	public final static int ALPHA_AT_MIN      = 0;
	public final static int ALPHA_FADING_UP   = 1;
	public final static int ALPHA_AT_MAX      = 2;
	public final static int ALPHA_FADING_DOWN = 3;
	
	int clearstate = ALPHA_AT_MAX;
	boolean clearmode = true;
	float screenclearalpha = 0.0f;
	int clearAlpha = 0x1e;
	int alphaFromLumiAmount = 0x00;
	int pixelAlpha = 0xff;
	int alphaAtLastEvent = 0x30;

	long lastAlphaEventTime = 0;
	float alphaFadeLength = 1.33f;

	boolean pixelaccess = true;
	boolean warpon = true;
	boolean dowarp = true;
	boolean warpwithalpha = false;
	Topology.TopologyBlendMode topologyNextFrameMode = Topology.TopologyBlendMode.averageThreaded;
	int topologyoversampling = 0;
	boolean resizeon = true;
	
	Dimension bufferdimension;
	Dimension screendimension;
	Image bufferimage;
	Image[] recordbuffer;
	String[] imagenames;
	Image[] preloaded;
	Image pixelbuffer;
	Image tilepixelbuffer;
	
	int[] bufferpixels;
	int[] tilepixels;
	int[] tilepixelsLoading;
	int[] processbuffer;
	int[] gradmap;
	int[] extractormap;
	float[] extractormapf;

	String savename = "tyler";
	int saveincrement = 0;
	int saveWhichBuffer = 0;
	boolean continuoussave = false;
    
    //Vector<ImageSaveThread> imageSaveThreads;
    java.util.List<ImageSaveThread> imageSaveThreads;
    
    //Vector<TopologyProcessorThread> topologyProcessorThreads;
    java.util.List<TopologyProcessorThread> topologyProcessorThreads;
    // better be greater than zero, that's all I'm gonna say :P
    int numTopologyProcessorThreads = 32;
    
	int saveframespacing = -1;
	boolean continuouspixelgrab = false;
	boolean continuousmapgrab = false;
	boolean continuousmapdraw = false;

	Topology.TopologyGeneratorMode topologygeneratormode = Topology.TopologyGeneratorMode.Foo;
	Topology.TopologyRenderMode topologyrendermode = Topology.TopologyRenderMode.DitheredMultilayer;
	
	int[] topologymap;
	int numwarplayers = 32;
	TopologyGenerationThread topologyGenerationThread = null;
	Topology topologylayer;
	boolean topologyRegenerating = false;
	float topologyBlendRate = 0.5f;
	int topologyBlendCount = 0;
	
	Component parentComponent;
	TextArea outputTextArea = null;
	MemoryImageSource masterpixel;
	MemoryImageSource slavepixel;

	Random randex;
	int numimages = 0;
	int currentimage = 0;
	
	TilerKeyMap keymap;
	
	Method[] menumethods;
	
	Vector<EditWidget> dawidgets;
	float widgetresultscale = 1;
	Graphics2D buffercontext;
	Graphics2D buffercontexta;
	Font defaultfont;
    
    // sleepdelay now represents nanoseconds
    // don't really want to delay at all!
	int sleepdelay = 100;

	public static Color bgcolor = java.awt.Color.black;
	public  static Color midcolor = java.awt.Color.darkGray;
	public static  Color fgcolor = java.awt.Color.white;
	
	boolean initialised = false;
	double initializedPercent = 0.0;
	
	public void init()
	{
		output("Checking graphics toolkit. . .");
		Toolkit defaulttoolkit;
		try {
			defaulttoolkit = this.getToolkit().getDefaultToolkit();
			ColorModel cmodel = defaulttoolkit.getColorModel();
			output("Default toolkit's current color model is: " + cmodel.toString() );
			
			//Dimension dscreen = defaulttoolkit.getScreenSize();
			//screendimension = new Dimension(dscreen);
			//output("Screen dimensions are: " + dscreen.toString());
			
			PointerInfo pinfo = MouseInfo.getPointerInfo();
            GraphicsDevice gdevice = pinfo.getDevice();
            Point plocation = pinfo.getLocation();
            GraphicsConfiguration selectedGraphicsConf = gdevice.getDefaultConfiguration();
            Dimension dscreen = selectedGraphicsConf.getBounds().getSize();
			screendimension = new Dimension(dscreen);
			output("Screen dimensions are: " + dscreen.toString());
			
			Clipboard cliptest = defaulttoolkit.getSystemClipboard();
			output("Clipboard success!: " + cliptest.getName());
		}
		catch (AWTError e) {output("Error getting toolkit"); }
		catch (SecurityException sx) {output("Security problem getting system clipboard.");}
		
		//bufferdimension = new Dimension( PIXELAREAWIDTH, PIXELAREAHEIGHT );
		
		setScalePixelBuffer( topologyresfactor );
		
		bufferdimension = new Dimension( (int)(screendimension.width/scalepixelbuffer), (int)(screendimension.height/scalepixelbuffer) );
		
		//this.getSize();
		output("Got pixel-area dimensions:" + bufferdimension.width + " by " + bufferdimension.height);
		output("Got screen-area dimensions:" + screendimension.width + " by " + screendimension.height);

		bufferimage = parentComponent.createImage(screendimension.width, screendimension.height);
		buffercontext = (Graphics2D) bufferimage.getGraphics();
		buffercontexta = (Graphics2D) buffercontext;
		
		initializedPercent = 0.05;
		
		//move config and image list initialisation to a method.
		FileReader configFileReader = null;
		LineNumberReader lineReader = null;
		String inames[] = null;
		int numlines = 0;

		validate();

		this.setMouseCursorMode( this.mousecursormode );
		
		//this.update()
		
		initializedPercent = 0.1;
		
		try
		{
			configFileReader = new FileReader(".tylerConfig");
			lineReader = new LineNumberReader( configFileReader );
		}
		catch ( java.io.FileNotFoundException ex) { System.err.println("could not load config file."); }


		if (lineReader != null)
		{
			String aline = null;

			try {
				lineReader.mark(10000000);
				aline = lineReader.readLine();
				numlines = 1;

				while(aline != null)
				{
					//print disabled for the mo...
					//output(aline);
					aline = lineReader.readLine(); numlines++;
				}

				if (numlines > 0)
				{
					output("Numlines in config file is:" + numlines  + ".");
					inames = new String[numlines];

					lineReader.reset();
					aline = lineReader.readLine();
					inames[0] = aline;
					int i = 1;
					while(aline != null)
					{
						aline = lineReader.readLine();
						inames[i] = aline;
						i++;
					}

				}
			} catch (IOException xzor) { System.err.println("IOError while loading config file."); }
		}
		
		initializedPercent = 0.15;
		
		randex = new Random();

		if ((numlines > 0) && (inames != null))
		{

			numimages = numlines-1;
			imagenames = inames;
			currentimage = randex.nextInt() % numimages;
			currentimage = (currentimage < 0 ) ? -currentimage : currentimage;

		} else {
			output("Config read failed for some reason! This means we'll only be loading a single image called 'tyler.jpg'");
			numimages = 1;
			imagenames = new String[numimages];
			imagenames[0] = "tyler.jpg";
			currentimage = 0;
		}
		
		// seems there's a new way to get double-buffering, but this isn't it:
		//self.setDoubleBuffered( True );
		// maybe need to build/set it with the parent window object and pass in
		
		initializedPercent = 0.2;

		bufferpixels = new int[bufferdimension.width*bufferdimension.height];
		processbuffer = new int[bufferdimension.width*bufferdimension.height];
		tilepixels = new int[bufferdimension.width*bufferdimension.height];
		tilepixelsLoading = new int[bufferdimension.width*bufferdimension.height];
		gradmap = new int[bufferdimension.width*bufferdimension.height];
		extractormap = new int[bufferdimension.width*bufferdimension.height];
		extractormapf = new float[bufferdimension.width*bufferdimension.height];
		
		setAlphaToGrad( bufferdimension, 0.0f, gradmap);

		initializedPercent = 0.25;
		if (warpon)
		{
			output("Warp is on, allocating space for " + numwarplayers + " layers, (this may take a while)...");
			topologylayer = new Topology(bufferdimension.width,bufferdimension.height,bufferpixels,numwarplayers,randex);
			topologylayer.setFourierTopology( bufferdimension.width/2, bufferdimension.height/2, 0, 10,false, null, true, 0 );
			topologylayer.renderAccurateMultiTopology( numwarplayers, true );
			
		} else output( "Warp disabled, memory will be allocated when it becomes enabled." );
		
		masterpixel = new MemoryImageSource( bufferdimension.width, bufferdimension.height, bufferpixels, 0, bufferdimension.width );
		masterpixel.setAnimated( true );
		pixelbuffer = createImage( masterpixel );
		masterpixel.newPixels( 0, 0, bufferdimension.width, bufferdimension.height );
		
		slavepixel = new MemoryImageSource( bufferdimension.width, bufferdimension.height, tilepixelsLoading, 0, bufferdimension.width );
		slavepixel.setAnimated( true );
		tilepixelbuffer = createImage( slavepixel );
		slavepixel.newPixels( 0, 0, bufferdimension.width, bufferdimension.height );
		
		initializedPercent = 0.3;
		
		//wait for this to happen..
		MediaTracker btracker = new MediaTracker(parentComponent);
		btracker.addImage(pixelbuffer,1);
		btracker.addImage(bufferimage,1);
		
		try {
		
			btracker.waitForAll();
			
		} catch (InterruptedException e) {
		
			output("Interrupted while waiting for buffer image construction." );
		}
		
		if ( bufferimage == null ) output("Var bufferimage was null! (even after waiting for all)");

		MiniPixelTools.init( );
		MiniPixelTools.setChannelOffset(20.0f);
		MiniPixelTools.setChannelMultiplier(4.0f);
		
		initializedPercent = 0.4;
		
		if (currentimage < 0) currentimage += numimages;
		output("Buffer size is: " + bufferdimension.toString());

		output("Starting ImageLoader thread. . .");
		iloader = new ImageLoader();
		imageloaderthread = new Thread(iloader);
		imageloaderthread.start();
		//imageloaderthread.setPriority(Thread.MIN_PRIORITY);
		
		initializedPercent = 0.45;

		pixelaccess = true; //grab the pixels the first time...
		loadImage( imagenames[ currentimage ] );
        
		MiniPixelTools.grabPixels(tile,bufferpixels,masterpixel);
        
		masterpixel.newPixels(0,0,bufferdimension.width,bufferdimension.height);

		defaultfont = new Font("Verdana",Font.PLAIN,32);

		numparticles = 1024;
		
		initializedPercent = 0.5;
		outputNoNewline("Loading tile [");
		while(!iloader.loadedOK)
		{
			outputNoNewline(".");
			
			if (initializedPercent < 0.6) initializedPercent += 0.01;
			
			try{
				Thread.currentThread().sleep(10);
			} catch (java.lang.InterruptedException e)
			{
				outputNoNewline("*");
			}
		}
		output("] done");
		
		initializedPercent = 0.6;
		
		dawidgets = new Vector<EditWidget>();

		int samplelength = 512;
		toolrule = new Tool1D(samplelength,bufferdimension.width,bufferdimension.height,bufferpixels,this, screendimension.width, screendimension.height);

		mousebufferlength = 16;
		mousebuffer = new Point[mousebufferlength];
		for (int i = 0; i < mousebufferlength; i++)
		{
			mousebuffer[i] = new Point( Math.abs(randex.nextInt() % bufferdimension.width),
			                            Math.abs(randex.nextInt() % bufferdimension.height) );
		}

		initializedPercent = 0.65;
		
		output("Divining a cloud of " + numparticles + " particles.");
		particluster = new ParticleSystem(numparticles, bufferdimension.width, bufferdimension.height, screendimension.width, screendimension.height, bufferpixels, toolrule);
		output("Allocated particles OK.");
		particluster.setDeathBySpeed(0.03f,Float.POSITIVE_INFINITY);
		particluster.setChannelSpinLevel( 0.02142f );
		particluster.setPixelSpaceScale( 1.0f/scalepixelbuffer );
		
		dust = new Particle[pbuffersize]; //dust buffering
		
		initializedPercent = 0.7;
		
		looppos = new Point(0,0);
		wigglepos =  new Point(0,0);
		pixelWiggleAmp = new Point(50,50);
		floatingclick = new Float2D(0.0f,0.0f);
		clickloc = new Point(screendimension.width/2,screendimension.height/2);
		pixelbufferloc = new Point( screendimension.width/2 ,screendimension.height/2);
		clicklooppos = new Float2D(0.0f,0.0f);
		flooppos = new Float2D(0.0f,0.0f);
		delta = new Float2D(0.0f,0.0f);
		
		setBackground(bgcolor); 
		
		Calendar calendar = Calendar.getInstance();
		java.util.Date now = calendar.getTime();
		DateFormat datef = DateFormat.getInstance();
		String nowstr = datef.format( now ).replace("/","_").replace(" ","_").replace(":","_");
		System.out.println("The time is now: " + nowstr);
		setSaveString("tyler_" + nowstr);
		
		output("Turning an ear toward the people out there. . .");
		mouseloc = new Point(bufferdimension.width/2,bufferdimension.height/2);
		lastemitloc = new Point(bufferdimension.width/2,bufferdimension.height/2);
		lastemitveloc = new float[2];
		lastemitveloc[0] = 0;
		lastemitveloc[1] = 0;
		
		//pass an xml file in here eventually, that can be saved after reconfiguration
		keymap = new TilerKeyMap( this, particluster, toolrule );
		
		addKeyListener(this);
		
		initializedPercent = 0.8;
		
		KeyboardFocusManager currKFM = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		currKFM.addKeyEventDispatcher(this);
		
 		// disable auto-binding for tab to cycle panes in the AWT since we're not using that but we'd like to use the tab key pls!!!
        // This generates a **harmless** compile-time warning because of the cast from empty-set to Vector-of-AWTKeyStroke's
		currKFM.setDefaultFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet() );
		currKFM.setDefaultFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet() );

		output("Added keyEventDispatcher interface callback");
		
		output("Turning ON particles..");
		setParticlesOn( true );
		
		output("Turning ON 'Continuous Pixel Grab'.. (Ctrl-Shift-w to Toggle)");
		setContinuousPixelGrab( true );
		
        imageSaveThreads = Collections.synchronizedList(new ArrayList<ImageSaveThread>());
        //new Vector<ImageSaveThread>();
        // 
        
        // let's do this on demand instead?, yep.. but not yet!
        //topologyProcessorThreads = Collections.synchronizedList(new ArrayList<TopologyProcessorThread>());
        topologyProcessorThreads = new ArrayList<TopologyProcessorThread>();
        addTopologyProcessorThreads( numTopologyProcessorThreads );
        output( "Created this number of topology-processing threads: " + topologyProcessorThreads.size() );
        
            //Vector<TopologyProcessorThread>();
		//setPixelAlpha( 33 );
		
		//mouseGestures = new MouseGestures();
        //mouseGestures.addMouseGesturesListener( this );
		//mouseGestures.setMouseButton(MouseEvent.BUTTON3_MASK);
		//mouseGestures.start();
		
		initializedPercent = 0.9;
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addFocusListener(this);
		this.setFocusable( true );
		this.requestFocus();
		
		output("Initialisation finished, woo! :).");
		output("--------");
		initializedPercent = 1.0;
		
		try
        {
            // Nano-sleep
            Thread.currentThread().sleep(0l,1);
		}
		catch(InterruptedException ie){ }
		
		initialised = true;

		this.setMouseCursorMode( this.mousecursormode );
	}

	public void setParentComponent(Component c)
	{ parentComponent = c; }

	public void tryToGetFocus()
	{ this.requestFocus(); }

	public void setOutputTextArea(TextArea x)
	{ outputTextArea = x; }
	
	public void outputNoNewline(String s)
	{
		if (!initialised)
		{
			System.out.print(s);
			//showStatus(s);
		} else {
			//if (debug) {
				System.out.print(s);
				if (outputTextArea != null)
				{
					outputTextArea.append(s);
				}
				//showStatus(s);
			//} //else showStatus(s);
		}
	}
	
	public void output(String s)
	{
		if (!initialised)
		{
			System.out.println(s);
			//showStatus(s);
		} else {
			//if (debug) {
				System.out.println(s);
				if (outputTextArea != null)
				{
					outputTextArea.append(s + System.getProperty("line.separator"));
				}
				//showStatus(s);
			//} //else showStatus(s);
		}
	}
	
	public void cueImage(Image img)
	{
		if (imageCurrentlyLoading == false) tile = img;
		else if (!initialised) tile = img;
		// output("loaded [" + tile.getWidth(this) + " * " + tile.getHeight(this) + "] pixels");
	}

	public void freshenArray(int[] bounty)
	{
		if ( !imageCurrentlyLoading )
		{
			if ( bufferpixels == bounty )
			{
				output("array identity for resolution: (" + bufferdimension.width + "," + bufferdimension.height + ")");
				System.arraycopy(bounty, 0, tilepixelsLoading, 0, bounty.length);
				slavepixel.newPixels(0,0,bufferdimension.width,bufferdimension.height);
	
			} else if ( bufferpixels.length == bounty.length )
			{
				output("buffer pixel array and pixel bounty are the same length.. copying the whole thing...");
				System.arraycopy(bounty, 0, tilepixelsLoading, 0, bounty.length);
				slavepixel.newPixels(0,0,bufferdimension.width,bufferdimension.height);
				
			} else {
				output("bufferpixel and pixel bounty are different length, doing nothing for the moment..");
			}
		} 
	}

	//default image loader
	public void loadImage(String imgname)
	{
		Image ok = null;
		ok = loadimage(imgname, true, bufferdimension, Image.SCALE_SMOOTH, true, tilepixelsLoading);

		if (ok == null) output("Image still loading: " + imgname);
		else output("Loading image: " + imgname);
	}

	//not to be called before numimages parameter is parsed!
	Image loadimage(String imageName, boolean resize, Dimension size, int hints, boolean pixelgrab, int[] grabdest)
	{
		if (resize) output("Loading and resizing image: " + imageName);
		else output("Loading image: " + imageName);

		Image temp = null;
		Image resized = null;
		if (imageName == "") return null;
		NumberFormat numf = new DecimalFormat("0000");
		String filename = imageName;

		//wait for image loading in another thread so the show can go on...
		int lastdot = filename.lastIndexOf('.');
		String imagename = filename.substring(0,lastdot);
		output("Currently loading " + imagename + " ... ");
		output("Requested size: " + size.width + "," + size.height);

		iloader.load( filename, grabdest, size.width, size.height, (Component)this, true, this );
		iloader.loadedOK = false;
		imageCurrentlyLoading = true;
		tileLoadCounter = 0;
		tileLoadAlphaAccum = 0;
		
		//may be null
		return temp;
	}

	public void start()
	{
		motionthread = new Thread(this);
		motionthread.start();
	}
	public synchronized void stop()
	{ motionthread = null; }

	//set negative at your own risk:
	public void setSleepDelay(int to)
	{ sleepdelay = to; }

	public void run()
	{
		Thread me = Thread.currentThread();
		
		while (motionthread == me)
        	{
			repaint();
			motionthread.yield();
        	//try
			//{
        	//	motionthread.sleep( 0l, sleepdelay );
			//} catch (InterruptedException e) {} //break;
		}
	}

	public void destroy()
	{
        	removeMouseListener(this);
        	removeMouseMotionListener(this);
	}

	public String getAppletInfo()
	{ 
		return new String("tiler by dan wills <gdanzo@gmail.com>"); 
	}

	public void paintComponent(Graphics context)
	{
		update(context); 
	}
	
	public float sinSum( float sample )
	{
		return (float) (Math.sin( sample ) + Math.sin( sample * 0.323442 + 83.311312f )*0.22712 + Math.sin( sample * 1.622212 - 313.313) * 0.1442  + Math.sin( sample * 1.422 - 93.2313 ) * 0.218 );
	}
	
	public synchronized void update( Graphics ocontext )
	{
		Graphics2D context = (Graphics2D) ocontext;

		java.awt.AlphaComposite ac = java.awt.AlphaComposite.getInstance( java.awt.AlphaComposite.SRC_OVER );
		context.setComposite( ac );
		
		// This was causing a bad case of the flickers':
		//parentComponent.setBackground( Color.gray );
		
		if ( !initialised )
		{
			if ( buffercontext != null )
			{
				//get all Progress Bar on yo' ass..
				
				this.setForeground(fgcolor);

				buffercontext.setColor(Color.black);
				buffercontext.clearRect(0,0,screendimension.width,screendimension.height);
				
				int centerx = screendimension.width/2;
				int centery = screendimension.height/2;
				
				int barwidth = (int)( screendimension.width/1.5 );
				int barheight = screendimension.height/12;
				
				int padbar = barheight/15;
				
				buffercontext.setColor(Color.white);
				buffercontext.fillRect(centerx - barwidth/2,centery - barheight/2, barwidth, barheight );
				
				buffercontext.setColor(Color.black);
				buffercontext.fillRect(centerx - barwidth/2 + padbar,centery - barheight/2 + padbar, barwidth - 2 * padbar, barheight - 2*padbar);
				
				int progressWidth = (int)( (barwidth - 4 * padbar) * initializedPercent);
				
				buffercontext.setColor(Color.white);
				buffercontext.fillRect( centerx - progressWidth/2, centery - barheight/2 + 2 * padbar, progressWidth, barheight - 4 * padbar);
				
				context.drawImage(bufferimage, 0, 0, this);
				framenumber++;
			}
		} else {
		
			//set antialiasing / filtering if possible...
			context.setFont( defaultfont );

			Dimension d = getSize();
			
			if (buffercontext == null)
			{
				//bufferdimension = d;
				bufferimage = createImage(screendimension.width, screendimension.height);
				buffercontext = (Graphics2D) bufferimage.getGraphics();
				buffercontexta = (Graphics2D) buffercontext;
				
			}
			
			if ( continuouspixelgrab )
			{
				int pbxbit = (int) ( ( bufferdimension.width ) / 2.0f); 
				int pbybit = (int) ( ( bufferdimension.height ) / 2.0f);
				
				//use scaled copy of masterpixel?
				// Image bufferimageScaled = bufferimage.getScaledInstance( (int)( bufferdimension.width / scalepixelbuffer * 2), (int)( bufferdimension.height / scalepixelbuffer * 2), Image.SCALE_FAST );
                // Image bufferimageScaled = bufferimage.getScaledInstance( (int)( bufferdimension.width / scalepixelbuffer * topologyresfactor), (int)( bufferdimension.height / scalepixelbuffer * topologyresfactor), Image.SCALE_AREA_AVERAGING );
                // Image bufferimageScaled = bufferimage.getScaledInstance( (int)( bufferdimension.width / scalepixelbuffer * topologyresfactor), (int)( bufferdimension.height / scalepixelbuffer * topologyresfactor), Image.SCALE_SMOOTH );
                Image bufferimageScaled = bufferimage.getScaledInstance( (int)( bufferdimension.width / scalepixelbuffer * overallresfactor), (int)( bufferdimension.height / scalepixelbuffer * overallresfactor), Image.SCALE_FAST );
				MiniPixelTools.grabPixelRectangle( bufferimageScaled, bufferpixels, 0, 0, (int)(bufferdimension.width) ,(int)(bufferdimension.height), masterpixel );
		}
			
			if ( screenclearalpha >= 1.0f )
			{
				 buffercontext.clearRect(0,0,screendimension.width,screendimension.height);
				 
			} else if ( screenclearalpha > 0 ) {
			
				Color clearColor = new Color( 0.0f, 0.0f, 0.0f, screenclearalpha );
				buffercontext.setColor(  clearColor );
				buffercontext.fillRect(0,0,screendimension.width,screendimension.height);
			}
			
			if ( ( currentlevel != 0 ) && ( !Float.isInfinite( currentlevel ) ) && ( !Float.isNaN( currentlevel ) ) )
	        {
	        	flooppos.x %= (float)bufferdimension.width/currentlevel;
	        	flooppos.y %= (float)bufferdimension.height/currentlevel;
	        } else {
	        	flooppos.x = 0;
	        	flooppos.y = 0;
	        }
		
		if (interactiveMode >= 1)
		{
			float time = ( (float) ( ( new java.util.Date() ).getTime() - constructTime ) ) * 0.01f;
			deltaAmp = sinSum( time * speedNoiseFreq - 12.443f ) * speedNoiseAmp;
			deltaAngle = (float) Math.pow( sinSum( time * rotNoiseFreq + 833.102f ), rotNoisePow) * rotNoiseAmp;
			
			pixelDeltaAngle = (float) Math.pow( sinSum( time * pixelRotNoiseFreq - 2072.102f ),  rotNoisePow) * rotNoiseAmp;
			
			Float2D pixelDelta = new Float2D( pixelWiggleAmp.x * deltaAmp, pixelWiggleAmp.y * deltaAmp );
	        pixelDelta.rotate(pixelDeltaAngle);
	        	
	        /* if (interactiveMode >= 3000)
	        {

	        	if ( ( framenumber % wiggleJumpFrameRate ) == 0) wigglepos.translate( (int) ( Math.random() * bufferdimension.width * 3) , (int) ( Math.random() * bufferdimension.height * 3 ) );

	        } else if (interactiveMode >= 300)
	        {
	        	Float2D noiseDelta = new Float2D( (float) improvedPerlinNoise.improvedPerlinNoiseThree( time * speedNoiseFreq - 2072.102f, 2.5, -8.545 ) * speedNoiseAmp, (float) improvedPerlinNoise.improvedPerlinNoiseThree( time * speedNoiseFreq - +772.822f, -92.735, 28.545 ) * speedNoiseAmp );
	        	wigglepos.translate( (int) noiseDelta.x, (int) noiseDelta.y );

	        } else if (interactiveMode >= 50)
	        {

	        	wigglepos.setLocation( improvedPerlinNoise.improvedPerlinNoiseThree( time * pixelRotNoiseFreq - 2072.102f, 2.5, -8.545 ) * screendimension.width, improvedPerlinNoise.improvedPerlinNoiseThree( time * pixelRotNoiseFreq - +772.822f, -92.735, 28.545 ) * screendimension.height );

	        } else if (interactiveMode >= 5) {

	        	wigglepos.setLocation(pixelDelta.x,pixelDelta.y);

	        } else wigglepos.setLocation( 0, 0 );
			*/
	        	
			if (interactiveMode >= 20)
			{
				//automatically generate topology every n frames...
				// move the emit position around..
				mousebuffer[currmouse].move( mouseloc.x + wigglepos.x ,mouseloc.y + wigglepos.y );
				currmouse = (currmouse + 1) % mousebufferlength;
			}
			
		} else {
			wigglepos.setLocation(0, 0);
			deltaAmp = 1;
			deltaAngle = 0;
		}
		    Float2D roDelta = new Float2D( delta.x, delta.y );
	        roDelta.rotate( deltaAngle );
	        roDelta.scale( deltaAmp );
            
            // Advect flooppos when not in dragging-mode
	        if ( !draggingTilePosition ) flooppos.translate( roDelta.x, roDelta.y );

	    	buffercontext.setPaintMode();
	    	buffercontext.setFont( defaultfont );

	    	if (pixelaccess)
	    	{
	    		if (clearmode)
	    		{
					//copy rotated tile to bufferpixels
	    			int fadeAlpha = clearAlpha;
	    			long ctime = (new java.util.Date()).getTime();

	    			if (lastAlphaEventTime > ctime)
	    			{
	    				output("lastAlphaEventTime was in the future! . . . setting it back to the present to prevent mathematical carnage.");
	    				lastAlphaEventTime = ctime;
	    			}

	    			if (clearstate == ALPHA_FADING_UP)
	    			{

	    				if (alphaFadeLength != 0)
	    				{
	    					float fadeAlphaf = ((ctime - lastAlphaEventTime)/1000.0f) / alphaFadeLength;

	    					if (fadeAlphaf >= 1) 
	    					{
								//fully faded up
	    						lastAlphaEventTime = ctime;
	    						clearstate = ALPHA_AT_MAX;
	    						fadeAlpha = clearAlpha;

	    					} else { //calculate fade

	    						fadeAlphaf = Math.max(0.0f,fadeAlphaf);
	    						fadeAlpha = (int) (fadeAlphaf * clearAlpha);
								//jus' to be safe ..
	    						fadeAlpha = fadeAlpha & 0xff;
	    					}

	    				} else {

	    					fadeAlpha = clearAlpha;
	    					lastAlphaEventTime = ctime;
	    					clearstate = ALPHA_AT_MAX;
	    				}

	    			} else if (clearstate == ALPHA_FADING_DOWN) {

	    				if (alphaFadeLength != 0)
	    				{
	    					float fadeAlphaf = 1.0f - (  ((ctime - lastAlphaEventTime)/1000.0f) / alphaFadeLength  );

	    					if (fadeAlphaf <= 0.0f) 
	    					{
								//fully faded down..
	    						lastAlphaEventTime = ctime;
	    						clearstate = ALPHA_AT_MIN;
	    						fadeAlpha = 0;
	    					} else { 
								//calculate fade
	    						fadeAlphaf = Math.max(0.0f,fadeAlphaf);
	    						fadeAlpha = (int) (fadeAlphaf * clearAlpha);
	    						fadeAlpha = fadeAlpha & 0xff; //jus' to be safe ..
	    					}

	    				} else {
	    					fadeAlpha = 0;
	    					lastAlphaEventTime = ctime;
	    					clearstate = ALPHA_AT_MIN;
	    				}
	    			}

	    			if (clearstate == ALPHA_AT_MIN) fadeAlpha = 0;
	    			if (clearstate == ALPHA_AT_MAX) fadeAlpha = clearAlpha;

	    			MiniPixelTools.fillAlphaChannel( fadeAlpha, tilepixels );

	    			//}
	    		}

	    		switch ( pixelblendmode )
	    		{
	    			case alpha:
	    			{
	    				MiniPixelTools.drawWithAlphaAndOffset(bufferpixels,tilepixels,(int)(flooppos.x*currentlevel) + bufferdimension.width * (int)(flooppos.y*currentlevel) );
	    				break;
	    			}
	    			case max:
	    			{
	    				MiniPixelTools.maxWithAlphaAndOffset(bufferpixels,tilepixels,(int)(flooppos.x*currentlevel) + bufferdimension.width * (int)(flooppos.y*currentlevel) );
	    				break;
	    			}
	    			case min:
	    			{
	    				MiniPixelTools.minWithAlphaAndOffset(bufferpixels,tilepixels,(int)(flooppos.x*currentlevel) + bufferdimension.width * (int)(flooppos.y*currentlevel) );
	    				break;
	    			}
	    			case absoluteSubtract:
	    			{
	    				MiniPixelTools.absSubWithAlphaAndOffset(bufferpixels,tilepixels,(int)(flooppos.x*currentlevel) + bufferdimension.width * (int)(flooppos.y*currentlevel) );
	    				break;
	    			}
	    			case absoluteAdd:
	    			{
	    				MiniPixelTools.absAddWithAlphaAndOffset(bufferpixels,tilepixels,(int)(flooppos.x*currentlevel) + bufferdimension.width * (int)(flooppos.y*currentlevel) );
	    				break;
	    			}
	    			case multiply:
	    			{
	    				MiniPixelTools.multiplyWithAlphaAndOffset(bufferpixels,tilepixels,(int)(flooppos.x*currentlevel) + bufferdimension.width * (int)(flooppos.y*currentlevel) );
	    				break;
	    			}
	    			case lumiDiffMix:
	    			{
	    				MiniPixelTools.lumiDiffMixWithAlphaAndOffset(bufferpixels,tilepixels,(int)(flooppos.x*currentlevel) + bufferdimension.width * (int)(flooppos.y*currentlevel) );
	    				break;
	    			}
	    		}
			
	    		if ((warpon) & (dowarp) & (clearmode))
	    		{
	    			doCurrentWarp();
	    		}
	    		
	    		masterpixel.newPixels(0,0,bufferdimension.width,bufferdimension.height);
			
		} else if (clearmode) {
			int pbxbit = (int) ( ( scalepixelbuffer * bufferdimension.width ) / 2.0f); 
			int pbybit = (int) ( ( scalepixelbuffer * bufferdimension.height ) / 2.0f);
		}
		
		currentparticlenum = (currentparticlenum + 1) % pbuffersize;

		float accx = 0;
		float accy = 0;
		float weight = 0.0f;
		
		for (int i = 1; i < mousebufferlength; i++)
		{
			weight = (float)i/(float)mousebufferlength; //goes from 0.0 to 1.0;
			accx += (weight * (mousebuffer[(i + currmouse) % mousebufferlength].x - mousebuffer[(i-1 + currmouse) % mousebufferlength].x));
			accy += (weight * (mousebuffer[(i + currmouse) % mousebufferlength].y - mousebuffer[(i-1 + currmouse) % mousebufferlength].y));
		}
		
		if ( pixelaccess )
		{
    		copyAlphaAndModifyWithLumi( bufferdimension, alphaFromLumiAmount, pixelAlpha, gradmap, bufferpixels );
    			
			if ( clearmode )
			{
				Image pixelbufferScaled = pixelbuffer;
				
    				if (scalepixelbuffer != 1.0f) 
    					pixelbufferScaled = pixelbuffer.getScaledInstance( (int)( bufferdimension.width * scalepixelbuffer ), (int)( bufferdimension.height * scalepixelbuffer ), Image.SCALE_FAST );
    					
				int pbxbit = (int) ( ( scalepixelbuffer * bufferdimension.width ) / overallresfactor); 
				int pbybit = (int) ( ( scalepixelbuffer * bufferdimension.height ) / overallresfactor);
				
				drawImage( pixelbufferScaled, buffercontext, pixelbufferloc.x + wigglepos.x - pbxbit , pixelbufferloc.y + wigglepos.y - pbybit, bufferdimension, screendimension);
								
				pixelbufferScaled.flush();
			}    			
    	}
		
		int x = (int)( flooppos.x * currentlevel );
	    x = (x < 0) ? -x : x;

	    int y = (int)( flooppos.y * currentlevel );
	    y = (y < 0) ? -y : y;

	    toolrule.setOffset( x, y );
	    toolrule.drawTool( buffercontext );
	    	
		if ( particleson )
		{
			Particle tmparti = null;
			
			// Add a particle at the mouse position for testing
			// Particle tmparti = particluster.activateParticle((float) mouseloc.x, (float) mouseloc.y, (mouseloc.x-bufferdimension.width) * currentlevel,(mouseloc.y-bufferdimension.height) * currentlevel, 0.93f, spin);
			
			float apartx = 0.0f;
			float aparty = 0.0f;
			
			if ( !mousedown )
			{
				apartx = accx*speedmultiplier;
				aparty = accy*speedmultiplier;
			} else {
				apartx = -accx*speedmultiplier;
				aparty = -accy*speedmultiplier;
			}

			// TODO: A atream-emission mode that helps to ensure that no particles are circularly overlapping,
			// To help make topological radially non-overlapping packings of particles for the 'brush' or emission pattern...
			//
			// post-emission interparticle forces
			
			// switch on mouseaction..
			if (true) //(!ctrldown) || (ctrldown && mousedown))
			{
				if ((particlerate >= 1.0f))
				{
					float initNoise = (float)framenumber * particluster.getBaseLFOFreq();
					
					for (int i = 0; i < (int)particlerate; i++)
					{
						//oversample the random attributes:
						//interpolate the emit position from last frame and the velocity

						float interFrame = (float)i/(float)particlerate;

						float partx = interFrame * apartx + (1.0f - interFrame) * lastemitveloc[0];
						float party = interFrame * aparty + (1.0f - interFrame) * lastemitveloc[1];
						
						float noiseX = (float)improvedPerlinNoise.improvedPerlinNoiseThree( initNoise + i * 272.2, 2.5, -8.545 );
						float noiseY = (float)improvedPerlinNoise.improvedPerlinNoiseThree( initNoise + i * 772.822 - 83.332, -92.735, 28.545 ) ;
						
						partx += (float)(noiseX * speedrandom);
						party += (float)(noiseY * speedrandom);
						
						//interpolate the emit position when rate > 1 per frame

						float xstart = (float)interFrame * ((float) mouseloc.x) + (1.0f - interFrame) * ((float) lastemitloc.x);
						float ystart = (float)interFrame * ((float) mouseloc.y) + (1.0f - interFrame) * ((float) lastemitloc.y);
						
						float noiseXx = (float) improvedPerlinNoise.improvedPerlinNoiseThree( initNoise + i * 12.2 + 689.223, 2.5, -28.545 );
						float noiseYy = (float) improvedPerlinNoise.improvedPerlinNoiseThree( initNoise + i * -72.22 + 101.13519, -92.735, -18.545 );
						
						xstart += noiseXx * locrandom;
						ystart += noiseYy * locrandom;
						
						if (interactiveMode > 20)
						{
							xstart += wigglepos.x;
							ystart += wigglepos.y;
						} 
						
						float noiseD = (float) improvedPerlinNoise.improvedPerlinNoiseThree( initNoise + i * -82.222 + 39.1, 52.231, -948.545 );
						float noiseR = (float) improvedPerlinNoise.improvedPerlinNoiseThree( initNoise + i * 35.2 + 978.1, -652.938, -88.45 );
						
						tmparti = particluster.activateParticle(xstart, ystart, partx,party, (float)(drag + noiseD*dragrandom), spin + noiseR*spinrandom, spinprobability, particluster.getParticleStability(), 0,particluster.getPixelSpaceScale(), i);

						if (tmparti != null)
						{
							if (connectEmission)
							{
								if (lastEmittedParticle != null)
								{
									tmparti.setUnidirectionalNeighbor(-1,lastEmittedParticle.myindex,1.0f,particluster,true);
								}
							}
							//still record the last particle whether it's being used or not..
							lastEmittedParticle = tmparti;
						}
					}

					lastemitloc.x = mouseloc.x;
					lastemitloc.y = mouseloc.y;
					
					if (interactiveMode > 10)
					{
						lastemitloc.translate( wigglepos.x, wigglepos.y );
					} 
					
					lastemitveloc[0] = apartx;
					lastemitveloc[1] = aparty;

				} else {
					if (particlerate > 0.0f)
					{
						int inverter = (int)(1.0f/particlerate); //rough but works ok
						if ((framenumber % inverter) == 0)
						{
							float partx = apartx + (float)(randex.nextGaussian()*speedrandom);
							float party = aparty + (float)(randex.nextGaussian()*speedrandom);
							
							
							float xstart = (float) mouseloc.x + (float)(randex.nextGaussian() * locrandom);
							float ystart = (float) mouseloc.y + (float)(randex.nextGaussian() * locrandom);
							
							if (interactiveMode > 20)
							{
								xstart += wigglepos.x;
								ystart += wigglepos.y;
							} 
							
							tmparti = particluster.activateParticle(xstart, ystart, partx,party, (float)(drag + randex.nextGaussian()*dragrandom), spin + randex.nextGaussian()*spinrandom,spinprobability,particluster.getParticleStability(),0,particluster.getPixelSpaceScale(), 1);


							if (tmparti != null)
							{
								if (connectEmission)
								{
									//output("connectEmission is on...");
									if (lastEmittedParticle != null)
									{
										//output("Connecting 2 particles...");
										tmparti.setUnidirectionalNeighbor(-1,lastEmittedParticle.myindex,1.0f,particluster,true);
										//tmparti.setBiDirectionalNeighbor(-1,lastEmittedParticle.myindex,1.0f,particluster,true);
									}
								}
								//still record the last particle whether it's being used or not..
								lastEmittedParticle = tmparti;
							}

						}
					
						lastemitloc.x = mouseloc.x;
						lastemitloc.y = mouseloc.y;
						
						if (interactiveMode > 20)
						{
							lastemitloc.translate( wigglepos.x, wigglepos.y );
						} 
					}
					//0.99 -> emit particle 99 out of every 100 frames
					//0.9 -> emit particle 9 out of every 10 frames
					//0.5 -> emit particle every other frame
					//0.1 -> emit particle every 10 frames
					//0.01 -> emit particle every 100 frames
				}
			}
				//keep a buffer of the particles sorted in time
		    	if ( tmparti != null ) dust[ currentparticlenum ] = tmparti; 
		    	
		    	particluster.update( framenumber );
				if (!particluster.isPixelDrawable())
				{
					particluster.draw( buffercontext, paintmode );
				}
			}
				
			if (particleson & particluster.isPixelDrawable())
	    	{
	    		particluster.drawPixels( bufferpixels );
	    	}

			Vector<EditWidget> removewidgets = new Vector<EditWidget>();

	    	for ( EditWidget awidget : dawidgets )
			{
				if (awidget.isAlive()) awidget.draw( buffercontext );
				else removewidgets.add( awidget );
			}

			// can't remove while iterating without throwing an exception, so doing it this way which is safer in any case
			for ( EditWidget killwidget : removewidgets )
			{
				dawidgets.remove( killwidget );
			}
			
			if (continuoussave) 
			{
				if (saveframespacing > 0)
				{
					if ( (framenumber % saveframespacing) == 0)
					{
						saveBuffer();
					}
				} else {
					saveBuffer();
				}
			}
			
			
			if ( ( continuouspixelgrab ) & ( screenclearalpha > 0) )
			{
				int pbxbit = (int) ( ( bufferdimension.width ) / 2.0f); 
				int pbybit = (int) ( ( bufferdimension.height ) / 2.0f);
				MiniPixelTools.grabPixelRectangle(bufferimage, bufferpixels, pixelbufferloc.x + wigglepos.x - pbxbit, pixelbufferloc.y + wigglepos.y - pbybit, (int)(bufferdimension.width) ,(int)(bufferdimension.height), masterpixel);
			}
			
			if (continuousmapgrab) MiniPixelTools.grabDefaultMapFromPixels(bufferpixels,extractormap,extractormapf,bufferdimension.width,bufferdimension.height);
			if (continuousmapdraw) MiniPixelTools.drawSingleChannelAsRGB(bufferpixels,extractormap,1,0,bufferdimension.width,bufferdimension.height);
			
			if (topologyRegenerating)
			{
				if (topologyGenerationThread != null)
				{
					if (topologyGenerationThread.topologyGenerated)
					{
						output("Topology generation is finished.. copying topology and destroying temporary storage.");
						topologyGenerationThread.copyTopologyAndKillThread( topologylayer );
						topologyGenerationThread.killThread();
						topologyRegenerating = false;
						topologyGenerationThread = null;

						if (interactiveMode > 500)
						{
							generateTopologyFromSettings();
						}
					}
				}
			}
			
			
			if (tileLoadCounter >= 0)
			{
				if (tileLoadCounter == 0) //image queued but may not be finished loading... keep blending the tile until count is reached
				{
					if (iloader != null) //sanity check
					{
						if (iloader.loadedOK) //it's finished, kick off the blending process
						{
							tile = bufferimage.getScaledInstance(bufferdimension.width,bufferdimension.height,Image.SCALE_FAST);
							//tile = bufferimage.getScaledInstance(bufferdimension.width,bufferdimension.height,Image.SCALE_SMOOTH);
							//tile = bufferimage.getScaledInstance(bufferdimension.width,bufferdimension.height,Image.SCALE_AREA_AVERAGING);
							tile.flush();
							//do tile write at tilealpha
							MiniPixelTools.imageMixByScalar( tilepixels, tilepixelsLoading, 0xff-tileLoadAlpha, tilepixels);
							tileLoadCounter = 1;
						}
					}
					
				} else {
					imageCurrentlyLoading = false;
					if (tileLoadAlphaAccum < 256 + 15 * (1 +  0xff- tileLoadAlpha ) )
					{
						if ((tileLoadCounter % tileLoadRate) == 0)
						{
							//output( "doing loaded tile blend " + (tileLoadCounter+1) + " / " + tileLoadAlphaAccum );
							int alph = 0xff-(tileLoadAlpha + tileLoadAlphaAccum/4);
							
							if (alph > 0)
							{
								MiniPixelTools.imageMixByScalar( tilepixels, tilepixelsLoading, alph, tilepixels);
							} else {
								MiniPixelTools.imageMixByScalar( tilepixels, tilepixelsLoading, 0, tilepixels);
								//end the loop
								tileLoadAlphaAccum = 256 + 15 * (1 +  0xff- tileLoadAlpha ) + 16;
							}	
							
							tileLoadAlphaAccum += tileLoadAlpha;
						}
						//do the tile write
						tileLoadCounter++;
						
					} else {
						//tile copy is done
						output("Finished tile blend, which took: " + tileLoadCounter + " frames"); 
						tileLoadCounter = -1;
					}
					
				}
			}
			// y offset here needs attention.. this is probably my-res(1920x1200)-specific!
			context.drawImage(bufferimage, 0, 0, this);
			framenumber++;
		}
	}
	
	public void addWidget( EditWidget e )
	{
		if ( ( e != null ) && ( e.isAlive() ) )
		{
			dawidgets.add( e );
		}
	}
	
	public Point getMousePosition()
	{
		return mouseloc;
	}
	
    // Want to create a pool of threads in advance, that can be updated with new
    // topologylayer/etc where needed, but that can be kicked-off without recreating the thread.
    public void addTopologyProcessorThreads( int nThreads )
    {
        int topoWidth = topologylayer.width;
        int topoHeight = topologylayer.height;

        for ( int ix=0; ix<nThreads; ix++)
        {
            // special case dividing into topoWidth-width boxes
            int thisStartOffset = topoHeight/nThreads * ix * topoWidth;
            int thisScanLength = topoWidth;
            int thisNScans = topoHeight / nThreads;
            int thisStride = topoWidth;
            
            TopologyProcessorThread newThread = new TopologyProcessorThread( topologylayer, bufferpixels, processbuffer, 
                thisStartOffset, thisScanLength, thisNScans, thisStride, ix );
            
            // Seems like the ordering of the next 2 lines might be important one day
            newThread.start();
            topologyProcessorThreads.add( newThread );
        }
    }
    
    // To be used, for example, when the topology gets regenerated!
    public void updateTopologyProcessorThreads()
    {
        synchronized( topologyProcessorThreads )
        {
            Iterator<TopologyProcessorThread> tptIter = topologyProcessorThreads.iterator();
            while ( tptIter.hasNext() )
            {
                TopologyProcessorThread tpt = tptIter.next();
                tpt.setTopologySrcAndDest( topologylayer, bufferpixels, processbuffer );
            }
        }
    }
    
	public void doCurrentWarp()
	{
		for (int i = 0; i < 1 + topologyoversampling; i++)
		{
			switch ( topologyNextFrameMode )
			{
				// Queue topologyprocessor threads to process sub-frame buckets of topology,
				// potentially also clobber/kill the current topologyprocessor queue if there's still things pending
				// then just wait for all buckets to be finished... 
				// should be fast-ish I hope (at least faster than single-threaded) if there's enough threads!?
                //
                //
				// Add blackpoint and scaling and maybe whitepoint and power, to all nextFrame modes.
                // or just multiplier/blackPt.
                
				case average :
				{
					topologylayer.nextFrameInlineBlend( bufferpixels, processbuffer, warpwithalpha, extractormap );
					break;
				}
                case averageThreaded :
                {       
                    // System.out.println( "workin' at all!" );
                    // create N threads (new Runnable) to do subsections (now happens earlier)
                    // and wake the threads by modifying isFinished.
                    // topologylayer.nextFrameInlineBlendSubSection( bufferpixels, processbuffer, 
                    // warpwithalpha, extractormap, startOffset, scanLength, nScans, stride );
                    //
                    // initial splits could be startOffsets at 1/numThreads, nScans at totalScans / nThreads
                    // with scanLength at the whole width
                    
                    // gotta synchronize while iterating this list,
                    // we are going to be modifying the thread objects!
                    synchronized( topologyProcessorThreads )
                    {
                        Iterator<TopologyProcessorThread> tptIter = topologyProcessorThreads.iterator();
                        while ( tptIter.hasNext() )
                        {
                            TopologyProcessorThread tpt = tptIter.next();
							tpt.setMode( TopologyProcessorThread.TopologyProcessorMode.Average );
                            // don't queue if already running
                            if ( tpt.isFinished )
                            {
                                tpt.setUnFinished();
                                tpt.interrupt();
                            }
                        }
                    }
                    // we need to not-queue any duing this time
                    waitForAllTopologyProcessorThreads();
                    
                    // System.out.println("Finished Waiting for all topology processor threads!");
                    
                    //reInit now, as they alllllll finistzd (finished, Sic.)
                    // topologyProcessorThreads = new Vector<TopologyProcessorThread>();
                    
                    break;
                }
                case contrasty :
                {
					topologylayer.nextFrameContrastyBlend( bufferpixels, processbuffer,warpwithalpha,extractormap );
					break;
				}
				case contrastyThreaded :
                {       
                    // gotta synchronize while iterating this list,
                    // we are going to be modifying the thread objects!
                    synchronized( topologyProcessorThreads )
                    {
                        Iterator<TopologyProcessorThread> tptIter = topologyProcessorThreads.iterator();
                        while ( tptIter.hasNext() )
                        {
                            TopologyProcessorThread tpt = tptIter.next();
							tpt.setMode( TopologyProcessorThread.TopologyProcessorMode.Contrasty );
                            // don't queue if already running
                            if ( tpt.isFinished )
                            {
                                tpt.setUnFinished();
                                tpt.interrupt();
                            }
                        }
                    }
                    // we need to not-queue any duing this time
                    waitForAllTopologyProcessorThreads();
                    break;
                }
				case averageRule :
				{
					topologylayer.nextFrameInlineBlendWithRule(bufferpixels,processbuffer,toolrule.getFloatHarvest(), toolrule.getColorHarvest(), false, 1.0f, warpwithalpha,extractormap);
					break;
				}
				case averageRuleColor :
				{
					topologylayer.nextFrameInlineBlendWithRule(bufferpixels,processbuffer,toolrule.getFloatHarvest(), toolrule.getColorHarvest(), true, 1.0f, warpwithalpha, extractormap);
					break;
				}
				case absoluteAdd:
				{
					topologylayer.nextFrameInlineAbsAdd(bufferpixels,processbuffer,warpwithalpha,extractormap);
					break;
				}
				case lumiDifference :
				{
					topologylayer.nextFrameInlineAnotherLumiDiffMix(bufferpixels,processbuffer,warpwithalpha,extractormap);
					break;
				}
				case lumiDifferenceThreaded :
				{
					synchronized( topologyProcessorThreads )
                    {
                        Iterator<TopologyProcessorThread> tptIter = topologyProcessorThreads.iterator();
                        while ( tptIter.hasNext() )
                        {
                            TopologyProcessorThread tpt = tptIter.next();
							tpt.setMode( TopologyProcessorThread.TopologyProcessorMode.LumiDiff );
                            // don't queue if already running
                            if ( tpt.isFinished )
                            {
                                tpt.setUnFinished();
                                tpt.interrupt();
                            }
                        }
                    }
                    // we need to not-queue any duing this time
                    waitForAllTopologyProcessorThreads();
					break;
				}
				case rule :
				{
					topologylayer.nextFrameWithLife(bufferpixels, processbuffer, toolrule.getFloatHarvest(), toolrule.getColorHarvest(), 1, 0, 1, warpwithalpha, extractormap, false, false);
					break;
				}
				case ruleColor :
				{
					topologylayer.nextFrameWithLife(bufferpixels, processbuffer, toolrule.getFloatHarvest(), toolrule.getColorHarvest(), 1, 0, 1, warpwithalpha, extractormap, false, true);
					break;
				}
				case ruleTransfer:
				{
					topologylayer.nextFrameWithLife(bufferpixels, processbuffer, toolrule.getFloatHarvest(), toolrule.getColorHarvest(), 1, 0, 1, warpwithalpha, extractormap, true, false);
					break;
				}
				case ruleTransferColor:
				{
					topologylayer.nextFrameWithLife(bufferpixels, processbuffer, toolrule.getFloatHarvest(), toolrule.getColorHarvest(), 1, 0, 1, warpwithalpha, extractormap, true, true);
					break;
				}
			
				case ruleLifeTransfer:
				{
					topologylayer.nextFrameWithNewLife(bufferpixels, processbuffer, toolrule.getFloatHarvest(), toolrule.getColorHarvest(), 1, 0, 1, warpwithalpha, extractormap, true, false);
					break;
				}
				case ruleLifeTransferColor:
				{
					topologylayer.nextFrameWithNewLife(bufferpixels, processbuffer, toolrule.getFloatHarvest(), toolrule.getColorHarvest(), 1, 0, 1, warpwithalpha, extractormap, true, true);
					break;
				}
				
				case ruleAverageDifference:
				{
					topologylayer.nextFrameAverageDifferenceRule( bufferpixels, processbuffer, toolrule.getFloatHarvest(), 1.0f, warpwithalpha, extractormap );
					break;
				}
				
				case ruleRD :
				{
					topologylayer.nextFrameWithLife(bufferpixels, processbuffer, toolrule.getFloatHarvest(), toolrule.getColorHarvest(), 1, 0, ( ( framenumber + i) % 2 == 0) ? 1 : -1, warpwithalpha, extractormap, false, false);
					break;
				}
				case ruleColorRD :
				{
					topologylayer.nextFrameWithLife(bufferpixels, processbuffer, toolrule.getFloatHarvest(), toolrule.getColorHarvest(), 1, 0, ( ( framenumber + i ) %2 == 0) ? 1 : -1, warpwithalpha, extractormap, false, true );
					break;
				}
				case ruleTransferRD:
				{
					topologylayer.nextFrameWithLife(bufferpixels, processbuffer, toolrule.getFloatHarvest(),toolrule.getColorHarvest(), 1, 0, ( ( framenumber + i) % 2 == 0) ? 1 : -1, warpwithalpha, extractormap, true, false);
					break;
				}
				case ruleTransferColorRD:
				{
					topologylayer.nextFrameWithLife(bufferpixels, processbuffer, toolrule.getFloatHarvest(), toolrule.getColorHarvest(), 1, 0, ( ( framenumber + i) % 2 == 0) ? 1 : -1, warpwithalpha, extractormap, true, true);
					break;
				}
				case ruleAverageDifferenceRD:
				{
					topologylayer.nextFrameAverageDifferenceRule(bufferpixels, processbuffer, toolrule.getFloatHarvest(), ( ( framenumber + i ) % 2 == 0) ? 1.0f : -1.0f, warpwithalpha, extractormap );
					break;
				}
				
				case RGBrule:
				{
					topologylayer.nextFrameWithRGBLifeRule(bufferpixels, processbuffer, toolrule.getFloatHarvest(), toolrule.getColorHarvest(),  -192, 2, warpwithalpha, extractormap, false);
					break;
				}
				case RGBruleRD :
				{
					topologylayer.nextFrameWithRGBLifeRule(bufferpixels, processbuffer, toolrule.getFloatHarvest(), toolrule.getColorHarvest(), ( ( framenumber + i ) % 2 == 0) ? -192 : 192, ( ( framenumber + i ) % 2 == 0) ? 2 : -2, warpwithalpha, extractormap, true);
					break;
				}
				case nextFrameRuleMaxMin :
				{
					topologylayer.nextFrameRuleMaxMin( bufferpixels, processbuffer, toolrule.getFloatHarvest(), 1.0f, warpwithalpha, extractormap, false);
				}
				case nextFrameRuleMaxMinTransfer :
				{
					topologylayer.nextFrameRuleMaxMin( bufferpixels, processbuffer, toolrule.getFloatHarvest(), 1.0f, warpwithalpha, extractormap,true );
				}
			
			}
			System.arraycopy( processbuffer, 0, bufferpixels, 0, processbuffer.length );
		}
	}
	
	public int getPixel(Point address2D, int[] array, int width, int height)
	{
		int xbit = address2D.x % width;
		int ybit = address2D.y % height;
		if (xbit < 0) xbit += width;
		if (ybit < 0) ybit += height;
		return array[xbit+ybit*width];
	}
	
	public void copyAlphaAndModifyWithLumi( Dimension bufdimension, int alphaFromLumi, int alphamult, int[] alphasrc, int[] destbuffer )
	{
		for (int i=0; i<bufdimension.height * bufdimension.width; i++)
		{
			int pixel = (destbuffer[ i ] & 0x00ffffff);	
			int lumi = ( ( (pixel & 0xff) + ( (pixel >> 8) & 0xff )  + ( (pixel >> 16) & 0xff) ) / 3 ) & 0xff;
			
			if ( alphaFromLumi <= 0xff )
			{
				//when alphaFromLumi is zero, this part should return 1
				// 1 -  ( (1-lumi) * alphaFromLumi )
				lumi = 0xff -  ( ( (0xff - lumi) * alphaFromLumi ) >> 8) ;
			} else {
				//otherwise, enhance the lumi-from-alpha effect by the amount
				//do this by recentering the lumi on 0.5
				//and expanding in fixed point using alphaFromLumi..
				//may need to clamp here rather than mask.. not sure..
				lumi = ( ( (   ( lumi - 0x88)  *  alphaFromLumi   )  + 0x88 ) >> 8 ) & 0xff;
			}
			if (lumi > 0xff) lumi = 0xff;
			
			lumi = ( ( ( alphasrc[i] * lumi ) >> 8 ) * alphamult ) >> 8;
			
			if (lumi > 0xff) lumi = 0xff;
			
			destbuffer[i] = pixel + ( lumi  << 24 );
		}
	}
	
	public void regenerateAlphaWithPower( float power )
	{
		setAlphaToGrad( bufferdimension, power, gradmap );
	}
	
	public void  setAlphaToGrad( Dimension bufdimension, float gradpower, int[] alphabuffer)
	{
		for (int h=0; h < bufdimension.height; h++)
		{
			for (int w=0; w < bufdimension.width; w++)
			{
				//alpha test .. didn't work ... yet! :D
				int i = w + h * bufdimension.width;
				
				int bdhh = bufdimension.height / 2;
	    			int bdhw = bufdimension.width / 2;
	    			
				int sqh = (bdhh - h);
				int sqw = (bdhw - w);
				sqh *= sqh;
				sqw *= sqw;
				
				//a quick bit of mostly fixed point...
				double p = Math.max( 0,  1.0 - Math.sqrt( sqh + sqw ) / ( Math.min( bufdimension.width / 2, bufdimension.height / 2 ) ) );
				p = Math.pow(p, gradpower);
				p *= (double) 0xff;
				int rolloff = ((int) p ) & 0xff;
							    	
				alphabuffer[ i ] = rolloff ;
			}
		}
	}
	
	public void drawtile(Image tilex, Graphics2D g, int x, int y, int tw, int th, int xdraw, int ydraw) //assumes tilex is samesize as context
	{
		Point dapoint = new Point(x % tw , y % th);
		if (dapoint.x < 0) dapoint.translate(tw,0);
		if (dapoint.y < 0) dapoint.translate(0,th);
		int xScreenOffs = (screendimension.height/2 - bufferdimension.height/2);
		int yScreenOffs = (screendimension.width/2 - bufferdimension.width/2);
	    
		if ((tilex != null) && (tilex.getHeight(this) > 0) && (tilex.getWidth(this) > 0))
		{
			if ((x == 0) && (y == 0))
			{ g.drawImage(tilex, xdraw, ydraw, this); }
			else
			{
				/*
				  do this to the image:
				  a|b    d|c
				  -+- -> -+-
				  c|d    b|a   where the + is Point looppos

				  drawimage(img, dest rect, source rect)

				  tw is the tilewidth
				  th is the tileheight
				*/

				//let the java.awt.Graphics clipper do the clipping for now

				//draw d:
				java.awt.geom.AffineTransform a = g.getTransform();
				g.translate( pixelbufferloc.x, pixelbufferloc.y );
				g.drawImage(tilex,     0,     0,tw - dapoint.x,th - dapoint.y,dapoint.x,dapoint.y,tw,th,this);
				g.drawImage(tilex,tw - dapoint.x,     0,    tw,th - dapoint.y,0,dapoint.y, dapoint.x,th,this);
				g.drawImage(tilex,     0,th - dapoint.y,tw - dapoint.x,    th,dapoint.x,0,tw, dapoint.y,this);
				g.drawImage(tilex,tw - dapoint.x,th - dapoint.y,    tw,    th,0,0, dapoint.x, dapoint.y,this);
				g.setTransform( a );
			}
		}
	}
	
	public void drawImage(Image tilex, Graphics2D g, int x, int y, Dimension tiledimension, Dimension clipSpace)
	{
		int tw = tiledimension.width;
		int th = tiledimension.height;
		
		Point dapoint = new Point(x % clipSpace.width , y % clipSpace.height );
		
		if (dapoint.x < 0) dapoint.translate( clipSpace.width, 0 );
		if (dapoint.y < 0) dapoint.translate( 0, clipSpace.height );		
		
		if (tilex != null)
		{
			if ( (tw > 0) && (th > 0) )
			{
				g.drawImage(tilex, dapoint.x, dapoint.y, this);
					
			}
		}
	}
    
    public boolean allTopologyProcessorThreadsFinished()
    {
        for ( TopologyProcessorThread tpt : topologyProcessorThreads )
        {
            if ( !tpt.isFinished )
            {
                return false;
            }
        }
        // alllllll done!
        return true;
    }
    
    //This is for calling in systemWideExit, so that the image writes get a chance to finish.
    public void waitForAllTopologyProcessorThreads()
    {
        long startTime = System.currentTimeMillis(); //fetch starting time thx2 Ankit Rustagi on stackExchange!
        
        // Exit if more than 5 seconds have elapsed, that *should* hopefully be more than enough.
        // There are some assumptions being made here but the threaded-image-save system is going to be bad
        // anyway if the saving can't keep up with the generation (ie use lotsa ram?)
        while( (!allTopologyProcessorThreadsFinished()) && ( System.currentTimeMillis() - startTime ) < 5000 ) 
        {
            // clear finished ones, hopefully ensuring the vector will eventually be empty..
            // Might eventually need to clear crashed-ones too.. just need to know the failure-modes.
            // But should plan for at least write-permission not-granted and/or disk-full.
            // cleanTopologyProcessorThreads();
            //try{
                // mili-nap
                //Thread.currentThread().sleep(1);
                // nano-nap
                //Thread.currentThread().sleep(0l,100);
                // let 'em at it!?
                
       		//}
            //catch(InterruptedException ie) { }
            Thread.currentThread().yield();
        }
    }
    
    public void cleanSaveThreads()
    {
        //output( "The number of image save threads before removing finished ones, is:" + imageSaveThreads.size() );
        Vector<ImageSaveThread> removeFinishedThreads = new Vector<ImageSaveThread>();
        
        synchronized( imageSaveThreads ) 
        {
            Iterator istIter = imageSaveThreads.iterator();
            while( istIter.hasNext() )
            {
                
                
                ImageSaveThread ist = (ImageSaveThread) istIter.next();
                if ( ist != null )
                {
                    if ( ist.isFinished )
                    {
                        output( "Queueing removal of thread from thread-pool of size: " + imageSaveThreads.size() );
                        removeFinishedThreads.add( ist );
                    }
                }
            }
        }
        
        for ( ImageSaveThread ist : removeFinishedThreads )
        {
            imageSaveThreads.remove( ist );
        }
    }
    
    public void waitForAllImageSaveThreads()
    {
        long startTime = System.currentTimeMillis(); //fetch starting time thx2 Ankit Rustagi on stackExchange!
        
        // Exit if more than 8 seconds have elapsed, that *should* hopefully be more than enough.
        // There are some assumptions being made here but the threaded-image-save system is going to be bad
        // anyway if the saving can't keep up with the generation (ie use lotsa ram?)
        while( (imageSaveThreads.size() > 0) && ( System.currentTimeMillis() - startTime ) < 8000 ) 
        {
            // clear finished ones, hopefully ensuring the vector will eventually be empty..
            // Might eventually need to clear crashed-ones too.. just need to know the failure-modes.
            // But should plan for at least write-permission not-granted and/or disk-full.
            cleanSaveThreads();
        }
    }
    
    // Made the saving happen in another thread!!
    //
    // We still probably need to wait for this BufferedImage (indeed, turned out to be true!)
    // but the actual save could definitely happen in a separate thread. - DONE
    //
    // This also leads to partially-written frames when you quit in the
    // middle of saving.. so need to make quit wait for all the threads to finish! - DONE!
    //
    // Doing the bufferedImage part in the separate thread led to partial images.. so moved it back!
    // To allow it, we could synchronize a lock on the obj till the draw is complete.
    //
	public synchronized boolean saveBuffer()
	{
		//try {
        BufferedImage bi;

        //if ( saveWhichBuffer == 0 )
        //{
        //    bi = new BufferedImage( screendimension.width, screendimension.height, BufferedImage.TYPE_INT_RGB );
        //    bi.setAccelerationPriority(1.0f);
        //    Graphics2D big = bi.createGraphics();
        //    big.drawImage( bufferimage, 0, 0, this );
        //    bufferimage.flush();
        //} else /*if (saveWhichBuffer == 1)*/ {
        //    bi = new BufferedImage( bufferdimension.width, bufferdimension.height, BufferedImage.TYPE_INT_RGB );
        //    bi.setAccelerationPriority(1.0f);
        //    Graphics2D big = bi.createGraphics();
        //    big.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC ) );
        //    big.drawImage( pixelbuffer, 0, 0, this );
        //    pixelbuffer.flush();
        //}

        NumberFormat numf = new DecimalFormat( "000000" );
        String imagesavename = savename + "." + numf.format( saveincrement ) + "." + saveFormatStr;
        output( "Saving buffer to file: " + imagesavename );
        // bi.flush();
        
        int saveWidth,saveHeight;
        Image imageToSave;
        boolean saveAlpha;
        
        if ( saveWhichBuffer == 0 )
        {
            saveWidth = screendimension.width;
            saveHeight = screendimension.height;
            imageToSave = bufferimage;
            saveAlpha = false;
        } else {
            saveWidth = bufferdimension.width;
            saveHeight = bufferdimension.height;
            imageToSave = pixelbuffer;
            saveAlpha = true;
        }
        
        bi = new BufferedImage( saveWidth, saveHeight, BufferedImage.TYPE_INT_RGB );
        bi.setAccelerationPriority(1.0f);
        
        Graphics2D big = bi.createGraphics();
        if ( saveAlpha ) 
        {
            big.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC ) );
        }
        
        big.drawImage( imageToSave, 0, 0, this );
        imageToSave.flush(); //shouldn't it be the Graphics2D that gets flushed?? hurrr
        
        ImageSaveThread saveThread = new ImageSaveThread( imagesavename, bi );
        // do this in the BACKGROUND!
        saveThread.setPriority(Thread.MIN_PRIORITY);
        saveThread.start();
        
        if ( imageSaveThreads.size() > 0 )
        {   
            cleanSaveThreads();
        }
        imageSaveThreads.add( saveThread );
        
        //File file = new File( imagesavename );
        //FileOutputStream out = new FileOutputStream( file );

        // Updated to newer, ImageIO-based way of writing..
        //ImageIO.write( bi, saveFormatStr, out );

        saveincrement += 1;
            
		//} catch (java.io.IOException iox) {
	
			//save fuxx
		//	System.err.println("IOException while trying to write image file");
		//	return false;
		//}
	
		//assume save went ok...
		return true;
	}
	
	public void generateTopologyFromSettings( )
	{
		if ( !topologyRegenerating )
		{
			topologyRegenerating = true;	
			int numpoints = (multipointfromparticle) ? particluster.getNumActiveParticles() : numcentersmultipoint; //enhance numcenters a bit
			
			output( ">>>>>>>>>>>>> numpoints: " + numpoints );
			
			if (numpoints == 0) // couldn't get any particle? fix this
			{
				numpoints = numcentersmultipoint;
				multipointfromparticle = false;
			}
			float xs[] = new float[numpoints];
			float ys[] = new float[numpoints];
			double levels[] = new double[numpoints];
			double powers[] = new double[numpoints];
			
			if (multipointfromparticle)
			{
				particluster.getParticlePoints(xs,ys);
			} else {
				for (int i = 0; i < numpoints; i++)
				{
					xs[i] = Math.abs((randex.nextFloat()*bufferdimension.width) % bufferdimension.width);
					ys[i] = Math.abs((randex.nextFloat()*bufferdimension.height) % bufferdimension.height);
				}
			}
			
			for (int i = 0; i < numpoints; i++)
			{
				// TODO: Provide more control ovr this rand!
				levels[i] = ((randex.nextDouble()-0.5f)*2.0f) * currenttopologyparam;
				powers[i] = currenttopologypower;
			}
			
			TopologyGenerationSettings tgs = new TopologyGenerationSettings( bufferdimension.width, bufferdimension.height, currenttopologyparam, xs, ys, mouseloc.x, mouseloc.y, powers, levels, generatetopologymapped, numpoints, (mouseloc.x - pixelbufferloc.x) / scalepixelbuffer, (mouseloc.y -  pixelbufferloc.y) / scalepixelbuffer, numwarplayers, nextbooleanmatrixsize, currentbooleanmatrix, topologygeneratormode, topologyrendermode, extractormapf, topologysquishvalue );
								
			//TopologyGenerationThread 
			topologyGenerationThread = new TopologyGenerationThread( tgs, bufferpixels, this );
			output("Starting topologyGeneration thread");
			//Thread tgThread = new Thread( topologyGenerationThread );
			
			try {
				topologyGenerationThread.start();
			}
			catch (Exception e)
			{
				output( e.toString() );
				//System.exit(0); // seems a little harsh!
			}
			output("TopologyGeneration Thread started..");
		} else {
			
			output("tried to queue topology generation when topology was already regenerating.. trying to kill thread (then give it another go).... .. ..");
			
			topologyGenerationThread.killThread();
			topologyRegenerating = false;
			topologyGenerationThread = null;
		}
	}
	
	public void setSaveString(String towhat)
	{
		savename = towhat;
	}
	
	public void setDoWarp(boolean towhat)
	{
		if (warpon)
		{
			dowarp = towhat; 
		} else {
			dowarp = false; 
		}
	}

	public void setWarpWithAlpha(boolean towhat)
	{
		if (extractormapf != null) warpwithalpha = towhat;
		else warpwithalpha = false;
	}
	
	public void setGenerateTopologyMapped(boolean towhat)
	{ generatetopologymapped = towhat; }

	public void setMultipointFromParticle(boolean towhat)
	{ multipointfromparticle = towhat; }
	
	public void setDebug(boolean towhat)
	{ debug = towhat; }
	
	public void toggleClearMode()
	{ clearmode = !clearmode; }
	
	public void togglePixelAccess()
	{ pixelaccess = !pixelaccess; }	
	
	public void setClearMode(boolean towhat)
	{ clearmode = towhat; }
	
	public void setScreenClearAlpha( float towhat)
	{ screenclearalpha = towhat; }
	
	public void setConnectEmission(boolean towhat)
	{ connectEmission = towhat; }
	
	public void setContinuousSave(boolean doit)
	{ continuoussave = doit; }

	public void setSaveFrameSpacing(int to)
	{ saveframespacing = to; }

	public void setContinuousMapGrab(boolean doit)
	{ continuousmapgrab = doit; }

	public void setContinuousMapDraw(boolean doit)
	{ continuousmapdraw = doit; }

	public void setContinuousPixelGrab(boolean doit)
	{ continuouspixelgrab = doit; }

	public void killParticles(boolean doit)
	{ if (doit) particluster.killAllParticles(); }
	
	public void killAllParticles()
	{ particluster.killAllParticles(); }
	
	//float setMethods( float towhat )
	
	public void setWiggleJumpFrameRate( int towhat )
	{ wiggleJumpFrameRate = towhat; }
	
	public void setPixelWiggleAmp( float towhat )
	{ pixelWiggleAmp.setLocation( towhat, towhat ); }
	
	public void setSpeedNoiseAmp( float towhat )
	{ speedNoiseAmp = towhat; }
	
	public void setPixelRotNoiseFreq( float towhat )
	{ pixelRotNoiseFreq = towhat; }
	
	public void setScalePixelBuffer( float towhat )
	{ scalepixelbuffer = Math.max(1,towhat); }
	
	public void setSpeedMultiplier(float towhat)
	{ speedmultiplier = towhat; }

	public void toggleParticlesOn( )
	{ 
		particleson = !particleson; 
		output("Particles are now " + particleson );
	}
	
	public void setParticlesOn(boolean towhat)
	{ particleson = towhat; }
	
	public void setParticleRate(float towhat)
	{ particlerate = towhat; }
	
	public void setAudioAmpSize( float towhat )
	{ audioAmpSize = towhat; }
	
	public void setDragLevel(float towhat)
	{ drag = towhat; }

	public void setDragRandom(float towhat)
	{ dragrandom = towhat; }
	
	public void setFloatRuleArrayInMiniPixelTools( )
	{ MiniPixelTools.setFloatRuleArray( toolrule.getFloatHarvest() ); }
	
	public int toint( boolean of )
	{
		if (of) return 1; else return 0;
	}
	
	//print this assumedly square mofo..
	public void setCurrentBooleanMatrix(boolean[] towhat)
	{
		currentbooleanmatrix = towhat;
		int sidelength = (int) Math.sqrt( towhat.length );
		
		if (debug)
		{
			System.out.println("Set " + sidelength + "x" + sidelength + " boolean matrix: ");
			if (sidelength*sidelength <= towhat.length )
			for (int i = 0; i < sidelength; i++)
			{
				for (int j = 0; j < sidelength; j++)
				{	
					if (j < sidelength-1) {
						System.out.print(toint(towhat[ j + i * sidelength ] ) + "|");
					} else{
						System.out.println( toint(towhat[ j + i * sidelength ] ) );
					}
				}
				
				if (i < sidelength-1) 
				{
					for (int k = 0; k < sidelength; k++)
					{	
						if (k < sidelength-1) System.out.print( "-+" );
						else System.out.println( "-" );
					}
				}
				
			}
			System.out.println("");
		}
	}
	
	public void setSpeedNoiseFreq( float towhat )
	{ speedNoiseFreq = towhat; }
	
	public void setCurrentTopologyParam(float towhat)
	{currenttopologyparam = towhat;}

	public void setTopologySquishValue( float towhat )
	{ topologysquishvalue = towhat; }
	
	public void setCurrentTopologyPower(float towhat)
	{ currenttopologypower = towhat; }
	
	public void setCurrentSpin(float towhat)
	{ spin = towhat; }

	public void setSpinRandom(float towhat)
	{ spinrandom = towhat; }

	public void setSpinProbability(float towhat)
	{ spinprobability = towhat; }

	public void setLocRandom(float towhat)
	{ locrandom = towhat; }

	public void setSpeedRandom(float towhat)
	{ speedrandom = towhat; }
	
	void copyBufferPixelsToTilePixels()
	{
		if (tilepixels.length == bufferpixels.length)
		{
			System.arraycopy(bufferpixels, 0, tilepixels, 0, tilepixels.length);
			output("copied bufferpixels to refresh tile");
		} else {
			output("could not copy buffer to refresh tile.. array length mismatch.");
		}
	}
	
	public void setCurrentLevel( float towhat )
	{
        // Correct the loopPos with respect to currentLevel
        flooppos.scale( currentlevel / towhat );
            
		float prevcurrentlevel = currentlevel;
        currentlevel = towhat;
		output("currentLevel is now: " + currentlevel );
	}
	
	public void addToCurrentLevel( float towhat )
	{ 
		currentlevel += towhat; 
		output("currentLevel is now: " + currentlevel );
	}
	
	public void multiplyCurrentLevel( float by )
	{ 
		currentlevel *= by; 
		output("currentLevel is now: " + currentlevel );
	}
	
	//public void halveCurrentLevel( )
	//{ currentlevel *= 0.5f; }
	
	public void powCurrentLevel( float pow )
	{ 
		currentlevel = (currentlevel >= 0) ? (float)Math.pow( currentlevel, pow ) : (float)-Math.pow( -currentlevel, pow ); 
		output("currentLevel is now: " + currentlevel );
	}
	
	//public void sqrtCurrentLevel( )
	//{ currentlevel = (float) ( ( currentlevel < 0 ) ? -Math.sqrt( -currentlevel ) : Math.sqrt( currentlevel ) ); }
	
    
	//int setMethods( int towhat )
	
	public void setMouseCursorModeFromInt( int towhat )
	{
        //output( "mcm string is: " + towhat );
		// MouseCursorMode mcm = MouseCursorMode.fromInteger( towhat );
		setMouseCursorMode( MouseCursorMode.values()[towhat] );
	}
	
	public void setMouseCursorMode( MouseCursorMode towhat )
	{ 
		mousecursormode = towhat;
		
		switch( mousecursormode )
		{
			case defaultSystem :
			{
				//output("Resetting to default mouse cursor.");
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				break;
			}
			case crosshair :
			{ 
				//output("Setting crossHair mouse cursor");	
				this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				break;
			}
			case hand : 
			{
				//output("Setting hand mouse cursor, HELLO THERE!! ;)");
				this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				break;
			}
			case hidden :
			{
				//output("Setting hidden mouse cursor");
				Toolkit defaulttoolkit;
				try {
					GraphicsEnvironment Ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
					GraphicsDevice[] Gs = Ge.getScreenDevices();
					GraphicsConfiguration gConfig = null;

					for(int i = 0; i < Gs.length; i++)
					{
						//output("GraphicsDevice[" + i + "] = " + Gs[i]);
						GraphicsConfiguration[] Gcs = Gs[0].getConfigurations();
						//just get the first one?? dunno...
						if (Gcs.length > 0) gConfig = Gcs[0];
					}

					if (gConfig != null)
					{
						VolatileImage hiddenImage = gConfig.createCompatibleVolatileImage( 1, 1, Transparency.BITMASK );
						Graphics2D volatileGraphics = hiddenImage.createGraphics();
						volatileGraphics.setBackground( new Color( 128, 128, 128, 0) );
						volatileGraphics.clearRect(0,0,1,1);
						defaulttoolkit = this.getToolkit().getDefaultToolkit();
						Cursor hiddenCursor =  defaulttoolkit.createCustomCursor( hiddenImage ,new Point(0,0),"hidden");;
						this.setCursor( hiddenCursor );
					}
				}
				catch (AWTError awe) {output("Error getting toolkit"); }
				catch (SecurityException sx) {output("Security problem getting system graphics toolkit.");}
				catch (IndexOutOfBoundsException iox) { }
				catch (HeadlessException hex) { output("Could not set mouse cursor because system appears to be headless!. so watch out! :P"); }
				break;
			}
		}
	}
	
	public void setAlphaFromLumiAmount( int towhat )
	{
		alphaFromLumiAmount = towhat & 0xff;
	}
	
	public void setPixelAlpha( int towhat )
	{
		pixelAlpha = Math.max( 0, Math.min( towhat, 0xff ) );
	}
	
	public void setSaveWhichBuffer( int towhat )
	{
		saveWhichBuffer = (towhat <= 0) ? 0 : 1;
	}
	
	public void postTabMenu( int selection )
	{
		System.out.println("temporary tab menu callback");
	}
	
	public void setTopologyGeneratorMode( int towhat )
	{
		topologygeneratormode = Topology.TopologyGeneratorMode.values()[ towhat ];
	}
		
	public void setTopologyOversampling( int towhat )
	{
		topologyoversampling = towhat;
	}
	
	public void setTopologyNextFrameMode( int towhat )
	{
        output( "topologyNextFrameMode set to: " + topologyNextFrameMode );
		topologyNextFrameMode = Topology.TopologyBlendMode.values()[ towhat ];
	}
	
	public void setPixelBlendMode( int towhat )
	{ pixelblendmode = PixelBlendMode.values()[towhat]; }
	
	public void setPaintMode(int towhat)
	{ 
		paintmode = ParticlePaintMode.values()[towhat]; 
		if ( paintmode == ParticlePaintMode.particleTrace ) { killParticles( true ); }
	}
	
	public void setTopologyRenderMode( int towhat )
	{
		topologyrendermode = Topology.TopologyRenderMode.values()[towhat];
		output("Set topology render mode to: " + topologyrendermode);
	}
	
	public void setClearAlpha( int towhat )
	{
		clearAlpha = towhat; 
	}
	
	public void setTileLoadAlpha( int towhat )
	{
		tileLoadAlpha = (towhat <= 0) ? 1 : (towhat > 0xff) ? 0xff : towhat;
	}
	
	public void setInteractiveMode(int towhat)
	{
		interactiveMode = towhat;
	}
	
	public void setNumCentersMultiPoint(int towhat)
	{
		numcentersmultipoint = towhat;
	}
	
	public void systemWideExitWhenTrue( boolean whentrue )
	{
		if ( whentrue )
		{
			systemWideExit();
		}
	}
    
	public void systemWideExitAfterNFrames( int nframes )
    {
        // currentFrame = etc;
        // exitFrame = currentFrame + nframes;
        // set a global exitframe at which systeam exit occurs.
    }
    
	//very trusting to have this as a public method.. but anyway..
	public void systemWideExit()
	{
		output("Tyler exiting!");
        output("Waiting for other threads to end...");
        waitForAllImageSaveThreads();
        output("Bye!");
		System.exit(0); 
	}
	
	public void tileFadeToggle()
	{
		if ((clearstate == ALPHA_FADING_DOWN) || (clearstate == ALPHA_AT_MIN))
		{
			long ctime = (new java.util.Date()).getTime();

			if (clearstate == ALPHA_FADING_DOWN)
			{
				lastAlphaEventTime = ctime;
			} else {
				lastAlphaEventTime = ctime;
			}
			
			clearstate = ALPHA_FADING_UP;
			output("Fading Alpha UP");

		} else if ((clearstate == ALPHA_FADING_UP) || (clearstate == ALPHA_AT_MAX)) {

			//project the linear fade backwards in time to give the appropriate fade level in the other direction..
			long ctime = (new java.util.Date()).getTime();

			if (clearstate == ALPHA_FADING_UP)
			{
				lastAlphaEventTime = ctime;
			} else {
				lastAlphaEventTime = ctime;
			}

			clearstate = ALPHA_FADING_DOWN;
			output("Fading Alpha DOWN");
		}
	}
	
	public void nextImage()
	{
		currentimage = (currentimage + 1) % numimages;						
		output("loading image " + currentimage);
		loadImage( imagenames[currentimage] );
	}
	
	public void pickNextImage()
	{
		dawidgets.add( new EditWidget( EditWidget.widgetType.EnumValue,"loadImage",this, (Object[]) imagenames,
									   mouseloc.x, mouseloc.y, EditWidget. widgetDrawMode.Default, 
                 					   widgetresultscale, bgcolor, midcolor, fgcolor,  (Component) this) );
	}
	
	//public void gestureMovementRecognized( String currentGesture ) 
	//{
    //	System.out.println( "got gesture: " + currentGesture );
    //}
	
	//public void processGesture( String gesture )
	//{
    //	try {
	//        Thread.sleep(200);
	//    } catch ( InterruptedException e ) {}
	//	
	//    System.out.println( "finished gesture: " + gesture );
    //}
	
	public void mouseClicked(MouseEvent e)
	{
		int mods = e.getModifiers();
		
		boolean ctrl = ( (mods & InputEvent.CTRL_MASK) != 0);
		boolean alt = ( (mods & InputEvent.ALT_MASK) != 0);
		boolean shift = ( (mods & InputEvent.SHIFT_MASK) != 0);
		
		if (  ( (mods & InputEvent.BUTTON1_MASK) != 0) && ( (mods & InputEvent.BUTTON2_MASK) != 0)  && ctrl  ) //hmm.. having to be a bit too specific here..
		{
			generateTopologyFromSettings(  );
			output("Topology generation cued...");
		}
		
		if ((mods & InputEvent.BUTTON3_MASK) != 0)
		{
			if ( ctrl )
			{
					currentimage = randex.nextInt() % numimages;
					currentimage = (currentimage < 0) ? -currentimage : currentimage;
                    output("Loading image " + currentimage);
                    loadImage(imagenames[currentimage]);
			} else {
                // System.out.println( "YO!" );
                //currentimage = (currentimage + 1) % numimages;
                // post tab menu
                String[] methodList = keymap.getKeyToolStrings();
                for ( String method : methodList )
                {
                    System.out.println( method );
                }
                EditWidget tabEditWidget = new EditWidget( EditWidget.widgetType.MenuValue, "postTabMenu", keymap, methodList, e.getX(), e.getY(), EditWidget.widgetDrawMode.Default, widgetresultscale, Tiler.bgcolor, Tiler.midcolor, Tiler.fgcolor, (Component) this);
                addWidget( tabEditWidget );
                //KeyAction tabMenuAction = new KeyAction( "", "postTabMenu", EditWidget.widgetType.MenuValue, false, K, methodList, T, "Tab menu for actions", tab );
                //keymap.put( tab, tabMenuAction );
                //keymap.postTabMenu( "" );
                // System.out.println( "B'OH!" );
            }
		}
		
		if ((mods & InputEvent.CTRL_MASK) != 0) ctrldown = true;
		else ctrldown = false;
	}

	public void mouseEntered(MouseEvent e)
	{
		int mods = e.getModifiers();
		if ((mods & InputEvent.CTRL_MASK) != 0) ctrldown = true;
		else ctrldown = false;

		mouseloc.move(e.getX(),e.getY());
	}

	public void mouseExited(MouseEvent e)
	{
		int mods = e.getModifiers();
		if ((mods & InputEvent.CTRL_MASK) != 0) ctrldown = true;
		else ctrldown = false;

		mouseloc.move(e.getX(),e.getY());
	}

	public void mousePressed(MouseEvent e)
	{
		int mods = e.getModifiers();
		if ((mods & InputEvent.CTRL_MASK) != 0) ctrldown = true;
		else ctrldown = false;
		
		boolean ctrl = ( (mods & InputEvent.CTRL_MASK) != 0);
		boolean alt = ( (mods & InputEvent.ALT_MASK) != 0);
		boolean shift = ( (mods & InputEvent.SHIFT_MASK) != 0);
		
		
		floatingclick.set((float)e.getX(),(float)e.getY());
		clicklooppos.set(flooppos.x,flooppos.y);
		
		/*if ( ( (mods & InputEvent.BUTTON1_MASK) != 0) & ctrl &alt & !shift )
		{
			int pbx = (int) e.getX();
			int pby =  (int) e.getY(); 
			pixelbufferloc.move( pbx ,pby ) ;
			if (continuouspixelgrab)
			{
				int pbxbit = (int) ( ( bufferdimension.width ) / 2.0f); 
				int pbybit = (int) ( ( bufferdimension.height ) / 2.0f);
				MiniPixelTools.grabPixelRectangle(bufferimage, bufferpixels, pixelbufferloc.x + wigglepos.x - pbxbit, pixelbufferloc.y + wigglepos.y - pbybit, (int)(bufferdimension.width) ,(int)(bufferdimension.height), masterpixel);
			}
		}*/
		
		if ( ctrl & alt & shift )
		{
			//also move the tile lookup pos when shift is pressed
			flooppos = new Float2D( (float) e.getX() * currentlevel, (float)e.getY() * currentlevel );
		} else {
			clickloc.move(e.getX(),e.getY());
			mouseloc.move(e.getX(),e.getY());
		}
		
		mousedown = true;
		//repaint();
	}

	public void mouseReleased(MouseEvent e)
	{                                          
		int mods = e.getModifiers();
		if ((mods & InputEvent.CTRL_MASK) != 0) ctrldown = true; 
		else ctrldown = false;
		
		boolean ctrl = ( (mods & InputEvent.CTRL_MASK) != 0);
		boolean alt = ( (mods & InputEvent.ALT_MASK) != 0);
		boolean shift = ( (mods & InputEvent.SHIFT_MASK) != 0);
		
		
		/*if ( ( (mods & InputEvent.BUTTON1_MASK) != 0) &&  ( (mods & InputEvent.CTRL_MASK) != 0) && ( (mods & InputEvent.ALT_MASK) != 0) && ( (mods & InputEvent.SHIFT_MASK) == 0))
		{
			int pbx = (int) e.getX();
			int pby =  (int) e.getY(); 
			pixelbufferloc.move( pbx ,pby ) ;
			//if ( ( (mods & InputEvent.CTRL_MASK) != 0) && ( ( mods & InputEvent.SHIFT_MASK) != 0 ) )  //then move the loopPos around in 'drag' mode
			//System.out.println("in mousereleased..");
			
		} */		
		
		delta = new Float2D( (float)( e.getX() - (float) pixelbufferloc.x ) / 8.0f,
		                     (float)( e.getY() - (float) pixelbufferloc.y ) / 8.0f );
		
		draggingTilePosition = false;
		
		mousedown = false;
	}

	public void mouseDragged(MouseEvent e)
	{
		int mods = e.getModifiers();
		if ((mods & InputEvent.CTRL_MASK) != 0) ctrldown = true;
		else ctrldown = false;
		
		/*if ( ( (mods & InputEvent.BUTTON1_MASK) != 0) && ( (mods & InputEvent.CTRL_MASK) != 0) && ( (mods & InputEvent.ALT_MASK) != 0) && ( (mods & InputEvent.SHIFT_MASK) == 0))
		{
			int pbx = (int) e.getX();
			int pby =  (int) e.getY(); 
			pixelbufferloc.move( pbx ,pby ) ;
		}*/
		
		if ( ( (mods & InputEvent.CTRL_MASK) != 0) && ( ( mods & InputEvent.SHIFT_MASK) != 0 ) )  //then move the loopPos around in 'drag' mode
		{
			flooppos = new Float2D(clicklooppos.x + ((float)(clickloc.x-e.getX()) * currentlevel),  clicklooppos.y + ((float)(clickloc.y-e.getY()) * currentlevel));
			draggingTilePosition = true;
		} else {
			draggingTilePosition = false;
		}
		
		if (interactiveMode > 20)
		{
			mousebuffer[currmouse].move(e.getX() + wigglepos.x ,e.getY() + wigglepos.y);
		} else {
			mousebuffer[currmouse].move(e.getX(), e.getY() );
		}
		currmouse = (currmouse + 1) % mousebufferlength;
		
		mouseloc.move(e.getX(),e.getY());
		
		//delta = new Float2D( (float)( e.getX() - (float) screendimension.width / 2 ) / 8,
		//                     (float)( e.getY() - (float) screendimension.height / 2 ) / 8 );
		delta = new Float2D( (float)( e.getX() - (float) pixelbufferloc.x ) / 8.0f,
		                     (float)( e.getY() - (float) pixelbufferloc.y ) / 8.0f );
		
		                       
		mousedown = true;
		//repaint();
	}

	public void mouseMoved(MouseEvent e)
	{
		//another way.. not as interesting..:
		//float tmp1 = (randsrc.nextFloat() - 0.5f) * 20;
		//float tmp2 = (randsrc.nextFloat() - 0.5f) * 20;
		//float tmpdrag = 0.99f - (randsrc.nextFloat() * 0.001f);
		//double tmprot = (randsrc.nextDouble() - 0.5)/5;
		//particluster.activateParticle((float) e.getX(), (float) e.getY(), tmp1, tmp2, tmpdrag, tmprot);
		int mods = e.getModifiers();
		draggingTilePosition = false;
		if ((mods & InputEvent.CTRL_MASK) != 0) ctrldown = true;
		else ctrldown = false;

		if (interactiveMode > 20)
		{
			mousebuffer[currmouse].move(e.getX() + wigglepos.x ,e.getY() + wigglepos.y);
		} else {
			mousebuffer[currmouse].move(e.getX(), e.getY() );
		}
		currmouse = (currmouse + 1) % mousebufferlength;
		
        if ( ( (mods & InputEvent.CTRL_MASK) != 0) && ( ( mods & InputEvent.SHIFT_MASK ) != 0 ) && ( ( mods & InputEvent.ALT_MASK) == 0 ) )  //then move the loopPos around in 'drag' mode
		{
			flooppos = new Float2D(clicklooppos.x + ((float)(clickloc.x-e.getX()) * currentlevel),  clicklooppos.y + ((float)(clickloc.y-e.getY()) * currentlevel));
			draggingTilePosition = true;
		} else {
			draggingTilePosition = false;
		}
        
		if ( ( (mods & InputEvent.CTRL_MASK) != 0) && ( (mods & InputEvent.SHIFT_MASK) != 0) && ((mods & InputEvent.ALT_MASK) != 0) ) 
		{
			for ( EditWidget awidget : dawidgets )
			{
				if ( awidget.isAlive() )
				{
					// output( "awidget is: " + awidget );
					// output( "position is: " + e.getX() + "," + e.getY() );
					int offsetX = 0;
					int offsetY = 0;
					
					// TODO: make dragging preserve the offset when engaged!
					// so you can move without dragging, then drag with current offset! - DONE!
					if ( draggingPos == null )
					{
						float[] widgetPos = awidget.getPosition(); 
						draggingPos = new Float2D( widgetPos[0] - e.getX(), widgetPos[1] - e.getY() );
					} 
					offsetX = (int)draggingPos.x;
					offsetY = (int)draggingPos.y;
					
					awidget.setPosition( offsetX + e.getX(), offsetY + e.getY() );
				}
			}
		} else if (draggingPos != null) {
			draggingPos = null;
		}
		
		mouseloc.move( e.getX(), e.getY() );
		
		//delta = new Float2D( (float)( e.getX() - (float) screendimension.width / 2 ) / 8,
		//			(float)( e.getY() - (float) screendimension.height / 2 ) / 8 );
		
		//only change the delta when not moving.. convenience
		if ( ( (mods & InputEvent.CTRL_MASK) == 0) && ( (mods & InputEvent.ALT_MASK) == 0))
		{
			delta = new Float2D( (float)( e.getX() - (float) pixelbufferloc.x ) / 8.0f,
					(float)( e.getY() - (float) pixelbufferloc.y ) / 8.0f );
		}
		
		mousedown = false;
		//repaint();

	}

	public void focusGained(FocusEvent e)
	{
		//output("focusGained");
	}

	public void focusLost(FocusEvent e)
	{
		// if auto automode..
		// go into automode - tick time on some procedural mofo
		//output("focusLost");
	}

	public boolean dispatchKeyEvent(KeyEvent k)
	{
		//output("KEYEVENT!!");
		return false;
	}
	
	public synchronized void keyPressed(KeyEvent e)
	{
		if (initialised)
		{
			int keykode = e.getKeyCode();
			int mods = e.getModifiers();
			if ((mods & InputEvent.CTRL_MASK) != 0) ctrldown = true;
			else ctrldown = false;
			
			// lookup keycode + mods in global actions map
			//so.. i guess I need a global actions map of some kind..
			//
			//
			//define a key class that contains the keykode and modifiers
			//and an action class to specify what to do (this is where it all gets interesting.. ;) )
			// then form a key-value pair map
			
			//Hashtable<keyMapKey,keyMapAction> keymap = new HashTable<keyMapKey,keyMapAction>();
			
			//hmm I need to associate keycode and modifiers to methodname and so on.. hmm..
			//this whizzbang auto-unboxing new-collections framework hashtable should do the trick! ;D
			
			
			if ( dawidgets.size() == 0 )
			{
			
				long kt = ( ( (long) keykode ) << 32 ) + ( (mods & KeyEvent.CTRL_MASK) | (mods & KeyEvent.ALT_MASK) | (mods & KeyEvent.SHIFT_MASK) );
				
				// System.out.println( "keykode: " + keykode + " tab keykode is: " + KeyEvent.VK_TAB );
				
				KeyAction kat = keymap.getKeyAction( kt );
				
				if (kat != null)
				{
					// output("got a keymap entry!!! methodName: " + kat.methodname );
					dawidgets.add( kat.doAction( mouseloc.x, mouseloc.y ) );
					e.consume();
					return;
				}
			
				//posx = flx * scm
				//if scm => scm - clevel  (ie someone has pressed VK_DOWN)
				//the new posx = flx * (scm - clevel)
				//and to adjust for this we need to add something to the flx
				// (scm - clevel)*flx + x = flx * scm
				//flooppos.x += scrollmultiplier;
				
				switch ( keykode )
				{
					/* 
					// This is now handled by TilerKeyMap..
					case KeyEvent.VK_F4 :
					{
						if ( (mods & KeyEvent.ALT_MASK) != 0)
						{ 
							output("Alt-F4 detected.. had enough then..?");
							motionthread = null;
							imageloaderthread = null;
							masterpixel = null;
							slavepixel = null;
							toolrule = null;
							bufferimage =  null;
							outputTextArea = null;
							//try to kill everything!
							//getting crashing on exit! :P
							System.runFinalization();
							output("Finalized everything...");
							System.gc();
							output("Garbage collected...");
							System.exit( 0 ); 
							output("Message from the void: ");
							break; 
						}
					}*/
					
					case KeyEvent.VK_ENTER:
					{
						//reload into pixel buffer
						if ((mods & KeyEvent.CTRL_MASK) != 0)
						{
							if ((mods & KeyEvent.SHIFT_MASK) != 0)
							{
								MiniPixelTools.grabDefaultMapFromPixels( bufferpixels, extractormap, extractormapf, bufferdimension.width, bufferdimension.height );
								output("Grabbed new default map from pixels.");
							} else {
								//MiniPixelTools.grabPixels(tile,bufferpixels,masterpixel);
								//output("grabbed pixels.");
							}
						}
						break;
					}
		
					
					//case KeyEvent.VK_SPACE:
					//{
					//	if ((mods & KeyEvent.SHIFT_MASK) != 0)
					//	{
					//		dawidgets.add( new EditWidget( EditWidget.widgetType.EnumValue,"loadImage",this, (Object[]) imagenames,
					//									   mouseloc.x, mouseloc.y, EditWidget. widgetDrawMode.Default, 
                 	//									   widgetresultscale, bgcolor, midcolor, fgcolor,  (Component) this) );
					//		break;
					//	}
	                //	
					//	if ((mods & KeyEvent.CTRL_MASK) != 0)
					//	{
					//		currentimage = randex.nextInt() % numimages;
					//		currentimage = (currentimage < 0) ? -currentimage : currentimage;
					//	} else currentimage = (currentimage + 1) % numimages;
					//	
					//	output("loading image " + currentimage);
					//	
					//	loadImage( imagenames[currentimage] );
					//	
					//	break;
					//}
				}
			}
			e.consume();
		}
	}
	
	public void keyReleased(KeyEvent e)
	{
		int keykode = e.getKeyCode();
		String keyname = e.getKeyText(keykode);
		//output(keyname + " key Released");
		
		int mods = e.getModifiers();
		if ((mods & InputEvent.CTRL_MASK) != 0) ctrldown = true;
		else ctrldown = false;
	}

	public void keyTyped(KeyEvent e)
	{
		int keykode = e.getKeyCode();
		String keyname = e.getKeyText(keykode);
		//output(keyname + " key Typed");

		int mods = e.getModifiers();
		if ((mods & InputEvent.CTRL_MASK) != 0) ctrldown = true;
		else ctrldown = false;
	}

	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
	{
		//if ((infoflags & ImageObserver.ABORT) != 0)      output("ABORT flag registered in ImageUpdate");
		if ((infoflags & ImageObserver.ALLBITS) != 0)
		{
			output("ALLBITS flag registered in ImageUpdate");
			allbits = true;
			return false;
		}
		return true;
	}
}
