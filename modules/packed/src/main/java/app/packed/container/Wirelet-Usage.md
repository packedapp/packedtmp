---- MainWirelet(String[] args)
Only usable at the root of a namespace for the CliExtension - It is an error otherwise
Takes last specified
Can be specified at both build time and runtime (last one will override)


---- Wirelets Consumption
BuildTime: Assembly.selectWirelets(), Extension.selectWirelets
LaunchTime: Optional<XWirelet>, WireletSelection<XWirelet>   (Optional assumes Inheritable


---- Wirelet properties



--- Ideas
  (Would be nice to have the possibility of have the value injected) @WireletInject(MainWirelet.class) public Optional<String[]> args;
  Maybe have a ValueWirelet??? That can be injected