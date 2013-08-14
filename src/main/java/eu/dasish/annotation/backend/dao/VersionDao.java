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

import eu.dasish.annotation.backend.identifiers.VersionIdentifier;
import eu.dasish.annotation.schema.Version;
import java.util.List;

/**
 *
 * @author olhsha
 */
public interface VersionDao {
    
      /**
     * 
     * @param internalID
     * @return extrnalID identifier of the resource with internalID
     */
    
    public VersionIdentifier getExternalID(Number internalID);    
   
   
    /**
     * 
     * @param internalID
     * @return the instance of Version.class  where the version internal Id is "internalID"
     * 
     */
    public Version getVersion(Number internalID);
    
    /**
     * 
     * @param sourceID
     * @return the list of the internal version id-s for the  target source with the internal Id "sourceID" 
     */
    public List<Number> retrieveVersionList(Number sourceID);
    
     
    /** @param versionID
     * removes the row of "version" with the internal ID "internalID" if no references to this version from the tables "sources_versions" and "source"
     * @return the amount of removed rows
     */
    
    public int deleteVersion(Number versionID);
    
    /**
     * 
     * @param version
     * @return the internal Id of the just added version
     * 
     */
    public Number addVersion(Version version);
   
    /**
     * 
     * @param versionID
     * @return removes the rows (versionID, some cached representation id) from the joint table "versions_cached_representations"
     */
    public int deleteVersionCachedRepresentationRow(Number versionID);
    
    
    /**
     * 
     * @param sourceID
     * @param cachedRepresentationID
     * @return 
     * 1) the amount of rows affected by deleting cached representation "cachedRepresentationID"
     * from the table "versions_cached_representations", if the corresponding version is a sibling-version of the source surceID
     * 2) the amount of rows affected by SAFE removing cachedRepresentationID from cached_representation table, 
     * if the first number>0
     * 
     * used to fulfill DELETE api/sources/<sid>/cached/<cid>
     */
    public  int[] deleteCachedRepresentationForSource(Number sourceID, Number cachedRepresentationID);
    
    public Number getInternalID(VersionIdentifier externalID);
}
    

