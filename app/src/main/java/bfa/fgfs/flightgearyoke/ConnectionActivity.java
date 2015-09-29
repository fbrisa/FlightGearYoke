package bfa.fgfs.flightgearyoke;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import bfa.fgfs.flightgearyoke.connection.FGFSConnection;
import bfa.fgfs.flightgearyoke.connection.IPv4;
import roboguice.activity.RoboActivity;
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

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private ConnectionTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}



    public void attemptConnect(View button) {
        if (mAuthTask != null) {
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
            mAuthTask = new ConnectionTask(host, port);
            mAuthTask.execute((Void) null);


        }
    }

    private boolean isHostNameValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPortValid(String password) {
        //TODO: Replace this with your own logic
        return true;
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
            mAuthTask = null;

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
            mAuthTask = null;
        }
    }

    private static final int DISCOVERY_TIMEOUT = 5000;

    private boolean searchCancelled = false;
    private final Object lock = new Object();

    public static String intToIp(int i) {

        return ((i >> 24) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                (i & 0xFF);
    }

    public void search(View button) {

        // Store values at the time of the login attempt.
        try {
            int port = new Integer(mPortView.getText().toString());

            SearchTask s = new SearchTask(port);
            s.execute((Void) null);
        } catch (NumberFormatException e) {
            Log.i("SEARCH", "Not a valid port");
        }

        // This works both in tethering and when connected to an Access Point





        // Get wifi ip address
        //int wifiIpAddress = wifiManager.getConnectionInfo().getIpAddress();
        //String ip = intToIp(wifiIpAddress);


//                    InetAddress wifiInetAddress = NetUtils.intToInetAddress(wifiIpAddress);
//
//                    // Acquire multicast lock
//                    multicastLock = wifiManager.createMulticastLock("kore2.multicastlock");
//                    multicastLock.setReferenceCounted(false);
//                    multicastLock.acquire();
//
//                    JmDNS jmDns = (wifiInetAddress != null)?
//                            JmDNS.create(wifiInetAddress) :
//                            JmDNS.create();
//
//                    // Get the json rpc service list
//                    final ServiceInfo[] serviceInfos =
//                            jmDns.list(MDNS_FGFS_SERVICENAME, DISCOVERY_TIMEOUT);
//
//                    synchronized (lock) {
//                        // If the user didn't cancel the search, and we are sill in the activity
//                        if (!searchCancelled) {// && isAdded()
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if ((serviceInfos == null) || (serviceInfos.length == 0)) {
//                                        noHostFound();
//                                    } else {
//                                        foundHosts(serviceInfos);
//                                    }
//                                }
//                            });
//                        }
//                    }


    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class SearchTask extends AsyncTask<Void, Void, Boolean> {


        private final int port;

        SearchTask(int port) {
            this.port = port;
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            onProgressUpdate(0, 100);

            try {
                Enumeration<NetworkInterface> interfaces = interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();

                    try {
                        if (networkInterface.isLoopback())
                            continue; // Don't want to broadcast to the loopback interface
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();


                        // Android seems smart enough to set to null broadcast to
                        //  the external mobile network. It makes sense since Android
                        //  silently drop UDP broadcasts involving external mobile network.
                        if (broadcast == null)
                            continue;

                        InetAddress ip_ = interfaceAddress.getAddress();
                        int networkPrefix = (int) interfaceAddress.getNetworkPrefixLength();


                        String ip = ip_.getHostAddress();

                        IPv4 ipv4 = new IPv4(ip+"/"+(new Integer(networkPrefix)).toString());

                        List<String> ips = ipv4.getAvailableIPs();

                        class MyThread implements Runnable {


                            MyThread() {

                            }

                            public boolean vivi=true;
                            public int state=0;
                            public String ip=null;
                            public int port=0;

                            @Override
                            public void run() {

                                Log.i("SOCK", "new thread");

                                while (vivi) {

                                    if (state==1) {
                                        try {
                                            state = 1;
                                            Socket socket = new Socket(ip, port);
                                            Log.i("SOCK", "found:" + ip);
                                        } catch (IOException e) {

                                        }
                                        state = 2;
                                    }



                                    try {
                                        Thread.sleep(20);
                                    } catch (InterruptedException e) { }
                                }

                                Log.i("SOCK", "end thread");
                            }
                        }



                        MyThread threads[]=new MyThread[10];
                        for (int t = 0; t < 10; t++) {
                            threads[t]=new MyThread();
                            new Thread(threads[t]).start();
                        }

                        int s = ips.size();
                        int t=0;
                        while (t<s) {

                            // search first available thread
                            for (MyThread m:threads) {
                                if (m.state!=1 && t<s) {
                                    m.port=port;
                                    m.ip=ips.get(t);
                                    m.state=1;
                                    onProgressUpdate(t,s);
                                    t++;
                                    break;
                                }
                            }

                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) { }

                        }

                        boolean waitingAllThread=true;
                        while(waitingAllThread) {
                            waitingAllThread=false;
                            for (MyThread m:threads) {
                                if (m.state==1) {
                                    waitingAllThread=true;
                                    break;
                                }
                            }
                        }



                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }



            return true;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                //finish();
            } else {

            }
        }


        protected void onProgressUpdate(Integer... progress) {
            mProgressSearchView.setMax(progress[1]);
            mProgressSearchView.setProgress(progress[0]);
            Log.d("SEA", new Integer(progress[0]).toString()+"/"+new Integer(progress[1]).toString());
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
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
