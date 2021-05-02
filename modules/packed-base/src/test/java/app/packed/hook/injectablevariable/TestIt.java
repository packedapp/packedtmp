package app.packed.hook.injectablevariable;

import org.junit.jupiter.api.Test;

public class TestIt {

    @Test
    public void foo() {
        System.out.println("SDASD");
    }

//    @Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD })
//    @Retention(RetentionPolicy.RUNTIME)
//    @Documented
//    @InjectableVariableHook(onAnnotation = ConfigVal.class, extension = ServiceExtension.class, bootstrap = InjectVarBootstrap.class)
//    public @interface IVH {
//        String value();
//    }
//
//    public static class InjectVarBootstrap extends InjectableVariableHook.Bootstrap {
//
//        @Override
//        protected void bootstrap() {
//
//        }
//
//    }
}
