package app.packed.build.usage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.application.programs.SomeApp;
import app.packed.bean.BeanMirror;
import app.packed.build.ApplyBuildHook;
import app.packed.bundle.Bundle;
import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.sandbox.BundleHook;
import app.packed.component.ComponentMirror;
import app.packed.extension.Extension;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;

class UsageLogging {

    @ApplyBuildHook(MyProc.class)
    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface EnableLogging {}

    record MyProc(Class<? extends Bundle<?>> assemblyType) implements BundleHook {

        @Override
        public void beforeBuild(BundleConfiguration configuration) {
            System.out.println("Assembly is " + assemblyType);
            
            configuration.use(ServiceExtension.class).provideInstance("hejhej");

            configuration.use(LoggingExtension.class).enable();
        }

        @Override
        public void afterBuild(BundleConfiguration configuration) {
            configuration.use(ServiceExtension.class).exportAll();
            for (ComponentMirror c : configuration.mirror().children()) {
                if (c instanceof BeanMirror b) {
                    System.out.println("WE got a bean of type " + b.beanType());
                }
            }
        }
    }

    static class LoggingExtension extends Extension {
        public void enable() {}
    }

    public static void main(String[] args) {
        ServiceContract.of(new MyOtherAss()).print();
        System.out.println("");
        SomeApp.run(new MyOtherAss());
    }

    @ApplyBuildHook(MyProc.class)
    public static class MyOtherAss extends MyAss {

        @Override
        protected void build() {
            // configuration.use(ServiceExtension.class).provideInstance("hejhej");

            install(NeedsString.class);
            installInstance("hejhej");
            installInstance("hejhej");
        }
    }

    public static class NeedsString {
        public NeedsString(String s) {
            System.out.println("GOOT " + s);
        }
    }
}
