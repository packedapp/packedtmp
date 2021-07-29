package app.packed.bean.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import app.packed.extension.Extension;

// Hvorfor skal man ikke bare annotere en bean med den??? Fordi vi ikke har mapningen @Provide->Hook
// Bliver noedt til at en slags BeanHookConfiguration klasse man kan apply'

// Kunne have en @MapAnnotation(Provide.class, @ApplyBeanHook)


// mapAnnotatedMethodToAccesibleMethod(Class<? extends Annotation>, SomeHook);
// /mapAnnotatedMethodToMethod(Class<? extends Annotation>, SomeHook);


@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
public @interface ApplyBeanHook {

    Class<? extends Extension> extension() default Extension.class;

    // field, fieldGettable, fieldSettable, fieldAccessible
    // method, methodAccessible

    Class<? extends BeanMethodHook>[] method() default {};
    Class<? extends BeanMethodHook>[] methodAccessible() default {};
}
// Maaske har vi bare en BeanMethodHook klasse. Og saa smider methodHandle() UnsupportedOperation 