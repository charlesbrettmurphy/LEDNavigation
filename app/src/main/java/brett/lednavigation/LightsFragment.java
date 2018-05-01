package brett.lednavigation;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
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
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListLightsFragmentInteractionListener}
 * interface.
 */
public class LightsFragment extends Fragment {


    private String debugTag = "LightsFragment";
    private static final String paramTag = "userUrl";
    private String userURL = "";
    int mColumnCount;
    private OnListLightsFragmentInteractionListener mListener;

    String response = "";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
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
        LightsContent lightsContent = new LightsContent();
        /*retrieves the url passed from SplashScreen.
          Constructs a BridgeCall to retrieve connected lights
          Parses the JSON from the HTTP Request and adds them to LightsContent
          which will be used in RecyclerViewAdapter to update the view. Checks for connectivity first*/
        CheckConnectivity checkConnectivity = new CheckConnectivity(getActivity());
        if (checkConnectivity.checkWifiOnAndConnected()) {
            if (getArguments() != null) {
                userURL = getArguments().getString(paramTag);
                BridgeCall bridgeCall = new BridgeCall();
                BuildURL buildURL = new BuildURL(userURL);
                userURL = buildURL.getLights();
                try {
                    //TODO: recode this is so it is using an interface and not blocking ui thread.
                    response = bridgeCall.execute(userURL, "GET").get();
                } catch (Exception e) {
                    Log.i(debugTag, e.toString());
                }


                try {
                    JSONObject jsonReader = new JSONObject(response);
                    Boolean hasMoreObjects = true;
                    int i = 1;
                    while (hasMoreObjects) {
                        if (jsonReader.has(Integer.toString(i))) {
                            lightsContent.createItem(jsonReader.getJSONObject(Integer.toString(i)), i);
                            Log.i(debugTag, LightsContent.items.get(i).name);
                            Log.i("State", LightsContent.items.get(i).state.toString());
                            i++;
                        } else {
                            hasMoreObjects = false;
                        }
                    }
                } catch (JSONException e) {
                    Log.i(debugTag, e.toString());
                }

            }
        } else{
            Toast.makeText(getActivity(), "No Wifi Connection or poor signal. Please Connect to Wifi and go back to Home", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lights_fragment_item_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new LightsRecyclerViewAdapter(LightsContent.items, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListLightsFragmentInteractionListener) {
            mListener = (OnListLightsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                                               + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {

        super.onDetach();
        mListener = null;
    }

   // @Override
    //public void onBridgeResponse(String response) {

    //}

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListLightsFragmentInteractionListener {

        void onListLightsFragmentInteraction(LightsContent.LightItem item); //for light interaction
        void onListLightsFragmentInteraction(String on, boolean isOn, int id); //for on Switch interaction
        void onListLightsFragmentInteraction(int id); // for new light search
    }
}
