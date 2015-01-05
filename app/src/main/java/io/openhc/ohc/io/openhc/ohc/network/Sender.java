package io.openhc.ohc.io.openhc.ohc.network;

import java.net.SocketAddress;

public class Sender
{
	SocketAddress endpoint_address;

	public Sender(SocketAddress endpoint_address)
	{
		this.endpoint_address = endpoint_address;
	}

	public void kill()
	{
		
	}
}
