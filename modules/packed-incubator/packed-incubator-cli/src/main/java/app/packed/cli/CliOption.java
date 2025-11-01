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
package app.packed.cli;

import java.lang.annotation.Annotation;

import app.packed.bean.scanning.BeanIntrospector;
import app.packed.bean.scanning.BeanTrigger;
import app.packed.namespace.sandbox.NamespaceOperation;

/**
 *
 */
@NamespaceOperation
@BeanTrigger.OnAnnotatedVariable(introspector = CliOptionBeanIntrospector.class)
public @interface CliOption {}

final class CliOptionBeanIntrospector extends BeanIntrospector<CliExtension> {

    /** {@inheritDoc} */
    @Override
    public void onAnnotatedVariable(Annotation annotation, OnVariable onVariable) {
        extension().ns().process(extension(), (CliOption) annotation, onVariable);
    }
}