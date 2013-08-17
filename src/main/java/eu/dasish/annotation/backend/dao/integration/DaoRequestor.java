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
package eu.dasish.annotation.backend.dao.integration;

import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.PermissionsDao;
import eu.dasish.annotation.backend.dao.SourceDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.backend.dao.VersionDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.CachedRepresentationIdentifier;
import eu.dasish.annotation.backend.identifiers.SourceIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.backend.identifiers.VersionIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewSourceInfo;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author olhsha
 */
public class DaoRequestor {

    @Autowired
    UserDao userDao;
    @Autowired
    PermissionsDao permissionsDao;
    @Autowired
    CachedRepresentationDao cachedRepresentationDao;
    @Autowired
    VersionDao versionDao;
    @Autowired
    SourceDao sourceDao;
    @Autowired
    AnnotationDao annotationDao;
    @Autowired
    NotebookDao notebookDao;
    
    
    
    public Number getAnnotationInternalIdentifier(AnnotationIdentifier annotationIdentifier){
        return annotationDao.getInternalID(annotationIdentifier);
    }
    
     public AnnotationIdentifier getAnnotationExternalIdentifier(Number annotationID){
        return annotationDao.getExternalID(annotationID);
    }

     public Number getUserInternalIdentifier(UserIdentifier userIdentifier){
        return userDao.getInternalID(userIdentifier);
    }
    
     public UserIdentifier getUserExternalIdentifier(Number userID){
        return userDao.getExternalID(userID);
    } 
     
    /**
     *
     * @param versionID
     * @param cachedID
     * @return result[0] # deleted rows (versionID, cachedID) in the table
     * "versions_cached_representations" result[1] # deleted rows in the table
     * "cached_representation"
     */
    public int[] deleteCachedForVersion(Number versionID, Number cachedID) {
        int[] result = new int[2];
        result[0] = versionDao.deleteVersionCachedRepresentation(versionID, cachedID);
        if (result[0] > 0) {
            result[1] = cachedRepresentationDao.deleteCachedRepresentationInfo(cachedID);
        } else {
            result[1] = 0;

        }
        return result;
    }

    /**
     *
     * @param versionID
     * @param cached
     * @return result[0] = the internalId of the added (if it is not yet in th
     * DB) cached representation result[1] # added rows to
     * "versions_cached_representations"
     */
    public Number[] addCachedForVersion(Number versionID, CachedRepresentationInfo cached) {
        Number[] result = new Number[2];
        result[0] = cachedRepresentationDao.getInternalID(new CachedRepresentationIdentifier(cached.getRef()));
        if (result[0] == null) {
            result[0] = cachedRepresentationDao.addCachedRepresentationInfo(cached);
        }
        result[1] = versionDao.addVersionCachedRepresentation(versionID, result[0]);
        return result;
    }
    
    /**
     * 
     * @param versionID
     * @return  
     * result[0] # deleted rows in the joit table "versions_cached_representations"
     * result[1] # deleted rows in "version" table
     * result[2] # deleted cached representations (which are not referred by other versions)
     * 
     */

    public int[] deleteVersionWithCachedRepresentations(Number versionID) {
        int[] result = new int[3];
        List<Number> cachedRepresentations = versionDao.retrieveCachedRepresentationList(versionID);
        int[] deleteVersion = versionDao.deleteVersion(versionID);
        result[0] = deleteVersion[0];
        result[1] = deleteVersion[1];
        result[3] = 0;
        for (Number cachedID : cachedRepresentations) {
            result[3] = result[3] + cachedRepresentationDao.deleteCachedRepresentationInfo(cachedID);

        }
        return result;
    }
    
    /**
     * 
     * @param sourceID
     * @return result[0] # deleted rows in "sources_versions" table name
     * result[1] # deleted rows in "source" table
     * result[2] # deleted rows in "version" table (not used by the other sources)
     */
    public int[] deleteSourceWithVersions(Number sourceID){
        int[] result = new int[3];  
        List<Number> versions = sourceDao.retrieveVersionList(sourceID);
        int[] deleteSource = sourceDao.deleteSource(sourceID);
        result[0] = deleteSource[0];
        result[1]=deleteSource[1];
        result[3] =0;
        for (Number versionID : versions) {
            int[] deleteVersion = deleteVersionWithCachedRepresentations(versionID);
            result[3]=result[3]+deleteVersion[1];
        }
      return result;
      
    }
    
    /**
     * @param source
     * @return internal Id of the newly added source or -1 if the source is not added because no version for it;
     * in the last case the rest-interface  will return envelope asking to add a version (with the cached representation);
     * after the clinet adds the version (s)he gets the version external ID which is to be set in source;
     * the the client asks to add the source again.
     * @throws SQLException 
     */
    public Number addSourceAndPairSourceVersion(NewSourceInfo newSource) throws SQLException{
        Number versionID = versionDao.getInternalID(new VersionIdentifier(newSource.getVersion()));
        if (versionID == null) {
            System.out.println("Cannot add source because there is no version for it, and no cached representation. Create them first and try again.");
            return -1;
        }
        Source source = new Source();
        SourceIdentifier externalIdentifier = new SourceIdentifier();
        source.setURI(externalIdentifier.toString());
        source.setLink(newSource.getLink());
        source.setVersion(newSource.getVersion());        
        Number result = sourceDao.addSource(source);
        final int sourceVersions = sourceDao.addSourceVersion(result, versionID);
        return result;
    }
    
  

    ///////////////////////////////////////////////
   
    /**
     * 
     * @param annotationID
     * @param sources
     * @return the mapping temporarySourceID -> peristenExternalSOurceId
     * adds a source from "sources" to the DB if it is not there, to the table "target_source"
     * adds the wro (annotationID, sourceID) to the joint table "annotations_sources"
     * @throws SQLException 
     */
    public Map<String, String> addTargetSourcesToAnnotation(Number annotationID, List<NewOrExistingSourceInfo> sources) throws SQLException {
        Map<String, String> result = new HashMap<String, String>();
        for (NewOrExistingSourceInfo noesi : sources) {
            SourceInfo source = noesi.getSource();
            if (source != null) {
                int affectedRows = annotationDao.addAnnotationSourcePair(annotationID, sourceDao.getInternalID(new SourceIdentifier(source.getRef())));
            } else {
                Number newSourceID = addSourceAndPairSourceVersion(noesi.getNewSource());
                if (newSourceID.intValue() == -1) {
                    result.put(noesi.getNewSource().getId(), null);
                } else {
                    result.put(noesi.getNewSource().getId(), sourceDao.getExternalID(newSourceID).toString());
                    int affectedRows = annotationDao.addAnnotationSourcePair(annotationID, newSourceID);
                }
            }
        }
        return result;
    } 
    
    
      ////////////////////////////////////////////////////////////////////////
   
    public List<Number> getFilteredAnnotationIDs(String link, String text, String access, String namespace, UserIdentifier owner, Timestamp after, Timestamp before) {

        List<Number> annotationIDs = null;
        
        if (link != null) {
            List<Number> sourceIDs = sourceDao.getSourcesForLink(link);
            annotationIDs = annotationDao.retrieveAnnotationList(sourceIDs);
        }
       
        Number ownerID =  null;  
        if (owner != null) {
            ownerID = userDao.getInternalID(owner);
        }

        return annotationDao.getFilteredAnnotationIDs(annotationIDs, text, access, namespace, ownerID, after, before);
    }

     /**
     * 
     * @param annotationId
     * @return 
     * result[0] = # removed "annotations_principals_permissions" rows
     * result[1] = # removed "annotations_target_sources" rows
     * result[2] = # removed annotation rows (should be 1)
     * result[3] = # deleted sources
     */
    public int[] deleteAnnotationWithSources(Number annotationID) throws SQLException {
        int[] result = new int[4];
        List<Number> sourceIDs = annotationDao.retrieveSourceIDs(annotationID);        
        int[] deleteAnnotation = annotationDao.deleteAnnotation(annotationID);
        result[0]= deleteAnnotation[0];
        result[1]= deleteAnnotation[1];
        result[2] = deleteAnnotation[2];
        result[3]=0;
        for (Number sourceID : sourceIDs) {
            int[] deleteSource = deleteSourceWithVersions(sourceID);
            result[3] = result[3] + deleteSource[1];
        }
        return result;
    }
     
    
    public Annotation getAnnotation(Number annotationID) throws SQLException{
        Annotation result = annotationDao.getAnnotationWithoutSources(annotationID);
        List<Number> sourceIDs = annotationDao.retrieveSourceIDs(annotationID);
        for (Number sourceID : sourceIDs) {
            NewOrExistingSourceInfo noesi = new  NewOrExistingSourceInfo();
            Source source = sourceDao.getSource(sourceID);
            SourceInfo sourceInfo = new SourceInfo();
            sourceInfo.setLink(source.getLink());
            sourceInfo.setRef(source.getURI());
            sourceInfo.setVersion(source.getVersion());
            noesi.setSource(sourceInfo);
            result.getTargetSources().getTarget().add(noesi);
        }
        return result;
    }
    
    
    //need to return an envelope!
    public Annotation addAnnotationWithTargetSources(Annotation annotation, Number userID) throws SQLException{
        
        Number annotationID = annotationDao.addAnnotation(annotation, userID);
        
        List<NewOrExistingSourceInfo> sources = annotation.getTargetSources().getTarget();
        Map<String, String> sourceIdPairs= addTargetSourcesToAnnotation(annotationID, sources);
        
        if (sourceIdPairs.containsValue(null)){
           // for one of the soirces there was no version and cached representation
            // envelope
           return annotation;
        }
        String body = Helpers.serializeBody(annotation.getBody());
        String newBody = Helpers.replace(body, sourceIdPairs);
        int affectedAnnotRows = annotationDao.updateBody(annotationID, newBody);
       
        // Add the permission (annotation_id, owner);
        int affectedPermissions = permissionsDao.addAnnotationPrincipalPermission(annotationID, userID, Permission.OWNER);
       
        Annotation newAnnotation = getAnnotation(annotationID);
        return newAnnotation;
    }
    
} 
