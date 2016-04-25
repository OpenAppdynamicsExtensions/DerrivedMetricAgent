package com.appdynamics.ace.agents.derivedMetrics;

/**
 * Created by stefan.marx on 14.04.16.
 */
public class CalculationException extends Exception {
    public CalculationException() {
        super();
    }

    public CalculationException(String message) {
        super(message);
    }

    public CalculationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalculationException(Throwable cause) {
        super(cause);
    }

    protected CalculationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
