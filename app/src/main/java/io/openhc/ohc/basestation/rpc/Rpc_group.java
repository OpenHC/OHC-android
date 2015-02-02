package io.openhc.ohc.basestation.rpc;

import java.util.ArrayList;
import java.util.List;

import io.openhc.ohc.OHC;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc;

public class Rpc_group
{
	private final OHC ohc;
	private List<Rpc> rpcs;
	private Rpc_group next;
	private RPC_GROUP_MODE mode;

	public Rpc_group(OHC ohc)
	{
		this(ohc, new ArrayList<Rpc>());
	}

	public Rpc_group(OHC ohc, List<Rpc> rpcs)
	{
		this(ohc, rpcs, RPC_GROUP_MODE.ALL);
	}

	public Rpc_group(OHC ohc, List<Rpc> rpcs, RPC_GROUP_MODE mode)
	{
		this(ohc, rpcs, mode, null);
	}

	public Rpc_group(OHC ohc, List<Rpc> rpcs, RPC_GROUP_MODE mode, Rpc_group next)
	{
		this.ohc = ohc;
		this.rpcs = rpcs;
		this.mode = mode;
		this.next = next;
	}

	public boolean has_finished()
	{
		boolean finished = true;
		switch(this.mode)
		{
			case ALL:
				for(Rpc rpc : this.rpcs)
					finished = finished && rpc.has_finished();
				break;
			case ANY:
				finished = this.rpcs.size() == 0;
				for(Rpc rpc : this.rpcs)
				{
					finished = finished || rpc.has_finished();
					if(finished)
						break;
				}
				break;
		}
		return finished;
	}

	public enum RPC_GROUP_MODE
	{
		ALL,
		ANY;
	}
}
