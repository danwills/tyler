import java.lang.*;
import java.util.*;

public final class TopologyGenerationThread extends Thread
{
	public Topology topologylayer;
	Topology currentBlendDestination = null;
	float currentBlendAlpha = 0.1f;
	public TopologyGenerationSettings theSettings;
	public boolean topologyGenerated;
	public boolean destroy;
	public boolean readyToBlend = false;
	//float accurateX[];
	//float accurateY[];
	int buffer[];
	Tiler parentTiler;
	
	TopologyGenerationThread( TopologyGenerationSettings ts, int[] pixels, Tiler parent )
	{
		theSettings = ts;
		buffer = pixels;
		parentTiler = parent;
		topologyGenerated = false;
		destroy = false;
	}
	
	public void run()
	{
		Thread.currentThread().setPriority( Thread.MIN_PRIORITY );
		if ( this.theSettings == null ) return;
		
		TopologyGenerationSettings t = this.theSettings;
		topologylayer = new Topology(t.topologyWidth, t.topologyHeight, buffer, t.numwarplayers,new Random());
		
		if ((t.topologygeneratormode != Topology.TopologyGeneratorMode.PlusNeighborhood) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.LifeNeighborhood) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.RandomIFS))
		{
			parentTiler.output("selected rendermode is:" + t.topologyrendermode);
			
			switch ( t.topologyrendermode )
			{
				case DitheredMultilayer : { parentTiler.output("rendering dithered multitopology.."); break; }
				case ChainMultilayer : { parentTiler.output("rendering chain multitopology..");  break; }
				case RampMultilayer : { parentTiler.output("rendering ramp multitopology.."); break; }
				case AbsoluteDitheredMultilayer : 	{ parentTiler.output("rendering absolute dithered multitopology.."); break; }
				case AbsoluteChainMultilayer : { parentTiler.output("rendering absolute chain multitopology.."); 	break; }
				case AbsoluteRampMultilayer : { parentTiler.output("rendering absolute ramp multitopology.."); break; }
			}
		}
		
		try {
		
			topologylayer.generateTopologyFromSettings( t );
		
			//render the float topology map into the faster integer-maps
			topologylayer.renderTopologyWithSettings( t );
		
			topologyGenerated = true;
			
		} catch ( java.lang.OutOfMemoryError outtaMem ) {
			
			System.out.println("Topology generation failed! java heap out of memory... maybe try generating topology with less layers?");
			topologyGenerated = true;
			
		} catch ( java.lang.NullPointerException somethingNotSet ) {
			
			System.out.println("Topology generation failed! something was Null that shouldn't have been.. has everything been setup correctly to render topology?");
			topologyGenerated = true;
			
		}
		
		//preserve the accurate topology szo it can be overwritten during the blend
		//accurateX = topologylayer.getAccurateX();
		//accurateY = topologylayer.getAccurateY();
		
		//readyToBlend = true;
		//keep alive while still needed
		//System.out.println("About to enter wait loop in topologyGenerationThread...");
		while (!destroy)
		{
			try {
				Thread.currentThread().sleep(1);
			} catch (InterruptedException e){
				//do muffin'
            }

			//if  ( (!readyToBlend) && (currentBlendDestination != null) )
			//{
			//	blendTopologyWithAlpha( currentBlendDestination, currentBlendAlpha );
			//	readyToBlend = true;
			//}
		}
		//System.out.println("About to exit run method in topologyGenerationThread.");
	}
	
	//this topology 'blend' mode doesn't work very well yet..
	public void cueTopologyAlphaBlend( Topology blendTarget , float blendAlpha)
	{
		currentBlendDestination = blendTarget;
		currentBlendAlpha = blendAlpha;
		readyToBlend = false;
	}
	
	//NOT WORKING YET
	/*
	public void blendTopologyWithAlpha( Topology writeHere, float blendAlpha )
	{
		TopologyGenerationSettings t = this.theSettings;
		//should wait here if the writeHere toplogy is currently being processed..
		//if this takes too long we could still see tearing.. hmm
		
		//topologylayer.newBlendedAccurateTopology( writeHere.getAccurateX(), writeHere.getAccurateY(), accurateX, accurateY,  blendAlpha );
		
		//writeHere.
		
		//render the float topology map into the faster integer-maps
		if ((t.topologygeneratormode != Topology.TopologyGeneratorMode.Plus) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.LifeNeightborhood) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.RandomIFS))
		{
			switch (t.topologyrendermode)
			{
				case DitheredMultilayer :
				{
					if ((t.topologygeneratormode != Topology.TopologyGeneratorMode.Fourier) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.Foo) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.PerspecitvePlane))
					{
						topologylayer.renderAccurateMultiTopology(t.numwarplayers,false);
					} else {
						topologylayer.renderAccurateMultiTopology(t.numwarplayers,true);
					}
					
					break;
				}
				case Topology.ChainMultilayer :
				{
					topologylayer.renderChainMultiTopology(t.numwarplayers,false, topologysquishvalue);
					break;
				}
				case Topology.RampMultilayer :
				{
					topologylayer.renderRampMultiTopology(t.numwarplayers,false, topologysquishvalue);
					break;
				}
				case Topology.AbsoluteDitheredMultilayer :
				{
					if ((t.topologygeneratormode != Topology.TopologyGeneratorMode.Fourier) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.Foo) && (t.topologygeneratormode != Topology.TopologyGeneratorMode.PerspectivePlane))
					{
						topologylayer.renderAccurateMultiTopology(t.numwarplayers,true);
					} else {
						topologylayer.renderAccurateMultiTopology(t.numwarplayers,false);
					}
					break;
				}
				case Topology.AbsoluteChainMulilayer :
				{
					topologylayer.renderChainMultiTopology(t.numwarplayers,true, topologysquishvalue);
					break;
				}
				case Topology.AbsoluteRampMultilayer :
				{
					topologylayer.renderRampMultiTopology(t.numwarplayers,true, topologysquishvalue);
					break;
				}
			}
		}
		writeHere.setInputArray( topologylayer.getInputs() );
	} */
	
	
	public void copyTopologyAndKillThread( Topology writeHere )
	{
		writeHere.setInputArray( topologylayer.getInputs() );
		destroy = true;
		//System.out.println("Set destroy to true");
	}
	
	public void killThread( )
	{
		destroy = true;
	}
}
