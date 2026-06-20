package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import eu.faircode.xlua.api.properties.MockPropConversions;
import eu.faircode.xlua.api.properties.MockPropGroupHolder;
import eu.faircode.xlua.api.properties.MockPropSetting;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.api.xmock.XMockQuery;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.ui.transactions.PropTransactionResult;
import eu.faircode.xlua.ui.PropertyQue;
import eu.faircode.xlua.ui.ViewFloatingAction;
import eu.faircode.xlua.ui.dialogs.PropertyAddDialogEx;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.ui.interfaces.IPropertyUpdate;
import eu.faircode.xlua.utilities.UiUtil;

public class FragmentProperties extends ViewFloatingAction implements
        View.OnClickListener,
        View.OnLongClickListener,
        ILoader, IPropertyUpdate {
    private final static String TAG = "XLua.FragmentProperties";

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private AdapterPropertiesGroup rvPropsAdapter;
    private TextView tvPropCount;

    private PropertyQue propertyQue;
    private View view;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.proprecyclerview, container, false);

        this.TAG_ViewFloatingAction = TAG;
        this.application = AppGeneric.from(getArguments(), getContext());

        super.initActions();
        super.bindTextViewsToAppId(this.view, R.id.ivPropertiesAppIcon, R.id.tvPropertiesPackageName, R.id.tvPropertiesPackageFull, R.id.tvPropertiesPackageUid);
        super.setFloatingActionBars(this, this, this.view,  R.id.flPropertiesMainButton, R.id.flPropertiesAddMapButton);

        tvPropCount = this.view.findViewById(R.id.tvPropCountProperties);

        progressBar = this.view.findViewById(R.id.pbProperties);
        int colorAccent = XUtil.resolveColor(Objects.requireNonNull(getContext()), R.attr.colorAccent);
        swipeRefresh = this.view.findViewById(R.id.swipeRefreshProperties);
        swipeRefresh.setColorSchemeColors(colorAccent, colorAccent, colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { loadData(); }
        });

        super.initRecyclerView(this.view, R.id.rvProperties, true);
        rvList.setVisibility(View.VISIBLE);
        rvList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean onRequestChildFocus(@NonNull RecyclerView parent, @NonNull RecyclerView.State state, @NonNull View child, View focused) { return true; }
        };

        llm.setAutoMeasureEnabled(true);
        rvList.setLayoutManager(llm);
        rvPropsAdapter = new AdapterPropertiesGroup(this);
        rvList.setAdapter(rvPropsAdapter);

        propertyQue = new PropertyQue(application);

        loadData();
        return this.view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onLongClick(View v) {
        int id = v.getId();
        XLog.i("onLongClick id=" + id);
        if (id == R.id.flPropertiesAddMapButton) {
            Snackbar.make(this.view, R.string.menu_property_add_hint, Snackbar.LENGTH_LONG).show();
        }
        return false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        XLog.i("onClick id=" + id);
        if (id == R.id.flPropertiesMainButton) {
            invokeFloatingActions();
        } else if (id == R.id.flPropertiesAddMapButton) {
            new PropertyAddDialogEx()
                    .setCallback(this)
                    .setPropertyQue(propertyQue)
                    .show(Objects.requireNonNull(getFragmentManager()), view.getContext().getString(R.string.title_add_property));
        }
    }

    @Override
    public void onPropertyUpdate(PropTransactionResult result) {
        if(result != null && result.hasAnySucceeded()) {
            Snackbar.make(this.view, R.string.result_property_added_success, Snackbar.LENGTH_LONG).show();
            loadData();
        }else Snackbar.make(this.view, R.string.result_property_added_failed, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onResume() { super.onResume(); }

    @Override
    public void onPause() { super.onPause(); }

    public void filter(String query) { if (rvPropsAdapter != null) rvPropsAdapter.getFilter().filter(query); }

    @Override
    public FragmentManager getManager() { return getFragmentManager(); }

    @Override
    public Fragment getFragment() { return this; }

    @Override
    public AppGeneric getApplication() { return this.application; }

    public void loadData() {
        XLog.i("Started Data Loader");
        LoaderManager manager = getActivity().getSupportLoaderManager();
        manager.restartLoader(ActivityMain.LOADER_DATA, new Bundle(), dataLoaderCallbacks).forceLoad();
    }

    LoaderManager.LoaderCallbacks<PropsDataHolder> dataLoaderCallbacks = new LoaderManager.LoaderCallbacks<PropsDataHolder>() {
        @NonNull
        @Override
        public Loader<PropsDataHolder> onCreateLoader(int id, Bundle args) { return new PropsDataLoader(getContext()).setApp(application); }

        @SuppressLint("SetTextI18n")
        @Override
        public void onLoadFinished(@NonNull Loader<PropsDataHolder> loader, PropsDataHolder data) {
            XLog.i("onLoadFinished");
            if(data.exception == null) {
                UiUtil.initTheme(getActivity(), data.theme);
                Collections.sort(data.propGroups, new Comparator<MockPropGroupHolder>() {
                    @Override
                    public int compare(MockPropGroupHolder o1, MockPropGroupHolder o2) {
                        return o1.getSettingName().compareToIgnoreCase(o2.getSettingName());
                    }
                });

                tvPropCount.setText(Integer.toString(data.totalProps));
                rvPropsAdapter.set(data.propGroups, getContext());
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
            }else {
                Log.e(TAG, Log.getStackTraceString(data.exception));
                Snackbar.make(Objects.requireNonNull(getView()), data.exception.toString(), Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<PropsDataHolder> loader) { }
    };

    private static class PropsDataLoader extends AsyncTaskLoader<PropsDataHolder> {
        private AppGeneric application;
        public PropsDataLoader setApp(AppGeneric application) { this.application = application; return this; }
        PropsDataLoader(Context context) { super(context); setUpdateThrottle(1000); }

        @Nullable
        @Override
        public PropsDataHolder loadInBackground() {
            XLog.i("Data Loader has Started! application=" + application);
            PropsDataHolder data = new PropsDataHolder();
            try {
                data.theme = XLuaCall.getTheme(getContext());
                Collection<MockPropSetting> props = XMockQuery.getAllProperties(getContext(), application);
                Collection<LuaSettingExtended> settings = new ArrayList<>(XMockQuery.getAllSettings(getContext(), true, application.getUid(), application.getPackageName()));
                data.totalProps = props.size();
                data.propGroups = new ArrayList<>(MockPropConversions.createHolders(getContext(), props, settings));
                XLog.i("Props count=" + props.size() + " Settings Count=" + settings.size() + " Groups Conversion=" + data.propGroups.size());
            }catch (Throwable ex) {
                data.propGroups.clear();
                data.exception = ex;
                data.totalProps = 0;
                XLog.e("Failed to load data in Background!", ex, true);
            }

            XLog.i("Data Loader has Finished! Groups Count=" + data.propGroups.size() + " Properties Count=" + data.totalProps);
            return data;
        }
    }

    private static class PropsDataHolder {
        String theme;
        List<MockPropGroupHolder> propGroups = new ArrayList<>();
        int totalProps;
        Throwable exception = null;
    }
}
