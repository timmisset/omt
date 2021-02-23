package com.misset.opp.util;

import com.google.common.io.CharStreams;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Helper {

    public static List<String> getResources(List<String> names, String folder) {
        ClassLoader classLoader = getClassLoader();

        return names.stream()
                .map(name -> {
                    String format = String.format("%s/%s", folder, name);
                    InputStream inputStream = classLoader.getResourceAsStream(format);
                    try (Reader reader = new InputStreamReader(Objects.requireNonNull(inputStream))) {
                        return CharStreams.toString(reader);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "";
                    }
                })
                .collect(Collectors.toList());
    }

    private static ClassLoader getClassLoader() {
        return Objects.requireNonNull(PluginManagerCore.getPlugin(PluginId.getId("com.misset.OMT"))).getPluginClassLoader();
    }

    public static File getResource(String item) {
        URL url = getClassLoader().getResource(item);
        return new File(Objects.requireNonNull(url).getPath());
    }

    public static String getResourceAsString(String item) throws IOException {
        File file = getResource(item);
        return new String(Files.readAllBytes(file.toPath()));
    }

}
