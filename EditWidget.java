import java.awt.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.lang.reflect.*;
//import TylerFileFilter;

//you have to be careful how you construct these widgets as they call the named method directly

// Allow persistant instances of this tool that have a little button that destroys them.
// When clicked on, capture and consume the mouseEvents until the 'input' session is finished, but don't destroy the node.
// Like entering the 'tool' mode of this widget (or node)

class EditWidget implements MouseListener, MouseMotionListener, KeyListener
{
	public enum widgetType
	{
		EnumValue,
		MenuValue,
		FloatValue,
		IntValue,
		BooleanValue,
		PointValue,
		PolarValue,
		NeighborhoodEdit,
		IntArrayValue,
		StringValue,
		Line,
		LStringEdit,
		StringChoice,
		NubValue,
		NoValue,
		BooleanMatrixValue;
		//dynamic enum.. uses a getList method or something to get the enum at construction time (these enums are fixed at compile time)
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( widgetType p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}
	
	/*public final static int CHOICE_WIDGET       = 0;
	public final static int FLOAT_WIDGET        = 1;
	public final static int INT_WIDGET          = 2;
	public final static int BOOLEAN_WIDGET      = 3;
	public final static int POINT_WIDGET        = 4;
	public final static int POLAR_WIDGET        = 5;
	public final static int NEIGHBORHOOD_WIDGET = 6;
	public final static int INT_ARRAY_WIDGET    = 7;
	public final static int STRING_WIDGET       = 8;
	public final static int LINE_WIDGET         = 9;
	public final static int L_STRING_WIDGET     = 10;
	public final static int STRING_CHOICE_WIDGET= 11;
	public final static int NUB_FUNCTION_WIDGET = 12;
	*/
	
	//added some ideas for more widget types.. :D
	//public final static int VECTOR_WIDGET        = 5;
	//	ND vector value widgetS
	//public final static int NUMBER_ICON_WIDGET = 12;
	//	?
	//public final static int COLOR_ARRAY_WIDGET = 13;
	//	packed pixel or float triplet color array - fair few drawmodes
	//public final static int FLOAT_ARRAY_WIDGET = 14;
	//	single channel raster wave with adjustable view and edit modes
	//public final static int VECTOR_ARRAY_WIDGET = 14;
	//	probably only 2D/3D initially.. arrowhead draw modes etc
	//public final static int TRANSLATE_WIDGET = 14;
	//	ND translation control
	//public final static int ROTATE_WIDGET = 14;
	//	ND (erm) rotation control
	//public final static int SCALE_WIDGET = 14;
	//	ND scaling control
	//public final static int EQSLIDERS_WIDGET = 14;
	//	 with that nice thing where you can drag accross the sliders to set them all with one click+drag action.
	//public final static int SAMPLEDISTRIBUTION_WIDGET = 14;
	//	convenient radial kernel-mungeing control
	//public final static int QUICKSKETCH_WIDGET = 14;
	//	a fast raster map and RGB[A] painting widget
	//public final static int MAPEXTRACTOR_WIDGET = 14;
	//	with some kind of preview of the map that will be extracted
	//public final static int SPRITEMAP_WIDGET = 14;
	//	ultimately have a pluggable node interface to this one so that ramp/lookup tables of the sprites can be completely proceduralized.
	//public final static int ROTOSHAPE_WIDGET = 14;
	//	(ab)use the AWT to do something nice 'ere .. tricky UI tho
	//public final static int WIDGETBUTTON_WIDGET = 14;
	//	this one might be able to be created from another widget while it is active, 
	//	it might have a couple of different modes so that the final value delivered in the 
	//	source widget can be either used (making a convenient preset-like button) or ignored - forcing re-edit of the widget value.
	//	these widgets could then kept on a history list, macro script or toolbox of some sort, for later re-use.
	//public final static int WIDGETNETWORK_WIDGET = 14;
	//	the mother of all widgets! :P an editable graph of nodes (other widgets and objects) and connections 
	
	//public final static int NUM_WIDGET_TYPES    = 12;
	
	public enum widgetDrawMode
	{
		Default,
		Hidden,
		BigFont,
		NoBackground,
		Radius;
		
		static String[] getNames()
		{
			String[] k = new String[ values().length ];
			int l = 0;
			for ( widgetDrawMode p : values() )
			{
				k[l] =  p.name();
				l++;
			}
			return k;
		}
	}
	
	//public final static int DRAW_DEFAULT    = 0;
	//public final static int DRAW_HIDDEN     = 1;
	//public final static int DRAW_BIGFONT    = 2;
	//public final static int DRAW_NOBG       = 3;
	//public final static int DRAW_RADIUS     = 4;
	
	//public final static int NUM_WIDGET_DRAWMODES    = 5;
	
	//public final static String[] widgetDrawModes = {"Default",
	//										        "Hidden",
	//										        "BigFont",
	//										        "No Background",
	//										        "Radius"};
	
	//public final static int SLIDER_WIDGET   = 6;
	//public final static int RADIUS_WIDGET   = 6;
	
	//public final static int GRADIENT_WIDGET = 5;
	//public final static int RULE_WIDGET = 5;
	//public final static int TOPOLOGY_WIDGET = 5;
	//public final static int ABSOLUTE_POINT_WIDGET = 5;
	//public final static int POLAR_WIDGET   = 6;
	//public final static int COLOR_WIDGET   = 7; //a java.awt.Color
	//public final static int MENU_WIDGET   = 7; //needs a method that accepts a MenuItem
	// menu draw: marking or traditional style
	// marking is dynamic nodetree struc with particle positions and connectors
	
	public class Polar
	{
		public double angle;
		public double radius;
		public Polar(double ang, double rad)
		{angle = ang; radius = rad;}
	}
	
	public class Neighborhood
	{
		//defines an (optionally weighted) square mask with an x center and a y center
		boolean weighted = false;
		boolean neighbormask[];
		int neighborweights[];
		int size, xaxis, yaxis;
		
		public Neighborhood() //not wise if you are planning to use the constructed object.. but may be a way to hack something
		{}
		
		public Neighborhood(int siz,int xoffset, int yoffset, boolean weight)
		{
			size = siz;
			neighbormask = new boolean[size*size];
			xaxis = size/2 + xoffset;
			yaxis = size/2 + yoffset;
			weighted = weight;
			if (weighted) neighborweights = new int[size*size];
		}
		
		public void setElement(int element, int weight)
		{
			element = (element >= 0) ? element % size*size : -element % size*size;
			if (weighted) 
			{
				neighbormask[element] = (weight != 0);
				neighborweights[element] = weight;
			} else { neighbormask[element] = (weight != 0);}
		}
		
		public void getVectors(int xsrc, int ysrc, int[] dest)
		{
			if (dest.length >= size*size)
			{
				for (int i = size*size-1; i >= 0; i--) //go backwards, its faster..
				{
					
				}
			}
		}
	}
	
	int chosen = -1;
	float floatvar = 0.0f;
	int intvar = 0;
	boolean boolvar = true;
	int neighborsize = 3;
	int fontpad = 5;
	
	int arraydata[];
	Point pointvar;
	Point pointvardest;
	Polar polarised;
	String widgetstring;
	FontMetrics fontmetwurst;
	
	boolean currentbooleanpaint = true;
	boolean[] booleanmatrix;
	int booleanmatrixsize = 7;
	
	float editscale = 40.0f;
	float resultscale = 1.0f;
	int border = 3;
	
	widgetType widgettype;
	Object[] widgetdata;
	String methodname;
	Method widgetmethod;
	Object methodowner;
	boolean gotmethod = false;
	boolean ready     = false; //for use when multiple widgets are being used to call 1 method...
	boolean delivered = false;
	boolean alive = false;
	boolean error = false;
	widgetDrawMode drawmode;
	int xloc;
	int yloc;
	long eventTime = 0;
	
	int choiceheight = 10;
	Rectangle boundbox;
	Rectangle widgetspace;
	Color normalcolor = Color.darkGray; //new Color(0.8f,0.8f,0.8f);
	Color unselectedcolor = new Color(0.1f,0.1f,0.1f);
	Color highlightcolor = Color.lightGray;
	Color selectedcolor = Color.white;
	Component eventsrc;
	
	static Vector<String> commandHistory;
	static boolean recording = false;
	
	static widgetDrawMode defaultdrawmode = widgetDrawMode.Default;
	
	Vector<EditWidget> childWidges;
	static int horizontalChildSpacing = 150;
	public static String lastlstring;
	public static String lastfilename;
	
	static Tree<String> menutree;
	static Tree<String> menuchosen;
	
	public EditWidget() //this constructor won't make a widget that will work.. its completely pointless.. and
	{
		alive = false; //definately false
	}
	
	public EditWidget( widgetType type, String method_name, Object methodclass, Object[] data, int x, int y, widgetDrawMode drawmodex, float resultscaling, Color normalcol, Color highlightcol, Color selectedcol, Component eventsource )
	{
		xloc = x;
		yloc = y;
		drawmode = drawmodex;
		resultscale = resultscaling;
		normalcolor = normalcol;
		highlightcolor = highlightcol;
		selectedcolor = selectedcol;
		widgettype = type;
		
		widgetdata = data;
		methodowner = methodclass;
		methodname = method_name;
		Class parms[] = new Class[1];
		eventsrc = eventsource;
		widgetspace = eventsrc.getBounds();
		
		pointvar = new Point(widgetspace.width/8,widgetspace.height/2);
		pointvardest = new Point(widgetspace.width - widgetspace.width/8,widgetspace.height/2);
		
		widgetstring = new String();
		eventTime = (new java.util.Date()).getTime();
		polarised = new Polar(0,0);
		menutree = new Tree( "" );
		menuchosen = new Tree( "" );
		
		switch ( widgettype )
		{
			case EnumValue :
			{
				parms[0] = Integer.TYPE;
				int w = 60;
				//need a way to assert or enforce that the names are there (and that data != null) for enum and string choice widgets
				if (data == null) 
				{
					data = new Object[1];
					data[0] = new String("Enum widget name error - names array was null!");
				}
				
				int h = data.length * choiceheight;
				boundbox = new Rectangle(x-w/2,y-h/2,w,h);
				break;
			}
			case MenuValue :
			{
				parms[0] = Integer.TYPE;
				int w = 60;
				if (data == null) 
				{
					data = new Object[1];
					data[0] = new String("Menu widget name error - names array was null!");
				}
				
				for (Object zitem : data)
				{
					String[] zplitstr = ((String)zitem).split("/");
					
					Tree currentRoot = menutree;
					
					for (String zplit : zplitstr )
					{
						Tree existingRoot = currentRoot.getTree( zplit );
						
						if (existingRoot != null ) 
						{
							currentRoot = existingRoot;
						} else {
							currentRoot = currentRoot.addLeaf( zplit );
						}
					}
					
				}
				
				//System.out.println( menutree.toString() );
				Collection<String> topLevel = menutree.getSuccessors( "" );
				//for (String lev : topLevel)
				//{
				//	System.out.println( lev );
				//}
				
				int h = topLevel.size() * choiceheight;
				boundbox = new Rectangle(x-w/2,y-h/2,w,h);
				break;
			}
			case FloatValue : 
			{
				parms[0] = Float.TYPE;
				if (data != null)
				{
					if (data.length > 0)
					{
						try {
							float v =( Float ) data[0];
							floatvar = v;
							//System.out.println("Successfully cast float initialiser value in editwidget constructor: " + v);
						} catch (ClassCastException ex ) {
							System.out.println("Could not cast float initialiser value in editwidget constructor.");
						}
					}
				}
				boundbox = new Rectangle((int)(x-editscale/2),(int)(y-editscale/2),(int)(editscale),(int)(editscale));
				break;
			}
			case IntValue  : 
			{
				parms[0] = Integer.TYPE;
				if (data != null)
				{
					if (data.length > 0)
					{
						try {
							int v =( Integer ) data[0];
							intvar = v;
							//System.out.println("Successfully cast int initialiser value in editwidget constructor: " + v);
						} catch (ClassCastException ex ) {
							System.out.println("Could not cast int initialiser value in editwidget constructor.");
						}
					}
				}
				boundbox = new Rectangle((int)(x-editscale/2),(int)(y-editscale/2),(int)(editscale),(int)(editscale));
				break;
			}
			case BooleanValue : 
			{
				parms[0] = Boolean.TYPE;
				if (data != null)
				{
					if (data.length > 0)
					{
						try {
							boolean v =( Boolean ) data[0];
							boolvar = v;
						} catch (ClassCastException ex ) {
							System.out.println("Could not cast boolean initialiser value in editwidget constructor.");
						}
					}
				}
				boundbox = new Rectangle((int)(x-editscale/2),(int)(y-editscale/2),(int)(editscale),(int)(editscale));
				break;
			}
			case PointValue   : 
			{
				parms[0] = (new Point(0,0)).getClass();
				if (data != null)
				{
					if (data.length > 0)
					{
						try {
							Point v =( Point ) data[0];
							pointvar = v;
						} catch (ClassCastException ex ) {
							System.out.println("could not cast point initialiser value in editwidget constructor.");
						}
					}
				}
				boundbox = new Rectangle((int)(x-editscale/2),(int)(y-editscale/2),(int)(editscale),(int)(editscale));
				break;
			}
			case PolarValue : 
			{
				parms[0] = polarised.getClass();
				
				if (data != null)
				{
					if (data.length > 0)
					{
						try {
							Polar v =( Polar ) data[0];
							polarised = v;
						} catch (ClassCastException ex ) {
							System.out.println("could not cast point initialiser value in editwidget constructor.");
						}
					}
				}
				break;
			}
			
			case NeighborhoodEdit : 
			{
				parms[0] = new Neighborhood().getClass();
				break;
			}
			case IntArrayValue        : 
			{
				arraydata = new int[(int)editscale];
				parms[0] = arraydata.getClass();
				break;
			}

			case StringValue:
			{
				parms[0] = (new String()).getClass();
				if (data != null)
				{
					if (data.length > 0)
					{
						try {
							String v =( String ) data[0];
							widgetstring = v;
						} catch (ClassCastException ex ) {
							System.out.println("could not cast string initialiser value in editwidget constructor.");
						}
					}
				}
				break;
			}
			case LStringEdit:
			{
				parms[0] = (new String()).getClass();
				if (data != null)
				{
					if (data.length > 0)
					{
						try {
							String v =( String ) data[0];
							widgetstring = v;
						} catch (ClassCastException ex ) {
							System.out.println("could not cast L-String initialiser value in editwidget constructor.");
						}
					}
				}
				break;
			}
			
			case StringChoice:
			{
				parms[0] = (new String()).getClass();
				int w = 60;
				if (data == null) 
				{
					data = new Object[1];
					data[0] = new String("StringChoice widget name error - names array was null!");
				}
				int h = data.length * choiceheight;
				boundbox = new Rectangle(x-w/2,y-h/2,w,h);
				break;
			}
			
			case NubValue:
			{
				parms[0] = (new String()).getClass();
				//create subwidgets..
				break;
			}
			
			case Line :
			{
				parms[0] = (new int[4]).getClass();
				boundbox = new Rectangle((int)(x-editscale/2),(int)(y-editscale/2),(int)(editscale),(int)(editscale));
				break;
			}
			
			case NoValue :
			{
				parms[0] = Void.TYPE;
				boundbox = new Rectangle((int)(x-editscale/2),(int)(y-editscale/2),(int)(editscale),(int)(editscale));
				break;
			}
			case BooleanMatrixValue:
			{
				//should allow specification of the entire matrix here
				//then make a method to save a booleanMatrixValue to an enum with a getMatrix for each enum - like a preset
				boolean gotmatrix = false;
				if (data != null)
				{
					try {
						if (data.length > 0) booleanmatrixsize = (Integer) data[0];
						if (data.length > 1)
						{
							booleanmatrix = ( boolean[] ) data[1];
							gotmatrix = true;
						}
					} catch ( ClassCastException ex ) { 
						System.out.println("could not cast point initialiser value in editwidget constructor.");
					}
				}
				
				if (!gotmatrix) booleanmatrix = new boolean[booleanmatrixsize * booleanmatrixsize];
				
				boundbox = new Rectangle((int)(x - editscale*2),(int)(y - editscale*2),(int)(editscale * 4),(int)(editscale * 4));
				Object p = new Object();
				parms[0] = p.getClass();//(Array. ).getClass();//booleanmatrix.getClass();
			}
			
			
		}
		
		try { widgetmethod = methodowner.getClass().getMethod(methodname,parms); }
		catch (NoSuchMethodException nsmx) { error = true; }// System.out.println("widget method " + methodname + "(" + parms[0].toString() + ") unexpectedly didn't exist, may have been static"); }
		catch (SecurityException sx) { error = true; System.out.println("Widget object method: " + methodname + "(" + parms[0].toString() + ") was unexpectedly private or protected."); }
		
		//try the backup plan, maybe the method was static, hey.. didn't think of that did ya?!? :)
		// the method might also not have any parameters at all, in which case a Void.TYPE is not the right thing to use..
		if (widgetmethod == null) 
		{
			//System.out.println("Maybe the method was static, attempting cast. .");
			try { widgetmethod = ((Class)methodowner).getMethod(methodname,parms);}
			catch (ClassCastException snobbery) { error = true; /*System.out.println("initial cast of widget method object failed.. trying alternatives...");*/}
			catch (NoSuchMethodException nsmx) { error = true; System.out.println("Widget static method  " + methodname + "( " + parms[0].toString() + " ) unexpectedly didn't exist."); }
			catch (SecurityException sx) { error = true; System.out.println("Widget method  " + methodname + "( " + parms[0].toString() + " ) was unexpectedly private or protected."); }
			
			try { 
				
				Method allPublicMethods[] = methodowner.getClass().getMethods();
				
				for (Method m : allPublicMethods) 
				{
					String mn = m.getName( );
					
					//System.out.println( "method: " + mn );
					
					Type[] tt = m.getGenericParameterTypes( );
					
					if ( tt.length == 0 )
					{
						if ( mn.equals( methodname ) )
						{
							widgetmethod = m;
							//System.out.println( "got method with no parameters: " + mn + "( ) OK" );
						}
						
						//System.out.println( "NO parms!! ");
					} else {
						if ( mn.equals( methodname ) )
						{
							//System.out.println( "found a method with the right name: " + mn + "( ).. hmm" );
							
							//int c = 1;
							//for (Type t: tt )
							//{
								//System.out.println( "parameter" + c + " is of type: " + t );
								//c++;
							//}
							//System.out.println( ". . . lets blithely assume that the caller knows what they are talking about and use this method to deliver the widget's result." );
							//let's assume the caller will call with the right parameters shall we? (HAH!!)
							widgetmethod = m;
						}
					}
				}
			}
			catch (ClassCastException snobbery) { error = true; System.out.println("Could not cast widget method object with no parameters.");}
			catch (SecurityException sx) { error = true; System.out.println("Widget method  " + methodname + "() was unexpectedly private or protected."); }
		}		
		
		if (widgetmethod != null) 
		{
			error = false;
			gotmethod = true;
			//System.out.println("successfully registered method to widget");
		}
		
		eventsrc.addMouseListener(this);
		eventsrc.addMouseMotionListener(this);
		eventsrc.addKeyListener(this);
		
		if (!error) alive = true;
		
	}
	
	public boolean isAlive()
	{return alive;}
	
	public void killWidget()
	{
		//kill childwidges first
		if (childWidges != null)
		{
			for (int p = 0; p < childWidges.size(); p++)
			{
				( (EditWidget)childWidges.elementAt(p) ).killWidget();
			}
		}
		
		if (alive) {
			eventsrc.removeMouseListener(this);
			eventsrc.removeMouseMotionListener(this);
			eventsrc.removeKeyListener(this);
		}
		alive = false;
	}
	
	private String getSlashSeperatedTree( Tree<String> tree )
	{
		String rootstr = "";
		String accumstr = "";
		
		Tree<String> nextTree = tree;
		Collection<String> nextLevel = tree.getSuccessors( rootstr );
		
		while ( nextLevel.size() > 0 )
		{
			String nextChoices[] = new String[ nextLevel.size() ];
			nextChoices = nextLevel.toArray( nextChoices );
			rootstr = nextChoices[0];
			String rootspl = rootstr.split(":")[0].trim();
			
			if (accumstr == "") accumstr = rootspl;
			else accumstr = accumstr + "/" + rootspl;
			
			nextTree = tree.getTree( rootstr );
			nextLevel = tree.getSuccessors( rootstr );
		}
		
		return accumstr;
	}
	
	//public void deliver(boolean final) //only record operation to commandhistory if it is final..
	public void deliver( boolean finaldelivery )
	{
		if (alive)
		{
			if ( childWidges != null )
			{
				for ( int p = 0; p < childWidges.size(); p++ )
				{
					( (EditWidget)childWidges.elementAt(p) ).deliver( finaldelivery );
				}
			}
			
			if ( gotmethod )
			{
				Object parms[] = new Object[1];
				try {
					switch ( widgettype )
					{
						case EnumValue:
						{
							if ((chosen >= 0) & (chosen < widgetdata.length)) parms[0] = new Integer(chosen);
							else parms[0] = new Integer((chosen < 0) ? 0 : ((chosen >= widgetdata.length) ? widgetdata.length-1 : chosen));
							break;
						}
						case MenuValue:
						{
							Collection<String> topLevel = menutree.getSuccessors( "" );
							
							String choices[] = new String[topLevel.size()];
							choices = topLevel.toArray( choices );
							
							//menuchosen
							
							//if ((chosen >= 0) & (chosen < choices.length)) parms[0] = new String( (String)choices[chosen] );
							//else parms[0] = new String( (chosen < 0) ? (String)choices[0] : ((chosen >= choices.length) ? (String)choices[choices.length-1] : (String)choices[chosen] ) );
							
							parms[0] = getSlashSeperatedTree( menuchosen );
							
							if ( finaldelivery ) menuchosen = new Tree( "" );
							break;
						}
						case FloatValue  : {parms[0] = new Float(floatvar); break;}
						case IntValue     : {parms[0] = new Integer(intvar); break;}
						case BooleanValue : {parms[0] = new Boolean(boolvar); break;}
						case PointValue   : {parms[0] = pointvar; break;}
						case StringValue  : {parms[0] = widgetstring; break;}
						case LStringEdit  : {parms[0] = widgetstring; break;}
						case Line : 
						{
							int[] aline = new int[4];
							aline[0] = pointvar.x;
							aline[1] = pointvar.y;
							aline[2] = pointvardest.x;
							aline[3] = pointvardest.y;
							
							parms[0] = aline; 
							break;
						}
						case StringChoice: { parms[0] = (String)widgetdata[chosen]; break; }
						case BooleanMatrixValue: { parms[0] = booleanmatrix; break; }
					}
					//if (parms[0] != null) 
					if (recording)
					{
						
						if (commandHistory == null) commandHistory = new Vector<String>();
					
						//if (methodowner == this.getClass()) {System.out.println("editWidget method call detected");}
						
						//dont record the 'start recording' or 'stop recording' commands
						//&& (methodowner != this.getClass())
						if (finaldelivery)
						{
							String command;
							boolean cancast = false;
							
							try {
								Class moclass = (Class) methodowner;
								System.out.println("cast success");
								cancast = true;
							} catch (ClassCastException clcx) { System.out.println("Could not cast methodowner to class Class"); }
							
							if ( methodowner != this.getClass() )//.getClass()  = .forName("Class"))
							{
								//try {
								//if (methodowner == 
								//Class poo = Class.forName("Class");
								//System.out.println("got Class Class object " + poo.getName());
								
								//{
								//	System.out.println("class Class detected: " + ((Class)methodowner).getName());
								//}
								///} catch (java.lang.ClassNotFoundException e) {System.out.println("could not get Class Class object");}
								if ( cancast ) command = ((Class)methodowner).getName() + "." + widgetmethod.getName() + "(";
								else command = methodowner.getClass().getName() + "." + widgetmethod.getName() + "(";	
							} else {
								
								command = ((Class)methodowner).getName() + "." + widgetmethod.getName() + "(";
							}
							
							
							for (int q = 0; q < parms.length; q++)
							{	
								command += parms[q].toString();
								if (q < parms.length-1) command += ",";
							}
							command = command + ");";
							
						
							commandHistory.add(command);
						}
						
						
					} else {
						if (commandHistory == null) commandHistory = new Vector<String>();
							
						if ((commandHistory.size() > 0) && (finaldelivery))
						{
							System.out.println("recorded commands:");
							System.out.println();

							int numlines = commandHistory.size();
							String cmdStrings[] = new String[numlines];

							for (int p = 0; p < numlines; p++)
							{
								System.out.println(commandHistory.elementAt(p).toString());
								cmdStrings[p] = commandHistory.elementAt(p).toString();
							}
							System.out.println();
							commandHistory.clear();


							GraphicsEnvironment Ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
							GraphicsDevice[] Gs = Ge.getScreenDevices();
							GraphicsConfiguration[] Gcs = Gs[0].getConfigurations();
							Frame aframe = new Frame(Gcs[0]);
							java.awt.FileDialog saveas = new java.awt.FileDialog(aframe,"Save Macro As:",FileDialog.SAVE);
							//saveas.setFilenameFilter(new FilenameFilter("*.
							//saveas.setDirectory();
							saveas.setVisible(true);

							if ( saveas.getFile() != null )
							{
								System.out.println("save as file chosen: " + saveas.getFile());
								try {

									File thefile = new File(saveas.getFile());
									FileWriter awriter = new FileWriter( thefile );
									System.out.println("got a fileWriter OK");
									//java.io.StringWriter swrite = new java.io.StringWriter(awriter);
									for (int p = 0; p < numlines; p++)
									{
										String cstring = cmdStrings[p];
										awriter.write(cstring,0,cstring.length());
										awriter.write('\n');
									}
									awriter.flush();
									awriter.close();
									System.out.println("macro file written of " + numlines + " lines");

								} catch (java.io.IOException ioxorz) { System.out.println("could not get a valid FileWriter to save with. " + ioxorz.toString()); }

							}
							//}
						}
					}
					
					if ( widgettype == widgetType.NoValue ) 
					{
						parms = null;
						//System.out.println("attempting to call NoValue method: " + widgetmethod.getName() );
						
						widgetmethod.invoke( methodowner );
						
					} else {
						widgetmethod.invoke( methodowner, parms );
					} 
					
				} catch (IllegalAccessException illax) { System.err.println("Widget method call failed with exception:" + illax.toString() ); }
				catch (InvocationTargetException invax) { System.err.println("Could not invoke widget method: " + widgetmethod.getName() + " on target object. " + invax.toString() );}
			}
		}
	}
	
	public static void setRecording(boolean towhat)
	{
		recording = towhat;
	}	
	
	public void setDrawMode(int towhat)
	{
		drawmode = widgetDrawMode.values()[towhat];
		System.out.println("drawmode set to: " + widgetDrawMode.getNames()[towhat] );
	}
	
	public void setHighlightColor(Color towhat)
	{
		highlightcolor = towhat;
	}
	
	public void setNormalColor(Color towhat)
	{
		normalcolor = towhat;
	}
	
	public void setSelectedColor(Color towhat)
	{
		selectedcolor = towhat;
	}
	
	public static void setDefaultDrawMode(int towhat)
	{
		defaultdrawmode = widgetDrawMode.values()[towhat];
		System.out.println("default drawmode set to: " + widgetDrawMode.getNames()[towhat] );
	}
	
	public static widgetDrawMode getDefaultDrawMode()
	{
		return defaultdrawmode;
	}
	
	public void setPosition( int xloc_pos, int yloc_pos )
	{
		xloc = xloc_pos;
		yloc = yloc_pos;
	}
	
	public float[] getPosition()
	{
		return new float[]{xloc, yloc};
	}
	
	public void mousePressed(MouseEvent m)
	{
		//System.out.println("mousePressed");
		if (alive)
		{
			int mods = m.getModifiers();
			int mousebutton = 0;
			if ((mods & InputEvent.BUTTON3_MASK) != 0) mousebutton = 3;
			if ((mods & InputEvent.BUTTON2_MASK) != 0) mousebutton = 2;
			if ((mods & InputEvent.BUTTON1_MASK) != 0) mousebutton = 1;

			switch(widgettype)
			{
				case Line :
				{
					if (mousebutton == 1)
					{
						pointvar.move(m.getX(),m.getY());
					} else { 
						pointvardest.move(m.getX(),m.getY());
					}
					break;
				}
				case BooleanMatrixValue:
				{
					//get position relative to center of widget
					int dx = (m.getX() - xloc);
					int dy = (m.getY() - yloc );

					//get cell dimensions
					int cellwidth = boundbox.width / booleanmatrixsize;
					int cellheight = boundbox.height / booleanmatrixsize;

					if (dx < 0) dx -= cellwidth;
					if (dy < 0) dy -= cellheight;

					//work out what cell  (signed, -1 0 1 )
					dx /= cellwidth;
					dy /= cellheight;

					//now we want the -1 to be 0, so add 1 -> ( 0, 1, 2)
					dx += booleanmatrixsize/2;
					dy += booleanmatrixsize/2;

					//clamp into the 3*3 matrix space
					dx = Math.min( booleanmatrixsize-1, Math.max(dx,0) );
					dy = Math.min( booleanmatrixsize-1, Math.max(dy,0) );

					//calculate the 1d address in the matrix
					int ix = dx + dy * booleanmatrixsize;

					booleanmatrix[ ix ] = !booleanmatrix[ ix ];
					currentbooleanpaint = booleanmatrix[ ix ];
					break;
				}
				case MenuValue : 
				{
					menuchosen = doMenuSelection( new Tree<String>(""), boundbox, "",  0, 0, new Point(m.getX(),m.getY() ) );
					break;
				}
			}
			if ( widgettype != widgetType.MenuValue ) deliver(false);
		}
		m.consume();
	}

	public void mouseReleased(MouseEvent m)
	{
		//System.out.println("mouseReleased");
		if ( alive )
		{
			int mods = m.getModifiers();
			int mousebutton = 0;
			if ((mods & InputEvent.BUTTON3_MASK) != 0) mousebutton = 3;
			if ((mods & InputEvent.BUTTON2_MASK) != 0) mousebutton = 2;
			if ((mods & InputEvent.BUTTON1_MASK) != 0) mousebutton = 1;
			boolean ctrl = ( (mods & InputEvent.CTRL_MASK) != 0 );
			boolean alt = ( (mods & InputEvent.ALT_MASK) != 0 );
			boolean shift = ( (mods & InputEvent.SHIFT_MASK) != 0 );

			if ( (widgettype == widgetType.Line) | (widgettype == widgetType.NeighborhoodEdit) | (widgettype == widgetType.IntArrayValue) | ( widgettype == widgetType.BooleanMatrixValue) )
			{

			} else { 
				if (ctrl & shift & alt)
				{
					deliver(true); 
				} else {
					
					if ( widgettype == widgetType.MenuValue )
					{
						menuchosen = doMenuSelection( new Tree<String>(""), boundbox, "",  0, 0, new Point(m.getX(),m.getY() ) );
					} else {
						deliver(true);
						killWidget();
					}
				}
			}
		}
		m.consume();
	}
	
	private int getMaxStringWidth( Collection<String> inCollection )
	{
		int maxwidth = 1;
		for ( String astr : inCollection )
		{
			int width = fontmetwurst.stringWidth( astr );
			if ( width > maxwidth ) maxwidth = width; 
		}
		return maxwidth;
	}
	
	private int getStringPosition( String[] inArray, String findStr )
	{
		for (int i = 0; i < inArray.length; i++)
		{
			if ( inArray[i].equals( findStr ) ) return i;
		}
		return -1;
	}
	
	private boolean rectContainsPtInX( Rectangle rect, Point pt )
	{
		//return ( pt.x > rect.x - rect.width/2 ) && ( pt.x < rect.x + rect.width/2 );
		return ( pt.x > rect.x ) && ( pt.x < rect.x + rect.width );
	}	
	
	private String getItemAtDepth( int depth, Tree<String> tree )
	{
		String rootstr = "";
		
		Tree<String> nextTree = tree;
		
		for (int i=0;i<=depth;i++)
		{
			Collection<String> nextLevel = tree.getSuccessors( rootstr );
			
			if ( nextLevel.size() > 0 )
			{
				String nextChoices[] = new String[ nextLevel.size() ];
				nextChoices = nextLevel.toArray( nextChoices );
				rootstr = nextChoices[0];
				nextTree = tree.getTree( rootstr );
			} else {
				//no more depth, just return the deepest? bollocks warning
				return rootstr;
			}
		}
		
		return rootstr;
	}
	
	private synchronized Tree<String> doMenuSelection( Tree<String> treeChoice, Rectangle parentbbox, String parentStr, int parentPosition, int depth, Point mouseloc )
	{
		if (fontmetwurst == null) return new Tree<String>( "" );
		
		Collection<String> nextLevel = menutree.getSuccessors( parentStr );
		
		String nextChoices[] = new String[ nextLevel.size() ];
		nextChoices = nextLevel.toArray( nextChoices );
				
		int choiceheight = fontmetwurst.getHeight() + border;
		int totalheight = (choiceheight * nextChoices.length) + border;
		
		int maxwidth = getMaxStringWidth( nextLevel ) + border*2;
		//int parentChosen = getStringPosition( nextChoices, parentStr );
		
		Rectangle bbox;
		
		if ( parentStr == "" )
		{
			bbox = new Rectangle( xloc-maxwidth/2, yloc-totalheight/2, maxwidth, totalheight );
		} else {
			//bbox = new Rectangle(parentbbox.x + parentbbox.width/2 + maxwidth/2, parentbbox.y - parentbbox.height/2 + (choiceheight * nextChoices.length)/2 + parentPosition * choiceheight, maxwidth, totalheight);
			bbox = new Rectangle( parentbbox.x + parentbbox.width, parentbbox.y + parentPosition * choiceheight, maxwidth, totalheight);
		}
		
		
		if ( nextChoices.length > 0 )
		{
			if ( rectContainsPtInX( bbox, mouseloc ) )
			{
				// System.out.println( "Pt is in rect, in X!" );
				// should prolly only work when within-range y-wise too!
				
				int ydown = mouseloc.y - bbox.y;
				int selection = ydown / choiceheight;
				//Collection<String> topLevel = menutree.getSuccessors( "" );

				if (selection < 0) chosen = 0;
				else if ( selection >= nextChoices.length ) selection = nextChoices.length-1;
				else chosen = selection;
				//else if (selection >= nextLevel.size()) selection = nextLevel.size()-1;

				//menuchosen = new Tree("");
				//String topItems[] = new String[ nextLevel.size() ];
				//topItems = nextLevel.toArray( topItems );
				if ( (chosen >= 0) & (chosen < nextChoices.length) )
				{
					String chosenStr = nextChoices[ chosen ];
					Tree<String> leafTree = treeChoice.addLeaf( chosenStr );
				}

			} else if ( mouseloc.x > ( bbox.x + bbox.width ) ) {
			
				//System.out.println( menuchosen );
				
				String selAtThisDepth = getItemAtDepth( depth, menuchosen );
				
				Tree<String> leafTree = treeChoice.addLeaf( selAtThisDepth );
				
				//System.out.println( "got sel at depth: " + depth + " = " + selAtThisDepth );
				int parentPos = getStringPosition( nextChoices, selAtThisDepth );
				//System.out.println( "parentpos: " + parentPos );
				if ( selAtThisDepth != parentStr )
				{
					treeChoice = doMenuSelection( leafTree, bbox, selAtThisDepth, parentPos, depth+1, mouseloc );
				}
			}
		}
		return treeChoice;
	}
	
	public synchronized void mouseMoved(MouseEvent m)
	{
		if (alive)
		{
			int mods = m.getModifiers();
			int mousebutton = 0;
			if ((mods & InputEvent.BUTTON3_MASK) != 0) mousebutton = 3;
			if ((mods & InputEvent.BUTTON2_MASK) != 0) mousebutton = 2;
			if ((mods & InputEvent.BUTTON1_MASK) != 0) mousebutton = 1;

			boolean ctrl = ( (mods & InputEvent.CTRL_MASK) != 0 );
			boolean alt = ( (mods & InputEvent.ALT_MASK) != 0 );
			boolean shift = ( (mods & InputEvent.SHIFT_MASK) != 0 );

			switch (widgettype)
			{
				case EnumValue  :
				{
					if ( boundbox.contains(new Point(m.getX(),m.getY())) )
					{
						int ydown = m.getY() - boundbox.y;
						int selection = ydown / choiceheight;
						if (selection < 0) chosen = 0; 
						else if (selection >= widgetdata.length) selection = widgetdata.length-1;
						else chosen = selection;
					}
					break;
				}
				case MenuValue :
				{
					menuchosen = doMenuSelection( new Tree<String>(""), boundbox, "",  0, 0, new Point(m.getX(),m.getY() ) );
					break;
				}
				case FloatValue   :
				{
					//for now...
					int dx = m.getX() - xloc;
					int dy = m.getY()-yloc;
					double dist = Math.sqrt(dx*dx+dy*dy)/100.0;
					floatvar = (float)(dist * resultscale);
					break;
				}
				case IntValue     :
				{
					int dx = m.getX() - xloc;
					int dy = m.getY()-yloc;
					double dist = Math.sqrt(dx*dx+dy*dy);
					intvar = (int) (dist * 2.0 * resultscale);
					break;
				}

				case BooleanValue :
				{
					int dx = m.getX() - xloc;
					int dy = m.getY()-yloc;
					double dist = Math.sqrt(dx*dx+dy*dy);
					if (dist < boundbox.width/2) boolvar = true; else boolvar = false;
					break;
				}
				case NoValue :
				{
					int dx = m.getX() - xloc;
					int dy = m.getY()-yloc;
					double dist = Math.sqrt(dx*dx+dy*dy);
					if (dist < boundbox.width/2) boolvar = true; else boolvar = false;
					break;
				}
				case PointValue   :
				{
					pointvar.move(m.getX() - xloc,m.getY()-yloc);
					break;
				}
				case StringChoice  :
				{
					if ( boundbox.contains(new Point(m.getX(),m.getY())) )
					{
						int ydown = m.getY() - boundbox.y;
						int selection = ydown / choiceheight;
						if (widgetdata != null)
						{
							if (selection < 0) chosen = 0; 
							else if (selection >= widgetdata.length) selection = widgetdata.length-1;
							else chosen = selection;
							widgetstring = (String)widgetdata[chosen];
						}
					}
					break;
				}
				case BooleanMatrixValue :
				{
					int dx = m.getX() - xloc;
					int dy = m.getY() - yloc;
					//if ctrl, shift and alt are down, 
					// if the mousebutton is 1, put 1's in the matrix
					// if the mousebutton is 2, put 0's in the matrix
					// if the mousebutton is 3, toggle the values in the matrix
					break;
				}

			}

			//lazy mode.. so u don't haaaave to click if you dont want to ;)
			if ( ctrl & alt & shift ) deliver(false);
		}
		m.consume();
	}
	
	public void mouseEntered(MouseEvent m){}
	public void mouseExited(MouseEvent m){}
	
	public void mouseDragged(MouseEvent m)
	{
		if (alive)
		{
			int mods = m.getModifiers();
			int mousebutton = 0;
			if ((mods & InputEvent.BUTTON3_MASK) != 0) mousebutton = 3;
			if ((mods & InputEvent.BUTTON2_MASK) != 0) mousebutton = 2;
			if ((mods & InputEvent.BUTTON1_MASK) != 0) mousebutton = 1;
			boolean ctrl = ( (mods & InputEvent.CTRL_MASK) != 0 );
			boolean alt = ( (mods & InputEvent.ALT_MASK) != 0 );
			boolean shift = ( (mods & InputEvent.SHIFT_MASK) != 0 );

			switch (widgettype)
			{
				case EnumValue  :
				{
					if ( boundbox.contains(new Point(m.getX(),m.getY())) )
					{
						int ydown = m.getY() - boundbox.y;
						int selection = ydown / choiceheight;
						if (selection < 0) chosen = 0; 
						else if (selection >= widgetdata.length) selection = widgetdata.length-1;
						else chosen = selection;
					}
					break;
				}
				case MenuValue  :
				{
					menuchosen = doMenuSelection( new Tree<String>(""), boundbox, "",  0, 0, new Point(m.getX(),m.getY() ) );
					break;
				}
				case FloatValue   :
				{
					//for now...
					int dx = m.getX() - xloc;
					int dy = m.getY()-yloc;
					double dist = Math.sqrt(dx*dx+dy*dy)/100.0;
					if (mousebutton == 1) floatvar = (float)( dist *resultscale );
					else floatvar = -(float)( dist * resultscale );
					break;
				}
				case IntValue     :
				{
					int dx = m.getX() - xloc;
					int dy = m.getY() - yloc;
					double dist = Math.sqrt(dx*dx+dy*dy);
					if (mousebutton == 1) intvar = (int)( dist * resultscale );
					else intvar = (int) ( -dist * resultscale );
					break;
				}

				case BooleanValue :
				{
					int dx = m.getX() - xloc;
					int dy = m.getY() - yloc;
					double dist = Math.sqrt(dx*dx+dy*dy);
					if (mousebutton == 1) 
					{
						if (dist < boundbox.width/2) boolvar = true; else boolvar = false;
					} else {
						if (dist < boundbox.width/2) boolvar = false; else boolvar = true;
					}
					break;
				}

				case NoValue :
				{
					int dx = m.getX() - xloc;
					int dy = m.getY() - yloc;
					double dist = Math.sqrt(dx*dx+dy*dy);
					if (mousebutton == 1) 
					{
						if (dist < boundbox.width/2) boolvar = true; else boolvar = false;
					} else {
						if (dist < boundbox.width/2) boolvar = false; else boolvar = true;
					}
					break;
				}

				case PointValue   :
				{
					if ( ctrl ) 
					{
						//relative to widget
						if (mousebutton == 1) pointvar.move(xloc - m.getX(),yloc - m.getY()); 
						//relative to center of applet
						else pointvar.move((widgetspace.width/2)-m.getX(),(widgetspace.height/2)-m.getY()); 
					} else {
						if (mousebutton == 1)
						{
							//absolute and relative to widget
							int xco = m.getX() - xloc;
							int yco = m.getY() - yloc;
							xco = (xco < 0) ? -xco : xco;
							yco = (yco < 0) ? -yco : yco;
							pointvar.move(xco,yco); 
						//absolute and relative to center of applet
						} else { 
							int xco = (widgetspace.width/2)  - m.getX();
							int yco = (widgetspace.height/2) - m.getY();
							xco = (xco < 0) ? -xco : xco;
							yco = (yco < 0) ? -yco : yco;
							pointvar.move(xco,yco); 
						}
					}
					break;
				}
				case Line :
				{
					if ( ctrl ) 
					{
						if (mousebutton == 1) //move whole line relative to src
						{
							int xdelta = pointvardest.x-pointvar.x;
							int ydelta = pointvardest.x-pointvar.x;
							pointvar.move(m.getX(),m.getY());
							pointvardest.move(m.getX()+xdelta,m.getY()+ydelta);
						} else { //move whole line relative to dest
							int xdelta = pointvar.x-pointvardest.x;
							int ydelta = pointvar.x-pointvardest.x;
							pointvardest.move(m.getX(),m.getY());
							pointvar.move(m.getX()+xdelta,m.getY()+ydelta);
						}
					} else {
						if (mousebutton == 1) 
						{
							pointvar.move(m.getX(),m.getY());
						} else { 
							pointvardest.move(m.getX(),m.getY());
						}
					}
					break;
				}
				case StringChoice  :
				{
					if ( boundbox.contains(new Point(m.getX(),m.getY())) )
					{
						int ydown = m.getY() - boundbox.y;
						int selection = ydown / choiceheight;

						if (widgetdata != null)
						{
							if (selection < 0) chosen = 0; 
							else if (selection >= widgetdata.length) selection = widgetdata.length-1;
							else chosen = selection;
							widgetstring = (String)widgetdata[chosen];
						}
					}
					break;
				}
				case BooleanMatrixValue:
				{
					//get position relative to center of widget
					int dx = (m.getX() - xloc);
					int dy = (m.getY() - yloc );

					//get cell dimensions
					int cellwidth = boundbox.width / booleanmatrixsize;
					int cellheight = boundbox.height / booleanmatrixsize;

					if (dx < 0) dx -= cellwidth;
					if (dy < 0) dy -= cellheight;

					//work out what cell  (signed, -1 0 1 )
					dx /= cellwidth;
					dy /= cellheight;

					//now we want the -1 to be 0, so add 1 -> ( 0, 1, 2)
					dx += booleanmatrixsize/2;
					dy += booleanmatrixsize/2;

					//clamp into the 3*3 matrix space
					dx = Math.min(booleanmatrixsize-1, Math.max(dx,0));
					dy = Math.min(booleanmatrixsize-1, Math.max(dy,0));

					//calculate the 1d address in the matrix
					int ix = dx + dy * booleanmatrixsize;

					booleanmatrix[ ix ] = currentbooleanpaint;
					break;
				}
			}
			if ( widgettype != widgetType.MenuValue ) deliver(false); //with mouse down message is always delivered
		}
		m.consume();
	}       
	        
	public void mouseClicked(MouseEvent m)
	{
		System.out.println("mouseClicked");
		if ( alive )
		{
			int mods = m.getModifiers();
			int mousebutton = 0;
			if ((mods & InputEvent.BUTTON3_MASK) != 0) mousebutton = 3;
			if ((mods & InputEvent.BUTTON2_MASK) != 0) mousebutton = 2;
			if ((mods & InputEvent.BUTTON1_MASK) != 0) mousebutton = 1;

			System.out.println("mouseButton " + mousebutton);
			//if (mousebutton != 1) {killWidget(); return;}
			
			System.out.println("widgettype " + widgettype);
			System.out.println("MenuValue " + widgettype.MenuValue);
			switch ( widgettype )
			{
				case EnumValue:
				{
					if (mousebutton == 1)
					{
						//if ((mods & InputEvent.CTRL_MASK) != 0) {chosen -= 1; if (chosen < 0) chosen += widgetdata.length;}
						//else { chosen = (chosen + 1) % widgetdata.length;}
						if ((chosen >= 0) & (chosen < widgetdata.length)) 
						deliver(true);
						killWidget();
					} else { killWidget(); }
					break;
				}
				case MenuValue:
				{
					System.out.println("MenuValue");
					if (mousebutton == 1)
					{
						System.out.println("mb1");
						//if ((mods & InputEvent.CTRL_MASK) != 0) {chosen -= 1; if (chosen < 0) chosen += widgetdata.length;}
						//else { chosen = (chosen + 1) % widgetdata.length;}
						//Collection<String> topLevel = menutree.getSuccessors( "" );
						//if ((chosen >= 0) & (chosen < topLevel.size())) 
						menuchosen = doMenuSelection( new Tree<String>(""), boundbox, "",  0, 0, new Point(m.getX(),m.getY() ) );
						deliver( true );
						killWidget();
					} /* else { killWidget(); }*/
					break;
				}
				case FloatValue  : 
				case IntValue     : 
				case BooleanValue : 
				case PointValue   : 
				case StringValue   : 
				case LStringEdit   : 
				{
					deliver(true);
					killWidget();
					break;
				}
				case NoValue :
				{
					if (boolvar)
					{
						deliver(true);
					}
					break;
				}
				case StringChoice:
				{
					if (mousebutton == 1)
					{
						//if ((mods & InputEvent.CTRL_MASK) != 0) {chosen -= 1; if (chosen < 0) chosen += widgetdata.length;}
						//else { chosen = (chosen + 1) % widgetdata.length;}
						if ((chosen >= 0) & (chosen < widgetdata.length))
						widgetstring = (String)widgetdata[chosen];
						deliver(true);
						killWidget();
					} else { killWidget(); }
					break;
				}
			}
		}
		m.consume();
	}
	
	public void keyPressed(KeyEvent k)
	{
		if (alive)
		{
			int keycode = k.getKeyCode();
			int mods = k.getModifiers();
			boolean ctrl = ( (mods & InputEvent.CTRL_MASK) != 0 );
			boolean alt = ( (mods & InputEvent.ALT_MASK) != 0 );
			boolean shift = ( (mods & InputEvent.SHIFT_MASK) != 0 );

			switch (keycode)
			{	
				case KeyEvent.VK_M:
				{
					//switch drawmode -> ctrl-m (or by widget (+shift))
					if (ctrl) 
					{
						if (shift) 
						{
							if (childWidges == null) childWidges = new Vector<EditWidget>();
							childWidges.add(new EditWidget(widgetType.EnumValue,"setDrawMode",this,
							(Object[]) widgetDrawMode.getNames(), xloc + horizontalChildSpacing, yloc, drawmode, resultscale, normalcolor, highlightcolor, selectedcolor, eventsrc) );

						} else {
							//drawmode = (drawmode+1) % NUM_WIDGET_DRAWMODES;
							//System.out.println("drawmode is now: "  + drawmode + ".");
						}
					}
					break;
				}
				case KeyEvent.VK_N:
				{
					//switch default drawmode -> ctrl-n
					if (ctrl) 
					{
						if (shift) 
						{
							//defaultdrawmode = (defaultdrawmode+1) % NUM_WIDGET_DRAWMODES;
							//System.out.println("widget class defaultDrawMode is now: "  + drawmode + ".");

						} else {

							if (childWidges == null) childWidges = new Vector<EditWidget>();
							childWidges.add(new EditWidget(widgetType.EnumValue,"setDefaultDrawMode",this,
							(Object[]) widgetDrawMode.getNames(), xloc + horizontalChildSpacing, yloc, drawmode, resultscale, normalcolor, highlightcolor, selectedcolor, eventsrc) );

						}
					}
					break;
				}

			}

			switch (  widgettype )
			{
				case FloatValue:
				{
					switch (keycode)
					{
						case KeyEvent.VK_DOWN : 
						{ 
							if (ctrl) 
							{
								if (floatvar != 0) floatvar = (float)Math.sqrt(floatvar);
							} else floatvar -= floatvar/2;
							break;		
						}

						case KeyEvent.VK_UP   : 
						{ 
							if (ctrl) floatvar *= floatvar;
							else  floatvar += floatvar/2;
							break;
						}

						case KeyEvent.VK_RIGHT   : 
						{ 
							if (ctrl) resultscale *= 2;
							else resultscale += 1;
							break;
						}

						case KeyEvent.VK_LEFT   : 
						{ 
							if (ctrl) resultscale /= 2;
							else resultscale -= 1;
							break;
						}
						case KeyEvent.VK_1    : {if (shift) floatvar = 1.0f;  else floatvar = 0.1f; break;}
						case KeyEvent.VK_2    : {if (shift) floatvar = 2.0f;  else floatvar = 0.2f; break;}
						case KeyEvent.VK_3    : {if (shift) floatvar = 3.0f;  else floatvar = 0.3f; break;}
						case KeyEvent.VK_4    : {if (shift) floatvar = 4.0f;  else floatvar = 0.4f; break;}
						case KeyEvent.VK_5    : {if (shift) floatvar = 5.0f;  else floatvar = 0.5f; break;}
						case KeyEvent.VK_6    : {if (shift) floatvar = 6.0f;  else floatvar = 0.6f; break;}
						case KeyEvent.VK_7    : {if (shift) floatvar = 7.0f;  else floatvar = 0.7f; break;}
						case KeyEvent.VK_8    : {if (shift) floatvar = 8.0f;  else floatvar = 0.8f; break;}
						case KeyEvent.VK_9    : {if (shift) floatvar = 9.0f;  else floatvar = 0.9f; break;}
						case KeyEvent.VK_0    : {if (shift) floatvar = 10.0f; else floatvar = 0.0f; break;}
						case KeyEvent.VK_MINUS    : { floatvar = -floatvar; break;}					
						case KeyEvent.VK_INSERT    : { floatvar += 0.01f; break;}
						case KeyEvent.VK_DELETE    : { floatvar -= 0.01f; break;}
						case KeyEvent.VK_HOME      : { floatvar += 0.1f;  break;}
						case KeyEvent.VK_END       : { floatvar -= 0.1f;  break;}
						case KeyEvent.VK_PAGE_UP   : { if (ctrl) floatvar += 10.0f; else floatvar += 1.0f; break;}
						case KeyEvent.VK_PAGE_DOWN : { if (ctrl) floatvar -= 10.0f; else floatvar -= 1.0f; break;}
					} //end float's switch on keycode
					break;
				}

				case IntValue:
				{
					switch(keycode)
					{	
						case KeyEvent.VK_1    : {if (shift) intvar = 10;  else intvar = 1; break;}
						case KeyEvent.VK_2    : {if (shift) intvar = 20;  else intvar = 2; break;}
						case KeyEvent.VK_3    : {if (shift) intvar = 30;  else intvar = 3; break;}
						case KeyEvent.VK_4    : {if (shift) intvar = 40;  else intvar = 4; break;}
						case KeyEvent.VK_5    : {if (shift) intvar = 50;  else intvar = 5; break;}
						case KeyEvent.VK_6    : {if (shift) intvar = 60;  else intvar = 6; break;}
						case KeyEvent.VK_7    : {if (shift) intvar = 70;  else intvar = 7; break;}
						case KeyEvent.VK_8    : {if (shift) intvar = 80;  else intvar = 8; break;}
						case KeyEvent.VK_9    : {if (shift) intvar = 90;  else intvar = 9; break;}
						case KeyEvent.VK_0    : {if (shift) intvar = 100; else intvar = 0; break;}
						case KeyEvent.VK_MINUS    : { intvar = -intvar; break;}
						case KeyEvent.VK_INSERT    : { intvar += 1; break;}
						case KeyEvent.VK_DELETE    : { intvar -= 1; break;}
						case KeyEvent.VK_HOME      : { intvar += 10; break;}
						case KeyEvent.VK_END       : { intvar -= 10; break;}
						case KeyEvent.VK_PAGE_UP   : { intvar += 100; break;}
						case KeyEvent.VK_PAGE_DOWN : { intvar -= 100; break;}
						case KeyEvent.VK_ENTER : {deliver(true); killWidget(); break;}

						case KeyEvent.VK_DOWN : 
						{
							if (ctrl) { if (intvar != 0) floatvar = (int)Math.sqrt(floatvar); }
							else intvar -= intvar/2;
							break;
						}
						case KeyEvent.VK_UP   :
						{ 
							if (ctrl) intvar *= intvar;
							else intvar += intvar/2;
							break;
						}


						case KeyEvent.VK_RIGHT   : 
						{ 
							if (ctrl) resultscale *= 2;
							else resultscale += 1;
							break;
						}

						case KeyEvent.VK_LEFT   : 
						{ 
							if (ctrl) resultscale /= 2;
							else resultscale -= 1;
							break;
						}
					} // end int's switch on keycode
					break;
				}

				case StringChoice :
				{
					if (widgetdata != null) switch (keycode)
					{
						case KeyEvent.VK_UP : {chosen = ((chosen - 1) < 0) ? (widgetdata.length - 1) : (chosen - 1);  break;}
						case KeyEvent.VK_DOWN : {chosen = (chosen + 1) % widgetdata.length; break;}
						case KeyEvent.VK_PAGE_UP : {chosen = 0; break;}
						case KeyEvent.VK_PAGE_DOWN : {chosen = widgetdata.length - 1; break;}
						case KeyEvent.VK_ENTER : {deliver(false); break;}
					}
					break;
				}

				case EnumValue :
				{
					if (widgetdata != null) switch (keycode)
					{
						case KeyEvent.VK_UP : {chosen = ((chosen - 1) < 0) ? (widgetdata.length - 1) : (chosen - 1);  break;}
						case KeyEvent.VK_DOWN : {chosen = (chosen + 1) % widgetdata.length; break;}
						case KeyEvent.VK_PAGE_UP : {chosen = 0; break;}
						case KeyEvent.VK_PAGE_DOWN : {chosen = widgetdata.length - 1; break;}
						case KeyEvent.VK_ENTER : {deliver(false); break;}
					}
				}

				case MenuValue :
				{
					Collection<String> topLevel = menutree.getSuccessors( "" );

					if (widgetdata != null) switch (keycode)
					{
						case KeyEvent.VK_UP : {chosen = ((chosen - 1) < 0) ? (topLevel.size() - 1) : (chosen - 1);  break;}
						case KeyEvent.VK_DOWN : {chosen = (chosen + 1) % topLevel.size(); break;}
						case KeyEvent.VK_PAGE_UP : {chosen = 0; break;}
						case KeyEvent.VK_PAGE_DOWN : {chosen = topLevel.size() - 1; break;}
						//case KeyEvent.VK_ENTER : {deliver(true); break;}
					}
				}

				case PointValue :
				{
					switch (keycode)
					{
						case KeyEvent.VK_UP :    {pointvar.translate(  0,-10); break;}
						case KeyEvent.VK_DOWN :  {pointvar.translate(  0, 10); break;}
						case KeyEvent.VK_LEFT :  {pointvar.translate(-10,  0); break;}
						case KeyEvent.VK_RIGHT : {pointvar.translate( 10,  0); break;}
						case KeyEvent.VK_ENTER : {deliver(false); break;}
					}
				break;
				}
				case BooleanValue:
				{
					switch (keycode)
					{
						case KeyEvent.VK_ENTER : {deliver(false); break;}
						default : {boolvar = !boolvar; break;}
					}
					break;
				}
				case StringValue:
				case LStringEdit:
				{
					switch (keycode)
					{
						case KeyEvent.VK_ENTER : {deliver(false); break;}
						case KeyEvent.VK_BACK_SPACE : 
						{
							if (widgetstring.length() > 0) 
							{
								String nustring = widgetstring.substring(0,widgetstring.length()-1); 
								widgetstring = new String(nustring);
								break;
							}
						}
						case KeyEvent.VK_L:
						{
							if (ctrl)
							{
								if (shift)
								{
									System.out.println("loading l-string from file...");
									if (childWidges == null) childWidges = new Vector<EditWidget>();
									String[] lPresetFileList = getLFileList();
									childWidges.add(new EditWidget(widgetType.StringChoice,"setLStringFromFile",this,
									(Object[]) lPresetFileList, xloc + horizontalChildSpacing, yloc, drawmode, resultscale, normalcolor, highlightcolor, selectedcolor, eventsrc) );
								}
							}
							break;
						}
					}
					break;
				}	
			}
		}
		k.consume();
	}           
	
	public void keyReleased(KeyEvent k)
	{
		if (alive)
		{
			int keycode = k.getKeyCode();
			int mods = k.getModifiers();
			boolean ctrl = ( (mods & InputEvent.CTRL_MASK) != 0 );
			boolean alt = ( (mods & InputEvent.ALT_MASK) != 0 );
			boolean shift = ( (mods & InputEvent.SHIFT_MASK) != 0 );

			switch (keycode)
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
					killWidget(); 
					break;
				}

				//case KeyEvent.VK_1    : {if (shift) intvar = 10;  else intvar = 1; break;}
				//case KeyEvent.VK_2    : {if (shift) intvar = 20;  else intvar = 2; break;}
				//case KeyEvent.VK_3    : {if (shift) intvar = 30;  else intvar = 3; break;}
				//case KeyEvent.VK_4    : {if (shift) intvar = 40;  else intvar = 4; break;}
				//case KeyEvent.VK_5    : {if (shift) intvar = 50;  else intvar = 5; break;}
				//case KeyEvent.VK_6    : {if (shift) intvar = 60;  else intvar = 6; break;}
				//case KeyEvent.VK_7    : {if (shift) intvar = 70;  else intvar = 7; break;}
				//case KeyEvent.VK_8    : {if (shift) intvar = 80;  else intvar = 8; break;}
				//case KeyEvent.VK_9    : {if (shift) intvar = 90;  else intvar = 9; break;}
				//case KeyEvent.VK_0    : {if (shift) intvar = 100; else intvar = 0; break;}

				case KeyEvent.VK_MINUS    : { if (shift) deliver(false); break;}
				case KeyEvent.VK_INSERT    : { if (shift) deliver(false); break;}
				case KeyEvent.VK_DELETE    : { if (shift) deliver(false); break;}
				case KeyEvent.VK_HOME      : { if (shift) deliver(false);; break;}
				case KeyEvent.VK_END       : { if (shift) deliver(false);; break;}
				case KeyEvent.VK_PAGE_UP   : { if (shift) deliver(false); break;}
				case KeyEvent.VK_PAGE_DOWN : { if (shift) deliver(false); break;}

				//case KeyEvent.VK_ENTER : {deliver(true); killWidget(); break;}

				case KeyEvent.VK_DOWN : 
				{
					if (shift) deliver(false);
					break;
				}
				case KeyEvent.VK_UP   :
				{ 
					if (shift) deliver(false);
					break;
				}


				case KeyEvent.VK_RIGHT   : 
				{ 
					if (shift) deliver(false);
					break;
				}

				case KeyEvent.VK_LEFT   : 
				{ 
					if (shift) deliver(false);
					break;
				}
			}
		}
		k.consume();
	}
	
	public void keyTyped(KeyEvent k)
	{
		if (alive)
		{
			char c = k.getKeyChar();
			char[] cArr = {c};
			String cStr = new String(cArr);
			int mods = k.getModifiers();

			boolean ctrl = ( (mods & InputEvent.CTRL_MASK) != 0 );
			boolean alt = ( (mods & InputEvent.ALT_MASK) != 0 );
			boolean shift = ( (mods & InputEvent.SHIFT_MASK) != 0 );

			switch(widgettype)
			{
				case LStringEdit:
				case StringValue:
				{
					//if (!k.isActionKey()) 

					//need a way to avoid getting the key that created the widget in here... tis a bit of an issu
					if ( !alt & !ctrl )
					{
						if ( (Character.isDefined(c)) & (!Character.isISOControl(c)) ) widgetstring = widgetstring + c;
					}

					break;
				}
			}
		}
		k.consume();
	}
	
	public void setLStringFromFile(String filename)
	{
		if (lastfilename != null) 
		{
			//System.out.println("filename:" + filename + " lastfilename: " + lastfilename + " lastLString: " + lastlstring);
			
			//if (lastfilename.equals(filename)) System.out.println("cached string is equal.");
			
			if ((lastfilename.equals(filename)) && (lastlstring != null))
			{
				System.out.println("cached lstring load for string:" + lastlstring);
				widgetstring = lastlstring;
				return;
			}
		}
			
		FileReader lStringFileReader = null;
		LineNumberReader lineReader = null;
		String lnames[] = null;
		int numlines = 0;
		
		try
		{
			lStringFileReader = new FileReader("LStrings/" + filename);
			lineReader = new LineNumberReader(lStringFileReader);
		}
		catch ( java.io.FileNotFoundException ex) { System.err.println("could not load tyler LString file:" + filename); }
		
		
		if (lineReader != null)
		{
			String aline = null;
			
			try {
				
				aline = lineReader.readLine();
				numlines = 1;
				
					//process only one liners for now
					//haahahahahahaaaa
	
				if (aline != null)
				{
					widgetstring = aline;	
					EditWidget.lastlstring = aline;
					EditWidget.lastfilename = filename;
					
					System.out.println("read lstring from file: " + aline);
					aline = lineReader.readLine(); 
					numlines++;
				}
			} catch (IOException xzor) { System.err.println("IOError while loading tyler L-System string file."); }
		}
	}
	
	public String[] getLFileList()
	{
		FileFilter LFilter = (FileFilter) new TylerFileFilter(new String[] {"L","l"},"tyler L-System string file.");
		File[] filelist = ( new File("./LStrings/") ).listFiles(LFilter);
		String[] namelist = new String[filelist.length];
		for (int i = 0; i < filelist.length; i++)
		{
			namelist[i] = filelist[i].getName();
			System.out.println("noticed L-StringFile called:" + namelist[i]);
		}
		return namelist;
	}
	
	public void drawMenuTree( Graphics2D g, Rectangle parentbbox, String parentStr, int parentPosition, int xloc, int yloc )
	{
		Collection<String> nextLevel = menutree.getSuccessors( parentStr );

		String nextChoices[] = new String[ nextLevel.size() ];
		nextChoices = nextLevel.toArray( nextChoices );
		
		int choiceheight = fontmetwurst.getHeight() + border;
		int totalheight = (choiceheight * nextChoices.length) + border;
		int maxwidth = 1;
		int parentChosen = 0;
		
		String menuitem[] = new String[ 1 ];
		Collection<String> successors = menuchosen.getSuccessors( parentStr );
		
		if ( successors.size() > 0 )
		{
			menuitem = successors.toArray( menuitem );
		} else {
			menuitem[0] = "";
		}
		
		for (int i = 0; i < nextChoices.length; i++)
		{
			if ( fontmetwurst.stringWidth( nextChoices[i] ) + border*2 > maxwidth) 
			{
				maxwidth = fontmetwurst.stringWidth( nextChoices[i] ) + border*2; 
			}
			
			//System.out.println(  nextChoices[i] + " mi: " + menuitem[0] + " i: " + i );
			if ( nextChoices[i].equals( menuitem[0] ) ) parentChosen = i;
		}
		
		maxwidth += border*2;
		Rectangle bbox;
		
		if (parentStr == "" )
		{
			bbox = new Rectangle(xloc-maxwidth/2,yloc-totalheight/2,maxwidth,totalheight);
			boundbox = bbox;
		} else bbox = new Rectangle(xloc, yloc, parentbbox.width, parentbbox.height);
		
		int newxloc = (parentStr == "") ? xloc : xloc + bbox.width/2 + maxwidth/2;
		int newyloc = (parentStr == "") ? yloc : yloc - bbox.height/2 + (choiceheight * nextChoices.length)/2 + parentPosition * choiceheight;
		
		Rectangle newboundbox = new Rectangle(newxloc - maxwidth/2, newyloc-totalheight/2, maxwidth, totalheight);
		
		
		
		for (int i = 0; i < nextChoices.length; i++)
		{
			if (menuitem[0] == nextChoices[i]) g.setColor( highlightcolor );
			else g.setColor( unselectedcolor );

			g.fill3DRect( newboundbox.x, newboundbox.y + i * choiceheight, newboundbox.width, choiceheight,true);

			String thisstring = nextChoices[i];

			if (menuitem[0] == nextChoices[i]) 
			g.setColor( normalcolor );
			else g.setColor( selectedcolor );

			g.drawString( thisstring, newxloc - newboundbox.width/2 + fontpad, ( (newyloc-totalheight/2) + ( (i+1) * choiceheight) ) - border );
			//g.drawString(thisstring,xloc - fontmetwurst.stringWidth(thisstring)/2,
			//             ( (yloc-totalheight/2) + ((i+1)*choiceheight) ) - border);
		}
		if ( menuitem[0] != "" )
		{
			drawMenuTree( g, newboundbox, menuitem[0], parentChosen, newxloc, newyloc );
		}
	}
	
	public void draw(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//Font oldfont = g.getFont();
		g.setFont(new Font("Verdana",Font.PLAIN,14));
		
		if (drawmode == widgetDrawMode.BigFont) { g.setFont(new Font("Verdana",Font.PLAIN,24));}
		
		if ((alive) && (drawmode != widgetDrawMode.Hidden))
		{	
			switch (widgettype) 
			{
				case StringChoice:
				case EnumValue:
				{		
					fontmetwurst = g.getFontMetrics();
					choiceheight = fontmetwurst.getHeight() + border;
					String choices[] = (String[]) widgetdata;
					
					int totalheight = (choiceheight * choices.length) + border;
					int maxwidth = 20;
					
					for (int i = 0; i < choices.length; i++)
					{
						if ( fontmetwurst.stringWidth(methodname + "(" + choices[i] + ")") > maxwidth) 
						{
							maxwidth = fontmetwurst.stringWidth( methodname + "(" + choices[i] + ")" ); 
						}
					}
					maxwidth += border*2;
					boundbox = new Rectangle(xloc-maxwidth/2,yloc-totalheight/2,maxwidth,totalheight);
					
					//g.setColor(normalcolor);
					//g.fill3DRect(boundbox.x,boundbox.y,boundbox.width,boundbox.height,true);
					//if ((chosen >= 0) && (drawmode != widgetDrawMode.NoBackground ))
					//{
					//	g.setColor( highlightcolor );
					//	g.fill3DRect(boundbox.x,boundbox.y + chosen * choiceheight, boundbox.width, choiceheight,true);
					//}
					
					for (int i = 0; i < choices.length; i++)
					{
						String thisstring = methodname + "(" + choices[i] + ")";
						
						if (i != chosen)
						{
							g.setColor( unselectedcolor );
							g.fill3DRect(boundbox.x,boundbox.y + i * choiceheight, boundbox.width, choiceheight, true);
							g.setColor( selectedcolor.darker() );
						} else {
							g.setColor( highlightcolor );
							g.fill3DRect(boundbox.x,boundbox.y + i * choiceheight, boundbox.width, choiceheight, true);
							g.setColor( selectedcolor );
						}
						
						g.drawString(thisstring,xloc - fontmetwurst.stringWidth(thisstring)/2,
						             ( (yloc-totalheight/2) + ((i+1)*choiceheight) ) - border);
					}
					break;
				}
				case MenuValue:
				{		
					fontmetwurst = g.getFontMetrics();
					choiceheight = fontmetwurst.getHeight() + border;
					drawMenuTree( g, boundbox, "", 0, xloc, yloc );
					
					break;
				}
				case FloatValue  : 
				{
					switch (drawmode)
					{
						case Default:
						case BigFont:
						{
							g.setColor(highlightcolor);
							fontmetwurst = g.getFontMetrics();
							String floss = new String(methodname + "(" + floatvar + ");");
							int sw = fontmetwurst.stringWidth(floss);
							int sh = fontmetwurst.getHeight();
							g.fill3DRect(xloc - ((sw / 2) + border),yloc - ((sh/2) + border), sw + border*2,sh + border*2,true);
							g.setColor(selectedcolor);
							g.drawString(floss,xloc - sw / 2,yloc + sh/2);
							break;
						}
						case NoBackground:
						{
							fontmetwurst = g.getFontMetrics();
							String floss = new String(methodname + "(" + floatvar + ");");
							int sw = fontmetwurst.stringWidth(floss);
							int sh = fontmetwurst.getHeight();
							g.setColor(selectedcolor);
							g.drawString(floss,xloc - sw / 2,yloc + sh/2);
							break;
						}
						case Radius :
						{
							g.setColor(highlightcolor);
							int rad = (int) floatvar;
							g.setStroke(new BasicStroke(50,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
							g.drawOval( xloc - rad,yloc - rad, rad * 2, rad * 2);
							fontmetwurst = g.getFontMetrics();
							String intuos = new String( " " + intvar + " ");
							int sw = fontmetwurst.stringWidth(intuos);
							int sh = fontmetwurst.getHeight();
							g.setColor(selectedcolor);
							g.drawString(intuos,xloc - sw / 2,yloc + sh/2);
							break;
						}
						
					}
					break;
				}
				case IntValue : 
				{
					switch (drawmode)
					{
						case Default:
						case BigFont:
						{
							g.setColor(highlightcolor);
							fontmetwurst = g.getFontMetrics();
							String intuos = new String(methodname + "(" + intvar + ")");
							int sw = fontmetwurst.stringWidth(intuos);
							int sh = fontmetwurst.getHeight();
							g.fill3DRect(xloc - ((sw / 2) + border),yloc - ((sh/2) + border), sw + border*2,sh + border*2,true);
							g.setColor(selectedcolor);
							g.drawString(intuos,xloc - sw / 2,yloc + sh/2);
							break;
						}
						case NoBackground:
						{
							fontmetwurst = g.getFontMetrics();
							String intuos = new String(methodname + "(" + intvar + ")");
							int sw = fontmetwurst.stringWidth(intuos);
							int sh = fontmetwurst.getHeight();
							g.setColor(selectedcolor);
							g.drawString(intuos,xloc - sw / 2,yloc + sh/2);
							break;
						}
						case Radius :
						{
							g.setColor(highlightcolor);
							int rad = intvar;
							g.setStroke(new BasicStroke(50,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
							g.drawOval( xloc - rad,yloc - rad, rad * 2, rad * 2);
							fontmetwurst = g.getFontMetrics();
							String intuos = new String( " " + intvar + " ");
							int sw = fontmetwurst.stringWidth(intuos);
							int sh = fontmetwurst.getHeight();
							g.setColor(selectedcolor);
							g.drawString(intuos,xloc - sw / 2,yloc + sh/2);
							break;
						}
					}
					break;
				}
				case BooleanValue : 
				{
					g.setColor(highlightcolor);
					fontmetwurst = g.getFontMetrics();
					String intuos = new String(methodname + "(" + boolvar + ")");
					int sw = fontmetwurst.stringWidth(intuos);
					int sh = fontmetwurst.getHeight();
					if (drawmode != widgetDrawMode.NoBackground) g.fill3DRect(xloc - ((sw / 2) + border),yloc - ((sh/2) + border), sw + border*2,sh + border*2,true);
					g.setColor(selectedcolor);
					g.drawString(intuos,xloc - sw / 2,yloc + sh/2);
					
					/* draw as filled circle
					if (boolvar) g.setColor(normalcolor);
					else g.setColor(selectedcolor);
					if (boundbox != null) g.fillOval(boundbox.x - border,boundbox.y - border,boundbox.width+border*2,boundbox.height + border*2);
					
					if (boolvar) g.setColor(selectedcolor);
					else g.setColor(normalcolor);
					if (boundbox != null) g.fillOval(boundbox.x,boundbox.y,boundbox.width,boundbox.height);
					*/
					break;
				}
				case PointValue:
				{
					g.setColor(normalcolor);
					g.drawOval(xloc - 5,yloc - 5,10,10);
					g.drawOval(xloc - 50,yloc - 50,100,100);
					if (drawmode != widgetDrawMode.NoBackground) g.fill3DRect(xloc - boundbox.width/2,yloc - +boundbox.height/2, boundbox.width, boundbox.height,true);
					g.setColor(highlightcolor);
					g.drawLine(xloc-boundbox.width/2,yloc,xloc+boundbox.width/2,yloc);
					g.drawLine(xloc,yloc-boundbox.height/2,xloc,yloc+boundbox.height/2);
					g.setColor(selectedcolor);
					g.drawLine(xloc + pointvar.x, yloc + pointvar.y,xloc,yloc);
					break;
				}
				case PolarValue          : 
				{
					break;
				}
				case NeighborhoodEdit   :
				{
					break;
				}
				case IntArrayValue          :
				{
					break;
				}

				
				case StringValue:
				{
					g.setColor(highlightcolor);
					if (drawmode != widgetDrawMode.BigFont) g.setFont(new Font("Verdana",Font.PLAIN,20));
					else g.setFont(new Font("Verdana",Font.PLAIN,40));
					fontmetwurst = g.getFontMetrics();
					String dastring = methodname + "(" + widgetstring + ")";
					int sw = fontmetwurst.stringWidth(dastring);
					int sh = fontmetwurst.getHeight();
					if (drawmode != widgetDrawMode.NoBackground) g.fill3DRect(xloc - ((sw / 2) + border),yloc - ((sh/2) + border), sw + border*2,sh + border*2,true);
					g.setColor(selectedcolor);
					g.drawString(dastring, xloc - sw / 2,yloc + sh/2);
					break;
				}
				
				case LStringEdit:
				{
					g.setColor(highlightcolor);
					if (drawmode != widgetDrawMode.BigFont) g.setFont(new Font("Verdana",Font.PLAIN,20));
					else g.setFont(new Font("Verdana",Font.PLAIN,40));
					fontmetwurst = g.getFontMetrics();
					String dastring = null;
					if (widgetstring.length() > 0) dastring = methodname + "(" + widgetstring + ")";
					else dastring = methodname + "()";
					
					int sw = fontmetwurst.stringWidth(dastring);
					int sh = fontmetwurst.getHeight();
					if (drawmode != widgetDrawMode.NoBackground) g.fill3DRect(xloc - ((sw / 2) + border),yloc - ((sh/2) + border), sw + border*2,sh + border*2,true);
					g.setColor(selectedcolor);
					g.drawString(methodname + "(" + widgetstring + ")", xloc - sw / 2,yloc + sh/2);
					break;
				}
				case Line:
				{
					g.setColor(selectedcolor);
					g.drawLine(pointvar.x,pointvar.y,pointvardest.x,pointvardest.y);
					break;
				}
				
				case BooleanMatrixValue:
				{
					int cellwidth = boundbox.width / booleanmatrixsize;
					int cellheight = boundbox.height / booleanmatrixsize;
					
					//this only works for odd dimensioned matrices at the moment..
					int halfmatrix = booleanmatrixsize/2;
					
					if (booleanmatrix != null)
					{
						for (int i = -halfmatrix; i <= halfmatrix; i++)
						{
							for (int j = -halfmatrix; j <= halfmatrix; j++)
							{
								int cellcornerX = xloc + i * cellwidth;
								int cellcornerY = yloc + j * cellwidth;
								int ix = i + halfmatrix + (j + halfmatrix) * booleanmatrixsize;
								//shouldn't be possible to get an out-of-bounds here.. but just in case..
								
								ix = Math.max(0, Math.min( ix, booleanmatrix.length - 1 ) );
								
								boolean boolvalue = booleanmatrix[ ix ];
								
								if (boolvalue) g.setColor(highlightcolor);
								else g.setColor( normalcolor );
								
								g.fill3DRect( cellcornerX, cellcornerY, cellwidth, cellheight, boolvalue );
							}
						}
					} else {
						System.out.println("bloody hell, booleanmatrix was null! .. can't draw it.");
					}
				}
				
				
			}
		}           
		//g.setFont(oldfont);
		if (childWidges != null)
		for (int k = 0; k < childWidges.size(); k++)
		{
			EditWidget child = (EditWidget) childWidges.elementAt(k);
			if (child.isAlive()) child.draw(g);
		}
	}
}
