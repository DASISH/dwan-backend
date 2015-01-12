/*
 * Copyright (C) 2013 DASISH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.dasish.annotation.backend.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 *
 * @author olhsha
 */
public class JaxbUnmarshallerFactory {

    private JAXBContext context;
    private Unmarshaller unmarshaller;
    // overwritten by the web.xml's 
    // why?? se the bean
    private static String schemaLocation = null;
    private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
    private static Schema dwanSchema;
    private static StreamSource schemaSource = null;

    public JaxbUnmarshallerFactory() throws Exception {
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) throws PropertyException {
        this.schemaLocation = schemaLocation;
    }

    public Unmarshaller createUnmarshaller(Class<?> type) throws Exception {
        context = JAXBContext.newInstance(type);
        unmarshaller = context.createUnmarshaller();
        if (dwanSchema == null) {
            unmarshaller.setSchema(getDwanSchema());
        } else {
            unmarshaller.setSchema(dwanSchema);
        }
        return unmarshaller;
    }

    public static synchronized Schema getDwanSchema() throws Exception{

        SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        URL schemaURL = new URL(schemaLocation);
        InputStreamReader is = new InputStreamReader(schemaURL.openStream());
        schemaSource = new StreamSource(is);        
        dwanSchema = schemaFactory.newSchema(schemaSource);
        return dwanSchema;
    }
}
