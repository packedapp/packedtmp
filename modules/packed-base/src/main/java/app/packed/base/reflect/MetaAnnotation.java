package app.packed.base.reflect;

import java.lang.annotation.Annotation;

public @interface MetaAnnotation {
    Class<? extends Annotation>[] value();
}

//@MetaAnnotation(Dooo.class)
//@Dooo