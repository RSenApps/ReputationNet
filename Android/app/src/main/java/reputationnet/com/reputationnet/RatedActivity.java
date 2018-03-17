package reputationnet.com.reputationnet;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RatedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rated);
        ((MaterialRatingBar) findViewById(R.id.rating)).setRating(getIntent().getIntExtra("score", 0));
        ((TextView) findViewById(R.id.sender)).setText(getIntent().getStringExtra("sender"));
    }

}
