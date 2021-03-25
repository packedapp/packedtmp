package packed.internal.base.application;

import app.packed.application.Main;
import app.packed.component.Assembly;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.ComponentDriver;

public class DriverTest extends Assembly<BaseComponentConfiguration> {

    protected DriverTest() {
        super(ComponentDriver.driverInstallInstance("foo"));
    }

    @Override
    protected void build() {
        // TODO Auto-generated method stub

    }

    public static void main(String[] args) {
        Main.run(new DriverTest());
    }

}
