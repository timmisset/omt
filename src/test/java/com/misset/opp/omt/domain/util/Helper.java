package com.misset.opp.omt.domain.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;

public class Helper {

    public static File getResource(String item) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(item);
        assert url != null;
        return new File(url.getPath());
    }
    public static String getResourceAsString(String item) throws IOException {
        File file = getResource(item);
        return new String(Files.readAllBytes(file.toPath()));
    }
}
