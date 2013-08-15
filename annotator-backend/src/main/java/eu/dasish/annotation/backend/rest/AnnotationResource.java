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
import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.PermissionsDao;
import eu.dasish.annotation.backend.dao.SourceDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Permission;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
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
    private AnnotationDao annotationDao;
    @Autowired
    private SourceDao sourceDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private PermissionsDao permissionsDao;
    @Autowired
    private NotebookDao notebookDao;
    //for Peter, see also http://stackoverflow.com/questions/6140697/jersey-the-context-annotation-for-injection-how-does-it-work
    @Context
    private HttpServletRequest httpServletRequest;

    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public AnnotationResource() {
    }

    /*public AnnotationResource(@Context HttpServletRequest request){
     this.httpServletRequest =  request;
     }*/
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<Annotation> getAnnotation(@PathParam("annotationid") String annotationIdentifier) throws SQLException {
        final Annotation annotation = annotationDao.getAnnotation(annotationDao.getInternalID(new AnnotationIdentifier(annotationIdentifier)));
        return new ObjectFactory().createAnnotation(annotation);
    }

    @DELETE
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    /*
     Delete _aid_. The related sources that are not related to other annotations must be deleted as well (TODO)
     */
    public String deleteAnnotation(@PathParam("annotationid") String annotationIdentifier) throws SQLException {
        Number annotationID = annotationDao.getInternalID(new AnnotationIdentifier(annotationIdentifier));
        int[] resultDelete = annotationDao.deleteAnnotation(annotationID);
        String result = Integer.toString(resultDelete[4]);
        return result;
    }

    // TODO: should be returning the envelope!!!
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<Annotation> createAnnotation(Annotation annotation) throws SQLException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number userID;
        if (remoteUser == null) {
            // happens in client testing
            // TODO sould be adjusted when the user handling mechanism is settled
            userID = null;
        } else {
            userID = userDao.getInternalID(new UserIdentifier(remoteUser));
        }

        
        //Add annotation
        Number annotationID = annotationDao.addAnnotation(annotation, userID);

        //Add the sources to the DB 
        List<NewOrExistingSourceInfo> sources = annotation.getTargetSources().getTarget();
        Map<String, String> sourceIdPairs= sourceDao.addTargetSourcesToAnnotation(annotationID, sources);
        
        if (sourceIdPairs.containsValue(null)){
           // for one of the soirces there was no version and cached representation
            // envelope
           return (new ObjectFactory().createAnnotation(null));
        }
        String body = Helpers.serializeBody(annotation.getBody());
        String newBody = Helpers.replace(body, sourceIdPairs);
        int affectedAnnotRows = annotationDao.updateBody(annotationID, newBody);
       
        // Add the permission (annotation_id, owner);
        int affectedPermissions = permissionsDao.addAnnotationPrincipalPermission(annotationDao.getExternalID(annotationID), new UserIdentifier(remoteUser), Permission.OWNER);
       
        Annotation newAnnotation = annotationDao.getAnnotation(annotationID);
        return (new ObjectFactory().createAnnotation(newAnnotation));
    }
    
    
}
