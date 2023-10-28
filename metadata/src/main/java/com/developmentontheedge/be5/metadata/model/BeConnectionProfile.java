package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.model.base.BeElementWithProperties;
import com.developmentontheedge.be5.metadata.model.base.BeModelElementSupport;
import com.developmentontheedge.be5.metadata.sql.ConnectionUrl;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.Strings2;
import com.developmentontheedge.beans.AbstractDynamicPropertySet;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.annot.PropertyDescription;
import com.developmentontheedge.beans.annot.PropertyName;
import one.util.streamex.StreamEx;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class BeConnectionProfile extends BeModelElementSupport implements BeElementWithProperties
{
    private static final String PROJECT_NAME_PLACEHOLDER = "{projectName}";

    // Eclipse Data Tools Platform (DTP) connection profile settings
    private String providerId;
    private String driverDefinition;
    private ConnectionUrl connectionUrl;
    private String username;
    private String password;
    private boolean isProtected;
    private final Map<String, String> properties = new LinkedHashMap<>();
    private String[] propertiesToRequest;

    public BeConnectionProfile(final String name, final BeConnectionProfiles origin)
    {
        super(name, origin);
        connectionUrl = new ConnectionUrl(getProject().getDatabaseSystem());
    }

    @Override
    protected void fireChanged()
    {
        final BeConnectionProfiles connectionProfiles = (BeConnectionProfiles) getOrigin();

        if (connectionProfiles.get(getName()) == this)
            connectionProfiles.fireCodeChanged();
    }

    // Getters and setters

    public ConnectionUrl getJdbcUrl()
    {
        return connectionUrl;
    }

    @PropertyName("Provider ID")
    public String getProviderId()
    {
        return providerId;
    }

    /**
     * Uses connection URL to determine the provider ID.
     *
     * @return provider ID or empty string if can't determine an RDBMS
     */
    public String getDefaultProviderId()
    {
        final Rdbms rdbms = getRdbms();

        if (rdbms != null)
            return rdbms.getProviderId();

        return "";
    }

    public boolean hasDefaultProviderId()
    {
        return Strings2.nullToEmpty(getProviderId()).equals(getDefaultProviderId());
    }

    public void setProviderId(final String providerId)
    {
        this.providerId = providerId;
        fireChanged();
    }

    @PropertyName("Driver definition")
    public String getDriverDefinition()
    {
        return driverDefinition;
    }

    public String getDefaultDriverDefinition()
    {
        final Rdbms rdbms = getRdbms();

        if (rdbms != null)
            return rdbms.getDriverDefinition();

        return "";
    }

    public boolean hasDefaultDriverDefinition()
    {
        return Strings2.nullToEmpty(getDriverDefinition()).equals(getDefaultDriverDefinition());
    }

    public void setDriverDefinition(final String driverDefinition)
    {
        this.driverDefinition = driverDefinition;
        fireChanged();
    }

    @PropertyName("Connection URL")
    public String getConnectionUrl()
    {
        return connectionUrl.toString();
    }

    public void setConnectionUrl(final String connectionUrl)
    {
        this.connectionUrl = new ConnectionUrl(connectionUrl);
        fireChanged();
    }

    @PropertyName("Protected profile")
    @PropertyDescription("Additional confirmation will be issued when using this profile")
    public boolean isProtected()
    {
        return isProtected;
    }

    public void setProtected(boolean isProtected)
    {
        this.isProtected = isProtected;
    }

    /**
     * Tries to determine a database name.
     *
     * @return database name or empty string if it can't determine the name
     */
    public String determineDatabaseName()
    {
        return connectionUrl.getDb();
    }

    @PropertyName("Database user name")
    public String getUsername()
    {
        return username;
    }

    public void setUsername(final String username)
    {
        this.username = username;
        fireChanged();
    }

    @PropertyName("Password")
    public String getPassword()
    {
        return password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
        fireChanged();
    }

    @Override
    public Collection<String> getPropertyNames()
    {
        return properties.keySet();
    }

    @Override
    public String getProperty(String name)
    {
        return properties.get(name);
    }

    @Override
    public void setProperty(String name, String value)
    {
        properties.put(name, value);
        fireChanged();
    }

    public DynamicPropertySet getProperties()
    {
        return new AbstractDynamicPropertySet()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public int size()
            {
                return properties.size();
            }

            @Override
            public boolean replaceWith(String name, DynamicProperty prop)
            {
                if (!properties.containsKey(name)) return false;
                properties.put(name, (String) prop.getValue());
                return true;
            }

            @Override
            public void renameProperty(String from, String to)
            {
                String value = properties.get(from);
                properties.remove(from);
                properties.put(to, value);
            }

            @Override
            public Object remove(String name)
            {
                return properties.remove(name);
            }

            @Override
            public Iterator<DynamicProperty> propertyIterator()
            {
                return StreamEx.ofKeys(properties).map(this::findProperty).iterator();
            }

            @Override
            public Iterator<String> nameIterator()
            {
                return properties.keySet().iterator();
            }

            @Override
            public boolean moveTo(String name, int index)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean contains(DynamicProperty property)
            {
                return properties.containsKey(property.getName());
            }

            @Override
            public Map<String, Object> asMap()
            {
                return new LinkedHashMap<>(properties);
            }

            @Override
            public boolean addBefore(String propName, DynamicProperty property)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAfter(String propName, DynamicProperty property)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public void add(DynamicProperty property)
            {
                properties.put(property.getName(), (String) property.getValue());
            }

            @Override
            protected DynamicProperty findProperty(String name)
            {
                String value = properties.get(name);
                return value == null ? null : new DynamicProperty(name, String.class, value);
            }
        };
    }

    public Rdbms getRdbms()
    {
        return connectionUrl.getRdbms();
    }

    public String[] getPropertiesToRequest()
    {
        return propertiesToRequest;
    }

    public void setPropertiesToRequest(String[] propertiesToRequest)
    {
        this.propertiesToRequest = propertiesToRequest;
        fireChanged();
    }

}
