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
package eu.dasish.annotation.backend.dao;

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.schema.Access;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This interface defines methods common for all dao's: Annotation, Target, Cached Representation, Principal, Notebook.
 * An implementation must set a non-null value for the field "resourceTableName". It is one of the five names of resource tables:
 * "annotation", "target", "cached_representation", "principal", "notebook".
 * @author olhsha
 */
public interface ResourceDao {

    /**
     * 
     * @param relResourcePath a string, representing "[relative-servise-url]/[resource]", e.g. "my-servlet/annotations".
     */
    void setResourcePath(String relResourcePath);

    /**
     * 
     * @param externalId the external UUID of a resource. 
     * @return the internal ID of the resource.
     * @throws NotInDataBaseException if the resource with this id is not found.
     */
    Number getInternalID(UUID externalId) throws NotInDataBaseException;

    /**
     * 
     * @param internalId the internal ID of the resource.
     * @return  the external UUID of the resource. 
     */
    UUID getExternalID(Number internalId);

    /**
     * 
     * @param oldIdentifier the current external UUID of a resource.
     * @param newIdentifier the new external UUID of the resource.
     * @return true iff the external id of the resource has been updated to the value of "newIdentifier".
     */
    boolean updateResourceIdentifier(UUID oldIdentifier, UUID newIdentifier);
  
    /**
     *
     * @param internalID the internal databaseID of the resource.
     * @return the hyper-reference string of the resource with "internalID". 
     */
    String getHrefFromInternalID(Number internalID);
    
    /**
     * 
     * @param href the hyper-reference of a resource.
     * @return  the internal ID of the resource with "href".
     * @throws NotInDataBaseException if the resource with ""href" is not found in the corresponding database.
     */
    Number getInternalIDFromHref(String href)  throws NotInDataBaseException;

    /**
     * 
     * @param resourceID the internal database id of a resource.
     * @return mapping "principal internal id" -> "access" for a given "resourceID"; 
     * till now makes sense only for annotations and notebooks.
     */
    List<Map<Number, String>> getPermissions(Number resourceID);

    /**
     * 
     * @param resourceID the internal database id of a resource.
     * @return the public access mode for "resourceID"; till now makes sense only for annotations and notebooks.
     */
    Access getPublicAttribute(Number resourceID);
    
    
}
