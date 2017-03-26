package com.developmentontheedge.be5.api.exceptions;


import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionHelper
{
    public static Be5Exception getInternalBe5Exception(Logger log, Throwable e) {
        log.log(Level.SEVERE, e.getMessage(), e);
        return Be5Exception.internal(e);
    }

    public static Be5Exception getInternalBe5Exception(Logger log, String message) {
        log.log(Level.SEVERE, message);
        return Be5Exception.internal(message);
    }

    public static Be5Exception getInternalBe5Exception(Logger log, String message, Throwable e) {
        log.log(Level.SEVERE, message + " " + e.getMessage(), e);
        return Be5Exception.internal(e, message);
    }
}
