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

import java.lang.annotation.Annotation;

/**
 *
 */


// If we can, we should move this to BeanProcessor.AnnotationReader

// Maybe BeanAnnotationReader? Don't think we will use it elsewhere?
// AnnotatedBeanElement?
// I think the only we reason we call it BeanAnnotationReader is because
// if we called AnnotationReader is should really be located in a utility package
public interface BeanAnnotationReader {

    // Throws BeanDefinitionException
    //// foo bean was expected method to dddoooo to be annotated with 
    <T extends Annotation> T readRequired(Class<T> annotationType);

    // Det er taenk
    Annotation[] readAnyOf(Class<?>... annotationTypes);
}
