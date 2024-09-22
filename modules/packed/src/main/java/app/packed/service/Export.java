package app.packed.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanTrigger.AnnotatedFieldBeanTrigger;
import app.packed.bean.BeanTrigger.AnnotatedMethodBeanTrigger;
import app.packed.extension.BaseExtension;

/**
 *
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedMethodBeanTrigger(extension = BaseExtension.class, allowInvoke = true)
@AnnotatedFieldBeanTrigger(extension = BaseExtension.class, allowGet = true)
public @interface Export {
    // Make Provide into meta annotation??
    String namespace() default "exports";
}
