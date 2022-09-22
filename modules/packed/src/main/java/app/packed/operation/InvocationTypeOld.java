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
import java.util.List;

/**
 * alle lane operation boer starte med laneX
 */
interface InvocationTypeOld {

    default MethodType invocationType() {
        throw new UnsupportedOperationException();
    }

    // Cannot add more than one bean instance lane
    default InvocationTypeOld laneAddBeanInstance() {
        return laneAddBeanInstance(Object.class);
    }

    InvocationTypeOld laneAddBeanInstance(Class<?> type);

    InvocationTypeOld laneAddInvocationContext(); // OperationInvocationContext?

    default OperationHandle lanesClear() {
        // No interceptors
        throw new UnsupportedOperationException();
    }

}

interface SandboxX {

    /** {@return an immutable list of lanes of this type.} */
    List<Lane> lanes();

    interface Lane {

        @SuppressWarnings("exports")
        LaneKind kind();

        Class<?> type();
    }

    // Er ikke sikker paa vi vil have det her
    enum LaneKind {
        // WireletArray? saa kan man evt. teste det Det er jo en rigtig

        /** The lane takes a bean instance. Typically of type Object. */
        BEAN_INSTANCE, CONTEXT, EXTENSION_CONTEXT, OTHER;
    }
}

interface Zarchive {

    // This should always be called before dabbling with dependencies
    /**
     * @param oit
     * @return
     * @throws IllegalStateException
     *             if invoked after dependencies has been customized or resolved
     */
    // Hmm det er jo ogsaa mere end bare invocation det er ogsaa contexts som er tilgaengelig
    // Problemet med at skille den ud, er at context er saa taet bundet til argument pladsen
    // At det ikke giver mening at skille dem ad
    default OperationHandle invokeWith(OperationInvocationType oit) {
        // Grunden til vi tager den her. Er fordi det bliver for besvarligt at tage OperationInvocationType hver gang vi skal
        // lave en operation.
        // Fx mht til LifetimeOperations for beans og containers. Nu kan vi bare returnere en liste af operationer.

        return null;
    }

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
    @SuppressWarnings("exports")

    public interface OperationInvocationType {

        OperationInvocationType addContextLane(Object contextDefinition);

        // Er ikke sikker paa den er public
        OperationInvocationType addExtensionContextLane();

        public static OperationInvocationType defaults() {
            return invokeRaw().addExtensionContextLane();
        }

        public static OperationInvocationType invokeRaw() {
            throw new UnsupportedOperationException();
        }

        // Takes a beanInstance
        public static OperationInvocationType varHandleInstance() {
            throw new UnsupportedOperationException();
        }

        public enum ResultKind {

        }
    }
}