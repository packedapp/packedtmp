package app.packed.bean.hooks.usage;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.hooks.BeanField;
import app.packed.bean.hooks.BeanHook;
import app.packed.bean.hooks.BeanMethod;
import app.packed.extension.ExtensionMember;
import app.packed.service.ServiceExtension;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented

@ExtensionMember(ServiceExtension.class)
@BeanHook(methodAnnotatedAccessible = MyMBoot.class, fieldAnnotatedSettable = MyFBoot.class)
public @interface NewProvie {}

class MyMBoot extends BeanMethod {}

class MyFBoot extends BeanField {}
