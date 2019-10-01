/**
 *
 * @author dan
 */

/**
 * Threading the topology-processing...
 * 
 * @author dan
 */

public final class TopologyProcessorThread extends Thread 
{
	public enum TopologyProcessorMode
	{
		Average,
		Contrasty,
		LumiDiff;
				
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( TopologyProcessorMode p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}
	
    Topology topo;
    int[] src;
    int[] dest;
    int startOffset;
    int scanLength;
    int nScans;
    int stride;
    int threadIndex;
    boolean terminate = false;
	
	TopologyProcessorMode currentMode = TopologyProcessorMode.Average;
	
    public Boolean isFinished;
    
    TopologyProcessorThread( Topology topoV, int[] srcV, int[] destV, int startOffsetV, int scanLengthV, int nScansV, int strideV, int threadIndexV )
    {
        topo = topoV;
        src = srcV;
        dest = destV;
        isFinished = new Boolean(true); // start off doing nothing
        startOffset = startOffsetV;
        scanLength = scanLengthV;
        nScans = nScansV;
        stride = strideV;
        threadIndex = threadIndexV;
        this.setPriority( Thread.MAX_PRIORITY ); 
    }
    
	public void setMode( TopologyProcessorMode toMode )
	{
		currentMode = toMode;
	}
	
    public void setTerminate( boolean setTerminate )
    {
        terminate = setTerminate;
    }
    
    // tells the thread to run again
    public synchronized void setUnFinished()
    {
        isFinished = new Boolean(false);
    }
    
    // Presumably we want to make sure this.isFinished is true first?
    // and maybe thread-safeness against these objects/arrays would be sensible?
    public void setTopologySrcAndDest( Topology toppo, int[] srcV, int[] destV )
    {
        long startTime = System.currentTimeMillis(); //fetch starting time thx2 Ankit Rustagi on stackExchange!
         
        // Wait a while, why?.. ultimately, half-a-second-later, give up yielding..
        while ( (!isFinished) && ( System.currentTimeMillis() - startTime ) < 500 )
        {
            this.yield();
        }
        // maybe should check we didn't time out above?
        // needs testing with actual failure scenario
        
        topo = toppo;
        src = srcV;
        dest = destV;
    }
    
    public static String repeatString(String s, int n) 
    {
        final StringBuilder sb = new StringBuilder();
        for(int i = 0; i < n; i++) 
        {
            sb.append(s);
        }
        return sb.toString();
    }
    
    public void run()
	{
        while(!terminate)
        {
            if (!isFinished)
            {
                // takes AGESSSS!?!??
                this.setPriority(Thread.MAX_PRIORITY); 
                                
                //System.out.println("Subsection:!" + startOffset);
                //if (threadIndex % 5 == 4 )
                //{
                //    System.out.println("thread run: " + repeatString( ">>", threadIndex ) );
                //}
				switch( currentMode )
				{
					case Average: {topo.nextFrameInlineBlendSubSection( src, dest, startOffset, scanLength, nScans, stride, threadIndex );break;}
					case Contrasty: {topo.nextFrameInlineContrastyBlendSubSection( src, dest, startOffset, scanLength, nScans, stride, threadIndex );break;}
					case LumiDiff: {topo.nextFrameInlineAnotherLumiDiffMixSubSection( src, dest, startOffset, scanLength, nScans, stride, threadIndex );break;}
				}
                isFinished = new Boolean(true);
                //if (threadIndex % 5 == 4 )
                //{
                //    System.out.println("thread end: " + repeatString( "<<", threadIndex ) );
                //}
            }
            synchronized( isFinished )
            {
                while ( isFinished )
                {
                    // check and only set when not already set!?
                    // this.setPriority(Thread.MIN_PRIORITY); 
                    // hang out waitin' to go again
                    try {
                        //this.sleep(0l,1);
                        //isFinished.wait();
                        //this.yield();
                        //this.wait( isFinished );
                        isFinished.wait();
                    } 
                    catch (InterruptedException e) 
                    {
                        //interrupt means go-again!
                        isFinished = new Boolean( false );
                    }

                    //this.yield();
                }
            }
        }
    }
}
