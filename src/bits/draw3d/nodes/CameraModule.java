package bits.draw3d.nodes;

import java.util.*;

import bits.draw3d.context.RenderTile;
import bits.math3d.actors.SpatialObject;
import bits.math3d.camera.*;



/**
 * @author decamp
 */
public class CameraModule implements RenderModule {

    
    @Deprecated public static CameraModule newInstance( SpatialObject  optCamera,
                                                        ViewFunc       optView,
                                                        ProjectionFunc optProj,
                                                        ViewportFunc   optViewport )
    {
        return new CameraModule( optCamera, optView, optProj, optViewport );
    }
    

    private final SpatialObject mCamera;
    private final ViewFunc mViewFunc;
    private final ProjectionFunc mProjFunc;
    private final ViewportFunc mViewportFunc;
    private final Map<RenderTile,CameraNode> mNodeMap = new LinkedHashMap<RenderTile,CameraNode>();
    
        
    public CameraModule( SpatialObject camera, ViewFunc view, ProjectionFunc proj, ViewportFunc viewport ) {
        mCamera       = camera == null ? new SpatialObject() : camera;
        mViewFunc     = view;
        mProjFunc     = proj;
        mViewportFunc = viewport;
    }
    

    
    public SpatialObject getCameraObject() {
        return mCamera;
    }    

    
    public synchronized Object getCameraNode( RenderTile tile, boolean createIfAbsent ) {
        CameraNode ret = mNodeMap.get( tile );
        if( ret != null || !createIfAbsent ) {
            return ret;
        }
        
        ret = new CameraNode( mCamera, tile, mViewFunc, mProjFunc, mViewportFunc );
        mNodeMap.put( tile, ret );
        return ret;
    }
    
    
    public Object getNodes( Class<?> nodeClass, RenderTile tile ) {
        if( nodeClass == DrawNode.class ) {
            return getCameraNode( tile, true );
        }
        
        return null;
    }
    
}


