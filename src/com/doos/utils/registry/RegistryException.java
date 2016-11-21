package com.doos.utils.registry;

/**
 * Created by Eugene Zrazhevsky on 21.11.2016.
 */
public class RegistryException extends Exception {
    public RegistryException() {
        super();
    }

    public RegistryException(String message) {
        super(message);
    }

    public RegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
