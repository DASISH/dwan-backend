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

import eu.dasish.annotation.backend.BackendConstants;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.ObjectFactory;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
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
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;

    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public AnnotationResource() {
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<Annotation> getAnnotation(@PathParam("annotationid") String ExternalIdentifier) throws SQLException {
        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(ExternalIdentifier));
        dbIntegrityService.setServiceURI(httpServletRequest.getServletPath());
        final Annotation annotation = dbIntegrityService.getAnnotation(annotationID);
        return new ObjectFactory().createAnnotation(annotation);
    }

    ///////////////////////////////////////////////////////
    // TODO: return envelope: deleted or not deleted
    @DELETE
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public String deleteAnnotation(@PathParam("annotationid") String externalIdentifier) throws SQLException {
        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(externalIdentifier));
        int[] resultDelete = dbIntegrityService.deleteAnnotation(annotationID);
        String result = Integer.toString(resultDelete[0]);
        return result;
    }

    ///////////////////////////////////////////////////////
    // TODO: should be returning the envelope!!!
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<Annotation> createAnnotation(Annotation annotation) throws SQLException {
        
        String remoteUser = httpServletRequest.getRemoteUser();
        UUID userExternalID = (remoteUser != null) ? UUID.fromString(remoteUser) : null;
        Number userID = dbIntegrityService.getUserInternalIdentifier(userExternalID);
        
        dbIntegrityService.setServiceURI(httpServletRequest.getServletPath());
        
        Number newAnnotationID =  dbIntegrityService.addUsersAnnotation(annotation, userID);
        Annotation newAnnotation = dbIntegrityService.getAnnotation(newAnnotationID); 
        return (new ObjectFactory().createAnnotation(newAnnotation));
    }
}
