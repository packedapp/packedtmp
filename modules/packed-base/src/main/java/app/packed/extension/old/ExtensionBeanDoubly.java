package app.packed.extension.old;

import java.util.function.Function;

import app.packed.extension.Extension;

public interface ExtensionBeanDoubly<E extends Extension, X> {

    boolean isMissing();
    boolean isPresent();
    
    <T> T extract(Function<E, T> fromExtension, Function<X, T> fromExtensor);
    
    <T> T extractOrElse(Function<E, T> fromExtension, Function<X, T> fromExtensor, T defaultValue);
}
