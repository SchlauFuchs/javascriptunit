package com.metservice.testing.javascript;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.metservice.testing.javascript.annotation.Javascript;

@RunWith(JavascriptTestRunner.class)
public class ExampleTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleTest.class);

    @Javascript(location = "src/test/resources/example.html", extraScripts = "var example = {'ping':'pong'}",
            extraScriptFileBaseDir = "src/test/resources", extraScriptFiles = "example.js", useLogger=true)
    private JavascriptContext context;

    @Test
    public void testEnvJsIsWorking() {
        String output = context.eval("new Date();", String.class);
        assertNotNull(output);
    }

    @Test
    public void testScriptElementsInHtmlAreLoaded() {
        String output = context.eval("$()", String.class);
        assertNotNull(output);
    }

    @Test
    public void testInjectedAnonymousScriptIsEvaluated() {
        String output = context.eval("example.ping", String.class);
        assertEquals("pong", output);
    }

    @Test
    public void testInjectedExternalScriptIsEvaluated() {
        int output = context.eval("externalExample", Integer.class);
        assertEquals(26, output);
    }

    @Test
    public void testConsoleOutputsWorking() {
        ScriptableObject console = context.eval("console", ScriptableObject.class);
        for (Object key : console.getAllIds()) {
            Object value = console.get(key.toString(), console);
            LOG.info(key + ": " + Context.jsToJava(value, String.class));
        }
        context.eval("console.error('a console error');");
        context.eval("console.warn('a console warning');");
        context.eval("console.info('a console info');");
        context.eval("console.debug('a console debug');");
    }

    @Test
    public void testParseToJSON() {
        context.eval("var example = {a:'b', b:'a'};");
        assertEquals("{\"a\":\"b\",\"b\":\"a\"}", context.parseToJSON("example"));
    }
    
}
