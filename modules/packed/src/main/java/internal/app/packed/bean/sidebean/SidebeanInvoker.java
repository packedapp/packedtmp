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

import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicLong;

import internal.app.packed.extension.ExtensionContext;

/**
 * Generates implementations of SAM (Single Abstract Method) interfaces using the Class File API.
 * The generated class delegates to a MethodHandle with ExtensionContext as the first argument.
 */
public class SidebeanInvoker {

    private static final AtomicLong COUNTER = new AtomicLong();

    private static final ClassDesc CD_MethodHandle = ClassDesc.of("java.lang.invoke.MethodHandle");
    private static final ClassDesc CD_ExtensionContext = ClassDesc.of("internal.app.packed.extension.ExtensionContext");
    private static final ClassDesc CD_Object = ClassDesc.of("java.lang.Object");

    /**
     * Generates a MethodHandle that creates instances implementing the given SAM interface.
     *
     * @param iface the SAM interface to implement
     * @return a MethodHandle with signature (MethodHandle, ExtensionContext) -> iface
     */
    public static MethodHandle generateInvoker(Class<?> iface) {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }

        Method samMethod = findSamMethod(iface);
        if (samMethod == null) {
            throw new IllegalArgumentException(iface + " is not a SAM interface");
        }

        String className = iface.getPackageName() + ".SidebeanInvoker$" + iface.getSimpleName() + "$" + COUNTER.incrementAndGet();
        ClassDesc classDesc = ClassDesc.of(className);
        ClassDesc ifaceDesc = ClassDesc.ofDescriptor(iface.descriptorString());

        byte[] bytes = ClassFile.of().build(classDesc, clb -> {
            clb.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC);
            clb.withSuperclass(CD_Object);
            clb.withInterfaceSymbols(ifaceDesc);

            // Fields
            clb.withField("methodHandle", CD_MethodHandle, ClassFile.ACC_PRIVATE | ClassFile.ACC_FINAL);
            clb.withField("extensionContext", CD_ExtensionContext, ClassFile.ACC_PRIVATE | ClassFile.ACC_FINAL);

            // Constructor: (MethodHandle, ExtensionContext)
            MethodTypeDesc ctorDesc = MethodTypeDesc.of(ClassDesc.ofDescriptor("V"), CD_MethodHandle, CD_ExtensionContext);
            clb.withMethod("<init>", ctorDesc, ClassFile.ACC_PUBLIC, mb -> {
                mb.withCode(cb -> {
                    // super()
                    cb.aload(0);
                    cb.invokespecial(CD_Object, "<init>", MethodTypeDesc.of(ClassDesc.ofDescriptor("V")));

                    // this.methodHandle = methodHandle
                    cb.aload(0);
                    cb.aload(1);
                    cb.putfield(classDesc, "methodHandle", CD_MethodHandle);

                    // this.extensionContext = extensionContext
                    cb.aload(0);
                    cb.aload(2);
                    cb.putfield(classDesc, "extensionContext", CD_ExtensionContext);

                    cb.return_();
                });
            });

            // SAM method implementation
            MethodTypeDesc samDesc = MethodTypeDesc.ofDescriptor(getMethodDescriptor(samMethod));
            clb.withMethod(samMethod.getName(), samDesc, ClassFile.ACC_PUBLIC, mb -> {
                mb.withCode(cb -> {
                    generateSamMethodBody(cb, classDesc, samMethod);
                });
            });
        });

        // Define the class using MethodHandles.Lookup
        // Use privateLookupIn to get access to the interface's package
        // This requires the interface's module to open its package to this module
        try {
            MethodHandles.Lookup ifaceLookup = MethodHandles.privateLookupIn(iface, MethodHandles.lookup());
            MethodHandles.Lookup definedLookup = ifaceLookup.defineHiddenClass(bytes, true);
            Class<?> generatedClass = definedLookup.lookupClass();

            MethodHandle constructor = definedLookup.findConstructor(generatedClass,
                MethodType.methodType(void.class, MethodHandle.class, ExtensionContext.class));

            // Change return type from generated class to the interface
            return constructor.asType(MethodType.methodType(iface, MethodHandle.class, ExtensionContext.class));
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to generate invoker for " + iface, e);
        }
    }

    private static void generateSamMethodBody(CodeBuilder cb, ClassDesc classDesc, Method samMethod) {
        Class<?>[] paramTypes = samMethod.getParameterTypes();
        Class<?> returnType = samMethod.getReturnType();
        int paramCount = paramTypes.length;

        // Load this.methodHandle
        cb.aload(0);
        cb.getfield(classDesc, "methodHandle", CD_MethodHandle);

        // Create Object[] array with size = 1 (extensionContext) + paramCount
        cb.ldc(1 + paramCount);
        cb.anewarray(CD_Object);

        // array[0] = this.extensionContext
        cb.dup();
        cb.ldc(0);
        cb.aload(0);
        cb.getfield(classDesc, "extensionContext", CD_ExtensionContext);
        cb.aastore();

        // array[1..n] = method parameters (boxed if primitive)
        int localSlot = 1;
        for (int i = 0; i < paramCount; i++) {
            cb.dup();
            cb.ldc(i + 1);
            Class<?> paramType = paramTypes[i];
            localSlot = loadAndBox(cb, paramType, localSlot);
            cb.aastore();
        }

        // Invoke methodHandle.invokeWithArguments(array)
        cb.invokevirtual(CD_MethodHandle, "invokeWithArguments",
            MethodTypeDesc.of(CD_Object, CD_Object.arrayType()));

        // Handle return value
        if (returnType == void.class) {
            cb.pop();
            cb.return_();
        } else if (returnType.isPrimitive()) {
            unboxAndReturn(cb, returnType);
        } else {
            cb.checkcast(ClassDesc.ofDescriptor(returnType.descriptorString()));
            cb.areturn();
        }
    }

    private static int loadAndBox(CodeBuilder cb, Class<?> type, int slot) {
        if (type == int.class) {
            cb.iload(slot);
            cb.invokestatic(ClassDesc.of("java.lang.Integer"), "valueOf",
                MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ClassDesc.ofDescriptor("I")));
            return slot + 1;
        } else if (type == long.class) {
            cb.lload(slot);
            cb.invokestatic(ClassDesc.of("java.lang.Long"), "valueOf",
                MethodTypeDesc.of(ClassDesc.of("java.lang.Long"), ClassDesc.ofDescriptor("J")));
            return slot + 2;
        } else if (type == double.class) {
            cb.dload(slot);
            cb.invokestatic(ClassDesc.of("java.lang.Double"), "valueOf",
                MethodTypeDesc.of(ClassDesc.of("java.lang.Double"), ClassDesc.ofDescriptor("D")));
            return slot + 2;
        } else if (type == float.class) {
            cb.fload(slot);
            cb.invokestatic(ClassDesc.of("java.lang.Float"), "valueOf",
                MethodTypeDesc.of(ClassDesc.of("java.lang.Float"), ClassDesc.ofDescriptor("F")));
            return slot + 1;
        } else if (type == boolean.class) {
            cb.iload(slot);
            cb.invokestatic(ClassDesc.of("java.lang.Boolean"), "valueOf",
                MethodTypeDesc.of(ClassDesc.of("java.lang.Boolean"), ClassDesc.ofDescriptor("Z")));
            return slot + 1;
        } else if (type == byte.class) {
            cb.iload(slot);
            cb.invokestatic(ClassDesc.of("java.lang.Byte"), "valueOf",
                MethodTypeDesc.of(ClassDesc.of("java.lang.Byte"), ClassDesc.ofDescriptor("B")));
            return slot + 1;
        } else if (type == short.class) {
            cb.iload(slot);
            cb.invokestatic(ClassDesc.of("java.lang.Short"), "valueOf",
                MethodTypeDesc.of(ClassDesc.of("java.lang.Short"), ClassDesc.ofDescriptor("S")));
            return slot + 1;
        } else if (type == char.class) {
            cb.iload(slot);
            cb.invokestatic(ClassDesc.of("java.lang.Character"), "valueOf",
                MethodTypeDesc.of(ClassDesc.of("java.lang.Character"), ClassDesc.ofDescriptor("C")));
            return slot + 1;
        } else {
            cb.aload(slot);
            return slot + 1;
        }
    }

    private static void unboxAndReturn(CodeBuilder cb, Class<?> type) {
        if (type == int.class) {
            cb.checkcast(ClassDesc.of("java.lang.Integer"));
            cb.invokevirtual(ClassDesc.of("java.lang.Integer"), "intValue",
                MethodTypeDesc.of(ClassDesc.ofDescriptor("I")));
            cb.ireturn();
        } else if (type == long.class) {
            cb.checkcast(ClassDesc.of("java.lang.Long"));
            cb.invokevirtual(ClassDesc.of("java.lang.Long"), "longValue",
                MethodTypeDesc.of(ClassDesc.ofDescriptor("J")));
            cb.lreturn();
        } else if (type == double.class) {
            cb.checkcast(ClassDesc.of("java.lang.Double"));
            cb.invokevirtual(ClassDesc.of("java.lang.Double"), "doubleValue",
                MethodTypeDesc.of(ClassDesc.ofDescriptor("D")));
            cb.dreturn();
        } else if (type == float.class) {
            cb.checkcast(ClassDesc.of("java.lang.Float"));
            cb.invokevirtual(ClassDesc.of("java.lang.Float"), "floatValue",
                MethodTypeDesc.of(ClassDesc.ofDescriptor("F")));
            cb.freturn();
        } else if (type == boolean.class) {
            cb.checkcast(ClassDesc.of("java.lang.Boolean"));
            cb.invokevirtual(ClassDesc.of("java.lang.Boolean"), "booleanValue",
                MethodTypeDesc.of(ClassDesc.ofDescriptor("Z")));
            cb.ireturn();
        } else if (type == byte.class) {
            cb.checkcast(ClassDesc.of("java.lang.Byte"));
            cb.invokevirtual(ClassDesc.of("java.lang.Byte"), "byteValue",
                MethodTypeDesc.of(ClassDesc.ofDescriptor("B")));
            cb.ireturn();
        } else if (type == short.class) {
            cb.checkcast(ClassDesc.of("java.lang.Short"));
            cb.invokevirtual(ClassDesc.of("java.lang.Short"), "shortValue",
                MethodTypeDesc.of(ClassDesc.ofDescriptor("S")));
            cb.ireturn();
        } else if (type == char.class) {
            cb.checkcast(ClassDesc.of("java.lang.Character"));
            cb.invokevirtual(ClassDesc.of("java.lang.Character"), "charValue",
                MethodTypeDesc.of(ClassDesc.ofDescriptor("C")));
            cb.ireturn();
        }
    }

    private static Method findSamMethod(Class<?> iface) {
        Method samMethod = null;
        for (Method m : iface.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers()) && !isObjectMethod(m)) {
                if (samMethod != null) {
                    return null;
                }
                samMethod = m;
            }
        }
        return samMethod;
    }

    private static boolean isObjectMethod(Method m) {
        String name = m.getName();
        Class<?>[] params = m.getParameterTypes();
        if (name.equals("equals") && params.length == 1 && params[0] == Object.class) return true;
        if (name.equals("hashCode") && params.length == 0) return true;
        if (name.equals("toString") && params.length == 0) return true;
        return false;
    }

    private static String getMethodDescriptor(Method m) {
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> p : m.getParameterTypes()) {
            sb.append(p.descriptorString());
        }
        sb.append(")");
        sb.append(m.getReturnType().descriptorString());
        return sb.toString();
    }
}
