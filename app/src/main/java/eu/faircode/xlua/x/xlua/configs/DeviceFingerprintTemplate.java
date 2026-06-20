package eu.faircode.xlua.x.xlua.configs;

import android.os.Build;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

/**
 * Builds a coherent set of fake android.os.Build.* identity values, plus
 * Build.SERIAL / ANDROID_ID / IMEI / GPS location overrides, that can be
 * bundled into an {@link XPConfig} and applied to an app via {@link AppProfile}.
 *
 * Field hooks live in assets/fingerprint/hooks.json under the "Fingerprint"
 * collection, each reading the setting key "fingerprint." + lowercase(hook name)
 * (see assets/fingerprint/fingerprint_value.lua). Build.SERIAL, the
 * Settings.Secure ANDROID_ID lookup, TelephonyManager.getImei, and GPS location
 * spoofing (assets/location_createfromparcel.lua) already had configurable
 * hooks before this feature existed (collection "Privacy"), so those are
 * reused here instead of duplicated.
 */
public class DeviceFingerprintTemplate {
    private static final String[] BUILD_FIELDS = {
            "BOARD", "BRAND", "DEVICE", "DISPLAY", "FINGERPRINT",
            "HARDWARE", "ID", "MANUFACTURER", "MODEL", "PRODUCT"
    };

    public static final String SETTING_SERIAL = "value.serial";
    public static final String SETTING_ANDROID_ID = "value.android_id";
    public static final String SETTING_IMEI = "value.imei";
    public static final String SETTING_LOCATION_TYPE = "location.type";
    public static final String SETTING_LOCATION_LATITUDE = "location.latitude";
    public static final String SETTING_LOCATION_LONGITUDE = "location.longitude";

    public static final String HOOK_SERIAL = "Privacy.Build.SERIAL";
    public static final String HOOK_ANDROID_ID = "Privacy.Settings.Secure.getString/android_id";
    public static final String HOOK_IMEI = "Privacy.TelephonyManager.getImei";
    public static final String HOOK_IMEI_SLOT = "Privacy.TelephonyManager.getImei/slot";
    public static final String HOOK_LOCATION = "Privacy.Location.createFromParcel";

    public final String displayName;
    public final Map<String, String> buildFields = new LinkedHashMap<>();
    public String serial;
    public String androidId;
    public String imei;
    public Double fakeLatitude;
    public Double fakeLongitude;

    public DeviceFingerprintTemplate(String displayName) {
        this.displayName = displayName;
    }

    public DeviceFingerprintTemplate setBuildField(String field, String value) {
        buildFields.put(field.toUpperCase(), value);
        return this;
    }

    public DeviceFingerprintTemplate setSerial(String serial) { this.serial = serial; return this; }
    public DeviceFingerprintTemplate setAndroidId(String androidId) { this.androidId = androidId; return this; }
    public DeviceFingerprintTemplate setImei(String imei) { this.imei = imei; return this; }

    public DeviceFingerprintTemplate setLocation(double latitude, double longitude) {
        this.fakeLatitude = latitude;
        this.fakeLongitude = longitude;
        return this;
    }

    /**
     * Captures this device's own real Build.* values. Always accurate since
     * it reads them live rather than relying on a hardcoded string that goes
     * stale with every monthly security patch.
     */
    public static DeviceFingerprintTemplate captureCurrentDevice() {
        DeviceFingerprintTemplate t = new DeviceFingerprintTemplate("This Device (" + Build.MODEL + ")");
        t.setBuildField("BOARD", Build.BOARD);
        t.setBuildField("BRAND", Build.BRAND);
        t.setBuildField("DEVICE", Build.DEVICE);
        t.setBuildField("DISPLAY", Build.DISPLAY);
        t.setBuildField("FINGERPRINT", Build.FINGERPRINT);
        t.setBuildField("HARDWARE", Build.HARDWARE);
        t.setBuildField("ID", Build.ID);
        t.setBuildField("MANUFACTURER", Build.MANUFACTURER);
        t.setBuildField("MODEL", Build.MODEL);
        t.setBuildField("PRODUCT", Build.PRODUCT);
        return t;
    }

    /**
     * A self-consistent but clearly synthetic identity. Does not claim to be
     * any specific real shipped device, so there is no "wrong/stale real
     * fingerprint" risk.
     */
    public static DeviceFingerprintTemplate genericSynthetic() {
        DeviceFingerprintTemplate t = new DeviceFingerprintTemplate("Generic Synthetic Device");
        t.setBuildField("BOARD", "generic_board");
        t.setBuildField("BRAND", "generic");
        t.setBuildField("DEVICE", "generic_device");
        t.setBuildField("DISPLAY", "GENERIC.000000.001");
        t.setBuildField("FINGERPRINT", "generic/generic_device/generic_device:14/GENERIC.000000.001/0000000:user/release-keys");
        t.setBuildField("HARDWARE", "generic_hw");
        t.setBuildField("ID", "GENERIC.000000.001");
        t.setBuildField("MANUFACTURER", "Generic");
        t.setBuildField("MODEL", "Generic Android Device");
        t.setBuildField("PRODUCT", "generic_device");
        return t;
    }

    public List<SettingPacket> toSettings() {
        List<SettingPacket> list = new ArrayList<>();
        for (String field : BUILD_FIELDS) {
            String value = buildFields.get(field);
            if (value != null) {
                SettingPacket p = new SettingPacket();
                p.name = "fingerprint.build." + field.toLowerCase();
                p.value = value;
                list.add(p);
            }
        }

        if (serial != null) {
            SettingPacket p = new SettingPacket();
            p.name = SETTING_SERIAL;
            p.value = serial;
            list.add(p);
        }

        if (androidId != null) {
            SettingPacket p = new SettingPacket();
            p.name = SETTING_ANDROID_ID;
            p.value = androidId;
            list.add(p);
        }

        if (imei != null) {
            SettingPacket p = new SettingPacket();
            p.name = SETTING_IMEI;
            p.value = imei;
            list.add(p);
        }

        if (fakeLatitude != null && fakeLongitude != null) {
            SettingPacket type = new SettingPacket();
            type.name = SETTING_LOCATION_TYPE;
            type.value = "set";
            list.add(type);

            SettingPacket lat = new SettingPacket();
            lat.name = SETTING_LOCATION_LATITUDE;
            lat.value = String.valueOf(fakeLatitude);
            list.add(lat);

            SettingPacket lon = new SettingPacket();
            lon.name = SETTING_LOCATION_LONGITUDE;
            lon.value = String.valueOf(fakeLongitude);
            list.add(lon);
        }

        return list;
    }

    public List<String> toHookIds() {
        List<String> ids = new ArrayList<>();
        for (String field : BUILD_FIELDS) {
            if (buildFields.containsKey(field))
                ids.add("Fingerprint.Build." + field);
        }

        if (serial != null)
            ids.add(HOOK_SERIAL);

        if (androidId != null)
            ids.add(HOOK_ANDROID_ID);

        if (imei != null) {
            ids.add(HOOK_IMEI);
            ids.add(HOOK_IMEI_SLOT);
        }

        if (fakeLatitude != null && fakeLongitude != null)
            ids.add(HOOK_LOCATION);

        return ids;
    }

    public XPConfig toXPConfig(String configName) {
        return XPConfig.create(configName, "device,fingerprint", "XPL-EX", "1.0", toSettings(), toHookIds());
    }
}
