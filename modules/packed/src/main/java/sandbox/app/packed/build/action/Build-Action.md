Ideen er alle ting vi goer eller saetter basalt set er en action


#### Relation to BuildHook
More finegrained


#### BuildSource
Lets say we get @Bean(named = "xxxx")
Saa er "BuildSourcen" jo en annotering vil jeg mene. IDK. Det er det gamle ConfigSite jo

Fx Load er vel mere en Action end et ConfigSite. Og dog



???? Mirror/Info
Either logging is the sole information channel, or we a mirror/info class that people can work upon
I think this only makes sense if we can query on the Source, Properties and Component(s)


So 3 options here
1. Logging only
2. Logging + Streaming -> Would have an Info class
3. Logging (+ Streaming?) + Mirror -> Would have a Mirror class


Logging would be nice to see quickly see stuff a.la. assembly.logon("action=bean.install").as("$name,$trace)
Streaming??? When logging is not enough???
Mirrors would be nice if we get a report, so we can click around and see what is going on


???? Definers
Who can define an action, I think it would make sense to allow users as well...
I have a build config file


???? BuildAction activation
Should we use ScopedValue????

???? Component Reference
Most actions has a target component(s) how do we set this


// I'm interested in bean*

Either we have mirrors and streaming observer
Or we only have have streaming (in which case we should probably rename mirror)



--- Bruger perspektiv

--- Extension perspektiv


