package app.packed.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.scanning.BeanTrigger;
import app.packed.bean.scanning.BeanTrigger.OnAnnotatedMethod;
import internal.app.packed.extension.BaseExtensionHostGuestBeanintrospector;

/**
 *
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@OnAnnotatedMethod(introspector = BaseExtensionHostGuestBeanintrospector.class, allowInvoke = true)
@BeanTrigger.OnAnnotatedField(introspector = BaseExtensionHostGuestBeanintrospector.class, allowGet = true)
public @interface Export {
    // Make Provide into meta annotation??
    String namespace() default "exports";
}
