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
package packed.internal.component.role;

/**
 *
 */
public abstract class CD2 {

    // Or maybe just prefixed with function as in functionUsing
    public interface FunctionOption {
        // using(TypeLiteral)
        // failOnAnnotation..
        // Allow X annotation
    }

    public interface HostOption {
        // ONLY_ADD, NO_REMOVE, NO_UPDATE, IDK
    }

    public interface ContainerOption {

    }

    public interface Option {

        // Cannot be hosts, and cannot have static children either...

        // childFree(), hostFree()
        static Option alwaysLeaf() {
            throw new UnsupportedOperationException();
        }
    }
}
