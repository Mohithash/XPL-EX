package eu.faircode.xlua.x.xlua.configs;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.commands.call.SetAppProfileCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

/**
 * Builds an {@link AppProfile} from a {@link DeviceFingerprintTemplate} and
 * applies it to one or more apps via {@link SetAppProfileCommand}.
 */
public class FingerprintProfileApi {
    private static final String TAG = "XLua.FingerprintProfileApi";

    public static AppProfile buildProfile(Context context, DeviceFingerprintTemplate template, String profileName, String packageName) throws PackageManager.NameNotFoundException {
        int uid = context.getPackageManager().getApplicationInfo(packageName, 0).uid;

        AppProfile profile = AppProfile.create();
        profile.name = profileName;
        profile.version = "1.0";
        profile.description = "Fingerprint profile: " + template.displayName;
        profile.creationDate = System.currentTimeMillis();
        profile.setUserIdentity(UserIdentity.fromUid(uid, packageName));
        profile.config = template.toXPConfig(profileName);

        return profile;
    }

    public static A_CODE applyToApp(Context context, DeviceFingerprintTemplate template, String profileName, String packageName) {
        try {
            AppProfile profile = buildProfile(context, template, profileName, packageName);
            A_CODE code = SetAppProfileCommand.call(context, profile);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Applied Fingerprint Profile [%s] to [%s], Result=%s", profileName, packageName, code));
            return code;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to Resolve Package for Fingerprint Profile Apply: " + packageName + " Error=" + e);
            return A_CODE.FAILED;
        }
    }

    public static Map<String, A_CODE> applyToApps(Context context, DeviceFingerprintTemplate template, String profileName, List<String> packageNames) {
        Map<String, A_CODE> results = new LinkedHashMap<>();
        for(String packageName : packageNames)
            results.put(packageName, applyToApp(context, template, profileName, packageName));

        return results;
    }
}
