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
 * Different requests for different resources have some steps in common and therefore
 * can be wrapped in a class whose higher-order methods perform the common steps and then call 
 * the methods performing the specific part; the specific-part object is passed as a parameter.
 * @author olhsha
 */
public class RequestWrappers<T> {

    private ResourceResource resourceResource;
    private String _internalID = "internalID";
    private String _principalID = "principalID";
    private String _externalId = "externalID";
    private String _resourceType = "resourceType";

    /**
     * Set the instance of a specific REST class.
     * @param resourceResource a {@link ResourceResource} object representing one of the rest resource classes of this package.
     */
    public RequestWrappers(ResourceResource resourceResource) {
        this.resourceResource = resourceResource;
    }

    /**
     * 
     * @param identifier the external UUID of the resource over which the action should have been performed.
     * @param resource a type of resource: "annotation", "principal", "cached representation", "notebook", "target".
     * @param action a string naming the action that should have been performed.
     * @return a string representing a tuned error message.
     */
    public String FORBIDDEN_RESOURCE_ACTION(String identifier, String resource, String action) {
        return " The logged-in principal cannot perform action that falls under access mode'" + action + "', with the " + resource + " with the identifier " + identifier;
    }

    /**
     * This is the simplest wrapper which checks if there is a logged-in principal.
     * @param params list of parameters that will be used by a specific part of the request (in "the continuation").
     * @param dbRequestor the object that will be performing the specific part of the request ("continuation").
     * @return the requested resource.
     * @throws IOException if sending an error fails in the called methods.
     * @throws NotInDataBaseException if thrown by the specific part of the request.
     * @throws ForbiddenException if the action is forbidden for the inlogged user.
     */
    public T wrapRequestResource(Map params, ILambda<Map, T> dbRequestor) throws IOException, NotInDataBaseException, ForbiddenException {
        Number remotePrincipalID = resourceResource.getPrincipalID();
        if (remotePrincipalID == null) {
            return null;
        }
        params.put(_principalID, remotePrincipalID);
        return dbRequestor.apply(params);
    }

    /**
     * 
     * @param params list of parameters that will be used by a specific part of the request (in "the continuation").
     * @param dbRequestor the object that will be performing the specific part of the request ("continuation").
     * @param resource a {@link Resource} object, representing the type of a resource:  "annotation", "principal", "cached representation", "notebook", "target".
     * @param action an {@link Access} object representing the type ("mightiness")  of the action.
     * @param externalId the external UUID of the resource.
     * @return the resource.
     * @throws IOException if sending an error fails in the called methods.
     * @throws NotInDataBaseException if thrown by the specific part of the request.
     * @throws ForbiddenException if the action is forbidden for the inlogged user.
     */
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
    
    
    /**
     * The method is used for uniformity to treat principal-specific requests as others;
     * in the future the common part can be added if necessary.
     * @param params list of parameters that will be used by a specific part of the request (in "the continuation").
     * @param dbRequestor the object that will be performing the specific part of the request ("continuation").
     * @return a {@link Principal} element.
     * @throws IOException if sending an error fails in the called methods.
     * @throws NotInDataBaseException if thrown by the specific part of the request.
     * @throws PrincipalExists if a principal with the given e-mail or name is already exist in the database
     * (can be sthrown if the request is to add or update a principal).
     */
    public JAXBElement<Principal> wrapAddPrincipalRequest(Map params, ILambdaPrincipal<Map, Principal> dbRequestor) throws IOException, NotInDataBaseException, PrincipalExists {
        return new ObjectFactory().createPrincipal(dbRequestor.apply(params));
    }
}
