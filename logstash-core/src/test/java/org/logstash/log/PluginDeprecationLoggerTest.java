package org.logstash.log;

import org.junit.*;

import org.apache.logging.log4j.LogManager;

import org.logstash.Event;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class PluginDeprecationLoggerTest {

    private static final String CONFIG = "log4j2-log-deprecation-test.properties";
    private static SystemPropsSnapshotHelper snapshotHelper = new SystemPropsSnapshotHelper();

    @BeforeClass
    public static void beforeClass() {
        snapshotHelper.takeSnapshot("log4j.configurationFile", "ls.log.format", "ls.logs",
                LogstashConfigurationFactory.PIPELINE_SEPARATE_LOGS);
        LogTestUtils.reloadLogConfiguration();
    }

    @AfterClass
    public static void afterClass() {
        snapshotHelper.restoreSnapshot("log4j.configurationFile", "ls.log.format", "ls.logs",
                LogstashConfigurationFactory.PIPELINE_SEPARATE_LOGS);
    }

    @Before
    public void setUp() throws IOException {
        System.setProperty("log4j.configurationFile", CONFIG);
        System.setProperty("ls.log.format", "plain");
        System.setProperty("ls.logs", "build/logs");

        LogTestUtils.deleteLogFile("logstash-deprecation.log");
    }

    @After
    public void tearDown() throws IOException {
        LogManager.shutdown();
        LogTestUtils.reloadLogConfiguration();
        LogTestUtils.deleteLogFile("logstash-deprecation.log");
    }

    @Test
    public void testJavaPluginUsesDeprecationLogger() throws IOException {
        Map<String, Object> config = new HashMap<>();
        TestingDeprecationPlugin sut = new TestingDeprecationPlugin(new ConfigurationImpl(config), new ContextImpl(null, null));

        // Exercise
        Event evt = new Event(Collections.singletonMap("message", "Spock move me back"));
        sut.encode(evt, null);

        // Verify
        String logs = LogTestUtils.loadLogFileContent("logstash-deprecation.log");
        assertTrue("Deprecation logs MUST contains the out line", logs.matches(".*Deprecated feature teleportation"));
    }
}
