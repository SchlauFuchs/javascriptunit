package com.hackemesser.testing.javascript;

public class JavascriptEvaluationException extends RuntimeException {

    private static final long serialVersionUID = -8242323498041957724L;

    public JavascriptEvaluationException() {
        super();
    }

    public JavascriptEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JavascriptEvaluationException(String message) {
        super(message);
    }

    public JavascriptEvaluationException(Throwable cause) {
        super(cause);
    }

}
