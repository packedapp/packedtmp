/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.component;

import static java.util.Objects.requireNonNull;

import app.packed.application.ApplicationMirror;
import app.packed.extension.Extension;
import app.packed.namespace.OverviewHandle;
import app.packed.namespace.OverviewMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.extension.ExtensionClassModel;
import internal.app.packed.invoke.ConstructorSupport;
import internal.app.packed.invoke.ConstructorSupport.OverviewMirrorFactory;
import internal.app.packed.util.types.TypeVariableExtractor;

/**
 *
 */
public abstract sealed class PackedOverviewHandle<E extends Extension<E>> implements OverviewHandle<E> {

    /** Extract the extension type variable from OverviewMirror. */
    private static final ClassValue<Class<? extends Extension<?>>> EXTENSION_TYPES = new ClassValue<>() {

        private static final TypeVariableExtractor EXTRACTOR = TypeVariableExtractor.of(OverviewMirror.class);

        @Override
        protected Class<? extends Extension<?>> computeValue(Class<?> type) {
            return ExtensionClassModel.extractE(EXTRACTOR, type);
        }
    };

    /** Cache OverviewMirror constructor factories. */
    private static final ClassValue<OverviewMirrorFactory> CONSTRUCTORS = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected OverviewMirrorFactory computeValue(Class<?> type) {
            return ConstructorSupport.findOverviewMirrorConstructor((Class<? extends OverviewMirror<?>>) type);
        }
    };

    @SuppressWarnings("unchecked")
    public static <O extends OverviewMirror<?>> O ofApplication(ApplicationSetup application, Class<O> type) {
        requireNonNull(type, "type is null");

        Class<? extends Extension<?>> extensionType = EXTENSION_TYPES.get(type);
        OverviewMirrorFactory factory = CONSTRUCTORS.get(type);

        PackedApplicationOverviewHandle<?> handle = new PackedApplicationOverviewHandle<>(application.mirror(), extensionType);

        return (O) factory.create(handle);
    }

    public static final class PackedApplicationOverviewHandle<E extends Extension<E>> extends PackedOverviewHandle<E> {

        private final ApplicationMirror applicationMirror;

        private final Class<? extends Extension<?>> extensionType;

        public PackedApplicationOverviewHandle(ApplicationMirror applicationMirror, Class<? extends Extension<?>> extensionType) {
            this.applicationMirror = requireNonNull(applicationMirror);
            this.extensionType = requireNonNull(extensionType);
        }

        @Override
        public OperationMirror.OfStream<OperationMirror> operations() {
            return applicationMirror.operations().filter(op -> op.installedByExtension() == extensionType);
        }

        @Override
        public <T extends OperationMirror> OperationMirror.OfStream<T> operations(Class<T> operationType) {
            return (OperationMirror.OfStream<T>) operations().ofType(operationType);
        }
    }
}
