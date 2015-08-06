package org.phenotips.data.internal;

import org.phenotips.data.Consent;
import org.phenotips.data.ConsentStatus;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

/**
 *
 * @version $Id$
 * @since 1.2RC1
 */
public class DefaultConsent implements Consent
{
    private String id;
    private String description;
    private boolean required;

    private ConsentStatus status = ConsentStatus.NOT_SET;

    DefaultConsent(String id, String description, boolean required)
    {
        this.id = id;
        this.description = description;
        this.required = required;
    }

    @Override public String getID()
    {
        return this.id;
    }

    @Override public String getDescription()
    {
        return this.description;
    }

    @Override public ConsentStatus getStatus()
    {
        return this.status;
    }

    @Override public void setStatus(ConsentStatus status)
    {
        this.status = status;
    }

    @Override public boolean isRequired()
    {
        return this.required;
    }

    @Override public JSON toJson()
    {
        JSONObject json = new JSONObject();
        json.put("id", this.getID());
        json.put("description", this.getDescription());
        json.put("isRequired", this.isRequired());
        json.put("status", this.getStatus().toString());
        return json;
    }

    @Override public Consent fromJson()
    {
        return new DefaultConsent("", "", false);
    }
}
