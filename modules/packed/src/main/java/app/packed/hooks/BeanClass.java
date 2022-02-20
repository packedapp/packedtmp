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
package app.packed.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;

import app.packed.base.Nullable;
import packed.internal.bean.hooks.usesite.UseSiteClassHookModel;

// Kan vi annotere Bootstrap med
// @MethodHook(annotatations = ...)
/**
 * The bootstrap class for a class hook. A new bootstrap instance will be created for every class hook that matches.
 * <p>
 * Implementations of this class must have a no-argument constructor.
 * 
 * @see ClassHook#bootstrap()
 */
public abstract class BeanClass {

    /**
     * A class hook allows for runtime
     */
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RUNTIME)
    @Documented
    public @interface ClassHook {

        // Maybe we allow injection of a Lookup object.
        // Eller ogsaa har vi metoderne direkte paa Bootstrap. Jaa
        // Tror ikke bootstrap supportere injection af noget som helst...
        // Alt er jo allerede bestemt

        // Tror ikke vi kan bruge lookup. Vi har noget @OpenForAll annoterings vaerk.
        // Og den vil lookup aldrig kunne forstaa.

        boolean allowAllAccess() default false;

        // Hvordan passer den med ConstructorHook???
        // boolean allowInstantiate() default false; <-- allows custom instantiation

        /** The hook's {@link BeanClass} class. */
        Class<? extends BeanClass> bootstrap();

        /**
         * Returns annotations
         * 
         * @return
         */
        Class<? extends Annotation>[] matchesAnnotation() default {};

        Class<?>[] matchesAssignableTo() default {};

        // Tror det er noget med vi kan filtere fields/constructor/method/...
        public interface MemberOption {

            public static MemberOption declaredOnly() {
                throw new UnsupportedOperationException();
            }
        }
    }
    // matchesAssignableTo was Inherited, Extending... men 
    
    /** The builder used for bootstrapping. Accessed by {@link UseSiteClassHookModel}. */
    private UseSiteClassHookModel.@Nullable Builder builder;

    /** Invoked by Packed at bootstrap time. */
    protected void bootstrap() {}

    /**
     * Returns a builder object for this bootstrap.
     * 
     * @return a builder object for this bootstrap
     */
    private final UseSiteClassHookModel.Builder builder() {
        UseSiteClassHookModel.Builder b = builder;
        if (b == null) {
            throw new IllegalStateException(
                    "This method cannot be called outside of the #bootstrap() method. Maybe you tried to call #bootstrap() directly");
        }
        return b;
    }

    /**
     * Returns a list of bootstraps for all of the constructors of the hooked class. If {@link #hasFullAccess()} is true,
     * {@link ConstructorHook.BeanConstructor#methodHandle()} returns a valid method handle for the constructor.
     * 
     * @return a list of bootstraps for all of the constructors of the hooked class
     */
    protected final List<BeanConstructor> constructors() {
        throw new UnsupportedOperationException();
    }

    // We need to have some kind of isGettable, isSettable paa bootstrap tror jeg...
    // Og det skal ikke inkludere om brugere har givet adgang. f.eks. med et lookup object.
    // Det er altsammen separat fra bootstrap...

    // Must use buildWith, or manageByClassBootstrap();
    protected final List<BeanField> fields() {
        return fields(false, Object.class);
    }

    protected final List<BeanField> fields(boolean declaredFieldsOnly, Class<?>... skipClasses) {
        return builder().fields(declaredFieldsOnly, skipClasses);
    }

    public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return type().getAnnotation(annotationClass);
    }

    public final Annotation[] getAnnotations() {
        return type().getAnnotations();
    }

    public final <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return type().getAnnotationsByType(annotationClass);
    }

    public final boolean hasFullAccess() {
        return builder().model.allowAllAccess;
    }

    /**
     * Returns true if an annotation for the specified type is <em>present</em> on the hooked class, else false.
     * 
     * @param annotationClass
     *            the Class object corresponding to the annotation type
     * @return true if an annotation for the specified annotation type is present on the hooked class, else false
     * 
     * @see Field#isAnnotationPresent(Class)
     */
    public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return type().isAnnotationPresent(annotationClass);
    }

    // b.setBuild();
    protected final List<BeanMethod> methods() {
        return methods(false, false, Object.class);
    }

    /**
     * @param declaredMethodsOnly
     *            whether or not to only include
     * @param ignoreDefaultMethods
     *            whether or not ignore default methods? Do we want to filter now? Maybe includeInterface is more
     *            interesting?
     * @param skipClasses
     *            classes to skip when processing
     * @return a list of method bootstraps
     */
    protected final List<BeanMethod> methods(boolean declaredMethodsOnly, boolean ignoreDefaultMethods, Class<?>... skipClasses) {
        return builder().methods(declaredMethodsOnly, skipClasses);
    }
    
    

    /**
     * Returns the class for which this bootstrap has been created.
     * 
     * @return the class for which this bootstrap has been created
     */
    public final Class<?> type() {
        return builder().type();
    }
}