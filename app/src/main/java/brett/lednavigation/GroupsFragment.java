package brett.lednavigation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This fragment displays a list of Groups configured on the bridge. The first item in the list allows you
 * to configure a new Group and all items below it contain a custom view with the name of the group,
 * the lights contained in the group, a switch to toggle on off and a color button to configure color changes
 * <p>
 * {@link GroupsContent} holds the data model which is refreshed when the fragment is refreshed
 * {@link GroupsRecyclerViewAdapter} binds the data and returns the inflated view
 * <p>
 * Activities containing this fragment MUST implement the {@link OnListGroupsFragmentInteractionListener}
 * interface.
 */
public class GroupsFragment extends Fragment {
    private final String debugTag = "GroupsFragment";
    private static final String paramTag = "userUrl";
    private String response = "";
    private OnListGroupsFragmentInteractionListener listener;
    private GroupsContent groupsContent = new GroupsContent();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupsFragment() {
    }


    public static GroupsFragment newInstance(String userUrl) {
        GroupsFragment fragment = new GroupsFragment();
        Bundle args = new Bundle();
        args.putString(paramTag, userUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckConnectivity checkConnectivity = new CheckConnectivity(getActivity());

        if (checkConnectivity.checkWifiOnAndConnected()) {
            if (getArguments() != null) {
                String userURL = getArguments().getString(paramTag);
                BridgeCall bridgeCall = new BridgeCall();
                BuildURL buildURL = new BuildURL(userURL);
                userURL = buildURL.getAllGroups();
                try {
                    //TODO: recode this properly using an interface so its not blocking the ui thread.
                    response = bridgeCall.execute(userURL, "GET").get();
                    Log.d(debugTag, response);
                } catch (Exception e) {
                    Log.d(debugTag, e.toString());
                }
                try {
                    JSONObject jsonReader = new JSONObject(response);
                    Log.d(debugTag, jsonReader.toString());
                    Boolean hasMoreObjects = true;
                    int i = 1;
                    while (hasMoreObjects) {
                        if (jsonReader.has(Integer.toString(i))) {
                            String parsedJSON = jsonReader.getJSONObject(Integer.toString(i)).toString();
                            Log.d(debugTag, parsedJSON);
                            groupsContent.createItem(jsonReader.getJSONObject(Integer.toString(i)), i);
                            Log.d(debugTag, Integer.toString(GroupsContent.items.get(i).id));
                            Log.d(debugTag, GroupsContent.items.get(i).name);
                            i++;
                        } else {
                            hasMoreObjects = false;
                        }
                    }
                } catch (JSONException e) {
                    Log.d(debugTag, e.toString());
                }
            }

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groups_fragment_item_list, container, false);
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new GroupsRecyclerViewAdapter(GroupsContent.items, listener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListGroupsFragmentInteractionListener) {
            listener = (OnListGroupsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                                               + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        GroupsContent.items.clear();
        listener = null;
    }

    /**
     *
     */
    public interface OnListGroupsFragmentInteractionListener {
        void onGroupsColorButtonPressed(GroupsContent.GroupItem item); //to pass the whole GroupItem

        void onGroupsSwitchFlipped(String anyOn, boolean isOn, int id);//to pass if any lights are on in the group

        void onCreateNewGroup(int id); //to pass the id of the item clicked

    }
}
