package reputationnet.com.reputationnet;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RatedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rated);
        ((MaterialRatingBar) findViewById(R.id.rating)).setEnabled(false);
        ((MaterialRatingBar) findViewById(R.id.rating)).setRating(getIntent().getIntExtra("score", 0));
        ((TextView) findViewById(R.id.sender)).setText(getIntent().getStringExtra("sender"));
        MediaPlayer mediaPlayer = null;
        switch(getIntent().getIntExtra("score", 0)) {
            case 1: mediaPlayer = MediaPlayer.create(this, R.raw.nosedive_1_star); break;
            case 2: mediaPlayer = MediaPlayer.create(this, R.raw.nosedive_2_stars); break;
            case 3: mediaPlayer = MediaPlayer.create(this, R.raw.nosedive_3_stars); break;
            case 4: mediaPlayer = MediaPlayer.create(this, R.raw.nosedive_4_stars); break;
            case 5: mediaPlayer = MediaPlayer.create(this, R.raw.nosedive_5_stars); break;
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }



}
