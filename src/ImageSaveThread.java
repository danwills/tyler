/**
 * Threading the image-write:
 * Well that is enormously successful! much faster! Yes,s,s,s,!!
 * 
 * Threading the writes has required waiting for these threads to all 
 * be complete before exiting the main thread!!!
 * 
 * See: imageSaveThreads in Tiler.
 * 
 * @author dan
 */

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;

public final class ImageSaveThread extends Thread 
{
    String imageSaveName;
    BufferedImage writeBufferedImage;
    //int width, height;
    //Image imageToWrite;
    //boolean saveAlpha;
    public Boolean isFinished;
    
    // int saveWidth, int saveHeight, Image imageToSave, boolean saveAlphaV )
    ImageSaveThread( String filename, BufferedImage writeBufferedImageV )
    {
        imageSaveName = filename;
        writeBufferedImage = writeBufferedImageV;
        //width = saveWidth;
        //height = saveHeight;
        //imageToWrite = imageToSave;
        //saveAlpha = saveAlphaV;
        isFinished = false;
    }
        
    public void run()
	{
        // Need to keep a lock on imageToWrite.. I think I'm getting partial images sometimes
        // don't start writing it till its been drawn to this BufferedImage
        // alternatively.. implmement bufferedImage interface (or cast?) and write directly?
        //
        
        // System.out.println( "ImageSaveThread: I'm running!" );
        try {
            //BufferedImage writeBufferedImage;
            
            //writeBufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
            //writeBufferedImage.setAccelerationPriority(1.0f);
            //Graphics2D big = writeBufferedImage.createGraphics();
            //if ( saveAlpha ) 
            //{
            //    big.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC ) );
            //}
            //big.drawImage( imageToWrite, 0, 0, null );
            // imageToWrite.flush();
            
            File file = new File( imageSaveName );
            BufferedOutputStream imageOutputStream = new BufferedOutputStream( new FileOutputStream( file ) );
            
            // FileOutputStream out = new FileOutputStream( file );
            
            // System.out.println( "imageSaveName is: " + imageSaveName );
            // Updated to newer, ImageIO-based way of writing..
            // ImageIO.write( bufferedimageToWrite, imageSaveName, out );
            // then even-newer BufferedOutputStream
            ImageIO.write( writeBufferedImage, "PNG", imageOutputStream );
            imageOutputStream.close();
                    
        } catch ( java.io.IOException iox ) {
			System.err.println("java.io.IOException while trying to write image file... somethin's wrawng wit' writin'! hurrr");
        }
        // Notify anyone monitoring the public var 'isFinished':, we're DONE!
        // don't garbage-collect 'till now!
        isFinished = true;
    }
}
