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

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.lifecycle.Initialize;
import app.packed.bean.lifecycle.Inject;
import app.packed.bean.lifecycle.OnStart;
import app.packed.bean.lifecycle.OnStartContext;
import app.packed.bean.lifecycle.Stop;
import app.packed.bean.lifecycle.StopContext;
import app.packed.bean.lifecycle.UnsupportedLifecycleException;
import app.packed.bean.scanning.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.context.ContextTemplate;
import app.packed.extension.BaseExtension;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.IntrospectorOnField;
import internal.app.packed.bean.scanning.IntrospectorOnMethod;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOnStartHandle;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationInitializeHandle;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationStopHandle;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.operation.PackedOp.NewOperation;
import internal.app.packed.operation.PackedOperationTemplate;

/**
 * Used by {@link app.packed.extension.BaseExtension} for its {@link app.packed.bean.scanning.BeanIntrospector}.
 */
public final class LifecycleAnnotationBeanIntrospector extends BeanIntrospector<BaseExtension> {

    /** A context template for {@link StartContext}. */
    private static final ContextTemplate CONTEXT_ON_START_TEMPLATE = ContextTemplate.of(OnStartContext.class);

    /** A context template for {@link StopContext}. */
    private static final ContextTemplate CONTEXT_ON_STOP_TEMPLATE = ContextTemplate.of(StopContext.class);

    /** An operation template for {@link Inject} and {@link Initialize}. */
    private static final OperationTemplate OPERATION_LIFECYCLE_TEMPLATE = OperationTemplate.defaults().withReturnIgnore();

    /** An operation template for {@link Start}. */
    private static final OperationTemplate OPERATION_ON_START_TEMPLATE = OperationTemplate.defaults().withReturnIgnore().withContext(CONTEXT_ON_START_TEMPLATE);

    /** An operation template for {@link Stop}. */
    private static final OperationTemplate OPERATION_ON_STOP_TEMPLATE = OperationTemplate.defaults().withReturnIgnore().withContext(CONTEXT_ON_STOP_TEMPLATE);

    /** Handles {@link Inject}. */
    @Override
    public void onAnnotatedField(Annotation annotation, OnField onField) {
        IntrospectorOnField field = (IntrospectorOnField) onField;
        BeanSetup bean = field.bean();

        if (annotation instanceof Inject) {
            checkNotStaticBean(bean, Inject.class);

            // TODO we need wrap/unwrap
            BeanLifecycleOperationHandle handle = field.newSetOperation(OPERATION_LIFECYCLE_TEMPLATE)
                    .install(i -> new LifecycleOperationInitializeHandle(i, InternalBeanLifecycleKind.INJECT));

            bean.operations.addLifecycleHandle(handle);

            // checkNotStatic
            // Det er jo inject service!???
            // field.newBindableVariable().unwrap();
            // OperationHandle handle = field.newSetOperation(null) .newOperation(temp);
            // bean.lifecycle.addInitialize(handle, null);
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
        IntrospectorOnMethod m = (IntrospectorOnMethod) method;

        BeanSetup bean = m.bean();
        BeanLifecycleOperationHandle handle;

        if (annotation instanceof Inject) {
            checkNotStaticBean(bean, Inject.class);
            handle = method.newOperation(OPERATION_LIFECYCLE_TEMPLATE)
                    .install(i -> new LifecycleOperationInitializeHandle(i, InternalBeanLifecycleKind.INJECT));
        } else if (annotation instanceof Initialize oi) {
            checkNotStaticBean(bean, Initialize.class);
            handle = method.newOperation(OPERATION_LIFECYCLE_TEMPLATE).install(i -> new LifecycleOperationInitializeHandle(i, oi));
        } else if (annotation instanceof OnStart oi) {
            checkNotStaticBean(bean, OnStart.class);
            handle = method.newOperation(OPERATION_ON_START_TEMPLATE).install(i -> new LifecycleOnStartHandle(i, oi));
        } else if (annotation instanceof Stop oi) {
            checkNotStaticBean(bean, Stop.class);
            handle = method.newOperation(OPERATION_ON_STOP_TEMPLATE).install(i -> new LifecycleOperationStopHandle(i, oi));
        } else {
            return;
        }

        bean.operations.addLifecycleHandle(handle);
    }

    public static void checkForFactoryOp(BeanSetup bean) {
        // Creating an bean factory operation representing the Op if an Op was specified when creating the bean.
        if (bean.beanSourceKind == BeanSourceKind.OP) {
            PackedOp<?> op = (PackedOp<?>) bean.beanSource;

            PackedOperationTemplate ot = bean.template.initializationTemplate();
            // if (ot.returnKind == ReturnKind.DYNAMIC) {
            // ot = ot.configure(c -> c.returnType(beanClass));
            // }

//            ot = bean.template.initializationTemplate()

            // What if no scanning and OP?????
            OperationSetup os = op.newOperationSetup(
                    new NewOperation(bean, bean.installedBy, ot, i -> new LifecycleOperationInitializeHandle(i, InternalBeanLifecycleKind.FACTORY), null));
            bean.operations.addLifecycleHandle((BeanLifecycleOperationHandle) os.handle());
        }

    }


    // Maybe we automatically fail on bean triggers on instance members.
    private static <H extends OperationHandle<?>> void checkNotStaticBean(BeanSetup bean, Class<? extends Annotation> annotationType) {
        if (bean.beanKind == BeanKind.STATIC) {
            throw new UnsupportedLifecycleException(annotationType + " is not supported for static beans");
        }
    }

    @Override
    public void onExtensionService(Key<?> key, OnExtensionService service) {
        OnVariableUnwrapped binding = service.binder();
        testExtensionService(service, binding);
    }

    static boolean testExtensionService(OnExtensionService service, OnVariableUnwrapped binding) {
        if (service.match(OnStartContext.class)) {
            binding.bindContext(OnStartContext.class);
            return true;
        } else if (service.match(StopContext.class)) {
            binding.bindContext(StopContext.class);
            return true;
        }
        return false;
    }

    static void testMethodAnnotation(NewIntrospector<BaseExtension> ni) {
        ni.onAnnotatedMethod(Initialize.class).install(OPERATION_LIFECYCLE_TEMPLATE, i -> {
            LifecycleOperationInitializeHandle h = new LifecycleOperationInitializeHandle(i, i.value());
            // hack.bean.operations.addHandle(h);
            return h;
        });

    }
}
