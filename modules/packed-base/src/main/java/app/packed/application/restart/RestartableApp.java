package app.packed.application.restart;

import java.util.function.BiFunction;

import app.packed.bundle.Bundle;
import app.packed.bundle.Wirelet;

public interface RestartableApp<T> {

    void restart();
    
    static <T> RestartableApp<T> of(BiFunction<Bundle<?>, Wirelet[], T> action, Bundle<?> assembly, Wirelet... wirelets) {
        // Vi smider maaske en special BuildException som vi kan catche
        throw new UnsupportedOperationException();
    }

}
