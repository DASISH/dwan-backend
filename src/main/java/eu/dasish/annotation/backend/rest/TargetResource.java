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

import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import eu.dasish.annotation.backend.BackendConstants;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.Target;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class TargetResource {

    @Autowired
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Context
    private UriInfo uriInfo;
    public static final Logger loggerServer = LoggerFactory.getLogger(HttpServletResponse.class);
    private final VerboseOutput verboseOutput = new VerboseOutput(httpServletResponse, loggerServer);
    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public TargetResource() {
    }

    // TODOD both unit tests
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{targetid: " + BackendConstants.regExpIdentifier + "}")
    @Transactional(readOnly = true)
    public JAXBElement<Target> getTarget(@PathParam("targetid") String externalIdentifier) throws SQLException, IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
            try {
                final Number targetID = dbIntegrityService.getTargetInternalIdentifier(UUID.fromString(externalIdentifier));
                if (targetID != null) {
                    final Target target = dbIntegrityService.getTarget(targetID);
                    return new ObjectFactory().createTarget(target);
                } else {
                   verboseOutput.TARGET_NOT_FOUND(externalIdentifier);
                }
            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(externalIdentifier);
            }
        } else {
           verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        return new ObjectFactory().createTarget(new Target());
    }

    // TODOD both unit tests
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{targetid: " + BackendConstants.regExpIdentifier + "}/versions")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getSiblingTargets(@PathParam("targetid") String externalIdentifier) throws SQLException, IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
            try {
                final Number targetID = dbIntegrityService.getTargetInternalIdentifier(UUID.fromString(externalIdentifier));
                if (targetID != null) {
                    final ReferenceList siblings = dbIntegrityService.getTargetsForTheSameLinkAs(targetID);
                    return new ObjectFactory().createReferenceList(siblings);
                } else {
                    verboseOutput.TARGET_NOT_FOUND(externalIdentifier);
                }
            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(externalIdentifier);
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser); 
        }
        
        return new ObjectFactory().createReferenceList(new ReferenceList());
    }


    @POST
    @Consumes("multipart/mixed")
    @Produces(MediaType.APPLICATION_XML)
    @Path("{targetid: " + BackendConstants.regExpIdentifier + "}/fragment/{fragmentDescriptor}/cached")
    public JAXBElement<CachedRepresentationInfo> postCached(@PathParam("targetid") String targetIdentifier,
            @PathParam("fragmentDescriptor") String fragmentDescriptor,
            MultiPart multiPart) throws SQLException, IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
            try {
                final Number targetID = dbIntegrityService.getTargetInternalIdentifier(UUID.fromString(targetIdentifier));
                if (targetID != null) {
                    CachedRepresentationInfo metadata = multiPart.getBodyParts().get(0).getEntityAs(CachedRepresentationInfo.class);
                    BodyPartEntity bpe = (BodyPartEntity) multiPart.getBodyParts().get(1).getEntity();
                    InputStream cachedSource = bpe.getInputStream();
                    final Number[] respondDB = dbIntegrityService.addCachedForTarget(targetID, fragmentDescriptor, metadata, cachedSource);
                    final CachedRepresentationInfo cachedInfo = dbIntegrityService.getCachedRepresentationInfo(respondDB[1]);
                    return new ObjectFactory().createCashedRepresentationInfo(cachedInfo);
                } else {
                    verboseOutput.TARGET_NOT_FOUND(targetIdentifier);
                }
            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(targetIdentifier);
            }
        } else {
           verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser); 
        }
       return new ObjectFactory().createCashedRepresentationInfo(new CachedRepresentationInfo());
    }

    @DELETE
    @Path("{targetid: " + BackendConstants.regExpIdentifier + "}/cached/{cachedid: " + BackendConstants.regExpIdentifier + "}")
    public String deleteCachedForTarget(@PathParam("targetid") String targetExternalIdentifier,
            @PathParam("cachedid") String cachedExternalIdentifier) throws SQLException, IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
            try {
                final Number targetID = dbIntegrityService.getTargetInternalIdentifier(UUID.fromString(targetExternalIdentifier));
                if (targetID != null) {
                    final Number cachedID = dbIntegrityService.getCachedRepresentationInternalIdentifier(UUID.fromString(cachedExternalIdentifier));
                    if (cachedID != null) {
                        int[] resultDelete = dbIntegrityService.deleteCachedRepresentationOfTarget(targetID, cachedID);
                        String result = Integer.toString(resultDelete[0]);
                        return result + " pair(s) target-cached deleted.";
                    } else {
                        verboseOutput.CACHED_REPRESENTATION_NOT_FOUND(cachedExternalIdentifier);
                    }
                } else {
                    verboseOutput.TARGET_NOT_FOUND(targetExternalIdentifier);
                }
            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(targetExternalIdentifier);
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        return " ";
    }
}
