package brett.lednavigation;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by brett on 3/24/2018.
 */

public class BuildJSON {


    public JSONObject setLightOn() {
        JSONObject json = new JSONObject();
        try {
            json.put("on", true);

        } catch (JSONException e) {
            Log.i("JSON Exception", e.toString());
        }
        return json;
    }

    public JSONObject setLightOff( ) {
        JSONObject json = new JSONObject();
        try {
            json.put("on", false);

        } catch (JSONException e) {

        }
        return json;
    }


    public JSONObject setColorTemperatureAndBrightness(int ct, int bri) {
        JSONObject json = new JSONObject();
        try {
            json.put("bri", bri);
            json.put("ct", ct);
        } catch (JSONException e) {
            Log.i("JSONException", e.toString());
        }
        return json;

    }

    public JSONObject setBrightness(int bri) {
        JSONObject json = new JSONObject();
        try {
            json.put("bri", bri);

        } catch (JSONException e) {
            Log.i("JSONException", e.toString());
        }
        return json;
    }

    public JSONObject setHueSatBriTime(int hue, int sat, int bri, int time) {
        JSONObject json = new JSONObject();
        try {
            json.put("hue", hue);
            json.put("sat", sat);
            json.put("bri", bri);
            json.put("transitiontime", time);
        } catch (JSONException e) {
            Log.i("JSONException", e.toString());
        }
        return json;
    }

    public JSONObject setColorLoop(boolean isLooping){
        JSONObject json = new JSONObject();
        try{
            if (isLooping) {
                json.put("effect", "colorloop");
            }
            if(!isLooping){
                json.put("effect", "none");
            }
        } catch (JSONException e){
            Log.i("JSONException", e.toString());
        }
        return json;
    }

    public JSONObject setHue(int hue) {
        JSONObject json = new JSONObject();
        try {
            json.put("hue", hue);
        } catch (JSONException e) {
            Log.i("JSONException", e.toString());
        }
        return json;
    }
}


