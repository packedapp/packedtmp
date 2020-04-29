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
package app.packed.lifecycle;

/**
 *
 */
public @interface When {

    // https://github.com/anomalizer/tungsten-fsm/tree/master/src/java/com/continuent/tungsten/commons/patterns/fsm
    // https://github.com/j-easy/easy-states

    // Enums, https://www.baeldung.com/java-enum-simple-state-machinehttps://www.baeldung.com/java-enum-simple-state-machine

    // https://doc.akka.io/docs/akka/2.4/java/lambda-fsm.html

    //// Vi har ikke rigtig event inputs
    // https://softwareengineering.stackexchange.com/questions/143145/how-to-recover-from-finite-state-machine-breakdown

    /// http://www.cs.sjsu.edu/faculty/pearce/ooa/chp10.htm Error state

    // Every FSM has an implicit error state. If the active state of M is q, and if event e occurs, and if:
}

// I think lifecycle is a generalization

// A graph, with a possible error state
