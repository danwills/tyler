public final class TopologyGenerationSettings
{
	public int topologyWidth;
	public int topologyHeight;
	public float currenttopologyparam;
	public boolean generatetopologymapped;
	public int numpoints;
	public float basePointX;
	public float basePointY;
	public float xs[];
	public float ys[];
	public float xmouse;
	public float ymouse;
	public double powers[];
	public double levels[];
	public int numwarplayers;
	public int booleanmatrixsize;
	public boolean[] booleanmatrix;
	
	public Topology.TopologyGeneratorMode topologygeneratormode;
	public Topology.TopologyRenderMode topologyrendermode;
	float extractormapf[];
	float topologysquishvalue;
	
	
	TopologyGenerationSettings( int w, int h, float tParam, float pX[], float pY[], float mouseX, float mouseY, double pow[], double lev[], boolean mapped, int nump, float bpX, float bpY, int layers, int matrixsize, boolean[] boolmatrix, Topology.TopologyGeneratorMode type, Topology.TopologyRenderMode rendermode, float extractor[], float topologysquish )
	{
		topologyWidth = w;
		topologyHeight = h;
		currenttopologyparam = tParam;
		generatetopologymapped = mapped;
		numpoints = nump;
		basePointX = bpX;
		basePointY = bpY;
		xs = pX;
		ys = pY;
		xmouse = mouseX;
		ymouse = mouseY;
		powers = pow;
		levels = lev;
		numwarplayers = layers;
		booleanmatrixsize = matrixsize;
		booleanmatrix = boolmatrix;
		topologygeneratormode = type;
		topologyrendermode = rendermode;
		extractormapf = extractor;
		topologysquishvalue = topologysquish;
	}
}