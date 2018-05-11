package brett.lednavigation;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the data for a GroupItem object and provides a list of all GroupItems
 * {@link GroupsRecyclerViewAdapter} inflates this list in a view
 * {@link GroupsFragment} refreshes data stored in this model.
 */
public class GroupsContent {


    static List<GroupItem> items = new ArrayList<>();
    public GroupsContent(){
        GroupItem temp = new GroupItem();
        items.add(temp);
    }

    public void createItem(JSONObject groupObject, int i) {
        GroupItem temp = new GroupItem(groupObject, i);
        items.add(temp);
    }

    public class GroupItem {
        private final String debugTag = "GroupContent";
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
            } catch (JSONException e) {
                Log.i(debugTag, e.toString());
            }

        }
        private GroupItem(){
            id =0;
            name = "Create a New Group";
        }


    }
}

