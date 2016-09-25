import java.awt.*;
import java.awt.event.*;
import java.util.*;

class tilerKeyMap
{
	Hashtable<Long, keyAction> keymap;
	Tiler T;
	ParticleSystem P;
	Tool1D L;
	Object M = MiniPixelTools.class; //these methods are static in minipixeltools so.. there's no need for a runtime instance at the mo'
	Object E = EditWidget.class;
	
	//try { E = (Object) Class.forName("EditWidget"); }
	//catch (ClassNotFoundException cnx) {output("the class loader had a problem recognising the EditWidget class name.. how did that happen?"); break;}
	
	//Object E = (Object) Class.forName("EditWidget");
	tilerKeyMap K;
	
	public tilerKeyMap( Tiler tilerTarget, ParticleSystem particleTarget, Tool1D toolTarget )
	{
		//grab the objects that the actions are to be performed on, just Tiler and particleSystem to begin with
		T = tilerTarget;
		P = particleTarget;
		L = toolTarget;
		K = this;
		
		
	 	keymap = new Hashtable<Long, keyAction>();
	
		//keyMapKey k = new keyMapKey( KeyEvent.VK_A);//, KeyEvent.CTRL_MASK );// | InputEvent.SHIFT_MASK )  );
		
		//declare all the keyActions
		
		keyAction setInteractiveModeAction = new keyAction( "Global/Interactive Mode", "setInteractiveMode", EditWidget.widgetType.IntValue, false, T , null, T, "Interactive mode - when set to zero, the user controls everything.. otherwise it turns on procedural control for various things as you increase the value" );
		keyAction setYourPantsOnFireAction = new keyAction( "Global/Pants on Fire", "setYourPantsOnFire", EditWidget.widgetType.IntValue, false, T, null, T, "Your pants on fire." );
		keyAction setParticleBaseAlphaAction = new keyAction( "Particles/Base Alpha", "setBaseAlpha", EditWidget.widgetType.IntValue, false, P , null, T, "Particle base alpha, the basic opacity of the particles" );
		keyAction setParticleChannelAlphaAction = new keyAction( "Particles/Channel Alpha", "setChannelAlpha", EditWidget.widgetType.IntValue, false, P , null, T, "Particle opacity amount that is set from their default extracted channel" );
		keyAction setMouseCursorModeAction = new keyAction( "GUI/Mouse Cursor", "setMouseCursorMode", EditWidget.widgetType.EnumValue, false, T , ( Object[] ) Tiler.MouseCursorMode.getNames() , T, "View mode of the mouse cursor" );
		keyAction setChannelMultiplierAction = new keyAction( "Channel/Channel Mult", "setChannelMultiplier", EditWidget.widgetType.FloatValue, false, M , null, T, "Default extracted channel multiplier" );
		keyAction generateTopologyFromSettingsAction = new keyAction( "Topology/Regenerate", "generateTopologyFromSettings", EditWidget.widgetType.NoValue, true, T , null , T, "Queue topology regeneration " );
		keyAction setChannelOffsetAction = new keyAction( "Channel/Channel Offset", "setChannelOffset", EditWidget.widgetType.FloatValue, false, M , null, T, "Offset for default extracted channel" );
		keyAction setChannelPowerAction = new keyAction( "Channel/Channel Power", "setChannelPower", EditWidget.widgetType.FloatValue, false, M , null, T, "Power for default extracted channel" );
		keyAction tileAlphaFadeAction = new keyAction( "Tile/Toggle Fade", "tileFadeToggle", EditWidget.widgetType.NoValue, true, T , null, T, "Toggle alpha fading state" );
		keyAction setSpeedNoiseAmpAction = new keyAction( "Particles/Speed Noise", "setSpeedNoiseAmp", EditWidget.widgetType.FloatValue, false, T , null, T, "Amplitude of speed noise for particles" );
		keyAction setWiggleJumpFrameRateAction = new keyAction( "Global/Wiggle Jump Rate", "setWiggleJumpFrameRate", EditWidget.widgetType.IntValue, false, T , null, T, "Length in frames between random jumps in pixelbuffer position " );
		Object[] matrixsize = { new Integer(T.nextbooleanmatrixsize) };
		keyAction setCurrentBooleanMatrixAction = new keyAction( "Topology/Set Boolean Matrix", "setCurrentBooleanMatrix", EditWidget.widgetType.BooleanMatrixValue, false, T , matrixsize , T, "Set a boolean matrix for neighborhood generation" );
		keyAction setToolEditModeAction = new keyAction( "Tool/Edit Mode", "setToolEditMode", EditWidget.widgetType.BooleanValue, false, L , null, T, "Tool1D editing mode" );
		keyAction setScreenClearAlphaAction = new keyAction( "Global/Screen Clear Alpha", "setScreenClearAlpha", EditWidget.widgetType.FloatValue, false, T , null, T, "Screen clearing on/off" );
		keyAction setTopologyOversamplingAction = new keyAction( "Topology/Oversampling", "setTopologyOversampling", EditWidget.widgetType.IntValue, false, T , null, T, "Topology oversampling" );
		keyAction setParticlePaintModeAction = new keyAction( "Particles/Paint Mode", "setPaintMode", EditWidget.widgetType.EnumValue, false, T , ( Object[] ) Tiler.ParticlePaintMode.getNames() , T, "Set the mode in which to draw particles" );
		keyAction toggleClearModeAction = new keyAction( "Pixelbuffer/Visibility", "toggleClearMode", EditWidget.widgetType.NoValue, true, T , null, T, "Toggle whether to draw the pixelbuffer or not" );
		keyAction setDebugModeAction = new keyAction( "Global/Debug Mode", "setDebug", EditWidget.widgetType.BooleanValue, false, T , null , T, "Toggle debugging output - add or remove aunt carla's sock" );
		keyAction setChannelDragLevelAction = new keyAction( "Particles/Drag From Channel", "setChannelDragLevel", EditWidget.widgetType.FloatValue, false, P , null , T, "Amount of drag on particles is set from their current default 'extracted channel" );
		keyAction setDragRandomAction = new keyAction( "Particles/Drag Random", "setDragRandom", EditWidget.widgetType.FloatValue, false, T , null , T, "Randomness in amount of drag on emitted particles" );
		keyAction setDragLevelAction = new keyAction( "Particles/Drag", "setDragLevel", EditWidget.widgetType.FloatValue, false, T , null , T, "Base amount of drag on emitted particles" );
		keyAction setSpeedMultiplierAction = new keyAction( "Particles/Speed", "setSpeedMultiplier", EditWidget.widgetType.FloatValue, false, T , null , T, "Multiplier of speed on emitted particles" );
		keyAction setTotalNumParticles = new keyAction( "Particles/Max Number", "setTotalNumParticles", EditWidget.widgetType.IntValue, false, P , null , T, "Total number of particles in particlesystem" );
		keyAction setSpeedRandomAction = new keyAction( "Particles/Speed Random", "setSpeedRandom", EditWidget.widgetType.FloatValue, false, T , null , T, "Randomness of speed on emitted particles" );
		keyAction setStaticChannelAction = new keyAction( "Channel/Static Value", "setStaticChannel", EditWidget.widgetType.IntValue, false, M , null , T, "Value of static channel when default extraction mode is set to staticChannel" );
		keyAction setLocRandomAction = new keyAction( "Particles/Random Location", "setLocRandom", EditWidget.widgetType.FloatValue, false, T , null , T, "Randomness of location on emitted particles" );
		keyAction copyBufferPixelsToTilePixelsAction = new keyAction( "Pixelbuffer/Copy to Source Tile", "copyBufferPixelsToTilePixels", EditWidget.widgetType.NoValue, true, T , null , T, "Copy contents of pixel buffer to tile buffer (ie grab current state as the tile) " );
		keyAction setTopologyGeneratorModeAction = new keyAction( "Topology/Set Generator", "setTopologyGeneratorMode", EditWidget.widgetType.EnumValue, false, T , (Object[]) Topology.TopologyGeneratorMode.getNames() , T, "Set the next type of topology to generate" );
		keyAction setWarpWithAlphaAction = new keyAction( "Topology/Use Alpha", "setWarpWithAlpha", EditWidget.widgetType.BooleanValue, false, T , null , T, "Set whether to use alpha in the topology processing (only supported in some topology blend modes)" );
		keyAction setInterpolationAction = new keyAction( "Particles/Interpolate Samples", "setInterpolation", EditWidget.widgetType.BooleanValue, false, T , null , T, "Set whether to use interpolation" );
		keyAction setPixelBlendModeAction = new keyAction( "Pixelbuffer/Pixel Blend Mode", "setPixelBlendMode", EditWidget.widgetType.EnumValue, false, T , (Object[]) Tiler.PixelBlendMode.getNames() , T, "Set the pixel blend mode of the tile image drawing into the feedback buffer" );
		keyAction setParticleDrawModeAction = new keyAction( "Particles/Draw Mode", "setParticleDrawMode", EditWidget.widgetType.EnumValue, false, P , (Object[]) Particle.particleDrawMode.getNames() , T, "Set the particle draw mode" );
		keyAction setMultipointFromParticleAction = new keyAction( "Topology/Use Particles in Multipoint", "setMultipointFromParticle", EditWidget.widgetType.BooleanValue, false, T ,null  , T, "Use particle positions or random positions in multipoint topology generation modes" );		
		keyAction setTopologyNextFrameModeAction = new keyAction( "Topology/Blend Mode", "setTopologyNextFrameMode", EditWidget.widgetType.EnumValue, false, T ,(Object[]) Topology.TopologyBlendMode.getNames() , T, "Topology processing mode (pixel and automata blend modes)" );
		keyAction setSizeSourceAction = new keyAction( "Particles/Size Source", "setSizeSource", EditWidget.widgetType.EnumValue, false, P ,(Object[]) ParticleSystem.particleSizeSource.getNames() , T, "Source of size value for particles" );
		keyAction setChanceOfDeathAction = new keyAction( "Particles/Chance of Death", "setChanceOfDeath", EditWidget.widgetType.FloatValue, false, P, null, T, "Chance of particles randomly being executed, hmm" );
		keyAction setDeathByAgeAction = new keyAction( "Particles/Death by Age", "setDeathByAge", EditWidget.widgetType.IntValue, false, P, null, T, "Particle max lifespan" );
		keyAction killAllParticlesAction = new keyAction( "Particles/Kill All", "killAllParticles", EditWidget.widgetType.NoValue, true, P, null, T, "Kill all particles" );
		keyAction killParticlesAction = new keyAction( "Particles/Kill Some", "killParticles", EditWidget.widgetType.BooleanValue, false, P, null, T, "Kill particles, or not, or kill em a few times ;P" );
		keyAction setBaseLFOFreqAction = new keyAction( "Particles/Base LFO Freq", "setBaseLFOFreq", EditWidget.widgetType.FloatValue, false, P, null, T, "Set the base frequency for oscillator-driven particle effects" );
		keyAction setLStringAction = new keyAction( "Particles/Set L-String", "setLString", EditWidget.widgetType.LStringEdit, false, P, null, T, "L-String with which particles are processed" );
		keyAction setCurrentTopologyParamAction = new keyAction( "Topology/Main Parameter", "setCurrentTopologyParam", EditWidget.widgetType.FloatValue, false, T, null, T, "Main topology generation parameter" );
		keyAction setMaxSpawnDepthAction = new keyAction( "Particles/Max Spawn Depth", "setMaxSpawnDepth", EditWidget.widgetType.IntValue, false, P, null, T, "Maximum depth to spawn particles to" );
		keyAction setCurrentTopologyPowerAction = new keyAction( "Topology/Power", "setCurrentTopologyPower", EditWidget.widgetType.FloatValue, false, T, null, T, "Topology generation power parameter" );
		keyAction setGenerateTopologyMappedAction = new keyAction( "Topology/Generate Mapped", "setGenerateTopologyMapped", EditWidget.widgetType.BooleanValue, false, T, null, T, "Whether to use mapped topology generation for generator modes that support it" );
		keyAction setDefaultChannelAction = new keyAction( "Channel/Set Extractor", "setDefaultChannel", EditWidget.widgetType.EnumValue, false, M, (Object[]) MiniPixelTools.pixelChannel.getNames(), T, "Default channel to extract from pixel values" );
		keyAction setToolDrawAlphaAction = new keyAction( "Tool/Opacity", "setToolDrawAlpha", EditWidget.widgetType.IntValue, false, L, null , T, "Opacity of the tool widget" );
		keyAction setMaxSpeedIntAction = new keyAction( "Particles/Kill Above Speed", "setMaxSpeedInt", EditWidget.widgetType.IntValue, false, P, null , T, "Speed above which particles are killed" );
		keyAction setMinSpeedAction = new keyAction( "Particles/Kill Below Speed", "setMinSpeed", EditWidget.widgetType.FloatValue, false, P, null , T, "Speed below which particles are killed" );
		keyAction setParticleStabilityAction = new keyAction( "Particles/Color Stability", "setParticleStability", EditWidget.widgetType.FloatValue, false, P, null , T, "Particle stability (kinda how much they withstand changes to their color)" );
		keyAction setParticleColorModeAction = new keyAction( "Particles/Color Mode", "setParticleColorMode", EditWidget.widgetType.EnumValue, false, P, (Object[]) Particle.particleColorMode.getNames(), T, "Particle stability (kinda how much they withstand changes to their color)" );
		keyAction togglePixelAccessAction = new keyAction( "Pixelbuffer/Visibility Again", "togglePixelAccess", EditWidget.widgetType.NoValue, true, T, null, T, "Toggle pixelaccess - pixel procesing and visibility of the main buffer" );
		keyAction setSizeSlopeAction = new keyAction( "Particles/Size Slope", "setSizeSlope", EditWidget.widgetType.FloatValue, false, P, null, T, "Sizeslope - how fast particles shrink when in 'age' particleDrawMode" );
		keyAction setParticleRateAction = new keyAction( "Particles/Emission Rate", "setParticleRate", EditWidget.widgetType.FloatValue, false, T, null, T, "Rate of particle emission" );
		keyAction setConnectEmissionAction = new keyAction( "Particles/Connected Emission", "setConnectEmission", EditWidget.widgetType.BooleanValue, false, T, null, T, "Whether to create connections between emitted particles" );
		keyAction setBaseSizeIntAction = new keyAction( "Particles/Base Size", "setBaseSizeInt", EditWidget.widgetType.IntValue, false, P, null, T, "Base size of emitted particles" );
		keyAction setChannelSpinLevelAction = new keyAction( "Particles/Channel Spin", "setChannelSpinLevel", EditWidget.widgetType.FloatValue, false, P, null, T, "Amount of spin on particles that is set by the particle's current default channel value" );
		keyAction setSpinRandomAction = new keyAction( "Particles/Spin Random", "setSpinRandom", EditWidget.widgetType.FloatValue, false, T, null, T, "Random amount of spin on particles" );
		keyAction setRecordingAction = new keyAction( "Global/Macro Record", "setRecording", EditWidget.widgetType.BooleanValue, false, E, null , T, "Toggle recording of EditWidget events.. (experimental)" );
		keyAction setCurrentSpinAction = new keyAction( "Particles/Base Spin", "setCurrentSpin", EditWidget.widgetType.FloatValue, false, T, null, T, "Toggle recording of EditWidget events.. (experimental)" );
		keyAction setSleepDelayAction = new keyAction( "GUI/Sleep Delay", "setSleepDelay", EditWidget.widgetType.IntValue, false, T, null, T, "Sleep delay in main thread (can be used to slow things down if neccesary)" );
		keyAction saveBufferAction = new keyAction( "Output/Save Image", "saveBuffer", EditWidget.widgetType.NoValue, true, T, null , T, "Save the currently selected buffer (to a frame-numbered jpg)" );
		
		Object[] filename = { new String( "tyler" ) };
		keyAction setSaveStringAction = new keyAction( "Output/Save Filename", "setSaveString", EditWidget.widgetType.LStringEdit, false, T, filename, T, "Filename to save to, eg: name.####.jpg" );
		
		keyAction setSpinProbabilityAction = new keyAction( "Particles/Spin Probability", "setSpinProbability", EditWidget.widgetType.FloatValue, false, T, null, T, "Probability that particles will 'turn'  (negative values will also invert the rotation direction)" );
		keyAction setDoWarpAction = new keyAction( "Topology/Enable Processing", "setDoWarp", EditWidget.widgetType.BooleanValue, false, T, null, T, "Enable or disable processing of topology warp/automata layer" );
		keyAction setLineAction = new keyAction( "Tool/Sample Line", "setLine", EditWidget.widgetType.Line, false, L, null, T, "Set the line along which to sample pixel values in the 'tool' widget" );
		keyAction setVerticalScaleAction = new keyAction( "Tool/Vertical Scale", "setVerticalScale", EditWidget.widgetType.FloatValue, false, L, null, T, "Tool vertical scaling value" );
		keyAction setToolIsVisibleAction = new keyAction( "Tool/Visibility", "setToolIsVisible", EditWidget.widgetType.BooleanValue, false, L, null, T, "Tool visibility" );
		keyAction setTileLoadAlphaAction = new keyAction( "Pixelbuffer/Tile Load Alpha", "setTileLoadAlpha", EditWidget.widgetType.IntValue, false, T, null, T, "Rate with which to blend in new tiles when they are loaded" );
		keyAction setClearAlphaAction = new keyAction( "Pixelbuffer/Tile Draw Alpha", "setClearAlpha", EditWidget.widgetType.IntValue, false, T, null, T, "Opacity with which to draw the tile into the processing buffer" );
		keyAction setToolAlwaysHarvestsAction = new keyAction( "Tool/Always Harvests", "setToolAlwaysHarvests", EditWidget.widgetType.BooleanValue, false, L, null, T, "Whether the tool harvests values from the 'line' every frame when not in tool edit mode" );
		keyAction setPixelSpaceScaleAction = new keyAction( "Particles/Pixel Space Scale", "setPixelSpaceScale", EditWidget.widgetType.FloatValue, false, P, null, T, "Scaling of the pixelbuffer space with respect to particle sampling" );
		keyAction setScalePixelBufferAction = new keyAction( "Pixelbuffer/Size", "setScalePixelBuffer", EditWidget.widgetType.FloatValue, false, T, null, T, "Draw-time re-scaling of pixelBuffer (this is slow unfortunately)" );
		keyAction setPixelWiggleAmpAction = new keyAction( "Auto Mode/Wiggle Amp", "setPixelWiggleAmp", EditWidget.widgetType.FloatValue, false, T, null, T, "Amplitude of movement of the various bits when in non-interactive mode " );
		keyAction setPixelRotNoiseFreqAction = new keyAction( "Auto Mode/Freq", "setPixelRotNoiseFreq", EditWidget.widgetType.FloatValue, false, T, null, T, "Frequency of movement of the various bits when in non-interactive mode " );
		keyAction setPixelAlphaAction = new keyAction( "Pixelbuffer/Draw Alpha", "setPixelAlpha", EditWidget.widgetType.IntValue, false, T, null, T, "Opacity of the pixelbuffer" );
		keyAction setAlphaFromLumiAmountAction = new keyAction( "Pixelbuffer/Alpha From Lumi Amount", "setAlphaFromLumiAmount", EditWidget.widgetType.IntValue, false, T, null, T, "Amount of opacity of the pixelbuffer that is set from luminosity" );
		keyAction regenerateAlphaWithPowerAction = new keyAction( "Pixelbuffer/Regenerate Alpha With Power", "regenerateAlphaWithPower", EditWidget.widgetType.FloatValue, false, T, null, T, "Regenerate the radial opacity ramp with the selected power (0 for no mask, 1 for linear)" );
		keyAction setContinuousPixelGrabAction = new keyAction( "Pixelbuffer/Continuous Pixelgrab", "setContinuousPixelGrab", EditWidget.widgetType.BooleanValue, false, T, null, T, "Enable or disable continuous grabbing of pixels into the processing buffer (most interesting with doWarp on " );
		keyAction setBaseStrokeWidthAction = new keyAction( "Particles/Stroke Width", "setBaseStrokeWidth", EditWidget.widgetType.FloatValue, false, P, null, T, "Base stroke width for particle drawmodes that draw lines or stroke shapes " );
		keyAction setContinuousSaveAction = new keyAction( "Output/Continuous Save", "setContinuousSave", EditWidget.widgetType.BooleanValue, false, T, null, T, "Turn on or off continuous saving of the pixelbuffer" );
		keyAction setSaveWhichBufferAction = new keyAction( "Output/Save Which Buffer", "setSaveWhichBuffer", EditWidget.widgetType.IntValue, false, T, null, T, "Select the buffer to save (0 = fullscreen, 1 = pixelbuffer) " );
		keyAction setSpeedNoiseFreqAction = new keyAction( "Auto Mode/Speed Noise Freq", "setSpeedNoiseFreq", EditWidget.widgetType.FloatValue, false, T, null, T, "Frequency of speed noise in non-interactive modes" );
		keyAction setTopologyRenderModeAction = new keyAction( "Topology/Rasterization Mode", "setTopologyRenderMode", EditWidget.widgetType.EnumValue, false, T, (Object[]) Topology.TopologyRenderMode.getNames(), T, "Mode in which to rasterize topologie. (the topology squish value is used in chain and ramp types)" );
		keyAction setTopologySquishValueAction = new keyAction( "Topology/Squish Value", "setTopologySquishValue", EditWidget.widgetType.FloatValue, false, T,  null , T, "Parameter that controls the feedback scaling used when generating chain and ramp topology layer rasterization modes" );
		keyAction setNumCentersMultiPointAction = new keyAction( "Topology/Num Centers for Multipoint", "setNumCentersMultiPoint", EditWidget.widgetType.IntValue, false, T, null, T, "Number of points to use when generating multipoint topology types" );
		keyAction setContinuousMapDrawAction = new keyAction( "Pixelbuffer/Continuous Map Draw", "setContinuousMapDraw", EditWidget.widgetType.BooleanValue, false, T, null, T, "Drawing of the map buffer" );
		keyAction setContinuousMapGrabAction = new keyAction( "Pixelbuffer/Continuous Map Grab", "setContinuousMapGrab", EditWidget.widgetType.BooleanValue, false, T, null, T, "Grabbing of the map buffer" );
		keyAction printKeyMapAction = new keyAction( "Global/Print Help","printKeyMap", EditWidget.widgetType.NoValue, true, K, null, T, "Print the keymap - a kind of help" );
		
		
		Object[] zeroFloat = { new Float( 0.0f ) };
		Object[] oneFloat = { new Float( 1.0f ) };
		Object[] twoFloat = { new Float( 2.0f ) };
		Object[] halfFloat = { new Float( 0.5f ) };
		
		keyAction doubleCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Double", "multiplyCurrentLevel", EditWidget.widgetType.FloatValue, true, T, twoFloat, T, "Double the currentlevel parameter" );
		keyAction halveCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Halve", "multiplyCurrentLevel", EditWidget.widgetType.FloatValue, true, T, halfFloat, T,"Halve the currentlevel parameter" );
		keyAction squareCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Squared", "powCurrentLevel", EditWidget.widgetType.FloatValue, true, T, twoFloat, T, "Square the currentlevel parameter" );
		keyAction sqrtCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Sqrt", "powCurrentLevel", EditWidget.widgetType.FloatValue, true, T, halfFloat, T, "Square root the currentlevel parameter" );
		
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
		
		keyAction setCurrentLevelToPoint1Action = new keyAction( "Pixelbuffer/Scroll Speed/0.1", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point1Float, T, "Set the currentlevel parameter to 0.1" );
		keyAction setCurrentLevelToPoint2Action = new keyAction( "Pixelbuffer/Scroll Speed/0.2", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point2Float, T, "Set the currentlevel parameter to 0.2" );
		keyAction setCurrentLevelToPoint3Action = new keyAction( "Pixelbuffer/Scroll Speed/0.3", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point3Float, T, "Set the currentlevel parameter to 0.3" );
		keyAction setCurrentLevelToPoint4Action = new keyAction( "Pixelbuffer/Scroll Speed/0.4", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point4Float, T, "Set the currentlevel parameter to 0.4" );
		keyAction setCurrentLevelToPoint5Action = new keyAction( "Pixelbuffer/Scroll Speed/0.5", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point5Float, T, "Set the currentlevel parameter to 0.5" );
		keyAction setCurrentLevelToPoint6Action = new keyAction( "Pixelbuffer/Scroll Speed/0.6", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point6Float, T, "Set the currentlevel parameter to 0.6" );
		keyAction setCurrentLevelToPoint7Action = new keyAction( "Pixelbuffer/Scroll Speed/0.7", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point7Float, T, "Set the currentlevel parameter to 0.7" );
		keyAction setCurrentLevelToPoint8Action = new keyAction( "Pixelbuffer/Scroll Speed/0.8", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point8Float, T, "Set the currentlevel parameter to 0.8" );
		keyAction setCurrentLevelToPoint9Action = new keyAction( "Pixelbuffer/Scroll Speed/0.9", "setCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point9Float, T, "Set the currentlevel parameter to 0.9" );
		
		Object[] minusOneFloat = { new Float( -1.0f ) };
		Object[] tenFloat = { new Float( 10.0f ) };
		Object[] minusTenFloat = { new Float( -10.0f ) };
		
		keyAction addPoint01ToCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Add 0.01", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point01Float, T, "Add 0.01 to the currentlevel parameter" );
		keyAction addMinusPoint01ToCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Subtract 0.01", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, minusPoint01Float, T, "Subtract 0.01 from the currentlevel parameter" );
		
		keyAction addPoint1ToCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Add 0.1", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, point1Float, T, "Add 0.1 to the currentlevel parameter" );
		keyAction addMinusPoint1ToCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Subtract 0.1", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, minusPoint1Float, T, "Subtract 0.1 from the currentlevel parameter" );
		
		keyAction add1ToCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Add 1.0", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, oneFloat, T, "Add 1 to the currentlevel parameter" );
		keyAction addMinus1ToCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Subtract 1.0", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, minusOneFloat, T, "Subtract 1 from the currentlevel parameter" );
		
		keyAction add10ToCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Add 10", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, tenFloat, T, "Add 10 to the currentlevel parameter" );
		keyAction addMinus10ToCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Subtract 10", "addToCurrentLevel", EditWidget.widgetType.FloatValue, true, T, minusTenFloat, T, "Subtract 10 from the currentlevel parameter" );
		
		keyAction negateCurrentLevelAction = new keyAction( "Pixelbuffer/Scroll Speed/Negate","multiplyCurrentLevel", EditWidget.widgetType.FloatValue, true, T, minusOneFloat, T, "Negate the currentlevel parameter (multiply by -1)" );
		
		keyAction systemWideExitAction = new keyAction( "Global/Quit", "systemWideExit", EditWidget.widgetType.NoValue, true, T, null, T, "Alt-F4 detected, had enough then?" );
		
		keyAction setParticleRateToZeroAction = new keyAction( "Particles/Zero Emission", "toggleParticlesOn", EditWidget.widgetType.FloatValue, true, T, zeroFloat, T, "Set particle rate to 0" );
		keyAction toggleParticlesOnAction = new keyAction( "Particles/Display", "toggleParticlesOn", EditWidget.widgetType.NoValue, true, T, null, T, "Toggle particle visibility" );
		
		keyAction setSaveFrameSpacingAction = new keyAction( "Output/Save Frame Spacing", "setSaveFrameSpacing",EditWidget.widgetType.IntValue, false, T, null , T, "Rate at which to save frames when in continuous save mode" );
		
		keyAction setsetLThreshold = new keyAction( "Particles/L-Threshold", "setLThreshold",EditWidget.widgetType.IntValue, false, P, null, T, "Threshold to measure particle 'default value' crossings in - to dive L-effects");
		//Object[] oneInteger = { new Integer( 1 ) };
		//keyAction setBaseSizeIntToOneAction = new keyAction( "setBaseSizeInt", EditWidget.widgetType.IntValue, true, P, oneInteger, T, "set base size of emitted particles to one" );
		
		//keyAction postBaseMenuActionAction = new keyAction( "postBaseMenuAction", EditWidget.widgetType.DynamicEnumValue, false, K , ( Object[] ) this ,  "post a menu of all actions.. experimental at this stage" );		
		
		//declare the key values
		
		long a =	(((long)KeyEvent.VK_A ) << 32);
		long b =	(((long)KeyEvent.VK_B ) << 32);
		long c =	(((long)KeyEvent.VK_C ) << 32);
		long d =	(((long)KeyEvent.VK_D ) << 32);
		long e =	(((long)KeyEvent.VK_E ) << 32);
		long f =	(((long)KeyEvent.VK_F ) << 32);
		long g =	(((long)KeyEvent.VK_G ) << 32);
		long h =	(((long)KeyEvent.VK_H ) << 32);
		long i =	(((long)KeyEvent.VK_I ) << 32);
		long j =	(((long)KeyEvent.VK_J ) << 32);
		long k =	(((long)KeyEvent.VK_K ) << 32);
		long l =	(((long)KeyEvent.VK_L ) << 32);
		long m =	(((long)KeyEvent.VK_M ) << 32);
		long n =	(((long)KeyEvent.VK_N ) << 32);
		long o =	(((long)KeyEvent.VK_O ) << 32);
		long p =	(((long)KeyEvent.VK_P ) << 32);
		long q =	(((long)KeyEvent.VK_Q ) << 32);
		long r =	(((long)KeyEvent.VK_R ) << 32);
		long s =	(((long)KeyEvent.VK_S ) << 32);
		long t =	(((long)KeyEvent.VK_T ) << 32);
		long u =	(((long)KeyEvent.VK_U ) << 32);
		long v =	(((long)KeyEvent.VK_V ) << 32);
		long w =	(((long)KeyEvent.VK_W ) << 32);
		long x =	(((long)KeyEvent.VK_X ) << 32);
		long y =	(((long)KeyEvent.VK_Y ) << 32);
		long z =	(((long)KeyEvent.VK_Z ) << 32);
		
		//won't worry about the numbers' shifted values for the moment..
		long k1 = (((long)KeyEvent.VK_1)<< 32);
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
		
		long ctrlE = addCtrl( e );
		long altE = addAlt( e );
		
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
		long ctrlP = addCtrl(  p );
		
		long ctrlShiftQ = addCtrl( addShift( q ) );
		long  ctrlQ = addCtrl( q );
		long  shiftQ = addShift( q );
		
		long  ctrlShiftR = addCtrl( addShift( r ) );
		long  ctrlR = addCtrl(  r  );
		long  altR = addAlt(  r  );
		long  shiftR = addShift(  r  );
		
		long  ctrlShiftS = addCtrl( addShift(  s  ) );
		long  ctrlS = addCtrl( s );
		//long  shiftS = addShift(  s  );
		long  altS = addAlt(  s  );
		
		long  ctrlShiftT = addCtrl( addShift(  t  ) );
		long  ctrlT = addCtrl( t );
		long  shiftT = addShift(  t  );
		long altT = addAlt( t );
		long ctrlAltT = addCtrl( addAlt( t ) );
		
		long  ctrlShiftU = addCtrl( addShift(  u  ) );
		long  ctrlU = addCtrl( u  );
		long  shiftU = addShift( u  );
		long  altU = addAlt( u  );
		
		long  ctrlShiftV = addCtrl( addShift(  v  ) );
		long  ctrlV = addCtrl( v );
		long  altShiftV = addAlt( addShift( v ) );
		long  shiftV = addShift( v );
		long  altV = addAlt( v );
		
		long  ctrlShiftW = addCtrl( addShift(  w  ) );
		long  ctrlW = addCtrl(  w  );
		long  shiftW = addShift(  w  );
		long  altW = addAlt(  w  );
		
		long  ctrlShiftX = addCtrl( addShift(  x  ) );
		long shiftX =  addShift(  x  );
		
		long  ctrlShiftZ = addCtrl( addShift(  z  ) );
		long  shiftZ = addShift(  z  );
		long  ctrlZ = addCtrl(  z );
		long  altZ = addAlt(  z );
		
		long ctrlUp = addCtrl( up );
		long ctrlDown = addCtrl( down );
		
		long ctrlPageUp = addCtrl( pageUp );
		long ctrlPageDown = addCtrl( pageDown );
		
		long altF4 = addAlt( F4 );
		long ctrlShftEsc = addCtrl( addShift( esc ) );
		long shftEsc = addShift( esc );
		//add the keycode + keyaction to the hashtable:
		
		keymap.put( ctrlA, setParticleBaseAlphaAction );
		keymap.put( altA, setYourPantsOnFireAction );
		keymap.put( shiftA, setParticleChannelAlphaAction );
		keymap.put( ctrlShiftA, setInteractiveModeAction );
		
		keymap.put( ctrlB, setChannelOffsetAction );
		keymap.put( altB, setCurrentBooleanMatrixAction );
		keymap.put( shiftB, setChannelPowerAction );
		keymap.put( ctrlShiftB, setChannelMultiplierAction );
		
		keymap.put( c, toggleClearModeAction );
		keymap.put( altC, setSpeedNoiseAmpAction );
		keymap.put( ctrlC, tileAlphaFadeAction );
		keymap.put( shiftC, setParticlePaintModeAction );
		
		keymap.put( ctrlAltD, setDebugModeAction );
		keymap.put( ctrlShiftD, setChannelDragLevelAction );
		keymap.put( ctrlD , setDragRandomAction );
		keymap.put( d , setDragLevelAction );
		
		keymap.put( ctrlE,  setSpeedMultiplierAction);			
		keymap.put( altE , setTotalNumParticles );
		
		keymap.put( ctrlShiftF,  setSpeedRandomAction);
		keymap.put( ctrlF,  setStaticChannelAction);
		keymap.put( altF, setLocRandomAction );	
		
		keymap.put( ctrlG, generateTopologyFromSettingsAction );
		keymap.put( ctrlShiftG, copyBufferPixelsToTilePixelsAction );
		keymap.put( shiftG, setTopologyGeneratorModeAction );
			
		keymap.put( ctrlH, setMouseCursorModeAction );
		keymap.put( ctrlShiftH, setWiggleJumpFrameRateAction );
		keymap.put( shiftH, setWarpWithAlphaAction );
		keymap.put( altH, printKeyMapAction );
		
		keymap.put( ctrlI, setInterpolationAction );
		keymap.put( shiftI, setPixelBlendModeAction );
		keymap.put( altI, setParticleDrawModeAction );				
		
		keymap.put( ctrlJ, setMultipointFromParticleAction );
		keymap.put( shiftJ, setTopologyNextFrameModeAction );
		keymap.put( altJ, setSizeSourceAction );
										
		keymap.put( ctrlAltK , setChanceOfDeathAction );
		
		keymap.put( ctrlShiftK, setDeathByAgeAction );
		keymap.put( ctrlK, killAllParticlesAction);
		keymap.put( shiftK, killParticlesAction );
		keymap.put( altK, printKeyMapAction );
		
		keymap.put( ctrlShiftL, setBaseLFOFreqAction );
		keymap.put( ctrlL, setLStringAction );
		keymap.put( shiftL, setCurrentTopologyParamAction);
		keymap.put( altShiftL, setMaxSpawnDepthAction);
		keymap.put( altL, setScreenClearAlphaAction );
		
		keymap.put( ctrlM, setCurrentTopologyPowerAction);
		keymap.put( shiftM, setGenerateTopologyMappedAction );
		keymap.put( altM, setDefaultChannelAction );
		
		keymap.put( ctrlShiftN, setToolDrawAlphaAction );
		keymap.put( altN, setSaveFrameSpacingAction );
		
		keymap.put( ctrlShiftO, setMaxSpeedIntAction );
		keymap.put( ctrlO, setMinSpeedAction );
		keymap.put( altO, setTopologyOversamplingAction );
		
		keymap.put( ctrlShiftP, setParticleStabilityAction );
		keymap.put( ctrlP, setParticleColorModeAction );
		keymap.put( p, togglePixelAccessAction);
		
		keymap.put( ctrlShiftQ, setSizeSlopeAction );
		keymap.put( ctrlQ, setParticleRateAction);
		keymap.put( shiftQ, setConnectEmissionAction);
		keymap.put( q , setBaseSizeIntAction );
		
		keymap.put( ctrlShiftR, setChannelSpinLevelAction );
		keymap.put( ctrlR, setSpinRandomAction );
		keymap.put( altR, setRecordingAction );
		keymap.put( r, setCurrentSpinAction );
		keymap.put( shiftR, setCurrentSpinAction );
		
		keymap.put( ctrlShiftS, setSleepDelayAction );
		keymap.put( ctrlS, saveBufferAction );
		keymap.put( altS, setSaveStringAction );
		keymap.put( s, setSpinProbabilityAction );
		
		keymap.put( ctrlShiftT, setDoWarpAction );
		keymap.put( ctrlT, setLineAction );
		keymap.put( shiftT, setVerticalScaleAction );
		keymap.put( t, setToolIsVisibleAction );
		keymap.put( altT, setToolEditModeAction );
		keymap.put( ctrlAltT, setsetLThreshold );
		
		keymap.put( ctrlShiftU, setTileLoadAlphaAction );
		keymap.put( ctrlU, setClearAlphaAction );
		keymap.put( shiftU, setToolAlwaysHarvestsAction );
		keymap.put( altU, setPixelSpaceScaleAction );
		keymap.put( u, setScalePixelBufferAction );
		
		keymap.put( ctrlShiftV, setPixelWiggleAmpAction );
		keymap.put( ctrlV, setPixelRotNoiseFreqAction );
		keymap.put( altShiftV, setPixelAlphaAction );
		keymap.put( shiftV, setAlphaFromLumiAmountAction );
		keymap.put( altV, regenerateAlphaWithPowerAction );
		
		keymap.put( ctrlShiftW, setContinuousPixelGrabAction );
		keymap.put( ctrlW, setBaseStrokeWidthAction );
		keymap.put( w, setSizeSlopeAction );
		keymap.put( shiftW, setContinuousSaveAction );
		keymap.put( altW, setSaveWhichBufferAction );
		
		keymap.put( ctrlShiftX, setSpeedNoiseFreqAction );
		keymap.put( shiftX, setTopologyRenderModeAction );
		keymap.put( x, setTopologySquishValueAction );
		
		keymap.put( ctrlShiftZ, setNumCentersMultiPointAction );
		keymap.put( shiftZ, setContinuousMapDrawAction );
		keymap.put( ctrlZ, setContinuousMapGrabAction  );
		//keymap.put( altZ, setBaseSizeIntToOneAction );
		
		keymap.put( up, doubleCurrentLevelAction );
		keymap.put( down, halveCurrentLevelAction );
		keymap.put( ctrlUp, squareCurrentLevelAction );
		keymap.put( ctrlDown, sqrtCurrentLevelAction );

		keymap.put( k1, setCurrentLevelToPoint1Action );
		keymap.put( k2, setCurrentLevelToPoint2Action );
		keymap.put( k3, setCurrentLevelToPoint3Action );
		keymap.put( k4, setCurrentLevelToPoint4Action );
		keymap.put( k5, setCurrentLevelToPoint5Action );
		keymap.put( k6, setCurrentLevelToPoint6Action );
		keymap.put( k7, setCurrentLevelToPoint7Action );
		keymap.put( k8, setCurrentLevelToPoint8Action );
		keymap.put( k9, setCurrentLevelToPoint9Action );
		
		//keymap.put( space, loadNextImageAction );
		//keymap.put( enter,
		//keymap.put( esc, toggleParticleVisibilityAction );
		
		keymap.put( minus, negateCurrentLevelAction );
		
		keymap.put( insert,  addPoint01ToCurrentLevelAction );
		keymap.put( delete, addMinusPoint01ToCurrentLevelAction );
		
		keymap.put( home, addPoint1ToCurrentLevelAction );
		keymap.put( end, addMinusPoint1ToCurrentLevelAction );
		
		keymap.put( pageUp, add1ToCurrentLevelAction );
		keymap.put( pageDown, addMinus1ToCurrentLevelAction );
		
		keymap.put( ctrlPageUp, add10ToCurrentLevelAction );
		keymap.put( ctrlPageDown, addMinus10ToCurrentLevelAction );
		
		
		
		keymap.put( esc, toggleParticlesOnAction );
		keymap.put( shftEsc, setParticleRateToZeroAction);
		keymap.put( ctrlShftEsc, systemWideExitAction );
		
		// This one needs to go last in order to be able to access the list of everything else.
		String[] methodList = getKeyToolStrings();
		for ( String st : methodList ) 
		{
			System.out.println( st );
		}
		
		keyAction tabMenuAction = new keyAction( "", "postTabMenu", EditWidget.widgetType.MenuValue, false, K, (Object[]) methodList, T, "Tab menu for actions" );
		keymap.put( tab, tabMenuAction );
		
		//keymap.put( altF4, systemWideExitAction );
		
	}
	
	public void postTabMenu( String action )
	{
		System.out.println("tab menu action: " + action );
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
	
	public static String getKeyString( long inLong )
	{
		long daKey = inLong & 0xffff0000;
		
		if ( daKey ==    ( ( (long)KeyEvent.VK_0 ) << 32) ) return "0";
		if ( daKey ==    ( ( (long)KeyEvent.VK_1 ) << 32) ) return "1";
		if ( daKey ==    ( ( (long)KeyEvent.VK_2 ) << 32) ) return "2";
		if ( daKey ==    ( ( (long)KeyEvent.VK_3 ) << 32) ) return "3";
		if ( daKey ==    ( ( (long)KeyEvent.VK_4 ) << 32) ) return "4";
		if ( daKey ==    ( ( (long)KeyEvent.VK_5 ) << 32) ) return "5";
		if ( daKey ==    ( ( (long)KeyEvent.VK_6 ) << 32) ) return "6";
		if ( daKey ==    ( ( (long)KeyEvent.VK_7 ) << 32) ) return "7";
		if ( daKey ==    ( ( (long)KeyEvent.VK_8 ) << 32) ) return "8";
		if ( daKey ==    ( ( (long)KeyEvent.VK_9 ) << 32) ) return "9";
		
		if ( daKey ==    ( ( (long)KeyEvent.VK_A ) << 32) ) return "a";
		if ( daKey ==	( ( (long)KeyEvent.VK_B ) << 32) )return "b";
		if ( daKey ==	( ( (long)KeyEvent.VK_C ) << 32) ) return "c";
		if ( daKey ==	( ( (long)KeyEvent.VK_D ) << 32) ) return "d";
		if ( daKey ==	( ( (long)KeyEvent.VK_E ) << 32) ) return "e";
		if ( daKey ==	( ( (long)KeyEvent.VK_F ) << 32) ) return "f";
		if ( daKey ==	( ( (long)KeyEvent.VK_G ) << 32) ) return "g";
		if ( daKey ==	( ( (long)KeyEvent.VK_H ) << 32) ) return "h";
		if ( daKey ==	( ( (long)KeyEvent.VK_I ) << 32) ) return "i";
		if ( daKey ==	( ( (long)KeyEvent.VK_J ) << 32) ) return "j";
		if ( daKey ==	( ( (long)KeyEvent.VK_K ) << 32) ) return "k";
		if ( daKey ==	( ( (long)KeyEvent.VK_L ) << 32) ) return "l";
		if ( daKey ==	( ( (long)KeyEvent.VK_M ) << 32) ) return "m";
		if ( daKey ==	( ( (long)KeyEvent.VK_N ) << 32) ) return "n";
		if ( daKey ==	( ( (long)KeyEvent.VK_O ) << 32) ) return "o";
		if ( daKey ==	( ( (long)KeyEvent.VK_P ) << 32) ) return "p";
		if ( daKey ==	( ( (long)KeyEvent.VK_Q ) << 32) ) return "q";
		if ( daKey ==	( ( (long)KeyEvent.VK_R ) << 32) ) return "r";
		if ( daKey ==	( ( (long)KeyEvent.VK_S ) << 32) ) return "s";
		if ( daKey ==	( ( (long)KeyEvent.VK_T ) << 32) ) return "t";
		if ( daKey ==	( ( (long)KeyEvent.VK_U ) << 32) ) return "u";
		if ( daKey ==	( ( (long)KeyEvent.VK_V ) << 32) ) return "v";
		if ( daKey ==	( ( (long)KeyEvent.VK_W ) <<32) ) return "w";
		if ( daKey ==	( ( (long)KeyEvent.VK_X ) << 32) ) return "x";
		if ( daKey ==	( ( (long)KeyEvent.VK_Y ) << 32) ) return "y";
		if ( daKey ==	( ( (long)KeyEvent.VK_Z ) << 32) ) return "z";
		if ( daKey ==	( ( (long)KeyEvent.VK_ENTER ) << 32) ) return "enter";
		if ( daKey ==	( ( (long)KeyEvent.VK_ESCAPE ) << 32) ) return "esc";
		if ( daKey ==	( ( (long)KeyEvent.VK_SPACE ) << 32) ) return "space";
		if ( daKey ==	( ( (long)KeyEvent.VK_UP ) << 32) ) return "up";
		if ( daKey ==	( ( (long)KeyEvent.VK_DOWN ) << 32) ) return "down";
		if ( daKey ==	( ( (long)KeyEvent.VK_MINUS ) << 32) ) return "minus";
		if ( daKey ==	( ( (long)KeyEvent.VK_INSERT ) << 32) ) return "insert";
		if ( daKey ==	( ( (long)KeyEvent.VK_DELETE ) << 32) ) return "delete";
		if ( daKey ==	( ( (long)KeyEvent.VK_HOME ) << 32) ) return "home";
		if ( daKey ==	( ( (long)KeyEvent.VK_END ) << 32) ) return "end";
		if ( daKey ==	( ( (long)KeyEvent.VK_PAGE_UP ) << 32) ) return "pageUp";
		if ( daKey ==	( ( (long)KeyEvent.VK_PAGE_DOWN ) << 32) ) return "pageDown";
		if ( daKey ==	( ( (long)KeyEvent.VK_F4 ) << 32) ) return "F4";
		if ( daKey ==	( ( (long)KeyEvent.VK_TAB ) << 32) ) return "tab";
		return "# - currently unmapped key";
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
			// System.out.println( "keymap entry number: " + k );
			keyAction kv = (keyAction) e.nextElement();
			long kk = (Long) kz.nextElement();
			
			//System.out.println( "Da Key: " + kk );
			
			if ( hasCtrl( kk ) ) System.out.print( "ctrl+" );
			if ( hasAlt( kk ) ) System.out.print( "alt+" );
			if ( hasShift( kk ) ) System.out.print( "shift+" );
			String c = getKeyString( kk );
			System.out.print( c );
			System.out.print(" : ");
			
			//System.out.print( "\"" + kv.docstring + "\" " );
			System.out.println( kv.methodname );
			k++;
     		}
	}
	
	public String[] getKeyActionStrings()
	{
		System.out.println( "keymap length: " + keymap.size() );
		String[] k = new String[ keymap.size() ];
		int l = 0;
		for ( keyAction ka : keymap.values() )
		{
			k[l] =  ka.docstring;
			l++;
		}
		return k;
	}
	
	public String[] getKeyToolStrings()
	{
		System.out.println( "keymap length: " + keymap.size() );
		String[] k = new String[ keymap.size() ];
		int l = 0;
		for ( keyAction ka : keymap.values() )
		{
			k[l] =  ka.toolname;
			l++;
		}
		return k;
	}
	
	public keyAction getKeyAction( long hashkey )
	{
		return keymap.get( hashkey );
	}
	
}


/*	



*/
