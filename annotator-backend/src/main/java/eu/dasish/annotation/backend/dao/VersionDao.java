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
     * @return the copy of "version" with the new external Id set in "version" text field  (for now)
     * 
     */
    public Version addVersion(Version version);
   

   
    
}
