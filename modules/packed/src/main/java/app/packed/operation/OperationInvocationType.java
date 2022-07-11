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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.util.List;
import java.util.Optional;

/**
 * Operation Invocation Type er det vi kalder operation med.
 * <p>
 * Fx for root Application er det bare et Wirelet array.
 * 
 */

// Skal BeanField ogsaa tage en OperationInvocationType eller er det kun BeanMethod?

// extension.newContainerOperation(OperationType, Assembly assembly);

// Operation kan vaere none | new bean | new container | new application (or its holder)?
// Det boer ikke vaere bundet til operationtypen...

// Kan ikke sige noget om VarHandle eller Method vil jeg mene???
// Det kan jo baade vaere en metode/constructor/function/field der ligger i den anden ende
public interface OperationInvocationType {
    
    /**
     * If the underlying handle is a {@link VarHandle} returns its accessMode
     * @return
     */
    Optional<AccessMode> accessMode();
    
    /** {@return an immutable list of lanes of this type.} */
    List<Lane> lanes();

    /**
     * Returns a method type that represent the methodType of the methodHandle or the {@link VarHandle#coordinateTypes()}
     * coordinates of the varhandle.
     * 
     * @return
     * @see MethodHandle#type()
     * @see VarHandle#accessModeType(java.lang.invoke.VarHandle.AccessMode)
     */
    MethodType methodType();

    public static OperationInvocationType defaults() {
        return raw().addExtensionContextLane();
    }

    public static OperationInvocationType raw() {
        throw new UnsupportedOperationException();
    }

    // Takes a beanInstance
    public static OperationInvocationType varHandleInstance() {
        throw new UnsupportedOperationException();
    }

    OperationInvocationType addContextLane(Object contextDefinition);

    // Er ikke sikker paa den er public
    OperationInvocationType addExtensionContextLane();

    public interface Lane {

        LaneKind kind();

        Class<?> type();
    }

    public enum LaneKind {
        // WireletArray? saa kan man evt. teste det Det er jo en rigtig
        
        /** The lane takes a bean instance. Typically of type Object. */
        BEAN_INSTANCE, CONTEXT, EXTENSION_CONTEXT, OTHER;
    }

    public enum ResultKind {

    }
}
