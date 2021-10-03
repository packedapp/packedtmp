package app.packed.bean.hooks.customization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.hooks.BeanHook;
import app.packed.bean.hooks.BeanMethod;
import app.packed.bundle.BaseBundle;
import app.packed.inject.service.Provide;

/**
 *
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BeanHookMapper {
    Class<?> from();

    BeanHook to(); // default null -> Remove it
}
//@BeanHookMapper(from = Provide.class, to = ....)
//@BeanHookMapper(from = SomeInterface.class, to = ....)

// Her har vi specifikt ingen extension...

// Altsaa mapper vi ogsaa sub klasses and super klasses??? Hvis man har adgang til super klasser vil jeg mene.
// For sub klasses probably always
@BeanHookMapper(from = Provide.class, to = @BeanHook(methodAnnotatedAccessible = MyHook.class))
abstract class SomeAssembly extends BaseBundle {}

class MyHook extends BeanMethod {

    @Override
    protected void bootstrap() {
        System.out.println("Method : " + method());

    }
}
