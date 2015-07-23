/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.data.internal.controller;

import org.phenotips.data.DictionaryPatientData;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.PatientDataController;

import org.xwiki.component.annotation.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.IntegerProperty;
import com.xpn.xwiki.objects.ListProperty;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Handles allergies information.
 *
 * @version $Id$
 */
@Component(roles = { PatientDataController.class })
@Named("allergies")
@Singleton
public class AllergiesController extends AbstractComplexController<Object>
{
    private static final String DATA_NAME = "allergiesData";

    private static final String ALLERGIES = "allergies";

    private static final String NKDA = "NKDA";

    /** Logging helper object. */
    @Inject
    private Logger logger;

    @Override
    public String getName()
    {
        return DATA_NAME;
    }

    @Override
    protected String getJsonPropertyName()
    {
        return getName();
    }

    @Override
    protected List<String> getProperties()
    {
        return Arrays.asList(ALLERGIES, NKDA);
    }

    @Override
    protected List<String> getBooleanFields()
    {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getCodeFields()
    {
        return Collections.emptyList();
    }

    @Override
    public PatientData<Object> load(Patient patient)
    {
        try {
            XWikiDocument doc = (XWikiDocument) this.documentAccessBridge.getDocument(patient.getDocument());
            BaseObject data = doc.getXObject(Patient.CLASS_REFERENCE);
            if (data == null) {
                throw new NullPointerException("The patient does not have a PatientClass");
            }

            Map<String, Object> result = new LinkedHashMap<String, Object>();

            // allergies
            ListProperty allergiesListProperty = (ListProperty) data.get(ALLERGIES);
            List<String> allergiesList = allergiesListProperty.getList();
            result.put(ALLERGIES, allergiesList);

            // NKDA
            IntegerProperty nkdaProperty = (IntegerProperty) data.get(NKDA);
            Integer nkdaInteger = (Integer) nkdaProperty.getValue();
            Boolean nkda = Boolean.TRUE;
            if (nkdaInteger != 1) {
                nkda = Boolean.FALSE;
            }
            result.put(NKDA, nkda);

            return new DictionaryPatientData<>(getName(), result);
        } catch (Exception e) {
            this.logger.error(
                "Could not find requested document or some unforeseen error has occurred during controller loading");
        }
        return null;
    }

    @Override
    public void writeJSON(Patient patient, JSONObject json, Collection<String> selectedFieldNames)
    {
        PatientData<Object> allergiesData = patient.getData(DATA_NAME);
        if (allergiesData == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> allergies = (List<String>) allergiesData.get(ALLERGIES);
        Boolean nkda = (Boolean) allergiesData.get(NKDA);

        if (nkda != null && nkda.booleanValue()) {
            json.put(ALLERGIES, NKDA);
            return;
        }

        if (allergies != null && allergies.size() > 0) {
            JSONArray allergiesArray = new JSONArray();
            allergiesArray.addAll(allergies);
            json.put(ALLERGIES, allergiesArray);
        }
    }

    @Override
    public PatientData<Object> readJSON(JSONObject json)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> allergiesList = new LinkedList<>();
        Boolean nkda = Boolean.FALSE;

        if (json.has(ALLERGIES)) {
            Object allergiesValue = json.get(ALLERGIES);

            if (allergiesValue instanceof JSONArray) {
                // Allergies list found
                JSONArray allergiesJsonArray = (JSONArray) allergiesValue;

                @SuppressWarnings("unchecked")
                Iterator<String> iterator = allergiesJsonArray.iterator();
                while (iterator.hasNext()) {
                    allergiesList.add(iterator.next());
                }

                nkda = Boolean.FALSE;
            } else if (allergiesValue instanceof String) {
                // NKDA is true
                String allergiesString = (String) allergiesValue;
                if (NKDA.equals(allergiesString)) {
                    nkda = Boolean.TRUE;
                }
            }

            // otherwise there's no allergies information for patient
        }

        result.put(NKDA, nkda);
        result.put(ALLERGIES, allergiesList);
        return new DictionaryPatientData<>(DATA_NAME, result);
    }
}
