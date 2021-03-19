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

/**
 * The base class for build-time configurations of components. The class is basically a thin wrapper on top of
 * {@link ComponentConfigurationContext}. All component configuration classes must extend, directly or indirectly, from
 * this class.
 * <p>
 * Instead of extending this class directly, you typically want to extend {@link BaseComponentConfiguration} instead.
 */
public abstract class ComponentConfiguration {

    /** The component's configuration context. */
    // Assembly needs access to this context for lookup() which is the main reason
    // We have this field...
    protected final ComponentConfigurationContext context;

    /**
     * Create a new component configuration.
     * 
     * @param context
     *            the configuration context
     */
    protected ComponentConfiguration(ComponentConfigurationContext context) {
        this.context = requireNonNull(context, "context is null");
    }
    
    /**
     * A callback method invoked by Packed immediatly before it is marked as no longer configurable
     */
    protected void onConfigured() {
        // Ideen er lidt at man kan lave lidt validering eller andet Goejl
    }
}
// I don't expect this class to have any $ methods
// This should most likely be located in the driver instead
// A component configuration is just a thin wrapper

///** A stack walker used from {@link #captureStackFrame(String)}. */
//private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

///**
//* Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
//* not located on any subclasses of {@link Extension} or any class that implements
//* <p>
//* Invoking this method typically takes in the order of 1-2 microseconds.
//* <p>
//* If capturing of stack-frame-based config sites has been disable via, for example, fooo. This method returns
//* {@link ConfigSite#UNKNOWN}.
//* 
//* @param operation
//*            the operation
//* @return a stack frame capturing config site, or {@link ConfigSite#UNKNOWN} if stack frame capturing has been disabled
//* @see StackWalker
//*/
//// TODO add stuff about we also ignore non-concrete container sources...
//protected final ConfigSite captureStackFrame(String operation) {
//// API-NOTE This method is not available on ExtensionContext to encourage capturing of stack frames to be limited
//// to the extension class in order to simplify the filtering mechanism.
//
//// Vi kan spoerge "if context.captureStackFrame() ...."
//
//if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
//    return ConfigSite.UNKNOWN;
//}
//Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
//return sf.isPresent() ? configSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
//}

///**
//* @param frame
//*            the frame to filter
//* @return whether or not to filter the frame
//*/
//private final boolean captureStackFrameIgnoreFilter(StackFrame frame) {
//
//Class<?> c = frame.getDeclaringClass();
//// Det virker ikke skide godt, hvis man f.eks. er en metode on a abstract container der override configure()...
//// Syntes bare vi filtrer app.packed.base modulet fra...
//// Kan vi ikke checke om imod vores container source.
//
//// ((PackedExtensionContext) context()).container().source
//// Nah hvis man koere fra config er det jo fint....
//// Fra config() paa en container er det fint...
//// Fra alt andet ikke...
//
//// Dvs ourContainerSource
//return Extension.class.isAssignableFrom(c)
//        || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && Assembly.class.isAssignableFrom(c));
//}
