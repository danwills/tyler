import java.awt.*;
import java.awt.event.*;
import java.util.*;

class KeyAction
{
	public String toolname;
	public String methodname;
	EditWidget.widgetType widgettype;
	boolean immediately;
	Object actionmethodobject;
	Object enumnames[];
	Component guiparent;
	float widgetresultscale = 1;
	public String docstring;
	public long keycode;
	
	public KeyAction( String tool, String method, EditWidget.widgetType type, boolean immediate, Object obj, Object enumnamesx[], Component parentcomp, String aboutstring, long keycodex )
	{
		toolname = tool;
		methodname = method;
		widgettype = type;
		immediately = immediate;
		actionmethodobject = obj;
		enumnames = enumnamesx;
		guiparent = parentcomp;
		docstring = aboutstring;
		keycode = keycodex;
	}
		
	public EditWidget doAction( int mouselocx, int mouselocy )
	{
		//print the docstring if in verbose mode..
		System.out.println( docstring );
		
		//System.out.println("widgettype " + widgettype);
		//System.out.println("MenuValue " + widgettype.MenuValue);

		if (immediately)
		{
			EditWidget e = new EditWidget( widgettype, methodname, actionmethodobject, enumnames, mouselocx, mouselocy, EditWidget.widgetDrawMode.Default, widgetresultscale, Tiler.bgcolor, Tiler.midcolor, Tiler.fgcolor, guiparent);
			e.deliver( true );
			e.killWidget();
			return e;
		} else {
			return new EditWidget( widgettype, methodname, actionmethodobject, enumnames, mouselocx, mouselocy, EditWidget.widgetDrawMode.Default, widgetresultscale, Tiler.bgcolor, Tiler.midcolor, Tiler.fgcolor, guiparent);
		}
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
	
	public String getKeyString()
	{
		long kk = this.keycode;

		//System.out.println( "Da Key: " + kk );
		String result = "";
		
		if ( hasCtrl( kk ) ) result += "ctrl+";
		if ( hasAlt( kk ) ) result += "alt+";
		if ( hasShift( kk ) ) result += "shift+";
		String c = getKeyString( kk );
		result += c;
		return result;
	}
	
	public static String getKeyString( long inLong )
	{
		long daKey = inLong & 0xffff0000;
		
		if ( daKey ==    ( ( (long) KeyEvent.VK_0 ) << 32) ) return "0";
		if ( daKey ==    ( ( (long) KeyEvent.VK_1 ) << 32) ) return "1";
		if ( daKey ==    ( ( (long) KeyEvent.VK_2 ) << 32) ) return "2";
		if ( daKey ==    ( ( (long) KeyEvent.VK_3 ) << 32) ) return "3";
		if ( daKey ==    ( ( (long) KeyEvent.VK_4 ) << 32) ) return "4";
		if ( daKey ==    ( ( (long) KeyEvent.VK_5 ) << 32) ) return "5";
		if ( daKey ==    ( ( (long) KeyEvent.VK_6 ) << 32) ) return "6";
		if ( daKey ==    ( ( (long) KeyEvent.VK_7 ) << 32) ) return "7";
		if ( daKey ==    ( ( (long) KeyEvent.VK_8 ) << 32) ) return "8";
		if ( daKey ==    ( ( (long) KeyEvent.VK_9 ) << 32) ) return "9";
		
		if ( daKey ==    ( ( (long) KeyEvent.VK_A ) << 32) ) return "a";
		if ( daKey ==	( ( (long) KeyEvent.VK_B ) << 32) ) return "b";
		if ( daKey ==	( ( (long) KeyEvent.VK_C ) << 32) ) return "c";
		if ( daKey ==	( ( (long) KeyEvent.VK_D ) << 32) ) return "d";
		if ( daKey ==	( ( (long) KeyEvent.VK_E ) << 32) ) return "e";
		if ( daKey ==	( ( (long) KeyEvent.VK_F ) << 32) ) return "f";
		if ( daKey ==	( ( (long) KeyEvent.VK_G ) << 32) ) return "g";
		if ( daKey ==	( ( (long) KeyEvent.VK_H ) << 32) ) return "h";
		if ( daKey ==	( ( (long) KeyEvent.VK_I ) << 32) ) return "i";
		if ( daKey ==	( ( (long) KeyEvent.VK_J ) << 32) ) return "j";
		if ( daKey ==	( ( (long) KeyEvent.VK_K ) << 32) ) return "k";
		if ( daKey ==	( ( (long) KeyEvent.VK_L ) << 32) ) return "l";
		if ( daKey ==	( ( (long) KeyEvent.VK_M ) << 32) ) return "m";
		if ( daKey ==	( ( (long) KeyEvent.VK_N ) << 32) ) return "n";
		if ( daKey ==	( ( (long) KeyEvent.VK_O ) << 32) ) return "o";
		if ( daKey ==	( ( (long) KeyEvent.VK_P ) << 32) ) return "p";
		if ( daKey ==	( ( (long) KeyEvent.VK_Q ) << 32) ) return "q";
		if ( daKey ==	( ( (long) KeyEvent.VK_R ) << 32) ) return "r";
		if ( daKey ==	( ( (long) KeyEvent.VK_S ) << 32) ) return "s";
		if ( daKey ==	( ( (long) KeyEvent.VK_T ) << 32) ) return "t";
		if ( daKey ==	( ( (long) KeyEvent.VK_U ) << 32) ) return "u";

		if ( daKey ==	( ( (long) KeyEvent.VK_V ) << 32) ) return "v";
		if ( daKey ==	( ( (long) KeyEvent.VK_W ) <<32) ) return "w";
		if ( daKey ==	( ( (long) KeyEvent.VK_X ) << 32) ) return "x";
		if ( daKey ==	( ( (long) KeyEvent.VK_Y ) << 32) ) return "y";
		if ( daKey ==	( ( (long) KeyEvent.VK_Z ) << 32) ) return "z";
		if ( daKey ==	( ( (long) KeyEvent.VK_ENTER ) << 32) ) return "enter";
		if ( daKey ==	( ( (long) KeyEvent.VK_ESCAPE ) << 32) ) return "esc";
		if ( daKey ==	( ( (long) KeyEvent.VK_SPACE ) << 32) ) return "space";
		if ( daKey ==	( ( (long) KeyEvent.VK_UP ) << 32) ) return "up";
		if ( daKey ==	( ( (long) KeyEvent.VK_DOWN ) << 32) ) return "down";
		if ( daKey ==	( ( (long) KeyEvent.VK_MINUS ) << 32) ) return "minus";
		if ( daKey ==	( ( (long) KeyEvent.VK_INSERT ) << 32) ) return "insert";
		if ( daKey ==	( ( (long) KeyEvent.VK_DELETE ) << 32) ) return "delete";
		if ( daKey ==	( ( (long) KeyEvent.VK_HOME ) << 32) ) return "home";
		if ( daKey ==	( ( (long) KeyEvent.VK_END ) << 32) ) return "end";
		if ( daKey ==	( ( (long) KeyEvent.VK_PAGE_UP ) << 32) ) return "pageUp";
		if ( daKey ==	( ( (long) KeyEvent.VK_PAGE_DOWN ) << 32) ) return "pageDown";
		if ( daKey ==	( ( (long) KeyEvent.VK_F4 ) << 32) ) return "F4";
		if ( daKey ==	( ( (long) KeyEvent.VK_TAB ) << 32) ) return "tab";
		return "# - currently unmapped key";
	}
}
