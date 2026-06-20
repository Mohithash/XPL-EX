package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.configs.AppProfile;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;

public class SetAppProfileCommand extends CallCommandHandlerEx {
    private static final String TAG = "XLua.SetAppProfileCommand";
    public static final String COMMAND_NAME = "setAppProfile";

    public SetAppProfileCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        AppProfile profile = commandData.readExtraAs(AppProfile.class);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Received a Set App Profile Command: Packet=" + Str.toStringOrNull(profile));

        if(profile == null || profile.config == null) {
            Log.e(TAG, "Set App Profile Command Failed, Profile or Config was Null");
            return A_CODE.FAILED.toBundle();
        }

        boolean prepRes = DatabaseHelpEx.prepareDatabase(commandData.getDatabase(), AppProfile.TABLE_INFO);
        if(!prepRes) {
            Log.e(TAG, "Failed to Prepare Table: " + Str.toStringOrNull(AppProfile.TABLE_INFO));
            return A_CODE.FAILED.toBundle();
        }

        int uid = commandData.getUid();
        String packageName = commandData.getCategory();

        profile.config.applyAssignments(commandData.getContext(), uid, packageName, true);
        profile.config.applySettings(commandData.getContext(), uid, packageName, true);

        profile.lastApplied = System.currentTimeMillis();
        boolean result = DatabaseHelpEx.insertItem(commandData.getDatabase(), AppProfile.TABLE_NAME, profile);
        A_CODE code = A_CODE.resultToCode_x(result);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Set App Profile Result Code=%s  Packet=%s", code, Str.toStringOrNull(profile)));

        return code.toBundle();
    }

    public static A_CODE call(Context context, AppProfile profile) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Calling [putAppProfile] Command, Profile=" + Str.toStringOrNull(profile));

        Bundle b = profile.toBundle();
        if(DebugUtil.isDebug())
            Log.d(TAG, "Calling [putAppProfile] Command, Profile Bundle=" + Str.toStringOrNull(b));


        return A_CODE.fromBundle(XProxyContent.luaCall(
                context,
                COMMAND_NAME, b));
    }
}
