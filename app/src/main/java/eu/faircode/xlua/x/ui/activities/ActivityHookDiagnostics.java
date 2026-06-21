package eu.faircode.xlua.x.ui.activities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.faircode.xlua.ActivityBase;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.xlua.commands.query.GetHookDiagnosticsCommand;
import eu.faircode.xlua.x.xlua.hook.HookDiagnosticPacket;

/**
 * Read-only list of hooks that failed to resolve (class/method not found on
 * this device/Android version/OEM build) or failed to install, across all
 * apps. This is the "which scopes actually work" answer - distinct from
 * Privacy Report, which only shows hooks that installed successfully and
 * either fired normally or threw at runtime.
 */
public class ActivityHookDiagnostics extends ActivityBase {
    private static final String TAG = "XLua.ActivityHookDiagnostics";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    private ListView listDiagnostics;
    private TextView textEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hookdiagnosticsview);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_hook_diagnostics);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listDiagnostics = findViewById(R.id.list_hook_diagnostics);
        textEmpty = findViewById(R.id.text_hook_diagnostics_empty);

        loadDiagnostics();
    }

    private String resolveAppLabel(PackageManager pm, String packageName) {
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(ai) + " (" + packageName + ")";
        } catch (Exception e) {
            return packageName;
        }
    }

    private class DiagnosticAdapter extends ArrayAdapter<HookDiagnosticPacket> {
        private final PackageManager pm;

        DiagnosticAdapter(List<HookDiagnosticPacket> entries, PackageManager pm) {
            super(ActivityHookDiagnostics.this, 0, entries);
            this.pm = pm;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if(view == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_hook_diagnostic, parent, false);

            HookDiagnosticPacket packet = getItem(position);
            if(packet != null) {
                ((TextView) view.findViewById(R.id.text_diagnostic_package)).setText(resolveAppLabel(pm, packet.getCategory()));
                ((TextView) view.findViewById(R.id.text_diagnostic_hook_id)).setText(packet.hookId);

                TextView status = view.findViewById(R.id.text_diagnostic_status);
                TextView reason = view.findViewById(R.id.text_diagnostic_reason);

                if(HookDiagnosticPacket.STATUS_INSTALL_FAILED.equals(packet.status)) {
                    status.setText(R.string.label_diagnostic_status_install_failed);
                    status.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReportException));
                } else {
                    status.setText(R.string.label_diagnostic_status_resolve_failed);
                    status.setTextColor(ContextCompat.getColor(getContext(), R.color.colorUnsavedSetting));
                }

                String reasonText = packet.reason == null ? "" : packet.reason;
                reason.setText(dateFormat.format(new Date(packet.timestamp)) + "  -  " + reasonText);
            }

            return view;
        }
    }

    private void loadDiagnostics() {
        executor.execute(() -> {
            List<HookDiagnosticPacket> diagnostics;
            try {
                diagnostics = GetHookDiagnosticsCommand.dump(this, false);
            } catch (Throwable e) {
                Log.e(TAG, "Failed to Load Hook Diagnostics: " + e);
                diagnostics = new ArrayList<>();
            }

            Collections.sort(diagnostics, new Comparator<HookDiagnosticPacket>() {
                @Override
                public int compare(HookDiagnosticPacket a, HookDiagnosticPacket b) {
                    return Long.compare(b.timestamp, a.timestamp);
                }
            });

            final List<HookDiagnosticPacket> finalDiagnostics = diagnostics;
            PackageManager pm = getPackageManager();
            runOnUiThread(() -> {
                if(finalDiagnostics.isEmpty()) {
                    listDiagnostics.setVisibility(View.GONE);
                    textEmpty.setVisibility(View.VISIBLE);
                } else {
                    listDiagnostics.setVisibility(View.VISIBLE);
                    textEmpty.setVisibility(View.GONE);
                    listDiagnostics.setAdapter(new DiagnosticAdapter(finalDiagnostics, pm));
                }
            });
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
