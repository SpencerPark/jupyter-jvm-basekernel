package io.github.spencerpark.jupyter.api;

import io.github.spencerpark.jupyter.api.comm.CommManager;
import io.github.spencerpark.jupyter.api.display.DisplayData;
import io.github.spencerpark.jupyter.api.display.Renderer;
import io.github.spencerpark.jupyter.api.history.HistoryManager;
import io.github.spencerpark.jupyter.api.magic.registry.Magics;

import java.util.List;

public interface JupyterKernel {
    public static final String IS_COMPLETE_YES = "complete";
    public static final String IS_COMPLETE_BAD = "invalid";
    public static final String IS_COMPLETE_MAYBE = "unknown";

    public Renderer renderer();

    public JupyterIO io();

    public default void display(DisplayData data) {
        this.io().display.display(data);
    }

    public CommManager comms();

    public Magics magics();

    /**
     * Get the active history manager for the kernel. If the history is ignored this method
     * should return {@code null}.
     *
     * @return the active {@link HistoryManager} or {@code null}.
     */
    public HistoryManager history();

    /**
     * Formats an error into a human friendly format. The default implementation prints
     * the stack trace as written by {@link Throwable#printStackTrace()} with a dividing
     * separator as a prefix.
     * <p>
     * Subclasses may override this method write better messages for specific errors but
     * may choose to still use this to display the stack trace. In this case it is recommended
     * to add the output of this call to the end of the output list.
     *
     * @param e the error to format
     *
     * @return a list of lines that make up the formatted error. This format should
     *         not include strings with newlines but rather separate strings each to go on a
     *         new line.
     */
    public List<String> formatError(Exception e);

    public String getBanner();

    public List<LanguageInfo.Help> getHelpLinks();

    /**
     * Inspect the code to get things such as documentation for a function. This is
     * triggered by {@code shift-tab} in the Jupyter notebook which opens a tooltip displaying
     * the returned bundle.
     * <p>
     * This should aim to return docstrings, function signatures, variable types, etc for
     * the value at the cursor position.
     *
     * @param code        the entire code cell to inspect
     * @param at          the character position within the code cell
     * @param extraDetail true if more in depth detail is requested (for example IPython
     *                    includes the function source in addition to the documentation)
     *
     * @return an output bundle for displaying the documentation or null if nothing is found
     *
     * @throws Exception if the code cannot be inspected for some reason (such as it not
     *                   compiling)
     */
    public DisplayData inspect(String code, int at, boolean extraDetail) throws Exception;

    public DisplayData eval(String expr) throws Exception;

    /**
     * Try to autocomplete code at a user's cursor such as finishing a method call or
     * variable name. This is triggered by {@code tab} in the Jupyter notebook.
     * <p>
     * If a single value is returned the replacement range in the {@code code} is replaced
     * with the return value.
     * <p>
     * If multiple matches are returned, a tooltip with the values in the order they are
     * returned is displayed that can be selected from.
     * <p>
     * If no matches are returned, no replacements are made. Effectively this is a no-op
     * in that case.
     *
     * @param code the entire code cell containing the code to complete
     * @param at   the character position that the completion is requested at
     *
     * @return the replacement options containing a list of replacement texts and a
     *         source range to overwrite with a user selected replacement from the list
     *
     * @throws Exception if code cannot be completed due to code compilation issues, or
     *                   similar. This should not be thrown if not replacements are available but rather just
     *                   an empty replacements returned.
     */
    public ReplacementOptions complete(String code, int at) throws Exception;

    /**
     * Check if the code is complete. This gives frontends the tools to provide
     * console environments that hold of executing code in situations such as
     * {@code "for (int i = 0; i < 10; i++)"} and rather let the newline go to
     * the next line for the developer to input the body of the for loop.
     * <p>
     * There are 4 cases to consider:
     * <p>
     * 1. {@link #IS_COMPLETE_MAYBE} is returned by default and is the equivalent
     * of abstaining from answering the request. <br>
     * 2. {@link #IS_COMPLETE_BAD} should be returned for invalid code that will
     * result in an error when being parsed/compiled. <br>
     * 3. {@link #IS_COMPLETE_YES} if the code is a complete, well formed, statement
     * and may be executed. <br>
     * 4. The code is valid but not yet complete (like the for loop example above). In
     * this case a string describing the prefix to start the next line with (such as 4 spaces
     * following the for loop). <br>
     *
     * @param code the code to analyze
     *
     * @return {@link #IS_COMPLETE_MAYBE}, {@link #IS_COMPLETE_BAD}, {@link #IS_COMPLETE_YES},
     *         or an indent string
     */
    public String isComplete(String code);

    public LanguageInfo getLanguageInfo();

    /**
     * The name of the kernel should be unique to this implementation. It is typically the name of the project
     * providing the implementation. This name can be used to disambiguate kernels implementing the same language. i.e.
     * IJava vs. another kernel that also implements `java` as it's {@link LanguageInfo#getName() language}. The IJava
     * kernel would return {@code "IJava"}.
     *
     * @return the name of the kernel.
     */
    public String getKernelName();

    /**
     * Coupled with the {@link #getKernelName() kernel name}, this provides the version. This version is free to update
     * independently of the {@link LanguageInfo#getVersion() language version}.
     *
     * @return the version of the kernel.
     */
    public String getKernelVersion();

    /**
     * Invoked when the kernel.json specifies an {@code interrupt_mode} of {@code message}
     * and the frontend requests an interrupt of the currently running cell.
     */
    public void interrupt();
}
