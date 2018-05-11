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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * /**
 * This fragment displays a list of Lights configured on the bridge. The first item in the list allows you
 * to begin a search for new Lights and all items below it contain a custom view with the name of the group,
 * the lights contained in the group, a switch to toggle on off and a color button to configure color changes
 * <p>
 * {@link LightsContent} holds the data model which is refreshed when the fragment is refreshed
 * {@link LightsRecyclerViewAdapter} binds the data and returns the inflated view
 * Activities containing this fragment MUST implement the {@link OnListLightsFragmentInteractionListener}
 * interface.
 */
public class LightsFragment extends Fragment implements BridgeCall.onBridgeResponseListener {


    private String debugTag = "LightsFragment";
    private static final String paramTag = "userUrl";
    private OnListLightsFragmentInteractionListener listener;
    LightsContent lightsContent = new LightsContent();
    LayoutInflater layoutInflater;
    ViewGroup viewGroup;
    Bundle currentInstanceState;


    public LightsFragment() {
    }

    public static LightsFragment newInstance(String userURL) {
        LightsFragment fragment = new LightsFragment();
        Bundle args = new Bundle();
        args.putString(paramTag, userURL);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*retrieves the url passed from SplashScreen.
          Constructs a BridgeCall to retrieve connected lights
          Parses the JSON from the HTTP Request and adds them to LightsContent
          which will be used in RecyclerViewAdapter to update the view. Checks for connectivity first*/
        CheckConnectivity checkConnectivity = new CheckConnectivity(getActivity());
        if (checkConnectivity.checkWifiOnAndConnected()) {
            if (getArguments() != null) {
                String userURL = getArguments().getString(paramTag);
                BridgeCall bridgeCall = new BridgeCall(this);
                BuildURL buildURL = new BuildURL(userURL);
                userURL = buildURL.getLights();
                //TODO: Implement interfaces so bridgeCall.execute is not blocking UI thread and animation is smoother
                try {
                    String json = bridgeCall.execute(userURL, "GET").get();
                    JSONObject jsonReader = new JSONObject(json);
                    Boolean hasMoreObjects = true;
                    int i = 1;
                    while (hasMoreObjects) {
                        if (jsonReader.has(Integer.toString(i))) {
                            lightsContent.createItem(jsonReader.getJSONObject(Integer.toString(i)), i);
                            Log.d(debugTag, LightsContent.items.get(i).name);
                            Log.d("State", LightsContent.items.get(i).state.toString());
                            i++;
                        } else {
                            hasMoreObjects = false;
                        }
                    }
                } catch (JSONException e) {
                    Log.d(debugTag, e.toString());
                } catch (InterruptedException e) {
                    Log.d(debugTag, e.toString());
                } catch (ExecutionException e) {
                    Log.d(debugTag, e.toString());
                }

            }
        } else {
            Toast.makeText(getActivity(), "No Wifi Connection or poor signal. Please Connect to Wifi and go back to Home", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle
                                     savedInstanceState) {
        View view = inflater.inflate(R.layout.lights_fragment_item_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new LightsRecyclerViewAdapter(LightsContent.items, listener));
        }
        layoutInflater = inflater;
        viewGroup = container;
        currentInstanceState = savedInstanceState;
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListLightsFragmentInteractionListener) {
            listener = (OnListLightsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                                               + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {

        super.onDetach();
        listener = null;
    }

    @Override
    public void onBridgeResponse(String json) {
/*
        try {

            JSONObject jsonReader = new JSONObject(json);
            Boolean hasMoreObjects = true;
            int i = 1;
            while (hasMoreObjects) {
                if (jsonReader.has(Integer.toString(i))) {
                    lightsContent.createItem(jsonReader.getJSONObject(Integer.toString(i)), i);
                    Log.d(debugTag, LightsContent.items.get(i).name);
                    Log.d("State", LightsContent.items.get(i).state.toString());
                    i++;
                } else {
                    hasMoreObjects = false;
                }
            }
        } catch (JSONException e) {
            Log.d(debugTag, e.toString());
        }

*/
    }


    public interface OnListLightsFragmentInteractionListener {
        //for light interaction
        void onColorButtonPressed(LightsContent.LightItem item);

        //for on Switch interaction
        void onSwitchFlipped(String on, boolean isOn, int id);

        // for new light search
        void onSearchPressed(int id);
    }
}
