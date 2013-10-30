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
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.Target;
import java.io.ByteArrayInputStream;
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
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author olhsha
 */
/**
 *
 * @author olhsha
 */
@Component
@Path("/targets")
public class TargetResource {

    @Autowired
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private UriInfo uriInfo;
   
    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public TargetResource() {
    }
    
    // TODOD both unit tests
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{targetid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<Target> getTarget(@PathParam("targetid") String ExternalIdentifier) throws SQLException {
         dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
         final Number TargetID = dbIntegrityService.getTargetInternalIdentifier(UUID.fromString(ExternalIdentifier));
        final Target Target = dbIntegrityService.getTarget(TargetID);
        return new ObjectFactory().createTarget(Target);
    }
    
    // TODOD both unit tests
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{targetid: " + BackendConstants.regExpIdentifier + "}/versions")
    public JAXBElement<ReferenceList> getSiblingTargets(@PathParam("targetid") String ExternalIdentifier) throws SQLException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        final Number targetID = dbIntegrityService.getTargetInternalIdentifier(UUID.fromString(ExternalIdentifier));
        final ReferenceList siblings = dbIntegrityService.getTargetsForTheSameLinkAs(targetID);
        return new ObjectFactory().createReferenceList(siblings);
    }
    
     // TODO both unit tests
    //changed path, /Targetpart is removed
    //how to overwork the input stream to make it downloadable
    // using mime type as well
    @DELETE
    @Produces(MediaType.TEXT_XML)
    @Path("{targetid: "+BackendConstants.regExpIdentifier +"}/cached/{cachedid: "+ BackendConstants.regExpIdentifier+"}")
    public int deleteCached(@PathParam("targetid") String TargetIdentifier, @PathParam("cachedid") String cachedIdentifier) throws SQLException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        final Number TargetID = dbIntegrityService.getCachedRepresentationInternalIdentifier(UUID.fromString(TargetIdentifier));
        final Number cachedID = dbIntegrityService.getCachedRepresentationInternalIdentifier(UUID.fromString(cachedIdentifier));
        int[] result = dbIntegrityService.deleteCachedRepresentationOfTarget(TargetID, cachedID);
        return result[1];
    }
    
    @POST
    //@Consumes("multipart/mixed")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{targetid: "+BackendConstants.regExpIdentifier +"}/cached")
    public JAXBElement<CachedRepresentationInfo> postCached(@PathParam("targetid") String targetIdentifier, CachedRepresentationInfo metadata) throws SQLException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        final Number targetID = dbIntegrityService.getTargetInternalIdentifier(UUID.fromString(targetIdentifier));
        final Number[] respondDB = dbIntegrityService.addCachedForTarget(targetID, metadata, new ByteArrayInputStream("aaa".getBytes()));
        final CachedRepresentationInfo cachedInfo = dbIntegrityService.getCachedRepresentationInfo(respondDB[1]);
        return new ObjectFactory().createCashedRepresentationInfo(cachedInfo);
    }
   
}
