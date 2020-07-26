package old.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.UndeclaredThrowableException;

public class MethodHandleImpl {

    private static final MethodHandle ON_SPIN_WAIT_HANDLE = resolve();

    private static MethodHandle resolve() {
        try {
            MethodHandle mh = MethodHandles.publicLookup().findStatic(Thread.class, "onSpinWait", MethodType.methodType(void.class));
            return MethodHandles.filterReturnValue(mh, MethodHandles.constant(boolean.class, true));
        } catch (Exception java8Ignore) {
            return MethodHandles.constant(boolean.class, false);
        }
    }

    static boolean onSpinWait() {
        try {
            return (boolean) ON_SPIN_WAIT_HANDLE.invokeExact();
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}