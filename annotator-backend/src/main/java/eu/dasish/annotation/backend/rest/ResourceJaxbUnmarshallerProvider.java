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
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Manages deserialisation of xml elements into java objects according to the dasish schema;
 * also validates according to this schema; the schema and other settings are done in {@link JaxbUnmarshallerFactory}.
 * @author olhsha
 */
@Provider
@Component
public class ResourceJaxbUnmarshallerProvider implements ContextResolver<Unmarshaller> {

    @Autowired
    private JaxbUnmarshallerFactory jaxbUnmarshallerFactory;
    private Logger logger = LoggerFactory.getLogger(ResourceJaxbUnmarshallerProvider.class);

    /*
     * ------------------------
     * Interface Implementation
     * ------------------------
     */
    @Override
    public Unmarshaller getContext(Class<?> type) {
        try {
            return jaxbUnmarshallerFactory.createUnmarshaller(type);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
        
    }
    
    
}

