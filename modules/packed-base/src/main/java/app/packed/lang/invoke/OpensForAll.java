package app.packed.lang.invoke;

import java.lang.annotation.Annotation;

@interface OpensForAll {

}

@interface AnnotationMetaMapper {
    Class<? extends Annotation> target();

    String name();
}
