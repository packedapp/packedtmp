package app.packed.bean;

import java.lang.invoke.MethodHandle;

public interface MethodHandleBuilder {

    Class<?> currentType();

    int currentIndex();

    MethodHandleBuilder add(Class<?> type); // returns index

    MethodHandleBuilder returnType(Class<?> type); // alternativ build(Class<?>)

    MethodHandle build();
}
