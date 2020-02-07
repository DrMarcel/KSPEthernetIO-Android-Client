package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.MyEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.EventProvider;

public class AsyncBroadcastClient extends EventProvider
{
	private DatagramSocket socket = null;
	private int port;
	
	private AtomicBoolean active = new AtomicBoolean(false);
	private AtomicBoolean cancel = new AtomicBoolean(false);
	
	private AsyncBroadcastClient sender;
	
	
	
	public AsyncBroadcastClient(int port)
	{
		super();
		this.port = port;
		sender = this;
	}

	
	
	public void startReceiveBroadcast()
	{
		Thread t = new Thread(tWaitForBroadcast);
		
		t.start();
	}
	private Runnable tWaitForBroadcast = new Runnable()
	{
		@Override
		public void run()
		{
			//If already waiting for a Broadcast do nothing
			if(active.getAndSet(true)) return;
			
			cancel.set(false);
			Exception exception = null;

			notifyEvent(new BroadcastMyEvent(sender, BroadcastMyEvent.BroadcastEventType.Started));
			
			//Try to open the socket
			try
			{
				socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
				socket.setSoTimeout(10);
			}
			catch(Exception e)
			{
				exception = e;

				cancel.set(true);
			}
			
			//If socket was opened wait for data or error
			while(!cancel.get())
			{
				try
				{
					DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
					socket.receive(packet);
										
					notifyEvent(new BroadcastMyEvent(sender, BroadcastMyEvent.BroadcastEventType.Received, packet));
				}
				catch(SocketTimeoutException e)
				{
					//Check for user cancel
				}
				catch(Exception e)
				{
					exception = e;
				}
			}
			
			//Canceled
			if(socket!=null) socket.close();
			socket = null;
			cancel.set(false);
			active.set(false);	
			
			notifyEvent(new BroadcastMyEvent(sender, BroadcastMyEvent.BroadcastEventType.Canceled, exception));
			return;
		}
	};
		

	
	public boolean isActive()
	{
		return active.get();
	}
	public void cancelReceiveBroadcast()
	{
		if(isActive()) cancel.set(true);
	}
	
	

	public static class BroadcastMyEvent extends MyEvent
	{
		public static enum BroadcastEventType {Received, Started, Canceled}
		
		public BroadcastMyEvent(AsyncBroadcastClient sender, BroadcastEventType t, DatagramPacket arg)
		{
			super(sender, t.ordinal(), arg);
		}
		public BroadcastMyEvent(AsyncBroadcastClient sender, BroadcastEventType t, Exception arg)
		{
			super(sender, t.ordinal(), arg);
		}
		public BroadcastMyEvent(AsyncBroadcastClient sender, BroadcastEventType t)
		{
			super(sender, t.ordinal(), null);
		}
		
		public BroadcastEventType getType()
		{
			return BroadcastEventType.values()[id];
		}
		public DatagramPacket getData()
		{
			return (DatagramPacket)arg;
		}
		public Exception getException()
		{
			return (Exception)arg;
		}
	}
}
