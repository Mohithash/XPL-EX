package eu.faircode.xlua.x.ui.activities;

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
import eu.faircode.xlua.x.xlua.commands.query.GetAssignmentsCommand;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;

/**
 * Read-only log of which hooks have actually fired for which apps, sourced
 * from the existing "assignment" table (used / exception / oldValue / newValue
 * columns, populated by {@link eu.faircode.xlua.hooks.XReport} whenever a hook
 * triggers). No new data collection is added here - this just surfaces data
 * that already exists but had no viewer.
 */
public class ActivityPrivacyReport extends ActivityBase {
    private static final String TAG = "XLua.ActivityPrivacyReport";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    private ListView listReport;
    private TextView textEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacyreportview);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_privacy_report);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listReport = findViewById(R.id.list_privacy_report);
        textEmpty = findViewById(R.id.text_privacy_report_empty);

        loadReport();
    }

    private class ReportAdapter extends ArrayAdapter<AssignmentPacket> {
        ReportAdapter(List<AssignmentPacket> entries) {
            super(ActivityPrivacyReport.this, 0, entries);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if(view == null)
                view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_privacy_report, parent, false);

            AssignmentPacket packet = getItem(position);
            if(packet != null) {
                ((TextView) view.findViewById(R.id.text_report_package)).setText(packet.getCategory());
                ((TextView) view.findViewById(R.id.text_report_time)).setText(dateFormat.format(new Date(packet.used)));
                ((TextView) view.findViewById(R.id.text_report_hook)).setText(packet.getHookId());

                TextView detail = view.findViewById(R.id.text_report_detail);
                if(packet.exception != null) {
                    detail.setText(getString(R.string.label_report_exception_prefix) + " " + packet.exception);
                    detail.setTextColor(ContextCompat.getColor(getContext(), R.color.colorReportException));
                    detail.setVisibility(View.VISIBLE);
                } else if(packet.oldValue != null || packet.newValue != null) {
                    detail.setText(packet.oldValue + " → " + packet.newValue);
                    detail.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
                    detail.setVisibility(View.VISIBLE);
                } else {
                    detail.setVisibility(View.GONE);
                }
            }

            return view;
        }
    }

    private void loadReport() {
        executor.execute(() -> {
            List<AssignmentPacket> assignments;
            try {
                assignments = GetAssignmentsCommand.dump(this, false);
            } catch (Throwable e) {
                Log.e(TAG, "Failed to Load Privacy Report: " + e);
                assignments = new ArrayList<>();
            }

            List<AssignmentPacket> used = new ArrayList<>();
            for(AssignmentPacket packet : assignments)
                if(packet.used > 0)
                    used.add(packet);

            Collections.sort(used, new Comparator<AssignmentPacket>() {
                @Override
                public int compare(AssignmentPacket a, AssignmentPacket b) {
                    return Long.compare(b.used, a.used);
                }
            });

            runOnUiThread(() -> {
                if(used.isEmpty()) {
                    listReport.setVisibility(View.GONE);
                    textEmpty.setVisibility(View.VISIBLE);
                } else {
                    listReport.setVisibility(View.VISIBLE);
                    textEmpty.setVisibility(View.GONE);
                    listReport.setAdapter(new ReportAdapter(used));
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
