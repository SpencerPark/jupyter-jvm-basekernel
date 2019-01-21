package io.github.spencerpark.jupyter.ipywidgets;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.StatePatch;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetComm;
import io.github.spencerpark.jupyter.kernel.BaseKernel;
import io.github.spencerpark.jupyter.kernel.comm.CommManager;
import io.github.spencerpark.jupyter.kernel.display.DisplayDataRenderable;
import io.github.spencerpark.jupyter.kernel.display.RenderContext;
import io.github.spencerpark.jupyter.kernel.display.mime.MIMEType;

import java.util.Set;

public class Widget<S extends WidgetPropertyContainer> implements DisplayDataRenderable {
    private final WidgetComm comm;

    private final S state;

    protected Widget(WidgetManager manager, S state) {
        this.state = state;
        this.state.notifyChildren(manager);

    }

    public void open(BaseKernel kernel) {
        this.open(kernel.getCommManager());
    }

    public void open(CommManager manager) {

    }

    @Override
    public Set<MIMEType> getSupportedRenderTypes() {
        return null; // TODO
    }

    @Override
    public Set<MIMEType> getPreferredRenderTypes() {
        return null;
    }

    @Override
    public void render(RenderContext context) {
//TODO
        WidgetPropertyContainer container = new WidgetPropertyContainer();
        container.connect(manager);
        container.property("");

        container.getProperty("dsadsa").set(blah);
        container
    }

    private void syncState() {
        StatePatch patch = this.state.createPatch();
        this.comm.updateState(patch);
    }

    public void updateState(StatePatch<S> patch) {
        S next = patch.apply(this.state);
        if (next != null)
            this.state = next;
        this.syncState();
    }
}
