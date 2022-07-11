package internal.app.packed.lifetime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.application.ComponentMirrorTree;
import app.packed.base.Nullable;
import app.packed.lifetime.LifetimeManagementKind;
import app.packed.lifetime.mirror.LifetimeMirror;
import app.packed.lifetime.mirror.LifetimeOperationMirror;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.container.ContainerSetup;

// Der er faktisk 2 strategier her...
// RepeatableImage -> Har vi 2 pools taenker jeg... En shared, og en per instans
// Ikke repeatable.. Kav vi lave vi noget af array'et paa forhaand... F.eks. smide
// bean instancerne ind i det

// Saa maaske er pool og Lifetime to forskellige ting???
//
public final class LifetimeSetup {

    /** Any child lifetimes. */
    private List<LifetimeSetup> children;

    /** The root component of the lifetime. */
    final ComponentSetup origin;

    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    // Der er jo som saadan ikke noget vi vejen for at vi har en DAG istedet for et trae...
    /** Any parent of this lifetime. The root lifetime always being identical to the application lifetime. */
    @Nullable
    final LifetimeSetup parent;

    /** The application's constant pool. */
    public final LifetimePoolSetup pool = new LifetimePoolSetup();

    /**
     * Creates a new application lifetime.
     * 
     * @param rootContainer
     *            the application's root container
     */
    public LifetimeSetup(ComponentSetup origin, @Nullable LifetimeSetup parent) {
        this.origin = requireNonNull(origin);
        this.parent = parent;
    }

    public LifetimeSetup addChild(ComponentSetup component) {
        LifetimeSetup l = new LifetimeSetup(component, this);
        if (children == null) {
            children = new ArrayList<>(1);
        }
        children.add(l);
        return l;
    }

    public LifetimeMirror mirror() {
        return new BuildtimeLifetimeMirror(this);
    }

    public record BuildtimeLifetimeMirror(LifetimeSetup l) implements LifetimeMirror {

        /** {@inheritDoc} */
        @Override
        public Stream<LifetimeMirror> children() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentMirrorTree components() {
            // component.tree.filter(n.lifetime==this)
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public LifetimeOriginKind originKind() {
            if (l.origin instanceof ContainerSetup c) {
                return c.application.container == c ? LifetimeOriginKind.APPLICATION : LifetimeOriginKind.CONTAINER;
            }
            return LifetimeOriginKind.BEAN;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<LifetimeMirror> parent() {
            return l.parent == null ? Optional.empty() : Optional.of(l.parent.mirror());
        }

        /** {@inheritDoc} */
        @Override
        public List<LifetimeOperationMirror> operations() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public LifetimeManagementKind managementKind() {
            throw new UnsupportedOperationException();
        }
    }

    // Vi kan sagtens folde bedste foraeldre ind ogsaa...
    // Altsaa bruger man kun et enkelt object kan vi jo bare folde det ind...
//    [ [GrandParent][Parent], O1, O2, O3]
}
