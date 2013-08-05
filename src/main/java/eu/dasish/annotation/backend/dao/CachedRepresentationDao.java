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

import eu.dasish.annotation.backend.identifiers.CachedRepresentationIdentifier;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.CachedRepresentations;
import java.util.List;

/**
 *
 * @author olhsha
 */
public interface CachedRepresentationDao {
    
    /**
     * 
     * @param internalID
     * @return extrnalID identifier of the resource with internalID
     */
    public CachedRepresentationIdentifier getExternalId(Number internalID);
    
    /**
     * 
     * @param externalID
     * @return internal identifier of the resource with externalID
     */
    public  Number getInternalId(CachedRepresentationIdentifier externalID);
    
    /**
     * 
     * @param internalID
     * @return the object "cached representation info"  with the internal id "internalID"
     */
    public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID);
    
    /**
     * 
     * @param versionID
     * @return The list list of cached representation internal id-s of all the cached representations of the version with versionID
     */
    public List<Number> retrieveCachedRepresentationList(Number versionID);
    
    /**
     * 
     * @param internalID
     * @return the amount of rows affected by removing from the DB the cached representation with the id "intenalID"
     * should be "1" (or 0 with  a non-existing id on the input)
     * as a obligadory side-effect: deletes all the rows in the table versions_cached_representations containing "internalId" as a cached_representaton_ID
     */
    public int deleteCachedRepresentationInfo(Number internalID);
    
    /**
     * 
     * @param cached
     * @return copy of "cached" after "cached" is added to the DB; the internal id is set in the return copy
     */
    public CachedRepresentationInfo addCachedRepresentationInfo(CachedRepresentationInfo cached);
    
    
    /**
     * 
     * @param internalID
     * removes the version with internalId from the DB is there is no reference to to it n the table versions_cached_representations
     * @return the amount of removed rows
     */
    public int purge(Number internalID);
   
}
