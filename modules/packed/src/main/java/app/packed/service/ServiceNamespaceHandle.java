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
package app.packed.service;

import app.packed.extension.BaseExtension;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceTemplate.Installer;

/**
 *
 */
// Problemet er at, det her handle nok bliver noedt til at vaere i intern package.
// Tror bare vi maa kalde vi MH
class ServiceNamespaceHandle extends NamespaceHandle<BaseExtension, ServiceNamespaceConfiguration> {

    /**
     * @param installer
     */
    protected ServiceNamespaceHandle(Installer installer) {
        super(installer);
    }

    /** {@inheritDoc} */
    @Override
    protected ServiceNamespaceConfiguration newNamespaceConfiguration(BaseExtension e) {
        return new ServiceNamespaceConfiguration(this, e);
    }
}
