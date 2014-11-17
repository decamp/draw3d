package bits.draw3d;


/**
 * @author Philip DeCamp
 */
public interface DrawUnit extends DrawResource {
    public void bind( DrawEnv d );
    public void unbind( DrawEnv d );
}
