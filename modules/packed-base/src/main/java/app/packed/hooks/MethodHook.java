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
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.ExtensionNest;
import packed.internal.component.source.MethodHookModel;

/**
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
// Taenker den ogsaa kan vaere paa ExtensionClass... Problemet er bare den ikke angiver en annotation
public @interface MethodHook {

    /**
     * Whether or not the extension method is allowed to invoke the method.
     * 
     * @see Bootstrap#methodHandle()
     */

    /**
     * Indicates whether the hook is allowed to invoke the target method. The default value is {@code false}.
     * <p>
     * Methods {@link Bootstrap#methodHandle()} and... will fail with {@link UnsupportedOperationException} unless the value
     * of this attribute is {@code true}
     * 
     * @return whether the hook is allowed to invoke the target method
     */
    boolean allowInvoke() default false;

    // Maybe it should be mandatory... We don't currently support method hooks that
    Class<? extends Annotation>[] annotatedWith() default {};

    /** The {@link Bootstrap} class the hook will use. */
    Class<? extends RealMethodSidecarBootstrap> bootstrap();

    /**
     * A bootstrap class that must be extended to configure how the method is processed.
     */
    abstract class Bootstrap {

        /** The builder used for bootstrapping. Updated by {@link MethodHookModel}. */
        private MethodHookModel.@Nullable Builder builder;

        /** Bootstraps the hook method. */
        protected void bootstrap() {}

        /** Disables any further processing of the method. */
        public final void disable() {
            // No I think disable... Clears out runtime and build time...
            // nulls about
            builder().disable();
        }
        
        /**
         * Returns this sidecar's builder object.
         * 
         * @return this sidecar's builder object
         */
        final MethodHookModel.Builder builder() {
            MethodHookModel.Builder c = builder;
            if (c == null) {
                throw new IllegalStateException("This method cannot called outside of the #bootstrap() method. Maybe you tried to call #bootstrap() directly");
            }
            return c;
        }
        // Build Injection
        // Bootstrap obviously (Not on itself, but on buildWith)
        // BuildContext
        // Extension
        // ExtensionContext

        public final Optional<Class<?>> buildType() {
            return Optional.empty();
        }

        //// Invoker.. <- first runtime
        // replaces the sidecar with another class that can be used
        // Vi kan jo faktisk generere kode her..
        // Vi kan ogsaa tillade Class instances som saa bliver instantieret..
        // Method (No) because then people would assume it was also present at runtime
        // Which would it must be present at build-time because we can exchange the runtime
        // object at build time
        /**
         * @param instance
         *            the build object
         * 
         * @throws IllegalStateException
         *             if called outside of the {@link #bootstrap()} method. Or if managed by another bootstrap class, outside
         *             of its bootstrap method
         * 
         */
        public final void buildWith(Object instance) {
            // Also overrides any runtime...
            builder().buildWith(instance);
        }

        /**
         * Returns any extension this bootstrap class belongs to. Or empty if this bootstrap class does not belong to any
         * extension.
         * 
         * @return any extension the source is a member of of
         * @see ExtensionNest
         */
        // Hmm, det er mest taenkt hvis vi skule passe den til nogle andre ikke?
        public final Optional<Class<? extends Extension>> extension() {
            return builder().extensionMember();
        }

        /**
         * Returns an annotated element from the method that is being bootstrapped.
         * 
         * @see AnnotatedElement#getAnnotation(Class)
         */
        // MS extends AnnotatedElement???? With meta annotations.
        // Call method if you want without them...
        public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return builder().methodUnsafe().getAnnotation(annotationClass);
        }

        public final Annotation[] getAnnotations() {
            return builder().methodUnsafe().getAnnotations();
        }

        public final <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
            return builder().methodUnsafe().getAnnotationsByType(annotationClass);
        }

        /**
         * Returns the modifiers of the method.
         * 
         * @return the modifiers of the method
         * @see Method#getModifiers()
         * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Method#getModifiers()}
         */
        public final int getModifiers() {
            return builder().methodUnsafe().getModifiers();
        }

        public boolean hasInvokePrivilidge() {
            return true;
        }

        /**
         * Returns true if an annotation for the specified type is <em>present</em> on the hooked method, else false.
         * 
         * @param annotationClass
         *            the Class object corresponding to the annotation type
         * @return true if an annotation for the specified annotation type is present on the hooked method, else false
         * 
         * @see Method#isAnnotationPresent(Class)
         */
        public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return builder().methodUnsafe().isAnnotationPresent(annotationClass);
        }

        /**
         * @param <T>
         * @param classBootstrap
         * @return an instance of the specified class hook bootstrap
         * @throws IllegalArgumentException
         *             if an extension hook and the specified class bootstrap is not from the same extension
         */
        public final <T extends ClassHook.Bootstrap> T manageWithClassHook(Class<T> classBootstrap) {
            requireNonNull(classBootstrap, "bootstrapType is null");
            return builder().manageBy(classBootstrap);
        }

        /**
         * Returns the hooked method.
         * 
         * @return the hooked method
         */
        public final Method method() {
            return builder().methodSafe();
        }

        /**
         * Returns a direct method handle to the hooked method (without any intervening argument bindings or transformations
         * that may have been configured elsewhere).
         * 
         * @return a direct method handle to the hooked method
         * @see Lookup#unreflect(Method)
         * @see MethodHook#allowInvoke()
         * @see ClassHook#allowAllAccess()
         * 
         * @throws UnsupportedOperationException
         *             if the extension method does not invocation access via {@link MethodHook#allowInvoke()}
         */
        public final MethodHandle methodHandle() {
            return builder().methodHandle();
        }

        protected static final void $ddd() {}
    }

    /**
    *
    */
    // Vi har et interface istedet for en konkret klasse fordi
    // 1. Den skal kunne injectes i metoder f.eks. paa en Bootstrap klasse
     
    
    //build context object man kan faa injected via @Build
    // istedet for en abstract klasse man implementere.
    // 1. Kan ikke overskrive baade bootstrap og build.
    // saa skal tit have 2 klasser.
    // 2. Hvis man vil have injected Extension ogsaa skal vi have noget med noget
    // annotering
    public interface BuildContext {

        void disable();

        // hvordan fungere den for pooled components???
        // Instance Sidecar...
        // Maybe specify a class
        void runWith(Object o); // skal vel ogsaa vaere paa bootstrap...
        // Men fjerner man saa @Build??? Eller saetter man bare en default....

        // hvorfor kun initialize??? vi kan ogsaa have start/stop
        // Altsaa taenker den kun er brubar hvis vi skal gemme data paa tvaers

        //
//        /**
//         * @return any
//         * @see Bootstrap#manageBy(Class)
//         */
//        Optional<Class<?>> managedBy();
    }
}

@interface Sandbox {

    /** Whether or not the extension will be activated automatically. The default is true. */
    // Skal lige have nogle use cases
    boolean activateExtension() default true;
}

// invoke...
// provide, but someone else invokes
class SandboxBootstrap {

    /**
     * Returns an immutable set of the method hooks that activated this bootstrap class. The size of the returned set is
     * normally. Unless there are multiple method hooks that each activate the same bootstrap type. In which each all of
     * hooks are returned.
     * 
     * @return the method hook(s) that activated the bootstrap
     */
    public final Set<MethodHook> activatedBy() {

        // @ScheduleAtFixedRate + @ScheduledAtVariableRate paa samme metode hvis de har samme bootstrap
        // Saa er det aktiveret af to hooks
        // allowInvoke -> any hooks that has allowInvoke
        // Hmm what about decorate if we have some kind of order???
        // Well they will need to have the same order

        // Hmm, vil vi ikke ogsaa gerne have selve annoteringerne???
        // Giver vel kun mening hvis annotations skal udfyldes...
        return Set.of();
    };

    /**
     * @return the build type
     * 
     *         // * @see #disable()
     */
    public final Optional<Class<?>> buildType() {
        // disabled -> Optional.empty

        // Can specify both
        // class
        // instance
        // prototype
        return Optional.of(getClass());
    }

    // An item that has class scope and is available at build time
    public final void manageBuildBy(Class<?> bootstrapType) {
        throw new UnsupportedOperationException();
    }

    public final <T extends ClassHook.Bootstrap> T manageBy(Class<T> bootstrapType) {
        throw new UnsupportedOperationException();
    }

    /**
     * If this method is managed by an extension class returns the Returns the ny extension class that this method is
     * managed.
     * 
     * @return any
     * @see ClassHook.Bootstrap#managedMethods()
     * @see ClassHook.Bootstrap#managedMethods(Class) // * @see Bootstrap#manageBy(Class) // * @see BuildContext#managedBy()
     */
    public final Optional<Class<? extends ClassHook.Bootstrap>> managedBy() {
        throw new UnsupportedOperationException();
    }

}

class ZOldSidecar {
    // public <T> void buildWithContext(Consumer<BuildContext> con) {}
    // public <T> void buildWithContext(T o, BiConsumer<T, BuildContext> con) {}
    //
    // public void buildWithNew(Class<? extends Build> buildType) {}
}

// Man kan ogsaa have en BuildContext som man kan faa injected...
// Why not just extended...
// Enten kan man extende build, eller faa injected BuildTimeContext
// Giver ikke
// Foerst overvejede vi at have en klasse ligesom bootstrap...
// Men den er irriterende fordi Bootstrap fx ikke kan extende begge
// Man kan hellere ikke fx faa injected Extension
