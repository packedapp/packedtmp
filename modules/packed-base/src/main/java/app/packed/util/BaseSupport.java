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
package app.packed.util;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;

import app.packed.container.BaseBundle;

/**
 * Provides an environment per module
 */
// ModuleEnv, ModuleEnvironment

// Taenker om man kan have flere....
// F.eks. et der er enabled naar man koerer tests....
// Og hvad med unnamed modules...
// GlobalEnv
/// Skal module env vaere public????
// GlobalEnv ... Der kan kun vaere en af disse.....
// GlovalEvn.with(Class<? extends ModuleEnv>)
// Attributable??????

// Why not multiple per module...

// BaseSupportController
// Behavior
// BaseSupportProvider
// BaseSupport
// Maaske flyt til .bundle

// BaseSupport, fordi det er en blanding...
// Saa kan vi have en WebSupport (extends AnySupport).....

// Should support some kind of profile support...
// In this way you can register all your bundles, but for example only generate for -Dprofile=someprofile..
// Would be nice with some support, for forexample, http://www.java2uml.com/p/modelSample
// Could browse a bundle in this way...

// BaseSupport withProfile(String... profiles)..
// Man skal vel naesten kalde de bundle noget forskelligt....
// ForeignService... <- JDBC driver

/**
 * Invoking any method on this class outside of the {@link #configure()} method will result in an
 * {@link IllegalStateException} being thrown.
 */
public abstract class BaseSupport {

    protected final void addSupport(BaseSupport support) {
        throw new UnsupportedOperationException();
    }

    // registerLookedUpClass
    // Why not ordinary classes???
    // lookupClass

    // open -> Maybe you can skip specifying a class, but just the lookup object
    // this is more or less like opening up the bundle

    /** Configures this support class. This method is invoked exactly once. */
    protected abstract void configure();

    // open??? openClass???
    protected final void registerClass(MethodHandles.Lookup lookup, Class<?>... type) {
        // Registers full access to the class for subclasses.
        // Is useful, for example, for having a base abstract class with @Inject in module.
        // That is overridden in another module.

        // split-module class hierarchies
        // Visibility is controlled via modules

        // registerAbstractClass(AbstractLoggable, )

        // as an alternative the package should be open to Packed
        // Finally, last scenario was to be able to add it to a bundle...

        // Check...
        // And insert in ClassValue.... [Cannot unload]
    }

    protected final void registerUnannotatedHook(Class<?> qualifierType) {
        // annotation -> Field, Method
        // interface, class -> Nu begynder sgu at vaere dum.... Maaske vi ikke har interface hooks alligevel
    }

    protected final void registerUnannotatedListenerType(Class<?> qualifierType) {}

    /**
     * Registered a qualifier annotation that is not annotated with
     * <p>
     * Sometimes you just do not have
     * 
     * @param qualifierType
     *            type
     */
    protected final void registerUnannotatedQualifier(Class<? extends Annotation> qualifierType) {}

    // global, module, specific bundles,...

    // Noget med activation... Hvis dette module bliver loaded saa skal det her proceceres

    // En native-test maade, saa vi kan teste at alt virker uden vi skal lave et image foerst....
    /// native-verify..... (Tests that everything is working with regards to native image...

    /**
     * Registers a root,
     * 
     * @param bundle
     *            bundle
     */
    // Registers the bundle, it will always be included in the native image generation....
    // Primary reflection will be activated...
    // Basically we need to register all the bundle roots
    protected final void scanBundle(BaseBundle bundle) {
        // primodial, boot, image, static
        // asNativeBundle()
    }
}
//
// protected BaseSupport() {
// this(false);// Module only is that we should only check stuff from bundles with the module?
// }
//
//// Is always module only!!!!
//// But We may allow. registerGlobalListener(...) from the module that defines the listener interface
// protected BaseSupport(boolean moduleOnly) {}
//
// protected BaseSupport(Class<? extends Bundle> bundles) {}
