package eu.faircode.xlua;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import eu.faircode.xlua.api.hook.assignment.LuaAssignment;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.rootbox.XReflectUtils;

public class XSecurity {
    private static final String TAG = "XLua.XSecurity";

    //Patched 0.79 + 0.74 + 0.83 (by: unknown)
    public static final String FINGERPRINT_ONE = "61ed377e85d386a8dfee6b864bd85bbfaa5af81";

    //Patched 0.83 + 0.82 (by: youarefinished)
    public static final String FINGERPRINT_TWO = "13501c9142468b5dca1ffc857abc173488b7784";

    public static void setPerms(File directoryOrFile) {
        setPerms(directoryOrFile, 0770);
    }
    public static void setPerms(File directoryOrFile, int mode) {
        Log.i(TAG, "Setting File Permissions (" + mode + ") SYSTEM_UID For XLUA Directory for UID: " + Process.SYSTEM_UID);

        // Set database file permissions
        // Owner: rwx (system)
        // Group: rwx (system)
        // World: ---
        //Process.myUid()
        XUtil.setPermissions(directoryOrFile.getAbsolutePath(), mode, Process.SYSTEM_UID, Process.SYSTEM_UID);
        File[] files = directoryOrFile.listFiles();
        if (files != null)
            for (File file : files)
                XUtil.setPermissions(file.getAbsolutePath(), mode, Process.SYSTEM_UID, Process.SYSTEM_UID);

        Log.i(TAG, "Finished setting permissions for: " + directoryOrFile.getPath());
    }


    public static Uri getURI() { return XposedUtil.isVirtualXposed() ? Uri.parse("content://eu.faircode.xlua.vxp/") : Settings.System.CONTENT_URI; }
    public static String getProFingerPrint(Context context) {
        try {
            return XUtil.getSha1FingerprintString(context, XCommandBridgeStatic.PRO_PACKAGE);
        }catch (Throwable e) {
            Log.e(TAG, "Failed to get Pro Sha1 Finger Print , Maybe the package does not exist / not installed ? ... " + e);
            return "0";
        }
    }

    public static boolean isSystemProcess(int callingUserId) {
        if(callingUserId == Process.SYSTEM_UID || callingUserId == Process.PHONE_UID) {
            if(DebugUtil.isDebug())
                Log.i(TAG, "Caller is SYSTEM_UID id=" + callingUserId);
            return true;
        } return false;
    }

    public static boolean isSelfProcess(PackageManager pm, int callingUserId) throws PackageManager.NameNotFoundException {
        int uid = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0).uid;
        if (pm.checkSignatures(callingUserId, uid) == PackageManager.SIGNATURE_MATCH) {
            if(DebugUtil.isDebug())
                Log.i(TAG, "Caller is Main Application id=" + callingUserId + " our pkg=" + BuildConfig.APPLICATION_ID);
            return true;
        } return false;
    }

    // Checks whether the caller is 'SYSTEM', the current (self signed) App, or the 'PRO COMPONENT'.
    // Does not modify anything, just inspects the caller. callingUserId must already be resolved
    // via XUtil.getAppId(Binder.getCallingUid()) on the original calling thread.
    private static boolean isTrustedAdminCaller(Context context, int callingUserId) {
        if(isSystemProcess(callingUserId))
            return true;

        try {
            PackageManager pm = context.getPackageManager();
            if(isSelfProcess(pm, callingUserId))
                return true;

            String[] cPackage = pm.getPackagesForUid(callingUserId);
            if(cPackage != null && cPackage.length > 0) {
                String name = cPackage[0];
                String print = XUtil.getSha1FingerprintString(context, name);
                if(DebugUtil.isDebug())
                    Log.i(TAG, "Requesting caller id=" + callingUserId + " pkg=" + name + " fingerprint=" + print);

                Resources resources = pm.getResourcesForApplication(BuildConfig.APPLICATION_ID);
                String proFpOfficial = resources.getString(R.string.pro_fingerprint);
                if(print.equals(proFpOfficial)) {
                    Log.i(TAG, "Authentication Success ! (welcome from ObbedCode) fp=" + proFpOfficial + " (official non modified pro companion application)");
                    return true;
                }

                if(print.equals(FINGERPRINT_ONE)) {
                    Log.i(TAG, "Authentication Success ! (welcome from ObbedCode) fp=" + proFpOfficial + " (Using Generic Modified Version [0.79 / 0.74 / 0.83] Patched by unknown)");
                    return true;
                }

                if(print.equals(FINGERPRINT_TWO)) {
                    Log.i(TAG, "Authentication Success ! (welcome from ObbedCode) fp=" + proFpOfficial + " (Using  0.83 / 0.82 Patched by: youarefinished)");
                    return true;
                }
            }else {
                Log.e(TAG, "Failed to get Caller [" + callingUserId + "] Package / Packages (null or empty) [getPackagesForUid]");
            }
        }catch (Throwable ex) {
            Log.e(TAG, "Call Error: " + ex.getMessage());
        }

        return false;
    }

    // True if the calling uid has a row in the 'assignment' table, meaning the main app has
    // already (admin-authenticated) registered this package as a hook target. Hooked target apps
    // never write this table themselves (InitApp/AssignHooks both require an admin caller), so
    // an app can't grant itself membership here - it can only show up after the main app/Pro
    // companion configured hooks for it.
    // Note: unlike the admin checks above, this compares against the *raw* (non appId-stripped)
    // calling uid, since that's what gets persisted via UserIdentityPacket#getOriginalUser().
    private static boolean isKnownHookedCaller(XDatabaseOld db, int callingUid) {
        if(db == null)
            return false;

        try {
            return SqlQuerySnake.create(db, LuaAssignment.Table.name)
                    .whereColumn("uid", callingUid)
                    .exists();
        }catch (Throwable ex) {
            Log.e(TAG, "Failed to check assignment table for caller uid=" + callingUid + " " + ex);
            return false;
        }
    }

    public static void checkCaller(Context context) throws SecurityException {
        //This will check caller to make sure its either 'SYSTEM' , Current App or 'PRO COMPONENT'
        //Does not Modify Anything, just checks caller makes sure its not an imposter
        //Ensure when calling this method, its not called from a different thread as the caller or result will not be good!!
        int callingUserId = XUtil.getAppId(Binder.getCallingUid());
        Log.i(TAG, "Checking caller id=" + callingUserId);
        long oIdentity = Binder.clearCallingIdentity();
        try {
            if(isTrustedAdminCaller(context, callingUserId))
                return;

            Log.e(TAG, "Signature error cuid=" + callingUserId);
            throw new SecurityException("Signature error cuid=" + callingUserId);
        } finally {
            Binder.restoreCallingIdentity(oIdentity);
        }
    }

    // Gate for the exported VXP ContentProvider (used in place of the SettingsProvider hook when
    // running under VirtualXposed). Unlike checkCaller(), this also accepts callers that aren't
    // the main app/Pro companion but ARE a package the main app has already configured as a hook
    // target (i.e. has an 'assignment' row) - this is required because legitimate calls come from
    // arbitrary hooked target apps running under Zygote injection, which don't share our signature.
    public static void checkVxpCaller(Context context, XDatabaseOld luaDb) throws SecurityException {
        int callingUid = Binder.getCallingUid();
        int callingAppId = XUtil.getAppId(callingUid);
        Log.i(TAG, "Checking VXP caller id=" + callingAppId);
        long oIdentity = Binder.clearCallingIdentity();
        try {
            if(isTrustedAdminCaller(context, callingAppId))
                return;

            if(isKnownHookedCaller(luaDb, callingUid))
                return;

            Log.e(TAG, "Rejected VXP caller, not an admin caller or a known hooked app, cuid=" + callingUid);
            throw new SecurityException("Unauthorized VXP caller cuid=" + callingUid);
        } finally {
            Binder.restoreCallingIdentity(oIdentity);
        }
    }
}
