# Functionality

1.
Somehow genetic to configure the namespace when creating it

Let us say build transformers provide a new bean.
How can we control the service namespace later on... And say you know what the current container should be root... 
We have already added the bean.

* We could allow people to change the namespace configuration
* Tell people onNew is a bad way to add new beans...

I think the last one is best


2- W