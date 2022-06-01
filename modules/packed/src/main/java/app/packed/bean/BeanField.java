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
package app.packed.bean;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.inject.Variable;
import app.packed.operation.InjectableOperationHandle;
import packed.internal.bean.hooks.PackedBeanField;

/**
 * This class represents a {@link Field} on a bean.
 * 
 * @see Extension#hookOnBeanField(BeanField)
 */
public sealed interface BeanField extends BeanElement permits PackedBeanField {

    /** {@return the underlying field.} */
    Field field();

    /**
     * {@return the modifiers of the field.}
     * 
     * @see Field#getModifiers()
     * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Field#getModifiers()}
     */
    int getModifiers();

    /**
     * Returns a method handle that gives read access to the underlying field as specified by
     * {@link Lookup#unreflectGetter(Field)}.
     * 
     * @return a method handle getter
     */
    InjectableOperationHandle newGetOperation(ExtensionBeanConfiguration<?> operator);

    /**
     * @param operator
     * @param accessMode
     * @return
     * 
     * @see VarHandle#toMethodHandle(java.lang.invoke.VarHandle.AccessMode)
     */
    InjectableOperationHandle newOperation(ExtensionBeanConfiguration<?> operator, VarHandle.AccessMode accessMode);

    /**
     * Returns a method handle that gives write access to the underlying field as specified by
     * {@link Lookup#unreflectSetter(Field)}.
     * 
     * @return a method handle setter
     */
    InjectableOperationHandle newSetOperation(ExtensionBeanConfiguration<?> operator);

    // Or maybe just rawVarHandle() on IOH
    default VarHandle varHandleOf(InjectableOperationHandle handle) { 
        throw new UnsupportedOperationException();
    }
    
    /**
     * {@return the underlying field represented as a {@code Variable}.}
     * 
     * @see Variable#ofField(Field)
     */
    Variable variable(); //mayby toVariable

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface AnnotatedWithHook {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        boolean allowGet() default false;

        /** Whether or not the sidecar is allow to set the contents of a field. */
        boolean allowSet() default false;

        /** The hook's {@link BeanField} class. */
        Class<? extends Extension<?>> extension();

        // Altsaa vi har jo ikke lukket for at vi senere kan goere nogle andre ting...
        // Class<Supplier<? extends BeanMethod>> bootstrap();
    }

}

interface Zandbox {

    // BeanInfo

    default int beanFieldId() {
        // IDeen er lidt at fields (og methods) har et unikt id...
        // Som man saa kan sammenligne med
        // Problemet er metoder med baade @ScheduleAtFixedRate og @ScheduleAtVariableRate
        // Maaske skal vi droppe Class<? extends Annotation> som parameter
        return 1;
    }
    // Can only read stuff...
    // Then we can just passe it off to anyone
    // IDK know about usecases
    BeanField unmodifiable();
}