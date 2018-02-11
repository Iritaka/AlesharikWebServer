/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.configuration.config.lang.parser;

import com.alesharik.webserver.api.reflection.FieldAccessor;
import lombok.val;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigParserInstructionsHeaderTest {
    private FileReader fileReader;

    @BeforeClass
    public static void setup() throws NoSuchFieldException {
        Field verbose = Constants.class.getDeclaredField("VERBOSE");
        verbose.setAccessible(true);
        FieldAccessor.setField(null, true, verbose);
        Constants.PRINT_CONFIG = true;
    }

    @Before
    public void setUp() throws Exception {
        fileReader = mock(FileReader.class);
    }

    @Test
    public void parseBasicDefine() {
        String test = "#define 'a' 'b'";
        ConfigParserInstructionsHeader header = ConfigParserInstructionsHeader.parse(Collections.singletonList(test), null, fileReader);
        assertTrue(header.getDefines().containsKey("a"));
        assertEquals("b", header.getDefines().get("a"));
    }

    @Test
    public void parseBasicIf() {
        val result = parse(
                "#define 'a' 'b'",
                "#ifdef a",
                "#define 'c' 'true'",
                "#endif",
                "#ifdef q",
                "define 'wat' 'true'",
                "#endif");
        assertTrue(result.containsKey("c"));
        assertEquals("true", result.get("c"));
        assertFalse(result.containsKey("wat"));
    }

    @Test
    public void parseIfndef() {
        val result = parse(
                "#define 'a' 'b'",
                "#ifndef c",
                "#define 'c' 'true'",
                "#endif",
                "#ifndef c",
                "define 'wat' 'true'",
                "#endif");
        assertTrue(result.containsKey("c"));
        assertEquals("true", result.get("c"));
        assertFalse(result.containsKey("wat"));
    }


    @Test
    public void parseComment() {
        val result = parse(
                "#define 'a' 'b'",
                "//This is a comment",
                "//#define 'wat' 'true'",
                "#ifndef c",
                "#define 'c' 'true'",
                "#endif",
                "#ifndef c",
                "define 'wat' 'true'",
                "#endif");
        assertTrue(result.containsKey("c"));
        assertEquals("true", result.get("c"));
        assertFalse(result.containsKey("wat"));
    }

    @Test
    public void parseSimpleInclude() {
        when(fileReader.readFile(argThat(argument -> argument.toString().equals("test.h")))).thenReturn(Collections.singletonList("#define 'a' 'b'"));
        val result = parse("#include test.h");
        assertTrue(result.containsKey("a"));
        assertEquals("b", result.get("a"));
    }

    @Test
    public void parseInclude() {
        when(fileReader.readFile(argThat(argument -> argument.toString().equals("test.h")))).thenReturn(Arrays.asList(
                "#ifdef asd",
                "#define 'a' 'b'",
                "#endif"));
        val result = parse(
                "#define 'asd' 'true'",
                "#include test.h",
                "#ifdef a",
                "#define 'ok' 'true'",
                "#endif");
        assertTrue(result.containsKey("a"));
        assertTrue(result.containsKey("asd"));
        assertTrue(result.containsKey("ok"));
        assertEquals("b", result.get("a"));
        assertEquals("true", result.get("asd"));
        assertEquals("true", result.get("ok"));
    }

    @Test
    public void parseSubBlocks() {
        val result = parse(
                "#define 'a' 'a'",
                "#define 'b' 'b'",
                "#define 'c' 'c'",
                "#ifdef a",
                "   #ifdef d",
                "       #define 'wat' 'true'",
                "   #endif",
                "   #define 'ok1' 'true'",
                "   #ifdef b",
                "       #ifdef c",
                "           #define 'ok2' 'true'",
                "       #endif",
                "   #endif",
                "#endif");
        assertFalse(result.containsKey("wat"));
        assertTrue(result.containsKey("ok1"));
        assertTrue(result.containsKey("ok2"));
        assertEquals("true", result.get("ok1"));
        assertEquals("true", result.get("ok2"));
    }

    @Test
    public void useResolver() {
        ConfigParserInstructionsHeader resolver = ConfigParserInstructionsHeader.parse(Arrays.asList("#define 'a' 'b'", "#define 'qwe' 'asd'"), null, fileReader);
        val result = parse(resolver,
                "#ifdef a",
                "#define 'b' 'ok'",
                "#endif",
                "#ifndef ui",
                "#define 'ok' 'true'",
                "#endif");
        assertFalse(result.containsKey("a"));
        assertTrue(result.containsKey("b"));
        assertEquals("ok", result.get("b"));
        assertTrue(result.containsKey("ok"));
        assertEquals("true", result.get("ok"));
    }

    @Test(expected = CodeParsingException.class)
    public void aLotOfEndsError() {
        parse("#ifdef a",
                "#define 'b' 'c'",
                "#endif",
                "#endif");
    }

    @Test(expected = CodeParsingException.class)
    public void tooLessEndsError() {
        parse("#ifdef a",
                "#ifdef a",
                "#define 'b' 'c'",
                "#endif");
    }

    @Test(expected = CodeParsingException.class)
    public void defineSyntaxError() {
        parse("#define asd qwe");
    }

    @Test(expected = CodeParsingException.class)
    public void defineSyntaxLessParametersError() {
        parse("#define 'asd'    ");
    }

    @Test(expected = CodeParsingException.class)
    public void defineSyntaxNoSpaceError() {
        parse("#define 'asd''b'");
    }

    @Test(expected = CodeParsingException.class)
    public void ifdefSyntaxError() {
        parse("#ifdef       ");
    }

    @Test(expected = CodeParsingException.class)
    public void ifndefSyntaxError() {
        parse("#ifndef   ");
    }

    @Test(expected = CodeParsingException.class)
    public void includeSyntaxError() {
        parse("#include         ");
    }

    @Test
    public void appendTest() {
        val a = ConfigParserInstructionsHeader.parse(Collections.singletonList("#define 'a' 'b'"), null, fileReader);
        val b = ConfigParserInstructionsHeader.parse(Collections.singletonList("#define 'c' 'd'"), null, fileReader);
        a.append(b);
        assertTrue(a.getDefines().containsKey("a"));
        assertTrue(a.getDefines().containsKey("c"));
        assertEquals("b", a.getDefines().get("a"));
        assertEquals("d", a.getDefines().get("c"));
    }

    private Map<String, String> parse(String... list) {
        return ConfigParserInstructionsHeader.parse(Arrays.asList(list), null, fileReader).getDefines();
    }

    private Map<String, String> parse(ConfigParserInstructionsHeader resolver, String... list) {
        return ConfigParserInstructionsHeader.parse(Arrays.asList(list), resolver, fileReader).getDefines();
    }
}