package reputationnet.com.reputationnet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.TextView;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class MainActivity extends AppCompatActivity {
    boolean buttonHidden = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(prefs.getString("password", ""), prefs.getString("walletPath", ""));
        }
        catch (Exception e) {
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
}
