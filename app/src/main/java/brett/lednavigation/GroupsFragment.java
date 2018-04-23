package brett.lednavigation;

import android.content.Context;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class GroupsFragment extends Fragment {
    private static final String debugTag = "GroupsFragment";
    private static final String paramTag = "userUrl";
    private String userURL ="";
    private int mColumnCount = 1;
    private String response="";
    private OnListFragmentInteractionListener mListener;
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

        if(checkConnectivity.checkWifiOnAndConnected()) {
            if (getArguments() != null) {
                userURL = getArguments().getString(paramTag);
                BridgeCall bridgeCall = new BridgeCall();
                BuildURL buildURL = new BuildURL(userURL);
                userURL = buildURL.getAllGroups();
                try {
                    //TODO: recode this properly using an interface so its not blocking the ui thread.
                    response = bridgeCall.execute(userURL, "GET").get();
                    Log.i(debugTag, response);
                } catch (Exception e) {
                    Log.i(debugTag, e.toString());
                }
                try {
                    JSONObject jsonReader = new JSONObject(response);
                    Log.i(debugTag, jsonReader.toString());
                    Boolean hasMoreObjects = true;
                    int i = 1;
                    while (hasMoreObjects) {
                        if (jsonReader.has(Integer.toString(i))) {
                            String parsedJSON = jsonReader.getJSONObject(Integer.toString(i)).toString();
                            Log.i(debugTag, parsedJSON);
                            groupsContent.createItem(jsonReader.getJSONObject(Integer.toString(i)), i);
                            Log.i(debugTag, Integer.toString(GroupsContent.items.get(i - 1).id));
                            Log.i(debugTag, GroupsContent.items.get(i - 1).name);
                            i++;
                        } else {
                            hasMoreObjects = false;
                        }
                    }
                } catch (JSONException e) {
                    Log.i(debugTag, e.toString());
                }
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groups_fragment_item_list, container, false);
       // MainActivity.setActionBarTitle("Your title");
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new GroupsRecyclerViewAdapter(GroupsContent.items, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                                               + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        GroupsContent.items.clear();
        mListener = null;
    }

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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(GroupsContent.GroupItem item);
        void onListFragmentInteraction(String anyOn, boolean isOn, int id);

    }
}
