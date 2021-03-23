package packed.internal.component;

import app.packed.cli.Main;
import app.packed.container.BaseAssembly;

public class Zap extends BaseAssembly {

    @Override
    protected void build() {}

    public static void main(String[] args) {
        Main.run(new Zap(), new InternalWirelet.FailOnFirstPass());
    }
}
