package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.hook.HookDiagnosticPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

public class PutHookDiagnosticCommand extends CallCommandHandlerEx {
    private static final String TAG = "XLua.PutHookDiagnosticCommand";
    public static final String COMMAND_NAME = "putHookDiagnostic";

    public PutHookDiagnosticCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        HookDiagnosticPacket packet = commandData.readExtraAs(HookDiagnosticPacket.class);
        if(packet == null || packet.hookId == null)
            return A_CODE.FAILED.toBundle();

        boolean prepRes = DatabaseHelpEx.prepareDatabase(commandData.getDatabase(), HookDiagnosticPacket.TABLE_INFO);
        if(!prepRes) {
            Log.e(TAG, "Failed to Prepare Table: " + Str.toStringOrNull(HookDiagnosticPacket.TABLE_INFO));
            return A_CODE.FAILED.toBundle();
        }

        packet.setUserIdentity(UserIdentity.fromUid(commandData.getUid(), commandData.getCategory()));
        boolean result = DatabaseHelpEx.insertItem(commandData.getDatabase(), HookDiagnosticPacket.TABLE_NAME, packet);
        A_CODE code = A_CODE.resultToCode_x(result);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Put Hook Diagnostic Result Code=%s  Packet=%s", code, Str.toStringOrNull(packet)));

        return code.toBundle();
    }

    public static A_CODE call(Context context, int uid, String packageName, HookDiagnosticPacket packet) {
        packet.setUserIdentity(UserIdentity.fromUid(uid, packageName));
        Bundle b = packet.toBundle();
        return A_CODE.fromBundle(XProxyContent.luaCall(context, COMMAND_NAME, b));
    }
}
