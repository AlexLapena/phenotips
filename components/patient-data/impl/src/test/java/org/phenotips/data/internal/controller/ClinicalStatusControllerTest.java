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

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.PatientDataController;
import org.phenotips.data.SimpleValuePatientData;
import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import java.util.Collection;
import java.util.LinkedList;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * Test for the {@link ClinicalStatusController} component,
 * implementation of the {@link org.phenotips.data.PatientDataController} interface
 */
public class ClinicalStatusControllerTest
{
    @Rule
    public MockitoComponentMockingRule<PatientDataController> mocker =
        new MockitoComponentMockingRule<PatientDataController>(ClinicalStatusController.class);

    private Logger logger;

    private DocumentAccessBridge documentAccessBridge;

    private ClinicalStatusController controller;

    @Mock
    private Patient patient;

    @Mock
    private XWikiDocument doc;

    @Mock
    private BaseObject data;

    private final String AFFECTED = "affected";

    private final String UNAFFECTED = "unaffected";

    private final String DATA_NAME = "clinicalStatus";

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        this.controller = (ClinicalStatusController)this.mocker.getComponentUnderTest();
        this.logger = this.mocker.getMockedLogger();
        this.documentAccessBridge = this.mocker.getInstance(DocumentAccessBridge.class);

        DocumentReference patientDocument = new DocumentReference("wiki", "patient", "00000001");
        doReturn(patientDocument).when(this.patient).getDocument();
        doReturn(this.doc).when(this.documentAccessBridge).getDocument(patientDocument);
        doReturn(this.data).when(this.doc).getXObject(Patient.CLASS_REFERENCE);
    }

    @Test
    public void loadCatchesExceptionWhenPatientDoesNotHavePatientClass()
    {
        doReturn(null).when(this.doc).getXObject(Patient.CLASS_REFERENCE);

        PatientData<String> result = this.controller.load(this.patient);

        verify(this.logger).error("Could not find requested document or some unforeseen"
            + " error has occurred during controller loading ",
            PatientDataController.ERROR_MESSAGE_NO_PATIENT_CLASS);
        Assert.assertNull(result);
    }

    @Test
    public void loadReturnsNullWhenGivenUnexpectedIntValue()
    {
        doReturn(7314862).when(this.data).getIntValue(UNAFFECTED);

        PatientData<String> result = this.controller.load(this.patient);

        Assert.assertNull(result);
    }

    @Test
    public void loadReturnsAffectedString()
    {
        doReturn(0).when(this.data).getIntValue(UNAFFECTED);

        PatientData<String> result = this.controller.load(this.patient);

        Assert.assertEquals(AFFECTED, result.getValue());
    }

    @Test
    public void loadReturnsUnaffectedString()
    {
        doReturn(1).when(this.data).getIntValue(UNAFFECTED);

        PatientData<String> result = this.controller.load(this.patient);

        Assert.assertEquals(UNAFFECTED, result.getValue());
    }

    @Test
    public void writeJSONReturnsWhenGetDataReturnsNull() {
        doReturn(null).when(this.patient).getData(DATA_NAME);
        JSONObject json = new JSONObject();

        this.controller.writeJSON(this.patient, json);

        Assert.assertTrue(json.getJSONObject(DATA_NAME) == null || json.getJSONObject(DATA_NAME).isNullObject());
    }

    @Test
    public void writeJSONWithSelectedFieldsReturnsWhenGetDataReturnsNull() {
        doReturn(null).when(this.patient).getData(DATA_NAME);
        JSONObject json = new JSONObject();
        Collection<String> selectedFields = new LinkedList<>();
        selectedFields.add(DATA_NAME);

        this.controller.writeJSON(this.patient, json, selectedFields);

        Assert.assertTrue(json.getJSONObject(DATA_NAME) == null || json.getJSONObject(DATA_NAME).isNullObject());
    }

    @Test
    public void writeJSONAddsAffectedValueToJSON() {
        PatientData<String> data = new SimpleValuePatientData<>(DATA_NAME, AFFECTED);
        doReturn(data).when(this.patient).getData(DATA_NAME);
        JSONObject json = new JSONObject();

        this.controller.writeJSON(this.patient, json);

        Assert.assertNotNull(json.getJSONObject(DATA_NAME));
        Assert.assertEquals(AFFECTED, json.getJSONObject(DATA_NAME).get(DATA_NAME));
    }

    @Test
    public void writeJSONWithSelectedFieldsAddsAffectedValueToJSON() {
        PatientData<String> data = new SimpleValuePatientData<>(DATA_NAME, AFFECTED);
        doReturn(data).when(this.patient).getData(DATA_NAME);
        JSONObject json = new JSONObject();
        Collection<String> selectedFields = new LinkedList<>();
        selectedFields.add(DATA_NAME);

        this.controller.writeJSON(this.patient, json, selectedFields);

        Assert.assertNotNull(json.getJSONObject(DATA_NAME));
        Assert.assertEquals(AFFECTED, json.getJSONObject(DATA_NAME).get(DATA_NAME));
    }

    @Test
    public void writeJSONAddsUnaffectedValueToJSON() {
        PatientData<String> data = new SimpleValuePatientData<>(DATA_NAME, UNAFFECTED);
        doReturn(data).when(this.patient).getData(DATA_NAME);
        JSONObject json = new JSONObject();

        this.controller.writeJSON(this.patient, json);

        Assert.assertNotNull(json.getJSONObject(DATA_NAME));
        Assert.assertEquals(UNAFFECTED, json.getJSONObject(DATA_NAME).get(DATA_NAME));
    }

    @Test
    public void writeJSONWithSelectedFieldsAddsUnaffectedValueToJSON() {
        PatientData<String> data = new SimpleValuePatientData<>(DATA_NAME, UNAFFECTED);
        doReturn(data).when(this.patient).getData(DATA_NAME);
        JSONObject json = new JSONObject();
        Collection<String> selectedFields = new LinkedList<>();
        selectedFields.add(DATA_NAME);

        this.controller.writeJSON(this.patient, json, selectedFields);

        Assert.assertNotNull(json.getJSONObject(DATA_NAME));
        Assert.assertEquals(UNAFFECTED, json.getJSONObject(DATA_NAME).get(DATA_NAME));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void saveIsUnsupported(){
        this.controller.save(this.patient);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void readJSONIsUnsupported(){
        this.controller.readJSON(new JSONObject());
    }

    @Test
    public void checkGetName()
    {
        Assert.assertEquals(DATA_NAME, this.controller.getName());
    }


}
