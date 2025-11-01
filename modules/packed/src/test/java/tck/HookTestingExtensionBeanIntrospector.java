package tck;

import app.packed.bean.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.util.AnnotationList;

public class HookTestingExtensionBeanIntrospector extends BeanIntrospector<HookTestingExtension> {

    @Override
    public void onExtensionService(Key<?> key, OnContextService service) {
        OnVariableUnwrapped variable = service.binder();
        HookTestingExtension e = extension();
        if (e.onVariableType != null) {
            e.onVariableType.accept(key.rawType(), variable);
        } else {
            super.onExtensionService(key, service);
        }
    }

    @Override
    public void onAnnotatedField(AnnotationList annotations, OnField onField) {
        HookTestingExtension e = extension();
        if (e.onAnnotatedField != null) {
            e.onAnnotatedField.accept(annotations, onField);
        } else {
            super.onAnnotatedField(annotations, onField);
        }
    }

    @Override
    public void onAnnotatedMethod(AnnotationList hooks, BeanIntrospector.OnMethod method) {
        HookTestingExtension e = extension();
        if (e.onAnnotatedMethod != null) {
            e.onAnnotatedMethod.accept(hooks, method);
        } else {
            super.onAnnotatedMethod(hooks, method);
        }
    }
}
