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
package packed.internal.component.old;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirror.Relation;
import app.packed.component.ComponentScope;
import app.packed.component.UserOrExtension;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import packed.internal.application.ApplicationInitializationContext;
import packed.internal.component.ComponentSetup;
import packed.internal.lifetime.LifetimePool;

/** An runtime more efficient representation of a component. We may use it again at a later time */
public final class RuntimeComponentMirror {

    /** Any child components this component might have. Is null if we know the component will never have any children. */
    @Nullable
    private final Map<String, RuntimeComponentMirror> children;

    /** The runtime model of the component. */
    final RuntimeComponentModel model;

    /** The name of the component. */
    private final String name;

    /** The parent component, or null if root. */
    @Nullable
    public final RuntimeComponentMirror parent;

    /** The region this component is part of. */
    public final LifetimePool pool;

    /**
     * Creates a new component.
     * 
     * @param parent
     *            the parent component, iff this component has a parent.
     * @param component
     *            the component build used to create this node
     * @param launch
     *            initialization context
     */
    public RuntimeComponentMirror(@Nullable RuntimeComponentMirror parent, ComponentSetup component, ApplicationInitializationContext launch) {
        this.parent = parent;
        this.model = RuntimeComponentModel.of(component);
        if (parent == null) {
            this.name = launch.name;
        } else {
            this.name = requireNonNull(component.name);
        }

        // Vi opbygger structuren foerst...
        // Og saa initialisere vi ting bagefter
        // Structuren bliver noedt til at vide hvor den skal spoerge efter ting...
        Map<String, RuntimeComponentMirror> children = null;
        LinkedHashMap<String, ComponentSetup> childComponents = new LinkedHashMap<>();// component.children;
        if (childComponents != null) {
            // Maybe ordered is the default...
            LinkedHashMap<String, RuntimeComponentMirror> result = new LinkedHashMap<>(childComponents.size());

//            for (ComponentSetup cc : component.children.values()) {
//                // We never carry over extensions into the runtime
//                RuntimeComponentMirror ac = new RuntimeComponentMirror(this, cc, launch);
//                result.put(ac.name(), ac);
//            }

            children = Map.copyOf(result);
        }
        this.children = children;

        // Maaske er region ikke final...
        // Problemet er en bruger der injecter Component i constructuren.
        // Og saa kalder children(). Som jo af gode grunde ikke noedvendig er fuldt
        // initialiseret f.eks. hvis vi supporter Source attributer...
        // Alternativ fejler vi bare naar folk kalder source attributer...
        // Tror bare vi har et check om en source instance er non-null

        // Cannot display the attribute values of /sds/we/ [source = wewe.class] until ccc.class has been instantiated

        // Vi create a new region is its the root, or if the component is a guest
        if (parent == null /*|| component.application.hasRuntime */ ) {
//            this.pool = component.pool.newPool(launch);
//
//            // Run all initializers
//            for (MethodHandle mh : component.application.initializers) {
//                try {
//                    mh.invoke(pool);
//                } catch (Throwable e) {
//                    throw ThrowableUtil.orUndeclared(e);
//                }
//            }
            this.pool = null;
            throw new UnsupportedOperationException();
        } else {
            this.pool = parent.pool;
        }
    }


    public ApplicationMirror application() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */

    public Collection<RuntimeComponentMirror> children() {
        Map<String, RuntimeComponentMirror> c = children;
        if (c == null) {
            return Collections.emptySet();
        }
        // Right now children are already immutable
        return Collections.unmodifiableCollection(c.values());
    }

    /** {@inheritDoc} */

    public int depth() {
        return model.depth;
    }

    public RuntimeComponentMirror findComponent(CharSequence path) {
        return findComponent(path.toString());
    }

    private RuntimeComponentMirror findComponent(String path) {
        if (path.length() == 0) {
            throw new IllegalArgumentException("Cannot specify an empty (\"\") path");
        }
        if (path.charAt(0) == '/' && path.length() == 1) {
            if (path().toString().equals("/")) {
                return this;
            }
        }
        // Vi smider IllegalArgumentException hvis man absolute path, og man ikke har samme prefix....

        // TODO fix for non-absolute paths....
        RuntimeComponentMirror c = children.get(path);
        if (c == null) {
            String p = path.toString();
            String[] splits = p.split("/");
            Map<String, RuntimeComponentMirror> chi = children;
            for (int i = 1; i < splits.length; i++) {
                if (chi == null) {
                    return null;
                }
                String ch = splits[i];
                RuntimeComponentMirror ac = chi.get(ch);
                if (ac == null) {
                    return null;
                }
                if (i == splits.length - 1) {
                    return ac;
                }
                chi = ac.children;
            }
        }
        return c;
    }


    public boolean isInSame(ComponentScope scope, ComponentMirror other) {
        throw new UnsupportedOperationException();
    }

    public boolean isInSameContainer(RuntimeComponentMirror other) {
        return isInSameContainer0() == other.isInSameContainer0();
    }

    private RuntimeComponentMirror isInSameContainer0() {
        RuntimeComponentMirror c = this;
        while (!(c.model.isContainer())) {
            c = c.parent;
        }
        return c;
    }

    /** {@inheritDoc} */

    public String name() {
        return name;
    }

    /** {@inheritDoc} */

    public Optional<RuntimeComponentMirror> parent() {
        return Optional.ofNullable(parent);
    }

    /** {@inheritDoc} */

    public NamespacePath path() {
     //   return PackedNamespacePath.of(this);
        throw new UnsupportedOperationException();
        
    }

    /** {@inheritDoc} */

    public Relation relationTo(ComponentMirror other) {
        requireNonNull(other, "other is null");
        throw new UnsupportedOperationException();
      //  return PackedComponentInstanceRelation.relation(this, (RuntimeComponentMirror) other);
    }

//    /**
//     * @param path
//     */
//
//    public RuntimeComponentMirror resolve(CharSequence path) {
//        RuntimeComponentMirror c = findComponent(path);
//        if (c == null) {
//            // Maybe try an match with some fuzzy logic, if children is a resonable size)
//            List<?> list = stream().map(e -> e.path()).toList();
//            throw new IllegalArgumentException("Could not find component with path: " + path + " avilable components:" + list);
//        }
//        return c;
//    }

    /** {@inheritDoc} */

    public RuntimeComponentMirror root() {
        RuntimeComponentMirror c = this;
        RuntimeComponentMirror p = parent;
        while (p != null) {
            c = p;
            p = p.parent;
        }
        return c;
    }

    /** {@inheritDoc} */

//    public ComponentMirrorStream stream(ComponentMirrorStream.Option... options) {
//        throw new UnsupportedOperationException();
////        return new PackedComponentStream(stream0(this, true, PackedComponentStreamOption.of(options)));
//    }
//
//    @SuppressWarnings("unused")
//    private Stream<RuntimeComponentMirror> stream0(RuntimeComponentMirror origin, boolean isRoot, PackedComponentStreamOption option) {
//        // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
//        Map<String, RuntimeComponentMirror> c = children;
//        if (c != null && !c.isEmpty()) {
//            if (option.processThisDeeper(origin, this)) {
//                Stream<RuntimeComponentMirror> s = c.values().stream().flatMap(co -> co.stream0(origin, false, option));
//                return isRoot && option.excludeOrigin() ? s : Stream.concat(Stream.of(this), s);
//            }
//            return Stream.empty();
//        } else {
//            return isRoot && option.excludeOrigin() ? Stream.empty() : Stream.of(this);
//        }
//    }


    public ContainerMirror container() {
        // TODO Auto-generated method stub
        return null;
    }


    public Stream<ComponentMirror> components() {
        // TODO Auto-generated method stub
        return null;
    }


    public Optional<Class<? extends Extension<?>>> managedByExtension() {
        throw new UnsupportedOperationException();
    }


    public UserOrExtension realm() {
        throw new UnsupportedOperationException();
    }
}
