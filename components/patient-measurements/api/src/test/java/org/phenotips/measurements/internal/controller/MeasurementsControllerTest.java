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
package org.phenotips.measurements.internal.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.phenotips.data.IndexedPatientData;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.PatientDataController;
import org.phenotips.data.SimpleValuePatientData;
import org.phenotips.measurements.MeasurementHandler;
import org.phenotips.measurements.data.MeasurementEntry;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Test for the {@link MeasurementsController} Component, only the overridden methods from {@link PatientDataController}
 * are tested here
 */
public class MeasurementsControllerTest {


    @Rule
    public MockitoComponentMockingRule<PatientDataController<MeasurementEntry>> mocker =
        new MockitoComponentMockingRule<PatientDataController<MeasurementEntry>>(MeasurementsController.class);

    private DocumentAccessBridge documentAccessBridge;

    @Mock
    private Patient patient;

    @Mock
    private XWikiDocument doc;
    
    @Mock
    private BaseObject obj1;

    @Mock
    private BaseObject obj2;

    private List<BaseObject> measurementXWikiObjects;

    private static final String MEASUREMENTS_STRING = "measurements";

    private static final String CONTROLLER_NAME = MEASUREMENTS_STRING;

    private static final String MEASUREMENT_ENABLING_FIELD_NAME = MEASUREMENTS_STRING;
    
    private static final String DATE_KEY = "date";

    private static final String AGE_KEY = "age";

    private static final String TYPE_KEY = "type";

    private static final String SIDE_KEY = "side";

    private static final String VALUE_KEY = "value";

    private static final String UNIT_KEY = "unit";

    private static final String SD_KEY = "sd";

    private static final String PERCENTILE_KEY = "percentile";

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String ARMSPAN_KEY = "armspan";
    
    
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        this.documentAccessBridge = this.mocker.getInstance(DocumentAccessBridge.class);

        DocumentReference patientDocument = new DocumentReference("wiki", "patient", "00000001");
        doReturn(patientDocument).when(this.patient).getDocument();
        doReturn(this.doc).when(this.documentAccessBridge).getDocument(patientDocument);
        this.measurementXWikiObjects = new LinkedList<>();
        BaseObject obj = mock(BaseObject.class);
        obj.setStringValue(DATE_KEY, "1993-01-02");
        doReturn(this.measurementXWikiObjects).when(this.doc).getXObjects(any(EntityReference.class));
    
    }
    
    @Test
    public void getNameTest() throws ComponentLookupException 
    {
    	Assert.assertEquals(CONTROLLER_NAME, this.mocker.getComponentUnderTest().getName());
    }
    
    // ----------------------------------------Load Tests----------------------------------------
    
    @Test
    public void loadCatchesExceptionFromDocumentAccess() throws Exception
    {
        Exception exception = new Exception();
        doThrow(exception).when(this.documentAccessBridge).getDocument(any(DocumentReference.class));

        PatientData<MeasurementEntry> result = this.mocker.getComponentUnderTest().load(this.patient);

        Assert.assertNull(result);
        verify(this.mocker.getMockedLogger()).error("Could not find requested document or some unforeseen "
            + "error has occurred during controller loading ", exception.getMessage());
    }

    @Test
    public void loadReturnsNull() throws ComponentLookupException
    {
        doReturn(null).when(this.doc).getXObjects(any(EntityReference.class));
        PatientData<MeasurementEntry> result = this.mocker.getComponentUnderTest().load(this.patient);
        Assert.assertNull(result);
        
        doReturn(Collections.<BaseObject>emptyList()).when(this.doc.getXObjects(any(EntityReference.class)));
        Assert.assertNull(this.mocker.getComponentUnderTest().load(this.patient));
    }
    
    @Test
    public void loadIgnoresNullFields() throws ComponentLookupException
    {
        BaseObject obj = mock(BaseObject.class);
        doReturn(null).when(obj).getField(anyString());
        this.measurementXWikiObjects.add(obj);

        PatientData<MeasurementEntry> result = this.mocker.getComponentUnderTest().load(this.patient);

        Assert.assertNull(result);
    }
    
    @Test
    public void dateMissingTest() throws Exception
    {
    	PatientData<MeasurementEntry> result = this.mocker.getComponentUnderTest().load(this.patient);
    	List<MeasurementEntry> internalList = new LinkedList<>();
    	Date date = null;
        String age = "67";
        String type = "armspan";
        String side = "l";
        Double value = 35.2;
        String units = "cm";
    	MeasurementEntry entry = new MeasurementEntry(date, age, type, side, value, units);
    	internalList.add(entry);
    	
    	
    	MeasurementEntry m = internalList.get(0);
    	Assert.assertNull(m.getDate());
    	
    	List<BaseObject> objects = new LinkedList<>();
        objects.add(this.obj1);
        objects.add(null);
        objects.add(this.obj2);
        Assert.assertNotNull(this.measurementXWikiObjects);
    }
    
    @Test
    public void loadTest() throws ComponentLookupException
    {
    	List<MeasurementEntry> internalList = new LinkedList<>();
        String age = "67";
        Date date = new Date(1999-03-03);
        String type = "armspan";
        String side = "l";
        Double value = 35.2;
        String units = "cm";
    	MeasurementEntry entry = new MeasurementEntry(date, age, type, side, value, units);
    	internalList.add(entry);
    	PatientData<MeasurementEntry> result = this.mocker.getComponentUnderTest().load(this.patient);

    	PatientData<MeasurementEntry> patientData = new IndexedPatientData<>(CONTROLLER_NAME, internalList);
        doReturn(patientData).when(this.patient).getData(CONTROLLER_NAME);
        
        Assert.assertEquals("l", entry.getSide());
        Assert.assertEquals("armspan", entry.getType());
        Assert.assertEquals("cm", entry.getUnits());
        Assert.assertSame(value, entry.getValue());
        Assert.assertEquals(date, entry.getDate());
        Assert.assertEquals("67", entry.getAge());
    }

    // ----------------------------------------Write Tests----------------------------------------
    
    @Test
    public void writeJSONReturnsWhenGetDataReturnsNull() throws ComponentLookupException
    {
        doReturn(null).when(this.patient).getData(CONTROLLER_NAME);
        JSONObject json = new JSONObject();
        Collection<String> selectedFields = new LinkedList<>();
        selectedFields.add(MEASUREMENT_ENABLING_FIELD_NAME);

        this.mocker.getComponentUnderTest().writeJSON(this.patient, json, selectedFields);

        Assert.assertFalse(json.has(CONTROLLER_NAME));
        verify(this.patient).getData(CONTROLLER_NAME);
    }
      
    @Test
    public void writeJSONWithNullFieldsReturnsWhenGetDataReturnsNull() throws ComponentLookupException
    {
        doReturn(null).when(this.patient).getData(CONTROLLER_NAME);
        JSONObject json = new JSONObject();

        this.mocker.getComponentUnderTest().writeJSON(this.patient, json);

        Assert.assertFalse(json.has(CONTROLLER_NAME));
        verify(this.patient).getData(CONTROLLER_NAME);
    }
    
    /*Not sure about this one?? Get help*/
    @Test
    public void writeJSONReturnsWhenDataIsEmpty() throws ComponentLookupException
    {
        List<MeasurementEntry> internalList = new LinkedList<>();
        PatientData<MeasurementEntry> patientData = new IndexedPatientData<>(CONTROLLER_NAME, internalList);
        doReturn(patientData).when(this.patient).getData(CONTROLLER_NAME);
        JSONObject json = new JSONObject();
        Collection<String> selectedFields = new LinkedList<>();
        selectedFields.add(MEASUREMENT_ENABLING_FIELD_NAME);

        this.mocker.getComponentUnderTest().writeJSON(this.patient, json, selectedFields);

        Assert.assertFalse(json.has(CONTROLLER_NAME));
        verify(this.patient).getData(CONTROLLER_NAME);
    }
    
    @Test
    public void writeJSONhasNext() throws ComponentLookupException
    {
    	List<MeasurementEntry> internalList = new LinkedList<>();
        String age = "67";
        Date date = new Date(1999-03-03);
        String type = "armspan";
        String side = "l";
        Double value = 35.2;
        String units = "cm";
    	MeasurementEntry entry = new MeasurementEntry(date, age, type, side, value, units);
    	internalList.add(entry);
        PatientData<MeasurementEntry> patientData = new IndexedPatientData<>(CONTROLLER_NAME, internalList);
        doReturn(patientData).when(this.patient).getData(CONTROLLER_NAME);
        
        JSONObject json = new JSONObject();
        
        Collection<String> selectedFields = new LinkedList<>();
        selectedFields.add(MEASUREMENT_ENABLING_FIELD_NAME);
        
    	this.mocker.getComponentUnderTest().writeJSON(this.patient, json, selectedFields);

    	Assert.assertSame("value", json.getDouble(VALUE_KEY));
    }
  
    // ----------------------------------------Read Tests----------------------------------------
    
    @Test
    public void readWithEmptyDataDoesNothing() throws ComponentLookupException
    {
        JSONObject json = new JSONObject();
        json.put(CONTROLLER_NAME, new JSONArray());
        Assert.assertNull(this.mocker.getComponentUnderTest().readJSON(json));
    }
    
    @Test
    public void readWithNullJsonDoesNothing() throws ComponentLookupException
    {
        Assert.assertNull(this.mocker.getComponentUnderTest().readJSON(null));
    }
    
    @Test
    public void readWithNoDataDoesNothing() throws ComponentLookupException
    {
        Assert.assertNull(this.mocker.getComponentUnderTest().readJSON(new JSONObject()));
    }
   
    @Test
    public void readWorksCorrectly() throws ComponentLookupException
    {
    	JSONArray data = new JSONArray();
    	JSONObject item = new JSONObject();
    	item.put(DATE_KEY, "1993-01-02");
    	item.put(AGE_KEY, 13);   	
    	item.put(TYPE_KEY, "armspan");
    	item.put(SIDE_KEY, "");
    	item.put(VALUE_KEY, 23.5);
    	item.put(UNIT_KEY, "cm");
    	data.put(item);
    	item = new JSONObject();
    	item.put(DATE_KEY, "1994-01-02");
    	item.put(AGE_KEY, 13);   	
    	item.put(TYPE_KEY, "weight");
    	item.put(SIDE_KEY, "");
    	item.put(VALUE_KEY, 23.5);
    	item.put(UNIT_KEY, "kg");
    	data.put(item);
    	JSONObject json = new JSONObject();
    	json.put(CONTROLLER_NAME, data);
    	PatientData<MeasurementEntry> result = this.mocker.getComponentUnderTest().readJSON(json);
    	Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.isIndexed());
    }
    
    @Test
    public void jsonEntryReturnsNull() throws ComponentLookupException
    {
    	JSONArray data = new JSONArray();
    	JSONObject item = new JSONObject();
    	item.put(AGE_KEY, 13);   	
    	item.put(SIDE_KEY, "");
    	item.put(VALUE_KEY, "2");
    	item.put(UNIT_KEY, "cm");
    	data.put(item);
    	JSONObject json = new JSONObject();
    	json.put(CONTROLLER_NAME, data);
    	PatientData<MeasurementEntry> result = this.mocker.getComponentUnderTest().readJSON(json);
    	Assert.assertNull(result);
    }
    
    @Test
    public void DuplicateDateTest() throws ComponentLookupException
    {
    	JSONArray data = new JSONArray();
    	JSONObject item = new JSONObject();
    	item.put(DATE_KEY, "1993-01-02");
    	item.put(AGE_KEY, 13);   	
    	item.put(TYPE_KEY, "armspan");
    	item.put(SIDE_KEY, "");
    	item.put(VALUE_KEY, 23.5);
    	item.put(UNIT_KEY, "cm");
    	data.put(item);
    	item = new JSONObject();
    	item.put(DATE_KEY, "1993-01-02");
    	item.put(AGE_KEY, 13);   	
    	item.put(TYPE_KEY, "weight");
    	item.put(SIDE_KEY, "");
    	item.put(VALUE_KEY, 23.5);
    	item.put(UNIT_KEY, "kg");
    	data.put(item);
    	JSONObject json = new JSONObject();
    	json.put(CONTROLLER_NAME, data);
    	PatientData<MeasurementEntry> result = this.mocker.getComponentUnderTest().readJSON(json);
    	Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.isIndexed());
    }
    
    @Test
    public void DuplicateTest() throws ComponentLookupException
    {
    	JSONArray data = new JSONArray();
    	JSONObject item = new JSONObject();
    	item.put(DATE_KEY, "1993-01-02");
    	item.put(AGE_KEY, 13);   	
    	item.put(TYPE_KEY, "ear");
    	item.put(SIDE_KEY, "l");
    	item.put(VALUE_KEY, 23.5);
    	item.put(UNIT_KEY, "cm");
    	data.put(item);
    	item = new JSONObject();
    	item.put(DATE_KEY, "1993-01-02");
    	item.put(AGE_KEY, 13);   	
    	item.put(TYPE_KEY, "ear");
    	item.put(SIDE_KEY, "l");
    	item.put(VALUE_KEY, 23.5);
    	item.put(UNIT_KEY, "cm");
    	data.put(item);
    	JSONObject json = new JSONObject();
    	json.put(CONTROLLER_NAME, data);
    	PatientData<MeasurementEntry> result = this.mocker.getComponentUnderTest().readJSON(json);
    	Assert.assertNotNull(result);
    }
    
    // ----------------------------------------Save Tests----------------------------------------
    
    @Test
    public void saveWithNoDataDoesNothing() throws ComponentLookupException
    {
        this.mocker.getComponentUnderTest().save(this.patient);
        Mockito.verifyZeroInteractions(this.doc);
    }

    @Test
    public void saveWithWrongTypeOfDataDoesNothing() throws ComponentLookupException
    {
        when(this.patient.getData(CONTROLLER_NAME)).thenReturn(new SimpleValuePatientData<Object>("a", "b"));
        this.mocker.getComponentUnderTest().save(this.patient);
        Mockito.verifyZeroInteractions(this.doc);
    }
}
