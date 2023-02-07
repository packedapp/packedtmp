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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import app.packed.bean.BeanIntrospector;
import internal.app.packed.bean.BeanHookModel.AnnotatedField;
import internal.app.packed.bean.BeanScannerFieldHelper.FieldPair;
import internal.app.packed.container.ExtensionSetup;

/**
 * An instance of this class is created per extension that participates in the introspection. The main purpose of the
 * class is to make sure that the extension points to the same bean introspector for the whole of the introspection.
 */
public final class BeanScannerExtension {

    /** The actual extension. */
    public final ExtensionSetup extension;

    boolean hasFullAccess;

    final BeanIntrospector introspector;

    public final BeanScanner scanner;

    BeanScannerExtension(BeanScanner scanner, ExtensionSetup extension, BeanIntrospector introspector) {
        this.extension = extension;
        this.introspector = introspector;
        this.scanner = scanner;
    }

    public boolean hasFullAccess() {
        return hasFullAccess;
    }

    void matchAnnotatedField(Field field, Annotation[] annotations, Annotation[] hooks, AnnotatedField... annotatedFields) {
        PackedBeanField of = new PackedBeanField(this, field, annotations, annotatedFields);
        PackedAnnotationList pac = new PackedAnnotationList(hooks);
        introspector.hookOnAnnotatedField(pac, of);
    }

    public void onAnnotatedField(Field f, Annotation a1, Annotation a2) {
        // Validating annotations
    }

    @SuppressWarnings("unused")
    public void onAnnotatedField(Field f, Annotation[] fieldAnnotations, Annotation[] hooks, AnnotatedField[] afs) {
        boolean isGetable = false;
        boolean isSetable = false;
        for (AnnotatedField af : afs) {
            isGetable |= af.isGettable();
            isSetable |= af.isSettable();
        }
    }

    // I think we need to already have checked that we only have 1 providing annotation
    public void onAnnotatedField(Field f, Annotation[] fieldAnnotations, FieldPair pair) {

    }

    public void onAnnotatedField(Field f, Annotation[] fieldAnnotations, FieldPair... pair) {

    }

    static class OnField {
        Annotation[] annotations;
        Field f;
        FieldPair[] pairs;
    }
}
