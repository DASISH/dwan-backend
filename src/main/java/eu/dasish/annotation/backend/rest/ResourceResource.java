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

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.NotLoggedInException;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Principal;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import javax.xml.ws.http.HTTPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
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
    protected String defaultAccess = "read";
    protected String[] admissibleAccess = {"read", "write", "owner"};

    public Number getPrincipalID() throws IOException, HTTPException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        verboseOutput = new VerboseOutput(loggerServer);
        String remotePrincipal = httpServletRequest.getRemoteUser();
        if (remotePrincipal != null) {
            if (!remotePrincipal.equals(anonym)) {
                try {
                    return dbIntegrityService.getPrincipalInternalIDFromRemoteID(remotePrincipal);
                } catch (NotInDataBaseException e) {
                    loggerServer.info(e.toString());;
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.toString());
                    throw new IOException(e);
                }
            } else {
                loggerServer.info("Shibboleth fall-back.  Logged in as 'anonymous' with no rights.");
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, " Shibboleth fall-back.  Logged in as 'anonymous' with no rights.");
                throw new IOException("Shibboleth fall-back.  Logged in as 'anonymous' with no rights.");
            }
        } else {
            loggerServer.info("Not logged in.");
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, " Not logged in.");
            throw new IOException("Not logged in.");
        }
    }
}
