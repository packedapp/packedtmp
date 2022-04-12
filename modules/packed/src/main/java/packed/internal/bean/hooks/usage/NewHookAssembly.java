package packed.internal.bean.hooks.usage;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.application.App;
import app.packed.bean.hooks.BeanField;
import app.packed.bean.hooks.BeanInfo;
import app.packed.bean.hooks.BeanMethod;
import app.packed.container.BaseAssembly;
import app.packed.container.BuildWirelets;
import app.packed.extension.Extension;

public class NewHookAssembly extends BaseAssembly {

    @Override
    protected void build() {
        install(My.class);
//        provideInstance("qweqwe");
//        // Det kan man ikke pga af lifecycle annotations
//        // beanExtension.newAlias(ContainerBean.class).exportAs()
//
//        install(new Factory1<String, @Tag("asd") String>(e -> e) {});

    }

    public static void main(String[] args) {
        App.run(new NewHookAssembly(), BuildWirelets.spyOnWire(c -> System.out.println(c.path())));
    }

    public static class My {

        @Kaaa
        private static String ss = "123123";
        
        @Kaaa
        public void foo() {
            
        }
    }

    public static class MyExt extends Extension<MyExt> {

        @Override
        protected void hookOnBeanBegin(BeanInfo beanInfo) {
            System.out.println("Begin for bean");
        }

        @Override
        protected void hookOnBeanField(Class<? extends Annotation> annotation, BeanField field) {
            System.out.println(annotation);
            System.out.println("Reading field " + field.varHandle().get());
        }

        @Override
        protected void hookOnBeanMethod(Class<? extends Annotation> annotation, BeanMethod method) {
            System.out.println(annotation);
            System.out.println("Reading Method " + method.methodHandle());
        }

        
        @Override
        protected void hookOnBeanEnd(BeanInfo beanInfo) {
            System.out.println("End for bean");
        }

    }

    @Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @BeanField.Hook(extension = MyExt.class)
    @BeanMethod.Hook(extension = MyExt.class)
    public @interface Kaaa {}
}
