package com.hackemesser.testing.javascript.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is a marker for the JavascriptTestRunner. it contains informations how to set up the Javascript
 * context for a test.
 * 
 * @author hackemesser
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Javascript {

    /**
     * If the context should load a html page from a url, add it here. If that html content has script elements, they
     * will be loaded and evaluated. Only one of {@link #location()} and {@link #locationURL()} is allowed.
     */
    String locationURL() default "";

    /**
     * If the context should load a html page from a source folder, add it here. If that html content has script
     * elements, they will be loaded and evaluated. Only one of {@link #location()} and {@link #locationURL()} is
     * allowed.
     */
    String location() default "";

    /**
     * Add some evaluatable script blocks here if required.
     * 
     * @return
     */
    String[] extraScripts() default "";

    /**
     * List all script files here that should be loaded into the javascript context. If they all have the same base
     * directory, you can shorten the strings by specifying {@link #extraScriptFileBaseDir()}. This takes
     * relative/absolute folder+file names only, not URI's
     */
    String[] extraScriptFiles() default "";

    /**
     * This is the base folder of the external script files to load. If not given, the extraScripts should be fully
     * specifying the script location.
     */
    String extraScriptFileBaseDir() default "";

    /**
     * If set to true, the test logger will be used to print console log.
     */
    boolean useLogger() default false;

    /**
     *  If set, the junit itself gets inserted into the javascript context using the given name.
     */
    String insertTestIntoScopeAs() default "";
}
