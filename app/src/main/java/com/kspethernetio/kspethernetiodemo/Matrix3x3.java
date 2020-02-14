package com.kspethernetio.kspethernetiodemo;

public class Matrix3x3
{
    private double[][] x = {{0,0,0},{0,0,0},{0,0,0}};


    /**
     * Get value at row,column
     * @param r Row
     * @param c Column
     * @return Value
     */
    public double get(int r, int c)
    {
        return x[c][r];
    }

    /**
     * Set value at row,column
     * @param r Row
     * @param c Column
     * @return Value
     */
    public void set(int r, int c, double v)
    {
        x[c][r] = v;
    }

    /**
     * Multiply with other matrix
     * @param m Matrix B
     * @return Product AxB
     */
    public Matrix3x3 mult(Matrix3x3 m)
    {
        Matrix3x3 p = new Matrix3x3();

        for(int c=0; c<3; c++)
            for(int r=0; r<3; r++)
                for(int i=0; i<3; i++) p.x[c][r] += x[i][r] * m.x[c][i];

        return p;
    }

    /**
     * Multiply Matrix with Vector
     * @param v Vector
     * @return Result vector
     */
    public Vector mult(Vector v)
    {
        Vector p = new Vector();

        for(int r=0; r<3; r++)
            for(int i=0; i<3; i++) p.set(r, p.get(r) + x[i][r] * v.get(i));

        return p;
    }

    /**
     * Create roll transformation matrix
     * @param gamma Angle
     * @return Transformation matrix
     */
    public static Matrix3x3 roll(double gamma)
    {
        Matrix3x3 p = new Matrix3x3();
        p.set(0, 0, 1);
        p.set(1, 1, Math.cos(gamma));
        p.set(2, 1, -Math.sin(gamma));
        p.set(1, 2, Math.sin(gamma));
        p.set(2, 2, Math.cos(gamma));
        return p;
    }

    /**
     * Create pitch transformation matrix
     * @param beta Angle
     * @return Transformation matrix
     */
    public static Matrix3x3 pitch(double beta)
    {
        Matrix3x3 p = new Matrix3x3();
        p.set(1, 1, 1);
        p.set(0, 0, Math.cos(beta));
        p.set(2, 0, Math.sin(beta));
        p.set(0, 2, -Math.sin(beta));
        p.set(2, 2, Math.cos(beta));
        return p;
    }

    /**
     * Create yaw transformation matrix
     * @param alpha Angle
     * @return Transformation matrix
     */
    public static Matrix3x3 yaw(double alpha)
    {
        Matrix3x3 p = new Matrix3x3();
        p.set(2, 2, 1);
        p.set(0, 0, Math.cos(alpha));
        p.set(1, 0, -Math.sin(alpha));
        p.set(0, 1, Math.sin(alpha));
        p.set(1, 1, Math.cos(alpha));
        return p;
    }

    /**
     * Create transformation matrix from quaterion
     * @param x Quaterion X
     * @param y Quaterion Y
     * @param z Quaterion Z
     * @param w Quaterion W
     * @return Transformation matrix
     */
    public static Matrix3x3 fromQuaterion(double x, double y, double z, double w)
    {
        Matrix3x3 p = new Matrix3x3();
        p.set(0, 0, 1-2*y*y-2*z*z);
        p.set(1, 0, 2*x*y - 2*z*w);
        p.set(2, 0, 2*x*z + 2*y*w);
        p.set(0, 1, 2*x*y + 2*z*w);
        p.set(1, 1, 1-2*x*x-2*z*z);
        p.set(2, 1, 2*x*y - 2*x*w);
        p.set(0, 2, 2*x*z - 2*y*w);
        p.set(1, 2, 2*y*z + 2*x*w);
        p.set(2, 2, 1-2*x*x-2*y*y);
        return p;
    }

    /**
     * Create combined transformation matrix from roll, pitch and yaw.
     * Various functions differ in the order the transformation is apllied
     * @param roll Roll
     * @param pitch Pitch
     * @param yaw Yaw
     * @return Transformation matrix
     */
    public static Matrix3x3 ypr(double roll, double pitch, double yaw)
    {
        Matrix3x3 p = yaw(yaw).mult(pitch(pitch).mult(roll(roll)));
        return p;
    }
    public static Matrix3x3 pry(double roll, double pitch, double yaw)
    {
        Matrix3x3 p = pitch(pitch).mult(roll(roll).mult(yaw(yaw)));
        return p;
    }
    public static Matrix3x3 ryp(double roll, double pitch, double yaw)
    {
        Matrix3x3 p = roll(roll).mult(yaw(yaw).mult(pitch(pitch)));
        return p;
    }
    public static Matrix3x3 yrp(double roll, double pitch, double yaw)
    {
        Matrix3x3 p = yaw(yaw).mult(roll(roll).mult(pitch(pitch)));
        return p;
    }
    public static Matrix3x3 pyr(double roll, double pitch, double yaw)
    {
        Matrix3x3 p = pitch(pitch).mult(yaw(yaw).mult(roll(roll)));
        return p;
    }
    public static Matrix3x3 rpy(double roll, double pitch, double yaw)
    {
        Matrix3x3 p = roll(roll).mult(pitch(pitch).mult(yaw(yaw)));
        return p;
    }

}
