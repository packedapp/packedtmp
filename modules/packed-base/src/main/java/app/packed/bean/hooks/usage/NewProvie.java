package app.packed.bean.hooks.usage;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.hooks.ApplyBeanHook;
import app.packed.bean.hooks.BeanMethodHook;
import app.packed.service.ServiceExtension;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApplyBeanHook(extension = ServiceExtension.class, methodAccessible = MyBoot.class)
public @interface NewProvie {

}

class MyBoot extends BeanMethodHook {

}
