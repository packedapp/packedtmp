package packed.internal.lifetime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.bean.BeanMirror;
import app.packed.component.ComponentMirrorTree;
import app.packed.lifetime.LifetimeMirror;
import app.packed.lifetime.LifetimeType;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ContainerSetup;

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
    final ComponentSetup component;

    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    /** The type of lifetime. */
    final LifetimeType lifetimeType;

    // Der er jo som saadan ikke noget vi vejen for at vi har en DAG istedet for et trae...
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
    public LifetimeSetup(ContainerSetup rootContainer) {
        this(LifetimeType.APPLICATION, rootContainer, null);
    }

    private LifetimeSetup(LifetimeType lifetimeType, ComponentSetup component, @Nullable LifetimeSetup parent) {
        this.component = requireNonNull(component);
        this.lifetimeType = lifetimeType;
        this.parent = parent;
    }

    public LifetimeSetup addChild(ComponentSetup component) {
        LifetimeSetup l = new LifetimeSetup(component instanceof ContainerSetup ? LifetimeType.CONTAINER : LifetimeType.BEAN, component, this);
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
        public List<BeanMirror> beans() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Set<LifetimeMirror> children() {
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
        public LifetimeType lifetimeType() {
            return l.lifetimeType;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<LifetimeMirror> parent() {
            return l.parent == null ? Optional.empty() : Optional.of(l.parent.mirror());
        }
    }

    // Vi kan sagtens folde bedste foraeldre ind ogsaa...
    // Altsaa bruger man kun et enkelt object kan vi jo bare folde det ind...
//    [ [GrandParent][Parent], O1, O2, O3]
}
