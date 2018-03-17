package reputationnet.com.reputationnet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.ChooserDialog;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.net.URISyntaxException;

public class LoginActivity extends AppCompatActivity {

    boolean walletSelected;
    String walletPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);
        walletSelected = false;
        findViewById(R.id.wallet_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ChooserDialog().with(LoginActivity.this)
                        .withStartFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                Toast.makeText(LoginActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                                walletPath = path;
                                ((TextView) findViewById(R.id.wallet_view)).setText(walletPath);
                                walletSelected = true;
                            }
                        })
                        .build()
                        .show();
            }
        });
        //Web3j web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"));


        findViewById(R.id.unlock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String password = ((EditText) findViewById(R.id.password)).getText().toString();
                    Credentials credentials = WalletUtils.loadCredentials(password, walletPath);
                    String displayName = ((TextView) findViewById(R.id.name)).getText().toString();
                    String title = ((TextView) findViewById(R.id.title)).getText().toString();
                    PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit()
                            .putString("password", password)
                            .putString("walletPath", walletPath)
                            .putString("name", displayName)
                            .putString("title", title)
                            .apply();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
                catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "Wallet file or password is incorrect", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

}
