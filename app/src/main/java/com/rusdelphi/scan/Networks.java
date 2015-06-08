package com.rusdelphi.scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class Networks extends Fragment {
    private ListView mListView;
    SimpleCursorAdapter sca;
    int mFragmentType;
    String mNetworkSsid;
    private static final String FRAGMENT_TYPE = "FRAGMENT_TYPE";
    private static final String NETWORK_SSID = "NETWORK_SSID";
    String[] from = new String[]{"name", "mac", "ip"};
    int[] to = new int[]{R.id.tv_name, R.id.tv_mac, R.id.tv_ip};
    View rootView;
    private ConnectivityManager connMgr;

    public final static String TAG = "Networks";
    protected final static String EXTRA_WIFI = "wifiDisabled";
    public static SharedPreferences prefs = null;
    public static NetInfo net = null;
    protected String info_ip_str = "";
    protected String info_in_str = "";
    protected String info_mo_str = "";
    protected String info_mac = "";
    private long network_ip = 0;
    private long network_start = 0;
    private long network_end = 0;
    final String NETWORK_URI = "content://com.rusdelphi.wifi_spy.providers.Networks/networks";
    final String ONE_NETWORK_URI = "content://com.rusdelphi.wifi_spy.providers.Networks/one_network";
    private long mNetwork_id;


    public static Networks getInstance(int fragmentType, String network_ssid) {
        Networks f = new Networks();
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_TYPE, fragmentType);
        args.putString(NETWORK_SSID, network_ssid);
        f.setArguments(args);
        f.setHasOptionsMenu(true);
        return f;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Listening for network events
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            info_ip_str = "";
            info_mo_str = "";

            // Wifi state
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                    int WifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    //Log.d(TAG, "WifiState=" + WifiState);
                    switch (WifiState) {
                        case WifiManager.WIFI_STATE_ENABLING:
                            info_in_str = getString(R.string.wifi_enabling);
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            info_in_str = getString(R.string.wifi_enabled);
                            break;
                        case WifiManager.WIFI_STATE_DISABLING:
                            info_in_str = getString(R.string.wifi_disabling);
                            break;
                        case WifiManager.WIFI_STATE_DISABLED:
                            info_in_str = getString(R.string.wifi_disabled);
                            break;
                        default:
                            info_in_str = getString(R.string.wifi_unknown);
                    }
                }

                if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) && net.getWifiInfo()) {
                    SupplicantState sstate = net.getSupplicantState(); // пошло подключение к сети
                    //Log.d(TAG, "SupplicantState=" + sstate);
                    if (sstate == SupplicantState.SCANNING) {
                        info_in_str = getString(R.string.wifi_scanning);
                    } else if (sstate == SupplicantState.ASSOCIATING) {
                        info_in_str = getString(R.string.wifi_associating,
                                (net.ssid != null ? net.ssid : (net.bssid != null ? net.bssid
                                        : net.macAddress)));
                    } else if (sstate == SupplicantState.COMPLETED) {
                        info_in_str = getString(R.string.wifi_dhcp, net.ssid); // получение ip адреса
                    }
                }
            }

            // получаем информацию по файфай соединению
            final NetworkInfo ni = connMgr.getActiveNetworkInfo();
            if (ni != null) {
                //Log.i(TAG, "NetworkState="+ni.getDetailedState());
                if (ni.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    int type = ni.getType();
                    //Log.i(TAG, "NetworkType="+type);
                    if (type == ConnectivityManager.TYPE_WIFI) { // WIFI
                        net.getWifiInfo();
                        if (net.ssid != null) {
                            net.getIp();
                            info_ip_str = getString(R.string.net_ip, net.ip, net.cidr, net.intf);
                            info_in_str = getString(R.string.net_ssid, net.ssid);
                            info_mo_str = getString(R.string.net_mode, getString(
                                    R.string.net_mode_wifi, net.speed, WifiInfo.LINK_SPEED_UNITS));
                            mNetworkSsid = net.ssid;
                            getNetworkId();
                        }
                    }
                }
            }

            // Always update network info
            setInfo();
        }


    };

    private void setInfo() {
        // Обновить интерфейс
        TextView tv_name = (TextView) rootView.findViewById(R.id.tv_name);
        //tv_name.setText("SSID: " + Tools.getWifiName(getActivity()));
        tv_name.setText(info_in_str);

        TextView tv_mac = (TextView) rootView.findViewById(R.id.tv_ip);
        // tv_mac.setText("MAC: " + Tools.getWifiMAC(getActivity()));
        tv_mac.setText(info_ip_str);
        TextView tv_mode = (TextView) rootView.findViewById(R.id.tv_mode);
        // tv_mode.setText("Mode: " + Tools.getWifiMode(getActivity()) + "Mbps");
        tv_mode.setText(info_mo_str);
        // Get ip information
        network_ip = NetInfo.getUnsignedLongFromIp(net.ip);


        // Detected IP начальный и конечный ip
        int shift = (32 - net.cidr);
        if (net.cidr < 31) {
            network_start = (network_ip >> shift << shift) + 1;
            network_end = (network_start | ((1 << shift) - 1)) - 1;
        } else {
            network_start = (network_ip >> shift << shift);
            network_end = (network_start | ((1 << shift) - 1));
        }

    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle(mFragmentType, null);
        //  net = new NetInfo(getActivity());
    }

    public void setTitle(int titleId, CharSequence subtitle) {
        setTitle(getActivity().getString(titleId), subtitle);
    }

    public void setTitle(CharSequence title, CharSequence subtitle) {
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity())
                    .getSupportActionBar();
            actionBar.setTitle(title);
            actionBar.setSubtitle(subtitle);
        } else {
            getActivity().setTitle(title);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentType = getArguments().getInt(FRAGMENT_TYPE);
        mNetworkSsid = getArguments().getString(NETWORK_SSID);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        net = new NetInfo(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.networks, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listView1);
        sca = new SimpleCursorAdapter(getActivity(), R.layout.list_item, null,
                from, to, 0);
        mListView.setAdapter(sca);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
//        if (mFragmentType == R.string.active_network)
//            inflater.inflate(R.menu.active_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.start_scan:
//                startScan();
//                return true;
//        }
        return true;
    }

    private void getNetworkId() {
        // надо получить network_id
        Cursor c = getActivity().getContentResolver().query(Uri.parse(ONE_NETWORK_URI), null, null,
                new String[]{mNetworkSsid}, null);
        c.moveToFirst();


        mNetwork_id = c.getLong((c.getColumnIndex("_id")));
        c.close();


    }


}
