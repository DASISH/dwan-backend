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
import eu.dasish.annotation.backend.PrincipalExists;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
import eu.dasish.annotation.backend.dao.ILambda;
import eu.dasish.annotation.backend.dao.ILambdaPrincipal;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Principal;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import org.slf4j.Logger;

/**
 *
 * @author olhsha
 */
public class RequestWrappers<T> {

    Logger loggerServer;
    HttpServletResponse httpServletResponse;
    ResourceResource resourceResource;
    DBIntegrityService dbIntegrityService;

    public RequestWrappers(Logger loggerServer, HttpServletResponse httpServletResponse, ResourceResource resourceResource, DBIntegrityService dbIntegrityService) {
        this.loggerServer = loggerServer;
        this.httpServletResponse = httpServletResponse;
        this.resourceResource = resourceResource;
        this.dbIntegrityService = dbIntegrityService;
    }
    
    public String FORBIDDEN_RESOURCE_ACTION(String identifier, String resource, String action) {
        return " The logged-in principal cannot " + action + " the " + resource + " with the identifier " + identifier;
    }

    public T wrapRequestResource(Map params, ILambda<Map, T> dbRequestor) throws IOException {
        Number remotePrincipalID = resourceResource.getPrincipalID();
        if (remotePrincipalID == null) {
            return null;
        }
        try {
            return dbRequestor.apply(params);
        } catch (NotInDataBaseException e1) {
            loggerServer.debug(e1.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
            return null;
        }
    }

    
    public T wrapRequestResource(Map params, ILambda<Map, T> dbRequestor, Resource resource, Access access,  String externalId, String action) throws IOException {
        Number principalID = resourceResource.getPrincipalID();
        if (principalID == null) {
            return null;
        }
        try {
            final Number resourceID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalId), resource);
            if (dbIntegrityService.canDo(Access.READ, principalID, resourceID)) {
                return dbRequestor.apply(params);
            } else {
                this.FORBIDDEN_RESOURCE_ACTION(externalId, resource.name(), action);
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return null;
        }
    }

    public JAXBElement<Principal> wrapAddPrincipalRequest(Map params, ILambdaPrincipal<Map, Principal> dbRequestor) throws IOException {
        
        try {
            try {
                return new ObjectFactory().createPrincipal(dbRequestor.apply(params));
            } catch (NotInDataBaseException e1) {
                loggerServer.debug(e1.toString());
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
                return new ObjectFactory().createPrincipal(new Principal());
            }
        } catch (PrincipalExists e) {
            loggerServer.debug(e.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            return new ObjectFactory().createPrincipal(new Principal());
        }

    }
}
