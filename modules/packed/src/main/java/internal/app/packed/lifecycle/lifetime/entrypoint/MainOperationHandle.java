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
package internal.app.packed.lifecycle.lifetime.entrypoint;

import app.packed.lifetime.MainOperationMirror;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationMirror;

/**
 *
 */
public class MainOperationHandle extends OperationHandle<OperationConfiguration> {

    /**
     * @param installer
     */
    public MainOperationHandle(OperationInstaller installer) {
        super(installer);
    }

    @Override
    protected OperationMirror newOperationMirror() {
       return new MainOperationMirror(this);
    }
}
