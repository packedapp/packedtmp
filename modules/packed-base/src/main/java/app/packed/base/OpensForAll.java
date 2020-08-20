package app.packed.base;

import java.lang.annotation.Annotation;

@interface OpensForAll {

}

@interface AnnotationMetaMapper {
    Class<? extends Annotation> target();

    String name();
}
