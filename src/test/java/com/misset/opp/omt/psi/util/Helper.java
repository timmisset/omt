package com.misset.opp.omt.psi.util;

import java.io.File;
import java.net.URL;

public class Helper {

    public static File getResource(String item) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(item);
        assert url != null;
        return new File(url.getPath());
    }
}
