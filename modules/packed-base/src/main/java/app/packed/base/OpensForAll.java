package app.packed.base;

import java.lang.annotation.Annotation;

import app.packed.container.Extension;

@interface AnnotationMetaMapper {
    String name();

    Class<? extends Annotation> target();
}

@interface OpensForx {
    boolean all();

    Class<? extends Extension> extension();

    String[] modules() default {};

    boolean thisModule();

}

@interface OpensForAll {

}