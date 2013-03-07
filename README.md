IsleOfLostToys
==============

Where sad, pathetic, rejected code goes to live until some loving child finds a use for them.

This project contains code that was written but, for some reason or another, was never used. It is stored
here so its memory will not be forgotten.

## Containers

This utility class encapsulates generic functions for dealing with unrelated container classes - maps and collections,
for instance. It allows us to generically check for "isEmpty" on an object type, more specifically. Further uses
have not yet been devised.


## ObjectDefaults

This is an extensible class designed to return default values for various objects, including primitives, wrappers,
enums and objects. It utilizes the "@Defaulted" annotation to default enumerations and objects if available.


##ObjectDefaulter

This utility class allows a consumer to fill the fields of a class (and potentially its superclasses) with default
values.


##Defaulted

This annotation defines a "default" value for an object. It should be used on a static function that accepts
no parameters and returns an instance of the implementing object.


##DirtyObject

This class allows an object to retain the "null state" of its protected and private fields while still allowing
default values to be returned from "get" functions. It contains a boolean that can be turned on or off to
retrieve values in their null state (null if not "dirty"), or as their default values. Useful when null state
needs to be preserved.


##DirtyObjects

This utility class exists for use by users of classes with "DirtyObject" support. It can turn the null state on
or off, and can easily populate defaults into the object without changing null status.