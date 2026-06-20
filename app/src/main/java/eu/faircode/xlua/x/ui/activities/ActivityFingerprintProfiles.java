package eu.faircode.xlua.x.ui.activities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.util.SparseBooleanArray;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.faircode.xlua.ActivityBase;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.xlua.configs.DeviceFingerprintTemplate;
import eu.faircode.xlua.x.xlua.configs.FingerprintProfileApi;
import eu.faircode.xlua.x.xlua.database.A_CODE;

public class ActivityFingerprintProfiles extends ActivityBase {
    private static final String TAG = "XLua.ActivityFingerprintProfiles";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Chip chipCurrentDevice;
    private EditText editProfileName;
    private EditText editLatitude;
    private EditText editLongitude;
    private ListView listApps;
    private AppListAdapter appListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprintprofilesview);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_fingerprint_profiles);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chipCurrentDevice = findViewById(R.id.chip_template_current_device);
        editProfileName = findViewById(R.id.edit_fingerprint_profile_name);
        editLatitude = findViewById(R.id.edit_fingerprint_latitude);
        editLongitude = findViewById(R.id.edit_fingerprint_longitude);
        listApps = findViewById(R.id.list_fingerprint_apps);
        MaterialButton buttonApply = findViewById(R.id.button_apply_fingerprint_profile);

        loadInstalledApps();

        buttonApply.setOnClickListener(v -> onApplyClicked());
    }

    private static class AppEntry {
        final String label;
        final String packageName;
        final Drawable icon;

        AppEntry(String label, String packageName, Drawable icon) {
            this.label = label;
            this.packageName = packageName;
            this.icon = icon;
        }
    }

    private class AppListAdapter extends ArrayAdapter<AppEntry> {
        AppListAdapter(List<AppEntry> entries) {
            super(ActivityFingerprintProfiles.this, 0, entries);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if(view == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_fingerprint_app, parent, false);

            AppEntry entry = getItem(position);
            if(entry != null) {
                ((TextView) view.findViewById(R.id.text_fingerprint_app_label)).setText(entry.label);
                ((TextView) view.findViewById(R.id.text_fingerprint_app_package)).setText(entry.packageName);
                ((ImageView) view.findViewById(R.id.image_fingerprint_app_icon)).setImageDrawable(entry.icon);
            }

            ((CheckBox) view.findViewById(R.id.checkbox_fingerprint_app)).setChecked(listApps.isItemChecked(position));
            return view;
        }
    }

    private void loadInstalledApps() {
        executor.execute(() -> {
            PackageManager pm = getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(0);

            List<AppEntry> entries = new ArrayList<>();
            for(ApplicationInfo ai : apps) {
                if(getPackageName().equals(ai.packageName))
                    continue;

                String label = String.valueOf(pm.getApplicationLabel(ai));
                Drawable icon;
                try {
                    icon = pm.getApplicationIcon(ai);
                } catch (Exception e) {
                    icon = null;
                }

                entries.add(new AppEntry(label, ai.packageName, icon));
            }

            Collections.sort(entries, new Comparator<AppEntry>() {
                @Override
                public int compare(AppEntry a, AppEntry b) {
                    return a.label.compareToIgnoreCase(b.label);
                }
            });

            runOnUiThread(() -> {
                appListAdapter = new AppListAdapter(entries);
                listApps.setAdapter(appListAdapter);
                listApps.setOnItemClickListener((parent, view, position, id) -> appListAdapter.notifyDataSetChanged());
            });
        });
    }

    private void onApplyClicked() {
        String profileName = editProfileName.getText() == null ? null : editProfileName.getText().toString().trim();
        if(TextUtils.isEmpty(profileName)) {
            Toast.makeText(this, R.string.msg_fingerprint_profile_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if(appListAdapter == null) {
            Toast.makeText(this, R.string.msg_fingerprint_select_at_least_one_app, Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedPackages = new ArrayList<>();
        SparseBooleanArray checked = listApps.getCheckedItemPositions();
        for(int i = 0; i < checked.size(); i++) {
            int position = checked.keyAt(i);
            if(checked.valueAt(i)) {
                AppEntry entry = appListAdapter.getItem(position);
                if(entry != null)
                    selectedPackages.add(entry.packageName);
            }
        }

        if(selectedPackages.isEmpty()) {
            Toast.makeText(this, R.string.msg_fingerprint_select_at_least_one_app, Toast.LENGTH_SHORT).show();
            return;
        }

        DeviceFingerprintTemplate template = chipCurrentDevice.isChecked()
                ? DeviceFingerprintTemplate.captureCurrentDevice()
                : DeviceFingerprintTemplate.genericSynthetic();

        String latText = editLatitude.getText() == null ? "" : editLatitude.getText().toString().trim();
        String lonText = editLongitude.getText() == null ? "" : editLongitude.getText().toString().trim();
        if(!latText.isEmpty() || !lonText.isEmpty()) {
            if(latText.isEmpty() || lonText.isEmpty()) {
                Toast.makeText(this, R.string.msg_fingerprint_location_partial, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                template.setLocation(Double.parseDouble(latText), Double.parseDouble(lonText));
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.msg_fingerprint_location_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        executor.execute(() -> {
            Map<String, A_CODE> results = FingerprintProfileApi.applyToApps(this, template, profileName, selectedPackages);

            int successes = 0;
            for(A_CODE code : results.values())
                if(A_CODE.isSuccessful(code))
                    successes++;

            final int successCount = successes;
            final int total = selectedPackages.size();
            Log.i(TAG, "Applied Fingerprint Profile [" + profileName + "] Results=" + results);

            runOnUiThread(() -> Toast.makeText(
                    ActivityFingerprintProfiles.this,
                    getString(R.string.msg_fingerprint_profile_applied, successCount, total),
                    Toast.LENGTH_LONG).show());
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
