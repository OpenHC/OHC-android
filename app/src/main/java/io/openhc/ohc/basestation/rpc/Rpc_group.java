package io.openhc.ohc.basestation.rpc;

import java.util.ArrayList;
import java.util.List;

import io.openhc.ohc.OHC;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc;

/**
 * A RPC group handles the serial / parallel execution of RPCs and calls a callback whenever either
 * any of the associated RPCs or all of the RPCs finished execution
 *
 * @author Tobias Schramm
 */
public class Rpc_group
{
	private final OHC ohc;
	private List<Rpc> rpcs;
	private Rpc_group next;
	private RPC_GROUP_MODE mode;

	/**
	 * Default constructor.
	 *
	 * @param ohc OHC instance
	 */
	public Rpc_group(OHC ohc)
	{
		this(ohc, new ArrayList<Rpc>());
	}

	/**
	 * Specifies a list of RPCs to call on execution
	 *
	 * @param ohc  OHC instnace
	 * @param rpcs List of RPCs
	 */
	public Rpc_group(OHC ohc, List<Rpc> rpcs)
	{
		this(ohc, rpcs, RPC_GROUP_MODE.ALL);
	}

	/**
	 * Specifies a list of RPCs to call on execution and a group mode to determine which condition
	 * must be met for the group to be finished
	 *
	 * @param ohc  OHC instance
	 * @param rpcs List of RPCs
	 * @param mode Execution mode
	 */
	public Rpc_group(OHC ohc, List<Rpc> rpcs, RPC_GROUP_MODE mode)
	{
		this(ohc, rpcs, mode, null);
	}

	/**
	 * Specifies a list of RPCs to call on execution and a group mode to determine which condition
	 * must be met for the group to be finished. Also defines the next group of RPCs to be called
	 * this one
	 *
	 * @param ohc  OHC instance
	 * @param rpcs List of RPCs
	 * @param mode Execution mode
	 * @param next Next group to be executed
	 */
	public Rpc_group(OHC ohc, List<Rpc> rpcs, RPC_GROUP_MODE mode, Rpc_group next)
	{
		this.ohc = ohc;
		this.rpcs = rpcs;
		this.mode = mode;
		this.next = next;
	}

	/**
	 * Determines if this group has finished based on the group mode
	 *
	 * @return Has this group finished
	 */
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

	/**
	 * Contains all supported RPC group modes
	 */
	public enum RPC_GROUP_MODE
	{
		ALL,
		ANY;
	}
}
