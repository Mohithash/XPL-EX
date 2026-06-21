package eu.faircode.xlua.x.xlua.commands.query;

import android.content.Context;
import android.database.Cursor;

import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.QueryCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.hook.HookDiagnosticApi;
import eu.faircode.xlua.x.xlua.hook.HookDiagnosticPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

public class GetHookDiagnosticsCommand extends QueryCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(GetHookDiagnosticsCommand.class);

    public static final String COMMAND_NAME = "getHookDiagnostics";

    public GetHookDiagnosticsCommand() { this.name = COMMAND_NAME; this.requiresPermissionCheck = false; }

    @Override
    public Cursor handle(QueryPacket commandData) throws Throwable {
        if(commandData.isDump()) {
            return CursorUtil.toMatrixCursor_final(
                    HookDiagnosticApi.dumpDiagnostics(commandData.getDatabase()),
                    marshall,
                    0);
        } else {
            return CursorUtil.toMatrixCursor_final(
                    HookDiagnosticApi.getDiagnostics(commandData.getDatabase(), commandData.getUserId(), commandData.getCategory()),
                    marshall, 0);
        }
    }

    public static List<HookDiagnosticPacket> dump(Context context, boolean marshall) {
        return ListUtil.copyToArrayList(
                CursorUtil.readCursorAs_final(
                        XProxyContent.luaQuery(context, XProxyContent.commandName(COMMAND_NAME, marshall), new String[] { ActionPacket.ACTION_DUMP }),
                        marshall, HookDiagnosticPacket.class));
    }

    public static List<HookDiagnosticPacket> get(Context context, boolean marshall, int uid, String category) {
        return ListUtil.copyToArrayList(
                CursorUtil.readCursorAs_final(
                        XProxyContent.luaQuery(context, XProxyContent.commandName(COMMAND_NAME, marshall),
                                UserIdentity.createSnakeQueryUID(uid, category).asSnake()),
                        marshall, HookDiagnosticPacket.class));
    }
}
