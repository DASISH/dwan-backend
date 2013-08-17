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
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.UserWithPermission;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author olhsha
 */
public interface PermissionsDao {
    
     /**
     * 
     * @param annotationId
     * @return retrieves all the pairs (user-permission) for "annotationId" from the table annotations_principals permissions
     */
    public List<UserWithPermission>  retrievePermissions(Number annotationId);
    
    /**
     * 
     * @param annotationID
     * @param userID
     * @param permission
     * @return the amount of rows added to the table annotations_principals_permissions
     */
    public int addAnnotationPrincipalPermission(Number annotationID, Number userID, Permission permission) throws SQLException;
    
    /**
     * 
     * @param annotationID
     * @return remove all the rows with annotationID from the table annotations_principals_permissions
     */
    public int removeAnnotation(Number annotationID); 

}
