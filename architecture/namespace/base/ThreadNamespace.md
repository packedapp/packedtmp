# Overview

A Thread namespace manages all threads and thread pools, creation and termination of the threads when an application is shutdown.

Thread namespaces are not supported by unmanaged applications.
A managed application always has at least 1 thread namespace (owned by the user) in order to support 
multiple threaded startup and shutdown.

# Truths
Vi bliver noedt til at supportere shutdown asynchronous,
  Ville vaere maerkeligt, hvis man fx kalder .shutdown() indefra en starting metode.
  Som saa begynder at koere alt stop koden, mens vi stadig er i start koden
 

1 Shared ThreadNamespace