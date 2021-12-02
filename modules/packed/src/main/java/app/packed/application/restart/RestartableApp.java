package app.packed.application.restart;

import java.util.function.BiFunction;

import app.packed.container.Assembly;
import app.packed.container.Wirelet;

public interface RestartableApp<T> {

    void restart();
    
    static <T> RestartableApp<T> of(BiFunction<Assembly , Wirelet[], T> action, Assembly  assembly, Wirelet... wirelets) {
        // Vi smider maaske en special BuildException som vi kan catche
        throw new UnsupportedOperationException();
    }

}
