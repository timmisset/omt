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

    BuiltInUtil builtInUtil = BuiltInUtil.SINGLETON;

    @Test
    void parseBuiltInOperators() throws IOException {
        // ARRANGE
        URL url = Thread.currentThread().getContextClassLoader().getResource("builtinOperators.ts");
        assert url != null;
        File file = new File(url.getPath());

        String content = new String(Files.readAllBytes(file.toPath()));


        // ACT
        JsonObject jsonObject = builtInUtil.parseBuiltIn(content, BuiltInType.Operator);

        // ASSERT
        assertNotNull(jsonObject);
        assertTrue(jsonObject.has("AND"));
    }

    @Test
    void parseParseJsonCommand() throws IOException {
        // ARRANGE
        URL url = Thread.currentThread().getContextClassLoader().getResource("json-parse-command.ts");
        assert url != null;
        File file = new File(url.getPath());

        String content = new String(Files.readAllBytes(file.toPath()));


        // ACT
        JsonObject jsonObject = builtInUtil.parseJsonCommand(content);

        // ASSERT
        assertNotNull(jsonObject);
        assertTrue(jsonObject.has("JSON_PARSE"));
    }

    @Test
    void parseHttpCommands() throws IOException {
        // ARRANGE
        URL url = Thread.currentThread().getContextClassLoader().getResource("http-commands.ts");
        assert url != null;
        File file = new File(url.getPath());

        String content = new String(Files.readAllBytes(file.toPath()));


        // ACT
        JsonObject jsonObject = builtInUtil.parseHttpCommands(content);

        // ASSERT
        assertNotNull(jsonObject);
        assertTrue(jsonObject.has("HTTP_GET"));
        assertTrue(jsonObject.has("HTTP_POST"));
        assertTrue(jsonObject.has("HTTP_PUT"));

    }

    @Test
    void parseBuiltInCommands() throws IOException {
        // ARRANGE
        URL url = Thread.currentThread().getContextClassLoader().getResource("builtinCommands.ts");
        assert url != null;
        File file = new File(url.getPath());

        String content = new String(Files.readAllBytes(file.toPath()));

        // ACT
        JsonObject jsonObject = builtInUtil.parseBuiltIn(content, BuiltInType.Command);

        // ASSERT
        assertNotNull(jsonObject);
        assertTrue(jsonObject.has("ADD_TO"));
    }
}
