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

import eu.dasish.annotation.backend.AnnotationRooted;
import eu.dasish.annotation.backend.BackendConstants;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.ObjectFactory;
import java.sql.SQLException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author olhsha
 */
@Component
@Path("/annotations")
public class AnnotationResource {
    
    @Autowired
    private AnnotationDao annotationDao;
   
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: "+BackendConstants.regExpIdentifier+"}")
    public JAXBElement<Annotation> getAnnotation(@PathParam("annotationid") String annotationIdentifier) throws SQLException{
        final Annotation annotation = annotationDao.getAnnotation(annotationDao.getAnnotationID(new AnnotationIdentifier(annotationIdentifier)));
        return new ObjectFactory().createAnnotation(annotation);
    }
    
    @DELETE
    @Path("{annotationid: "+BackendConstants.regExpIdentifier+"}")
    /*
     Delete _aid_. The related sources that are not related to other annotations must be deleted as well (TODO)
     */
    public String deleteAnnotation(@PathParam("annotationid") String annotationIdentifier) throws SQLException{
        return Integer.toString(annotationDao.deleteAnnotation(annotationDao.getAnnotationID(new AnnotationIdentifier(annotationIdentifier))));
    }
    
    // TODO: should be returning the envelope!!!
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<Annotation> createAnnotation(AnnotationRooted annotation) {
        AnnotationIdentifier newAnnotationIdentifier = annotationDao.addAnnotation(annotation);
        //return newAnnotationIdentifier.toString();
        if (newAnnotationIdentifier == null) {
            return null;
        } else {
            return (new ObjectFactory().createAnnotation(annotation));
        }
    }
    
  
}
