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
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author olhsha
 */
public class JdbcUserDao extends JdbcResourceDao implements UserDao {

    private final Logger loggerUserDao = LoggerFactory.getLogger(JdbcUserDao.class);

    public JdbcUserDao(DataSource dataSource) {
        setDataSource(dataSource);
        internalIdName = principal_id;
        resourceTableName = principalTableName;
    }

    @Override
    public void setServiceURI(String serviceURI) {
        _serviceURI = serviceURI;
    }

    /////////// GETTERS //////////////////////
    @Override
    public User getUser(Number internalID) {

        if (internalID == null) {
            loggerUserDao.debug("internalID: " + nullArgument);
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT ");
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
    public User getUserByInfo(String eMail) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(principalStar).append(" FROM ").append(principalTableName).append(" WHERE ").append("LOWER(").append(e_mail).append(")").append("= ? LIMIT 1");
        List<User> result = getSimpleJdbcTemplate().query(sql.toString(), userRowMapper, eMail.toLowerCase());
        return (!result.isEmpty() ? result.get(0) : null);
    }

    @Override
    public boolean userIsInUse(Number userID) {

        if (userID == null) {
            loggerUserDao.debug("userID: " + nullArgument);
            return false;
        }

        StringBuilder sqlPermissions = new StringBuilder("SELECT ");
        sqlPermissions.append(principal_id).append(" FROM ").append(permissionsTableName).append(" WHERE ").append(principal_id).append("= ? LIMIT 1");
        List<Number> resultTargets = getSimpleJdbcTemplate().query(sqlPermissions.toString(), internalIDRowMapper, userID);
        if (resultTargets.size() > 0) {
            return true;
        };

        StringBuilder sqlNotebooks = new StringBuilder("SELECT ");
        sqlNotebooks.append(owner_id).append(" FROM ").append(notebookTableName).append(" WHERE ").append(owner_id).append("= ? LIMIT 1");
        List<Number> resultNotebooks = getSimpleJdbcTemplate().query(sqlNotebooks.toString(), ownerIDRowMapper, userID);
        if (resultNotebooks.size() > 0) {
            return true;
        };
        return false;
    }

    @Override
    public boolean userExists(User user) {
        if (user == null) {
            loggerUserDao.debug("user: " + nullArgument);
            return false;
        }

        String emailCriterion = user.getEMail().toLowerCase();
        StringBuilder sqlTargets = new StringBuilder("SELECT ");
        sqlTargets.append(principal_id).append(" FROM ").append(principalTableName).append(" WHERE ").append("LOWER(").append(e_mail).append(")= ? LIMIT 1");
        List<Number> resultTargets = getSimpleJdbcTemplate().query(sqlTargets.toString(), internalIDRowMapper, emailCriterion);
        if (resultTargets.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getRemoteID(Number internalID) {

        if (internalID == null) {
            loggerUserDao.debug("internalID: " + nullArgument);
            return null;
        }

        StringBuilder requestDB = new StringBuilder("SELECT ");
        requestDB.append(remote_id).append(" FROM ").append(principalTableName).append(" WHERE ").append(principal_id).append("= ? LIMIT 1");
        List<String> result = getSimpleJdbcTemplate().query(requestDB.toString(), remoteIDRowMapper, internalID);
        return (result.size() > 0) ? result.get(0) : null;
    }
    private final RowMapper<String> remoteIDRowMapper = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getString(remote_id);
        }
    };

    @Override
    public Number getUserInternalIDFromRemoteID(String remoteID) {

        if (remoteID == null) {
            loggerUserDao.debug("remoteID: " + nullArgument);
            return null;
        }

        StringBuilder requestDB = new StringBuilder("SELECT ");
        requestDB.append(principal_id).append(" FROM ").append(principalTableName).append(" WHERE ").append(remote_id).append("= ? LIMIT 1");
        List<Number> result = getSimpleJdbcTemplate().query(requestDB.toString(), internalIDRowMapper, remoteID);
        return (result.size() > 0) ? result.get(0) : null;
    }

    @Override
    public String getTypeOfUserAccount(Number internalID) {

        if (internalID == null) {
            loggerUserDao.debug("internalID: " + nullArgument);
            return null;
        }

        StringBuilder requestDB = new StringBuilder("SELECT ");
        requestDB.append(account).append(" FROM ").append(principalTableName).append(" WHERE ").append(principal_id).append("= ? LIMIT 1");
        List<String> result = getSimpleJdbcTemplate().query(requestDB.toString(), adminRightsRowMapper, internalID);
        return (result.size() > 0) ? result.get(0) : null;
    }
    private final RowMapper<String> adminRightsRowMapper = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getString(account);
        }
    };
    
    
    @Override
    public List<Number> getPrincipalIDsWithPermissionForNotebook(Number notebookID, Permission permission) {
        if (notebookID == null) {
            loggerUserDao.debug("notebookID: " + nullArgument);
            return null;
        }

        if (permission == null) {
            loggerUserDao.debug("permission: " + nullArgument);
            return null;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("notebookID", notebookID);
        params.put("accessMode", permission.value());

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(principal_id).append(" FROM ").append(notebookPermissionsTableName).append(" WHERE ").
                append(notebook_id).append(" = :notebookID AND ").append(this.permission).append(" = :accessMode");

        return getSimpleJdbcTemplate().query(sql.toString(), internalIDRowMapper, params);

    }

    ///////////////////// ADDERS ////////////////////////////
    @Override
    public Number addUser(User user, String remoteID) {

        if (remoteID == null) {
            loggerUserDao.debug("remoteID: " + nullArgument);
            return null;
        }

        if (user == null) {
            loggerUserDao.debug("user: " + nullArgument);
            return null;
        }

        UUID externalIdentifier = UUID.randomUUID();
        String newExternalIdentifier = externalIdentifier.toString();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", newExternalIdentifier);
        params.put("principalName", user.getDisplayName());
        params.put("email", user.getEMail());
        params.put("remoteID", remoteID);
        params.put("accountType", this.user);
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(principalTableName).append("(").append(external_id).append(",").
                append(principal_name).append(",").append(e_mail).append(",").
                append(remote_id).append(",").append(account).append(" ) VALUES (:externalId, :principalName, :email, :remoteID, :accountType)");
        final int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
        return (affectedRows > 0 ? getInternalID(externalIdentifier) : null);
    }

    ////////// UPDATERS ///////////////////////
    @Override
    public boolean updateAccount(UUID externalID, String account) {

        if (externalID == null) {
            loggerUserDao.debug("eternalId: " + nullArgument);
            return false;
        }

        if (account == null) {
            loggerUserDao.debug("account: " + nullArgument);
            return false;
        }

        if (!account.equals(admin) && !account.equals(developer) && !account.equals(user)) {
            logger.error("the given type of account '" + account + "' does not exist.");
            return false;
        }
        Number principalID = this.getInternalID(externalID);
        if (principalID != null) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("externalId", externalID.toString());
            params.put("accountType", account);
            StringBuilder sql = new StringBuilder("UPDATE ");
            sql.append(principalTableName).append(" SET ").
                    append(this.account).append("= :accountType").
                    append(" WHERE ").append(external_id).append("= :externalId");
            int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), params);
            if (affectedRows > 0) {
                return true;
            } else {
                logger.error("For some reason the database refuses update the account of " + externalID.toString() + " . Consult the servers' respond.");
                return false;
            }
        } else {
            logger.error("The user with external ID " + externalID.toString() + " is not found in the data base");
            return false;
        }

    }

    @Override
    public Number updateUser(User user) {

        if (user == null) {
            loggerUserDao.debug("user: " + nullArgument);
            return null;
        }

        Number principalID = this.getInternalIDFromURI(user.getURI());
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(principalTableName).append(" SET ").
                append(e_mail).append("= '").append(user.getEMail()).append("',").
                append(principal_name).append("= '").append(user.getDisplayName()).append("' ").
                append(" WHERE ").append(principal_id).append("= ?");
        int affectedRows = getSimpleJdbcTemplate().update(sql.toString(), principalID);
        return principalID;
    }

    ////// DELETERS ////////////
    @Override
    public int deleteUser(Number internalID) {
        if (internalID == null) {
            loggerUserDao.debug("internalID: " + nullArgument);
            return 0;
        }


        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(principalTableName).append(" where ").append(principal_id).append(" = ?");
        return getSimpleJdbcTemplate().update(sql.toString(), internalID);

    }

    @Override
    public int deleteUserSafe(Number internalID) {
         
        if (internalID == null) {
            loggerUserDao.debug("internalID: " + nullArgument);
            return 0;
        }
        
        
        if (userIsInUse(internalID)) {
            loggerUserDao.debug("User is in use, and cannot be deleted.");
            return 0;
        }
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(principalTableName).append(" where ").append(principal_id).append(" = ?");
        return getSimpleJdbcTemplate().update(sql.toString(), internalID);

    }
}