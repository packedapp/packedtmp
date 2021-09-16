package app.packed.bundle.host;

import app.packed.application.ApplicationImage;
import app.packed.application.various.UseCases.Guest;
import app.packed.service.ServiceConfiguration;

public interface InstalledApplicationConfiguration<T> {
    
    // Tror den bliver injected lokalt i hosten beanen og ikke som en (container wide) service...
    
    ServiceConfiguration<ApplicationImage<T>> provideSingleLauncher();
    ServiceConfiguration<Guest> provideGuest();
}