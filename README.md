# Jupyter JVM BaseKernel

This project is a Java implementation of the [Jupyter kernel messaging spec](http://jupyter-client.readthedocs.io/en/latest/messaging.html). Consumers of this project need to simply extend the `BaseKernel` and implement the things specific to the language that the kernel is working for. A simple example using the [Nashorn JS engine](https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/nashorn/api.html) can be found in the examples folder.

#### Why use this base kernel?

Firstly **don't use this kernel simply because you prefer a JVM language over python**. If it is no trouble to use the python base, go for it! Consider that using a JVM project is going to require user to have a JVM installed which unless your project is already using it, is a big extra dependency.

**Do use this kernel if your kernel's language is already running on the JVM**. In this case Java is already a requirement and interfacing with your compiler/interpreter can be much easier when things are all in one process. For example a Groovy, kotlin, Clojure, Scala, [MellowD](https://github.com/SpencerPark/MellowD) (check this one out, it's great!), etc. kernels would already be running on the JVM and so this simply lets it speak to Jupyter or **any other client that speaks the protocol**. Check out [Hydrogen](https://atom.io/packages/hydrogen), an atom extension for executing code inline by speaking with Jupyter kernels (this could be you!).

The implementation aims to cover the messaging protocol alone. It supports some base classes for naive autocomplete, history, comms, etc. to make getting started painless but this is all opt-in functionality that your implementation doesn't have to touch. At the very least you don't have to reinvent the wheel and can get started by **interfacing with a high level communication layer that implements the messaging protocol**. 

#### BaseKernel

The base kernel requires, at a very minimum to get things up and running, an implementation for `eval` which evaluates some code in the kernel's language. In the Nashorn case this code is nashorn's flavour of javascript. 