package com.kspethernetio.kspethernetiodemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.KSPEthernetClient;

import java.util.concurrent.Semaphore;



//TODO Add more orbital infos
//TODO Centralize all coloring in config files
//TODO Tests on different screen sizes
//TODO Clean compiler warnings
//TODO Buffer incoming data packets with adaptive playout to get "smooth" data stream
//TODO Rendering Navball is very CPU intense - Implement OpenGL rendering
//TODO Split FlightInfo class in some smaller classes


public class FlightInfo extends AppCompatActivity
{
    Activity activeActivity;

    Settings settings;

    Semaphore navballLock = new Semaphore(1);
    Navball navball = new Navball();
    Bitmap navballBitmap;

    Button buttonPower;
    Button buttonSettings;
    Button buttonInfo;
    Button buttonConnect;

    Button buttonAltitude;
    Button buttonVelocity;
    Button buttonManeuver;
    Button buttonTarget;
    Button buttonStage;
    Button buttonAbort;
    Button buttonMap;
    Button buttonCamera;
    Button buttonMenu;
    
    ToggleButton toggleAction1;
    ToggleButton toggleAction2;
    ToggleButton toggleAction3;
    ToggleButton toggleAction4;
    ToggleButton toggleAction5;
    ToggleButton toggleActionLight;
    ToggleButton toggleActionBrakes;
    ToggleButton toggleActionGears;
    ToggleButton toggleRCS;

    ToggleButton toggleSAS;
    ToggleButton toggleSASPro;
    ToggleButton toggleSASRet;
    ToggleButton toggleSASNor;
    ToggleButton toggleSASANo;
    ToggleButton toggleSASRIn;
    ToggleButton toggleSASROu;
    ToggleButton toggleSASTar;
    ToggleButton toggleSASAta;
    ToggleButton toggleSASMan;
    
    ImageView imageAltitude;
    ImageView imageNavball;
    
    ImageView imageFuelSF;
    ImageView imageFuelLF;
    ImageView imageFuelOX;
    ImageView imageFuelXE;
    ImageView imageFuelMP;
    ImageView imageFuelES;

    TextView textStatus;

    TextView textAP;
    TextView textPE;
    TextView textAPT;
    TextView textPET;

    KSPEthernetClient client;

    boolean terrainAltitude = false;

    Semaphore updateUiLock = new Semaphore(1);
    Semaphore vesselDataLock = new Semaphore(1);
    DataPackets.VesselData vesselData;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();

        activeActivity = this;

        setContentView(R.layout.activity_flight_info);

        settings = new Settings(getApplicationContext());
        Utility.initialize(getApplicationContext());
        initializeViews();
        updateViews(true);
        initializeClient();

    }

    private void initializeClient()
    {
        if(client!=null)
        {
            client.removeEventListener(clientListener);
            client.destroy();
        }
        client = new KSPEthernetClient(settings.getPort(), settings.getIntervall());
        client.addEventListener(clientListener);
        updateUi.run();
    }
    
    private void initializeViews()
    {
        buttonPower = findViewById(R.id.buttonPower);
        buttonSettings = findViewById(R.id.buttonSettings);
        buttonInfo = findViewById(R.id.buttonInfo);
        buttonConnect = findViewById(R.id.buttonConnect);

        buttonAltitude = findViewById(R.id.buttonAltitude);
        buttonVelocity = findViewById(R.id.buttonVelocity);
        buttonManeuver = findViewById(R.id.buttonManeuver);
        buttonTarget = findViewById(R.id.buttonTarget);
        buttonStage = findViewById(R.id.buttonStage);
        buttonAbort = findViewById(R.id.buttonAbort);
        buttonCamera = findViewById(R.id.buttonCamera);
        buttonMap = findViewById(R.id.buttonMap);
        buttonMenu = findViewById(R.id.buttonMenu);

        toggleAction1 = findViewById(R.id.toggleButtonAG1);
        toggleAction2 = findViewById(R.id.toggleButtonAG2);
        toggleAction3 = findViewById(R.id.toggleButtonAG3);
        toggleAction4 = findViewById(R.id.toggleButtonAG4);
        toggleAction5 = findViewById(R.id.toggleButtonAG5);
        toggleActionLight = findViewById(R.id.toggleButtonLight);
        toggleActionBrakes = findViewById(R.id.toggleButtonBrakes);
        toggleActionGears = findViewById(R.id.toggleButtonGears);
        toggleRCS = findViewById(R.id.toggleButtonRCS);
        toggleSAS = findViewById(R.id.toggleButtonSAS);
        toggleSASPro = findViewById(R.id.toggleButtonPrograde);
        toggleSASRet = findViewById(R.id.toggleButtonRetrograde);
        toggleSASNor = findViewById(R.id.toggleButtonNormal);
        toggleSASANo = findViewById(R.id.toggleButtonAntinormal);
        toggleSASRIn = findViewById(R.id.toggleButtonRadialIn);
        toggleSASROu = findViewById(R.id.toggleButtonRadialOut);
        toggleSASTar = findViewById(R.id.toggleButtonTarget);
        toggleSASAta = findViewById(R.id.toggleButtonAntiTarget);
        toggleSASMan = findViewById(R.id.toggleButtonManeuver);

        imageAltitude = findViewById(R.id.imageViewAltitude);
        imageNavball = findViewById(R.id.imageViewNavball);

        imageFuelSF = findViewById(R.id.imageViewSF);
        imageFuelLF = findViewById(R.id.imageViewLF);
        imageFuelOX = findViewById(R.id.imageViewOX);
        imageFuelXE = findViewById(R.id.imageViewXE);
        imageFuelMP = findViewById(R.id.imageViewMP);
        imageFuelES = findViewById(R.id.imageViewES);

        textStatus = findViewById(R.id.textViewStatus);
        textAP = findViewById(R.id.textViewAP);
        textPE = findViewById(R.id.textViewPE);
        textAPT = findViewById(R.id.textViewAPT);
        textPET = findViewById(R.id.textViewPET);

        buttonPower.setOnClickListener(menuButtonListener);
        buttonSettings.setOnClickListener(menuButtonListener);
        buttonInfo.setOnClickListener(menuButtonListener);
        buttonConnect.setOnClickListener(menuButtonListener);

        buttonAltitude.setOnClickListener(actionButtonListener);
        buttonVelocity.setOnClickListener(actionButtonListener);
        buttonManeuver.setOnClickListener(actionButtonListener);
        buttonTarget.setOnClickListener(actionButtonListener);
        buttonStage.setOnClickListener(actionButtonListener);
        buttonAbort.setOnClickListener(actionButtonListener);
        buttonMap.setOnClickListener(actionButtonListener);
        buttonCamera.setOnClickListener(actionButtonListener);
        buttonMenu.setOnClickListener(actionButtonListener);

        toggleAction1.setOnCheckedChangeListener(actionToggleListener);
        toggleAction2.setOnCheckedChangeListener(actionToggleListener);
        toggleAction3.setOnCheckedChangeListener(actionToggleListener);
        toggleAction4.setOnCheckedChangeListener(actionToggleListener);
        toggleAction5.setOnCheckedChangeListener(actionToggleListener);
        toggleActionLight.setOnCheckedChangeListener(actionToggleListener);
        toggleActionBrakes.setOnCheckedChangeListener(actionToggleListener);
        toggleActionGears.setOnCheckedChangeListener(actionToggleListener);
        toggleRCS.setOnCheckedChangeListener(actionToggleListener);
        toggleSAS.setOnCheckedChangeListener(actionToggleListener);

        toggleSASPro.setOnCheckedChangeListener(actionSASSelectListener);
        toggleSASRet.setOnCheckedChangeListener(actionSASSelectListener);
        toggleSASNor.setOnCheckedChangeListener(actionSASSelectListener);
        toggleSASANo.setOnCheckedChangeListener(actionSASSelectListener);
        toggleSASRIn.setOnCheckedChangeListener(actionSASSelectListener);
        toggleSASROu.setOnCheckedChangeListener(actionSASSelectListener);
        toggleSASTar.setOnCheckedChangeListener(actionSASSelectListener);
        toggleSASAta.setOnCheckedChangeListener(actionSASSelectListener);
        toggleSASMan.setOnCheckedChangeListener(actionSASSelectListener);

        buttonMenu.setOnLongClickListener(actionButtonHoldListener);

        setActionNames();
    }

    private void setActionNames()
    {
        toggleAction1.setTextOn(settings.getActionName(0));
        toggleAction2.setTextOn(settings.getActionName(1));
        toggleAction3.setTextOn(settings.getActionName(2));
        toggleAction4.setTextOn(settings.getActionName(3));
        toggleAction5.setTextOn(settings.getActionName(4));
        toggleAction1.setTextOff(settings.getActionName(0));
        toggleAction2.setTextOff(settings.getActionName(1));
        toggleAction3.setTextOff(settings.getActionName(2));
        toggleAction4.setTextOff(settings.getActionName(3));
        toggleAction5.setTextOff(settings.getActionName(4));
    }
    
    private void updateViews(boolean inactive)
    {
        boolean enable = !inactive;
        
        buttonAltitude.setEnabled(enable);
        buttonVelocity.setEnabled(enable);
        buttonManeuver.setEnabled(enable);
        buttonTarget.setEnabled(enable);
        buttonStage.setEnabled(enable);
        buttonAbort.setEnabled(enable);
        buttonMap.setEnabled(enable);
        buttonCamera.setEnabled(enable);
        buttonMenu.setEnabled(enable);

        toggleAction1.setEnabled(enable);
        toggleAction2.setEnabled(enable);
        toggleAction3.setEnabled(enable);
        toggleAction4.setEnabled(enable);
        toggleAction5.setEnabled(enable);
        toggleActionLight.setEnabled(enable);
        toggleActionBrakes.setEnabled(enable);
        toggleActionGears.setEnabled(enable);
        toggleRCS.setEnabled(enable);

        toggleSAS.setEnabled(enable);
        toggleSASPro.setEnabled(enable);
        toggleSASRet.setEnabled(enable);
        toggleSASNor.setEnabled(enable);
        toggleSASANo.setEnabled(enable);
        toggleSASRIn.setEnabled(enable);
        toggleSASROu.setEnabled(enable);
        toggleSASTar.setEnabled(enable);
        toggleSASAta.setEnabled(enable);
        toggleSASMan.setEnabled(enable);

        toggleSAS.bringToFront();
        toggleRCS.bringToFront();
        buttonVelocity.bringToFront();
        buttonManeuver.bringToFront();
        buttonTarget.bringToFront();

        if(client == null || !client.isActive()) buttonConnect.setVisibility(View.VISIBLE);
        else buttonConnect.setVisibility(View.INVISIBLE);

        if(client != null)
        {
            if(client.isStopped()) buttonConnect.setText("Connect");
            else buttonConnect.setText("Cancel");
        }

        if(client != null) textStatus.setText(client.getState());
        if(client == null || client.isActive()) textStatus.setVisibility(View.INVISIBLE);
        else textStatus.setVisibility(View.VISIBLE);

        if(inactive)
        {

            if(client == null || !client.isActive()) buttonAltitude.setText("Not connected");
            else buttonAltitude.setText("Connected");
            buttonVelocity.setText("");
            buttonManeuver.setText("");
            buttonTarget.setText("");
            Bitmap empty = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);
            imageFuelSF.setImageBitmap(empty);
            imageFuelLF.setImageBitmap(empty);
            imageFuelOX.setImageBitmap(empty);
            imageFuelXE.setImageBitmap(empty);
            imageFuelMP.setImageBitmap(empty);
            imageFuelES.setImageBitmap(empty);
            imageNavball.setImageBitmap(empty);

            toggleAction1.setChecked(false);
            toggleAction2.setChecked(false);
            toggleAction3.setChecked(false);
            toggleAction4.setChecked(false);
            toggleAction5.setChecked(false);
            toggleActionLight.setChecked(false);
            toggleActionBrakes.setChecked(false);
            toggleActionGears.setChecked(false);
            toggleRCS.setChecked(false);
            toggleSAS.setChecked(false);


            textAP.setText("-");
            textPE.setText("-");
            textAPT.setText("");
            textPET.setText("");
        }
    }


    private View.OnClickListener menuButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch(v.getId())
            {
                case R.id.buttonConnect:
                    if(client!=null && client.isStopped()) client.start();
                    else if(client!=null && !client.isStopped()) client.stop();
                    break;
                case R.id.buttonPower:
                    finish();
                    System.exit(0);
                    break;
                case R.id.buttonInfo:
                    showInfo();
                    break;
                case R.id.buttonSettings:
                    settings.showSettings(activeActivity, settingsChanged);
                    break;
            }
        }
    };

    private DialogInterface.OnClickListener settingsChanged = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            setActionNames();
            initializeClient();
        }
    };

    private void showInfo()
    {
        new AlertDialog.Builder(activeActivity)
                .setTitle("Info")
                .setMessage(
                        "KSPEthernetIO Android demo\n" +
                                "\n" +
                                "Android sample client for the Kerbal Space Program KSPEthernetIO plugin. " +
                                "KSPEthernetIO is an Open-Source tool to export data from Kerbal Space Program via Ethernet. " +
                                "The plugin is based on a similar project KSPSerialIO originally written by zitronen. \n" +
                                "This is only a sample application to show how to receive data from KSPEthernetIO. " +
                                "Feel free to contact me with feedback, questions or bug reports. \n" +
                                "Have fun and a good flight!\n" +
                                "\n" +
                                "Author: DrMarcel\n" +
                                "Contact: kspethernetio@mail.perske.eu\n" +
                                "GitHub: https://github.com/DrMarcel/KSPEthernetIO-Android-Client\n" +
                                "License: CC BY 4.0\n" +
                                "\n" +
                                "https://creativecommons.org/licenses/by/4.0/\n" +
                                "\n" +
                                "Icons under CC BY 4.0 License\n" +
                                "https://www.pngrepo.com/")
                .setPositiveButton("Close",null)
                //.setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.menu_info)
                .show();
    }



    private View.OnLongClickListener actionButtonHoldListener = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(View v)
        {
            switch(v.getId())
            {
                case R.id.buttonMenu:
                    client.controlData.toggleMenu();
                    return true;
                default:
                    return false;
            }
        }
    };

    private View.OnClickListener actionButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch(v.getId())
            {
                case R.id.buttonAltitude:
                    terrainAltitude = !terrainAltitude;
                    updateUi.run();
                    break;
                case R.id.buttonVelocity:
                    if(vesselDataLock.tryAcquire())
                    {
                        client.controlData.rotateNavballMode(vesselData.isTargetSet());
                        vesselDataLock.release();
                    }
                    break;
                case R.id.buttonAbort:
                    client.controlData.setAbort();
                    break;
                case R.id.buttonStage:
                    client.controlData.setStage();
                    break;
                case R.id.buttonManeuver:
                    break;
                case R.id.buttonTarget:
                    break;
                case R.id.buttonMap:
                    client.controlData.toggleMap();
                    break;
                case R.id.buttonCamera:
                    client.controlData.rotateCamMode();
                    break;
                case R.id.buttonMenu:
                    client.controlData.rotateUiMode();
                    break;
                default:
                    break;
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener actionToggleListener = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton v, boolean isChecked)
        {
            if(!v.isPressed()) return;
            v.setChecked(!isChecked);
            switch(v.getId())
            {
                case R.id.toggleButtonLight:
                    client.controlData.setLight(isChecked);
                    break;
                case R.id.toggleButtonBrakes:
                    client.controlData.setBrakes(isChecked);
                    break;
                case R.id.toggleButtonGears:
                    client.controlData.setGears(isChecked);
                    break;
                case R.id.toggleButtonAG1:
                    client.controlData.setActionGroup(0,isChecked);
                    break;
                case R.id.toggleButtonAG2:
                    client.controlData.setActionGroup(1,isChecked);
                    break;
                case R.id.toggleButtonAG3:
                    client.controlData.setActionGroup(2,isChecked);
                    break;
                case R.id.toggleButtonAG4:
                    client.controlData.setActionGroup(3,isChecked);
                    break;
                case R.id.toggleButtonAG5:
                    client.controlData.setActionGroup(4,isChecked);
                    break;
                case R.id.toggleButtonSAS:
                    client.controlData.setSAS(isChecked);
                    if(!isChecked) client.controlData.setSASMode(DataPackets.SASMode.Off);
                    else client.controlData.setSASMode(DataPackets.SASMode.Regular);
                    break;
                case R.id.toggleButtonRCS:
                    client.controlData.setRCS(isChecked);
                    break;

            }
        }
    };

    private CompoundButton.OnCheckedChangeListener actionSASSelectListener = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton v, boolean isChecked)
        {
            if(!v.isPressed()) return;
            v.setChecked(!isChecked);
            if(!isChecked) client.controlData.setSASMode(DataPackets.SASMode.Regular);
            else switch(v.getId())
            {
                case R.id.toggleButtonPrograde:
                    client.controlData.setSASMode(DataPackets.SASMode.Prograde);
                    break;
                case R.id.toggleButtonRetrograde:
                    client.controlData.setSASMode(DataPackets.SASMode.Retrograde);
                    break;
                case R.id.toggleButtonNormal:
                    client.controlData.setSASMode(DataPackets.SASMode.Normal);
                    break;
                case R.id.toggleButtonAntinormal:
                    client.controlData.setSASMode(DataPackets.SASMode.Antinormal);
                    break;
                case R.id.toggleButtonRadialIn:
                    client.controlData.setSASMode(DataPackets.SASMode.RadialIn);
                    break;
                case R.id.toggleButtonRadialOut:
                    client.controlData.setSASMode(DataPackets.SASMode.RadialOut);
                    break;
                case R.id.toggleButtonTarget:
                    client.controlData.setSASMode(DataPackets.SASMode.Target);
                    break;
                case R.id.toggleButtonAntiTarget:
                    client.controlData.setSASMode(DataPackets.SASMode.AntiTarget);
                    break;
                case R.id.toggleButtonManeuver:
                    client.controlData.setSASMode(DataPackets.SASMode.Maneuver);
                    break;
                default:
                    client.controlData.setSASMode(DataPackets.SASMode.Regular);
                    break;
            }
        }
    };

    private KSPEthernetClient.KSPEthernetListener clientListener = new KSPEthernetClient.KSPEthernetListener()
    {
        @Override
        public void onKSPEthernetError(KSPEthernetClient sender, Exception e)
        {

            Utility.showMessage(activeActivity,"Ethernet client error: "+e.getMessage());
        }

        @Override
        public void onKSPEthernetInvalidate(KSPEthernetClient sender, DataPackets.VesselData data)
        {
            try
            {
                vesselDataLock.acquire();
                vesselData = data;
                vesselDataLock.release();

                activeActivity.runOnUiThread(updateUi);
            }
            catch(InterruptedException e)
            {

            }
        }

        @Override
        public void onKSPEthernetStateChanged(KSPEthernetClient sender, String state)
        {
            try
            {
                vesselDataLock.acquire();
                client.controlData.forceResync();
                vesselData=null;
                vesselDataLock.release();

                runOnUiThread(updateUi);
            }
            catch(InterruptedException e)
            {

            }
        }

        @Override
        public void onKSPEthernetHostStateChanged(KSPEthernetClient sender, DataPackets.HostState state)
        {
            switch(state)
            {
                case InFlight:
                    Utility.showMessage(activeActivity,"Flight started");
                    break;
                case NotInFlight:
                    Utility.showMessage(activeActivity,"Flight stopped");
                    try
                    {
                        vesselDataLock.acquire();
                        client.controlData.forceResync();
                        vesselData=null;
                        vesselDataLock.release();

                        runOnUiThread(updateUi);
                    }
                    catch(InterruptedException e)
                    {

                    }
                    break;
                default:
                    //Unknown host state
                    break;
            }
        }
    };

    Runnable updateStatus = new Runnable()
    {
        @Override
        public void run()
        {
        }
    };

    Runnable updateUi = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                updateUiLock.acquire();

                updateStatus.run();

                DataPackets.VesselData data = null;
                try
                {
                    vesselDataLock.acquire();
                    data = vesselData;
                    vesselDataLock.release();
                }
                catch(InterruptedException e)
                {
                    updateUiLock.release();
                    return;
                }


                if(data!=null)
                {
                    updateViews(false);

                    toggleAction1.setChecked(data.getActionGroup(0));
                    toggleAction2.setChecked(data.getActionGroup(1));
                    toggleAction3.setChecked(data.getActionGroup(2));
                    toggleAction4.setChecked(data.getActionGroup(3));
                    toggleAction5.setChecked(data.getActionGroup(4));

                    toggleActionLight.setChecked(data.getLight());
                    toggleActionBrakes.setChecked(data.getBrakes());
                    toggleActionGears.setChecked(data.getGears());

                    toggleSAS.setChecked(data.getSAS());
                    toggleRCS.setChecked(data.getRCS());


                    toggleSASPro.setEnabled(data.getSAS() && !(data.getNavballMode()== DataPackets.NavballMode.Target));
                    toggleSASRet.setEnabled(data.getSAS() && !(data.getNavballMode()== DataPackets.NavballMode.Target));
                    toggleSASNor.setEnabled(data.getSAS());
                    toggleSASANo.setEnabled(data.getSAS());
                    toggleSASRIn.setEnabled(data.getSAS());
                    toggleSASROu.setEnabled(data.getSAS());
                    toggleSASTar.setEnabled(data.getSAS() && data.isTargetSet());
                    toggleSASAta.setEnabled(data.getSAS() && data.isTargetSet());
                    toggleSASMan.setEnabled(data.getSAS() && data.isManeuverSet());
                    
                    toggleSASPro.setChecked(false);
                    toggleSASRet.setChecked(false);
                    toggleSASNor.setChecked(false);
                    toggleSASANo.setChecked(false);
                    toggleSASRIn.setChecked(false);
                    toggleSASROu.setChecked(false);
                    toggleSASTar.setChecked(false);
                    toggleSASAta.setChecked(false);
                    toggleSASMan.setChecked(false);
                    switch(data.getSASMode())
                    {
                        case Prograde:
                            toggleSASPro.setChecked(true);
                            break;
                        case Retrograde:
                            toggleSASRet.setChecked(true);
                            break;
                        case Normal:
                            toggleSASNor.setChecked(true);
                            break;
                        case Antinormal:
                            toggleSASANo.setChecked(true);
                            break;
                        case RadialIn:
                            toggleSASRIn.setChecked(true);
                            break;
                        case RadialOut:
                            toggleSASROu.setChecked(true);
                            break;
                        case Target:
                            toggleSASTar.setChecked(true);
                            break;
                        case AntiTarget:
                            toggleSASAta.setChecked(true);
                            break;
                        case Maneuver:
                            toggleSASMan.setChecked(true);
                            break;
                        default:
                            break;
                    }

                    float alt = 0;
                    if(terrainAltitude)
                    {
                        imageAltitude.setImageDrawable(getResources().getDrawable(R.drawable.nav_altmountain));
                        buttonAltitude.setText(DataPackets.distanceToString(data.RAlt));
                    }
                    else
                    {
                        imageAltitude.setImageDrawable(getResources().getDrawable(R.drawable.nav_altsea));
                        buttonAltitude.setText(DataPackets.distanceToString(data.Alt));
                    }


                    switch(data.getNavballMode())
                    {
                        case Orbit:
                            buttonVelocity.setText("Orbit\n"+String.format("%.1f", data.VOrbit)+"m/s");
                            break;
                        case Surface:
                            buttonVelocity.setText("Surface\n"+String.format("%.1f", data.Vsurf)+"m/s");
                            break;
                        case Target:
                            buttonVelocity.setText("Target\n"+String.format("%.1f", data.TargetV)+"m/s");
                            break;
                        case Ignore:
                        default:
                            buttonVelocity.setText("Unknown");
                            break;
                    }

                    if(data.isManeuverSet())
                        buttonManeuver.setText(String.format("%.1f", data.MNDeltaV)+"m/s\nT"+DataPackets.timeToString(data.MNTime));
                    else
                        buttonManeuver.setText("No Maneuver");

                    if(data.isTargetSet())
                        buttonTarget.setText(String.format("%.1f", data.TargetV)+"m/s\n"+String.format("%.1f", data.TargetDist)+"m");
                    else
                        buttonTarget.setText("No Target");


                    drawBar(imageFuelSF, data.SolidFuel / data.SolidFuelTot);
                    drawBar(imageFuelLF, data.LiquidFuel / data.LiquidFuelTot);
                    drawBar(imageFuelOX, data.Oxidizer / data.OxidizerTot);
                    drawBar(imageFuelXE, data.XenonGas / data.XenonGasTot);
                    drawBar(imageFuelMP, data.MonoProp / data.MonoPropTot);
                    drawBar(imageFuelES, data.ECharge/ data.EChargeTot);


                    textAP.setText(DataPackets.distanceToString(data.AP));
                    textPE.setText(DataPackets.distanceToString(data.PE));
                    textAPT.setText("T"+DataPackets.timeToString(data.TAp));
                    textPET.setText("T"+DataPackets.timeToString(data.TPe));


                    if(navballLock.tryAcquire())
                    {
                        navball.set(2 * Math.PI * data.Roll / 65535.0,2 * Math.PI * data.Pitch / 65535.0,2 * Math.PI * data.Heading / 65535.0);
                        navball.setPrograde(2 * Math.PI * data.ProgradePitch / 65535.0, 2 * Math.PI * data.ProgradeHeading / 65535.0);
                        navball.setManeuver(2 * Math.PI * data.ManeuverPitch / 65535.0, 2 * Math.PI * data.ManeuverHeading / 65535.0);
                        navball.setTarget(2 * Math.PI * data.TargetPitch / 65535.0, 2 * Math.PI * data.TargetHeading / 65535.0);
                        navball.setManeuverDisable(!data.isManeuverSet());
                        navball.setTargetDisable(!data.isTargetSet());
                        navball.hideRadialNormal(data.getNavballMode() == DataPackets.NavballMode.Target);
                        Thread t = new Thread(renderNavball);
                        t.start();
                    }


                    //Check for vessel change
                    if(client.controlData.hasVesselChanged(data))
                    {
                        Utility.showMessage(activeActivity,"Vessel changed");

                        //Initiliaze stuff like light, landing gears and action groups from new
                        //vessel to prevent bad things.
                        client.controlData.syncNewVessel(data);
                    }

                }
                else updateViews(true);

                updateUiLock.release();
            }
            catch(InterruptedException e)
            {
            }
        }
    };

    Runnable renderNavball = new Runnable()
    {
        @Override
        public void run()
        {
            navballBitmap = navball.render(imageNavball.getMeasuredWidth(), imageNavball.getMeasuredHeight(), activeActivity);
            activeActivity.runOnUiThread(renderNavballFinished);
        }
    };
    Runnable renderNavballFinished = new Runnable()
    {
        @Override
        public void run()
        {
            if(client.isActive()) imageNavball.setImageBitmap(navballBitmap);
            navballLock.release();
        }
    };


    private void drawBar(ImageView view, float p)
    {
        int w = view.getMeasuredWidth();
        int h = view.getMeasuredHeight();
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(w, h, conf);
        Canvas canvas = new Canvas(bmp);
        Paint paint =  new Paint();
        paint.setARGB(255,50,200,50);
        float twoDp = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 5,
                activeActivity.getResources().getDisplayMetrics());
        canvas.drawRect(new RectF(twoDp,((1-p)*(h-2*twoDp))+twoDp,w-twoDp,h-twoDp), paint);
        view.setImageBitmap(bmp);
    }



    @Override
    protected void onDestroy()
    {
        client.removeEventListener(clientListener);
        client.destroy();
        super.onDestroy();
    }
}
