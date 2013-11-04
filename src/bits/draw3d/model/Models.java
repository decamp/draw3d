package bits.draw3d.model;

import java.awt.image.BufferedImage;
import java.util.*;

import bits.draw3d.pick.*;
import bits.math3d.Tol;
import bits.math3d.geom.*;




/** 
 * @author Philip DeCamp  
 */
public final class Models {
    
    
    public static Aabb computeBounds(MeshModel model) {
        double x0 = Double.POSITIVE_INFINITY;
        double y0 = Double.POSITIVE_INFINITY;
        double z0 = Double.POSITIVE_INFINITY;
        double x1 = Double.NEGATIVE_INFINITY;
        double y1 = Double.NEGATIVE_INFINITY;
        double z1 = Double.NEGATIVE_INFINITY;
        boolean first = true;
        
        for(Group g: model.getGroupsRef()) {
            for(Triangle t: g.getTrianglesRef()) {
                for(double[] vv: t.vertexRef()) {
                    
                    if(first) {
                        first = false;
                        x0 = x1 = vv[0];
                        y0 = y1 = vv[1];
                        z0 = z1 = vv[2];
                        
                    }else{
                        if(vv[0] < x0) {
                            x0 = vv[0];
                        }else if(vv[0] > x1) {
                            x1 = vv[0];
                        }

                        if(vv[1] < y0) {
                            y0 = vv[1];
                        }else if(vv[1] > y1) {
                            y1 = vv[1];
                        }

                        if(vv[2] < z0) {
                            z0 = vv[2];
                        }else if(vv[2] > z1) {
                            z1 = vv[2];
                        }
                    }
                }
            }
        }
        
        return Aabb.fromEdges(x0, y0, z0, x1, y1, z1);
    }
    
    
    public static KdPointTree<IndexedVertex> indexVertices( MeshModel model ) {
        return indexVertices( model, Tol.ABS_ERR );
    }
    
    
    public static KdPointTree<IndexedVertex> indexVertices( MeshModel model, double equalDistance ) {
        KdPointTree<double[]> tree       = new KdPointTree<double[]>(DimComparator.DOUBLE_ARRAY_INSTANCE);
        PointPickResult<double[]> result = tree.newPointPickResult();
        
        for(Group g: model.getGroups()) {
            for(Triangle t: g.getTriangles()) {
                for(int i = 0; i < 3; i++) {
                    if(!tree.pick(t.vertex(i), result) || result.pickedDistance() > equalDistance) {
                        tree.add(t.vertex(i));
                    }
                }
            }
        }
        
        KdPointTree<IndexedVertex> ret = new KdPointTree<IndexedVertex>(IndexedVertex.ACCESSOR);
        int idx = 0;
        
        for(double[] v: tree) {
            ret.add(new IndexedVertex(idx++, v));
        }
        
        return ret;
    }
    
    
    public static Map<Triangle, Integer> indexTriangles( MeshModel model ) {
        Map<Triangle, Integer> map = new HashMap<Triangle, Integer>();
        int idx = 0;
        
        for(Group g: model.getGroups()) {
            for(Triangle t: g.getTriangles()) {
                if(!map.containsKey(t))
                    map.put(t, idx++);
            }
        }
        
        return map;
    }

    
    public static Map<Triangle, Integer> indexTriangleGroups( MeshModel model ) {
        Map<Triangle, Integer> map = new HashMap<Triangle, Integer>();
        List<Group> groups = model.getGroups();
        
        for(int i = 0; i < groups.size(); i++) {
            Integer n = new Integer(i);
            
            for(Triangle t: groups.get(i).getTriangles()) {
                map.put(t, n);
            }
        }
        
        return map;
    }

    
    public static Map<ModelMaterial, Integer> indexTexMaterials( MeshModel model ) {
        Map<ModelMaterial,Integer> map = new HashMap<ModelMaterial,Integer>();
        List<Group> groups           = model.getGroupsRef();
        int idx = 0;
        
        for( Group g: groups ) {
            BufferedImage im = g.getTexture();
            Material mat     = g.getMaterial();
            ModelMaterial tm   = new ModelMaterial( im, mat );
            
            if( ( im != null || mat != null ) && !map.containsKey( tm ) ) {
                map.put( tm, idx++ );
            }
        }
        
        return map;
    }
    
    
    public static void correctPolygonOrientation( MeshModel model ) {

        Aabb bounds    = computeBounds(model);
        double centerX = bounds.centerX();
        double centerY = bounds.centerY();
        double centerZ = bounds.centerZ();
        double[] center = {centerX, centerY, centerZ};
        
        for(Group g: model.getGroups()) {

            Set<Triangle> set = new HashSet<Triangle>();
            List<Triangle> t = g.getTrianglesRef();

            if(t.isEmpty())
                continue;
            
            set.addAll(t);
            
            while(set.size() > 0) {
                Set<Triangle> doneSet = new HashSet<Triangle>();
                Set<Triangle> correctSet = new HashSet<Triangle>();
                int awayNum = 1;

                Triangle tt = set.iterator().next();
                set.remove(tt);
            
                if(Triangles.computePerpDistance(tt.vertex(0), tt.vertex(1), tt.vertex(2), center) > 0)
                    tt.reverseOrientation();
                
                correctSet.add(tt);
                awayNum++;
                
                while(correctSet.size() > 0) {
                    tt = correctSet.iterator().next();
                    correctSet.remove(tt);
                    doneSet.add(tt);
                    
                    for(Triangle ttt: set) {
                        
COMPARE_TRIANGLES:                        
                        for(int v1 = 0; v1 < 3; v1++) {
                            for(int v2 = 0; v2 < 3; v2++) {
                         
                                if(tt.vertex(v1) != ttt.vertex(v2))
                                    continue;
                                
                                if(tt.vertex((v1+1) % 3) == ttt.vertex((v2+1) % 3)) {
                                    ttt.reverseOrientation();
                                    correctSet.add(ttt);
                                    set.remove(ttt);
                                    
                                    if(Triangles.computePerpDistance(ttt.vertex(0), ttt.vertex(1), ttt.vertex(2), center) < 0) 
                                        awayNum++;
                                    
                                    break COMPARE_TRIANGLES;
                                }
                                
                                if(tt.vertex((v1+1) % 3) == ttt.vertex((v2+2)%3)) {
                                    correctSet.add(ttt);
                                    set.remove(ttt);
                                    
                                    if(Triangles.computePerpDistance(ttt.vertex(0), ttt.vertex(1), ttt.vertex(2), center) < 0)
                                        awayNum++;
                                    
                                    break COMPARE_TRIANGLES;
                                }
                            }
                        }
                    }
                }
                
                if(awayNum < doneSet.size() / 2) {
                    for(Triangle ttt: doneSet) {
                        ttt.reverseOrientation();
                    }
                }
            }
        }
    }

    
    public static void colorizeRandom( MeshModel model ) {
        Random rand = new Random( System.nanoTime() );
                
        for(Group g: model.getGroups()) {
            for(Triangle t: g.getTriangles()) {
                double[][] c = new double[3][];
                c[0] = new double[]{rand.nextDouble(), rand.nextDouble(), rand.nextDouble()};
                c[1] = c[0];
                c[2] = c[0];
                
                t.setColorRef(c);
            }
        }
    }
    
    
    public static void colorize( MeshModel model, double r, double g, double b, double a ) {
        Random rand = new Random( System.nanoTime() );
        for( Group gr: model.getGroups() ) {
            for( Triangle t: gr.getTriangles() ) {
                double[][] c = new double[3][];
                c[0] = new double[]{r, g, b, a};
                c[1] = new double[]{r, g, b, a};
                c[2] = new double[]{r, g, b, a};
                
                t.setColorRef( c );
            }
        }
    }
   
    
    public static void clipQuick( MeshModel model, Aabb clip ) {
        double[] c = new double[3];
        
        for(Group g: model.getGroups()) {
            List<Triangle> out = new ArrayList<Triangle>();
            
            for(Triangle t: g.getTrianglesRef()) {
                Triangles.computeCenter(t.vertex(0), t.vertex(1), t.vertex(2), c);
                if(clip.contains(c[0], c[1], c[2])) {
                    out.add(t);
                }
            }
            
            g.setTrianglesRef(out);
        }
    }
    
    
    public static void clip(MeshModel model, Aabb clip) {
        for(Group g: model.getGroups()) {
            List<Triangle> tris = clip(g.getTriangles(), clip);
            g.setTrianglesRef(tris);
        }
    }

    
    public static List<Triangle> clip(List<Triangle> tris, Aabb clipCube) {
        List<Triangle> ret = new ArrayList<Triangle>();
        
        int[] ii = new int[3];
        int[] oi = new int[3];
        
        double[][] bounds = new double[][]{{clipCube.minX(), clipCube.maxX()},
                                           {clipCube.minY(), clipCube.maxY()},
                                           {clipCube.minZ(), clipCube.maxZ()}};
        
        for(Triangle t: tris) {
            if(t == null)
                continue;
            
            int inCount = 0;
            int outCount = 0;
            
            double maxClip = Double.NEGATIVE_INFINITY; 
            int dim = 0;
            double plane = 0.0;
            int side = -1;
            
            //Find clipping pane.
            
            for(int vi = 0; vi < 3; vi++) {
                double[] x = t.vertex(vi);
                boolean outed = false;
                
                for(int d = 0; d < 3; d++) {
                    if(x[d] < bounds[d][0]) {
                        double dist = bounds[d][0] - x[d];
                        
                        if(dist > maxClip) {
                            maxClip = dist;
                            dim = d;
                            plane = bounds[d][0];
                            side = -1;
                        }
                        
                        outed = true;
                    
                    }else if(x[d] > bounds[d][1]) {
                        double dist = x[d] - bounds[d][1];
                        
                        if(dist > maxClip) {
                            maxClip = dist;
                            dim = d;
                            plane = bounds[d][1];
                            side = 1;
                        }
                        
                        outed = true;
                    }
                }
                
                if(outed) {
                    oi[outCount++] = vi;
                }else{
                    ii[inCount++] = vi;
                }
            }
            
            switch(inCount) {
            case 1:
            {
                //One point in, two points out.
                double p0 = Math.abs(t.vertex( ii[0]       )[dim] - plane);
                double p1 = Math.abs(t.vertex((ii[0]+1) % 3)[dim] - plane);
                double p2 = Math.abs(t.vertex((ii[0]+2) % 3)[dim] - plane);
                
                Triangle[] chips = Triangles.splitTriangle2(t, ii[0], p0 / (p0 + p1), p0 / (p0 + p2));
                
                //In case of rounding errors, make sure the clip is clean.
                if(side > 0) {
                    double[] x;
                    x = chips[0].vertex(1);
                    x[dim] = Math.min(x[dim], plane);
                    x = chips[0].vertex(2);
                    x[dim] = Math.min(x[dim], plane);
                    
                }else{
                    double[] x;
                    x = chips[0].vertex(1);    
                    x[dim] = Math.max(x[dim], plane);
                    x = chips[0].vertex(2);
                    x[dim] = Math.max(x[dim], plane);
                }

                //The other two chips are culled.
                chips[1] = null;
                chips[2] = null;
                
                //Resegment, in case the triangle passes through more than one bounding plane.
                ret.addAll(clip(Arrays.asList(chips), clipCube));
                break;
            }
            
            case 2:
            {
                //Two points in, one point out.
                double p0 = Math.abs(t.vertex( oi[0]       )[dim] - plane);
                double p1 = Math.abs(t.vertex((oi[0]+1) % 3)[dim] - plane);
                double p2 = Math.abs(t.vertex((oi[0]+2) % 3)[dim] - plane);
                
                Triangle[] chips = Triangles.splitTriangle2(t, oi[0], p0 / (p0 + p1), p0 / (p0 + p2));
                
                //In case of rounding errors, make sure the clip is clean.
                
                if(side > 0) {
                    double[] x;
                    x = chips[1].vertex(1);
                    x[dim] = Math.min(x[dim], plane);
                    x = chips[1].vertex(2);
                    x[dim] = Math.min(x[dim], plane);
                    x = chips[2].vertex(1);
                    x[dim] = Math.min(x[dim], plane);
                }else{
                    double[] x;
                    x = chips[1].vertex(1);
                    x[dim] = Math.max(x[dim], plane);
                    x = chips[1].vertex(2);
                    x[dim] = Math.max(x[dim], plane);
                    x = chips[2].vertex(1);
                    x[dim] = Math.max(x[dim], plane);
                }
                
                chips[0] = null;
                ret.addAll(clip(Arrays.asList(chips), clipCube));
                break;
            }
            
            case 3:
                //All points in.
                ret.add(t);
                break;
            }
        }
        
        return ret;
    }

    
    public static void cullTriangles(MeshModel model, Comparable<Triangle> comp) {
        for(Group g: model.getGroups()) {
            List<Triangle> out = new ArrayList<Triangle>();
            
            for(Triangle t: g.getTrianglesRef()) {
                if(comp.compareTo(t) != 0)
                    out.add(t);
            }
            
            g.setTrianglesRef(out);
        }
    }

    
    public static List<Triangle> findTriangles(MeshModel model, Comparable<Triangle> comp) {
        List<Triangle> ret = new ArrayList<Triangle>();
        
        for(Group g: model.getGroups()) {
            for(Triangle t: g.getTrianglesRef()) {
                if(comp.compareTo(t) != 0)
                    ret.add(t);
            }
        }
        
        return ret;
    }
    
    
    public static void splitLongEdges(MeshModel model, double maxLength) {
        for(Group g: model.getGroups()) {
            List<Triangle> tris = Triangles.splitLongEdges(g.getTrianglesRef(), maxLength, 0);
            g.setTrianglesRef(tris);
        }
    }
    
    
    public static void partition(List<Triangle> tris, Volume manifold, List<Triangle> inside, boolean computeNorms, List<Triangle> outside) {
        List<Triangle> manTris = Triangulations.triangulate(manifold, computeNorms);
        List<Triangle> splitTris = split(tris, manTris, null);
        double[] cent = new double[3];
        
        for(Triangle t: splitTris) {
            Triangles.computeCenter(t.vertex(0), t.vertex(1), t.vertex(2), cent);
            
            if(manifold.contains(cent[0], cent[1], cent[2])) {
                inside.add(t);
            }else{
                outside.add(t);
            }
        }
    }
    
    
    public static List<Triangle> split(List<Triangle> tris, List<Triangle> cuts, List<Triangle> out) {
        if(out == null)
            out = new ArrayList<Triangle>();
        
        if(cuts.size() == 0) {
            out.addAll(tris);
            return out;
        }
        
        List<Triangle> listA = new ArrayList<Triangle>();
        List<Triangle> listB = new ArrayList<Triangle>();
        
        for(int i = 0; i < cuts.size(); i++) {
            List<Triangle> a;
            List<Triangle> b;
            
            if(i % 2 == 0) {
                a = listA;
                b = listB;
            }else{
                a = listB;
                b = listA;
            }
            
            b.clear();
            
            if(i == 0)
                a = tris;
            
            if(i == cuts.size() - 1) {
                b = out;
                listA = null;
                listB = null;
            }
            
            
            Triangle cut = cuts.get(i);
            
            for(Triangle t: a) {
                Triangle[] frac = Triangles.splitTriangle(t, cut, 0.00001);
                
                if(frac == null) {
                    b.add(t);
                }else{
                    for(int j = 0; j < frac.length; j++) {
                        b.add(frac[j]);
                    }
                }
            }
        }
        
        return out;
    }
    

    public static MeshModel scale(MeshModel m, double s) { 
        return scale(m, s, s, s);
    }
    
        
    public static MeshModel scale( MeshModel m, double sx, double sy, double sz ) {
        List<Group> groups = new ArrayList<Group>();

        for (Group g : m.getGroupsRef()) {
        
            List<Triangle> scaledTris = new ArrayList<Triangle>();
            for (Triangle t : g.getTrianglesRef()) {
                scaledTris.add(Triangles.scale(t, sx, sy, sz));
            }
            groups.add( new Group( g.getName(), g.getTexture(), g.getMaterial(), scaledTris ) );
        }
        
        return new MeshModel(groups);
    }
    
    /**
     * Converts a model from Y-up to Z-up
     * @param m
     * @return
     */
    public static MeshModel swapYZ( MeshModel m ) {
        List<Group> groups = new ArrayList<Group>();

        for (Group g : m.getGroupsRef()) {
            List<Triangle> swappedTris = new ArrayList<Triangle>();
        
            for (Triangle t : g.getTrianglesRef()) {
                double[][] v = { t.vertex(0), t.vertex(1), t.vertex(2) };
                double[][] n = { t.normalRef()[0], t.normalRef()[1], t.normalRef()[2] };
                for (int i = 0; i < 3; ++i) {
                    v[i] = new double[] { v[i][2], v[i][0], v[i][1] };
                    n[i] = new double[] { n[i][2], n[i][0], n[i][1] };
                }
                swappedTris.add(new Triangle(v, n, t.texRef()));
            }
            groups.add( new Group( g.getName(), g.getTexture(), g.getMaterial(), swappedTris ) );
        }
        
        return new MeshModel(groups);
    }
        
    
    
    private Models() {}

}

