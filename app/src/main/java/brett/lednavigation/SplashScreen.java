package brett.lednavigation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.HueLog;
import com.philips.lighting.hue.sdk.wrapper.Persistence;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnection;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedEvent;
import com.philips.lighting.hue.sdk.wrapper.connection.ConnectionEvent;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscovery;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryCallback;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryResult;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeBuilder;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridge;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridges;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Activities that contain this fragment must implement the
 * {@link SplashScreen.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SplashScreen extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    // TODO: Refactor to make this cleaner and more modular. Bit of a god Fragment

    private static final String ARG_PARAM1 = "uri";
    private String gatewayURL;
    Handler handler = new Handler();
    int[] color;
    int redValue = 255;
    int greenValue = 255;
    int blueValue = 0;
    int startColor = 0xffffffff; //0xfff9f7a8
    int endColor = 0xff000000;
    int decimalColor = 0xffecd473;
    int brightValue = 250;
    int getLogoBrightness = -110; //default
    boolean decreasing = false;
    boolean rgbSwitch = false;
    GradientDrawable background;
    View backgroundView;
    Bitmap sowiloLogoBitmap;
    ImageView sowiloImageView;
    TextView bridgeIP;
    String userName;
    String ipAddress;
    Boolean mqttOn = false;
    Button rescanButton;
    int delay;
    int[] gradientColor;

    //Hue variables

    private final String TAG = "Gateway Search";

    private Bridge bridge;
    private BridgeDiscovery bridgeDiscovery;
    private List<BridgeDiscoveryResult> bridgeDiscoveryResults;

    // UI elements
    private TextView statusTextView;
    private ListView bridgeDiscoveryListView;
    private View pushLinkImage;
    private Button hueAutoConnect;

    enum UIState {
        Idle,
        BridgeDiscoveryRunning,
        BridgeDiscoveryResults,
        Connecting,
        Pushlinking,
        Connected
    }

    public interface OnFragmentInteractionListener {
        void onUrlPassed(String userUrl);
    }

    private OnFragmentInteractionListener mListener;

    public SplashScreen() {
        // Required empty public constructor
    }

    public static SplashScreen newInstance(String uri) {
        SplashScreen fragment = new SplashScreen();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gatewayURL = getArguments().getString(ARG_PARAM1);
        }
        Persistence.setStorageLocation(getActivity().getFilesDir().getAbsolutePath(), "LEDNavigation");
        HueLog.setConsoleLogLevel(HueLog.LogLevel.INFO);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        statusTextView = view.findViewById(R.id.statusTextView);
        final Button changeBrokerIP = view.findViewById(R.id.changeBrokerIP);
        final EditText enterGateWayIP = view.findViewById(R.id.enterGatewayIP);
        final EditText enterPort = view.findViewById(R.id.enterPort);
        final Button rescanButton = view.findViewById(R.id.rescanButton);
        rescanButton.setVisibility(View.INVISIBLE);


        sowiloImageView = view.findViewById(R.id.sowiloLogo);
        sowiloImageView.setDrawingCacheEnabled(true);
        bridgeDiscoveryListView = view.findViewById(R.id.bridge_discovery_result_list);
        bridgeDiscoveryListView.setOnItemClickListener(this);
        hueAutoConnect = view.findViewById(R.id.hueAutoConnect);
        statusTextView = view.findViewById(R.id.statusTextView);
        pushLinkImage = view.findViewById(R.id.pushlink_image);
        bridgeIP = view.findViewById(R.id.bridgeIP);

        if (!mqttOn) {

            enterGateWayIP.setVisibility(View.GONE);
            changeBrokerIP.setVisibility(View.GONE);
            enterPort.setVisibility(View.GONE);
        }
        //load logo
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.sowilo, options);
        sowiloLogoBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.sowilo, 600, 200);
        sowiloImageView.setImageBitmap(sowiloLogoBitmap);
        backgroundView = this.getView();
        sowiloImageView.setAlpha(0f);
        sowiloImageView.animate().alpha(1f).setDuration(3000);

        //load splash screen animation
        gradientColor = new int[6];
        delay = 30; // milliseconds between callbacks
        handler.postDelayed(new Runnable() {
            public void run() {
                String hexColor = String.format("%02x%02x%02x%02x", 255, redValue, greenValue, blueValue).toUpperCase();
                decimalColor = (int) Long.parseLong(hexColor, 16);
                gradientColor[0] = startColor;
                gradientColor[1] = startColor;
                gradientColor[2] = decimalColor;
                gradientColor[3] = endColor;
                gradientColor[4] = endColor;
                gradientColor[5] = endColor;
                background = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, color);
                background.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                background.setGradientRadius(returnGradientRadius(brightValue));
                background.setGradientCenter(0, 1);
                background.setShape(GradientDrawable.RECTANGLE);
                background.setColors(gradientColor);
                backgroundView.setBackground(background);
                colorCycle(5);
                brightCycle(290, 370);
                //     pictureThread.adjustBrightness(getLogoBrightness - 155);
                bridgeIP.setTextColor(decimalColor);
                statusTextView.setTextColor(decimalColor);
                handler.postDelayed(this, delay);
            }
        }, delay);
        bridgeIP.setBackgroundColor(0x00000000);

        //TODO: Find a more elegant way to code this gateway check.
        //if we already have a gateway url;
        final CheckConnectivity checkConnectivity = new CheckConnectivity(getActivity());
        final boolean isWifiOn = checkConnectivity.checkWifiOnAndConnected();
        if (gatewayURL != null) {
            Log.d("gatewayURL", gatewayURL);
            if (isWifiOn) {
                boolean isConnected = quickTestGatewayConnection(gatewayURL);
                //check that wifi is on and ping the gateway for internet services
                if (isConnected) {
                    //if its a valid gateway and we have services connect to it
                    onBridgeInitialized(gatewayURL);
                    Log.d("onBridgeInitialized", "debug1");
                    statusTextView.setText("Connected");
                    hueAutoConnect.setVisibility(View.GONE);
                    bridgeIP.setVisibility(View.INVISIBLE);
                }
                if (!isConnected) {
                    Log.d("isNotConnected", "debug3");
                    //if wifi is connected but the connection test failed, try and use cache
                    String bridgeIp = getLastUsedBridgeIp();
                    if (bridgeIp == null) {
                        //if there is no cache then start bridge discovery
                        startBridgeDiscovery();
                    } else {
                        //if there is, try to connect
                        connectToBridge(bridgeIp);
                    }
                }
            }

            if (!isWifiOn) {
                //if wifi is not connected inform the user to connect and rescan
                Log.d("in !wificheck", "debug 2");
                statusTextView.setText("1. Please Connect To A WiFi Network \n 2. Press Auto-Connect");
                hueAutoConnect.setVisibility(View.VISIBLE);
            }
        } else { // if gatewayURL is null, then application must have just booted and we need to do this anyway

            if (isWifiOn) {
                String bridgeIp = getLastUsedBridgeIp();
                if (bridgeIp == null) {
                    startBridgeDiscovery();
                } else {
                    connectToBridge(bridgeIp);
                }
            } else {
                statusTextView.setText("1. Please Connect To A WiFi Network \n 2. Press Auto-Connect");
            }

        }

//
        hueAutoConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkConnectivity.checkWifiOnAndConnected()) {
                    startBridgeDiscovery();
                } else {
                    statusTextView.setText("1. Please Connect To A WiFi Network \n 2. Press Auto-Connect");

                }
            }
        });
    }

    //Passes the URL for the connected gateway and user to MainActivity
    public void onBridgeInitialized(String userUrl) {
        if (mListener != null) {
            mListener.onUrlPassed(userUrl);
        }
    }

    //Fragment Lifecycle Method
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                                               + " must implement OnFragmentInteractionListener");
        }
    }

    //Fragment Lifecycle Method
    @Override
    public void onDetach() {
        super.onDetach();
        handler.removeCallbacksAndMessages(null); //stop animation from burning cpu
        mListener = null;
    }


    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                           && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;

    }

    //changes radius values for light animation gradient

    public int returnGradientRadius(int brightValue) {
        int radius = 500;
        if (brightValue * 6 < radius)
            return radius;
        else
            return brightValue * 6;
    }

    //TODO: In settings, allow user to set the variable change.
    public void colorCycle(int change) {

        if (rgbSwitch==true) {
            redValue = redValue + change;
            if (redValue >= 255) {
                redValue = 255;
                blueValue = blueValue - change;
                if (blueValue <= 0) {
                    blueValue = 0;
                    greenValue = greenValue + change;
                    if (greenValue >= 255) {
                        greenValue = 255;
                    }

                }

            }
        }

        if (rgbSwitch==false) {
            redValue = redValue - change;
            if (redValue <= 0) {
                redValue = 0;
                blueValue = blueValue + change;
                if (blueValue >= 255) {
                    blueValue = 255;
                    greenValue = greenValue - change;
                    if (greenValue <= 0) {
                        greenValue = 0;
                        rgbSwitch = true;
                    }
                }
            }
        }
        if (redValue == 255 && greenValue == 255) {
            rgbSwitch = false;


        }
    }

    /*
      returns a modulating value for brightness between min and max.
      brightValue is then used to calculate gradient radius for animation
     */
    //TODO: In settings, allow the user to change min and max
    public void brightCycle(int min, int max) {
        if (brightValue >= max)
            decreasing = true;
        if (brightValue <= min)
            decreasing = false;
        if (!decreasing) {
            brightValue++;
            getLogoBrightness = getLogoBrightness + 4;
        }

        if (decreasing) {
            brightValue--;
            getLogoBrightness = getLogoBrightness - 4;
        }

    }

    public boolean quickTestGatewayConnection(String testUrl) {
        BridgeCall bridgeCall = new BridgeCall();
        BuildURL buildURL = new BuildURL(testUrl);
        testUrl = buildURL.getConnectionStatusUrl();
        Log.d("quickTest", testUrl);
        //TODO: Implement interface so waiting for the result is not blocking UI thread
        try {
            String result = bridgeCall.execute(testUrl, "GET").get();
            JSONObject jsonObject = new JSONObject(result);
            JSONObject internetservices = jsonObject.getJSONObject("internetservices");
            return (internetservices.getString("internet").equalsIgnoreCase("Connected"));

        } catch (Exception e) {
            return false;
        }

    }


    /**
     * Use the KnownBridges API to retrieve the last connected bridge
     *
     * @return Ip address of the last connected bridge, or null
     */
    private String getLastUsedBridgeIp() {
        List<KnownBridge> bridges = KnownBridges.getAll();
        //String whiteList = KnownBridges.retrieveWhitelistEntry();
        if (bridges.isEmpty()) {
            return null;
        }
        return Collections.max(bridges, new Comparator<KnownBridge>() {
            @Override
            public int compare(KnownBridge a, KnownBridge b) {
                return a.getLastConnected().compareTo(b.getLastConnected());
            }
        }).getIpAddress();
    }

    /**
     * Start the bridge discovery search
     * Read the documentation on meethue for an explanation of the bridge discovery options
     */
    private void startBridgeDiscovery() {
        disconnectFromBridge();

        bridgeDiscovery = new BridgeDiscovery();
        bridgeDiscovery.search(BridgeDiscovery.BridgeDiscoveryOption.UPNP, bridgeDiscoveryCallback);

        updateUI(UIState.BridgeDiscoveryRunning, "Scanning the network for hue bridges...");
    }

    /**
     * Stops the bridge discovery if it is still running
     */
    private void stopBridgeDiscovery() {
        if (bridgeDiscovery != null) {
            bridgeDiscovery.stop();
            bridgeDiscovery = null;
        }
    }

    /**
     * The callback that receives the results of the bridge discovery
     */
    private BridgeDiscoveryCallback bridgeDiscoveryCallback = new BridgeDiscoveryCallback() {
        @Override
        public void onFinished(final List<BridgeDiscoveryResult> results, final ReturnCode returnCode) {
            // Set to null to prevent stopBridgeDiscovery from stopping it
            bridgeDiscovery = null;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (returnCode == ReturnCode.SUCCESS) {
                        bridgeDiscoveryListView.setAdapter(new BridgeDiscoveryResultAdapter(getActivity().getApplicationContext(), results));
                        bridgeDiscoveryResults = results;

                        updateUI(UIState.BridgeDiscoveryResults, "Found " + results.size() + " bridge(s) in the network.");
                    } else if (returnCode == ReturnCode.STOPPED) {
                        Log.d(TAG, "Bridge discovery stopped.");
                    } else {
                        updateUI(UIState.Idle, "Error doing bridge discovery: " + returnCode);
                    }
                }
            });
        }
    };

    /**
     * Use the BridgeBuilder to create a bridge instance and connect to it
     */
    public void connectToBridge(String bridgeIp) {
        stopBridgeDiscovery();
        disconnectFromBridge();

        bridge = new BridgeBuilder("SowiloDesign", android.os.Build.MODEL)
                         .setIpAddress(bridgeIp)
                         .setConnectionType(BridgeConnectionType.LOCAL)
                         .setBridgeConnectionCallback(bridgeConnectionCallback)
                         .addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback)
                         .build();

        bridge.connect();

    }

    /**
     * Disconnect a bridge
     * The hue SDK supports multiple bridge connections at the same time,
     * but we are only using one
     */

    private void disconnectFromBridge() {
        if (bridge != null) {
            bridge.disconnect();
            bridge = null;
        }
    }


    /**
     * The callback that receives bridge connection events
     */

    private BridgeConnectionCallback bridgeConnectionCallback = new BridgeConnectionCallback() {
        @Override
        public void onConnectionEvent(BridgeConnection bridgeConnection, ConnectionEvent connectionEvent) {
            Log.d(TAG, "Connection event: " + connectionEvent);

            switch (connectionEvent) {
                case LINK_BUTTON_NOT_PRESSED:
                    updateUI(UIState.Pushlinking, "Press the link button to authenticate.");
                    break;

                case COULD_NOT_CONNECT:
                    updateUI(UIState.Connecting, "Could not connect.");
                    startBridgeDiscovery();

                    break;

                case CONNECTION_LOST:
                    updateUI(UIState.Connecting, "Connection lost. Attempting to reconnect.");
                    break;

                case CONNECTION_RESTORED:
                    updateUI(UIState.Connected, "Connection restored.");
                    break;

                case DISCONNECTED:
                    // User-initiated disconnection.
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onConnectionError(BridgeConnection bridgeConnection, List<HueError> list) {
            for (HueError error : list) {
                Log.e(TAG, "Connection error: " + error.toString());
            }
        }
    };

    /**
     * The callback the receives bridge state update events
     */
    private BridgeStateUpdatedCallback bridgeStateUpdatedCallback = new BridgeStateUpdatedCallback() {
        @Override
        public void onBridgeStateUpdated(Bridge bridge, BridgeStateUpdatedEvent bridgeStateUpdatedEvent) {
            Log.d(TAG, "Bridge state updated event: " + bridgeStateUpdatedEvent);
            switch (bridgeStateUpdatedEvent) {
                case INITIALIZED:
                    // The bridge state was fully initialized for the first time.
                    // It is now safe to perform operations on the bridge state.
                    updateUI(UIState.Connected, "Connected!");
                    // Log.d("Bridge Info", bridge.getInfo().toString());
                    ipAddress = bridge.getBridgeConfiguration().getNetworkConfiguration().getIpAddress();
                    userName = bridge.getBridgeConnection(BridgeConnectionType.LOCAL).getConnectionOptions().getUserName();
                    String userUrl = "http://".concat(ipAddress).concat("/api/").concat(userName).concat("/");
                    onBridgeInitialized(userUrl);
                    Log.d("Bridge Ip", ipAddress);
                    Log.d("UserName", userName);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String bridgeIp = bridgeDiscoveryResults.get(i).getIP();
        Log.d("Ip Address", bridgeIp);
        bridgeIP.setText(bridgeIp);
        connectToBridge(bridgeIp);
    }

    @Override
    public void onClick(View view) {

        if (view == hueAutoConnect) {

            startBridgeDiscovery();
            Log.d("in hueAuto", "rediscovering");
        }
    }

    private void updateUI(final UIState state, final String status) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Status: " + status);
                statusTextView.setText(status);
                bridgeDiscoveryListView.setVisibility(View.GONE);
                pushLinkImage.setVisibility(View.GONE);
                hueAutoConnect.setVisibility(View.GONE);

                switch (state) {
                    case Idle:
                        hueAutoConnect.setVisibility(View.VISIBLE);

                        break;
                    case BridgeDiscoveryRunning:
                        bridgeDiscoveryListView.setVisibility(View.VISIBLE);

                        break;
                    case BridgeDiscoveryResults:
                        bridgeDiscoveryListView.setVisibility(View.VISIBLE);
                        break;
                    case Connecting:
                        bridgeIP.setVisibility(View.VISIBLE);
                        hueAutoConnect.setVisibility(View.VISIBLE);

                        break;
                    case Pushlinking:
                        bridgeIP.setVisibility(View.VISIBLE);
                        pushLinkImage.setVisibility(View.VISIBLE);
                        hueAutoConnect.setVisibility(View.VISIBLE);
                        break;
                    case Connected:
                        bridgeIP.setVisibility(View.VISIBLE);
                        hueAutoConnect.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }
}


