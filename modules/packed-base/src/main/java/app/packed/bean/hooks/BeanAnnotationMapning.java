package app.packed.bean.hooks;

import java.lang.annotation.Annotation;

// was BeanHookMapper

// @BeanHookMapper(from = Provide.class, to = ....)
//@BeanHookMapper(from = SomeInterface.class, to = ....)

public @interface BeanAnnotationMapning {

    // Class<?> + BeanHookMapper instead?? Then we only n
    Class<? extends Annotation> annotation();

    ApplyBeanHook to();
}
