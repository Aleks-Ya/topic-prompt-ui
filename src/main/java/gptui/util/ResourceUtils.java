package gptui.util;

import com.google.common.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

    public static String resourceContent(Class<?> clazz, String resource) {
        try {
            return Resources.toString(requireNonNull(clazz.getResource(resource)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
