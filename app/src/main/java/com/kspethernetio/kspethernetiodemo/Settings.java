package com.kspethernetio.kspethernetiodemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Stores settings in SharedPreferences
 */
public class Settings
{
    private final static String settingsFile = "KSPEthernetIO";
    private SharedPreferences sharedPreferences;

    public Settings(Context application)
    {
        sharedPreferences = application.getSharedPreferences(settingsFile, Context.MODE_PRIVATE);
    }

    public int getPort()
    {
        return sharedPreferences.getInt("port", 2342);
    }
    public void setPort(int port)
    {
        sharedPreferences.edit().putInt("port", port).apply();
    }
    public int getIntervall()
    {
        return sharedPreferences.getInt("intervall", 50);
    }
    public void setIntervall(int intervall)
    {
        sharedPreferences.edit().putInt("intervall", intervall).apply();
    }
    public String getActionName(int n)
    {
        if(n<0 || n>4) return "";
        return sharedPreferences.getString("action"+Integer.toString(n), "Action "+Integer.toString(n+1));
    }
    public void setActionName(int n, String name)
    {
        if(n<0 || n>4) return;
        sharedPreferences.edit().putString("action"+Integer.toString(n), name).apply();
    }

    /**
     * Show setting dialog. Trigger callback if settings changed
     * @param activeActivity Parent activity for the dialog
     * @param callback Called when settings changed
     */
    public void showSettings(Activity activeActivity, final DialogInterface.OnClickListener callback)
    {
        LinearLayout settingsView = new LinearLayout(activeActivity);
        settingsView.setOrientation(LinearLayout.VERTICAL);

        ScrollView scrollView = new ScrollView(activeActivity);
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setVerticalScrollBarEnabled(true);
        scrollView.addView(settingsView);

        TextView labelPort = new TextView(activeActivity);
        labelPort.setText("Port:");
        EditText inputPort = new EditText(activeActivity);
        inputPort.setMaxLines(1);
        inputPort.setMaxEms(16);
        inputPort.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputPort.setText(Integer.toString(getPort()));
        inputPort.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);
        settingsView.addView(labelPort);
        settingsView.addView(inputPort);

        TextView labelIntervall = new TextView(activeActivity);
        labelIntervall.setText("Send intervall (ms):");
        EditText inputIntervall = new EditText(activeActivity);
        inputIntervall.setMaxLines(1);
        inputIntervall.setMaxEms(16);
        inputIntervall.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputIntervall.setText(Integer.toString(getIntervall()));
        inputIntervall.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);
        settingsView.addView(labelIntervall);
        settingsView.addView(inputIntervall);

        EditText[] inputAction = new EditText[5];
        for(int i=0; i<inputAction.length; i++)
        {
            TextView label = new TextView(activeActivity);
            label.setText("Action "+Integer.toString(i+1));
            inputAction[i] = new EditText(activeActivity);
            inputAction[i].setMaxLines(1);
            inputAction[i].setMaxEms(32);
            inputAction[i].setInputType(InputType.TYPE_CLASS_TEXT);
            inputAction[i].setText(getActionName(i));
            inputAction[i].setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);
            settingsView.addView(label);
            settingsView.addView(inputAction[i]);
        }

        final EditText inputPortF = inputPort;
        final EditText inputIntervallF = inputIntervall;
        final EditText inputAction0F = inputAction[0];
        final EditText inputAction1F = inputAction[1];
        final EditText inputAction2F = inputAction[2];
        final EditText inputAction3F = inputAction[3];
        final EditText inputAction4F = inputAction[4];

        new AlertDialog.Builder(activeActivity)
                .setCancelable(false)
                .setTitle("Settings")
                .setView(scrollView)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        try
                        {
                            int newPort = Integer.decode(inputPortF.getText().toString());
                            int newIntervall = Integer.decode(inputIntervallF.getText().toString());
                            String newAction[] = new String[5];
                            newAction[0] = inputAction0F.getText().toString();
                            newAction[1] = inputAction1F.getText().toString();
                            newAction[2] = inputAction2F.getText().toString();
                            newAction[3] = inputAction3F.getText().toString();
                            newAction[4] = inputAction4F.getText().toString();

                            setPort(newPort);
                            setIntervall(newIntervall);
                            for(int i=0; i<newAction.length; i++)
                                setActionName(i, newAction[i]);

                            Utility.showMessage("Settings saved!");

                            callback.onClick(dialog, which);
                        }
                        catch(Exception e)
                        {
                            Utility.showMessage("Settings not saved: Wrong input format!");
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.menu_settings)
                .show();
    }
}
