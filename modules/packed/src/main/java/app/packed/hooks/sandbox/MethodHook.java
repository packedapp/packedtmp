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
package app.packed.hooks.sandbox;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import app.packed.bundle.BaseAssembly;
import app.packed.bundle.Assembly;
import app.packed.bundle.Composer;
import app.packed.extension.Extension;
import app.packed.hooks.BeanClass;
import app.packed.hooks.BeanMethod;
import app.packed.lifecycle.OnInitialize;

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
 * In order to be usable with  ComposedAnnotation, this annotation has ElementType.ANNOTATION_TYPE among its
 * targets.
 */
// InvokableMethodHook
// UpdatableFieldHook
// BeanMethodHook
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
//@Repeatable(MethodHook.All.class)
@Documented
// Tag noget tekst fra den nederste comment
// https://stackoverflow.com/questions/4797465/difference-between-hooks-and-abstract-methods-in-java
@interface MethodHook {

    /**
     * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
     * <p>
     * Methods such as {@link BeanMethod#methodHandle()} and... will fail with {@link UnsupportedOperationException} unless
     * the value of this attribute is {@code true}.
     * 
     * @return whether or not the implementation is allowed to invoke the target method
     * 
     * @see BeanMethod#methodHandle()
     */
    boolean allowInvoke() default false; // allowIntercept...

    /** Bootstrap classes for this hook. */
    Class<? extends BeanMethod> bootstrap();
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
     * @see BeanMethod#methodHandle()
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
    public final Set<? /*MethodHook*/> activatedBy() {

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

    public final <T extends BeanClass> T manageBy(Class<T> bootstrapType) {
        throw new UnsupportedOperationException();
    }

    /**
     * If this method is managed by an extension class returns the Returns the ny extension class that this method is
     * managed.
     * 
     * @return any
     * @see BeanClass#managedMethods(Class) // * @see Bootstrap#manageBy(Class) // * @see BuildContext#managedBy()
     */
    public final Optional<Class<? extends BeanClass>> managedBy() {
        throw new UnsupportedOperationException();
    }
}

//@Doo(extension = Rooddd(23, 234))

@ZuperSupport // same
@BeanMethod.Hook(bootstrap = Zester.Scan.class)
class Zester extends BaseAssembly {

    /** {@inheritDoc} */
    // build vs assemble... Vi har jo ogsaa composer. Compose??
    // Men det er jo stadig en del af build/compose processen
    @Override
    protected void build() {}

    class Scan extends BeanMethod {

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
@BeanMethod.Hook(bootstrap = Zester.Scan.class)
@interface ZuperSupport {}

// Man kan ogsaa have en BuildContext som man kan faa injected...
// Why not just extended...
// Enten kan man extende build, eller faa injected BuildTimeContext
// Giver ikke
// Foerst overvejede vi at have en klasse ligesom bootstrap...
// Men den er irriterende fordi Bootstrap fx ikke kan extende begge
// Man kan hellere ikke fx faa injected Extension
