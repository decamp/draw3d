package bits.draw3d.nodes;

import java.lang.reflect.Modifier;
import java.util.*;

import bits.draw3d.context.RenderTile;


/**
 * Utility class for creating NodeProviders from a collection of NodeFactories.
 * By default, the NodeProvider that is constructed will create one
 * node instance for each GLContext.  I might make the reuse/creation policy
 * more flexible if eventually needed.  As is, this already seems like an obnoxious
 * piece of over engineering.
 *
 * @author decamp
 */
public class RenderModuleBuilder {


    private final Map<Class<?>, NodeFactory<?>> mFactoryMap = new HashMap<Class<?>, NodeFactory<?>>();


    public <N> void addFactory( Class<N> nodeClass, NodeFactory<? extends N> factory, boolean addSuperclasses ) {
        mFactoryMap.put( nodeClass, factory );
        Class<?> clazz = nodeClass;

        while( addSuperclasses && clazz != null ) {
            if( (clazz.getModifiers() & Modifier.PUBLIC) != 0 ) {
                mFactoryMap.put( clazz, factory );
            }

            for( Class<?> c : clazz.getInterfaces() ) {
                if( (c.getModifiers() & Modifier.PUBLIC) != 0 ) {
                    mFactoryMap.put( c, factory );
                }
            }

            clazz = clazz.getSuperclass();
        }
    }


    public RenderModule build() {
        Map<NodeFactory<?>, Set<Class<?>>> classMap = new HashMap<NodeFactory<?>, Set<Class<?>>>();

        for( Map.Entry<Class<?>, NodeFactory<?>> e : mFactoryMap.entrySet() ) {
            NodeFactory<?> f = e.getValue();
            if( f == null ) {
                continue;
            }

            Set<Class<?>> classSet = classMap.get( f );

            if( classSet == null ) {
                classSet = new HashSet<Class<?>>();
                classMap.put( f, classSet );
            }

            classSet.add( e.getKey() );
        }


        Map<Class<?>, NodeCache<?>> cacheMap = new HashMap<Class<?>, NodeCache<?>>();

        for( NodeFactory<?> factory : classMap.keySet() ) {
            if( factory == null ) {
                continue;
            }

            NodeCache<?> cache = new NodeCache<Object>( factory );

            for( Class<?> clazz : classMap.get( factory ) ) {
                cacheMap.put( clazz, cache );
            }
        }

        return new NodeProviderImpl( cacheMap );
    }


    private static final class NodeCache<N> {

        final Map<Object, N> mMap = new HashMap<Object, N>();
        final NodeFactory<? extends N> mFactory;

        NodeCache( NodeFactory<? extends N> factory ) {
            mFactory = factory;
        }


        N get( Object key, RenderTile tile ) {
            if( !mMap.containsKey( key ) ) {
                N n = mFactory.create( tile );
                mMap.put( key, n );
            }

            return mMap.get( key );
        }

    }


    private static final class NodeProviderImpl implements RenderModule {

        private final Map<Class<?>, NodeCache<?>> mCacheMap;


        NodeProviderImpl( Map<Class<?>, NodeCache<?>> cacheMap ) {
            mCacheMap = cacheMap;
        }


        public Object getNodes( Class<?> clazz, RenderTile tile ) {
            NodeCache<?> cache = mCacheMap.get( clazz );
            Object ret = null;

            if( cache != null ) {
                ret = cache.get( tile.context(), tile );
            }

            return ret;
        }

    }

}
