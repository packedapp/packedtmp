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
package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.artifact.ArtifactSource;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import packed.internal.config.ConfigSiteSupport;

/** An abstract implementation of ComponentConfiguration that can be extended by extensions. */

// Vil ikke afvise at vi dropper interface ComponentConfiguration...
// Og omdoeber denne til ComponentConfiguration

// Tror det vil vaere rart i forbindelse f.eks. med at StackWalker...
// At vi altid ved den er ComponentConfiguration...

// Grunde til vi ikke har lyst til det....

// F.eks. hvis vi gerne vil lave en immutable version...
// Kunne have en clone()... som returnere en ny instans med en ny context...
// Som fejler ved checkConfigurable....

// Saa vi kun har en type...

// Lad mig spoerge paa en anden maade.. Kan vi paa nogen maade forstille os at der kommer til at 
// vaere flere implementation af en specific component configuration type...??? 
// Det tror jeg ikke...

// Men det er jo det samme med image... Det der er rart er jo at vi kan gemme implementeringen vaek
// Men taenker dem der laver noget har den knyttet til en Extension??? som kan kalde en package
// private constructor... Kan ikke forstille mig folk laver typer udenom en Extension
// Eller vi har jo nok ComponentModel<Factory> -> ComponentModel<SingletonConfiguration>

// model.newConfiguration(ComponentContainerContext ccc) -> return new SingletonCC(ccc);
public abstract class AbstractComponentConfiguration implements ComponentConfiguration {

    /** A stack walker used from {@link #captureStackFrame(String)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** The configuration context */
    protected final ComponentConfigurationContext context;

    protected AbstractComponentConfiguration() {
        context = (ComponentConfigurationContext) this;
    }

    protected AbstractComponentConfiguration(ComponentConfigurationContext context) {
        this.context = requireNonNull(context, "context is null");
    }

    protected abstract String initializeNameDefaultName();

    /**
     * Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
     * not located on any subclasses of {@link Extension} or any class that implements {@link ArtifactSource}.
     * <p>
     * Invoking this method typically takes in the order of 1-2 microseconds.
     * <p>
     * If capturing of stack-frame-based config sites has been disable via, for example, fooo. This method returns
     * {@link ConfigSite#UNKNOWN}.
     * 
     * @param operation
     *            the operation
     * @return a stack frame capturing config site, or {@link ConfigSite#UNKNOWN} if stack frame capturing has been disabled
     * @see StackWalker
     */
    // TODO add stuff about we also ignore non-concrete container sources...
    protected final ConfigSite captureStackFrame(String operation) {
        // API-NOTE This method is not available on ExtensionContext to encourage capturing of stack frames to be limited
        // to the extension class in order to simplify the filtering mechanism.

        // Vi kan spoerge "if context.captureStackFrame() ...."

        if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
            return ConfigSite.UNKNOWN;
        }
        Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
        return sf.isPresent() ? configSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
    }

    /**
     * @param frame
     *            the frame to filter
     * @return whether or not to filter the frame
     */
    private final boolean captureStackFrameIgnoreFilter(StackFrame frame) {

        Class<?> c = frame.getDeclaringClass();
        // Det virker ikke skide godt, hvis man f.eks. er en metode on a abstract bundle der override configure()...
        // Syntes bare vi filtrer app.packed.base modulet fra...
        // Kan vi ikke checke om imod vores container source.

        // ((PackedExtensionContext) context()).container().source
        // Nah hvis man koere fra config er det jo fint....
        // Fra config() paa en bundle er det fint...
        // Fra alt andet ikke...

        // Dvs ourContainerSource
        return Extension.class.isAssignableFrom(c)
                || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && ArtifactSource.class.isAssignableFrom(c));
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        context.checkConfigurable();
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return context.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Class<? extends Extension>> extension() {
        return context.extension();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getDescription() {
        return context.getDescription();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return context.getName();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentDescriptor model() {
        return context.model();
    }

    /** {@inheritDoc} */
    @Override
    public void onNamed(Consumer<? super ComponentConfiguration> action) {
        context.onNamed(action);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath path() {
        return context.path();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration setDescription(String description) {
        return context.setDescription(description);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration setName(String name) {
        return context.setName(name);
    }
}
