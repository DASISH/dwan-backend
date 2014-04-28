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

import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.PrincipalExists;
import eu.dasish.annotation.backend.dao.DBDispatcher;
import eu.dasish.annotation.schema.Principal;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author olhsha
 */
public class ResourceResource<T> {

    @Autowired
    protected DBDispatcher dbDispatcher;
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
    protected String admin = "admin";
    protected String anonym = "anonymous";
    protected String defaultAccess = "read";
    protected String[] admissibleAccess = {"read", "write", "owner"};

    public Number getPrincipalID() throws IOException {
        dbDispatcher.setServiceURI(uriInfo.getBaseUri().toString());
        String remotePrincipal = httpServletRequest.getRemoteUser();
        if (remotePrincipal != null) {
            if (!remotePrincipal.equals("anonymous")) {
                try {
                    return dbDispatcher.getPrincipalInternalIDFromRemoteID(remotePrincipal);
                } catch (NotInDataBaseException e) {
                    loggerServer.info(e.toString());
                    loggerServer.info("The record for the user with the Shibboleth id " + remotePrincipal + " will be generated now automatically.");
                    try {
                        try {
                            Principal newPrincipal = Helpers.createPrincipalElement(remotePrincipal, remotePrincipal);
                            return dbDispatcher.addPrincipal(newPrincipal, remotePrincipal);
                        } catch (PrincipalExists e2) {
                            loggerServer.info(e2.toString());
                            httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e2.toString());
                            return null;
                        }
                    } catch (NotInDataBaseException e1) {
                        loggerServer.info(e1.toString());
                        httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
                        return null;
                    }
                }
            } else {
                loggerServer.info("Shibboleth fall-back.  Logged in as 'anonymous' with no rights.");
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, " Shibboleth fall-back.  Logged in as 'anonymous' with no rights.");
                return null;
            }
        } else {
            loggerServer.info("Null principal");
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, " Null principal");
            return null;
        }
    }

    protected void ADMIN_RIGHTS_EXPECTED() throws IOException {
        loggerServer.debug("The request can be performed only by the principal with the admin rights.");
    }

    protected void INVALID_ACCESS_MODE(String accessMode) throws IOException {
        loggerServer.debug(accessMode + " is an invalid access value, which must be either owner, or read, or write.");
    }
}
