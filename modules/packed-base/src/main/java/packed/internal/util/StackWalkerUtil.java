package packed.internal.util;

import java.lang.StackWalker.Option;

public class StackWalkerUtil {
    public static final StackWalker SW = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    // Maybe we want to take an additional parameter. That says stop here..
    // Typically we know some class on stack that makes the actual call
    public static boolean containsConstructorOf(Class<?> clazz) {
        return SW.walk(s -> s.anyMatch(f -> f.getDeclaringClass() == clazz && f.getMethodName().equals("<init>")));
    }
}
