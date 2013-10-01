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
package eu.dasish.annotation.backend.dao.impl;

import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.SourceDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.SourceInfoList;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.PermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import eu.dasish.annotation.schema.SourceList;
import eu.dasish.annotation.schema.User;
import eu.dasish.annotation.schema.UserInfo;
import eu.dasish.annotation.schema.UserWithPermission;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author olhsha
 */
public class DBIntegrityServiceImlp implements DBIntegrityService {

    @Autowired
    UserDao userDao;
    @Autowired
    CachedRepresentationDao cachedRepresentationDao;
    @Autowired
    SourceDao sourceDao;
    @Autowired
    AnnotationDao annotationDao;
    @Autowired
    NotebookDao notebookDao;

    //////////////////////////////////
    @Override
    public void setServiceURI(String serviceURI) {
        userDao.setServiceURI(serviceURI+"users/");
        cachedRepresentationDao.setServiceURI(serviceURI+"cached/");
        sourceDao.setServiceURI(serviceURI+"sources/");
        annotationDao.setServiceURI(serviceURI+"annotations/");
        notebookDao.setServiceURI(serviceURI+"notebooks/");
    }

    ///////////// GETTERS //////////////////////////
    @Override
    public Number getAnnotationInternalIdentifier(UUID externalID) {
        return annotationDao.getInternalID(externalID);
    }

    @Override
    public UUID getAnnotationExternalIdentifier(Number annotationID) {
        return annotationDao.getExternalID(annotationID);
    }

     ///////////// GETTERS //////////////////////////
    @Override
    public Number getSourceInternalIdentifier(UUID externalID) {
        return sourceDao.getInternalID(externalID);
    }

    @Override
    public UUID getSourceExternalIdentifier(Number annotationID) {
        return sourceDao.getExternalID(annotationID);
    }
    
    @Override
    public Number getUserInternalIdentifier(UUID externalID) {
        return userDao.getInternalID(externalID);
    }

    @Override
    public UUID getUserExternalIdentifier(Number userID) {
        return userDao.getExternalID(userID);
    }

    @Override
    public Number getCachedRepresentationInternalIdentifier(UUID externalID) {
        return cachedRepresentationDao.getInternalID(externalID);
    }

    @Override
    public UUID getCachedRepresentationExternalIdentifier(Number cachedID) {
        return cachedRepresentationDao.getExternalID(cachedID);
    }

    ////////////////////////////////////////////////////////
    // TODO: refactor, source grabbing should be made a separate private method
    @Override
    public Annotation getAnnotation(Number annotationID) throws SQLException {
        Annotation result = annotationDao.getAnnotationWithoutSourcesAndPermissions(annotationID);
        if (result == null) {
            return null;
        }
        
        int userID = Integer.parseInt(result.getOwner().getRef());
        String userURI =userDao.externalIDtoURI(userDao.getExternalID(userID).toString());
        ResourceREF ownerRef = new ResourceREF();
        ownerRef.setRef(userURI);
        result.setOwner(ownerRef);
        
        List<Number> sourceIDs = annotationDao.retrieveSourceIDs(annotationID);
        SourceInfoList sis = new SourceInfoList();
        for (Number sourceID : sourceIDs) {
            Source source = sourceDao.getSource(sourceID);
            SourceInfo sourceInfo = new SourceInfo();
            sourceInfo.setLink(source.getLink());
            sourceInfo.setRef(source.getURI());
            sourceInfo.setVersion(source.getVersion());
            sis.getTargetSource().add(sourceInfo);
        }
        result.setTargetSources(sis);

        result.setPermissions(getPermissionsForAnnotation(annotationID));
        return result;
    }

    ///////////////////////////////////////////////////
    // TODO UNIT tests
    @Override
    public PermissionList getPermissionsForAnnotation(Number annotationID) throws SQLException {
        List<Map<Number, String>> principalsPermissions = annotationDao.getPermissions(annotationID);
        PermissionList result = new PermissionList();
        List<UserWithPermission> list = result.getUser();
        for (Map<Number, String> principalPermission : principalsPermissions) {

            Number[] principal = new Number[1];
            principalPermission.keySet().toArray(principal);

            UserWithPermission userWithPermission = new UserWithPermission();
            userWithPermission.setRef(userDao.externalIDtoURI(userDao.getExternalID(principal[0]).toString()));
            userWithPermission.setPermission(Permission.fromValue(principalPermission.get(principal[0])));

            list.add(userWithPermission);
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////
    @Override
    public List<Number> getFilteredAnnotationIDs(String link, String text, String access, String namespace, UUID owner, Timestamp after, Timestamp before) {

        List<Number> annotationIDs = null;

        if (link != null) {
            List<Number> sourceIDs = sourceDao.getSourcesForLink(link);
            annotationIDs = annotationDao.retrieveAnnotationList(sourceIDs);
        }

        Number ownerID = null;
        if (owner != null) {
            ownerID = userDao.getInternalID(owner);
        }

        return annotationDao.getFilteredAnnotationIDs(annotationIDs, text, access, namespace, ownerID, after, before);
    }

    @Override
    public SourceList getAnnotationSources(Number annotationID) throws SQLException {
        SourceList result = new SourceList();
        List<Number> sourceIDs = annotationDao.retrieveSourceIDs(annotationID);
        for (Number sourceID : sourceIDs) {            
            ResourceREF ref = new ResourceREF();
            ref.setRef(sourceDao.externalIDtoURI(sourceDao.getExternalID(sourceID).toString()));
            result.getTargetSource().add(ref);
        }
        return result;
    }

    @Override
    public List<Number> getSourcesWithNoCachedRepresentation(Number annotationID) {
        if (annotationID == null) {
            return null;
        }
        List<Number> result = new ArrayList<Number>();
        List<Number> sourceIDs = annotationDao.retrieveSourceIDs(annotationID);
        for (Number sourceID : sourceIDs) {
            List<Number> versions = sourceDao.getCachedRepresentations(sourceID);
            if (versions == null) {
                result.add(sourceID);
            } else {
                if (versions.isEmpty()) {
                    result.add(sourceID);
                }

            }
        }
        return result;
    }

    @Override
    public AnnotationInfoList getFilteredAnnotationInfos(String link, String text, String access, String namespace, UUID owner, Timestamp after, Timestamp before)
    throws SQLException{
        List<Number> annotationIDs = getFilteredAnnotationIDs(link, text, access, namespace, owner, after, before);
        AnnotationInfoList result = new AnnotationInfoList();
        for (Number annotationID :annotationIDs) {
            AnnotationInfo annotationInfo = annotationDao.getAnnotationInfoWithoutSources(annotationID);
            SourceList targetSources = getAnnotationSources(annotationID);
            annotationInfo.setTargetSources(targetSources);
            result.getAnnotationInfo().add(annotationInfo);
             
//          refactor: push the work on userID's below to DAO for user when retrieving the user-ids
            ResourceREF ownerExt = new ResourceREF();
            String internalIDstring = annotationInfo.getOwner().getRef();
            Number internalID = Integer.parseInt(internalIDstring);
            String externaID = userDao.getExternalID(internalID).toString();
            String uri = userDao.externalIDtoURI(externaID);
            ownerExt.setRef(uri);
            annotationInfo.setOwner(ownerExt);
        }
        
        return result;
    }

 
      // TODO unit test
    @Override
    public Source getSource(Number internalID) throws SQLException{
        Source result = sourceDao.getSource(internalID);
        result.setVersionsSiblings(getSiblingSources(internalID));
        return result;
    }

    // TODO unit test
    @Override
    public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID) {
        return cachedRepresentationDao.getCachedRepresentationInfo(internalID);
    }

    
    //TODO unit test
    @Override
    public Blob getCachedRepresentationBlob(Number cachedID) throws SQLException {
        return cachedRepresentationDao.getCachedRepresentationBlob(cachedID);
    }
    
    @Override
    public ReferenceList getSiblingSources(Number sourceID) throws SQLException {
        List<Number> sourceIDs = sourceDao.getSiblingSources(sourceID);
        ReferenceList referenceList = new ReferenceList();
        for (Number siblingID: sourceIDs) {
            referenceList.getRef().add(sourceDao.externalIDtoURI(sourceDao.getExternalID(siblingID).toString()));
        }
        return referenceList;
    }
    
    @Override
    public User getUser(Number userID){
        return userDao.getUser(userID);
    }
            
    @Override
    public User getUserByInfo(String eMail){
       return userDao.getUserByInfo(eMail);
    }
    
    @Override
    public UserInfo getUserInfo(Number userID){
        return userDao.getUserInfo(userID);
    }
    
    @Override
    public Permission  getPermission(Number annotationID, Number userID){
        return annotationDao.getPermission(annotationID, userID);
    }
    
    ///// UPDATERS /////////////////
    
    @Override
    public int updateSiblingSourceClassForSource(Number sourceID, Number siblingSourceID) throws SQLException {
        
        Integer classIDsibling = sourceDao.getSourceSiblingClass(siblingSourceID);
        if (classIDsibling == null) {
             return 0; 
        }
        return sourceDao.updateSiblingClass(sourceID, classIDsibling);
        
    }

    @Override
    public int updateAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) throws SQLException{
        return annotationDao.addAnnotationPrincipalPermission(annotationID, userID, permission);
    }
    
    @Override
    public int updatePermissions(Number annotationID, PermissionList permissionList) throws SQLException{
        List<UserWithPermission> usersWithPermissions = permissionList.getUser();
        int result = 0;
        for (UserWithPermission userWithPermission: usersWithPermissions){
            Number userID = userDao.getInternalID(UUID.fromString(userDao.stringURItoExternalID(userWithPermission.getRef())));
            Permission permission = userWithPermission.getPermission();
            int current;
            Permission currentPermission= annotationDao.getPermission(annotationID, userID);
            if (currentPermission != null) {
                if (!permission.value().equals(currentPermission.value())){
                    result = result + annotationDao.updateAnnotationPrincipalPermission(annotationID, userID, permission);
                }
            }
            else {
                result = result + annotationDao.addAnnotationPrincipalPermission(annotationID, userID, permission);
            }
        }
        
        return result;
    }
    
    /////////////// ADDERS  /////////////////////////////////
    @Override
    public Number[] addCachedForSource(Number sourceID, CachedRepresentationInfo cachedInfo, Blob cachedBlob) throws SQLException {
        Number[] result = new Number[2];
        result[1] = cachedRepresentationDao.getInternalIDFromURI(cachedInfo.getRef());
        if (result[1] == null) {
            result[1] = cachedRepresentationDao.addCachedRepresentation(cachedInfo, cachedBlob);
        }
        result[0] = sourceDao.addSourceCachedRepresentation(sourceID, result[1]);
        return result;

    }


    // TODo: mapping uri to external ID
    @Override
    public Map<String, String> addSourcesForAnnotation(Number annotationID, List<SourceInfo> sources) throws SQLException {
        Map<String, String> result = new HashMap<String, String>();
        Number sourceIDRunner;
        for (SourceInfo sourceInfo : sources) {
            sourceIDRunner = sourceDao.getInternalIDFromURI(sourceInfo.getRef());
            if (sourceIDRunner != null) {
                int affectedRows = annotationDao.addAnnotationSource(annotationID, sourceIDRunner);
            } else {
                Source newSource = createFreshSource(sourceInfo);
                Number sourceID = sourceDao.addSource(newSource);
                String sourceTemporaryID = sourceDao.stringURItoExternalID(sourceInfo.getRef());
                result.put(sourceTemporaryID, sourceDao.getExternalID(sourceID).toString());
                int affectedRows = annotationDao.addAnnotationSource(annotationID, sourceID);
            }
        }
        return result;
    }

    @Override
    public Number addUsersAnnotation(Number userID, Annotation annotation) throws SQLException {
        Number annotationID = annotationDao.addAnnotation(annotation, userID);
        int affectedAnnotRows = addSources(annotation, annotationID);
        int affectedPermissions = annotationDao.addAnnotationPrincipalPermission(annotationID, userID, Permission.OWNER);
        return annotationID;
    }

    // TODO: unit test
    @Override
    public Number updateUsersAnnotation(Number userID, Annotation annotation) throws SQLException {
        Number annotationID = annotationDao.updateAnnotation(annotation, userID);
        int affectedAnnotRows = addSources(annotation, annotationID);
        int affectedPermissions = annotationDao.addAnnotationPrincipalPermission(annotationID, userID, Permission.OWNER);
        return annotationID;
    }

    @Override
    public Number addUser(User user, String remoteID) {
        if (userDao.userExists(user)) {
            return null;
        } else {
            return userDao.addUser(user, remoteID);
        }
    }

    ////////////// DELETERS //////////////////
    @Override
    public int deleteUser(Number userID) {
        return userDao.deleteUser(userID);
    }

    @Override
    public int[] deleteCachedRepresentationOfSource(Number versionID, Number cachedID) throws SQLException{
        int[] result = new int[2];
        result[0] = sourceDao.deleteSourceCachedRepresentation(versionID, cachedID);
        if (result[0] > 0) {
            result[1] = cachedRepresentationDao.deleteCachedRepresentation(cachedID);
        } else {
            result[1] = 0;

        }
        return result;
    }

  
    @Override
    public int[] deleteAllCachedRepresentationsOfSource(Number sourceID) throws SQLException{
        int[] result = new int[2];
        result[0]=0;
        result[1]=0;
        List<Number> cachedIDs = sourceDao.getCachedRepresentations(sourceID);
        for (Number cachedID: cachedIDs) {
            int[] currentResult = deleteCachedRepresentationOfSource(sourceID, cachedID);
            result[0] = result[0] + currentResult[0];
            result[1] = result[1] + currentResult[1];
        }
        return result;
    }


    @Override
    public int[] deleteAnnotation(Number annotationID) throws SQLException {
        int[] result = new int[4];
        result[1] = annotationDao.deleteAnnotationPrincipalPermissions(annotationID);
        List<Number> sourceIDs = annotationDao.retrieveSourceIDs(annotationID);
        result[2] = annotationDao.deleteAllAnnotationSource(annotationID);
        result[0] = annotationDao.deleteAnnotation(annotationID);
        result[3] = 0;
        for (Number sourceID : sourceIDs) {
            deleteAllCachedRepresentationsOfSource(sourceID);            
            result[3] = result[3] + sourceDao.deleteSource(sourceID);
        }
        return result;
    }

    ////////////// HELPERS ////////////////////
    private Source createFreshSource(SourceInfo sourceInfo) {
        Source source = new Source();
        source.setLink(sourceInfo.getLink());
        source.setVersion(sourceInfo.getVersion());
        return source;
    }

   

    private int addSources(Annotation annotation, Number annotationID) throws SQLException {
        List<SourceInfo> sources = annotation.getTargetSources().getTargetSource();
        Map<String, String> sourceIdPairs = addSourcesForAnnotation(annotationID, sources);

        String bodyText = annotation.getBody().getValue();
        String newBody = Helpers.replace(bodyText, sourceIdPairs);
        return annotationDao.updateBodyText(annotationID, newBody);
    }
}
