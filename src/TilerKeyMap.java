import java.awt.*;
import java.awt.event.*;
import java.util.*;

class TilerKeyMap
{
	Hashtable<Long, KeyAction> keymap;
	Hashtable<String, KeyAction> toolmap;
	Tiler T;
	ParticleSystem P;
	Tool1D L;
	Object M = MiniPixelTools.class; //these methods are static in minipixeltools so.. there's no need for a runtime instance at the mo'
	Object E = EditWidget.class;
	
	//try { E = (Object) Class.forName("EditWidget"); }
	//catch (ClassNotFoundException cnx) {output("the class loader had a problem recognising the EditWidget class name.. how did that happen?"); break;}
	
	//Object E = (Object) Class.forName("EditWidget");
	TilerKeyMap K;
	
	public TilerKeyMap( Tiler tilerTarget, ParticleSystem particleTarget, Tool1D toolTarget )
	{
		//grab the objects that the actions are to be performed on, just Tiler and particleSystem to begin with
		T = tilerTarget;
		P = particleTarget;
		L = toolTarget;
		K = this;
		
		
		//declare key values
		
		long a = (((long)KeyEvent.VK_A ) << 32);
		long b = (((long)KeyEvent.VK_B ) << 32);
		long c = (((long)KeyEvent.VK_C ) << 32);
		long d = (((long)KeyEvent.VK_D ) << 32);
		long e = (((long)KeyEvent.VK_E ) << 32);
		long f = (((long)KeyEvent.VK_F ) << 32);
		long g = (((long)KeyEvent.VK_G ) << 32);
		long h = (((long)KeyEvent.VK_H ) << 32);
		long i = (((long)KeyEvent.VK_I ) << 32);
		long j = (((long)KeyEvent.VK_J ) << 32);
		long k = (((long)KeyEvent.VK_K ) << 32);
		long l = (((long)KeyEvent.VK_L ) << 32);
		long m = (((long)KeyEvent.VK_M ) << 32);
		long n = (((long)KeyEvent.VK_N ) << 32);
		long o = (((long)KeyEvent.VK_O ) << 32);
		long p = (((long)KeyEvent.VK_P ) << 32);
		long q = (((long)KeyEvent.VK_Q ) << 32);
		long r = (((long)KeyEvent.VK_R ) << 32);
		long s = (((long)KeyEvent.VK_S ) << 32);
		long t = (((long)KeyEvent.VK_T ) << 32);
		long u = (((long)KeyEvent.VK_U ) << 32);
		long v = (((long)KeyEvent.VK_V ) << 32);
		long w = (((long)KeyEvent.VK_W ) << 32);
		long x = (((long)KeyEvent.VK_X ) << 32);
		long y = (((long)KeyEvent.VK_Y ) << 32);
		long z = (((long)KeyEvent.VK_Z ) << 32);
		
		//won't worry about the numbers' shifted values for the moment..
		long k1 = (((long)KeyEvent.VK_1)<< 32);
		long shiftk1 = addShift( k1 );
		long k2 = (((long)KeyEvent.VK_2)<< 32);
		long k3 = (((long)KeyEvent.VK_3)<< 32);
		long k4 = (((long)KeyEvent.VK_4)<< 32);
		long k5 = (((long)KeyEvent.VK_5)<< 32);
		long k6 = (((long)KeyEvent.VK_6)<< 32);
		long k7 = (((long)KeyEvent.VK_7)<< 32);
		long k8 = (((long)KeyEvent.VK_8)<< 32);
		long k9 = (((long)KeyEvent.VK_9)<< 32);
		long k0 = (((long)KeyEvent.VK_0)<< 32);
		
		long space = (((long)KeyEvent.VK_SPACE)<< 32);
		long enter = (((long)KeyEvent.VK_ENTER)<< 32);
		long esc = (((long)KeyEvent.VK_ESCAPE)<< 32);
		long tab = (((long)KeyEvent.VK_TAB)<< 32);
		
		long down = (((long)KeyEvent.VK_DOWN)<< 32);
		long up = (((long)KeyEvent.VK_UP)<< 32);
		
		long minus = (((long)KeyEvent.VK_MINUS)<< 32);
		long insert = (((long)KeyEvent.VK_INSERT)<< 32);
		long delete = (((long)KeyEvent.VK_DELETE)<< 32);
		long home = (((long)KeyEvent.VK_HOME)<< 32);
		long end = (((long)KeyEvent.VK_END)<< 32);
		long pageUp = (((long)KeyEvent.VK_PAGE_UP)<< 32);
		long pageDown = (((long)KeyEvent.VK_PAGE_DOWN)<< 32);
		long F4 = (((long)KeyEvent.VK_F4)<< 32);
		
		//and the modified ones..
		
		long altA = addAlt( a );
		long ctrlA = addCtrl( a );
		long shiftA = addShift( a );
		long ctrlShiftA = addCtrl( addShift( a ) );
		
		long altB = addAlt( b );
		long ctrlShiftB = addCtrl( addShift( b ) );
		long ctrlB = addCtrl ( b );
		long shiftB = addShift ( b );
		
		long ctrlC = addCtrl ( c );
		long altC = addAlt ( c );
		long shiftC = addShift( c );
		
		long ctrlAltD = addCtrl( addAlt( d ) );
		long ctrlShiftD = addCtrl( addShift( d ) );
		long ctrlD = addCtrl( d );
		long altD = addAlt( d );
		
		long ctrlE = addCtrl( e );
		long altE = addAlt( e );
		long shiftE = addShift( e );
		
		long ctrlShiftF = addShift( addCtrl( f ));
		long ctrlF = addCtrl( f );
		long altF = addAlt( f );
		
		long ctrlG = addCtrl( g );
		long ctrlShiftG = addShift( addCtrl( g ));
		long shiftG = addShift( g );
		
		long ctrlH = addCtrl( h );
		long ctrlShiftH = addShift( addCtrl( h ) );
		long shiftH = addShift( h );
		long altH = addAlt( h );
		
		long ctrlI = addCtrl( i );
		long shiftI = addShift( i );
		long altI = addAlt( i );
		
		long ctrlJ = addCtrl( j );
		long shiftJ = addShift( j );
		long altJ = addAlt( j );
		
		long ctrlAltK = addCtrl( addAlt( k ) );
		long ctrlShiftK = addCtrl( addShift( k ) );
		long ctrlK = addCtrl( k );
		long shiftK = addShift( k );
		long altK = addAlt( k );
		
		long ctrlShiftL = addCtrl( addShift( l ) );
		long ctrlL = addCtrl( l );
		long shiftL = addShift( l );
		long altShiftL = addAlt( addShift(  l ) );
		long altL = addAlt( l );
		
		long ctrlM = addCtrl( m );
		long shiftM = addShift( m );
		long altM = addAlt( m );
		
		long ctrlShiftN = addCtrl( addShift( n ) );
		long altN = addAlt( n );
		
		long ctrlShiftO = addCtrl( addShift( o ) );
		long ctrlO = addCtrl( o );
		long altO = addAlt( o );
		
		long ctrlShiftP = addCtrl( addShift( p ) );
		long altP = addAlt(  p );
		long ctrlP = addCtrl(  p );
		
		long ctrlShiftQ = addCtrl( addShift( q ) );
        long ctrlAltQ = addCtrl( addAlt( q ) );
		long ctrlQ = addCtrl( q );
		long altQ = addAlt( q );
		long shiftQ = addShift( q );
		
		long ctrlShiftR = addCtrl( addShift( r ) );
		long ctrlR = addCtrl( r );
		long altR = addAlt( r );
		long shiftR = addShift( r );
		
		long ctrlShiftS = addCtrl( addShift( s ) );
		long ctrlS = addCtrl( s );
		long shiftS = addShift( s );
		long altS = addAlt( s );
		
		long ctrlShiftT = addCtrl( addShift( t ) );
		long ctrlT = addCtrl( t );
		long shiftT = addShift( t );
		long altT = addAlt( t );
		long ctrlAltT = addCtrl( addAlt( t ) );
		
		long ctrlShiftU = addCtrl( addShift( u ) );
		long ctrlU = addCtrl( u );
		long shiftU = addShift( u );
		long altU = addAlt( u );
		
		long ctrlShiftV = addCtrl( addShift( v ) );
		long ctrlV = addCtrl( v );
		long altShiftV = addAlt( addShift( v ) );
		long shiftV = addShift( v );
		long altV = addAlt( v );
		
		long ctrlShiftW = addCtrl( addShift( w ) );
		long ctrlW = addCtrl( w );
		long shiftW = addShift( w );
		long altW = addAlt( w );
		
		long ctrlShiftX = addCtrl( addShift( x ) );
		long shiftX =  addShift( x );
		
		long ctrlShiftZ = addCtrl( addShift( z ) );
		long shiftZ = addShift( z );
		long ctrlZ = addCtrl( z );
		long altZ = addAlt( z );
		
		long ctrlUp = addCtrl( up );
		long ctrlDown = addCtrl( down );
		
		long ctrlPageUp = addCtrl( pageUp );
		long ctrlPageDown = addCtrl( pageDown );
		
		long altF4 = addAlt( F4 );
		long ctrlShftEsc = addCtrl( addShift( esc ) );
		long shftEsc = addShift( esc );
		
	 	keymap = new Hashtable<Long, KeyAction>();
	 	toolmap = new Hashtable<String, KeyAction>();
		
		//initial boolean matrix data
		Object[] matrixsize = { new Integer(T.nextbooleanmatrixsize) };
		
		//initial save filename
		Object[] filename = { new String( "tyler" ) };
		
		//constants
		Object[] zeroFloat = { new Float( 0.0f ) };
		Object[] oneFloat = { new Float( 1.0f ) };
		Object[] twoFloat = { new Float( 2.0f ) };
		Object[] halfFloat = { new Float( 0.5f ) };
		
		Object[] ptOTwoFiveFloat = { new Float(0.025) };
		Object[] ptOEightFloat = { new Float(0.08) };
		Object[] quarterFloat = { new Float(0.25) };
		
		Object[] point1Float = { new Float( 0.1f ) };
		Object[] point2Float = { new Float( 0.2f ) };
		Object[] point3Float = { new Float( 0.3f ) };
		Object[] point4Float = { new Float( 0.4f ) };
		Object[] point5Float = { new Float( 0.5f ) };
		Object[] point6Float = { new Float( 0.6f ) };
		Object[] point7Float = { new Float( 0.7f ) };
		Object[] point8Float = { new Float( 0.8f ) };
		Object[] point9Float = { new Float( 0.9f ) };
		Object[] minusPoint1Float = { new Float( -0.1f ) };
		Object[] minusPoint01Float = { new Float( -0.01f ) };
		Object[] point01Float = { new Float( 0.01f ) };
		Object[] minusOneFloat = { new Float( -1.0f ) };
		Object[] tenFloat = { new Float( 10.0f ) };
		Object[] minusTenFloat = { new Float( -10.0f ) };
		
		//keyMapKey k = new keyMapKey( KeyEvent.VK_A);//, KeyEvent.CTRL_MASK );// | InputEvent.SHIFT_MASK )  );
		
		//declare all the KeyActions
		
		Vector<KeyAction> actions = new Vector<KeyAction>();
		
		//Auto Mode
		actions.add( new KeyAction( "Auto Mode/Interactive Mode", "setInteractiveMode", EditWidget.widgetType.IntValue, false, T , null, T, "Interactive mode - when set to zero, the user controls everything.. otherwise it turns on procedural control for various things as you increase the value", ctrlShiftA ) );
		actions.add( new KeyAction( "Globalz/Draw Keys", "setDrawKeys", EditWidget.widgetType.BooleanValue, false, T , null, T, "Draw Keys - Render text to display what keys are being pressed, for screen-captures.", altD ) );
		actions.add( new KeyAction( "Globalz/Draw Actions", "setDrawActions", EditWidget.widgetType.BooleanValue, false, T , null, T, "Draw Actions - Render text to display the most recent widget action.", shiftE ) );
		
		actions.add( new KeyAction( "Auto Mode/Wiggle Amp", "setPixelWiggleAmp", EditWidget.widgetType.FloatValue, false, T, null, T, "Amplitude of movement of the various bits when in non-interactive mode ", ctrlShiftV ) );
		actions.add( new KeyAction( "Auto Mode/Frequency", "setPixelRotNoiseFreq", EditWidget.widgetType.FloatValue, false, T, null, T, "Frequency of movement of the various bits when in non-interactive mode ", ctrlV ) );
		actions.add( new KeyAction( "Auto Mode/Speed Noise Freq", "setSpeedNoiseFreq", EditWidget.widgetType.FloatValue, false, T, null, T, "Frequency of speed noise in non-interactive modes", ctrlShiftX ) );
		
		//Channel
		actions.add( new KeyAction( "Channel/Channel Mult", "setChannelMultiplier", EditWidget.widgetType.FloatValue, false, M , null, T, "Default extracted channel multiplier", ctrlShiftB ) );
		actions.add( new KeyAction( "Channel/Channel Offset", "setChannelOffset", EditWidget.widgetType.FloatValue, false, M , null, T, "Offset for default extracted channel", ctrlB ) );
		actions.add( new KeyAction( "Channel/Channel Power", "setChannelPower", EditWidget.widgetType.FloatValue, false, M , null, T, "Power for default extracted channel", shiftB ) );
		actions.add( new KeyAction( "Channel/Static Value", "setStaticChannel", EditWidget.widgetType.IntValue, false, M , null , T, "Value of static channel when default extraction mode is set to staticChannel", ctrlF ) );
		actions.add( new KeyAction( "Channel/Set Extractor", "setDefaultChannel", EditWidget.widgetType.EnumValue, false, M, MiniPixelTools.pixelChannel.getNames(), T, "Default channel to extract from pixel values", altM ) );
		
		
		//Global
		actions.add( new KeyAction( "Globalz/Screen Clear Alpha", "setScreenClearAlpha", EditWidget.widgetType.FloatValue, false, T , null, T, "Screen clearing on/off", altL ) );
		actions.add( new KeyAction( "Globalz/Debug Mode", "setDebug", EditWidget.widgetType.BooleanValue, false, T , null , T, "Toggle debugging output - add or remove aunt carla's sock", ctrlAltD ) );
		actions.add( new KeyAction( "Globalz/Pants on Fire", "setYourPantsOnFire", EditWidget.widgetType.IntValue, false, T, null, T, "Your pants are now on fire.", altA ) );
		actions.add( new KeyAction( "Globalz/Wiggle Jump Rate", "setWiggleJumpFrameRate", EditWidget.widgetType.IntValue, false, T , null, T, "Length in frames between random jumps in pixelbuffer position ", ctrlShiftH ) );
		actions.add( new KeyAction( "Globalz/Mouse Cursor", "setMouseCursorModeFromInt", EditWidget.widgetType.EnumValue, false, T , Tiler.MouseCursorMode.getNames(), T, "View mode of the mouse cursor", ctrlH ) );
		actions.add( new KeyAction( "Globalz/Macro Record", "setRecording", EditWidget.widgetType.BooleanValue, false, E, null , T, "Toggle recording of EditWidget events.. (experimental) )", altR ) );
		actions.add( new KeyAction( "Globalz/Sleep Delay", "setSleepDelay", EditWidget.widgetType.IntValue, false, T, null, T, "Sleep delay in main thread (can be used to slow things down if neccesary) )", ctrlShiftS ) );
		actions.add( new KeyAction( "Globalz/Print Help","printKeyMap", EditWidget.widgetType.NoValue, true, K, null, T, "Print the keymap - a kind of help", altH ) );
		actions.add( new KeyAction( "Globalz/Quit", "systemWideExit", EditWidget.widgetType.NoValue, true, T, null, T, "Alt-F4 detected, had enough then?", ctrlShftEsc ) );
		//Too many times by accident!
		//actions.add( new KeyAction( "Globalz/Quit", "systemWideExit", EditWidget.widgetType.NoValue, true, T, null, T, "Ctrl-Q detected, had enough have we?", ctrlQ ) );
		//make it so you have to send it True
		actions.add( new KeyAction( "Globalz/Quit", "systemWideExitWhenTrue", EditWidget.widgetType.BooleanValue, false, T, null, T, "Ctrl-Q detected, time to decide if we've had enough is it?", ctrlQ ) );
        actions.add( new KeyAction( "Globalz/QuitAfterNFrames", "systemWideExitAfterNFrames", EditWidget.widgetType.IntValue, false, T, null, T, "Close the app after this many frames have been captured.", ctrlAltQ ) );
		
		
		//Output
		actions.add( new KeyAction( "Output/Save Image", "saveBuffer", EditWidget.widgetType.NoValue, true, T, null , T, "Save the currently selected buffer (to a frame-numbered jpg) )", ctrlS ) );
		actions.add( new KeyAction( "Output/Save Filename", "setSaveString", EditWidget.widgetType.LStringEdit, false, T, filename, T, "Filename to save to, eg: name.####.jpg", altS ) );
		actions.add( new KeyAction( "Output/Continuous Save", "setContinuousSave", EditWidget.widgetType.BooleanValue, false, T, null, T, "Turn on or off continuous saving of the pixelbuffer", shiftW ) );
		actions.add( new KeyAction( "Output/Save Which Buffer", "setSaveWhichBuffer", EditWidget.widgetType.IntValue, false, T, null, T, "Select the buffer to save (0 = fullscreen, 1 = pixelbuffer) ) ", altW ) );
		actions.add( new KeyAction( "Output/Save Frame Spacing", "setSaveFrameSpacing",EditWidget.widgetType.IntValue, false, T, null , T, "Rate at which to save frames when in continuous save mode", altN ) );
		
		
		
		//Particles
		//Particles/Visual
		actions.add( new KeyAction( "Particles/Visual/Base Alpha", "setBaseAlpha", EditWidget.widgetType.IntValue, false, P , null, T, "Particle base alpha, the basic opacity of the particles", ctrlA ) );
		actions.add( new KeyAction( "Particles/Visual/Channel Alpha", "setChannelAlpha", EditWidget.widgetType.IntValue, false, P , null, T, "Particle opacity amount that is set from their default extracted channel", shiftA ) );
		actions.add( new KeyAction( "Particles/Visual/Paint Mode", "setPaintMode", EditWidget.widgetType.EnumValue, false, T , Tiler.ParticlePaintMode.getNames(), T, "Set the mode in which to draw particles", shiftC ) );
		actions.add( new KeyAction( "Particles/Visual/Draw Shape", "setParticleDrawMode", EditWidget.widgetType.EnumValue, false, P , Particle.particleDrawMode.getNames(), T, "Set the particle draw mode", altI ) );
		actions.add( new KeyAction( "Particles/Visual/Size Source", "setSizeSource", EditWidget.widgetType.EnumValue, false, P , ParticleSystem.particleSizeSource.getNames(), T, "Source of size value for particles", altJ ) );
		actions.add( new KeyAction( "Particles/Visual/Color Stability", "setParticleStability", EditWidget.widgetType.FloatValue, false, P, null , T, "Particle stability (kinda how much they withstand changes to their color) )", ctrlShiftP ) );
		actions.add( new KeyAction( "Particles/Visual/Color Mode", "setParticleColorMode", EditWidget.widgetType.EnumValue, false, P, Particle.particleColorMode.getNames(), T, "Particle stability (kinda how much they withstand changes to their color)", ctrlP ) );
		actions.add( new KeyAction( "Particles/Visual/Size Slope", "setSizeSlope", EditWidget.widgetType.FloatValue, false, P, null, T, "Sizeslope - how fast particles shrink when in 'age' particleDrawMode", ctrlShiftQ ) );
		actions.add( new KeyAction( "Particles/Visual/Base Size", "setBaseSizeInt", EditWidget.widgetType.IntValue, false, P, null, T, "Base size of emitted particles", q  ) );
		actions.add( new KeyAction( "Particles/Visual/Pixel Space Scale", "setPixelSpaceScale", EditWidget.widgetType.FloatValue, false, P, null, T, "Scaling of the pixelbuffer space with respect to particle sampling", altU ) );
		actions.add( new KeyAction( "Particles/Visual/Stroke Width", "setBaseStrokeWidth", EditWidget.widgetType.FloatValue, false, P, null, T, "Base stroke width for particle drawmodes that draw lines or stroke shapes ", ctrlW ) );
		
		//Particles/Motion
		actions.add( new KeyAction( "Particles/Motion/Speed Noise", "setSpeedNoiseAmp", EditWidget.widgetType.FloatValue, false, T , null, T, "Amplitude of speed noise for particles", altC ) );
		actions.add( new KeyAction( "Particles/Motion/Drag From Channel", "setChannelDragLevel", EditWidget.widgetType.FloatValue, false, P , null , T, "Amount of drag on particles is set from their current default 'extracted channel", ctrlShiftD ) );
		actions.add( new KeyAction( "Particles/Motion/Drag Random", "setDragRandom", EditWidget.widgetType.FloatValue, false, T , null , T, "Randomness in amount of drag on emitted particles", ctrlD  ) );
		actions.add( new KeyAction( "Particles/Motion/Drag Base", "setDragLevel", EditWidget.widgetType.FloatValue, false, T , null , T, "Base amount of drag on emitted particles", d  ) );
		actions.add( new KeyAction( "Particles/Motion/Speed Base", "setSpeedMultiplier", EditWidget.widgetType.FloatValue, false, T , null , T, "Multiplier of speed on emitted particles", ctrlE ) );
		actions.add( new KeyAction( "Particles/Motion/Speed Random", "setSpeedRandom", EditWidget.widgetType.FloatValue, false, T , null , T, "Randomness of speed on emitted particles", ctrlShiftF ) );
		actions.add( new KeyAction( "Particles/Motion/Kill Above Speed", "setMaxSpeedInt", EditWidget.widgetType.IntValue, false, P, null , T, "Speed above which particles are killed", ctrlShiftO ) );
		actions.add( new KeyAction( "Particles/Motion/Kill Below Speed", "setMinSpeed", EditWidget.widgetType.FloatValue, false, P, null , T, "Speed below which particles are killed", ctrlO ) );
		actions.add( new KeyAction( "Particles/Motion/Channel Spin", "setChannelSpinLevel", EditWidget.widgetType.FloatValue, false, P, null, T, "Amount of spin on particles that is set by the particle's current default channel value", ctrlShiftR ) );
		actions.add( new KeyAction( "Particles/Motion/Spin Random", "setSpinRandom", EditWidget.widgetType.FloatValue, false, T, null, T, "Random amount of spin on particles", ctrlR ) );
		actions.add( new KeyAction( "Particles/Motion/Base Spin", "setCurrentSpin", EditWidget.widgetType.FloatValue, false, T, null, T, "Base amount of spin on particles", r ) );
		actions.add( new KeyAction( "Particles/Motion/Spin Probability", "setSpinProbability", EditWidget.widgetType.FloatValue, false, T, null, T, "Probability that particles will 'turn'  (negative values will also invert the rotation direction) )", s ) );
		
		//Particles/Global
		actions.add( new KeyAction( "Particles/Global/Max Number", "setTotalNumParticles", EditWidget.widgetType.IntValue, false, P , null , T, "Total number of particles in particlesystem", altE  ) );
		actions.add( new KeyAction( "Particles/Global/Interpolate Samples", "setInterpolation", EditWidget.widgetType.BooleanValue, false, T , null , T, "Set whether to use interpolation", ctrlI ) );
		actions.add( new KeyAction( "Particles/Global/Base LFO Freq", "setBaseLFOFreq", EditWidget.widgetType.FloatValue, false, P, null, T, "Set the base frequency for oscillator-driven particle effects", ctrlShiftL ) );
		actions.add( new KeyAction( "Particles/Global/Set L-String", "setLString", EditWidget.widgetType.LStringEdit, false, P, null, T, "L-String with which particles are processed", ctrlL ) );
		actions.add( new KeyAction( "Particles/Global/Connected Emission", "setConnectEmission", EditWidget.widgetType.BooleanValue, false, T, null, T, "Whether to create connections between emitted particles", shiftQ ) );
		actions.add( new KeyAction( "Particles/Global/Display", "toggleParticlesOn", EditWidget.widgetType.NoValue, true, T, null, T, "Toggle particle visibility", altP ) );
		actions.add( new KeyAction( "Particles/Global/L-Threshold", "setLThreshold",EditWidget.widgetType.IntValue, false, P, null, T, "Threshold to measure particle 'default value' crossings in - to drive L-effects,", ctrlAltT ) );
		
		//Particles/Emission
		actions.add( new KeyAction( "Particles/Emission/Random Location", "setLocRandom", EditWidget.widgetType.FloatValue, false, T , null , T, "Randomness of location on emitted particles", altF ) );
		actions.add( new KeyAction( "Particles/Emission/Chance of Death", "setChanceOfDeath", EditWidget.widgetType.FloatValue, false, P, null, T, "Chance of particles randomly being executed, hmm", ctrlAltK  ) );
		actions.add( new KeyAction( "Particles/Emission/Death by Age", "setDeathByAge", EditWidget.widgetType.IntValue, false, P, null, T, "Particle max lifespan", ctrlShiftK ) );
		actions.add( new KeyAction( "Particles/Emission/Kill All", "killAllParticles", EditWidget.widgetType.NoValue, true, P, null, T, "Kill all particles", ctrlK ) );
		actions.add( new KeyAction( "Particles/Emission/Kill Some", "killParticles", EditWidget.widgetType.BooleanValue, false, P, null, T, "Kill particles, or not, or kill em a few times ;P", shiftK ) );
		actions.add( new KeyAction( "Particles/Emission/Max Spawn Depth", "setMaxSpawnDepth", EditWidget.widgetType.IntValue, false, P, null, T, "Maximum depth to spawn particles to", altShiftL ) );
		actions.add( new KeyAction( "Particles/Emission/Emission Rate", "setParticleRate", EditWidget.widgetType.FloatValue, false, T, null, T, "Rate of particle emission", altQ ) );
		actions.add( new KeyAction( "Particles/Emission/Zero Emission", "toggleParticlesOn", EditWidget.widgetType.FloatValue, true, T, zeroFloat, T, "Set particle rate to 0", shftEsc ) );
		
		
		//Pixelbuffer
		actions.add( new KeyAction( "Pixelbuffer/Toggle Fade", "tileFadeToggle", EditWidget.widgetType.NoValue, true, T , null, T, "Toggle alpha fading state", ctrlC ) );
		actions.add( new KeyAction( "Pixelbuffer/Visibility", "toggleClearMode", EditWidget.widgetType.NoValue, true, T , null, T, "Toggle whether to draw the pixelbuffer or not", c ) );
		actions.add( new KeyAction( "Pixelbuffer/Copy to Source Tile", "copyBufferPixelsToTilePixels", EditWidget.widgetType.NoValue, true, T , null , T, "Copy contents of pixel buffer to tile buffer (ie grab current state as the tile) ) ", ctrlShiftG ) );
		actions.add( new KeyAction( "Pixelbuffer/Pixel Blend Mode", "setPixelBlendMode", EditWidget.widgetType.EnumValue, false, T , Tiler.PixelBlendMode.getNames(), T, "Set the pixel blend mode of the tile image drawing into the feedback buffer", shiftI ) );
		actions.add( new KeyAction( "Pixelbuffer/Visibility Again", "togglePixelAccess", EditWidget.widgetType.NoValue, true, T, null, T, "Toggle pixelaccess - pixel procesing and visibility of the main buffer", p ) );
		actions.add( new KeyAction( "Pixelbuffer/Tile Load Alpha", "setTileLoadAlpha", EditWidget.widgetType.IntValue, false, T, null, T, "Rate with which to blend in new tiles when they are loaded", ctrlShiftU ) );
		actions.add( new KeyAction( "Pixelbuffer/Tile Draw Alpha", "setClearAlpha", EditWidget.widgetType.IntValue, false, T, null, T, "Opacity with which to draw the tile into the processing buffer", u ) );
		actions.add( new KeyAction( "Pixelbuffer/Tile Draw Alpha", "setClearAlpha", EditWidget.widgetType.IntValue, false, T, null, T, "Opacity with which to draw the tile into the processing buffer", ctrlU ) );
		actions.add( new KeyAction( "Pixelbuffer/Draw Alpha", "setPixelAlpha", EditWidget.widgetType.IntValue, false, T, null, T, "Opacity of the pixelbuffer", altShiftV ) );
		actions.add( new KeyAction( "Pixelbuffer/Alpha From Lumi Amount", "setAlphaFromLumiAmount", EditWidget.widgetType.IntValue, false, T, null, T, "Amount of opacity of the pixelbuffer that is set from luminosity", shiftV ) );
		actions.add( new KeyAction( "Pixelbuffer/Regenerate Alpha With Power", "regenerateAlphaWithPower", EditWidget.widgetType.FloatValue, false, T, null, T, "Regenerate the radial opacity ramp with the selected power (0 for no mask, 1 for linear) )", altV ) );
		actions.add( new KeyAction( "Pixelbuffer/Continuous Pixelgrab", "setContinuousPixelGrab", EditWidget.widgetType.BooleanValue, false, T, null, T, "Enable or disable continuous grabbing of pixels into the processing buffer (this is a fari bit more interesting with doWarp on!!)", ctrlShiftW ) );
		//actions.add( new KeyAction( "Pixelbuffer/Size", "setScalePixelBuffer", EditWidget.widgetType.FloatValue, false, T, null, T, "Draw-time re-scaling of pixelBuffer (this is slow unfortunately) )", u ) );
		actions.add( new KeyAction( "Pixelbuffer/Continuous Map Draw", "setContinuousMapDraw", EditWidget.widgetType.BooleanValue, false, T, null, T, "Drawing of the map buffer", shiftZ ) );
		actions.add( new KeyAction( "Pixelbuffer/Continuous Map Grab", "setContinuousMapGrab", EditWidget.widgetType.BooleanValue, false, T, null, T, "Grabbing of the map buffer", ctrlZ ) );
		
		actions.add( new KeyAction( "Pixelbuffer/Next Image Source", "nextImage", EditWidget.widgetType.NoValue, true, T, null, T, "Fade to the next image in the set.", space ) );
		
		//Topology
		actions.add( new KeyAction( "Topology/Regenerate", "generateTopologyFromSettings", EditWidget.widgetType.NoValue, true, T , null , T, "Queue topology regeneration ", ctrlG ) );
		actions.add( new KeyAction( "Topology/Set Boolean Matrix", "setCurrentBooleanMatrix", EditWidget.widgetType.BooleanMatrixValue, false, T , matrixsize , T, "Set a boolean matrix for neighborhood generation", altB ) );
		actions.add( new KeyAction( "Topology/Oversampling", "setTopologyOversampling", EditWidget.widgetType.IntValue, false, T , null, T, "Topology oversampling", altO ) );
		actions.add( new KeyAction( "Topology/Use Alpha", "setWarpWithAlpha", EditWidget.widgetType.BooleanValue, false, T , null , T, "Set whether to use alpha in the topology processing (only supported in some topology blend modes) )", shiftH ) );
		actions.add( new KeyAction( "Topology/Use Particles in Multipoint", "setMultipointFromParticle", EditWidget.widgetType.BooleanValue, false, T ,null  , T, "Use particle positions or random positions in multipoint topology generation modes", ctrlJ ) );
		actions.add( new KeyAction( "Topology/Blend Mode", "setTopologyNextFrameMode", EditWidget.widgetType.EnumValue, false, T , Topology.TopologyBlendMode.getNames(), T, "Topology processing mode (pixel and automata blend modes)", shiftJ ) );
		actions.add( new KeyAction( "Topology/Generate Mapped", "setGenerateTopologyMapped", EditWidget.widgetType.BooleanValue, false, T, null, T, "Whether to use mapped topology generation for generator modes that support it", shiftM ) );
		actions.add( new KeyAction( "Topology/Rasterization Mode", "setTopologyRenderMode", EditWidget.widgetType.EnumValue, false, T, Topology.TopologyRenderMode.getNames(), T, "Mode in which to rasterize topologie. (the topology squish value is used in chain and ramp types)", shiftX ) );
		actions.add( new KeyAction( "Topology/Squish Value", "setTopologySquishValue", EditWidget.widgetType.FloatValue, false, T,  null , T, "Parameter that controls the feedback scaling used when generating chain and ramp topology layer rasterization modes", x ) );
		actions.add( new KeyAction( "Topology/Num Centers for Multipoint", "setNumCentersMultiPoint", EditWidget.widgetType.IntValue, false, T, null, T, "Number of points to use when generating multipoint topology types", ctrlShiftZ ) );
		actions.add( new KeyAction( "Topology/Enable Processing", "setDoWarp", EditWidget.widgetType.BooleanValue, false, T, null, T, "Enable or disable processing of topology warp/automata layer", ctrlShiftT ) );
		actions.add( new KeyAction( "Topology/Power Parameter", "setCurrentTopologyPower", EditWidget.widgetType.FloatValue, false, T, null, T, "Topology generation power parameter", ctrlM ) );
		actions.add( new KeyAction( "Topology/Main Parameter", "setCurrentTopologyParam", EditWidget.widgetType.FloatValue, false, T, null, T, "Main topology generation parameter", shiftL ) );
		actions.add( new KeyAction( "Topology/Set Generator", "setTopologyGeneratorMode", EditWidget.widgetType.EnumValue, false, T , Topology.TopologyGeneratorMode.getNames(), T, "Set the next type of topology to generate", shiftG ) );
		
		//Tool
		actions.add( new KeyAction( "Tool/Edit Mode", "setToolEditMode", EditWidget.widgetType.BooleanValue, false, L , null, T, "Tool1D editing mode", altT ) );
		actions.add( new KeyAction( "Tool/Set Sample Line", "setLine", EditWidget.widgetType.Line, false, L, null, T, "Set the line along which to sample pixel values in the 'tool' widget", ctrlT ) );
		actions.add( new KeyAction( "Tool/Always Harvests", "setToolAlwaysHarvests", EditWidget.widgetType.BooleanValue, false, L, null, T, "Whether the tool harvests values from the 'line' every frame when not in tool edit mode", shiftU ) );
		actions.add( new KeyAction( "Tool/Opacity", "setToolDrawAlpha", EditWidget.widgetType.IntValue, false, L, null , T, "Opacity of the tool widget", ctrlShiftN ) );
		actions.add( new KeyAction( "Tool/Vertical Scale", "setVerticalScale", EditWidget.widgetType.FloatValue, false, L, null, T, "Tool vertical scaling value", shiftT ) );
		actions.add( new KeyAction( "Tool/Draw Background", "setDrawBackground", EditWidget.widgetType.BooleanValue, false, L, null, T, "Tool background draw", shiftS ) );
		actions.add( new KeyAction( "Tool/Visibility", "setToolIsVisible", EditWidget.widgetType.BooleanValue, false, L, null, T, "Tool visibility", t ) );
		
		//Scroll Rate
		actions.add( new KeyAction( "Scroll Rate/Double", "multiplyCurrentLevel", EditWidget.widgetType.FloatValue, true, T, twoFloat, T, "Double the currentlevel parameter", up ) );
		actions.add( new KeyAction( "Scroll Rate/Halve", "multiplyCurrentLevel", EditWidget.widgetType.FloatValue, true, T, halfFloat, T,"Halve the currentlevel parameter", down ) );
		actions.add( new KeyAction( "Scroll Rate/Squared", "powCurrentLevel", EditWidget.widgetType.FloatValue, true, T, twoFloat, T, "Square the currentlevel parameter", ctrlUp ) );
		actions.add( new KeyAction( "Scroll Rate/Sqrt", "powCurrentLevel", EditWidget.widgetType.FloatValue, true, T, halfFloat, T, "Square root the currentlevel parameter", ctrlDown ) );
		actions.add( new KeyAction( "Scroll Rate/Negate","multiplyCurrentLevel", EditWidget.widgetType.FloatValue, true, T, minusOneFloat, T, "Negate the currentlevel parameter (multiply by -1) )", minus ) );
		
		//Scroll Rate/Set
		actions.add( new KeyAction( "Scroll Rate/Set/Set 0.0", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, zeroFloat, T, "Set the currentlevel parameter to 0.0", k0 ) );
		actions.add( new KeyAction( "Scroll Rate/Set/Set 0.025", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, ptOTwoFiveFloat, T, "Set the currentlevel parameter to 0.025", k1 ) );
		actions.add( new KeyAction( "Scroll Rate/Set/Set 1.0", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, oneFloat, T, "Set the currentlevel parameter to 1.0", shiftk1 ) );
		actions.add( new KeyAction( "Scroll Rate/Set/Set 0.08", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, ptOEightFloat, T, "Set the currentlevel parameter to 0.08", k2 ) );
		actions.add( new KeyAction( "Scroll Rate/Set/Set 0.25", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, quarterFloat, T, "Set the currentlevel parameter to 0.25", k3 ) );
		actions.add( new KeyAction( "Scroll Rate/Set/Set 0.4", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point4Float, T, "Set the currentlevel parameter to 0.4", k4 ) );
		actions.add( new KeyAction( "Scroll Rate/Set/Set 0.5", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point5Float, T, "Set the currentlevel parameter to 0.5", k5 ) );
		actions.add( new KeyAction( "Scroll Rate/Set/Set 0.6", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point6Float, T, "Set the currentlevel parameter to 0.6", k6 ) );
		actions.add( new KeyAction( "Scroll Rate/Set/Set 0.7", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point7Float, T, "Set the currentlevel parameter to 0.7", k7 ) );
		actions.add( new KeyAction( "Scroll Rate/Set/Set 0.8", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point8Float, T, "Set the currentlevel parameter to 0.8", k8 ) );
		actions.add( new KeyAction( "Scroll Rate/Set/Set 0.9", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point9Float, T, "Set the currentlevel parameter to 0.9", k9 ) );
		
		//Scroll Rate/Add
		actions.add( new KeyAction( "Scroll Rate/Add/Add 0.01", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point01Float, T, "Add 0.01 to the currentlevel parameter", insert ) );
		actions.add( new KeyAction( "Scroll Rate/Add/Add 0.1", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point1Float, T, "Add 0.1 to the currentlevel parameter", home ) );
		actions.add( new KeyAction( "Scroll Rate/Add/Add 1.0", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, oneFloat, T, "Add 1 to the currentlevel parameter", pageUp ) );
		actions.add( new KeyAction( "Scroll Rate/Add/Add 10", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, tenFloat, T, "Add 10 to the currentlevel parameter", ctrlPageUp ) );
		
		//Scroll Rate/Subtract
		actions.add( new KeyAction( "Scroll Rate/Subtract/Subtract 0.01", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, minusPoint01Float, T, "Subtract 0.01 from the currentlevel parameter", delete ) );
		actions.add( new KeyAction( "Scroll Rate/Subtract/Subtract 0.1", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, minusPoint1Float, T, "Subtract 0.1 from the currentlevel parameter", end ) );
		actions.add( new KeyAction( "Scroll Rate/Subtract/Subtract 1.0", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, minusOneFloat, T, "Subtract 1 from the currentlevel parameter", pageDown ) );
		actions.add( new KeyAction( "Scroll Rate/Subtract/Subtract 10", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, minusTenFloat, T, "Subtract 10 from the currentlevel parameter", ctrlPageDown ) );
		
		//add the keycode + keyaction to the hashtables:
				
		for ( KeyAction action : actions )
		{
			keymap.put( action.keycode, action );
			toolmap.put( action.toolname, action );
			//toolmap.put( keymap.get(action).toolname, keymap.get(action) );
		}
		// This one needs to go last in order to be able to access the list of everything else.
		String[] methodList = getKeyToolStrings();
		//for ( String st : methodList ) 
		//{
		//	System.out.println( st );
		//}
		
		KeyAction tabMenuAction = new KeyAction( "This Menu", "postTabMenu", EditWidget.widgetType.MenuValue, false, K, methodList, T, "Tab menu for actions", tab );
		keymap.put( tab, tabMenuAction );
	}
	
	public void postTabMenu( String action )
	{
		//System.out.println("tab menu action: " + action );
		
		if ( toolmap.containsKey( action ) )
		{
			Point currMouse = T.getMousePosition();
			T.addWidget( toolmap.get(action).doAction( currMouse.x, currMouse.y ) );
		}
	}
	
	public static long addCtrl( long to )
	{
		return to | (long) KeyEvent.CTRL_MASK;
	}
	
	public static long addAlt( long to )
	{
		return to | (long) KeyEvent.ALT_MASK;
	}
	
	public static long addShift( long to )
	{
		return to | (long) KeyEvent.SHIFT_MASK;
	}
	
	public static boolean hasCtrl( long to )
	{
		return ( to & (long) KeyEvent.CTRL_MASK ) != 0;
	}
	
	public static boolean hasAlt( long to )
	{
		return ( to & (long) KeyEvent.ALT_MASK ) != 0;
	}
	
	public static boolean hasShift( long to )
	{
		return ( to & (long) KeyEvent.SHIFT_MASK ) != 0;
	}
	
	public void printKeyMap()
	{
		long k = 0;
		
		//Vector v = new Vector( keymap.keySet() );
		//Collections.sort(v);
		
		//System.out.println( v );
		
		Enumeration kz = keymap.keys();
		
		for (Enumeration e = keymap.elements() ; e.hasMoreElements() ;) 
		{
			//System.out.println( "keymap entry number: " + k );
			KeyAction kv = (KeyAction) e.nextElement();
			long kk = (Long) kz.nextElement();
			
			//System.out.println( "Da Key: " + kk );
			
			//if ( hasCtrl( kk ) ) System.out.print( "ctrl+" );
			//if ( hasAlt( kk ) ) System.out.print( "alt+" );
			//if ( hasShift( kk ) ) System.out.print( "shift+" );
			
			String c = kv.getKeyString();
			
			//System.out.println( "Da KeyString: " + c );
			
			System.out.print( c );
			System.out.print(" : ");
			
			System.out.print( "\"" + kv.docstring + "\" " );
			System.out.println("");
			//System.out.println( kv.methodname );
			k++;
     		}
	}
	
	public String[] getKeyActionStrings()
	{
		System.out.println( "Keymap length: " + keymap.size() );
		String[] k = new String[ keymap.size() ];
		int l = 0;
		for ( KeyAction ka : keymap.values() )
		{
			k[l] =  ka.docstring;
			l++;
		}
		return k;
	}
	
	public String[] getKeyToolStrings()
	{
		//System.out.println( "Keymap length: " + keymap.size() );
		String[] k = new String[ keymap.size() ];
		int l = 0;
		for ( KeyAction ka : keymap.values() )
		{
			k[l] =  ka.toolname + "  :  " + ka.getKeyString();
			l++;
		}
		return k;
	}
	
	public KeyAction getKeyAction( long hashkey )
	{
		return keymap.get( hashkey );
	}
	
}


/*	



*/
