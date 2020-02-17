package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;

import java.net.InetAddress;

/**
 * Contains data packet structures and functions to convert between structs and byte arrays.
 * Slighly modified version of zitronen-git/KSPSerialIO 0.19.1
 * Angles changed to UInt16 like in some other forks of the original code.
 *
 * Because Java does not support unsigned data types all unsigned types from the host data packets
 * are converted to the next bigger data type:
 *  byte -> short
 *  Uint16 -> int
 *  Uint32 -> long
 *
 *  Also contains some helper functions for data handling.
 */
public class DataPackets
{
	public final static int MaxPayloadSize = 255;
    public final static byte HSPid = 0, VDid = 1, SPid = 2, CPid = 101; //hard coded values for packet IDS


    public enum SASMode {Off, Regular, Prograde, Retrograde, Normal, Antinormal, RadialIn, RadialOut, Target, AntiTarget, Maneuver, Unknown}
    public enum NavballMode {Ignore, Target, Orbit, Surface, Unkonwn}
    public enum UiMode {Stage, Docking, Map}
    public enum CamMode {Auto, Free, Orbital, Chase, Locked}
    public enum HostState {Undefined, Disconnected, InFlight, NotInFlight}

    /**
     * Convert time in seconds to String of format XXyXXXdXXhXXmXXs
     *
     * @param t Time in seconds
     * @return String of format XXyXXXdXXhXXmXXs
     */
    public static String timeToString(long t)
    {
        String ret = "";
        if(t>0) ret+="-";
        else
        {
            t*=-1;
            ret+="+";
        }

        long y=0,d=0,h=0,m=0,s=0;
        y=t/(60*60*24*365);
        t%=(60*60*24*365);
        d=t/(60*60*24);
        t%=(60*60*24);
        h=t/(60*60);
        t%=(60*60);
        m=t/60;
        s=t%60;

        if(y>0) ret+=y+"y";
        if(y>0 || d>0) ret+=d+"d";
        if(y>0 || d>0 || h>0) ret+=h+"h";
        if(y>0 || d>0 || h>0 || m>0) ret+=m+"m";
        ret+=s+"s";

        return ret;
    }

    /**
     * Convert distance in meters to String of format XXX[.]X(m|k|M)
     *
     * @param d Distance in meters
     * @return String of format XXX[.]X(m|k|M)
     */
    public static String distanceToString(float d)
    {
        String ret = "";
        if(d<100) ret += String.format("%.1f", d) + "m";
        else if(d<300000) ret += String.format("%.0f", d) + "m";
        else if(d<300000000) ret += String.format("%.0f", d/1000) + "k";
        else ret += String.format("%.0f", d/1000000) + "M";
        return ret;
    }

    /**
     * Unidirectional data from KSPEthernetIO to client.
     */
    public static class VesselData
    {
        public short id;              //1
        public float AP;             //2
        public float PE;             //3
        public float SemiMajorAxis;  //4
        public float SemiMinorAxis;  //5
        public float VVI;            //6
        public float e;              //7
        public float inc;            //8
        public float G;              //9
        public int TAp;              //10
        public int TPe;              //11
        public float TrueAnomaly;    //12
        public float Density;        //13
        public int period;           //14
        public float RAlt;           //15
        public float Alt;            //16
        public float Vsurf;          //17
        public float Lat;            //18
        public float Lon;            //19
        public float LiquidFuelTot;  //20
        public float LiquidFuel;     //21
        public float OxidizerTot;    //22
        public float Oxidizer;       //23
        public float EChargeTot;     //24
        public float ECharge;        //25
        public float MonoPropTot;    //26
        public float MonoProp;       //27
        public float IntakeAirTot;   //28
        public float IntakeAir;      //29
        public float SolidFuelTot;   //30
        public float SolidFuel;      //31
        public float XenonGasTot;    //32
        public float XenonGas;       //33
        public float LiquidFuelTotS; //34
        public float LiquidFuelS;    //35
        public float OxidizerTotS;   //36
        public float OxidizerS;      //37
        public long MissionTime;     //38
        public float deltaTime;      //39
        public float VOrbit;         //40
        public long MNTime;          //41
        public float MNDeltaV;       //42
        public int Pitch;            //43
        public int Roll;             //44
        public int Heading;          //45
        public int ActionGroups;     //46  state bit order:SAS, RCS, Light, Gear, Brakes, Abort, Custom01 - 10
        public short SOINumber;      //47  SOI Number (decimal format: sun-planet-moon e.g. 130 = kerbin, 131 = mun)
        public short MaxOverHeat;    //48  Max part overheat (% percent)
        public float MachNumber;     //49
        public float IAS;            //50  Indicated Air Speed
        public short CurrentStage;   //51  Current stage number
        public short TotalStage;     //52  TotalNumber of stages
        public float TargetDist;     //53  Distance to targeted vessel (m)
        public float TargetV;        //54  Target vessel relative velocity (m/s)
        public short NavballSASMode; //55  Combined byte for nav_navballbackground vect_target mode and SAS mode
                                     // First four bits indicate AutoPilot mode:
                                     // 0 SAS is off  //1 = Regular Stability Assist //2 = Prograde
                                     // 3 = RetroGrade //4 = Normal //5 = Antinormal //6 = Radial In
                                     // 7 = Radial Out //8 = Target //9 = Anti-Target //10 = Maneuver node
                                     // Last 4 bits set nav_navballbackground mode. (0=ignore,1=ORBIT,2=SURFACE,3=TARGET)
        public int ProgradePitch;    //56 Pitch   Of the Prograde Vector;  int_16 ranging from (-0x8000(-360 degrees) to 0x7FFF(359.99ish degrees));
        public int ProgradeHeading;  //57 Heading Of the Prograde Vector;  see above for range   (Prograde vector depends on nav_navballbackground mode, eg Surface/Orbit/Target)
        public int ManeuverPitch;    //58 Pitch   Of the Maneuver Vector;  see above for range;  (0 if no Maneuver node)
        public int ManeuverHeading;  //59 Heading Of the Maneuver Vector;  see above for range;  (0 if no Maneuver node)
        public int TargetPitch;      //60 Pitch   Of the Target   Vector;  see above for range;  (0 if no Target)
        public int TargetHeading;    //61 Heading Of the Target   Vector;  see above for range;  (0 if no Target)
        public int NormalHeading;    //62 Heading Of the Prograde Vector;  see above for range;  (Pitch of the Heading Vector is always 0)
        public short vesselSync;     //63 Current sync value, changed on every vessel change - has to be mirrored into ControlPacket


        /**
         * Return if target is set.
         * TODO Transfer target set bit in data packet
         * @return True if target is set
         */
        public boolean isTargetSet()
        {
            return !(TargetHeading == 0 && TargetPitch == 0);
        }

        /**
         * Return if maneuver node is set.
         * TODO Transfer maneuver node set bit in data packet
         * @return True if maneuver node is set
         */
        public boolean isManeuverSet()
        {
            return !(ManeuverHeading == 0 && ManeuverPitch == 0);
        }

        /**
         * Get Navball mode.
         *
         * @return Navball mode enum
         */
        public NavballMode getNavballMode()
        {
            switch((NavballSASMode&0b11110000)>>4)
            {
                case 0:
                    return NavballMode.Ignore;
                case 1:
                    return NavballMode.Orbit;
                case 2:
                    return NavballMode.Surface;
                case 3:
                    return NavballMode.Target;
                default:
                    return NavballMode.Unkonwn;
            }
        }

        /**
         * Get SAS mode.
         *
         * @return SAS mode enum
         */
        public SASMode getSASMode()
        {
            switch(NavballSASMode&0b1111)
            {
                case 0:
                    return SASMode.Off;
                case 1:
                    return SASMode.Regular;
                case 2:
                    return  SASMode.Prograde;
                case 3:
                    return  SASMode.Retrograde;
                case 4:
                    return  SASMode.Normal;
                case 5:
                    return  SASMode.Antinormal;
                case 6:
                    return  SASMode.RadialIn;
                case 7:
                    return  SASMode.RadialOut;
                case 8:
                    return  SASMode.Target;
                case 9:
                    return  SASMode.AntiTarget;
                case 10:
                    return SASMode.Maneuver;
                default:
                    return  SASMode.Unknown;
            }
        }

        /**
         * Get action group state.
         *
         * @param n Action group 0-9
         * @return True if action group is set. False if out of range.
         */
        public boolean getActionGroup(int n)
        {
            if(n<0 || n>9) return false;
            return (ActionGroups & (1<<(n+6)))!=0;
        }

        /**
         * Get SAS state.         *
         * @return True if SAS is on
         */
        public boolean getSAS()
        {
            return (ActionGroups & (1<<(0)))!=0;
        }
        /**
         * Get RCS state.         *
         * @return True if RCS is on
         */
        public boolean getRCS()
        {
            return (ActionGroups & (1<<(1)))!=0;
        }
        /**
         * Get Light state.         *
         * @return True if light is on
         */
        public boolean getLight()
        {
            return (ActionGroups & (1<<(2)))!=0;
        }
        /**
         * Get gears state.         *
         * @return True if gears extracted
         */
        public boolean getGears()
        {
            return (ActionGroups & (1<<(3)))!=0;
        }
        /**
         * Get brakes state.         *
         * @return True if brakes are on
         */
        public boolean getBrakes()
        {
            return (ActionGroups & (1<<(4)))!=0;
        }
        /**
         * Get abort state.         *
         * @return True on abort
         */
        public boolean getAbort()
        {
            return (ActionGroups & (1<<(5)))!=0;
        }

        /**
         * Convert byte array starting with packet ID to VesselData object.
         *
         * @param packet Data array
         * @return VesselData
         * @throws PacketException Packet read error
         */
        public static VesselData fromPacket(byte[] packet) throws PacketException
        {
        	byte[] data = getPayload(packet);
        	Serializer s = new Serializer(data);
        	VesselData VDP = new VesselData();
        	
        	VDP.id=s.deserializeUB8();
        	VDP.AP=s.deserializeF();
        	VDP.PE=s.deserializeF();
        	VDP.SemiMajorAxis=s.deserializeF();
        	VDP.SemiMinorAxis=s.deserializeF();
            VDP.VVI=s.deserializeF();
            VDP.e=s.deserializeF();
            VDP.inc=s.deserializeF();
            VDP.G=s.deserializeF();
            VDP.TAp=s.deserializeB32();
            VDP.TPe=s.deserializeB32();
            VDP.TrueAnomaly=s.deserializeF();   
            VDP.Density=s.deserializeF();
            VDP.period=s.deserializeB32();
            VDP.RAlt=s.deserializeF();          
            VDP.Alt=s.deserializeF();           
            VDP.Vsurf=s.deserializeF();         
            VDP.Lat=s.deserializeF();           
            VDP.Lon=s.deserializeF();           
            VDP.LiquidFuelTot=s.deserializeF(); 
            VDP.LiquidFuel=s.deserializeF();    
            VDP.OxidizerTot=s.deserializeF();   
            VDP.Oxidizer=s.deserializeF();      
            VDP.EChargeTot=s.deserializeF();    
            VDP.ECharge=s.deserializeF();       
            VDP.MonoPropTot=s.deserializeF();   
            VDP.MonoProp=s.deserializeF();      
            VDP.IntakeAirTot=s.deserializeF();  
            VDP.IntakeAir=s.deserializeF();     
            VDP.SolidFuelTot=s.deserializeF();  
            VDP.SolidFuel=s.deserializeF();     
            VDP.XenonGasTot=s.deserializeF();   
            VDP.XenonGas=s.deserializeF();      
            VDP.LiquidFuelTotS=s.deserializeF();
            VDP.LiquidFuelS=s.deserializeF();   
            VDP.OxidizerTotS=s.deserializeF();  
            VDP.OxidizerS=s.deserializeF();     
            VDP.MissionTime=s.deserializeUB32();
            VDP.deltaTime=s.deserializeF();
            VDP.VOrbit=s.deserializeF();
            VDP.MNTime=s.deserializeUB32();
            VDP.MNDeltaV=s.deserializeF();
            VDP.Pitch=s.deserializeUB16();
            VDP.Roll=s.deserializeUB16();
            VDP.Heading=s.deserializeUB16();
            VDP.ActionGroups=s.deserializeUB16(); 
            VDP.SOINumber=s.deserializeUB8();  
            VDP.MaxOverHeat=s.deserializeUB8(); 
            VDP.MachNumber=s.deserializeF();
            VDP.IAS=s.deserializeF(); 
            VDP.CurrentStage=s.deserializeUB8();
            VDP.TotalStage=s.deserializeUB8();
            VDP.TargetDist=s.deserializeF();
            VDP.TargetV=s.deserializeF();
            VDP.NavballSASMode=s.deserializeUB8();             
            VDP.ProgradePitch=s.deserializeUB16(); 
            VDP.ProgradeHeading=s.deserializeUB16();
            VDP.ManeuverPitch=s.deserializeUB16();
            VDP.ManeuverHeading=s.deserializeUB16();            
            VDP.TargetPitch=s.deserializeUB16();
            VDP.TargetHeading=s.deserializeUB16(); 
            VDP.NormalHeading=s.deserializeUB16();
            VDP.vesselSync =s.deserializeUB8();

            return VDP;
        }

        /**
         * Convert to String.
         * TODO VesselData.toString not implemented yet. Do if very bored some time.
         *
         * @return String representing the VesselData
         */
        public String toString()
        {
        	String str = "VDP{"+"}";
        	return str;
        }
    }

    /**
     * Get the packet id from packet data array.
     * @param packet Packet data
     * @return Packet ID or -1
     */
    public static int getPacketID(byte[] packet)
    {
        if(packet.length<4) return -1;
        return packet[3];
    }

    /**
     * Bidirectional handshake data.
     * Also contains the InetAddress of the sender.
     */
    public static class HandshakePacket
    {
        public short id = HSPid;
        public short M1;
        public short M2;
        public short state;

        //Wrapper for the sender InetAddress, has to be set manually after receive
        //TODO Set InetAddress in contructor
        public InetAddress sender = null;

        /**
         * Convert byte array starting with packet ID to VesselData object.
         *
         * @param packet Data array
         * @return HandshakePacket
         * @throws PacketException Packet read error
         */
        public static HandshakePacket fromPacket(byte[] packet) throws PacketException
        {
        	byte[] data = getPayload(packet);
        	Serializer s = new Serializer(data);
        	HandshakePacket HP = new HandshakePacket();
        	HP.id=s.deserializeUB8();
        	HP.M1=s.deserializeUB8();
        	HP.M2=s.deserializeUB8();
        	HP.state =s.deserializeUB8();
        	return HP;
        }

        /**
         * Convert HandshakePacket to byte array.
         *
         * @return Data byte array
         * @throws PacketException Packet write error
         */
        public byte[] toPacket() throws PacketException
        {
        	byte[] data = new byte[1024];
        	Serializer s = new Serializer(data);
        	s.serializeUB8(id);
        	s.serializeUB8(M1);
        	s.serializeUB8(M2);
        	s.serializeUB8(state);
        	byte[] datacut = new byte[s.getLenght()];
        	System.arraycopy(data, 0, datacut, 0, s.getLenght());
        	return fromPayload(datacut);
        }

        /**
         * Convert to String.
         *
         * @return String representing the HandshakePacket
         */
        public String toString()
        {
        	String str = "HP{"+M1+","+M2+","+ state +"}";
        	return str;
        }

        /**
         * Get the host state.
         * @return Host state
         */
        public HostState getState()
        {
            switch(state)
            {
                case 1:
                    return HostState.InFlight;
                case 2:
                    return HostState.NotInFlight;
                default:
                    return HostState.Undefined;
            }
        }
    }

    /**
     * Unidirectional state data.
     * Contains current host state.
     */
    public static class StatusPacket
    {
        public short id = SPid;
        public short state;

        /**
         * Convert byte array starting with packet ID to VesselData object.
         *
         * @param packet Data array
         * @return StatusPacket
         * @throws PacketException Packet read error
         */
        public static StatusPacket fromPacket(byte[] packet) throws PacketException
        {
            byte[] data = getPayload(packet);
            Serializer s = new Serializer(data);
            StatusPacket SP = new StatusPacket();
            SP.id=s.deserializeUB8();
            SP.state =s.deserializeUB8();
            return SP;
        }

        /**
         * Convert StatusPacket to byte array.
         *
         * @return Data byte array
         * @throws PacketException Packet write error
         */
        public byte[] toPacket() throws PacketException
        {
            byte[] data = new byte[1024];
            Serializer s = new Serializer(data);
            s.serializeUB8(id);
            s.serializeUB8(state);
            byte[] datacut = new byte[s.getLenght()];
            System.arraycopy(data, 0, datacut, 0, s.getLenght());
            return fromPayload(datacut);
        }

        /**
         * Convert to String.
         *
         * @return String representing the StatusPacket
         */
        public String toString()
        {
            String str = "SP{"+ state +"}";
            return str;
        }

        /**
         * Get the host state.
         * @return Host state
         */
        public HostState getState()
        {
            switch(state)
            {
                case 1:
                    return HostState.InFlight;
                case 2:
                    return HostState.NotInFlight;
                default:
                    return HostState.Undefined;
            }
        }
    }

    /**
     * Unidirectional data from client to KSPEthernetIO.
     */
    public static class ControlPacket
    {
        public short id = CPid;
        public short MainControls;                      //SAS RCS Lights Gear Brakes Precision Abort Stage
        public short Mode;                              //0 = stage, 1 = docking, 2 = map (Bit 0-3)
                                                        //Camera Mode 0:Auto 1:Free 2:Orbital 3:Chase 4:Locked (Bit 4-7)
        public boolean[] ControlGroup = new boolean[16];//control groups 1-10 in 2 bytes
        public short NavballSASMode;                    //AutoPilot mode (See above for AutoPilot modes)(Ignored if the equal to zero or out of bounds (>10)) //Navball mode
        public short AdditionalControlByte1;            //Bit 0: Open Menu
        public short Pitch;                             //-1000 -> 1000
        public short Roll;                              //-1000 -> 1000
        public short Yaw;                               //-1000 -> 1000
        public short TX;                                //-1000 -> 1000
        public short TY;                                //-1000 -> 1000
        public short TZ;                                //-1000 -> 1000
        public short WheelSteer;                        //-1000 -> 1000
        public short Throttle;                          // 0 -> 1000
        public short WheelThrottle;                     // 0 -> 1000
        public short vesselSync;                        // Mirror from host to control active vessel


        /**
         * Synchronize some data after a vessel change.
         * Sets vesselSync value.
         * Has to be called after vessel change to control new vessel.
         * @param data Data received from the new vessel
         */
        public void syncNewVessel(VesselData data)
        {
            for(int i=0;i<10;i++)
                setActionGroup(i, data.getActionGroup(i));
            setLight(data.getLight());
            setGears(data.getGears());
            setBrakes(data.getBrakes());
            setRCS(data.getRCS());
            setSAS(data.getSAS());
            setSASMode(data.getSASMode());

            AdditionalControlByte1 = 0;
            Mode = 0;

            vesselSync = data.vesselSync;
        }

        /**
         * Force to resync the control data after next vessel change.
         * Is calles on flight end or connection loss.
         */
        public void forceResync()
        {
            vesselSync = 0;
        }

        /**
         * Check if active vessel has changed since last call of syncNewVessel()
         * @param data Current vessel data
         * @return True if active vessel has changed since last syncNewVessel()
         */
        public boolean hasVesselChanged(VesselData data)
        {
            return vesselSync != data.vesselSync;
        }

        /**
         * Toggle Map view
         */
        public void toggleMap()
        {
            AdditionalControlByte1^=0b00000010;
        }

        /**
         * Toggle Menu
         */
        public void toggleMenu()
        {
            AdditionalControlByte1^=0b00000001;
        }

        /**
         * Set camera mode.
         *
         * @param m CamMode enum
         */
        public void setCamMode(CamMode m)
        {
            Mode &= 0b1111;
            switch(m)
            {
                case Auto:
                    Mode|=0<<4;
                    break;
                case Free:
                    Mode|=1<<4;
                    break;
                case Orbital:
                    Mode|=2<<4;
                    break;
                case Chase:
                    Mode|=3<<4;
                    break;
                case Locked:
                    Mode|=4<<4;
                    break;
                default:
                    break;
            }
        }

        /**
         * Rotate through all camera modes.
         */
        public void rotateCamMode()
        {
            int tmp = (Mode & ~0b1111)>>4;
            Mode&=0b1111;
            tmp++;
            if(tmp>4) tmp=0;
            Mode|=tmp<<4;
        }

        /**
         * Set UI mode.
         *
         * @param m UiMode enum
         */
        public void setUiMode(UiMode m)
        {
            Mode &= ~0b1111;
            switch(m)
            {
                case Stage:
                    Mode|=0;
                    break;
                case Docking:
                    Mode|=1;
                    break;
                case Map:
                    Mode|=2;
                    break;
                default:
                    break;
            }
        }

        /**
         * Rotate through all Ui modes
         */
        public void rotateUiMode()
        {
            int tmp = Mode & 0b1111;
            Mode&=~0b1111;
            tmp++;
            if(tmp>2) tmp=0;
            Mode|=tmp;
        }

        /**
         * Set Navball mode.
         *
         * @param m NavballMode enum
         */
        public void setNavballMode(NavballMode m)
        {
            NavballSASMode&=~0b11110000;
            switch(m)
            {
                case Orbit:
                    NavballSASMode |= (1<<4);
                    break;
                case Surface:
                    NavballSASMode |= (2<<4);
                    break;
                case Target:
                    NavballSASMode |= (3<<4);
                    break;
                case Ignore:
                default:
                    break;
            }
        }

        /**
         * Rotate through Navball mode.
         * Enter Target mode only if target is set.
         *
         * @param isTargetSet True to enable enter target mode
         */
        public void rotateNavballMode(boolean isTargetSet)
        {
            int curMode = (NavballSASMode&0b11110000)>>4;
            if(curMode==0 || curMode==1) setNavballMode(NavballMode.Surface); // is in Orbit
            else if(curMode == 2 && isTargetSet) setNavballMode(NavballMode.Target); // is in Surface
            else setNavballMode(NavballMode.Orbit); // is in Surface or Target
        }

        /**
         * Set SAS mode.
         *
         * @param m SAS mode enum
         */
        public void setSASMode(SASMode m)
        {
            NavballSASMode&=~0b1111;
            switch(m)
            {
                case Regular:
                    NavballSASMode|=1;
                    break;
                case Prograde:
                    NavballSASMode|=2;
                    break;
                case Retrograde:
                    NavballSASMode|=3;
                    break;
                case Normal:
                    NavballSASMode|=4;
                    break;
                case Antinormal:
                    NavballSASMode|=5;
                    break;
                case RadialIn:
                    NavballSASMode|=6;
                    break;
                case RadialOut:
                    NavballSASMode|=7;
                    break;
                case Target:
                    NavballSASMode|=8;
                    break;
                case AntiTarget:
                    NavballSASMode|=9;
                    break;
                case Maneuver:
                    NavballSASMode|=10;
                    break;
                case Off:
                case Unknown:
                default:
                    break;
            }
        }

        /**
         * Set action group.
         *
         * @param n Action group 0-9
         * @param b Activate/Deactivate
         */
        public void setActionGroup(int n, boolean b)
        {
            ControlGroup[n+1]=b;
        }

        /**
         * Set SAS.
         *
         * @param b True to activate SAS
         */
        public void setSAS(boolean b)
        {
            if(b) MainControls |= (1<<7);
            else MainControls &= ~(1<<7);
        }

        /**
         * Set RCS.
         *
         * @param b True to activate RCS
         */
        public void setRCS(boolean b)
        {
            if(b) MainControls |= (1<<6);
            else MainControls &= ~(1<<6);
        }

        /**
         * Set light.
         *
         * @param b True to activate light
         */
        public void setLight(boolean b)
        {
            if(b) MainControls |= (1<<5);
            else MainControls &= ~(1<<5);
        }

        /**
         * Set gears.
         *
         * @param b True to extract gears
         */
        public void setGears(boolean b)
        {
            if(b) MainControls |= (1<<4);
            else MainControls &= ~(1<<4);
        }

        /**
         * Set brakes.
         *
         * @param b True to activate brakes
         */
        public void setBrakes(boolean b)
        {
            if(b) MainControls |= (1<<3);
            else MainControls &= ~(1<<3);
        }

        /**
         * Set precision mode.
         *
         * @param b True to activate precision control mode
         */
        public void setPrecision(boolean b)
        {
            if(b) MainControls |= (1<<2);
            else MainControls &= ~(1<<2);
        }

        /**
         * Trigger abort.
         * Is automatically reset after next packet sent.
         */
        public void setAbort()
        {
            MainControls |= (1<<1);
        }

        /**
         * Trigger staging.
         * Is automatically reset after next packet sent.
         */
        public void setStage()
        {
            MainControls |= (1<<0);
        }

        /**
         * Reset abort.
         * Is automatically called after next packet sent.
         */
        public void resetAbort()
        {
            MainControls &= ~(1<<1);
        }

        /**
         * Reset staging.
         * Is automatically called after next packet sent.
         */
        public void resetStage()
        {
            MainControls &= ~(1<<0);
        }

        /**
         * Convert ControlPacket to byte array.
         *
         * @return Data byte array
         * @throws PacketException Packet write error
         */
        public byte[] toPacket() throws PacketException
        {
        	byte[] data = new byte[1024];
        	Serializer s = new Serializer(data);
        	s.serializeUB8(id);
            s.serializeUB8(MainControls); 
            s.serializeUB8(Mode);
            int cg = 0;
            for(int i=0; i<16; i++) if(ControlGroup[i]) cg|=(1<<i);
            s.serializeUB16(cg);
            s.serializeUB8(NavballSASMode);
            s.serializeUB8(AdditionalControlByte1);
            s.serializeB16(Pitch);
            s.serializeB16(Roll);
            s.serializeB16(Yaw);
            s.serializeB16(TX);
            s.serializeB16(TY);
            s.serializeB16(TZ);
            s.serializeB16(WheelSteer);
            s.serializeB16(Throttle);
            s.serializeB16(WheelThrottle);
            s.serializeUB8(vesselSync);
        	byte[] datacut = new byte[s.getLenght()];
        	System.arraycopy(data, 0, datacut, 0, s.getLenght());
        	return fromPayload(datacut);
        }

        /**
         * Convert to String.
         * TODO ControlPacket.toString not implemented yet. Do if very bored some time.
         *
         * @return String representing the ControlPacket
         */
        public String toString()
        {
        	String str = "CP{"+"}";
        	return str;
        }
    }

    /**
     * Helper class to manually serialize or deserialize data.
     * Usage serialize:
     *  Create Serializer object with empty byte array.
     *  Each call of serialize member function appends data to that array.
     * Usage deserialize:
     *  Create Serializer object with filled byte array.
     *  Each call of deserialize member function reads data from that array.
     *  Automatically seeks forward the number of read bytes.
     */
    private static class Serializer
    {
    	private int off=0;
    	private byte[] buffer;
    	
    	public Serializer(byte[] buffer)
    	{
    		this.buffer = buffer;
    		off=0;
    	}    			
    	public void serializeUB8(short data)
    	{
    		buffer[off++]=(byte)(data&0xFF);
    	}	 			
    	public void serializeB8(byte data)
    	{
    		buffer[off++]=(byte)(data&0xFF);
    	}		
    	public void serializeUB16(int data)
    	{
    		buffer[off++] = (byte)((data>>0)&0xFF);
    		buffer[off++] = (byte)((data>>8)&0xFF);
    	}		
    	public void serializeB16(short data)
    	{
    		buffer[off++] = (byte)((data>>0)&0xFF);
    		buffer[off++] = (byte)((data>>8)&0xFF);
    	}
    	public void serializeUB32(long data)
    	{
    		buffer[off++] = (byte)((data>>24)&0xFF);
    		buffer[off++] = (byte)((data>>16)&0xFF);
    		buffer[off++] = (byte)((data>>8)&0xFF);
    		buffer[off++] = (byte)((data>>0)&0xFF);
    	}
    	public void serializeB32(int data)
    	{
    		buffer[off++] = (byte)((data>>24)&0xFF);
    		buffer[off++] = (byte)((data>>16)&0xFF);
    		buffer[off++] = (byte)((data>>8)&0xFF);
    		buffer[off++] = (byte)((data>>0)&0xFF);
    	}
    	public void serializeF(float fdata)
    	{
    		int data =  Float.floatToIntBits(fdata);
    		buffer[off++] = (byte)((data>>24)&0xFF);
    		buffer[off++] = (byte)((data>>16)&0xFF);
    		buffer[off++] = (byte)((data>>8)&0xFF);
    		buffer[off++] = (byte)((data>>0)&0xFF);
    	}   			
    	public short deserializeUB8()
    	{
    		short ret=0;
    		ret |= ((short)buffer[off++])&0xFF;
    		return ret;
    	}	 			
    	public byte deserializeB8()
    	{
    		byte ret=0;
    		ret |= ((short)buffer[off++])&0xFF;
    		return ret;
    	}		
    	public int deserializeUB16()
    	{
    		int ret=0;
    		ret |= ((int)buffer[off++]<<0)&0x00FF;
    		ret |= ((int)buffer[off++]<<8)&0xFF00;
    		return ret;
    	}		
    	public short deserializeB16()
    	{
    		short ret=0;
    		ret |= ((short)buffer[off++]<<0)&0x00FF;
    		ret |= ((short)buffer[off++]<<8)&0xFF00;
    		return ret;
    	}
    	public long deserializeUB32()
    	{
    		long ret=0;
    		ret |= ((long)buffer[off++]<<0)&0x000000FF;
    		ret |= ((long)buffer[off++]<<8)&0x0000FF00;
    		ret |= ((long)buffer[off++]<<16)&0x00FF0000;
    		ret |= ((long)buffer[off++]<<24)&0xFF000000;
    		return ret;
    	}
    	public int deserializeB32()
    	{
    		int ret=0;
    		ret |= ((int)buffer[off++]<<0)&0x000000FF;
    		ret |= ((int)buffer[off++]<<8)&0x0000FF00;
    		ret |= ((int)buffer[off++]<<16)&0x00FF0000;
    		ret |= ((int)buffer[off++]<<24)&0xFF000000;
    		return ret;
    	}
    	public float deserializeF()
    	{
    		int ret=0;
    		ret |= ((int)buffer[off++]<<0)&0x000000FF;
    		ret |= ((int)buffer[off++]<<8)&0x0000FF00;
    		ret |= ((int)buffer[off++]<<16)&0x00FF0000;
    		ret |= ((int)buffer[off++]<<24)&0xFF000000;
    		
    		return Float.intBitsToFloat(ret);
    	}
    	
    	public int getLenght()
    	{
    		return off;
    	}
    }

    /**
     * Get payload of KSPEthernetIO data packet.
     *
     * Packet structure:
     * Packet = [header][size][payload][checksum];
     * Header = [Header1=0xBE][Header2=0xEF]
     * size   = [payload.length (0-255)]
     *
     * @param packet Data packet
     * @return Byte array with payload
     * @throws PacketException Packet read error
     */
    public static byte[] getPayload(byte[] packet) throws PacketException
    {
    	if(packet.length < 4) throw new PacketException("Packet too short!");
    	if(packet.length > 255+4) throw new PacketException("Packet too long!");
    	if(packet[0]!=(byte)0xbe || packet[1]!=(byte)0xef) throw new PacketException("Wrong packet header!");
    	
        short size = (short)(packet.length - 4);
        byte checksum = (byte)size;
        byte checksumReceived = packet[packet.length-1];
        byte[] payload = new byte[size];
        
        for(int i=0; i<size; i++)
        {
        	payload[i] = packet[3+i];
        	checksum ^= payload[i];
        }
    	if(checksum != checksumReceived) throw new PacketException("Packet checksum error!");
        
        return payload;
    }

    /**
     * Create packet from payload.
     *
     * Packet structure:
     * Packet = [header][size][payload][checksum];
     * Header = [Header1=0xBE][Header2=0xEF]
     * size   = [payload.length (0-255)]
     *
     * @param payload Payload data
     * @return Data packet
     * @throws PacketException Packet write error
     */
    public static byte[] fromPayload(byte[] payload) throws PacketException
    { 
    	if(payload.length <= 0) throw new PacketException("Payload too short!");
    	if(payload.length > 255) throw new PacketException("Payload too long!");
    	
        short size = (short)(payload.length);
        byte checksum = (byte)size;
    	byte[] packet = new byte[payload.length+4];
        
    	packet[0] = (byte)0xbe;
    	packet[1] = (byte)0xef;
    	packet[2] = (byte)size;
    	
        for(int i=0; i<size; i++)
        {
        	packet[3+i] = payload[i];
        	checksum ^= payload[i];
        }
    	packet[packet.length-1] = checksum;
        
        return packet;
    }

    /**
     * Packet exception.
     *  - Payload too long
     *  - Payload too short
     *  - Wrong packet header
     *  - Checksum error
     */
    public static class PacketException extends Exception
    {
    	public PacketException(String message)
    	{
    		super(message);
    	}
    }
}
