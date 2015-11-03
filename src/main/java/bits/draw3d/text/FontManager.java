/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.text;

import com.jogamp.opengl.GLContext;
import java.awt.*;
import java.util.*;


/**
 * Stores FontTextures associated with different GLContexts.
 * 
 * @author decamp
 */
public class FontManager {

    
    private final Map<GLContext, Map<Font, FontTexture>> mMap = new WeakHashMap<GLContext, Map<Font, FontTexture>>();
    
    
    public FontTexture getFontTexture( Font font ) {
        return getFontTexture( font, GLContext.getCurrent() );
    }
    
    
    public synchronized FontTexture getFontTexture( Font font, GLContext context ) {
        Map<Font, FontTexture> fontMap = mMap.get(context);
        
        if(fontMap == null) {
            fontMap = new HashMap<Font, FontTexture>();
            mMap.put(context, fontMap);
        }
        
        FontTexture tex = fontMap.get(font);
        
        if(tex == null) {
            tex = new FontTexture(font);
            fontMap.put(font, tex);
        }
        
        return tex;
    }

    
}
