package gt.com.ro.devumgapp.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppUpdateCheckerTest {

    @Test
    public void testVersionComparison() {
        // Versión remota mayor que local
        assertTrue(AppUpdateChecker.compareVersions("1.1.0", "1.0") > 0);
        assertTrue(AppUpdateChecker.compareVersions("1.1.0", "1.0.0") > 0);
        assertTrue(AppUpdateChecker.compareVersions("2.0.0", "1.9.9") > 0);
        assertTrue(AppUpdateChecker.compareVersions("1.10.0", "1.2.0") > 0);
        assertTrue(AppUpdateChecker.compareVersions("1.0.1", "1.0.0") > 0);

        // Versiones equivalentes
        assertEquals(0, AppUpdateChecker.compareVersions("1.0.0", "1.0"));
        assertEquals(0, AppUpdateChecker.compareVersions("1.0", "1.0.0"));
        assertEquals(0, AppUpdateChecker.compareVersions("1.1.0", "1.1.0"));

        // Versión remota menor que local
        assertTrue(AppUpdateChecker.compareVersions("1.0.0", "1.1.0") < 0);
        assertTrue(AppUpdateChecker.compareVersions("0.9.5", "1.0.0") < 0);
    }
}
