package app.packed.moduletests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import app.packed.extension.InternalExtensionException;
import app.packed.moduletests.isopen.IsOpenAssembly;
import app.packed.moduletests.isopen.IsOpenExtension;
import app.packed.moduletests.notopen.NotOpenExtension;

// TODO add BeanIntrospector
public class ExtensionAccessTest {

    @Test
    void extensionisOpen() {
        IsOpenAssembly.build(c -> c.containerRoot().use(IsOpenExtension.class));
    }

    @Test
    void extensionNotOpen() {
        InternalExtensionException e = assertThrows(InternalExtensionException.class,
                () -> IsOpenAssembly.build(c -> c.containerRoot().use(NotOpenExtension.class)));
        assertEquals("class app.packed.moduletests.notopen.NotOpenExtension must be opened to Packed by adding this line "
                + "'opens app.packed.moduletests.notopen to module app.packed' to module-info.java", e.getMessage());
    }

    @Test
    void sidebeanInvokerIsOpen() {
        IsOpenAssembly.build(c -> c.containerRoot().use(IsOpenExtension.class).addSidebeanIsOpen());
    }

    @Test
    void sidebeanInvokerNotOpen() {
        InternalExtensionException e = assertThrows(InternalExtensionException.class,
                () -> IsOpenAssembly.build(c -> c.containerRoot().use(IsOpenExtension.class).addSidebeanNotOpen()));
        assertEquals("interface app.packed.moduletests.notopen.NotOpenInvokerInterface must be opened to Packed by adding this line "
                + "'opens app.packed.moduletests.notopen to module app.packed' to module-info.java", e.getMessage());
    }
}
