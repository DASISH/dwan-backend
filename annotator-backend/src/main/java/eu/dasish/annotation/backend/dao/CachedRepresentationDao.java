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
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author olhsha
 */
public interface CachedRepresentationDao extends ResourceDao {

    /**
     * GETTERS
     *
     */
    /**
     *
     * @param internalID the internal database Id of the cached representation.
     * @return a {@link CachedRepresentationInfo} object representing the metadata
     * of the cached representation with the internal id "internalID".
     */
    public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID);

    /**
     *
     * @param internalID the internal database Id of a target.
     * @return the Blob of the cached representation with the internal database id "internalID".
     */
    public InputStream getCachedRepresentationBlob(Number internalID);

    /** 
     * @param targetID the internal database Id of a target.
     * @return the list of the cached representation's ID-s for the target with the internal database ID "targetID". 
     */
    public List<Number> getCachedRepresentationsForTarget(Number targetID);

    /**
     * ADDERS
     */
    /**
     *
     * @param cachedInfo a {@link CachedRepresentationInfo} object representing the metadata of a cached representation.
     * @param cachedBlob the content of the cached representation, considered as BLOB.
     * @return the internal ID of the just added cached representation, or throws an exception if adding fails.
     * @throws NotInDataBaseException if there is no object with the generated for the new cached representation external id. 
     * @throws IOException if reading blob fails.
     */
   
    public Number addCachedRepresentation(CachedRepresentationInfo cachedInfo, InputStream cachedBlob) throws NotInDataBaseException, IOException;

    /**
     * UPDATERS
     */
    
    /**
     * 
     * @param internalID the internal database id of a cached representation.
     * @param cachedInfo new metadata for it.
     * @return # of updated rows in the table "cached_representation". Must be 1 if updated, and 0 otherwise.
     */
    public int updateCachedRepresentationMetadata(Number  internalID, CachedRepresentationInfo cachedInfo);

    /**
     * 
     * @param internalID the internal database id of a cached representation.
     * @param cachedBlob the new content considered as BLOB.
     * @return # of updated rows in the table "cached_representation". Must be 1 if updated, and 0 otherwise.
     * @throws IOException if reading BLOB fails.
     */
    public int updateCachedRepresentationBlob(Number  internalID, InputStream cachedBlob) throws IOException;

    /**
     * DELETERS
     */
    /**
     *
     * @param internalID the internal database id of a cached representation.
     * @return # deleted rows on the table "cached_representation".
     */
    public int deleteCachedRepresentation(Number internalID);
}
