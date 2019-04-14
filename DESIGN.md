# Design

Widgets are a collection of properties. A property container has the following property types:
1.  `property`: a plain, observable, wrapped value.
2.  `inline`: a container such that all properties are inlined into the parent container. These properties may be prefixed to put them under a namespace.
3.  `isolated`: these properties are on the object but not synchronised. The intention behind the naming is that they handle their own updates and as such are out of the control of the container that holds a reference to it. It is *on it's own* (isolated).

A container is always associated with a context in which it was created. The context is meant to provide the host for mounting the widgets but otherwise should remain somewhat opaque. User implementations should never need to provide it, just pass it around. For example a jupyter kernel would provide a context in the user space.