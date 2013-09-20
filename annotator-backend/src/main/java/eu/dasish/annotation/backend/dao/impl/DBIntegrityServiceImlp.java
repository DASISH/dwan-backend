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
import eu.dasish.annotation.backend.dao.VersionDao;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.SourceInfoList;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.PermissionList;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import eu.dasish.annotation.schema.UserWithPermission;
import eu.dasish.annotation.schema.Version;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
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
    VersionDao versionDao;
    @Autowired
    SourceDao sourceDao;
    @Autowired
    AnnotationDao annotationDao;
    @Autowired
    NotebookDao notebookDao;

    //////////////////////////////////
    @Override
    public void setServiceURI(String serviceURI) {
        userDao.setServiceURI(serviceURI);
        cachedRepresentationDao.setServiceURI(serviceURI);
        versionDao.setServiceURI(serviceURI);
        sourceDao.setServiceURI(serviceURI);
        annotationDao.setServiceURI(serviceURI);
        notebookDao.setServiceURI(serviceURI);
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

    @Override
    public Number getUserInternalIdentifier(UUID externalID) {
        return userDao.getInternalID(externalID);
    }

    @Override
    public UUID getUserExternalIdentifier(Number userID) {
        return userDao.getExternalID(userID);
    }

    ////////////////////////////////////////////////////////
    @Override
    public Annotation getAnnotation(Number annotationID) throws SQLException {
        Annotation result = annotationDao.getAnnotationWithoutSourcesAndPermissions(annotationID);
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
    
    private PermissionList getPermissionsForAnnotation(Number annotationID) throws SQLException {
        List<Map<Number, String>> principalsPermissions = annotationDao.retrievePermissions(annotationID);
        PermissionList result  = new PermissionList();
        List<UserWithPermission> list = result.getUser();
        for (Map<Number, String> principalPermission: principalsPermissions) {
            
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

    /////////////// ADDERS  /////////////////////////////////
    @Override
    public Number[] addCachedForVersion(Number versionID, CachedRepresentationInfo cachedInfo, Blob cachedBlob) {
        Number[] result = new Number[2];
        result[1] = cachedRepresentationDao.getInternalIDFromURI(cachedInfo.getRef());
        if (result[1] == null) {
            result[1] = cachedRepresentationDao.addCachedRepresentation(cachedInfo, cachedBlob);
        }
        result[0] = versionDao.addVersionCachedRepresentation(versionID, result[1]);
        return result;

    }

   
    @Override
    public Number[] addSiblingVersionForSource(Number sourceID, Version version) throws SQLException {
        Number[] result = new Number[2];
        result[1] = versionDao.getInternalIDFromURI(version.getURI());
        if (result[1] == null) {
            result[1] = versionDao.addVersion(version);
        }
        result[0] = sourceDao.addSourceVersion(sourceID, result[1]);
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
                Version newVersion = createFreshVersion(sourceInfo);
                Number sourceID = sourceDao.addSource(newSource);
                Number[] intermediateResult = addSiblingVersionForSource(sourceID, newVersion);
                String sourceTemporaryID = sourceDao.stringURItoExternalID(sourceInfo.getRef());
                result.put(sourceTemporaryID, sourceDao.getExternalID(sourceID).toString());
                int affectedRows = annotationDao.addAnnotationSource(annotationID, sourceID);
            }
        }
        return result;
    }


    @Override
    public Number addUsersAnnotation(Annotation annotation, Number userID) throws SQLException {

        Number annotationID = annotationDao.addAnnotation(annotation, userID);

        List<SourceInfo> sources = annotation.getTargetSources().getTargetSource();
        Map<String, String> sourceIdPairs = addSourcesForAnnotation(annotationID, sources);

        String bodyText = annotation.getBody().getValue();
        String newBody = Helpers.replace(bodyText, sourceIdPairs);
        int affectedAnnotRows = annotationDao.updateBodyText(annotationID, newBody);

        // Add the permission (annotation_id, owner);
        int affectedPermissions = annotationDao.addAnnotationPrincipalPermission(annotationID, userID, Permission.OWNER);

        return annotationID;
    }

    ////////////// DELETERS //////////////////
    @Override
    public int[] deleteCachedOfVersion(Number versionID, Number cachedID) {
        int[] result = new int[2];
        result[0] = versionDao.deleteVersionCachedRepresentation(versionID, cachedID);
        if (result[0] > 0) {
            result[1] = cachedRepresentationDao.deleteCachedRepresentation(cachedID);
        } else {
            result[1] = 0;

        }
        return result;
    }

    @Override
    public int[] deleteAllCachedOfVersion(Number versionID) {
        int[] result = new int[3];
        if (!versionDao.versionIsInUse(versionID)) {
            List<Number> cachedRepresentations = versionDao.retrieveCachedRepresentationList(versionID);
            result[1] = versionDao.deleteAllVersionCachedRepresentation(versionID);
            result[0] = versionDao.deleteVersion(versionID);
            result[2] = 0;
            for (Number cachedID : cachedRepresentations) {
                result[2] = result[2] + cachedRepresentationDao.deleteCachedRepresentation(cachedID);

            }
        } else {
            result[0] = 0;
            result[1] = 0;
            result[2] = 0;
        }
        return result;
    }

    @Override
    public int[] deleteAllVersionsOfSource(Number sourceID) throws SQLException {
        int[] result = new int[3];
        if (!sourceDao.sourceIsInUse(sourceID)) {
            List<Number> versions = sourceDao.retrieveVersionList(sourceID);
            result[1] = sourceDao.deleteAllSourceVersion(sourceID);
            result[0] = sourceDao.deleteSource(sourceID);
            result[2] = 0;
            for (Number versionID : versions) {
                int[] deleteVersion = deleteAllCachedOfVersion(versionID);
                result[2] = result[2] + deleteVersion[0];
            }
        } else {
            result[0] = 0;
            result[1] = 0;
            result[2] = 0;
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
            int[] deleteSource = deleteAllVersionsOfSource(sourceID);
            result[3] = result[3] + deleteSource[1];
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

    /////////////////////////////////////////
    private Version createFreshVersion(SourceInfo sourceInfo) {
        Version version = new Version();
        version.setVersion(sourceInfo.getVersion());
        return version;
    }
}
