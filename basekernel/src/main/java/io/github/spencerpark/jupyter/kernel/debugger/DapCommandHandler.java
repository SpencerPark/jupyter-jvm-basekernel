package io.github.spencerpark.jupyter.kernel.debugger;

@FunctionalInterface
public interface DapCommandHandler<A, B> {
    B handle(A arguments) throws DapException;
}
