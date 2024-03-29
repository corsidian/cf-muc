/**
 * $RCSfile$
 * $Revision: 3144 $
 * $Date: 2005-12-01 14:20:11 -0300 (Thu, 01 Dec 2005) $
 *
 * Copyright (C) 2004-2008 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.emiva.crossfire.plugin.muc.spi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.emiva.crossfire.XMPPServer;
import net.emiva.crossfire.plugin.muc.MUCPlugin;
import net.emiva.crossfire.plugin.muc.cluster.MUCServicePropertyClusterEventTask;
import net.emiva.database.DbConnectionManager;
import net.emiva.util.cache.CacheFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves and stores MUC service properties. Properties are stored in the database.
 *
 * @author Daniel Henninger
 */
public class MUCServiceProperties implements Map<String, String> {

	private static final Logger Log = LoggerFactory.getLogger(MUCServiceProperties.class);

    private static final String LOAD_PROPERTIES = "SELECT name, propValue FROM ofEmiMucServiceProp WHERE serviceID=?";
    private static final String INSERT_PROPERTY = "INSERT INTO ofEmiMucServiceProp(serviceID, name, propValue) VALUES(?,?,?)";
    private static final String UPDATE_PROPERTY = "UPDATE ofEmiMucServiceProp SET propValue=? WHERE serviceID=? AND name=?";
    private static final String DELETE_PROPERTY = "DELETE FROM ofEmiMucServiceProp WHERE serviceID=? AND name=?";

    private String subdomain;
    private Long serviceID;
    private Map<String, String> properties;

    public MUCServiceProperties(String subdomain) {
        this.subdomain = subdomain;
        if (properties == null) {
            properties = new ConcurrentHashMap<String, String>();
        }
        else {
            properties.clear();
        }

        MUCPlugin plugin = (MUCPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("muc");
        serviceID = plugin.getMultiUserChatManager().getMultiUserChatServiceID(subdomain);

        if (serviceID == null) {
            Log.debug("MUCServiceProperties: Unable to find service ID for subdomain "+subdomain);
        }
        else {
            loadProperties();
        }
    }

    public int size() {
        return properties.size();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public boolean containsKey(Object key) {
        return properties.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return properties.containsValue(value);
    }

    public Collection<String> values() {
        return Collections.unmodifiableCollection(properties.values());
    }

    public void putAll(Map<? extends String, ? extends String> t) {
        for (Map.Entry<? extends String, ? extends String> entry : t.entrySet() ) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return Collections.unmodifiableSet(properties.entrySet());
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    public String get(Object key) {
        return properties.get(key);
    }

    /**
     * Return all children property names of a parent property as a Collection
     * of String objects. For example, given the properties <tt>X.Y.A</tt>,
     * <tt>X.Y.B</tt>, and <tt>X.Y.C</tt>, then the child properties of
     * <tt>X.Y</tt> are <tt>X.Y.A</tt>, <tt>X.Y.B</tt>, and <tt>X.Y.C</tt>. The method
     * is not recursive; ie, it does not return children of children.
     *
     * @param parentKey the name of the parent property.
     * @return all child property names for the given parent.
     */
    public Collection<String> getChildrenNames(String parentKey) {
        Collection<String> results = new HashSet<String>();
        for (String key : properties.keySet()) {
            if (key.startsWith(parentKey + ".")) {
                if (key.equals(parentKey)) {
                    continue;
                }
                int dotIndex = key.indexOf(".", parentKey.length()+1);
                if (dotIndex < 1) {
                    if (!results.contains(key)) {
                        results.add(key);
                    }
                }
                else {
                    String name = parentKey + key.substring(parentKey.length(), dotIndex);
                    results.add(name);
                }
            }
        }
        return results;
    }

    /**
     * Returns all property names as a Collection of String values.
     *
     * @return all property names.
     */
    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    public String remove(Object key) {
        String value;
        synchronized (this) {
            value = properties.remove(key);
            // Also remove any children.
            Collection<String> propNames = getPropertyNames();
            for (String name : propNames) {
                if (name.startsWith((String)key)) {
                    properties.remove(name);
                }
            }
            deleteProperty((String)key);
        }

        // Generate event.
        Map<String, Object> params = Collections.emptyMap();
        MUCServicePropertyEventDispatcher.dispatchEvent(subdomain, (String)key, MUCServicePropertyEventDispatcher.EventType.property_deleted, params);

        // Send update to other cluster members.
        CacheFactory.doClusterTask(MUCServicePropertyClusterEventTask.createDeleteTask(subdomain, (String) key));

        return value;
    }

    void localRemove(String key) {
        properties.remove(key);
        // Also remove any children.
        Collection<String> propNames = getPropertyNames();
        for (String name : propNames) {
            if (name.startsWith(key)) {
                properties.remove(name);
            }
        }

        // Generate event.
        Map<String, Object> params = Collections.emptyMap();
        MUCServicePropertyEventDispatcher.dispatchEvent(subdomain, key, MUCServicePropertyEventDispatcher.EventType.property_deleted, params);
    }

    public String put(String key, String value) {
        if (key == null || value == null) {
            throw new NullPointerException("Key or value cannot be null. Key=" +
                    key + ", value=" + value);
        }
        if (key.endsWith(".")) {
            key = key.substring(0, key.length()-1);
        }
        key = key.trim();
        String result;
        synchronized (this) {
            if (properties.containsKey(key)) {
                if (!properties.get(key).equals(value)) {
                    updateProperty(key, value);
                }
            }
            else {
                insertProperty(key, value);
            }

            result = properties.put(key, value);
        }

        // Generate event.
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("value", value);
        MUCServicePropertyEventDispatcher.dispatchEvent(subdomain, key, MUCServicePropertyEventDispatcher.EventType.property_set, params);

        // Send update to other cluster members.
        CacheFactory.doClusterTask(MUCServicePropertyClusterEventTask.createPutTask(subdomain, key, value));

        return result;
    }

    void localPut(String key, String value) {
        properties.put(key, value);

        // Generate event.
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("value", value);
        MUCServicePropertyEventDispatcher.dispatchEvent(subdomain, key, MUCServicePropertyEventDispatcher.EventType.property_set, params);
    }

    public String getProperty(String name, String defaultValue) {
        String value = properties.get(name);
        if (value != null) {
            return value;
        }
        else {
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String name) {
        return Boolean.valueOf(get(name));
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String value = get(name);
        if (value != null) {
            return Boolean.valueOf(value);
        }
        else {
            return defaultValue;
        }
    }

    private void insertProperty(String name, String value) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(INSERT_PROPERTY);
            pstmt.setLong(1, serviceID);
            pstmt.setString(2, name);
            pstmt.setString(3, value);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    private void updateProperty(String name, String value) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_PROPERTY);
            pstmt.setString(1, value);
            pstmt.setLong(2, serviceID);
            pstmt.setString(3, name);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    private void deleteProperty(String name) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(DELETE_PROPERTY);
            pstmt.setLong(1, serviceID);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    private void loadProperties() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_PROPERTIES);
            pstmt.setLong(1, serviceID);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString(1);
                String value = rs.getString(2);
                properties.put(name, value);
            }
        }
        catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }
}