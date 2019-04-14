package io.github.spencerpark.jupyter.ipywidgets.mock;

import io.github.spencerpark.jupyter.ipywidgets.props.WidgetPropertyContainer;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;

import java.util.UUID;

public class MockWidgetContext implements WidgetContext {
    @Override
    public MockRemoteWidgetState connect(WidgetPropertyContainer container) {
        return null;
    }

    @Override
    public UUID registerInstance(WidgetPropertyContainer instance) {
        return null;
    }

    @Override
    public void unregisterInstance(WidgetPropertyContainer instance) {

    }

    @Override
    public WidgetPropertyContainer lookupInstance(UUID id) {
        return null;
    }
}
