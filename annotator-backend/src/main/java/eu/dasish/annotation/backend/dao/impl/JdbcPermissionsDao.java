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

import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.PermissionsDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.UserWithPermission;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author olhsha
 */
public class JdbcPermissionsDao extends JdbcResourceDao implements PermissionsDao {

    @Autowired
    private UserDao jdbcUserDao;
    
    @Autowired
    private AnnotationDao jdbcAnnotationDao;
   
    
    public JdbcPermissionsDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    ///////////////////////////////////////////////////////////////////
    @Override
    public List<UserWithPermission> retrievePermissions(Number annotationId) {
        if (annotationId == null) {
            return null;
        }
        String sql = "SELECT " + principal_id + "," + permission + " FROM " + permissionsTableName + " WHERE " + annotation_id + "  = ?";
        List<UserWithPermission> result = getSimpleJdbcTemplate().query(sql, principalsPermissionsRowMapper, annotationId.toString());
        return result;
    }
    private final RowMapper<UserWithPermission> principalsPermissionsRowMapper = new RowMapper<UserWithPermission>() {
        @Override
        public UserWithPermission mapRow(ResultSet rs, int rowNumber) throws SQLException {
            UserWithPermission result = new UserWithPermission();
            result.setRef((jdbcUserDao.getExternalID(rs.getInt(principal_id))).toString());
            result.setPermission(Permission.fromValue(rs.getString(permission)));
            return result;
        }
    };
    
    /////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int addAnnotationPrincipalPermission(AnnotationIdentifier annotationIdenitifier, UserIdentifier userIdentifier, Permission permission) throws SQLException {
        Map<String, Object> paramsPermissions = new HashMap<String, Object>();
        paramsPermissions.put("annotationId", jdbcAnnotationDao.getAnnotationID(annotationIdenitifier));
        paramsPermissions.put("principalId", jdbcUserDao.getInternalID(userIdentifier));
        paramsPermissions.put("status", permission.value());
        String sqlUpdatePermissionTable = "INSERT INTO " + permissionsTableName + " (" + annotation_id + "," + principal_id + "," + permission + ") VALUES (:annotationId, :principalId, :status)";
        final int affectedPermissions = getSimpleJdbcTemplate().update(sqlUpdatePermissionTable, paramsPermissions);
        return affectedPermissions;
    }
    
    @Override
    public int removeAnnotation(Number annotationID){        
        String sqlPermissions = "DELETE FROM " + permissionsTableName + " where "+annotation_id + " = ?";        
        int affectedPermissions = getSimpleJdbcTemplate().update(sqlPermissions, annotationID);
        return affectedPermissions;
    }
   ///////////////////////////////////////////////////////////////////////////////// 
    //TODO replace name "user" in the scheme beacuse it is misleading. E.g. replace it with 
    // getUser actual gives you the list of PAIRS (user, permission) that are refferred from an annotation
//   @Override 
//   public PermissionList makeFreshPermissionList(UserIdentifier owner) {
//       PermissionList result = new PermissionList();
//       
//       result.setURI((new PermissionListIdentifier()).toString());
//       
//       UserWithPermission idOwner = new UserWithPermission();
//       idOwner.setPermission(Permission.fromValue("owner"));
//       idOwner.setRef(owner.toString());
//       
//       result.getUser().add(idOwner);
//       return result;
//   }
    
}
