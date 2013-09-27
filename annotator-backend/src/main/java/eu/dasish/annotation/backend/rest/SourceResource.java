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
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
/**
 *
 * @author olhsha
 */
@Component
@Path("/sources")
public class SourceResource {

    @Autowired
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;
   
    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public SourceResource() {
    }
    
    // TODOD both unit tests
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{sourceid: " + BackendConstants.regExpIdentifier + "}/versions")
    public JAXBElement<ReferenceList> getSiblingSources(@PathParam("sourceid") String ExternalIdentifier) throws SQLException {
        final Number sourceID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(ExternalIdentifier));
        dbIntegrityService.setServiceURI(httpServletRequest.getServletPath());
        final ReferenceList siblings = dbIntegrityService.getSiblingSources(sourceID);
        return new ObjectFactory().createReferenceList(siblings);
    }
    
     // TODO both unit tests
    //changed path, /sourcepart is removed
    //how to overwork the input stream to make it downloadable
    // using mime type as well
    @DELETE
    @Produces(MediaType.TEXT_XML)
    @Path("{sourceid: "+BackendConstants.regExpIdentifier +"}/cached/{cachedid: "+ BackendConstants.regExpIdentifier+"}")
    public int deleteCached(@PathParam("sourceid") String sourceIdentifier, @PathParam("cachedid") String cachedIdentifier) throws SQLException {
        final Number sourceID = dbIntegrityService.getCachedRepresentationInternalIdentifier(UUID.fromString(sourceIdentifier));
        final Number cachedID = dbIntegrityService.getCachedRepresentationInternalIdentifier(UUID.fromString(cachedIdentifier));
        int[] result = dbIntegrityService.deleteCachedRepresentationOfSource(sourceID, cachedID);
        return result[1];
    }
   
}
