package packed.internal.hooks2;

import java.lang.annotation.Annotation;

public interface AnnotatedFieldHookModel {

    /**
     * @param builder
     *            the field builder
     * @param annotation
     *            the matching annotation
     */
    void bootstrapAnnotatedField(HookUsingClass.FieldProcessor builder, Annotation annotation);
}
