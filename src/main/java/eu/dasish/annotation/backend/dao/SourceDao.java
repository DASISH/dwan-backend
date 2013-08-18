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

import eu.dasish.annotation.backend.identifiers.SourceIdentifier;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author olhsha
 */
public interface SourceDao extends ResourceDao{
    
     /**
     * 
     * @param internalID
     * @return extrnalID identifier of the resource with internalID
     */
    public SourceIdentifier getExternalID(Number internalID);
    
   
    
    /**
     * 
     * @param inernalID
     * @return the object containing the source with the intrenal Id "internalId"
     */
    public Source getSource(Number internalID);
    
   
    
    /**
     * 
     * @param internalId
     * @return # deleted rows in "source" table
     */
    public int deleteSource(Number internalID);
    
    /**
     * 
     * @param source
     * @param versionID
     * adds freshSource to the DB and assigns the fresh external Identifier to it
     * @return the internal ID of the just added source
     * return -1 id the source cannot be added because its version is not in the DB
     */
    public Number addSource(Source source) throws SQLException;   
    
    public int addSourceVersion(Number sourceID, Number versionID) throws SQLException;  
    
    public int deleteAllSourceVersion(Number sourceID) throws SQLException;  
    
    
    
     /**
     * 
     * @param sourceID
     * @return the list of the internal version id-s for the  target source with the internal Id "sourceID" 
     */
    public List<Number> retrieveVersionList(Number sourceID);
    
  
    public List<SourceInfo> getSourceInfos(List<Number> sources);
    
  
    /**
     * 
     * @param link
     * @return the list source ID's which link-fields contain "link" as a substring
     */ 
    public List<Number> getSourcesForLink(String link);
  
    
    public boolean sourceIsInUse(Number sourceID);
}
