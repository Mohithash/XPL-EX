/*
    This file is part of XPrivacyLua.

    XPrivacyLua is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    XPrivacyLua is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with XPrivacyLua.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2017-2019 Marcel Bokhorst (M66B)
 */

package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.Locale;

import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.utilities.PrefUtil;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;

public class ActivityBase extends AppCompatActivity {
    private static final String TAG = "XLua.ActivityBase";
    private String theme;
    private boolean isForceEnglish;
    //private Locale mCurrentLocale;

    @Override
    protected void onStart() {
        super.onStart();
        //PrefUtil.setString(this, "language", "es");
        //mCurrentLocale = getResources().getConfiguration().locale;
        //mCurrentLocale = new Locale("es");
    }

    public void setForceEnglish(boolean force) {
        try {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            prefs.edit().putBoolean("forceenglish", force).apply();
        }catch (Exception ignored) { }
    }

    public boolean getIsForceEnglish() {
        try {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if(!prefs.contains("forceenglish")) {
                prefs.edit().putBoolean("forceenglish", false).apply();
                return false;
            }

            return prefs.getBoolean("forceenglish", false);
        }catch (Exception ignored) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        theme = GetSettingExCommand.getTheme(this, Process.myUid());
        if(DebugUtil.isDebug())
            Log.d(TAG, "OnCreate Theme=" + theme);

        isForceEnglish = getIsForceEnglish();
        setTheme("dark".equals(theme) ? R.style.AppThemeDark : R.style.AppThemeLight);

        // Android 16 (API 36) enforces edge-to-edge with no opt-out; draw behind the
        // system bars ourselves and restore spacing via applyEdgeToEdgeInsets() below.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        if(isForceEnglish) {
            try {
                String languageToLoad  = "en"; // your language
                Locale locale = new Locale(languageToLoad);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
            }catch (Exception ignored) {

            }
        }

        //Both methods work tho the first one not sure how to change and keeps ovveriding to english this one one alone works as well
        /*String languageToLoad  = "es"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());*/

        super.onCreate(savedInstanceState);
    }


    /*@Override
    protected void onRestart() {
        //set theme ?
        super.onRestart();
        Locale locale = getLocale(this);
        if (!locale.equals(mCurrentLocale)) {
            mCurrentLocale = locale;
            recreate();
        }
    }*/

    public String getThemeName() { return (theme == null ? "dark" : theme); }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        applyEdgeToEdgeInsets();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        applyEdgeToEdgeInsets();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        applyEdgeToEdgeInsets();
    }

    // Pads the activity's actual content (skipping any DrawerLayout's own padding, so
    // its drawer panel stays full-bleed) with the current system bar insets, so toolbars,
    // lists, FABs and snackbars don't render under the status/navigation bar on Android 16+.
    private void applyEdgeToEdgeInsets() {
        ViewGroup decorContent = findViewById(android.R.id.content);
        if (decorContent == null || decorContent.getChildCount() == 0)
            return;

        View root = decorContent.getChildAt(0);
        final View insetTarget = (root instanceof DrawerLayout && ((ViewGroup) root).getChildCount() > 0)
                ? ((ViewGroup) root).getChildAt(0)
                : root;

        final int basePaddingLeft = insetTarget.getPaddingLeft();
        final int basePaddingTop = insetTarget.getPaddingTop();
        final int basePaddingRight = insetTarget.getPaddingRight();
        final int basePaddingBottom = insetTarget.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(insetTarget, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    basePaddingLeft + bars.left,
                    basePaddingTop + bars.top,
                    basePaddingRight + bars.right,
                    basePaddingBottom + bars.bottom);
            return windowInsets;
        });
    }

    /*public static Locale getLocale(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lang = sharedPreferences.getString("language", "es");
        switch (lang) {
            case "English":
                lang = "en";
                break;
            case "Spanish":
                lang = "es";
                break;
        }
        return new Locale(lang);
    }*/
}
