package eu.faircode.xlua.x.xlua.hook;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.xlua.IBundleData;
import eu.faircode.xlua.x.xlua.PacketBase;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.interfaces.IParcelType;

/**
 * Records that an assigned hook failed to resolve (its target class/method
 * doesn't exist on this device/Android version/OEM build) or failed to
 * install (the hook framework rejected it). This is distinct from
 * {@link AssignmentPacket}'s used/exception columns, which record runtime
 * failures of a hook that DID install successfully. Written by
 * {@link eu.faircode.xlua.x.hook.HookCore} when it would otherwise silently
 * skip a hook it can't apply.
 */
public class HookDiagnosticPacket extends PacketBase implements IBundleData, IParcelType, IJsonType, ICursorType {
    public static final String STATUS_RESOLVE_FAILED = "resolve_failed";
    public static final String STATUS_INSTALL_FAILED = "install_failed";

    public static final String FIELD_USER = UserIdentityIO.FIELD_USER;
    public static final String FIELD_CATEGORY = UserIdentityIO.FIELD_CATEGORY;

    public static final String FIELD_HOOK_ID = "hookId";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_REASON = "reason";
    public static final String FIELD_TIMESTAMP = "timestamp";

    public static final String TABLE_NAME = "hook_diagnostics";

    public static final TableInfo TABLE_INFO = TableInfo.create(TABLE_NAME)
            .putIdentification()
            .putText(FIELD_HOOK_ID)
            .putText(FIELD_STATUS)
            .putText(FIELD_REASON)
            .putInteger(FIELD_TIMESTAMP)
            .putPrimaryKey(true, FIELD_HOOK_ID);

    public String hookId;
    public String status;
    public String reason;
    public long timestamp;

    public HookDiagnosticPacket() { }
    public HookDiagnosticPacket(Parcel in) { fromParcel(in); }

    public static HookDiagnosticPacket create(String hookId, String status, String reason) {
        HookDiagnosticPacket p = new HookDiagnosticPacket();
        p.hookId = hookId;
        p.status = status;
        p.reason = reason;
        p.timestamp = System.currentTimeMillis();
        return p;
    }

    @Override
    public String getObjectId() { return hookId; }

    @Override
    public void setId(String id) { this.hookId = id; }

    @Override
    public void populateFromBundle(Bundle b) {
        if(b != null) {
            super.populateFromBundle(b);
            this.hookId = b.getString(FIELD_HOOK_ID);
            this.status = b.getString(FIELD_STATUS);
            this.reason = b.getString(FIELD_REASON);
            this.timestamp = b.getLong(FIELD_TIMESTAMP);
        }
    }

    @Override
    public void populateBundle(Bundle b) {
        if(b != null) {
            super.populateBundle(b);
            b.putString(FIELD_HOOK_ID, this.hookId);
            b.putString(FIELD_STATUS, this.status);
            b.putString(FIELD_REASON, this.reason);
            b.putLong(FIELD_TIMESTAMP, this.timestamp);
        }
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        populateBundle(b);
        return b;
    }

    @Override
    public void fromParcel(Parcel in) {
        this.hookId = in.readString();
        this.status = in.readString();
        this.reason = in.readString();
        this.timestamp = in.readLong();
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(this.hookId);
        parcel.writeString(this.status);
        parcel.writeString(this.reason);
        parcel.writeLong(this.timestamp);
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(FIELD_HOOK_ID, this.hookId);
        values.put(FIELD_STATUS, this.status);
        values.put(FIELD_REASON, this.reason);
        values.put(FIELD_TIMESTAMP, this.timestamp);
        return values;
    }

    @Override
    public void fromCursor(Cursor c) {
        if(c != null) {
            this.hookId = CursorUtil.getString(c, FIELD_HOOK_ID);
            this.status = CursorUtil.getString(c, FIELD_STATUS);
            this.reason = CursorUtil.getString(c, FIELD_REASON);
            this.timestamp = CursorUtil.getLong(c, FIELD_TIMESTAMP);
        }
    }

    @Override
    public void populateSnake(SQLQueryBuilder snake) {
        snake.whereIdentity(getUserIdentity().getUserId(true), getCategory());
        snake.whereColumn(FIELD_HOOK_ID, this.hookId);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Parcelable.Creator<HookDiagnosticPacket> CREATOR = new Parcelable.Creator<HookDiagnosticPacket>() {
        @Override
        public HookDiagnosticPacket createFromParcel(Parcel source) { return new HookDiagnosticPacket(source); }
        @Override
        public HookDiagnosticPacket[] newArray(int size) { return new HookDiagnosticPacket[size]; }
    };

    @Override
    public String toJSONString() throws JSONException { return toJSONObject().toString(); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(FIELD_USER, 0);
        object.put(FIELD_CATEGORY, getCategory());
        object.put(FIELD_HOOK_ID, hookId);
        object.put(FIELD_STATUS, status);
        object.put(FIELD_REASON, reason);
        object.put(FIELD_TIMESTAMP, timestamp);
        return object;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(obj != null) {
            this.hookId = obj.optString(FIELD_HOOK_ID);
            this.status = obj.optString(FIELD_STATUS);
            this.reason = obj.optString(FIELD_REASON);
            this.timestamp = obj.optLong(FIELD_TIMESTAMP);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine(FIELD_USER, getUid())
                .appendFieldLine(FIELD_CATEGORY, getCategory())
                .appendFieldLine(FIELD_HOOK_ID, hookId)
                .appendFieldLine(FIELD_STATUS, status)
                .appendFieldLine(FIELD_REASON, reason)
                .appendFieldLine(FIELD_TIMESTAMP, timestamp)
                .toString(true);
    }
}
