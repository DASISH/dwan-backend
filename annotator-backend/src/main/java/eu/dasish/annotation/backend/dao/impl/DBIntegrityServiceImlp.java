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
import eu.dasish.annotation.backend.dao.TargetDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.backend.rest.AnnotationResource;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationFragment;
import eu.dasish.annotation.schema.CachedRepresentationFragmentList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.NotebookInfoList;
import eu.dasish.annotation.schema.TargetInfoList;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.UserWithPermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.User;
import eu.dasish.annotation.schema.UserWithPermission;
import java.io.InputStream;
import java.lang.Number;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @Autowired
    NotebookDao notebookDao;
    final static protected String admin = "admin";
    private static final Logger logger = LoggerFactory.getLogger(AnnotationResource.class);
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

    @Override
    public Number getTargetInternalIdentifier(UUID externalID) {
        return targetDao.getInternalID(externalID);
    }

    @Override
    public UUID getTargetExternalIdentifier(Number targetID) {
        return targetDao.getExternalID(targetID);
    }

    @Override
    public String getTargetURI(Number targetID) {
        return targetDao.getURIFromInternalID(targetID);
    }

    @Override
    public String getUserURI(Number userID) {
        return userDao.getURIFromInternalID(userID);
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

    @Override
    public Annotation getAnnotation(Number annotationID) {
        if (annotationID != null) {
            Annotation result = annotationDao.getAnnotationWithoutTargetsAndPermissions(annotationID);
            result.setOwnerRef(userDao.getURIFromInternalID(annotationDao.getOwner(annotationID)));
            List<Number> targetIDs = annotationDao.retrieveTargetIDs(annotationID);
            TargetInfoList sis = new TargetInfoList();
            for (Number targetID : targetIDs) {
                TargetInfo targetInfo = getTargetInfoFromTarget(targetDao.getTarget(targetID));
                sis.getTargetInfo().add(targetInfo);
            }
            result.setTargets(sis);

            result.setPermissions(this.getPermissionsForAnnotation(annotationID));
            return result;
        } else {
            return null;
        }
    }

    @Override
    public Number getAnnotationOwner(Number annotationID) {
        return annotationDao.getOwner(annotationID);
    }

    ///////////////////////////////////////////////////
    // TODO UNIT tests
    @Override
    public UserWithPermissionList getPermissionsForAnnotation(Number annotationID) {
        if (annotationID != null) {
            List<Map<Number, String>> principalsPermissions = annotationDao.getPermissions(annotationID);
            UserWithPermissionList result = new UserWithPermissionList();
            List<UserWithPermission> list = result.getUserWithPermission();
            for (Map<Number, String> principalPermission : principalsPermissions) {

                Number[] principal = new Number[1];
                principalPermission.keySet().toArray(principal);

                UserWithPermission userWithPermission = new UserWithPermission();
                userWithPermission.setRef(userDao.getURIFromInternalID(principal[0]));
                userWithPermission.setPermission(Permission.fromValue(principalPermission.get(principal[0])));

                list.add(userWithPermission);
            }
            return result;
        } else {
            return null;
        }

    }

    ////////////////////////////////////////////////////////////////////////
    @Override
    public List<Number> getFilteredAnnotationIDs(UUID ownerId, String link, String text, Number inloggedUserID, String access, String namespace, String after, String before) {

        Number ownerID = (ownerId != null) ? userDao.getInternalID(ownerId) : null;
        if (ownerID != null) {
            if ("owner".equals(access) && !inloggedUserID.equals(ownerID)) {
                logger.info("The inlogged user cannot be the owner of the annotations owned by " + ownerId.toString());
                return null;
            }
        }

        //filtering on tables "target" and "annotations_targets"
        List<Number> annotationIDsForTargets = null;
        if (link != null) {
            List<Number> targetIDs = targetDao.getTargetsReferringTo(link);
            annotationIDsForTargets = annotationDao.getAnnotationIDsForTargets(targetIDs);
            if (annotationIDsForTargets == null) {
                logger.info("There are no annotations for the targets referring to " + link + ".");
                return null;
            }
        }

        // filtering in the table "annotation"
        if (ownerID == null && "owner".equals(access)) {
            ownerID = inloggedUserID;
        }
        List<Number> annotationIDs = annotationDao.getFilteredAnnotationIDs(ownerID, text, namespace, after, before);
        if (annotationIDs != null) {
            if (annotationIDsForTargets != null) {
                annotationIDs.retainAll(annotationIDsForTargets);
            } else {
                // nothing to filter on link == null
            }
        } else {
            logger.info("There are no annotations for the given filters on the annotation table.");
            return null;
        }

        // filtering on table "annotations_principals_permissions"
        if ("reader".equals(access) || "writer".equals(access)) {
            // owner != inloggedUser
            List<Number> annotationIDsPermission = annotationDao.getAnnotationIDsForUserWithPermission(inloggedUserID, access);
            if (annotationIDsPermission != null) {
                annotationIDs.retainAll(annotationIDsPermission);
            } else {
                logger.info("There are no annotations for which the inlogged user has access " + access);
                return null;
            }
        } else {
            // inloggedUser == owner
        }
        return annotationIDs;
    }

    @Override
    public ReferenceList getAnnotationTargets(Number annotationID) {
        ReferenceList result = new ReferenceList();
        List<Number> targetIDs = annotationDao.retrieveTargetIDs(annotationID);
        for (Number targetID : targetIDs) {
            result.getRef().add(targetDao.getURIFromInternalID(targetID));
        }
        return result;
    }

    @Override
    public List<String> getTargetsWithNoCachedRepresentation(Number annotationID) {
        if (annotationID == null) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        List<Number> targetIDs = annotationDao.retrieveTargetIDs(annotationID);
        for (Number targetID : targetIDs) {
            List<Number> versions = targetDao.getCachedRepresentations(targetID);
            if (versions == null) {
                result.add(targetDao.getURIFromInternalID(targetID));
            } else {
                if (versions.isEmpty()) {
                    result.add(targetDao.getURIFromInternalID(targetID));
                }

            }
        }
        return result;
    }

    @Override
    public List<String> getUsersWithNoInfo(Number annotationID) {
        if (annotationID == null) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        List<Map<Number, String>> usersWithPermissions = annotationDao.getPermissions(annotationID);
        for (Map<Number, String> userWithPermission : usersWithPermissions) {
            Number[] userID = new Number[1];
            userWithPermission.keySet().toArray(userID);
            User user = userDao.getUser(userID[0]);

            if (user.getDisplayName() == null || user.getDisplayName().trim().isEmpty() || user.getEMail() == null || user.getEMail().trim().isEmpty()) {
                result.add(userDao.getURIFromInternalID(userID[0]));

            }
        }
        return result;
    }

    @Override
    public AnnotationInfoList getFilteredAnnotationInfos(UUID ownerId, String word, String text, Number inloggedUserID, String access, String namespace, String after, String before) {
        List<Number> annotationIDs = this.getFilteredAnnotationIDs(ownerId, word, text, inloggedUserID, access, namespace, after, before);
        if (annotationIDs != null) {
            AnnotationInfoList result = new AnnotationInfoList();
            for (Number annotationID : annotationIDs) {
                AnnotationInfo annotationInfo = annotationDao.getAnnotationInfoWithoutTargets(annotationID);
                annotationInfo.setTargets(this.getAnnotationTargets(annotationID));
                annotationInfo.setOwnerRef(userDao.getURIFromInternalID(annotationDao.getOwner(annotationID)));
                result.getAnnotationInfo().add(annotationInfo);

            }

            return result;
        } else {
            return null;
        }
    }

    @Override
    public AnnotationInfoList getAllAnnotationInfos() {
        List<Number> annotationIDs = annotationDao.getAllAnnotationIDs();
        if (annotationIDs != null) {
            AnnotationInfoList result = new AnnotationInfoList();
            for (Number annotationID : annotationIDs) {
                Number ownerID = annotationDao.getOwner(annotationID);
                ReferenceList targets = getAnnotationTargets(annotationID);
                AnnotationInfo annotationInfo = annotationDao.getAnnotationInfoWithoutTargets(annotationID);
                annotationInfo.setTargets(targets);
                if (ownerID != null) {
                    annotationInfo.setOwnerRef(userDao.getURIFromInternalID(ownerID));
                } else {
                    annotationInfo.setOwnerRef("ACHTUNG: This annotation does not have an owner in the DB!!!!");
                }
                result.getAnnotationInfo().add(annotationInfo);
            }

            return result;
        } else {
            return null;
        }
    }

    // TODO unit test
    @Override
    public Target getTarget(Number internalID) {
        Target result = targetDao.getTarget(internalID);
        result.setSiblingTargets(getTargetsForTheSameLinkAs(internalID));
        Map<Number, String> cachedIDsFragments = targetDao.getCachedRepresentationFragmentPairs(internalID);
        CachedRepresentationFragmentList cachedRepresentationFragmentList = new CachedRepresentationFragmentList();
        for (Number key : cachedIDsFragments.keySet()) {
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
    public InputStream getCachedRepresentationBlob(Number cachedID) {
        return cachedRepresentationDao.getCachedRepresentationBlob(cachedID);
    }

    @Override
    public ReferenceList getTargetsForTheSameLinkAs(Number targetID) {
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
    public String getUserRemoteID(Number internalID) {
        return userDao.getRemoteID(internalID);
    }

    @Override
    public Permission getPermission(Number annotationID, Number userID) {
        return annotationDao.getPermission(annotationID, userID);
    }

    @Override
    public Number getUserInternalIDFromRemoteID(String remoteID) {
        return userDao.getUserInternalIDFromRemoteID(remoteID);
    }

    @Override
    public String getTypeOfUserAccount(Number userID) {
        return userDao.getTypeOfUserAccount(userID);
    }

    @Override
    public boolean canRead(Number userID, Number annotationID) {
        if (userID.equals(annotationDao.getOwner(annotationID)) || userDao.getTypeOfUserAccount(userID).equals(admin)) {
            return true;
        }

        final Permission permission = annotationDao.getPermission(annotationID, userID);
        if (permission != null) {
            return (permission.value().equals(Permission.WRITER.value()) || permission.value().equals(Permission.READER.value()));
        } else {
            return false;
        }
    }

    @Override
    public boolean canWrite(Number userID, Number annotationID) {
        if (userID.equals(annotationDao.getOwner(annotationID)) || userDao.getTypeOfUserAccount(userID).equals(admin)) {
            return true;
        }
        final Permission permission = annotationDao.getPermission(annotationID, userID);
        if (permission != null) {
            return (permission.value().equals(Permission.WRITER.value()));
        } else {
            return false;
        }
    }

    /// notebooks ///
    @Override
    public NotebookInfoList getNotebooks(Number prinipalID, Permission permission) {
        NotebookInfoList result = new NotebookInfoList();
        List<Number> notebookIDs = notebookDao.getNotebookIDs(prinipalID, permission);
        for (Number notebookID : notebookIDs) {
            NotebookInfo notebookInfo = notebookDao.getNotebookInfoWithoutOwner(notebookID);
            Number ownerID = notebookDao.getOwner(notebookID);
            notebookInfo.setOwnerRef(userDao.getURIFromInternalID(ownerID));
            result.getNotebookInfo().add(notebookInfo);
        }

        return result;
    }

    @Override
    public NotebookInfoList getNotebooksOwnedBy(Number principalID) {
        NotebookInfoList result = new NotebookInfoList();
        List<Number> notebookIDs = notebookDao.getNotebookIDsOwnedBy(principalID);
        String ownerRef = userDao.getURIFromInternalID(principalID);
        for (Number notebookID : notebookIDs) {
            NotebookInfo notebookInfo = notebookDao.getNotebookInfoWithoutOwner(notebookID);
            notebookInfo.setOwnerRef(ownerRef);
            result.getNotebookInfo().add(notebookInfo);
        }

        return result;
    }

    @Override
    public List<UUID> getPrincipals(Number notebookID, Permission permission) {
        List<UUID> result = new ArrayList<UUID>();
        List<Number> principalIDs = notebookDao.getPrincipalIDsWithPermission(notebookID, permission);
        for (Number principalID : principalIDs) {
            UUID uuid = userDao.getExternalID(principalID);
            result.add(uuid);
        }
        return result;
    }

    @Override
    public NotebookInfo getNotebookInfo(Number notebookID) {
        NotebookInfo result = notebookDao.getNotebookInfoWithoutOwner(notebookID);
        result.setOwnerRef(userDao.getURIFromInternalID(notebookDao.getOwner(notebookID)));
        return result;
    }

    /////////////////////////////////////////////////////////////
    @Override
    public List<UUID> getAnnotationsForNotebook(Number notebookID, int startAnnotation, int maximumAnnotations, String orderedBy, boolean desc) {
        List<Number> annotationIDs = notebookDao.getAnnotations(notebookID);

        if (startAnnotation < -1) {
            logger.info("Variable's startAnnotation value " + startAnnotation + " is invalid. I will return null.");
            return null;
        }

        if (maximumAnnotations < -1) {
            logger.info("Variable's maximumAnnotations value " + maximumAnnotations + " is invalid. I will return null.");
            return null;
        }

        int offset = (startAnnotation > 0) ? startAnnotation - 1 : 0;
        String direction = desc ? " DESC " : " ASC ";
        List<Number> selectedAnnotIDs = annotationDao.sublistOrderedAnnotationIDs(annotationIDs, offset, maximumAnnotations, orderedBy, direction);
        List<UUID> annotationUUIDs = new ArrayList<UUID>();
        for (Number annotationID : selectedAnnotIDs) {
            annotationUUIDs.add(annotationDao.getExternalID(annotationID));
        }
        return annotationUUIDs;
    }

    ///// UPDATERS /////////////////
    @Override
    public boolean updateAccount(UUID userExternalID, String account) {
        return userDao.updateAccount(userExternalID, account);
    }

    @Override
    public int updateAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) {
        if (permission != null) {
            return annotationDao.updateAnnotationPrincipalPermission(annotationID, userID, permission);
        } else {
            return annotationDao.deleteAnnotationPrincipalPermission(annotationID, userID);
        }
    }

    @Override
    public int updatePermissions(Number annotationID, UserWithPermissionList permissionList) {

        List<UserWithPermission> usersWithPermissions = permissionList.getUserWithPermission();
        int result = 0;
        for (UserWithPermission userWithPermission : usersWithPermissions) {
            Number userID = userDao.getInternalID(UUID.fromString(userDao.stringURItoExternalID(userWithPermission.getRef())));
            if (userID != null) {
                Permission permission = userWithPermission.getPermission();
                Permission currentPermission = annotationDao.getPermission(annotationID, userID);
                if (currentPermission != null) {
                    if (!permission.value().equals(currentPermission.value())) {
                        result = result + annotationDao.updateAnnotationPrincipalPermission(annotationID, userID, permission);
                    }
                } else {
                    result = result + annotationDao.addAnnotationPrincipalPermission(annotationID, userID, permission);
                }
            }
        }

        return result;
    }

    // TODO: optimize (not chnaged targets should not be deleted)
    // TODO: unit test
    @Override
    public int updateAnnotation(Annotation annotation) {
        int updatedAnnotations = annotationDao.updateAnnotation(annotation, userDao.getInternalIDFromURI(annotation.getOwnerRef()));
        Number annotationID = annotationDao.getInternalIDFromURI(annotation.getURI());
        int deletedTargets = annotationDao.deleteAllAnnotationTarget(annotationID);
        int deletedPrinsipalsPermissions = annotationDao.deleteAnnotationPrincipalPermissions(annotationID);
        int addedTargets = addTargets(annotation, annotationID);
        int addedPrincipalsPermissions = addPrincipalsPermissions(annotation.getPermissions().getUserWithPermission(), annotationID);
        return updatedAnnotations;
    }

    // TODO: unit test
    @Override
    public int updateAnnotationBody(Number internalID, AnnotationBody annotationBody) {
        String[] body = annotationDao.retrieveBodyComponents(annotationBody);
        return annotationDao.updateAnnotationBody(internalID, body[0], body[1], annotationBody.getXmlBody() != null);
    }

    @Override
    public Number updateUser(User user) {
        return userDao.updateUser(user);
    }
    /// notebooks ///

    @Override
    public boolean updateNotebookMetadata(Number notebookID, NotebookInfo upToDateNotebookInfo) {
        Number ownerID = userDao.getInternalIDFromURI(upToDateNotebookInfo.getOwnerRef());
        return notebookDao.updateNotebookMetadata(notebookID, upToDateNotebookInfo.getTitle(), ownerID);
    }

    @Override
    public boolean addAnnotationToNotebook(Number notebookID, Number annotationID) {
        return notebookDao.addAnnotationToNotebook(notebookID, annotationID);
    }

    /////////////// ADDERS  /////////////////////////////////
    @Override
    public Number[] addCachedForTarget(Number targetID, String fragmentDescriptor, CachedRepresentationInfo cachedInfo, InputStream cachedBlob) {
        Number[] result = new Number[2];
        result[1] = cachedRepresentationDao.getInternalIDFromURI(cachedInfo.getURI());
        if (result[1] == null) {
            result[1] = cachedRepresentationDao.addCachedRepresentation(cachedInfo, cachedBlob);
        }
        result[0] = targetDao.addTargetCachedRepresentation(targetID, result[1], fragmentDescriptor);
        return result;

    }

    // TODo: mapping uri to external ID
    @Override
    public Map<String, String> addTargetsForAnnotation(Number annotationID, List<TargetInfo> targets) {
        Map<String, String> result = new HashMap<String, String>();
        Number targetIDRunner;
        for (TargetInfo targetInfo : targets) {
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
    public Number addUsersAnnotation(Number ownerID, Annotation annotation) {
        Number annotationID = annotationDao.addAnnotation(annotation, ownerID);
        int affectedAnnotRows = addTargets(annotation, annotationID);
        if (annotation.getPermissions() != null) {
            if (annotation.getPermissions().getUserWithPermission() != null) {
                int addedPrincipalsPermissions = this.addPrincipalsPermissions(annotation.getPermissions().getUserWithPermission(), annotationID);
            }
        }
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

    @Override
    public int addAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) {
        return annotationDao.addAnnotationPrincipalPermission(annotationID, userID, permission);
    }
    
    //////////// notebooks //////

    @Override
    public Number createNotebook(Notebook notebook, Number ownerID) {
        Number notebookID = notebookDao.createNotebookWithoutPermissionsAndAnnotations(notebook, ownerID);
        boolean updateOwner = notebookDao.setOwner(notebookID, ownerID);
        List<UserWithPermission> permissions = notebook.getPermissions().getUserWithPermission();
        for (UserWithPermission principalPermission : permissions) {
            Number principalID = userDao.getInternalIDFromURI(principalPermission.getRef());
            Permission permission = principalPermission.getPermission();
            boolean updatePermissions = notebookDao.addPermissionToNotebook(notebookID, principalID, permission);
        }
        return notebookID;
    }

    @Override
    public boolean createAnnotationInNotebook(Number notebookID, Annotation annotation, Number ownerID) {
        Number newAnnotationID = this.addUsersAnnotation(ownerID, annotation);
        return notebookDao.addAnnotationToNotebook(notebookID, newAnnotationID);
    }

    ////////////// DELETERS //////////////////
    @Override
    public int deleteUser(Number userID) {
        return userDao.deleteUser(userID);
    }

    ////////////// DELETERS //////////////////
    @Override
    public int deleteUserSafe(Number userID) {
        return userDao.deleteUserSafe(userID);
    }

    @Override
    public int[] deleteCachedRepresentationOfTarget(Number versionID, Number cachedID) {
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
    public int[] deleteAllCachedRepresentationsOfTarget(Number TargetID) {
        int[] result = new int[2];
        result[0] = 0;
        result[1] = 0;
        List<Number> cachedIDs = targetDao.getCachedRepresentations(TargetID);
        for (Number cachedID : cachedIDs) {
            int[] currentResult = this.deleteCachedRepresentationOfTarget(TargetID, cachedID);
            result[0] = result[0] + currentResult[0];
            result[1] = result[1] + currentResult[1];
        }
        return result;
    }

    @Override
    public int[] deleteAnnotation(Number annotationID) {
        int[] result = new int[4];
        result[1] = annotationDao.deleteAnnotationPrincipalPermissions(annotationID);
        List<Number> targetIDs = annotationDao.retrieveTargetIDs(annotationID);
        result[2] = annotationDao.deleteAllAnnotationTarget(annotationID);
        result[0] = annotationDao.deleteAnnotation(annotationID);
        result[3] = 0;
        if (targetIDs != null) {
            for (Number targetID : targetIDs) {
                this.deleteAllCachedRepresentationsOfTarget(targetID);
                result[3] = result[3] + targetDao.deleteTarget(targetID);

            }
        }
        return result;
    }
    
    @Override
    public boolean deleteNotebook(Number notebookID) {
        boolean deletePermissions = notebookDao.deleteAllPermissionsForNotebook(notebookID);
        boolean deleteAnnotations = notebookDao.deleteAllAnnotationsFromNotebook(notebookID);
        return notebookDao.deleteNotebook(notebookID);
    }

    ////////////// HELPERS ////////////////////
    private Target createFreshTarget(TargetInfo targetInfo) {
        Target target = new Target();
        target.setLink(targetInfo.getLink());
        target.setVersion(targetInfo.getVersion());
        return target;
    }

    private int addTargets(Annotation annotation, Number annotationID) {
        List<TargetInfo> targets = annotation.getTargets().getTargetInfo();
        Map<String, String> targetIdPairs = addTargetsForAnnotation(annotationID, targets);
        AnnotationBody annotationBody = annotation.getBody();
        String bodyText;
        String newBodyText;
        String mimeType;
        if (annotationBody.getXmlBody() != null) {
            bodyText = Helpers.elementToString(annotation.getBody().getXmlBody().getAny());
            mimeType = annotationBody.getXmlBody().getMimeType();
        } else {
            if (annotation.getBody().getTextBody() != null) {
                bodyText = annotation.getBody().getTextBody().getBody();
                mimeType = annotationBody.getTextBody().getMimeType();
            } else {
                logger.error("The client has sent ill-formed annotation body.");
                return -1;
            }
        }
        newBodyText = Helpers.replace(bodyText, targetIdPairs);
        return annotationDao.updateAnnotationBody(annotationID, newBodyText, mimeType, annotationBody.getXmlBody() != null);
    }

    private int addPrincipalsPermissions(List<UserWithPermission> permissions, Number annotationID) {
        int addedPermissions = 0;
        for (UserWithPermission permission : permissions) {
            addedPermissions = addedPermissions + annotationDao.addAnnotationPrincipalPermission(annotationID, userDao.getInternalIDFromURI(permission.getRef()), permission.getPermission());

        }
        return addedPermissions;
    }

    private TargetInfo getTargetInfoFromTarget(Target target) {
        TargetInfo targetInfo = new TargetInfo();
        targetInfo.setRef(target.getURI());
        targetInfo.setLink(target.getLink());
        targetInfo.setVersion(target.getVersion());
        return targetInfo;
    }
}
