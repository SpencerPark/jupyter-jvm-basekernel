package io.github.spencerpark.jupyter.ipywidgets.gson;

import io.github.spencerpark.jupyter.ipywidgets.common.Description;
import io.github.spencerpark.jupyter.ipywidgets.protocol.WidgetContext;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class WidgetsGsonTest {
    public void t() {
        WidgetContext ctx;

        Description d = ctx.inflate(Description::new);
        d.style.get().width.get();
    }
}