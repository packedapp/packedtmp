package app.packed.hook.method;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.application.programs.SomeApp;
import app.packed.container.BaseAssembly;
import app.packed.hooks.BeanMethod;

public class MethodHookBootstrapTest {

    // we do not anything that needs to be closed
    static final ThreadLocal<BootstrapHandler> TL = new ThreadLocal<>();

    @Test
    @Disabled
    public void foo() {
        TL.set(new BootstrapHandler() {

            @Override
            void onBootstrapConstruction(HookTestBootstrap b) {
                System.out.println(b.method());
            }

            @Override
            void onBootstrap(HookTestBootstrap b) {
                // TODO Auto-generated method stub
                super.onBootstrap(b);
            }
            
        });
        SomeApp.run(new BaseAssembly() {

            @Override
            protected void build() {
                install(SimpleSource.class);
            }
        });
    }

    @Retention(RetentionPolicy.RUNTIME)
    @BeanMethod.Hook(bootstrap = HookTestBootstrap.class)
    static @interface HookTest {
        String value();
    }

    static class HookTestBootstrap extends BeanMethod {
        HookTestBootstrap() {
            TL.get().onBootstrapConstruction(this);
        }

        @Override
        protected void bootstrap() {
            TL.get().onBootstrap(this);
        }
    }

    static class SimpleSource {

        @HookTest("foo")
        public void ff() {

        }
    }

    static class BootstrapHandler {

        void onBootstrapConstruction(HookTestBootstrap b) {

        }

        void onBootstrap(HookTestBootstrap b) {

        }
    }

}
