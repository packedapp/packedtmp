package app.packed.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.extension.BaseExtension;
import app.packed.extension.ExtensionMetaHook.AnnotatedBeanFieldHook;
import app.packed.extension.ExtensionMetaHook.AnnotatedBeanMethodHook;

/**
 *
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedBeanMethodHook(extension = BaseExtension.class, allowInvoke = true)
@AnnotatedBeanFieldHook(extension = BaseExtension.class, allowGet = true)
public @interface Export {

    // Overrides the return type
    // This attribute is primarily used
    Class<?> as() default FromMethodSignature.class;
}

final class FromMethodSignature {}

////  ProvisionMode[] mode() default {}; // Use value from @Provide, if no @Provide and empty = On_DEMAND, otherwise use single value
//
//// If empty it takes the value of @Provide
//// Used standalone either this single value or defaults to Prototype
//// Doo<int, String>
//// Nahhh. Hvor tit eksportere man parameterized typer???
//Class<?>[] parameters() default {};
// // Problemet er den constant... Vi bliver ogsaa noedt til at have den her...
// Hvis den skal bruges standalone
// Og ahh det er aandsvagt at skulle specificere den 2 steder...
// Saa syntes vi skal have en exported=true paa Provide

// Problemet er saa at man ikke kun kan exportere...
// med mindre man bruger mode = ProvideOnly, ExportOnly, Both
// Hvilket virker langt mere kompliceret

// Extensions cannot export services...