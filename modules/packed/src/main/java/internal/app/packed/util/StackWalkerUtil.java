package internal.app.packed.util;

import java.lang.StackWalker.Option;

public class StackWalkerUtil {
    public static final StackWalker SW = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    // Maybe we want to take an additional parameter. That says stop here..
    // Typically we know some class on stack that makes the actual call
    public static boolean containsConstructorOf(Class<?> clazz) {
        return SW.walk(s -> s.anyMatch(f -> f.getDeclaringClass() == clazz && f.getMethodName().equals("<init>")));
    }

    public static boolean inConstructorOfSubclass(Class<?> clazz) {
        SW.forEach(c->{
            IO.println(c.getMethodName());
        });
        //Object o = SW.walk(s -> s.anyMatch(f -> IO.println(f.getMethodName());return null;));
        return SW.walk(s -> s.anyMatch(f -> clazz.isAssignableFrom(f.getDeclaringClass()) && f.getMethodName().equals("<init>")));
    }
}
