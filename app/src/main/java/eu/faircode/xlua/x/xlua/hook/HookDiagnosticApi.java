package eu.faircode.xlua.x.xlua.hook;

import java.util.List;

import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;

public class HookDiagnosticApi {
    public static List<HookDiagnosticPacket> dumpDiagnostics(SQLDatabase db) {
        if(!DatabaseHelpEx.ensureTableIsReady(HookDiagnosticPacket.TABLE_INFO, db))
            return ListUtil.emptyList();

        return DatabaseHelpEx.getFromDatabase(
                db,
                HookDiagnosticPacket.TABLE_NAME,
                HookDiagnosticPacket.class, true);
    }

    public static List<HookDiagnosticPacket> getDiagnostics(SQLDatabase db, int userId, String category) {
        if(!DatabaseHelpEx.ensureTableIsReady(HookDiagnosticPacket.TABLE_INFO, db))
            return ListUtil.emptyList();

        return SQLSnake.create(db, HookDiagnosticPacket.TABLE_NAME)
                .ensureDatabaseIsReady()
                .whereIdentity(userId, category)
                .asSnake()
                .queryAs(HookDiagnosticPacket.class, true, false);
    }
}
