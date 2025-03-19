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
package app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import app.packed.bean.BeanMirror;
import app.packed.bean.scanning.BeanTrigger.OnContextServiceInheritableVariable;
import app.packed.binding.BindingMirror;
import app.packed.binding.Key;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerMirror;
import app.packed.context.Context;
import app.packed.context.ContextMirror;
import app.packed.context.ContextScopeMirror;
import app.packed.context.ContextualizedElementMirror;
import app.packed.extension.Extension;
import app.packed.lifetime.LifetimeMirror;
import app.packed.service.mirror.ServiceProviderMirror;
import app.packed.service.mirrorold.ServiceProviderIsThisUsefulMirror;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.extension.BaseExtensionMirrorBeanIntrospector;
import internal.app.packed.util.AbstractDelegatingStream;
import sandbox.operation.mirror.DependenciesMirror;

/**
 * A mirror for an operation on a bean.
 * <p>
 * This class can be extended to provide more detailed information about a particular type of operation. For example,
 * the {@link BaseExtension} provides details about an exported service via {@link ExportedServiceMirror}.
 * <p>
 * NOTE: Subclasses of this class:
 * <ul>
 * <li>Must be located in the same module as the extension it is a member of.</li>
 * </ul>
 */
@OnContextServiceInheritableVariable(introspector = BaseExtensionMirrorBeanIntrospector.class)
public non-sealed class OperationMirror implements ComponentMirror, ContextualizedElementMirror, ContextScopeMirror, ServiceProviderIsThisUsefulMirror {

    /** The handle of the operation we are mirroring. */
    private final OperationHandle<?> handle;

    /**
     * Create a new operation mirror.
     *
     * @param handle
     *            the operation's handle
     */
    public OperationMirror(OperationHandle<?> handle) {
        this.handle = requireNonNull(handle);
    }

    /** {@return a map of all services that are available for the given operation} */
    public final Map<Key<?>, ServiceProviderMirror> availableServices() {
        throw new UnsupportedOperationException();
    }

    /** {@return the bean that this operation is a part of.} */
    public final BeanMirror bean() {
        return handle.operation.bean.mirror();
    }

    /** {@return the bindings of this operation.} */
    public final List<BindingMirror> bindings() {
        BindingSetup[] bindings = handle.operation.bindings;
        if (bindings.length == 0) {
            return List.of();
        }
        BindingMirror[] hooks = new BindingMirror[bindings.length];
        for (int i = 0; i < hooks.length; i++) {
            BindingSetup bs = bindings[i];
            if (bs != null) {
                hooks[i] = bs.mirror();
            } else {
                throw new IllegalStateException("No bindings");
            }
        }
        return List.of(hooks);
    }

    // D
    List<OperationMirror> callsites() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath componentPath() {
        return handle.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public final Set<String> componentTags() {
        return handle.componentTags();
    }

    /** {@return an unchangeable set view of all the contexts that the operation operates within.} */
    @Override
    public final Map<Class<? extends Context<?>>, ContextMirror> contexts() {
        return ContextSetup.allMirrorsFor(handle.operation);
    }

    /**
     * {@return any lifetime this operation is an entry point in. Or empty if this operation is not an entry point
     * operation.}
     */
    public final Optional<LifetimeMirror> entryPointIn() {
        return Optional.ofNullable(handle.operation.entryPoint).map(s -> s.lifetime.mirror());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof OperationMirror m && handle == m.handle;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return handle.hashCode();
    }

    /** {@return the extension that installed the operation} */
    public final Class<? extends Extension<?>> installedByExtension() {
        return handle.operation.installedByExtension.extensionType;
    }

    /**
     * {@return the name of the operation}
     * <p>
     * The name of an operation is always unique among other operations on the same bean.
     */
    public final String name() {
        return handle.operation.name();
    }

    /**
     * If this operation is a nested operation. Returns the corresponding binding in the parent operationWW. Otherwise
     * {@link Optional#empty()}.
     *
     * @return the binding this operation is used by if a nested operation, otherwise {@code empty}
     */
    public final Optional<BindingMirror> nestedIn() {
        return Optional.ofNullable(handle.operation.embeddedInto).map(b -> b.operation().bindings[b.bindingIndex()].mirror());
    }

    /** {@return the operation site.} */
    public final OperationTarget target() {
        return handle.target();
    }

    @Override
    public String toString() {
        return handle.toString();
    }

    /** {@return the type of the operation.} */
    public final OperationType type() {
        return handle.operation.type;
    }

    /** A stream of operation mirrors. */
    public static final class OfStream<T extends OperationMirror> extends AbstractDelegatingStream<T> {

        /**
         * @param stream
         */
        private OfStream(Stream<T> stream) {
            super(stream);
        }

        public <S extends OperationMirror> OfStream<S> ofType(Class<S> c) {
            return of(filter(c::isInstance).map(c::cast));
        }

        public OfStream<T> taggedWith(String tag) {
            return filter(o -> o.componentTags().contains(tag));
        }

//        // ownedBy
//
//        // Will attempt to resolveTheClassPath, and the String
//        // app.operations.hasBinding(Clock.class).source().print();
//
//        // All uniqueBeans in the stream
//        /** {@return a bean stream of a all distinct beans} */
//        BeanMirrorStream<BeanMirror> beans();
//
//        OperationMirrorStream<T> inContext(Class<? extends Context<?>> context);
//
//        OperationMirrorStream<T> installedByExtension(Class<? extends Extension<?>> extensionType);
//
//        OperationMirrorStream<T> ownedByUser();
//
//        OperationMirrorStream<T> ownedByExtension(Class<? extends Extension<?>> extensionType);

        ///////////////////////// Return type adapted

        /** {@inheritDoc} */
        @Override
        public OfStream<T> distinct() {
            return of(stream.distinct());
        }

        /** {@inheritDoc} */
        @Override
        public OfStream<T> filter(Predicate<? super T> predicate) {
            return of(stream.filter(predicate));
        }

        /** {@inheritDoc} */
        @Override
        public OfStream<T> limit(long maxSize) {
            return of(stream.limit(maxSize));
        }

        /** {@inheritDoc} */
        @Override
        public OfStream<T> parallel() {
            return of(stream.parallel());
        }

        /** {@inheritDoc} */
        @Override
        public OfStream<T> peek(Consumer<? super T> action) {
            return of(stream.peek(action));
        }

        /** {@inheritDoc} */
        @Override
        public OfStream<T> sequential() {
            return of(stream.sequential());
        }

        /** {@inheritDoc} */
        @Override
        public OfStream<T> skip(long n) {
            return of(stream.skip(n));
        }

        /** {@inheritDoc} */
        @Override
        public OfStream<T> sorted() {
            return of(stream.sorted());
        }

        /** {@inheritDoc} */
        @Override
        public OfStream<T> sorted(Comparator<? super T> comparator) {
            return of(stream.sorted(comparator));
        }

        /** {@inheritDoc} */
        @Override
        public OfStream<T> unordered() {
            return of(stream.unordered());
        }

        /**
         * Create a new operation mirror stream from a normal stream of operation mirrors.
         *
         * @param <T>
         *            the type of operation mirrors
         * @param stream
         *            the stream to wrap
         * @return the operation mirror stream
         */
        public static <T extends OperationMirror> OfStream<T> of(Stream<T> stream) {
            return new OfStream<>(stream);
        }
    }
}

//Class -> members
//Scanning class -> Hooks
//Bean -> Operation

//Does an operation always introduce a dependency between two beans???
class ZandboxOM {

//  // The returned set of keys may contains key that result in a cycle.
//  // For example, if a bean is provided as a service. Calling this method on any of the
//  // operations on the bean will include the key under which the bean is being provided.
//  public Set<Key<?>> keys() {
//      HashSet<Key<?>> result = new HashSet<>();
//      for (ServiceManagerEntry e : operation.bean.container.sm.entries.values()) {
//          if (e.provider() != null) {
//              result.add(e.key);
//          }
//      }
//      return Set.copyOf(result);
//  }

    // Composites, What about services???
    // Services no, because one operation may be used multiple places
//  /** {@return a set of any contexts initiated by invoking the operation.} */
//  // Context root?
//  public Set<ContextMirror> zCreatesContexts() {
//      // contexts().values().scope = Operation
//      throw new UnsupportedOperationException();
//  }

    /**
     * Returns whether or new component lifetime is created every time the operation is invoked. The lifetime will last jast
     * for the lifetime of the operation.
     *
     * {@return true if the operation creates a new bean instance every time it is invoked, otherwise false.}
     */
    // Creates a new Lifetime just for the duration of the operation!
    // What about start/stop (not present)
    // spawnsLifetime

    // Altsaa ved ikke om det bare er en boolean???? MakesNewBean?
    // Problemet er lidt hvordan selve registreringen skal vaere?
    // Man skal jo markere containeren og sige. Alle @Get beans i den her container starter en ny container.
    // Det er vel ikke meget anderledes end all @Get beans i denne bean starter en ny container
    // Hvori foriøvrigt det kun er den ene bean der skal launches
    //

    final Optional<LifetimeMirror> createsLifetime() {
        // Hvad med en constructor??
        //// Alt hvad der kalder en i user code kraever en operation

        // Operation
        // Factory
        // Operation+Factory

        // Jeg tror maaske Factory ikke er operations...
        // Men saa er de irriterende i forbindelse med injection....

        throw new UnsupportedOperationException();
    }

    // Synchronous/Asynchronous
    final boolean createsNewThread() {
        // Does the operation spawn a new thread????
        // Er vel kun hvis vi fx kommer fra en daemon?

        // synchronous (in calling thread)
        // Spawn (er jo en slags asynchronous...)
        // Hoere det til i noget meta data per extension???
        return false;
    }

//    /** {@return the services that are available at this injection site.} */
//    final Set<Key<?>> availableServices() {
//        throw new UnsupportedOperationException();
//    }

    public Collection<BeanMirror> dependsOn() {
        // Introduces dependencies on xx beans
        // Det er saa her hvor jeg syntes vi skal koere gennem composite
        // Hvis vi wrapper noget i en composite skal vi stadig lave en ny
        return List.of();
    }

    /** {@return how errors are handle when calling the operation.} */
    final Object /* OperationErrorHandlingMirror */ errorHandling() {
        // field??? Unhandled?
        throw new UnsupportedOperationException();
    }

    final Optional<Class<? extends Annotation>> hook() {
        // We should also add this to DependencyMirror

        // Enten Provide eller ogsaa MetaAnnotation
        // use source().annotationReader();

        // Hvad hvis vi har @ScheduleAtFixRated + ScheduleAtFlatRate
        // der sammen laver en operation

        // Maybe this is on target???
        // Maybe it is a list
        throw new UnsupportedOperationException();
    }

    /** {@return any interceptors that are applied to the operation.} */
    final List<Object /* OperationInterceptorMirror */> interceptors() {
        throw new UnsupportedOperationException();
    }

    // I don't know if we want this? It is very internal...
    final MethodType invocationType() {
        // OperationTypeMirror? Nope things are erased
        throw new UnsupportedOperationException();
    }

    ContainerMirror invokedFromContainer() {
        throw new UnsupportedOperationException();
    }

    final void printBindings() {
        // Det er jo bare en trae af ServiceDependency

        // ResolvedVariable -> Status Unresolved but Optional.

        // InjectableDependency?

        // En for hver parameter...
//        com.javadeveloperzone:maven-show-dependency-tree:jar:1.0-SNAPSHOT
//        \- org.springframework.boot:spring-boot-devtools:jar:1.5.4.RELEASE:compile
//           +- org.springframework.boot:spring-boot:jar:1.5.4.RELEASE:compile
//           |  +- org.springframework:spring-core:jar:4.3.9.RELEASE:compile
//           |  |  \- commons-logging:commons-logging:jar:1.2:compile
//           |  \- org.springframework:spring-context:jar:4.3.9.RELEASE:compile
//           |     +- org.springframework:spring-aop:jar:4.3.9.RELEASE:compile
//           |     +- org.springframework:spring-beans:jar:4.3.9.RELEASE:compile
//           |     \- org.springframework:spring-expression:jar:4.3.9.RELEASE:compile
//           \- org.springframework.boot:spring-boot-autoconfigure:jar:1.5.4.RELEASE:compile
    }

    /**
     * Returns the return type of the operation. If the operation does not provide {@code void.class} if the the operation
     * does not return a result.
     * <p>
     * This might not match the return type of any underlying method. For examples, you might return a completable future (a
     * wrapper) if returning the result in a wrapper. see method declaredResultType() for actual returning class??? or do we
     * just inspect operation type
     *
     * Also with regards to return type. If the operator ignores the result, this method typically just returns void.class
     *
     * @return the
     *
     * @see Method#getReturnType()
     */
    // why not just return void???
    // returnType() + returnTypeToken()
    // Think we will just have Type
    final Class<?> resultType() {

        // skal vi omnavngive den til returnType?
        // Det der er godt ved result er at det er lidt mindre bundet op
        // til metode navngivning. Men mere "sig" selv

        // Type genericReturnType?
        // declaredResultType kan include wrapperen...
        return void.class;
    }

    /** {@return the dependencies this operation introduces.} */
    public DependenciesMirror zDependencies() {
        throw new UnsupportedOperationException();
    }

//    public static <T extends OperationMirror> Stream<T> findAll(ApplicationMirror application, Class<T> operationType) {
//        throw new UnsupportedOperationException();
//    }
//
//    public static void main(String[] args) {
////        SandboxOp.findAll(null, BeanLifecycleMirror.class).filter(m -> m.state() == RunState.INITIALIZED).count();
//
//    }

}
//A bean constructor is _not_ an operation... Or maybe

//AnnotatedElement????? Nah, Det er targettt der kan vaere annoteret

//Attribute support. Det vil give mening at kunne attache noget information af lidt mere dynamisk karakter?
//Fx informations annotations?? Her taenker jeg paa OpenAPI annoteringer

//ExportServiceMirror vs ExportedServiceMirror
//ProvideServiceMirror vs ProvidedServiceMirror
//SubscribeEventMirror vs SubscribedEventMirror
//ConfigUseMirror vs ConfigUsedMirror
//LifecycleCallMirror vs LifecycleCalledMirror
//ScheduleTaskMirror vs ScheduledTaskMirror

///**
//*
//* If the lifetime
//*
//* {@return the lifetime of the the operation.}
//*/

/// En operation har ikke en lifetime.
/// Den spawner _masske_ en ny lifetime

//public final LifetimeMirror lifetime() {
//  return bean().lifetime().get();
//  // lifetime != bean.lifetime() -> operation lifetime
//}
//
//public Optional<Lifetime> requiresNewLifetime() {
//  // @Get may be both
//  // @OnInitialize never requires new Lifetime
//
//  // Maaske man kan kan kigge paa lifetime rooten...
//  // Om det er en operation, en
//
//  throw new UnsupportedOperationException();
//}

////Paa en eller anden maade maa den bindes til noget...
//Object boundTo();
//
///** {@return the component behind the action.} */
//ComponentMirror component();
//
//InterruptPolicy interruptPolicy();
//
//boolean isAsync();
//
//public boolean isFactory() {
//// isInitializer
//// Ideen er at constructuren ser anderledes ud
//return false;
//}
