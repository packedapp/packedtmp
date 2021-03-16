package packed.internal.inject.classscan.tester;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.stream.IntStream;

import app.packed.hooks.AutoService;
import app.packed.inject.InjectionContext;
import app.packed.inject.Provide;
import packed.internal.inject.classscan.Infuser;

public class InfuserTester {

    InfuserTester(IntStream i, Secret s, InjectionContext ic) {
        System.out.println(ic.keys());
        System.out.println(i);
        System.out.println(s);

        ic.forEach(sss -> {
            System.out.println(sss);
        });
    }

    public static void main(String[] args) throws Throwable {
        Infuser i = Infuser.build(MethodHandles.lookup(), c -> {
            c.provide(IntStream.class).extractPublic("chars");
            c.provideHidden(Secret.class).adapt(1);
        }, String.class, Secret.class);

        MethodHandle mh = i.findConstructorFor(InfuserTester.class);
        InfuserTester it = (InfuserTester) mh.invokeExact("sdf", new Secret());
        System.out.println("Bte ");
    }

    public static class Secret {

    }
    
    
    @AutoService
    interface XX {
        
        @Provide
        private static XX provide() {
            return new XX() {};
        }
    }
}
