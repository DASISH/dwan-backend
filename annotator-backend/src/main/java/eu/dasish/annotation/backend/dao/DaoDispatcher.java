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
 */
public interface DaoDispatcher{
    
    public void setServiceURI(String serviceURI);

    /////////////// ADDERS  /////////////////////////////////
    Number[] addCachedForVersion(Number versionID, CachedRepresentationInfo cached);

    Number[] addSiblingVersionForSource(Number sourceID, Version version) throws SQLException;

    Map<String, String> addSourcesForAnnotation(Number annotationID, List<NewOrExistingSourceInfo> sources) throws SQLException;

    Number addUsersAnnotation(Annotation annotation, Number userID) throws SQLException;

    int[] deleteAllCachedOfVersion(Number versionID);

    int[] deleteAllVersionsOfSource(Number sourceID) throws SQLException;

    int[] deleteAnnotation(Number annotationID) throws SQLException;

    ////////////// DELETERS //////////////////
    int[] deleteCachedOfVersion(Number versionID, Number cachedID);

    Annotation getAnnotation(Number annotationID) throws SQLException;

    UUID getAnnotationExternalIdentifier(Number annotationID);

    ///////////// GETTERS //////////////////////////
    Number getAnnotationInternalIdentifier(UUID UUID);

    ////////////////////////////////////////////////////////////////////////
    List<Number> getFilteredAnnotationIDs(String link, String text, String access, String namespace, UUID
            owner, Timestamp after, Timestamp before);

    UUID getUserExternalIdentifier(Number userID);

    Number getUserInternalIdentifier(UUID UUID);
    
}
