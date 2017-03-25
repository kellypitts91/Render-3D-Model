// Class taken from Arno demo with a few methods removed
public class Vector3F {
    public float x, y, z;

    Vector3F() {
        x = y = z = 0.0f;
    }

    public Vector3F minus(final Vector3F rhs, Vector3F out) {
        out.x = x - rhs.x;
        out.y = y - rhs.y;
        out.z = z - rhs.z;
        return out;
    }

    public Vector3F normalize() {
        final double magnitudeSqrd = x * x + y * y + z * z;
        if (magnitudeSqrd == 0.) return this;

        final double magnitude = Math.sqrt(magnitudeSqrd);
        x /= magnitude;
        y /= magnitude;
        z /= magnitude;

        return this;
    }

    public static Vector3F crossProduct(final Vector3F v1, final Vector3F v2, Vector3F out) {
        out.x = v1.y * v2.z - v1.z * v2.y;
        out.y = v1.z * v2.x - v1.x * v2.z;
        out.z = v1.x * v2.y - v1.y * v2.x;
        return out;
    }
}
