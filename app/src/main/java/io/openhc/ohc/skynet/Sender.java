package io.openhc.ohc.skynet;

import android.os.AsyncTask;

import io.openhc.ohc.skynet.transaction.Transaction_generator;

public abstract class Sender extends AsyncTask<Transaction_generator.Transaction, Void,
		Transaction_generator.Transaction>
{

}
