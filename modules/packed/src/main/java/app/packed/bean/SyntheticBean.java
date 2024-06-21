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
package app.packed.bean;

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.operation.Op;
import app.packed.operation.Op1;
import app.packed.service.Provide;

/**
 *
 */

// Can be made from scratch, using an optional source and specified by the developer
// Can mutate a bean before it is introspected for example via a bean build hook


// There


// BeanBuildTrigger.installAll(ASS, b->b.debugInstantiantion());


// Virtual vs Synthetic. I think ideally we want something that is not in the JDK
// Fx synthetic beanMethod.modifiers() may return synthetic or a non-synthetic method

// SyntheticBean?? beanMirror.isVirtual sounds not right

// Okay, I only think authorities can manipulate their own beans

// Whenever you do manipulation of a non-synthetic bean. You get a syntheticBean (Do we store linage??)

// En ny source type
// Man kan lave instancer

// Ideen er lidt du kan goere hvad du vil.

// Og til sidst lade frameworket generere en implementation

// En af usecasene

// Adapted/Customized

// of() (SyntheticBean) vs functional bean??? Hmm
// of() and then add functions...
// function -> Lambda
// Operation -> Op?
public interface SyntheticBean<T> {

    // A bean can be synthetic but with a source
    BeanSourceKind source();

    // What is the beanClass()? SyntheticBean
    static <T> SyntheticBean<T> of() {
        throw new UnsupportedOperationException();
    }

    static <T> SyntheticBean<T> of(Op<?> op) {
        throw new UnsupportedOperationException();
    }

    // Maaske har vi instance mutators? Static mutators er hmm simpler...
    static <T> SyntheticBean<T> of(Consumer<? super BeanClassMutator> mutate) {
        throw new UnsupportedOperationException();
    }

    static void main(String[] args) {

        class C1 extends Op1<String, String> {
            public C1() {
                super(e -> e);
            }
        }
        of(new C1());

        of(new Op1<String, String>(e -> e) {});

        of(new Op1<String, String>(e -> e) {});

        // God damn this is ugly
        of(new Op1<String, String>(new Function<>() {
            @Provide
            public String apply(String t) {
                return null;
            }
        }) {});

    }

    // I think it is more of a builder you return
    static <T> SyntheticBean<T> of(T instance) {
        throw new UnsupportedOperationException();
    }

    static <T> SyntheticBean<T> of(Class<T> beanClass) {
        throw new UnsupportedOperationException();
    }

    // Think we should move to a builder approach
    // Can ikke se den er anderledes en BeanMutator
    public interface Builder {

        Builder addOperation(Op<?> operation);
    }
}
//// Maybe just use BeanSourceKind
//public enum Seed {
//  CLASS, INSTANCE, VOID;
//}