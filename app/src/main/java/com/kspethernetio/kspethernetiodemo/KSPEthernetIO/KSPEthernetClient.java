package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncBroadcastClient.BroadcastMyEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncTcpClient.TcpMyEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.ControlPacket;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.HandshakePacket;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.PacketException;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.VesselData;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.MyEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.EventProvider;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.PacketHandler.PacketMyEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Statemachine.State;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Statemachine.StatemachineListener;

public class KSPEthernetClient
{
	private static final int dt = 5;
	
	private Statemachine sm = new Statemachine("KSPEthernetClient", dt);
	private StatemachineController smc = new StatemachineController(sm);

	private static enum StatemachineCommandType {Start, Stop, Reset}
	private final StatemachineCommand StartCommand = new StatemachineCommand(smc, StatemachineCommandType.Start);
	private final StatemachineCommand StopCommand = new StatemachineCommand(smc, StatemachineCommandType.Stop);
	private final StatemachineCommand ResetCommand = new StatemachineCommand(smc, StatemachineCommandType.Stop);
	
	private AsyncBroadcastClient broadcastClient = null;
	private AsyncTcpClient tcpClient = null;
	private PacketHandler packetHandler = null;
	private InetAddress host = null;
	
	public ControlPacket controlData = new ControlPacket();
	private VesselData vesselData = new VesselData();
	
	private int port;
	private int refresh;
	
	public KSPEthernetClient(int port, int refresh)
	{
		this.port = port;
		this.refresh = refresh;
		smc.addEventListener(sm);
		sm.addStatemachineListener(new StatemachineListener()
		{			
			@Override
			public void statemachineStopped(Statemachine sm) { }
			@Override
			public void statemachineStateChanged(Statemachine sm, State os, State ns) { notifyStateChanged(ns); }
			@Override
			public void statemachineStarted(Statemachine sm) { }
			@Override
			public void statemachineFinished(Statemachine sm) { }
		});
		sm.start(S0_Initialize);
	}
	
	public void start()
	{
		if(sm.isActive()) smc.notifyEvent(StartCommand);
	}
	public void stop()
	{
		if(sm.isActive()) smc.notifyEvent(StopCommand);		
	}
	public void destroy()
	{
		if(sm != null && sm.isActive()) sm.stop();
		if(broadcastClient != null && broadcastClient.isActive()) broadcastClient.cancelReceiveBroadcast();
		if(tcpClient != null && tcpClient.isActive()) tcpClient.cancelReceiveData();
	}
	public void reset()
	{
		if(sm.isActive()) smc.notifyEvent(ResetCommand);		
	}
	public String getState()
	{
		if(sm.isActive()) return sm.getActiveState().getName();
		else return "Inactive";
	}
	public boolean isInitialized()
	{
		return sm.isActive() && sm.getActiveState() != S0_Initialize;
	}
	public boolean isStopped()
	{
		return sm.isActive() && sm.getActiveState() == S1_WaitStart;
	}
	public boolean isActive()
	{
		return sm.isActive() && sm.getActiveState() == S5_Active;
	}
	
	
	private State S0_Initialize = new State("Initialize", sm)
	{
		@Override
		public void onEnter()
		{
			broadcastClient = new AsyncBroadcastClient(port);
			broadcastClient.addEventListener(sm);
			packetHandler = new PacketHandler();
			packetHandler.setBroadcastClient(broadcastClient);
			packetHandler.addEventListener(sm);
		}
		
		@Override
		public State onExecute(MyEvent e)
		{
			return S1_WaitStart;
		}
		
		@Override
		public void onExit()
		{
		}
	};
	private State S1_WaitStart = new State("Wait for start command", sm)
	{
		@Override
		public void onEnter()
		{
		}
		
		@Override
		public State onExecute(MyEvent myEvent)
		{
			if(myEvent == StartCommand) return S2_WaitBroadcast;
			else return this;
		}
		
		@Override
		public void onExit()
		{
		}
	};
	private State S2_WaitBroadcast = new State("Wait for broadcast", sm)
	{
		@Override
		public void onEnter()
		{
			broadcastClient.startReceiveBroadcast();
		}
		
		@Override
		public State onExecute(MyEvent myEvent)
		{
			if(myEvent == StopCommand) return S7_Stop;
			
			if(myEvent != null && myEvent.sender == packetHandler)
			{
				PacketMyEvent packetEvent = (PacketMyEvent) myEvent;
				switch(packetEvent.getType())
				{
				case HandshakeReceived:
					host = packetEvent.getHandshakePacket().sender;
					return S3_Connect;
				default:
					break;
				}
			}

			if(myEvent != null && myEvent.sender == broadcastClient)
			{
				BroadcastMyEvent broadcastEvent = (BroadcastMyEvent) myEvent;
				switch(broadcastEvent.getType())
				{
					case Canceled:
						Exception e = broadcastEvent.getException();
						if(e!=null) notifyError(e);
						return S0_Initialize;
					default:
						break;
				}
			}
			
			if(!broadcastClient.isActive()) return S0_Initialize;
			
			return this;
		}
		
		@Override
		public void onExit()
		{
			broadcastClient.cancelReceiveBroadcast();
		}
	};
	private State S3_Connect = new State("Connect TCP client", sm)
	{

		@Override
		public void onEnter()
		{
			tcpClient = new AsyncTcpClient(host, port);
			packetHandler.setTcpClient(tcpClient);
			tcpClient.addEventListener(sm);
			tcpClient.startReceiveData();
		}
		
		@Override
		public State onExecute(MyEvent myEvent)
		{
			if(myEvent == ResetCommand) return S6_Restart;
			if(myEvent == StopCommand) return S7_Stop;
			
			if(myEvent != null && myEvent.sender == tcpClient)
			{
				TcpMyEvent tcpEvent = (TcpMyEvent) myEvent;
				switch(tcpEvent.getType())
				{
				case Connected:
					return S4_Handshake;
				case Disconnected:
					Exception e = tcpEvent.getException();
					if(e!=null) notifyError(e);
					return S6_Restart;
				default:
					break;
				}
			}
			
			return this;
		}
		
		@Override
		public void onExit()
		{
		}
		
	};
	private State S4_Handshake = new State("Perform handshake", sm)
	{

		@Override
		public void onEnter()
		{			
		}
		
		@Override
		public State onExecute(MyEvent myEvent)
		{
			if(myEvent == ResetCommand) return S6_Restart;
			if(myEvent == StopCommand) return S7_Stop;
			
			HandshakePacket HP = new HandshakePacket();
			HP.M1 = 3;
			HP.M2 = 1;
			HP.M3 = 4;
			try
			{
				tcpClient.sendData(HP.toPacket());
			}
			catch(PacketException e)
			{
				notifyError(e);
				return S6_Restart;
			}
			
			if(myEvent != null && myEvent.sender == tcpClient)
			{
				TcpMyEvent tcpEvent = (TcpMyEvent) myEvent;
				switch(tcpEvent.getType())
				{
				case Disconnected:
					Exception e = tcpEvent.getException();
					if(e!=null) notifyError(e);
					return S6_Restart;
				default:
					break;
				}
			}
			return S5_Active;
		}
		
		@Override
		public void onExit()
		{
		}
		
	};
	private State S5_Active = new State("Active", sm)
	{
		int sendTimer;

		@Override
		public void onEnter()
		{
			sendTimer = 0;
		}
		
		@Override
		public State onExecute(MyEvent myEvent)
		{
			if(myEvent == ResetCommand) return S6_Restart;
			if(myEvent == StopCommand) return S7_Stop;
			
			sendTimer += dt;			
			if(sendTimer >= refresh)
			{
				sendTimer=0;
				
				try
				{
					tcpClient.sendData(controlData.toPacket());
					controlData.resetAbort();
					controlData.resetStage();
				}
				catch(PacketException e)
				{
					notifyError(e);
					return S6_Restart;
				}
			}			

			if(myEvent != null && myEvent.sender == packetHandler)
			{
				PacketMyEvent packetEvent = (PacketMyEvent) myEvent;
				switch(packetEvent.getType())
				{
				case VesselDataReceived:
					vesselData = packetEvent.getVesselData();
					notifyInvalidate();
					break;
				default:
					break;
				}
			}			
			
			if(myEvent != null && myEvent.sender == tcpClient)
			{
				TcpMyEvent tcpEvent = (TcpMyEvent) myEvent;
				switch(tcpEvent.getType())
				{
				case Disconnected:
					Exception e = tcpEvent.getException();
					if(e!=null) notifyError(e);
					return S6_Restart;
				default:
					break;
				}
			}
			
			return this;
		}
		
		@Override
		public void onExit()
		{
		}
		
	};
	private State S6_Restart = new State("Restart TCP client", sm)
	{
		@Override
		public void onEnter()
		{
			if(tcpClient != null)
			{
				tcpClient.removeEventListener(sm);
				tcpClient.cancelReceiveData();
				tcpClient = null;
			}
		}
		
		@Override
		public State onExecute(MyEvent e)
		{
			return S2_WaitBroadcast;
		}
		
		@Override
		public void onExit()
		{
		}
		
	};
	private State S7_Stop = new State("Shutdown TCP client", sm)
	{
		@Override
		public void onEnter()
		{
			if(tcpClient != null)
			{
				tcpClient.removeEventListener(sm);
				tcpClient.cancelReceiveData();
				tcpClient = null;
			}
		}
		
		@Override
		public State onExecute(MyEvent e)
		{
			return S1_WaitStart;
		}
		
		@Override
		public void onExit()
		{
		}
		
	};
	


	private static class StatemachineCommand extends MyEvent
	{			
		public StatemachineCommand(StatemachineController sender, StatemachineCommandType t)
		{
			super(sender, t.ordinal(), null);
		}
		
		public StatemachineCommandType getType()
		{
			return StatemachineCommandType.values()[id];
		}
	}


	
	private static class StatemachineController extends EventProvider
	{
		public StatemachineController(Statemachine sm)
		{
			super();
			addEventListener(sm);
		}
		
	};
	
	
	
	public static interface KSPEthernetListener
	{
		void onKSPEthernetError(KSPEthernetClient sender, Exception e);
		void onKSPEthernetInvalidate(KSPEthernetClient sender, VesselData vesselData);
		void onKSPEthernetStateChanged(KSPEthernetClient sender, String state);
	}
	
	private List<KSPEthernetListener> listeners = new ArrayList<KSPEthernetListener>();		
	
	public void addEventListener(KSPEthernetListener l)
	{
		listeners.add(l);
	}
	public void removeEventListener(KSPEthernetListener l)
	{
		listeners.remove(l);			
	}
	
	private void notifyError(Exception e)
	{
		for(KSPEthernetListener l : listeners) l.onKSPEthernetError(this, e);
	}
	private void notifyInvalidate()
	{
		for(KSPEthernetListener l : listeners) l.onKSPEthernetInvalidate(this, vesselData);		
	}
	private void notifyStateChanged(State s)
	{
		for(KSPEthernetListener l : listeners) l.onKSPEthernetStateChanged(this, s.getName());		
	}
}
