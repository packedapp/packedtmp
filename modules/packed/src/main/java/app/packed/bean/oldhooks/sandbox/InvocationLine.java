package app.packed.bean.oldhooks.sandbox;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

// Meningen er at man gemmer dem i en statisk variable

public interface InvocationLine<T> {
    
    static InvocationLine<MethodHandle> of(MethodType methodType) {
        throw new UnsupportedOperationException();
    }
}
