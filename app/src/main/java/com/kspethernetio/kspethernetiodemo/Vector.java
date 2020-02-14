package com.kspethernetio.kspethernetiodemo;

public class Vector
{
    public double x, y, z;

    /**
     * Create new vector
     */
    public Vector()
    {
        x=y=z=0.0f;
    }

    /**
     * Create vector with initial parameters
     * @param x X
     * @param y Y
     * @param z Z
     */
    public Vector(double x, double y, double z)
    {
        this.x=x;
        this.y=y;
        this.z=z;
    }


    /**
     * Get value.
     * r:selector
     * r=0: X
     * r=1: Y
     * r=2: Z
     * @param r Selector
     * @return Value
     */
    public double get(int r)
    {
        if(r==0) return x;
        if(r==1) return y;
        if(r==2) return z;
        return 0;
    }

    /**
     * Set value.
     * r:selector
     * r=0: X
     * r=1: Y
     * r=2: Z
     * @param r Selector
     * @param v Value
     */
    public void set(int r, double v)
    {
        if(r==0) x=v;
        if(r==1) y=v;
        if(r==2) z=v;
    }

    /**
     * Calculate AxB
     * @param b Second vector
     * @return Crossproduct AxB
     */
    public Vector crossproduct(Vector b)
    {
        Vector a = this;
        double nx = a.y*b.z-a.z*b.y;
        double ny = a.z*b.x-a.x*b.z;
        double nz = a.x*b.y-a.y*b.x;
        return new Vector(nx,ny,nz);
    }

    /**
     * Get point on sphere with radius 1
     * @param alpha Angle between vector and Z Axis
     * @param beta Angle between vector and X Axis
     * @return
     */
    public static Vector point(double alpha, double beta)
    {
        Vector v = new Vector();
        v.z=Math.cos(alpha);
        v.x=Math.sin(alpha)*Math.sin(beta);
        v.y=Math.sin(alpha)*Math.cos(beta);
        return v;
    }

    /**
     * Vector to String
     * @return String representing the vector
     */
    public String toString()
    {
        return "{"+x+","+y+","+z+"}";
    }
}
