package app.packed.base.reflect;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;

import app.packed.base.reflect.MetaAnnotation.Many;

@Repeatable(Many.class)
public @interface MetaAnnotation {

    Class<? extends Annotation> value();

    String[] mapAttribute() default {}; // Foo , Foo->App

    @interface Many {
        MetaAnnotation[] value();
    }
}

//@MetaAnnotation(Dooo.class)
//@Dooo