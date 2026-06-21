package eu.faircode.xlua.utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EvidenceTest {

    // --- file() / FILTER_ROOT ---

    @Test
    public void file_detectsKnownSuPath() {
        assertTrue(Evidence.file("/system/bin/su", Evidence.FILTER_ROOT));
    }

    @Test
    public void file_detectsSuManagerByName() {
        assertTrue(Evidence.file("/some/dir/magisk", "magisk", Evidence.FILTER_ROOT));
    }

    @Test
    public void file_ignoresUnrelatedPathUnderRootFilter() {
        assertFalse(Evidence.file("/data/data/com.example.app/files/data.db", Evidence.FILTER_ROOT));
    }

    @Test
    public void file_rootPathIgnoredWhenFilterIsEmulatorOnly() {
        // FILTER_EMULATOR (0x1) should not match root-only evidence
        assertFalse(Evidence.file("/system/bin/su", Evidence.FILTER_EMULATOR));
    }

    // --- file() / FILTER_EMULATOR ---

    @Test
    public void file_detectsEmulatorSysFile() {
        assertTrue(Evidence.file("/system/bin/qemu-props", Evidence.FILTER_EMULATOR));
    }

    @Test
    public void file_detectsEmulatorDevFile() {
        assertTrue(Evidence.file("/dev/qemu_pipe", Evidence.FILTER_EMULATOR));
    }

    @Test
    public void file_detectsEmulatorAppByName() {
        assertTrue(Evidence.file("/data/app/com.bluestacks.settings", "com.bluestacks.settings", Evidence.FILTER_EMULATOR));
    }

    @Test
    public void file_emulatorPathIgnoredWhenFilterIsRootOnly() {
        assertFalse(Evidence.file("/dev/qemu_pipe", Evidence.FILTER_ROOT));
    }

    // --- file() / FILTER_EMULATOR_ROOT combined ---

    @Test
    public void file_combinedFilterCatchesBothKinds() {
        assertTrue(Evidence.file("/system/bin/su", Evidence.FILTER_EMULATOR_ROOT));
        assertTrue(Evidence.file("/dev/qemu_pipe", Evidence.FILTER_EMULATOR_ROOT));
    }

    // --- proc maps/mounts/self path detection ---

    @Test
    public void isProcMapsLine_detectsMagiskArtifact() {
        assertTrue(Evidence.isProcMapsLine("7f1234000-7f1235000 r-xp 00000000 00:00 0 /data/adb/magisk/magisk64"));
    }

    @Test
    public void isProcMapsLine_detectsLspdArtifact() {
        assertTrue(Evidence.isProcMapsLine("7f1234000-7f1235000 r-xp 00000000 00:00 0 /system/lib64/liblspd.so"));
    }

    @Test
    public void isProcMapsLine_ignoresNormalLibrary() {
        assertFalse(Evidence.isProcMapsLine("7f1234000-7f1235000 r-xp 00000000 00:00 0 /system/lib64/libc.so"));
    }

    @Test
    public void isProcMapsLine_handlesNull() {
        assertFalse(Evidence.isProcMapsLine(null));
    }

    @Test
    public void isProcMountLine_detectsMagiskMirror() {
        assertTrue(Evidence.isProcMountLine("/dev/block/dm-1 /system/core/mirror ext4 ro 0 0"));
    }

    @Test
    public void isProcMountLine_ignoresNormalMount() {
        assertFalse(Evidence.isProcMountLine("/dev/block/dm-1 /data ext4 rw 0 0"));
    }

    @Test
    public void isProcSelfPath_detectsMapsPath() {
        assertTrue(Evidence.isProcSelfPath("/proc/self/maps"));
    }

    @Test
    public void isProcSelfPath_isCaseInsensitive() {
        assertTrue(Evidence.isProcSelfPath("/PROC/SELF/MAPS"));
    }

    @Test
    public void isProcSelfPath_ignoresUnrelatedPath() {
        assertFalse(Evidence.isProcSelfPath("/data/data/com.example/files/foo"));
    }

    // --- root property spoofing ---

    @Test
    public void spoofRootProperty_returnsConfiguredValue() {
        assertEquals("0", Evidence.spoofRootProperty("ro.debuggable"));
        assertEquals("release-keys", Evidence.spoofRootProperty("ro.build.tags"));
    }

    @Test
    public void spoofRootProperty_returnsNullForUnknownProp() {
        assertNull(Evidence.spoofRootProperty("ro.totally.unknown.prop"));
    }

    @Test
    public void isRootProperty_trueForSpoofedProp() {
        assertTrue(Evidence.isRootProperty("ro.secure"));
    }

    @Test
    public void isRootProperty_trueForDetectionOnlyProp() {
        assertTrue(Evidence.isRootProperty("persist.log.tag.LSPosed"));
    }

    @Test
    public void isRootProperty_falseForUnrelatedProp() {
        assertFalse(Evidence.isRootProperty("ro.product.model"));
    }

    // --- emulator property detection ---

    @Test
    public void isEmulatorProperty_trueForQemuProp() {
        assertTrue(Evidence.isEmulatorProperty("ro.kernel.qemu"));
        assertTrue(Evidence.isEmulatorProperty("qemu.hw.mainkeys"));
    }

    @Test
    public void isEmulatorProperty_falseForUnrelatedProp() {
        assertFalse(Evidence.isEmulatorProperty("ro.product.model"));
    }

    @Test
    public void isEmulatorProperty_handlesNull() {
        assertFalse(Evidence.isEmulatorProperty(null));
    }
}
