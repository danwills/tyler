//import Particle;
import java.util.*;
import java.awt.*;
//import Topology;
//import StringSorter;

/*
 * Particle System object, manages control for and updates an array of particles in time.
 * 
 * dan wills <danw@rsp.com.au>
 * 
 * 
 * fix to use a single particle as the control/spawning template.
 */
 

public class ParticleSystem
{
	public enum particleSizeSource
	{
		Channel,
		Speed,
		Drag,
		Age,
		AgeTriangle,
		AgeSine,
		ChannelAgeTriangle,
		ChannelAgeSine,
		SpeedChannel,
		AgeChannel;
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( particleSizeSource p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}
	
	//public final static int CHANNEL_SIZE  = 0;
	//public final static int SPEED_SIZE    = 1;
	//public final static int DRAG_SIZE     = 2;
	//public final static int AGE_SIZE    = 3;
	//public final static int BRANCH_DEPTH = 4;
	//public final static int NUM_SIZEMODES = 4;
	
	particleSizeSource sizesource = particleSizeSource.Channel;
	
	//smoothstep +
	//age -> size normalization + -> lifespan
	//age falloff power
	//speed -> size power control
	//
	//new size modes..
	//age*default
	//speed*default
	//proximity to point (eg mousepos)
	//f(x,y)
	//public final static String[] SIZESOURCES = {"Default","Speed","Drag","Age"};

	//particle respawn criteria
	//RESPAWN_SLOW
	//RESPAWN_FAST
	//RESPAWN_DEFAULTOVER
	//RESPAWN_DEFAULTUNDER
	//RESPAWN_ONDEATH
	//RESPAWN_COLLIDE
	//RESPAWN_COLOR
	//RESPAWN_AGE
	
	Particle.particleColorMode particlecolormode = Particle.particleColorMode.AlphaBlend;
	Particle.particleDrawMode particledrawmode = Particle.particleDrawMode.TimePolygon;
	//int particlespawnmode = Particle.particleSpawnSPAWN_NEVER;
	public int maxspawndepth = 2;
	int drawIndexCounter = 0;
	Topology particlewarp;
	float pixelSpaceScale = 0.2f;
	int[] pixeluniverse;
	int[] defaultpixels;
	int pixmapsize = 32;
	Tool1D toolsamplesource;
	//this topology can either be used to warp the particles path or to draw them as a pixel warp
	//particlewarp.setWeighted(true);
	//particlewarp.setSize(space or particlesize);
	//get the default 'softbob' drawn into the weights structure if possible
	//use topology.origin().
	
	//(L%10)d+f|
	//
	
	//implement an enum of presets
	
	// an L language of zero crossings
	// to allow conditional L-atoms
	// as in:
	// X10[ means at crossng 10, branch 
	// X%5+ means at every 5 crossngs , rotate
	//
	
	String lstring = "d+f|";
	boolean spawnparticles = false;
	boolean channeldrag = false;
	float channeldraglevel = 1.0f;
	boolean channelspin = true;
	double channelspinlevel = -0.001f;
	boolean speeddeath = true;
	float baseLFOfreq = 0.03f;
	float speedmax = 256.0f;
	float speedmin = 0.0f;
	float particlestability = 0.5f; //a control channel for the particles
	boolean agedeath = true;
	private long agetobucketkick = 150;
	private int lthreshold = 90;
	boolean randomdeath = false;
	private float chanceofdeath = 0.00001f;
	boolean colordeath;
	int thecolorofdeath = 0x0000;
	boolean defaultdeath;
	int dangerousdefault = 0;
	//collision detect with attract, repel, bounce, destroy, spawn, etc
	
	
	// a public cluster of particles.
	public Particle cluster[];
	public Random randex;
	int basealpha = 0xff;
	int channelalpha = 0x00;
	int basecolor = 0xffffffff;
	float sizepower = 1.0f;
	float sizeslope = 0.25f;
	float basesize = 9.16616f;
	float basestrokewidth = 0.45f;
	float maxsize = 4000.0f; 
	int dimension; // the dimensionality of the space the particles are in
	int dimensionclip[]; // the lengths of the dimensions of the space the particles are sampling in
	int drawdimensionclip[]; // the lengths of the dimensions of the space the particles are drawn in
	long framenumber;	// the number of frames this ParticleSystem has existed for
	boolean interparticle = false; //whether to compute interparticle effects
	double interparticleworkbuffer;
	float[] offset;
	boolean interpolate = true;
	/**
	 * Constructs a particle system with no active particles.
	 */
	
	ParticleSystem(int size, int w, int h, int draww, int drawh, int[] cols, Tool1D tool)
	{	
		toolsamplesource = tool;
		dimension = 2;
		dimensionclip = new int[dimension];
		drawdimensionclip = new int[dimension];
		offset = new float[dimension];
		offset[0] = 0;
		offset[1] = 0;
		cluster = new Particle[size];
		//interparticleworkbuffer = new double[size*size];
		dimensionclip[0] = w;
		dimensionclip[1] = h;
		
		drawdimensionclip[0] = draww;
		drawdimensionclip[1] = drawh;
		
		defaultpixels = new int[pixmapsize*pixmapsize];
		
		MiniPixelTools.renderLinearFalloffMap(defaultpixels,pixmapsize,pixmapsize,255.0f);
		
		for (int i = 0; i < pixmapsize*pixmapsize; i++)
		{
			int defjam = defaultpixels[i];
			
			//clamp defjam.. it should already be but anyhoo...
			defjam = (defjam > 0xff) ? 0xff : (defjam < 0) ? 0 : defjam;
			
			//defjam = (defjam < 0) ? 0 : (defjam > 0xfe) ? 0xfe : defjam;
			
			defaultpixels[i] = (defjam << 24) | 0x00ffffff; //render in white
			//do the conversion manually for now...
		}
		
		pixeluniverse = cols;
		randex = new Random();
		
		
		for (int i=0; i < cluster.length; i++)
		{	
			/** 
			 * Initialise the particle cluster, initially all particles are invisible, 
			 * at location (0,0) and moving at speed (0,0)
			 * with no acceleration, calls to ActivateParticle activate individual particles 
			 */
			cluster[i] = new Particle(dimensionclip[0], dimensionclip[1], drawdimensionclip[0], drawdimensionclip[1], cols, particledrawmode, particlecolormode,i);
			cluster[i].setPixelMap(defaultpixels,pixmapsize,pixmapsize);
			cluster[i].setDrawIndex(drawIndexCounter++);
			
		}
		framenumber = 0L;				
	}
	
	public boolean update(int i, long frame)
	{
		if (cluster[i].isActive()) 
		{	
			float speedlength = Particle.getLength(cluster[i].getSpeed());
			int gotcolor = cluster[i].getIntColor();
			float defezzerf = MiniPixelTools.getDefaultF(gotcolor);
			int defezzer = (int) defezzerf;
			
			switch (sizesource)
			{
				case Channel : { cluster[i].setSize(( (defezzerf/255.0f) * basesize)); break;}
				//adjust because speedsize is normally too large
				case Speed : { cluster[i].setSize(speedlength * (basesize * sizeslope)); break;}
				case Drag : { cluster[i].setSize( ((cluster[i].getDrag())) * basesize ); break; }
				case Age :  
				{ 
					float adjage = basesize - (cluster[i].getAge() * sizeslope);
					if (adjage < 0) adjage = 0;
					cluster[i].setSize(adjage);
					break; 
				}
				case AgeTriangle :  
				{ 
					//System.out.println("age: " + cluster[i].getAge() + " lifespan: " + agetobucketkick + " basesize: " + basesize);
					float adjage = (float) cluster[i].getAge() / (float)agetobucketkick;
					if (adjage < 0.0f) adjage = 0.0f;
					if (adjage > 1.0f) adjage = 1.0f;
					
					if (adjage > 0.5) adjage = 1.0f - adjage;
					cluster[i].setSize(  adjage * basesize );
					break; 
				}
				
				case AgeSine :  
				{ 
					float adjage = (float) cluster[i].getAge() / (float)agetobucketkick;
					if (adjage < 0.0f) adjage = 0.0f;
					if (adjage > 1.0f) adjage = 1.0f;
					
					adjage *= Math.PI;
					
					cluster[i].setSize(  (float) Math.sin( adjage ) * basesize );
					break; 
				}
				
				case ChannelAgeTriangle:
				{
					float adjage = (float) cluster[i].getAge() / (float)agetobucketkick;
					if (adjage < 0.0f) adjage = 0.0f;
					if (adjage > 1.0f) adjage = 1.0f;
					
					if (adjage > 0.5) adjage = 1.0f - adjage;
					cluster[i].setSize(  adjage * basesize );
					break; 
				}
				
				case ChannelAgeSine:
				{
					float adjage = (float) cluster[i].getAge() / (float)agetobucketkick;
					if (adjage < 0.0f) adjage = 0.0f;
					if (adjage > 1.0f) adjage = 1.0f;
					
					adjage *= Math.PI;
					
					cluster[i].setSize(  (float) Math.sin( adjage ) * basesize * ( defezzerf / 255.0f ) );
					break; 
				}
				
				case SpeedChannel:
				{
					float starFirst = speedlength * (basesize * sizeslope) * ( defezzerf / 255.0f );
					cluster[i].setSize( starFirst ); 	
					break;
				}
				
				case AgeChannel:
				{
					float adjage = basesize - (cluster[i].getAge() * sizeslope);
					if (adjage < 0) adjage = 0;
					cluster[i].setSize( adjage *  ( defezzerf / 255.0f ) );
					break;
				}
			}
			
			if ((channeldrag) && (channeldraglevel != 0)) 
			{
				float dragon = ( ( ( defezzerf / 255.0f ) ) * channeldraglevel);
				cluster[i].setDrag((1.0f - channeldraglevel) * cluster[i].getDrag() + dragon);
				//cluster[i].setDrag(cluster[i].getDrag() + dragon);
			}
			if (channelspin)
			{
				double chspin = (defezzerf / 255.0) * Math.PI * 2.0;
				cluster[i].setRotation((1.0 - channelspinlevel/100)*cluster[i].getRotation() + channelspinlevel/100*chspin);
			}
			
			cluster[i].setStrokeWidth(basestrokewidth);
			cluster[i].setInterpolation(interpolate);
			cluster[i].setOffset(offset[0],offset[1]);
			cluster[i].update(this,frame);
			
			
			if ((speeddeath) & ((speedlength < speedmin) || (speedlength > speedmax))) {cluster[i].kill();}
			if ((agedeath) & (cluster[i].getAge() >= agetobucketkick)) {cluster[i].kill();}
			if ((randomdeath) & (randex.nextFloat() < chanceofdeath)) {cluster[i].kill();}
			if ((colordeath) & (gotcolor == thecolorofdeath)) {cluster[i].kill();}
			if ((defaultdeath) & (defezzer == dangerousdefault)) {cluster[i].kill();}
			if (cluster[i].getSpawnDepth() > maxspawndepth) {cluster[i].kill();}
			
			if (cluster[i].isActive()) return true; 
			
			else return false;
		} else return false;
	}
		
	public void update(long frame)
	{
		for (int i=0; i < cluster.length; i++)
		{
			if (cluster[i].isActive()) 
			{	
				float speedlength = Particle.getLength(cluster[i].getSpeed());
				int gotcolor = cluster[i].getIntColor();
				float defezzerf = MiniPixelTools.getDefaultF(gotcolor);
				int defezzer = (int) defezzerf;
				
				switch (sizesource)
				{
					case Channel : { cluster[i].setSize(( (defezzerf/255.0f) * basesize)); break;}
					//adjust because speedsize is normally too large
					case Speed : { cluster[i].setSize(speedlength * (basesize * sizeslope)); break;}
					case Drag : { cluster[i].setSize( ((cluster[i].getDrag())) * basesize ); break; }
					case Age :  
					{ 
						float adjage = basesize - (cluster[i].getAge() * sizeslope);
						if (adjage < 0) adjage = 0;
						cluster[i].setSize(adjage);
						break; 
					}
					case AgeTriangle :  
					{ 
						float adjage = (float) cluster[i].getAge() / (float)agetobucketkick;
						if (adjage < 0.0f) adjage = 0.0f;
						if (adjage > 1.0f) adjage = 1.0f;
						
						if (adjage > 0.5) adjage = 1.0f - adjage;
						cluster[i].setSize(  adjage * basesize );
						break; 
					}
					
					case AgeSine :  
					{ 
						float adjage = (float) cluster[i].getAge() / (float)agetobucketkick;
						if (adjage < 0.0f) adjage = 0.0f;
						if (adjage > 1.0f) adjage = 1.0f;
						
						adjage *= Math.PI;
						
						cluster[i].setSize(  (float) Math.sin( adjage ) * basesize );
						break; 
					}
					
					case ChannelAgeTriangle:
					{
						float adjage = (float) cluster[i].getAge() / (float)agetobucketkick;
						if (adjage < 0.0f) adjage = 0.0f;
						if (adjage > 1.0f) adjage = 1.0f;
						
						if (adjage > 0.5) adjage = 1.0f - adjage;
						cluster[i].setSize(  adjage * basesize );
						break; 
					}
					
					case ChannelAgeSine:
					{
						float adjage = (float) cluster[i].getAge() / (float)agetobucketkick;
						if (adjage < 0.0f) adjage = 0.0f;
						if (adjage > 1.0f) adjage = 1.0f;
						
						adjage *= Math.PI;
						
						cluster[i].setSize(  (float) Math.sin( adjage ) * basesize * ( defezzerf / 255.0f ) );
						break; 
					}
					
					case SpeedChannel:
					{
						float starFirst = speedlength * (basesize * sizeslope) * ( defezzerf / 255.0f );
						cluster[i].setSize( starFirst ); 	
						break;
					}
					
					case AgeChannel:
					{
						float adjage = basesize - (cluster[i].getAge() * sizeslope);
						if (adjage < 0) adjage = 0;
						cluster[i].setSize( adjage *  ( defezzerf / 255.0f ) );
						break;
					}
				}
				
				if ((channeldrag) && (channeldraglevel != 0)) 
				{
					float dragon = ( ( ( defezzerf / 255.0f ) ) * channeldraglevel);
					cluster[i].setDrag((1.0f - channeldraglevel) * cluster[i].getDrag() + dragon);
					//cluster[i].setDrag(cluster[i].getDrag() + dragon);
				}
				if (channelspin)
				{
					double chspin = (defezzerf / 255.0) * Math.PI * 2.0;
					cluster[i].setRotation((1.0 - channelspinlevel/100)*cluster[i].getRotation() + channelspinlevel/100*chspin);
				}
				
				cluster[i].setStrokeWidth(basestrokewidth);
				cluster[i].setInterpolation(interpolate);
				cluster[i].setOffset(offset[0],offset[1]);
				cluster[i].update(this,frame);
				
				if ((speeddeath) & ((speedlength < speedmin) || (speedlength > speedmax))) {cluster[i].kill();}
				if ((agedeath) & (cluster[i].getAge() >= agetobucketkick)) {cluster[i].kill();}
				if ((randomdeath) & (randex.nextFloat() < chanceofdeath)) {cluster[i].kill();}
				if ((colordeath) & (gotcolor == thecolorofdeath)) {cluster[i].kill();}
				if ((defaultdeath) & (defezzer == dangerousdefault)) {cluster[i].kill();}
				if (cluster[i].getSpawnDepth() > maxspawndepth) {cluster[i].kill();}
			}
		}
		framenumber++;
	}
	
	//public void attractTo(Point p,float strength)
	
	public boolean isPixelDrawable()
	{ return (particledrawmode == Particle.particleDrawMode.AlphaPIXELS); }
	
	public void setInterparticle(boolean towhat)
	{ interparticle = towhat; }
	
	public Particle getParticle(int index) 
	{
		int indexx = Math.max(0,Math.min(cluster.length,index));
		return cluster[indexx];
	}
	
	public float getParticleStability() 
	{ return particlestability; }

	public void setMaxSpawnDepth(int to)
	{ maxspawndepth = to; }
	
	public void setInterpolation(boolean towhat)
	{ interpolate = towhat; }

	public void setLString(String s)
	{
		if ((s != null) && (s != ""))
		lstring = s;
	}
	
	public void setOffset(float x, float y)
	{ offset[0] = x;offset[1] = y; }
		
	public void setParticleStability(float towhat)
	{
		//if (towhat >= 0.0f) 
		particlestability = towhat; 
		//else particlestability = 0.0f;
	}
	
	public void addParticles(Particle[] whom)
	{
		//dynamic or static, ref or copy...
	}
	
	public void setChannelDragLevel(float towhat)
	{
		if (Math.abs(towhat) != 0.0f)
		{
			channeldrag = true;
			channeldraglevel = towhat;
		} else {
			channeldrag = false;
		}
	}
	
	public void setChannelSpinLevel(float towhat)
	{
		if (Math.abs(towhat) != 0.0f)
		{
			channelspin = true;
			channelspinlevel = (double)towhat;
		} else {
			channelspin = false;
		}
	}
	
	public synchronized void setSizePower(int to)
	{
		sizepower = to;
	}
	
	public synchronized void setSizeSlope(float to)
	{
		sizeslope = to;
	}
	
	public synchronized void setSizeSource(int to)
	{ 
		sizesource = particleSizeSource.values()[to]; 
	}
	
	public particleSizeSource getSizeSource()
	{
		return sizesource;
	}
	
	public void setMinSpeed(float minspeed)
	{
		if (minspeed > 0)
		{
			speeddeath = true;
			speedmin = minspeed;
		} else {
			speeddeath = false;
		}
	}

	public void setMaxSpeed(float maxspeed)
	{
		if (maxspeed > 0)
		{
			speeddeath = true;
			speedmax = maxspeed;
		} else {
			speeddeath = false;
		}
	}
	
	public void setMaxSpeedInt(int maxspeed)
	{
		if (maxspeed > 0)
		{
			speeddeath = true;
			speedmax = (float)maxspeed;
		} else {
			speeddeath = false;
		}
	}

	public void setDeathBySpeed(float minspeed, float maxspeed)
	{
		speeddeath = true;
		speedmax = maxspeed;
		speedmin = minspeed;
	}
	
	public void setDeathByAge(int cyclifetime)
	{
		if (cyclifetime > 0) 
		{
			agedeath = true;
			agetobucketkick = cyclifetime;
		} else {
			agedeath = false;
		}
	}
	
	public void setLThreshold( int thresha )
	{ lthreshold = thresha; }
	
	public void setChanceOfDeath(float likely)
	{
		if (likely >= 0) 
		{
			randomdeath = true;
			chanceofdeath = likely;
		} else {
			randomdeath = false;
		}
	}
	
	public void setColorOfDeath(int pallor)
	{
		colordeath = true;
		thecolorofdeath = pallor;
	}
	
	public void setValueOfDeath(int to)
	{
		defaultdeath = true;
		dangerousdefault = to;
	}
	
	public void resize(int w, int h)
	{
		int sc[] = {w,h};
		
		for (int i = 0; i < cluster.length; i++)
		{
			cluster[i].setSpaceClip(sc);
		}
	}
	
	public void setBaseColor(int to)
	{ basecolor = to; }
	
	public void setToolSampleSource( Tool1D src )
	{ toolsamplesource = src; }
	
	public void setBaseAlpha(int to)
	{ basealpha = to; }
	
	public void setPixelSpaceScale( float towhat )
	{  pixelSpaceScale = towhat; }
	
	public float getPixelSpaceScale()
	{ return pixelSpaceScale; }
	
	public int getBaseAlpha()
	{ return basealpha; }
	
	public void setChannelAlpha(int to)
	{ channelalpha = to; }
	
	public void setBaseSize(float to)
	{ basesize = to; }
	
	public void setBaseLFOFreq(float to)
	{ baseLFOfreq = to; }

	public float getBaseLFOFreq()
	{ return baseLFOfreq; }

	public void setBaseStrokeWidth(float to)
	{ basestrokewidth = to; }
	
	public void setBaseSizeInt(int to)
	{ basesize = (float)to; }
	
	public float getBaseSize()
	{ return basesize; }
	
	public int getNumParticles()
	{ return cluster.length; }
	
	public void setTotalNumParticles(int to)
	{
		if (to > 0)
		{
			Particle newcluster[] = new Particle[to];
			for (int p = 0; p < to ; p++)
			{
				if (p < cluster.length)
				{
					newcluster[p] = cluster[p];
					
				} else {
					
					newcluster[p] = new Particle(dimensionclip[0], dimensionclip[1], drawdimensionclip[0], drawdimensionclip[1], pixeluniverse, particledrawmode, particlecolormode,p);
				}
			}
			cluster = newcluster;
			
		} else {
			//print something?
		}
	}
	
	public int getNumActiveParticles()
	{
		int numactive = 0;
		for (int k = 0; k < cluster.length; k++)
		{
			numactive = (cluster[k].isActive()) ? numactive+1 : numactive;
		}
		return numactive;
	}
	
	public void getParticlePoints(float xout[], float yout[])
	{
		for (int i = 0; i < Math.min(xout.length,yout.length); i += 1)
		{
			xout[i] = cluster[i].getXLocation();
			yout[i] = cluster[i].getYLocation();
		}
	}
	
	
	public void killAllParticles()
	{
		for (int i = 0; i < cluster.length; i++)
		{cluster[i].kill();}
	}	
	
	//activate the next free particle
	public Particle activateParticle(float xloc, float yloc, 
	                             float xspd, float yspd,
	                             float drag, double rot, double rotprob, float stabil, int spawndepth, float samplespacescale, int drawID)
	{
		Particle nxfp = nextFreeParticle(); 
		if (nxfp != null)
		{
			nxfp.setTilePosition( 0 );
			nxfp.setActive( true );
			nxfp.setLString( lstring );
			nxfp.setCurrentAtom( 0 );
			nxfp.setLocation( xloc, yloc );
			nxfp.setSampleSpaceScale( samplespacescale );
			nxfp.setSpeed( xspd, yspd );
			nxfp.setMass( 1.0f );
			nxfp.setDrag( drag );
			nxfp.setRotation( rot );
			nxfp.setRotationProbability( rotprob );
			nxfp.setLFOFreq( baseLFOfreq );
			nxfp.setDrawMode( particledrawmode );
			nxfp.setSpawnDepth( spawndepth );
			nxfp.setStability( particlestability );
			nxfp.setAge( 0 );
			nxfp.setLifespan( agetobucketkick );
			nxfp.setLThreshold( lthreshold );
			nxfp.resetClippingAndThresholdMeasure( );
			nxfp.setSampleSource( toolsamplesource );
			//temporoarily use 'default' color mode to init the particle color
			if (spawndepth == 0) nxfp.setColorMode( Particle.particleColorMode.Default ); else nxfp.setColorMode( particlecolormode );
			nxfp.updateColor(); //(re)initialise color at (re)start of particle life
			
			nxfp.setColorMode( particlecolormode );
			nxfp.setDrawID( drawID );
			nxfp.setAlpha( basealpha );
			nxfp.setChannelAlpha(channelalpha);
			nxfp.setDrawIndex(drawIndexCounter++);
			nxfp.clearNeighbors(this);
			//nxfp.setColorMode(particlecolormode);
		} //else do nothing, no free particles available. 
		return nxfp;
	}
	
	private int nextFreeParticleNum()
	{
		for (int i=0; i < cluster.length; i++)
		{
			if (!cluster[i].isActive()) {return i;}
			//another kind of search might be better,
			//eg start from lastfound particle or sumfin.
		}
		return -1; //this will cause an ArrayOutOfBoundsException
	}
	
	private Particle nextFreeParticle()
	
	{
		for (int i=0; i < cluster.length; i++)
		{
			if (!cluster[i].isActive()) {return cluster[i];}
		}
		return null; //this will cause an ArrayOutOfBoundsException
	}
		
	public Particle.particleDrawMode getParticleDrawMode()
	{ return particledrawmode; }
	
	public void setParticleDrawMode(int towhat)
	{ particledrawmode = Particle.particleDrawMode.values()[towhat]; }
	
	/*public void setParticleSpawnMode(int towhat)
	{
		towhat %= Particle.NUM_SPAWNMODES;
		if (towhat < 0) towhat += Particle.NUM_SPAWNMODES;
		particlespawnmode = towhat;
	}*/
	
	public Particle.particleColorMode getParticleColorMode()
	{return particlecolormode;}
	
	public void setParticleColorMode(int towhat)
	{
		particlecolormode = Particle.particleColorMode.values()[towhat];
	}
	
	public void drawNeighbors(Graphics2D onwhat)
	{
		for (int i=0; i < cluster.length; i++)
		{
			if (cluster[i].isActive()) 
			{
				cluster[i].drawNeighbors(onwhat, this);
			}
		}
		
		//reset the is-drawn tracking so that it works again next frame..
		for (int i=0; i < cluster.length; i++)
		{
			if (cluster[i].isActive()) 
			{
				cluster[i].resetDrawNeighbors(false);
			}
		}
		
	}
	
	public void draw(Graphics2D onwhat, Tiler.ParticlePaintMode drawmode)
	{
		//quicksort the array by some criteria... then draw... ?		
		//int drawMap[] = new int[cluster.length];
		
		//String drawIndexes[] = new String[cluster.length];
		//System.out.println("UNSORTED:");
		
		//for (int i=0; i < cluster.length; i++)
		//{
		//	drawIndexes[i] = (cluster[i].drawIndex + "|" + i);
		//	System.out.println(drawIndexes[i]);
		//}
		
		//System.out.println("SORTED:");
		
		//StringSorter.sort(drawIndexes);
		//for (int i=0; i < cluster.length; i++) System.out.println(drawIndexes[i]);
		
		//nother paintmode that always draws two steps of particle motion, this step and the last step.. 
		//this will help to fill in the gaps when topology feedback is used..	
		
		switch ( drawmode )
		{
			case particle :
			{
				java.util.Arrays.sort( cluster );
				
				for (int i=0; i < cluster.length; i++)
				{
					if (cluster[i].isActive()) 
					{
						cluster[i].draw(onwhat);
					}
				}		
				break;
			}
			case particleTrace :
			{
				//all particles reach the end of their lifespan during one frame..
				for (int i=0; i < cluster.length; i++)
				{
					int bailout = 0;
					while ((cluster[i].isActive()) && (bailout < 1000)) //hard limited lifespan for trace mode atm
					{
						cluster[i].draw(onwhat);
						this.update(i,i);
						bailout++;
					}
				}
				break;
			}
			case neighbors :
			{
				drawNeighbors(onwhat);
				break;
			}
		}
		
	}
	
	public void drawPixels(int[] pixbuffer)
	{
		pixeluniverse = pixbuffer;
		for (int i=0; i < cluster.length; i++)
		{
			if (cluster[i].isActive()) 
			{
				cluster[i].setPixelWorld(pixeluniverse);
				cluster[i].setPixelMap(defaultpixels,pixmapsize,pixmapsize);
				cluster[i].draw(pixbuffer);
			}
		}
	}
}
