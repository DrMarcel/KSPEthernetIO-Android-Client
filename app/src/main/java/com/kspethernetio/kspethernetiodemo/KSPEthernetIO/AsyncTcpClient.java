package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.AbstractEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.EventProvider;

/**
 * Async TCP client.
 * Connects to TCP server and provides events on data receive.
 */
public class AsyncTcpClient extends EventProvider
{
	private Socket socket = null;
	private SocketAddress server;
	
	private AtomicBoolean active = new AtomicBoolean(false); //Thread safe active stated
	private AtomicBoolean cancel = new AtomicBoolean(false); //Thread safe cancel signal
	
	public AsyncTcpClient sender; //Contains 'this' to access from child thread

    /**
     * Create new TCP client for specific host.
     *
     * @param address Host address
     * @param port Host port
     */
	public AsyncTcpClient(InetAddress address, int port)
	{
		super();
		server = new InetSocketAddress(address, port);
		sender = this;
	}

    /**
     * Try to connect to host.
     * Triggers Connected event on success.
     * Trigger Disconnected event on failure.
     */
	public void startReceiveData()
	{
		Thread t = new Thread(tWaitForData);
		t.start();
	}

    /**
     * Try to send data to connected host.
     *
     * @param data Data to send
     */
	public void sendData(byte[] data)
	{
		if(isActive())
		{
			try
			{
				socket.getOutputStream().write(data);
			}
			catch(Exception e)
			{
				cancel.set(true);
			}
		}
	}

    /**
     * Background thread listening for incoming data.
     * Can be stopped by setting cancel = true.
     * Delievers events on Connect, Disconnect and data receive
     */
	private Runnable tWaitForData = new Runnable()
	{		
		@Override
		public void run()
		{
			//If already waiting for data do nothing
			if(active.getAndSet(true)) return;
			
			cancel.set(false);
			Exception exception = null;
						
			//Try to open the socket
			try
			{
				//Try to connect
				socket = new Socket();
				socket.connect(server, 1000);
				socket.setSoTimeout(10);
				socket.setTcpNoDelay(true); // Don't buffer small packets
				socket.setReceiveBufferSize(1024);
				socket.setSendBufferSize(1024);
				notifyEvent(new TcpEvent(sender, TcpEvent.TcpEventType.Connected));
			}
			catch(Exception e)
			{
				exception = e;
				cancel.set(true);
			}

			byte[] buffer = new byte[1024];
			
			//If socket was opened wait for data or error
			while(!cancel.get())
			{
				try
				{
					int n = socket.getInputStream().read(buffer);
					
					if(n>0)
					{
						byte[] rec = new byte[n];
						for(int i=0;i<n;i++) rec[i]=buffer[i];
						notifyEvent(new TcpEvent(sender, TcpEvent.TcpEventType.Received, rec));
					}
					else cancel.set(true);
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
			if(socket.isConnected())
			{
				try { socket.close(); }
				catch(IOException e) {}
			}
			socket = null;
			cancel.set(false);
			active.set(false);	
			
			notifyEvent(new TcpEvent(sender, TcpEvent.TcpEventType.Disconnected, exception));

			return;
		}
	};

    /**
     * Check if TCP client is active.
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
	public void cancelReceiveData()
	{
		if(isActive()) cancel.set(true);
	}

    /**
     * TCP client events.
     */
	public static class TcpEvent extends AbstractEvent
	{
		public enum TcpEventType {Connected, Disconnected, Received}

        /**
         * New TcpEvent with received data.
         *
         * @param sender AsyncTcpClient
         * @param t TcpEventType
         * @param arg Byte array
         */
		public TcpEvent(AsyncTcpClient sender, TcpEventType t, byte[] arg)
		{
			super(sender, t.ordinal(), arg);
		}

        /**
         * New TcpEvent with Exception.
         *
         * @param sender AsyncTcpClient
         * @param t TcpEventType
         * @param arg Exception
         */
		public TcpEvent(AsyncTcpClient sender, TcpEventType t, Exception arg)
		{
			super(sender, t.ordinal(), arg);
		}

        /**
         * New TcpEvent without argument.
         *
         * @param sender AsyncTcpClient
         * @param t TcpEventType
         */
		public TcpEvent(AsyncTcpClient sender, TcpEventType t)
		{
			super(sender, t.ordinal(), null);
		}

        /**
         * Get event type.
         *
         * @return TcpEventType
         */
		public TcpEventType getType()
		{
			return TcpEventType.values()[id];
		}

        /**
         * Get data of received data event.
         *
         * @return Byte array
         */
		public byte[] getData()
		{
			return (byte[])arg;
		}

        /**
         * Get exception of disconnect event.
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
