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
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfos;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
     * @param annotationID
     * @return the list of the source's internal IDs of all the target sources of annotationID
     */
    public List<Number> retrieveSourceIDs(Number annotationID);
    
    /**
     * 
     * @param inernalID
     * @return the object containing the source with the intrenal Id "internalId"
     */
    public Source getSource(Number internalID);
    
    /**
     * 
     * @param internalId
     * removes the source with the ID "internalId" from the DB, if it is not a target source of some annotation
     * @return the amount of affected rows in the "source" table
     */
    public int deleteSource(Number internalID);
    
    /**
     * 
     * @param freshSource
     * adds freshSource to the DB and assigns the fresh external Identifier to it
     * @return the copy of freshSource with the assigned external identifier 
     */
    public Source addSource(Source freshSource) throws SQLException;
   
    //////////////////////////////////////////////
    
    /**
     * 
     * @param annotationID
     * @return the Information about the target sources to which annotationId refers
     */
    public List<SourceInfo> getSourceInfos(Number annotationID);
    
   /**
    * 
    * @param sourceInfoList
    * @return the list of NewOrExistingSourceo objects in such a way  that an element of the sourceInfoList is injected into an element of the return list
    */
   //TODO: add non-existing sources!! now is implemented only for existing sources
    public NewOrExistingSourceInfos contructNewOrExistingSourceInfo(List<SourceInfo> sourceInfoList);
    
    
    
    /**
     * 
     * @param sourceID
     * @return delete all the rows in "sources_versions" table with sourceID
     */
    public int deleteSourceVersionRows(Number sourceID);
    
    
    /**
     * 
     * @param annotationID
     * @param sources
     * @return the mapping of a (possible new, freshly added) source onto the corresponding same source in the DB.
     * The difference between the argument/key source and the value source is just their internal and external identifiers.
     * The already existing source is mapped onto itself.
     * The side-effect: the joint table "annotations_target_sources" is extended by the pairs (annotationID, addedSoiurceID).
     */
    public Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo> addTargetSources(Number annotationID, List<NewOrExistingSourceInfo> sources) throws SQLException;        
    
        
}
