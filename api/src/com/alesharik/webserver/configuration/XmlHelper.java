package com.alesharik.webserver.configuration;

import com.alesharik.webserver.control.AdminDataStorage;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.module.server.SecuredStoreModule;
import lombok.experimental.UtilityClass;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class XmlHelper {
    private static Configuration configuration = null;

    public static void setConfiguration(Configuration config) {
        if(configuration == null) {
            configuration = config;
        }
    }

    /**
     * Get {@link SecuredStoreModule} form xml config
     *
     * @param nodeName name of node, which contains {@link SecuredStoreModule} name
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link SecuredStoreModule} instance
     */
    @Nullable
    public static SecuredStoreModule getSecuredStore(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            try {
                return (SecuredStoreModule) configuration.getModuleByName(nameNode.getTextContent());
            } catch (ClassCastException e) {
                throw new ConfigurationParseError("Node " + nodeName + " type not expected!", e);
            }
        }
    }

    /**
     * Get {@link AdminDataStorage} form xml config
     *
     * @param nodeName name of node, which contains {@link AdminDataStorage} name
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link AdminDataStorage} instance
     */
    @Nullable
    public static AdminDataStorage getAdminDataStorage(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            try {
                return (AdminDataStorage) configuration.getModuleByName(nameNode.getTextContent());
            } catch (ClassCastException e) {
                throw new ConfigurationParseError("Node " + nodeName + " type not expected!", e);
            }
        }
    }

    /**
     * Get {@link DashboardDataHolder} form xml config
     *
     * @param nodeName name of node, which contains {@link DashboardDataHolder} name
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link DashboardDataHolder} instance
     */
    @Nullable
    public static DashboardDataHolder getDashboardDataHolder(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            try {
                return (DashboardDataHolder) configuration.getModuleByName(nameNode.getTextContent());
            } catch (ClassCastException e) {
                throw new ConfigurationParseError("Node " + nodeName + " type not expected!", e);
            }
        }
    }

    /**
     * Get {@link java.io.File} form xml config
     *
     * @param nodeName name of node, which contains {@link java.io.File} address
     * @param config   config node
     * @param required if true, throw {@link ConfigurationParseError} if node not found
     * @return null if required == false and value not found, overwise {@link File} instance
     */
    @Nullable
    public static File getFile(String nodeName, Element config, boolean required) {
        Node nameNode = config.getElementsByTagName(nodeName).item(0);
        if(nameNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + nodeName + " not found!");
            } else {
                return null;
            }
        } else {
            return new File(nameNode.getTextContent());
        }
    }

    /**
     * Return list form xml
     *
     * @param containerNodeName list container node name
     * @param listNode          list item node name
     * @param config            config node
     * @param required          if true, throw {@link ConfigurationParseError} if node not found
     * @return modifiable list
     */
    @Nonnull
    public List<String> getList(String containerNodeName, String listNode, Element config, boolean required) {
        Element containerNode = (Element) config.getElementsByTagName(containerNodeName).item(0);
        if(containerNode == null) {
            if(required) {
                throw new ConfigurationParseError("Node " + containerNodeName + " not found!");
            } else {
                return Collections.emptyList();
            }
        } else {
            List<String> list = new ArrayList<>();
            NodeList elements = containerNode.getElementsByTagName(listNode);
            for(int i = 0; i < elements.getLength(); i++) {
                list.add(elements.item(i).getTextContent());
            }
            return list;
        }
    }
}