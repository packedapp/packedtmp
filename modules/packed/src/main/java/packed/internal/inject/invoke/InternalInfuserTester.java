package packed.internal.inject.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.stream.IntStream;

import app.packed.bean.BeanDependency;
import app.packed.container.Extension;
import app.packed.inject.Provide;
import app.packed.inject.service.ServiceExtension;
import packed.internal.inject.service.runtime.ServiceRegistry;
import packed.internal.util.LookupUtil;

public class InternalInfuserTester {
    /** A handle for invoking {@link Extension#onContainerLinkage()}. */
    static final MethodHandle MH_INJECT_PARENT = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Secret.class, "l", Long.class);

    static final MethodHandle MH_INT_STREAM_CHARS = LookupUtil.lookupVirtualPublic(String.class, "chars", IntStream.class);

    InternalInfuserTester(IntStream i, Secret s, ServiceRegistry ic, Long ll) {
        System.out.println(ic.keys());
        System.out.println(i);
        System.out.println(s);
        System.out.println(ll);
        ic.forEach(sss -> {
            System.out.println(sss);
        });
    }

    public static void main(String[] args) throws Throwable {
            ClassValue<String> cv = new ClassValue<String>() {

                @Override
                protected String computeValue(Class<?> type) {
                    // TODO Auto-generated method stub
                    return null;
                }
            };
            System.out.println(cv.get(String.class));
        
        InternalInfuser.Builder builder = InternalInfuser.builder(MethodHandles.lookup(), InternalInfuserTester.class, String.class, Secret.class);
        builder.provide(IntStream.class).invokeExact(MH_INT_STREAM_CHARS, 0);
        builder.provideHidden(Secret.class).adaptArgument(1);
        builder.provideHidden(Long.class).invokeExact(MH_INJECT_PARENT, 1);
        MethodHandle mh = builder.findConstructor(InternalInfuserTester.class, e -> new IllegalArgumentException(e));

        InternalInfuserTester it = (InternalInfuserTester) mh.invokeExact("sdf", new Secret());
        System.out.println("Bte " + it);
    }

    public static class Secret {

        public Long l() {
            return 12L;
        }
    }

    @BeanDependency.ProvisionHook(extension = ServiceExtension.class)
    interface XX {

        // Det er super smart at man ikke skal lave en ny klasse...
        // Men maaske lidt for smart
        @Provide
        private static XX provide() {
            return new XX() {};
        }
    }
}
