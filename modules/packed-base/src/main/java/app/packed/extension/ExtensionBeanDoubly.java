package app.packed.extension;

import java.util.function.Function;

public interface ExtensionBeanDoubly<E extends Extension, X extends ExtensionBean<E>> {

    boolean isMissing();
    boolean isPresent();
    
    <T> T extract(Function<E, T> fromExtension, Function<X, T> fromExtensor);
    
    <T> T extractOrElse(Function<E, T> fromExtension, Function<X, T> fromExtensor, T defaultValue);
}
