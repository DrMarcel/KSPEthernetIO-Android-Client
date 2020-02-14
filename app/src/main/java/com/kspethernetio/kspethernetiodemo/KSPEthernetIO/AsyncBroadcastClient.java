package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.AbstractEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.EventProvider;

/**
 * Async UDP broadcast listener.
 * Listens on specified port for broadcast messages and provides events if a broadcast packet was
 * received.
 */
public class AsyncBroadcastClient extends EventProvider
{
	private DatagramSocket socket = null;
	private int port;
	
	private AtomicBoolean active = new AtomicBoolean(false); //Thread safe active state
	private AtomicBoolean cancel = new AtomicBoolean(false); //Thread safe cancel signal
	
	private AsyncBroadcastClient sender; //Contains 'this' to access from child thread


	/**
	 * Create new broadcast client listening on specific port.
	 *
	 * @param port Port
	 */
	public AsyncBroadcastClient(int port)
	{
		super();
		this.port = port;
		sender = this;
	}

	/**
	 * Start listening.
	 * Starts background thread listening for broadcasts.
	 * Triggers a 'Started' event.
	 * If not successful also triggers a 'Canceled' event.
	 */
	public void startReceiveBroadcast()
	{
		Thread t = new Thread(tWaitForBroadcast);
		
		t.start();
	}

	/**
	 * Background thread listening for broadcasts.
	 * Can be stopped by setting cancel = true.
	 * Delievers events on Start, Stop and data receive
	 */
	private Runnable tWaitForBroadcast = new Runnable()
	{
		@Override
		public void run()
		{
			//If already waiting for a Broadcast do nothing
			if(active.getAndSet(true)) return;
			
			cancel.set(false);
			Exception exception = null;

			notifyEvent(new BroadcastEvent(sender, BroadcastEvent.BroadcastEventType.Started));
			
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
										
					notifyEvent(new BroadcastEvent(sender, BroadcastEvent.BroadcastEventType.Received, packet));
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
			
			notifyEvent(new BroadcastEvent(sender, BroadcastEvent.BroadcastEventType.Canceled, exception));
			return;
		}
	};

	/**
	 * Check if broadcast client is active.
	 *
 	 * @return True if active
	 */
	public boolean isActive()
	{
		return active.get();
	}

	/**
	 * Cancel listening.
	 */
	public void cancelReceiveBroadcast()
	{
		if(isActive()) cancel.set(true);
	}

	/**
	 * Broadcast events
	 */
	public static class BroadcastEvent extends AbstractEvent
	{
		public enum BroadcastEventType {Received, Started, Canceled}

		/**
		 * Create BroadcastEvent with DatagramPacket.
		 *
		 * @param sender AsyncBroadcastClient
		 * @param t BroadcastEventType
		 * @param arg DatagramPacket
		 */
		public BroadcastEvent(AsyncBroadcastClient sender, BroadcastEventType t, DatagramPacket arg)
		{
			super(sender, t.ordinal(), arg);
		}

		/**
		 * Create BroadcastEvent with Exception.
		 *
		 * @param sender AsyncBroadcastClient
		 * @param t BroadcastEventType
		 * @param arg Exception
		 */
		public BroadcastEvent(AsyncBroadcastClient sender, BroadcastEventType t, Exception arg)
		{
			super(sender, t.ordinal(), arg);
		}

		/**
		 * Create BroadcastEvent without argument.
		 *
		 * @param sender AsyncBroadcastClient
		 * @param t BroadcastEventType
		 */
		public BroadcastEvent(AsyncBroadcastClient sender, BroadcastEventType t)
		{
			super(sender, t.ordinal(), null);
		}

		/**
		 * Get Event type.
		 *
		 * @return BroadcastEventType
		 */
		public BroadcastEventType getType()
		{
			return BroadcastEventType.values()[id];
		}

		/**
		 * Get data from Received event.
		 *
		 * @return DatagramPacket
		 */
		public DatagramPacket getData()
		{
			return (DatagramPacket)arg;
		}

		/**
		 * Get exception from Canceled event.
		 * Return null on user cancel.
		 *
		 * @return Exception or null
		 */
		public Exception getException()
		{
			return (Exception)arg;
		}
	}
}
