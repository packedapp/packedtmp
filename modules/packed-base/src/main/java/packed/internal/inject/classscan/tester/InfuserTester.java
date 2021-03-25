package packed.internal.inject.classscan.tester;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.stream.IntStream;

import app.packed.container.Extension;
import app.packed.hooks.AutoService;
import app.packed.inject.InjectionContext;
import app.packed.inject.Provide;
import packed.internal.inject.classscan.Infuser;
import packed.internal.util.LookupUtil;

public class InfuserTester {
    /** A handle for invoking {@link Extension#onContainerLinkage()}. */
    static final MethodHandle MH_INJECT_PARENT = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Secret.class, "l", Long.class);

    private InfuserTester() {
        
    }
    
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
        Infuser i = Infuser.build(MethodHandles.lookup(), c -> {
            c.provide(IntStream.class).invokePublicMethod("chars");
            c.provideHidden(Secret.class).adapt(1);
            c.provideHidden(Long.class).transform(MH_INJECT_PARENT, 1);
        }, String.class, Secret.class);

        MethodHandle mh = i.findConstructorFor(InfuserTester.class);
        
        InfuserTester it = (InfuserTester) mh.invokeExact("sdf", new Secret());
        System.out.println("Bte " + it);
    }

    public static class Secret {

        public Long l() {
            return 12L;
        }
    }
    
    
    @AutoService
    interface XX {
        
        @Provide
        private static XX provide() {
            return new XX() {};
        }
    }
}
