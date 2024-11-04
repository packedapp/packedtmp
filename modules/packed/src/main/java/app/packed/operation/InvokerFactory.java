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

/**
 *
 */

public interface InvokerFactory {
    // For those that are "afraid" of method handles. You can specify a SAM interface (Or abstract class with an empty
    // constructor)
    // I actually think this is a lot prettier, you can see the signature
    // Maybe a class value in the template.
    // This will codegen though
    // constructor arguments are for abstract classes only

    // I think the idea is that the Invoker does not expose MethodHandle, so what are the constructor arguments used for?

    // Do we want ExtensionContext? I don't think so

    // Det her bliver jo injected......

    /**
     * <p>
     * An abstract class can be specified, this can be used to hold metadata about operation. That can be useful at runtime.
     *
     * @param <T>
     * @param handleClass
     * @param constructorArguments
     *            in case the specified class is an abstract class, the abstract class can take the specified arguments
     * @return
     */
    // Specifically, the abstract class does not allow injection of services.
    // It is primarily meant for meta data at runtime.
    <T> T invokerAs(Class<T> handleClass, Object... abstractClassConstructorArguments);

    MethodHandle invokerAsMethodHandle();

    /**
     * {@return an invoker represented as a var handle}
     *
     * @throws UnsupportedOperationException
     *             if the invoker cannot be represented as a var handle, for example underlying operation does not represent
     *             a field
     */
    // Ved sgu ikke om vi bare skal droppe den?
    VarHandle invokerAsVarHandle();

    /**
     * {@return the invocation type of this operation.}
     * <p>
     * The method handle will return {@link #invokerType()} from its {@link MethodHandle#type() method handle type}.
     */
    MethodType invokerType();

    // fx for initialize, vil vi godt bruge vaeredien af first operation og saa smide den videre
    // Hmm, maaske har vi noget builder faetter her
    // Men det er vel mere eller mindre compound operation...
    // InvokerFactory ofAll(OperationTemplate template, OperationHandle... handles);

    // Det er vel fold... Vi bruger resultatet i naeste operation.
    // Men naeste operation kan jo saa ikke rigtigt returnere noget.

    // Altsaa hvor tit har folk brug for dette
    // @Verify(X), @VerifyY
}
