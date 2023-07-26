package kd.finalyearproject.runtime.profiler.util;

import static kd.finalyearproject.runtime.profiler.util.Constants.OUT;

public class CustomException extends Exception {
    public CustomException(String message) {
        OUT.printf(message + "\n");
    }

    public CustomException(Throwable cause) {
        OUT.printf(cause + "\n");
    }

    public CustomException(String message, Throwable cause) {
        OUT.printf(message + cause + "\n");
    }
}
