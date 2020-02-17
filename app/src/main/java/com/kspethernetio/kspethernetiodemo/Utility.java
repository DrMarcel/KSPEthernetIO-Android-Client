package com.kspethernetio.kspethernetiodemo;

import android.app.Activity;
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
    public static void showMessage(Activity activeActivity, String message)
    {
        staticMessage = message;
        activeActivity.runOnUiThread(doMessage);
    }
    private static String staticMessage;
    private static Runnable doMessage = new Runnable()
    {
        @Override
        public void run()
        {

            Toast toast = Toast.makeText(
                    applicationContext,
                    staticMessage,
                    Toast.LENGTH_LONG);
            toast.show();
        }
    };
}
