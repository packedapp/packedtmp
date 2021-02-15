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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import app.packed.base.Nullable;
import packed.internal.component.source.ClassHookModel;

/**
 *
 */
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

    /** The sidecar that is activated. */
    Class<? extends ClassHook.Bootstrap> bootstrap();

    // Must have
    Class<? extends Annotation>[] matchesAnnotation() default {};
    
    Class<?>[] matchesAssignableTo() default {};
    
    // Kan vi annotere Bootstrap med 
    // @MethodHook(annotatations = ...)
    public abstract class Bootstrap {

        /** The builder used for bootstrapping. Updated by {@link ClassHookModel}. */
        private ClassHookModel.@Nullable Builder builder;

        protected void bootstrap() {}
        
        /**
         * Returns this sidecar's builder object.
         * 
         * @return this sidecar's builder object
         */
        final ClassHookModel.Builder builder() {
            ClassHookModel.Builder c = builder;
            if (c == null) {
                throw new IllegalStateException("This method cannot called outside of the #bootstrap() method. Maybe you tried to call #bootstrap() directly");
            }
            return c;
        }

        protected final List<ConstructorHook.Bootstrap> constructors() {
            throw new UnsupportedOperationException();
        }

        // We need to have some kind of isGettable, isSettable paa bootstrap tror jeg...
        // Og det skal ikke inkludere om brugere har givet adgang. f.eks. med et lookup object.
        // Det er altsammen separat fra bootstrap...
        
        // Must use buildWith, or manageByClassBootstrap();
        protected final List<FieldHook.Bootstrap> fields() {
            return fields(false, Object.class);
        }

        protected final List<FieldHook.Bootstrap> fields(boolean declaredFieldsOnly, Class<?>... skipClasses) {
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
            return builder().bootstrapModel.allowAllAccess;
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

        /**
         * Returns a list of all methods that are explicitly managed by this bootstrap instance.
         * 
         * @return a list of all methods that are explicitly managed by this bootstrap instance
         * @see FieldHook.Bootstrap#manageBy(Class)
         * @see MethodHook.Bootstrap#manageByClassHook(Class)
         */
        // Tror maaske det er godt at specificere type... dvs ikke denne metode...
        // Fordi saa kan vi checke at det er samme extension
        protected final List<MethodHook.Bootstrap> managedMethods() {
            return managedMethods(MethodHook.Bootstrap.class);
        }

        protected final <T extends MethodHook.Bootstrap> List<T> managedMethods(Class<T> type) {
            throw new UnsupportedOperationException();
        }

        // b.setBuild();
        protected final List<MethodHook.Bootstrap> methods() {
            return methods(false, false, Object.class);
        }
        
        /**
         * @param declaredMethodsOnly
         *            whether or not to only include
         * @param ignoreDefaultMethods
         *            whether or not ignore default methods? Do we want to filter now? Maybe includeInterface is more interesting?
         * @param skipClasses
         *            classes to skip when processing
         * @return a list of method bootstraps
         */
        protected final List<MethodHook.Bootstrap> methods(boolean declaredMethodsOnly, boolean ignoreDefaultMethods, Class<?>... skipClasses) {
            return builder().methods(declaredMethodsOnly, skipClasses);
        }

        /**
         * Returns the class that is being bootstrapped.
         * 
         * @return the class that is being bootstrapped
         */
        public final Class<?> type() {
            throw new UnsupportedOperationException();
        }
    }
    
    // Tror det er noget med vi kan filtere fields/constructor/method/...
    public interface MemberOption {
        
        public static MemberOption declaredOnly() { 
            throw new UnsupportedOperationException();
        }
    }
}
// matchesAssignableTo was Inherited, Extending... men 