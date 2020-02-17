package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncBroadcastClient.BroadcastEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncTcpClient.TcpEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.ControlPacket;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.HandshakePacket;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.PacketException;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.VesselData;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.HostState;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.AbstractEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.EventProvider;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.PacketHandler.PacketEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Statemachine.State;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Statemachine.StatemachineListener;

/**
 * KSPEthernetClient
 *
 * Handles all communication with KSPEthernetIO host.
 * Contains a statemachine that initializes the client, waits for user start command,
 * manages handshake with host and tries to keep connection alive.
 *
 * Provides various events for all KSPEthernetListener.
 *
 * @author Josh Perske
 */
public class KSPEthernetClient
{
	private static final int dt = 5; //Statemachine refresh rate in milliseconds

	//Statemachine controller is used to send commands to the statemachine
	//The commands are received in the active state.
	private Statemachine sm = new Statemachine("KSPEthernetClient", dt);
	private StatemachineController smc = new StatemachineController(sm);

	//All initialized and used in Statemachine
	private AsyncBroadcastClient broadcastClient = null;
	private AsyncTcpClient tcpClient = null;
	private PacketHandler packetHandler = null;
	private InetAddress host = null;

	//Content of controlData is frequently sent to connected host
	//vesselData contains last received VesselData
	public ControlPacket controlData = new ControlPacket();
	private VesselData vesselData = new VesselData();

	//Current host state
	private HostState hostState = HostState.Disconnected;

	private int port; //Host port
	private int refresh; //Data send refresh rate

	/**
	 * Start initialize KSPEthernetClient.
	 * Fully initialized if isInitialized() returns true.
	 * May take a few milliseconds after constructor was called.
	 *
	 * @param port Host port
	 * @param refresh Data send refresh rate
	 */
	public KSPEthernetClient(int port, int refresh)
	{
		this.port = port;
		this.refresh = refresh;

		//Connect StatemachineController to Statemachine
		smc.addEventListener(sm);

		//Add StatemachineListener to notify KSPEthernetListener on State change
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

		//Start Statemachine in State 0
		sm.start(S0_Initialize);
	}

	/**
	 * Start searching for host.
	 * It may take a few milliseconds until isActive() returns true.
	 */
	public void start()
	{
		if(sm.isActive()) smc.notifyEvent(StartCommand);
	}

	/**
	 * Stop client.
	 * It may take a few milliseconds until isStopped() returns true.
	 */
	public void stop()
	{
		if(sm.isActive()) smc.notifyEvent(StopCommand);		
	}

	/**
	 * Stop all background threads to prepare KSPEthernetClient for garbage collection.
	 * KSPEthernetClient object is not longer usable after call.
	 */
	public void destroy()
	{
		if(sm != null && sm.isActive()) sm.stop();
		if(broadcastClient != null && broadcastClient.isActive()) broadcastClient.cancelReceiveBroadcast();
		if(tcpClient != null && tcpClient.isActive()) tcpClient.cancelReceiveData();
	}

	/**
	 * Restart active client.
	 */
	public void reset()
	{
		if(sm.isActive()) smc.notifyEvent(ResetCommand);		
	}

	/**
	 * Get HostState.
	 *
	 * @return HostState
	 */
	public HostState getHostState()
	{
		return hostState;
	}

	/**
	 * Set HostState and notify listeners if changed.
	 */
	private void setHostState(HostState newState)
	{
		if(hostState != newState)
		{
			hostState = newState;
			notifyHostStateChanged(hostState);
		}
	}

	/**
	 * Get String representing current client state.
	 *
	 * @return Current client state as String
	 */
	public String getState()
	{
		if(sm.isActive()) return sm.getActiveState().getName();
		else return "Inactive";
	}

	/**
	 * Check if client is initialized after constructor call.
	 *
	 * @return True if fully initialized
	 */
	public boolean isInitialized()
	{
		return sm.isActive() && sm.getActiveState() != S0_Initialize;
	}

	/**
	 * Check if client is waiting for start command.
	 *
	 * @return True if client waits for start command
	 */
	public boolean isStopped()
	{
		return sm.isActive() && sm.getActiveState() == S1_WaitStart;
	}

	/**
	 * Check if client is ready to receiving and sending data.
	 *
	 * @return True if client is able to receive and send data
	 */
	public boolean isActive()
	{
		return sm.isActive() && sm.getActiveState() == S5_Active;
	}


	/**
	 * Dummy event provider to send Events to a Statenmachine.
	 */
	private static class StatemachineController extends EventProvider
	{
		public StatemachineController(Statemachine sm)
		{
			super();
			addEventListener(sm);
		}

	};

	/**
	 * Defines events that may be sent by a StatemachineController to a Statemachine
	 */
	private static class StatemachineCommand extends AbstractEvent
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

	//Commands to send with the StatemachineController to the active state of the statemachine
	private enum StatemachineCommandType {Start, Stop, Reset}
	private final StatemachineCommand StartCommand = new StatemachineCommand(smc, StatemachineCommandType.Start);
	private final StatemachineCommand StopCommand = new StatemachineCommand(smc, StatemachineCommandType.Stop);
	private final StatemachineCommand ResetCommand = new StatemachineCommand(smc, StatemachineCommandType.Reset);


	/**
	 * KSPEthernetClient event listener interface.
	 * Classes which want to receive data from the client should implement this.
	 */
	public interface KSPEthernetListener
	{
		void onKSPEthernetError(KSPEthernetClient sender, Exception e);
		void onKSPEthernetInvalidate(KSPEthernetClient sender, VesselData vesselData);
		void onKSPEthernetStateChanged(KSPEthernetClient sender, String state);
		void onKSPEthernetHostStateChanged(KSPEthernetClient sender, HostState state);
	}

	//Event listeners
	private List<KSPEthernetListener> listeners = new ArrayList<KSPEthernetListener>();

	/**
	 * Add KSPEthernetListener.
	 *
	 * @param l KSPEthernetListener
	 */
	public void addEventListener(KSPEthernetListener l)
	{
		listeners.add(l);
	}

	/**
	 * Remove KSPEthernetListener.
	 *
	 * @param l KSPEthernetListener
	 */
	public void removeEventListener(KSPEthernetListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Notify all listeners about an error.
	 *
	 * @param e Exception
	 */
	private void notifyError(Exception e)
	{
		for(KSPEthernetListener l : listeners) l.onKSPEthernetError(this, e);
	}

	/**
	 * Notify all listeners if new data was received.
	 */
	private void notifyInvalidate()
	{
		for(KSPEthernetListener l : listeners) l.onKSPEthernetInvalidate(this, vesselData);
	}

	/**
	 * Notify all listeners if the client state has changed.
	 *
	 * @param s Client state as String
	 */
	private void notifyStateChanged(State s)
	{
		for(KSPEthernetListener l : listeners) l.onKSPEthernetStateChanged(this, s.getName());
	}

	/**
	 * Notify all listeners if the host state has changed.
	 *
	 * @param s HostState
	 */
	private void notifyHostStateChanged(HostState s)
	{
		for(KSPEthernetListener l : listeners) l.onKSPEthernetHostStateChanged(this, s);
	}


	/* *************** *
	 *   Statemachine  *
	 * *************** */


	/**
	 * S0_Initialize
	 *
	 * Initialize broadcast client
	 * Initialize packet handler
	 * Jump to S1_WaitStart
	 */
	private State S0_Initialize = new State("Initialize", sm)
	{
		@Override
		public void onEnter()
		{
			setHostState(HostState.Disconnected);
			broadcastClient = new AsyncBroadcastClient(port);
			broadcastClient.addEventListener(sm);
			packetHandler = new PacketHandler();
			packetHandler.setBroadcastClient(broadcastClient);
			packetHandler.addEventListener(sm);
		}
		@Override
		public State onExecute(AbstractEvent event)
		{
			return S1_WaitStart;
		}
		@Override
		public void onExit()
		{
		}
	};

	/**
	 * S1_WaitStart
	 *
	 * Wait for Start command
	 * Then jump to S2_WaitBroadcast
	 */
	private State S1_WaitStart = new State("Wait for start command", sm)
	{
		@Override
		public void onEnter()
		{
			setHostState(HostState.Disconnected);
		}
		@Override
		public State onExecute(AbstractEvent event)
		{
			if(event == StartCommand) return S2_WaitBroadcast;
			else return this;
		}
		@Override
		public void onExit()
		{
		}
	};

	/**
	 * S2_WaitBroadcast
	 *
	 * Wait for HandshakeReceived then jump to S3_Connect
	 * On broadcastClient error jump to S0_Initialize
	 * On Stop jump to S7_Stop
	 */
	private State S2_WaitBroadcast = new State("Wait for broadcast", sm)
	{
		@Override
		public void onEnter()
		{
			setHostState(HostState.Disconnected);
			broadcastClient.startReceiveBroadcast();
		}
		@Override
		public State onExecute(AbstractEvent event)
		{
			//Check for Stop
			if(event == StopCommand) return S7_Stop;

			//Check for Handshake receive
			if(event != null && event.sender == packetHandler)
			{
				PacketEvent packetEvent = (PacketEvent) event;
				switch(packetEvent.getType())
				{
				case HandshakeReceived:
					//Save current host state
					setHostState(packetEvent.getHandshakePacket().getState());
					//Save host from received packet
					host = packetEvent.getHandshakePacket().sender;
					return S3_Connect;
				default:
					break;
				}
			}

			//Check for broadcast client error
			if(event != null && event.sender == broadcastClient)
			{
				BroadcastEvent broadcastEvent = (BroadcastEvent) event;
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

	/**
	 * S3_Connect
	 *
	 * Initialize TCP connection
	 * Wait for TCP connection accepted then jump to S4_Handshake
	 * On tcpClient error or manual restart jump to S6_Restart
	 * On Stop jump to S7_Stop
	 */
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
		public State onExecute(AbstractEvent event)
		{
			if(event == ResetCommand) return S6_Restart;
			if(event == StopCommand) return S7_Stop;
			
			if(event != null && event.sender == tcpClient)
			{
				TcpEvent tcpEvent = (TcpEvent) event;
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

	/**
	 * S4_Handshake
	 *
	 * Send Handshake packet
	 * On tcpClient error, handshake send error or manual restart jump to S6_Restart
	 * On Stop jump to S7_Stop
	 * If everything is fine jump to S5_Active
	 * If the handshake is not accepted the host will cancel the connection
	 */
	private State S4_Handshake = new State("Perform handshake", sm)
	{

		@Override
		public void onEnter()
		{			
		}
		@Override
		public State onExecute(AbstractEvent event)
		{
			//Check for manual reset
			if(event == ResetCommand) return S6_Restart;
			//Check for stop
			if(event == StopCommand) return S7_Stop;

			//Send handshake
			HandshakePacket HP = new HandshakePacket();
			HP.M1 = 3;
			HP.M2 = 1;
			HP.state = 4;
			try
			{
				tcpClient.sendData(HP.toPacket());
			}
			catch(PacketException e)
			{
				notifyError(e);
				return S6_Restart;
			}

			//Check for received data
			if(event != null && event.sender == packetHandler)
			{
				PacketEvent packetEvent = (PacketEvent) event;
				switch(packetEvent.getType())
				{
					case StatusPacketReceived:
						setHostState(packetEvent.getStatusPacket().getState());
						break;
					default:
						break;
				}
			}

			//Check for tcpClient error
			if(event != null && event.sender == tcpClient)
			{
				TcpEvent tcpEvent = (TcpEvent) event;
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

			//Everything was fine
			return S5_Active;
		}
		@Override
		public void onExit()
		{
		}
		
	};

	/**
	 * S5_Active
	 *
	 * Start timer
	 * Send data packet if refresh time is exceed
	 * Notify listeners if data was received
	 * On tcpClient error, data send error or manual restart jump to S6_Restart
	 * On Stop jump to S7_Stop
	 * If everything is fine stay in active state
	 */
	private State S5_Active = new State("Active", sm)
	{
		int sendTimer;

		@Override
		public void onEnter()
		{
			sendTimer = 0;
		}
		@Override
		public State onExecute(AbstractEvent event)
		{
			//Check for manual restart
			if(event == ResetCommand) return S6_Restart;
			//Check for stop
			if(event == StopCommand) return S7_Stop;

			//Refresh timer to send data packets
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

			//Check for received data
			if(event != null && event.sender == packetHandler)
			{
				PacketEvent packetEvent = (PacketEvent) event;
				switch(packetEvent.getType())
				{
				case VesselDataReceived:
					vesselData = packetEvent.getVesselData();
					notifyInvalidate();
					break;
				case StatusPacketReceived:
					setHostState(packetEvent.getStatusPacket().getState());
					break;
				default:
					break;
				}
			}			

			//Check for tcpClient error
			if(event != null && event.sender == tcpClient)
			{
				TcpEvent tcpEvent = (TcpEvent) event;
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

	/**
	 * S6_Restart
	 *
	 * Stop active tcpClient
	 * Jump to S2_WaitBroadcast
	 */
	private State S6_Restart = new State("Restart TCP client", sm)
	{
		@Override
		public void onEnter()
		{
			setHostState(HostState.Disconnected);
			if(tcpClient != null)
			{
				tcpClient.removeEventListener(sm);
				tcpClient.cancelReceiveData();
				tcpClient = null;
			}
		}
		@Override
		public State onExecute(AbstractEvent event)
		{
			return S2_WaitBroadcast;
		}
		@Override
		public void onExit()
		{
		}
		
	};

	/**
	 * S7_Stop
	 *
	 * Stop active tcpClient
	 * Jump to S1_WaitStart
	 */
	private State S7_Stop = new State("Shutdown TCP client", sm)
	{
		@Override
		public void onEnter()
		{
			setHostState(HostState.Disconnected);
			if(tcpClient != null)
			{
				tcpClient.removeEventListener(sm);
				tcpClient.cancelReceiveData();
				tcpClient = null;
			}
		}
		@Override
		public State onExecute(AbstractEvent event)
		{
			return S1_WaitStart;
		}
		@Override
		public void onExit()
		{
		}
		
	};

}
