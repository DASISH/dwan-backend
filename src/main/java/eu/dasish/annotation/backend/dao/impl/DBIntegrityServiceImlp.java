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
import eu.dasish.annotation.backend.dao.TargetDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationFragment;
import eu.dasish.annotation.schema.CachedRepresentationFragmentList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.TargetInfoList;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.UserWithPermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.User;
import eu.dasish.annotation.schema.UserWithPermission;
import java.io.InputStream;
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
    TargetDao targetDao;
    @Autowired
    AnnotationDao annotationDao;

    //////////////////////////////////
    @Override
    public void setServiceURI(String serviceURI) {
        userDao.setServiceURI(serviceURI + "users/");
        cachedRepresentationDao.setServiceURI(serviceURI + "cached/");
        targetDao.setServiceURI(serviceURI + "targets/");
        annotationDao.setServiceURI(serviceURI + "annotations/");
        //notebookDao.setServiceURI(serviceURI+"notebooks/");
    }

    ///////////// GETTERS //////////////////////////
    @Override
    public Number getAnnotationInternalIdentifier(UUID externalID) {
        return annotationDao.getInternalID(externalID);
    }
    
    @Override
    public Number getAnnotationInternalIdentifierFromURI(String uri) {
        return annotationDao.getInternalIDFromURI(uri);
    }

    @Override
    public UUID getAnnotationExternalIdentifier(Number annotationID) {
        return annotationDao.getExternalID(annotationID);
    }

    ///////////// GETTERS //////////////////////////
    @Override
    public Number getTargetInternalIdentifier(UUID externalID) {
        return targetDao.getInternalID(externalID);
    }

    @Override
    public UUID getTargetExternalIdentifier(Number annotationID) {
        return targetDao.getExternalID(annotationID);
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
    // TODO: refactor, Target grabbing should be made a separate private method
    @Override
    public Annotation getAnnotation(Number annotationID) throws SQLException {
        Annotation result = annotationDao.getAnnotationWithoutTargetsAndPermissions(annotationID);
        if (result == null) {
            return null;
        }

        int userID = Integer.parseInt(result.getOwnerRef());
        String userURI = userDao.externalIDtoURI(userDao.getExternalID(userID).toString());
        result.setOwnerRef(userURI);

        List<Number> targetIDs = annotationDao.retrieveTargetIDs(annotationID);
        TargetInfoList sis = new TargetInfoList();
        for (Number targetID : targetIDs) {
            Target target = targetDao.getTarget(targetID);
            TargetInfo targetInfo = new TargetInfo();
            targetInfo.setLink(target.getLink());
            targetInfo.setRef(target.getURI());
            targetInfo.setVersion(target.getVersion());
            sis.getTargetInfo().add(targetInfo);
        }
        result.setTargets(sis);

        result.setPermissions(getPermissionsForAnnotation(annotationID));
        return result;
    }

    ///////////////////////////////////////////////////
    // TODO UNIT tests
    @Override
    public UserWithPermissionList getPermissionsForAnnotation(Number annotationID) throws SQLException {
        List<Map<Number, String>> principalsPermissions = annotationDao.getPermissions(annotationID);
        UserWithPermissionList result = new UserWithPermissionList();
        List<UserWithPermission> list = result.getUserWithPermission();
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
    public List<Number> getFilteredAnnotationIDs(String word, String text, String access, String namespace, UUID owner, Timestamp after, Timestamp before) {

        List<Number> annotationIDs = null;

        if (word != null) {
            List<Number> targetIDs = targetDao.getTargetsReferringTo(word);
            annotationIDs = annotationDao.retrieveAnnotationList(targetIDs);
        }

        Number ownerID = null;
        if (owner != null) {
            ownerID = userDao.getInternalID(owner);
        }

        return annotationDao.getFilteredAnnotationIDs(annotationIDs, text, access, namespace, ownerID, after, before);
    }

    @Override
    public ReferenceList getAnnotationTargets(Number annotationID) throws SQLException {
        ReferenceList result = new ReferenceList();
        List<Number> targetIDs = annotationDao.retrieveTargetIDs(annotationID);
        for (Number targetID : targetIDs) {
            result.getRef().add(targetDao.externalIDtoURI(targetDao.getExternalID(targetID).toString()));
        }
        return result;
    }

    @Override
    public List<Number> getTargetsWithNoCachedRepresentation(Number annotationID) {
        if (annotationID == null) {
            return null;
        }
        List<Number> result = new ArrayList<Number>();
        List<Number> targetIDs = annotationDao.retrieveTargetIDs(annotationID);
        for (Number targetID : targetIDs) {
            List<Number> versions = targetDao.getCachedRepresentations(targetID);
            if (versions == null) {
                result.add(targetID);
            } else {
                if (versions.isEmpty()) {
                    result.add(targetID);
                }

            }
        }
        return result;
    }

    @Override
    public AnnotationInfoList getFilteredAnnotationInfos(String word, String text, String access, String namespace, UUID owner, Timestamp after, Timestamp before)
            throws SQLException {
        List<Number> annotationIDs = getFilteredAnnotationIDs(word, text, access, namespace, owner, after, before);
        AnnotationInfoList result = new AnnotationInfoList();
        for (Number annotationID : annotationIDs) {
            AnnotationInfo annotationInfo = annotationDao.getAnnotationInfoWithoutTargets(annotationID);
            ReferenceList targets = getAnnotationTargets(annotationID);
            annotationInfo.setTargets(targets);
            result.getAnnotationInfo().add(annotationInfo);

//          refactor: push the work on userID's below to DAO for user when retrieving the user-ids
            String internalIDstring = annotationInfo.getOwnerRef();
            Number internalID = Integer.parseInt(internalIDstring);
            String externaID = userDao.getExternalID(internalID).toString();
            annotationInfo.setOwnerRef(userDao.externalIDtoURI(externaID));
        }

        return result;
    }

    // TODO unit test
    @Override
    public Target getTarget(Number internalID) throws SQLException {
        Target result = targetDao.getTarget(internalID);
        result.setSiblingTargets(getTargetsForTheSameLinkAs(internalID));
        Map<Number, String> cachedIDsFragments = targetDao.getCachedRepresentationFragmentPairs(internalID);
        CachedRepresentationFragmentList cachedRepresentationFragmentList = new CachedRepresentationFragmentList();
        for (Number key: cachedIDsFragments.keySet()) {
            CachedRepresentationFragment cachedRepresentationFragment = new CachedRepresentationFragment();
            cachedRepresentationFragment.setRef(cachedRepresentationDao.getURIFromInternalID(key));
            cachedRepresentationFragment.setFragmentString(cachedIDsFragments.get(key));
            cachedRepresentationFragmentList.getCached().add(cachedRepresentationFragment);
        }
        result.setCachedRepresentatinons(cachedRepresentationFragmentList);
        return result;
    }
    
    

    // TODO unit test
    @Override
    public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID) {
        return cachedRepresentationDao.getCachedRepresentationInfo(internalID);
    }

    //TODO unit test
    @Override
    public InputStream getCachedRepresentationBlob(Number cachedID) throws SQLException {
        return cachedRepresentationDao.getCachedRepresentationBlob(cachedID);
    }

    @Override
    public ReferenceList getTargetsForTheSameLinkAs(Number targetID) throws SQLException {
        List<Number> targetIDs = targetDao.getTargetsForLink(targetDao.getLink(targetID));
        ReferenceList referenceList = new ReferenceList();
        for (Number siblingID : targetIDs) {
            referenceList.getRef().add(targetDao.externalIDtoURI(targetDao.getExternalID(siblingID).toString()));
        }
        return referenceList;
    }

    @Override
    public User getUser(Number userID) {
        return userDao.getUser(userID);
    }

    @Override
    public User getUserByInfo(String eMail) {
        return userDao.getUserByInfo(eMail);
    }

    @Override
    public Permission getPermission(Number annotationID, Number userID) {
        return annotationDao.getPermission(annotationID, userID);
    }

    ///// UPDATERS /////////////////
    @Override
    public int updateAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) throws SQLException {
        return annotationDao.addAnnotationPrincipalPermission(annotationID, userID, permission);
    }

    @Override
    public int updatePermissions(Number annotationID, UserWithPermissionList permissionList) throws SQLException {
        List<UserWithPermission> usersWithPermissions = permissionList.getUserWithPermission();
        int result = 0;
        for (UserWithPermission userWithPermission : usersWithPermissions) {
            Number userID = userDao.getInternalID(UUID.fromString(userDao.stringURItoExternalID(userWithPermission.getRef())));
            Permission permission = userWithPermission.getPermission();
            int current;
            Permission currentPermission = annotationDao.getPermission(annotationID, userID);
            if (currentPermission != null) {
                if (!permission.value().equals(currentPermission.value())) {
                    result = result + annotationDao.updateAnnotationPrincipalPermission(annotationID, userID, permission);
                }
            } else {
                result = result + annotationDao.addAnnotationPrincipalPermission(annotationID, userID, permission);
            }
        }

        return result;
    }

    /////////////// ADDERS  /////////////////////////////////
    @Override
    public Number[] addCachedForTarget(Number targetID, CachedRepresentationInfo cachedInfo, Blob cachedBlob) throws SQLException {
        Number[] result = new Number[2];
        result[1] = cachedRepresentationDao.getInternalIDFromURI(cachedInfo.getURI());
        if (result[1] == null) {
            result[1] = cachedRepresentationDao.addCachedRepresentation(cachedInfo, cachedBlob);
        }
        result[0] = targetDao.addTargetCachedRepresentation(targetID, result[1]);
        return result;

    }

    // TODo: mapping uri to external ID
    @Override
    public Map<String, String> addTargetsForAnnotation(Number annotationID, List<TargetInfo> Targets) throws SQLException {
        Map<String, String> result = new HashMap<String, String>();
        Number targetIDRunner;
        for (TargetInfo targetInfo : Targets) {
            targetIDRunner = targetDao.getInternalIDFromURI(targetInfo.getRef());
            if (targetIDRunner != null) {
                int affectedRows = annotationDao.addAnnotationTarget(annotationID, targetIDRunner);
            } else {
                Target newTarget = createFreshTarget(targetInfo);
                Number targetID = targetDao.addTarget(newTarget);
                String targetTemporaryID = targetDao.stringURItoExternalID(targetInfo.getRef());
                result.put(targetTemporaryID, targetDao.getExternalID(targetID).toString());
                int affectedRows = annotationDao.addAnnotationTarget(annotationID, targetID);
            }
        }
        return result;
    }

    @Override
    public Number addUsersAnnotation(Number userID, Annotation annotation) throws SQLException, Exception {
        Number annotationID = annotationDao.addAnnotation(annotation, userID);
        int affectedAnnotRows = addTargets(annotation, annotationID);
        int affectedPermissions = annotationDao.addAnnotationPrincipalPermission(annotationID, userID, Permission.OWNER);
        return annotationID;
    }

    // TODO: optimize (not chnaged targets should not be deleted)
    // TODO: unit test
    @Override
    public int updateUsersAnnotation(Number userID, Annotation annotation) throws SQLException, Exception {
        int updatedAnnotations = annotationDao.updateAnnotation(annotation, userID);
        Number annotationID = annotationDao.getInternalIDFromURI(annotation.getURI());
        int deletedTargets = annotationDao.deleteAllAnnotationTarget(annotationID);
        int deletedPrinsipalsPermissions = annotationDao.deleteAnnotationPrincipalPermissions(annotationID);
        int addedTargets = addTargets(annotation, annotationID);
        int addedPrincipalsPErmissions = addPrincipalsPermissions(annotation, annotationID);
        return updatedAnnotations;
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
    public int[] deleteCachedRepresentationOfTarget(Number versionID, Number cachedID) throws SQLException {
        int[] result = new int[2];
        result[0] = targetDao.deleteTargetCachedRepresentation(versionID, cachedID);
        if (result[0] > 0) {
            result[1] = cachedRepresentationDao.deleteCachedRepresentation(cachedID);
        } else {
            result[1] = 0;

        }
        return result;
    }

    @Override
    public int[] deleteAllCachedRepresentationsOfTarget(Number TargetID) throws SQLException {
        int[] result = new int[2];
        result[0] = 0;
        result[1] = 0;
        List<Number> cachedIDs = targetDao.getCachedRepresentations(TargetID);
        for (Number cachedID : cachedIDs) {
            int[] currentResult = deleteCachedRepresentationOfTarget(TargetID, cachedID);
            result[0] = result[0] + currentResult[0];
            result[1] = result[1] + currentResult[1];
        }
        return result;
    }

    @Override
    public int[] deleteAnnotation(Number annotationID) throws SQLException {
        int[] result = new int[4];
        result[1] = annotationDao.deleteAnnotationPrincipalPermissions(annotationID);
        List<Number> TargetIDs = annotationDao.retrieveTargetIDs(annotationID);
        result[2] = annotationDao.deleteAllAnnotationTarget(annotationID);
        result[0] = annotationDao.deleteAnnotation(annotationID);
        result[3] = 0;
        for (Number TargetID : TargetIDs) {
            deleteAllCachedRepresentationsOfTarget(TargetID);
            result[3] = result[3] + targetDao.deleteTarget(TargetID);
        }
        return result;
    }

    ////////////// HELPERS ////////////////////
    private Target createFreshTarget(TargetInfo targetInfo) {
        Target target = new Target();
        target.setLink(targetInfo.getLink());
        target.setVersion(targetInfo.getVersion());
        return target;
    }

    private int addTargets(Annotation annotation, Number annotationID) throws SQLException, Exception {
        List<TargetInfo> targets = annotation.getTargets().getTargetInfo();
        Map<String, String> targetIdPairs = addTargetsForAnnotation(annotationID, targets);
        String bodyText;
        String newBody;
        if (annotation.getBody().getXmlBody() != null) {
            bodyText = Helpers.elementToString(annotation.getBody().getXmlBody().getAny());            
        } else {
            if (annotation.getBody().getTextBody() != null) {
                bodyText = annotation.getBody().getTextBody().getValue();
                
            }
            else {
                throw new Exception(Helpers.INVALID_BODY_EXCEPTION);
            }
        }
         newBody = Helpers.replace(bodyText, targetIdPairs);
        return annotationDao.updateBodyText(annotationID, newBody);
    }

    private int addPrincipalsPermissions(Annotation annotation, Number annotationID) throws SQLException {
        List<UserWithPermission> permissions = annotation.getPermissions().getUserWithPermission();
        int addedPermissions =0;
        for (UserWithPermission permission:permissions) {
            addedPermissions = addedPermissions + annotationDao.addAnnotationPrincipalPermission(annotationID, userDao.getInternalIDFromURI(permission.getRef()), permission.getPermission());
        
        }
        return addedPermissions;
    }
}
