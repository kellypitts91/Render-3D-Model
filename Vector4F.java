// Class taken from Arno demo with a few methods removed
public class Vector4F {
    public float x;
    public float y;
    public float z;
    public float w;

    public Vector4F(final float xval, final float yval, final float zval,
                    final float wval) {
        x = xval;
        y = yval;
        z = zval;
        w = wval;
    }

    public Vector4F(final Vector4F v) {
        x = v.x;
        y = v.y;
        z = v.z;
        w = v.w;
    }
}
