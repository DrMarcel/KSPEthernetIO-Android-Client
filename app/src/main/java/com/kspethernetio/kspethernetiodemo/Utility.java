package com.kspethernetio.kspethernetiodemo;

import android.content.Context;
import android.widget.Toast;

/**
 * Some utility functions
 */
public class Utility
{
    private static Context applicationContext;

    /**
     * Initialize utilities
     * @param application Application context
     */
    public static void initialize(Context application)
    {
        applicationContext = application;
    }

    /**
     * Show toast message
     * @param message Message string
     */
    public static void showMessage(String message)
    {
        Toast toast = Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG);
        toast.show();
    }
}
