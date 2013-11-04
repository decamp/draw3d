package bits.draw3d.render;

/**
 * @author decamp
 */
public interface Renderer {
    public void init();
    public void draw();
    public void finish();
    public void dispose();
}
