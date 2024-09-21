package internal.app.packed.util.notused;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import internal.app.packed.util.MethodHandleUtil;

//app.packed.micro.MethodHandleBenchmark.testCreateEagerMethodHandle  avgt    5  527,696 ±   6,326  ns/op
//app.packed.micro.MethodHandleBenchmark.testCreateLazyMethodHandle   avgt    5  943,727 ± 287,735  ns/op
//app.packed.micro.MethodHandleBenchmark.testCreateLazyMethodHandle2  avgt    5  876,788 ± 274,766  ns/op
//app.packed.micro.MethodHandleBenchmark.testCreateLazyMethodHandle3  avgt    5  877,988 ± 258,475  ns/op
public class LazyMethodHandleFactory3 {



    // Example usage
    public static void main(String[] args) throws Throwable {
        MethodType methodType = MethodType.methodType(String.class, String.class);

        // Supplier that lazily provides the actual MethodHandle
        Supplier<MethodHandle> supplier = () -> {
            try {
                System.out.println("Initializing MethodHandle...");
                return MethodHandles.lookup().findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };

        // Create the lazy MethodHandle using the instance method
        MethodHandle lazyHandle = MethodHandleUtil.lazy(methodType, supplier);

        // Invoke the lazy MethodHandle
        String result1 = (String) lazyHandle.invokeExact("hello");
        System.out.println(result1); // Output: Initializing MethodHandle... HELLO

        String result2 = (String) lazyHandle.invokeExact("world");
        System.out.println(result2); // Output: WORLD
    }
}
