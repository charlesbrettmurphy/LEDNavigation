package brett.lednavigation;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

// https://youtu.be/BUwMVIaN3Fc?t=664 good screenshot of recyclerView

public class LightsContent {

    private final String debugTag = "LightContent";
     static List<LightItem> items = new ArrayList<>();

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
                Log.i(debugTag, e.toString());
            }
            Log.i(debugTag, Integer.toString(i) + name);


        }


    }
}