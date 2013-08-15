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
import eu.dasish.annotation.schema.CachedRepresentationInfo;
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
    * @param externalID
    * @return the internal Id of the Version with the external ID "externalID"
    */
    public Number getInternalID(VersionIdentifier externalID);
    
   
    /**
     * 
     * @param internalID
     * @return the instance of Version.class  with the internal Id equal to "internalID"
     * 
     */
    public Version getVersion(Number internalID);
    
   
     
    /** @param versionID
     * @return result[0] # deleted rows in the joint table "sources_versions"
     * result[1] # deleted rows in the joit table "versions_cached_representations"
     * result[2] # deleted rows in "version" table
     * result[3] # deleted cached representations (which are not referred by other versions)
     */
    
    public int[] deleteVersion(Number versionID);
    
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
     * @return The list of the cached representation internal id-s of all the cached representations of the version with "versionID"
     */
    public List<Number> retrieveCachedRepresentationList(Number versionID);
    
}
    

