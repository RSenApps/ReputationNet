package reputationnet.com.reputationnet;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RatedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rated);
        ((MaterialRatingBar) findViewById(R.id.rating)).setEnabled(false);
        ((MaterialRatingBar) findViewById(R.id.rating)).setRating(getIntent().getIntExtra("score", 0));
        String name = getIntent().getStringExtra("name");
        if (name != null && name.length() > 0) {
            ((TextView) findViewById(R.id.sender)).setText(name);
        }
        else {
            ((TextView) findViewById(R.id.sender)).setText(getIntent().getStringExtra("sender"));
        }
        MediaPlayer mediaPlayer = null;
        switch (getIntent().getIntExtra("score", 0)) {
            case 1:
                mediaPlayer = MediaPlayer.create(this, R.raw.nosedive_1_star);
                break;
            case 2:
                mediaPlayer = MediaPlayer.create(this, R.raw.nosedive_2_stars);
                break;
            case 3:
                mediaPlayer = MediaPlayer.create(this, R.raw.nosedive_3_stars);
                break;
            case 4:
                mediaPlayer = MediaPlayer.create(this, R.raw.nosedive_4_stars);
                break;
            case 5:
                mediaPlayer = MediaPlayer.create(this, R.raw.nosedive_5_stars);
                break;
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }

        TextView rep = (TextView) findViewById(R.id.rep);
        rep.setText("?");
        new UpdateReputation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getIntent().getStringExtra("recipient"));

        rep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateReputation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getIntent().getStringExtra("recipient"));
                Toast.makeText(RatedActivity.this, "Updating...", Toast.LENGTH_SHORT).show();
            }
        });
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
            ((TextView) findViewById(R.id.rep)).setText("" + result);


        }
    }
}
