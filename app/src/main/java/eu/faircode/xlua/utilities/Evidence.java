package eu.faircode.xlua.utilities;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.logger.XLog;

public class Evidence {
    private static final String TAG = "XLua.Evidence";
    //public static List<String> BAD

    //List of bad dev ids / serial
    public static final List<String> DEFECT_IDS = Arrays.asList("9774d56d682e549c", "unknown", "000000000000000");

    public static final List<String> SU_PATHS = Arrays.asList(
            "/system/app/superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su",
            "/data/adb/magisk", "/data/adb/ksu", "/data/adb/ap",
            "/data/adb/modules", "/data/adb/ksud", "/data/adb/apd",
            "/system/app/Superuser.apk", "/system/app/SuperSU",
            "/system/etc/init.d", "/system/xbin/daemonsu");

    public static final List<String> ROOT_PACKAGES = Arrays.asList(
            "io.github.vvb2060.magisk", "io.github.vvb2060.magisk.lit",
            "com.noshufou.android.su", "com.noshufou.android.su.elite",
            "eu.chainfire.supersu", "com.koushikdutta.superuser",
            "com.thirdparty.superuser", "com.yellowes.su",
            "com.topjohnwu.magisk", "io.github.huskydg.magisk",
            "me.weishu.kernelsu", "me.bmax.apatch",
            "com.topjohnwu.magisk.alpha", "com.topjohnwu.magisk.canary",
            "io.github.vvb2060.magisk.canary");

    public static final List<String> BAD_APPS = Arrays.asList(
            "com.oasisfeng.greenify", "com.koushikdutta.rommanager",
            "com.dimonvideo.luckypatcher", "com.chelpus.lackypatch",
            "com.ramdroid.appquarantine", "eu.faircode.xlua",
            "org.lsposed.manager", "com.tsng.hidemyapplist",
            "rikka.appops", "com.guoshi.httpcanary", "com.httpcanary.pro",
            "com.aistra.hail", "github.tornaco.android.thanos.pro",
            "com.mrchandler.disableprox", "org.adaway",
            "dev.ukanth.ufirewall.donate", "more.shizuku.privileged.api",
            "ru.bluecat.android.xposed.mods.appsettings", "com.qingyu.rm",
            "lozn.hookui", "me.jsonet.jshook", "com.zhenxi.jnitrace",
            "com.zhenxi.fundex2", "com.zhenxi.funelf",
            "cn.wq.myandroidtools", "ccc71.at.free",
            "com.sanmer.mrepo", "com.fox2code.mmm",
            "me.rhunk.snapenhance", "eu.faircode.xlua.pro",
            "com.berdik.letmedowngrade", "biz.bokhorst.xprivacy",
            "cn.qssq666.systool", "com.wind.cotter",
            "player.normal.np", "lozn.godhand",
            "me.weishu.exp", "com.joeykrim.rootcheck",
            "com.scottyab.rootbeer.sample", "com.devadvance.rootcloak",
            "com.devadvance.rootcloakplus", "com.zachspong.temprootremovejb",
            "com.amphoras.hidemyroot", "com.saurik.substrate",
            "de.robv.android.xposed.installer",
            "org.meowcat.edxposed.manager", "com.formyhm.hideroot",
            "me.weishu.kernelsu", "me.bmax.apatch");
    public static final List<String> CLOAK_APPS = Arrays.asList("com.koushikdutta.rommanager", "com.dimonvideo.luckypatcher", "com.chelpus.lackypatch", "com.ramdroid.appquarantine");

    public static final List<String> SU_MANAGERS = Arrays.asList("busybox", "su", "magisk", "ksu", "ksud", "apd", "apatch");
    public static final List<String> SU_PATHS_EX = Arrays.asList(
            "/data/local/", "/data/local/bin/", "/data/local/xbin/",
            "/sbin/", "/su/bin/", "/system/bin/", "/system/bin/.ext/",
            "/system/bin/failsafe/", "/system/sd/xbin/",
            "/system/usr/we-need-root/", "/system/xbin/",
            "/system/xbin/daemonsu/", "/system/etc/init.d/99SuperSUDaemon/",
            "/system/bin/.ext/.su/", "/system/etc/.has_su_daemon/",
            "/system/etc/.installed_su_daemon/", "/cache/", "/data/", "/dev/",
            "/data/adb/", "/data/adb/modules/", "/data/adb/ksu/",
            "/data/adb/ap/");

    public static final List<String> NON_WRITABLE_DIRS = Arrays.asList("/system", "/system/bin", "/system/sbin", "/system/xbin", "/vendor/bin", "/sbin", "/etc");

    public static final List<String> EMULATOR_APPS = Arrays.asList("com.bignox.appcenter", "com.bluestacks.settings", "com.bluestacks.filemanager", "com.genymotion.superuser", "org.greatfruit.andy.ime", "com.kaopu001.tiantianserver", "com.tiantian.ime", "com.microvirt.installer", "com.android.ld.appstore", "com.ldmnq.launcher3", "com.jide.Appstore");


    public static final List<String> EMULATOR_FILES_RC = Arrays.asList("init.android_x86.rc", "ueventd.android_x86.rc", "fstab.android_x86", "x86.prop", "ueventd.ttVM_x86.rc", "init.ttVM_x86.rc", "fstab.ttVM_x86", "fstab.vbox86", "init.vbox86.rc", "ueventd.vbox86.rc", "ueventd.android_x86_64.rc", "init.android_x86_64.rc", "fstab.goldfish", "init.goldfish.rc", "init.superuser.rc");
    public static final List<String> EMULATOR_SYS_FILES = Arrays.asList("/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace", "/system/bin/qemu-props");
    public static final List<String> EMULATOR_DEV_FILES = Arrays.asList("/dev/socket/qemud", "/dev/qemu_pipe");
    public static final List<String> EMULATOR_FILES = Arrays.asList("init.ranchu.rc", "init.remixos.rc", "init.andy.rc", "ueventd.andy.rc", "bin/genybaseband", "bin/genymotion-vbox-sf", "ueventd.nox.rc", "init.nox.rc", "/system/bin/noxd");

    public static final List<String> EMULATOR_PROPS = Arrays.asList("ro.kernel.qemu", "init.svc.qemu-props", "qemu.hw.mainkeys", "qemu.sf.fake_camera", "qemu.sf.lcd_density", "ro.kernel.android.qemud", "qmu.adb.secure", "qemu.gles", "qemu.logcat", "qemu.timezone", "ro.kernel.qemu.encrypt", "ro.kernel.qemu.gles", "ro.kernel.qemu.gltransport", "ro.kernel.qemu.opengles.version", "ro.kernel.qemu.vsync", "ro.kernel.qemu.wifi", "ro.qemu.initrc");
    public static final List<String> ROOT_PROPS = Arrays.asList(
            "vzw.os.rooted", "magisk",
            "persist.log.tag.LSPosed", "persist.log.tag.LSPosed-Bridge",
            "init.svc.magisk_daemon", "init.svc.magisk_pfs",
            "persist.magisk.hide", "ro.magisk.disable");

    public static final List<String> PROC_MAPS_ARTIFACTS = Arrays.asList(
            "magisk", "lspd", "lsposed", "edxposed", "riru",
            "zygisk", "libmemtrack_real", "libriru", "xposed",
            "substrate", "frida", "sandhook", "yahfa",
            "lsplant", "pine", "whale", "epichook");

    public static final List<String> PROC_MOUNT_ARTIFACTS = Arrays.asList(
            "magisk", "core/mirror", "core/img",
            "ksu", "apatch", "/sbin/.magisk",
            "tmpfs /system/", "tmpfs /vendor/",
            "devpts /dev/pts");

    public static final List<String> PROC_SELF_PATHS = Arrays.asList(
            "/proc/self/maps", "/proc/self/mounts",
            "/proc/self/mountinfo", "/proc/self/mountstats",
            "/proc/self/status", "/proc/self/cmdline",
            "/proc/mounts");

    public static final Map<String, String> ROOT_PROP_SPOOFS = new HashMap<String, String>() {{
        put("ro.debuggable", "0");
        put("ro.secure", "1");
        put("ro.build.selinux", "0");
        put("ro.build.tags", "release-keys");
        put("ro.build.type", "user");
        put("service.bootanim.exit", "1");
    }};

    public static final List<String> EMULATOR_MANUFACTURER_NAMES = Arrays.asList("unknown", "Genymotion", "AndyOS");
    public static final List<String> EMULATOR_BRAND_NAMES = Arrays.asList("generic", "generic_x86", "Android", "AndyOS");
    public static final List<String> EMULATOR_DEVICE_NAMES = Arrays.asList("AndyOSX", "Droid4X", "generic", "generic_x86", "vbox86p");
    public static final List<String> EMULATOR_HARDWARE_NAMES = Arrays.asList("goldfish", "vbox86", "andy", "ranchu", "ttVM_x86", "android_x86");
    public static final List<String> EMULATOR_MODEL_NAMES = Arrays.asList("sdk", "google_sdk", "Android SDK built for x86", "generic");
    public static final List<String> EMULATOR_PRODUCT_NAMES = Arrays.asList("vbox86p", "Genymotion", "Driod4X", "AndyOSX", "remixemu");

    public static final List<String> BAD_STACK_XPOSED = Arrays.asList(
            "xposed", "lsposed", "sandhook", "xlua", "luaj",
            "lsphooker", "yahfa", "lsplant", "pine", "whale",
            "edxposed", "substrate", "frida", "epichook");

    //public static final String BAD_FILE = "superUserApk";//suFileName
    public static final Map<String, String> DANGEROUS_PROPERTIES = new HashMap<String, String>() {{
            put("[ro.debuggable]", "[1]");
            put("[ro.secure]", "[0]");
        }
    };

    public static final String PROP_DEBUGGABLE = "ro.debuggable";
    public static final String PROP_DEBUGGABLE_GOOD = "0";
    public static final String PROP_DEBUGGABLE_BAD = "1";

    public static final String PROP_SECURE = "ro.secure";
    public static final String PROP_SECURE_GOOD = "1";
    public static final String PROP_SECURE_BAD = "0";

    public static final String SETTING_ROOT = "hide.root*.bool";
    public static final String SETTING_QEMU_EMULATOR = "qemu.emulator*.bool";
    public static final String SETTING_EMULATOR = "hide.emulator*.bool";

    public static final String BAD_TAGS = "test-keys";
    public static final String BAD_IP = "0.0.0.0";
    public static final String BAD_NAME = "goldfish";
    public static final String EMULATOR_SHARE_FOLDER = "windows/BstSharedFolder";
    public static final String BAD_PRODUCT_NAME_PATTERN = ".*_?sdk_?.*";//str.matches

    public static final int FILTER_EMULATOR = 0x1;
    public static final int FILTER_ROOT = 0x2;
    public static final int FILTER_EMULATOR_ROOT = 0x3;

    public static StackTraceElement[] stack(StackTraceElement[] trace) {
        List<StackTraceElement> elements = new ArrayList<>();
        //Using this hook can cause issues with hooks that do have errors and dump stack
        //Note this so work around it and with it, use Log here
        boolean hasEndInvoke = false;
        boolean hasInitZygote = false;
        for (int i = trace.length - 1; i >= 0; i--) {
            StackTraceElement el = trace[i];
            String cName = el.getClassName().toLowerCase();
            String mName = el.getMethodName().toLowerCase();

            if(cName.equals("com.android.internal.os") && mName.equals("zygoteinit")) {
                if(!hasInitZygote) {
                    hasInitZygote = true;
                    elements.add(el);
                } continue;
            }

            boolean found  = false;
            for(String xE : BAD_STACK_XPOSED) {
                if(cName.contains(xE)) {
                    Log.w(TAG, "Found a Stack Trace Element: class:[" + cName + "]");
                    found = true;
                    hasEndInvoke = true;
                    break;
                }
            }

            if(found) continue;
            if(hasEndInvoke && cName.equals("java.lang.reflect.method") && mName.equals("invoke")) {
                hasEndInvoke = false;//Found the Invoke Part , bridges to the Hook we don't want to write this
                continue;
            } elements.add(el);
        }

        Collections.reverse(elements);
        return elements.toArray(new StackTraceElement[0]);
    }

    public static boolean containsProperty(String data, int code) {
        if(code == FILTER_EMULATOR || code == FILTER_EMULATOR_ROOT)
            for(String s : EMULATOR_PROPS)
                if(s.contains(data))
                    return true;

        if(code == FILTER_ROOT || code == FILTER_EMULATOR_ROOT)
            for(String s : ROOT_PACKAGES)
                if(s.contains(data))
                    return true;

        return false;
    }

    public static boolean property(String property, int code) {
        if(code == FILTER_EMULATOR || code == FILTER_EMULATOR_ROOT) if(EMULATOR_PROPS.contains(property)) return true;
        if(code == FILTER_ROOT || code == FILTER_EMULATOR_ROOT) return ROOT_PROPS.contains(property);
        return false;
    }

    public static File[] fileArray(File[] files, int code) {
        if(files == null || files.length == 0) return files;
        List<File> nFile = fileList(Arrays.asList(files), code);
        return nFile.toArray(new File[0]);
    }

    public static List<File> fileList(List<File> files, int code) {
        if(files == null || files.isEmpty()) return files;
        List<File> nFiles = new ArrayList<>();
        for(File f : files)
            if(!file(f, code))
                nFiles.add(f);

        return nFiles;
    }

    public static String[] stringArray(String[] files, int code) {
        if(files == null || files.length == 0) return files;
        List<String> nFile = stringList(Arrays.asList(files), code);
        return nFile.toArray(new String[0]);
    }

    public static List<String> stringList(List<String> files, int code) {
        if(files == null || files.isEmpty()) return files;
        List<String> nFiles = new ArrayList<>();
        for(String f : files) {
            if(!f.contains("/"))
                if(!file(null, f, code))
                    nFiles.add(f);
            else {
                if(!file(f, code))
                    nFiles.add(f);
            }
        } return nFiles;
    }

    public static boolean packageName(String packageName, int code) {
        String pLow = packageName.toLowerCase();
        if(code == FILTER_ROOT || code == FILTER_EMULATOR_ROOT) if(ROOT_PACKAGES.contains(pLow) || BAD_APPS.contains(pLow) || CLOAK_APPS.contains(pLow))  return true;
        if(code == FILTER_EMULATOR || code == FILTER_EMULATOR_ROOT) return EMULATOR_APPS.contains(pLow);
        return false;
    }

    public static boolean file(String file, int code) { return file(new File(file), code); }
    public static boolean file(File file,  int code) { return file(file.getAbsolutePath(), file.getName(), code); }
    public static boolean file(String fileFull, String fileName, int code) {
        String ffLow = fileFull == null ? fileName : fileFull.toLowerCase();
        String fnLow = fileName == null ? new File(fileFull).getName().toLowerCase() : fileName.toLowerCase();
        if(code == FILTER_EMULATOR || code == FILTER_EMULATOR_ROOT) {
            if(ffLow != null) {
                for(String f : EMULATOR_SYS_FILES)
                    if(ffLow.startsWith(f))
                        return true;

                for(String f : EMULATOR_DEV_FILES)
                    if(ffLow.startsWith(f))
                        return true;

                for(String f : EMULATOR_FILES_RC)
                    if(ffLow.contains(f.toLowerCase()))
                        return true;

                for(String f : EMULATOR_FILES)
                    if(ffLow.contains(f.toLowerCase()))
                        return true;
            }

            if(EMULATOR_APPS.contains(fnLow))
                return true;

            if(ffLow != null) {
                if(ffLow.contains(EMULATOR_SHARE_FOLDER.toLowerCase()))
                    return true;
            }
        }

        if(code == FILTER_ROOT || code == FILTER_EMULATOR_ROOT) {
            for(String f : SU_PATHS)
                if(f.equalsIgnoreCase(fileFull))
                    return true;
            if(SU_MANAGERS.contains(fnLow)) return true;
            return ROOT_PACKAGES.contains(ffLow) || CLOAK_APPS.contains(ffLow) || BAD_APPS.contains(ffLow);
        } return false;
    }

    public static boolean isProcSelfPath(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        for (String p : PROC_SELF_PATHS)
            if (lower.contains(p))
                return true;
        return false;
    }

    public static boolean isProcMapsLine(String line) {
        if (line == null) return false;
        String lower = line.toLowerCase();
        for (String artifact : PROC_MAPS_ARTIFACTS)
            if (lower.contains(artifact))
                return true;
        return false;
    }

    public static boolean isProcMountLine(String line) {
        if (line == null) return false;
        String lower = line.toLowerCase();
        for (String artifact : PROC_MOUNT_ARTIFACTS)
            if (lower.contains(artifact))
                return true;
        return false;
    }

    public static String spoofRootProperty(String propName) {
        if (propName == null) return null;
        return ROOT_PROP_SPOOFS.get(propName);
    }

    public static boolean isRootProperty(String propName) {
        if (propName == null) return false;
        if (ROOT_PROPS.contains(propName)) return true;
        return ROOT_PROP_SPOOFS.containsKey(propName);
    }

    public static boolean isEmulatorProperty(String propName) {
        if (propName == null) return false;
        return EMULATOR_PROPS.contains(propName);
    }
}
