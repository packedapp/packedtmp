package app.packed.service;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanTrigger;
import app.packed.bean.BeanTrigger.OnAnnotatedMethod;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;
import internal.app.packed.service.ServiceExportOperationHandle;

/**
 *
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@OnAnnotatedMethod(introspector = ExportBeanIntrospector.class, allowInvoke = true)
@BeanTrigger.OnAnnotatedField(introspector = ExportBeanIntrospector.class, allowGet = true)
public @interface Export {
    // Make Provide into meta annotation??
    String namespace() default "exports";
}

final class ExportBeanIntrospector extends BaseExtensionBeanIntrospector {

    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
        // Det vi godt vil sige
        if (!Modifier.isStatic(method.modifiers())) {
            if (beanKind() != BeanLifetime.SINGLETON) {
                throw new BeanInstallationException("Not okay)");
            }
        }
        ServiceExportOperationHandle.install((Export) annotation, method);
    }
}