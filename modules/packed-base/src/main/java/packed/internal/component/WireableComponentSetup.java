package packed.internal.component;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.Wirelet;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.BuildSetup;
import packed.internal.container.ExtensionSetup;

/**
 * A component that can be wired. For now, this every component type except an {@link ExtensionSetup extension}.
 */
public abstract class WireableComponentSetup extends ComponentSetup implements ComponentConfigurationContext {

    /** Wirelets that was specified when creating the component. */
    @Nullable
    public final WireletWrapper wirelets;

    public WireableComponentSetup(BuildSetup build, ApplicationSetup application, RealmSetup realm, WireableComponentDriver<?> driver,
            @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        super(build, application, realm, driver, parent);

        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            this.wirelets = null;
        } else {

            // Various
            if (parent == null) {
                Wirelet[] ws;
                if (application.driver.wirelet == null) {
                    ws = WireletArray.flatten(wirelets);
                } else {
                    ws = WireletArray.flatten(application.driver.wirelet, Wirelet.combine(wirelets));
                }
                this.wirelets = new WireletWrapper(ws);
            } else {
                Wirelet[] ws = WireletArray.flatten(wirelets);
                this.wirelets = new WireletWrapper(ws);
                this.onWire = parent.onWire;
            }

            // May initialize the component's name, onWire, ect
            if (this.wirelets != null) {
                for (Wirelet w : this.wirelets.wirelets) {
                    if (w instanceof InternalWirelet bw) {
                        bw.firstPass(this);
                    }
                }
            }
            if (nameInitializedWithWirelet && parent != null) {

                // addChild(child, name);
            }
        }
    }
}
