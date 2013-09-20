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

import eu.dasish.annotation.schema.Version;
import java.util.List;

/**
 *
 * @author olhsha
 */
public interface VersionDao extends ResourceDao{
    
   /** 
     * GETTERS 
     **/
    
    /**
     * 
     * @param internalID
     * @return the Version-object build from the version  with the "internalID".
     * 
     */
    public Version getVersionWithoutCachedRepresentations(Number internalID);
    
    
       /**
     * 
     * @param versionID
     * @return The list of the cached-representation internal ID-s of all the cached representations of the version with "versionID".
     */
    public List<Number> retrieveCachedRepresentationList(Number versionID);
    
     
    /**
     * 
     * @param versionsID
     * @return true if "versionID" occurs in the table "versions_cached_representations".
     */
    public boolean versionIsInUse(Number versionID);
    
    
    /** 
     * ADDERS
     **/
    
    /**
     * 
     * @param versionID
     * @param cachedID
     * @return # rows added to the table "versions_cached_representations", when adding the pair (versionID, sourceID). Should be 1, if it has been added.
     */
    public int addVersionCachedRepresentation(Number versionID, Number cachedID);
    
    
    /**
     * 
     * @param version
     * @return the internal Id of the just added "version", or null if it has not been added.
     * 
     */
    public Number addVersion(Version version);
   
  
    
    /** 
     * DELETERS  
     **/
    
     
    /** @param versionID
     * @return # deleted rows in "version" table after deleting the version with "versionID". Should be "1" if the version has been deleted.
     */
    
    public int deleteVersion(Number versionID);
    
    /**
     * 
     * @param versionID
     * @param cachedID
     * @return # deleted rows in the table "versions_cached_representations" after deleting the pair (versionID, cachedID).
     */
    public int deleteVersionCachedRepresentation(Number versionID, Number cachedID);
    
    
    /**
     * 
     * @param versionID
     * @return # deleted rows in the table "versions_cached_representations" after deleting all the pairs of the form (versionID, *).
     */
    public int deleteAllVersionCachedRepresentation(Number versionID);
   
}
    

