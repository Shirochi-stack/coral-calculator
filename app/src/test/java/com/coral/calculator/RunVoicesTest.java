package com.coral.calculator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

/** Lines are recorded either among the raw resources or in the voices asset pack, never in both; and the tool that records them knows both. */
public class RunVoicesTest {
    static File raw(String name) { return new File("src/main/res/raw/run_v_" + name + ".ogg"); }

    static File packed(String name) { return new File("../voices/src/main/assets/" + RunAudio.PACK_DIR + "/run_v_" + name + ".ogg"); }

    /** Whether a line has its recording, in either place. */
    static boolean recorded(String name) { return raw(name).isFile() || packed(name).isFile(); }

    @Test public void aLineIsRecordedInOnePlaceOnly() {
        File[] inPack = new File("../voices/src/main/assets/" + RunAudio.PACK_DIR).listFiles();
        assertTrue(inPack != null && inPack.length > 0);
        for (File one : inPack) {
            assertTrue(one.getName(), one.getName().startsWith("run_v_") && one.getName().endsWith(".ogg"));
            assertFalse(one.getName(), new File("src/main/res/raw/" + one.getName()).exists());
        }
    }

    @Test public void theVoicesPackIsDeliveredAndInTheDebugBuild() throws Exception {
        String settings = new String(java.nio.file.Files.readAllBytes(new File("../settings.gradle").toPath()), "UTF-8");
        String app = new String(java.nio.file.Files.readAllBytes(new File("build.gradle").toPath()), "UTF-8");
        assertTrue(settings.contains("include ':voices'"));
        assertTrue(app.contains("':voices'") && app.contains("'../voices/src/main/assets'"));
        String tool = new String(java.nio.file.Files.readAllBytes(new File("../tools/make_run_voice.py").toPath()), "UTF-8");
        assertTrue(tool.contains("--pack") && tool.contains("PACK = ROOT / \"voices\""));
    }
}
