package brett.lednavigation;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupsContent {

    final String debugTag = "GroupContent";
    static List<GroupItem> items = new ArrayList<>();

    public void createItem(JSONObject groupObject, int i) {
        GroupItem temp = new GroupItem(groupObject, i);
        items.add(temp);
    }

    public class GroupItem {

        int id;
        String name;
        JSONObject state;
        boolean reachable;
        int bri;
        int sat;
        int hue;
        JSONArray lights;
        boolean on;

        private GroupItem(JSONObject groupObject, int i) {
            try {
                id = i;
                name = groupObject.getString("name");
                state = groupObject.getJSONObject("state");
                on = state.getBoolean("any_on");
                lights = groupObject.getJSONArray("lights");

               // state = groupObject.getJSONObject("state");
            } catch (JSONException e) {
                Log.i(debugTag, e.toString());
            }
            Log.i(debugTag, Integer.toString(i) + name);


        }


    }
}
