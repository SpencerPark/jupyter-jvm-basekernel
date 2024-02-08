package io.github.spencerpark.jupyter.messages.reply;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.kernel.LanguageInfo;
import io.github.spencerpark.jupyter.messages.ContentType;
import io.github.spencerpark.jupyter.messages.MessageType;
import io.github.spencerpark.jupyter.messages.ReplyType;
import io.github.spencerpark.jupyter.messages.request.KernelInfoRequest;

import java.util.List;

public class KernelInfoReply implements ContentType<KernelInfoReply>, ReplyType<KernelInfoRequest> {
    public static final MessageType<KernelInfoReply> MESSAGE_TYPE = MessageType.KERNEL_INFO_REPLY;
    public static final MessageType<KernelInfoRequest> REQUEST_MESSAGE_TYPE = MessageType.KERNEL_INFO_REQUEST;

    @Override
    public MessageType<KernelInfoReply> getType() {
        return MESSAGE_TYPE;
    }

    @Override
    public MessageType<KernelInfoRequest> getRequestType() {
        return REQUEST_MESSAGE_TYPE;
    }

    /**
     * Semantic version string. X.Y.Z
     */
    @SerializedName("protocol_version")
    protected String protocolVersion;

    /**
     * Ex. 'ipython' for IPython
     */
    @SerializedName("implementation")
    protected String implementationName;

    /**
     * Semantic version string for the kernel
     */
    @SerializedName("implementation_version")
    protected String implementationVersion;

    @SerializedName("language_info")
    protected LanguageInfo langInfo;

    /**
     * An optional banner text about the kernel.
     */
    protected String banner;

    /**
     * True if the kernel supports debugging, false (default) otherwise.
     */
    @SerializedName("debugger")
    protected boolean supportsDebugging;

    /**
     * Optional help links about the kernel language
     */
    @SerializedName("help_links")
    protected List<LanguageInfo.Help> helpLinks;

    public KernelInfoReply(String protocolVersion, String implementationName, String implementationVersion, LanguageInfo langInfo, String banner, boolean supportsDebugging, List<LanguageInfo.Help> helpLinks) {
        this.protocolVersion = protocolVersion;
        this.implementationName = implementationName;
        this.implementationVersion = implementationVersion;
        this.langInfo = langInfo;
        this.banner = banner;
        this.supportsDebugging = supportsDebugging;
        this.helpLinks = helpLinks;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getImplementationName() {
        return implementationName;
    }

    public String getImplementationVersion() {
        return implementationVersion;
    }

    public LanguageInfo getLangInfo() {
        return langInfo;
    }

    public String getBanner() {
        return banner;
    }

    public boolean getSupportsDebugging() {
        return supportsDebugging;
    }

    public List<LanguageInfo.Help> getHelpLinks() {
        return helpLinks;
    }
}
