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

package com.alesharik.webserver.reporting;

import com.alesharik.webserver.api.ComputerData;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.EqualsAndHashCode;
import org.glassfish.grizzly.utils.Charsets;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ReporterName("cpu")
@EqualsAndHashCode
@Prefixes({"[Reporter]", "[CpuReporter]"})
public final class CpuReporter extends Reporter {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private File file;
    private ReportType reportType;

    @Override
    public void setup(@Nonnull File file, long tickPeriod, Element config) {

        this.file = file;
        if(!file.exists()) {
            try {
                if(!file.createNewFile()) {
                    Logger.log("Can't create file " + file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        parseReportType(config);
    }

    @Override
    public void reload(Element config) {
        parseReportType(config);
    }

    private void parseReportType(Element config) {
        Node reportTypeNode = config.getElementsByTagName("type").item(0);
        if(reportTypeNode == null) {
            reportType = ReportType.XML;
        } else {
            reportType = ReportType.formString(reportTypeNode.getTextContent());
        }
    }

    @Override
    protected void report() throws Exception {
        switch (reportType) {
            case CSV:
                reportCSV();
                break;
            case XML:
                reportXml();
                break;
            case JSON:
                reportJson();
                break;
        }
    }

    private void reportCSV() throws IOException {
        CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF8_CHARSET));
        List<String[]> entries = csvReader.readAll();
        csvReader.close();

        Date date = new Date();
        date.setTime(System.currentTimeMillis());

        for(int i = 0; i < ComputerData.INSTANCE.getCoreCount(); i++) {
            String[] line = new String[10];
            line[0] = date.toString();
            line[1] = String.valueOf(i);
            int j = 2;

            int zeroCount = 0;
            for(ComputerData.CoreLoadType coreLoadType : ComputerData.CoreLoadType.values()) {
                long coreTime = ComputerData.INSTANCE.getCoreTime(i, coreLoadType);
                if(coreTime == 0) {
                    zeroCount++;
                }
                line[j++] = String.valueOf(coreTime);
            }
            if(zeroCount == 8) {
                Logger.log("CPU data read warning!");
                return;
            }
            entries.add(line);
        }

        CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF8_CHARSET));
        csvWriter.writeAll(entries);
        csvWriter.flush();
        csvWriter.close();
    }

    private void reportJson() throws IOException {
        Optional<String> content = Files.readAllLines(file.toPath()).stream().reduce(String::concat);
        JsonArray elements;
        elements = content.map(s -> new JsonParser().parse(s).getAsJsonArray()).orElseGet(JsonArray::new);

        Date date = new Date();
        date.setTime(System.currentTimeMillis());

        JsonObject object = new JsonObject();
        object.add("time", new JsonPrimitive(date.toString()));
        for(int i = 0; i < ComputerData.INSTANCE.getCoreCount(); i++) {
            int zeroCount = 0;
            for(ComputerData.CoreLoadType coreLoadType : ComputerData.CoreLoadType.values()) {
                long coreTime = ComputerData.INSTANCE.getCoreTime(i, coreLoadType);
                if(coreTime == 0) {
                    zeroCount++;
                }
            }
            if(zeroCount == 8) {
                Logger.log("CPU data read warning!");
                return;
            }

            JsonObject toAdd = new JsonObject();
            toAdd.add("user", new JsonPrimitive(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.USER)));
            toAdd.add("nice", new JsonPrimitive(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.NICE)));
            toAdd.add("system", new JsonPrimitive(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.SYSTEM)));
            toAdd.add("idle", new JsonPrimitive(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.IDLE)));
            toAdd.add("iowait", new JsonPrimitive(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.IOWAIT)));
            toAdd.add("irq", new JsonPrimitive(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.IRQ)));
            toAdd.add("softirq", new JsonPrimitive(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.SOFTIRQ)));
            toAdd.add("all", new JsonPrimitive(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.ALL)));

            object.add("cpu" + i, toAdd);
        }
        elements.add(object);
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file, false), Charsets.UTF8_CHARSET)) {
            fileWriter.write(GSON.toJson(elements));
            fileWriter.flush();
        }

    }

    private void reportXml() throws ParserConfigurationException, IOException, TransformerException { //TODO rewrite
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Date date = new Date();
        date.setTime(System.currentTimeMillis());

        Element data = document.createElement("data");

        Element time = document.createElement("time");
        time.setTextContent(date.toString());
        data.appendChild(time);

        Element cpus = document.createElement("cpus");
        for(int i = 0; i < ComputerData.INSTANCE.getCoreCount(); i++) {
            Element cpu = document.createElement("cpu");
            cpus.appendChild(cpu);

            int zeroCount = 0;
            for(ComputerData.CoreLoadType coreLoadType : ComputerData.CoreLoadType.values()) {
                long coreTime = ComputerData.INSTANCE.getCoreTime(i, coreLoadType);
                if(coreTime == 0) {
                    zeroCount++;
                }
            }
            if(zeroCount == 8) {
                Logger.log("CPU data read warning!");
                return;
            }

            Element id = document.createElement("id");
            id.setTextContent(String.valueOf(i));
            cpu.appendChild(id);

            Element user = document.createElement("user");
            user.setTextContent(String.valueOf(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.USER)));
            cpu.appendChild(user);

            Element nice = document.createElement("nice");
            nice.setTextContent(String.valueOf(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.NICE)));
            cpu.appendChild(nice);

            Element system = document.createElement("system");
            system.setTextContent(String.valueOf(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.SYSTEM)));
            cpu.appendChild(system);

            Element idle = document.createElement("idle");
            idle.setTextContent(String.valueOf(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.IDLE)));
            cpu.appendChild(idle);

            Element iowait = document.createElement("iowait");
            iowait.setTextContent(String.valueOf(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.IOWAIT)));
            cpu.appendChild(iowait);

            Element irq = document.createElement("irq");
            irq.setTextContent(String.valueOf(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.IRQ)));
            cpu.appendChild(irq);

            Element softirq = document.createElement("softirq");
            softirq.setTextContent(String.valueOf(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.SOFTIRQ)));
            cpu.appendChild(softirq);

            Element all = document.createElement("all");
            all.setTextContent(String.valueOf(ComputerData.INSTANCE.getCoreTime(i, ComputerData.CoreLoadType.ALL)));
            cpu.appendChild(all);
        }
        data.appendChild(cpus);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(data);

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(source, new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("[\n\r]", "");
        writer.close();

        try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file, true), Charsets.UTF8_CHARSET)) {
            fileWriter.write(output);
            fileWriter.flush();
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void shutdownNow() {
    }

    @Override
    public String getName() {
        return "cpu";
    }

    private enum ReportType {
        CSV("csv"),
        JSON("json"),
        XML("xml");

        private final String typeName;

        ReportType(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }

        /**
         * Default is XML
         */
        public static ReportType formString(String str) {
            switch (str) {
                case "csv":
                    return CSV;
                case "json":
                    return JSON;
                default:
                case "xml":
                    return XML;
            }
        }
    }
}
