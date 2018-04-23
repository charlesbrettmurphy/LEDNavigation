package brett.lednavigation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Timer;

import brett.lednavigation.dummy.DummyContent;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                           LightsFragment.OnListLightsFragmentInteractionListener,
                           GroupsFragment.OnListFragmentInteractionListener,
                           SchedulesFragment.OnListFragmentInteractionListener,
                           SplashScreen.OnFragmentInteractionListener {
    String uri = "";


    static {
        System.loadLibrary("huesdk");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Fragment fragment = null;
        Class fragmentClass = null;
        fragmentClass = SplashScreen.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            Log.i("Fragment error", e.toString());
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.flContent, fragment).commit();

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This will add new lights, groups and schedules eventually", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            Toast.makeText(getApplicationContext(), "This will eventually allow you to change color spaces, among other things", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    int lastSelectedId = -1; //to compare menu items and not reload fragments that are already loaded in content view

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        boolean hasToast = false;
        //if the selected fragment is not in view or if its the first load then launch the appropriate fragment
        //if the ids are equal, then the fragment is already in view so don't reload.
        if (lastSelectedId != id || lastSelectedId == -1) {
            if (id == R.id.nav_lights) {
                if (!uri.isEmpty()) {
                    LightsFragment lightsFragment = LightsFragment.newInstance(uri);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, lightsFragment, lightsFragment.getTag()).commit();
                    lastSelectedId = id;
                }

            } else if (id == R.id.nav_groups) {
                if (!uri.isEmpty()) {
                    GroupsFragment groupsFragment = GroupsFragment.newInstance(uri);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, groupsFragment, groupsFragment.getTag()).commit();
                    lastSelectedId = id;
                }

            } else if (id == R.id.nav_schedules) {
                hasToast = true;
            /*
            SchedulesFragment schedulesFragment = SchedulesFragment.newInstance(2);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, schedulesFragment, schedulesFragment.getTag()).commit();
*/
                lastSelectedId = id;
            }


            if (id == R.id.nav_home) {
                SplashScreen splashScreen = SplashScreen.newInstance(uri);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, splashScreen, splashScreen.getTag()).commit();
                lastSelectedId = id;
            }
            if (id == R.id.nav_feedback) {
                Intent intent = new Intent(MainActivity.this, FeedbackActivity.class);
                startActivity(intent);

            }
            if (id == R.id.nav_website) {
                Uri uri = Uri.parse("https://www.sowilodesign.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if (hasToast) {
            Toast.makeText(MainActivity.this, "Schedule alarms and ability set lights according to weather coming soon!", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override //Interface GroupsFragment colorButton
    public void onListFragmentInteraction(GroupsContent.GroupItem item) {
        Log.i("GroupsInterface", Integer.toString(item.id).concat(" ").concat(item.name));
        Log.i("colorButtonPressed", "");
        Intent intent = new Intent(MainActivity.this, LEDController.class);
        BuildURL buildURL = new BuildURL(uri);
        String url = buildURL.getGroupAttributesById(item.id);
        intent.putExtra("url", url);
        startActivity(intent);

    }

    @Override //Interface to GroupsFragment onSwitch
    public void onListFragmentInteraction(String anyOn, boolean isOn, int id) {
        BuildJSON buildJSON = new BuildJSON();
        BuildURL buildURL = new BuildURL(uri);
        String url = buildURL.setGroupState(id);
        BridgeCall bridgeCall = new BridgeCall();
        if (isOn) {
            bridgeCall.execute(url, "PUT", buildJSON.setLightOn().toString());
        } else {
            bridgeCall.execute(url, "PUT", buildJSON.setLightOff().toString());
        }
    }


    @Override //interface from SplashScreen Fragment passing ip and user for connected gateway
    public void onFragmentInteraction(String userUrl) {
        uri = userUrl;
        Log.i("Listener", userUrl);

    }


    @Override //interface from Schedules Fragment
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override //interface from LightsFragment
    public void onListLightsFragmentInteraction(LightsContent.LightItem item) {
        Log.i("colorButtonPressed", "");
        Intent intent = new Intent(MainActivity.this, LEDController.class);
        BuildURL buildURL = new BuildURL(uri);
        String url = buildURL.getLightState(item.id);
        intent.putExtra("url", url);
        startActivity(intent);

    }

    @Override//interface from LightsFragment to signal when the switch has been flipped
    public void onListLightsFragmentInteraction(String on, boolean isOn, int id) {
        BuildJSON buildJSON = new BuildJSON();
        BuildURL buildURL = new BuildURL(uri);
        String url = buildURL.setLightState(id);
        BridgeCall bridgeCall = new BridgeCall();
        if (isOn) {
            bridgeCall.execute(url, "PUT", buildJSON.setLightOn().toString());
        } else {
            bridgeCall.execute(url, "PUT", buildJSON.setLightOff().toString());
        }
    }

    @Override
    public void onListLightsFragmentInteraction(int id) {
        if (id == 0) {
            BuildURL buildURL = new BuildURL(uri);
            String url = buildURL.getLights();
            BridgeCall bridgeCall = new BridgeCall();
            bridgeCall.execute(url, "POST");
            new CountDownTimer(40000, 1000) {
                public void onTick(long millisUntilFinished) {
                    Toast.makeText(getApplicationContext(), "Searching for "+(int)millisUntilFinished/1000 , Toast.LENGTH_SHORT).show();
                }

                public void onFinish() {
                   Toast.makeText(getApplicationContext(), "Finished, please navigate home and then reload this screen", Toast.LENGTH_LONG).show();
                }
            }.start();

            }
        }
    }

