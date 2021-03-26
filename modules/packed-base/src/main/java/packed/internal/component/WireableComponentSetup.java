package packed.internal.component;

import app.packed.base.Nullable;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.Wirelet;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.BuildSetup;

public abstract class WireableComponentSetup extends ComponentSetup implements ComponentConfigurationContext {

    /** Wirelets that was specified when creating the component. */
    @Nullable
    public final WireletWrapper wirelets;

    public WireableComponentSetup(BuildSetup build, ApplicationSetup application, RealmSetup realm, WireableComponentDriver<?> driver,
            @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        super(build, application, realm, driver, parent);

        // Various
        if (parent == null) {
            this.wirelets = WireletWrapper.forApplication(application.driver, driver, wirelets);
        } else {
            this.wirelets = WireletWrapper.forComponent(driver, wirelets);
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

            //addChild(child, name);
        }
    }
}
