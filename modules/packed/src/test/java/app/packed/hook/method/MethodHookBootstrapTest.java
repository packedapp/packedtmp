package app.packed.hook.method;

import org.junit.jupiter.api.Test;

public class MethodHookBootstrapTest {

    @Test
    public void test() {
        
    }
//    // we do not anything that needs to be closed
//    static final ThreadLocal<BootstrapHandler> TL = new ThreadLocal<>();
//
//    @Test
//    @Disabled
//    public void foo() {
//        TL.set(new BootstrapHandler() {
//
//            @Override
//            void onBootstrapConstruction(HookTestBootstrap b) {
//                System.out.println(b.method());
//            }
//
//            @Override
//            void onBootstrap(HookTestBootstrap b) {
//                // TODO Auto-generated method stub
//                super.onBootstrap(b);
//            }
//            
//        });
//        App.run(new BaseAssembly() {
//
//            @Override
//            protected void build() {
//                install(SimpleSource.class);
//            }
//        });
//    }
//
//    @Retention(RetentionPolicy.RUNTIME)
//    @OldBeanMethodHook(bootstrap = HookTestBootstrap.class)
//    static @interface HookTest {
//        String value();
//    }
//
//    static class HookTestBootstrap extends OldBeanMethod {
//        HookTestBootstrap() {
//            TL.get().onBootstrapConstruction(this);
//        }
//
//        @Override
//        protected void bootstrap() {
//            TL.get().onBootstrap(this);
//        }
//    }
//
//    static class SimpleSource {
//
//        @HookTest("foo")
//        public void ff() {
//
//        }
//    }
//
//    static class BootstrapHandler {
//
//        void onBootstrapConstruction(HookTestBootstrap b) {
//
//        }
//
//        void onBootstrap(HookTestBootstrap b) {
//
//        }
//    }

}
