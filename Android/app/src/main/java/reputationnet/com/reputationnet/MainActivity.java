package reputationnet.com.reputationnet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {
    boolean buttonHidden = true;
    AsyncTask pollingTask;
    AsyncTask reputationTask;

    Credentials credentials;

    String contractAddress = "0x35d38490eE059e94BCC062dB965Ab45871637A9c";

    MessageListener listener;
    Message message;

    String addressToSendTo = "";
    MySimpleArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        try {
            credentials = WalletUtils.loadCredentials(prefs.getString("password", ""), prefs.getString("walletPath", ""));
        } catch (Exception e) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ((TextView) findViewById(R.id.name)).setText(prefs.getString("name", ""));
        ((TextView) findViewById(R.id.title)).setText(prefs.getString("title", ""));
        /*findViewById(R.id.rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.INVISIBLE);
                sendRating(addressToSendTo, (int) ((MaterialRatingBar) findViewById(R.id.rating)).getRating());
                Toast.makeText(MainActivity.this, "Submitted Rating", Toast.LENGTH_SHORT).show();

                ((MaterialRatingBar) findViewById(R.id.rating)).setRating(0);

            }
        });*/
        ((MaterialRatingBar) findViewById(R.id.rating)).setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                // Prepare the View for the animation
                if (addressToSendTo.equals("")) {
                    Toast.makeText(MainActivity.this, "Select user to rate first.", Toast.LENGTH_LONG).show();
                    return;
                }
                View view = findViewById(R.id.rate);
                if (buttonHidden && rating > 0) {
                    buttonHidden = false;
                    view.setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.rate).setOnTouchListener(new View.OnTouchListener() {
           /* @Override
            public void onSwipeUp() {
                if (addressToSendTo.equals("")) {
                    return;
                }
                View v = findViewById(R.id.rate);
                Toast.makeText(MainActivity.this, "Submitted Rating", Toast.LENGTH_SHORT).show();
                v.setVisibility(View.INVISIBLE);
                buttonHidden = true;
                sendRating(addressToSendTo, (int) ((MaterialRatingBar) findViewById(R.id.rating)).getRating());

                ((MaterialRatingBar) findViewById(R.id.rating)).setRating(0);
            }
*/
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int rating = (int) ((MaterialRatingBar) findViewById(R.id.rating)).getRating();
                if (addressToSendTo.equals("")  || rating == 0) {
                    return false;
                }
                View v = findViewById(R.id.rate);
                Toast.makeText(MainActivity.this, "Submitted Rating", Toast.LENGTH_SHORT).show();
                v.setVisibility(View.INVISIBLE);
                buttonHidden = true;
                sendRating(addressToSendTo, rating);

                ((MaterialRatingBar) findViewById(R.id.rating)).setRating(0);
                return false;
            }
        });

        adapter = new MySimpleArrayAdapter(this, new ArrayList<MySimpleArrayAdapter.Data>());
        ((ListView) findViewById(R.id.feed)).setAdapter(adapter);

        listener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.d("test", "Found message: " + new String(message.getContent()));
                String msg = new String(message.getContent());
                MySimpleArrayAdapter.Data d = new MySimpleArrayAdapter.Data();
                d.address = msg.split(",")[0];
                d.name = msg.split(",")[1];
                d.title = msg.split(",")[2];
                for (MySimpleArrayAdapter.Data x : adapter.values) {
                    if (x.address.equals(d.address)) {
                        return;
                    }
                }
                adapter.values.add(d);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onLost(Message message) {
                Log.d("test", "Lost sight of message: " + new String(message.getContent()));
            }
        };

        ((ListView) findViewById(R.id.feed)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                view.setSelected(true);
                addressToSendTo = adapter.values.get(position).address;

            }
        });

        String m = credentials.getAddress() + ", " + prefs.getString("name", "") + ", " + prefs.getString("title", "");
        message = new Message(m.getBytes());

        ((TextView) findViewById(R.id.reputation)).setText("?");
        ((TextView) findViewById(R.id.reputation)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reputationTask = new UpdateReputation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, credentials.getAddress());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Web3j web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"));
        Repnet contract = Repnet.load(contractAddress, web3j, credentials, BigInteger.valueOf(11), BigInteger.valueOf(100000));

        Log.d("test123", credentials.getAddress());
        reputationTask = new UpdateReputation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, credentials.getAddress());
        pollingTask = new PollRatingsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, contract);

        /*
        subscription = contract.subscribeRateEventEventObservable(DefaultBlockParameterName.EARLIEST, new Observer<Repnet.RateEventEventResponse>() {
            @Override
            public void onCompleted() {
                Log.e("test", "finished");

            }

            @Override
            public void onError(Throwable e) {
                Log.e("test", e.getLocalizedMessage());

            }

            @Override
            public void onNext(Repnet.RateEventEventResponse rateEventEventResponse) {
                Log.e("test", rateEventEventResponse.toString());
            }
        });*/

        /*Web3j web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"));
        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST, <contract-address>)
             .addSingleTopic();
        web3j.ethLogObservable(filter).subscribe(new Observer<Log>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Log log) {
                log.
            }
        });*/
    }

    @Override
    protected void onPause() {
        reputationTask.cancel(true);
        pollingTask.cancel(true);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Nearby.getMessagesClient(this).publish(message);
        Nearby.getMessagesClient(this).subscribe(listener);
    }

    @Override
    protected void onStop() {
        Nearby.getMessagesClient(this).unpublish(message);
        Nearby.getMessagesClient(this).unsubscribe(listener);
        super.onStop();
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit()
                    .putString("password", "")
                    .putString("walletPath", "")
                    .putString("name", "")
                    .putString("title", "")
                    .apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendRating(String recipient, int rating) {
        // web3j solidity generate contract.bin contract.abi -o . -p reputationnet.com.reputationnet
        Log.d("test", "starting task");
        Web3j web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"));
        Repnet contract = Repnet.load(contractAddress, web3j, credentials, BigInteger.valueOf(100), BigInteger.valueOf(100000));
        new RateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, recipient, BigInteger.valueOf(rating), contract);
    }

    static class RateTask extends AsyncTask<Object, Void, TransactionReceipt> {

        private Exception exception;

        protected TransactionReceipt doInBackground(Object... params) {
            try {
                Log.d("test", "starting");
                RemoteCall<TransactionReceipt> rc = ((Repnet) params[2]).rate((String) params[0], (BigInteger) params[1]);
                return rc.send();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(TransactionReceipt receipt) {
            if (receipt != null) {
                Log.d("test", receipt.toString());
            }
        }
    }

    class UpdateReputation extends AsyncTask<String, Void, Float> {

        private Exception exception;

        protected Float doInBackground(String... address) {
            while (true) {
                try {
                    Log.d("test", "rep" + address[0]);
                    NetClient nc = new NetClient("ec2-54-165-241-160.compute-1.amazonaws.com", 1234);

                    nc.sendDataWithString(address[0] + "\n");
                    Log.d("test", "sent");
                    return Float.parseFloat(nc.receiveDataFromServer());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        protected void onPostExecute(Float result) {
            ((TextView) findViewById(R.id.reputation)).setText("" + result);
        }
    }

    class PollRatingsTask extends AsyncTask<Repnet, Repnet.RateEventEventResponse, Repnet.RateEventEventResponse> {
        Subscription subscription;

        protected Repnet.RateEventEventResponse doInBackground(final Repnet... contract) {
            final Event event = new Event("RateEvent",
                    Arrays.<TypeReference<?>>asList(),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint8>() {}));
            Web3j web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"));
            EthBlockNumber startBlock;
            try {
               startBlock = web3j.ethBlockNumber().send();
            } catch (IOException e) {
                e.printStackTrace();
                startBlock = null;
            }
            while(true) {
                EthFilter filter = new EthFilter(new DefaultBlockParameterNumber(startBlock.getBlockNumber().add(BigInteger.ONE)), DefaultBlockParameterName.LATEST, contractAddress);
                filter.addSingleTopic(EventEncoder.encode(event));

                try {
                    EthLog ethLog = web3j.ethGetLogs(filter).send();

                    for (EthLog.LogResult l : ethLog.getLogs()) {
                        Repnet.RateEventEventResponse e = contract[0].getRateEventEvent((org.web3j.protocol.core.methods.response.Log) l);
                        if (e.receipient.equals(credentials.getAddress())) {
                            Log.d("test", e.receipient + ", " + e.sender + ", " + e.score);
                           return e;
                        }
                        Log.d("test, but no good", e.receipient + ", " + e.sender + ", " + e.score);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


                try {
                    startBlock = web3j.ethBlockNumber().send();
                } catch (IOException e) {
                    e.printStackTrace();
                    startBlock = null;
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        protected void onPostExecute(Repnet.RateEventEventResponse event) {
            Intent i = new Intent(MainActivity.this, RatedActivity.class);
            i.putExtra("sender", event.sender);
            i.putExtra("recipient", event.receipient);
            i.putExtra("score", event.score.intValue());
            for (int x = 0; x < adapter.values.size(); x++) {
                if (adapter.values.get(x).address.equals(event.sender)) {
                    i.putExtra("name", adapter.values.get(x).name);
                    break;
                }
            }
            startActivity(i);
        }
    }
}


