package io.github.spencerpark.jupyter.client.system;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class JupyterPaths {
    private static final class PathAdapter implements JsonSerializer<Path>, JsonDeserializer<Path> {
        @Override
        public Path deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Paths.get(jsonElement.getAsString());
        }

        @Override
        public JsonElement serialize(Path path, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(path.toString());
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Path.class, new PathAdapter())
            .create();
    private static final String PY_PRINT_SYS_PREFIX_TO_STDOUT = "import sys; sys.stdout.write(sys.prefix)";

    public static JupyterPaths withoutSysPrefix() {
        return JupyterPaths.fromSysPrefix(null);
    }

    public static JupyterPaths fromSysPrefix(Path sysPrefix) {
        // config data runtime
        return new JupyterPaths(
                JupyterPaths.getConfigPath(sysPrefix),
                JupyterPaths.getPath(sysPrefix),
                Collections.singletonList(JupyterPaths.getRuntimeDir())
        );
    }

    public static JupyterPaths fromPythonCmd(String pythonCmd) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(pythonCmd, "-c", PY_PRINT_SYS_PREFIX_TO_STDOUT);

        Process process = pb.start();

        try (BufferedReader processOut = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String sysPrefix = processOut.readLine();
            return JupyterPaths.fromSysPrefix(Paths.get(sysPrefix));
        }
    }

    public static JupyterPaths fromJupyterCmd(String jupyterCmd) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(jupyterCmd, "--path", "--json");

        Process process = pb.start();

        try (Reader processOut = new InputStreamReader(process.getInputStream())) {
            return GSON.fromJson(processOut, JupyterPaths.class);
        }
    }

    private enum Os {
        MAC,
        WINDOWS,
        OTHER
    }

    private static Os OS;

    public static Os getOs() {
        if (JupyterPaths.OS == null) {
            // Detect the os in the same manner as jupyter to get similar results
            String osName = System.getProperty("os.name", "assuming linux").toLowerCase(Locale.ENGLISH);
            if (osName.contains("mac") || osName.contains("darwin"))
                JupyterPaths.OS = Os.MAC;
            else if (osName.contains("win"))
                JupyterPaths.OS = Os.WINDOWS;
            else
                JupyterPaths.OS = Os.OTHER;
        }

        return JupyterPaths.OS;
    }

    public static Path getHomeDir() {
        return Paths.get(System.getProperty("user.home")).toAbsolutePath();
    }

    // Get the Jupyter config directory for this platform and user.
    public static Path getConfigDir() {
        Path home = JupyterPaths.getHomeDir();

        if (System.getenv("JUPYTER_NO_CONFIG") != null) {
            try {
                return Files.createTempDirectory("jupyter-clean-cfg").toAbsolutePath();
            } catch (IOException e) {
                throw new RuntimeException("JUPYTER_NO_CONFIG specified but could not create temp directory.", e);
            }
        }

        String envConfigDir = System.getenv("JUPYTER_CONFIG_DIR");
        if (envConfigDir != null)
            return Paths.get(envConfigDir).toAbsolutePath();

        return home.resolve(".jupyter");
    }

    // Get the config directory for Jupyter data files.
    // These are non-transient, non-configuration files.
    public static Path getDataDir() {
        String envDataDir = System.getenv("JUPYTER_DATA_DIR");
        if (envDataDir != null)
            return Paths.get(envDataDir);

        Path home = JupyterPaths.getHomeDir();

        switch (JupyterPaths.getOs()) {
            case MAC:
                // Yes "Jupyter" is uppercase on mac, but nowhere else
                return home.resolve("Library").resolve("Jupyter");
            case WINDOWS:
                String appdataDir = System.getenv("APPDATA");
                if (appdataDir != null)
                    return Paths.get(appdataDir).resolve("jupyter").toAbsolutePath();
                else
                    return JupyterPaths.getConfigDir().resolve("data");
            default:
                String xdgDataHome = System.getenv("XDG_DATA_HOME");
                if (xdgDataHome != null)
                    return Paths.get(xdgDataHome).resolve("jupyter").toAbsolutePath();
                else
                    return home.resolve(".local").resolve("share").resolve("jupyter");
        }
    }

    // Return the runtime dir for transient jupyter files.
    public static Path getRuntimeDir() {
        String envRuntimeDir = System.getenv("JUPYTER_RUNTIME_DIR");
        if (envRuntimeDir != null)
            return Paths.get(envRuntimeDir).toAbsolutePath();

        switch (JupyterPaths.getOs()) {
            case MAC:
            case WINDOWS:
                return JupyterPaths.getDataDir().resolve("runtime");
            default:
                String xdgDataHome = System.getenv("XDG_RUNTIME_HOME");
                if (xdgDataHome != null)
                    return Paths.get(xdgDataHome).resolve("jupyter").toAbsolutePath();
                else
                    return JupyterPaths.getDataDir().resolve("runtime");
        }
    }

    public static List<Path> getSysPath(Path sysPrefix) {
        switch (JupyterPaths.getOs()) {
            case WINDOWS:
                String programData = System.getenv("PROGRAMDATA");
                if (programData != null)
                    return Collections.singletonList(Paths.get(programData).resolve("jupyter").toAbsolutePath());
                else if (sysPrefix != null)
                    return Collections.singletonList(sysPrefix.resolve("share").resolve("jupyter").toAbsolutePath());
                else
                    return Collections.emptyList();
            default:
                List<Path> sysJupyterPath = new ArrayList<>(2);
                sysJupyterPath.add(Paths.get("/usr/local/share/jupyter").toAbsolutePath());
                sysJupyterPath.add(Paths.get("/usr/share/jupyter").toAbsolutePath());
                return sysJupyterPath;
        }
    }

    public static List<Path> getEnvPath(Path sysPrefix) {
        if (sysPrefix == null)
            return Collections.emptyList();
        return Collections.singletonList(sysPrefix.resolve("share").resolve("jupyter").toAbsolutePath());
    }

    // Return a list of directories to search for data files.
    public static List<Path> getPath(Path sysPrefix) {
        List<Path> paths = new ArrayList<>();

        String jupyterPath = System.getenv("JUPYTER_PATH");
        if (jupyterPath != null) {
            // JUPYTER_PATH is set and should be interpreted as a path separated list of directories
            for (String path : jupyterPath.split(File.pathSeparator))
                paths.add(Paths.get(path).toAbsolutePath());
        }

        paths.add(JupyterPaths.getDataDir());

        List<Path> envPath = JupyterPaths.getEnvPath(sysPrefix);
        List<Path> sysPath = JupyterPaths.getSysPath(sysPrefix);

        envPath.stream()
                .filter(ep -> !sysPath.contains(ep))
                .forEachOrdered(paths::add);

        paths.addAll(sysPath);

        return paths;
    }

    public static List<Path> getSysConfigPath() {
        switch (JupyterPaths.getOs()) {
            case WINDOWS:
                String programData = System.getenv("PROGRAMDATA");
                if (programData != null)
                    return Collections.singletonList(Paths.get(programData).resolve("jupyter").toAbsolutePath());
                else
                    return Collections.emptyList();
            default:
                List<Path> sysJupyterPath = new ArrayList<>(2);
                sysJupyterPath.add(Paths.get("/usr/local/etc/jupyter").toAbsolutePath());
                sysJupyterPath.add(Paths.get("/etc/jupyter").toAbsolutePath());
                return sysJupyterPath;
        }
    }

    public static List<Path> getEnvConfigPath(Path sysPrefix) {
        if (sysPrefix == null)
            return Collections.emptyList();
        return Collections.singletonList(sysPrefix.resolve("etc").resolve("jupyter").toAbsolutePath());
    }

    // Return the search path for Jupyter config files as a list.
    public static List<Path> getConfigPath(Path sysPrefix) {
        List<Path> paths = new LinkedList<>();
        paths.add(JupyterPaths.getConfigDir());

        if (System.getenv("JUPYTER_NO_CONFIG") != null)
            return paths;

        String envConfigPath = System.getenv("JUPYTER_CONFIG_PATH");
        if (envConfigPath != null)
            Arrays.stream(envConfigPath.split(File.pathSeparator))
                    .map(Paths::get)
                    .forEachOrdered(paths::add);

        List<Path> envPath = JupyterPaths.getEnvConfigPath(sysPrefix);
        List<Path> sysPath = JupyterPaths.getSysConfigPath();

        envPath.stream()
                .filter(ep -> !sysPath.contains(ep))
                .forEachOrdered(paths::add);

        paths.addAll(sysPath);

        return paths;
    }

    @SerializedName("config")
    private final List<Path> configDirs;
    @SerializedName("data")
    private final List<Path> dataDirs;
    @SerializedName("runtime")
    private final List<Path> runtimeDirs;

    private JupyterPaths() {
        // For gson, don't use
        this.configDirs = Collections.emptyList();
        this.dataDirs = Collections.emptyList();
        this.runtimeDirs = Collections.emptyList();
    }

    public JupyterPaths(List<Path> configDirs, List<Path> dataDirs, List<Path> runtimeDirs) {
        this.configDirs = configDirs;
        this.dataDirs = dataDirs;
        this.runtimeDirs = runtimeDirs;
    }

    // --paths
    // runtime: [jupyter_runtime_dir()]
    // config: jupyter_config_path()
    // data: jupyter_path()

    // --config-dir: jupyter_config_dir()
    // --data-dir: jupyter_data_dir()
    // --runtime-dir: jupyter_runtime_dir()


    public List<Path> getConfigDirs() {
        return configDirs;
    }

    public List<Path> getDataDirs() {
        return dataDirs;
    }

    public List<Path> getRuntimeDirs() {
        return runtimeDirs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("JupyterPaths {\n");
        sb.append("\tconfigDirs = ").append(configDirs).append('\n');
        sb.append("\tdataDirs = ").append(dataDirs).append('\n');
        sb.append("\truntimeDirs = ").append(runtimeDirs).append('\n');
        sb.append("}\n");
        return sb.toString();
    }
}
