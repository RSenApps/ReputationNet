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
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

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
    Credentials credentials;

    String contractAddress = "0x35d38490eE059e94BCC062dB965Ab45871637A9c";

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
        }

        ((TextView) findViewById(R.id.name)).setText(prefs.getString("name", ""));
        ((TextView) findViewById(R.id.title)).setText(prefs.getString("title", ""));
        findViewById(R.id.rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TranslateAnimation animate = new TranslateAnimation(
                        0,                 // fromXDelta
                        0,                 // toXDelta
                        0,                 // fromYDelta
                        v.getHeight()); // toYDelta
                animate.setDuration(500);
                animate.setFillAfter(true);
                v.startAnimation(animate);
                buttonHidden = true;
                sendRating("0x02a17c4a884a8060B16958984b56744a362289FB", (int) ((MaterialRatingBar) findViewById(R.id.rating)).getRating());

                ((MaterialRatingBar) findViewById(R.id.rating)).setRating(0);

            }
        });
        ((MaterialRatingBar) findViewById(R.id.rating)).setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                // Prepare the View for the animation
                View view = findViewById(R.id.rate);
                if (buttonHidden && rating > 0) {
                    buttonHidden = false;
                    view.setVisibility(View.VISIBLE);
                    TranslateAnimation animate = new TranslateAnimation(
                            0,                 // fromXDelta
                            0,                 // toXDelta
                            view.getHeight(),  // fromYDelta
                            0);                // toYDelta
                    animate.setDuration(500);
                    animate.setFillAfter(true);
                    view.startAnimation(animate);
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        Web3j web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"));
        Repnet contract = Repnet.load(contractAddress, web3j, credentials, BigInteger.valueOf(11), BigInteger.valueOf(100000));
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
        super.onPause();

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
                        if (e.sender.equals(credentials.getAddress())) {
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
            startActivity(i);
        }
    }
}


