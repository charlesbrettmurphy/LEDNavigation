package brett.lednavigation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
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
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SplashScreen.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SplashScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SplashScreen extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    // TODO: Refactor to make this cleaner and more modular.
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "uri";


    private String gatewayURL;
    private String mParam2;

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
    Bitmap sowilologobit;
    ImageView sowiloimageview;
    TextView bridgeIP;
    String userName;
    String ipAddress;
    Boolean mqttOn = false;
    Button rescanButton;
    int delay;

    //Hue variables

    private final String TAG = "Gateway Search";

    private Bridge bridge;
    private BridgeDiscovery bridgeDiscovery;
    private List<BridgeDiscoveryResult> bridgeDiscoveryResults;

    // UI elements
    private TextView statusTextView;
    private ListView bridgeDiscoveryListView;
    private View pushlinkImage;
    private Button hueAutoConnect;

    enum UIState {
        Idle,
        BridgeDiscoveryRunning,
        BridgeDiscoveryResults,
        Connecting,
        Pushlinking,
        Connected
    }

    //end of hue variables


    private OnFragmentInteractionListener mListener;

    public SplashScreen() {
        // Required empty public constructor
    }


    /* TODO: Rename and change types and number of parameters */
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        statusTextView = view.findViewById(R.id.statusTextView);
        final Button changeColor = view.findViewById(R.id.colorButton);
        final Button changeBrokerIP = view.findViewById(R.id.changeBrokerIP);
        final EditText enterGateWayIP = view.findViewById(R.id.enterGatewayIP);
        final EditText enterPort = view.findViewById(R.id.enterPort);
        final Button rescanButton = view.findViewById(R.id.rescanButton);
        rescanButton.setVisibility(View.INVISIBLE);


        sowiloimageview = view.findViewById(R.id.sowiloLogo);
        sowiloimageview.setDrawingCacheEnabled(true);
        bridgeDiscoveryListView = view.findViewById(R.id.bridge_discovery_result_list);
        bridgeDiscoveryListView.setOnItemClickListener(this);
        hueAutoConnect = view.findViewById(R.id.hueAutoConnect);
        statusTextView = view.findViewById(R.id.statusTextView);
        pushlinkImage = view.findViewById(R.id.pushlink_image);
        bridgeIP = view.findViewById(R.id.bridgeIP);

        if (!mqttOn) {

            enterGateWayIP.setVisibility(View.GONE);
            changeBrokerIP.setVisibility(View.GONE);
            enterPort.setVisibility(View.GONE);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.sowilo, options);
        sowilologobit = decodeSampledBitmapFromResource(getResources(), R.drawable.sowilo, 600, 200);
        sowiloimageview.setImageBitmap(sowilologobit);
        backgroundView = this.getView();
        //  pictureThread = new PictureThread(sowiloimageview, sowilologobit);
        // pictureThread.start();
        sowiloimageview.setAlpha(0f);
        sowiloimageview.animate().alpha(1f).setDuration(3000);
        delay = 30; // milliseconds between callbacks
        handler.postDelayed(new Runnable() {
            public void run() {
                String hexColor = String.format("%02x%02x%02x%02x", 255, redValue, greenValue, blueValue).toUpperCase();
                decimalColor = (int) Long.parseLong(hexColor, 16);
                //  String log = Integer.toString(decimalColor);
                int[] gradientColor = new int[6];
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
        if (gatewayURL != null) {
            Log.i("gatewayURL", gatewayURL);
            boolean isConnected = quickTestGatewayConnection(gatewayURL);
            //check that wifi is on and ping the gateway for internet services
            if (checkWifiOnAndConnected() && isConnected) {
                //if its a valid gateway and we have services connect to it
                onBridgeInitialized(gatewayURL);
                Log.i("onBridgeInitialized", "debug1");
                statusTextView.setText("Connected");
                hueAutoConnect.setVisibility(View.GONE);
                bridgeIP.setVisibility(View.INVISIBLE);
            } if (!checkWifiOnAndConnected()) {
                //if wifi is not connected inform the user to connect and rescan
                Log.i("in !wificheck", "debug 2");
                statusTextView.setText("Please connect to WIFI and press Hue AutoConnect");
                hueAutoConnect.setVisibility(View.VISIBLE);
                changeColor.setVisibility(View.GONE);
            } if (!isConnected){
                Log.i("isNotConnected", "debug3");
                //if wifi is connected but the connection test failed, try and use cache
                String bridgeIp = getLastUsedBridgeIp();
                if (bridgeIp == null) {
                    //if there is no cache then start bridge discovery
                    startBridgeDiscovery();
                } else {
                    //if there is, try to connect
                    bridgeIP.setText(bridgeIp);
                    connectToBridge(bridgeIp);
                }
            }
        } else { // if gatewayURL is null, then application must have just booted and we need to do this anyway
            if (checkWifiOnAndConnected()) {
                String bridgeIp = getLastUsedBridgeIp();
                if (bridgeIp==null) {
                    startBridgeDiscovery();
                }else{
                    bridgeIP.setText(bridgeIp);
                    connectToBridge(bridgeIp);

                }
            }

        }


        hueAutoConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkWifiOnAndConnected()) {
                    startBridgeDiscovery();
                } else {
                    statusTextView.setText("Please connect to WIFI and press Hue AutoConnect");

                }
            }
        });
    }


    public void onBridgeInitialized(String userUrl) {
        if (mListener != null) {
            mListener.onFragmentInteraction(userUrl);
        }
    }


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

    @Override
    public void onDetach() {
        super.onDetach();
        handler.removeCallbacksAndMessages(null);
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String userUrl);
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

    /**
     * changes radius values for gradient
     */
    public int returnGradientRadius(int brightValue) {
        int radius = 500;
        if (brightValue * 7 < radius)
            return radius;
        else
            return brightValue * 7;
    }

    public void colorCycle(int change) {

        if (rgbSwitch == true) {
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

        if (rgbSwitch == false) {
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

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if (wifiInfo.getNetworkId() == -1) {
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        } else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    /**
     * returns a modulating value for brightness between min and max.
     * brightValue is then used to calculate gradient radius for animation
     */
    public void brightCycle(int min, int max) {
        if (brightValue >= max)
            decreasing = true;
        if (brightValue <= min)
            decreasing = false;
        //could put a colorValue method call here and ui color will change as well as pulse
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
        testUrl =buildURL.getConnectionStatusUrl();
        Log.i("quickTest", testUrl);
        try {
            String result = bridgeCall.execute(testUrl, "GET").get();
            JSONObject jsonObject = new JSONObject(result);
            JSONObject internetservices = jsonObject.getJSONObject("internetservices");
            if (internetservices.getString("internet").equalsIgnoreCase("Connected"))
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }

    }


    /** hue Connect Methods */

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
                        Log.i(TAG, "Bridge discovery stopped.");
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
            Log.i(TAG, "Connection event: " + connectionEvent);

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
            Log.i(TAG, "Bridge state updated event: " + bridgeStateUpdatedEvent);

            switch (bridgeStateUpdatedEvent) {
                case INITIALIZED:
                    // The bridge state was fully initialized for the first time.
                    // It is now safe to perform operations on the bridge state.
                    updateUI(UIState.Connected, "Connected!");
                    // Log.i("Bridge Info", bridge.getInfo().toString());
                    ipAddress = bridge.getBridgeConfiguration().getNetworkConfiguration().getIpAddress();
                    userName = bridge.getBridgeConnection(BridgeConnectionType.LOCAL).getConnectionOptions().getUserName();
                    String userUrl = "http://".concat(ipAddress).concat("/api/").concat(userName).concat("/");
                    onBridgeInitialized(userUrl);
                    Log.i("Bridge Ip", ipAddress);
                    Log.i("Bridge UserName", userName);
                    break;

                case LIGHTS_AND_GROUPS:
                    // At least one light was updated.
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String bridgeIp = bridgeDiscoveryResults.get(i).getIP();
        Log.i("Ip Address", bridgeIp);
        bridgeIP.setText(bridgeIp);
        connectToBridge(bridgeIp);
    }

    @Override
    public void onClick(View view) {

        if (view == hueAutoConnect) {

            startBridgeDiscovery();
            Log.i("in hueAuto", "rediscovering");
        }
    }

    private void updateUI(final UIState state, final String status) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Status: " + status);
                statusTextView.setText(status);
                bridgeDiscoveryListView.setVisibility(View.GONE);
                pushlinkImage.setVisibility(View.GONE);
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
                        pushlinkImage.setVisibility(View.VISIBLE);
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


