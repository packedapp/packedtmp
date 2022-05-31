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

import app.packed.bean.BeanDefinitionException;
import app.packed.operation.dependency.DependencyProvider;

/**
 *
 */
// For now it is a place holder... Ved ikke om vi vil beholder dent. BeanVarInjcector er ogsaa lidt sjot
// BeanVariable bare
public sealed interface BeanElement permits BeanClass, BeanConstructor, BeanField, BeanMethod, DependencyProvider, BeanVariable {

    // annotationReadr()???
    // We need it on every element

    /**
     * @param postFix
     *            the message to include in the final message
     * 
     * @throws BeanDefinitionException
     *             always thrown
     */
    default void failWith(String postFix) {
        throw new BeanDefinitionException("OOPS " + postFix);
    }

    /** {@return information about this method's declaring bean.} */
    default BeanInfo beanInfo() {
        throw new UnsupportedOperationException();
    }

}
// CheckRealmIsApplication
// CheckRealmIsExtension

// BeanAnnotationReader???
