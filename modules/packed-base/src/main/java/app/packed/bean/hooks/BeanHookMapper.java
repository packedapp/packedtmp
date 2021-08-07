package app.packed.bean.hooks;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BeanHookMapper {
    Class<?> from();

    ApplyBeanHook to();
}
//@BeanHookMapper(from = Provide.class, to = ....)
//@BeanHookMapper(from = SomeInterface.class, to = ....)
