package io.github.spencerpark.jupyter.ipywidgets;

import io.github.spencerpark.jupyter.kernel.comm.Comm;

public class Widget<S> {
    private Comm comm;

    private S state;

    protected Widget(WidgetManager manager, S state) {
        this.state = state;
    }

    private void syncState() {

    }

    public void updateState(StatePatch<S> patch) {
        S next = patch.apply(this.state);
        if (next != null)
            this.state = next;
        this.syncState();
    }
}
