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
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
@Component
@Path("")
@Transactional(rollbackFor = {Exception.class})
public class ResourceResource {

    @Autowired
    protected DBIntegrityService dbIntegrityService;
    @Context
    protected HttpServletRequest httpServletRequest;
    @Context
    protected HttpServletResponse httpServletResponse;
    @Context
    protected UriInfo uriInfo;
    @Context
    protected Providers providers;
    @Context
    protected ServletContext context;
    protected Logger loggerServer = LoggerFactory.getLogger(HttpServletResponse.class);
    protected VerboseOutput verboseOutput;
    protected String admin = "admin";
    protected String anonym = "anonymous";

    public Number getUserID() throws IOException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        verboseOutput = new VerboseOutput(httpServletResponse, loggerServer);
        String remoteUser = httpServletRequest.getRemoteUser();
        if (remoteUser != null) {
            if (!remoteUser.equals(anonym)) {
                final Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
                if (userID != null) {
                    return userID;
                }
                verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser, dbIntegrityService.getDataBaseAdmin().getDisplayName(), dbIntegrityService.getDataBaseAdmin().getEMail());
                return null;
            }
        }

        verboseOutput.NOT_LOGGED_IN(dbIntegrityService.getDataBaseAdmin().getDisplayName(), dbIntegrityService.getDataBaseAdmin().getEMail());
        return null;

    }

    @GET
    @Produces({"text/html"})
    @Path("")
    @Transactional(readOnly = true)
    public String welcome() throws IOException {
        Number remoteUserID = this.getUserID();
        if (remoteUserID != null) {
            String welcome = "<!DOCTYPE html><body>"
                    + "<h3>Welcome to DASISH Webannotator (DWAN)</h3><br>"
                    +"<a href=\"../\"> to DWAN's test jsp page</a>"
                    + "</body>";
            return welcome;
        }
        return null;
    }
}
