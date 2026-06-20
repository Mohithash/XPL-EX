package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.api.properties.MockPropPacket;
import eu.faircode.xlua.api.properties.MockPropSetting;

import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.ui.interfaces.IPropertyUpdate;
import eu.faircode.xlua.ui.transactions.PropTransactionResult;
import eu.faircode.xlua.ui.PropertyQue;
import eu.faircode.xlua.ui.dialogs.PropertyDeleteDialog;
import eu.faircode.xlua.utilities.StringUtil;

public class AdapterProperty  extends RecyclerView.Adapter<AdapterProperty.ViewHolder> implements Filterable {
    private final List<MockPropSetting> properties = new ArrayList<>();
    private List<MockPropSetting> filtered = new ArrayList<>();

    private boolean dataChanged = false;
    private CharSequence query = null;

    private ILoader fragmentLoader;
    private PropertyQue propertiesQue;

    public class ViewHolder extends RecyclerView.ViewHolder
            implements
            CompoundButton.OnCheckedChangeListener,
            View.OnLongClickListener,
            View.OnClickListener,
            IPropertyUpdate {

        final View itemView;
        final TextView tvPropName;
        final CheckBox cbHide, cbSkip, cbForce;
        final ImageView ivDelete;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvPropName = itemView.findViewById(R.id.tvPropPropertyName);
            cbHide = itemView.findViewById(R.id.cbPropHide);
            cbSkip = itemView.findViewById(R.id.cbPropSkip);
            cbForce = itemView.findViewById(R.id.cbPropForce);
            ivDelete = itemView.findViewById(R.id.ivBtPropSettingDelete);
        }

        private void unWire() {
            cbHide.setOnCheckedChangeListener(null);
            cbHide.setOnLongClickListener(null);
            cbSkip.setOnCheckedChangeListener(null);
            cbSkip.setOnLongClickListener(null);
            cbForce.setOnCheckedChangeListener(null);
            cbForce.setOnLongClickListener(null);
            ivDelete.setOnClickListener(null);
            ivDelete.setOnLongClickListener(null);
        }

        private void wire() {
            cbHide.setOnCheckedChangeListener(this);
            cbHide.setOnLongClickListener(this);
            cbSkip.setOnCheckedChangeListener(this);
            cbSkip.setOnLongClickListener(this);
            cbForce.setOnCheckedChangeListener(this);
            cbForce.setOnLongClickListener(this);
            ivDelete.setOnClickListener(this);
            ivDelete.setOnLongClickListener(this);
        }

        @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
        @Override
        public void onCheckedChanged(final CompoundButton cButton, final boolean isChecked) {
            int code = cButton.getId();
            XLog.i("onCheckedChanged: code=" + code + " isChecked=" + isChecked);
            try {
                int adapterPosition = getAdapterPosition();
                MockPropSetting setting = filtered.get(adapterPosition);
                int valueNeeded = MockPropPacket.PROP_NULL;
                if(isChecked) {
                    if (code == R.id.cbPropSkip) valueNeeded = MockPropPacket.PROP_SKIP;
                    else if (code == R.id.cbPropHide) valueNeeded = MockPropPacket.PROP_HIDE;
                    else if (code == R.id.cbPropForce) valueNeeded = MockPropPacket.PROP_FORCE;
                }

                propertiesQue.sendPropertySetting(cButton.getContext(), setting, adapterPosition, valueNeeded, false, this);
            }catch (Exception e) { XLog.e("onCheckedChanged Failed: code=" + code + " isChecked" + isChecked, e, true); }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onLongClick(View v) {
            int code = v.getId();
            XLog.i("onLongClick: code=" + code);
            try {
                if (code == R.id.cbPropSkip) {
                    Snackbar.make(v, R.string.check_prop_skip_hint, Snackbar.LENGTH_LONG).show();
                } else if (code == R.id.cbPropHide) {
                    Snackbar.make(v, R.string.check_prop_hide_hint, Snackbar.LENGTH_LONG).show();
                } else if (code == R.id.cbPropForce) {
                    Snackbar.make(v, R.string.check_prop_force_hint, Snackbar.LENGTH_LONG).show();
                } else if (code == R.id.ivBtPropSettingDelete) {
                    Snackbar.make(v, R.string.menu_property_setting_delete_hint, Snackbar.LENGTH_LONG).show();
                }
            }catch (Exception e) { XLog.e("onLongClick Failed: code=" + code, e, true); }
            return true;
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            int code = v.getId();
            XLog.i("onClick: code=" + code);
            try {
                int position = getAdapterPosition();
                MockPropSetting setting = filtered.get(position);
                if (code == R.id.ivBtPropSettingDelete) {
                    new PropertyDeleteDialog()
                            .addAdapterPosition(position)
                            .addSetting(setting)
                            .addCallback(this)
                            .addPropertyQue(propertiesQue)
                            .show(fragmentLoader.getManager(), v.getContext().getString(R.string.title_delete_property));
                }
            }catch (Exception e) { XLog.e("Failed to Invoke onClick: code=" + code, e, true);  }
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onPropertyUpdate(PropTransactionResult result) {
            try {
                Toast.makeText(result.context, result.result.getResultMessage(), Toast.LENGTH_SHORT).show();
                MockPropSetting propSetting = result.getSetting();
                if(result.hasAnySucceeded()) {
                    if(result.code == MockPropPacket.CODE_DELETE_PROP_MAP_AND_SETTING) {
                        fragmentLoader.loadData();
                    }else {
                        int adapterPosition = result.getAdapterPosition();
                        propSetting.setValue(result.getPacket().getValue());//Update it, ensure this works
                        if(adapterPosition > -1) notifyItemChanged(adapterPosition);
                        else notifyDataSetChanged();
                    }
                }
            }catch (Exception e) { XLog.e("Failed to Post Property Results!", e, true); }
        }
    }

    AdapterProperty() { setHasStableIds(true); }
    AdapterProperty(ILoader loader, PropertyQue propQue) { this(); this.fragmentLoader = loader; this.propertiesQue = propQue; }

    void set(List<MockPropSetting> properties) {
        this.dataChanged = true;
        this.properties.clear();
        this.properties.addAll(properties);
        XLog.i("Properties Settings Count=" + properties.size());
        getFilter().filter(query);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            private boolean expanded1 = false;

            @Override
            protected FilterResults performFiltering(CharSequence query) {
                AdapterProperty.this.query = query;
                List<MockPropSetting> visible = new ArrayList<>(properties);
                List<MockPropSetting> results = new ArrayList<>();
                if (!StringUtil.isValidAndNotWhitespaces(query)) results.addAll(visible);
                else {
                    String q = query.toString().toLowerCase().trim();
                    for(MockPropSetting prop : visible) {
                        if(prop.getName().toLowerCase().contains(q))
                            results.add(prop);
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = results;
                filterResults.count = results.size();
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence query, FilterResults result) {
                final List<MockPropSetting> props = (result.values == null ? new ArrayList<MockPropSetting>() : (List<MockPropSetting>) result.values);
                if(dataChanged) {
                    dataChanged = false;
                    filtered = props;
                    notifyDataSetChanged();
                }else {
                    DiffUtil.DiffResult diff =
                            DiffUtil.calculateDiff(new AppDiffCallback(expanded1, filtered, props));
                    filtered = props;
                    diff.dispatchUpdatesTo(AdapterProperty.this);
                }
            }
        };
    }

    private static class AppDiffCallback extends DiffUtil.Callback {
        private final boolean refresh;
        private final List<MockPropSetting> prev;
        private final List<MockPropSetting> next;
        AppDiffCallback(boolean refresh, List<MockPropSetting> prev, List<MockPropSetting> next) {
            this.refresh = refresh;
            this.prev = prev;
            this.next = next;
        }

        @Override
        public int getOldListSize() { return prev.size(); }

        @Override
        public int getNewListSize() { return next.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            MockPropSetting s1 = prev.get(oldItemPosition);
            MockPropSetting s2 = next.get(newItemPosition);
            return (!refresh && s1.getName().equalsIgnoreCase(s2.getName()));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            MockPropSetting s1 = prev.get(oldItemPosition);
            MockPropSetting s2 = next.get(newItemPosition);
            return s1.getName().equalsIgnoreCase(s2.getName());
        }
    }

    @Override
    public long getItemId(int position) { return filtered.get(position).hashCode(); }

    @Override
    public int getItemCount() { return filtered.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.propelement, parent, false)); }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.unWire();
        MockPropSetting property = filtered.get(position);
        holder.tvPropName.setText(property.getName());
        holder.tvPropName.setSelected(true);
        holder.cbSkip.setChecked(property.isSkip());
        holder.cbHide.setChecked(property.isHide());
        holder.cbForce.setChecked(property.isForce());
        holder.wire();
    }
}
