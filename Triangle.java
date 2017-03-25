// Class taken from Arno demo with a few methods removed
public class Triangle {
    public final Vector4F[] v; // Vertices
    public final Vector3F normal;

    private static Vector3F tmpV1 = new Vector3F();
    private static Vector3F tmpV2 = new Vector3F();
    private static Vector3F tmpV3 = new Vector3F();
    private static Vector3F vec1 = new Vector3F();
    private static Vector3F vec2 = new Vector3F();

    public Triangle(Vector4F vertex1, Vector4F vertex2, Vector4F vertex3) {
        v = new Vector4F[3];
        v[0] = vertex1;
        v[1] = vertex2;
        v[2] = vertex3;
        normal = new Vector3F();
    }

    public void calculateNormal() {
        tmpV1.x = v[0].x;
        tmpV1.y = v[0].y;
        tmpV1.z = v[0].z;
        tmpV2.x = v[1].x;
        tmpV2.y = v[1].y;
        tmpV2.z = v[1].z;
        tmpV3.x = v[2].x;
        tmpV3.y = v[2].y;
        tmpV3.z = v[2].z;
        Vector3F.crossProduct(
                tmpV2.minus(tmpV1, vec1),
                tmpV3.minus(tmpV1, vec2),
                normal);
        normal.normalize();
    }
}
