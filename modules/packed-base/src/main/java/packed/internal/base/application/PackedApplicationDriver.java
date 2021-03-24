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
import java.lang.invoke.MethodType;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationImage;
import app.packed.base.Completion;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Composer;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.validate.Validation;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.PackedInitializationContext;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link ApplicationDriver}. */
public final class PackedApplicationDriver<A> implements ApplicationDriver<A> {

    /** A daemon driver. */
    public static final PackedApplicationDriver<Completion> COMPLETABLE = new PackedApplicationDriver<>(true,
            MethodHandles.dropArguments(MethodHandles.constant(Completion.class, Completion.success()), 0, PackedInitializationContext.class));

    /** A daemon driver. */
    public static final PackedApplicationDriver<Completion> DAEMON = new PackedApplicationDriver<>(true,
            MethodHandles.empty(MethodType.methodType(Void.class, PackedInitializationContext.class)));

    /** The initial set of modifiers for any system that uses this driver. */
    private final boolean isStateful;

    /** The method handle used for creating new application instances. */
    private final MethodHandle mhConstructor; // (PackedInitializationContext)Object

    /** May contain a wirelet that will be processed _after_ any other wirelets. */
    @Nullable
    public final Wirelet wirelet;

    /**
     * Creates a new driver.
     * 
     * @param isStateful
     *            whether or not the the artifact is stateful
     * @param mhNewShell
     *            a method handle that can create new artifacts
     */
    public PackedApplicationDriver(boolean isStateful, MethodHandle mhNewShell) {
        this.isStateful = isStateful;
        this.mhConstructor = requireNonNull(mhNewShell);
        this.wirelet = null;
    }

    private PackedApplicationDriver(boolean isStateful, MethodHandle newArtifactMH, Wirelet prefix) {
        this.isStateful = isStateful;
        this.mhConstructor = requireNonNull(newArtifactMH);
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
    public ApplicationImage<A> buildImage(Assembly<?> assembly, Wirelet... wirelets) {
        BuildSetup build = buildFromAssembly(assembly, wirelets, false, true);
        return new PackedApplicationImage<>(this, build.component);
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

    public boolean isStateful() {
        return isStateful;
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
    public Validation validate(Assembly<?> assembly, Wirelet... wirelets) {
        // Denne metoder siger ikke noget om at alle kontrakter er fullfilled.
        // Det er fuldt ud "Lovligt" ikke at specificere alt muligt...
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDriver<A> with(Wirelet... wirelets) {
        Wirelet w = wirelet == null ? Wirelet.combine(wirelets) : wirelet.andThen(wirelets);
        return new PackedApplicationDriver<>(isStateful, mhConstructor, w);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationDriver<A> with(Wirelet wirelet) {
        requireNonNull(wirelet, "wirelet is null");
        Wirelet w = this.wirelet == null ? wirelet : wirelet.andThen(wirelet);
        return new PackedApplicationDriver<>(isStateful, mhConstructor, w);
    }

    // A method handle that takes an ArtifactContext and produces something that is compatible with A
    public static <A> ApplicationDriver<A> of(MethodHandles.Lookup caller, Class<A> artifactType, MethodHandle mh) {
        // TODO validate type
        // shellType must match MH
        boolean isGuest = AutoCloseable.class.isAssignableFrom(artifactType);
        // TODO fix....

        return new PackedApplicationDriver<>(isGuest, mh);
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

        // Normally we just check if App.iface extends AutoClosable...
        // But might want to override this.
        // forceManaged, forceUnmanaged

        // If not autoclosable
        static Option forceGuest() {
            throw new UnsupportedOperationException();
        }

        static Option forceNonGuest() {
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

        static Option postfixWirelets(Wirelet... wirelets) {
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

        // Ideen var man skulle kunne angive nogle prefix wirelets naar man lavede en shell...
        // Men det er nu lavet om til options pre and post wirelets
        // executing -> Create (and initialize), start, startAsync, Execute, executeAsync
        // StartExecutor ?
        // final T create(Assembly source, Wirelet... shellWirelets) {
        //
//         // Ideen er lidt at Artifact Implementering, kan kalde med dens egen wirelets...
//         // ala
//         // Maaske er det en option.. .. ellers andThen... eller have en Wirelet customWirelets(Assemble)..
        //
//         // start(Assembly, Wirelet... wirelets) {
//         // create(Assembly, wirelets, ArtifactWirelets.startSynchronous());
//         // create(Assembly, wirelets, ArtifactWirelets.startAsynchronous());
//         // }
//         // What about execute....
//         throw new UnsupportedOperationException();
        // }
    }

    /** An implementation of {@link ApplicationImage} used by {@link ApplicationDriver#buildImage(Assembly, Wirelet...)}. */
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

    static class PackedBuilder /* Implements ArtifactDriver.Builder */ {

    }
}
