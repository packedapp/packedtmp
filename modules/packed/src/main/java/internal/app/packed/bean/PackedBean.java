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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;

import app.packed.bean.Bean;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanSourceKind;
import app.packed.binding.Key;
import app.packed.binding.Provider;
import app.packed.operation.Op;
import app.packed.util.AnnotationList;
import app.packed.util.Nullable;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.util.PackedAnnotationList;

/**
 *
 */
// Implementering
// En masse fields, som vi overskriver
// Delta updates extending DeltaBean, med en parent
// A Map<?, Object> with all the properties

// Der er nogle maader man kan implementere denne paa.
// Enten har vi altid det rigtige.
// Eller ogsaa har vi versioner




public final class PackedBean<T> implements Bean<T> {

    /** A list ofIllegal bean classes. Void is technically allowed but {@link #installWithoutSource()} needs to used. */
    // TODO Align with Key and allowed classes
    public static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Class.class, Key.class, Op.class, Optional.class, Provider.class);

    /** The bean class. */
    public final Class<?> beanClass;

    /** The source ({@code null}, {@link Class}, {@link PackedOp}, otherwise the bean instance) */
    @Nullable
    public final Object beanSource;

    /** The type of source the installer is created from. */
    public final BeanSourceKind beanSourceKind;

    private PackedBean(BeanSourceKind sourceKind, Class<?> beanClass, @Nullable Object source) {
        if (sourceKind != BeanSourceKind.SOURCELESS && ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new BeanInstallationException(beanClass + ", is not a valid type for a bean");
        }
        if (sourceKind == BeanSourceKind.OP && !(source instanceof PackedOp)) {
            // throw new Error();
        }
        this.beanClass = beanClass;
        this.beanSource = source;
        this.beanSourceKind = sourceKind;
    }

    /** {@inheritDoc} */
    @Override
    public BeanSourceKind beanSourceKind() {
        return beanSourceKind;
    }

    /**
     * Creates a new bean
     *
     * @return
     */
    public static PackedBean<?> of() {
        return new PackedBean<>(BeanSourceKind.SOURCELESS, void.class, null);
    }

    public static <T> PackedBean<T> of(Class<T> beanClass) {
        requireNonNull(beanClass, "beanClass is null");
        return new PackedBean<>(BeanSourceKind.CLASS, beanClass, beanClass);
    }

    // Like instance it is fairly limited what you can do
    public static <T> PackedBean<T> of(Op<?> op) {
        PackedOp<?> pop = PackedOp.crack(op);
        Class<?> beanClass = pop.type.returnRawType();
        return new PackedBean<>(BeanSourceKind.OP, beanClass, pop);
    }

    // I think it is more of a builder you return
    public static <T> PackedBean<T> ofInstance(T instance) {
        requireNonNull(instance, "instance is null");
        return new PackedBean<>(BeanSourceKind.INSTANCE, instance.getClass(), instance);
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationList annotations() {
        return new PackedAnnotationList(beanClass.getAnnotations());
    }
}
