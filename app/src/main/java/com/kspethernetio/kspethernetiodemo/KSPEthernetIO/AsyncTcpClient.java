package com.kspethernetio.kspethernetiodemo.KSPEthernetIO;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.MyEvent;
import com.kspethernetio.kspethernetiodemo.KSPEthernetIO.Events.EventProvider;

public class AsyncTcpClient extends EventProvider
{
	private Socket socket = null;
	private SocketAddress server;
	
	private AtomicBoolean active = new AtomicBoolean(false);
	private AtomicBoolean cancel = new AtomicBoolean(false);
	
	public AsyncTcpClient sender;
	
	
	public AsyncTcpClient(InetAddress address, int port)
	{
		super();
		server = new InetSocketAddress(address, port);
		sender = this;
	}

	
	
	public void startReceiveData()
	{
		Thread t = new Thread(tWaitForData);
		t.start();
	}
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
				socket.setTcpNoDelay(true);
				socket.setReceiveBufferSize(1024);
				socket.setSendBufferSize(1024);
				notifyEvent(new TcpMyEvent(sender, TcpMyEvent.TcpEventType.Connected));
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
						notifyEvent(new TcpMyEvent(sender, TcpMyEvent.TcpEventType.Received, rec));
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
			
			notifyEvent(new TcpMyEvent(sender, TcpMyEvent.TcpEventType.Disconnected, exception));

			return;
		}
	};
		

	
	public boolean isActive()
	{
		return active.get();
	}
	public void cancelReceiveData()
	{
		if(isActive()) cancel.set(true);
	}
	
	public static class TcpMyEvent extends MyEvent
	{
		public static enum TcpEventType {Connected, Disconnected, Received}
		
		public TcpMyEvent(AsyncTcpClient sender, TcpEventType t, byte[] arg)
		{
			super(sender, t.ordinal(), arg);
		}
		public TcpMyEvent(AsyncTcpClient sender, TcpEventType t, Exception arg)
		{
			super(sender, t.ordinal(), arg);
		}
		public TcpMyEvent(AsyncTcpClient sender, TcpEventType t)
		{
			super(sender, t.ordinal(), null);
		}
		
		public TcpEventType getType()
		{
			return TcpEventType.values()[id];
		}
		public byte[] getData()
		{
			return (byte[])arg;
		}
		public Exception getException()
		{
			return (Exception)arg;
		}
	}
}
