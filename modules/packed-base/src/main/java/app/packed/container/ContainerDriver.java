package app.packed.container;

import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;

public interface ContainerDriver<C extends ContainerConfiguration> extends ComponentDriver<C> {

    @Override
    ContainerDriver<C> with(Wirelet... wirelet);

}
