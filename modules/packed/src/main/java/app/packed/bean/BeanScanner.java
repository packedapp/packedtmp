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

import app.packed.base.Nullable;
import app.packed.bean.BeanField.AnnotatedWithHook;
import app.packed.container.InternalExtensionException;
import packed.internal.bean.BeanSetup;
import packed.internal.container.ExtensionSetup;

/**
 *
 */
public class BeanScanner {

    /**
     * The configuration of this scanner. Is initially null but populated via
     * {@link #initialize(ExtensionSetup, BeanSetup)}.
     */
    @Nullable
    private Setup setup;

    public final Object beanAnnotatedReader() {
        // AnnotatedReader.of(beanClass());
        throw new UnsupportedOperationException();
    }
    
    public final Class<?> beanClass() {
        return setup().bean.beanClass();
    }

    public final BeanKind beanKind() {
        return setup().bean.beanKind();
    }

    /**
     * Invoked by a MethodHandle from ExtensionSetup.
     * 
     * @param bean
     *            the bean we are scanning
     */
    final void initialize(ExtensionSetup extension, BeanSetup bean) {
        if (this.setup != null) {
            throw new IllegalStateException("This scanner has already been initialized.");
        }
        this.setup = new Setup(extension, bean);
    }

    public void onBeanClass(BeanClass clazz) {}

    /**
     * A callback method that is invoked for any field on a newly added bean where the field:
     * 
     * is annotated with an annotation that itself is annotated with {@link BeanField.AnnotatedWithHook} and where
     * {@link AnnotatedWithHook#extension()} matches the type of this extension.
     * <p>
     * This method is never invoked more than once for a single field for any given extension. Even if there are multiple
     * matching hook annotations on the same field. This method will only be called once for the field.
     * 
     * @param bf
     *            the bean field
     * @see BeanField.AnnotatedWithHook
     */
    public void onBeanField(BeanField bf) {}

    public void onBeanMethod(BeanMethod method) {
        // Test if getClass()==BeanScanner forgot to implement
        // Not we want to return generic bean scanner from newBeanScanner
        // We probably want to throw an internal extension exception instead
        throw new InternalExtensionException(setup().extension.model.fullName() + " failed to handle bean method");
    }

    public void onBeanVariable(BeanVariable variable) {}

    public void onClose() {}

    /**
     * 
     * This method is always called before
     * 
     * @param beanInfo
     *            information about the bean
     */
    // What happens if we install an extension that adds a bean for scanning that uses the same extension???
    // I think we should wait with the scanning??? IDK
    public void onNew() {}

    /**
     * {@return all the extensions that are being mirrored.}
     * 
     * @throws InternalExtensionException
     *             if called from the constructor of the mirror
     */
    private final Setup setup() {
        Setup b = setup;
        if (b == null) {
            throw new InternalExtensionException("This method cannot be called from the constructor of this class.");
        }
        return b;
    }

    private record Setup(ExtensionSetup extension, BeanSetup bean) {}

}
