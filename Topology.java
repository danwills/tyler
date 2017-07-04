//import FlexiLife;
//import PixelTools;
//import Particle;
import java.util.*;
import java.lang.Math.*;
import java.io.*;
//import improvedPerlinNoise.*;
/*
 * todo: a generalised way of handling topology that is independant of whether it is to be
 * set, added (allocated) added (vectorwise) multiplied by, etc etc...
 * and dealing with masked areas of images instead of just the whole thing.
 *
 * spacepolicy handling stuff must be in another class.
 * toplogy generation in a separate process - so that it can happen ain the background and get queued
 */
 
 /*
  * TopologyFilters that shorten the lengths of the vectors (w/selective and mapped versions)
  * will keep the 2D vectors when they're generated (or make a way to turn 1d -> 2d)
  * topologyfilters that blur, spin, and attract the vectors.
  * 
  * Sinusoidal, fractal basins, IFS Topologies, flame fractal topologies,
  * square spiral topologies, mapped vector rotation, mapped randomlength, mapped radial/zoom,
  * Multi-center/parameter radial/zoom/spiral, Julia topologies. Hilbert and other space filling curve topologies,
  * 
  * Particle trajectory topologies. Particles with spawn/death policies that create topology.
  * mapped: 
  * 	particle number - how many to emit from each cell
  *     particle velocity - direction of travel
  * 	particle speed - how fast they are moving
  * 	particle decay probablility - the chance that spawn or terminate/create topology will occur
  * 	topology creation probability - control whether/how much to create connections
  * 	particle drag - with multiplier, mapped control of particle slowdown/accelerate
  * 	particle spawn number/branch depth limit - max number of spawns
  * 	particle initial rotation/rotation noise level 
  * 	particle block field - stops particles if they head too far uphill
  * 
  * Fiberous topologies (pick points, trace, create)
  * transform single layer topologies into multilayer by creating extra connections to more connection-distant elements.. effectively chaining the lookups
  * 
  * Unify kernel/liferule approaches with flexi liferule length and separate src and dest values.
  * vamp liferule editor to allow more advanced editing, zoom, pan, unpack (from library), filter, automata.
  *
  * liferule viewable in ND 
  * 
  * 
  * nextFrameWithTemporal(...)
  * uses a seq of other nextFrames() stored and recombined with color blending weighted less contribution
  * the further in the past. linear, bell, fractal, map, whatever.
  *
  * Array of liferule pointer[s] as big as array of nodes (pixels) then each cell can have a 'species'.
  * then let them compete.
  * 
  * IFStoplogygenerator
  * ifs atoms:
  * s - scale <scale>
  * r - rotation <angle>
  * t - translation <xoffset>
  * p - polar radius power <pow>
  * o - polar radius offset <offset>
  * i - polar inversion around radius <rad>
  * h - shear <amp>
  * a - lin spiral <amp>
  * l - log spiral <amp>
  * x - anisotropy <aspect>
  * c - polar transform <blend>
  * k - inverse polar transform <blend>
  * u - x power <pow>
  * n - noise <freq>
  *
  *
  * topology must always be generated with numNeighboirs in mind..
  * for this IFS mode, the total number of transforms could be more than numNeighbors,
  * and groups of atoms could be composed to form each neighbor.
  * 
  * 
  * idea of a null connection
  * idea of weighted connection
  * more transfer modes - life/small table propoagation rules
  * alive/dead mattes.. by histogram
  * gamut traversal (in this case rgb cube gamut)
  * 3dcloud topology, fwd transform each pixel into a 3d loc/radius
  * 
  * 
  */


public final class Topology
{	
	public enum TopologySpaceMode
	{
		Loop,
		Clip,
		Fold,
		Moebius;
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( TopologySpaceMode p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}
	
	//public static final int SPACE_LOOP = 0; //creates a toroidal space
	//public static final int SPACE_CLIP = 1; //chops connections falling outside so that they instead hit the edge of the space
	//public static final int SPACE_FOLD = 2; //reflects connections falling outside so that they fall inside
	//public static final int SPACE_MOEB = 3; //loop space moebius style (ie off right edge is on bottom edge)
	
	public enum TopologyBlendMode
	{
		average,
        contrasty,
		lumiDifference,
        // boost the options here!, contrasty-average, balanced-subtracty
        // median, smooth sharpen-y, random neighbour
		absoluteAdd,
		averageRule,
		averageRuleColor,
		rule,
		ruleColor,
		ruleTransfer,
		ruleTransferColor,
		ruleLifeTransfer,
		ruleLifeTransferColor,
		ruleAverageDifference,
		RGBrule,
		nextFrameRuleMaxMin,
		nextFrameRuleMaxMinTransfer,
		ruleRD,
		ruleColorRD,
		ruleTransferRD,
		ruleTransferColorRD,
		ruleAverageDifferenceRD,
		RGBruleRD;
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( TopologyBlendMode p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}
	
	/*public static final int INLINE_BLEND           = 0;
	public static final int RULE_BLEND             = 1;
	public static final int RULE_RD_BLEND          = 2;
	public static final int RULE_TRANSFER_BLEND    = 3;
	public static final int RULE_TRANSFER_RD_BLEND = 4;
	public static final int RGBRULE_BLEND = 5;
	public static final int RGBRULE_TRANSFER_BLEND = 6;
	public static final int INLINE_LUMIDIFF           = 7;
	*/
	//public static final int SIMPLE_SINGLE_LAYER = 0;
	//public static final int ALPHA_IN_SRC = 1;
	//public static final int ALPHA_IN_DEST = 2;
	//public static final int FULL_BLEND = 3;
	//public static final int KERNEL_BLEND = 5;
	//public static final int BLUR_SHARP = 6;
	
	
	/*
	//nummodes should be the number defined above
	public static final int numtopologyblendmodes = 8;
	
	public final static String[] TOPOLOGYBLENDMODES = {
													"Inline Blend",
													"Rule Blend",
													"Rule RD Blend",
													"Rule Transfer Blend",
													"Rule Transfer RD Blend",
													"RGBRule Blend",
													"RGBRule Transfer Blend",
													"Inline LumiDiffMix"};
													//"Kernel Blend",
													//"Single Connection",
													//"Single Connection With Src Alpha",
													//"Single Connection With Dest Alpha",
													//"Blur and Sharpen",
													//
													
													//"Unpacked Blend",
													//"Full Blend",
	*/
	//final String topologyNames = new String[numtopologygenerators]; //use to make menu.
	//HILBERT_TOPOLOGY
	//SQUARE_SPIRAL_TOPOLOGY
	//METAFIELD_TOPOLOGY (eg ideal flow fields)
	
	public enum TopologyGeneratorMode
	{
		LinearVector,
		Zoom,
		Spin,
		Radial,
		MultiRadial,
		Voronoi,
		VoronoiCell,
		VoronoiSpinCell,
		Spiral,
		MultiSpiral,
		Sinusoidal,
		Fourier,
		Foo,
		InvertedRadial,
		Refraction,
		VectorRotation,
		RandomNeighborhood,
		PlusNeighborhood,
		BooleanMatrixNeighborhood,
		LifeNeighborhood,
		RandomIFS,
		JuliaFractal,
		MandelbrotFractal,
		BarnsleyJuliaFractal,
		BarnsleyMandelbrotFractal,
		LambdaFractal,
		PerspectivePlane,
		FractalNoise;
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( TopologyGeneratorMode p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}
	
	/*public final static int VECTOR_TOPOLOGY               = 0; 
	public final static int POINT_ZOOM_TOPOLOGY           = 1;
	public final static int POINT_SPIN_TOPOLOGY           = 2;
	public final static int POINT_RADIAL_TOPOLOGY         = 3;
	public final static int MULTIPOINT_RADIAL_TOPOLOGY    = 4;
	public final static int MULTIPOINT_VORONOI_TOPOLOGY   = 5;
	public final static int MULTIPOINT_CELL_TOPOLOGY      = 6;
	public final static int MULTIPOINT_SPINCELL_TOPOLOGY  = 7;
	public final static int POINT_SPIRAL_TOPOLOGY         = 8;
	public final static int MULTIPOINT_SPIRAL_TOPOLOGY    = 9;
	public final static int SINUSOIDAL                    = 10;
	public final static int FOURIER                       = 11;
	public final static int FOO                           = 12;
	public final static int POINT_HORSESHOE_TOPOLOGY      = 13;
	public final static int REFRACTION_TOPOLOGY           = 14;
	public final static int VECTOR_ROTATION_TOPOLOGY      = 15;
	public final static int RANDOM_TOPOLOGY               = 16;
	public final static int PLUS_TOPOLOGY                 = 17;
	public final static int LIFE_TOPOLOGY                 = 18;
	public final static int RANDOM_IFS                    = 19;
	public final static int JULIA_TOPOLOGY                = 20;
	public final static int MANDEL_TOPOLOGY               = 21;
	public final static int BARNSJULIA_TOPOLOGY           = 22;
	public final static int BARNSMANDEL_TOPOLOGY          = 23;
	public final static int LAMBDA_TOPOLOGY               = 24;
	public final static int PERSPECTIVE_PLANE_TOPOLOGY    = 25;
	public final static int FRACTAL_NOISE_TOPOLOGY    = 26;
	
	//currently undocumented/mysterious modes: ;)
	public final static int HUGE_BROWNIAN_LOOP_TOPOLOGY   = 90;	
	public final static int CREATE_BLANK                  = 91;
	public final static int BOOLEAN_MATRIX                = 92;
	public final static int SELF_TOPOLOGY                 = 93;	
	public final static int PARTICLE_BRANCH               = 94;
	
	public final static int numtopologygenerators = 27;
	
	public final static String[] TOPOLOGYGENERATORS = {"Vector",          //0
												"Zoom",           //1
												"Spin",           //2
												"Radial",         //3
												"MultiRadial",    //4
												"MultiVoronoi",   //5
												"MultiCell",      //6
												"MultiSpinCell",  //7
												"Spiral",         //8
												"MultiSpiral",    //9
												"Sinusoidal",     //10
												"Fourier",        //11
												"Foo",            //12
												"Inverted Radial",//13
												"Refraction",     //14
												"VectorRotation", //15
												"RandomTopology", //16
												"PlusTopology",   //17
												"LifeTopology",   //18
												"Random IFS",     //1//09
												"Julia",          //210
												"Mandel",         //221
												"BarnsleyJulia",  //232
												"BarnsleyMandel", //243
												"Lambda",         //254
												"Perspective Plane",/6/25
												"Fractal Noise"};//267
														8
														9//"Looping Random",
														1//"Blank Multilayer",
														1//"Boolean Matrix",
														1//"Particle Branch",
														1//"SelfTopology",  
	                                             */
	public enum TopologyRenderMode
	{
		DitheredMultilayer,
		ChainMultilayer,
		RampMultilayer,
		AbsoluteDitheredMultilayer,
		AbsoluteChainMultilayer,
		AbsoluteRampMultilayer;
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( TopologyRenderMode p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}	                                             
	
	/*
	//public final static int SINGLE_LAYER                  = 0;
	public final static int DITHERED_MULTILAYER           = 0;
	public final static int CHAIN_MULTILAYER              = 1;
	public final static int RAMP_MULTILAYER               = 2;
	//public final static int ABSOLUTE_SINGLE_LAYER         = 4;
	public final static int ABSOLUTE_DITHERED_MULTILAYER  = 3;
	public final static int ABSOLUTE_CHAIN_MULTILAYER     = 4;
	public final static int ABSOLUTE_RAMP_MULTILAYER      = 5;
	                                                   
	public final static int numtopologyrendermodes = 6;
	                                                   
	//"Single Layer",                                  
	//"Abs Single Layer",
	
	public final static String[] TOPOLOGYRENDERMODES = {"Dithered Multilayer",
													"Chain Multilayer",
													"Ramp Multilayer",
													"Abs Dithered Multilayer",
													"Abs Chain Multilayer",
													"Abs Ramp Multilayer"};
	*/
	
	TopologySpaceMode currentspacepolicy = TopologySpaceMode.Loop;
	
	int width, height, lifesize, flatlength, numinputs;
	
	int inputs[][]; //the connections [ 1D addresses in data[] ]
	
	// if the inputs have weights the following will be true and there should be 
	// an array of floats the same length as the array of inputs in weights[]
	//
	// not currently used.
	boolean isweighted = false;
	float weights[][]; //the weights of the connections
	
	boolean highaccuracy = false;
	float accurateX[]; //used to store higher accuracy inputs, the x components
	float accurateY[]; //the y components, single layer at a time.
	
	int data[]; //the values we are addressing
	
	// if each cell has the same number of inputs this will be true,
	// then it can be used to control the loop
	// if not, inputs[cell].length must be used
	boolean ishomogeneous = true;
	
	boolean initialised = false;
	public boolean isProcessing = false;
	boolean debug = false;
	
	Random randex;
	 
	Topology(int w, int h, int[] dat, int numlayers, Random r)
	{
		if (debug) System.out.println("Topology constructor starting..");
		isweighted = false;
		ishomogeneous = true;
		numinputs = numlayers;
	 	randex = r;
	 	width = w;
	 	height = h;
	 	lifesize = height*width;
	 	flatlength = height*width;
	 	data = dat;
	 	accurateX = new float[lifesize];
	 	accurateY = new float[lifesize];
	 	inputs = new int[lifesize][numinputs];
	 	if (lifesize != data.length) {System.out.println("Wrong parameter(s) passed to topology constructor.");}
	 	if (debug) System.out.println("Topology constructor finished");
	 	initialised = true;
	}
	
	Topology(int w, int h, int numlayers)
	{
		if (debug) System.out.println("Topology constructor starting..");
		isweighted = false;
		ishomogeneous = true;
		numinputs = numlayers;
		inputs = new int[lifesize][numinputs];
	 	randex = new Random();
	 	width = w;
	 	height = h;
	 	lifesize = height*width;
	 	data = null;
	 	accurateX = new float[lifesize];
	 	accurateY = new float[lifesize];
	 	initialised = true;
	 	if (debug) System.out.println("Topology constructor finished");
	}
	
	//set the source array for the inputs.
	public synchronized void setData(int[] k)
	{ data = k; }
	
	//returns true when all cells have the same number of inputs
	public boolean homogeneous()
	{ return ishomogeneous; }
	
	public boolean initialised()
	{ return initialised; }
	
	//the inhomogeneous case should perhaps return -1 not 0, but 0 is safer.
	//this probably should sheck it is true for every cell...
	public int numInputs()
	{ if (ishomogeneous) return numinputs; else return 0; }
	
	public int numInputs(int ofelement)
	{ if (ishomogeneous) return numinputs; else return inputs[ofelement].length; }
	
	public int maxInputs()
	{ 
		if (ishomogeneous) {return numinputs;}
		else 
		{ 
			int track = 0;
			for (int i = 0; i < inputs.length; i++)
			{if (inputs[i].length > track) track = inputs[i].length;}
			return track; 
		}
	}
	
	public int applySpacePolicy(int xco, int yco)
	{
		switch (currentspacepolicy)
		{
			case Loop: {return loop2D(xco,yco);}
			case Clip: {return clip2D(xco,yco);}
			case Fold: {return bounce2D(xco,yco);}
			case Moebius: {return moebius2D(xco,yco);}
		}
		//fall-thru case is loop2D
		return loop2D(xco,yco);
	}
	
	public int applySpacePolicy(float xco, float yco)
	{
		return applySpacePolicy((int)xco,(int)yco);
	}
	
	public int applySpacePolicy(int k)
	{
		switch (currentspacepolicy)
		{
			case Loop: {return loop1D(k);}
			case Clip: {return clip1D(k);}
			case Fold: {return bounce1D(k);}
			case Moebius: {return clip1D(k);}
		}
		//fall-thru case is loop1D
		return loop1D(k);
	}
	         
	public int clip1D(int k)
	{
		if (k >= data.length) k = data.length-1;
		if (k < 0) k = 0;
		return k;
	}
	
	public int clip2D(int x, int y)
	{
		if (x >= width) x = width-1;
		else if (x < 0) x = 0;
		
		if (y >= height) y = height-1;
		else if (y < 0) y = 0;
		return x + y * width;
	}
	
	public int loop1D(int k)
	{
		if (k < 0) {k = (data.length-1) + k % data.length;}
		else {k %= data.length;}
		return k;
	}
	
	public final int loop2D(int x, int y)
	{
		x = (x < 0) ? ((width-1) + x % width) : (x % width);
		y = (y < 0) ? ((height-1) + y % height) : (y % height);
		return x + y * width;
	}
	
	public int bounce1D(int k)
	{
		if (k < 0) 
		{
			//do it recursively in case its really big
			return bounce1D(Math.abs(k));
		} else if (k >= lifesize) {
			return bounce1D(lifesize + (lifesize - k)); //|-----|-----+------>
		} else {return k;}
	}
	
	public int bounce2D(int x, int y)
	{
		if (x < 0) x = Math.abs(x);
		else if (x >= width) x = (width + (width - x));
		
		if (y < 0) y = Math.abs(x);
		else if (y >= width) y = (height + (height - y));
		
		if ((x >= 0) && (x < width) && (y >= 0) && (y < height)) 
		return x + y * width; 
		else
		return bounce2D(x,y);
	}
	
	public int moebius2D(int x, int y)
	{
	
		if ((x > 0) && (x < width) && (y > 0) && (y < height)) 
		{
			return x + y * width; 
			
		} else {
			int tmpx = x;
			int tmpy = y;
			
			if (x < 0) tmpy = Math.abs(x); 
			else if (x >= width) tmpy = height + (width - x);
			
			if (y < 0) tmpx = Math.abs(y); 
			else if (y >= width) tmpx = width + (height - y);
			
			return moebius2D(tmpx,tmpy);
		}
	}
	
	public int getXPartOf1D(int flat)
	{return flat%width;}
	
	public int getYPartOf1D(int flat)
	{return flat/width;}
	
	public void addInput(int address, int toelement)
	{
		synchronized(this)
		{
			if (inputs[toelement] != null)
			{
				int[] tmp = inputs[toelement];
				int oldlength = inputs[toelement].length;
				inputs[toelement] = new int[oldlength + 1];
				for (int i = 0; i < oldlength; i++)
				{inputs[toelement][i] = tmp[i];}
				inputs[toelement][oldlength] = address;
			} else {
				inputs[toelement] = new int[1];
				inputs[toelement][0] = address;
			}
		}
	}
	
	public void addInputs(int addresses[], int whichcell)
	{
		synchronized(this)
		{
			int[] tmp = inputs[whichcell];
			int oldlength = tmp.length;
		
			//alloc new space
			inputs[whichcell] = new int[oldlength + addresses.length];
			
			for (int i = 0; i < oldlength; i++)
			{inputs[whichcell][i] = tmp[i];}
			
			for (int i = oldlength; i < addresses.length; i++)
			{inputs[whichcell][oldlength] = addresses[i];}
		}
	}
	
	public int[][] getInputs()
	{ return inputs; }
	
	public void copyInputs(int[][] into)
	{
		for (int i = 0; i < into.length; i++)
		{
			System.arraycopy(inputs,0,into,0,into[i].length);
		}
	}
	
	public void setInputArray(int[][] inpex)
	{
		inputs = inpex;
	}
	
	public void setInputs(int[][] inpex)
	{ 
		synchronized(this) 
		{
			for (int i = 0; i < inputs.length; i++)
			{
				System.arraycopy(inpex,0,inputs,0,inputs[i].length);
			}
		} 
	}
	
	public int[] getinputs(int ofelement)
	{ return inputs[ofelement]; }
	
	public int getData(int srcelement, int inputnum)
	{ return data[inputs[srcelement][inputnum]]; }
	
	//the main reason for this class:
	//this method retrieves all a particular elements input values and places them in the
	//preallocated array.
	//may not be very fast having a method call to do this.. thinking about inlining it - vm should do it but.. better to be safe..
	public void getInputData(int srcelement, int[] destarray)
	{
		//could do this with a clone...
		//if (highaccuracy) ->
		
		if (initialised) 
		{
			for (int i = 0; i < inputs[srcelement].length; i++)
			{
				destarray[i] = data[inputs[srcelement][i]];
			}
		}
	}
	
	public void inlineWarpPoint( float location[] )
	{
		//hmm i think I may have thought about this wrong way.. 
		//need the nonexistsnt reverse-mapping to be able to do what I want to do.. stick 2d points to pixels flowing thru the toplogy..
		//can still do the reverse tho - forward warp to neighbor\(s)-average location I spose.. 
		//or ... could blithely assume that -1*forwardVector = reverse... ;)
		//or.. search for particles whilst generating nextframe of topology and move them
		//to currentCell
		return;	
		
	}
	
	public void saveTopologyToFile( File f )
	{
		//hmmm pick a format, any format..? :P
		//serialize?
		//just write maGICNUMBER then dimensions then data?
		//
		// just do it
		return;
	}
	
	public void loadFromFile( File f )
	{
		return;
	}
	
	public void generateTopologyFromSettings( TopologyGenerationSettings t )
	{
		if ( t != null )
		{
			switch (t.topologygeneratormode)
			{
				case LinearVector :
				{
					this.setVectorTopology(( (t.basePointX - t.topologyWidth/2) * t.currenttopologyparam),((t.ys[0] - t.topologyHeight/2) * t.currenttopologyparam), t.generatetopologymapped, t.extractormapf ,0,0);
					break;
				}
				case Zoom:
				{
					this.setPointZoomTopology( (int)(t.basePointX), (int)(t.basePointY), (double)-t.currenttopologyparam/10,(double)-t.currenttopologyparam/10,t.generatetopologymapped,t.extractormapf,0,true,0);
					break;
				}
				case Spin:
				{
					//this.setPointSpinTopology( (int)(t.basePointX]), (int)(t.basePointY), t.currenttopologyparam, t.generatetopologymapped, t.extractormapf, 0, true, 0);
					break;
				}
				case Radial:
				{
					this.setPointRadialTopology( (int)(t.basePointX), (int)(t.basePointY), -t.currenttopologyparam, t.powers[0], t.generatetopologymapped, t.extractormapf,0,true,0);
					break;
				}
				case MultiRadial :
				{
					this.setMultiPointRadialTopology(t.xs, t.ys, t.levels, t.powers, t.generatetopologymapped, t.extractormapf,0.0f,true,0);
					break;
				}
				case Voronoi :
				{
					this.setMultiPointVoronoiTopology(t.xs, t.ys, t.levels, t.powers, t.generatetopologymapped, t.extractormapf,0.0f,true,0);
					break;
				}
				case VoronoiCell :
				{
					this.setMultiPointCellTopology(t.xs, t.ys, t.levels, t.powers, t.generatetopologymapped, t.extractormapf,0.0f,true,0);
					break;
				}
				case VoronoiSpinCell :
				{
					this.setMultiPointSpinCellTopology(t.xs, t.ys, t.levels, t.powers, t.generatetopologymapped, t.extractormapf,0.0f,true,0);
					break;
				}
				case Spiral :
				{
					this.setPointSpiralTopology((int)t.basePointX, (int)t.basePointY, t.currenttopologyparam, t.powers[0], t.generatetopologymapped, t.extractormapf,0.0f,true,0);
					break;
				}

				case MultiSpiral :
				{
					this.setMultiPointSpiralTopology(t.xs, t.ys, t.levels, t.powers, t.generatetopologymapped,t.extractormapf,0.0f,true,0);
					break;
				}
				case Sinusoidal :
				{
					float rxfreq = randex.nextFloat() * (float) t.currenttopologyparam;
					float ryfreq = randex.nextFloat() * (float) t.currenttopologyparam;
					float rxphase = randex.nextFloat() * (2.0f * (float)Math.PI);
					float ryphase = randex.nextFloat() * (2.0f * (float)Math.PI);
					float rxamp = t.xmouse - t.topologyWidth/2;
					float ryamp = t.ymouse - t.topologyHeight/2;

					this.setSinusTopology(rxfreq,rxamp,rxphase,ryfreq,ryamp,ryphase,0,false,true);
					break;
				}

				case Fourier :
				{
					this.setFourierTopology(t.topologyWidth/2, t.topologyHeight/2, 0, 10+(int)t.currenttopologyparam,t.generatetopologymapped,t.extractormapf, true, 0);
					break;
				}

				case Foo :
				{
					this.setFooTopology(t.topologyWidth/2, t.topologyHeight/2, 0, t.currenttopologyparam, t.powers[0], 10, t.generatetopologymapped, t.extractormapf, true, 0);
					break;
				}

				case InvertedRadial :
				{
					this.setPointInverseHorseshoeTopology((int)(t.basePointX), (int)(t.basePointY), t.currenttopologyparam, t.generatetopologymapped, t.extractormapf,0,true,0);
					break;
				}

				case Refraction :
				{
					//not yet.. no map
					this.setRefractionTopology( t.extractormapf,(float) t.currenttopologyparam, true, 0);
					//output("refraction topology not implemented yet.");
					break;
				}

				case VectorRotation :
				{
					//not yet.. no map
					this.setVectorRotationTopology(((t.basePointX)-(t.topologyWidth/2.0f))/4.0f,(t.basePointY-(t.topologyHeight/2.0f))/4.0f,t.extractormapf, t.currenttopologyparam, true, 0);
					//output("vector rotation toology not implemented yet.");
					break;
				}

				case RandomNeighborhood :
				{
					this.setRandomTopology((float)t.currenttopologyparam,0, t.generatetopologymapped, t.extractormapf, 0);
					break;
				}

				case PlusNeighborhood :
				{
					this.setPlusTopology();
					//output("plus topology not implemented yet.");
					break;
				}

				case BooleanMatrixNeighborhood:
				{
					//default to centered for the moment ;P
					this.setBooleanMatrixTopology( t.booleanmatrix, t.booleanmatrixsize, t.booleanmatrixsize, t.booleanmatrixsize/2, t.booleanmatrixsize/2 );  
					break;
				}

				case LifeNeighborhood :
				{
					//nope
					this.setLifeTopology(true);
					break;
				}

				case RandomIFS :
				{
					for (int i = 0; i < t.numwarplayers; i++)
					{
						boolean rb1 = (randex.nextInt() > 0);
						boolean rb2 = (randex.nextInt() > 0);
						boolean rb3 = (randex.nextInt() > 0);
						this.setIFSQuarterLayerTopology(Math.abs(randex.nextInt() % t.topologyWidth),Math.abs(randex.nextInt() % t.topologyHeight),rb1,rb2,rb3,false,true,null,0,i);
					}
					break;
				}

				case JuliaFractal :
				{
					this.setPointJuliaTopology(t.basePointX + t.topologyWidth, t.basePointY + t.topologyHeight, (int) t.currenttopologyparam, (float)t.powers[0], t.generatetopologymapped, t.extractormapf,0,true,0);
					break;
				}

				case MandelbrotFractal :
				{
					this.setPointMandelTopology(t.basePointX, t.basePointY, (int) t.currenttopologyparam, (float) t.powers[0], t.generatetopologymapped, t.extractormapf, 0,true,0);
					break;
				}

				case BarnsleyJuliaFractal :
				{
					this.setPointBarnsleyJuliaTopology(t.basePointX + t.topologyWidth/2, t.basePointY + t.topologyHeight/2, 0, 2.0f, (int) t.currenttopologyparam,  (float)t.powers[0], t.generatetopologymapped, t.extractormapf, 0,true,0);
					break;
				}

				case BarnsleyMandelbrotFractal :
				{
					this.setPointBarnsleyMandelTopology(t.basePointX + t.topologyWidth, t.basePointY + t.topologyHeight, (int) t.currenttopologyparam,  (float)t.powers[0], t.generatetopologymapped, t.extractormapf, 0,true,0);
					break;
				}

				case LambdaFractal :
				{
					this.setPointLambdaTopology(t.basePointX + t.topologyWidth, t.basePointY + t.topologyHeight, (int) t.currenttopologyparam,  (float)t.powers[0], t.generatetopologymapped, t.extractormapf, 0,true,0);
					break;
				}

				case PerspectivePlane :
				{
					this.setPerspectivePlaneTopology((int)t.basePointX, (int)t.basePointY, t.currenttopologyparam/10, t.currenttopologyparam/10, false,null,true,0);
					break;
				}

				case FractalNoise :
				{
					this.setFractalNoiseTopology(t.basePointX, t.basePointY, (int) t.currenttopologyparam,  (float)t.powers[0], t.generatetopologymapped, t.extractormapf, 0,true,0);
					break;
				}
			}
		} else {
			System.out.println("topology render settings aren't ready yet!");
		}
	}
	
	//render the float topology map into the faster integer-maps for realtime rendering
	public void renderTopologyWithSettings( TopologyGenerationSettings t )
	{
		if ((t.topologygeneratormode != Topology.TopologyGeneratorMode.PlusNeighborhood) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.LifeNeighborhood) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.RandomIFS) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.BooleanMatrixNeighborhood))
		{
			switch ( t.topologyrendermode )
			{
				case DitheredMultilayer :
				{
					if ((t.topologygeneratormode != Topology.TopologyGeneratorMode.Fourier) && (t.topologygeneratormode != TopologyGeneratorMode.Foo) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.PerspectivePlane))
					{
						this.renderAccurateMultiTopology(t.numwarplayers,false);
					} else {
						this.renderAccurateMultiTopology(t.numwarplayers,true);
					}
					
					break;
				}
				
				case ChainMultilayer :
				{
					this.renderChainMultiTopology(t.numwarplayers,false, t.topologysquishvalue );
					break;
				}
				
				case RampMultilayer :
				{
					this.renderRampMultiTopology(t.numwarplayers,false, t.topologysquishvalue );
					break;
				}
				
				case AbsoluteDitheredMultilayer :
				{
					if ((t.topologygeneratormode != Topology.TopologyGeneratorMode.Fourier) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.Foo) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.PerspectivePlane))
					{
						this.renderAccurateMultiTopology(t.numwarplayers,true);
					} else {
						this.renderAccurateMultiTopology(t.numwarplayers,false);
					}
					break;
				}
				
				case AbsoluteChainMultilayer :
				{
					this.renderChainMultiTopology(t.numwarplayers,true, t.topologysquishvalue);
					break;
				}
				
				case AbsoluteRampMultilayer :
				{
					this.renderRampMultiTopology(t.numwarplayers,true, t.topologysquishvalue);
					break;
				}
			}
		}
	}
	
	public void setLifeTopology(boolean self) //connect each pixel to 8 others. . .
	                                          //all those it is adjacent to in the pixel grid
	{
		if (debug) 
		{
			System.out.print("Setting life topology, ");
			if (self) System.out.print("including"); else System.out.print("excluding");
			System.out.println(" a connection from each cell to itself.");
		}
		
		initialised = false;
		ishomogeneous = true;
		isweighted= false;
		if (self) {numinputs = 9;} else {numinputs = 8;}
		inputs = new int[lifesize][numinputs];
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				//if (inputs[i+j*width].length != numinputs) 
				//{inputs[i+j*width] = new Point[numinputs];}
				for (int m = 0; m < 9; m++)
				{
					if (self)
					{
						//this should give: -1  0  1 -1  0  1 -1  0  1 in one direction
						//             and  -1 -1 -1  0  0  0  1  1  1 in the other
						int ofsi = i + ((m % 3) -1 );
						int ofsj = j + ((m / 3) -1 );
						
						inputs[i+j*width][m] = applySpacePolicy(ofsi,ofsj);
					}else{
						int ofsi = i + ((m % 3) -1 );
						int ofsj = j + ((m / 3) -1 );
						
						// skip the middle cell by detecting m > 4;
						if (m < 4)
						{
							inputs[i+j*width][m] = applySpacePolicy(ofsi,ofsj);
						}
						else if (m > 4)
						{
							inputs[i+j*width][m-1] = applySpacePolicy(ofsi,ofsj);
						}
					}
				}		
			}
		}
		if (debug) System.out.println("Life topology set.");
		initialised = true;
	}
	
	
	public void setBooleanMatrixTopology(boolean [] connect, int kwidth, int kheight, int selfx, int selfy)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted= false;
		int count = 0;
		int ofsi, ofsj;
		for (int i = 0; i < connect.length; i++)
		{ if (connect[i]) count++; }
		System.out.println("Creating " + count + " layers of topology using boolean matrix.");
		numinputs = count;
		inputs = new int[lifesize][numinputs];
		
		for (int i = 0; i < height; i++) 
		{
			for (int j = 0; j < width; j++) 
			{
				int kindex = 0;
				int iindex = 0;
				
				for (int m = 0; m < kheight; m++)
				{
					for (int n = 0; n < kwidth; n++)
					{
						kindex = n + m * kwidth;
						if ((connect[kindex]) & (iindex < count))
						{
							ofsi = (i + m) - selfy;	
							ofsj = (j + n) - selfx;
							inputs[j+i*width][iindex] = applySpacePolicy(ofsj,ofsi);
							iindex++;
						}	
					}
				}		
			}
		}
		if (debug) System.out.println("Connection Matrix Topology set.");
		initialised = true;
	}
	
	
	public void setSelfTopology(int inlayer) //connect each pixel to itself, a good place to begin
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted= false;
		numinputs = 1;
		inputs = new int[lifesize][numinputs];	
		for (int i = 0; i < width*height; i++) 
		{
			if (inlayer >= 0) inputs[i][inlayer] = i;
			else for (int j = 0; j < inputs[i].length; j++) {inputs[i][j] = i;}
		}
		if (debug) System.out.println("Self topology set.");
		initialised = true;
	}
	
	public void createBlankTopology(int numlayers)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted= false;
		numinputs = numlayers;
		inputs = null;
		inputs = new int[lifesize][numinputs];
		for (int i = 0; i < lifesize; i++) 
		{
			for (int k = 0; k < numinputs; k++)
			{ inputs[i][k] = i; }
		}
		
		if (debug) System.out.println("Blank topology, " + numinputs + " deep created.");
		initialised = true;
	}
	
	//warning! does not generate an accurateTopology!
	public void setIFSQuarterLayerTopology(int x, int y,boolean xmirroring, boolean ymirroring, boolean rotation, 
	boolean mappedmode, boolean dither, int map[], int mapoffset, int inlayer)
	{	
		initialised = false;
		ishomogeneous = false;
		int xape, pos, ni, nj;
		int[] addresses = new int[4];
		
		for (int i = 0; i < height/2; i++)
		{
			for (int j = 0; j < width/2; j++)
			{
				pos = applySpacePolicy(x + j, y + i);
				
				//mirror/rotate
				if (xmirroring) {nj = (width-1)/2 - j;} else {nj = j;}
				if (ymirroring) {ni = (height-1)/2 - i;} else {ni = i;}
				if (rotation) {int tmp = nj; nj = ni; ni = tmp;}
				xape = (nj << 1) + (ni << 1) * width;
				xape = applySpacePolicy(xape);
				
				if (mappedmode) 
				{
					
					if (dither) { if (map[xape] >= Math.abs(randex.nextInt() % 0xff) ) inputs[pos][inlayer] = xape; }
					else { if (map[xape] >= 128) inputs[pos][inlayer] = xape; }
				} else { inputs[pos][inlayer] = xape; } /*not mappedmode*/ 
				
				
			}
		}
		initialised = true;
	}
	
	public void setIFSMappedTopology(int x, int y,boolean xmirroring, boolean ymirroring, boolean rotation, int[] map, int inlayer)
	{	
		initialised = false;
		ishomogeneous = false;
		int xape, pos, ni, nj;
		int[] addresses = new int[4];
		
		for (int i = 0; i < height/2; i++)
		{
			for (int j = 0; j < width/2; j++)
			{
				//do mirror/rotate here
				pos = applySpacePolicy(x + i, y + j);
				
				if (xmirroring) {nj = (width-1)/2 - j;} else {nj = j;}
				if (ymirroring) {ni = (height-1)/2 - i;} else {ni = i;}
				if (rotation) {int tmp = nj; nj = ni; ni = tmp;}
				
				xape = (nj << 1) + (ni << 1) * width;
				
				//addInput(pos,pos);
				//xape++;
				//addresses[1] = xape; 
				//xape = xape + (width-1);
				//addresses[2] = xape; 
				//xape++;
				//addresses[3] = xape;
				//if (inputs[pos].length > 1)
				//else {inputs[pos] = addresses;}
			}
		}
		initialised = true;
	}
	
	//space must be square with edge length a power of two for this to work.
	public void setHilbertCurveTopology()
	{
		// This method generates Hilbert's masterpiece as a long topological loop.
		//  
		//  ][
		//  UU
		//  
		//  U ] [ U
		//  n ] [ n
		//  ] [ ] [
		//  U U U U
		//
		// X-->+     +-->X
		//     |     |    
		//  <--+     <--+
		//  |  +-->-->  |
		//  |  |     |  |
		//  +-->     +-->
		//
		
		//first element = element 0
		//last element = element width-1
		//divide space in quarters until u get 4x4 pixel fragments.
		
		initialised = false;
		ishomogeneous = true;
		numinputs = 1;
		inputs = new int[lifesize][numinputs];
		
		int orientation = 0;
		// if:
		// 0 = right
		// 1 = down
		// 2 = left
		// 3 = up
		//
		// then the seq to fill 8 when oriented to the right is:
		// {0, 1, 2, 1, 1, 0, 3, 0, 1, 0, 3, 3, 2, 3, 0}
		// when oriented down the seq is:
		// {1, 0, 3, 0, 0, 1, 2, 1, 0, 1, 2, 2, 3, 2, 1} 
		// when oriented left the seq is:
		// {2, 3, 0, 3, 3, 2, 1, 2, 3, 2, 1, 1, 0, 1, 2} 
		// when oriented up the seq is:
		// {3, 2, 1, 2, 2, 3, 0, 3, 2, 3, 0, 0, 1, 0, 3} 
		
		
		// right {r, d, l, d, d, r, u, r, d, r, u, u, l, u, r}
		// down  {d, r, u, r, r, d, l, d, r, d, l, l, u, l, d} 
		// left  {l, u, r, u, u, l, d, l, u, l, d, d, r, d, l} 
		// up    {u, l, d, l, l, u, r, u, l, u, r, r, d, r, u} 
		
		// 
		//spacially: U
		//
		// a0 - b1   o0 - p -> a
		//      |    |       
		// d1 - c2   n3 - m2
		// |              |  
		// e1   h0 - i1   l3
		// |    |    |    |  
		// f0 - g3   j0 - k3
		//
		//or: ]
		//
		// ad   dr - er - fd
		// |    |         |  
		// br - cu   hd - gl
		//           |       
		// od - nl   ir - jd
		// |    |         |  
		// pX   mu - ll - kl
		// 
		// if q2 & right: r, d, l, d, d, r, u, r, d, r, r, u, u, l, u, r, join to next
		// d, r, u, r
		// r, d, l ,d
		// r, d, l, d
		// l, u, r, r
		// 
		// define right & left turns:
		// right[RIGHT] = down
		// right[DOWN]  = left
		// right[LEFT]  = up
		// right[UP]    = right
		//
		// current = right(current) = (current+1) % 4;
		// left(current) = (current-1); if current < 0 return 3 else return current;
		// 
		// 
		// 
		
		/*int pos = 0;       //element 0
		int direction = 1; //down
		while (pos != (width-1))
		{
			int hilbert(x1,y1,x2,y2, int orient, int quarter);
			{
				if ((x1-x2 == 1) && (y1-y2 == 1))
				case (orient)
				{
					RIGHT:{ nextpos = next[RIGHT][quarter]; }
					DOWN: { nextpos = next[DOWN][quarter];  }
					LEFT: { nextpos = next[LEFT][quarter];  }
					UP:   { nextpos = next[UP][quarter];    }
				}
				
				addInput(prevpos,nextpos);
				
			int last = dohilbert();
			//join last to first
			dohilbert(2);
			dohilbert(3);
			dohilbert(4);
		}
		*/
		
		//orient down;
		//dodownhilb;
		//joinbottomlefttotopright;
		//orient right;
		//dorighthilb;
		//jointoprighttotopleft;
		//orient right
		//dorighthilb;
		//jointoprighttobottomright;
		//douphilb;
		//jointoprighttooriginalpos;
		
		
		initialised = true;
	}
	
	public void setVectorTopology(float x, float y, boolean mapped, float[] map, int mapoffset, int inlayer)
	{
		//assumes the map is rgb pixels	
		if (debug) System.out.println("Setting length mapped vector topology.");
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		float mapparm = 0;
		float ofsi, ofsj;
		int insert = 0;
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				insert = i + j * width;
				ofsi = x;
				ofsj = y;
				if (mapped)
				{
					mapparm = map[insert];
					ofsi *= mapparm/256.0f;
					ofsj *= mapparm/256.0f;
				}
				accurateX[insert] = ofsi;
				accurateY[insert] = ofsj;
				ofsi += (float)i;
				ofsj += (float)j;
				inputs[insert][inlayer] = applySpacePolicy((int)ofsi,(int)ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Length mapped vector topology set.");
	}
	
	public void setVectorRotationTopology(float x, float y, float[] map, float rotationmultiplier, boolean dither, int inlayer) 
	{
		//assumes the map is rgb pixels	
		if (debug) System.out.println("Setting rotationmapped vector topology.");
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		float mapparm = 0;
		int insert = 0;
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				insert = i + j * width;
				mapparm = map[insert];
				mapparm = (mapparm/256.0f) * ((float)Math.PI * 2.0f) * rotationmultiplier; //get a number between 0 and 2*pi
				
				//[costheta, -sintheta]
				//[sintheta, +costheta]
				float ofsi = (x * (float)Math.cos(mapparm)) - (y * (float)Math.sin(mapparm));
				float ofsj = (x * (float)Math.sin(mapparm)) + (y * (float)Math.cos(mapparm));
				
				accurateX[insert] = ofsi;
				accurateY[insert] = ofsj;
				ofsi += (float)i;
				ofsj += (float)j;
				if (dither)
				{
					ofsi += (randex.nextFloat() - 0.5);
					ofsj += (randex.nextFloat() - 0.5);
				}
				inputs[insert][inlayer] = applySpacePolicy((int)ofsi,(int)ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Rotation mapped vector topology set.");
	}
	
	public void setParticleTrajectoryTopology(int numparticles, int maxspawn, int branchfactor, float stability, double randrotlevel, double initialrotphase, float initialspeed, float speedscaling, int[] map)
	{
		//initialspeednoiselevel or map
		/*
		Particle[][][] theparticles = new Particle[numparticles][maxspawn][];
		double finalrot;
		float xcomp,ycomp;
		boolean isalive = true;
		boolean nonealive = false;
		
		//get numparticles layers of topology to put all this in...
		numinputs = numparticles;
		inputs = new int[lifesize][numinputs];
		for (int i = 0; i < lifesize; i++)
		{
			for (int j = 0; j < numinputs; j++)
			{
				inputs[i][j] = i;
			}
		}
		
		
		for (int i = 0; i < numparticles; i++)
		{
			// speed/direction evenly spread radially out from cell, plus randomrotlevel noise.
			// make speed vectors from ((i/numparticles) * 2PI + initialrotphase) rotation and mapped 
			// length, plus randrotlevel noise
			finalrot = initialrotphase + (((double)i/(double)numparticles) - 0.5) * (2.0*Math.PI);
			finalrot += (randex.nextDouble()-0.5)*randrotlevel; //half randrotlevel in each direction.
			xcomp = initialspeed * (float)Math.cos(finalrot);
			ycomp = initialspeed * (float)Math.sin(finalrot);
			theparticles[i][0] = new Particle[1]; //always just one particle at first spawnlevel
			theparticles[i][0][0] = new Particle(width,height,xcomp,ycomp,randex);
		}
		
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				int pos = j + i * width;
				if ((j == 0) & (i % 16 == 0)) System.out.println("Starting raster: " + i);
				//System.out.println("Starting element: " + pos);
				//initialize first particle layer
				for (int l = 0; l < numparticles; l++)
				{
					finalrot = initialrotphase + (((double)l/(double)numparticles) - 0.5) * (2.0*Math.PI);
					finalrot += (randex.nextDouble()-0.5)*randrotlevel; //half randrotlevel in each direction.
					xcomp = initialspeed * (float)Math.cos(finalrot);
					ycomp = initialspeed * (float)Math.sin(finalrot);
					
					theparticles[l][0][0].setLocation(0,0);
					theparticles[l][0][0].setActive(true);
					theparticles[l][0][0].setSpeed(xcomp,ycomp);
					theparticles[l][0][0].setRespawnSpeedScale(speedscaling);
					theparticles[l][0][0].setRespawnLevel(branchfactor);
					theparticles[l][0][0].setRandomRotation((double)randrotlevel);
					theparticles[l][0][0].setRespawnRotation((double)randrotlevel);
					theparticles[l][0][0].setStability(stability);
				}
				
				
				for (int g = 0; g < numparticles; g++)
				{	
					int spawn = 0;
					while (spawn < maxspawn)
					{	
						//System.out.println("Processing spawnlevel " + spawn + " on Particle " + g + " element " + j);
						nonealive = false;
						while (!nonealive) //if none left alive in this generation . . .
						{
							nonealive = true;
							if (theparticles[g][spawn] != null) //ie there are particles to process
							{
								//System.out.println("Processing " + theparticles[g][spawn].length + " Particles.");
								for (int r = 0; r < theparticles[g][spawn].length; r++)
								{
									theparticles[g][spawn][r].update();
									if (theparticles[g][spawn][r].justdied) 
									{
										//System.out.println("Particle killed, creating topology in layer " + spawn);
										//create topology
										float[] finalvector = theparticles[g][spawn][r].getLocation();
										accurateX[pos] = finalvector[0];
										accurateY[pos] = finalvector[1];
										inputs[pos][g] = applySpacePolicy(j+finalvector[0],i+finalvector[1]);
										
									} else if (theparticles[g][spawn][r].isActive()) nonealive = false; //at least one particle is still alive, stay on this respawn level.	
								
									Particle partlings[] = theparticles[g][spawn][r].getSpawn();
									if (partlings != null)
									{
										//System.out.println("hatched " + partlings.length + " partlings");
										if (spawn < maxspawn - 1) //then add spawn to next spawnlevel
										{
											//put these partlings into the queue
											if (theparticles[g][spawn+1] == null)
											{
												theparticles[g][spawn+1] = partlings;
											} else {//merge existing and newly spawned particle arrays:
												Particle spawnpool[] = theparticles[g][spawn+1];
												theparticles[g][spawn+1] = new Particle[spawnpool.length + partlings.length];
												
												for (int m = 0; m < spawnpool.length; m++)
												{theparticles[g][spawn+1][m] = spawnpool[m];}
												
												for (int m = spawnpool.length; m < spawnpool.length+partlings.length; m++)
												{theparticles[g][spawn+1][m] = partlings[m - spawnpool.length];}
											}
										}
									} //else no hatchlings to rear.
								}
							} else { spawn = maxspawn; } //exit loop if no spawned particles at this branch depth for this particle
						}
						spawn++;//. . . move on to next branch depth level
					} //done max spawn levels for this particle
				} //go on to next particle
			} //go on to next position
		}
		*/
		System.out.println("ParticleTraceTopology is currently disabled.");
	}
	
	///public void setSmoothedRefractionTopology(float[] map, float multiplier, float filterradius, float filteramp, float filterpower, float filtercenterjitter, boolean dither, int inlayer)
	//{
	//  possibility.. sinc filtered refraction generator (very slow by this method for large maximal squares)
	//
	//  basic layout notes:
	//  foreach destination vector, calulate maximal bounding square for filterradius, foreach on it and clip/tile the addresse, form delta vectors,
	//  sum these vectors weighted by filterfunction on radius to generate refraction vectors
	//
	//  maybe link this into a method to connect more/less distantly via a localised center of extremities such as min/max 
	//		(for dilate/erode-style refraction topologies)
	//
	//  for over the maximal square, tile the (float) addresses and sum the (bilinearly?) interpolated loookups, weighted by the filterfunction/power/etc
	//		possibility to introduce a sampling density control, with jitter and further inter/extra-polation controls 
	//		such as contrast bailouts and conditional oversampling techniques
	//}
	
	
	//
	// 
	// barnsley fractaltopology generators
	// basin fractaltopology generators
	// inversion for mandel & julia types 
	// 
	// 
	// 
	// 
	
	public void setRefractionTopology(float[] map, float multiplier, boolean dither, int inlayer)
	{
		//assumes the map is unpacked ints
		//
		//not used yet: [will be slower but more flexible]
		//int[][] derivator = { {-1,-1},{ r,-1},{ 1,-1}
		//                      {-1, r},{ r, r},{ 1, r}
		//                      {-1, 1},{ r, 1},{ 1, 1} }
		
		initialised = false;
		//ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		float myval;
		int insert, tmppos, ofsi, ofsj;
		double xworking, yworking, delta;
		
		for (int i = 1; i < width-1; i++) 
		{
			for (int j = 1; j < height-1; j++) 
			{
				insert = i + j * width;
				myval = map[insert];
				tmppos = (insert - width) - 1;
				
				//top neighbors of element
				delta = (myval - map[tmppos])/255.0;
				xworking = (delta*-1.0);
				yworking = (delta*-1.0);
				
				tmppos++;
				delta = (myval - map[tmppos])/255.0;
				yworking += (delta*-1.0);
				
				tmppos++;
				delta = (myval - map[tmppos])/255.0;
				xworking += (delta * 1.0);
				yworking += (delta *-1.0);
				
				//left and right of element
				tmppos += (width-2);
				delta = (myval - map[tmppos])/255.0;
				xworking += (delta *-1.0);
				tmppos += 2;
				delta = (myval - map[tmppos])/255.0;
				xworking += (delta * 1.0);
				
				//bottom neighbors of element
				tmppos += (width - 2);
				delta = (myval - map[tmppos])/255.0;
				xworking += (delta *-1.0);
				yworking += (delta * 1.0);
				
				tmppos++;
				delta = (myval - map[tmppos])/255.0;
				yworking += (delta * 1.0);
				
				tmppos++;
				delta = (myval - map[tmppos])/255.0;
				xworking += (delta * 1.0);
				yworking += (delta * 1.0);
				
				xworking *= multiplier;
				yworking *= multiplier;
				
				accurateX[insert] = (float) xworking;
				accurateY[insert] = (float) yworking;
				
				if (dither)
				{
					ofsi = i + (int) (xworking+(randex.nextDouble() - 0.5));
					ofsj = j + (int) (yworking+(randex.nextDouble() - 0.5));
				} else {
					ofsi = i + (int) (xworking);
					ofsj = j + (int) (yworking);
				}
				
				inputs[insert][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Refraction mapped vector topology added.");
	}

	public void setPointRadialTopology(float x, float y, double multiplier, double power, boolean mapped, float[] map, 
	                                   float mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		//double realx = (double) x;
		//double realy = (double) y;
		int ofsi, ofsj;
		int pos = 0;
		double dx, dy;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				
				dx = (double) ((x+width/2) - i);
				dy = (double) ((y+height/2) - j);
				
				double len = Math.sqrt(dx*dx + dy*dy);
				len = 20.0 * Math.pow(len/20.0f,power);
				
				if (len!=0)
				{
					dx /= len;
					dy /= len;
				}
				
				//double powlen = Math.pow(len / 10, power);
				
				//dx *= powlen;
				//dy *= powlen;
				
				dx *= multiplier;
				dy *= multiplier;
				
				if (mapped)
				{
					dx *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
					dy *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				
				accurateX[pos] = (float) dx;
				accurateY[pos] = (float) dy;			
				//do some dithering:
				//if (dither)
				
				if (dither)
				{
					ofsi = (int)Math.round( ((i + dx + (randex.nextDouble() - 0.5))) );
					ofsj = (int)Math.round( ((j + dy + (randex.nextDouble() - 0.5))) );
				} else {
					ofsi = (int)Math.round((i + dx));
					ofsj = (int)Math.round((j + dy));
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Point Radial topology set.");
	}
	
	public void setMultiPointRadialTopology(float x[], float y[], double multipliers[], double powers[], boolean mapped, float[] map, 
	                                        float mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		//double realx = (double) x;
		//double realy = (double) y;
		int ofsi, ofsj;
		int pos = 0;
		double dx, dy, wdx, wdy;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				dx = 0; dy = 0;
				
				for (int k = 0; k < x.length; k++)
				{
					wdx = (double) (x[k] - i);
					wdy = (double) (y[k] - j);
					double len = Math.sqrt(wdx*wdx + wdy*wdy);
					
					//normalize around 20pixels: eek
					len = 20.0 * Math.pow(len/20.0f,powers[k]);
					
					if (len!=0) //normalize the vector
					{
						wdx /= len;
						wdy /= len;
					}
					
					//does a strange sierpinski-ization because of the signs.. do it on the length instead:
					//wdx = Math.pow(wdx,powers[k]);
					//wdy = Math.pow(wdy,powers[k]);
					
					wdx *= multipliers[k];
					wdy *= multipliers[k];
					
					dx += wdx;
					dy += wdy;
				}
				
				if (mapped)
				{
					dx *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
					dy *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				
				accurateX[pos] = (float) dx;
				accurateY[pos] = (float) dy;			
				//do some dithering:
				//if (dither)
				
				if (dither)
				{
					ofsi = (int)Math.round((i + dx) + (randex.nextDouble() - 0.5));
					ofsj = (int)Math.round((j + dy) + (randex.nextDouble() - 0.5));
				} else {
					ofsi = (int)Math.round((i + dx));
					ofsj = (int)Math.round((j + dy));
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("MultiPoint Radial topology set.");
	}
	
	public void setMultiPointVoronoiTopology(float x[], float y[], double multipliers[], double powers[], boolean mapped, float[] map, 
	                                        float mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		//double realx = (double) x;
		//double realy = (double) y;
		int ofsi, ofsj;
		int pos = 0;
		int nearestPoint = 0;
		double nearestDistance = Double.MAX_VALUE;
		double dx, dy, wdx, wdy;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				dx = 0; dy = 0;
				
				//find the closest center to the position we are currently evaluating...
				nearestPoint = 0;
				nearestDistance = Double.MAX_VALUE;
				
				for (int k = 0; k < x.length; k++)
				{
					wdx = (double) (x[k] - i);
					wdy = (double) (y[k] - j);
					double len = Math.sqrt(wdx*wdx + wdy*wdy);
					
					
					if (len < nearestDistance)
					{
							nearestPoint = k;
							nearestDistance = len;
					}
						
					//normalize around 20pixels: eek
					//len = 20.0 * Math.pow(len/20.0f,powers[k]);
					
					//if (len!=0) //normalize the vector
					//{
					//	wdx /= len;
					//	wdy /= len;
					//}
					
					//wdx *= multipliers[k];
					//wdy *= multipliers[k];
					
					//dx += wdx;
					//dy += wdy;
				}
				
				double cellnoise = multipliers[nearestPoint]; //randex.nextDouble();
				
				dx = (double) (x[nearestPoint] - i) * cellnoise * (powers[nearestPoint] - nearestDistance);
				dy = (double) (y[nearestPoint] - j) * cellnoise * (powers[nearestPoint] - nearestDistance);
				
				if (mapped)
				{
					dx *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
					dy *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				
				accurateX[pos] = (float) dx;
				accurateY[pos] = (float) dy;
				
				if (dither)
				{
					ofsi = (int)Math.round((i + dx) + (randex.nextDouble() - 0.5));
					ofsj = (int)Math.round((j + dy) + (randex.nextDouble() - 0.5));
				} else {
					ofsi = (int)Math.round((i + dx));
					ofsj = (int)Math.round((j + dy));
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("MultiPoint Voronoi topology set.");
	}
	
	public void setMultiPointCellTopology(float x[], float y[], double multipliers[], double powers[], boolean mapped, float[] map, 
	                                        float mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		//double realx = (double) x;
		//double realy = (double) y;
		int ofsi, ofsj;
		int pos = 0;
		int nearestPoint = 0;
		double nearestDistance = Double.MAX_VALUE;
		int secondNearestPoint = 0;
		double secondNearestDistance = Double.MAX_VALUE;
		double dx, dy, wdx, wdy;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				dx = 0; dy = 0;
				
				//find the closest center to the position we are currently evaluating...
				nearestPoint = 0;
				nearestDistance = Double.MAX_VALUE;
				secondNearestPoint = 0;
				secondNearestDistance = Double.MAX_VALUE;
				
				for (int k = 0; k < x.length; k++)
				{
					wdx = (double) (x[k] - i);
					wdy = (double) (y[k] - j);
					double len = Math.sqrt(wdx*wdx + wdy*wdy);
					
					
					if (len < secondNearestDistance)
					{
						if (len > nearestDistance) {
							
							secondNearestPoint = k;
							secondNearestDistance = len;
							
						} else {
							secondNearestPoint = nearestPoint;
							secondNearestDistance = nearestDistance;
							nearestPoint = k;
							nearestDistance = len;
							
						}
					}
						
					//normalize around 20pixels: eek
					//len = 20.0 * Math.pow(len/20.0f,powers[k]);
					
					//if (len!=0) //normalize the vector
					//{
					//	wdx /= len;
					//	wdy /= len;
					//}
					
					//wdx *= multipliers[k];
					//wdy *= multipliers[k];
					
					//dx += wdx;
					//dy += wdy;
				}
				
				double cellnoise = multipliers[nearestPoint]; //randex.nextDouble();
				
				double nearBoundary = (secondNearestDistance - nearestDistance);
				if (nearBoundary != 0) nearBoundary = 1.0/nearBoundary;
				else nearBoundary = Double.MAX_VALUE;
				
				dx = (double) (x[nearestPoint] - i) * cellnoise * (powers[nearestPoint] - nearestDistance) * nearBoundary;
				dy = (double) (y[nearestPoint] - j) * cellnoise * (powers[nearestPoint] - nearestDistance) * nearBoundary;
				
				if (mapped)
				{
					dx *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
					dy *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				
				accurateX[pos] = (float) dx;
				accurateY[pos] = (float) dy;
				
				if (dither)
				{
					ofsi = (int)Math.round((i + dx) + (randex.nextDouble() - 0.5));
					ofsj = (int)Math.round((j + dy) + (randex.nextDouble() - 0.5));
				} else {
					ofsi = (int)Math.round((i + dx));
					ofsj = (int)Math.round((j + dy));
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("MultiPoint Cell topology set.");
	}
	
	public void setMultiPointSpinCellTopology(float x[], float y[], double multipliers[], double powers[], boolean mapped, float[] map, 
	                                        float mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		//double realx = (double) x;
		//double realy = (double) y;
		int ofsi, ofsj;
		int pos = 0;
		int nearestPoint = 0;
		double nearestDistance = Double.MAX_VALUE;
		int secondNearestPoint = 0;
		double secondNearestDistance = Double.MAX_VALUE;
		double dx, dy, wdx, wdy;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				dx = 0; dy = 0;
				
				//find the closest center to the position we are currently evaluating...
				nearestPoint = 0;
				nearestDistance = Double.MAX_VALUE;
				secondNearestPoint = 0;
				secondNearestDistance = Double.MAX_VALUE;
				
				for (int k = 0; k < x.length; k++)
				{
					wdx = (double) (x[k] - i);
					wdy = (double) (y[k] - j);
					double len = Math.sqrt(wdx*wdx + wdy*wdy);
					
					
					if (len < secondNearestDistance)
					{
						if (len > nearestDistance) {
							
							secondNearestPoint = k;
							secondNearestDistance = len;
							
						} else {
							secondNearestPoint = nearestPoint;
							secondNearestDistance = nearestDistance;
							nearestPoint = k;
							nearestDistance = len;
						}
					}
						
					//normalize around 20pixels: eek
					//len = 20.0 * Math.pow(len/20.0f,powers[k]);
					
					//if (len!=0) //normalize the vector
					//{
					//	wdx /= len;
					//	wdy /= len;
					//}
					
					//wdx *= multipliers[k];
					//wdy *= multipliers[k];
					
					//dx += wdx;
					//dy += wdy;
				}
				
				double nearBoundary = (secondNearestDistance - nearestDistance);
				
				//if (nearBoundary != 0) nearBoundary = 1.0/nearBoundary;
				//else nearBoundary = Double.MAX_VALUE;
				
				dx = (double) (x[nearestPoint] - i);
				dy = (double) (y[nearestPoint] - j);
				
				double cellnoise = multipliers[nearestPoint] + nearestDistance * (powers[nearestPoint]/1000); //randex.nextDouble();
				cellnoise *= nearBoundary; //no effect near boundary
				
				double tmpdx = dx * Math.cos(cellnoise) - dy * Math.sin(cellnoise);
				double tmpdy = dx * Math.sin(cellnoise) + dy * Math.cos(cellnoise);
				
				dx = -tmpdx;
				dy = -tmpdy;
				
				if (mapped)
				{
					dx *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
					dy *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				
				accurateX[pos] = (float) dx;
				accurateY[pos] = (float) dy;
				
				if (dither)
				{
					ofsi = (int)Math.round((i + dx) + (randex.nextDouble() - 0.5));
					ofsj = (int)Math.round((j + dy) + (randex.nextDouble() - 0.5));
				} else {
					ofsi = (int)Math.round((i + dx));
					ofsj = (int)Math.round((j + dy));
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("MultiPoint spinCell topology set.");
	}
	
	public void setPointSpiralTopology(float x, float y, double multiplier, double power, boolean mapped, float[] map, 
	                                   float mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		//ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		//double realx = (double) x;
		//double realy = (double) y;
		int ofsi, ofsj;
		int pos = 0;
		double dx, dy;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				
				dx = (double) ((x+width/2) - i);
				dy = (double) ((y+height/2) - j);
				double len = Math.sqrt(dx*dx + dy*dy);
				len = 20.0 * Math.pow(len/20.0,power);
				if (len!=0)
				{
					dx /= len;
					dy /= len;
				}
				//rotate by 9r degrees
				
				double tmp = dx;
				dx = dy;
				dy = -tmp;
				
				dx *= multiplier;
				dy *= multiplier;
				if (mapped)
				{
					dx *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
					dy *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				accurateX[pos] = (float) dx;
				accurateY[pos] = (float) dy;			
				//do some dithering:
				//if (dither)
				
				if (dither)
				{
					ofsi = (int)Math.round((i + dx) + (randex.nextDouble() - 0.5));
					ofsj = (int)Math.round((j + dy) + (randex.nextDouble() - 0.5));
				} else {
					ofsi = (int)Math.round((i + dx));
					ofsj = (int)Math.round((j + dy));
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Point Spiral topology set.");
	}
	
	public void setMultiPointSpiralTopology(float x[], float y[], double multipliers[], double powers[], boolean mapped, float[] map, 
	                                        float mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		//ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		//double realx = (double) x;
		//double realy = (double) y;
		int ofsi, ofsj;
		int pos = 0;
		double dx, dy, wdx, wdy;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				dx = 0; dy = 0;
				for (int k = 0; k < x.length; k++)
				{
					wdx = (double) (x[k] - i);
					wdy = (double) (y[k] - j);
					double len = Math.sqrt(wdx*wdx + wdy*wdy);
					len = Math.pow(len,powers[k]);
					if (len!=0)
					{
						wdx /= len;
						wdy /= len;
					}
					//rotate by 90 degrees
					double tmp = wdx;
					wdx = wdy;
					wdy = -tmp;
					wdx *= multipliers[k];
					wdy *= multipliers[k];
					dx += wdx; dy += wdy;
				}
				if (mapped)
				{
					dx *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
					dy *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				accurateX[pos] = (float) dx;
				accurateY[pos] = (float) dy;			
				//do some dithering:
				//if (dither)
				
				if (dither)
				{
					ofsi = (int)Math.round((i + dx) + (randex.nextDouble() - 0.5));
					ofsj = (int)Math.round((j + dy) + (randex.nextDouble() - 0.5));
				} else {
					ofsi = (int)Math.round((i + dx));
					ofsj = (int)Math.round((j + dy));
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("MultiPoint Spiral topology set.");
	}
	
	public void setPointPhotoshopTopology(int x, int y, double amp, double power, double frequency, boolean mapped, int[] map, 
	                                      int mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		//double realx = (double) x;
		//double realy = (double) y;
		int ofsi, ofsj;
		int pos = 0;
		double dx, dy;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				
				dx = (double) (x - i);
				dy = (double) (y - j);
				double len = Math.sqrt(dx*dx + dy*dy);
				if (len!=0)
				{
					dx /= len;
					dy /= len;
				}
				
				dx = Math.pow(dx,power);
				dy = Math.pow(dy,power);
				
				dx *= amp;
				dy *= amp;
				if (mapped)
				{
					dx *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
					dy *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				
				accurateX[pos] = (float) dx;
				accurateY[pos] = (float) dy;			
				//do some dithering:
				//if (dither)
				
				if (dither)
				{
					ofsi = (int)Math.round( ((i + dx + (randex.nextDouble() - 0.5))) );
					ofsj = (int)Math.round( ((j + dy + (randex.nextDouble() - 0.5))) );
				} else {
					ofsi = (int)Math.round((i + dx));
					ofsj = (int)Math.round((j + dy));
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Point Radial topology set.");
	}
	
	public void setPointJuliaTopology(float x, float y, int iterations, float multiplier, boolean mapped, float[] map, int mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		int pos = 0;
		int ofsi, ofsj;
		double dr, di, tmpdr, tmpdi, rsq, isq, rconst, iconst;
		
		rconst = (x-width/2.0)/(width*2.0);
		iconst = (y-height/2.0)/(height*2.0);
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				
				//get position in centered, normalized space
				dr = (double) (width/2.0 - i)/(double)width;
				di = (double) (height/2.0 - j)/(double)height;
				
				tmpdr = dr; //rconst
				tmpdi = di; //iconst
								
				for (int k = 0; k < iterations; k++)
				{
					rsq = tmpdr*tmpdr;
					isq = tmpdi*tmpdi;
					tmpdi = (2.0 * tmpdr * tmpdi) + iconst;
					tmpdr = (rsq - isq) + rconst;
				}
				
				dr = tmpdr * (width*2.0 * multiplier);
				di = tmpdi * (height*2.0 * multiplier);
				
				//if (mapped)
				//{
				//	
				//}
				//dx = -dx;
				//dy = -dy;
				
				accurateX[pos] = (float)dr;
				accurateY[pos] = (float)di;
				
				//do some dithering:
				if (dither)
				{
					ofsi = (int)Math.round( (width/2.0 + dr) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (height/2.0 + di) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(width/2.0 + dr);
					ofsj = (int)Math.round(height/2.0 + di);
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Julia fractal topology set.");
	}
	
	public void setPointMandelTopology(float x, float y, int iterations, float multiplier,  boolean mapped, float[] map, int mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		int pos = 0;
		int ofsi, ofsj;
		double dr, di, tmpdr, tmpdi, rsq, isq, rconst, iconst;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				rconst = (i-width/2.0)/(width*3.0) + (x-width/2.0)/(width*3.0);
				iconst = (j-height/2.0)/(height*3.0) + (y-height/2.0)/(height*3.0);
				
				pos = i + j * width;
				
				//get position in centered, normalized space
				dr = (double) (width/2.0 - i)/(double)width;
				di = (double) (height/2.0 - j)/(double)height;
				
				tmpdr = dr; //rconst
				tmpdi = di; //iconst
								
				for (int k = 0; k < iterations; k++)
				{
					rsq = tmpdr*tmpdr;
					isq = tmpdi*tmpdi;
					tmpdi = (2.0 * tmpdr * tmpdi) + iconst;
					tmpdr = (rsq - isq) + rconst;
				}
				
				dr = tmpdr * (width*3.0 * multiplier);
				di = tmpdi * (height*3.0 * multiplier);
				
				//if (mapped)
				//{
				//	
				//}
				//dx = -dx;
				//dy = -dy;
				
				accurateX[pos] = (float)dr;
				accurateY[pos] = (float)di;
				
				//do some dithering:
				if (dither)
				{
					ofsi = (int)Math.round( (width/2.0 + dr) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (height/2.0 + di) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(width/2.0 + dr);
					ofsj = (int)Math.round(height/2.0 + di);
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Mandelbrot fractal topology set.");
	}
	
	public void setPointBarnsleyJuliaTopology(float x, float y, float rotation, float zoom, int iterations, float multiplier, boolean mapped, float[] map, int mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		int pos = 0;
		int ofsi, ofsj;
		double dr, di, tmpdr, tmpdi, rsq, isq, cr, ci;
		
		cr = (x-width/2.0)/ (width * zoom);
		ci = (y-height/2.0)/ (height * zoom);
		
		
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				
				//get position in centered, normalized space
				dr = (double) (width/2.0 - i)/(double)width;
				di = (double) (height/2.0 - j)/(double)height;
				
				//apply rotation:
				tmpdr = dr * Math.cos(rotation) - di * Math.sin(rotation);
				tmpdi = dr * Math.sin(rotation) + di * Math.cos(rotation);
				dr = tmpdr;
				di = tmpdi;
				
				//tmpdr = dr; //rconst
				//tmpdi = di; //iconst
								
				for (int k = 0; k < iterations; k++)
				{
					if (tmpdr >= 0)
					{
						rsq = ((tmpdr - 1.0) * cr) - (tmpdi * ci);
						isq = tmpdr*ci + tmpdi*cr;
						tmpdr = rsq;
						tmpdi = isq;
					} else {
						rsq = ((tmpdr + 1.0) * cr) - (tmpdi * ci);
						isq = tmpdr*ci + tmpdi*cr;
						tmpdr = rsq;
						tmpdi = isq;
					}
				}
				
				dr = tmpdr * (width*2.0 * multiplier);
				di = tmpdi * (height*2.0 * multiplier);
				
				//if (mapped)
				//{
				//	
				//}
				//dx = -dx;
				//dy = -dy;
				
				accurateX[pos] = (float)dr;
				accurateY[pos] = (float)di;
				
				//do some dithering:
				if (dither)
				{
					ofsi = (int)Math.round( (width/2.0 + dr) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (height/2.0 + di) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(width/2.0 + dr);
					ofsj = (int)Math.round(height/2.0 + di);
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("BarnsleyJulia fractal topology set.");
	}
	
	public void setPointBarnsleyMandelTopology(float x, float y, int iterations, float multiplier, boolean mapped, float[] map, int mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		int pos = 0;
		int ofsi, ofsj;
		double cr,ci,dr, di, tmpdr, tmpdi, rsq, isq, rconst, iconst;
		
		
		ci = (x-width/2.0)/(width*2.0);
		cr = (y-height/2.0)/(height*2.0);
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				
				//get position in centered, normalized space
				dr = (double) (width/2.0 - i)/(double)width;
				di = (double) (height/2.0 - j)/(double)height;
				
				tmpdr = dr + cr;
				tmpdi = di + ci;

				for (int k = 0; k < iterations; k++)
				{
					// barnsleym2:
					//if (tmpdr*di + dr*tmpdr >= 0)
					// barnsleym1
					if (tmpdr >= 0)
					{
						rsq = ((tmpdr - 1.0) * dr) - (tmpdi * di);
						isq = (tmpdr-1.0) * di + tmpdi * dr;
						tmpdr = rsq;
						tmpdi = isq;
					} else {
						rsq = ((tmpdr + 1.0) * dr) - (tmpdi * di);
						isq = tmpdi * di + (tmpdr-1.0) * dr;
						tmpdr = rsq;
						tmpdi = isq;
					}
				}
				
				dr = tmpdr * (width*2.0 * multiplier);
				di = tmpdi * (height*2.0 * multiplier);
				
				//if (mapped)
				//{
				//	
				//}
				//dx = -dx;
				//dy = -dy;
				
				accurateX[pos] = (float)dr;
				accurateY[pos] = (float)di;
				
				//do some dithering:
				if (dither)
				{
					ofsi = (int)Math.round( (width/2.0 + dr) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (height/2.0 + di) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(width/2.0 + dr);
					ofsj = (int)Math.round(height/2.0 + di);
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("BarnsleyMandel fractal topology set.");
	}
	
	public void setPointLambdaTopology(float x, float y, int iterations, float multiplier, boolean mapped, float[] map, int mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		int pos = 0;
		int ofsi, ofsj;
		double dr, di, tmpdr, tmpdi, rsq, isq, zrzi, zrsq, cr, ci;
		/*
			z = pixel
		loop:
  			z = @lambda * z * (1 - z)
		*/
		
		cr = (x-width/2.0)/(width*3.0);
		ci = (y-height/2.0)/(height*3.0);
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				
				//get position in centered, normalized space
				dr = (double) (width/2.0 - i)/(double)width;
				di = (double) (height/2.0 - j)/(double)height;
				
				tmpdr = dr; //rconst
				tmpdi = di; //iconst
								
				for (int k = 0; k < iterations; k++)
				{
					zrzi = tmpdr * tmpdi;
					zrsq = tmpdr * tmpdr;
					rsq = cr * (tmpdr - zrsq - tmpdi) + ci * (-zrzi + tmpdi * tmpdi);
					isq = cr * (-zrzi + tmpdi - zrzi) + ci * (tmpdr - zrsq + tmpdi - zrzi);
					tmpdr = rsq;
					tmpdi = isq;
				}
				
				dr = tmpdr * (width*2.0 * multiplier);
				di = tmpdi * (height*2.0 * multiplier);
				
				//if (mapped)
				//{
				//	
				//}
				//dx = -dx;
				//dy = -dy;
				
				accurateX[pos] = (float)dr;
				accurateY[pos] = (float)di;
				
				//do some dithering:
				if (dither)
				{
					ofsi = (int)Math.round( (width/2.0 + dr) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (height/2.0 + di) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(width/2.0 + dr);
					ofsj = (int)Math.round(height/2.0 + di);
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("BarnsleyJulia fractal topology set.");
	}
	
	public void setPointSpinTopology(int x, int y, double angle, 
	                                 boolean mapped, float[] map, float mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		int pos = 0;
		int ofsi, ofsj;
		double dx, dy, tmpdx, tmpdy, thisangle;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				
				dx = (double) (x - i);
				dy = (double) (y - j);
				//transform into plane [signed, axis is (x,y)]
				//double len = Math.sqrt(dx*dx + dy*dy);
				//if (len != 0)
				//{
				thisangle = angle;
				if (mapped)
				{
					thisangle *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				
				tmpdx = dx * Math.cos(thisangle) - dy * Math.sin(thisangle);
				tmpdy = dx * Math.sin(thisangle) + dy * Math.cos(thisangle);
				
				dx = tmpdx;
				dy = tmpdy;
				
				//}
				
				dx = -dx;
				dy = -dy;
				
				accurateX[pos] = (float)dx;
				accurateY[pos] = (float)dy;
				
				//do some dithering:
				if (dither)
				{
					ofsi = (int)Math.round( (x + dx) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (y + dy) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(x + dx);
					ofsj = (int)Math.round(y + dy);
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Point Spin topology set.");
	}
	
	public void setPerspectivePlaneTopology(int x, int y, double xzoomratio, double yzoomratio, 
	                                 boolean mapped, float[] map, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		int pos = 0;
		int ofsi, ofsj;
		double dx, dy, rdx, rdy;
		double rangle = randex.nextDouble() * Math.PI * 2.0;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				
				dx = (double) (x - i) / (double)(width/2.0);
				dy = (double) (y - j) / (double)(height/2.0);
				
				rdx = dx * Math.cos(rangle) - dy * Math.sin(rangle);
				rdy = dx * Math.sin(rangle) + dy * Math.cos(rangle);
				
				
				//System.out.println("normdy:      " + dy);
				
				rdy = (1.0 / rdy) * (double)(height/2.0);
				rdx = rdx * (rdy/100) * (double)width/2.0;
				rdy = (rdy < 0) ? -rdy : rdy;
				
				//transform into plane [signed, axis is (x,y)]
					
				rdx *= xzoomratio;
				rdy *= yzoomratio;
				
				//}
				
				if (mapped)
				{
					rdx *= (double)(map[pos])/(256.0);
					rdy *= (double)(map[pos])/(256.0);
				}
				
				dx = rdx * Math.cos(-rangle) - rdy * Math.sin(-rangle);
				dy = rdx * Math.sin(-rangle) + rdy * Math.cos(-rangle);
				
				dx = -dx;
				dy = -dy;
				
				accurateX[pos] = (float)dx;
				accurateY[pos] = (float)dy;
				
				//do some dithering:
				if (dither)
				{
					ofsi = (int)Math.round( (x + dx) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (y + dy) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(x + dx);
					ofsj = (int)Math.round(y + dy);
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Perspective Plane topology set.");
	}
	
	public void setPointZoomTopology(int x, int y, double xzoomratio, double yzoomratio, 
	                                 boolean mapped, float[] map, float mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		int pos = 0;
		int ofsi, ofsj;
		double dx, dy;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				
				dx = (double) ((x+width/2) - i);
				dy = (double) ((y+height/2) - j);
				
				//dx = (double) (x - i);
				//dy = (double) (y - j);
				//transform into plane [signed, axis is (x,y)]
				//double len = Math.sqrt(dx*dx + dy*dy);
				//if (len != 0)
				//{
				dx *= xzoomratio;
				dy *= yzoomratio;
				//}
				if (mapped)
				{
					dx *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
					dy *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				dx = -dx;
				dy = -dy;
				
				accurateX[pos] = (float)dx;
				accurateY[pos] = (float)dy;
				
				//do some dithering:
				if (dither)
				{
					ofsi = (int)Math.round( (x + dx) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (y + dy) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(x + dx);
					ofsj = (int)Math.round(y + dy);
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Point Zoom topology set.");
	}
	
	public void setMultiPointZoomTopology(int xcos[], int ycos[], double zoomratios[],
	                                      boolean mapped, float[] map, float mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		int pos = 0;
		int ofsi, ofsj;
		double dx, dy, wdx, wdy;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				dx = 0;
				dy = 0;
				for (int k = 0; k < xcos.length; k++)
				{
					wdx = (double) (xcos[k] - i) * zoomratios[k];
					wdy = (double) (ycos[k] - j) * zoomratios[k];
					dx += wdx;
					dy += wdy;
				}
				
				//transform into plane [signed, axis is (x,y)]
				//double len = Math.sqrt(dx*dx + dy*dy);
				//if (len != 0)
				//{
				//}
				if (mapped)
				{
					dx *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
					dy *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				
				//not correct...
				accurateX[pos] = -(float)dx;
				accurateY[pos] = -(float)dy;
				//do some dithering:
				if (dither)
				{
					//not correct yet
					ofsi = (int)Math.round( (xcos[0] - dx) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (ycos[0] - dy) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(xcos[0] - dx);
					ofsj = (int)Math.round(ycos[0] - dy);
				}
				
				//int ofsi = (int)Math.round(x - dx);
				//int ofsj = (int)Math.round(y - dy);
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("MultiPoint Zoom topology set.");
	}
	
	public void setPointInverseHorseshoeTopology(int x, int y, double multiplier, boolean mapped, 
	                                             float[] map, float mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		//numinputs = 1;
		//inputs = new int[lifesize][numinputs];
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		int pos = 0;
		int ofsi,ofsj;
		double dx, dy;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				
				dx = (double) ((x+width/2) - i);
				dy = (double) ((y+height/2) - j);
				//dx = (double) (x - i);
				//dy = (double) (y - j);
				double len = Math.sqrt(dx*dx + dy*dy);
				if (len!=0)
				{
					dx /= len;
					dy /= len;
				}
				
				dx *= multiplier;
				dy *= multiplier;
				if (mapped)
				{
					dx *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
					dy *= (double)(map[pos] + mapoffset)/(256.0+(double)mapoffset);
				}
				accurateX[pos] = (float)dy;
				accurateY[pos] = (float)dx;
				if (dither)
				{
					ofsi = (int)Math.round((i + dy) + (randex.nextDouble() - 0.5));//(x * (len/50.0));//((double) i + dy * multiplier); 
					ofsj = (int)Math.round((j + dx) + (randex.nextDouble() - 0.5));//(y * (len/50.0));//((double) j + dx * multiplier);
				} else {
					ofsi = (int)Math.round(i + dy);
					ofsj = (int)Math.round(j + dx);
				}
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Inverted Radial topology set.");
	}
	
	public void setSinusTopology(float xfreq, float xamp, float xphase,
	                             float yfreq, float yamp, float yphase, int inlayer, boolean ortho, boolean dither)
	{
		initialised = false;
		isweighted = false;
		initialised = true;
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				double xd = (( (double)j/(double)width ) * Math.PI * xfreq) + xphase;
				double xbd = xd;
				double yd = (( (double)i/(double)height ) * Math.PI * yfreq) + yphase;
				
				if (ortho) xd = Math.sin(xd) * xamp;
				else xd = Math.sin(yd) * xamp;
				
				if (ortho) yd = Math.sin(yd) * yamp;
				else yd = Math.sin(xbd) * yamp;
				
				int pos = j + i * width;
				
				accurateX[pos] = (float)xd;
				accurateY[pos] = (float)yd;
				if (dither)
				{
					inputs[pos][inlayer] = applySpacePolicy( (int)(j + xd + (randex.nextDouble() - 0.5) ), (int)(i + yd + (randex.nextDouble() - 0.5) ) );
				} else {
					inputs[pos][inlayer] = applySpacePolicy( (int)(j + xd), (int)(i + yd) );
				}
			}
		}
	}
	
	public void setPhaseSinusTopology(float xfreq, float xamp, float xphase,
	                             float yfreq, float yamp, float yphase, int inlayer, boolean ortho, boolean dither)
	{
		isweighted = false;
		
		double rotradius = Math.sqrt(xamp * xamp + yamp * yamp);
		double rotangle = Math.acos(yamp/rotradius);
		System.out.println("rotangle:" + rotangle + ".");
		
		double sinexdirection = yamp;
		double sineydirection = -xamp;
		
		double tmpx = 0, tmpy = 0;
		double xacc = 0, yacc = 0;
		
		//if ortho...
		//double normy = yamp/rotradius;
		//double normx = xamp/rotradius;		
				
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				double xd = (double)j/(double)width - 0.5;
				xd = xd * Math.PI * xfreq;
				
				double xbd = xd;
				
				double yd = (double)i/(double)height - 0.5;
				yd = yd * Math.PI * yfreq;
				
				if (ortho) xd = Math.sin(xd) * xamp;
				else xd = Math.sin(yd) * xamp;
				
				if (ortho) yd = Math.sin(yd) * yamp;
				else yd = Math.sin(xbd) * yamp;
				
				tmpx = xd * Math.cos(xphase) - yd * Math.sin(xphase);
				tmpy = xd * Math.sin(yphase) + yd * Math.cos(yphase);
				
				xd = tmpx;
				yd = tmpy;
				
				int pos = j + i * width;
				
				accurateX[pos] = (float)xd;
				accurateY[pos] = (float)yd;
				if (dither)
				{
					inputs[pos][inlayer] = applySpacePolicy( (int)(j + xd + (randex.nextDouble() - 0.5) ), (int)(i + yd + (randex.nextDouble() - 0.5) ) );
				} else {
					inputs[pos][inlayer] = applySpacePolicy( (int)(j + xd), (int)(i + yd) );
				}
			}
		}
	}
	
	//scraaaaaaaaaaaaaaap
	//for (int o = 1; o < octaves; o++)
				//{
					//for each octave pick random direction vector and amplitude
					
					
					//double octx = xd * Math.cos(rotangle) - yd * Math.sin(rotangle);
					//double octy = xd * Math.sin(rotangle) + yd * Math.cos(rotangle);
				//	if (ortho) 
				//	{
				//		tmpx = Math.sin(xd * Math.PI * xfreq + xphase) * xamp;
				//		tmpy = Math.sin(yd * Math.PI * yfreq + yphase) * yamp;
				//		
				//		double warprad = Math.sqrt(tmpx*tmpx+tmpy*tmpy);
				//		double warptheta = Math.acos(tmpx/warprad);
				//		
				//		xacc += tmpx * Math.cos(warptheta + rotangle) - tmpy * Math.sin(warptheta + rotangle);
				//		yacc += tmpx * Math.sin(warptheta + rotangle) + tmpy * Math.cos(warptheta + rotangle);
				//	} else {
				//		tmpx = Math.sin(yd * Math.PI * xfreq + xphase) * xamp;
				//		tmpy = Math.sin(xd * Math.PI * yfreq + yphase) * yamp;
				//		
				//		double warprad = Math.sqrt(tmpx*tmpx+tmpy*tmpy);
				//		double warptheta = Math.acos(tmpx/warprad);
				//		
				//		xacc += tmpx * Math.cos(warptheta + rotangle) - tmpy * Math.sin(warptheta + rotangle);
				//		yacc += tmpx * Math.sin(warptheta + rotangle) + tmpy * Math.cos(warptheta + rotangle);
				//	}
				//}
	
	public void setFooTopology(float x, float y, double angle, double amp, double power, int octaves,
	                               boolean mapped, float[] map, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		double xvec = Math.cos(angle) - Math.sin(angle);
		double yvec = Math.sin(angle) + Math.cos(angle);
				
		int pos = 0;
		int ofsi, ofsj;
		double dx, dy, tmpdx, tmpdy, t2dx, t2dy, thisangle, totalangle = 0;
		double angles[] = new double[octaves];
		double amps[] = new double[octaves];
		double freqs[] = new double[octaves];
		double phases[] = new double[octaves];
		
		for (int oq = 0; oq < octaves; oq++)
		{
			angles[oq] = randex.nextDouble() * Math.PI * 2.0;
			totalangle += angles[oq];
			amps[oq]   = randex.nextDouble() * 5 * amp;
			freqs[oq]  = randex.nextDouble() * 10;
			phases[oq]  = randex.nextDouble() * Math.PI * 2.0;
		}
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				dx = (double) (x - i);
				dy = (double) (y - j);
				//double radius = Math.sqrt(dx*dx+dy*dy);
				
				//transform into plane [signed, axis is (x,y)]
				//double len = Math.sqrt(dx*dx + dy*dy);
				//if (len != 0)
				//{
				
				tmpdx = dx;// * Math.cos(thisangle) - dy * Math.sin(thisangle);
				tmpdy = dy;// * Math.sin(thisangle) + dy * Math.cos(thisangle);
				
				for (int o = 0; o < octaves; o++)
				{
					//buffer the rotation
					t2dx = tmpdx * Math.cos(angles[o]) - tmpdy * Math.sin(angles[o]);
					t2dy = tmpdx * Math.sin(angles[o]) + tmpdy * Math.cos(angles[o]);
					tmpdx = t2dx;
					tmpdy = t2dy;
					
					if (mapped)
					{
						tmpdx += Math.sin(tmpdx/width * Math.PI * freqs[o] + phases[o]) * amps[o];
						tmpdy += Math.sin(tmpdy/height * Math.PI * freqs[o] + phases[o]) * amps[o];
					} else {
						tmpdx += Math.sin(tmpdy/width * Math.PI * freqs[o] + phases[o]) * amps[o];
						tmpdy += Math.sin(tmpdx/height * Math.PI * freqs[o] + phases[o]) * amps[o];
					}
				}
				//unrotate space
				dx = tmpdx * Math.pow(Math.cos(-totalangle),power) - tmpdy * Math.pow(Math.sin(-totalangle),power);
				dy = tmpdx * Math.pow(Math.sin(-totalangle),power) + tmpdy * Math.pow(Math.cos(-totalangle),power);
				
				//dx = tmpdx;
				//dy = tmpdy;
				dx = -dx;
				dy = -dy;
				
				accurateX[pos] = (float)((x+dx)-width/2.0);
				accurateY[pos] = (float)((y+dy)-height/2.0);
				
				//do some dithering:
				if (dither)
				{
					ofsi = (int)Math.round( (x + dx) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (y + dy) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(x + dx);
					ofsj = (int)Math.round(y + dy);
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
	}
				
	public void setFourierTopology(float x, float y, double angle, int octaves,
	                               boolean mapped, float[] map, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		double xvec = Math.cos(angle) - Math.sin(angle);
		double yvec = Math.sin(angle) + Math.cos(angle);
				
		int pos = 0;
		int ofsi, ofsj;
		double dx, dy, tmpdx, tmpdy, t2dx, t2dy, thisangle, totalangle = 0;
		double angles[] = new double[octaves];
		double amps[] = new double[octaves];
		double freqs[] = new double[octaves];
		double phases[] = new double[octaves];
		
		for (int oq = 0; oq < octaves; oq++)
		{
			angles[oq] = randex.nextDouble() * Math.PI * 2.0;
			totalangle += angles[oq];
			amps[oq]   = randex.nextDouble() * 5;
			freqs[oq]  = randex.nextDouble() * 10;
			phases[oq]  = randex.nextDouble() * Math.PI * 2.0;
		}
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				pos = i + j * width;
				dx = (double) (x - i);
				dy = (double) (y - j);
				//double radius = Math.sqrt(dx*dx+dy*dy);
				
				//transform into plane [signed, axis is (x,y)]
				//double len = Math.sqrt(dx*dx + dy*dy);
				//if (len != 0)
				//{
				
				tmpdx = dx;// * Math.cos(thisangle) - dy * Math.sin(thisangle);
				tmpdy = dy;// * Math.sin(thisangle) + dy * Math.cos(thisangle);
				
				for (int o = 0; o < octaves; o++)
				{
					//buffer the rotation
					t2dx = tmpdx * Math.cos(angles[o]) - tmpdy * Math.sin(angles[o]);
					t2dy = tmpdx * Math.sin(angles[o]) + tmpdy * Math.cos(angles[o]);
					tmpdx = t2dx;
					tmpdy = t2dy;
					
					if (mapped)
					{
						tmpdx += Math.sin(tmpdx/width * Math.PI * freqs[o] + phases[o]) * amps[o];
						tmpdy += Math.sin(tmpdy/height * Math.PI * freqs[o] + phases[o]) * amps[o];
					} else {
						tmpdx += Math.sin(tmpdy/width * Math.PI * freqs[o] + phases[o]) * amps[o];
						tmpdy += Math.sin(tmpdx/height * Math.PI * freqs[o] + phases[o]) * amps[o];
					}
				}
				//unrotate space
				dx = tmpdx * Math.cos(-totalangle) - tmpdy * Math.sin(-totalangle);
				dy = tmpdx * Math.sin(-totalangle) + tmpdy * Math.cos(-totalangle);
				
				//dx = tmpdx;
				//dy = tmpdy;
				dx = -dx;
				dy = -dy;
				
				accurateX[pos] = (float)((x+dx)-width/2.0);
				accurateY[pos] = (float)((y+dy)-height/2.0);
				
				//do some dithering:
				if (dither)
				{
					ofsi = (int)Math.round( (x + dx) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (y + dy) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(x + dx);
					ofsj = (int)Math.round(y + dy);
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
	}
	
	
	
	public void setOddTopology(float xfreq, float xamp, float xphase,
	                             float yfreq, float yamp, float yphase, int inlayer, boolean ortho, boolean dither)
	{
		isweighted = false;
		
		double rotradius = Math.sqrt(xamp * xamp + yamp * yamp);
		double rotangle = -Math.acos(yamp/rotradius);
		System.out.println("rotangle:" + rotangle + ".");
		
		double tmpx = 0, tmpy = 0;
		double xacc = 0, yacc = 0;
		
		//if ortho...
		double normy = yamp/rotradius;
		double normx = xamp/rotradius;		
				
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				double xd = ((double)j/(double)width) - 0.5;
				double yd = ((double)i/(double)height) - 0.5;
				xacc = yacc = 0;
				
				double smpradius = Math.sqrt(xd * xd + yd * yd);
				double smpangle = Math.acos(yd/smpradius);
				
				//get rotated point
				tmpx = xd * Math.cos(smpangle + rotangle) - yd * Math.sin(smpangle + rotangle);
				tmpy = xd * Math.sin(smpangle + rotangle) + yd * Math.cos(smpangle + rotangle);
				
				//do strange things...
				xacc = ((ortho) ? xamp : yamp) * Math.sin(tmpx * Math.PI * xfreq + xphase);
				yacc = ((ortho) ? yamp : xamp) * Math.sin(tmpy * Math.PI * yfreq + yphase);
				
				int pos = j + i * width;
				accurateX[pos] = (float)xacc;
				accurateY[pos] = (float)yacc;
				if (dither)
				{
					inputs[pos][inlayer] = applySpacePolicy( (int)(j + xacc + (randex.nextDouble() - 0.5) ), (int)(i + yacc + (randex.nextDouble() - 0.5)) );
				} else {
					inputs[pos][inlayer] = applySpacePolicy( (int)(j + xacc), (int)(i + yacc) );
				}
			}
		}
	}
	
	public void setPlusTopology() //connect each pixel to 4 others. . .
	                              //those directly above, below, to the left & right of it
	{
		if (debug) System.out.println("Setting plus topology.");
		ishomogeneous = true;
		isweighted = false;
		numinputs = 4;
		inputs = new int[lifesize][numinputs];
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				//if (inputs[i+j*width].length != numinputs) 
				//{inputs[i+j * width] = new Point[numinputs];}
				//loop the space in the negative
				int insert = i+j*width;
				
				inputs[insert] = new int[numinputs];
				
				inputs[insert][0] = applySpacePolicy(i-1,  j  );//left
				inputs[insert][1] = applySpacePolicy(i,    j-1);//up
				inputs[insert][2] = applySpacePolicy(i+1,  j  );//right
				inputs[insert][3] = applySpacePolicy(i,    j+1);//down
			}
		}
		if (debug) System.out.println("Plus topology set.");
	}
	
	public void setRandomTopology(float howfarm, int inlayer,boolean mapped, float[] map, int mapoffset)
	{	
		isweighted = false;
		if (debug) System.out.println("Setting Random Topology.");
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				int insert = i+j*width;
				
				if (inlayer < 0)
				for (int k = 0; k < inputs[insert].length; k++)
				{
					float t1 = ( ((randex.nextFloat()-0.5f) * 2.0f) * howfarm);
					float t2 = ( ((randex.nextFloat()-0.5f) * 2.0f) * howfarm);
					if (mapped)
					{
						t1 *= (double)(map[insert] + mapoffset)/(256.0+(double)mapoffset);
						t2 *= (double)(map[insert] + mapoffset)/(256.0+(double)mapoffset);
					}
					accurateX[insert] = t1;
					accurateY[insert] = t2;
					inputs[insert][k] = applySpacePolicy(i + t1,j + t2);
				} else {
					float t1 = ( ((randex.nextFloat() - 0.5f)*2.0f) * (howfarm));
					float t2 = ( ((randex.nextFloat() - 0.5f)*2.0f) * (howfarm));
					if (mapped)
					{
						t1 *= (double)(map[insert] + mapoffset)/(256.0+(double)mapoffset);
						t2 *= (double)(map[insert] + mapoffset)/(256.0+(double)mapoffset);
					}
					accurateX[insert] = t1;
					accurateY[insert] = t2;
					inputs[insert][inlayer] = applySpacePolicy(i + t1, j + t2);
				}	
			}	
		}
		if (debug) System.out.println("Random Topology Set.");
	}
	
	public void setRandomLoopTopology(int inlayer)
	{	
		ishomogeneous = true;
		isweighted = false;
		numinputs = 1;
		inputs = new int[lifesize][numinputs];
		int map[] = new int[lifesize];
		for (int i = 0; i < lifesize; i++)
		{map[i] = i;}
		int first = Math.abs(randex.nextInt() % lifesize);
		int rs = first;
		int rd = 0;
		int topone = lifesize - 1;
		
		for (int i = 0; i < lifesize; i++) //lifesize-1
		{
			rd = Math.abs(randex.nextInt() % topone);
			inputs[rs][inlayer] = rd;
			map[rd] = map[topone];
			topone--;
			rs = rd;
			//inputs[i] = rd;
		}
		inputs[rd][inlayer] = first;
		if (debug) System.out.println("Random loop Topology Set.");
	}
	
	public void setFractalNoiseTopology(float x, float y,int iterations,  float multiplier,  boolean mapped, float[] map, int mapoffset, boolean dither, int inlayer)
	{	
		initialised = false;
		ishomogeneous = true;
		isweighted = false;
		if (accurateX == null) accurateX = new float[lifesize];
		if (accurateY == null) accurateY = new float[lifesize];
		
		float octavegain = 0.656f;
		float octavegap = 1.8f;
		float feedOctaves = 0.15f;
		int pos = 0;
		int ofsi, ofsj;
		double dr, di, tmpdr, tmpdi, rsq, isq, rconst, iconst;
		
		float baseFreq = (float) ( Math.sqrt(x*x+y*y) / ( Math.sqrt( width * width * 0.25 + height * height * 0.25) ) )  * 0.5f;
		
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				rconst = (i-width/2.0)/(width/5.0) + (x-width/2.0)/(width/5.0);
				iconst = (j-height/2.0)/(height/5.0) + (y-height/2.0)/(height/5.0);
				
				pos = i + j * width;
				
				//get position in centered, normalized space
				dr = (double) (width/2.0 - i)/(double)width + x;
				di = (double) (height/2.0 - j)/(double)height + y;
				
				tmpdr = 0; //rconst
				tmpdi = 0; //iconst
				
				float oAmp = multiplier;	
				float oFreq = baseFreq;
				
				for (int k = 0; k < iterations; k++)
				{
					rsq = improvedPerlinNoise.improvedPerlinNoiseThree( rconst * oFreq + feedOctaves * dr + 11.23239, -98.26, -17.917 );
					isq = improvedPerlinNoise.improvedPerlinNoiseThree( iconst * oFreq + feedOctaves * di + 74.9125, 80.6, -201.755 );
					tmpdi += oAmp * rsq;
					tmpdr += oAmp * isq;
					oAmp *= octavegain;
					oFreq *= octavegap;
				}
				
				//dr += tmpdr * (width*5.0 * multiplier);
				//di += tmpdi * (height*5.0 * multiplier);
				
				//if (mapped)
				//{
				//	
				//}
				//dx = -dx;
				//dy = -dy;
				
				accurateX[pos] = (float)tmpdr;
				accurateY[pos] = (float)tmpdi;
				
				//do some dithering:
				if (dither)
				{
					ofsi = (int)Math.round( (width/2.0 + dr) + (randex.nextDouble() - 0.5) );
					ofsj = (int)Math.round( (height/2.0 + di) + (randex.nextDouble() - 0.5) );
				} else {
					ofsi = (int)Math.round(width/2.0 + dr);
					ofsj = (int)Math.round(height/2.0 + di);
				}
				
				inputs[pos][inlayer] = applySpacePolicy(ofsi,ofsj);
			}
		}
		initialised = true;
		if (debug) System.out.println("Noise fractal topology set.");
	}
	
	
	
	
	public synchronized void jitterTopology(float howfar, int inlayer, boolean mapped, int[] map, int mapoffset)
	{	
		if (debug) System.out.println("Jittering Topology.");
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				int insert = i+j*width;
				if (inlayer > 0)
				{
					int t1 = (int) ( (randex.nextGaussian()) * howfar);	
					int t2 = (int) ( (randex.nextGaussian()) * howfar);
					if (mapped)
					{
						t1 = (int) ((double) t1 * (double)(map[insert] + mapoffset)/(256.0+(double)mapoffset));
						t2 = (int) ((double) t2 * (double)(map[insert] + mapoffset)/(256.0+(double)mapoffset));
					}
					int toadd = t1 + t2 * width;
					int todo = inputs[insert][inlayer];
					todo += toadd;
					inputs[insert][inlayer] = applySpacePolicy(todo);
				} else {
					for (int k = 0; k < inputs[insert].length; k++)
					{
						int t1 = (int) ( (randex.nextGaussian()) * howfar);	
						int t2 = (int) ( (randex.nextGaussian()) * howfar);
						if (mapped)
						{
							t1 = (int) ((double) t1 * (double)(map[insert] + mapoffset)/(256.0+(double)mapoffset));
							t2 = (int) ((double) t2 * (double)(map[insert] + mapoffset)/(256.0+(double)mapoffset));
						}
						int toadd = t1 + t2 * width;
						int todo = inputs[insert][k];
						todo += toadd;
						inputs[insert][k] = applySpacePolicy(todo);
					}
				
				}
			}
		}
	}
	
	// TopologyFilter: create cyclic attractors & promote flowthrough.
	// Promote topology that results in pixel values being preserved and cycled.
	// There are many possible policies that can be used to do this
	
	public synchronized void promoteCyclicTopology(int howmany, int howlong, int inlayer)
	{	
		if (debug) System.out.println("Promoting Topological Cycles.");
		//currently operates only on layer r of the topology.
		int firstelement;
		int nextelement;
		int prevelement;
		
		for (int i = 0; i < howmany; i++)
		{
			firstelement = Math.abs(randex.nextInt() % lifesize);
			prevelement = firstelement;
			nextelement = firstelement;
			boolean terminatedloop = false;
			for (int j = 0; j < howlong; j++)
			{
				nextelement = inputs[prevelement][inlayer];
				prevelement = nextelement;
			}
			//close off the cycle.
			inputs[firstelement][inlayer] = nextelement;
		}
	}
	
	public synchronized void blurTopology(float howmuch)
	{	
		if (debug) System.out.println("Blurring Topology.");
		float xw[] = new float[lifesize];
		float yw[] = new float[lifesize];
		
		for (int i = 1; i < width - 1; i++) 
		{
			for (int j = 1; j < height - 1; j++) 
			{
				int insert = i+j*width;
				
				float xworking = ( accurateX[(insert-width)-1] +
				                   accurateX[(insert-width)  ] +
				                   accurateX[(insert-width)+1] +
				                   accurateX[(insert      )-1] +
				                   accurateX[(insert      )  ] +
				                   accurateX[(insert      )+1] +
				                   accurateX[(insert+width)-1] +
				                   accurateX[(insert+width)  ] +
				                   accurateX[(insert+width)+1] ) / 9.0f;
				
				float yworking = ( accurateY[(insert-width)-1] +
				                   accurateY[(insert-width)  ] +
				                   accurateY[(insert-width)+1] +
				                   accurateY[(insert      )-1] +
				                   accurateY[(insert      )  ] +
				                   accurateY[(insert      )+1] +
				                   accurateY[(insert+width)-1] +
				                   accurateY[(insert+width)  ] +
				                   accurateY[(insert+width)+1] ) / 9.0f;
				                   
				//need to buffer this.
				xw[insert] = (howmuch * xworking) + ((1.0f - howmuch) * accurateX[insert]);
				yw[insert] = (howmuch * yworking) + ((1.0f - howmuch) * accurateY[insert]);
			}
		}
		accurateX = xw;
		accurateY = yw;
		renderAccurateTopology(0);
	}
	
	public void renderAccurateTopology(int inlayer)
	{
		int pos;
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				pos = j + i * width;
				inputs[pos][inlayer] = applySpacePolicy((int)(j+accurateX[pos]),(int)(i+accurateY[pos]));
			}
		}
	}
	
	public float[] getAccurateX()
	{ return accurateX; }
	
	public float[] getAccurateY()
	{ return accurateY; }
	
	//wierd mechanism.. but I had to avoid modifying a live topology at all costs! :P
	//
	public void newBlendedAccurateTopology(float blendXa[], float blendYa[],float blendXb[], float blendYb[], float blendAlpha)
	{
		accurateX = new float[lifesize];
	 	accurateY = new float[lifesize];
	 	
		int pos;
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				pos = j + i * width;
				accurateX[pos] = (1.0f - blendAlpha) * blendXb[pos] + blendAlpha*blendXa[pos];
				accurateY[pos] = (1.0f - blendAlpha) * blendYb[pos] + blendAlpha*blendYa[pos];
				//inputs[pos][inlayer] = (absolute) ? applySpacePolicy((int)(width/2.0 + accurateX[pos]),(int)(width/2.0 + accurateY[pos])) 
				//                                  : applySpacePolicy((int)(j+accurateX[pos]),(int)(i+accurateY[pos]));
			}
		}
	}
	
	public void renderAccurateTopology(int inlayer,boolean absolute)
	{
		int pos;
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				pos = j + i * width;
				inputs[pos][inlayer] = (absolute) ? applySpacePolicy((int)(width/2.0 + accurateX[pos]),(int)(width/2.0 + accurateY[pos])) 
				                                  : applySpacePolicy((int)(j+accurateX[pos]),(int)(i+accurateY[pos]));
			}
		}
	}
	
	public void renderAccurateMultiTopology(int numlayers,boolean absolut)
	{
		numinputs = numlayers;
		inputs = new int[lifesize][numlayers];
		int pos;
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				pos = j + i * width;
				for (int k = 0; k < numlayers; k++)
				{
					//do a different dither in each channel.
					//should make this do weighted layers, simulating interpolation.
					//if (dither)
					//{
					
					//no point not dithering
					if (absolut) inputs[pos][k] = applySpacePolicy((int)( (width/2.0 + accurateX[pos])+(randex.nextFloat()-0.5f) ),(int)( (height/2.0 + accurateY[pos])+(randex.nextFloat()-0.5f) ) );
					else inputs[pos][k] = applySpacePolicy((int)( (j+accurateX[pos])+(randex.nextFloat()-0.5f) ),(int)( (i+accurateY[pos])+(randex.nextFloat()-0.5f) ) );
					//} else {
						//inputs[pos][k] = applySpacePolicy( (int)(j + accurateX[pos]),(int)(i + accurateY[pos]) );
					//}
				}
			}
		}
	}
	
	public void renderChainMultiTopology(int numdestlayers,boolean absolut, float compression)
	{
		//int numinputs = numdestlayers;
		if (numinputs < numdestlayers) 
		{ inputs = new int[flatlength][numdestlayers]; numinputs = numdestlayers; }
		//int inputs[][] = thetopology.getInputs();
		
		int pos, curriedinput;
		float curriedX, curriedY;
		float nextX, nextY;
		
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				pos = j + i * width;
				if (absolut)
				{
					curriedX = accurateX[pos];
					curriedY = accurateY[pos];
					
					int next = applySpacePolicy(curriedX,curriedY);
					inputs[pos][0] = next;
					
					for (int k = 1; k < numdestlayers; k++)
					{
						float deltaX = ( width/2.0f + accurateX[next] ) - curriedX;
						float deltaY = ( height/2.0f + accurateY[next] ) - curriedY;
						
						curriedX += deltaX * compression;
						curriedY += deltaY * compression;
						
						next = applySpacePolicy(curriedX,curriedY);
						inputs[pos][k] = next;
					}
				} else {
					curriedX = j + accurateX[pos];
					curriedY = i + accurateY[pos];
					
					int next = applySpacePolicy(curriedX,curriedY);
					inputs[pos][0] = next;
					
					for (int k = 1; k < numdestlayers; k++)
					{
						
						float deltaX = accurateX[next] - curriedX;
						float deltaY = accurateY[next] - curriedY;
						curriedX += deltaX * compression;
						curriedY += deltaY * compression;
						
						next = applySpacePolicy(curriedX,curriedY);
						inputs[pos][k] = next;
					}
				}
			}
		}
	}
	
	public void renderRampMultiTopology(int numdestlayers, boolean absolut, float multiplier)
	{
		//int numinputs = numdestlayers;
		if (numinputs < numdestlayers) 
		{ inputs = new int[flatlength][numdestlayers]; numinputs = numdestlayers; }
		//int inputs[][] = thetopology.getInputs();
		
		int pos, curriedinput;
		float curriedX, curriedY;
		float nextX, nextY;
		
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				if (absolut)
				{
					pos = j + i * width;
					//curriedX = accurateX[pos];
					//curriedY = accurateY[pos];
					//int next = applySpacePolicy(i + curriedX,j + curriedY);
					//inputs[pos][0] = next;
					
					for (int k = 0; k < numdestlayers; k++)
					{
						//curriedX *= multiplier;
						//curriedY *= multiplier;
						float rampValue = ( ( (float)k/(float)(numdestlayers-1)) ) * multiplier ;
						
						curriedX = accurateX[pos] * (rampValue * width/2);
						curriedY = accurateY[pos] * (rampValue * height/2);
						
						inputs[pos][k] = applySpacePolicy(width/2.0f + curriedX,height/2.0f + curriedY);
					}
				} else {
					pos = j + i * width;
					//curriedX = accurateX[pos];
					//curriedY = accurateY[pos];
					//int next = applySpacePolicy(i + curriedX,j + curriedY);
					//inputs[pos][0] = next;
					
					for (int k = 0; k < numdestlayers; k++)
					{
						float rampValue = ( (float)k/(float)numdestlayers ) * multiplier ;
						//float rampValue = ( ( (float)k/(float)numdestlayers ) - 0.5f ) * multiplier + 0.5f ;
						
						curriedX = accurateX[pos] * rampValue;
						curriedY = accurateY[pos] * rampValue;
						inputs[pos][k] = applySpacePolicy( j + curriedX, i + curriedY );
					}
				}
			}
		}
	}
	
	
	public void nextFrameDrawTopology(int dest[], int layer)
	{
		isProcessing = true;
		for (int i = 0; i < flatlength; i++)
		{
			//assumes space is brightest at highest addresses, this may need 'adjusting' ;).
			float lev = ((float)inputs[i][0]/(float)flatlength) * 256.0f;
			int ilev = (int) lev;
			dest[i] = PixelTools.makePackedGrey(ilev);
		}
		isProcessing = false;
	}
	
	public void nextFrameAverageBlend(int[] src, int dest[], boolean withalpha, int[] alpha)
	{
		isProcessing = true;
		//does life from state -> nxstate then swaps nxstate & state.
		int aaccum, raccum, gaccum, baccum;;
		//int accum = 0;
		//int unpak[] = new int[4];
		//int meval = 0;
		//int mepak[] = new int[4];
		int flatlength = src.length;
		setData(src);
		int numinputs = maxInputs();
		int pixelcatcher;
		
		for (int insert = 0; insert < flatlength; insert++) 
		{
			//System.out.println("About to get input pixels");
			//getInputData(insert, pixelcatcher);
			final int numinp = inputs[insert].length;
			pixelcatcher = 0;
			aaccum = 0;
			raccum = 0;
			gaccum = 0;
			baccum = 0;
			
			for (int k = 0; k < numinp; k++)
			{
				// this should eventually be done using methods accumulateRed(), Green(), 
				// Blue(), and accumulateAvg() that are optimized to work on arrays.
				// unpack(pixelcatcher[k],colaccum); //make sure this is optimised	
				pixelcatcher = src[inputs[insert][k]];
				//if (weighted) aaccum += (pixelcatcher >> 24) & 0xff; //albhahahaaaa
				raccum += (pixelcatcher >> 16) & 0xff; //reb
				gaccum += (pixelcatcher >>  8) & 0xff; //greeb
				baccum += (pixelcatcher      ) & 0xff; //lube
			}
			
			//unpack(meval,mepak);
			//int mxinp = max(accum);
			//mxinp = applyRule(mxinp, false);
			//int addit = rule.applyRule(accum, numinputs);
			
			//make averages
			raccum /= numinp; //r,
			gaccum /= numinp; //g,
			baccum /= numinp; //b.
			
			//accum[0] = (accum[0] > 0xff) ? 0xff : (accum[0] < 0) ? r : accum[0];
			
			if (withalpha)
			{
				int alva = alpha[insert];
				int invalv = 0xff - alva;
				int meval = src[insert];
				raccum = ((raccum * alva) >> 8) + ((((meval >>16) & 0xff) * invalv) >> 8) + 1;
				gaccum = ((gaccum * alva) >> 8) + ((((meval >> 8) & 0xff) * invalv) >> 8) + 1;
				baccum = ((baccum * alva) >> 8) + ((((meval     ) & 0xff) * invalv) >> 8) + 1;
			} else {}
			dest[insert] = PixelTools.pack(0xff,raccum,gaccum,baccum);
		}
		isProcessing = false;
	}
	
	//
	//public void inlineMultiblend() 
	//inlineblend with alpha-per-layer blending, list-instruction blending, particle/orbit traces, etc.
	
	public void nextFrameInlineBlend(int[] src, int dest[], boolean withalpha, int[] alpha)
	{
		isProcessing = true;
		//does life from state -> nxstate then swaps nxstate & state.
		int aaccum, raccum, gaccum, baccum, destr, destg, destb, desta;
		//int accum = 0;
		//int unpak[] = new int[4];
		//int meval = 0;
		//int mepak[] = new int[4];
		int flatlength = src.length;
		setData(src);
		int numinputs = maxInputs();
		int pixelcatcher;
		
		for (int insert = 0; insert < flatlength; insert++) 
		{
			//System.out.println("About to get input pixels");
			//getInputData(insert, pixelcatcher);
			final int numinp = inputs[insert].length;
			pixelcatcher = 0;
			int meval = dest[insert];
			int alva, alvadjusted, invalva;
			raccum = 0;
			gaccum = 0;
			baccum = 0;
			
			if (withalpha)
			{
				alva = alpha[insert];
				invalva = 0xff - alva;
			} else {
				alva = 0xff;	
				invalva = 0xff - alva;
			}
			
			desta = (meval >> 24) & 0xff;
			destr = (((meval >> 16) & 0xff) * invalva) >> 8;
			destg = (((meval >>  8) & 0xff) * invalva) >> 8;
			destb = (((meval      ) & 0xff) * invalva) >> 8;
			
			
			for (int k = 0; k < numinp; k++)
			{
				// this should eventually be done using methods accumulateRed(), Green(), 
				// Blue(), and accumulateAvg() that are optimized to work on arrays.
				// unpack(pixelcatcher[k],colaccum); //make sure this is optimised	
				pixelcatcher = src[inputs[insert][k]];
				
				//if (weighted) aaccum += (pixelcatcher >> 24) & 0xff; //albhahahaaa
				
				raccum += ((pixelcatcher >> 16) & 0xff);
				gaccum += ((pixelcatcher >>  8) & 0xff);
				baccum += ((pixelcatcher      ) & 0xff);
			}
			
			//unpack(meval,mepak);
			//int mxinp = max(accum);
			//mxinp = applyRule(mxinp, false);
			//int addit = rule.applyRule(accum, numinputs);
			
			//make averages
			raccum /= numinp; //r,
			gaccum /= numinp; //g,
			baccum /= numinp; //b.
			
			raccum = (alva * raccum) >> 8;
			gaccum = (alva * gaccum) >> 8;
			baccum = (alva * baccum) >> 8; 
			
			//accum[0] = (accum[0] > 0xff) ? 0xff : (accum[0] < 0) ? r : accum[0];
			
			dest[insert] = (desta << 24) + ( ((raccum + destr) & 0xff) << 16) +
			                            ( ((gaccum + destg) & 0xff) <<  8) +
			                            ( ((baccum + destb) & 0xff)      );
		}
		isProcessing = false;
	}
	
    public void nextFrameContrastyBlend(int[] src, int dest[], boolean withalpha, int[] alpha)
	{
		isProcessing = true;
		//does life from state -> nxstate then swaps nxstate & state.
		int aaccum, raccum, gaccum, baccum, destr, destg, destb, desta;
		//int accum = 0;
		//int unpak[] = new int[4];
		//int meval = 0;
		//int mepak[] = new int[4];
		int flatlength = src.length;
		setData(src);
		int numinputs = maxInputs();
		int pixelcatcher;
		
		for (int insert = 0; insert < flatlength; insert++) 
		{
			//System.out.println("About to get input pixels");
			//getInputData(insert, pixelcatcher);
			final int numinp = inputs[insert].length;
            //int numinp = inputs[insert].length;
            
			pixelcatcher = 0;
			int meval = dest[insert];
			int alva, alvadjusted, invalva;
			raccum = 0;
			gaccum = 0;
			baccum = 0;
			
			if (withalpha)
			{
				alva = alpha[insert];
				invalva = 0xff - alva;
			} else {
				alva = 0xff;	
				invalva = 0xff - alva;
			}
			
			desta = (meval >> 24) & 0xff;
			destr = (((meval >> 16) & 0xff) * invalva) >> 8;
			destg = (((meval >>  8) & 0xff) * invalva) >> 8;
			destb = (((meval      ) & 0xff) * invalva) >> 8;
			
			
			for (int k = 0; k < numinp; k++)
			{
				pixelcatcher = src[inputs[insert][k]];
				raccum += ((pixelcatcher >> 16) & 0xff);
				gaccum += ((pixelcatcher >>  8) & 0xff);
				baccum += ((pixelcatcher      ) & 0xff);
			}
			
			//make averages
			raccum /= numinp; //r,
			gaccum /= numinp; //g,
			baccum /= numinp; //b.
			
            raccum = (int)((raccum - 128) * 1.1) + 128;
            gaccum = (int)((gaccum - 128) * 1.1) + 128;
            baccum = (int)((baccum - 128) * 1.1) + 128;
            
            raccum = clamp(0,0xff,raccum);
			gaccum = clamp(0,0xff,gaccum);
			baccum = clamp(0,0xff,baccum);
            
			raccum = (alva * raccum) >> 8;
			gaccum = (alva * gaccum) >> 8;
			baccum = (alva * baccum) >> 8; 
			
			//accum[0] = (accum[0] > 0xff) ? 0xff : (accum[0] < 0) ? r : accum[0];
			
			dest[insert] = (desta << 24) + ( ((raccum + destr) & 0xff) << 16) +
			                            ( ((gaccum + destg) & 0xff) <<  8) +
			                            ( ((baccum + destb) & 0xff)      );
		}
		isProcessing = false;
	}
    
	public void nextFrameInlineBlendWithRule(int[] src, int dest[], float[] liferule, int[] colorliferule, boolean useColor, float rulemultiplier, boolean withalpha, int[] alpha)
	{
		isProcessing = true;
		//does life from state -> nxstate then swaps nxstate & state.
		int aaccum, raccum, gaccum, baccum, destr, destg, destb, desta;
		//int accum = 0;
		//int unpak[] = new int[4];
		//int meval = 0;
		//int mepak[] = new int[4];
		int flatlength = src.length;
		setData(src);
		int numinputs = maxInputs();
		int pixelcatcher;
		
		for (int insert = 0; insert < flatlength; insert++) 
		{
			//System.out.println("About to get input pixels");
			//getInputData(insert, pixelcatcher);
			final int numinp = inputs[insert].length;
			pixelcatcher = 0;
			int meval = dest[insert];
			int alva, alvadjusted, invalva;
			raccum = 0;
			gaccum = 0;
			baccum = 0;
			
			if (withalpha)
			{
				alva = alpha[insert];
				invalva = 0xff - alva;
			} else {
				alva = 0xff;	
				invalva = 0xff - alva;
			}
			
			desta = (meval >> 24) & 0xff;
			destr =  (meval >> 16) & 0xff;
			destg = (meval >>  8) & 0xff;
			destb = (meval      ) & 0xff;
			
			
			for (int k = 0; k < numinp; k++)
			{
				// this should eventually be done using methods accumulateRed(), Green(), 
				// Blue(), and accumulateAvg() that are optimized to work on arrays.
				// unpack(pixelcatcher[k],colaccum); //make sure this is optimised	
				pixelcatcher = src[inputs[insert][k]];
				
				//if (weighted) aaccum += (pixelcatcher >> 24) & 0xff; //albhahahaaa
				
				raccum += ((pixelcatcher >> 16) & 0xff);
				gaccum += ((pixelcatcher >>  8) & 0xff);
				baccum += ((pixelcatcher      ) & 0xff);
			}
			
			//unpack(meval,mepak);
			//int mxinp = max(accum);
			//mxinp = applyRule(mxinp, false);
			//int addit = rule.applyRule(accum, numinputs);
			
			//make averages
			raccum /= numinp; //r,
			gaccum /= numinp; //g,
			baccum /= numinp; //b.
			
			float avg = (float)(raccum + gaccum + baccum) /3.0f / 256.0f;
			int rulelookup = (int)(avg * liferule.length);
			//can assume its not signed here.,. until I process float pixel automata
			rulelookup = rulelookup % liferule.length;
			float rulevalue = liferule[rulelookup] / 128.0f;
			
			//alva may be signed or very large or small afer this..
			//alva = 0x100; //(int) ( rulevalue * 256.0f );
			alva = (int) ( Math.abs( rulevalue ) * 0xff );
			//alva = Math.max( 0, Math.min( 0xff, alva ) );
			invalva = 0xff - alva;
			invalva = Math.max( 0, Math.min( 0xff, invalva ) );
			
			int rinv = ((0xff - raccum) * alva) >> 8;
			int ginv = ((0xff - gaccum) * alva) >> 8;
			int binv = ((0xff - baccum) * alva) >> 8;
			
			destr = (destr * invalva) >> 8;
			destg = (destg * invalva) >> 8;
			destb = (destb * invalva) >> 8;
			
			if (useColor)
			{
				int rulecolor = colorliferule[ rulelookup ];
				
				int r = ((rulecolor >> 16) & 0xff);
				int g = ((rulecolor >>  8) & 0xff);
				int b = ((rulecolor      ) & 0xff);
				
				if ( rulevalue > 0)
				{
					raccum = (r * alva * raccum) >> 16;
					gaccum = (g * alva * gaccum) >> 16;
					baccum = (b * alva * baccum) >> 16;
				} else {
					rinv = (r * alva * rinv) >> 16;
					ginv = (g * alva * ginv) >> 16;
					binv = (b * alva * binv) >> 16;
				 }
				 
			} else {
				
				if ( rulevalue > 0)
				{
					raccum = (alva * raccum) >> 8;
					gaccum = (alva * gaccum) >> 8;
					baccum = (alva * baccum) >> 8; 
				} else {
				 	rinv = (alva * rinv) >> 8;
					ginv = (alva * ginv) >> 8;
					binv = (alva * binv) >> 8; 
				}
				
			}
			
			//ra = Math.max( 0, Math.min( 0xff, ra ) );
			//ga = Math.max( 0, Math.min( 0xff, ga ) );
			//ba = Math.max( 0, Math.min( 0xff, ba ) );
			if ( rulevalue > 0)
			{
				raccum = Math.max( 0, Math.min( 0xff, raccum ) );
				gaccum = Math.max( 0, Math.min( 0xff, gaccum ) );
				baccum = Math.max( 0, Math.min( 0xff, baccum ) );
				
				dest[insert] = (desta << 24) + 
							( ( destr + raccum ) << 16 ) +
							( ( destg + gaccum ) <<  8 ) + //was destr, oops
							( ( destb + baccum ) );
			} else {
				rinv = Math.max( 0, Math.min( 0xff, rinv ) );
				ginv = Math.max( 0, Math.min( 0xff, ginv ) );
				binv = Math.max( 0, Math.min( 0xff, binv ) );
				dest[insert] = (desta << 24) + 
							( ( destr + rinv ) << 16 ) +
							( ( destg + ginv ) <<  8) + //was destr
							( ( destb + binv ) );
			}
						
				//(( alva * raccum ) >> 8) + (( invalva * destr ) >> 8);   //Math.max( 0, Math.min( 0xff, ( destr * invalva ) >> 8 + raccum) );
				//(( alva * gaccum ) >> 8) + (( invalva * destg ) >> 8);   // Math.max( 0, Math.min( 0xff, ( destg * invalva ) >> 8 + gaccum) );
				// (( alva * baccum ) >> 8) + (( invalva * destb ) >> 8); //Math.max( 0, Math.min( 0xff, ( destb * invalva ) >> 8 + baccum) );
			                            
			//dest[insert] = (desta << 24) + ( ((destr) & 0xff) << 16) +
			//                            ( ( ( destg) & 0xff) <<  8) +
			//                            ( ( ( destb) & 0xff)      );
		}
		isProcessing = false;
	}
	
	public void nextFrameInlineLumiDiffMix(int[] src, int dest[], boolean withalpha, int[] alpha)
	{
		isProcessing = true;
		//does life from state -> nxstate then swaps nxstate & state.
		int aaccum, raccum, gaccum, baccum, destr, destg, destb, desta;
		//int accum = 0;
		//int unpak[] = new int[4];
		//int meval = 0;
		//int mepak[] = new int[4];
		int flatlength = src.length;
		setData(src);
		int numinputs = maxInputs();
		int pixelcatcher;
		
		for (int insert = 0; insert < flatlength; insert++) 
		{
			//System.out.println("About to get input pixels");
			//getInputData(insert, pixelcatcher);
			final int numinp = inputs[insert].length;
			pixelcatcher = 0;
			int meval = dest[insert];
			int alva, alvadjusted, invalva;
			raccum = 0;
			gaccum = 0;
			baccum = 0;
			aaccum = 0;
			
			if (withalpha)
			{
				alva = alpha[insert];
				invalva = 0xff - alva;
			} else {
				alva = 0xff;	
				invalva = 0xff - alva;
			}
			
			desta = (meval >> 24) & 0xff;
			destr = (((meval >> 16) & 0xff) * invalva) >> 8;
			destg = (((meval >>  8) & 0xff) * invalva) >> 8;
			destb = (((meval      ) & 0xff) * invalva) >> 8;
			int destlumi = (destr + destg + destb)/3;
			
			for (int k = 0; k < numinp; k++)
			{
				// this should eventually be done using methods accumulateRed(), Green(), 
				// Blue(), and accumulateAvg() that are optimized to work on arrays.
				// unpack(pixelcatcher[k],colaccum); //make sure this is optimised
				
				pixelcatcher = src[inputs[insert][k]];
				
				//if (weighted) aaccum += (pixelcatcher >> 24) & 0xff; //albhahahaaa
				
				aaccum += ((pixelcatcher >> 24) & 0xff);
				raccum += ((pixelcatcher >> 16) & 0xff);
				gaccum += ((pixelcatcher >>  8) & 0xff);
				baccum += ((pixelcatcher      ) & 0xff);
				
			}
			
			//unpack(meval,mepak);
			//int mxinp = max(accum);
			//mxinp = applyRule(mxinp, false);
			//int addit = rule.applyRule(accum, numinputs);
			
			//make averages
			raccum /= numinp; //r,
			gaccum /= numinp; //g,
			baccum /= numinp; //b.
			aaccum /= numinp; //a,
			
			int srclumi = (raccum + gaccum + baccum )/3;
			int lumidiff = (srclumi < destlumi) ? destlumi - srclumi : srclumi - destlumi;
			
			raccum = ((lumidiff * raccum) >> 8) + (((0xff - lumidiff) * destr) >> 8);
			gaccum = ((lumidiff * gaccum) >> 8) + (((0xff - lumidiff) * destg) >> 8);
			baccum = ((lumidiff * baccum) >> 8) + (((0xff - lumidiff) * destb) >> 8);
			
			int invalpha = 0xff - aaccum;
			
			raccum = ((raccum * aaccum) >> 8) + ((destr * invalpha) >> 8);
			gaccum = ((gaccum * aaccum) >> 8) + ((destg * invalpha) >> 8);
			baccum= ((baccum * aaccum) >> 8) + ((destb * invalpha) >> 8);
			
			dest[insert] = ((desta << 24) | (raccum << 16) | (gaccum << 8) | baccum);	
		}
		isProcessing = false;
	}
	
	public void nextFrameInlineAnotherLumiDiffMix(int[] src, int dest[], boolean withalpha, int[] alpha)
	{
		isProcessing = true;
		//does life from state -> nxstate then swaps nxstate & state.
		//int aaccum, raccum, gaccum, baccum, destr, destg, destb, desta;
        
        // At least don't destroy destA! though this may be meaningless or wrong..
        // maybe srca would be more important to preserve?
        int raccum, gaccum, baccum, destr, destg, destb, desta;
		//int accum = 0;
		//int unpak[] = new int[4];
		//int meval = 0;
		//int mepak[] = new int[4];
		int flatlength = src.length;
		setData(src);
		//int numinputs = maxInputs();
		int pixelcatcher;
		int decay = 0xe0;
        
		for (int insert = 0; insert < flatlength; insert++) 
		{
			//System.out.println("About to get input pixels");
			//getInputData(insert, pixelcatcher);
			final int numinp = inputs[insert].length;
			pixelcatcher = 0;
			int meval = dest[insert];
			//int alva, alvadjusted, invalva;
			raccum = 0;
			gaccum = 0;
			baccum = 0;
			
			/*if (withalpha)
			{
				alva = alpha[insert];
				invalva = 0xff - alva;
			} else {
				alva = 0xff;	
				invalva = 0xff - alva;
			}*/
			
			desta = (meval >> 24) & 0xff;
			destr = ((meval >> 16) & 0xff);
			destg = ((meval >>  8) & 0xff);
			destb = ((meval      ) & 0xff);
			
			
			for (int k = 0; k < numinp; k++)
			{
				// this should eventually be done using methods accumulateRed(), Green(), 
				// Blue(), and accumulateAvg() that are optimized to work on arrays.
				// unpack(pixelcatcher[k],colaccum); //make sure this is optimised	
				pixelcatcher = src[inputs[insert][k]];
				
				//if (weighted) aaccum += (pixelcatcher >> 24) & 0xff; //albhahahaaa
				
				raccum += ((pixelcatcher >> 16) & 0xff);
				gaccum += ((pixelcatcher >>  8) & 0xff);
				baccum += ((pixelcatcher      ) & 0xff);
			}
			
			//unpack(meval,mepak);
			//int mxinp = max(accum);
			//mxinp = applyRule(mxinp, false);
			//int addit = rule.applyRule(accum, numinputs);
			
			//make averages
			raccum /= numinp; //r,
			gaccum /= numinp; //g,
			baccum /= numinp; //b.
			
			//raccum = (alva * raccum) >> 8;
			//gaccum = (alva * gaccum) >> 8;
			//baccum = (alva * baccum) >> 8; 
			
			//accum[0] = (accum[0] > 0xff) ? 0xff : (accum[0] < 0) ? r : accum[0];
			
			//dest[insert] = (desta << 24) + ( ((raccum + destr) & 0xff) << 16) +
			//                            ( ((gaccum + destg) & 0xff) <<  8) +
			//                            ( ((baccum + destb) & 0xff)      );
			                            
			                            
			int destlumi = (destr + destg + destb)/3;
			int srclumi =  (raccum + gaccum + baccum)/3;
			
			raccum = (int) ( raccum * 1.25f + 32.0f);
			gaccum = (int) ( gaccum * 1.25f + 32.0f);
			baccum = (int) ( baccum * 1.25f + 32.0f);
			
			int lumidiff = (srclumi < destlumi) ? destlumi - srclumi : srclumi - destlumi;			
			lumidiff *= 2;
			
			//mix by difference in lumi with a bit of a boost ;)

			
			lumidiff = Math.min(0xff, Math.max(0, lumidiff));
			
            //raccum = Math.min(0xff, Math.max(0, raccum));
			//gaccum = Math.min(0xff, Math.max(0, gaccum));
			//baccum= Math.min(0xff, Math.max(0, baccum));
						
			raccum = ((lumidiff * raccum) >> 8) + (((0xff - lumidiff) * destr) >> 8);
			gaccum = ((lumidiff * gaccum) >> 8) + (((0xff - lumidiff) * destg) >> 8);
			baccum = ((lumidiff * baccum) >> 8) + (((0xff - lumidiff) * destb) >> 8);
			
			destr = Math.min(0xff, Math.max(0,((raccum + destr) * decay ) >> 8));
			destg = Math.min(0xff, Math.max(0,((gaccum + destg) * decay ) >> 8));
			destb = Math.min(0xff, Math.max(0,((baccum + destb) * decay ) >> 8));
            
            //destr = ((raccum * alva) >> 8) + ((destr * invalva) >> 8);
			//destg = ((gaccum * alva) >> 8) + ((destg * invalva) >> 8);
			//destb = ((baccum * alva) >> 8) + ((destb * invalva) >> 8);
			
            
			
			dest[ insert ] = ((desta << 24) | (destr << 16) | (destg<< 8) | destb);	
			                            
		}
		isProcessing = false;
	}
	
	
	public void nextFrameInlineAbsAdd(int[] src, int dest[], boolean withalpha, int[] alpha)
	{
		isProcessing = true;
		//does life from state -> nxstate then swaps nxstate & state.
		int aaccum, raccum, gaccum, baccum, destr, destg, destb, desta;
		//int accum = 0;
		//int unpak[] = new int[4];
		//int meval = 0;
		//int mepak[] = new int[4];
		int flatlength = src.length;
		setData(src);
		int numinputs = maxInputs();
		int pixelcatcher;
		
		for (int insert = 0; insert < flatlength; insert++) 
		{
			//System.out.println("About to get input pixels");
			//getInputData(insert, pixelcatcher);
			final int numinp = inputs[insert].length;
			pixelcatcher = 0;
			int meval = dest[insert];
			int alva, alvadjusted, invalva;
			raccum = 0;
			gaccum = 0;
			baccum = 0;
			
			if (withalpha)
			{
				alva = alpha[insert];
				invalva = 0xff - alva;
			} else {
				alva = 0xff;	
				invalva = 0xff - alva;
			}
			
			desta = (meval >> 24) & 0xff;
			destr = ((meval >> 16) & 0xff);
			destg = ((meval >>  8) & 0xff);
			destb = ((meval      ) & 0xff);
			
			
			for (int k = 0; k < numinp; k++)
			{
				// this should eventually be done using methods accumulateRed(), Green(), 
				// Blue(), and accumulateAvg() that are optimized to work on arrays.
				// unpack(pixelcatcher[k],colaccum); //make sure this is optimised	
				pixelcatcher = src[inputs[insert][k]];
				
				//if (weighted) aaccum += (pixelcatcher >> 24) & 0xff; //albhahahaaa
				
				raccum += ((pixelcatcher >> 16) & 0xff);
				gaccum += ((pixelcatcher >>  8) & 0xff);
				baccum += ((pixelcatcher      ) & 0xff);
			}
			
			//make averages
			raccum /= numinp; //r,
			gaccum /= numinp; //g,
			baccum /= numinp; //b.                            
			
			int srcdiffr = (raccum - 0x74)/4 + destr;
			int srcdiffg = (gaccum - 0x74)/4 + destg;
			int srcdiffb = (baccum - 0x74)/4 + destb;
			
			srcdiffr = (srcdiffr > 0xff) ? 0xff : (srcdiffr < 0) ? 0 : srcdiffr;
			srcdiffg = (srcdiffg > 0xff) ? 0xff : (srcdiffg < 0) ? 0 : srcdiffg;
			srcdiffb = (srcdiffb > 0xff) ? 0xff : (srcdiffb < 0) ? 0 : srcdiffb;
			
			int redbit   = ((srcdiffr * alva) >> 8) + ((destr * invalva) >> 8);
			int greenbit = ((srcdiffg * alva) >> 8) + ((destg * invalva) >> 8);
			int bluebit  = ((srcdiffb * alva) >> 8) + ((destb * invalva) >> 8);
			
			dest[insert] = ((0xff << 24) | (redbit << 16) | (greenbit << 8) | bluebit);
						
		}
		isProcessing = false;
	}
	
	public void nextFrameRuleMaxMin(int[] src, int dest[], float[] liferule, float ruleMultiplier, boolean withalpha, int[] alpha, boolean transfer)
	{
		isProcessing = true;
		
		int r, g, b, l, aaccum, raccum, gaccum, baccum, destr, destg, destb, desta;
		
		int flatlength = src.length;
		setData(src);
		int numinputs = maxInputs();
		int pixelcatcher;
		
		for (int insert = 0; insert < flatlength; insert++) 
		{
			int rmax = 0;
			int gmax = 0;
			int bmax = 0;
			
			int rmin = 0xff;
			int gmin = 0xff;
			int bmin = 0xff;
			
			//int lumin = 0xff;
			//int lumax = 0;
			
			final int numinp = inputs[insert].length;
			pixelcatcher = 0;
			int meval = dest[insert];
			int alva, alvadjusted, invalva;
			raccum = 0;
			gaccum = 0;
			baccum = 0;
			
			if (withalpha)
			{
				alva = alpha[insert];
				invalva = 0xff - alva;
			} else {
				alva = 0xff;	
				invalva = 0xff - alva;
			}
			
			desta = (meval >> 24) & 0xff;
			destr = ((meval >> 16) & 0xff);
			destg = ((meval >>  8) & 0xff);
			destb = ((meval      ) & 0xff);
			
			for (int k = 0; k < numinp; k++)
			{
				pixelcatcher = src[inputs[insert][k]];
				
				r = ((pixelcatcher >> 16) & 0xff);
				g = ((pixelcatcher >>  8) & 0xff);
				b = ((pixelcatcher      ) & 0xff);
				
				//l = (r + g + b)/3; //must find out the weightings to make this a 'luminosity' rather than average..
				
				//if (l < lumin)
				//{ lumin = l; rmin = r;  gmin = g; bmin = b; }
				
				//if ( l >= lumax )
				//{ lumax = l; rmax = r; gmax = g; bmax = b; }
				
				//wonder what this'll do.. ;)
				if (r < rmin) r = rmin;
				if (g < gmin) g = gmin;
				if (b < bmin) b = bmin;
				
				if (r > rmax) r = rmax;
				if (g > gmax) g = gmax;
				if (b > bmax) b = bmax;
				
				raccum += r;
				gaccum += g;
				baccum += b;
			}
						
			//make averages
			raccum /= numinp; //r,
			gaccum /= numinp; //g,
			baccum /= numinp; //b.
			
			//should all be positive until I run a floating pt automata..
			float looker = ( (float) (raccum + gaccum + baccum)/3.0f / 256.0f) * liferule.length;
			
			float lifeamount = liferule[ (int)looker % liferule.length ] / 256.0f * ruleMultiplier;
			
			if ( lifeamount >= 0 )
			{
				if (transfer)
				{ 
					raccum = (int) ( ( rmax - raccum ) * ( lifeamount + 1.0f ) ) + raccum;
					gaccum = (int) ( ( gmax - gaccum ) * ( lifeamount + 1.0f ) ) + gaccum;
					baccum= (int) ( ( bmax - baccum ) * ( lifeamount +1.0f ) ) + baccum;
				} else {
					raccum = destr + (int) ( ( rmax - raccum ) * ( lifeamount + 1.0f ) ) + raccum;
					gaccum = destg + (int) ( ( gmax - gaccum ) * ( lifeamount + 1.0f ) ) + gaccum;
					baccum= destb + (int) ( ( bmax - baccum ) * ( lifeamount +1.0f ) ) + baccum;
				}
			} else {
				if (transfer)
				{ 
					raccum = (int) ( ( rmin - raccum ) * ( -lifeamount + 1.0f ) ) + raccum;
					gaccum = (int) ( ( gmin - gaccum ) * ( -lifeamount + 1.0f ) ) + gaccum;
					baccum= (int) ( ( bmin - baccum ) * ( -lifeamount + 1.0f ) ) + baccum;
				} else {
					raccum = destr + (int) ( ( rmin - raccum ) * ( -lifeamount + 1.0f ) ) + raccum;
					gaccum = destg + (int) ( ( gmin - gaccum ) * ( -lifeamount + 1.0f ) ) + gaccum;
					baccum= destb + (int) ( ( bmin - baccum ) * ( -lifeamount + 1.0f ) ) + baccum;
				}
				
			}
			
			raccum = clamp(0,0xff,raccum);
			gaccum = clamp(0,0xff,gaccum);
			baccum = clamp(0,0xff,baccum);
			
			destr   = ((raccum * alva) >> 8) + ((destr * invalva) >> 8);
			destg = ((gaccum * alva) >> 8) + ((destg * invalva) >> 8);
			destb = ((baccum * alva) >> 8) + ((destb * invalva) >> 8);
			
			dest[ insert ] = ((0xff << 24) | (destr << 16) | (destg<< 8) | destb);	
			                            
		}
		isProcessing = false;
	}
	
	public void nextFrameUnpacked(int a[], int r[], int g[], int b[], int desta[], int destr[], int destg[], int destb[], int w, int h)
	{
		isProcessing = true;
		//does life from a[],r[],g[],b[] -> desta[],destr[],destg[],destb[].
		//does alpha.
		int aaccum, raccum, gaccum, baccum;
		int flatlength = w * h;
		int numinputs = maxInputs();
		
		for (int insert = 0; insert < flatlength; insert++) 
		{
			int numinp = inputs[insert].length;
			aaccum = 0;
			raccum = 0;
			gaccum = 0;
			baccum = 0;
			for (int k = 0; k < numinp; k++)
			{
				int inpaddr = inputs[insert][k];
				aaccum += a[inpaddr]; //albhahahaaaa
				raccum += r[inpaddr]; //reb
				gaccum += g[inpaddr]; //greeb
				baccum += b[inpaddr]; //lube
			}
			//make averages
			aaccum /= numinp; //a,
			raccum /= numinp; //r,
			gaccum /= numinp; //g,
			baccum /= numinp; //b.
			
			//accum[0] = (accum[0] > 0xff) ? 0xff : (accum[0] < 0) ? 0 : accum[0];
			//alpha channel used to blend after, not during input gathering
			//experiment with this...
			//aaccum = a[insert]; 
			//int invsrc = 0xff - aaccum;
			desta[insert] = aaccum;//((raccum * aaccum) >> 8) + ((r[insert] * invsrc) >> 8);
			destr[insert] = raccum;//((raccum * aaccum) >> 8) + ((r[insert] * invsrc) >> 8);
			destg[insert] = gaccum;//((gaccum * aaccum) >> 8) + ((g[insert] * invsrc) >> 8);
			destb[insert] = baccum;//((baccum * aaccum) >> 8) + ((b[insert] * invsrc) >> 8);
		}
		isProcessing = false;
	}
	
	public void nextFrameSingleLayer(int layer, int[] src, int[] dest)
	{
		isProcessing = true;
		int flatlength = src.length;
		if (width*height == flatlength)
		{
			for (int insert = 0; insert < flatlength; insert++) 
			{
				if (layer < inputs[insert].length) dest[insert] = src[inputs[insert][layer]];
			}
		} else { System.out.println("Tried to apply topology to wrong sized array"); }
		isProcessing = false;
	}
	
	public void nextFrameSingleLayerWithAlpha(int[] src, int[] dest)
	{
		isProcessing = true;
		//assumes everything is the right size and that the alpha's are packed
		int flatlength = src.length;
		if (width*height == flatlength)
		{
			for (int insert = 0; insert < flatlength; insert++) 
			{ 
				dest[insert] = PixelTools.pixelAlphaBlend(src[inputs[insert][0]],dest[insert]);
			}
		} else {
			System.out.println("Tried to apply topology to wrong sized array"); 
		}
		isProcessing = false;
	}
	
	public void nextFrameSingleLayerWithAlphaChannelInDest(int layer, int[] src, int[] dest, int[] alpha)
	{
		isProcessing = true;
		//assumes everything is the right size
		setData(src);
		int flatlength = src.length;
		if (width*height == flatlength)
		{
			for (int insert = 0; insert < flatlength; insert++) 
			{ 
				dest[insert] = PixelTools.blendColors(src[inputs[insert][layer]],dest[insert],alpha[insert]);
			}
		} else {
			System.out.println("Tried to apply topology to wrong sized array"); 
		}
		isProcessing = false;
	}
	
	public void nextFrameSingleLayerWithAlphaChannelInSrc(int layer, int[] src, int[] dest, int[] alpha)
	{
		isProcessing = true;
		//assumes everything is the right size
		int flatlength = src.length;
		int srcalpha = 0;
		if (width*height == flatlength)
		{
			for (int insert = 0; insert < flatlength; insert++) 
			{
				//should check the input exists
				dest[insert] = PixelTools.blendColors(src[inputs[insert][layer]],dest[insert],alpha[inputs[insert][layer]]);
			}
		} else {
			System.out.println("Tried to apply topology to wrong sized array"); 
		}
		isProcessing = false;
	}
	
	public void nextFrameWithKernel(int[] src, int[] dest, int[] kernel, boolean withalpha, int[] alpha)
	{
		isProcessing = true;
		int pos, grabber;
		int rbit,gbit,bbit, avg;
		int kernelval;
		int kernellength = kernel.length;
		int scale = 0;
		for (int i=0; i < kernel.length; i++) scale += kernel[i];
		if (scale==0) scale = 1;
		
		//System.out.println("Doing NextFrame With Kernel");
		
		for (int i = 0; i < src.length; i++)
		{
			grabber = 0;
			rbit = 0;
			gbit = 0;
			bbit = 0;
			
			int numinp = inputs[i].length;
			if (numinp <= kernel.length)
			for (int j = 0; j < kernel.length; j++)
			{
				grabber = src[inputs[i][j]];
				kernelval = kernel[j]; //not checked to be long enough
				rbit += ((grabber >> 16) & 0xff) * kernelval; //reb
				gbit += ((grabber >>  8) & 0xff) * kernelval; //greeb
				bbit += ((grabber      ) & 0xff) * kernelval; //lube
			} else {System.out.println("Kernel too short."); }
			
			//scale to kernel
			rbit /= scale; 
			gbit /= scale; 
			bbit /= scale;
			
			if (withalpha)
			{
				int alph = alpha[i];
				int invalph = 0xff - alph;
				int meval = src[i];
				rbit = ((rbit * alph) >> 8) + ((((meval >>16) & 0xff) * invalph) >> 8);
				gbit = ((gbit * alph) >> 8) + ((((meval >> 8) & 0xff) * invalph) >> 8);
				bbit = ((bbit * alph) >> 8) + ((((meval     ) & 0xff) * invalph) >> 8);
			}
			
			dest[i] = PixelTools.pack(src[i] & 0xff000000,rbit,gbit,bbit);
		}
		isProcessing = false;
	}

	public int clamp(int lower,int upper,int val)
	{
		return (val <= upper) ? (val >= lower) ? val : lower : upper;
	}
	
	public void nextFrameWithLife( int[] src, int[] dest, float[] liferule, int[] colorliferule, float ruledensity, float ruleoffset, float rulescaling, boolean withalpha, int[] alpha, boolean transfer, boolean docolor )
	{
		isProcessing = true;
		try
		{
			int pos, grabber;
			int racc,gacc,bacc,destr,destg,destb, desta;
			float scale = 0;
			for (int l=0; l < liferule.length; l++) scale += liferule[l];
			if (scale==0.0) scale = 1.0f;		
			//if (debug) 
			//System.out.println("Doing NextFrame With Rule");
			
			for (int i = 0; i < src.length; i++)
			{
				grabber = 0;
				racc = 0;
				gacc = 0;
				bacc = 0;
				
				int numinp = inputs[i].length;
				
				//System.out.println("About to accumulate.");
				for (int j = 0; j < numinp; j++)
				{
					grabber = src[inputs[i][j]];
					//weighted connections would be computed here.	
					racc += ((grabber >> 16) & 0xff);// * kernelval; //reb
					gacc += ((grabber >>  8) & 0xff);// * kernelval; //greeb
					bacc += ((grabber      ) & 0xff);// * kernelval; //lube
				}
				
				//scale to liferule ? 
				//racc /= scale; 
				//gacc /= scale; 
				//bacc /= scale;
				//average
				
				racc /= numinp;
				gacc /= numinp;
				bacc /= numinp;
				
				//rbit = applySpacePolicy(rbit,0,liferule.length);
				//racc = (racc >= liferule.length) ? liferule.length - 1 : (racc < 0) ? 0 : racc;
				//gacc = (gacc >= liferule.length) ? liferule.length - 1 : (gacc < 0) ? 0 : gacc;
				//bacc = (bacc >= liferule.length) ? liferule.length - 1 : (bacc < 0) ? 0 : bacc;
				//System.out.println("About to apply rule.");
				//apply rule (then u get a number positive or negative.
				
				int alf,invalf;
				
				if (withalpha)
				{
					alf = alpha[i];
					invalf = 0xff - alf;
				} else {
					alf = 0xff;	
					invalf = 0xff - alf;
				}
				
				int meval = src[i];
				desta = (meval >> 24) & 0xff;
				destr = (((meval >> 16) & 0xff) * invalf) >> 8;
				destg = (((meval >>  8) & 0xff) * invalf) >> 8;
				destb = (((meval      ) & 0xff) * invalf) >> 8;
				
				if (transfer)
				{
					int ruleix = (int)((racc+gacc+bacc/3 * ruledensity + ruleoffset)) % liferule.length;
					int ruleamnt = (int) (rulescaling * liferule[ ruleix ] );
					
					//gacc += (int) (128 * liferule[gacc ]);
					//bacc += (int) (128 * liferule[bacc % liferule.length]);
					
					//rule as alpha amount? (front/backyard)	
					if (docolor)
					{
						int col =  colorliferule[ ruleix ];
						racc += (((col >> 16) & 0xff) * ruleamnt) >> 8;
						gacc += (((col >>  8) & 0xff) * ruleamnt) >> 8;
						bacc += (((col      ) & 0xff) * ruleamnt) >> 8;
					} else {
					
						racc += ruleamnt;
						gacc += ruleamnt;
						bacc += ruleamnt;
					}
					
					racc = ((racc * alf) >> 8);
					gacc = ((gacc * alf) >> 8);
					bacc = ((bacc * alf) >> 8);
					racc = clamp(0,0xff,racc);
					gacc = clamp(0,0xff,gacc);
					bacc = clamp(0,0xff,bacc);
					
					dest[i] = (desta << 24) + ( ((racc + destr) & 0xff) << 16) +
										   ( ((gacc + destg) & 0xff) <<  8) +
										   ( ((bacc + destb) & 0xff)      ); 
					                              
				} else { //
					if (docolor)
					{
						int rix = (int)(racc*ruledensity + ruleoffset) % liferule.length;
						int gix = (int)(gacc*ruledensity + ruleoffset) % liferule.length;
						int bix =  (int)(bacc*ruledensity + ruleoffset) % liferule.length;
						
						int r = (colorliferule[ rix ] >> 16) & 0xff;
						int g = (colorliferule[ gix ] >>  8) & 0xff;
						int b = (colorliferule[ bix ] ) & 0xff;
						
						racc = ( (int) (rulescaling * liferule[ rix ] ) * r) >> 8; 
						gacc = ( (int) (rulescaling * liferule[ gix ] ) * g) >> 8;;
						bacc = ( (int) (rulescaling * liferule[ bix ] ) * b) >> 8;;
						
						racc = clamp(0,0xff,racc);
						gacc = clamp(0,0xff,gacc);
						bacc = clamp(0,0xff,bacc);
						
						dest[i] = (desta << 24) + ( ((racc + destr) & 0xff) << 16) +
											   ( ((gacc + destg) & 0xff) <<  8) +
											   ( ((bacc + destb) & 0xff)      );
						
					} else {
						racc = (int) (rulescaling * liferule[ (int)(racc*ruledensity + ruleoffset) % liferule.length]); //these +128's were not da original flayvah
						gacc = (int) (rulescaling * liferule[ (int)(gacc*ruledensity + ruleoffset) % liferule.length]);
						bacc = (int) (rulescaling * liferule[ (int)(bacc*ruledensity + ruleoffset) % liferule.length]);
						int avg = (racc+bacc+gacc)/3;
						if (withalpha) avg = (avg * alf) >> 8;
						
						avg = clamp(0,0xff,avg);
						
						dest[i] = (desta << 24) + ( ((avg + destr) & 0xff) << 16) +
											   ( ((avg + destg) & 0xff) <<  8) +
											   ( ((avg + destb) & 0xff)      );
					}
					
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {}//System.out.print("[" + e.toString() + "]");}
		isProcessing = false;
	}
	
	public void nextFrameWithNewLife( int[] src, int[] dest, float[] liferule, int[] colorliferule, float ruledensity, float ruleoffset, float rulescaling, boolean withalpha, int[] alpha, boolean transfer, boolean colorLinkedChannels )
	{
		isProcessing = true;
		try
		{
			int pos, grabber;
			int racc,gacc,bacc,destr,destg,destb, desta;
			float scale = 0;
			for (int l=0; l < liferule.length; l++) scale += liferule[l];
			if (scale==0.0) scale = 1.0f;		
			//if (debug) 
			//System.out.println("Doing NextFrame With Rule");
			
			for (int i = 0; i < src.length; i++)
			{
				grabber = 0;
				racc = 0;
				gacc = 0;
				bacc = 0;
				
				int numinp = inputs[i].length;
				
				//System.out.println("About to accumulate.");
				for (int j = 0; j < numinp; j++)
				{
					grabber = src[inputs[i][j]];
					//weighted connections would be computed here.	
					racc += ((grabber >> 16) & 0xff);// * kernelval; //reb
					gacc += ((grabber >>  8) & 0xff);// * kernelval; //greeb
					bacc += ((grabber      ) & 0xff);// * kernelval; //lube
				}
				
				//scale to liferule ? 
				//racc /= scale; 
				//gacc /= scale; 
				//bacc /= scale;
				//average
				
				racc /= numinp;
				gacc /= numinp;
				bacc /= numinp;
				
				//rbit = applySpacePolicy(rbit,0,liferule.length);
				//racc = (racc >= liferule.length) ? liferule.length - 1 : (racc < 0) ? 0 : racc;
				//gacc = (gacc >= liferule.length) ? liferule.length - 1 : (gacc < 0) ? 0 : gacc;
				//bacc = (bacc >= liferule.length) ? liferule.length - 1 : (bacc < 0) ? 0 : bacc;
				//System.out.println("About to apply rule.");
				//apply rule (then u get a number positive or negative.
				
				int alf,invalf;
				
				if (withalpha)
				{
					alf = alpha[i];
					invalf = 0xff - alf;
				} else {
					alf = 0xff;	
					invalf = 0xff - alf;
				}
				
				int lookupPackedColor = (alf << 24) + ( ( (racc & 0xff) << 16) +
										   ( (gacc & 0xff) <<  8) +
										   (bacc & 0xff) ); 
										   
				float lookupVal = MiniPixelTools.getDefaultF( lookupPackedColor );
				
				int meval = src[i];
				desta = (meval >> 24) & 0xff;
				destr = (((meval >> 16) & 0xff) * invalf) >> 8;
				destg = (((meval >>  8) & 0xff) * invalf) >> 8;
				destb = (((meval      ) & 0xff) * invalf) >> 8;
				
				if (transfer)
				{
					int ruleix = (int)((lookupVal * ruledensity + ruleoffset)) % liferule.length;
					int ruleamnt = (int) (rulescaling * liferule[ ruleix ] );
					
					//gacc += (int) (128 * liferule[gacc ]);
					//bacc += (int) (128 * liferule[bacc % liferule.length]);
					
					//rule as alpha amount? (front/backyard)	
					if (colorLinkedChannels)
					{
						int col =  colorliferule[ ruleix ];
						racc += (((col >> 16) & 0xff) * ruleamnt) >> 8;
						gacc += (((col >>  8) & 0xff) * ruleamnt) >> 8;
						bacc += (((col      ) & 0xff) * ruleamnt) >> 8;
						racc = ((racc * alf) >> 8);
						gacc = ((gacc * alf) >> 8);
						bacc = ((bacc * alf) >> 8);
						racc = clamp(0,0xff,racc);
						gacc = clamp(0,0xff,gacc);
						bacc = clamp(0,0xff,bacc);
					
						dest[i] = (desta << 24) + ( ((racc + destr) & 0xff) << 16) +
											   ( ((gacc + destg) & 0xff) <<  8) +
											   ( ((bacc + destb) & 0xff)      ); 
					} else {
						int col =  colorliferule[ ruleix ];
						racc = ((col >> 16) & 0xff) - racc;
						gacc = ((col >>  8) & 0xff) - gacc;
						bacc = ((col      ) & 0xff) - bacc;
						
						//destr *= ruleamnt;
						//destg *= ruleamnt;
						//destb *= ruleamnt;
						
						dest[i] = (desta << 24) + ( ((racc + destr) & 0xff) << 16) +
											   ( ((gacc + destg) & 0xff) <<  8) +
											   ( ((bacc + destb) & 0xff)      ); 
					}
					
					
					                              
				} else { //
					if (colorLinkedChannels)
					{
						int rix = (int)(racc*ruledensity + ruleoffset) % liferule.length;
						int gix = (int)(gacc*ruledensity + ruleoffset) % liferule.length;
						int bix =  (int)(bacc*ruledensity + ruleoffset) % liferule.length;
						
						int r = (colorliferule[ rix ] >> 16) & 0xff;
						int g = (colorliferule[ gix ] >>  8) & 0xff;
						int b = (colorliferule[ bix ] ) & 0xff;
						
						racc = ( (int) (rulescaling * liferule[ rix ] ) * r) >> 8; 
						gacc = ( (int) (rulescaling * liferule[ gix ] ) * g) >> 8;;
						bacc = ( (int) (rulescaling * liferule[ bix ] ) * b) >> 8;;
						
						racc = clamp(0,0xff,racc);
						gacc = clamp(0,0xff,gacc);
						bacc = clamp(0,0xff,bacc);
						
						dest[i] = (desta << 24) + ( ((racc + destr) & 0xff) << 16) +
											   ( ((gacc + destg) & 0xff) <<  8) +
											   ( ((bacc + destb) & 0xff)      );
						
					} else {
						racc = (int) (rulescaling * liferule[ (int)(racc*ruledensity + ruleoffset) % liferule.length]); //these +128's were not da original flayvah
						gacc = (int) (rulescaling * liferule[ (int)(gacc*ruledensity + ruleoffset) % liferule.length]);
						bacc = (int) (rulescaling * liferule[ (int)(bacc*ruledensity + ruleoffset) % liferule.length]);
						int avg = (racc+bacc+gacc)/3;
						if (withalpha) avg = (avg * alf) >> 8;
						
						avg = clamp(0,0xff,avg);
						
						dest[i] = (desta << 24) + ( ((avg + destr) & 0xff) << 16) +
											   ( ((avg + destg) & 0xff) <<  8) +
											   ( ((avg + destb) & 0xff)      );
					}
					
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {}//System.out.print("[" + e.toString() + "]");}
		isProcessing = false;
	}
	
	public void nextFrameWithLifeRule(int[] src, int[] dest, float[] liferule, boolean withalpha, int[] alpha, boolean transfer)
	//boolean inline, normalise
	{
		isProcessing = true;
		try
		{
			int pos, grabber;
			int racc,gacc,bacc,destr,destg,destb, desta;
			float scale = 0;
			for (int l=0; l < liferule.length; l++) scale += liferule[l];
			if (scale==0.0) scale = 1.0f;		
			//if (debug) 
			//System.out.println("Doing NextFrame With Rule");
			
			for (int i = 0; i < src.length; i++)
			{
				grabber = 0;
				racc = 0;
				gacc = 0;
				bacc = 0;
				
				int numinp = inputs[i].length;
				
				//System.out.println("About to accumulate.");
				for (int j = 0; j < numinp; j++)
				{
					grabber = src[inputs[i][j]];
					//weighted connections would be computed here.	
					racc += ((grabber >> 16) & 0xff);// * kernelval; //reb
					gacc += ((grabber >>  8) & 0xff);// * kernelval; //greeb
					bacc += ((grabber      ) & 0xff);// * kernelval; //lube
				}
				
				//scale to liferule ? 
				//racc /= scale; 
				//gacc /= scale; 
				//bacc /= scale;
				//average
				
				racc /= numinp;
				gacc /= numinp;
				bacc /= numinp;
				
				//rbit = applySpacePolicy(rbit,0,liferule.length);
				//racc = (racc >= liferule.length) ? liferule.length - 1 : (racc < 0) ? 0 : racc;
				//gacc = (gacc >= liferule.length) ? liferule.length - 1 : (gacc < 0) ? 0 : gacc;
				//bacc = (bacc >= liferule.length) ? liferule.length - 1 : (bacc < 0) ? 0 : bacc;
				//System.out.println("About to apply rule.");
				//apply rule (then u get a number positive or negative.
				
				int alf,invalf;
				
				if (withalpha)
				{
					alf = alpha[i];
					invalf = 0xff - alf;
				} else {
					alf = 0xff;	
					invalf = 0xff - alf;
				}
				
				int meval = src[i];
				desta = (meval >> 24) & 0xff;
				destr = (((meval >> 16) & 0xff) * invalf) >> 8;
				destg = (((meval >>  8) & 0xff) * invalf) >> 8;
				destb = (((meval      ) & 0xff) * invalf) >> 8;
				
				if (transfer)
				{
					int ruleamnt = (int) (128 * liferule[(racc+gacc+bacc/3) % liferule.length]);
					//gacc += (int) (128 * liferule[gacc ]);
					//bacc += (int) (128 * liferule[bacc % liferule.length]);
					
					//rule as alpha amount? (front/backyard)	
					
					racc += ruleamnt;
					gacc += ruleamnt;
					bacc += ruleamnt;
					racc = ((racc * alf) >> 8);
					gacc = ((gacc * alf) >> 8);
					bacc = ((bacc * alf) >> 8);
					racc = clamp(0,0xff,racc);
					gacc = clamp(0,0xff,gacc);
					bacc = clamp(0,0xff,bacc);
					
					dest[i] = (desta << 24 ) + ( ((racc + destr) & 0xff) << 16) +
										   ( ((gacc + destg) & 0xff) <<  8) +
										   ( ((bacc + destb) & 0xff)      ); 
					                              
				} else { //
				
					racc = (int) (128 * liferule[racc%liferule.length]) + 128; //these +128's were not da original flayvah
					gacc = (int) (128 * liferule[gacc%liferule.length]) + 128;
					bacc = (int) (128 * liferule[bacc%liferule.length]) + 128;
					
					int avg = (racc+bacc+gacc)/3;
					if (withalpha) avg = (avg * alf) >> 8;
					
					avg = clamp(0,0xff,avg);
					
					dest[i] = (desta << 24 ) + ( ((avg + destr) & 0xff) << 16) +
										   ( ((avg + destg) & 0xff) <<  8) +
										   ( ((avg + destb) & 0xff)      );
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {}//System.out.print("[" + e.toString() + "]");}
		isProcessing = false;
	}
	
	public void nextFrameAverageDifferenceRule(int[] src, int[] dest, float[] liferule, float rulemultiplier, boolean withalpha, int[] alpha )
	//boolean inline, normalise
	{
		isProcessing = true;
		try
		{
			int pos, grabber;
			int racc,gacc,bacc,destr,destg,destb, desta;
			float scale = 0;
			for (int l=0; l < liferule.length; l++) scale += liferule[l];
			if (scale==0.0) scale = 1.0f;		
			//if (debug) 
			//System.out.println("Doing NextFrame With Rule");
			
			for (int i = 0; i < src.length; i++)
			{
				grabber = 0;
				racc = 0;
				gacc = 0;
				bacc = 0;
				
				int numinp = inputs[i].length;
				
				//System.out.println("About to accumulate.");
				for (int j = 0; j < numinp; j++)
				{
					grabber = src[inputs[i][j]];
					//weighted connections would be computed here.	
					racc += ((grabber >> 16) & 0xff);// * kernelval; //reb
					gacc += ((grabber >>  8) & 0xff);// * kernelval; //greeb
					bacc += ((grabber      ) & 0xff);// * kernelval; //lube
				}
				
				//scale to liferule ? 
				//racc /= scale; 
				//gacc /= scale; 
				//bacc /= scale;
				//average
				
				racc /= numinp;
				gacc /= numinp;
				bacc /= numinp;
				
				//rbit = applySpacePolicy(rbit,0,liferule.length);
				//racc = (racc >= liferule.length) ? liferule.length - 1 : (racc < 0) ? 0 : racc;
				//gacc = (gacc >= liferule.length) ? liferule.length - 1 : (gacc < 0) ? 0 : gacc;
				//bacc = (bacc >= liferule.length) ? liferule.length - 1 : (bacc < 0) ? 0 : bacc;
				//System.out.println("About to apply rule.");
				//apply rule (then u get a number positive or negative.
				
				int alf,invalf;
				
				if (withalpha)
				{
					alf = alpha[i];
					invalf = 0xff - alf;
				} else {
					alf = 0xff;	
					invalf = 0xff - alf;
				}
				
				int meval = src[i];
				desta = (meval >> 24) & 0xff;
				
				destr = ((meval >> 16) & 0xff);
				destg = ((meval >>  8) & 0xff);
				destb = ((meval      ) & 0xff);
				
				//lookup using lumi (RGB average, actually) of the neighborhood:
				float rulelookup = rulemultiplier * liferule[ (int) ( (racc+gacc+bacc/3.0f)/256.0f * liferule.length ) % liferule.length ];
				int rulelookupi = (int) ( rulelookup * 2 + 256 );
				
				//System.out.println("looked up int: " + rulelookupi );
				//use the rule lookup (which should be signed..) to do a blend between 
				//the average value and the absolute difference between the average and the current pixel value
				//  the dest is not premulted, we 
				
				racc = Math.abs( destr + (rulelookupi * racc)>>8);
				gacc = Math.abs( destg + (rulelookupi * gacc)>>8);
				bacc = Math.abs( destb + (rulelookupi * bacc)>>8);
				
				racc = clamp(0,0xff,racc);
				gacc = clamp(0,0xff,gacc);
				bacc = clamp(0,0xff,bacc);
				
				racc = ((racc * alf) >> 8);
				gacc = ((gacc * alf) >> 8);
				bacc = ((bacc * alf) >> 8);
				
				racc = clamp(0,0xff,racc);
				gacc = clamp(0,0xff,gacc);
				bacc = clamp(0,0xff,bacc);
				
				destr = destr * invalf;
				destg = destg * invalf;
				destb= destb * invalf;
				
				dest[i] = (desta << 24 ) + ( ((racc + destr) & 0xff) << 16) +
									   ( ((gacc + destg) & 0xff) <<  8) +
									   ( ((bacc + destb) & 0xff)      ); 
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {}//System.out.print("[" + e.toString() + "]");}
		isProcessing = false;
	}
	
	public void nextFrameWithRGBLifeRule(int[] src, int[] dest, float[] lifefloatrule, int[] lifecolrule, int ruleOffset, int ruleMultiplier, boolean withalpha, int[] alpha, boolean transfer)
	//boolean inline, normalise
	{
		isProcessing = true;
		try
		{
			int pos, grabber;
			int racc,gacc,bacc,destr,destg,destb, desta;
			
			//get normalization property for liferule
			/*
			float scaleR = 0;
			float scaleG = 0;
			float scaleB = 0;
			
			for (int l=0; l < liferule.length; l++) 
			{
				int lx = liferule[l];
				scaleR += (float) ( (lx >> 16) & 0xff) / 256.0f;
				scaleG += (float) ( (lx >> 8) & 0xff) / 256.0f;
				scaleB += (float) ( (lx      ) & 0xff) / 256.0f;
			}
			if (scaleR == 0.0) scaleR = 1.0f;		
			if (scaleG == 0.0) scaleG = 1.0f;		
			if (scaleB == 0.0) scaleB = 1.0f;		
			*/
			//if (debug) 
			//System.out.println("Doing NextFrame With Rule");
			
			for (int i = 0; i < src.length; i++)
			{
				grabber = 0;
				racc = 0;
				gacc = 0;
				bacc = 0;
				
				int numinp = inputs[i].length;
				
				//System.out.println("About to accumulate.");
				for (int j = 0; j < numinp; j++)
				{
					grabber = src[inputs[i][j]];
					//weighted connections would be computed here.	
					racc += ((grabber >> 16) & 0xff);// * kernelval; //reb
					gacc += ((grabber >>  8) & 0xff);// * kernelval; //greeb
					bacc += ((grabber      ) & 0xff);// * kernelval; //lube
				}
				
				//scale to liferule ? 
				//racc /= scale; 
				//gacc /= scale; 
				//bacc /= scale;
				//average
				
				racc /= numinp;
				gacc /= numinp;
				bacc /= numinp;
				
				//rbit = applySpacePolicy(rbit,0,liferule.length);
				//racc = (racc >= liferule.length) ? liferule.length - 1 : (racc < 0) ? 0 : racc;
				//gacc = (gacc >= liferule.length) ? liferule.length - 1 : (gacc < 0) ? 0 : gacc;
				//bacc = (bacc >= liferule.length) ? liferule.length - 1 : (bacc < 0) ? 0 : bacc;
				//System.out.println("About to apply rule.");
				//apply rule (then u get a number positive or negative.
				
				int alf,invalf;
				
				if (withalpha)
				{
					alf = alpha[i];
					invalf = 0xff - alf;
				} else {
					alf = 0xff;	
					invalf = 0xff - alf;
				}
				
				int meval = src[i];
				desta = (meval >> 24) & 0xff;
				destr = (((meval >> 16) & 0xff) * invalf) >> 8;
				destg = (((meval >>  8) & 0xff) * invalf) >> 8;
				destb = (((meval      ) & 0xff) * invalf) >> 8;
				
				if (transfer)
				{
					//use lumi (ultimately might use a 'currentDefault' extracted channel from miniPixelTools
					//
					int rulecollookup = ( racc+gacc+bacc / 3 ) % lifecolrule.length;
					int rulefloatlookup = ( racc+gacc+bacc / 3 ) % lifefloatrule.length;
					
					int ruleRGB =  lifecolrule[ rulecollookup ];
					float ruleFloat =  lifefloatrule[ rulefloatlookup ];
					//gacc += (int) (128 * lifecolrule[gacc ]);
					//bacc += (int) (128 * lifecolrule[bacc % lifecolrule.length]);
					
					//rule as alpha amount? (front/backyard)	
					
					racc += (int) ( (( ruleRGB >> 16) & 0xff ) * ruleMultiplier * ruleFloat) + ruleOffset;
					gacc += (int) ( (( ruleRGB >>  8) & 0xff ) * ruleMultiplier * ruleFloat) + ruleOffset;
					bacc +=  (int) ( (( ruleRGB         ) & 0xff ) * ruleMultiplier * ruleFloat) + ruleOffset;
					racc = ((racc * alf) >> 8);
					gacc = ((gacc * alf) >> 8);
					bacc = ((bacc * alf) >> 8);
					racc = clamp(0,0xff,racc);
					gacc = clamp(0,0xff,gacc);
					bacc = clamp(0,0xff,bacc);
					
					dest[i] = (desta << 24 ) + ( ((racc + destr) & 0xff) << 16) +
										   ( ((gacc + destg) & 0xff) <<  8) +
										   ( ((bacc + destb) & 0xff)      ); 
					                              
				} else { //
					//let each channel look up its own rule..
					racc = ( lifecolrule[ racc % lifecolrule.length ] >> 16) & 0xff;
					gacc = ( lifecolrule[ gacc % lifecolrule.length] >>   8)  & 0xff;
					bacc = ( lifecolrule[ bacc % lifecolrule.length] )  & 0xff;
					
					int rulefloatlookup = ( racc+gacc+bacc / 3 ) % lifefloatrule.length;
					float ruleFloat =  lifefloatrule[ rulefloatlookup ];
					
					racc = (int) ( racc * ruleMultiplier * ruleFloat );
					gacc = (int) ( gacc * ruleMultiplier * ruleFloat );
					bacc = (int) ( bacc * ruleMultiplier * ruleFloat );
					
					racc += ruleOffset;
					gacc += ruleOffset;
					bacc += ruleOffset;
					
					//int avg = (racc+bacc+gacc)/3;
					if (withalpha)
					{
						racc = (racc * alf) >> 8;
						gacc = (gacc * alf) >> 8;
						bacc = (bacc * alf) >> 8;
					}
					
					racc = clamp(0,0xff,racc);
					gacc = clamp(0,0xff,gacc);
					bacc = clamp(0,0xff,bacc);
					
					dest[i] = (desta << 24 ) + ( ((racc + destr) & 0xff) << 16) +
										   ( ((gacc + destg) & 0xff) <<  8) +
										   ( ((bacc + destb) & 0xff)      );
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {}//System.out.print("[" + e.toString() + "]");}
		isProcessing = false;
	}
	//public void nextFrameWithLifeFunction(LifeFunction lifefunc); //with conways for example
	//public void nextFrameWithTransferFunction(TransferFunc transfunc); //for RD, ripples and excitable media	
	//public void nextFrameWithOrbitTransfer(TransferFunc transfunc); //eg for classical fractals
	//public void nextFrameWithTemporalAntialias(TransferFunc transfunc); //doesnt do an entire buffer at once, instead random points are chosen and alphablended.
	//public void nextFrameMaxVal(), nextFrameMinVal(), nextFrameMaxMedMin()
}
