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
package app.packed.container;

import java.util.function.Function;

import app.packed.bundle.UpstreamWiringOperation;

/**
 *
 */
// Hmm
public abstract class ContainerImportStage extends UpstreamWiringOperation {
    public static final ContainerImportStage NO_STARTING_POINTS = null;
    public static final ContainerImportStage NO_STOPPING_POINTS = null;

    public ContainerImportStage renameStartingPoint(String existingName, String newName) {
        // I think we should fail
        // Expected to rename stage, but no
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        ContainerImportStage.NO_STARTING_POINTS.andThen(ContainerImportStage.NO_STOPPING_POINTS);
    }

    // Is used, for example, to rename the root component
    // Because we cannot have 2 children with the same name...
    public String componentRootName(String name) {
        return name;
    }

    public static ContainerImportStage rootComponentRename(String newName) {
        throw new UnsupportedOperationException();
    }

    public static ContainerImportStage rootComponentRename(Function<String, String> renameFunction) {
        throw new UnsupportedOperationException();
    }

}
