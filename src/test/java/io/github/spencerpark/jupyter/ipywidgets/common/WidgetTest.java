package io.github.spencerpark.jupyter.ipywidgets.common;

import io.github.spencerpark.jupyter.ipywidgets.mock.MockRemoteWidgetState;
import io.github.spencerpark.jupyter.ipywidgets.mock.MockWidgetContext;
import io.github.spencerpark.jupyter.ipywidgets.protocol.RemoteWidgetState;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WidgetTest {
    public MockWidgetContext context;

    @Before
    public void setUp() throws Exception {
        this.context = new MockWidgetContext();
    }

    @After
    public void tearDown() throws Exception {
        this.context = null;
    }

    @Test
    public void syncsSimpleChanges() {
        IntText t = this.context.inflate(IntText::new);
        MockRemoteWidgetState remote = (MockRemoteWidgetState) t.connect();

        assertEquals(0, remote.getState().getIntProp("value"));
        t.value.set(10);
        assertEquals(10, remote.getState().getIntProp("value"));
    }

    @Test
    public void syncsSimpleInlinedChanges() {
        IntText t = this.context.inflate(IntText::new);
        MockRemoteWidgetState remote = (MockRemoteWidgetState) t.connect();

        assertNull(remote.getState().getProp("description_tooltip"));
        t.description.descriptionTooltip.set("test");
        assertEquals("test", remote.getState().getStringProp("description_tooltip"));
    }

    @Test
    public void syncsRenamedInlinedChanges() {
        Layout l = this.context.inflate(Layout::new);
        MockRemoteWidgetState remote = (MockRemoteWidgetState) l.connect();

        assertNull(remote.getState().getProp("grid_auto_flow"));
        l.grid.auto.flow.set(GridAuto.GridAutoFlow.COLUMN);
        assertEquals("column", remote.getState().getStringProp("grid_auto_flow"));
    }

    @Test
    public void syncsNestedWidgets() {
        IntText t = this.context.inflate(IntText::new);
        MockRemoteWidgetState remote = (MockRemoteWidgetState) t.description.style.get().connect();

        assertNull(remote.getState().getProp("description_width"));
        t.description.style.get().width.set("10px");
        assertEquals("10px", remote.getState().getStringProp("description_width"));
    }
}
