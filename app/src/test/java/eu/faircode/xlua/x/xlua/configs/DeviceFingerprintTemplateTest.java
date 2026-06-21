package eu.faircode.xlua.x.xlua.configs;

import org.junit.Test;

import java.util.List;

import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DeviceFingerprintTemplateTest {

    private String settingValue(List<SettingPacket> settings, String name) {
        for (SettingPacket p : settings)
            if (name.equals(p.name))
                return p.value;
        return null;
    }

    @Test
    public void genericSynthetic_producesAllTenBuildFields() {
        DeviceFingerprintTemplate t = DeviceFingerprintTemplate.genericSynthetic();
        List<SettingPacket> settings = t.toSettings();

        assertEquals("generic_board", settingValue(settings, "fingerprint.build.board"));
        assertEquals("generic", settingValue(settings, "fingerprint.build.brand"));
        assertEquals("generic_device", settingValue(settings, "fingerprint.build.device"));
        assertEquals("Generic", settingValue(settings, "fingerprint.build.manufacturer"));
        assertEquals("Generic Android Device", settingValue(settings, "fingerprint.build.model"));
    }

    @Test
    public void genericSynthetic_producesMatchingHookIds() {
        DeviceFingerprintTemplate t = DeviceFingerprintTemplate.genericSynthetic();
        List<String> ids = t.toHookIds();

        assertTrue(ids.contains("Fingerprint.Build.MODEL"));
        assertTrue(ids.contains("Fingerprint.Build.MANUFACTURER"));
        assertTrue(ids.contains("Fingerprint.Build.BOARD"));
        assertEquals(10, ids.size()); // exactly the 10 build fields, nothing else set
    }

    @Test
    public void noSerialAndroidIdImei_meansNoExtraSettingsOrHooks() {
        DeviceFingerprintTemplate t = DeviceFingerprintTemplate.genericSynthetic();

        assertNull(settingValue(t.toSettings(), DeviceFingerprintTemplate.SETTING_SERIAL));
        assertFalse(t.toHookIds().contains(DeviceFingerprintTemplate.HOOK_SERIAL));
        assertFalse(t.toHookIds().contains(DeviceFingerprintTemplate.HOOK_ANDROID_ID));
        assertFalse(t.toHookIds().contains(DeviceFingerprintTemplate.HOOK_IMEI));
    }

    @Test
    public void setSerial_addsSettingAndHookId() {
        DeviceFingerprintTemplate t = DeviceFingerprintTemplate.genericSynthetic().setSerial("ABC123");

        assertEquals("ABC123", settingValue(t.toSettings(), DeviceFingerprintTemplate.SETTING_SERIAL));
        assertTrue(t.toHookIds().contains(DeviceFingerprintTemplate.HOOK_SERIAL));
    }

    @Test
    public void setImei_addsBothImeiHookVariants() {
        DeviceFingerprintTemplate t = DeviceFingerprintTemplate.genericSynthetic().setImei("123456789012345");

        assertEquals("123456789012345", settingValue(t.toSettings(), DeviceFingerprintTemplate.SETTING_IMEI));
        assertTrue(t.toHookIds().contains(DeviceFingerprintTemplate.HOOK_IMEI));
        assertTrue(t.toHookIds().contains(DeviceFingerprintTemplate.HOOK_IMEI_SLOT));
    }

    @Test
    public void setLocation_addsTypeLatitudeLongitudeSettingsAndHook() {
        DeviceFingerprintTemplate t = DeviceFingerprintTemplate.genericSynthetic().setLocation(12.34, 56.78);
        List<SettingPacket> settings = t.toSettings();

        assertEquals("set", settingValue(settings, DeviceFingerprintTemplate.SETTING_LOCATION_TYPE));
        assertEquals("12.34", settingValue(settings, DeviceFingerprintTemplate.SETTING_LOCATION_LATITUDE));
        assertEquals("56.78", settingValue(settings, DeviceFingerprintTemplate.SETTING_LOCATION_LONGITUDE));
        assertTrue(t.toHookIds().contains(DeviceFingerprintTemplate.HOOK_LOCATION));
    }

    @Test
    public void noLocationSet_meansNoLocationSettingsOrHook() {
        DeviceFingerprintTemplate t = DeviceFingerprintTemplate.genericSynthetic();

        assertNull(settingValue(t.toSettings(), DeviceFingerprintTemplate.SETTING_LOCATION_TYPE));
        assertFalse(t.toHookIds().contains(DeviceFingerprintTemplate.HOOK_LOCATION));
    }

    @Test
    public void setBuildField_isCaseInsensitiveOnFieldName() {
        DeviceFingerprintTemplate t = new DeviceFingerprintTemplate("test").setBuildField("model", "Pixel Test");
        assertEquals("Pixel Test", settingValue(t.toSettings(), "fingerprint.build.model"));
    }
}
