import java.awt.*;
import java.lang.Math;
//import MiniPixelTools;
//import ParticleSystem;
//import Topology;
//import java.awt.Color;
import java.awt.RenderingHints;

/**
 * Particle class.
 * (c) 2001 Dan Wills
 * 
 * todo:
 * particle bonds 
 * new draw modes:
 *   polyDot, polySquare  
 *   text w/string pointer
 *   API alphaSprite
 *   fillShape/ellipsoid
 *   grads, stroking, different line joins
 *   bezierLine, rail, rake, zigzag, traintracks, helix
 *   convolve
 *   image chunks, with affine
 *   intelligent use of clipping on multiple particles
 *   reSampled/tilemip texturing
 *   orbiting subparticles that draw splines/etc
 *   sampling/forming (or looking up) default parameter surface derivative at particlepos
 *   opacity fading in/out
 *   radius intersection testing, collision detection
 *   packing controls, bonded particles with bond drawing
 *   emitterParticles
 *
 * extend L-capabilities to support multiple-frame-during-lifespan Lstrings
 * as well as birth and death Lstrings
 * use comma to separate frames, track L-position accross frames
 *
 * make atom mapping more flexible
 * 
 *
 */

//Bond class
//2 tables

public class Particle implements Comparable
{
	//particle draw modes:=
	
	public enum particleDrawMode
	{
		FillCircle,
		FillSquare,
		Fill3DRectangle,
		ZapPolygon,
		TimePolygon,
		DiamondPolygon,
		ParallelogramPolygon,
		BowtiePolygon,
		RectangleRibbon,
		Line,
		Circle,
		Square,
		AgeText,
		MidpointPolygon,
		AlphaPIXELS,
		PIXELS,
		Zap,
		TrackAndRail,
		Track,
		Rail,
		Helix;
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( particleDrawMode p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
		
	}
	
	/*public final static int DRAW_FILLCIRCLE    = 0;
	public final static int DRAW_FILLSQUARE    = 1;
	public final static int DRAW_3DRECT        = 2;
	public final static int DRAW_ZAP           = 3;
	public final static int DRAW_POLYGON       = 4;
	public final static int DRAW_DIAMOND       = 5;
	public final static int DRAW_PARALELLO     = 6;
	public final static int DRAW_BOWTIE        = 7;
	public final static int DRAW_RECTRIBBON    = 8;
	public final static int DRAW_TIMELINE      = 9;
	public final static int DRAW_CIRCLE        = 10;
	public final static int DRAW_SQUARE        = 11;
	public final static int DRAW_STRING_AGE    = 12;
	public final static int DRAW_MIDPOLY       = 13;
	public final static int DRAW_ALPHA_PIXELS  = 14;
	public final static int DRAW_PIXELS        = 15;
	public final static int DRAW_ZAPLINE       = 16;
	public final static int DRAW_TRAINTRACKS   = 17;
	public final static int DRAW_RAIL          = 18;
	public final static int DRAW_HELIX         = 19;
	public final static int DRAW_BSPLINE       = 20;
	public final static int DRAW_MESSYBSPLINE  = 21;
	
	//public final static int DRAW_METABLOB    = 14;
	//public final static int DRAW_SOFT_TUBE        = 15;
	//public final static int DRAW_PROJECTION  = 15;
	//public final static int DRAW_TOPOLOGY  = 15;
	//public final static int DRAW_ALPHAPIXELS  = 15;
	//public final static int DRAW_CHANNELPIXELS  = 15;
	
	//public final static boolean DRAW_VISUAL_WRITEBACK = true;
	//public final static int DRAW_ARC         = 7;
	//public final static int DRAW_IMAGE       = 6;
	
	public final static int NUM_DRAWMODES    = 20;
	
	public final static String[] DRAWMODENAMES = {"Filled Circle",
											"Filled Square",
											"3D Rectangle",
											"Zap",
											"Polygon",
											"Diamond",
											"Parallelogram",
											"Bowtie",
											"RectangleRibbon",
											"Line",
											"Circle",
											"Square",
											"Age String",
											"Midpoint Polygon",
											"Alpha Pixels",
											"Pixels",
											"ZapLine",
											"TrainTracks",
											"Rail",
											"Helix"};
											//"Spline",
											//"MessySpline"};*/
	
	/* this is currently not used... probably will be completely superceded by a proper L-implementation anyway... */
	/*public final static int SPAWN_NEVER =          0;
	public final static int SPAWN_RANDOM=          1; 
	public final static int SPAWN_AT_AGE=          2;
	public final static int SPAWN_SNEEZE=          3;
	public final static int SPAWN_SNUFFED_IT=      4;
	public final static int SPAWN_SPEED_OVER=      5;
	public final static int SPAWN_SPEED_UNDER=     6;
	public final static int SPAWN_CHANNEL_OVER=    7;
	public final static int SPAWN_CHANNEL_UNDER=   8;
	public final static int SPAWN_MATE=            10;
	public final static int SPAWN_COLLIDE=         11;
	public final static int SPAWN_PROXIMITY=       12;
	public final static int SPAWN_PROBABILISTIC=   13;
	public final static int SPAWN_RECURSIVE=       14;
	public final static int SPAWN_L_STRING=        15;
	public final static int SPAWN_ORBITERS=        16;
	public final static int SPAWN_TOPOLOGY_LAYERS= 17;
	public final static int SPAWN_BRANCH_DEF=      18;
	public final static int NUM_SPAWNMODES=        19;*/
	public enum particleColorMode
	{
		Default,
		AlphaBlend,
		ChannelMax,
		ChannelMin,
		ChannelAdd,
		ChannelSubtract,
		ChannelExclusiveOR,
		ChannelHue,
		ChannelSaturation,
		ChannelLumi,
		ChannelRed,
		ChannelGreen,
		ChannelBlue,
		Red,
		Green,
		Blue,
		Add,
		Subtract,
		ExlusiveOR,
		Max,
		Min,
		Hue,
		Saturation,
		Luminosity,
		ruleColorLifespan;
		
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( particleColorMode p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}
	
	/*public final static int COLOR_DEFAULT       = 0;
	public final static int COLOR_ALPHABLEND    = 1;
	public final static int COLOR_CHANNELMAX    = 2;
	public final static int COLOR_CHANNELMIN    = 3;
	public final static int COLOR_CHANNELADD    = 4;
	public final static int COLOR_CHANNELSUB    = 5;
	public final static int COLOR_CHANNELXOR    = 6;
	public final static int COLOR_HUECHANNEL    = 7;
	public final static int COLOR_SATCHANNEL    = 8;
	public final static int COLOR_LUMICHANNEL   = 9;
	public final static int COLOR_RCHANNEL      = 10;
	public final static int COLOR_GCHANNEL      = 11;
	public final static int COLOR_BCHANNEL      = 12;
	public final static int COLOR_R             = 13;
	public final static int COLOR_G             = 14;
	public final static int COLOR_B             = 15;
	public final static int COLOR_ADD           = 16;
	public final static int COLOR_SUB           = 17;
	public final static int COLOR_XOR           = 18;
	public final static int COLOR_MAX           = 19;
	public final static int COLOR_MIN           = 20;
	public final static int COLOR_HUE           = 21;
	public final static int COLOR_SAT           = 22;
	public final static int COLOR_LUMI          = 23;
	
	public final static int NUM_COLORMODES      = 24;
											
	public final static String[] COLORMODENAMES = {"Default",
											"Alpha Blend",
											"ChannelMax",
											"ChannelMin",
											"ChannelAdd",
											"ChannelSubtract",
											"ChannelExclusiveOR",
											"ChannelHue",
											"ChannelSaturation",
											"ChannelLumi",
											"ChannelRed",
											"ChannelGreen",
											"ChannelBlue",
											"Red",
											"Green",
											"Blue",
											"Add",
											"Subtract",
											"ExlusiveOR",
											"Max",
											"Min",
											"Hue",
											"Saturation",
											"Luminosity",};
*/
	
	private particleColorMode colormode = particleColorMode.AlphaBlend;
	private particleDrawMode drawmode = particleDrawMode.TimePolygon;
	private boolean active = false;	      //the status of this particle.
	private int dimension;        //dimension of the space this particle is in.
	private int dimensionclip[];  //the clipping lengths of each dimension of the space to sample.
	private int drawdimensionclip[];  //the clipping lengths of each dimension of the space to draw.
	
	private float location[];     //the unclipped spacial location of this particle.
	private float drawlocation[];     //the spacial location to draw for this particle (clipped to drawdimension).
	private float samplelocation[];     //the spacial location to sample for this particle (clipped to bufferdimension).
	private float samplespacescale[]; //the scaling of the space that the particle is sampling from..  to allow zoomed in sampling
	
	private float lastloc[];      //the spacial location of this particle in the previous frame.
	private float lastsize;       //the size of this particle in the previous frame.
	private float offset[];       //pixel offset into visual[]
	private float polypoints[];   //for keeping track of what we drew last frame.
	
	
	// L string character definitions:
	// f  move
	// a  accelerate
	// d  drag
	// +  rotate positive
	// -  rotate negative
	// [  spawn particle
	// ]  kill particle
	// w  warp location
	// p  render pixels
	// |  next frame
	// s  s molecule +f-f-f+f+f-
	// z  z molecule -f+f+f-f-f+
	// l  l molecule -f+f+f-
	// r  r molecule +f-f-f+
	// y  y molecule f-[++[]
	// w  w molecuke 
	// >> push state
	// << pop state
	//
	// (condition)atom  conditional operator for execution of atom, or block of {atoms}
	//
	// variables: $x
	// $s - particle's size
	// $f - particle's speedlength
	// $r - particle's rotation
	// $a - particle's age
	// $t - global table lookup
	// $d - default channel of pixel under particle
	// eg ~($s > 50){-[+} means left side branch if size > 50
	// ad+f| is the basic setup
	// 
	// 
	
	private boolean l_mode = true;
	private String l_string = "d+f|";
	private int currentatom = 0;
	private int spawndepth = 0;
	public int drawIndex = 0;
	public int drawID = 0;
	//private int spawnmode = SPAWN_NEVER;
	private boolean spacewarping = false;
	private boolean fertile = true;
	private boolean justspawned = false;
	
	long currentFrame = 0;
	
	private int justclipped[] = { 0, 0 };
	private int justcrossedThreshold = 0;
	private int lthreshold = 50;
	private int lthresholdcrossingcount = 0;
	
	//keep track of which tile the particle is in, so that tiledDraw can work properly - it only needs to know when the start and end particle
	//of a primitive (eg a line primitive) are in the same tile, or some other tile
	private int tilepos[];
	
	private boolean moving = false;	      //whether this particle is moving or not.
	private float delta[];        //speed of this particle,
	private float mass;
	
	private boolean accelerating = false; //whether to accelerate this particle or not.
	private float deltadelta[];   //acceleration of this particle.
	
	private boolean dragging;
	private float drag;

	private boolean turning = false;     //whether to turn this particle's velocity or not...
	private double thetaprobability = 1; //probability that this particle turns every frame
	private double theta;                //how much this particle turns every frame
	private static java.util.Random randex = new java.util.Random();
	
	private float lfofreq = 1.0f/21.0f;
	
	//private long thresholdCrossingCount = 0;
	//private boolean justCrossedThreshold = false;
	
	private boolean ripe = false;
	
	private ParticleSystem currentParticleSystem;
	private int[] visual;         //color of this particle may be altered based on an image the same size as the space
	                              // &
	                              //the pixel_draw and pixel_warp (and so on) modes will use this buffer
	private int[] ppixel;         //pixel representation of particle (used in ALPHA_DRAW mode)
	private int ppixw, ppixh;     //pixmap width & height
	private Topology warpgate;
	private int color = 0x00000000;        //the actual color
	private int colorXtract = 0x00000000;  //the default channel extracted from the color
	private int alpha = 0x00000088;     //init alpha
	private int channelalpha = 0;       //init to basic alpha behaviour
	private Tool1D samplesource;
	private boolean pixelinterpolate = false;
	private boolean tiledraw = true;
	private float stability = 0.6f;//used to control the particle's uptake of color/etc
	private boolean channelmix = true;
	private float size = 0;   //how big this particle is.
	private float strokewidth = 4.0f;   //how wide to stroke this particle for stroking draw modes.
	private float maxsize = 1000.0f;
	private long age = -1;         //number of frames this particle has existed for.
	private long lifespan = 100;
	
	public int myindex = -1;
	
	//up to 4 neighbors to begin with.. mmm carbon
	private int numneighbors = -1;
	private int[] neighbors;               // int indexes to neighbor particles in the system
	private float[] neighborweights;       // float weighings to neighbors
	public boolean[] neighborlinkdrawn;    // a bit (that can be set by a reciprocal-neighbor particle) indicating whether the link to each neighbor has been drawn yet.
	private boolean[] isreciprocalneighbor;// a bit indicating whether each neighbor is a reciprocal link or not.
	private int[] reciprocalneighborID;    // to be used to keep track of bidirecional neighbor drawing, 
	                                       // by setting systemparticles[neighbors[k]].neighborlinkdrawn = true in the reciprocal particle whilst in drawNeighbors
	                                       // 
	
	/**
	 * Constructs a still particle at (0,0, . . ,0) in an N-dimensional space.
	 * All arrays used to initialise it must be the same length as the dimension.
	 * this is not checked.
	 *
	 *
	 * the parameters (in order) are: 
	 *
	 * boolean - whether this particle is on or off
	 * int - dimension of the space the particle is in
	 * int[dim] - lengths of the dimensions of the space that this particle is in	 
	 * Color - color of this particle
	 * float - stability of this particle
	 * float - size of this particle
	 */
	
	Particle(boolean status,
	         int dim, 
	         int dimclip[],
	         int drawdimclip[],
	         int[] vis,
	         float stabil, 
	         float siz,
	         particleDrawMode draw,
	         particleColorMode colmode)
	{
		colormode = colmode;
		drawmode = draw;
		active = status;		
		moving = false;
		accelerating = false;
		dimension = dim;
		dimensionclip = new int[dimension];
		drawdimensionclip = new int[dimension];
		location = new float[dimension];
		drawlocation = new float[dimension];
		samplelocation = new float[dimension];
		samplespacescale = new float[dimension];
		lastloc = new float[dimension];
		polypoints = new float[dimension*2];
		offset = new float[dimension];
		delta = new float[dimension];
		tilepos = new int[dimension];
		
		reNeighbor(4);
		
		mass = 1.0f;
		for (int i=0; i < dimension; i++)
		{
			tilepos[i] = 0;
			dimensionclip[i] = dimclip[i];
			drawdimensionclip[i] = drawdimclip[i];
			offset[i] = 0.0f;
			location[i] = 0.0f;
			drawlocation[i] = 0.0f;
			samplelocation[i] = 0.0f;
			samplespacescale[i] = 1.0f;
			lastloc[i] = 0.0f;
			lastsize = 0f;
			polypoints[i] = 0.0f;
			delta[i] = 0.0f;
		}
										
		visual = vis;
		
		stability = stabil;
		size = siz;
		age = 0L;
		
	}
	/**
	 * Construct the default Particle, 2 dimensional in the space bounded by h & w;
	 */
	Particle(int w, int h, int wd, int hd, int[] c, particleDrawMode draw, particleColorMode colmode, int myindexx)
	{
		colormode = colmode;
		active = false;
		drawmode = draw;
		moving = false;
		accelerating = false;
		turning = false;
		dimension = 2;
		dimensionclip = new int[dimension];
		drawdimensionclip = new int[dimension];
		location = new float[dimension];
		drawlocation = new float[dimension];
		samplelocation = new float[dimension];
		samplespacescale = new float[dimension];
		offset = new float[dimension];
		lastloc = new float[dimension];
		polypoints = new float[dimension*2];
		delta = new float[dimension];
		deltadelta = new float[dimension];
		tilepos = new int[dimension];
		
		myindex = myindexx;
		
		reNeighbor(4);
		
		mass = 1.0f;
		dimensionclip[0] = w;
		dimensionclip[1] = h;
		
		drawdimensionclip[0] = wd;
		drawdimensionclip[1] = hd;
		
		for (int i=0; i < dimension; i++)
		{			
			tilepos[i] = 0;
			location[i] = 0.0f;
			drawlocation[i] = 0.0f;
			samplelocation[i] = 0.0f;
			samplespacescale[i] = 1.0f;
			offset[i] = 0.0f;
			delta[i] = 0.0f;
		}
										
		visual = c;
		stability = 1.0f;
		size = 4;
		age = 0L;
	}
	
	//this interface is currently implemented so that the particle array can be sorted for drawing purposes
	public int compareTo( Object p )
	{
		return (this.drawIndex > ((Particle)p).drawIndex) ? 1 : (this.drawIndex < ((Particle)p).drawIndex) ? -1 : 0;
	}
	
	public void clearNeighbor(int index)
	{
		if ((index >= 0) && (index < numneighbors))
		{
			neighbors[index] = -1;
			neighborweights[index] = -1.0f;
			isreciprocalneighbor[index] = false;
			reciprocalneighborID[index] = -1;
		}
	}
	
	public void clearNeighbors(ParticleSystem memates)
	{
		for (int n = 0; n < numneighbors; n++)
		{
			if (isreciprocalneighbor[n])
			{
				memates.cluster[neighbors[n]].clearNeighbor(reciprocalneighborID[n]);
			}
			
			neighbors[n] = -1;
			neighborweights[n] = -1.0f;
			
			isreciprocalneighbor[n] = false;
			reciprocalneighborID[n] = -1;
		}
	}
	
	//can be used to set a neighbor or to disable one (with a negative index)
	//may return a different whichneighbor to the one requested, if force is off
	public int setUnidirectionalNeighbor(int whichneighbor, int towhichindex, float weight, ParticleSystem memates, boolean force)
	{
		
		//check the connection target is a valid particle (it may not be active.. but at least it exists..)
		if ( (towhichindex >= 0) && (towhichindex < memates.cluster.length) )
		{
			
			
			// if force is on and the index is valid, just overwrite
			if ( (whichneighbor >= 0) && (whichneighbor < numneighbors) && (force))
			{	
				
				neighbors[whichneighbor] = towhichindex;
				neighborweights[whichneighbor] = weight;
				
				//set non-bidirectional
				isreciprocalneighbor[whichneighbor] = false;
				reciprocalneighborID[whichneighbor] = -1;
				
			} else {
				
				if (force)
				{
					
					//since force is on but an invalid index was specified.. (very-pseudo) randomly select a neighbor to overwrite
					int whichwhich = towhichindex % numneighbors; //Math.max(0,Math.min(numneighbors,whichneighbor));
					
					//could have a policy here to decide what to do when force is on and the index is out of range..
					//
					//eg
					//
					//switch (neighborpolicy)
					//{
					//	case(modulo) : 
					//	case(clip) : 
					//	case(closestToNewNeighbor) :
					//	case(furthestFromNewNeighbor) :
					//	case(growNumNeighbors) :
					//	case(ifNewneighborIsUniqueThenGrowNumNeighbors) :
					//	case() :
					//
					//int neighborclip = Math.min(numneighbors,whichneighbor);
					neighbors[whichwhich] = towhichindex;
					neighborweights[whichwhich] = weight;
					
					//set non-bidirectional
					
					isreciprocalneighbor[whichwhich] = false;
					reciprocalneighborID[whichwhich] = -1;
					return whichwhich;
					
				} else {
					
					//force wasn't on and no free neighbor was found.. advise
					return -1;
				}
				
			}
					
			return -1;
		} else {
			// invalid towhichindex specified
			return -1;
		}
	}
	
	
	//return codes:
	// zero or positive int K returned - sucessfully set neighbor K to the selected valid index 
	//   - this might not be the one that was requested, and the link may not be bidirectional if force is off.
	//
	// negative int returned - if force is on -1 is returned if an invalid towhichindex is specified.. as in it's an error and nothing was set.
	//                         if force is not on and all neighbors are already set -1 is returned
	//
	
	public int setBidirectionalNeighbor(int whichneighbor, int towhichindex, float weight, ParticleSystem memates, boolean force)
	{
		
		//first check a valid neighbor has been chosen to connect to:
		if ( (towhichindex >= 0) && (towhichindex < memates.cluster.length) )
		{
			
			// u know which SELECTED neighbor u want to set.. depending whether force=true u might get it
			
			if( (whichneighbor >= 0) && (whichneighbor < numneighbors) )
			{
				
				
				int nindex = -1;
				
				//if selected neighbor is uninitialised, no need to force, just use it
				if (neighbors[whichneighbor] < 0)
				{
					//this ALWAYS gets thru the condition below
					nindex = whichneighbor;
					
				} else {
					
					//otherwise, if force is on, overwrite
					if (force)
					{
						//this also ALWAYS gets thru the condition below
						nindex = whichneighbor;
						
					} else {
						
						//find a free index *sigh* . . . 
						// - exit the loop if/when one is found
						for (int w = 0; ((w < numneighbors) && (nindex < 0)); w++)
						{
							if (neighbors[w] < 0)
							{
								nindex = w;
							}
							//nindex might still be negative at this point - ie force wasn't on and no free index was found
						}
					}
				}	
				
				//if we got a valid index - set the neighbor and request bidirectional link - may not be granted if force=false
				if (nindex >= 0)
				{
					neighbors[nindex] = towhichindex;
					neighborweights[nindex] = weight;
					
					// using force = true would ensure that we always get a valid index here.. (since we know myindex is a valid particle number in this particle's system..)
					// but force might not be true.. so check and fallback to unidirectional
					int recipOK = memates.cluster[towhichindex].biLinkNeighbor(myindex, weight, whichneighbor, force);
					
					if (recipOK >= 0)
					{
						isreciprocalneighbor[nindex] = true;
						reciprocalneighborID[nindex] = recipOK;
						return nindex;
					} else {
						
						//the universe exploded - they couldn't spare another neighbor - fallback to unidirectional
						isreciprocalneighbor[nindex] = false;
						reciprocalneighborID[nindex] = -1;
						
						//special return code for unidirectional_only
						return -2;
						
					}
					
				} else {
					
					//force must be off - and we didn't find an index
					return -1;
				}
			
			} else if (whichneighbor < 0) {
				
				// invalid neighbor or chose not to specify - towhichindex is stil valid though, so
				// find next free neighbor, if none availavle and force=false, return -1 and do nothing
				// if one available, set it and propagate the force criteria to the selected reciprocal neighbor.. 
				//
					
				
				int nindex = -1;
				
				
				if (force) 
				{
					//pick a pseudorandom neighbor to force-overwrite
					nindex = towhichindex % numneighbors;
				} else {
					
					//look for a free neighbor (this is the laziest way to try to connect without forcing)
					for (int n = 0; ( (n < numneighbors) && (nindex < 0) ); n++)
					{
						if (neighbors[n] < 0)
						{
							nindex = n;
						}
					}
				}
				
				
				if (nindex >= 0)
				{
					neighbors[nindex] = towhichindex;
					neighborweights[nindex] = weight;
					
					int recipOK = memates.cluster[towhichindex].biLinkNeighbor(myindex, weight, whichneighbor, force);
					
					//did the link request succeed?
					if (recipOK >= 0)
					{
						isreciprocalneighbor[nindex] = true;
						reciprocalneighborID[whichneighbor] = recipOK;
					} else {
						//the universe exploded - they couldn't spare another neighbor - fallback to unidirectional
						isreciprocalneighbor[nindex] = false;
						reciprocalneighborID[whichneighbor] = -1;
						//special return code for unidirectional_only
						return -2;
					}
					
				} else {
					return -1;
				}
				
				return -1;
			} //end else-if whichneighbor was negative
			
			return -1;
			
		} else {	
			
			//invalid neighbor-towhichindex selected - error
			return -1;
		}
	}
	
	
	
	public int biLinkNeighbor(int toindex, float weight, int reciprocalID, boolean force)
	{
		if (toindex != myindex)
		{
		
			int nindex = -1;
			
			if (toindex >= 0)
			{
				//search for first free neighbor, hopefully its the first one! erk
				for (int p = 0; ( (p < numneighbors) && (nindex < 0) ); p++)
				{
					//if neighbor is unassigned.. use it - no need to force
					if (neighbors[p] < 0)
					{
						nindex = p;
					}
				}
				
				// if a free index was not found but force was still on then we still need one.
				// So, pick an index to overwrite - can't afford rand.. *shrug*, how about modulo toindex..? :)
				if ((nindex < 0) && (force))
				{
					nindex = toindex % neighbors.length;
				}
				
				
				if (nindex < 0) //this should not be possible unless force was off
				{
					return -1;
					
				} else {
					
					// all is well
					neighbors[nindex] = toindex;
					neighborweights[nindex] = weight;
					isreciprocalneighbor[nindex] = true;
					reciprocalneighborID[nindex] = reciprocalID;
				}
			} else {
				
				//can't connect to an invalid index.. do nothing - return the invalid negative index
				
				return toindex;
				
			}
			
			return nindex;
		} else {
			return -1;
		}
		
	}
	
	public void reNeighbor(int setnumneighbors)
	{
		//if neighbors preexist..
		if (setnumneighbors == numneighbors)
		{
			//we're already that size! do nothing...
			return; 
			
		} else {
		
			if ((numneighbors > 0) && (neighbors != null))
			{
				//neighbor array already exists? - then copy any overlap with new size via buffer arrays..
				
				int[] newneighbors = new int[setnumneighbors];
				boolean[] newneighborlinkdrawn = new boolean[setnumneighbors];
				boolean[] newrecips = new boolean[setnumneighbors];
				int[] newrecipIDs = new int[setnumneighbors];
				float[] newweights = new float[setnumneighbors];
				
				for (int g = 0; g < setnumneighbors; g++)
				{
					if (g < neighbors.length) 
					{
						//if array position is within old size.. then copy
						//this loses some neighbors when the new number of neighbors is less than it was before.. o well
						newneighbors[g] = neighbors[g];
						newneighborlinkdrawn[g] = neighborlinkdrawn[g];
						newrecips[g] = isreciprocalneighbor[g];
						newrecipIDs[g] = reciprocalneighborID[g];
						newweights[g] = neighborweights[g];
						
					} else {
						
						//if there's more neighbors.. make sure the new ones are uninitialised.
						newneighbors[g] = -1;
						newneighborlinkdrawn[g] = false;
						newrecips[g] = false;
						newrecipIDs[g] = -1;
						newweights[g] = 1.0f;
					}
					
				}
				
				//assign the new data via the buffer pointers ...
				neighbors = newneighbors;
				neighborlinkdrawn = newneighborlinkdrawn;
				isreciprocalneighbor = newrecips;
				reciprocalneighborID = newrecipIDs;
				neighborweights = newweights;
				
			} else {
				
				// never been set before .. just create arrays and initialise..
				neighbors = new int[setnumneighbors];
				neighborlinkdrawn = new boolean[setnumneighbors];
				isreciprocalneighbor = new boolean[setnumneighbors];
				reciprocalneighborID = new int[setnumneighbors];
				neighborweights = new float[setnumneighbors];
				
				for (int g = 0; g < setnumneighbors; g++)
				{
					//set uninitialised values
					neighbors[g] = -1;
					neighborlinkdrawn[g] = false;
					isreciprocalneighbor[g] = false;
					reciprocalneighborID[g] = -1;
					neighborweights[g] = 1.0f;
				}
			}
		
			numneighbors = setnumneighbors;
		}
	}
	
	public static float getLength(float[] of)
	{
		float accu = 0;
		for (int i=0; i < of.length; i++)
		{
			accu += of[i] * of[i];
		}
		//assuming 2D:
		accu = (float) java.lang.Math.sqrt((double)accu);
		
		//take the nth root of the result 
		//try
		//{
		//	accu = (float) java.lang.Math.pow( (double) accu, (double) -of.length );
		//} catch (ArithmeticException e) { accu = 0.0f; }				
		
		return accu;
	}
	
	public void setPixelWorld(int[] where)
	{
		visual = where;
	}
	
	public void setPixelMap(int[] pmap, int w, int h) //offset axis?
	{
		ppixel = pmap;
		ppixw = w;
		ppixh = h;
	}
	
	public void setTopologyWarp(Topology warpField)
	{
		warpgate = warpField;
	}
	
	public void setNeigbor(int neighbornumber, int newneighbor)
	{
		neighbors[neighbornumber] = newneighbor;
	}	
	
	public void setLFOFreq(float x)
	{
		lfofreq = x;
	}
	
	public void setOffset(float x, float y)
	{
		offset[0] = x;
		offset[1] = y;
	}
	
	public void setLocation(float x, float y)
	{
		location[0] = x;
		location[1] = y;
		clipToSpaces();
		resetPolyPoints();
	}
	
	public void setLifespan( long to )
	{
		lifespan = to;
	}
	
	public void resetPolyPoints()
	{
		polypoints[0] = (int)( drawlocation[0] );
		polypoints[1] = (int)( drawlocation[1] );
		polypoints[2] = (int)( drawlocation[0] );
		polypoints[3] = (int)( drawlocation[1] );
	}
	
	public float[] getLocation()
	{
		return location;
	}
	
	public float[] getDrawLocation()
	{
		return drawlocation;
	}
	
	public int[] getTilePosition()
	{
		return tilepos;
	}
	
	public float getXLocation()
	{
		return location[0];
	}
	
	public float getYLocation()
	{
		return location[1];
	}
	
	public float getLocationInDimension(int dimension)
	{
		if ((dimension > 0 ) & (dimension < location.length)) 
		{
			return location[dimension];
		} else {
			return Float.NaN;
		}
	}
	
	//public void setSpawnMode(int to)
	//{spawnmode = to;}
	
	public void setLThreshold( int towhat )
	{
		lthreshold = towhat;;
	}
	
	public void resetClippingAndThresholdMeasure( )
	{
		for (int k = 0; k < justclipped.length; k++) justclipped[ k ] = 0;
		justcrossedThreshold = 0;
		lthresholdcrossingcount = 0;
	}
	
	public void setSpawnDepth(int to)
	{spawndepth = to;}
	
	public int getSpawnDepth()
	{return spawndepth;}
	
	public void setLString(String towhat)
	{
		if (towhat.length() > 0)
		{
			l_mode = true;
			l_string = towhat;
		} else {
			l_mode = false;
			l_string = null;
		}
	}
	
	/*Particle[] getSpawn()
	{
		//decide whether to or not, then use spawnparams to generate the new particles...
		
		switch (spawnmode)
		{
			case SPAWN_NEVER: {break;}
			case SPAWN_RANDOM:
			case SPAWN_AT_AGE:
			case SPAWN_SNEEZE:
			case SPAWN_SNUFFED_IT:
			case SPAWN_SPEED_OVER:
			case SPAWN_SPEED_UNDER:
			case SPAWN_CHANNEL_OVER:
			case SPAWN_CHANNEL_UNDER:
			case SPAWN_MATE:
			case SPAWN_COLLIDE:
			case SPAWN_PROXIMITY:
			case SPAWN_PROBABILISTIC:
			case SPAWN_RECURSIVE:
			case SPAWN_L_STRING:
			case SPAWN_ORBITERS:
			case SPAWN_TOPOLOGY_LAYERS:
			case SPAWN_BRANCH_DEF:
			{break;}
		}
		
		int numspawn = 0;
		Particle spawn[] = new Particle[numspawn];
		for (int i = 0; i < numspawn; i++)
		{
			//spawn[i] = new Particle(kdjfgkdjfghknvsdjghsk);
		}
		return spawn;
	}*/
	
	// introduce zero crossings counter
	// maintain a justcrossedzero boolean
	// X - conditional atom
	// # - an integer
	// add conditional atom X<#%>A
	// if no number, just do it on the first zero crossing 
	// if only X and the modulo, do it on every zero crossing
	// if only X and a number, do it on
	
	void doLatom( char atom, char nextatom )
	{
		switch ( atom )
		{
			case '|' :  // pass-through / skip
			{
				break;
			}
			case 'f':
			{	
				for (int i = 0; i < dimension; i++) {lastloc[i] = drawlocation[i]; lastsize = size;}
				applySpeed();
				break;
			}
			case 'F':{break;}
			case 'd':
			{
				applyDrag();
				break;
			}
			case 'D':{break;}
			case 'a':
			{
				applyAcceleration();
				break;
			}
			case '+':
			{
				float noi = (float)improvedPerlinNoise.improvedPerlinNoiseThree( (float)(age * currentParticleSystem.getBaseLFOFreq()) + drawID * 272.2, currentFrame* currentParticleSystem.getBaseLFOFreq() + drawID * 12.85 - 822.21, -2.15 );
				noi = (noi + 1.0f) * 0.5f;
				if ( noi < Math.abs(thetaprobability) )
				{
					if (thetaprobability > 0)
					{
						applyRotation(theta);
					} else {
						float noix = (float)improvedPerlinNoise.improvedPerlinNoiseThree( (float)(age + currentFrame) * currentParticleSystem.getBaseLFOFreq() * 3.0 + drawID * 222.2 - 645.334, 955.5, -58.545 );
						if ( noix <= 0 ) applyRotation(theta);
						else applyRotation(-theta);
					}
				}
				break;
			}
			case '-':
			{
				float noi = (float)improvedPerlinNoise.improvedPerlinNoiseThree( (float)(age * currentParticleSystem.getBaseLFOFreq()) + drawID * 272.2, currentFrame * currentParticleSystem.getBaseLFOFreq() + drawID * 12.85 - 822.21, -2.15 );
				if ( noi < Math.abs(thetaprobability) )
				{
					if (thetaprobability > 0)
					{
						applyRotation(theta);
					} else {
						float noix = (float)improvedPerlinNoise.improvedPerlinNoiseThree( (float)(age + currentFrame) * currentParticleSystem.getBaseLFOFreq() * 3.0 + drawID * 222.2 - 645.334, 955.5, -58.545 );
						if ( noix <= 0 ) applyRotation(-theta);
						else applyRotation(theta);
					}
				}
				break;
			}
			case '[':
			{
				//test spawnprobability here...
				if ( spawndepth < currentParticleSystem.maxspawndepth ) currentParticleSystem.activateParticle( location[0], location[1], delta[0], delta[1], drag, theta, thetaprobability, stability, spawndepth+1, samplespacescale[0], drawID );
				break;
			}
			case ']':{ kill(); break; }
			case 'w':
			{
				warpgate.inlineWarpPoint(location);
				break;
			}
			case '>': // only exec next atom on positive threshold crossings
			{
				if (justcrossedThreshold == 1)
				{
					//System.out.println("> atom executing next atom: " + nextatom );
					doLatom(nextatom,'|'); //this disables double conditionals.. poo - well its not really meant to work at that frequency of crossings anyway..
				}
				currentatom++;
				currentatom %= l_string.length();
				break;
			}
			
			case '<': // only exec next atom on negative threshold crossings
			{
				if (justcrossedThreshold == -1)
				{
					//System.out.println("< atom executing next atom: " + nextatom );
					doLatom(nextatom,'|'); //this disables double conditionals.. poo - well its not really meant to work at that frequency of crossings anyway..
				}
				currentatom++;
				currentatom %= l_string.length();
				break;
			}
			
			case '}': // only exec next atom on positive threshold value
			{
				if (colorXtract <= lthreshold)
				{
					doLatom(nextatom,'|'); //this disables double conditionals.. poo - well its not really meant to work at that frequency of crossings anyway..
				}
				currentatom++;
				currentatom %= l_string.length();
				break;
			}
			
			case '{': // only exec next atom on negative threshold value
			{
				if (colorXtract > lthreshold)
				{
					doLatom(nextatom,'|'); //this disables double conditionals.. poo - well its not really meant to work at that frequency of crossings anyway..
				}
				currentatom++;
				currentatom %= l_string.length();
				break;
			}
			
			//lthresholdcrossingcount
		}
	}
	
	
	//particle[] update()
	void update( ParticleSystem memates, long frame )
	{
		if (active)
		{
			currentParticleSystem = memates;
			currentFrame = frame;
			
			ripe = false;
			if ( ( l_mode ) && ( l_string.length() != 0) )
			{
				char act = '|';
				currentatom %= l_string.length();
				//try { 
				act = l_string.charAt( currentatom );
				int distanceTraveled = 0;
				//nextact will only be evaluated if the atom is a conditional.. in which case it does the pointer incrementation itself..
				char nextact = l_string.charAt( (currentatom + 1) % l_string.length() );
				//} catch (IndexOutOfBoundsException x) {act = '|';}
				
				while ( ( act != '|' ) & ( active ) & ( distanceTraveled < l_string.length() ) ) //process a frame block in the rule.. need to have another bailout here or the particle might live forever..
				{
					doLatom( act, nextact );
					distanceTraveled++;
					currentatom++;
					currentatom %= l_string.length();
					act = l_string.charAt( currentatom );
					
					//updatecolor also updates default value - which affects the threshold-crossing-tracking
					updateColor();
					
					
					clipToSpaces();
					age++;
				}
				if (act == '|') { currentatom++; }
			}
			
			//always clipToSpaces (particularly because updateColor() and draw() are called next..)
			
			//thresholdCrossingCount = 0;
			//justCrossedThreshold = false;
			
			//for (int i = 0; i < dimension; i++) {lastloc[i] = location[i];}
			//if (turning) applyRotation(theta); 
			//if (dragging) applyDrag();
			//if (accelerating) applyAcceleration();			
			//if (moving) applySpeed();			
			//if (spacewarping) warpgate.inlineWarpPoint(location);
			//Particle[] spawn = null;
			//if (fertile) {spawn = getSpawn();}
			//memates.addParticles(spawn);
			
			
			ripe = true;
		}
	}
		
	public void setDrawIndex(int towhat)
	{ drawIndex = towhat; }
	
	public void setDrawID(int towhat)
	{ drawID = towhat; }
	
	public void setCurrentAtom(int which)
	{
		if (l_string != null) currentatom = (which > 0) ? which%l_string.length() : ( which % l_string.length()) + l_string.length() ;
		else currentatom = 0;
	}	
	public void setActive(boolean towhat)
	{
		if ((!active) & (towhat)) {age = 0;}
		if ((active) & (!towhat)) {age = -1;}
		active = towhat;
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	//just set the scaling isotropically for the moment.. would be cool to ultimately allow rotation and deformation (and topology warping) of the sample space
	public void setSampleSpaceScale(float to) 
	{
		for (int d = 0; d < 2; d++)
		{
			samplespacescale[d] = to;
		}
	}
	public void setTilePosition(int to)
	{
		for (int d = 0; d < 2; d++)
		{
			tilepos[d] = to;
		}
	}
	
	public void kill()
	{
		active = false;
		//location = null;
	
		ripe = false;
			
		moving = false;
		//delta = null;
		
		accelerating = false;
		//deltadelta = null;
		
		turning = false;
		theta = 0.0;
		
		dragging = false;
		drag = 0.0f;
		age = -1;
		
		setTilePosition(0);
	}
	
	public void setSpeed(float xcomp, float ycomp)
	{
		moving = true;
		delta[0] = xcomp;
		delta[1] = ycomp;
		polypoints[0] = drawlocation[0];//-delta[0]);
		polypoints[1] = drawlocation[1];//+delta[1]);
		polypoints[2] = drawlocation[0];//+delta[0]);
		polypoints[3] = drawlocation[1];//-delta[1]);
	}
	
	public float[] getSpeed()
	{ return delta; }
	
	public long getAge()
	{ return age; }
	
	public void setAge( long to )
	{ age =  to; }
	
	public long getLifespan()
	{
		return lifespan;
	}
	
	public void applySpeed()
	{
		for (int i = 0; i < dimension; i++)
		{
			location[i] += delta[i];
		}
	}
	
	public void setAcceleration(float xc, float yc)
	{
		accelerating = true; 
		deltadelta[0] = xc;
		deltadelta[1] = yc;
		
		/**for (int i = 0; i < dimension; i++)
		{
			deltadelta[i] = towhat[i];
		}*/
	}
	
	public void setMass(float m)
	{ mass = m; }
	
	void applyAcceleration()
	{		
		{
			for (int i = 0; i < dimension; i++)
			{
				delta[i] += delta[i]+deltadelta[i]*mass;
			}
		}
	}
	
	public void setRotation(double towhat)
	{
		turning = true; 
		theta = towhat;
	}
	
	public void setRotationProbability(double towhat)
	{
		if (towhat != 0) 
		{
			turning = true;
			thetaprobability = towhat;
		} else {
			thetaprobability = towhat;
			turning = false;
		}
	}
	
	public double getRotation()
	{return theta;}
	/**
	* multiply speed vector by cosine & sine of 
	* angle provided... should work for all dimensional vectors.
	*/
		
	public void applyRotation(double theta)
	{
		float tmpdelta[] = new float[dimension];
		//standard vector rotation
		// x' = x*cos(theta) - y*sin(theta)
		// y' = x*sin(theta) + y*cos(theta)
		
		tmpdelta[0] = (float) (delta[0] * (float) java.lang.Math.cos(theta)) - 
		                      (delta[1] * (float) java.lang.Math.sin(theta));
		              
		tmpdelta[1] = (float) (delta[0] * (float) java.lang.Math.sin(theta)) + 
		                      (delta[1] * (float) java.lang.Math.cos(theta));
		                      
		delta[0] = tmpdelta[0];
		delta[1] = tmpdelta[1];
	}
	
	public float getDrag()
	{
		return drag; //shouldnt really do this unless dragging is true but...
	}
	
	public void setDrag(float towhat)
	{
		dragging = true;
		drag = towhat;
	}
	
	public void applyDrag()
	{
		for (int i = 0; i < dimension; i++)
		{
			delta[i] *= (1.0f - drag);
		}		
	}
	
	public void setSize(float towhat)
	{
		size = (towhat < 0) ? -towhat : towhat;
		if (size > maxsize) towhat = maxsize;
		if (size == Float.NaN) size = 0;
	}
	
	public void setStrokeWidth(float towhat)
	{
		strokewidth = (towhat < 0) ? -towhat : towhat;
		//if (strokewidth > maxsize) towhat = maxsize;
		if (strokewidth == Float.NaN) strokewidth = 0;
	}
	
	public void setSpaceClip(int dimclip[])
	{
		dimensionclip = dimclip;
	}
	

	void clipToSpaces() //wraps the particle's unclipped location into the drawing and sampling spaces.
	{
		//justclipped = false;
		
		for (int i = 0; i < dimension; i++)
		{
			//if tilepos changes - set justClipped for the current dimension
			int lastTilePos = tilepos[i];
			
			if (location[i] >= 0)
			{
				drawlocation[i] = location[i] % (float) drawdimensionclip[i];
			} else { // (location[i] < 0)
				drawlocation[i] = (location[i] % (float) drawdimensionclip[i]) + drawdimensionclip[i];
			}
			
			if ( ( location[i] *samplespacescale[i]  + offset[i] ) >= 0)
			{
				samplelocation[i] = ( location[i] * samplespacescale[i] + offset[i] ) % (float) dimensionclip[i];
			} else {
				samplelocation[i] = (  ( location[i] * samplespacescale[i] + offset[i] ) % (float) dimensionclip[i] ) + dimensionclip[i];
			}
			
			//need the modulo for int sequence to only have one zero location, ie not this::
			// -2 -1 0 0 1 2 3
			//but this:
			// -2 -1 0 1 2 3
			
			tilepos[i] = (int) ( location[i] / drawdimensionclip[i] );
			
			// this means if the location is negative you need to subtract one from the tilepos to properly detect clipping:
			if ( location[i] < 0 ) tilepos[i] = tilepos[i] - 1;
			
			if (tilepos[i] > lastTilePos) 
			{
				justclipped[i] = 1;
				
			} else if ( tilepos[i] < lastTilePos ) {
				
				justclipped[i] = -1;
				
			} else {
				
				justclipped[i] = 0;
			}
			
		}
	}
		
	public Color getColor()
	{
		//getIntColor(),true);
		float a = (float) alpha/255.0f;
		a = (a < 0) ? 0 : (a > 1.0f) ? 1.0f : a;
		
		if (channelalpha == 0) return new Color(((color & 0x00ff0000) >> 16)/255.0f,((color & 0x0000ff00) >> 8)/255.0f,((color & 0x000000ff))/255.0f, a);
		else 
		{
			float alp = a - ((float)channelalpha/255.0f) * ((float)colorXtract/255.0f);
			alp = (alp < 0) ? 0 : (alp > 1.0f) ? 1.0f : alp;
			return new Color(((color & 0x00ff0000) >> 16)/255.0f,((color & 0x0000ff00) >> 8)/255.0f,((color & 0x000000ff))/255.0f, alp);
		}
		//getIntColor(),true);
	}
	
	public int getIntColor()
	{return (alpha << 24)|color;}
	
	public void setAlpha(int towhat)
	{
		alpha = (towhat > 255) ? 255 : (towhat < 0) ? 0 : towhat;
	}
	
	public void setChannelAlpha(int amount)
	{
		channelalpha = amount;
	}
	
	public void setChannelAlpha(boolean towhat)
	{channelalpha = (towhat) ? 255 : 0;}
	
	public void setInterpolation(boolean towhat)
	{pixelinterpolate = towhat;}
	
	public void setSampleSource( Tool1D source )
	{ samplesource = source; }
	
	int updateColor() //updates this particle's color _and returns the result
	{
		//do some clipping?
		//offset the tile?
		//do alpha?
		
		//clipToSpaces();
		
		float xo = samplelocation[0];
		float yo = samplelocation[1];
		
		int loc = 0;
		int nextcolx = 0;
		
		if (pixelinterpolate)
		{
			nextcolx = MiniPixelTools.getInterpolatedPixel(xo,yo,visual,dimensionclip[0],dimensionclip[1]);
		} else { //nearest neighbor
			loc = ( (int) xo  + (int) yo * dimensionclip[0] ) % visual.length; //shouldn't need the modulo, but just in case
			if (loc < 0) loc += visual.length; //in case
			nextcolx = visual[loc];
		}
		
		int prevcolx = 0;
		if (age > 0) prevcolx = color; else prevcolx = nextcolx;
		
		int nxdef = MiniPixelTools.getDefault(nextcolx);
		int prdef = MiniPixelTools.getDefault(prevcolx);
		
		if ( (prdef <= lthreshold) & (nxdef > lthreshold) )
		{
			//System.out.println("crossed threshold positively, count:" + ( lthresholdcrossingcount + 1 ) );
			justcrossedThreshold = 1;
			lthresholdcrossingcount++;
		} else if ( (nxdef <= lthreshold) & (prdef > lthreshold) ) {
			//System.out.println("crossed threshold negatively, count:" + ( lthresholdcrossingcount + 1 ) );
			justcrossedThreshold = -1;
			lthresholdcrossingcount++;
		} else {
			justcrossedThreshold = 0;
		}
		
		
		switch (colormode)
		{
			case Default    :{ color = nextcolx; break; }
			case AlphaBlend :{color = MiniPixelTools.blendColors(nextcolx,prevcolx,(int)((Math.min(stability,0xff))*255.0f));break;}
			case ChannelMax :
			{
				if (nxdef > prdef) color = MiniPixelTools.blendColors(nextcolx,prevcolx,(int)(stability*255.0f));
				else color = MiniPixelTools.blendColors(nextcolx,prevcolx,255-(int)(stability*255.0f));
				//color =  nextcolx; else color =  prevcolx; 
				break;
			}
			case ChannelMin :
			{
				//if (nxdef < prdef) color =  nextcolx; else color = prevcolx; 
				if (nxdef < prdef) color = MiniPixelTools.blendColors(nextcolx,prevcolx,(int)(stability*255.0f));
				else color = MiniPixelTools.blendColors(nextcolx,prevcolx,255-(int)(stability*255.0f));
				break;
			}
			case ChannelAdd :
			{
				color = MiniPixelTools.addScalarColor(color,(int)( (float)nxdef * stability ) );
				break;
			}
			case ChannelSubtract :
			{
				color = MiniPixelTools.addScalarColor(color,(int)( (float)(-nxdef) * stability ) );
				break;
			}
			case ChannelExclusiveOR :
			{
				int newr = ((prevcolx & 0x00ff0000) >> 16) ^ (int) (nxdef * (1.0f-stability));
				int newg = ((prevcolx & 0x0000ff00) >> 8) ^ (int) (nxdef * (1.0f-stability));
				int newb = ((prevcolx & 0x000000ff)     ) ^ (int) (nxdef * (1.0f-stability));
				newr = (newr < 0) ? 0 : (newr > 0xff) ? 0xff : newr;
				newg = (newg < 0) ? 0 : (newg > 0xff) ? 0xff : newg;
				newb = (newb < 0) ? 0 : (newb > 0xff) ? 0xff : newb;
				color = (newr << 16) | (newg << 8) | newb;
				break;
			}
				
			case ChannelHue :
			{
				float tmp[] = new float[3];
				Color.RGBtoHSB((prevcolx >> 16)&0xff,(prevcolx >> 8)&0xff,prevcolx&0xff,tmp);
				//int h = (int)(tmp[0] * 0xff); //hue
				//int s = (int)(tmp[1] * 0xff); //sat
				//int b = (int)(tmp[2] * 0xff); //val
				//Color huecol = Color.getHSBColor(tmp[0],1.0f,1.0f);
				float fdef = (float)nxdef/255.0f;
				if (fdef < 0) fdef = 0;
				if (fdef > 1.0f) fdef = 1.0f;
				fdef = (stability * tmp[0])+(1.0f-stability)*fdef;
				if (fdef < 0) fdef = 0;
				if (fdef > 1.0f) fdef = 1.0f;
				color = Color.getHSBColor(fdef,tmp[1],tmp[2]).getRGB();
				break;
			}
			case ChannelSaturation :
			{
				float tmp[] = new float[3];
				Color.RGBtoHSB((prevcolx >> 16)&0xff,(prevcolx >> 8)&0xff,prevcolx&0xff,tmp);
				float fdef = (float)nxdef/255.0f;
				
				if (fdef < 0) fdef = 0;
				if (fdef > 1.0f) fdef = 1.0f;
				fdef = (stability * tmp[1])+(1.0f-stability)*fdef;
				if (fdef < 0) fdef = 0;
				if (fdef > 1.0f) fdef = 1.0f;
				color = Color.getHSBColor(tmp[0],fdef,tmp[2]).getRGB();
				break;
			}
			case ChannelLumi:
			{
				float tmp[] = new float[3];
				Color.RGBtoHSB((prevcolx >> 16)&0xff,(prevcolx >> 8)&0xff,prevcolx&0xff,tmp);
				float fdef = (float)nxdef/255.0f;
				if (fdef < 0) fdef = 0;
				if (fdef > 1.0f) fdef = 1.0f;
				fdef = (stability * tmp[2])+(1.0f-stability)*fdef;
				if (fdef < 0) fdef = 0;
				if (fdef > 1.0f) fdef = 1.0f;
				color = Color.getHSBColor(tmp[0],tmp[1],fdef).getRGB();
				break;
			}
			case ChannelRed   :
			{
				float mixer = stability*(float)((prevcolx & 0x00ff0000) >> 16) + (1.0f-stability)*(float)nxdef;
				if (mixer > 255.0f) mixer = 255.0f;
				int imix = (int)mixer;
				imix = (imix < 0) ? 0 : (imix > 0xff) ? 0xff : imix;
				color = (prevcolx & 0xff00ffff) | (imix << 16);
				break;
			}
			case ChannelGreen   :
			{
				float mixer = stability*(float)((prevcolx & 0x0000ff00) >> 8) + (1.0f-stability)*(float)nxdef;
				if (mixer > 255.0f) mixer = 255.0f;
				int imix = (int)mixer;
				imix = (imix < 0) ? 0 : (imix > 0xff) ? 0xff : imix;
				color = (prevcolx & 0xffff00ff) | (imix << 8);
				break;
			}
			case ChannelBlue   :
			{
				float mixer = stability*(float)((prevcolx & 0x000000ff) >> 8) + (1.0f-stability)*(float)nxdef;
				if (mixer > 255.0f) mixer = 255.0f;
				int imix = (int)mixer;
				imix = (imix < 0) ? 0 : (imix > 0xff) ? 0xff : imix;
				color = (prevcolx & 0xffffff00) | imix;
				break;
			}
			case Red     :      
			{
				float mixer = stability*(float)((prevcolx & 0x00ff0000) >> 16) + (1.0f-stability)*(float)((nextcolx & 0x00ff0000) >> 16);
				if (mixer > 255.0f) mixer = 255.0f;
				int imix = (int)mixer;
				imix = (imix < 0) ? 0 : (imix > 0xff) ? 0xff : imix;
				color = (prevcolx & 0xffffff00) | imix;
				break;
			}
			case Green     :
			{
				float mixer = stability*(float)((prevcolx & 0x0000ff00) >> 8) + (1.0f-stability)*(float)((nextcolx & 0x0000ff00) >> 8);
				if (mixer > 255.0f) mixer = 255.0f;
				int imix = (int)mixer;
				imix = (imix < 0) ? 0 : (imix > 0xff) ? 0xff : imix;
				color = (prevcolx & 0xffffff00) | imix;
				break;
			}
			case Blue     :      
			{
				float mixer = stability*(float)(prevcolx & 0x000000ff) + (1.0f-stability)*(float)(nextcolx & 0x000000ff);
				if (mixer > 255.0f) mixer = 255.0f;
				int imix = (int)mixer;
				imix = (imix < 0) ? 0 : (imix > 0xff) ? 0xff : imix;
				color = (prevcolx & 0xffffff00) | imix;
				break;
			}
			case Add   :      
			{
				int newr = ((prevcolx & 0x00ff0000) >> 16) - (int)( (float)(  (nextcolx & 0x00ff0000) >> 16 ) * (1.0f-stability) );
				int newg = ((prevcolx & 0x0000ff00) >> 8) - (int)( (float)(  (nextcolx & 0x0000ff00) >> 8  ) * (1.0f-stability) );
				int newb = ((prevcolx & 0x000000ff)     ) - (int)( (float)(   nextcolx & 0x000000ff        ) * (1.0f-stability) );
				newr = (newr < 0) ? 0 : (newr > 0xff) ? 0xff : newr;
				newg = (newg < 0) ? 0 : (newg > 0xff) ? 0xff : newg;
				newb = (newb < 0) ? 0 : (newb > 0xff) ? 0xff : newb;
				color = (newr << 16) | (newg << 8) | newb;
				break;
			}
			case Subtract   : 
			{
				int newr = ((prevcolx & 0x00ff0000) >> 16) - (int)( (float)(  (nextcolx & 0x00ff0000) >> 16 ) * (1.0f-stability) );
				int newg = ((prevcolx & 0x0000ff00) >> 8) - (int)( (float)(   (nextcolx & 0x0000ff00) >> 8  ) * (1.0f-stability) );
				int newb = ((prevcolx & 0x000000ff)     ) - (int)( (float)(    nextcolx & 0x000000ff        ) * (1.0f-stability) );
				newr = (newr < 0) ? 0 : (newr > 0xff) ? 0xff : newr;
				newg = (newg < 0) ? 0 : (newg > 0xff) ? 0xff : newg;
				newb = (newb < 0) ? 0 : (newb > 0xff) ? 0xff : newb;
				color = (newr << 16) | (newg << 8) | newb;
				break;
			}
			case ExlusiveOR   :
			{
				int newr = ((prevcolx & 0x00ff0000) >> 16) - (int)( (float)(  (nextcolx & 0x00ff0000) >> 16 ) * (1.0f-stability) );
				int newg = ((prevcolx & 0x0000ff00) >> 8) - (int)( (float)(  (nextcolx &  0x0000ff00) >> 8  ) * (1.0f-stability) );
				int newb = ((prevcolx & 0x000000ff)     ) - (int)( (float)(   nextcolx &  0x000000ff        ) * (1.0f-stability) );
				newr = ((newr < 0) ? 0 : (newr > 0xff) ? 0xff : newr);
				newg = ((newg < 0) ? 0 : (newg > 0xff) ? 0xff : newg);
				newb = ((newb < 0) ? 0 : (newb > 0xff) ? 0xff : newb);
				int alpha = (int)(stability*0xff);
				alpha = (alpha > 0xff) ? 0xff000000 : (alpha < 0) ? 00000000 : alpha << 24;
				color = MiniPixelTools.pixelAlphaBlend(alpha | (newr << 16) | (newg << 8) | newb, color);
				break;
			}
			case Max   :
			{
				int newr = (prevcolx & 0x00ff0000) >> 16;
				int nxr =  (nextcolx & 0x00ff0000) >> 16;
				if (nxr > newr) newr = nxr;
				int newg = (prevcolx & 0x0000ff00) >> 8;
				int nxg =  (nextcolx & 0x0000ff00) >> 8;
				if (nxg > newg) newg = nxg;
				int newb = prevcolx & 0x000000ff;
				int nxb =  nextcolx & 0x000000ff;
				if (nxb > newb) newr = nxb;
				int alpha = (int)(stability*0xff);
				alpha = (alpha > 0xff) ? 0xff000000 : (alpha < 0) ? 00000000 : alpha << 24;
				color = MiniPixelTools.pixelAlphaBlend(alpha | (newr << 16) | (newg << 8) | newb, color);
				break;
			}
			case Min   :      
			{
				int newr = (prevcolx & 0x00ff0000) >> 16;
				int nxr =  (nextcolx & 0x00ff0000) >> 16;
				if (nxr < newr) newr = nxr;
				int newg = (prevcolx & 0x0000ff00) >> 8;
				int nxg =  (nextcolx & 0x0000ff00) >> 8;
				if (nxg < newg) newg = nxg;
				int newb = prevcolx & 0x000000ff;
				int nxb =  nextcolx & 0x000000ff;
				if (nxb < newb) newr = nxb;
				color = (newr << 16) | (newg << 8) | newb;
				break;
			}
			case Hue   :      
			{
				float tmp[] = new float[3];
				Color.RGBtoHSB((nextcolx >> 16)&0xff,(nextcolx >> 8)&0xff,nextcolx&0xff,tmp);
				float nexthue = tmp[0];
				Color.RGBtoHSB((prevcolx >> 16)&0xff,(prevcolx >> 8)&0xff,prevcolx&0xff,tmp);
				float mixu = (stability * tmp[0]) + (1.0f - stability)*nexthue;
				mixu = mixu % 1.0f; //wrap the hue channel
				if (mixu < 0) mixu += 1.0f;
				color = Color.getHSBColor(mixu,tmp[1],tmp[2]).getRGB();
				break;
			}
			case Saturation   :      
			{
				float tmp[] = new float[3];
				Color.RGBtoHSB((nextcolx >> 16)&0xff,(nextcolx >> 8)&0xff,nextcolx&0xff,tmp);
				float nextsat = tmp[1];
				Color.RGBtoHSB((prevcolx >> 16)&0xff,(prevcolx >> 8)&0xff,prevcolx&0xff,tmp);
				float mixu = (stability * tmp[1]) + (1.0f - stability)*nextsat;
				if (mixu < 0) mixu = 0;
				if (mixu > 1.0f) mixu = 1.0f;
				color = Color.getHSBColor(tmp[0],mixu,tmp[2]).getRGB();
				break;
			}
			case Luminosity  :      
			{
				float tmp[] = new float[3];
				Color.RGBtoHSB((nextcolx >> 16)&0xff,(nextcolx >> 8)&0xff,nextcolx&0xff,tmp);
				float nextlumi = tmp[2];
				Color.RGBtoHSB((prevcolx >> 16)&0xff,(prevcolx >> 8)&0xff,prevcolx&0xff,tmp);
				float mixu = (stability * tmp[2]) + (1.0f - stability)*nextlumi;
				if (mixu < 0) mixu = 0;
				if (mixu > 1.0f) mixu = 1.0f;
				color = Color.getHSBColor(tmp[0],tmp[1],mixu).getRGB();
				break;
			}
			case ruleColorLifespan:
			{
				if (lifespan > 0)
				{
					float adjage = (float) age / (float) lifespan;
					if (adjage < 0.0f) adjage = 0.0f;
					if (adjage > 1.0f) adjage = 1.0f;
					int[] rule = samplesource.getColorHarvest();
					if (rule != null)
					{
						int rulelookup = (int) ( adjage * (rule.length - 1) );
						color = rule[ rulelookup ]; 
					}
				}
				break;
			}
		}
		colorXtract = nxdef;
	    return (alpha<<24)|color;
	}
	             
	public void setStability(float towhat)
	{if (towhat >= 0.0f) stability = towhat; else stability = 0.0f;}
	
	public particleColorMode getColorMode()
	{return colormode;}
	
	public void setColorMode( int towhat )
	{ colormode = particleColorMode.values()[towhat]; }
	
	public void setColorMode( particleColorMode towhat )
	{ colormode = towhat; }
	
	public particleDrawMode getDrawMode()
	{return drawmode;}
	
	public void setDrawMode(int towhat)
	{ drawmode = particleDrawMode.values()[towhat]; }
	
	public void setDrawMode(particleDrawMode towhat)
	{ drawmode = towhat; }
	
	//reset the tracking of has-been-drawn for each neighbor connection
	void refreshDrawNeighbors()
	{
		for (int n = 0; n < numneighbors; n++)
		{
			//if (isreciprocalneighbor[n])
			//{
				neighborlinkdrawn[n] = false;
			//}
		}
	}
	
	void resetDrawNeighbors(boolean to)
	{
		for (int n = 0; n < numneighbors; n++)
		{
			neighborlinkdrawn[n] = to;
		}
	}
	
	void drawNeighbors(Graphics2D where, ParticleSystem memates)
	{
		where.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		where.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		where.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        where.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        where.setColor(getColor());
        
        for (int n = 0; n < numneighbors; n++)
        {
        	if ((neighbors[n] > 0) && (!neighborlinkdrawn[n]))
        	{
        		where.setStroke(new BasicStroke(strokewidth*size,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
				
				int xs[] = new int[2];
				int ys[] = new int[2];
				
				Particle neighbor = memates.getParticle(neighbors[n]);
				
				if (neighbor.isActive())
				{
				
					float nloc[] = neighbor.getDrawLocation();
					int tile[] = neighbor.getTilePosition();
					
					xs[0] = (int)(drawlocation[0] + tilepos[0]*drawdimensionclip[0]);
					ys[0] = (int)(drawlocation[1] + tilepos[1]*drawdimensionclip[1]);
					
					xs[1] = (int)(nloc[0] + tile[0]*drawdimensionclip[0]);
					ys[1] = (int)(nloc[1] + tile[1]*drawdimensionclip[1]);
					
					where.drawPolyline(xs,ys,2);
					
					if (isreciprocalneighbor[n])
					{
						//check sanity...
						if ((reciprocalneighborID[n] > 0) && (reciprocalneighborID[n] < neighbor.neighborlinkdrawn.length))
						{
							//tell the neighbor the bidirectional link has already been drawn
							neighbor.neighborlinkdrawn[reciprocalneighborID[n]] = true;
						}
					}
				} else { // particle is no longer active .. disable neighbor
					isreciprocalneighbor[n] = false;
					neighbors[n] = -1;
					reciprocalneighborID[n] = -1;
				}
        	}
        }
        
	}
	
	void draw(int[] where)
	{
		float speedlength = getLength(delta);
		switch (drawmode)
		{
			case AlphaPIXELS:
			{
				for (int i = ppixw*ppixh-1; i >= 0; i--)
				{
					ppixel[i] = ((ppixel[i] & 0xff000000) | (color & 0x00ffffff));
				}
				
				//switch (colormode){}
				
				MiniPixelTools.drawWithAlpha((int)samplelocation[0] - ppixw/2,(int)samplelocation[1] - ppixh/2,
				                             visual,dimensionclip[0],dimensionclip[1],
				                             ppixel,ppixw,ppixh);
				break;
			}
			case PIXELS:
			{	
				MiniPixelTools.renderLinearFalloffMapAt(visual, dimensionclip[0], dimensionclip[1], 0, 1, 1, 1, samplelocation[0], samplelocation[1], ppixw, ppixh);
				break;
			}
		}
		
	}
	
	void draw(Graphics2D where)
	{
		where.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		where.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		where.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        	where.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        	//where.setStroke(new BasicStroke(size,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        
        
        	//System.out.println("draw [" + location[0] + "," + location[1] + "]");
		//assumes a 2d particle
		Color pColor = getColor();
		
		//if (colorXtract > lthreshold)
		//{
		//if (justcrossedThreshold == -1)
		//{
		//	where.setColor(  Color.black );
		//	
		//} else if (justcrossedThreshold == 1) {
		//	
		//	where.setColor(  Color.white );
		//	
		//} else {
			where.setColor( pColor );
		//}
		//} else {
		//	where.setColor( new Color( 255-pColor.getRed(), 255-pColor.getGreen(), 255-pColor.getBlue() ) );
		//}
		float speedlength = getLength( delta );
		
		//if (!justclipped)
		//{
		/*
		FillCircle,
		FillSquare,
		Fill3DRectangle,
		ZapPolygon,
		TimePolygon,
		DiamondPolygon,
		ParallelogramPolygon,
		BowtiePolygon,
		RectangleRibbon,
		Line,
		Circle,
		Square,
		AgeText,
		MidpointPolygon,
		AlphaPIXELS,
		PIXELS,
		Zap,
		TrackAndRail,
		Track,
		Rail,
		Helix;
		*/
			
		switch (drawmode)
		{
			case FillCircle:
			{
				//yr basic filled oval. . .
				where.fillOval((int)(drawlocation[0] - size/2), (int)(drawlocation[1] - size/2), (int) size, (int) size);
			    	if(tiledraw)
		  		{
					if (drawlocation[0] < size/2) //off the left, add width
					where.fillOval((int)((drawdimensionclip[0]+drawlocation[0]) - size/2), (int)(drawlocation[1] - size/2), (int) size, (int) size);
					if (drawlocation[1] < size/2) //off the top, add height
					where.fillOval( (int)(drawlocation[0] - size/2), (int)( (drawdimensionclip[1] + drawlocation[1]) - size/2), (int) size, (int) size);
					if ((drawlocation[0]+size/2) > drawdimensionclip[0]) //off the right, sub width
					where.fillOval((int)( (drawlocation[0]-drawdimensionclip[0]) - size/2 ), (int)(drawlocation[1] - size/2), (int) size, (int) size);
					if ((drawlocation[1]+size/2) > drawdimensionclip[1]) //off the bottom, sub height
					where.fillOval((int)(drawlocation[0] - size/2), (int)((drawlocation[1]-drawdimensionclip[1]) - size/2), (int) size, (int) size);
			  	}
				break;
		   	}
		   	
		   	case FillSquare    :
		 	{
		 		where.fillRect( (int)(drawlocation[0] - size/2), (int)(drawlocation[1] - size/2), (int) size, (int) size);
		    		if(tiledraw)
		  		{
					if (drawlocation[0] < size/2) //off the left, add width
					where.fillRect((int)((drawdimensionclip[0]+drawlocation[0]) - size/2), (int)(drawlocation[1] - size/2), (int) size, (int) size);
					if (drawlocation[1] < size/2) //off the top, add height
					where.fillRect( (int)(drawlocation[0] - size/2), (int)( (drawdimensionclip[1] + drawlocation[1]) - size/2), (int) size, (int) size);
					if ((drawlocation[0]+size/2) > drawdimensionclip[0]) //off the right, sub width
					where.fillRect((int)( (drawlocation[0]-drawdimensionclip[0]) - size/2 ), (int)(drawlocation[1] - size/2), (int) size, (int) size);
					if ((drawlocation[1]+size/2) > drawdimensionclip[1]) //off the bottom, sub height
					where.fillRect((int)(drawlocation[0] - size/2), (int)((drawlocation[1]-drawdimensionclip[1]) - size/2), (int) size, (int) size);
			  	}
				break;
			}
		   	
		   	case Fill3DRectangle  :
			{
				where.fill3DRect( (int)(drawlocation[0] - size/2), (int)(drawlocation[1] - size/2),  (int) size, (int) size , true);
				if(tiledraw)
		  		{
		  			if (drawlocation[0] < size/2) //off the left, add width
			  		where.fill3DRect((int)((drawdimensionclip[0]+drawlocation[0]) - size/2), (int)(drawlocation[1] - size/2), (int) size, (int) size,true);
			  		if (drawlocation[1] < size/2) //off the top, add height
			  		where.fill3DRect( (int)(drawlocation[0] - size/2), (int)( (drawdimensionclip[1] + drawlocation[1]) - size/2), (int) size, (int) size,true);
			  		if ((drawlocation[0]+size/2) > drawdimensionclip[0]) //off the right, sub width
			  		where.fill3DRect((int)( (drawlocation[0]-drawdimensionclip[0]) - size/2 ), (int)(drawlocation[1] - size/2), (int) size, (int) size,true);
			  		if ((drawlocation[1]+size/2) > drawdimensionclip[1]) //off the bottom, sub height
			  		where.fill3DRect((int)(drawlocation[0] - size/2), (int)((drawlocation[1]-drawdimensionclip[1]) - size/2), (int) size, (int) size,true);
			  	}
				break;
			}
			
			case ZapPolygon   :
			{
				int xs[] = new int[4];
				int ys[] = new int[4];
				xs[0] = (int)(lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) - delta[1] * lastsize/2);
				ys[0] = (int)(lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) - delta[0] * lastsize/2);
				xs[1] = (int)(lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) + delta[1] * lastsize/2);
				ys[1] = (int)(lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) + delta[0] * lastsize/2);
				xs[2] = (int)(drawlocation[0] - delta[1] * size/2);
				ys[2] = (int)(drawlocation[1] - delta[0] * size/2);
				xs[3] = (int)(drawlocation[0] + delta[1] * size/2);
				ys[3] = (int)(drawlocation[1] + delta[0] * size/2);
				where.fillPolygon(xs,ys,4);
				break;
			}
			
			case TimePolygon :
			{
				int xs[] = new int[4];
				int ys[] = new int[4];
				
				float aaxs[] = new float[2];
				float aays[] = new float[2];
				
				float xbit = size * (delta[1]/speedlength);
				float ybit = size * (delta[0]/speedlength);
				
				xs[0] = (int)(polypoints[0]) - ( justclipped[0] * drawdimensionclip[0] );
				ys[0] = (int)(polypoints[1]) - ( justclipped[1] * drawdimensionclip[1] );
				xs[1] = (int)(polypoints[2]) - ( justclipped[0] * drawdimensionclip[0] );
				ys[1] = (int)(polypoints[3]) - ( justclipped[1] * drawdimensionclip[1] );
				
				aaxs[0] = drawlocation[0] - xbit;
				aays[0] = drawlocation[1] + ybit;
				aaxs[1] = drawlocation[0] + xbit;
				aays[1] = drawlocation[1] - ybit;
				
				float drawAlpha = pColor.getAlpha() / 255.0f;
				
				float xAAadj =  (delta[0]/speedlength) * drawAlpha; //three quarters of a FRICKING PIXEL HEY :D heheh
				float yAAadj = (delta[1]/speedlength) * drawAlpha; //flicking hope this works
				
				//well.. so it doesn't reeeealy work but its better than nothing for the moment.. erg. this is tricky - getting 2 antialiased polys to line up perfectly for any arbitrary opacity
				// at least this works for completely opaque polys (no gap) and reduces the bright line of intersection that you get when you draw with less than full opacity
				
				polypoints[0] = (int)aaxs[1];
				polypoints[1] = (int)aays[1];
				polypoints[2] = (int)aaxs[0];
				polypoints[3] = (int)aays[0];
				
				xs[2] = (int)(aaxs[0] + xAAadj);
				ys[2] = (int)(aays[0] + yAAadj);
				xs[3] = (int)(aaxs[1] + xAAadj);
				ys[3] = (int)(aays[1] + yAAadj);
				
				
				where.fillPolygon(xs,ys,4);
				
				//if (tiledraw)
				//{
					//this should now choose where extra to draw based on the states of justclipped.. actually that is bollocks
					//.. it should clip the polygon and decide where else to draw it based on whether it overlaps the horizontal or vertical borders, or both
					// numdraws will go up to 4 when the origin (0,0) in raster space - in fact if any of the 'corners' - is/are covered by the polygon
					// numdraws will never be 3, only 1, 2 or 4
					
				if (tiledraw)
				{
					if ((xs[0] < 0) | (xs[1] < 0) | (xs[2] < 0) | (xs[3] < 0))
					{
						xs[0] += drawdimensionclip[0];
						xs[1] += drawdimensionclip[0];
						xs[2] += drawdimensionclip[0];
						xs[3] += drawdimensionclip[0];
						where.fillPolygon(xs,ys,4);
					}
					if ((ys[0] < 0) | (ys[1] < 0) | (ys[2] < 0) | (ys[3] < 0))
					{
						ys[0] += drawdimensionclip[1];
						ys[1] += drawdimensionclip[1];
						ys[2] += drawdimensionclip[1];
						ys[3] += drawdimensionclip[1];
						where.fillPolygon(xs,ys,4);
					}
					if ((xs[0] > drawdimensionclip[0]) | (xs[1] > drawdimensionclip[0]) | (xs[2] > drawdimensionclip[0]) | (xs[3] > drawdimensionclip[0]))
					{
						xs[0] -= drawdimensionclip[0];
						xs[1] -= drawdimensionclip[0];
						xs[2] -= drawdimensionclip[0];
						xs[3] -= drawdimensionclip[0];
						where.fillPolygon(xs,ys,4);
					}
					if ((ys[0] > drawdimensionclip[1]) | (ys[1] > drawdimensionclip[1]) | (ys[2] > drawdimensionclip[1]) | (ys[3] > drawdimensionclip[1]))
					{
						ys[0] -= drawdimensionclip[1];
						ys[1] -= drawdimensionclip[1];
						ys[2] -= drawdimensionclip[1];
						ys[3] -= drawdimensionclip[1];
						where.fillPolygon(xs,ys,4);
					}
				}
				
				break;
			}
			
			case DiamondPolygon :
			{
				int xs[] = new int[4];
				int ys[] = new int[4];
				float midx = lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) + delta[0]/2;
				float midy = lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) + delta[1]/2;
				
				xs[0] = (int)(lastloc[0]- ( justclipped[0] * drawdimensionclip[0] ) );
				ys[0] = (int)(lastloc[1]- ( justclipped[1] * drawdimensionclip[1] ) );
				xs[1] = (int)(midx + size * (delta[1]/speedlength) );
				ys[1] = (int)(midy + size * (delta[0]/speedlength) );
				xs[2] = (int)(drawlocation[0]);
				ys[2] = (int)(drawlocation[1]);
				xs[3] = (int)(midx - size * (delta[1]/speedlength) );
				ys[3] = (int)(midy - size * (delta[0]/speedlength) );
				where.fillPolygon(xs,ys,4);
				break;
			}
			
			case ParallelogramPolygon :
			{
				int xs[] = new int[4];
				int ys[] = new int[4];
				xs[0] = (int)(polypoints[0]) - ( justclipped[0] * drawdimensionclip[0] );
				ys[0] = (int)(polypoints[1]) - ( justclipped[1] * drawdimensionclip[1] );
				xs[1] = (int)(polypoints[2]) - ( justclipped[0] * drawdimensionclip[0] );
				ys[1] = (int)(polypoints[3]) - ( justclipped[1] * drawdimensionclip[1] );
				xs[2] = (int)( drawlocation[0] - size * (delta[1]/speedlength) );
				ys[2] = (int)( drawlocation[1] - size * (delta[0]/speedlength) );
				xs[3] = (int)( drawlocation[0] + size * (delta[1]/speedlength) );
				ys[3] = (int)( drawlocation[1] + size * (delta[0]/speedlength) );
				where.fillPolygon(xs,ys,4);
				polypoints[0] = xs[3];
				polypoints[1] = ys[3];
				polypoints[2] = xs[2];
				polypoints[3] = ys[2];
				break;
			}
			
			case BowtiePolygon  :
			{
				int xs[] = new int[4];
				int ys[] = new int[4];
				xs[0] = (int)(lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) - delta[1] * lastsize/2);
				ys[0] = (int)(lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) - delta[0] * lastsize/2);
				xs[2] = (int)(lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) + delta[1] * lastsize/2);
				ys[2] = (int)(lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) + delta[0] * lastsize/2);
				xs[1] = (int)(drawlocation[0] - delta[1] * size/2);
				ys[1] = (int)(drawlocation[1] - delta[0] * size/2);
				xs[3] = (int)(drawlocation[0] + delta[1] * size/2);
				ys[3] = (int)(drawlocation[1] + delta[0] * size/2);
				where.fillPolygon(xs,ys,4);
				break;
			}
		 	
		 	case RectangleRibbon:
			{
				int topx = (int)( (drawlocation[0] < lastloc[0] - ( justclipped[0] * drawdimensionclip[0] )) ? (drawlocation[0]) : (lastloc[0]) - ( justclipped[0] * drawdimensionclip[0] ) );
				int topy = (int)( (drawlocation[1] < lastloc[1] - ( justclipped[1] * drawdimensionclip[1] )) ? (drawlocation[1]) : (lastloc[1]) - ( justclipped[1] * drawdimensionclip[1] ) );
				
				int w = (int) (drawlocation[0] - ( lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) ) );
				w = (w < 0) ? -w : w;
				int h = (int)(drawlocation[1] - lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) );
				h = (h < 0) ? -h : h;
				
				int sizeDeltaX = (int)(w * size/2);
				int sizeDeltaY = (int)(h * size/2);
				
				where.fillRect(topx - sizeDeltaX, topy - sizeDeltaY, w + sizeDeltaX, h + sizeDeltaY);
				break;
			}
			
		 	case Line:
			{
				//if ( ( justclipped[0] == 0) && ( justclipped[1] == 0) )
				//{
				where.setStroke(new BasicStroke(strokewidth*size,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
				int xs[] = new int[2];
				int ys[] = new int[2];
				
				xs[0] = (int) lastloc[0] - ( justclipped[0] * drawdimensionclip[0] );
				ys[0] = (int) lastloc[1] - ( justclipped[1] * drawdimensionclip[1] );
				
				xs[1] = (int)drawlocation[0];
				ys[1] = (int)drawlocation[1];
				
				where.drawPolyline(xs,ys,2);
				//}
				/*where.drawLine( (int)lastloc[0], (int)lastloc[1], (int)location[0], (int)location[1] );
				if (tiledraw)
				{
					if ((lastloc[0] < 0) | (location[0] < 0)) //off left, add width to x
					where.drawLine( (int)lastloc[0]+dimensionclip[0], (int)lastloc[1], (int)location[0]+dimensionclip[0], (int)location[1] );
					if ((lastloc[1] < 0) | (location[1] < 0)) //off right, add height to y
					where.drawLine( (int)lastloc[0], (int)lastloc[1]+dimensionclip[1], (int)location[0], (int)location[1] + dimensionclip[1]);
					if ((lastloc[0] >= dimensionclip[0]) | (location[0] >= dimensionclip[0])) //off right, sub width from x
					where.drawLine( (int)lastloc[0]-dimensionclip[0], (int)lastloc[1], (int)location[0]-dimensionclip[0], (int)location[1] );
					if ((lastloc[0] >= dimensionclip[0]) | (location[0] >= dimensionclip[0])) //off bottom, sub height from y
					where.drawLine( (int)lastloc[0], (int)lastloc[1]-dimensionclip[1], (int)location[0], (int)location[1]-dimensionclip[1] );
				}*/
				
				
		 		break;
		 	}
		 	
		 	case Circle:
			{
				where.drawOval( (int)(drawlocation[0] - size/2),  (int)(drawlocation[1] - size/2), (int) size, (int) size);
		  		if(tiledraw)
		  		{
		  			if (drawlocation[0] < size/2) //off the left, add width
			  		where.drawOval((int)((drawdimensionclip[0]+drawlocation[0]) - size/2), (int)(drawlocation[1] - size/2), (int) size, (int) size);
			  		if (drawlocation[1] < size/2) //off the top, add height
			  		where.drawOval( (int)(drawlocation[0] - size/2), (int)( (drawdimensionclip[1] + drawlocation[1]) - size/2), (int) size, (int) size);
			  		if ((drawlocation[0]+size/2) > drawdimensionclip[0]) //off the right, sub width
			  		where.drawOval((int)( (drawlocation[0]-drawdimensionclip[0]) - size/2 ), (int)(drawlocation[1] - size/2), (int) size, (int) size);
			  		if ((drawlocation[1]+size/2) > drawdimensionclip[1]) //off the bottom, sub height
			  		where.drawOval((int)(drawlocation[0] - size/2), (int)((drawlocation[1]-drawdimensionclip[1]) - size/2), (int) size, (int) size);
			  	}
		 		break;
		 	}
		 	
			case Square :
			{
				where.setStroke(new BasicStroke(strokewidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
				where.drawRect( (int)(drawlocation[0] - size/2),
		    		           		 (int)(drawlocation[1] - size/2), (int) size, (int) size);
		    		if(tiledraw)
		  		{
		  			if (drawlocation[0] < size/2) //off the left, add width
			  		where.drawRect((int)((drawdimensionclip[0]+drawlocation[0]) - size/2), (int)(drawlocation[1] - size/2), (int) size, (int) size);
			  		if (drawlocation[1] < size/2) //off the top, add height
			  		where.drawRect( (int)(drawlocation[0] - size/2), (int)( (drawdimensionclip[1] + drawlocation[1]) - size/2), (int) size, (int) size);
			  		if ((drawlocation[0]+size/2) > drawdimensionclip[0]) //off the right, sub width
			  		where.drawRect((int)( (drawlocation[0]-drawdimensionclip[0]) - size/2 ), (int)(drawlocation[1] - size/2), (int) size, (int) size);
			  		if ((drawlocation[1]+size/2) > drawdimensionclip[1]) //off the bottom, sub height
			  		where.drawRect((int)(drawlocation[0] - size/2), (int)((drawlocation[1]-drawdimensionclip[1]) - size/2), (int) size, (int) size);
			  	}
				break;
			}
			
			case AgeText :
			{
				//Font f = where.getFont();
				FontMetrics fm = where.getFontMetrics();
				//set font to right size..
				//Font fontoid = where.getFont();
				
				String s = new String("[" + age + "]");
				int xpos = (int)drawlocation[0] - fm.stringWidth(s);
				int ypos = (int)drawlocation[1] - fm.getHeight();
				where.drawString(s,xpos,ypos);
				break;
			}
			
			case MidpointPolygon :
			{
				int xs[] = new int[4];
				int ys[] = new int[4];
				float midx = lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) + delta[0]/2;
				float midy = lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) + delta[1]/2;
				
				xs[0] = (int)(polypoints[0]) - ( justclipped[0] * drawdimensionclip[0] );
				ys[0] = (int)(polypoints[1]) - ( justclipped[1] * drawdimensionclip[1] );
				xs[1] = (int)(polypoints[2]) - ( justclipped[0] * drawdimensionclip[0] );
				ys[1] = (int)(polypoints[3]) - ( justclipped[1] * drawdimensionclip[1] );
				xs[2] = (int)(midx - size * (delta[1]/speedlength) );
				ys[2] = (int)(midy + size * (delta[0]/speedlength) );
				xs[3] = (int)(midx + size * (delta[1]/speedlength) );
				ys[3] = (int)(midy - size * (delta[0]/speedlength) );
				
				polypoints[0] = xs[3];
				polypoints[1] = ys[3];
				polypoints[2] = xs[2];
				polypoints[3] = ys[2];
				
				where.fillPolygon(xs,ys,4);
				if (tiledraw)
				{
					if ((xs[0] < 0) | (xs[1] < 0) | (xs[2] < 0) | (xs[3] < 0))
					{
						xs[0] += drawdimensionclip[0];
						xs[1] += drawdimensionclip[0];
						xs[2] += drawdimensionclip[0];
						xs[3] += drawdimensionclip[0];
						where.fillPolygon(xs,ys,4);
					}
					if ((ys[0] < 0) | (ys[1] < 0) | (ys[2] < 0) | (ys[3] < 0))
					{
						ys[0] += drawdimensionclip[1];
						ys[1] += drawdimensionclip[1];
						ys[2] += drawdimensionclip[1];
						ys[3] += drawdimensionclip[1];
						where.fillPolygon(xs,ys,4);
					}
					if ((xs[0] > drawdimensionclip[0]) | (xs[1] > drawdimensionclip[0]) | (xs[2] > drawdimensionclip[0]) | (xs[3] > drawdimensionclip[0]))
					{
						xs[0] -= drawdimensionclip[0];
						xs[1] -= drawdimensionclip[0];
						xs[2] -= drawdimensionclip[0];
						xs[3] -= drawdimensionclip[0];
						where.fillPolygon(xs,ys,4);
					}
					if ((ys[0] > drawdimensionclip[1]) | (ys[1] > drawdimensionclip[1]) | (ys[2] > drawdimensionclip[1]) | (ys[3] > drawdimensionclip[1]))
					{
						ys[0] -= drawdimensionclip[1];
						ys[1] -= drawdimensionclip[1];
						ys[2] -= drawdimensionclip[1];
						ys[3] -= drawdimensionclip[1];
						where.fillPolygon(xs,ys,4);
					}
				}
				
				break;
			}
			
			case Zap :
			{
				where.setStroke(new BasicStroke(strokewidth*size,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER));
				int xs[] = new int[3];
				int ys[] = new int[3];
				
				float midx = lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) + delta[0]/2;
				float midy = lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) + delta[1]/2;
				
				float xbit = size * (delta[1]/speedlength);
				float ybit = size * (delta[0]/speedlength);
				
				xs[0] = (int)(lastloc[0]) - ( justclipped[0] * drawdimensionclip[0] ) ;
				ys[0] = (int)(lastloc[1]) - ( justclipped[1] * drawdimensionclip[1] ) ;
				//xs[1] = (int)(polypoints[2]);
				//ys[1] = (int)(polypoints[3]);
				if ((age % 2) == 0)
				{
					xs[1] = (int)(midx - xbit);
					ys[1] = (int)(midy + ybit);
				} else {
					xs[1] = (int)(midx + xbit);
					ys[1] = (int)(midy - ybit);
				}
				
				xs[2] = (int)(drawlocation[0]);
				ys[2] = (int)(drawlocation[1]);
				
				where.drawPolyline(xs,ys,3);
				break;
			}
			
			
			case TrackAndRail:
			{
				where.setStroke(new BasicStroke(strokewidth*size,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
				
				int xs[] = new int[2];
				int ys[] = new int[2];
				
				//float midx = lastloc[0] + delta[0]/2;
				//float midy = lastloc[1] + delta[1]/2;
				
				float xbit = size * (delta[1]/speedlength);
				float ybit = size * (delta[0]/speedlength);
				
				xs[0] = (int)(drawlocation[0] - xbit);
				ys[0] = (int)(drawlocation[1] + ybit);
				
				xs[1] = (int)(drawlocation[0] + xbit);
				ys[1] = (int)(drawlocation[1] - ybit);
				
				where.drawPolyline(xs,ys,2);
				
				float xbitl = lastsize * (delta[1]/speedlength);
				float ybitl = lastsize * (delta[0]/speedlength);
				
				xbit = size * (delta[1]/speedlength);
				ybit = size * (delta[0]/speedlength);
				
				xs[0] = (int)(lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) - xbitl);
				ys[0] = (int)(lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) + ybitl);
				
				xs[1] = (int)(drawlocation[0] - xbit);
				ys[1] = (int)(drawlocation[1] + ybit);
				
				where.drawPolyline(xs,ys,2);
				
				xs[0] = (int)(lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) + xbitl);
				ys[0] = (int)(lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) - ybitl);
				
				xs[1] = (int)(drawlocation[0] + xbit);
				ys[1] = (int)(drawlocation[1] - ybit);
				
				where.drawPolyline(xs,ys,2);
				
				break;
			}
			
			case Track :
			{
				where.setStroke(new BasicStroke(strokewidth*size,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
				
				int xs[] = new int[2];
				int ys[] = new int[2];
				
				//float midx = lastloc[0] + delta[0]/2;
				//float midy = lastloc[1] + delta[1]/2;
				
				float xbit = size * (delta[1]/speedlength);
				float ybit = size * (delta[0]/speedlength);
				
				xs[0] = (int)(drawlocation[0] - xbit);
				ys[0] = (int)(drawlocation[1] + ybit);
				
				xs[1] = (int)(drawlocation[0] + xbit);
				ys[1] = (int)(drawlocation[1] - ybit);
				
				where.drawPolyline(xs,ys,2);
				
				break;
			}
			
			case Rail :
			{
				where.setStroke(new BasicStroke(strokewidth*size,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
				
				int xs[] = new int[2];
				int ys[] = new int[2];
				
				float xbitl = lastsize * (delta[1]/speedlength);
				float ybitl = lastsize * (delta[0]/speedlength);
				
				float xbit = size * (delta[1]/speedlength);
				float ybit = size * (delta[0]/speedlength);
				
				xs[0] = (int)(lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) - xbitl);
				ys[0] = (int)(lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) + ybitl);
				
				xs[1] = (int)(drawlocation[0] - xbit);
				ys[1] = (int)(drawlocation[1] + ybit);
				
				where.drawPolyline(xs,ys,2);
				
				xs[0] = (int)(lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) + xbitl);
				ys[0] = (int)(lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) - ybitl);
				
				xs[1] = (int)(drawlocation[0] + xbit);
				ys[1] = (int)(drawlocation[1] - ybit);
				
				where.drawPolyline(xs,ys,2);
				
				break;
			}
			
			case Helix :
			{
				where.setStroke(new BasicStroke(strokewidth*size,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_MITER));
				
				int xs[] = new int[2];
				int ys[] = new int[2];
				
				float sine = (float)Math.sin((float) age * lfofreq);
				float sinex = (float)Math.sin((float) (age-1) * lfofreq);
				
				float lcosine = (float)Math.cos((float) age * lfofreq + Math.PI*2);
				float lcosinex = (float)Math.cos((float) (age-1) * lfofreq + Math.PI*2);

				float xbit = size * (delta[1]/speedlength);
				float ybit = size * (delta[0]/speedlength);
				
				float xbitl = lastsize * (delta[1]/speedlength);
				float ybitl = lastsize * (delta[0]/speedlength);
				
				xs[0] = (int)(lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) - xbitl * sinex);
				ys[0] = (int)(lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) + ybitl * sinex);
				
				xs[1] = (int)(drawlocation[0] - xbit * sine);
				ys[1] = (int)(drawlocation[1] + ybit * sine);
				
				where.drawPolyline(xs,ys,2);
				
				xs[0] = (int)(lastloc[0] - ( justclipped[0] * drawdimensionclip[0] ) + xbitl * lcosinex);
				ys[0] = (int)(lastloc[1] - ( justclipped[1] * drawdimensionclip[1] ) - ybitl * lcosinex);
				
				xs[1] = (int)(drawlocation[0] + xbit * lcosine);
				ys[1] = (int)(drawlocation[1] - ybit * lcosine);
				
				where.drawPolyline(xs,ys,2);
								
				break;
			}
			
			/*case Arc  : 
			{	
				// from the last velocity figure out which direction the curvature should go in..
				// then draw an arc to fit in the "velocity rectangle" with the appropriate orientation
				// 
				// 
				int topx = (int)( (drawlocation[0] < lastloc[0]) ? (drawlocation[0]) : (lastloc[0]) );
				int topy = (int)( (drawlocation[1] < lastloc[1]) ? (drawlocation[1]) : (lastloc[1]) );
				int w = (int)Math.abs(drawlocation[0] - lastloc[0]);
				int h = (int)Math.abs(drawlocation[1] - lastloc[1]);
				where.drawArc(topx, topy, w, h, 0 , (int)size);
				break;
			}*/
			
			/*case Spline
			{
				// get the 2 equations of the 2 lines formed by the tangent vectors (ie the velocity at the previus and current frames)
				// make sure the lines are not parrallel, then
				// solve the intersection point of the two lines and draw a spline with it as the middle point..
				// constrain the solution so that the intersection point must be 'in front' of the last frame's line,  and 'behind' the current frame's one
			*/
		}
	
		/*
		} else { //just clipped
		
			switch (drawmode)
			{
				case DRAW_FILLCIRCLE:
				{
					//yr basic filled oval. . .
					where.fillOval((int)(drawlocation[0] - size/2), 
			    		           (int)(drawlocation[1] - size/2), (int) size, (int) size);
			    	if(tiledraw)
			  		{
			  			if (drawlocation[0] < size/2) //off the left, add width
				  		where.fillOval((int)((dimensionclip[0]+drawlocation[0]) - size/2), (int)(drawlocation[1] - size/2), (int) size, (int) size);
				  		if (drawlocation[1] < size/2) //off the top, add height
				  		where.fillOval( (int)(drawlocation[0] - size/2), (int)( (dimensionclip[1] + drawlocation[1]) - size/2), (int) size, (int) size);
				  		if ((drawlocation[0]+size/2) > dimensionclip[0]) //off the right, sub width
				  		where.fillOval((int)( (drawlocation[0]-dimensionclip[0]) - size/2 ), (int)(drawlocation[1] - size/2), (int) size, (int) size);
				  		if ((drawlocation[1]+size/2) > dimensionclip[1]) //off the bottom, sub height
				  		where.fillOval((int)(drawlocation[0] - size/2), (int)((drawlocation[1]-dimensionclip[1]) - size/2), (int) size, (int) size);
				  	}
			    	break;
			   	}
			   	
				case DRAW_CIRCLE:
				{
					where.setStroke(new BasicStroke(strokewidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
					
					where.drawOval( (int)(drawlocation[0] - size/2), 
			    		            (int)(drawlocation[1] - size/2), (int) size, (int) size);
			  		if(tiledraw)
			  		{
			  			if (drawlocation[0] < size/2) //off the left, add width
				  		where.drawOval((int)((dimensionclip[0]+drawlocation[0]) - size/2), (int)(drawlocation[1] - size/2), (int) size, (int) size);
				  		if (drawlocation[1] < size/2) //off the top, add height
				  		where.drawOval( (int)(drawlocation[0] - size/2), (int)( (dimensionclip[1] + drawlocation[1]) - size/2), (int) size, (int) size);
				  		if ((drawlocation[0]+size/2) > dimensionclip[0]) //off the right, sub width
				  		where.drawOval((int)( (drawlocation[0]-dimensionclip[0]) - size/2 ), (int)(drawlocation[1] - size/2), (int) size, (int) size);
				  		if ((drawlocation[1]+size/2) > dimensionclip[1]) //off the bottom, sub height
				  		where.drawOval((int)(drawlocation[0] - size/2), (int)((drawlocation[1]-dimensionclip[1]) - size/2), (int) size, (int) size);
				  	}
			 		break;
			 	}
			 	
				case DRAW_MIDPOLY:
				{
					int xs[] = new int[4];
					int ys[] = new int[4];
					float midx = lastloc[0] + delta[0]/2;
					float midy = lastloc[1] + delta[1]/2;
					
					xs[0] = (int)(polypoints[0]);
					ys[0] = (int)(polypoints[1]);
					xs[1] = (int)(polypoints[2]);
					ys[1] = (int)(polypoints[3]);
					xs[2] = (int)(midx - size * (delta[1]/speedlength) );
					ys[2] = (int)(midy + size * (delta[0]/speedlength) );
					xs[3] = (int)(midx + size * (delta[1]/speedlength) );
					ys[3] = (int)(midy - size * (delta[0]/speedlength) );
					break;
				}
				
				case DRAW_POLYGON:
				case DRAW_PARALELLO: //bad fix
				{
					polypoints[0] = (int)( drawlocation[0] + size * (delta[1]/speedlength) );
					polypoints[1] = (int)( drawlocation[1] + size * (delta[0]/speedlength) );
					polypoints[2] = (int)( drawlocation[0] - size * (delta[1]/speedlength) );
					polypoints[3] = (int)( drawlocation[1] - size * (delta[0]/speedlength) );
					break;
				}
			}
		}*/
	}   
}