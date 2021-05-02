package packed.internal.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.stream.IntStream;

import app.packed.container.Extension;
import app.packed.hooks.sandbox2.OldAutoService;
import app.packed.inject.InjectionContext;
import app.packed.inject.Provide;
import packed.internal.util.LookupUtil;

public class InfuserTester {
    /** A handle for invoking {@link Extension#onContainerLinkage()}. */
    static final MethodHandle MH_INJECT_PARENT = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Secret.class, "l", Long.class);

    static final MethodHandle MH_INT_STREAM_CHARS = LookupUtil.lookupVirtualPublic(String.class, "chars", IntStream.class);

    InfuserTester(IntStream i, Secret s, InjectionContext ic, Long ll) {
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
        
        Infuser.Builder builder = Infuser.builder(MethodHandles.lookup(), InfuserTester.class, String.class, Secret.class);
        builder.provide(IntStream.class).invokeExact(MH_INT_STREAM_CHARS, 0);
        builder.provideHidden(Secret.class).adaptArgument(1);
        builder.provideHidden(Long.class).invokeExact(MH_INJECT_PARENT, 1);
        MethodHandle mh = builder.findConstructor(InfuserTester.class, e -> new IllegalArgumentException(e));

        InfuserTester it = (InfuserTester) mh.invokeExact("sdf", new Secret());
        System.out.println("Bte " + it);
    }

    public static class Secret {

        public Long l() {
            return 12L;
        }
    }

    @OldAutoService
    interface XX {

        @Provide
        private static XX provide() {
            return new XX() {};
        }
    }
}
