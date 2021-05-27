package app.packed.application;

import app.packed.application.UseCases.Guest;
import app.packed.service.ServiceConfiguration;

public interface InstalledApplicationConfiguration<T> {
    ServiceConfiguration<Launcher<T>> provideSingleLauncher();
    ServiceConfiguration<Guest> provideGuest();
}