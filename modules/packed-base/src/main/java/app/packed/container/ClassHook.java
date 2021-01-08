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
package app.packed.container;

import java.lang.annotation.Annotation;
import java.util.List;

import app.packed.base.Nullable;
import packed.internal.component.source.ClassHookModel;

/**
 *
 */
// SubClassHook... ClassExtendHook
public @interface ClassHook {

    // Maybe we allow injection of a Lookup object.
    // Eller ogsaa har vi metoderne direkte paa ExtensionMethodSeutp

    // Tror ikke vi kan bruge lookup. Vi har noget @OpenForAll annoterings vaerk.
    // Og den vil lookup aldrig kunne forstaa.

    boolean allowAllAccess() default false;

    // boolean allowInstantiate() default false; <-- allows custom instantiation

    /** The sidecar that is activated. */
    Class<? extends ClassHook.Bootstrap> bootstrap();

    // Must have
    Class<? extends Annotation>[] annotatedWith() default {};
    
    Class<?>[] extending() default {};
    
    Class<?>[] inheriting() default {};
    
    public abstract class Bootstrap {

        /** The builder used for bootstrapping. Updated by {@link ClassHookModel}. */
        @Nullable
        private ClassHookModel.Builder builder;

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
        
        protected void bootstrap() {}

        protected final List<ConstructorHook.Bootstrap> constructors() {
            throw new UnsupportedOperationException();
        }

        // We need to have some kind of isGettable, isSettable paa bootstrap tror jeg...
        // Og det skal ikke inkludere om brugere har givet adgang. f.eks. med et lookup object.
        // Det er altsammen separat fra bootstrap...
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

        public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return type().isAnnotationPresent(annotationClass);
        }

        /**
         * Returns a list of all methods that are explicitly managed by this bootstrap instance.
         * 
         * @return a list of all methods that are explicitly managed by this bootstrap instance
         * @see FieldHook.Bootstrap#manageBy(Class)
         * @see MethodHook.Bootstrap#manageBy(Class)
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
        
        public final boolean hasFullAccess() {
            return builder().bootstrapModel.allowAllAccess;
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
    
    public interface MemberOption {
        
        public static MemberOption declaredOnly() { 
            throw new UnsupportedOperationException();
        }
    }
}
