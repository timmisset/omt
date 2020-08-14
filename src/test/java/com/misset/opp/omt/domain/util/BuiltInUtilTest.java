package com.misset.opp.omt.domain.util;

import com.google.gson.JsonObject;
import com.misset.opp.omt.external.util.builtIn.BuiltInType;
import com.misset.opp.omt.external.util.builtIn.BuiltInUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BuiltInUtilTest {

    @Test
    void parseBuiltInOperators() throws IOException {
        // ARRANGE
        URL url = Thread.currentThread().getContextClassLoader().getResource("builtinOperators.ts");
        assert url != null;
        File file = new File(url.getPath());

        String content = new String(Files.readAllBytes(file.toPath()));


        // ACT
        JsonObject jsonObject = BuiltInUtil.parseBuiltIn(content, BuiltInType.Operators);

        // ASSERT
        assertNotNull(jsonObject);
        assertTrue(jsonObject.has("AND"));
    }

    @Test
    void parseBuiltInCommands() throws IOException {
        // ARRANGE
        URL url = Thread.currentThread().getContextClassLoader().getResource("builtinCommands.ts");
        assert url != null;
        File file = new File(url.getPath());

        String content = new String(Files.readAllBytes(file.toPath()));

        // ACT
        JsonObject jsonObject = BuiltInUtil.parseBuiltIn(content, BuiltInType.Commands);

        // ASSERT
        assertNotNull(jsonObject);
        assertTrue(jsonObject.has("ADD_TO"));
    }
}
