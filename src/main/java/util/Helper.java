package util;

import com.google.common.io.CharStreams;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class Helper {

    public static boolean hasResourcePath(String item) {
        return Thread.currentThread().getContextClassLoader().getResource(item) != null;
    }

    public static List<String> getResources(List<String> names, String folder) {
        ClassLoader classLoader = getClassLoader();

        return names.stream()
                .map(name -> {
                    String format = String.format("%s/%s", folder, name);
                    InputStream inputStream = classLoader.getResourceAsStream(format);
                    try (Reader reader = new InputStreamReader(inputStream)) {
                        String data = CharStreams.toString(reader);
                        return data;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "";
                    }
                })
                .collect(Collectors.toList());
    }

    private static ClassLoader getClassLoader() {
        return PluginId.getRegisteredIds().containsKey("com.misset.OMT") ?
                PluginManager.getPlugin(PluginId.getId("com.misset.OMT")).getPluginClassLoader() :
                Thread.currentThread().getContextClassLoader();
    }

    public static File getResource(String item) {
        URL url = getClassLoader().getResource(item);

        if (url == null) {
            return null;
        }
        return new File(url.getPath());
    }

    public static String getResourceAsString(String item) throws IOException {
        File file = getResource(item);
        return new String(Files.readAllBytes(file.toPath()));
    }

}
