package topicpromptui.core.util;

import java.io.InputStream;
import java.net.URL;

import static java.util.Objects.requireNonNull;

public class ResourceUtils {

    private ResourceUtils() {
    }

    public static URL resourceUrl(Class<?> clazz, String resource) {
        return requireNonNull(clazz.getResource(resource));
    }

    public static InputStream resourceIS(Class<?> clazz, String resource) {
        return requireNonNull(clazz.getResourceAsStream(resource));
    }

}
