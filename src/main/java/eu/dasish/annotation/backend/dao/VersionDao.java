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
     * @return the instance of Version.class  with the internal Id equal to "internalID"
     * 
     */
    public Version getVersion(Number internalID);
    
    
       /**
     * 
     * @param versionID
     * @return The list of the cached representation internal id-s of all the cached representations of the version with "versionID"
     */
    public List<Number> retrieveCachedRepresentationList(Number versionID);
    
     
    // Not tested
    public boolean versionIsInUse(Number versionsID);
    
    
    /** 
     * ADDERS
     **/
    
    
    public int addVersionCachedRepresentation(Number versionID, Number cachedID);
    
    
    /**
     * 
     * @param version
     * @return the internal Id of the just added version
     * 
     */
    public Number addVersion(Version version);
   
  
    
    /** 
     * DELETERS  
     **/
    
     
    /** @param versionID
     * @return deleted rows in "version" table
     */
    
    public int deleteVersion(Number versionID);
    
    
    public int deleteVersionCachedRepresentation(Number versionID, Number cachedID);
    
    
    
    public int deleteAllVersionCachedRepresentation(Number versionID);
   
}
    

