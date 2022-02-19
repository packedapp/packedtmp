/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.packed.inject.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;

import app.packed.base.AnnotationMaker;
import app.packed.bean.BeanExtension;
import app.packed.extension.ExtensionMember;
import app.packed.hooks.BeanField;
import app.packed.hooks.BeanFieldHook;
import app.packed.hooks.BeanMethod;
import app.packed.hooks.accessors.RealMethodSidecarBootstrap;
import packed.internal.bean.hooks.usesite.UseSiteFieldHookModel;

/**
 * An annotation indicating that an annotated type, method or field provides a object of some kind. A field
 * 
 * Or a final field.
 * 
 * Using this annotation on non-final fields are not supported. Eller.... hvad er forskellen paa at expose en metode der
 * return non-final-field;.... Maaske skriv noget med volatile og multiple threads
 * 
 * <p>
 * Both fields and methods can make used of qualifiers to specify the exact key they are made available under. For
 * example, given to two qualifier annotations: {@code @Left} and {@code @Right}<pre>
 *  &#64;Left
 *  &#64;Provide
 *  String name = "left";
 *   
 *  &#64;Right
 *  &#64;Provide
 *  String provide() {
 *      return "right";
 *  }
 *  </pre>
 * 
 * The field will is made available with the key {@code Key<@Left String>} while the method will be made available under
 * the key {@code Key<@Right String>}.
 * <p>
 * Injection is never performed on any objects provided by a annotated field or method is <b>never</b> injected. This
 * must be done manually if needed, for example, via <pre> 
 *   &#64;Provides
 *   public SomeObject provide(String name, Injector i) {
 *       SomeObject o = new SomeObject(name);
 *       i.injectMembers(o, MethodHandles.lookup());
 *       return o;
 *   }
 * </pre>
 * <p>
 * The annotation can be used on both static and non-static fields and methods. However, if you use a non-static field
 * or method you implicitly introduces a dependency to the instance of the type on which the field or method is located.
 * This is normally not a problem, however in some situations it can lead to circles in the dependency graph.
 * <p>
 * 
 * If using
 * <p>
 * Proving a null value, for example, via a null field or by returning null from a method is illegal unless the
 * dependency is optional.
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtensionMember(BeanExtension.class)
@BeanFieldHook(allowGet = true, bootstrap = ProvideFieldBootstrap.class)
@BeanMethod.Hook(allowInvoke = true, bootstrap = ProvideMethodBootstrap.class)
public @interface Provide {

    public static final AnnotationMaker<Provide> MAKER = AnnotationMaker.of(MethodHandles.lookup(), Provide.class);

    /**
     * Indicates whether or not the provided value is a constant.
     * <p>
     * Constant values are always eagerly constructed. For example, as part of the initialization of a container.
     * 
     * Constants may be cached by the runtime... I think we need to make some strong guarantees.
     * 
     * The provided does not have to be an constant in the way t. But from the perspectiv of the runtime. The instance
     * 
     * Constants may be cached
     * 
     * <p>
     * The default value is <code>false</code>
     * 
     * @return whether or not the provided value is a constant
     */
    /// Maaske har vi slet ikke constant....
    //// Maaske har vi en @ConstantProvide istedet for??? Eller Scope(Constant);
    boolean constant() default false;
}

///**
//* @return the provision mode being used
//* 
//* @implNote
//*/
//ProvisionMode mode() default ProvisionMode.ON_DEMAND;

/** A field sidecar for {@link Provide}. */
final class ProvideFieldBootstrap extends BeanField {

    /** {@inheritDoc} */
    @Override
    protected void bootstrap() {
        UseSiteFieldHookModel.getBuilder(this).provideAsService(variable().getAnnotation(Provide.class).constant());
    }
}

/** A method sidecar for {@link Provide}. */
final class ProvideMethodBootstrap extends RealMethodSidecarBootstrap {

    /** {@inheritDoc} */
    @Override
    protected void bootstrap() {
        serviceRegister(getAnnotation(Provide.class).constant());
        // new Factory<@Provide Long>(() -> 2L) {};
    }
}
