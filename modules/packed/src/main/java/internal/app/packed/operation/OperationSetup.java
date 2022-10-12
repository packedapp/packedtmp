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
package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.operation.InvocationType;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.operation.OperationTarget.BeanInstanceAccess;
import internal.app.packed.operation.binding.BindingSetup;
import internal.app.packed.operation.binding.NestedBindingSetup;
import internal.app.packed.service.inject.InternalDependency;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Represents an operation on a bean. */
public final class OperationSetup {

    /** An empty array of {@code BindingSetup}. */
    private static final BindingSetup[] EMPTY = new BindingSetup[0];

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OperationMirror.class, "initialize",
            void.class, OperationSetup.class);

    /** The bean this operation concerns. */
    public final BeanSetup bean;

    /** Bindings for this operation. */
    public final BindingSetup[] bindings;

    /** By who and how this operation is invoked */
    public final OperationInvoker invoker;

    /** Whether or not an invoker has been computed */
    boolean isComputed;

    /** Supplies a mirror for the operation */
    Supplier<? extends OperationMirror> mirrorSupplier = OperationMirror::new;

    /** The underlying target of the operation. */
    public final OperationTarget operationTarget;

    /** The type of the operation. */
    public final OperationType type;

    public final @Nullable NestedBindingSetup parentBinding;

    public OperationSetup(BeanSetup bean, OperationType type, OperationInvoker invoker, OperationTarget operationTarget) {
        this.bean = requireNonNull(bean);
        this.type = requireNonNull(type);
        this.invoker = requireNonNull(invoker);
        this.operationTarget = requireNonNull(operationTarget);

        this.bindings = type.parameterCount() == 0 ? EMPTY : new BindingSetup[type.parameterCount()];
        this.parentBinding = null;
    }

    public OperationSetup(BeanSetup bean, OperationType type, OperationInvoker invoker, OperationTarget operationTarget, @Nullable NestedBindingSetup nested) {
        this.bean = requireNonNull(bean);
        this.type = requireNonNull(type);
        this.invoker = requireNonNull(invoker);
        this.operationTarget = requireNonNull(operationTarget);
        this.parentBinding = nested;
        
        this.bindings = type.parameterCount() == 0 ? EMPTY : new BindingSetup[type.parameterCount()];
    }

    /** {@return a new mirror.} */
    public OperationMirror mirror() {
        // Create a new OperationMirror
        OperationMirror mirror = mirrorSupplier.get();
        if (mirror == null) {
            throw new NullPointerException(mirrorSupplier + " returned a null instead of an " + OperationMirror.class.getSimpleName() + " instance");
        }

        // Initialize OperationMirror by calling OperationMirror#initialize(OperationSetup)
        try {
            MH_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public void resolve() {
        List<InternalDependency> id = InternalDependency.fromOperationType(type);
        
        for (int i = 0; i < bindings.length; i++) {
            if (bindings[i] == null) {
                System.out.println("XXX");
                InternalDependency ia = id.get(i);
                
                // isComposite...
                
                // try resolve annotation binding
                
                // try resolve class binding
                
                // Otherwise as a service
                bean.container.sm.addBinding(ia.key(), !ia.isOptional(), this, i);
            }
        }
    }

    // Relative to x
    public static OperationSetup beanAccess(BeanSetup bean) {
        return new OperationSetup(bean, OperationType.of(bean.beanClass), new OperationInvoker(InvocationType.raw(), bean.installedBy),
                new BeanInstanceAccess(null, false));
    }
}

//public static final class BeanInstanceAccessSetup extends BeanOperationSetup {
//
//  /**
//   * @param bean
//   * @param type
//   * @param installedBy
//   * @param invocationType
//   */
//  public BeanInstanceAccessSetup(BeanSetup bean) {
//      super(bean, OperationType.of(bean.beanClass), null, null);
//  }
//
//  /** {@inheritDoc} */
//  @Override
//  public MethodHandle methodHandle() {
//      throw new UnsupportedOperationException();
//  }
//
//  /** {@inheritDoc} */
//  @Override
//  public boolean isStatic() {
//      return false;
//  }
//}
//
///** Represents a field access on a bean */
//public static final class BeanFieldAccessSetup extends BeanOperationSetup {
//
//  /** The access mode. */
//  public final AccessMode accessMode;
//
//  /** The field that is accessed. */
//  public final Field field;
//
//  /** A direct method handle for the field and accessMode. */
//  public final MethodHandle methodHandle;
//
//  /**
//   * @param bean
//   *            the bean where the field is located
//   * @param operator
//   *            the extension where the operating bean that will access the field is located
//   * @param invocationType
//   *            the invocation type that the operating bean will use
//   * @param field
//   *            the field
//   * @param accessMode
//   *            the access mode
//   * @param methodHandle
//   *            a method handle for accessing the field
//   */
//  public BeanFieldAccessSetup(BeanSetup bean, ExtensionSetup operator, InvocationType invocationType, Field field, AccessMode accessMode,
//          MethodHandle methodHandle) {
//      super(bean, OperationType.ofFieldAccess(field, accessMode), operator, invocationType);
//      this.field = field;
//      this.accessMode = accessMode;
//      this.methodHandle = methodHandle;
//  }
//
//  /** {@inheritDoc} */
//  @Override
//  public MethodHandle methodHandle() {
//      return methodHandle;
//  }
//
//  /** {@inheritDoc} */
//  @Override
//  public boolean isStatic() {
//      return Modifier.isStatic(field.getModifiers());
//  }
//}
//
//public static final class BeanMethodInvokeSetup extends BeanOperationSetup {
//
//  public final Method method;
//
//  public final MethodHandle methodHandle;
//
//  /**
//   * @param bean
//   * @param type
//   * @param operator
//   * @param invocationType
//   * @param target
//   */
//  public BeanMethodInvokeSetup(BeanSetup bean, ExtensionSetup operator, OperationType operationType, InvocationType invocationType, Method method,
//          MethodHandle methodHandle) {
//      super(bean, operationType, operator, invocationType);
//      this.method = method;
//      this.methodHandle = methodHandle;
//  }
//
//  /** {@inheritDoc} */
//  @Override
//  public MethodHandle methodHandle() {
//      return methodHandle;
//  }
//
//  /** {@inheritDoc} */
//  @Override
//  public boolean isStatic() {
//      return Modifier.isStatic(method.getModifiers());
//  }
//}