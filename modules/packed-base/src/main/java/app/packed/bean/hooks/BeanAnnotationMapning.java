package app.packed.bean.hooks;

import java.lang.annotation.Annotation;

// was MapBeanAnnotation
public @interface BeanAnnotationMapning {
    Class<? extends Annotation> annotation();
    ApplyBeanHook to();
}
