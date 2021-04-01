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
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import app.packed.base.ComposedAnnotation;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.Composer;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.exceptionhandling.BuildException;
import app.packed.hooks.sandbox.InstanceHandle;
import app.packed.inject.Provide;
import app.packed.state.OnInitialize;
import packed.internal.hooks.usesite.UseSiteMethodHookModel;
import packed.internal.util.StackWalkerUtil;

/**
 * A method hook allows for run-time customization of methods.
 * <p>
 * This annotation can be applied the following places:
 * 
 * On an annotation with target Method... The annotation must declare itself in {@link #matchesAnnotation()}
 * 
 * On a subclass of {@link Assembly} (will not match components that part of an extension (different realm))
 * 
 * On a subclass of {@link Composer}
 * 
 * On a subclass of {@link Extension} (Only target classes
 * 
 * On a class used as a component source
 * 
 * On a meta annotation which can then be applied on one of the above targets.
 * <p>
 * In order to be usable with {@link ComposedAnnotation}, this annotation has ElementType.ANNOTATION_TYPE among its
 * targets.
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
@Repeatable(MethodHook.All.class)
@Documented
// Tag noget tekst fra den nederste comment
// https://stackoverflow.com/questions/4797465/difference-between-hooks-and-abstract-methods-in-java
public @interface MethodHook {

    /**
     * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
     * <p>
     * Methods such as {@link Bootstrap#methodHandle()} and... will fail with {@link UnsupportedOperationException} unless
     * the value of this attribute is {@code true}.
     * 
     * @return whether or not the implementation is allowed to invoke the target method
     * 
     * @see Bootstrap#methodHandle()
     */
    boolean allowInvoke() default false; // allowIntercept...

    /** The hook's {@link Bootstrap} class. */
    Class<? extends MethodHook.Bootstrap>[] bootstrap();

    /** Any extension this hook is part of. */
    // I think it is okay to require that. We could have an InternalExtension.class for our own special hooks
    // And then have Extension.class for users... Which would then be the default for our own non-extension hooks. fx @Main
    // Maaske kan man bare ikke have invoking user method hooks...
    Class<? extends Extension> extension(); // maybe just have it as an array with defaults...

    /** Any annotations that activates the method hook. */
    Class<? extends Annotation>[] matchesAnnotation() default {};

    /**
     * An annotation that allows for placing multiple {@linkplain MethodHook @MethodHook} annotations on a single target. Is
     * typically used to define meta annotations with multiple method hook annotations.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Inherited
    @Documented
    @interface All {

        /** An array of {@linkplain MethodHook @MethodHook} declarations. */
        MethodHook[] value();
    }

    /**
     * A method hooks bootstrap class is responsible for the detailed configuration of the hook. And one must always be set
     * via {@link MethodHook#bootstrap()).
     * <p>
     * Implementations must have a no-args constructor.
     */
    abstract class Bootstrap {

        /** The builder used for bootstrapping. Updated by {@link UseSiteMethodHookModel}. */
        private UseSiteMethodHookModel.@Nullable Builder builder;

        /** Create a new bootstrap instance. */
        protected Bootstrap() {}

        /** Bootstrap the hook for a matching method. */
        protected void bootstrap() {}

        /**
         * Returns this hook's builder object.
         * 
         * @return this hook's builder object
         * @throws IllegalStateException
         *             if called after the methods declaring class has been bootstrapped or from the constructor of the
         *             bootstrap class.
         */
        /* Todoprivate (RealTimeBootstrap) */ final UseSiteMethodHookModel.Builder builder() {
            UseSiteMethodHookModel.Builder b = builder;
            if (b == null) {
                if (StackWalkerUtil.containsConstructorOf(getClass())) {
                    throw new IllegalStateException("This method cannot be called from the constructor of " + getClass()
                            + ". You will need to call this method from within #bootstrap()");
                }
                // Tror det er bootstrap af klassen for en realm
                throw new IllegalStateException("This method cannot called outside of the #bootstrap() method. Maybe you tried to call #bootstrap() directly");
            }
            return b;
        }

        //// Invoker.. <- first runtime
        // replaces the sidecar with another class that can be used
        // Vi kan jo faktisk generere kode her..
        // Vi kan ogsaa tillade Class instances som saa bliver instantieret..
        // Method (No) because then people would assume it was also present at runtime
        // Which would it must be present at build-time because we can exchange the runtime
        // object at build time
        /**
         * Replaces this bootstrap with the specified instance at build-time (and run-time).
         * 
         * @param instance
         *            the instance to replace this bootstrap with
         * 
         * @throws IllegalStateException
         *             if called outside of the {@link #bootstrap()} method. Or if managed by another bootstrap class, outside
         *             of its bootstrap method
         * 
         */
        // This should probably fail if not annotated with @BuildHook
        public final void buildWithInstance(Object instance) {
            builder().buildWith(instance);
        }

        // Can take this bootstrap instance...
        public final void buildWithPrototype(Class<?> implementation) {
            // IDK
            throw new UnsupportedOperationException();
        }

        public final void buildWithPrototype(Class<?> implementation, Object buildData) {
            // alternativ en store metode.. Saa kan man bruge det hvor man vil
        }

        /** Disables any further processing of the hook. */
        // or cancel
        public final void disable() {
            builder().disable();
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

        public final boolean hasInvokeAccess() {
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
            // TODO process meta annotations...
            return builder().methodUnsafe().isAnnotationPresent(annotationClass);
        }

        /**
         * @param <T>
         *            the type of class hook that will manage this book
         * @param classBootstrap
         *            the class hook bootstrap that will manage this hook
         * @return an instance of the specified class hook bootstrap
         * @throws IllegalArgumentException
         *             if both this bootstrap class and the specified bootstrap class is annotated with nest and their two
         *             extension types are not identical
         */
        public final <T extends ClassHook.Bootstrap> T manageByClassHook(Class<T> classBootstrap) {
            requireNonNull(classBootstrap, "classBootstrap is null");
            return builder().manageBy(classBootstrap);
        }

        /**
         * Returns the matching method.
         * 
         * @return the matching method
         */
        public final Method method() {
            return builder().methodSafe();
        }

        /**
         * Returns a direct method handle to the {@link #method()} (without any intervening argument bindings or transformations
         * that may have been configured elsewhere).
         * 
         * @return a direct method handle to the matching method
         * @see Lookup#unreflect(Method)
         * @see MethodHook#allowInvoke()
         * @see ClassHook#allowAllAccess()
         * 
         * @throws UnsupportedOperationException
         *             if invocation access has not been granted via {@link MethodHook#allowInvoke()}
         */
        public final MethodHandle methodHandle() {
            return builder().methodHandle();
        }

        // A new instanceof of implementation will be created at build-time
        // for every matching method

        // ***** Available for injection...*** Same as @OnBuild
        // This Bootstrap obviously (Not on itself, but on buildWith)
        // BuildContext
        // Extension
        // ExtensionContext

        // Eneste skulle vaere hvis har en common klasse.
        // som andre vil tage som argument
        public final void runWithInstance(Object instance) {
            throw new UnsupportedOperationException();
        }

        public final void runWithPrototype(Class<?> implementation) {
            throw new UnsupportedOperationException();
        }

        protected static final void $addDataClass(Class<?> clazz) {}

        /**
         * Ignore default methods. A bootstrap instance will not be created for any methods that are default methods.
         * 
         * @throws IllegalStateException
         *             if called from outside of the static initializer block of a bootstrap class.
         */
        protected static final void $failOnInstanceMethods() {}

        // I think I like require better.. 3 words instead of 4
        // and
        // requireStaticModifier
        /**
         * 
         * Will fail with {@link BuildException}.
         */
        protected static final void $failOnStaticMethods() {}

        /**
         * Ignore default methods. No bootstrap instance will be created for default methods.
         * 
         * @throws IllegalStateException
         *             if called from outside of the static initializer block of a bootstrap class.
         */
        protected static final void $ignoreDefaultMethods() {}

        // @Inject, @Get, ...
        protected static final void $inputMethod() {}

        // @Provide
        // Maybe input it default, and you need to call output
        protected static final void $outputMethod() {}

//        // Hmmm skal det vaere paa annoteringen istedet for...
//        protected static final void $supportApplicationShell(Class<? extends Bootstrap> bootstrap) {
//            // hook.isApplicationShell
//
//            // Det er her den er grim...
//            // Vi er i samme container...
//            // Men saa alligevel ikke
//            // Maa vaere en special case...
//
//            // Hvis comp is parent and wirelet
//        }

        // Do we need a new bootstrap??? I'm mostly worried about injection...
        // Can always handle a few if/elses/...
        // @Path("${component.id}/")
        // Altsaa vi kan jo altid bare kalde WebSubextension.add...
     //   protected static final void $supportMetaHook(Class<? extends Bootstrap> bootstrap) {}
    }

    /**
    *
    */
    // Vi har et interface istedet for en konkret klasse fordi
    // 1. Den skal kunne injectes i metoder f.eks. paa en Bootstrap klasse

    // build context object man kan faa injected via @Build
    // istedet for en abstract klasse man implementere.
    // 1. Kan ikke overskrive baade bootstrap og build.
    // saa skal tit have 2 klasser.
    // 2. Hvis man vil have injected Extension ogsaa skal vi have noget med noget
    // annotering
    public interface BuildContext {

        // hvordan fungere den for pooled components???
        // Instance Sidecar...
        // Maybe specify a class
//        void buildWithInstance(Object o); // skal vel ogsaa vaere paa bootstrap...
        // Men fjerner man saa @Build??? Eller saetter man bare en default....

        void disable();

        boolean isImage();

        Optional<Class<?>> runClass();

        void runWithInstance(Object instance);

        void runWithPrototype(Class<?> implementation);

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

class RBadIdeas {

    public final Optional<Class<?>> buildType() {
        // Vil ikke mene vi behoever den her.. Tror den forvirre mere end den gavner
        return Optional.empty();
    }

    public final Optional<Class<?>> runClass() {
        // problemet er at vi har baade run type og build type... det er sgu lidt forvirrende
        throw new UnsupportedOperationException();
    }

}

@interface Sandbox {

    /** Whether or not the extension will be activated automatically. The default is true. */
    // Skal lige have nogle use cases
    boolean activateExtension() default true;

    /**
     * Whether or not the extension method is allowed to invoke the method.
     * 
     * @see MethodHook.Bootstrap#methodHandle()
     */

    // activatedByPrefix = "get"
    // Maybe just MatchesName -> Compiles to pattern and matched against the method name
    // "get*"
    // Hmmmmmm Der er ikke meget sparet
    String matchesNamePrefix() default "*";

    Class<?> matchesReturnType() default Object.class;
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
    // Maybe it should be mandatory... We don't currently support method hooks that
    // maybe annotation() instead. Sounds better if we, for example, adds
    // nameStartsWith()
    // Taenker den ogsaa kan vaere paa ExtensionClass... Problemet er bare den ikke angiver en annotation
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

@ZuperSupport // same
@MethodHook(matchesAnnotation = Provide.class, extension = Extension.class, bootstrap = Zester.Scan.class)
class Zester extends BaseAssembly {

    /** {@inheritDoc} */
    // build vs assemble... Vi har jo ogsaa composer. Compose??
    // Men det er jo stadig en del af build/compose processen
    @Override
    protected void build() {}

    class Scan extends MethodHook.Bootstrap {

        @OnInitialize
        public void doo(InstanceHandle<Runnable> ih) {
            ih.instance().run();
        }

        @OnInitialize
        public void doo(Method m) {
            System.out.println(m);
        }
    }

    // public <T> void buildWithContext(Consumer<BuildContext> con) {}
    // public <T> void buildWithContext(T o, BiConsumer<T, BuildContext> con) {}
    //
    // public void buildWithNew(Class<? extends Build> buildType) {}
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ComposedAnnotation
@MethodHook(matchesAnnotation = Provide.class, extension = Extension.class, bootstrap = Zester.Scan.class)
@interface ZuperSupport {}

// Man kan ogsaa have en BuildContext som man kan faa injected...
// Why not just extended...
// Enten kan man extende build, eller faa injected BuildTimeContext
// Giver ikke
// Foerst overvejede vi at have en klasse ligesom bootstrap...
// Men den er irriterende fordi Bootstrap fx ikke kan extende begge
// Man kan hellere ikke fx faa injected Extension
