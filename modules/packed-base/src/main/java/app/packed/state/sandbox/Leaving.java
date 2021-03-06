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
package app.packed.state.sandbox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation indicating that a method needs to be executed by the runtime after a particular event in the sidecars
 * lifecycle. Each sidecar type defines the type of events it supports as static string fields on the sidecar
 * annotation. For example,
 * <p>
 * Return values from annotated methods are always ignored. And unless otherwise specified no objects are available for
 * injection into the method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
// Useable with Qualifiers...
// @Exiting

public @interface Leaving {

    /**
     * The state that the object is leaving for which the method should be invoked.
     * 
     * @return name of the event
     */
    String state(); // StateExpression????

    String andNextStateIs() default "*";

    // Normally the annotated method will never be invoked
    Class<? extends Throwable>[] alsoOnFailure() default {};
}
//
//cp.findMethods(m -> {
//    onMethod(m);
//    Leaving oa = m.getAnnotation(Leaving.class);
//    if (oa != null) {
//        // To support nextStates. We create a MH filter with a guard on the next state...
//        // To support Error transitions. I think we have specific code for this
//        // It is not performance criticial... So we do not want to check this normally...
//
//        // Validate states
//        String state = oa.state();
//        int index = statics.ld.indexOf(state);
//        if (index == -1) {
//            // TODO remove instantiating, available values
//            throw new InternalExtensionException("Unknown sidecar lifecycle event '" + state + "' for method " + m + ", available states are "
//                    + Arrays.toString(statics.ld.toArray()));
//        }
//
//        MethodHandleBuilder mhb = MethodHandleBuilder.of(void.class, Object.class, Object.class);
//        decorateOnSidecar(mhb);
//        MethodHandle lt = MethodHandles.constant(StateTransition.class, new DefaultLifecycleTransition("Gll", "F", "FordiDuErDum"));
//        lt = MethodHandles.dropArguments(lt, 0, Object.class);
//        mhb.addKey(StateTransition.class, lt, 0);
//        MethodHandle mh = mhb.build(cp, m);
//
//        // If there are multiple methods with the same index. We just fold them to a single MethodHandle
//        MethodHandle existing = postSidecars[index];
//        postSidecars[index] = existing == null ? mh : MethodHandles.foldArguments(existing, mh);
//    }
//});
// Failing(types = IOException.class , state = Starting)

// LifecycleTest(Object instance()

//Host.exportAll()...

//Altsaa det med kontrakter hvor skal vi putte det????
//Foerst taenkte jeg vi bare tager en Pipeline som argument paa en extension...
// Men det er jo for selve containeren...

// Som udgangspunkt bestemmer en extension selv hvordan man kommunikere paa tvaers af hosts/guests
// Dvs.. f.eks. ServiceExtension kan godt sige at ja alle services er tilgængelige hos en Guest....

// Men de kan også sige at man skal specifict skal sige ExportAll....

// Kan evt. have en GuestIsolate(Class<? extends Extension> <- gør at det for en guest ser ud til 
// at det er den første i et træ. Det betyder også at evt. System sidecars bliver splittet??
// Eller??? Måske ikke, igen alt det her opdeling er jo frivillig

///
///
///
///
// Altså en Extension kan maaske ogsaa have en HostSidecar.....
// taenker jeg....
// Den kan saa faa injected en host pipeline evt....
// Saa kan en extension tage den som en Optional<MyExtensionHost>

// Service Mesh... <- Det er jo en hostsidecar der f.eks. faar
// GuestAdded... (Extension????)

// @HostSidecar
//// #GUEST_ADDED
//// #GUEST_REMOVED

// Alternativt kan extension jo selv kalde metoder....
//// Hehe nu har vi lige pludselig vendt det om...
//// Saa det ikke er gaesten der styrer det...
// men hosten...

// PRoblemet med en HostSidecar er vi skal have en per instans...
// Men naar vi konfigurer en pod med flere artifakter til et image.
// Saa har vi jo en sidecar der skal multiples hver gang vi laver en ny instans...