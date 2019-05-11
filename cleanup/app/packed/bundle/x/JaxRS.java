package app.packed.bundle.x;

import app.packed.contract.Contract;

public class JaxRS {

    public static final Contract v2_1 = null;

    public static final Contract v3_0 = null; // rebind(OldInterface, newInterface, new OldInterfaceToNewInterfaceAdaptor()));
    // Ideen er at kan supportere updates ved at omskrive behov for en gammel klasse

    public static final Contract LATEST = v3_0;
}
