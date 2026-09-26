package topicpromptui.core.config;

/**
 * Write half of {@code config.properties}, kept apart from the read-only {@link ConfigModel} that
 * {@code core.ai}, {@code core.prompt} and {@code core.storagefilesystem} consume — they have no
 * business writing, and a test double of theirs must not be able to touch the real file.
 */
public interface ConfigWriter {
    /** Persists one property. Never throws: a failed write is logged and only durability is lost. */
    void setProperty(String propertyName, String value);
}
