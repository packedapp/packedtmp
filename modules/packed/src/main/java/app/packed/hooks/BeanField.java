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
package app.packed.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.base.Variable;
import app.packed.bean.hooks.OldBeanField;
import app.packed.bean.hooks.OldBeanFieldHook;
import app.packed.component.Realm;
import app.packed.extension.Extension;

/**
 *
 */
public interface BeanField {

    // BeanInfo
    
    default int beanFieldId() {
        // IDeen er lidt at fields (og methods) har et unikt id...
        // Som man saa kan sammenligne med
        // Problemet er metoder med baade @ScheduleAtFixedRate og @ScheduleAtVariableRate
        // Maaske skal vi droppe Class<? extends Annotation> som parameter
        return 1;
    }
    /**
     * Returns the underlying field.
     * 
     * @return the underlying field
     */
    Field field();

    /**
     * Returns the modifiers of the field.
     * 
     * @return the modifiers of the field
     * @see Field#getModifiers()
     * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Method#getModifiers()}
     */
    int getModifiers();

    /**
     * @return a method handle getter
     */
    MethodHandle methodHandleGetter();

    MethodHandle methodHandleSetter();

    BeanOperation operation(VarHandle.AccessMode accessMode);

    BeanOperation operationGetter();
    
    BeanOperation operationSetter();

    /**
     * Must have both get and set
     * 
     * @return the variable
     * @see Lookup#unreflectVarHandle(Field)
     * @see OldBeanFieldHook#allowGet()
     * @see OldBeanFieldHook#allowSet()
     * @throws UnsupportedOperationException
     *             if the extension field has not both get and set access
     */
    VarHandle varHandle();

    /**
     * {@return the underlying represented as a {@code Variable}.}
     * 
     * @see Variable#ofField(Field)
     */
    Variable variable();
    
    Realm realm();
    
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface Hook {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        boolean allowGet() default false;

        /** Whether or not the sidecar is allow to set the contents of a field. */
        boolean allowSet() default false;

        /** The hook's {@link OldBeanField} class. */
        Class<? extends Extension<?>> extension();

        // Altsaa vi har jo ikke lukket for at vi senere kan goere nogle andre ting...
        // Class<Supplier<? extends BeanMethod>> bootstrap();
    }

}
