package com.metservice.testing.javascript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.Main;
import org.slf4j.Logger;

public class JavascriptContextBuilder {

    private static final String ENV_JS = "env.rhino.1.2.js";
    private Context context = Context.enter();
    private static ContextFactory contextFactory;
    private Scriptable scope;
    static {
        Global global = Main.getGlobal();
        contextFactory = ContextFactory.getGlobal();
        global.init(contextFactory);
    }

    public JavascriptContextBuilder() throws FileNotFoundException, IOException {

        context = contextFactory.enterContext();

        scope = context.initStandardObjects();
        context.setOptimizationLevel(-1);
        context.setLanguageVersion(Context.VERSION_1_5);
        context.getWrapFactory().setJavaPrimitiveWrap(false);

        InputStream inputStream = getClass().getResourceAsStream(ENV_JS);
        context.evaluateReader(scope, new InputStreamReader(inputStream, Charset.defaultCharset()),
            ENV_JS, 0, null);
        context.evaluateString(scope, "Envjs({" +
            "  scriptTypes :{" +
            "    '': true,'text/javascript': true,'text/envjs': true" +
            "  }, " +
            "  onScriptLoadError : function(script){ " +
            "    console.error('failed to load script ' + script.src); " +
            "  }" +
            "}); Envjs.log = function(message){java.lang.System.out.println(message);}",
            "", 0, null);
    }

    /**
     * Sets up a target web page to load into the javascript scope.
     * 
     * @param location the url to load into the testing scope
     * @return the builder
     */
    public JavascriptContextBuilder forPageLocation(URL location) {

        String url = location.toString().replace("file:", "file://");
        context.evaluateString(scope, "window.location='" + url + "'", "", 0, null);
        return this;
    }

    /**
     * Loads an additional script into the javascript scope
     * 
     * @param script the script file to inject into the current testing scope.
     * @return the builder
     * @throws FileNotFoundException
     * @throws IOException
     */
    public JavascriptContextBuilder injectScript(File script) throws FileNotFoundException, IOException {
        context.evaluateReader(scope, new FileReader(script),
            script.getName(), 0, null);

        return this;
    }

    /**
     * Injects some javascript content into to testing scope
     * 
     * @param script javascript
     * @return this builder
     */
    public JavascriptContextBuilder injectScript(String script) {
        context.evaluateString(scope, script, "anonymous-" + script.hashCode() + ".js", 0, null);

        return this;
    }

    JavascriptContext build() {
        return new JavascriptContext(context, scope);
    }

    /**
     * injects a Logger into the Javascript context, so outputs via console.
     * 
     * @param log
     */
    public JavascriptContextBuilder useLogger(Logger log) {
        String name = "logger_" + log.hashCode();
        ScriptableObject.putProperty(scope, name, Context.javaToJS(log, scope));
        context.evaluateString(scope,
            "parseArgs = function(args){\n" +
            "  var l = new java.util.ArrayList(args.length);\n" +
            "  for (var i = 0; i < args.length; i++){\n" +
            "    l.add(args[i]);\n"+
            "  }\n" +
            "  return l.toArray();\n" +
            "};\n" +
            "rep = function(txt){var out = (typeof txt == 'string')?txt.replace(/%[sdiofc]/, '{}'):txt; return out;}; "+
            "console.debug = function(message){" + name + ".debug(rep(message), parseArgs(arguments));};\n"
                + "console.info = function(message){" + name
                + ".info(rep(message),   parseArgs(arguments));};\n" + "console.warn = function(message){"
                + name + ".warn(rep(message),   parseArgs(arguments));};\n"
                + "console.error = function(message){" + name
                + ".error(rep(message),   parseArgs(arguments));};", "", 0,
            null);
        return this;
    }

    public void injectObject(String name, Object object) {
        scope.put(name, scope, Context.javaToJS(object, scope));
    }
}
