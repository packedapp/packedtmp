package app.packed.container.usage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.application.App;
import app.packed.bean.BeanMirror;
import app.packed.component.ComponentMirror;
import app.packed.container.Assembly;
import app.packed.container.AssemblySetup;
import app.packed.container.ContainerConfiguration;
import app.packed.extension.Extension;
import app.packed.inject.service.ServiceContract;
import app.packed.inject.service.ServiceExtension;

class UsageLogging {

    @AssemblySetup(MyProc.class)
    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface EnableLogging {}

    record MyProc(Class<? extends Assembly> assemblyType) implements AssemblySetup.Processor {

        @Override
        public void beforeBuild(ContainerConfiguration configuration) {
            System.out.println("Assembly is " + assemblyType);

            configuration.use(ServiceExtension.class).provideInstance("hejhej");

            configuration.use(LoggingExtension.class).enable();
        }

        @Override
        public void afterBuild(ContainerConfiguration configuration) {
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
        App.run(new MyOtherAss());
    }

    @AssemblySetup(MyProc.class)
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