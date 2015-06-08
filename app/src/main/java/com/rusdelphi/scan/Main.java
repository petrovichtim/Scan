package com.rusdelphi.scan;

import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class Main extends AppCompatActivity implements
        AdapterView.OnItemClickListener {


    private static final String PAGE = "page";
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private static int page = 0;
    private Fragment pendingFragment = Networks.getInstance(R.string.nd_menu_active,"test");
    static DbAdapter mDb;


    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.header_bg)));
        actionBar.setIcon(R.drawable.menu);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setOnItemClickListener(this);
        drawerList.setAdapter(new MenuAdapter(this, R.layout.drawer_list_item,
                R.array.menu_items, R.array.menu_icons));
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.ns_menu_open, R.string.ns_menu_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                loadFragment();
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        loadSettings();
        if (mDb == null)
            mDb = new DbAdapter(this).open();
        loadFragment();
    }

    private void loadSettings() {
        // prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        // mDBisDownloaded = prefs.getBoolean(LoadFullBaseTask.DB_IS_DOWNLOADED,
        // false);
        // mDBisUnzipped = prefs.getBoolean(UnzipTask.DB_IS_UNZIPPED, false);
        // mKey = prefs.getString(LoadFullBaseTask.UPGRADE_KEY, null);
    }

    private void loadFragment() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (page == 4)// ���������
                {
                    FragmentTransaction ft = getSupportFragmentManager()
                            .beginTransaction();
                    ft.replace(R.id.content, pendingFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                } else {
                    if (!pendingFragment.isAdded()) {
                        getSupportFragmentManager().popBackStack(null,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        FragmentTransaction ft = getSupportFragmentManager()
                                .beginTransaction();
                        ft.replace(R.id.content, pendingFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                }
            }
        }).start();

    }

    // @Override
    // protected void onStop() {
    // if (mDb != null) {
    // mDb.close();
    // mDb = null;
    // }
    // super.onStop();
    // }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        page = position;
        Tools.hideSoftKeyboard(this);
        switch (position) {
            case 0:
                pendingFragment = Networks.getInstance(R.string.nd_menu_active,"test");
                break;
            case 1:
                pendingFragment =Networks.getInstance(R.string.nd_menu_hist,"test");
                break;

            case 2:
                pendingFragment = Settings.getInstance();
                break;
        }
        drawerLayout.closeDrawers();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStackImmediate();
        } else {
            finish();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration cfg) {
        super.onConfigurationChanged(cfg);
        drawerToggle.onConfigurationChanged(cfg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE, page);
    }

}
