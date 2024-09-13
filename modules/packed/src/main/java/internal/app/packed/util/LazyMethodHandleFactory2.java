package internal.app.packed.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.WrongMethodTypeException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public record LazyMethodHandleFactory2(MutableCallSite callSite, Supplier<MethodHandle> supplier, AtomicReference<MethodHandle> actualHandleRef) {

    // Store the initialize method handle in a static final field
    private static final MethodHandle INIT_METHOD_HANDLE;

    static {
        try {
            INIT_METHOD_HANDLE = MethodHandles.lookup().findVirtual(LazyMethodHandleFactory2.class, "initialize",
                    MethodType.methodType(Object.class, Object[].class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static MethodHandle lazy(MethodType mt, Supplier<MethodHandle> supplier) {

        // Create the necessary components
        MutableCallSite callSite = new MutableCallSite(mt);
        AtomicReference<MethodHandle> actualHandleRef = new AtomicReference<>();

        // Instantiate the LazyMethodHandleFactory record
        LazyMethodHandleFactory2 factory = new LazyMethodHandleFactory2(callSite, supplier, actualHandleRef);

        // Bind 'this' to the method handle since initialize is an instance method
        MethodHandle initMethodHandle = INIT_METHOD_HANDLE.bindTo(factory);

        // Adjust the handle to collect arguments into an Object[]
        initMethodHandle = initMethodHandle.asCollector(Object[].class, mt.parameterCount()).asType(mt);

        // Set the MutableCallSite's target to the initialization handle
        callSite.setTarget(initMethodHandle);

        // Return the dynamic invoker of the call site
        return callSite.dynamicInvoker();
    }

    // The initialization method that will be called on the first invocation
    public Object initialize(Object[] args) throws Throwable {
        MethodHandle actualHandle = actualHandleRef.get();

        if (actualHandle == null) {
            synchronized (actualHandleRef) {
                actualHandle = actualHandleRef.get();
                if (actualHandle == null) {
                    // Obtain the actual MethodHandle from the supplier
                    actualHandle = supplier.get();

                    // Validate that the method types match
                    if (!actualHandle.type().equals(callSite.type())) {
                        throw new WrongMethodTypeException("Supplier returned a MethodHandle with wrong type");
                    }

                    // Set the actual handle in the AtomicReference
                    actualHandleRef.set(actualHandle);

                    // Update the MutableCallSite to point to the actual MethodHandle
                    callSite.setTarget(actualHandle);
                }
            }
        }

        // Invoke the actual MethodHandle with the provided arguments
        return actualHandle.invokeWithArguments(args);
    }

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
        MethodHandle lazyHandle = LazyMethodHandleFactory2.lazy(methodType, supplier);

        // Invoke the lazy MethodHandle
        String result1 = (String) lazyHandle.invokeExact("hello");
        System.out.println(result1); // Output: Initializing MethodHandle... HELLO

        String result2 = (String) lazyHandle.invokeExact("world");
        System.out.println(result2); // Output: WORLD
    }
}
