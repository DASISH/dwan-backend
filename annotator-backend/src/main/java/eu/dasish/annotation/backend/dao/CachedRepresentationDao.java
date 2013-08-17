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
    public CachedRepresentationIdentifier getExternalID(Number internalID);
    
     
    /**
     * 
     * @param externalID
     * @return the internal identifier of the resource with the "externalID"
     */   
    public Number getInternalID(CachedRepresentationIdentifier externalID);
    
  
    /**
     * 
     * @param internalID
     * @return the object "cCachedRepresentationInfo"  with the internal id "internalID"
     */
    public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID);
    
  
    /**
     * 
     * @param internalID
     * @return  # deleted rows on the table "cached_representation"
     */
    public  int deleteCachedRepresentationInfo(Number internalID);
    
    /**
     * 
     * @param cached
     * @return the internal Id of the just added "cached"
     */
    public Number addCachedRepresentationInfo(CachedRepresentationInfo cached);
    
   
    
}
