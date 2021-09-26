package app.packed.bean.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RUNTIME)
@Documented
//// Altsaa skal vi fjerne classX???? og saa kande den BeanMemberHook
//// Taenker alt med klasse alligevel kraver
public @interface BeanHook {

    Class<? extends BeanClass>[] classAnnotated() default {};

    Class<? extends BeanClass>[] classAnnotatedAllAccess() default {};

    Class<? extends BeanField>[] fieldAnnotated() default {};

    Class<? extends BeanField>[] fieldAnnotatedAccessible() default {};

    Class<? extends BeanField>[] fieldAnnotatedGettable() default {};

    Class<? extends BeanField>[] fieldAnnotatedSettable() default {};

    Class<? extends BeanMethod>[] methodAnnotated() default {};

    Class<? extends BeanMethod>[] methodAnnotatedAccessible() default {};

    Class<? extends BeanMethodInterceptor>[] methodAnnotatedIntercept() default {}; // interceptBefore, interceptAfter, interceptAround
}

// Taenker vi faar brug for den
enum BeanHookType {
    ANNOTATED_CLASS, ANNOTATED_CLASS_ALL_ACCESS,
}

// Fungere ikke rigtigt, da vi ikke kan putte mere end en paa klasse
//Class<? extends BeanHookCustomizer>[] customize() default {};

//Hvorfor skal man ikke bare annotere en bean med den??? Fordi vi ikke har mapningen @Provide->Hook
//Bliver noedt til at en slags BeanHookConfiguration klasse man kan apply'

//Kunne have en @MapAnnotation(Provide.class, @ApplyBeanHook)

//mapAnnotatedMethodToAccesibleMethod(Class<? extends Annotation>, SomeHook);
///mapAnnotatedMethodToMethod(Class<? extends Annotation>, SomeHook);

@interface Zandbox {

    boolean metaAnnotationsEnabled() default true; // @MetaAnnotationTarget

}

@interface Zarchive {

    // Det er jo nok primaert i forbindelse med injection de interessante
    // Tror faktisk ikke vi vil bruge dem her... Istedet for skal man bare bruge en extension
    // FooExtension.installSuperBean(Class<? extends MySuper> implementation);
    Class<? extends BeanClass>[] classSubclass() default {}; // Interfaces not supported

    Class<? extends BeanClass>[] classSubclassAllAccess() default {}; // Interfaces not supported

    // I sidste ende gav parameter ikke rigtig mening udover hvad InjectableVariable daekker over

//  Class<? extends BeanInjectableParameter>[] parameterAnnotatedInjectable() default {};
//
//  Class<? extends BeanInjectableParameter>[] parameterExactTypeInjectable() default {};
//
//  Class<? extends BeanOrFunctionInjectableVariable>[] variableAnnotatedInjectable() default {};
//
//  Class<? extends BeanOrFunctionInjectableVariable>[] variableExactTypeInjectable() default {};
}