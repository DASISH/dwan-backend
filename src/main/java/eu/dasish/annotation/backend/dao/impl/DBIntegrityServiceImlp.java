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

import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.ResourceDao;
import eu.dasish.annotation.backend.dao.TargetDao;
import eu.dasish.annotation.backend.dao.PrincipalDao;
import eu.dasish.annotation.backend.rest.AnnotationResource;
import eu.dasish.annotation.schema.Action;
import eu.dasish.annotation.schema.ActionList;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationActionName;
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
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.PermissionActionName;
import eu.dasish.annotation.schema.PermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.Principal;
import eu.dasish.annotation.schema.Permission;
import java.io.InputStream;
import java.lang.Number;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.lang.reflect.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author olhsha
 */
public class DBIntegrityServiceImlp implements DBIntegrityService {

    @Autowired
    PrincipalDao principalDao;
    @Autowired
    CachedRepresentationDao cachedRepresentationDao;
    @Autowired
    TargetDao targetDao;
    @Autowired
    AnnotationDao annotationDao;
    @Autowired
    NotebookDao notebookDao;
    
    
    final static protected String admin = "admin";
    private static final Logger logger = LoggerFactory.getLogger(DBIntegrityServiceImlp.class);

    //////////////////////////////////
    private ResourceDao getDao(Resource resource) {
        switch (resource) {
            case PRINCIPAL:
                return principalDao;
            case ANNOTATION:
                return annotationDao;
            case TARGET:
                return targetDao;
            case CACHED_REPRESENTATION:
                return cachedRepresentationDao;
            case NOTEBOOK:
                return notebookDao;
            default:
                return null;
        }
    }
    
   

    @Override
    public void setServiceURI(String serviceURI) {
        principalDao.setServiceURI(serviceURI + "principals/");
        cachedRepresentationDao.setServiceURI(serviceURI + "cached/");
        targetDao.setServiceURI(serviceURI + "targets/");
        annotationDao.setServiceURI(serviceURI + "annotations/");
        notebookDao.setServiceURI(serviceURI + "notebooks/");
    }

    ///////////// GETTERS //////////////////////////
    @Override
    public Number getResourceInternalIdentifier(UUID externalID, Resource resource) {
        return this.getDao(resource).getInternalID(externalID);
    }

    @Override
    public Number getResourceInternalIdentifierFromURI(String uri, Resource resource) {
        return this.getDao(resource).getInternalIDFromURI(uri);
    }

    @Override
    public UUID getResourceExternalIdentifier(Number resourceID, Resource resource) {
        return this.getDao(resource).getExternalID(resourceID);
    }

    @Override
    public String getResourceURI(Number resourceID, Resource resource) {
        return this.getDao(resource).getURIFromInternalID(resourceID);
    }

    @Override
    public Annotation getAnnotation(Number annotationID) {
        if (annotationID != null) {
            Annotation result = annotationDao.getAnnotationWithoutTargetsAndAccesss(annotationID);
            result.setOwnerRef(principalDao.getURIFromInternalID(annotationDao.getOwner(annotationID)));
            List<Number> targetIDs = targetDao.retrieveTargetIDs(annotationID);
            TargetInfoList sis = new TargetInfoList();
            for (Number targetID : targetIDs) {
                TargetInfo targetInfo = getTargetInfoFromTarget(targetDao.getTarget(targetID));
                sis.getTargetInfo().add(targetInfo);
            }
            result.setTargets(sis);

            result.setPermissions(this.getPermissions(annotationID, Resource.ANNOTATION));
            return result;
        } else {
            return null;
        }
    }

    @Override
    public Number getAnnotationOwnerID(Number annotationID) {
        return annotationDao.getOwner(annotationID);
    }
    
    @Override
    public Principal getAnnotationOwner(Number annotationID) {
       return principalDao.getPrincipal(annotationDao.getOwner(annotationID)); 
    }

    ///////////////////////////////////////////////////
    // TODO UNIT tests
    @Override
    public PermissionList getPermissions(Number resourceID, Resource resource) {
        if (resourceID != null) {
            List<Map<Number, String>> principalsAccesss = this.getDao(resource).getPermissions(resourceID);
            PermissionList result = new PermissionList();
            List<Permission> list = result.getPermission();
            for (Map<Number, String> principalAccess : principalsAccesss) {
                Number[] principal = new Number[1];
                principalAccess.keySet().toArray(principal);
                Permission permission = new Permission();
                permission.setPrincipalRef(principalDao.getURIFromInternalID(principal[0]));
                permission.setLevel(Access.fromValue(principalAccess.get(principal[0])));
                list.add(permission);
            }
            return result;

        }
        return null;
    }

////////////////////////////////////////////////////////////////////////
    @Override
    public List<Number> getFilteredAnnotationIDs(UUID ownerId, String link, String text, Number inloggedPrincipalID, String access, String namespace, String after, String before) {

        Number ownerID = (ownerId != null) ? principalDao.getInternalID(ownerId) : null;
        if (ownerID != null) {
            if ("owner".equals(access) && !inloggedPrincipalID.equals(ownerID)) {
                logger.info("The inlogged principal cannot be the owner of the annotations owned by " + ownerId.toString());
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
            ownerID = inloggedPrincipalID;
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

        // filtering on table "annotations_principals_accesss"
        if ("read".equals(access) || "write".equals(access)) {
            // owner != inloggedPrincipal
            List<Number> annotationIDsAccess = annotationDao.getAnnotationIDsForPermission(inloggedPrincipalID, access);
            if (annotationIDsAccess != null) {
                annotationIDs.retainAll(annotationIDsAccess);
            } else {
                logger.info("There are no annotations for which the inlogged principal has access " + access);
                return null;
            }
        } else {
            // inloggedPrincipal == owner
        }
        return annotationIDs;
    }

    @Override
    public ReferenceList getAnnotationTargets(Number annotationID) {
        ReferenceList result = new ReferenceList();
        List<Number> targetIDs = targetDao.retrieveTargetIDs(annotationID);
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
        List<Number> targetIDs = targetDao.retrieveTargetIDs(annotationID);
        for (Number targetID : targetIDs) {
            List<Number> versions = cachedRepresentationDao.getCachedRepresentationsForTarget(targetID);
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
    public List<String> getPrincipalsWithNoInfo(Number annotationID) {
        if (annotationID == null) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        List<Map<Number, String>> principalsWithAccesss = annotationDao.getPermissions(annotationID);
        for (Map<Number, String> permission : principalsWithAccesss) {
            Number[] principalID = new Number[1];
            permission.keySet().toArray(principalID);
            Principal principal = principalDao.getPrincipal(principalID[0]);

            if (principal.getDisplayName() == null || principal.getDisplayName().trim().isEmpty() || principal.getEMail() == null || principal.getEMail().trim().isEmpty()) {
                result.add(principalDao.getURIFromInternalID(principalID[0]));

            }
        }
        return result;
    }

    @Override
    public AnnotationInfoList getFilteredAnnotationInfos(UUID ownerId, String word, String text, Number inloggedPrincipalID, String access, String namespace, String after, String before) {
        List<Number> annotationIDs = this.getFilteredAnnotationIDs(ownerId, word, text, inloggedPrincipalID, access, namespace, after, before);
        if (annotationIDs != null) {
            AnnotationInfoList result = new AnnotationInfoList();
            for (Number annotationID : annotationIDs) {
                AnnotationInfo annotationInfo = annotationDao.getAnnotationInfoWithoutTargets(annotationID);
                annotationInfo.setTargets(this.getAnnotationTargets(annotationID));
                annotationInfo.setOwnerRef(principalDao.getURIFromInternalID(annotationDao.getOwner(annotationID)));
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
                    annotationInfo.setOwnerRef(principalDao.getURIFromInternalID(ownerID));
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
    public Principal getPrincipal(Number principalID) {
        return principalDao.getPrincipal(principalID);
    }

    @Override
    public Principal getPrincipalByInfo(String eMail) {
        return principalDao.getPrincipalByInfo(eMail);
    }

    @Override
    public String getPrincipalRemoteID(Number internalID) {
        return principalDao.getRemoteID(internalID);
    }

    @Override
    public Access getAccess(Number annotationID, Number principalID) {
        return annotationDao.getAccess(annotationID, principalID);
    }

    @Override
    public Number getPrincipalInternalIDFromRemoteID(String remoteID) {
        return principalDao.getPrincipalInternalIDFromRemoteID(remoteID);
    }

    @Override
    public String getTypeOfPrincipalAccount(Number principalID) {
        return principalDao.getTypeOfPrincipalAccount(principalID);
    }
    
    @Override
    public Principal getDataBaseAdmin(){
        return principalDao.getPrincipal(principalDao.getDBAdminID());
    }
    

    @Override
    public boolean canRead(Number principalID, Number annotationID) {
        if (principalID.equals(annotationDao.getOwner(annotationID)) || principalDao.getTypeOfPrincipalAccount(principalID).equals(admin)) {
            return true;
        }

        final Access access = annotationDao.getAccess(annotationID, principalID);
        if (access != null) {
            return (access.value().equals(Access.WRITE.value()) || access.value().equals(Access.READ.value()));
        } else {
            return false;
        }
    }

    @Override
    public boolean canWrite(Number principalID, Number annotationID) {
        if (principalID.equals(annotationDao.getOwner(annotationID)) || principalDao.getTypeOfPrincipalAccount(principalID).equals(admin)) {
            return true;
        }
        final Access access = annotationDao.getAccess(annotationID, principalID);
        if (access != null) {
            return (access.value().equals(Access.WRITE.value()));
        } else {
            return false;
        }
    }
    
    ////// noetbooks ///////

    @Override
    public NotebookInfoList getNotebooks(Number principalID, String access) {
        NotebookInfoList result = new NotebookInfoList();
        if (access.equalsIgnoreCase("read") || access.equalsIgnoreCase("write")) {
            List<Number> notebookIDs = notebookDao.getNotebookIDs(principalID, Access.fromValue(access));
            for (Number notebookID : notebookIDs) {
                NotebookInfo notebookInfo = notebookDao.getNotebookInfoWithoutOwner(notebookID);
                Number ownerID = notebookDao.getOwner(notebookID);
                notebookInfo.setOwnerRef(principalDao.getURIFromInternalID(ownerID));
                result.getNotebookInfo().add(notebookInfo);
            }
        } else {
            if (access.equalsIgnoreCase("owner")) {
                List<Number> notebookIDs = notebookDao.getNotebookIDsOwnedBy(principalID);
                String ownerRef = principalDao.getURIFromInternalID(principalID);
                for (Number notebookID : notebookIDs) {
                    NotebookInfo notebookInfo = notebookDao.getNotebookInfoWithoutOwner(notebookID);
                    notebookInfo.setOwnerRef(ownerRef);
                    result.getNotebookInfo().add(notebookInfo);
                }
            } else {
                return null;
            }
        }
        return result;
    }

    @Override
    public boolean hasAccess(Number notebookID, Number principalID, Access access) {
        List<Number> notebookIDs = notebookDao.getNotebookIDs(principalID, access);
        if (notebookIDs == null) {
            return false;
        }
        return notebookIDs.contains(notebookID);
    }

    @Override
    public ReferenceList getNotebooksOwnedBy(Number principalID) {
        ReferenceList result = new ReferenceList();
        List<Number> notebookIDs = notebookDao.getNotebookIDsOwnedBy(principalID);
        for (Number notebookID : notebookIDs) {
            String reference = notebookDao.getURIFromInternalID(notebookID);
            result.getRef().add(reference);
        }
        return result;
    }

    @Override
    public ReferenceList getPrincipals(Number notebookID, String access) {
        ReferenceList result = new ReferenceList();
        List<Number> principalIDs = principalDao.getPrincipalIDsWithAccessForNotebook(notebookID, Access.fromValue(access));
        for (Number principalID : principalIDs) {
            String reference = principalDao.getURIFromInternalID(principalID);
            result.getRef().add(reference);
        }
        return result;
    }

    @Override
    public Notebook getNotebook(Number notebookID) {
        Notebook result = notebookDao.getNotebookWithoutAnnotationsAndAccesssAndOwner(notebookID);

        result.setOwnerRef(principalDao.getURIFromInternalID(notebookDao.getOwner(notebookID)));

        ReferenceList annotations = new ReferenceList();
        List<Number> annotationIDs = annotationDao.getAnnotations(notebookID);
        for (Number annotationID : annotationIDs) {
            annotations.getRef().add(annotationDao.getURIFromInternalID(annotationID));
        }
        result.setAnnotations(annotations);

        PermissionList ups = new PermissionList();
        List<Access> accesss = new ArrayList<Access>();
        accesss.add(Access.READ);
        accesss.add(Access.WRITE);
        for (Access access : accesss) {
            List<Number> principals = principalDao.getPrincipalIDsWithAccessForNotebook(notebookID, access);
            if (principals != null) {
                for (Number principal : principals) {
                    Permission up = new Permission();
                    up.setPrincipalRef(principalDao.getURIFromInternalID(principal));
                    up.setLevel(access);
                    ups.getPermission().add(up);
                }
            }
        }
        result.setPermissions(ups);
        return result;
    }

    @Override
    public Number getNotebookOwner(Number notebookID) {
        return notebookDao.getOwner(notebookID);
    }

    /////////////////////////////////////////////////////////////
    @Override
    public ReferenceList getAnnotationsForNotebook(Number notebookID, int startAnnotation, int maximumAnnotations, String orderedBy, boolean desc) {
        List<Number> annotationIDs = annotationDao.getAnnotations(notebookID);

        if (startAnnotation < -1) {
            logger.info("Variable's startAnnotation value " + startAnnotation + " is invalid. I will return null.");
            return null;
        }

        if (maximumAnnotations < -1) {
            logger.info("Variable's maximumAnnotations value " + maximumAnnotations + " is invalid. I will return null.");
            return null;
        }

        int offset = (startAnnotation > 0) ? startAnnotation - 1 : 0;
        String direction = desc ? "DESC" : "ASC";
        List<Number> selectedAnnotIDs = annotationDao.sublistOrderedAnnotationIDs(annotationIDs, offset, maximumAnnotations, orderedBy, direction);
        ReferenceList references = new ReferenceList();
        for (Number annotationID : selectedAnnotIDs) {
            references.getRef().add(annotationDao.getURIFromInternalID(annotationID));
        }
        return references;
    }

    ///// UPDATERS /////////////////
    @Override
    public boolean updateAccount(UUID principalExternalID, String account) {
        return principalDao.updateAccount(principalExternalID, account);
    }

    @Override
    public int updateAnnotationPrincipalAccess(Number annotationID, Number principalID, Access access) {
        if (access != null) {
            return annotationDao.updateAnnotationPrincipalAccess(annotationID, principalID, access);
        } else {
            return annotationDao.deleteAnnotationPrincipalAccess(annotationID, principalID);
        }
    }

    @Override
    public int updatePermissions(Number annotationID, PermissionList permissionList) {

        List<Permission> permissions = permissionList.getPermission();
        int result = 0;
        for (Permission permission : permissions) {
            Number principalID = principalDao.getInternalID(UUID.fromString(principalDao.stringURItoExternalID(permission.getPrincipalRef())));
            if (principalID != null) {
                Access access = permission.getLevel();
                Access currentAccess = annotationDao.getAccess(annotationID, principalID);
                if (currentAccess != null) {
                    if (!access.value().equals(currentAccess.value())) {
                        result = result + annotationDao.updateAnnotationPrincipalAccess(annotationID, principalID, access);
                    }
                } else {
                    result = result + annotationDao.addAnnotationPrincipalAccess(annotationID, principalID, access);
                }
            }
        }

        return result;
    }

    // TODO: optimize (not chnaged targets should not be deleted)
    // TODO: unit test
    @Override
    public int updateAnnotation(Annotation annotation) {
        int updatedAnnotations = annotationDao.updateAnnotation(annotation, principalDao.getInternalIDFromURI(annotation.getOwnerRef()));
        Number annotationID = annotationDao.getInternalIDFromURI(annotation.getURI());
        int deletedTargets = annotationDao.deleteAllAnnotationTarget(annotationID);
        int deletedPrinsipalsAccesss = annotationDao.deleteAnnotationPrincipalAccesss(annotationID);
        int addedTargets = addTargets(annotation, annotationID);
        int addedPrincipalsAccesss = addPrincipalsAccesss(annotation.getPermissions().getPermission(), annotationID);
        return updatedAnnotations;
    }

    // TODO: unit test
    @Override
    public int updateAnnotationBody(Number internalID, AnnotationBody annotationBody) {
        String[] body = annotationDao.retrieveBodyComponents(annotationBody);
        return annotationDao.updateAnnotationBody(internalID, body[0], body[1], annotationBody.getXmlBody() != null);
    }

    @Override
    public Number updatePrincipal(Principal principal) {
        return principalDao.updatePrincipal(principal);
    }
    /// notebooks ///

    @Override
    public boolean updateNotebookMetadata(Number notebookID, NotebookInfo upToDateNotebookInfo) {
        Number ownerID = principalDao.getInternalIDFromURI(upToDateNotebookInfo.getOwnerRef());
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
    public Number addPrincipalsAnnotation(Number ownerID, Annotation annotation) {
        Number annotationID = annotationDao.addAnnotation(annotation, ownerID);
        int affectedAnnotRows = addTargets(annotation, annotationID);
        if (annotation.getPermissions() != null) {
            if (annotation.getPermissions().getPermission() != null) {
                int addedPrincipalsAccesss = this.addPrincipalsAccesss(annotation.getPermissions().getPermission(), annotationID);
            }
        }
        return annotationID;
    }

    @Override
    public Number addPrincipal(Principal principal, String remoteID) {
        if (principalDao.principalExists(principal)) {
            return null;
        } else {
            return principalDao.addPrincipal(principal, remoteID);
        }
    }

    @Override
    public int addAnnotationPrincipalAccess(Number annotationID, Number principalID, Access access) {
        return annotationDao.addAnnotationPrincipalAccess(annotationID, principalID, access);
    }

    //////////// notebooks //////
    @Override
    public Number createNotebook(Notebook notebook, Number ownerID) {
        Number notebookID = notebookDao.createNotebookWithoutAccesssAndAnnotations(notebook, ownerID);
        boolean updateOwner = notebookDao.setOwner(notebookID, ownerID);
        List<Permission> permissions = notebook.getPermissions().getPermission();
        for (Permission permission : permissions) {
            Number principalID = principalDao.getInternalIDFromURI(permission.getPrincipalRef());
            Access access = permission.getLevel();
            boolean updateAccesss = notebookDao.addAccessToNotebook(notebookID, principalID, access);
        }
        return notebookID;
    }

    @Override
    public boolean createAnnotationInNotebook(Number notebookID, Annotation annotation, Number ownerID) {
        Number newAnnotationID = this.addPrincipalsAnnotation(ownerID, annotation);
        return notebookDao.addAnnotationToNotebook(notebookID, newAnnotationID);
    }

    ////////////// DELETERS //////////////////
    
   
    
    
    @Override
    public int deletePrincipal(Number principalID) {
        return principalDao.deletePrincipal(principalID);
    }

    
    @Override
    public int deletePrincipalSafe(Number principalID) {
        return principalDao.deletePrincipalSafe(principalID);
    }

    @Override
    public int deleteCachedRepresentation(Number internalID) {
        
        if (internalID == null) {
            logger.debug("Cached's internalID is null");
            return 0;
        }
        
        if (targetDao.cachedIsInUse(internalID)) {
            logger.debug("Cached Repr. is in use, and cannot be deleted.");
            return 0;
        }

        return cachedRepresentationDao.deleteCachedRepresentation(internalID);
    };
    
    
    @Override
    public int[] deleteCachedRepresentationOfTarget(Number targetID, Number cachedID) {
        int[] result = new int[2];
        result[0] = targetDao.deleteTargetCachedRepresentation(targetID, cachedID);
        if (result[0] > 0) {
            result[1] = cachedRepresentationDao.deleteCachedRepresentation(cachedID);
        } else {
            result[1] = 0;

        }
        return result;
    }

    @Override
    public int[] deleteAllCachedRepresentationsOfTarget(Number targetID) {
        int[] result = new int[2];
        result[0] = 0;
        result[1] = 0;
        List<Number> cachedIDs = cachedRepresentationDao.getCachedRepresentationsForTarget(targetID);
        for (Number cachedID : cachedIDs) {
            int[] currentResult = this.deleteCachedRepresentationOfTarget(targetID, cachedID);
            result[0] = result[0] + currentResult[0];
            result[1] = result[1] + currentResult[1];
        }
        return result;
    }

    @Override
    public int[] deleteAnnotation(Number annotationID) {
        int[] result = new int[5];
        result[1] = annotationDao.deleteAnnotationPrincipalAccesss(annotationID);
        List<Number> targetIDs = targetDao.retrieveTargetIDs(annotationID);
        result[2] = annotationDao.deleteAllAnnotationTarget(annotationID);
        result[3] = 0;
        if (targetIDs != null) {
            for (Number targetID : targetIDs) {
                this.deleteAllCachedRepresentationsOfTarget(targetID);
                result[3] = result[3] + this.deleteTarget(targetID);

            }
        }
        
        result[4] = annotationDao.deleteAnnotationFromAllNotebooks(annotationID);
        
        result[0] = annotationDao.deleteAnnotation(annotationID);
        return result;
    }
    
    ////////////////////// DELETERS ////////////////////////
    @Override
    public int deleteTarget(Number internalID) {
        if (internalID == null) {
            logger.debug("internalID of the target is null.");
            return 0;
        }
        
        if (annotationDao.targetIsInUse(internalID)) {
            logger.debug("The target is in use, and cannot be deleted.");
            return 0;
        }
        
        return targetDao.deleteTarget(internalID);

    }

    @Override
    public boolean deleteNotebook(Number notebookID) {
        if (notebookDao.deleteAllAccesssForNotebook(notebookID) || notebookDao.deleteAllAnnotationsFromNotebook(notebookID)) {
            return notebookDao.deleteNotebook(notebookID);
        } else {
            return false;
        }
    }

////////////// HELPERS ////////////////////
    ////////////////////////////////////////
    @Override
    public ResponseBody makeAnnotationResponseEnvelope(Number annotationID) {
        ResponseBody result = new ResponseBody();
        Annotation annotation = this.getAnnotation(annotationID);
        result.setAnnotation(annotation);
        List<String> targetsNoCached = this.getTargetsWithNoCachedRepresentation(annotationID);
        ActionList actionList = new ActionList();
        result.setActionList(actionList);
        actionList.getAction().addAll(makeActionList(targetsNoCached, AnnotationActionName.CREATE_CACHED_REPRESENTATION.value()));
        return result;
    }

    @Override
    public ResponseBody makeNotebookResponseEnvelope(Number notebookID) {
        ResponseBody result = new ResponseBody();
        result.setPermissions(null);
        Notebook notebook = this.getNotebook(notebookID);
        result.setNotebook(notebook);
        return result;
    }

    @Override
    public ResponseBody makeAccessResponseEnvelope(Number resourceID, Resource resource) {
        ResponseBody result = new ResponseBody();
        PermissionList permissions = this.getPermissions(resourceID, resource);
        result.setPermissions(permissions);
        List<String> principalsWithNoInfo = this.getPrincipalsWithNoInfo(resourceID);
        ActionList actionList = new ActionList();
        result.setActionList(actionList);
        actionList.getAction().addAll(makeActionList(principalsWithNoInfo, PermissionActionName.PROVIDE_PRINCIPAL_INFO.value()));
        return result;
    }

    private List<Action> makeActionList(List<String> resourceURIs, String message) {
        if (resourceURIs != null) {
            if (resourceURIs.isEmpty()) {
                return (new ArrayList<Action>());
            } else {
                List<Action> result = new ArrayList<Action>();
                for (String resourceURI : resourceURIs) {
                    Action action = new Action();
                    result.add(action);
                    action.setMessage(message);
                    action.setObject(resourceURI);
                }
                return result;
            }
        } else {
            return null;
        }
    }

    //// priveee ///
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

    private int addPrincipalsAccesss(List<Permission> permissions, Number annotationID) {
        int addedAccesss = 0;
        for (Permission permission : permissions) {
            addedAccesss = addedAccesss + annotationDao.addAnnotationPrincipalAccess(annotationID, principalDao.getInternalIDFromURI(permission.getPrincipalRef()), permission.getLevel());

        }
        return addedAccesss;
    }

    private TargetInfo getTargetInfoFromTarget(Target target) {
        TargetInfo targetInfo = new TargetInfo();
        targetInfo.setRef(target.getURI());
        targetInfo.setLink(target.getLink());
        targetInfo.setVersion(target.getVersion());
        return targetInfo;
    }
}
