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

import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.ResourceREF;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Created on : Jun 27, 2013, 10:34:13 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */

// TODO: Getting Target Sources from Body and testing must be added!!!


public interface AnnotationDao extends ResourceDao{
    
     /**
     * 
     * @param internalId
     * @return the external identifier for the annotation with internalId
     */
    public AnnotationIdentifier getExternalID(Number internalId);
    
    
    /**
     * 
     * @param annotationID
     * @return the Annotation object with empty list of sources 
     * constructing a complete Annotation object from theresult and "retrieveSourceIDs" is done in "CompoundRequests"
     * 
     */
    public Annotation getAnnotationWithoutSources(Number annotationID) throws SQLException;
    
    
    /**
     * 
     * @param annotationId
     * @return 
     * result[0] = # removed "annotations_principals_perissions" rows
     * result[1] = # removed "annotatiobs_target_sources" rows
     * result[2] = # removed annotation rows (should be 1)
     */
    
    public int[] deleteAnnotation(Number annotationId) throws SQLException;
    
    
   
    /**
     * 
     * @param annotation added to the table with annotations 
     * @return  internal Id of the added annotation
     **/
    
    public Number addAnnotation(Annotation annotation, Number ownerID) throws SQLException;
 
     
    /**
     * 
     * @param link optional
     * @param text optional
     * @param access optional
     * @param namespace optional TODO: do not know what to do with it 
     * @param owner optional 
     * @param after optional
     * @param before optional 
     * @return the list of internal annotation identifiers for annotations 
     * -- referring to the "link", 
     * -- bodies of which contain the "text", 
     * -- to which inlogged user has "access", 
     * -- owned by "owner", 
     * -- added to the database between "before" and "after" time-dates.
     * 
     * 
     * The first step for GET api/annotations?<filters>
     */
    public List<Number> getFilteredAnnotationIDs(List<Number> annotationIDs, String text, String access, String namespace, Number ownerID, Timestamp after, Timestamp before);
       
    /**
     * 
     * @param annotationIDs
     * @return the list of annotationInfos (owner, headline, target sources, external_id) for the internal Ids from the  input list
     * used on the second step for GET api/annotations?<filters>
     */
    public List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs);    
     
   
    
    public List<ResourceREF> getAnnotationREFs(List<Number> annotationIDs); 
    
    /**
     * 
     * @param sourceIDs
     * @return the list of annotationdIDs of the annotations that are having target sources from "sourceIDs" list
     */
    public List<Number> retrieveAnnotationList(List<Number> sourceIDs);
   
    
    // NOT TESTED
    public int updateBody(Number annotationID, String serializedNewBody);
    
     /**
     * 
     * @param annotationID
     * @return the list of the source's internal IDs of all the target sources of annotationID
     */
    public List<Number> retrieveSourceIDs(Number annotationID);
    
    public int addAnnotationSourcePair(Number annotationID, Number sourceID) throws SQLException;
    
}
