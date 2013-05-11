package cogmac.draw3d.util;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

import javax.imageio.ImageIO;

import cogmac.prototype.ImagePanel;

public class TestImages {
    
    
    public static void main( String[] args ) throws Exception {
        test2();
    }
    
    
    static void test1() throws Exception {
        BufferedImage im = testImage();
        im = Images.toArgb( im );

        ImagePanel.showImage( im );
        im = Images.resizeBilinear( im, im.getWidth(), im.getHeight() );
        Images.fillRgb( im, 0xFF00FF, null );
        ImagePanel.showImage( im );
        ImageIO.write( im, "png", new File( "/tmp/fruit.png" ) );
    }
    
    
    static void test2() throws Exception {
        BufferedImage im = testImage();
        ImagePanel.showImage( im );
        
        float[][] planes = Images.imageToRgbaPlanes( im, null, null );
        
        Random rand = new Random();
        for( int i = 0; i < planes[0].length; i++ ) {
            planes[3][i] = rand.nextFloat();
        }
        
        im = Images.rgbaPlanesToImage( planes, im.getWidth(), im.getHeight(), null, null );
        Images.multRgbByAlpha( im, null );
        Images.fillAlpha( im, 0xFF, null );
        
        ImagePanel.showImage( im );
    }
    
    
    static BufferedImage testImage() throws IOException {
        return ImageIO.read( new File( "resources_test/fruit.jpg" ) );
    }
    

}
