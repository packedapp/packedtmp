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
package app.packed.extension.application;

/**
 *
 */
public final class PackedApplicationTemplate implements ApplicationTemplate {


    // De her er ikke public fordi de kun kan bruges fra Bootstrap App
    // Hvor ikke specificere en template direkte. Fordi den kun skal bruges en gang
    // Til at lave selve bootstrap applicationene.
    PackedApplicationTemplate ROOT_MANAGED;

    PackedApplicationTemplate ROOT_UNMANAGED;
}
