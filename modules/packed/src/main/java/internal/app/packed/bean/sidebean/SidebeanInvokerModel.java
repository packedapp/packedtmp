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
package internal.app.packed.bean.sidebean;

import static java.lang.classfile.ClassFile.ACC_FINAL;
import static java.lang.classfile.ClassFile.ACC_PRIVATE;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.classfile.ClassFile.ACC_SYNTHETIC;
import static java.lang.constant.ConstantDescs.CD_MethodHandle;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_Throwable;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.lang.constant.ConstantDescs.INIT_NAME;

import java.lang.classfile.ClassFile;
import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import internal.app.packed.extension.ExtensionContext;

/**
 * Generates implementations of SAM (Single Abstract Method) interfaces using the Class File API. The generated class
 * delegates to a MethodHandle with ExtensionContext as the first argument.
 *
 * @implNote the main reason we do not use {@link java.lang.invoke.MethodHandleProxies} is that they require the
 *           interface to be public.
 */
public final class SidebeanInvokerModel {

    private static final ClassDesc CD_Error = ClassDesc.of(Error.class.getName());
    private static final ClassDesc CD_ExtensionContext = ClassDesc.of(ExtensionContext.class.getName());
    private static final ClassDesc CD_RuntimeException = ClassDesc.of(RuntimeException.class.getName());
    private static final ClassDesc CD_UndeclaredThrowableException = ClassDesc.of(UndeclaredThrowableException.class.getName());

    private final Supplier<MethodHandle> constructor;

    public final Class<?> iface;

    private final Method method;

    private SidebeanInvokerModel(Class<?> iface, Method method) {
        this.iface = iface;
        this.method = method;
        this.constructor = StableValue.supplier(() -> generateInvoker(iface, method));
    }

    public MethodHandle constructor() {
        return constructor.get();
    }

    public Class<?> returnType() {
        return method.getReturnType();
    }

    private static Method findSamMethod(Class<?> iface) {
        if (!iface.isInterface())
            throw new IllegalArgumentException(iface + " is not an interface");
        Method sam = null;
        for (Method m : iface.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers()) && !isObjectMethod(m)) {
                if (sam != null)
                    throw new IllegalArgumentException(iface + " has multiple abstract methods");
                sam = m;
            }
        }
        if (sam == null)
            throw new IllegalArgumentException(iface + " is not a SAM interface");
        return sam;
    }

    static MethodHandle generateInvoker(Class<?> iface) {
        return generateInvoker(iface, findSamMethod(iface));
    }

    /**
     * Generates a MethodHandle that creates instances implementing the given SAM interface.
     *
     * @param iface
     *            the SAM interface to implement
     * @return a MethodHandle with signature (MethodHandle, ExtensionContext) -> iface
     */
    private static MethodHandle generateInvoker(Class<?> iface, Method sam) {

        // Define names and descriptors
        // Hidden classes automatically get a unique suffix, so "SidebeanInvokerImpl" is fine as a prefix.
        ClassDesc cd = ClassDesc.of(iface.getPackageName(), "SidebeanInvokerImpl");
        ClassDesc ifaceDesc = ClassDesc.ofDescriptor(iface.descriptorString());

        MethodTypeDesc samDesc = MethodTypeDesc.ofDescriptor(MethodType.methodType(sam.getReturnType(), sam.getParameterTypes()).descriptorString());
        MethodTypeDesc invokeDesc = samDesc.insertParameterTypes(0, CD_ExtensionContext);

        byte[] bytes = ClassFile.of().build(cd, clb -> {
            clb.withFlags(ACC_PUBLIC | ACC_FINAL | ACC_SYNTHETIC);
            clb.withInterfaceSymbols(ifaceDesc);

            // Fields to hold the state
            clb.withField("mh", CD_MethodHandle, ACC_PRIVATE | ACC_FINAL);
            clb.withField("ctx", CD_ExtensionContext, ACC_PRIVATE | ACC_FINAL);

            // Constructor: (MethodHandle, ExtensionContext)
            clb.withMethod(INIT_NAME, MethodTypeDesc.of(CD_void, CD_MethodHandle, CD_ExtensionContext), ACC_PUBLIC, mb -> mb.withCode(cb -> {
                cb.aload(0);
                cb.invokespecial(CD_Object, INIT_NAME, MethodTypeDesc.of(CD_void));
                cb.aload(0);
                cb.aload(1);
                cb.putfield(cd, "mh", CD_MethodHandle);
                cb.aload(0);
                cb.aload(2);
                cb.putfield(cd, "ctx", CD_ExtensionContext);
                cb.return_();
            }));

            // SAM Implementation
            clb.withMethod(sam.getName(), samDesc, ACC_PUBLIC, mb -> mb.withCode(cb -> {
                Label start = cb.newLabel();
                Label end = cb.newLabel();
                Label handlerRethrow = cb.newLabel();
                Label handlerWrap = cb.newLabel();

                // 1. Define Exception Table
                List<ClassDesc> toPropagate = new ArrayList<>(List.of(CD_RuntimeException, CD_Error));
                for (Class<?> ex : sam.getExceptionTypes()) {
                    toPropagate.add(ClassDesc.ofDescriptor(ex.descriptorString()));
                }

                for (ClassDesc exDesc : toPropagate) {
                    cb.exceptionCatch(start, end, handlerRethrow, exDesc);
                }
                cb.exceptionCatch(start, end, handlerWrap, CD_Throwable);

                // 2. Method Body
                cb.labelBinding(start);
                cb.aload(0);
                cb.getfield(cd, "mh", CD_MethodHandle);
                cb.aload(0);
                cb.getfield(cd, "ctx", CD_ExtensionContext);

                int slot = 1;
                for (Class<?> p : sam.getParameterTypes()) {
                    TypeKind kind = TypeKind.from(p);
                    cb.loadLocal(kind, slot);
                    slot += kind.slotSize();
                }

                // Call MethodHandle.invoke(extensionContext, ...args)
                cb.invokevirtual(CD_MethodHandle, "invoke", invokeDesc);
                cb.labelBinding(end);
                cb.return_(TypeKind.from(sam.getReturnType()));

                // 3. Exception Handlers

                // Rethrow: Just throw the exception that is already on the stack
                cb.labelBinding(handlerRethrow);
                cb.athrow();

                // Wrap: Undeclared checked exceptions
                cb.labelBinding(handlerWrap);
                cb.astore(slot); // Save the caught Throwable to a local variable
                cb.new_(CD_UndeclaredThrowableException);
                cb.dup();
                cb.aload(slot); // Load the cause for the constructor
                cb.invokespecial(CD_UndeclaredThrowableException, INIT_NAME, MethodTypeDesc.of(CD_void, CD_Throwable));
                cb.athrow();
            }));
        });

        try {
            // Using privateLookupIn allows implementation of package-private interfaces
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(iface, MethodHandles.lookup()).defineHiddenClass(bytes, true);

            return lookup.findConstructor(lookup.lookupClass(), MethodType.methodType(void.class, MethodHandle.class, ExtensionContext.class))
                    .asType(MethodType.methodType(iface, MethodHandle.class, ExtensionContext.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to generate invoker for " + iface.getName(), e);
        }
    }

    private static boolean isObjectMethod(Method m) {
        return switch (m.getName()) {
        case "equals" -> m.getParameterCount() == 1 && m.getParameterTypes()[0] == Object.class;
        case "hashCode", "toString" -> m.getParameterCount() == 0;
        default -> false;
        };
    }

    public static SidebeanInvokerModel of(Class<?> iface) {
        Method sam = findSamMethod(iface);
        SidebeanInvokerModel sim = new SidebeanInvokerModel(iface, sam);
        return sim;
    }
}