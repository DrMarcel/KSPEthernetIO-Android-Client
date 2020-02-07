package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;

import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncBroadcastClient.BroadcastMyEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncBroadcastClient.BroadcastMyEvent.BroadcastEventType;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncTcpClient.TcpMyEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.AsyncTcpClient.TcpMyEvent.TcpEventType;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.HandshakePacket;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.PacketException;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.DataPackets.VesselData;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.MyEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.EventListener;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.EventProvider;

public class PacketHandler extends EventProvider implements EventListener
{
	private AsyncTcpClient tcpClient = null;
	private AsyncBroadcastClient broadcastClient = null;
	
	public PacketHandler()
	{
		super();
	}
	
	public void setTcpClient(AsyncTcpClient client)
	{
		if(tcpClient != null) tcpClient.removeEventListener(this);
		tcpClient = client;
		tcpClient.addEventListener(this);
	}	
	public void setBroadcastClient(AsyncBroadcastClient client)
	{
		if(broadcastClient != null) broadcastClient.removeEventListener(this);
		broadcastClient = client;
		broadcastClient.addEventListener(this);
	}

	@Override
	public void onEvent(MyEvent myEvent)
	{
		if(myEvent.sender == broadcastClient) broadcastEvent((BroadcastMyEvent) myEvent);
		if(myEvent.sender == tcpClient) tcpEvent((TcpMyEvent) myEvent);
	}
	
	private void broadcastEvent(BroadcastMyEvent event)
	{
		if(event.getType() == BroadcastEventType.Received)
		{
			try
			{
				byte[] data = new byte[event.getData().getLength()];
				System.arraycopy(event.getData().getData(), 0, data, 0, event.getData().getLength());
				HandshakePacket HP = HandshakePacket.fromPacket(data);
				HP.sender = event.getData().getAddress();
				
				notifyEvent(new PacketMyEvent(this, PacketMyEvent.PacketEventType.HandshakeReceived, HP));
			}
			catch(PacketException e)
			{
				notifyEvent(new PacketMyEvent(this, PacketMyEvent.PacketEventType.PacketError, e));
			}
		}
	}
	
	private void tcpEvent(TcpMyEvent event)
	{
		if(event.getType() == TcpEventType.Received)
		{
			try
			{
				byte[] data = new byte[event.getData().length];
				System.arraycopy(event.getData(), 0, data, 0, event.getData().length);
				VesselData VDP = VesselData.fromPacket(data);
				notifyEvent(new PacketMyEvent(this, PacketMyEvent.PacketEventType.VesselDataReceived, VDP));
			}
			catch(PacketException e)
			{
				notifyEvent(new PacketMyEvent(this, PacketMyEvent.PacketEventType.PacketError, e));
			}
		}
	}
	
	public static class PacketMyEvent extends MyEvent
	{
		public static enum PacketEventType {HandshakeReceived, VesselDataReceived, PacketError}
		
		public PacketMyEvent(PacketHandler sender, PacketEventType t, HandshakePacket arg)
		{
			super(sender, t.ordinal(), arg);
		}
		public PacketMyEvent(PacketHandler sender, PacketEventType t, VesselData arg)
		{
			super(sender, t.ordinal(), arg);
		}
		public PacketMyEvent(PacketHandler sender, PacketEventType t, Exception arg)
		{
			super(sender, t.ordinal(), arg);
		}
		public PacketMyEvent(PacketHandler sender, PacketEventType t)
		{
			super(sender, t.ordinal(), null);
		}
		
		public PacketEventType getType()
		{
			return PacketEventType.values()[id];
		}
		public HandshakePacket getHandshakePacket()
		{
			return (HandshakePacket)arg;
		}
		public VesselData getVesselData()
		{
			return (VesselData)arg;
		}
		public Exception getException()
		{
			return (Exception)arg;
		}
	}

}
