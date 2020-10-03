package app.packed.service.sandbox;

import java.lang.annotation.Annotation;

import app.packed.base.OpenMode;
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

@interface OpensFor {

    boolean all() default false;

    Class<? extends Extension>[] extension() default {};

    String[] modules() default {};

    boolean thisModule() default false;

    OpenMode[] mode();
}

//AccessibleByExtension?
@interface OpensToExtension {

    Class<? extends Extension> extension();

    OpenMode[] mode();
}

//@SourceExtension(....)
//@ExtensionMember -> Extension will be able to use sources

//Kun med OpensForExtension ->