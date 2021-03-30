package packed.internal.component;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.Wirelet;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.BuildSetup;

/**
 * A component that can be wired. For now, this is every component setup type except ExtensionSetup.
 */
public abstract class WireableComponentSetup extends ComponentSetup implements ComponentConfigurationContext {

    /** Wirelets that was specified when creating the component. */
    // Alternativ er den ikke final.. men bliver nullable ud eftersom der ikke er flere wirelets
    @Nullable
    public final WireletWrapper wirelets;

    public WireableComponentSetup(BuildSetup build, ApplicationSetup application, RealmSetup realm, WireableComponentDriver<?> driver,
            @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        super(build, application, realm, driver, parent);

        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            this.wirelets = null;
        } else {
            // If it is the root
            Wirelet[] ws;
            if (PackedComponentModifierSet.isApplication(modifiers)) {
                if (application.driver.wirelet == null) {
                    ws = WireletArray.flatten(wirelets);
                } else {
                    ws = WireletArray.flatten(application.driver.wirelet, Wirelet.combine(wirelets));
                }
            } else {
                ws = WireletArray.flatten(wirelets);
            }

            this.wirelets = new WireletWrapper(ws);

            // May initialize the component's name, onWire, ect
            // Do we need to consume internal wirelets???
            // Maybe that is what they are...
            int unconsumed = 0;
            for (Wirelet w : ws) {
                if (w instanceof InternalWirelet bw) {
                    // Maaske er alle internal wirelets first passe
                    bw.onBuild(this);
                } else {
                    unconsumed++;
                }
            }
            if (unconsumed > 0) {
                this.wirelets.unconsumed = unconsumed;
            }
            
            if (nameInitializedWithWirelet && parent != null) {

                // addChild(child, name);
            }
        }
    }

    /** {@inheritDoc} */
    public final String getName() {
        // Dette kunne ogsaa wire componenten?
        return name;
    }
    
    /** {@inheritDoc} */
    public final void setName(String name) {
        checkComponentName(name); // Check if the name is valid
        checkIsWiring();

        // If a name has been set using a wirelet it cannot be overridden
        if (nameInitializedWithWirelet) {
            return;
        } else if (name.equals(this.name)) {
            return;
        }

        // maybe assume s==0

        if (parent != null) {
            parent.children.remove(this.name);
            if (parent.children.putIfAbsent(name, this) != null) {
                throw new IllegalArgumentException("A component with the specified name '" + name + "' already exists");
            }
        }
        this.name = name;
    }
}
