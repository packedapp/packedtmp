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
package app.packed.bean.hooks;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Optional;

import app.packed.application.BuildException;
import app.packed.base.Nullable;
import app.packed.bean.hooks.BeanClass.ClassHook;
import app.packed.bean.operation.OperationConfiguration;
import app.packed.hooks3.MethodHook;
import packed.internal.bean.hooks.usesite.UseSiteMethodHookModel;
import packed.internal.util.StackWalkerUtil;

/**
 * A method hooks bootstrap class is responsible for the detailed configuration of the hook. 
 * <p>
 * Implementations must have a no-args constructor.
 */
public abstract class BeanMethod {

    /** The builder used for bootstrapping. Updated by {@link UseSiteMethodHookModel}. */
    private UseSiteMethodHookModel.@Nullable Builder builder;
    
    /** Create a new bootstrap instance. */
    protected BeanMethod() {}

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
    /* Todoprivate (RealTimeBootstrap) */ protected final UseSiteMethodHookModel.Builder builder() {
        UseSiteMethodHookModel.Builder b = builder;
        if (b == null) {
            if (StackWalkerUtil.containsConstructorOf(getClass())) {
                throw new IllegalStateException(
                        "This method cannot be called from the constructor of " + getClass() + ". You will need to call this method from within #bootstrap()");
            }
            // Tror det er bootstrap af klassen for en realm
            throw new IllegalStateException("This method cannot called outside of the #bootstrap() method. Maybe you tried to call #bootstrap() directly");
        }
        return b;
    }

    public final OperationConfiguration operation() {
        return builder().operation();
    }
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
    public final void buildWith(Object instance) {
        builder().buildWith(instance);
    }

    //// Invoker.. <- first runtime
    // replaces the sidecar with another class that can be used
    // Vi kan jo faktisk generere kode her..
    // Vi kan ogsaa tillade Class instances som saa bliver instantieret..
    // Method (No) because then people would assume it was also present at runtime
    // Which would it must be present at build-time because we can exchange the runtime
    // object at build time

    public final void buildWithPrototype(Class<?> implementation, Object buildData) {
        // alternativ en store metode.. Saa kan man bruge det hvor man vil
    }

    /** Disables any further processing of the hook. */
    // or cancel
    public final void disable() {
        builder().disable();
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
     * @param <T>
     *            the type of class hook that will manage this book
     * @param classBootstrap
     *            the class hook bootstrap that will manage this hook
     * @return an instance of the specified class hook bootstrap
     * @throws IllegalArgumentException
     *             if both this bootstrap class and the specified bootstrap class is annotated with nest and their two
     *             extension types are not identical
     */
    public final <T extends BeanClass> T manageByClassHook(Class<T> classBootstrap) {
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

    public final <T extends BootstrapClassNest> T nestWith(Class<T> classBootstrap) {
        // Must be in the same module as...
        throw new UnsupportedOperationException();
    }

    // Eneste skulle vaere hvis har en common klasse.
    // som andre vil tage som argument
    public final void runWithInstance(Object instance) {
        throw new UnsupportedOperationException();
    }

    // A new instanceof of implementation will be created at build-time
    // for every matching method

    // ***** Available for injection...*** Same as @OnBuild
    // This Bootstrap obviously (Not on itself, but on buildWith)
    // BuildContext
    // Extension
    // ExtensionContext

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

    protected static final void $requireRunnableApplication() {}

    
    protected static final void $nestWithClass(Class<? extends BeanClass> methodType) {}

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
    
    // Er den her ens paa tvars af hooks???
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
    // protected static final void $supportMetaHook(Class<? extends Bootstrap> bootstrap) {}

}