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
package packed.internal.base.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationImage;
import app.packed.application.ApplicationRuntime;
import app.packed.base.Completion;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Composer;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.inject.ServiceLocator;
import app.packed.validate.Validation;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.PackedInitializationContext;
import packed.internal.inject.FindInjectableConstructor;
import packed.internal.inject.classscan.Infuser;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link ApplicationDriver}. */
public final class PackedApplicationDriver<A> implements ApplicationDriver<A> {

    /** A daemon driver. */
    public static final ApplicationDriver<Completion> DAEMON = ApplicationDriver.builder()
            .old(MethodHandles.empty(MethodType.methodType(Void.class, PackedInitializationContext.class)));

    /** The method handle used for creating new application instances. */
    private final MethodHandle mhConstructor; // (PackedInitializationContext)Object

    /** The initial set of modifiers for any system that uses this driver. */
    private final boolean needsRuntime;

    /** May contain a wirelet that will be processed _after_ any other wirelets. */
    @Nullable
    public final Wirelet wirelet;

    /**
     * Create a new application driver using the specified builder.
     * 
     * @param builder
     *            the used for construction
     */
    private PackedApplicationDriver(Builder builder) {
        this.needsRuntime = builder.needsRuntime;
        this.mhConstructor = builder.mh;
        this.wirelet = builder.prefix;
    }

    private PackedApplicationDriver(PackedApplicationDriver<A> existing, Wirelet prefix) {
        this.needsRuntime = existing.needsRuntime;
        this.mhConstructor = existing.mhConstructor;
        this.wirelet = prefix;
    }

    /** {@inheritDoc} */
    @Override
    public Component analyze(Assembly<?> assembly, Wirelet... wirelets) {
        BuildSetup build = buildFromAssembly(assembly, wirelets, true, false);
        return build.component.adaptor();
    }

    /** {@inheritDoc} */
    @Override
    public A apply(Assembly<?> assembly, Wirelet... wirelets) {
        // Build the system
        BuildSetup build = buildFromAssembly(assembly, wirelets, false, false);

        // Initialize the system. And start it if necessary (if it is a guest)
        PackedInitializationContext pic = PackedInitializationContext.process(build.component, null);

        // Return the system in a new shell
        return newApplication(pic);
    }

    /**
     * Returns the raw type of the artifacts that this driver creates.
     * 
     * @return the raw type of the artifacts that this driver creates
     */
    public Class<?> artifactRawType() {
        return mhConstructor.type().returnType();
    }

    /**
     * @param assembly
     *            the root assembly
     * @param wirelets
     *            optional wirelets
     * @param isAnalysis
     *            is it an analysis
     * @param isImage
     *            is it an image
     * @return a build setup
     */
    private BuildSetup buildFromAssembly(Assembly<?> assembly, Wirelet[] wirelets, boolean isAnalysis, boolean isImage) {

        // Extract the component driver from the assembly
        PackedComponentDriver<?> componentDriver = PackedComponentDriver.getDriver(assembly);

        // Create a new build setup
        BuildSetup build = new BuildSetup(this, assembly, componentDriver, isImage, wirelets);

        // Create the component configuration that is needed by the assembly
        ComponentConfiguration configuration = componentDriver.toConfiguration(build.component);

        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        try {
            PackedComponentDriver.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        build.close();
        return build;
    }

    /** {@inheritDoc} */
    @Override
    public <CC extends ComponentConfiguration, CO extends Composer<?>> A compose(ComponentDriver<CC> componentDriver,
            Function<? super CC, ? extends CO> composerFactory, Consumer<? super CO> consumer, Wirelet... wirelets) {
        PackedComponentDriver<CC> pcd = (PackedComponentDriver<CC>) requireNonNull(componentDriver, "componentDriver is null");
        requireNonNull(composerFactory, "composerFactory is null");
        requireNonNull(consumer, "consumer is null");

        // Create a build setup
        BuildSetup build = new BuildSetup(this, consumer, pcd, wirelets);

        CC componentConfiguration = pcd.toConfiguration(build.component);

        // Used the supplied composer factory to create a composer from a component configuration instance
        CO composer = requireNonNull(composerFactory.apply(componentConfiguration), "composerFactory.apply() returned null");

        // Invoked the consumer supplied by the end-user
        consumer.accept(composer);

        build.close();

        // Initialize the application. And start it if necessary (if it is a guest)
        PackedInitializationContext pic = PackedInitializationContext.process(build.component, null);

        // Return a new application instance
        return newApplication(pic);
    }

    public boolean needsRuntime() {
        return needsRuntime;
    }

    /**
     * Create a new application using the specified initialization context.
     * 
     * @param pic
     *            the initialization context to wrap
     * @return the new application instance
     */
    // application interface???
    private A newApplication(PackedInitializationContext pic) {
        try {
            return (A) mhConstructor.invoke(pic);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationImage<A> newImage(Assembly<?> assembly, Wirelet... wirelets) {
        BuildSetup build = buildFromAssembly(assembly, wirelets, false, true);
        return new PackedApplicationImage<>(this, build.component);
    }

    /** {@inheritDoc} */
    @Override
    public Validation validate(Assembly<?> assembly, Wirelet... wirelets) {
        // Denne metoder siger ikke noget om at alle kontrakter er fullfilled.
        // Det er fuldt ud "Lovligt" ikke at specificere alt muligt...
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDriver<A> with(Wirelet... wirelets) {
        Wirelet w = wirelet == null ? Wirelet.combine(wirelets) : wirelet.andThen(wirelets);
        return new PackedApplicationDriver<>(this, w);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDriver<A> with(Wirelet wirelet) {
        requireNonNull(wirelet, "wirelet is null");
        Wirelet w = this.wirelet == null ? wirelet : wirelet.andThen(wirelet);
        return new PackedApplicationDriver<>(this, w);
    }

    public static class Builder implements ApplicationDriver.Builder {
        MethodHandle mh;
        boolean needsRuntime = true;
        Wirelet prefix;
        boolean useShellAsSource;

        @Override
        public <S> ApplicationDriver<S> build(Lookup caller, Class<? extends S> implementation) {
            if (implementation == Void.class) {
                throw new IllegalArgumentException("Cannot specify Void.class use daemon() instead");
            }

            // We currently do not support @Provide ect... Nope...
            // Must add it as a component
            // Would just be so nice if you could do @OnStart()->application started...
            // And then they would show as "properties" on the container...

            // Altsaa hvis vi nu siger, at vi tillader injection af de services der skal bruges...
            // Og saa gemmer vi ServiceLocator til hvis det er brugere der skal bruge den...

            // Altsaa om vi bruger ServiceWirelets.provide()... eller @Provide paa application...
            // Eller om vi bruger LifecycleWirelets.onStop()... eller @OnStop

            // Create a new MethodHandle that can create artifact instances.

            // Vi har maaske en ApplicationDriver builder...

            // Saa kan evt. specificere mandatory services som skal exportes. og saa behover man ikke
            // traekke det ud af service locatoren.

            // Uhh uhhh species... Job<R> kan vi lave det???

            // Create an infuser (SomeExtension, Class)
            Infuser infuser = Infuser.build(caller, c -> {
                c.provide(Component.class).transform(PackedInitializationContext.MH_COMPONENT);
                c.provide(ServiceLocator.class).transform(PackedInitializationContext.MH_SERVICES);
                if (needsRuntime) {
                    c.provide(ApplicationRuntime.class).transform(PackedInitializationContext.MH_RUNTIME);
                }
            }, PackedInitializationContext.class);

            // Find the constructor for the subtension, only 1 constructor must be declared on the class
            Constructor<?> con = FindInjectableConstructor.constructorOf(implementation, s -> new IllegalArgumentException(s));

            mh = infuser.findConstructorFor(con, implementation);

            return new PackedApplicationDriver<>(this);
        }

        @Override
        public <A> ApplicationDriver<A> build(Lookup caller, Class<A> artifactType, MethodHandle mh) {
            // TODO fix....
            this.mh = mh;

            return new PackedApplicationDriver<>(this);
        }

        @Override
        public Builder noRuntime() {
            needsRuntime = false;
            return this;
        }

        @Override
        public <A> ApplicationDriver<A> old(MethodHandle mhNewShell) {
            mh = MethodHandles.empty(MethodType.methodType(Void.class, PackedInitializationContext.class));
            return new PackedApplicationDriver<>(this);
        }

        @Override
        public Builder useShellAsSource() {
            useShellAsSource = true;
            return this;
        }
    }

    /** Options that can be applied when creating a shell driver. */
    interface Option {

        // String Reason??? This extension has been blacklisted
        @SafeVarargs
        static Option blacklistExtensions(Class<? extends Extension>... extensions) {
            throw new UnsupportedOperationException();
        }

        static Option blacklistExtensions(String... extensions) {
            throw new UnsupportedOperationException();
        }

        // Mapning af Execeptions/Errors....
        //// Saa er det let at rette i f.eks. App

        // Debug Options...

        // whitelistExtension(...)
        // blacklistExtension

        //// Can only use of them
        // Altsaa det maa vaere meget taet paa ArchUnit a.la.. Hmmm

        static Option nameProvider() {
            // prefix???
            // Ideen er ihvertfald at
            throw new UnsupportedOperationException();
        }

        // Hmmm... Fungere jo ikke rigtigt med image....
        static Option prefixWirelets(Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        // custom ServiceProvider..
        static Option serviceProvider() {
            throw new UnsupportedOperationException();
        }

        // Hmmm... Fungere jo ikke rigtigt med image......
        @SafeVarargs
        static Option whitelistExtensions(Class<? extends Extension>... extensions) {
            throw new UnsupportedOperationException();
        }

        // non execution -> Create...
    }

    /** An implementation of {@link ApplicationImage} used by {@link ApplicationDriver#newImage(Assembly, Wirelet...)}. */
    private final record PackedApplicationImage<A> (PackedApplicationDriver<A> driver, ComponentSetup root) implements ApplicationImage<A> {

        /** {@inheritDoc} */
        @Override
        public Component component() {
            return root.adaptor();
        }

        /** {@inheritDoc} */
        @Override
        public A apply(Wirelet... wirelets) {
            // Initialize a new application
            PackedInitializationContext pic = PackedInitializationContext.process(root, wirelets);

            // Wrap the system in a new shell and return it
            return driver.newApplication(pic);
        }
    }
}