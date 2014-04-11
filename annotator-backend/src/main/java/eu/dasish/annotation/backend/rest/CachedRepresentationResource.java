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
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.ObjectFactory;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
@Component
@Path("/cached")
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class CachedRepresentationResource extends ResourceResource {

    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    // TODOD both unit tests
    //changed path, /Target/cached part is removed
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{cachedid: " + BackendConstants.regExpIdentifier + "}/metadata")
    @Transactional(readOnly = true)
    public JAXBElement<CachedRepresentationInfo> getCachedRepresentationInfo(@PathParam("cachedid") String externalId) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return new ObjectFactory().createCashedRepresentationInfo(new CachedRepresentationInfo());
        }
        try {
            final Number cachedID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalId), Resource.CACHED_REPRESENTATION);

            final CachedRepresentationInfo cachedInfo = dbIntegrityService.getCachedRepresentationInfo(cachedID);
            return new ObjectFactory().createCashedRepresentationInfo(cachedInfo);

        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return new ObjectFactory().createCashedRepresentationInfo(new CachedRepresentationInfo());
        }
    }

    @GET
    @Produces({"image/jpeg", "image/png"})
    @Path("{cachedid: " + BackendConstants.regExpIdentifier + "}/content")
    @Transactional(readOnly = true)
    public BufferedImage getCachedRepresentationContent(@PathParam("cachedid") String externalId) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return null;
        }
        try {
            final Number cachedID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalId), Resource.CACHED_REPRESENTATION);
            InputStream dbRespond = dbIntegrityService.getCachedRepresentationBlob(cachedID);
            if (dbRespond != null) {
                ImageIO.setUseCache(false);
                try {
                    BufferedImage result = ImageIO.read(dbRespond);
                    return result;
                } catch (IOException e1) {
                    loggerServer.debug(e1.toString());
                    httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
                    return null;
                }
            } else {
                loggerServer.info(" The cached representation with the id " + externalId + " has null blob.");
                return null;
            }

        } catch (NotInDataBaseException e) {
            loggerServer.debug(e.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.toString());
            return null;
        }
    }

    @GET
    @Produces({"text/plain", "text/html", "text/xml", "application/zip"})
    @Path("{cachedid: " + BackendConstants.regExpIdentifier + "}/stream")
    @Transactional(readOnly = true)
    public InputStream getCachedRepresentationContentStream(@PathParam("cachedid") String externalId) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return null;
        }
        try {
            final Number cachedID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalId), Resource.CACHED_REPRESENTATION);
            InputStream dbRespond = dbIntegrityService.getCachedRepresentationBlob(cachedID);
            if (dbRespond != null) {
                return dbRespond;
            } else {
                loggerServer.info("The cached representation with the id " + externalId + " has null blob.");
                return null;
            }

        } catch (NotInDataBaseException e) {
            loggerServer.debug(e.toString());;
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.toString());
            return null;
        }
    }

    @PUT
    @Consumes("multipart/mixed")
    @Produces(MediaType.APPLICATION_XML)
    @Path("{cachedid: " + BackendConstants.regExpIdentifier + "}/stream")
    public String updateCachedBlob(@PathParam("cachedid") String cachedIdentifier,
            MultiPart multiPart) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return "Nothing is updated. You are no tlogged in";
        }
        try {
            final Number cachedID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(cachedIdentifier), Resource.CACHED_REPRESENTATION);
            BodyPartEntity bpe = (BodyPartEntity) multiPart.getBodyParts().get(0).getEntity();
            InputStream cachedSource = bpe.getInputStream();
            try {
                final int result = dbIntegrityService.updateCachedBlob(cachedID, cachedSource);
                return result + "rows are updated";
            } catch (IOException e) {
                loggerServer.debug(e.toString());
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                return "Nothing is updated. ";
            }
        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return "Nothing is updated. ";
        }
    }

    @PUT
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("metadata")
    public String updateCachedMetadata(CachedRepresentationInfo cachedInfo) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return "Nothing is updated. You are no tlogged in";
        }
        try {
            final int result = dbIntegrityService.updateCachedMetada(cachedInfo);
            return result + "rows are updated";
        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return "Nothing is updated. ";
        }
    }
}
