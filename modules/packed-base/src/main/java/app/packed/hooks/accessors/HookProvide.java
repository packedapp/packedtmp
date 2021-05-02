package app.packed.hooks.accessors;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Problemet er jeg syntes ikke vi skal aktivere ServiceExtension... Hvad provide jo goer
/**
 * Provides a service to the target of a hook.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@AccessibleMethodHook(onAnnotation = Fooo.class, extension = ServiceExtension.class, bootstrap = MyBootstrap.class)
public @interface HookProvide {

}

// Paa en eller anden maade Vil den gerne have fat i BootstrapLoader klassen???

// hvad onHook goer er jo ret forskelligt ang hvilken klasse den extender...

//class MyBootstrap extends AccessibleMethodHook.Bootstrap {
//
//    public void bootstrap() {
//        System.out.println("Bootstrap method " + method());
//    }
//}