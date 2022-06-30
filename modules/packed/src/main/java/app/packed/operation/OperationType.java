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
package app.packed.operation;

import java.lang.invoke.MethodType;

/**
 *
 */
// extension.newContainerOperation(OperationType, Assembly assembly);

// Operation kan vaere none | new bean | new container | new application?
// Det boer ikke vaere bundet til operationtypen...

// Eneste problem med navnet er at vi maaske vil snakke om the type of operation. (f.x webget)
public interface OperationType {

    MethodType methodType();

    public static OperationType raw() {
        throw new UnsupportedOperationException();
    }

    public static OperationType defaults() {
        return null;
    }

    public static OperationType varHandle() {
        throw new UnsupportedOperationException();
    }
    
    public interface Lane {
        Class<?> type();
        LaneKind laneLind();
    }
    
    public enum LaneKind {
        EXTENSION_CONTEXT, BEAN_INSTANCE, CONTEXT, OTHER;
    }
}
