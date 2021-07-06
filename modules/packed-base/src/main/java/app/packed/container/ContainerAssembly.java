package app.packed.container;

import app.packed.component.Assembly;

public abstract non-sealed class ContainerAssembly<C extends ContainerConfiguration> extends Assembly<C> {

    /**
     * Create a new container assembly using the specified driver.
     * 
     * @param driver
     *            the container driver
     */
    protected ContainerAssembly(ContainerDriver<? extends C> driver) {
        super(driver);
    }
}
