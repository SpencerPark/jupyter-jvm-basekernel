package io.github.spencerpark.jupyter.kernel.debugger;

@FunctionalInterface
public interface DapNoArgCommandHandler<B> {
    B handle() throws DapException;
}
