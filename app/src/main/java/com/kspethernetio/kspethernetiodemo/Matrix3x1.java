package com.kspethernetio.kspethernetiodemo;

public class Matrix3x1
{
    private double[] x = {0,0,0};

    public double get(int r)
    {
        return x[r];
    }
    public void set(int r, double v)
    {
        x[r] = v;
    }
}
