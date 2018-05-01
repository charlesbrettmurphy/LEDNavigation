package brett.lednavigation;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * This Activity class contains all the controls for adjusting light settings.
 * This Activity should be passed the url of the light or group needing adjusting
 */

public class LEDController extends AppCompatActivity {
    private final String USER_ID = "QIJVaMgJjb9eUshNm-QOV9SyPeSVfEJCHjPb02RB";
    private String desktop_ID = "j3QuLeYd0hwRahCerIabErwQI6ZTbAJ0Ni7FQg1j";
    private String TEST_SERVER = "tcp://test.mosquitto.org:1883";
    private String bridgeBuilderUrl;
    /* Initial setttings for seekbars */
    private int redValue = 255;
    private int blueValue = 0;
    private int greenValue = 255;
    private int hueValue = 0;
    int time = 6;
    private int colorInt; //color value
    float[] hueSatBright; // array for hue sat brightness values
    final int START_COLOR = 0xfff9f7a8; //#ffecd473 0xfff9f7a8 for ui light
    final int START_COLOR2=  0xffffffff;
    final int END_COLOR = 0x00000000; //black
    private View backgroundView; //View container for ui light animation
    static String json; //to deliver json payload
    private static TextView progressTextView; //Textview that displays submitted and retrieved info and error messages
    SeekBar redSeek;
    SeekBar blueSeek;
    SeekBar greenSeek;
    private Button colorLoopButton;  //loops through all available hues
    private Button colorTempButton; //launches a new activity for controlling color temperature
    private Switch onSwitch; //on and off
    private Switch protocolSwitch; //toggle between mqtt and http
    private boolean mqttOn = false; //default is http
    boolean isLooping = false;
    private Button mqttClient; //loads a page to manually edit topics and payloads.
    Button cycleButton;
    String clientId;
    Boolean lightOn;
    Boolean lightOff;
    int b = 0;
    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
        if (isLooping)
            colorLoop(!isLooping);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledcontroller);
        cycleButton = findViewById(R.id.cycleButton);
        redSeek = findViewById(R.id.redSeek);
        blueSeek = findViewById(R.id.blueSeek);
        greenSeek = findViewById(R.id.greenSeek);
        colorTempButton = findViewById(R.id.colorTempButton); //to switch to color temperature mode
        colorLoopButton = findViewById(R.id.colorLoopButton);
        onSwitch = findViewById(R.id.onSwitch);
        protocolSwitch = findViewById(R.id.protocolSwitch);
        mqttClient = findViewById(R.id.mqttClient);
        progressTextView = findViewById(R.id.progressTextView);
        progressTextView.setTextColor(getResources().getColor(R.color.StartColor));
        //sets boundary coordinates for the view of the ui light animation, and sets the background to black
        backgroundView = this.getWindow().getDecorView();
        backgroundView.setBackgroundColor(0xffffffff);

        //  final String mqttGatewayIP = loadLastUsedIP(); //only for testing with silicon labs gateway.
        if (!mqttOn) {
            protocolSwitch.setVisibility(View.GONE);
            mqttClient.setVisibility(View.GONE);
            cycleButton.setVisibility(View.GONE);
        }


        //load the ip and user of the gateway along with the resources being accesssed
        bridgeBuilderUrl = getIntent().getStringExtra("url");

        Log.i("debug1", bridgeBuilderUrl);
        if (getIntent().getStringExtra("url") != null) {
            if (bridgeBuilderUrl.contains("lights")) {
                getInitalLightState("state");
                bridgeBuilderUrl = bridgeBuilderUrl.concat("/state");
            }
            if (bridgeBuilderUrl.contains("groups")) {
                getInitalLightState("action");
                bridgeBuilderUrl = bridgeBuilderUrl.concat("/action");
            }
        }

        if (bridgeBuilderUrl.equals("Error")) {
            colorLoopButton.setVisibility(View.GONE);
            onSwitch.setVisibility(View.GONE);
            redSeek.setVisibility(View.GONE);
            greenSeek.setVisibility(View.GONE);
            blueSeek.setVisibility(View.GONE);
            colorTempButton.setVisibility(View.GONE);
            progressTextView.setText("Connection Error, please connect to Wifi and go back to the Main Screen");

        }
        Log.i("BridgeBuilder", bridgeBuilderUrl);

        //Check to see if dev mode is enabled to show MQTT UI elements


        //

        protocolSwitch.setOnClickListener(new ToggleButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (protocolSwitch.isChecked()) {
                    progressTextView.setText("Protocol switched to MQTT");
                    protocolSwitch.setText("MQTT");
                    mqttOn = true;
                }

                if (!protocolSwitch.isChecked()) {
                    progressTextView.setText("Protocol switched to HTTP");
                    protocolSwitch.setText("HTTP");
                    mqttOn = false;

                }
            }
        });

        //Toggles light on and off.

        onSwitch.setOnClickListener(new ToggleButton.OnClickListener() {
            @Override
            public void onClick(View view) {

                BuildJSON buildJSON = new BuildJSON();
                if (onSwitch.isChecked()) {
                    lightOn = true;
                    onSwitch.setText("ON");
                    json = buildJSON.setLightOn().toString();
                } else {
                    onSwitch.setText("OFF");
                    lightOn = false;
                    json = buildJSON.setLightOff().toString();
                    Log.i("json", buildJSON.setLightOff().toString());
                }
                BridgeCall bridgeCall = new BridgeCall();
                bridgeCall.execute(bridgeBuilderUrl, "PUT", json);
            }
        });


        redSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setLightState();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                //  backgroundView.invalidate();
                updateUI(progress, "redValue");
            }
        });
        blueSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setLightState();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                //   backgroundView.invalidate();
                updateUI(progress, "blueValue");


            }
        });
        greenSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setLightState();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                // backgroundView.invalidate();
                updateUI(progress, "greenValue");

            }
        });


        cycleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int h = 5983;
                int s = 192;
                b = 255;
                BuildURL buildURL = new BuildURL(bridgeBuilderUrl);
                String groupURL = buildURL.setGroupState(1);
                Log.i("builtURL: ", groupURL);
                BuildJSON buildJson = new BuildJSON();
                String json = buildJson.setHueSatBriTime(h, s, b, time).toString();
                progressTextView.setText(json);
                BridgeCall bridgeCall = new BridgeCall();
                String response;

                try {
                    response = bridgeCall.execute(groupURL, "PUT", json).get();
                    progressTextView.setText(response);
                } catch (Exception e) {
                    Log.i("Response Error", e.toString());


                    progressTextView.setText("Max reached");
                }
            }
        });


        // Submits info to gateway. Uses RGB to derive HSB and posts a JSON to gateway */

        colorLoopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Begins a colorloop for all lights*/
                {

                    if (!isLooping) {
                        colorLoop(true);
                        isLooping = true;
                        Toast.makeText(LEDController.this,
                                "Color Loop loops through hue but not saturation. The further apart R G and B are the more noticeable the effect will be",
                                Toast.LENGTH_LONG).show();
                        colorLoopButton.setText("Stop");
                    } else {
                        isLooping = false;
                        colorLoop(isLooping);
                        colorLoopButton.setText("Color Loop");
                    }
                }




                /* For Dev with Silicon Labs Gateway only

                if (mqttOn) {
                    hueValue = (int) Math.sqrt(hueValue);
                    String lvl = Integer.toHexString(Math.round(hueSatBright[2]));
                    String hue = Integer.toHexString(Math.round(hueSatBright[0]));
                    String sat = Integer.toHexString(Math.round(hueSatBright[1]));
                    Log.i("Using Connection: ", "MQTT ON");
                    String lvlmsg = "{\"commands\":[{\"command\":\"zcl level-control o-mv-to-level " + "0x" + lvl + " 0\"},{\"command\":\"plugin device-table send {000B57FFFE1731C1} 1\"}]}";
                    byte[] lvlmsgBytes = lvlmsg.getBytes();
                    MqttMessage lvlpayload = new MqttMessage();
                    lvlpayload.setPayload(lvlmsgBytes);
                    try {
                        client.publish("gw/0022A30000170948/commands", lvlpayload);
                        Log.i("Payload: ", lvlmsg);
                    } catch (MqttException e) {
                        Log.i("Mqtt Error", e.toString());
                    }
                    String saturationBrightMsg = "{\"commands\":[{\"command\":\"zcl color-control movetohueandsat " + "0x" + hue + " " + "0x" + sat + " 1\"},{\"command\":\"plugin device-table send {000B57FFFE1731C1} 1\"}]}";
                    byte[] saturationBrightMsgBytes = saturationBrightMsg.getBytes();
                    MqttMessage satBriPayload = new MqttMessage();
                    satBriPayload.setPayload(saturationBrightMsgBytes);
                    try {
                        client.publish("gw/0022A30000170948/commands", satBriPayload);
                        Log.i("Payload: ", saturationBrightMsg);
                        progressTextView.setText(saturationBrightMsg);
                    } catch (MqttException e) {
                        Log.i("Mqtt Error", e.toString());
                    }
                }
                */
            }
        });

        colorTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent colorTemperatureActivity = new Intent(LEDController.this, ColorTemperatureActivity.class);
                colorTemperatureActivity.putExtra("bridgeBuilderURL", bridgeBuilderUrl);
                Log.i("bridgeBuilderURL", bridgeBuilderUrl);
                startActivity(colorTemperatureActivity);


            }
        });


    }



    public void colorLoop(boolean isLooping) {
        BuildJSON buildJSON = new BuildJSON();
        String json = buildJSON.setColorLoop(isLooping).toString();
        BridgeCall bridgeCall = new BridgeCall();
        bridgeCall.execute(bridgeBuilderUrl, "PUT", json);
    }

    public void setLightState() {
        Log.i("builtURL: ", bridgeBuilderUrl);
        BuildJSON buildJson = new BuildJSON();

        String json = buildJson.setHueSatBri(
                Math.round(hueSatBright[0]),
                Math.round(hueSatBright[1]),
                Math.round(hueSatBright[2])).toString();
        progressTextView.setText(json);
        BridgeCall bridgeCall = new BridgeCall();
        try {
            bridgeCall.execute(bridgeBuilderUrl, "PUT", json);
        } catch (Exception e) {
            progressTextView.setText("Error: " +  e.toString());
        }
        // progressTextView.setText(response); keep for debug

    }

    public void getInitalLightState(String resource) {

        Log.i("initialLighturl", bridgeBuilderUrl); //action is not a valid get, set after
        BridgeCall bridgeCall = new BridgeCall();
        String response = "";
        String method = getResources().getString(R.string.GET);
        Log.i("method", method);
        try {
            json = bridgeCall.execute(bridgeBuilderUrl, method).get();
            progressTextView.setText(response);
            progressTextView.setTextColor(getResources().getColor(R.color.Blue));
        } catch (Exception e) {
            Log.i("Async Error", e.toString());
        }

        Log.i("json", json);

        int hue, sat, bri;
        try {
            JSONObject lightObject;
            lightObject = new JSONObject(json);
            Log.i("JSON", lightObject.toString());
            JSONObject state = lightObject.getJSONObject(resource);
            hue = state.getInt("hue");
            sat = state.getInt("sat");
            bri = state.getInt("bri");
            lightOn = state.getBoolean("on");
            onSwitch.setChecked(lightOn);
            if (lightOn) {
                onSwitch.setText("ON");
            } else {
                onSwitch.setText("OFF");
            }

            float[] hsv = new float[3];
            hsv[0] = (float) (hue * 360);
            hsv[0] = hsv[0] / 65535;
            hsv[1] = (float) sat / 254;
            hsv[2] = (float) bri / 254;

            int rgb = Color.HSVToColor(hsv);
          //  Log.i("HSV", Float.toString(hsv[0]) + " Sat: " + Float.toString(hsv[1]) + " Bri: " + Float.toString(hsv[2]));
          //  Log.i("COLOR INT", Integer.toString(rgb));
            int red = Color.red(rgb);
            int blue = Color.blue(rgb);
            int green = Color.green(rgb);
         //   Log.i("red", Integer.toString(red));
          //  Log.i("blue ", Integer.toString(blue));
           // Log.i("green", Integer.toString(green));
            redSeek.setProgress(red);
            blueSeek.setProgress(blue);
            greenSeek.setProgress(green);
            updateUI(red, "redValue");
            updateUI(blue, "blueValue");
            updateUI(green, "greenValue");


        } catch (JSONException e) {
            Log.i("Into JSON array", e.toString());
        }


    }

    /**
     * Loads a file containing IP settings for the MQTTbroker.
     * Settings can be entered from the MainActivity.
     **/


    public String loadLastUsedIP() {
        FileInputStream fis;
        StringBuffer ipSettings = new StringBuffer("");
        Log.i("Location ", "In loadLastUsedIP");
        try {
            fis = new FileInputStream(new File("/data/user/0/sowilo.a5channelled/files/ipSettings.txt"));
            byte[] bytes = new byte[64];
            int n;
            while ((n = fis.read(bytes)) != -1) {
                ipSettings.append(new String(bytes, 0, n));
                Log.i("Reconstructed IP: ", ipSettings.toString());
            }
            fis.close();
        } catch (FileNotFoundException e) {
            progressTextView.setText("Ip Settings Not Found, Please Re-Enter IP and Port");
            Log.i("Error ", "Ip Settings Not Found, Please Re-Enter IP and Port ");
        } catch (IOException e) {
            progressTextView.setText("IOException: ".concat(e.toString()));
            Log.i("Error ", e.toString());
        } catch (SecurityException e) {
            progressTextView.setText("Security Exception: ".concat(e.toString()));
            Log.i("Error ", e.toString());
        }

        return ipSettings.toString();
    }

    /**
     * This method adjusts the light animation properties corresponding to each slider
     * Will take the changed value and update the UI gradient drawable used to animate the light
     */

    private void updateUI(int progress, String changedValue) {
        switch (changedValue) {
            case "redValue":
                redValue = progress;
                break;

            case "blueValue":
                blueValue = progress;
                break;

            case "greenValue":
                greenValue = progress;
                break;

        }

        String colorInfo = "Red: " + redValue + " Green: " + greenValue + " Blue: " + blueValue;
        progressTextView.setText(colorInfo);

        int[] color = {redValue, greenValue, blueValue}; //array for RGB
        //  color = returnRGB(redValue, greenValue, blueValue); //retrieves RGB values from values scaled over 4096
        progressTextView.setTextColor(colorInt);
        hueSatBright = RGBtoHSB(color[0], color[1], color[2]);
        hueValue = Math.round(hueSatBright[0]);
        colorInt = Color.rgb(color[0], color[1], color[2]);
        // String hexColor = String.format( "%02x%02x%02x%02x",255, color[0], color[1], color[2]).toUpperCase(); Useful Keep
        // int decimalColor = (int) Long.parseLong(hexColor, 16); Useful keep
        int[] gradientColor = new int[9];
        // Sets the parameters for the Gradient, could spend forever tweaking these */
        gradientColor[0] = START_COLOR;
        gradientColor[1] = colorInt;
        gradientColor[2] = colorInt;
        gradientColor[3] = colorInt;
        gradientColor[4] = START_COLOR;
        gradientColor[5] = colorInt;
        gradientColor[6] = END_COLOR;
        gradientColor[7] = END_COLOR;
        gradientColor[8] = END_COLOR;
        GradientDrawable background;
        background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, color);
        background.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        // adjusts the radius of the animation according to how bright it is
        background.setGradientRadius(returnGradientRadius(Math.round(hueSatBright[2])));
        background.setGradientCenter(0f, 0f);
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColors(gradientColor);
        backgroundView.setBackground(background);
    }


    /*
     * determines the radius of the light simulation
     */
    private int returnGradientRadius(int brightValue) {
        int radius = 700;
        int increment = brightValue * 6;
        return (radius + increment);


    }

    /*
     * Conversion from the RGB color space to HSB. Cube to Cylinder
     */

    private float[] RGBtoHSB(int r, int g, int b) {
        float hue, saturation, brightness;
        float[] hsbvals = new float[3];

        // finds the highest value of the R G B channels and sets it to brightness*/
        int cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        int cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;
        brightness = ((float) cmax) / 255.0f;

        // Determines saturation based on how far apart RGB are in relation to each other

        if (cmax != 0)
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));

            // Hue is derived from degrees on a circle, determine which part of the circle we are in */
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        /* return values for Hue ranging from 0 to 65535 and for sat and brightness 0-254
        for integration with philips api.
         */
        hsbvals[0] = hue * 65535;
        hsbvals[1] = saturation * 254;
        hsbvals[2] = brightness * 254;
        return hsbvals;
    }


    private String getBridge() {
        if (getIntent().getStringExtra("ipAddress") != null && getIntent().getStringExtra("userName") != null) {
            String ipAddress = getIntent().getStringExtra("ipAddress");
            String userName = getIntent().getStringExtra("userName");
            String url = "http://".concat(ipAddress).concat("/api/").concat(userName).concat("/");
            Log.i("Url", url);

            return url;
        } else {
            return "Error";
        }
    }

    /**
     * This is only for Development Mode, and allows for automation of data collection with oscilloscope.
     * You can set an interval for calls in Thread.Sleep(someValue x) This is not synchronized
     * but you can quickly get a set of values for a particular channel by setting the sleep
     * to 5000ms, sending a new change command, and then using the script for the oscilloscope
     * to automate data capture every someValue x and executing this script after (someValue x)/2 time
     * has passed. Keep in mind that bridge calls typically take~50ms to complete and the bridge
     * throttles commands to about 100ms so commands will desync after awhile.
     * Not perfect but way faster than manually collecting the data.
     * <p>
     * In the future, it will probably best to have this script running on the machine with scopeX
     * and syncronize these commands for a much faster data capture.
     */

    private static class HTTPColorCycle extends AsyncTask<Integer, Integer, String> {
        String statusmsg;
        URL url;
        HttpURLConnection urlConnection;
        InputStream in;

        private final String LIGHT_STATE_URL1 = "http://192.168.1.67/api/AqDIZRbjpk4esKWRpRHwbQhqbufBVVY8pdf-zjxj/groups/1";


        @Override
        protected String doInBackground(Integer... integers) {


            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                Log.i("Tag", e.toString());
            }

            JSONObject setColor = new JSONObject();
            try {
                setColor.put("hue", integers[0]);
                setColor.put("sat", integers[1]);
                setColor.put("bri", integers[2]);
                setColor.put("transitiontime", 0);
            } catch (JSONException e) {
                Log.i("JSON Error", e.toString());
            }
            networkCall(setColor);


            return setColor.toString();
        }

        private void networkCall(JSONObject jsonObject) {
            try {
                url = new URL(LIGHT_STATE_URL1);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("PUT");
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                Log.i("JSON in HTTPColorCyle", jsonObject.toString());
                //Log.i("URL in Async", LIGHT_STATE_URL4);
                writer.write(jsonObject.toString());
                writer.close();
                outputStream.close();
                in = new BufferedInputStream(urlConnection.getInputStream());
                String encoding = urlConnection.getContentEncoding();
                String response = IOUtils.toString(in, encoding);
                Log.i("Http Response", response);
            } catch (UnsupportedEncodingException e) {
                Log.i("EncodingNotSupported", e.toString());
                statusmsg = "Encoding Not Supported-OutputStreamwriter UTF-8 " + e.toString();
            } catch (MalformedURLException e) {
                Log.i("MalformedURLException", e.toString());
                statusmsg = "MalformedURLException in URL " + e.toString();

            } catch (IllegalStateException e) {
                Log.i("IllegalStateException", e.toString());
                statusmsg = "IllegalStateException in urlConnection Object " + e.toString();

            } catch (NullPointerException e) {
                Log.i("NullPointerException", e.toString());
                statusmsg = "NullPointerException in urlConnection Object " + e.toString();

            } catch (ProtocolException e) {
                Log.i("ProtcolException", e.toString());
                statusmsg = "ProtocolException Protocol not supported " + e.toString();
            } catch (IOException e) {
                Log.i("IOException", e.toString());
                statusmsg = "IOException in write function " + e.toString();

            } finally {
                urlConnection.disconnect();
            }


        }

    }
}