package com.metservice.testing.javascript;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;
import com.metservice.testing.javascript.annotation.Javascript;

public class JavascriptTestRunner extends BlockJUnit4ClassRunner {

    public JavascriptTestRunner(Class<?> klass) throws InitializationError {
        super(klass);

    }

    @Override
    protected Object createTest() throws Exception {

        Object test = super.createTest();
        for (Field f : test.getClass().getDeclaredFields()) {
            Javascript annotation = f.getAnnotation(Javascript.class);
            if (annotation != null) {
                JavascriptContextBuilder builder = new JavascriptContextBuilder();
                if(annotation.useLogger()){
                    builder.useLogger(LoggerFactory.getLogger(test.getClass()));
                }
                if(annotation.insertTestIntoScopeAs().length()>0){
                    builder.injectObject(annotation.insertTestIntoScopeAs(),test);
                }
                String location = annotation.locationURL();
                String url = null;
                if (location.length() == 0) {
                    location = annotation.location();
                    if (location.length() > 0) {
                        File file = new File(location).getAbsoluteFile();
                        if(file.canRead()){
                        url = file.toURI().toString();
                        }
                        else {
                            throw new IllegalArgumentException("Can't read file " + file);
                        }
                    }
                } else {
                    url = location;
                }
                if (url != null) {
                    builder.forPageLocation(new URL(url));
                }
                for(String script:annotation.extraScriptFiles()){
                    if(script.length()==0){
                        continue;
                    }
                    File scriptFile;
                    if( annotation.extraScriptFileBaseDir().length()>0){
                        scriptFile = new File(annotation.extraScriptFileBaseDir(), script);
                    } else {
                        scriptFile = new File(script);
                    }
                    builder.injectScript(scriptFile);
                }
                for(String script:annotation.extraScripts()){
                    builder.injectScript(script);
                }

                // TODO additional scripts 
                f.setAccessible(true);
                f.set(test, builder.build());
            }
        }
        return test;
    }

    @Override
    protected List<TestRule> getTestRules(final Object target) {
        List<TestRule> rules = super.getTestRules(target);
        for (final Field f : target.getClass().getDeclaredFields()) {
            Javascript annotation = f.getAnnotation(Javascript.class);
            if (annotation != null) {
                rules.add(new TestRule() {

                    @Override
                    public Statement apply(final Statement base, Description description) {
                        return new Statement() {
                            
                            @Override
                            public void evaluate() throws Throwable {
                                base.evaluate();
                                f.setAccessible(true);
                                try {
                                    ((JavascriptContext)f.get(target)).close();
                                } catch (IllegalAccessException e) {
                                  throw new IllegalArgumentException(e);
                                }
                            }
                        };
                    }
                });
                
            }
        }
        return rules;
    }

}
