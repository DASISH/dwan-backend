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

import eu.dasish.annotation.backend.ForbiddenException;
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.PrincipalExists;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.dao.ILambda;
import eu.dasish.annotation.backend.dao.ILambdaPrincipal;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Principal;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
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

    public T wrapRequestResource(Map params, ILambda<Map, T> dbRequestor) throws IOException, NotInDataBaseException {
        Number remotePrincipalID = resourceResource.getPrincipalID();
        if (remotePrincipalID == null) {
            return null;
        }
        params.put(_principalID, remotePrincipalID);
        return dbRequestor.apply(params);
    }

    public T wrapRequestResource(Map params, ILambda<Map, T> dbRequestor, Resource resource, Access action, String externalId) throws IOException, ForbiddenException, NotInDataBaseException {
        Number principalID = resourceResource.getPrincipalID();
        if (principalID == null) {
            return null;
        }
        params.put(_principalID, principalID);
        final Number resourceID = resourceResource.dbDispatcher.getResourceInternalIdentifier(UUID.fromString(externalId), resource);
        if (resourceResource.dbDispatcher.canDo(action, principalID, resourceID, resource)) {
            params.put(_externalId, externalId);
            params.put(_internalID, resourceID);
            params.put(_resourceType, resource);
            return dbRequestor.apply(params);
        } else {
            throw new ForbiddenException(this.FORBIDDEN_RESOURCE_ACTION(externalId, resource.name(), action.name()));
        }

    }
    
    

    public JAXBElement<Principal> wrapAddPrincipalRequest(Map params, ILambdaPrincipal<Map, Principal> dbRequestor) throws IOException, NotInDataBaseException, PrincipalExists {
        return new ObjectFactory().createPrincipal(dbRequestor.apply(params));
    }
}
