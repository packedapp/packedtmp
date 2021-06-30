package app.packed.extension.sandbox;

import app.packed.extension.Extension;

// Er virkelig ikke speciel vild med at skulle skrive implementeringen der

// Maaske er det det der bliver extensors???? Og saa er nuvaerende extensors->Extension Bean
public @interface ExtensionService {
    Class<? extends Extension> extension();

    Class<?> implementation();
}

///// Alternativt
//Maaske er det et interface man skal implementere...
//implements ServiceExtension<ConvExtension>
//Saa er det ihvertfald let at dokumentere...
//Det er tilgaengaeldt meget invasivt...
