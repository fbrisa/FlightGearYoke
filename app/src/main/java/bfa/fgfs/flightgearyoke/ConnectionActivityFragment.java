package bfa.fgfs.flightgearyoke;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import bfa.fgfs.flightgearyoke.connection.FGFSConnection;
import bfa.fgfs.flightgearyoke.connection.IPv4;
import roboguice.RoboGuice;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;


/**
 * A placeholder fragment containing a simple view.
 */
public class ConnectionActivityFragment extends RoboFragment {
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


    @InjectView(R.id.buttonSearch) Button buttonSearch;


    public ConnectionActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connection, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //RoboGuice.getInjector(getActivity()).injectViewMembers(this);

        setRetainInstance(true);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(v);
            }
        });


    }


    private boolean isHostNameValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPortValid(String password) {
        //TODO: Replace this with your own logic
        return true;
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
        try {
            int port = new Integer(mPortView.getText().toString());

            SearchTask s = new SearchTask(port);
            s.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,(Void) null);
        } catch (NumberFormatException e) {
            Log.i("SEARCH", "Not a valid port");
        }
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
                                            Socket client=new Socket();
                                            client.connect(new InetSocketAddress(ip,port),2*1000);
                                            Log.i("SOCK", "found:" + ip);
                                        } catch (IOException e) {

                                        }
                                        state = 2;
                                    }



                                    try {Thread.sleep(20);} catch (InterruptedException e) { }
                                }

                                Log.i("SOCK", "end thread");
                            }
                        }


                        final int MAX_SEARCH_THREADS=30;
                        MyThread threads[]=new MyThread[MAX_SEARCH_THREADS];
                        for (int t = 0; t < MAX_SEARCH_THREADS; t++) {
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
                                    Log.i("START", m.ip);
                                    onProgressUpdate(t,s);
                                    t++;
                                    break;
                                }
                            }

                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) { }

                        }

                        for (MyThread m:threads) {
                            m.vivi=false;
                        }

                        boolean waitingAllThread=true;
                        while(waitingAllThread) {
                            waitingAllThread=false;
                            for (MyThread m:threads) {
                                if (m.state==1) {
                                    Log.i("WAIT", m.ip);
                                    waitingAllThread=true;
                                    break;
                                }
                            }

                            try {Thread.sleep(20);} catch (InterruptedException e) { }
                        }

                        onProgressUpdate(0,s);

                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }



            return true;

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //mAuthTask = null;

            if (success) {
                //finish();
            } else {

            }
        }


        protected void onProgressUpdate(Integer... progress) {
            mProgressSearchView.setMax(progress[1]);
            mProgressSearchView.setProgress(progress[0]);
            //Log.d("SEA", new Integer(progress[0]).toString()+"/"+new Integer(progress[1]).toString());
        }

        @Override
        protected void onCancelled() {
            //mAuthTask = null;
        }
    }

}
