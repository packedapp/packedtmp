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
package app.packed.service.mirror;

import java.util.Optional;

import app.packed.binding.BindingHandle;
import app.packed.binding.BindingMirror;
import app.packed.binding.Key;
import internal.app.packed.binding.PackedBindingHandle;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.service.ServiceProviderSetup;

/**
 *
 * @see BindingKind#SERVICE
 */
public class ServiceBindingMirror extends BindingMirror {

    /** The service binding. */
    private final ServiceBindingSetup binding;

    /**
     * @param handle
     */
    public ServiceBindingMirror(BindingHandle handle) {
        super(handle);
        PackedBindingHandle pbh = (PackedBindingHandle) handle;
        this.binding = (ServiceBindingSetup) pbh.binding();
    }

    /** {@return the binding key.} */
    public Key<?> key() {
        return binding.key;
    }

    /** {@return the service the binding is bound to, or empty if a service could not be provided} */
    public Optional<ServiceProviderMirror> service() {
        return Optional.ofNullable(binding.resolvedProvider).map(ServiceProviderSetup::mirror);
    }
}

//
///**
//*
//* @see app.packed.bean.BeanConfiguration#overrideService(Class, Object)
//* @see app.packed.bean.BeanConfiguration#overrideService(app.packed.util.Key, Object)
//*/
//// Alternativt peger den bare paa en ConstantMirror i beanen's namespace??
//public static final class FromBean extends ServiceBindingMirror {
//
//  /**
//   * @param handle
//   */
//  public FromBean(BindingHandle handle) {
//      super(handle);
//  }
//
//  public BeanMirror bean() {
//      throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public Key<?> key() {
//      throw new UnsupportedOperationException();
//  }
//}
//
///**
//*
//*/
//// Ideen er at man kan provide nogle i en operation context...
//// Som ikke noedvendigvis er binding hooks
//// Er factory? En context??? Hmmm En slags jo...
//public static non-sealed class FromContext extends ServiceBindingMirror {
//
//  final Key<?> key;
//  final ContextMirror context;
//
//  /**
//   * @param handle
//   */
//  public FromContext(BindingHandle handle, Key<?> key, ContextMirror context) {
//      super(handle);
//      this.key = key;
//      this.context = context;
//  }
//
//  /** {@return the context that provided the binding} */
//  public ContextMirror context() {
//      return context;
//  }
//
//  // Class<? extends Annotation> contextualServiceProvidingAnnotation>
//
//  /** {@inheritDoc} */
//  @Override
//  public Key<?> key() {
//      return key;
//  }
//}
//
///**
//* A binding of a service.
//* <p>
//* I virkeligheden eksistere der ikke noedvendig en service. Men bindingen er blevet resolvet som en service
//*/
//// findAll(SBM.class).filterOn(key.equals(String.class)).toList();
//
//// Hvor faar vi den fra successfuld
////// En Bean (constant)
////// En Lifetime bean
////// En prototypeBean
////// En @Provide method
//
//// unsuccessfull
////// Missing
////// missing but Optional
////// missing but default
//
//// Maaske er ServiceBinding altid en service, og det andet er en manuel binding
//public static non-sealed class FromNamespace extends ServiceBindingMirror {
//
//  /** The service binding */
//  private final ServiceBindingSetup binding;
//
//  public FromNamespace(BindingHandle handle, ServiceBindingSetup binding) {
//      super(handle);
//      this.binding = requireNonNull(binding);
//  }
//
//  /** {@return the domain this service is provided from.} */
//  public ServiceNamespaceMirror namespace() {
//      throw new UnsupportedOperationException();
//  }
//
//  /** {@inheritDoc} */
//  @Override
//  public Key<?> key() {
//      return binding.key;
//  }
//
//  // non null if resolvedx
//  // Der er noget med en sti til servicen.
//// public Optional<ProvidedServiceMirror> providingService() {
////     throw new UnsupportedOperationException();
//// }
//}
////
//// public Optional<BeanMirror> providedBy() {
//// throw new UnsupportedOperationException();
//// }
//
///**
//*
//* @see app.packed.bean.BeanConfiguration#overrideService(Class, Object)
//* @see app.packed.bean.BeanConfiguration#overrideService(app.packed.util.Key, Object)
//*/
//// Alternativt peger den bare paa en ConstantMirror i beanen's namespace??
//public static final class FromOperation extends ServiceBindingMirror {
//
//  /**
//   * @param handle
//   */
//  public FromOperation(BindingHandle handle) {
//      super(handle);
//  }
//
//  public BeanMirror bean() {
//      throw new UnsupportedOperationException();
//  }
//
//  @Override
//  public Key<?> key() {
//      throw new UnsupportedOperationException();
//  }
//}
//
///**
//*
//* @see app.packed.bean.BeanConfiguration#overrideService(Class, Object)
//* @see app.packed.bean.BeanConfiguration#overrideService(app.packed.util.Key, Object)
//*/
//// Alternativt peger den bare paa en ConstantMirror i beanen's namespace??
//public static final class Unresolved extends ServiceBindingMirror {
//
//  /**
//   * @param handle
//   */
//  public Unresolved(BindingHandle handle) {
//      super(handle);
//  }
//
//  @Override
//  public Key<?> key() {
//      throw new UnsupportedOperationException();
//  }
//}

//
///**
// * {@return whether or not the service is required.}
// * <p>
// * A service might not be required, for example, if it has a default value.
// */
//public boolean isRequired() {
//    return binding.isRequired;
//}
//
//public boolean isResolved() {
//    return binding.isResolved();
//}
//
///**
// * A satisfiable binding is binding that is either resolved or not required.
// * <p>
// * By default building an application will fail if any service bindings are not satisfiable
// *
// * @return
// */
//public boolean isSatisfiable() {
//    return isResolved() || !isRequired();
//}