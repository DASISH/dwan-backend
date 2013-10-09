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

import eu.dasish.annotation.schema.Annotation;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 *
 * @author olhsha
 */
public class JaxbMarshallerFactory {
    
    private JAXBContext context;
    private Marshaller marshaller;
    
    // overwritten by the bean's property 
    private String schemaLocation = "http://www.dasish.eu/ns/addit file:/Users/olhsha/repositories/DASISH/t5.6/schema/trunk/annotator-schema/src/main/resources/DASISH-schema.xsd";

    
    public JaxbMarshallerFactory() throws Exception {
    context = JAXBContext.newInstance(Annotation.class);

    // Setup the marshaller
    marshaller = context.createMarshaller(); 
    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
    }
    
    public String getSchemaLocation(){
        return schemaLocation;
    }
    
    public void setSchemaLocation(String schemaLocation){
        this.schemaLocation = schemaLocation;
    }
    
    public Marshaller getMarshaller(){
        return marshaller;
    }
}
