package app.packed.application.restart;

import java.util.function.BiFunction;

import app.packed.bundle.BundleAssembly;
import app.packed.bundle.Wirelet;

public interface RestartableApp<T> {

    void restart();
    
    static <T> RestartableApp<T> of(BiFunction<BundleAssembly , Wirelet[], T> action, BundleAssembly  assembly, Wirelet... wirelets) {
        // Vi smider maaske en special BuildException som vi kan catche
        throw new UnsupportedOperationException();
    }

}
