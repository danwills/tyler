/*
 * tyler - a realtime image traversal and resynthesis engine
 *
 *   some things it does:
 *   		particles - driven by l-system like atoms and drawn with the JWT then [optionally] pixelgrabbed or (beta) direct to the pixelbuffer.
 *   		pixel neighborhood effects with lots of experimental "warp field rasterized to neighborhood image" - or 'topology', generators
 *   		neighborhood processing modes and feedback blend modes
 *		save jpegs - every frame if you want
 *		
 *  
 * some things it kinda does:
 *		extract rule data from an image
 *		display rule data in a tool widget
 *		process topology with rule data
 * 		
 * some things I'd like it to do:
 *		navigate a directory tree and load many more formats of image
 *		be able to deal with large images - and extract a fast-filtered (optionally well-filtered) affine subtile of them..
 *		more pixel-draw particle paint modes, including sprites and sprite tabled
 *		topology-draw particle paint modes
 *		topology-warping toplogy
 *		3d particles
 *		spline editor
 *		node view
 *		macro save/load/repeat
 *		undo
 *		proper reaction/diffusion
 *		really kewl rule edit widget, reealy kewl
 *		
 *
 *
 *
 *
 *		
 * was once a browser applet, but security was too restrictive,
 * so now tyler is  standalone and can use the JWT's transparency, antialiasting and other.
 *  
 * Tyler is an English (old English) word which means door keeper of an inn. 
 * It is also thought to be a derived occupational name derived from "tiler", 
 * one who makes tiles. It is used both as a surname, and as given name for both genders.
 *
 *
 * next:
 * clipboard, sound, control saveToFile, commandline,
 * offscreen/batch hirez rendering mode
 * 
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class tyler extends JPanel implements KeyListener
{
    static JFrame frame;
    static Window aWindow;
	static Tiler atiler;
	
	static final int tilerSizeX = 512;
	static final int tilerSizeY = 512;
	static Dimension tilerDimension = new Dimension( tilerSizeX, tilerSizeY );
	
    static String metal= "Metal";
    static String metalClassName = "javax.swing.plaf.metal.MetalLookAndFeel";

    static String motif = "Motif";
    static String motifClassName = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";

    static String windows = "Windows";
    static String windowsClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
		
    public tyler() 
    {
    	super(true);
		
		ScrollPane scrollo = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		
		String nl = System.getProperty("line.separator");
		TextArea txae = new TextArea(("*** Tyler output pane ***" + nl),8,32,TextArea.SCROLLBARS_NONE); //_VERTICAL_ONLY
		txae.setSize( tilerDimension );
		txae.setBackground(new Color(0.05f,0.05f,0.05f));
		txae.setForeground(new Color(0.4f,0.4f,0.4f));
		scrollo.add(txae);
		
	setBorder(BorderFactory.createEtchedBorder());
	setLayout(new BorderLayout());

     	aWindow.add(atiler, BorderLayout.CENTER);
     	
     	atiler.setSize( tilerDimension );
     	
     	atiler.setOutputTextArea(txae);
     	aWindow.validate();
     	aWindow.setVisible(true);
     	aWindow.requestFocus();
     	this.addKeyListener(this);
     	this.setFocusable(true);
	}
	
    public static void main( String args[] ) 
    {
		try 
		{
	          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch (Exception exc) {
	    	System.err.println("Error loading L&F: " + exc);
	    }
		
		GraphicsEnvironment Ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] Gs = Ge.getScreenDevices();
        
        //MouseInfo minfo = new MouseInfo();
        PointerInfo pinfo = MouseInfo.getPointerInfo();
        GraphicsDevice gdevice = pinfo.getDevice();
        Point plocation = pinfo.getLocation();
        GraphicsConfiguration selectedGraphicsConf = gdevice.getDefaultConfiguration();
		System.out.println("Selected screenDevice: " + gdevice);
        
        Point loc = new Point(0,0);
        
        for (GraphicsDevice g : Gs)
        {
		    System.out.println("ScreenDevice: " + g);
            Rectangle rect = g.getDefaultConfiguration().getBounds();
            System.out.println("ScreenRect: " + rect);
            
            if ( rect.contains( plocation ) )
            {
                loc.move( rect.x, rect.y );
            }
        }
        
		//GraphicsConfiguration[] Gcs = gdevice.getConfigurations();
        
        /*for (GraphicsConfiguration gc : Gcs )
        {
            System.out.println("graphicsConfig: " + gc);
        }*/
        
        System.out.println("Pointer location: " + plocation);
        //if (plocation.x > Gs[0].getDefaultConfiguration().getBounds()
        
		frame = new JFrame("tyler");
		
		frame.setResizable(true);

		Dialog aDialog = new Dialog(frame,"tyler",false, selectedGraphicsConf );
		
		aDialog.setResizable(true);
		aDialog.setUndecorated(true);
		aWindow = aDialog;
		
		aWindow.setLocation(loc.x,loc.y);
		aWindow.setBackground(new Color(0,0,0));
        
		Toolkit defaulttoolkit = frame.getToolkit().getDefaultToolkit();
        Rectangle screenBounds = selectedGraphicsConf.getBounds();
		Dimension dscreen = screenBounds.getSize();
        //defaulttoolkit.getScreenSize();
		
		//aWindow.setSize(new Dimension((int)dscreen.getWidth()/2,(int)dscreen.getHeight()));
		aWindow.setSize( dscreen );


		atiler = new Tiler( selectedGraphicsConf );
		tyler tylerPanel = new tyler();
		
		aWindow.addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e) {System.exit(0);} } );
		aWindow.validate();
		aWindow.setVisible(true);
		
		atiler.setParentComponent( aWindow );
        aWindow.setBackground(new Color(0,0,0));
        
	    aWindow.addKeyListener( atiler );
		
        atiler.setSize( dscreen );
        
        atiler.setLocation(0,0);
        atiler.start();
		atiler.init();
    }
	
    public void keyTyped(KeyEvent e)
    {
    	System.out.println("keytyped:");
    }
    public void keyReleased(KeyEvent e)
    {
    	System.out.println("keyReleased:");
    }

    public void keyPressed(KeyEvent e) 
    {
    	System.out.println("keyPressed:");
    }
	
}
