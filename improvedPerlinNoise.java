//
// Improved Perlin Noise by Ken Perlin
//
// this is a port to java of an implementation originally by Malcolm from rsp:
//
// Copyright (C) 2003 Risingsun Pictures
//		Author: Malcolm Humphreys 
// 
// ported to java by Dan Wills 2005.
// (heheh.. that was hard! :P)
//

//#include <math.h>

public class improvedPerlinNoise
{
	//void   init( void );
	//double fade( double t );
	//double lerp( double t, double a, double b );
	//double gradThree( int hash, double x, double y, double z );
	//double gradFour( int hash, double x, double y, double z, double w );
	
	// noise functions
   // double improvedPerlinNoiseThree( double x, double y, double z );
	//double improvedPerlinNoiseFour( double x, double y, double z, double w );
	
	// Harmonic Summing function
	//double perlinNoise3D( double x, double y, double z, double alpha, double beta, int n );
	//double perlinNoise4D( double x, double y, double z, double time, double amplitude, double frequency, int depth );
	
	public static void main(String sargs[]) 
	{
		for (int s = 0; s < sargs.length; s++)
		{
			if ( (sargs[s].equals("-h") ) | (sargs[s].equals("-help") ) | (sargs[s].equals( "-?" ) )| (sargs[s].equals( "--help" ) ) )
			{
				System.out.println("");
				System.out.println("Ken Perlin's Improved Noise function");
				System.out.println("");
				System.out.println("Usage: ");
				System.out.println("\t\timprovedPerlinNoise step rangestart rangeend printwidth sleepdelay");
				System.exit(0);
			}
			System.out.println(sargs[s]);
		}
		
		double rangestart = 0.0;
		double rangeend = 256.0;
		double step = 0.181371;
		int drawwidth = 40;
		int sleepdelay = 4;
		boolean drawvalue = true;
		boolean drawaxis = true;
		boolean drawgraph = true;
		
		if (sargs.length >= 1)
		{
			try{
			
				step = Double.parseDouble( sargs[0] );
				
			} catch (NumberFormatException e) {
				System.out.println("could not parse argument 1 (step). Using default value of " + step );
			}
		
		} 
		if (sargs.length >= 2) {
			try{
			
				rangestart = Double.parseDouble( sargs[1] );
				
			} catch (NumberFormatException e) {
				System.out.println("could not parse argument 2 (rangestart). Using default value of " + rangestart );
				
			}
		}
		
		if (sargs.length >= 3) {
			try{
			
				rangeend = Double.parseDouble( sargs[2] );
				
			} catch (NumberFormatException e) {
				System.out.println("could not parse argument 3 (rangeend). Using default value of " + rangeend );
				
			}
		} 
		
		if (sargs.length >= 4) {
			try{
				
				drawwidth = Integer.parseInt( sargs[3] );
				
			} catch (NumberFormatException e) {
				System.out.println("could not parse argument 4 (drawwidth). Using default value of " + drawwidth );
			}
		}
		
		if (sargs.length >= 5) {
			try{
				
				sleepdelay = Integer.parseInt( sargs[4] );
				
			} catch (NumberFormatException e) {
				System.out.println("could not parse argument 5 (sleepdelay). Using default value of " + sleepdelay );
			}
		}
		
		if (sargs.length >= 6) {
			try{
				
				drawvalue = Boolean.parseBoolean( sargs[5] );
				
			} catch (NumberFormatException e) {
				System.out.println("could not parse argument 6 (drawvalue). Using default value of " + drawvalue );
			}
		}
		
		if (sargs.length >= 7) {
			try{
				
				drawaxis = Boolean.parseBoolean( sargs[6] );
				
			} catch (NumberFormatException e) {
				System.out.println("could not parse argument 7 (drawaxis). Using default value of " + drawaxis );
			}
		}
		
		
		if (sargs.length >= 8) {
			try{
				
				drawgraph = Boolean.parseBoolean( sargs[7] );
				
			} catch (NumberFormatException e) {
				System.out.println("could not parse argument 8 (drawgraph). Using default value of " + drawgraph );
			}
		}
		
		if (sargs.length >= 1) System.out.println("Using settings (step/rangestart/rangeend/drawwidth/sleepdelay/drawvalue/drawaxis/drawgraph ): \n ( " + step + " / " + rangestart + " / " + rangeend + " / " + drawwidth + " / " + sleepdelay + " / " + drawvalue + " / " + drawaxis + " / " + drawgraph  +" ) ");
		
		for (double z = rangestart; z <= rangeend; z+= step)
		{
			try {
				//move in all 3 dimensionnes, o'le!
				double sample = improvedPerlinNoiseThree(-127.5 - z*0.061, 30.5 + z * 0.9212,z*0.5636);
				
				boolean doneZero = false;
				boolean invertedDisplay = false;// (Math.random() > 0.5);
				if ( drawaxis ) System.out.print("[");
				
				if ( drawgraph )
				for (int q = 0; q < drawwidth; q++)
				{
					
					if ( (q >= 0.5 * drawwidth) && !doneZero) 
					{
						if ( drawaxis )
						{
							if ( ((float)q / (float)drawwidth) < ( (sample + 1) * 0.5 )  )
							{
								System.out.print("+");
							} else {
								
								System.out.print("-");
							}
							doneZero = true;
						} else {
							if ( ((float)q / (float)drawwidth) < ( (sample + 1) * 0.5 )  ) System.out.print("*");
							else System.out.print(" ");
						}
					} else {
					
						if ( ((float)q / (float)drawwidth) < ( (sample + 1) * 0.5 )  )
						{
							if (!invertedDisplay) System.out.print("*");
							else System.out.print(" ");
						} else {
							if (!invertedDisplay) System.out.print(" ");
							else System.out.print("*");
						}
					}
				}
				
				if (drawvalue) 
				{
					if ( drawaxis ) System.out.println("] = " + sample);
					else System.out.println("" + sample);
				} else {
					if ( drawaxis ) System.out.println("]");
					else System.out.println("");
				}
				
				Thread.sleep( sleepdelay );
			} catch (java.lang.InterruptedException e) { System.out.println("Interrupted"); break; }
		}
	}
	
	// vars
	static int init_p = 1; // only init once
	//p has 512 entries
	static int p[];
	static int permutation[];
	
	//
	
	static double fade( double t ) { return t * t * t * ( t * ( t * 6 - 15 ) + 10 ); }
	
	static double lerp( double t, double a, double b ) { return a + t * ( b - a ); }
	
	static double gradThree( int hash, double x, double y, double z )
	{
		int h = hash & 15;							// CONVERT LO 4 BITS OF HASH CODE
		double
		u = h < 8 || h == 12 || h == 13 ? x : y,	// INTO 12 GRADIENT DIRECTIONS.
		v = h < 4 || h == 12 || h == 13 ? y : z;
		return ( ( h&1 ) == 0 ? u : -u ) + ( ( h&2 ) == 0 ? v : -v );
	}
	
	static double gradFour( int hash, double x, double y, double z, double w )
	{
		int h = hash & 31; // CONVERT LO 5 BITS OF HASH TO 32 GRAD DIRECTIONS.
		double a = y, b = z, c = w;            // X,Y,Z
		// OR, DEPENDING ON HIGH ORDER 2 BITS:
		switch ( h >> 3)
		{
			case 1:		// W,X,Y
				a = w;
				b = x;
				c = y;
				break;
			case 2:		// Z,W,X
				a = z;
				b = w;
				c = x;
				break;
			case 3:		// Y,Z,W
				a = y;
				b = z;
				c = w;
				break;
		  }
		  return ( ( h&4 ) == 0 ? -a : a ) + ( ( h&2 ) == 0 ? -b : b ) + ( ( h&1 ) == 0 ? -c : c );
	   }
	
	static void init(  )
	{
		int i;
		int table[] = { 151,160,137,91,90,15,
			131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
			190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
			88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
			77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
			102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
			135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
			5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
			223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
			129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
			251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
			49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
			138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180 };
		
		p = new int[512];
		permutation = new int[512];
		 
		for ( i = 0; i < 256 ; i++ )
			p[ 256 + i ] = p[ i ] = permutation[ i ] = table[i];
	}
	
	// 3D Improved Perlin Noise
	static double improvedPerlinNoiseThree( double x, double y, double z )
	{
		int    X, Y, Z,
			   A, AA, AB, B, BA, BB;
		double u, v, w;
		
		if ( init_p != 0 ) { init_p = 0; init(); }
		
		// FIND UNIT CUBE THAT CONTAINS POINT.
		X = (int)java.lang.Math.floor( x ) & 255;
		Y = (int)java.lang.Math.floor( y ) & 255;
		Z = (int)java.lang.Math.floor( z ) & 255;
	
		// FIND RELATIVE X,Y,Z OF POINT IN CUBE.
		x -= java.lang.Math.floor( x );
		y -= java.lang.Math.floor( y );
		z -= java.lang.Math.floor( z );
		
		// COMPUTE FADE CURVES FOR EACH OF X,Y,Z.
		u = fade( x );		
		v = fade( y );
		w = fade( z );
		
		// HASH COORDINATES OF THE 8 CUBE CORNERS,
		A  = p[ X     ] + Y;
		AA = p[ A     ] + Z;
		AB = p[ A + 1 ] + Z;
		B  = p[ X + 1 ] + Y;
		BA = p[ B     ] + Z;
		BB = p[ B + 1 ] + Z;
	
		// AND ADD BLENDED RESULTS FROM 8 CORNERS OF CUBE
		return lerp(w, lerp(v, lerp( u, gradThree( p[ AA     ], x    , y    , z     ),
										gradThree( p[ BA     ], x - 1, y    , z     )),
							   lerp( u, gradThree( p[ AB     ], x    , y - 1, z     ),
										gradThree( p[ BB     ], x - 1, y - 1, z     ))),
					   lerp(v, lerp( u, gradThree( p[ AA + 1 ], x    , y    , z - 1 ),
										gradThree( p[ BA + 1 ], x - 1, y    , z - 1 )),
							   lerp( u, gradThree( p[ AB + 1 ], x    , y - 1, z - 1 ),
										gradThree( p[ BB + 1 ], x - 1, y - 1, z - 1 ))));
	}
	
	// 4D Improved Perlin Noise, 4th D is time
	static double improvedPerlinNoiseFour( double x, double y, double z, double w )
	{
		int X, Y, Z, W,
			A, AA, AAA, AB, ABA, AAB, ABB,
			B, BB, BBB, BA, BBA, BAB, BAA;
		double a, b, c, d;
	
		if ( init_p != 0 ) { init_p = 0; init(); }
	
		// FIND UNIT HYPERCUBE THAT CONTAINS POINT.
		X = (int)java.lang.Math.floor( x ) & 255;
		Y = (int)java.lang.Math.floor( y ) & 255;
		Z = (int)java.lang.Math.floor( z ) & 255;
		W = (int)java.lang.Math.floor( w ) & 255;
	
		// FIND RELATIVE X,Y,Z OF POINT IN CUBE.
		x -= java.lang.Math.floor( x );
		y -= java.lang.Math.floor( y );
		z -= java.lang.Math.floor( z );
		w -= java.lang.Math.floor( w );
	
		// COMPUTE FADE CURVES FOR EACH OF X,Y,Z,W.
		a = fade(x);
		b = fade(y);
		c = fade(z);
		d = fade(w);
	
		// HASH COORDINATES OF THE 16 CORNERS OF THE HYPERCUBE.
		A   = p[ X      ] + Y;
		AA  = p[ A      ] + Z;
		AB  = p[ A + 1  ] + Z;
		B   = p[ X + 1  ] + Y;
		BA  = p[ B      ] + Z;
		BB  = p[ B + 1  ] + Z;
		AAA = p[ AA     ] + W;
		AAB = p[ AA + 1 ] + W;
		ABA = p[ AB     ] + W;
		ABB = p[ AB + 1 ] + W;
		BAA = p[ BA     ] + W;
		BAB = p[ BA + 1 ] + W;
		BBA = p[ BB     ] + W;
		BBB = p[ BB + 1 ] + W;
	
		// INTERPOLATE DOWN.
		return lerp(d,                                     
					lerp( c, lerp( b, lerp( a, gradFour( p[ AAA     ], x    , y    , z    , w ), 
											   gradFour( p[ BAA     ], x - 1, y    , z    , w ) ),
									  lerp( a, gradFour( p[ ABA     ], x    , y - 1, z    , w ), 
											   gradFour( p[ BBA     ], x - 1, y - 1, z    , w ) ) ),
	
							 lerp( b, lerp( a, gradFour( p[ AAB     ], x    , y    , z - 1, w ), 
											   gradFour( p[ BAB     ], x - 1, y    , z - 1, w ) ),
									  lerp( a, gradFour( p[ ABB     ], x    , y - 1, z - 1, w ),
											   gradFour( p[ BBB     ], x - 1, y - 1, z - 1, w ) ) ) ),
	
					lerp( c, lerp( b, lerp( a, gradFour( p[ AAA + 1 ], x    , y    , z    , w - 1 ), 
											   gradFour( p[ BAA + 1 ], x - 1, y    , z    , w - 1 ) ),
									  lerp( a, gradFour( p[ ABA + 1 ], x    , y - 1, z    , w - 1 ), 
											   gradFour( p[ BBA + 1 ], x - 1, y - 1, z    , w - 1 ) ) ),
	
							 lerp( b, lerp( a, gradFour( p[ AAB + 1 ], x    , y    , z - 1, w - 1 ), 
											   gradFour( p[ BAB + 1 ], x - 1, y    , z - 1, w - 1 ) ),
									  lerp( a, gradFour( p[ ABB + 1 ], x    , y - 1, z - 1, w - 1 ),
											   gradFour( p[ BBB + 1 ], x - 1, y - 1, z - 1, w - 1 ) ) ) ) );
	}
	
	
	/////////////////////////
	// Harmonic Summing function
	static double perlinNoise3D( double x, double y, double z, double alpha, double beta, int n )
	{
		int i;
		double value, sum = 0;
		double scale = 1;
		for ( i = 0; i < n; i++ )
		{
			value = improvedPerlinNoiseThree( x, y, z );
			scale *= alpha;
			x *= beta;
			y *= beta;
			z *= beta;
			sum += value / scale;
		}
		return sum;
	}
	
	static double perlinNoise4D( double x, double y, double z, double time, double amplitude, double frequency, int depth )
	{
		int i;
		double value, sum = 0;
		double scale = 1;
		for ( i = 0; i < depth; i++ )
		{	
			value = improvedPerlinNoiseFour( x, y, z, time );
			scale *= amplitude;
			x *= frequency;
			y *= frequency;
			z *= frequency;
			time *= frequency;
			sum += value / scale;
		}
		return sum;
	}
}
