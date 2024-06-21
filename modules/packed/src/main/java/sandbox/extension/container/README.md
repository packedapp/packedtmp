GuestComponent -> Application | Container | Bean

ComponentGuestAdaptorBean -> Embeds a Single Guest Component (Cannot directly launch it)

// Is it just a fucking normal bean???
ComponentGuestLauncherBean -> Launches Guests, and can manage them Embeds any number of ComponentHostBeans
  MH (... -> ComponentGuest)

ApplicationDeployerBean -> Like Launcher but can remove and add Applications (Cannot add/remove beans or methods) 
[ComponentHostCollection 1..Many]
----
HostBean <- Holds everything on the Guest   

Alternative naming ComponentHostBean->ComponentGuestHolderBean || ComponentGuestWrapperBean || ComponentGuestAdaptorBean

 ComponentGuestLauncherBean -> Host

// Hvem specificere ApplicationTemplaten??? Det maa jo vaere hvad der er Bootstrap Consumer nu!!!! 
// Selvfoelgelig Det er jo en application template vi har. 
// Vi har ogsaa en for BootstrapApp
// ApplicationTemplate maa ogsaa noedvendigvis have en for RootContaineren i applicationen
// Det betyder jo saa ogsaa at der er maaske er ting den ikke kan den root container. Det er jo udefra
 
LEVELS

1) Bootstrap (Single UnmanagedBean that gets a MethodHandle launcher)
2) Bean that gets a Launcher (MethodHandle) ApplicationTemplate?

// Maybe HolderType is in ApplicationTemplate???
GuestBeanHolder <- newHostedApplication(Assembly, ApplicationTemplate, HolderClassType.class, Wirelet...)
HostBean.Props <- newHostOfBeans(HolderClassType.class) <-- We need a tmp class to configure before installing it
HostBean <- newHostOfContainers(HolderClassType.class)
HostBean <- newHostOfApplications(HolderClassType.class) // Man kan jo ogsaa bare soerge for man kun registrere en type

// Skal bare have en GuestLauncher Injected (Maaske faar man bare et GuestLauncher[] eller __Map<String, GuestLauncher>__ (hvis flere))
// En HostBean kan kun manage en Holder type (Som skal vides paa forhaan for scannings skyld) // launchers.values.iterators().next();


@OnStateChange <-- Kan man faa injected GuestLauncher ogsaa... Ved jeg ikke om man skal bruge kan jo have det i GuestHolderen

HostBean.add(GuestHolderBeanConfiguration...) (Must be same container, must be)

3) Bean that gets Multiple Launchers injected (se 2ern)

4) Bean that launchers + ComponentHostCollections

//// Skal konfigures naar man laver host beanen -> Map<String, CHC> har en enkelt der hedder main 

// Map<String, String> (LauncherName -> HostCollectionName)


5) Alle Andre plus ImageRepository (GuestLauncher