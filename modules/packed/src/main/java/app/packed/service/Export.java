package app.packed.service;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanLifetime;
import app.packed.bean.scanning.BeanIntrospector;
import app.packed.bean.scanning.BeanTrigger;
import app.packed.bean.scanning.BeanTrigger.OnAnnotatedMethod;
import app.packed.binding.Key;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import internal.app.packed.extension.BaseExtensionBeanIntrospector;
import internal.app.packed.operation.OperationSetup;

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

    static final OperationTemplate OPERATION_TEMPLATE = OperationTemplate.defaults().withReturnTypeDynamic();

    /**
     * Handles {@link Provide} and {@link Export}.
     *
     * {@inheritDoc}
     */
    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
        if (!Modifier.isStatic(method.modifiers())) {
            if (beanKind() != BeanLifetime.SINGLETON) {
                throw new BeanInstallationException("Not okay)");
            }
        }
        // Checks that it is a valid key
        Key<?> key = method.toKey();

        OperationSetup operation = OperationSetup.crack(method.newOperation(OPERATION_TEMPLATE).install(OperationHandle::new));
        bean().serviceNamespace().export(key, operation);
    }
}