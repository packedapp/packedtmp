package app.packed.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Throwable {
        Method test = Test.class.getMethod("test");

        MethodHandle mh = MethodHandles.lookup().findStatic(Test.class, "test", MethodType.methodType(Boolean.class));

        System.out.println(test.invoke(null) == Boolean.TRUE);

        System.out.println(mh.invoke() == Boolean.TRUE);

        printKey(new Key<String>() {});
        new Test().foor();
    }

    private void foor() {
        Key<?> k = new Key<String>() {};
        printKey(k);
        k = k.canonicalize();
        printKey(k);
    }

    private static void printKey(Key<?> k) {
        System.out.println(k + " " + k.getClass() + " " + List.of(k.getClass().getDeclaredFields()));
    }

    public static Boolean test() {
        return true;
    }
}