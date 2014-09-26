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
import eu.dasish.annotation.backend.ForbiddenException;
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.ResourceAction;
import eu.dasish.annotation.backend.dao.ILambda;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.Target;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.http.HTTPException;
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
public class TargetResource extends ResourceResource {

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
    public JAXBElement<Target> getTarget(@PathParam("targetid") String externalIdentifier) throws IOException {
        Map params = new HashMap();
        try {
            Target result = (Target) (new RequestWrappers(this)).wrapRequestResource(params, new GetTarget(), Resource.TARGET, ResourceAction.READ, externalIdentifier);
            if (result != null) {
                return new ObjectFactory().createTarget(result);
            } else {
                return new ObjectFactory().createTarget(new Target());
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return new ObjectFactory().createTarget(new Target());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return new ObjectFactory().createTarget(new Target());
        }
    }

    private class GetTarget implements ILambda<Map, Target> {

        @Override
        public Target apply(Map params) throws NotInDataBaseException {
            Number targetID = (Number) params.get("internalID");
            return dbDispatcher.getTarget(targetID);
        }
    }

    /////////////////////////////////////////////////
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{targetid: " + BackendConstants.regExpIdentifier + "}/versions")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getSiblingTargets(@PathParam("targetid") String externalIdentifier) throws HTTPException, IOException {
        Map params = new HashMap();
        try {
            ReferenceList result = (ReferenceList) (new RequestWrappers(this)).wrapRequestResource(params, new GetSiblingTargets(), Resource.TARGET, ResourceAction.READ, externalIdentifier);
            if (result != null) {
                return new ObjectFactory().createReferenceList(result);
            } else {
                return new ObjectFactory().createReferenceList(new ReferenceList());
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return new ObjectFactory().createReferenceList(new ReferenceList());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return new ObjectFactory().createReferenceList(new ReferenceList());
        }
    }

    private class GetSiblingTargets implements ILambda<Map, ReferenceList> {

        @Override
        public ReferenceList apply(Map params) throws NotInDataBaseException {
            Number targetID = (Number) params.get("internalID");
            return dbDispatcher.getTargetsForTheSameLinkAs(targetID);
        }
    }

    ////////////////////////////////////////////////
    @POST
    @Consumes("multipart/mixed")
    @Produces(MediaType.APPLICATION_XML)
    @Path("{targetid: " + BackendConstants.regExpIdentifier + "}/fragment/{fragmentDescriptor}/cached")
    public JAXBElement<CachedRepresentationInfo> postCached(@PathParam("targetid") String targetIdentifier,
            @PathParam("fragmentDescriptor") String fragmentDescriptor,
            MultiPart multiPart) throws IOException {

        Map params = new HashMap();
        params.put("cachedInfo", multiPart.getBodyParts().get(0).getEntityAs(CachedRepresentationInfo.class));
        BodyPartEntity bpe = (BodyPartEntity) multiPart.getBodyParts().get(1).getEntity();
        params.put("cachedBlob", bpe.getInputStream());
        params.put("fragmentDescriptor", fragmentDescriptor);
        try {
            CachedRepresentationInfo result = (CachedRepresentationInfo) (new RequestWrappers(this)).wrapRequestResource(params, new PostCached(), Resource.TARGET, ResourceAction.WRITE_W_METAINFO, targetIdentifier);
            if (result != null) {
                return new ObjectFactory().createCashedRepresentationInfo(result);
            } else {
                return new ObjectFactory().createCashedRepresentationInfo(new CachedRepresentationInfo());
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return new ObjectFactory().createCashedRepresentationInfo(new CachedRepresentationInfo());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return new ObjectFactory().createCashedRepresentationInfo(new CachedRepresentationInfo());
        }
    }

    private class PostCached implements ILambda<Map, CachedRepresentationInfo> {

        @Override
        public CachedRepresentationInfo apply(Map params) throws NotInDataBaseException {
            Number targetID = (Number) params.get("internalID");
            String fragmentDescriptor = (String) params.get("fragmentDescriptor");
            CachedRepresentationInfo metadata = (CachedRepresentationInfo) params.get("cachedInfo");
            InputStream cachedSource = (InputStream) params.get("cachedBlob");
            try {
                final Number[] respondDB = dbDispatcher.addCachedForTarget(targetID, fragmentDescriptor, metadata, cachedSource);
                return dbDispatcher.getCachedRepresentationInfo(respondDB[1]);
            } catch (IOException e) {
                loggerServer.info(e.toString());
                return null;
            }
        }
    }

    ////////////////////////////////////////////////
    @PUT
    @Produces(MediaType.APPLICATION_XML)
    @Path("{targetid: " + BackendConstants.regExpIdentifier + "}/{cachedid: " + BackendConstants.regExpIdentifier + "}/fragment/{fragmentDescriptor}")
    public String updateTargetCachedFragment(@PathParam("targetid") String targetIdentifier, @PathParam("cachedid") String cachedIdentifier,
            @PathParam("fragmentDescriptor") String fragmentDescriptor) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return "You are not logged in. Nothing is updated. ";
        }
        try {
            final Number targetID = dbDispatcher.getResourceInternalIdentifier(UUID.fromString(targetIdentifier), Resource.TARGET);
            try {
                final Number cachedID = dbDispatcher.getResourceInternalIdentifier(UUID.fromString(cachedIdentifier), Resource.CACHED_REPRESENTATION);
                final int updated = dbDispatcher.updateTargetCachedFragment(targetID, cachedID, fragmentDescriptor);
                return updated + "rows is/are updated.";
            } catch (NotInDataBaseException e1) {
                loggerServer.debug(e1.toString());
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.toString());
                return "Nothing is updated: " + e1;
            }
        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return "Nothing is updated: " + e2;
        }
    }

    @DELETE
    @Path("{targetid: " + BackendConstants.regExpIdentifier + "}/cached/{cachedid: " + BackendConstants.regExpIdentifier + "}")
    public String deleteCachedForTarget(@PathParam("targetid") String targetExternalIdentifier,
            @PathParam("cachedid") String cachedExternalIdentifier) throws HTTPException, IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return "Nothing is deleted";
        }
        try {
            final Number targetID = dbDispatcher.getResourceInternalIdentifier(UUID.fromString(targetExternalIdentifier), Resource.TARGET);
            try {
                final Number cachedID = dbDispatcher.getResourceInternalIdentifier(UUID.fromString(cachedExternalIdentifier), Resource.CACHED_REPRESENTATION);
                int[] result = dbDispatcher.deleteCachedRepresentationOfTarget(targetID, cachedID);
                return result[0] + " pair(s) target-cached deleted.";
            } catch (NotInDataBaseException e) {
                loggerServer.debug(e.toString());
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.toString());
                return "Nothing is deleted.";
            }

        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return "Nothing is deleted.";
        }
    }
}
