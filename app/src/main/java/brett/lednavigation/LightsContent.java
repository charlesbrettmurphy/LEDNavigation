package brett.lednavigation;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the data for a LightItem object and provides a list of all LightItems connected
 * to the bridge.
 * {@link LightsRecyclerViewAdapter} inflates this list in a view
 * {@link LightsFragment} refreshes data stored in this model.
 */

public class LightsContent {


    static List<LightItem> items = new ArrayList<>();

    LightsContent() {
        LightItem temp = new LightItem();
        items.add(temp);
    }

    public void createItem(JSONObject lightObject, int i) {
        LightItem temp = new LightItem(lightObject, i);
        items.add(temp);
    }

    public class LightItem {

        int id;
        String name;
        JSONObject state;
        boolean reachable;
        int bri;
        int sat;
        int hue;
        boolean on;


        private LightItem(JSONObject lightObject, int i) {
            final String debugTag = "LightContent";
            try {
                id = i;
                name = lightObject.getString("name");
                state = lightObject.getJSONObject("state");
                bri = state.getInt("bri");
                reachable = state.getBoolean("reachable");
                sat = state.getInt("sat");
                hue = state.getInt("hue");
                on = state.getBoolean("on");
            } catch (JSONException e) {
                Log.d(debugTag, e.toString());
            }
            Log.d(debugTag, Integer.toString(i) + name);


        }

        //first item in the list is always the option to search
        private LightItem() {
            name = "Search For New Lights";
            id = 0;
        }


    }
}
