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

import eu.dasish.annotation.schema.CachedRepresentationInfo;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author olhsha
 */
public interface CachedRepresentationDao extends ResourceDao{
    
   
    /** GETTERS
     * 
     */
  
    /**
     * 
     * @param internalID
     * @return the object of the class "CachedRepresentationInfo"  with the internal id "internalID".
     */
    public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID);
    
    
    /**
     * 
     * @param internalID
     * @return the Blob of the cached representation  with the internal id "internalID".
     */
    public InputStream getCachedRepresentationBlob(Number internalID);
    
    
     /* 
     * @param targetID
     * @return the list of the cached representation's ID-s for the target the internal ID "targetID". 
     */
    public List<Number> getCachedRepresentationsForTarget(Number targetID);
  
    /**
     * ADDERS
     */
    
     /**
     * 
     * @param cachedInfo
     * @param cachedBlob
     * @return the internal ID of the just added "cached", or null if the cached representation is not added for some reason.
     */
    public Number addCachedRepresentation(CachedRepresentationInfo cachedInfo, InputStream cachedBlob);
    
    
   /**
    * DELETERS
    */
    /**
     * 
     * @param internalID
     * @return  # deleted rows on the table "cached_representation".
     */
    public  int deleteCachedRepresentation(Number internalID);
    
   
    
}
