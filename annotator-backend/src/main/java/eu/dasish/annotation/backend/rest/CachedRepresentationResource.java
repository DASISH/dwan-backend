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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
@Component
@Path("/cached")
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class CachedRepresentationResource {

    @Autowired
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Context
    private UriInfo uriInfo;

    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    // TODOD both unit tests
    //changed path, /Target/cached part is removed
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{cachedid: " + BackendConstants.regExpIdentifier + "}/metadata")
    @Transactional(readOnly = true)
    public JAXBElement<CachedRepresentationInfo> getCachedRepresentationInfo(@PathParam("cachedid") String externalId) throws SQLException, IOException {

        final Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(httpServletRequest.getRemoteUser());
        if (remoteUserID != null) {
            dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
            final Number cachedID = dbIntegrityService.getCachedRepresentationInternalIdentifier(UUID.fromString(externalId));
            if (cachedID != null) {
                final CachedRepresentationInfo cachedInfo = dbIntegrityService.getCachedRepresentationInfo(cachedID);
                return new ObjectFactory().createCashedRepresentationInfo(cachedInfo);
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The cached representation with the given id is not found in the database");
                return null;
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged in user is not found in the database");
            return null;
        }
    }

    @GET
    @Produces({"image/jpeg", "image/png"})
    @Path("{cachedid: " + BackendConstants.regExpIdentifier + "}/content")
    @Transactional(readOnly = true)
    public BufferedImage getCachedRepresentationContent(@PathParam("cachedid") String externalId) throws SQLException, IOException {
        final Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(httpServletRequest.getRemoteUser());
        if (remoteUserID != null) {
            dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
            final Number cachedID = dbIntegrityService.getCachedRepresentationInternalIdentifier(UUID.fromString(externalId));
            if (cachedID != null) {
                InputStream dbRespond = dbIntegrityService.getCachedRepresentationBlob(cachedID);
                ImageIO.setUseCache(false);
                BufferedImage result = ImageIO.read(dbRespond);
                return result;
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The cached representation  with the given id is not found in the database");
                return null;
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged in user is not found in the database");
            return null;
        }
    }
}
