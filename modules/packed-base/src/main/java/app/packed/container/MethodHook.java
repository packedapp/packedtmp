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
import packed.internal.component.source.ClassHookModel;
import packed.internal.component.source.MethodHookModel;

/**
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
// Taenker den ogsaa kan vaere paa ExtensionClass... Problemet er bare den ikke angiver en annotation
public @interface MethodHook {

    // Hmm skal vi bare altid specificere den...
    // Hvis man bruger andre annotationer Saa bliver der kun lavet en
    // bootstrap instance??? Nej det goer der alligevel altid...
    // Hmm IDK virker lidt underligt

    // Maaske har vi hellere noget priority??? idk
    boolean allowDecorate() default false;

    /**
     * Whether or not the extension method is allowed to invoke the method.
     * 
     * @see Bootstrap#methodHandle()
     */

    /**
     * Indicates whether the hook is allowed to invoke the target method. The default value is {@code false}.
     * <p>
     * Methods {@link Bootstrap#methodHandle()} and...  will fail with {@link UnsupportedOperationException} unless the value of this attribute is {@code true}
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

        /** The builder used for bootstrapping. Updated by {@link ClassHookModel}. */
        @Nullable
        private MethodHookModel.Builder builder;

        /** Bootstraps the hook method. */
        protected void bootstrap() {};

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

        // @ScheduleAtFixedRate + @ScheduledAtVariableRate paa samme metode hvis de har samme bootstrap
        // Saa er det aktiveret af to hooks
        // allowInvoke -> any hooks that has allowInvoke
        // Hmm what about decorate if we have some kind of order???
        // Well they will need to have the same order

        // replaces the sidecar with another class that can be used
        // Vi kan jo faktisk generere kode her..
        // Vi kan ogsaa tillade Class instances som saa bliver instantieret..
        /**
         * @param build
         *            the build object
         */
        // Build Injection
        // Bootstrap obviously (Not on itself, but on buildWith)
        // BuildContext
        // Extension
        // ExtensionContext

        //// Invoker.. <- first runtime

        // Method (No) because then people would assume it was also present at runtime
        // Which would it must be present at build-time because we can exchange the runtime
        // object at build time
        public final void buildWith(Object build) {
            builder().buildWith(build);
        }

        /** Disables any further processing of the method. */
        public final void disable() {
            builder().disable();
        }

        /**
         * Returns any extension this bootstrap class belongs to. Or empty if this bootstrap class does not belong to any
         * extension.
         * 
         * @return any extension the source is a member of of
         * @see ExtensionNest
         */
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

        /**
         * Returns an immutable set of the method hooks that activated this bootstrap class. The size of the returned set is
         * normally. Unless there are multiple method hooks that each activate the same bootstrap type. In which each all of
         * hooks are returned.
         * 
         * @return the method hook(s) that activated the bootstrap
         */
        public final Set<MethodHook> hooks(MethodHook mh) {
            // Giver vel kun mening hvis annotations skal udfyldes...
            return Set.of();
        }

        public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return builder().methodUnsafe().isAnnotationPresent(annotationClass);
        }

        public final <T extends ClassHook.Bootstrap> T manageBy(Class<T> bootstrapType) {
            requireNonNull(bootstrapType, "bootstrapType is null");
            return builder().manageBy(bootstrapType);
        }

        /**
         * Returns the method that is being processed.
         * 
         * @return the method that is being processed
         */
        public final Method method() {
            return builder().methodSafe();
        }

        /**
         * Returns a direct method handle to the method (without any intervening argument bindings or transformations that may
         * have been setup elsewhere).
         * 
         * @return a direct method handle to the method
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
    }

    /**
    *
    */
    // Vi har et build context object man kan faa injected via @Build
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
