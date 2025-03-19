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
package app.packed.bean.sandbox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.extension.Extension;

/**
 *
 */
// Ideen er vi kan finde fx entity beans

// container.beanFinder().find

// Filter -> Scan -> Action

// Supports jandex

// JpaExtension jpa = use(JpaExtension.class);
// foreach(e -> JPA jpa.install()

//beanFinder().forEach(JPAExtension.class, JPAExtension::install);

// forEach(JPAExtension.class, (e, c) -> e.installer);

public interface BeanFinder<T> {

    // Will only load the extension if beans found...

    <E extends Extension<?>> void forEach(Class<E> extension, BiConsumer<? super E, ? super T> c);

    void forEach(Consumer<? super T> c);
}

// @BeanInstaller(JPAExtension.class, AnnotatedWith(@Entity.class))
// @JPABeanInstaller(filter= AnnotatedWith(@Entity.class), container=".", package="./**")

// With default values
//@JPABeanInstaller