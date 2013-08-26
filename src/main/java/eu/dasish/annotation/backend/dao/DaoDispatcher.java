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

import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.Version;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author olhsha
 * 
 * Resource  and the corresponding Dao's are, so to say, "lavelled".  Notebook has level 5, Annotation has level 4, Source has level 3, Version has level 2 and CachedRepresentation has level 1. Users are not subject to this hierarchy.
 * The hierarchy is based on the way the schemas  for the resources are designed: to describe resource of the level X we need resource X-1.
 * 
 * DaoDispathcer class contains "getters", "adders" and "deleters".
 * 
 * An <adder> has two parameters: <resource<X>ID> and <resource<X-1>Object>. It return numbers of updated rows.
 * 
 * A <deleter> has one parameter: <resource<X>ID>. First, it checks if <resource<X>ID> is in use by calling <X>isInUse of the corresponding Dao.. 
 * If "yes" the nothing happens. Otherwise  the deletion proceeds.
 * Second, delete<X> is called from the corresponding Dao.
 * Third, delete<X-1> are recursively called for all the related sub-resources of the level X-1. E.g., after deleting an annotation itself all the sources (which are not used by other annotations) must be deleted as well.
 **/

/**
 * 
 * Comments on Dao-classes.
 * 
 * -- Each Dao-class contains "isInUse(internalID") method. It return "true" if the resource  with ID occurs at least in one of the joint tables. Used in "delete(internalID)" methods.
 * 
 * -- If the resource with "internalID" is asked to be deleted, the deletion methods will first call "isInUse(internalID)". If it returns 'true" nothing will happen. Otherwise deletion is happen. 
 * 
 * -- every "add(object)" method return the added object new internalID or null if the DB has not been updated for some reason.
 * 
 **/


public interface DaoDispatcher{
    
    public void setServiceURI(String serviceURI);
    
    
    /**
     * GETTERS
     */
    
    /**
     * 
     * @param UUID
     * @return the internal identifier of the annotations with "externalID", or null if no such annotation. 
     */
    Number getAnnotationInternalIdentifier(UUID externalID);
    
    /**
     * 
     * @param annotationID
     * @return the externalID of the annotation with "internalID" or null if there is no such annotation.
     */
    UUID getAnnotationExternalIdentifier(Number annotationID);


   /**
    * 
    * @param link
    * @param text
    * @param access
    * @param namespace
    * @param owner
    * @param after
    * @param before
    * @return the list of internal id-s of the annotations such that:
    * -- sources' links of which contain "link" (as a substring),
    * -- serialized bodies of which contain "text",
    * -- current user has "access" (owner, reader, writer)  to them,
    * -- namespace ???,
    * -- owned by "owner",
    * -- created after time-samp "after and before time-stamp "before".
    */
    List<Number> getFilteredAnnotationIDs(String link, String text, String access, String namespace, UUID
            owner, Timestamp after, Timestamp before);

    /**
     * 
     * @param userID
     * @return the external identifier of the user "userID", or null if no such user.
     */
    UUID getUserExternalIdentifier(Number userID);

    /**
     * 
     * @param externalID
     * @return the internal identifier of the user with "externalID", or null if there is no such user.
     */
    Number getUserInternalIdentifier(UUID externalID);
    
    /**
     * 
     * @param annotationID
     * @return the object Annotation generated from the tables "annotation", "annotations_target_sources", "source", "annotations_principals_permissions".
     * @throws SQLException 
     */
    Annotation getAnnotation(Number annotationID) throws SQLException;

   
   /**
    * ADDERS
    */
    /**
     * 
     * @param versionID
     * @param cached
     * @return result[0] = # updated rows in the table "versions_cached_representations" (must be 1 or 0).
     * result[1] = the internal ID of the added cached (a new one if "cached" was new for the Data Base).
     */
    Number[] addCachedForVersion(Number versionID, CachedRepresentationInfo cached);
    
    

    /**
     * 
     * @param sourceID
     * @param version
     * @return result[0] = added rows in the table "sources_versions" (1, or 0)
     * result[1] = the internal id of the added "version" ( a new one if the version was new for the DB
     * @throws SQLException 
     */
    
    Number[] addSiblingVersionForSource(Number sourceID, Version version) throws SQLException;

    
    /**
     * 
     * @param annotationID
     * @param sources
     * @return map temporarySourceID |--> sourceExternalID. Its domain is the temporary IDs of all the new sources. While adding a new source a new external ID is generated for it and it becomes the value of the map. The sourceIDs which are already present in the DB are not in the domain. If all sources are old, then the map is empty. 
     * @throws SQLException 
     */
    Map<String, String> addSourcesForAnnotation(Number annotationID, List<NewOrExistingSourceInfo> sources) throws SQLException;

    /**
     * 
     * @param annotation
     * @param userID
     * @return the internalId of the just added "annotation" (or null if it is not added) by the owner "userID". 
     * calls "addSourcesForAnnotation" 
     * @throws SQLException 
     */
    Number addUsersAnnotation(Annotation annotation, Number userID) throws SQLException;

    /**
     * DELETERS
     */
    
    /**
     * 
     * @param versionID
     * @param cachedID
     * @return result[0] = # deleted rows in the table "versions_cached_representations" (1, or 0).
     * result[1] = # deleted rows in the table "cached_representation" (should be 0 if the cached representation is in use by some other version).
     */
    int[] deleteCachedOfVersion(Number versionID, Number cachedID);



    
    /**
     * 
     * @param versionID
     * @return result[0] = # deleted tows in the table "version".
     * result[1] =  # deleted rows in the table "versions_cached_representations".
     * result[2] = # deleted rows in the table "cached_representation".
     * If the version "versionId" is in use (occurs in at least one of the joint tables) then nothing happes in the DB and all the three values result[0], result[1] and result[2] are zeros.
     */
    
    int[] deleteAllCachedOfVersion(Number versionID);

    
   /**
    * 
    * @param sourceID
    * @return result[0] = # deleted rows in the table "source" ( 0 is the source is in use).
    * result[1] = # deleted rows in the table "sources_versions".
    * result[2] = # deleted rows in the table source.
    * @throws SQLException 
    */
    int[] deleteAllVersionsOfSource(Number sourceID) throws SQLException;

    /**
     * 
     * @param annotationID
     * @return result[0] = # deleted rows in the table "annotation" (1 or 0).
     * result[1] = # deleted rows in the table "annotations_principals_permissions".
     * result[2] = # deleted rows in the table "annotations_target_sources".
     * result[3] = # deleted rows in the table "source".
     * @throws SQLException 
     */
    int[] deleteAnnotation(Number annotationID) throws SQLException;

    
    
}
