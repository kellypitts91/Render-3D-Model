/**
 * Class taken from Arno demo with a few methods removed
 * A 4x4 single-precision floating point matrix in column-major layout.
 */
public class Matrix4F {
    public final float[] m;

    /**
     * Initialises the matrix with the identity matrix.
     */
    public Matrix4F() {
        m = new float[16];
        m[0] = 1.f;
        m[5] = 1.f;
        m[10] = 1.f;
        m[15] = 1.f;
    }

    public static Matrix4F createTranslateInstance(final float dx, final float dy,
                                                   final float dz) {
        final Matrix4F mat = new Matrix4F();
        mat.m[12] = dx;
        mat.m[13] = dy;
        mat.m[14] = dz;
        return mat;
    }

    public static Matrix4F createScaleInstance(final float sx, final float sy,
                                               final float sz) {
        final Matrix4F mat = new Matrix4F();
        mat.m[0] = sx;
        mat.m[5] = sy;
        mat.m[10] = sz;
        return mat;
    }

    public static Matrix4F createRotateXInstance(final float theta) {
        final Matrix4F mat = new Matrix4F();
        final float cos = (float) Math.cos((double) theta);
        final float sin = (float) Math.sin((double) theta);
        mat.m[5] = cos;
        mat.m[9] = -sin;
        mat.m[6] = sin;
        mat.m[10] = cos;
        return mat;
    }

    public static Matrix4F createRotateYInstance(final float theta) {
        final Matrix4F mat = new Matrix4F();
        final float cos = (float) Math.cos((double) theta);
        final float sin = (float) Math.sin((double) theta);
        mat.m[0] = cos;
        mat.m[8] = sin;
        mat.m[2] = -sin;
        mat.m[10] = cos;
        return mat;
    }

    public static Matrix4F createRotateZInstance(final float theta) {
        final Matrix4F mat = new Matrix4F();
        final float cos = (float) Math.cos((double) theta);
        final float sin = (float) Math.sin((double) theta);
        mat.m[0] = cos;
        mat.m[4] = -sin;
        mat.m[1] = sin;
        mat.m[5] = cos;
        return mat;
    }

    /**
     * Multiplies this by rhs and returns the result in a new matrix instance.
     *
     * @param rhs The other matrix.
     * @return A new matrix that is the matrix product of this by rhs.
     */
    public Matrix4F multiply(final Matrix4F rhs) {
        return multiply(rhs, new Matrix4F());
    }

    /**
     * Multiplies this by rhs and returns the result in out.
     *
     * @param rhs The other matrix.
     * @param out The output matrix.
     * @return The matrix given in out.
     */
    public Matrix4F multiply(final Matrix4F rhs, final Matrix4F out) {
        int outIdx;
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                outIdx = i + j * 4;
                out.m[outIdx] = 0.f;
                for (int k = 0; k < 4; ++k) {
                    out.m[outIdx] += m[i + k * 4] * rhs.m[k + j * 4];
                }
            }
        }
        return out;
    }

    /**
     * Multiplies this this by rhs and returns the result in out.
     *
     * @param rhs The column vector to multiply by.
     * @param out The result vector.
     * @return The vector given in out.
     */
    public Vector4F multiply(final Vector4F rhs, final Vector4F out) {
        out.x = m[0] * rhs.x + m[4] * rhs.y + m[8] * rhs.z + m[12] * rhs.w;
        out.y = m[1] * rhs.x + m[5] * rhs.y + m[9] * rhs.z + m[13] * rhs.w;
        out.z = m[2] * rhs.x + m[6] * rhs.y + m[10] * rhs.z + m[14] * rhs.w;
        out.w = m[3] * rhs.x + m[7] * rhs.y + m[11] * rhs.z + m[15] * rhs.w;

        return out;
    }
}
