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

import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.schema.CurrentUserInfo;
import eu.dasish.annotation.schema.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author olhsha
 */
public class JdbcUserDao extends JdbcResourceDao implements UserDao {

    public JdbcUserDao(DataSource dataSource) {
        setDataSource(dataSource);
        internalIdName = principal_id;
        resourceTableName = principalTableName;
    }

    @Override
    public void setServiceURI(String serviceURI){
        _serviceURI = serviceURI;
    }
    
    /////////// GETTERS //////////////////////
    @Override 
    public User getUser(Number internalID){
        StringBuilder sql  = new StringBuilder("SELECT ");
        sql.append(principalStar).append(" FROM ").append(principalTableName).append(" WHERE ").append(principal_id).append("= ? LIMIT 1");
        List<User> result = getSimpleJdbcTemplate().query(sql.toString(), userRowMapper, internalID);
        return (!result.isEmpty() ? result.get(0) : null);
     }
    
    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNumber) throws SQLException {
            User result = new User();
            result.setURI(externalIDtoURI(rs.getString(external_id)));
            result.setDisplayName(rs.getString(principal_name));
            result.setEMail(rs.getString(e_mail));
            return result;
        }
    };
    
    @Override 
    public User getUserByInfo(String eMail){
        StringBuilder sql  = new StringBuilder("SELECT ");
        sql.append(principalStar).append(" FROM ").append(principalTableName).append(" WHERE ").append(e_mail).append("= ? LIMIT 1");
        List<User> result = getSimpleJdbcTemplate().query(sql.toString(), userRowMapper, eMail.toLowerCase());
        return (!result.isEmpty() ? result.get(0) : null);
     }
    
   
    
    @Override
    public boolean userIsInUse(Number userID) {
        StringBuilder sqlPermissions  = new StringBuilder("SELECT ");
        sqlPermissions.append(principal_id).append(" FROM ").append(permissionsTableName).append(" WHERE ").append(principal_id).append("= ? LIMIT 1");
        List<Number> resultTargets = getSimpleJdbcTemplate().query(sqlPermissions.toString(), principalIDRowMapper, userID);
        if (resultTargets.size() > 0) {
            return true;
        };
        
        StringBuilder sqlNotebooks  = new StringBuilder("SELECT ");
        sqlNotebooks.append(owner_id).append(" FROM ").append(notebookTableName).append(" WHERE ").append(owner_id).append("= ? LIMIT 1");
        List<Number> resultNotebooks = getSimpleJdbcTemplate().query(sqlNotebooks.toString(), ownerIDRowMapper, userID);
        if (resultNotebooks.size() > 0) {
            return true;
        };
        return false;
    }
    
    
    @Override 
    public boolean userExists(User user){
        String emailCriterion = user.getEMail();
        StringBuilder sqlTargets  = new StringBuilder("SELECT ");
        sqlTargets.append(principal_id).append(" FROM ").append(principalTableName).append(" WHERE ").append(e_mail).append("= ? LIMIT 1");
        List<Number> resultTargets = getSimpleJdbcTemplate().query(sqlTargets.toString(), principalIDRowMapper, emailCriterion);
        if (resultTargets.size() > 0) {
            return true;
        }
        else {
            return false;
        }
    }

     ////////////////////////////////////////////////////////////
     public Number addUser(User user, String remoteID){
        UUID externalIdentifier = UUID.randomUUID();
        String newExternalIdentifier = externalIdentifier.toString();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", newExternalIdentifier);
        params.put("principalName", user.getDisplayName());
        params.put("email", user.getEMail());
        params.put("remoteID", remoteID);
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(principalTableName).append("(").append(external_id).append(",").append(principal_name).append(",").append(e_mail).append(",").append(remote_id).append(" ) VALUES (:externalId, :principalName, :email, :remoteID)");
        final int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
        return (affectedRows>0 ? getInternalID(externalIdentifier) : null);
     }
     
     
     public int deleteUser(Number internalID){
          if (userIsInUse(internalID)) {
            return 0;
        }        
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(principalTableName).append(" where ").append(principal_id).append(" = ?");
        return getSimpleJdbcTemplate().update(sql.toString(), internalID);

     }
    
}