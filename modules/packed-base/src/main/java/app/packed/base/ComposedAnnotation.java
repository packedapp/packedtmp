package app.packed.base;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Dem der benytter meta annotation skal kunne read hvad meta annoteringer peger paa
//@Repeatable(Many.class)

// TODOs

// Must be readable by app.packed.base. Otherwise we cannot for example.. create X.many annotations

// Must test for readability on target

// All annotations must have target = Method if meta annotations is on a method
// Else fail with ComponentDefinitionException...
// Faktisk skal vi bare lave de samme tests som

// Skal have en Repeatable annotation, hvis vi har multiple  
// Supportere ikke overrides eller andre fancy ting
// Ellers fejl

// Meta annotations er fx @Target


// Fra Spring https://github.com/spring-projects/spring-framework/wiki/Spring-Annotation-Programming-Model
// https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-meta-annotations

// Hvad med Qualifiers...
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComposedAnnotation {}
// Could have an alias annotation https://www.logicbig.com/tutorials/spring-framework/spring-web-mvc/meta-annotation.html


// Ideen er lidt at man bruger en eller anden mapper
// Til at lave de annoteringer.. Men ved ikke om man kan
// det
// Skal ihvertfald vaere visible
// Maaske kan man faa AnnotationMakers...
// For alt hvad der er visible...
@interface DynamicMetaAnnotation {
    // Mapper

    // Annotation->Annotation[] mapper...  Hvor Annotation er MetaAnnotation annotationen...
    
    // Class<? extends Annotation> value();

    String[] mapAttribute() default {}; // Foo , Foo->App

    @interface Many {
        ComposedAnnotation[] value();
    }
}
//@MetaAnnotation(Dooo.class)
//@Dooo