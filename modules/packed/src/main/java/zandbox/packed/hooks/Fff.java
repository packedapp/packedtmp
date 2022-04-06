package zandbox.packed.hooks;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Optional;

import app.packed.bean.hooks.accessors.ScopedProvide;
import app.packed.inject.service.ServiceExtension;
import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor;

public class Fff {

    public static void main(String[] args) throws Exception {
        ClassBootstrapProcessor.Builder b = new ClassBootstrapProcessor.Builder(MyClass.class);

        System.out.println("BYE");

        b.build();
    }

    static class MyClass {

        @Fooo
        @Booo
        public int i;

        @ConfigVal("hejgej")
        public Optional<List<?>> id;

        @Booo
        public static void foo() {
            System.out.println("BOOOOO");
        }
    }

    @Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @InjectAnnotatedVariableHook(onAnnotation = ConfigVal.class, extension = ServiceExtension.class, bootstrapBean = InjectVarBootstrap.class)
    public @interface ConfigVal {
        String value();
    }

    @Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @AccessibleFieldHook(onAnnotation = Fooo.class, extension = ServiceExtension.class, bootstrapBean = MyBootstrap.class)
    public @interface Fooo {}

    @Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @AccessibleFieldHook(onAnnotation = Booo.class, extension = ServiceExtension.class, bootstrapBean = MyBootstrap.class)
    @AccessibleMethodHook(onAnnotation = Booo.class, extension = ServiceExtension.class, bootstrapBean = MyMethodBootstrap.class)
    public @interface Booo {}

    static class InjectVarBootstrap extends InjectAnnotatedVariableHook.Bootstrap {

        // 1st
        //// Support one HookProvide annotation...

        // Supports multiple hook provide

        String val;

        @Override
        public void bootstrap() {
            val = getAnnotation(ConfigVal.class).value();
            // $autoConvert <-

            System.out.println(getDeclaredType());
            System.out.println(getActualType());

            System.out.println(getParameterizedType());
            System.out.println(getActualParameterizedType());
        }

        static {
            $supportWildcardTypes();

        }

        @ScopedProvide
        public String create() {
            return System.getProperty(val);
        }
    }

    static class MyBootstrap extends AccessibleFieldHook.Bootstrap {

        @Override
        public void bootstrap() {}
    }

    static class MyMethodBootstrap extends AccessibleMethodHook.Bootstrap {

        @ScopedProvide
        public String create() {
            return "COOL";
        }

        @Override
        public void bootstrap() {

        }
    }
}
