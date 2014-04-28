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
import eu.dasish.annotation.backend.ResourceAction;
import eu.dasish.annotation.backend.dao.ILambda;
import eu.dasish.annotation.backend.dao.ILambdaPrincipal;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Principal;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;

/**
 *
 * @author olhsha
 */
public class RequestWrappers<T> {

    
    private ResourceResource resourceResource;
    private String _internalID = "internalID";
    private String _principalID = "principalID";
    private String _externalId = "externalID";
    private String _resourceType = "resourceType";

    public RequestWrappers(ResourceResource resourceResource) {
        this.resourceResource = resourceResource;
    }

    public String FORBIDDEN_RESOURCE_ACTION(String identifier, String resource, String action) {
        return " The logged-in principal cannot " + action + " the " + resource + " with the identifier " + identifier;
    }

    public T wrapRequestResource(Map params, ILambda<Map, T> dbRequestor) throws IOException {
        Number remotePrincipalID = resourceResource.getPrincipalID();
        if (remotePrincipalID == null) {
            return null;
        }
        params.put(_principalID, remotePrincipalID);
        try {
            return dbRequestor.apply(params);
        } catch (NotInDataBaseException e1) {
            resourceResource.loggerServer.debug(e1.toString());
            resourceResource.httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
            return null;
        }
    }

    public T wrapRequestResource(Map params, ILambda<Map, T> dbRequestor, Resource resource, ResourceAction action, String externalId, boolean isUri) throws IOException {
        Number principalID = resourceResource.getPrincipalID();
        if (principalID == null) {
            return null;
        }
        params.put(_principalID, principalID);
        try {
            final Number resourceID;
            if (isUri) {
                resourceID = resourceResource.dbIntegrityService.getResourceInternalIdentifierFromURI(externalId, resource);
                params.put(_externalId,  resourceResource.dbIntegrityService.getResourceExternalIdentifier(resourceID, resource).toString());
            } else {
                resourceID = resourceResource.dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalId), resource);
                params.put(_externalId, externalId);
            }
            if (resourceResource.dbIntegrityService.canDo(action, principalID, resourceID, resource)) {
                params.put(_internalID, resourceID);
                params.put(_resourceType, resource);
                return dbRequestor.apply(params);
            } else {
                this.FORBIDDEN_RESOURCE_ACTION(externalId, resource.name(), action.name());
                resourceResource.httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
        } catch (NotInDataBaseException e2) {
            resourceResource.loggerServer.debug(e2.toString());
            resourceResource.httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return null;
        }
    }

    public JAXBElement<Principal> wrapAddPrincipalRequest(Map params, ILambdaPrincipal<Map, Principal> dbRequestor) throws IOException {

        try {
            try {
                return new ObjectFactory().createPrincipal(dbRequestor.apply(params));
            } catch (NotInDataBaseException e1) {
                resourceResource.loggerServer.debug(e1.toString());
                resourceResource.httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
                return new ObjectFactory().createPrincipal(new Principal());
            }
        } catch (PrincipalExists e) {
            resourceResource.loggerServer.debug(e.toString());
            resourceResource.httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            return new ObjectFactory().createPrincipal(new Principal());
        }

    }
}
