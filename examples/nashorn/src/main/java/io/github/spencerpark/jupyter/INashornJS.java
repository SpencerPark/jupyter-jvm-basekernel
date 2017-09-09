package io.github.spencerpark.jupyter;

import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class INashornJS {
    public static void main(String[] args) throws Exception {
        if (args.length < 1)
            throw new IllegalArgumentException("Missing connection file argument");

        Path connectionFile = Paths.get(args[0]);

        if (!Files.isRegularFile(connectionFile))
            throw new IllegalArgumentException("Connection file '" + connectionFile + "' isn't a file.");

        String contents = new String(Files.readAllBytes(connectionFile));

        JupyterSocket.JUPYTER_LOGGER.setLevel(Level.WARNING);

        KernelConnectionProperties connProps = KernelConnectionProperties.parse(contents);
        JupyterConnection connection = new JupyterConnection(connProps);

        NashornKernel kernel = new NashornKernel();
        kernel.becomeHandlerForConnection(connection);

        connection.waitUntilClose();
    }
}
