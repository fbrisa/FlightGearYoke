package bfa.fgfs.flightgearyoke;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import java.io.IOException;

import bfa.fgfs.flightgearyoke.connection.FGFSConnection;
import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContentView;
import roboguice.inject.ContextSingleton;
import roboguice.inject.InjectView;

@ContextSingleton
@ContentView(R.layout.activity_connection)
public class ConnectionActivity extends RoboFragmentActivity {
    @InjectView(R.id.host)
    AutoCompleteTextView mHostView;
    @InjectView(R.id.port)
    EditText mPortView;
    @InjectView(R.id.progressBarConnect)
    ProgressBar mProgressView;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressSearchView;
    @Inject
    WifiManager wifiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}




    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private ConnectionTask mAuthTaskConnection = null;

    private boolean isHostNameValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPortValid(String password) {
        //TODO: Replace this with your own logic
        return true;
    }
    public void attemptConnect(View button) {
        if (mAuthTaskConnection != null) {
            return;
        }


        // Reset errors.
        mHostView.setError(null);
        mPortView.setError(null);

        // Store values at the time of the login attempt.
        String host = mHostView.getText().toString();
        String port = mPortView.getText().toString();


        mProgressView.setProgress(0);
        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(port) && !isPortValid(port)) {
            mPortView.setError(getString(R.string.error_field_required));
            focusView = mPortView;
            cancel = true;
        }

        mProgressView.setProgress(10);

        // Check for a valid host address.
        if (TextUtils.isEmpty(host)) {
            mHostView.setError(getString(R.string.error_field_required));
            focusView = mHostView;
            cancel = true;
        } else if (!isHostNameValid(host)) {
            mHostView.setError(getString(R.string.error_field_required));
            focusView = mHostView;
            cancel = true;
        }

        mProgressView.setProgress(20);

        if (cancel) {
            // There was an error;
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTaskConnection = new ConnectionTask(host, port);
            mAuthTaskConnection.execute((Void) null);


        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class ConnectionTask extends AsyncTask<Void, Void, Boolean> {

        private final String mHost;
        private final String mPort;

        ConnectionTask(String host, String port) {
            mHost = host;
            mPort = port;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                onProgressUpdate(30);

                FGFSConnection fgfsConnection = new FGFSConnection(mHost, new Integer(mPort).intValue());

                onProgressUpdate(40);

                boolean res = fgfsConnection.connect();
                onProgressUpdate(100);
                Thread.sleep(20);

                return res;
            } catch (InterruptedException e) {
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTaskConnection = null;

            if (success) {
                finish();
            } else {
                mPortView.setError(getString(R.string.error_field_required));
                mPortView.requestFocus();
            }
        }


        protected void onProgressUpdate(Integer... progress) {
            mProgressView.setProgress(progress[0]);
        }

        @Override
        protected void onCancelled() {
            mAuthTaskConnection = null;
        }
    }














    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
