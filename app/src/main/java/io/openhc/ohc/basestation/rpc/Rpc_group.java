package io.openhc.ohc.basestation.rpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.openhc.ohc.OHC;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * A RPC group handles the serial / parallel execution of RPCs and calls a callback whenever either
 * any of the associated RPCs or all of the RPCs finished execution
 *
 * @author Tobias Schramm
 */
public class Rpc_group implements Transaction_generator.Transaction_receiver
{
	private final OHC ohc;
	private List<Rpc> rpcs;
	private Iterator<Rpc> rpc_iterator;
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
		this.ohc = ohc;
		this.rpcs = rpcs;
		this.mode = mode;
	}

	/**
	 * Determines if this group has finished based on the group mode
	 *
	 * @return Has this group finished execution
	 */
	public boolean has_finished()
	{
		boolean finished = true;
		for(Rpc rpc : this.rpcs)
			finished = finished && rpc.has_finished();
		return finished;
	}

	/**
	 * Sets a session token for all contained RPCs
	 *
	 * @param token Session token
	 */
	public void set_session_token(String token)
	{
		for(Rpc rpc : this.rpcs)
			rpc.set_session_token(token);
	}

	@Override
	public void on_receive_transaction(Transaction_generator.Transaction transaction)
	{

	}

	/**
	 * Contains all supported RPC group modes
	 */
	public enum RPC_GROUP_MODE
	{
		SERIAL,
		PARALLEL;
	}

	/**
	 * Interface for callbacks
	 */
	public interface Rpc_group_callback
	{
		/**
		 * Callback function
		 *
		 * @param group The RPC group that finished execution
		 */
		public void on_group_finish(Rpc_group group);
	}
}
