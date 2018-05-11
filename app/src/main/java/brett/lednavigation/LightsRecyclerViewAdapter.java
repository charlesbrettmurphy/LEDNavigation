package brett.lednavigation;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import brett.lednavigation.LightsFragment.OnListLightsFragmentInteractionListener;

/**
 * This class binds data from {@link LightsContent.LightItem} list to the viewHolder and passes
 * relevant info back to {@link MainActivity} via the appropriate method in the interface
 * {@link OnListLightsFragmentInteractionListener}.
 */
public class LightsRecyclerViewAdapter extends RecyclerView.Adapter<LightsRecyclerViewAdapter.ViewHolder> {

    private final List<LightsContent.LightItem> mValues;
    private final OnListLightsFragmentInteractionListener mListener;

    public LightsRecyclerViewAdapter(List<LightsContent.LightItem> items, LightsFragment.OnListLightsFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.lights_fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        //pull values from LightsItem and populate relevant data
        if (position == 0) {
            viewHolder.mNameView.setText(mValues.get(position).name);
            viewHolder.onSwitch.setVisibility(View.GONE);
            viewHolder.colorButton.setVisibility(View.GONE);
            viewHolder.hueSatBriTextView.setText("Tap to begin a search");

        } else {

            viewHolder.mId = Integer.toString(mValues.get(position).id);
            viewHolder.mItem = mValues.get(position);
            viewHolder.mNameView.setText(mValues.get(position).name);
            viewHolder.onSwitch.setChecked(mValues.get(position).on);
            viewHolder.reachable = mValues.get(position).reachable;
            viewHolder.hueSatBriTextView.setText("Hue: ".concat(Integer.toString(mValues.get(position).hue))
                                                         .concat(" Sat: ")
                                                         .concat(Integer.toString(mValues.get(position).sat))
                                                         .concat(" Bri: ")
                                                         .concat(Integer.toString(mValues.get(position).bri)));
            //check UI states and display available actions.
            if (viewHolder.onSwitch.isChecked()) {
                viewHolder.onSwitch.setText("On");

            } else {
                viewHolder.onSwitch.setText("Off");
            }

            if (!viewHolder.reachable) {
                viewHolder.hueSatBriTextView.setText("Unreachable");
                viewHolder.onSwitch.setVisibility(View.INVISIBLE);
                viewHolder.colorButton.setVisibility(View.INVISIBLE);
            }
        }

        viewHolder.colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onColorButtonPressed(viewHolder.mItem);

            }
        });

        viewHolder.onSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isOn;
                if (viewHolder.onSwitch.isChecked()) {
                    viewHolder.onSwitch.setText("On");
                    isOn = true;
                } else {
                    viewHolder.onSwitch.setText("Off");
                    isOn = false;
                }
                if (null != mListener) {
                    mListener.onSwitchFlipped("on", isOn, viewHolder.mItem.id);
                }
            }
        });


        viewHolder.mNameView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (null != mListener) {


                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onSearchPressed(viewHolder.getAdapterPosition());
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mNameView;
        private final Switch onSwitch;
        private final TextView hueSatBriTextView;
        private String mId;
        private final Button colorButton;
        private LightsContent.LightItem mItem;
        private boolean reachable;
        public boolean searchNew;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.nameTextView);
            onSwitch = view.findViewById(R.id.onSwitch);
            hueSatBriTextView = view.findViewById(R.id.hueSatBriTextView);
            colorButton = view.findViewById(R.id.colorButton);


        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
