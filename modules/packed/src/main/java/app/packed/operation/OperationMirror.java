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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.BeanMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;
import app.packed.container.InternalExtensionException;
import app.packed.inject.service.ServiceExportOperationMirror;
import app.packed.lifetime.LifetimeMirror;
import app.packed.operation.dependency.DependencyMirror;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.Mirror;
import packed.internal.operation.OperationSetup;

/**
 * A mirror for a bean operation.
 * <p>
 * This class can be extended to provide more detailed information about a particular type of operation. For example,
 * the {@link app.packed.inject.service.ServiceExtension} provides details about an exported service via
 * {@link ServiceExportOperationMirror}.
 * <p>
 * NOTE: Subclasses of this class:
 * <ul>
 * <li>Must be located in the same module as the extension it is a member of (iff the extension is defined in a
 * module).</li>
 * </ul>
 */
//Class -> members
//Scanning class -> Hooks
//Bean -> Operation
public class OperationMirror implements Mirror {

    /**
     * The internal configuration of the operation we are mirrored. Is initially null but populated via
     * {@link #initialize(ExtensionSetup)}.
     */
    @Nullable
    private OperationSetup operation;

    /**
     * Create a new operation mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    public OperationMirror() {}

    /** {@return the application the operation belongs to.} */
    public final ApplicationMirror application() {
        return operation().bean.application.mirror();
    }

    /** {@return the bean the operation belongs to.} */
    public final BeanMirror bean() {
        return operation().bean.mirror();
    }

    /** {@return the container the operation belongs to.} */
    public final ContainerMirror container() {
        return operation().bean.parent.mirror();
    }

    /** {@return any dependencies that the operation takes.} */
    public final List<DependencyMirror> dependencies() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof OperationMirror m && operation() == m.operation();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return operation().hashCode();
    }

    /**
     * {@return the internal configuration of operation.}
     * 
     * @throws InternalExtensionException
     *             if {@link #initialize(OperationSetup)} has not been called.
     */
    private OperationSetup operation() {
        OperationSetup o = operation;
        if (o == null) {
            throw new InternalExtensionException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return o;
    }

    /**
     * Invoked by {@link Extension#mirrorInitialize(ExtensionMirror)} to set the internal configuration of the extension.
     * 
     * @param owner
     *            the internal configuration of the extension to mirror
     */
    final void initialize(OperationSetup operation) {
        if (this.operation != null) {
            throw new IllegalStateException("The specified mirror has already been initialized.");
        }
        this.operation = operation;
    }

    /** {@return the extension that is responsible for invoking the operation.} */
    public final Class<? extends Extension<?>> invokedBy() {
        // ExtensionMirror??? Hmm, saa returnere vi en Single?
        return operation().operatorBean.extension.extensionType;
    }

    /** {@return the target of the operation.} */
    public final OperationTargetMirror target() {
        return operation().operationTarget.mirror();
    }

    //////////// Tvivlsomme

    /** {@return the services that are available at this injection site.} */
    final Set<Key<?>> availableServices() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether or not a new bean instance is created every time the operation is invoked.
     * 
     * {@return true if the operation creates a new bean instance every time it is invoked, otherwise false.}
     */
    // Creates a new Lifetime just for the duration of the operation!
    // What about start/stop
    // spawnsLifetime
    final Optional<LifetimeMirror> createsNewLifetime() {
        // Hvad med en constructor?? 
        //// Alt hvad der kalder en i user code kraever en operation

        // Operation
        // Factory
        // Operation+Factory

        // Jeg tror maaske Factory ikke er operations...
        // Men saa er de irriterende i forbindelse med injection....

        throw new UnsupportedOperationException();
    }

    final boolean createsNewThread() {
        // synchronous (in calling thread)
        // Spawn (er jo en slags asynchronous...)
        // Hoere det til i noget meta data per extension???
        return false;
    }

    /** {@return how errors are handle when calling the operation.} */
    final Object /* OperationErrorHandlingMirror */ errorHandling() {
        // field??? Unhandled?
        throw new UnsupportedOperationException();
    }

    final Optional<Class<? extends Annotation>> hook() {
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

    final String name() {
        // Maaske maa operationer godt have det samme navn????
        
        // Altsaa webGet1 + webGet2 giver jo ikke rigtig noget information...
        // Saa kan vi ligesaa godt have webGet + webGet
        
        //// Vi har vel 3 interessante navne
        // Name
        // BeanExtension#name
        // BeanExtension.longClass#name

        // Kan vi have noget container path

        // zerviceExport, convertValue
        // webGet
        return "";
    }

    final void printDependencyTree() {
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
    final Class<?> resultType() {

        // skal vi omnavngive den til returnType?
        // Det der er godt ved result er at det er lidt mindre bundet op
        // til metode navngivning. Men mere "sig" selv

        // Type genericReturnType?
        // declaredResultType kan include wrapperen...
        return void.class;
    }

    public static <T extends OperationMirror> Stream<T> findAll(ApplicationMirror application, Class<T> operationType) {
        throw new UnsupportedOperationException();
    }
}
//Is invoked by an extension

//A bean function
//A bean method
//A bean field get/set/compute

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
