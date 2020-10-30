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

import app.packed.base.TreePath;
import app.packed.bundle.Extension;
import app.packed.config.ConfigSite;
import app.packed.inject.Factory;
import packed.internal.component.ComponentBuild;
import packed.internal.config.ConfigSiteSupport;

/**
 * An abstract base class for creating component configuration classes. It is basically a thin wrapper on top of
 * {@link ComponentConfigurationContext}.
 * <p>
 * Component configuration classes do not need to extend this class.
 */
public abstract class AbstractComponentConfiguration {

    /** A stack walker used from {@link #captureStackFrame(String)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** The component's configuration context. */
    protected final ComponentConfigurationContext context;

    /**
     * Creates a new component configuration.
     * 
     * @param context
     *            the component's configuration context
     */
    protected AbstractComponentConfiguration(ComponentConfigurationContext context) {
        this.context = requireNonNull(context, "context is null");
    }

    /**
     * Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
     * not located on any subclasses of {@link Extension} or any class that implements
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
                || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && Assembly.class.isAssignableFrom(c));
    }

    /**
     * Checks that the component is still configurable or throws an {@link IllegalStateException}.
     * <p>
     * A component is typically only configurable inside of {@link Assembly#build()}.
     * 
     * @throws IllegalStateException
     *             if the component is no long configurable.
     */
    public final void checkConfigurable() {
        context.checkConfigurable();
    }

    /**
     * Returns the configuration site that created this configuration.
     * 
     * @return the configuration site that created this configuration
     */
    public final ConfigSite configSite() {
        return context.configSite();
    }

    /**
     * Returns the name of the component. If no name has previously been set via {@link #setName(String)} a name is
     * automatically generated by the runtime as outlined in {@link #setName(String)}.
     * <p>
     * Trying to call {@link #setName(String)} after invoking this method will result in an {@link IllegalStateException}
     * being thrown.
     * 
     * @return the name of the component
     * @see #setName(String)
     */

    public final String getName() {
        return context.getName();
    }

    /**
     * Creates a new container with this container as its parent by linking the specified bundle.
     * 
     * @param bundle
     *            the bundle to link
     * @param wirelets
     *            any wirelets
     */
    public final void link(Assembly<?> bundle, Wirelet... wirelets) {
        ((ComponentBuild) context).link(bundle, wirelets);
    }

    /**
     * Returns the full path of the component.
     * <p>
     * Once this method has been invoked, the name of the component can no longer be changed via {@link #setName(String)}.
     * <p>
     * If building an image, the path of the instantiated component might be prefixed with another path.
     * 
     * <p>
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     * 
     * @return the path of this configuration.
     */
    public final TreePath path() {
        return context.path();
    }

    /**
     * Sets the {@link Component#name() name} of the component. The name must consists only of alphanumeric characters and
     * '_', '-' or '.'. The name is case sensitive.
     * <p>
     * If no name is set using this method. A name will be assigned to the component when the component is initialized, in
     * such a way that it will have a unique name other sibling components.
     *
     * @param name
     *            the name of the component
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see #getName()
     * @see Component#name()
     */
    public AbstractComponentConfiguration setName(String name) {
        context.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return context.toString();
    }

    /**
     * Wires a new child component using the specified driver
     * 
     * @param <C>
     *            the type of configuration returned by the driver
     * @param driver
     *            the driver to use for creating the component
     * @param wirelets
     *            any wirelets that should be used when creating the component
     * @return a configuration for the component
     */
    public final <C> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return context.wire(driver, wirelets);
    }

    public final <C, I> C wire(ComponentClassDriver<C, I> driver, Class<? extends I> implementation, Wirelet... wirelets) {
        ComponentDriver<C> cd = driver.bind(implementation);
        return wire(cd, wirelets);
    }

    public final <C, I> C wire(ComponentFactoryDriver<C, I> driver, Factory<? extends I> implementation, Wirelet... wirelets) {
        ComponentDriver<C> cd = driver.bind(implementation);
        return wire(cd, wirelets);
    }

    public final <C, I> C wireInstance(ComponentInstanceDriver<C, I> driver, I instance, Wirelet... wirelets) {
        ComponentDriver<C> cd = driver.bindInstance(instance);
        return wire(cd, wirelets);
    }
}
