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

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Manages serialisation of java object into xml elements.
 * @author olhsha
 */
@Provider
@Component
public class ResourceJaxbMarshallerProvider implements ContextResolver<Marshaller> {

    @Autowired
    private JaxbMarshallerFactory jaxbMarshallerFactory;

    /*
     * ------------------------
     * Interface Implementation
     * ------------------------
     */
    @Override
    public Marshaller getContext(Class<?> type) {
        try{
           return jaxbMarshallerFactory.createMarshaller(type);
        } catch (JAXBException e) {
            System.out.println("Cannot create a marshaller for type "+type.getCanonicalName());
            return null;
        }
    }
}
