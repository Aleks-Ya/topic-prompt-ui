package gptui.util;

import java.io.InputStream;
import java.net.URL;

import static java.util.Objects.requireNonNull;

public class ResourceUtils {

    public static URL resourceUrl(Class<?> clazz, String resource) {
        return requireNonNull(clazz.getResource(resource));
    }

    public static String resourcePath(Class<?> clazz, String resource) {
        return resourceUrl(clazz, resource).toString();
    }

    public static InputStream resourceIS(Class<?> clazz, String resource) {
        return requireNonNull(clazz.getResourceAsStream(resource));
    }

}
