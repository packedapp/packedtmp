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
package app.packed.bean.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

import app.packed.base.Variable;
import app.packed.bean.operation.InjectableOperationHandle;
import app.packed.bean.operation.RawOperationHandle;
import app.packed.extension.Extension;
import packed.internal.bean.hooks.PackedBeanField;

/**
 *
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

    InjectableOperationHandle newOperation(VarHandle.AccessMode accessMode);

    InjectableOperationHandle newOperationGetter();

    InjectableOperationHandle newOperationSetter();

    /**
     * Returns a method handle that gives read access to the underlying field as specified by
     * {@link Lookup#unreflectGetter(Field)}.
     * 
     * @return a method handle getter
     */
    RawOperationHandle<MethodHandle> newRawGetterOperation();

    /**
     * Must have both get and set
     * 
     * @return the variable
     * @see Lookup#unreflectVarHandle(Field)
     * @see BeanField.Hook#allowGet()
     * @see BeanField.Hook#allowSet()
     * @throws UnsupportedOperationException
     *             if the extension field has not both get and set access
     */
    RawOperationHandle<VarHandle> newRawOperation();

    /**
     * Returns a method handle that gives write access to the underlying field as specified by
     * {@link Lookup#unreflectSetter(Field)}.
     * 
     * @return a method handle setter
     */
    RawOperationHandle<MethodHandle> newRawSetterOperation();
    
    /**
     * {@return the underlying field represented as a {@code Variable}.}
     * 
     * @see Variable#ofField(Field)
     */
    Variable variable();

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface Hook {

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

interface Sandbox {

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