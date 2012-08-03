package com.metservice.testing.javascript;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class JavascriptContext {

    private final Scriptable scope;
    private final Context context;

    JavascriptContext(Context context, Scriptable scope) {
        this.context = context;
        this.scope = scope;
    }

    private Object evaluate(String script) {
        return context.evaluateString(scope, script, "anonymous-" + script.hashCode() + ".js", 0, null);
    }

    public void eval(String js) {
        evaluate(js);
    }

    @SuppressWarnings("unchecked")
    public <T> T eval(String js, Class<T> returnType)
        throws JavascriptEvaluationException {
        Object evaluate = null;
        try {
            evaluate = evaluate(js);
            return (T) Context.jsToJava(evaluate, returnType);
        } catch (EvaluatorException e) {
            if (returnType.isInterface() && evaluate instanceof NativeObject) {
                final NativeObject scriptable = (NativeObject) evaluate;
                return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {returnType},
                    new InvocationHandler() {

                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if ("equals".equals(method.getName()) && args.length == 1) {
                                return scriptable.equals(args[0]);
                            }
                            if ("hashCode".equals(method.getName()) && args.length == 0) {
                                return scriptable.hashCode();
                            }
                            Object[] wrappedArgs;
                            if (args == null) {
                                wrappedArgs = new Object[0];
                            } else {

                                wrappedArgs = new Object[args.length];
                                for (int i = 0; i < args.length; i++)
                                {
                                    wrappedArgs[i] = Context.javaToJS(args[i], scope);
                                }
                            }

                            Object returned = NativeObject.callMethod(scriptable, method.getName(), wrappedArgs);
                            if ("void".equals(method.getReturnType().getName())) {
                                return null;
                            }
                            return Context.jsToJava(returned, method.getReturnType());

                        }
                    });
            }
            throw new JavascriptEvaluationException(e);
        }
    }

    void close() {
        Context.exit();
    }

    /**
     * Adds a java object under the given name into the javascript context.
     * 
     * @param name the variable name. If existing, it will be overwritten
     * @param object a java object.
     */
    public void put(String name, Object object) {
        ScriptableObject.putProperty(scope, name, Context.javaToJS(object, scope));
    }

    /**
     * Adds a java object as property to the named existing object in the context.
     * 
     * @param exitingObject the name of the object in the scope
     * @param propertyName
     * @param object
     * 
     */
    public void update(String exitingObject, String propertyName, Object object) {
        Object obj = context.evaluateString(scope, exitingObject, "", 0, null);
        if (obj instanceof Undefined) {
            throw new IllegalArgumentException(exitingObject + " is undefined");

        }
        ScriptableObject.putProperty((ScriptableObject) obj, propertyName, Context.javaToJS(object, scope));
    }

    public Object toJSObject(Object obj) {
        return Context.javaToJS(obj, scope);
    }

    public String parseToJSON(String objectName) {
        try {
            // Workaround until Rhino 1.7R4 is released. This is currently a protected class.
            Class<?> clazz = Class.forName("org.mozilla.javascript.NativeJSON");
            Method method = clazz.getMethod("stringify", new Class[] {Context.class, Scriptable.class, Object.class,
                Object.class, Object.class});
            method.setAccessible(true);
            return method.invoke(null, new Object[] {context, scope, scope.get(objectName, scope), null, null})
                .toString();
        } catch (Exception e) {
            throw new JavascriptEvaluationException(e);
        }
    }

    public Object call(Scriptable object, String methodName, Object... args) {
        return ScriptableObject.callMethod(object, methodName, args);
    }

    public Object call(BaseFunction method, NativeObject thisObject, Object... args) {
        return method.call(context, scope, thisObject, args);
    }

}
