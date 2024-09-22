package app.packed.micro;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import internal.app.packed.util.MethodHandleUtil;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class MethodHandleBenchmark {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodType METHOD_TYPE = MethodType.methodType(String.class, String.class);


    // Supplier method to create the actual MethodHandle
    private MethodHandle createMethodHandle() {
        try {
            return LOOKUP.findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Setup(Level.Trial)
    public void setup() throws Throwable {
        // Eagerly initialized MethodHandle
//        eagerHandle = LOOKUP.findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
//
//        // Lazily initialized MethodHandle using LazyMethodHandleFactory
//        lazyHandle = LazyMethodHandleFactory.lazy(METHOD_TYPE, this::createMethodHandle);
//
//        lazy2Handle = LazyMethodHandleFactory2.lazy(METHOD_TYPE, this::createMethodHandle);

    }

    @Benchmark
    public MethodHandle testCreateEagerMethodHandle() throws Throwable {
        // Measures the time to create an eagerly initialized MethodHandle
        return LOOKUP.findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
    }

//    @Benchmark
//    public MethodHandle testCreateLazyMethodHandle() throws Throwable {
//        // Measures the time to create a lazy MethodHandle (without invoking it)
//        return LazyMethodHandleFactory.lazy(METHOD_TYPE, this::createMethodHandle);
//    }
//
//    @Benchmark
//    public MethodHandle testCreateLazyMethodHandle2() throws Throwable {
//        // Measures the time to create a lazy MethodHandle (without invoking it)
//        return LazyMethodHandleFactory2.lazy(METHOD_TYPE, this::createMethodHandle);
//    }

    @Benchmark
    public MethodHandle testCreateLazyMethodHandle3() throws Throwable {
        // Measures the time to create a lazy MethodHandle (without invoking it)
        return MethodHandleUtil.lazy(METHOD_TYPE, this::createMethodHandle);
    }
//
//    @Benchmark
//    public String testEagerMethodHandle() throws Throwable {
//        return (String) eagerHandle.invokeExact("benchmark");
//    }
//
//    @Benchmark
//    public String testLazy2MethodHandle() throws Throwable {
//        return (String) lazy2Handle.invokeExact("benchmark");
//    }
//
//    @Benchmark
//    public String testLazyMethodHandle() throws Throwable {
//        return (String) lazyHandle.invokeExact("benchmark");
//    }

//
//    @Benchmark
//    public String testInvokeNewEagerMethodHandle() throws Throwable {
//        // Measures the time to create and invoke an eagerly initialized MethodHandle
//        MethodHandle handle = LOOKUP.findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
//        return (String) handle.invokeExact("benchmark");
//    }
//
//    @Benchmark
//    public String testInvokeNewLazyMethodHandle() throws Throwable {
//        // Measures the time to create and invoke a lazily initialized MethodHandle
//        MethodHandle handle = LazyMethodHandleFactory.lazy(METHOD_TYPE, this::createMethodHandle);
//        return (String) handle.invokeExact("benchmark");
//    }
}
