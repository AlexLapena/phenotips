/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.phenotips.oo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class OmimSourceParser
{
    private final static String RECORD_MARKER = "*RECORD*";

    private final static String END_MARKER = "*THEEND*";

    private final static String FIELD_MARKER = "*FIELD* ";

    private int counter = 0;

    private RecordData crtTerm = new RecordData();

    private Map<String, RecordData> data = new LinkedHashMap<String, RecordData>();

    private Set<String> fieldSelection;

    public OmimSourceParser(String path, Set<String> fieldSelection)
    {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new URL(path).openConnection().getInputStream()));
        } catch (MalformedURLException uex) {
            try {
                in = new BufferedReader(new FileReader(path));
            } catch (FileNotFoundException fex) {
                // TODO Auto-generated catch block
                fex.printStackTrace();
                return;
            }
        } catch (IOException iex) {
            // TODO Auto-generated catch block
            iex.printStackTrace();
            return;
        }
        try {
            transform(in, fieldSelection);
            if (in != null) {
                in.close();
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Map<String, RecordData> transform(BufferedReader in, Set<String> fieldSelection) throws IOException
    {
        this.fieldSelection = fieldSelection;

        String line;
        StringBuilder fieldValue = new StringBuilder();
        String fieldName = "";
        this.counter = 0;
        while ((line = in.readLine()) != null) {
            if (line.trim().equalsIgnoreCase(RECORD_MARKER) || line.trim().equalsIgnoreCase(END_MARKER)) {
                if (this.counter > 0) {
                    storeCrtTerm();
                }
                ++this.counter;
                continue;
            }
            if (line.startsWith(FIELD_MARKER)) {
                if (!StringUtils.isBlank(fieldValue) && !StringUtils.isBlank(fieldName)) {
                    loadField(fieldName, fieldValue.toString().trim());
                }
                fieldValue.delete(0, fieldValue.length());
                fieldName = line.substring(FIELD_MARKER.length());
                if (fieldSelection.size() > 0 && !fieldSelection.contains(fieldName)) {
                    fieldName = "";
                }
            } else {
                if (!StringUtils.isBlank(line) && !StringUtils.isBlank(fieldName)) {
                    fieldValue.append(line.trim()).append(" ");
                }
            }
        }

        return this.data;
    }

    private void storeCrtTerm()
    {
        if (this.crtTerm.getId() != null) {
            this.data.put(this.crtTerm.getId(), this.crtTerm);
        }
        this.crtTerm = new RecordData();
    }

    private boolean isFieldSelected(String name)
    {
        return this.fieldSelection.isEmpty() || this.fieldSelection.contains(name);
    }

    private void loadField(String name, String value)
    {
        if (!(isFieldSelected(name))) {
            return;
        }
        // System.out.println("Adding " + name + " " + value.replaceAll("\"([^\"]+)\".*", "$1"));
        this.crtTerm.addTo(name, value.replaceAll("\"([^\"]+)\".*", "$1"));
    }

    public Map<String, RecordData> getData()
    {
        return this.data;
    }
}
