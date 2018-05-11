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

import brett.lednavigation.GroupsContent.GroupItem;
import brett.lednavigation.GroupsFragment.OnListGroupsFragmentInteractionListener;

/**
 * This class binds data from {@link GroupsContent} list to the viewHolder and passes
 * relevant info back to {@link MainActivity}
 */
public class GroupsRecyclerViewAdapter extends RecyclerView.Adapter<GroupsRecyclerViewAdapter.ViewHolder> {

    private final List<GroupsContent.GroupItem> mValues;
    private final OnListGroupsFragmentInteractionListener mListener;

    public GroupsRecyclerViewAdapter(List<GroupsContent.GroupItem> items, OnListGroupsFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.groups_fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {

        if (position == 0) {
            viewHolder.idView.setVisibility(View.GONE);
            viewHolder.nameView.setText(mValues.get(position).name);
            viewHolder.onSwitch.setVisibility(View.GONE);
            viewHolder.colorButton.setVisibility(View.GONE);
            viewHolder.lights.setText("Tap to add a new Group");


        } else {
            viewHolder.mItem = mValues.get(position);
            viewHolder.nameView.setText(mValues.get(position).name);
            viewHolder.idView.setText(Integer.toString(mValues.get(position).id));
            viewHolder.lights.setText(mValues.get(position).lights.toString());
            viewHolder.onSwitch.setChecked(mValues.get(position).on);
            if (viewHolder.onSwitch.isChecked()) {
                viewHolder.onSwitch.setText("On");

            } else {
                viewHolder.onSwitch.setText("Off");
            }
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
                    if (mListener != null) {
                        mListener.onGroupsSwitchFlipped("any_on", isOn, viewHolder.mItem.id);
                    }

                }
            });
        }


        viewHolder.colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) the color button has been pressed
                    mListener.onGroupsColorButtonPressed(viewHolder.mItem);
                }
            }
        });
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onCreateNewGroup(position);
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
        private final TextView nameView;
        private final TextView idView;
        private final TextView lights;
        private final Switch onSwitch;
        private final Button colorButton;
        private GroupItem mItem;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            nameView = view.findViewById(R.id.nameTextView);
            idView = view.findViewById(R.id.idTextView);
            lights = view.findViewById(R.id.lightsTextView);
            onSwitch = view.findViewById(R.id.onSwitch);
            colorButton = view.findViewById(R.id.colorButton);


        }

        @Override
        public String toString() {
            return super.toString() + " '" + nameView.getText() + "'";
        }
    }
}
