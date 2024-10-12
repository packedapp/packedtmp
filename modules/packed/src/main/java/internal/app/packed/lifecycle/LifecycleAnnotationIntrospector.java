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
package internal.app.packed.lifecycle;

import java.lang.annotation.Annotation;
import java.util.Set;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.lifecycle.Initialize;
import app.packed.bean.lifecycle.Inject;
import app.packed.bean.lifecycle.Start;
import app.packed.bean.lifecycle.StartContext;
import app.packed.bean.lifecycle.Stop;
import app.packed.bean.lifecycle.StopContext;
import app.packed.bean.lifecycle.UnsupportedLifecycleException;
import app.packed.binding.Key;
import app.packed.binding.UnwrappedBindableVariable;
import app.packed.context.Context;
import app.packed.context.ContextTemplate;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.PackedBeanField;
import internal.app.packed.bean.scanning.PackedBeanMethod;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationInitializeHandle;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationStartHandle;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationStopHandle;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.operation.PackedOp.NewOS;
import internal.app.packed.operation.PackedOperationTemplate;

/**
 * Used by {@link app.packed.extension.BaseExtension} for its {@link app.packed.bean.scanning.BeanIntrospector}.
 */
public final class LifecycleAnnotationIntrospector {

    /** A template for bean lifecycle operations. */
    private static final OperationTemplate BEAN_LIFECYCLE_TEMPLATE = OperationTemplate.of(c -> c.returnIgnore());

    static final ContextTemplate ON_START_CONTEXT_TEMPLATE = ContextTemplate.of(StartContext.class);

    static final ContextTemplate ON_STOP_CONTEXT_TEMPLATE = ContextTemplate.of(StopContext.class);

    private static final OperationTemplate BEAN_LIFECYCLE_ON_START_TEMPLATE = OperationTemplate.of(c -> c.returnIgnore().inContext(ON_START_CONTEXT_TEMPLATE));

    private static final OperationTemplate BEAN_LIFECYCLE_ON_STOP_TEMPLATE = OperationTemplate.of(c -> c.returnIgnore().inContext(ON_STOP_CONTEXT_TEMPLATE));

    public static void checkForFactoryOp(BeanSetup bean) {
        // Creating an bean factory operation representing the Op if an Op was specified when creating the bean.
        if (bean.beanSourceKind == BeanSourceKind.OP) {
            PackedOp<?> op = (PackedOp<?>) bean.beanSource;

            PackedOperationTemplate   ot = bean.template.initializationTemplate();
            //  if (ot.returnKind == ReturnKind.DYNAMIC) {
            //      ot = ot.configure(c -> c.returnType(beanClass));
             // }

//            ot = bean.template.initializationTemplate()

            // What if no scanning and OP?????
            OperationSetup os = op.newOperationSetup(
                    new NewOS(bean, bean.installedBy, ot, i -> new LifecycleOperationInitializeHandle(i, InternalBeanLifecycleKind.FACTORY), null));
            bean.operations.addHandle((BeanLifecycleOperationHandle) os.handle());
        }

    }

    public static boolean testContextualService(Key<?> key, Class<?> actualHook, Set<Class<? extends Context<?>>> contexts, UnwrappedBindableVariable binding) {
        Class<?> hook = key.rawType();
        if (hook == StartContext.class) {
            // v.bindInvocationArgument(1);
            binding.bindContext(StartContext.class);
        } else if (hook == StopContext.class) {
            // v.bindInvocationArgument(1);
            binding.bindContext(StopContext.class);
        } else {
            return false;
        }
        return true;
    }

    public static boolean testFieldAnnotation(PackedBeanField field, Annotation annotation) {
        BeanSetup bean = field.bean();

        if (annotation instanceof Inject) {
            checkNotStaticBean(bean, Inject.class);

            // TODO we need wrap/unwrap
            BeanLifecycleOperationHandle handle = field.newSetOperation(BEAN_LIFECYCLE_TEMPLATE)
                    .install(i -> new LifecycleOperationInitializeHandle(i, InternalBeanLifecycleKind.INJECT));

            bean.operations.addHandle(handle);

            // checkNotStatic
            // Det er jo inject service!???
            // field.newBindableVariable().unwrap();
            // OperationHandle handle = field.newSetOperation(null) .newOperation(temp);
            // bean.lifecycle.addInitialize(handle, null);
            throw new UnsupportedOperationException();
        }
        return false;
    }

    public static boolean testMethodAnnotation(PackedBeanMethod method, Annotation annotation) {
        BeanSetup bean = method.bean();
        BeanLifecycleOperationHandle handle;

        if (annotation instanceof Inject) {
            checkNotStaticBean(bean, Inject.class);
            handle = method.newOperation(BEAN_LIFECYCLE_TEMPLATE).install(i -> new LifecycleOperationInitializeHandle(i, InternalBeanLifecycleKind.INJECT));
        } else if (annotation instanceof Initialize oi) {
            checkNotStaticBean(bean, Initialize.class);
            handle = method.newOperation(BEAN_LIFECYCLE_TEMPLATE).install(i -> new LifecycleOperationInitializeHandle(i, oi));
        } else if (annotation instanceof Start oi) {
            checkNotStaticBean(bean, Start.class);
            handle = method.newOperation(BEAN_LIFECYCLE_ON_START_TEMPLATE).install(i -> new LifecycleOperationStartHandle(i, oi));
        } else if (annotation instanceof Stop oi) {
            checkNotStaticBean(bean, Stop.class);
            handle = method.newOperation(BEAN_LIFECYCLE_ON_STOP_TEMPLATE).install(i -> new LifecycleOperationStopHandle(i, oi));
        } else {
            return false;
        }

        bean.operations.addHandle(handle);
        return true;
    }

    private static <H extends OperationHandle<?>> void checkNotStaticBean(BeanSetup bean, Class<? extends Annotation> annotationType) {
        if (bean.beanKind == BeanKind.STATIC) {
            throw new UnsupportedLifecycleException(annotationType + " is not supported for static beans");
        }
    }
}
