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
     * GETTERS 
     **/
    
    /**
     * 
     * @param inernalID
     * @return the source with the intrenal Id "internalID".
     */
    public Source getSource(Number internalID);
    
    /**
     * 
     * @param sources
     * @return the list of SoirceInfo objects corresponding to the sources with the internalIds from the list "sources".
     */
    public List<SourceInfo> getSourceInfos(List<Number> sources);
    
    /**
     * 
     * @param sourceID
     * @return id of the sibling-source class of sourceID if defined, or null otherwise
     */
    public Integer getSourceSiblingClass(Number sourceID);
      
     /**
     * 
     * @param sourceID
     * @return the list of the internal source ID-s ("siblings") for the target source with the internal ID "sourceID". 
     */
    public List<Number> getSiblingSources(Number sourceID);
    
     /**
     * 
     * @param sourceID
     * @return the list of the cached representation's ID-s for the target source with the internal ID "sourceID". 
     */
    public List<Number> getCachedRepresentations(Number sourceID);
    
    /**
     * 
     * @param link
     * @return the list of source ID's which link-fields contain "link" as a substring.
     */ 
    public List<Number> getSourcesForLink(String link);
  
    
    /**
     * 
     * @param sourceID
     * @return true if "sourceID" occurs in at least one of the joint tables "annotations_target_sources" and "sources_versions".
     */
    public boolean sourceIsInUse(Number sourceID);
  
    /** 
     * ADDERS
     **/
    
     /**
     * 
     * @param source: the Source-object of the source to be added to "source" table.
     * @return the internal ID of the just added source or null if it has not been added.
     */
    public Number addSource(Source source) throws SQLException;   
    
    /**
     * 
     * @param sourceID
     * @param cachedID
     * @return # added rows to the table "sources_cached_representations". Should be "1" if the pair (sourceID, cachedID) has been added.
     * @throws SQLException 
     */
    public int addSourceCachedRepresentation(Number sourceID, Number cachedID) throws SQLException; 
    
    /**
     * 
     * @param sourceID
     * @param classID
     * @return # of updated rows (should be 1) when updating the row for sourceID by class classID
     * @throws SQLException 
     */
    public int updateSiblingClass(Number sourceID, int classID) throws SQLException;
    
    
    /** 
     * DELETERS
     **/
    
    /**
     * 
     * @param internalId
     * @return # deleted rows in "source" table. Should be "1" if the source has been deleted.
     */
    public int deleteSource(Number internalID);
    
   
    /**
     * 
     * @param sourceID
     * @return # deleted rows in the table "sources_cached_representation" when deleting the pair (sourceID, chachedID)
     * @throws SQLException 
     */
    public int deleteSourceCachedRepresentation(Number sourceID, Number chachedID) throws SQLException;  
    
    
  
}
