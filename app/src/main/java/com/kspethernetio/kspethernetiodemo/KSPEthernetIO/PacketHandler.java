package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;

import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncBroadcastClient.BroadcastEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncBroadcastClient.BroadcastEvent.BroadcastEventType;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncTcpClient.TcpEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncTcpClient.TcpEvent.TcpEventType;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.HandshakePacket;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.PacketException;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.VesselData;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.AbstractEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.EventListener;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.EventProvider;

/**
 * PacketHandler is connected to TcpClient. Provides events whenever a VesselData or Handshake
 * was received.
 * The events can be received by an EventListener.
 */
public class PacketHandler extends EventProvider implements EventListener
{
	private AsyncTcpClient tcpClient = null;
	private AsyncBroadcastClient broadcastClient = null;

	/**
	 * Initialize new PacketHandler.
	 * To receive data and broadcasts an AsyncTcpClient and an AsyncBroadcastClient
	 * have to be connected via setTcpClient() and setBroadcastClient().
	 */
	public PacketHandler()
	{
		super();
	}

	/**
	 * Set or remove an AsyncTcpClient. The client is not automatically started.
	 *
	 * @param client AsyncTcpClient or null
	 */
	public void setTcpClient(AsyncTcpClient client)
	{
		if(tcpClient != null) tcpClient.removeEventListener(this);
		tcpClient = client;
		tcpClient.addEventListener(this);
	}

	/**
	 * Set or remove an AsyncBroadcastClient. The client is not automatically started.
	 *
	 * @param client AsyncBroadcastClient or null
	 */
	public void setBroadcastClient(AsyncBroadcastClient client)
	{
		if(broadcastClient != null) broadcastClient.removeEventListener(this);
		broadcastClient = client;
		broadcastClient.addEventListener(this);
	}

	/**
	 * Broadcast and TCP event listener.
	 *
	 * @param event BroadcastEvent or TcpEvent
	 */
	@Override
	public void onEvent(AbstractEvent event)
	{
		if(event.sender == broadcastClient) broadcastEvent((BroadcastEvent) event);
		if(event.sender == tcpClient) tcpEvent((TcpEvent) event);
	}

	/**
	 * Handle broadcast event.
	 * Read received data and try to convert to HandshakePacket.
	 * Triggers HandshakeReceived event if successful.
	 *
	 * @param event BroadcastEvent
	 */
	private void broadcastEvent(BroadcastEvent event)
	{
		if(event.getType() == BroadcastEventType.Received)
		{
			try
			{
				byte[] data = new byte[event.getData().getLength()];
				System.arraycopy(event.getData().getData(), 0, data, 0, event.getData().getLength());
				HandshakePacket HP = HandshakePacket.fromPacket(data);
				HP.sender = event.getData().getAddress();
				
				notifyEvent(new PacketEvent(this, PacketEvent.PacketEventType.HandshakeReceived, HP));
			}
			catch(PacketException e)
			{
				notifyEvent(new PacketEvent(this, PacketEvent.PacketEventType.PacketError, e));
			}
		}
	}

	/**
	 * Handle TCP event.
	 * Read received data and try to convert to ControlPacket.
	 * Triggers VesselDataReceived event if successful.
	 *
	 * @param event BroadcastEvent
	 */
	private void tcpEvent(TcpEvent event)
	{
		if(event.getType() == TcpEventType.Received)
		{
			try
			{
				byte[] data = new byte[event.getData().length];
				System.arraycopy(event.getData(), 0, data, 0, event.getData().length);
				VesselData VDP = VesselData.fromPacket(data);
				notifyEvent(new PacketEvent(this, PacketEvent.PacketEventType.VesselDataReceived, VDP));
			}
			catch(PacketException e)
			{
				notifyEvent(new PacketEvent(this, PacketEvent.PacketEventType.PacketError, e));
			}
		}
	}

	/**
	 * Packet event
	 */
	public static class PacketEvent extends AbstractEvent
	{
		public enum PacketEventType {HandshakeReceived, VesselDataReceived, PacketError}

		/**
		 * Create PacketEvent with HandshakePacket.
		 *
		 * @param sender PacketHandler
		 * @param t PacketEventType
		 * @param arg HandshakePacket
		 */
		public PacketEvent(PacketHandler sender, PacketEventType t, HandshakePacket arg)
		{
			super(sender, t.ordinal(), arg);
		}

		/**
		 * Create PacketEvent with VesselData.
		 *
		 * @param sender PacketHandler
		 * @param t PacketEventType
		 * @param arg VesselData
		 */
		public PacketEvent(PacketHandler sender, PacketEventType t, VesselData arg)
		{
			super(sender, t.ordinal(), arg);
		}

		/**
		 * Create PacketEvent with Exception.
		 *
		 * @param sender PacketHandler
		 * @param t PacketEventType
		 * @param arg Exception
		 */
		public PacketEvent(PacketHandler sender, PacketEventType t, Exception arg)
		{
			super(sender, t.ordinal(), arg);
		}

		/**
		 * Create PacketEvent without argument.
		 *
		 * @param sender PacketHandler
		 * @param t PacketEventType
		 */
		public PacketEvent(PacketHandler sender, PacketEventType t)
		{
			super(sender, t.ordinal(), null);
		}

		/**
		 * Get Packet event identifier.
		 *
		 * @return PacketEventType
		 */
		public PacketEventType getType()
		{
			return PacketEventType.values()[id];
		}

		/**
		 * Get HandshakePacket from HandshakeReceived event.
		 *
		 * @return HandshakePacket
		 */
		public HandshakePacket getHandshakePacket()
		{
			return (HandshakePacket)arg;
		}

		/**
		 * Get VesselData from VesselDataReceived event.
		 *
		 * @return VesselData
		 */
		public VesselData getVesselData()
		{
			return (VesselData)arg;
		}

		/**
		 * Get Exception from PacketError event.
		 *
		 * @return Exception
		 */
		public Exception getException()
		{
			return (Exception)arg;
		}
	}

}
