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
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.PrincipalCannotBeDeleted;
import eu.dasish.annotation.backend.dao.PrincipalDao;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.Principal;
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
public class JdbcPrincipalDao extends JdbcResourceDao implements PrincipalDao {

    private final Logger loggerPrincipalDao = LoggerFactory.getLogger(JdbcPrincipalDao.class);

    public JdbcPrincipalDao(DataSource dataSource) {
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
    public Principal getPrincipal(Number internalID) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(principalStar).append(" FROM ").append(principalTableName).append(" WHERE ").append(principal_id).append("= ? LIMIT 1");
        List<Principal> result = this.loggedQuery(sql.toString(), principalRowMapper, internalID);
        return result.get(0);
    }
    private final RowMapper<Principal> principalRowMapper = new RowMapper<Principal>() {
        @Override
        public Principal mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Principal result = new Principal();
            result.setURI(externalIDtoURI(rs.getString(external_id)));
            result.setDisplayName(rs.getString(principal_name));
            result.setEMail(rs.getString(e_mail));
            return result;
        }
    };

    @Override
    public Principal getPrincipalByInfo(String eMail) throws NotInDataBaseException {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(principalStar).append(" FROM ").append(principalTableName).append(" WHERE ").append("LOWER(").append(e_mail).append(")").append("= ? LIMIT 1");
        List<Principal> result = this.loggedQuery(sql.toString(), principalRowMapper, eMail.toLowerCase());
        if (result.isEmpty()) {
            throw new NotInDataBaseException("principal");
        }
        return result.get(0);
    }

    @Override
    public boolean principalIsInUse(Number principalID) {

        StringBuilder sqlAccesss = new StringBuilder("SELECT ");
        sqlAccesss.append(principal_id).append(" FROM ").append(permissionsTableName).append(" WHERE ").append(principal_id).append("= ? LIMIT 1");
        List<Number> resultTargets = this.loggedQuery(sqlAccesss.toString(), internalIDRowMapper, principalID);
        if (resultTargets.size() > 0) {
            return true;
        };

        StringBuilder sqlNotebooks = new StringBuilder("SELECT ");
        sqlNotebooks.append(owner_id).append(" FROM ").append(notebookTableName).append(" WHERE ").append(owner_id).append("= ? LIMIT 1");
        List<Number> resultNotebooks = this.loggedQuery(sqlNotebooks.toString(), ownerIDRowMapper, principalID);
        if (resultNotebooks.size() > 0) {
            return true;
        };
        return false;
    }

    @Override
    public boolean principalExists(String remoteID) {

        StringBuilder sqlTargets = new StringBuilder("SELECT ");
        sqlTargets.append(principal_id).append(" FROM ").append(principalTableName).append(" WHERE ").append(remote_id).append("= ? LIMIT 1");
        List<Number> result = this.loggedQuery(sqlTargets.toString(), internalIDRowMapper, remoteID);
        if (result.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getRemoteID(Number internalID) {
        StringBuilder requestDB = new StringBuilder("SELECT ");
        requestDB.append(remote_id).append(" FROM ").append(principalTableName).append(" WHERE ").append(principal_id).append("= ? LIMIT 1");
        List<String> result = this.loggedQuery(requestDB.toString(), remoteIDRowMapper, internalID);
        return result.get(0);
    }
    private final RowMapper<String> remoteIDRowMapper = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getString(remote_id);
        }
    };

    @Override
    public Number getPrincipalInternalIDFromRemoteID(String remoteID) throws NotInDataBaseException {

        StringBuilder requestDB = new StringBuilder("SELECT ");
        requestDB.append(principal_id).append(" FROM ").append(principalTableName).append(" WHERE ").append(remote_id).append("= ? LIMIT 1");
        List<Number> result = this.loggedQuery(requestDB.toString(), internalIDRowMapper, remoteID);
        if (result.isEmpty()) {
            throw new NotInDataBaseException("principal");
        }
        return result.get(0);
    }

    @Override
    public String getTypeOfPrincipalAccount(Number internalID) {
        StringBuilder requestDB = new StringBuilder("SELECT ");
        requestDB.append(account).append(" FROM ").append(principalTableName).append(" WHERE ").append(principal_id).append("= ? LIMIT 1");
        List<String> result = this.loggedQuery(requestDB.toString(), adminRightsRowMapper, internalID);
        return result.get(0);
    }
    private final RowMapper<String> adminRightsRowMapper = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getString(account);
        }
    };

    @Override
    public Number getDBAdminID() {
        StringBuilder requestDB = new StringBuilder("SELECT ");
        requestDB.append(principal_id).append(" FROM ").append(principalTableName).append(" WHERE account = 'admin'  LIMIT 1");
        List<Number> result = this.loggedQuery(requestDB.toString(), internalIDRowMapper);
        return result.get(0);
    }

    @Override
    public List<Number> getPrincipalIDsWithAccessForNotebook(Number notebookID, Access access) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("notebookID", notebookID);
        params.put("accessMode", access.value());

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(principal_id).append(" FROM ").append(notebookPermissionsTableName).append(" WHERE ").
                append(notebook_id).append(" = :notebookID AND ").append(this.access).append(" = :accessMode");

        return this.loggedQuery(sql.toString(), internalIDRowMapper, params);

    }

    ///////////////////// ADDERS ////////////////////////////
    @Override
    public Number addPrincipal(Principal principal, String remoteID) throws NotInDataBaseException {

        UUID externalIdentifier = UUID.randomUUID();
        String newExternalIdentifier = externalIdentifier.toString();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", newExternalIdentifier);
        params.put("principalName", principal.getDisplayName());
        params.put("email", principal.getEMail());
        params.put("remoteID", remoteID);
        params.put("accountType", "user");
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(principalTableName).append("(").append(external_id).append(",").
                append(principal_name).append(",").append(e_mail).append(",").
                append(remote_id).append(",").append(account).append(" ) VALUES (:externalId, :principalName, :email, :remoteID, :accountType)");
        final int affectedRows = this.loggedUpdate(sql.toString(), params);
        return getInternalID(externalIdentifier);
    }
    
    @Override
    public int addSpringUser(String username, String password, int strength, String salt){
       Map<String, Object> params = new HashMap<String, Object>();
        params.put("username", username);
        params.put("password", Helpers.hashPswd(password, strength, salt));
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(springUserTableName).append("(").append(springUsername).append(",").
                append(springPassword).append(") VALUES (:username, :password)");
        final int affectedRows = this.loggedUpdate(sql.toString(), params);
        return affectedRows; 
    }
    
    @Override
     public int addSpringAuthorities(String username){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("username", username);
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(springAuthoritiesTableName).append("(").append(springUsername).append(") VALUES (:username)");
        final int affectedRows = this.loggedUpdate(sql.toString(), params);
        return affectedRows;  
    }

    ////////// UPDATERS ///////////////////////
    @Override
    public boolean updateAccount(UUID externalID, String account) throws NotInDataBaseException {
        Number principalID = this.getInternalID(externalID);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID.toString());
        params.put("accountType", account);
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(principalTableName).append(" SET ").
                append(this.account).append("= :accountType").
                append(" WHERE ").append(external_id).append("= :externalId");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        if (affectedRows > 0) {
            return true;
        } else {
            logger.error("For some reason the database refuses update the account of " + externalID.toString() + " . Consult the servers' respond.");
            return false;
        }

    }

    @Override
    public int updatePrincipal(Principal principal) throws NotInDataBaseException {
        Number principalID = this.getInternalIDFromURI(principal.getURI());
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(principalTableName).append(" SET ").
                append(e_mail).append("= '").append(principal.getEMail()).append("',").
                append(principal_name).append("= '").append(principal.getDisplayName()).append("' ").
                append(" WHERE ").append(principal_id).append("= ?");
        return this.loggedUpdate(sql.toString(), principalID);

    }

   

    ////// DELETERS ////////////
    @Override
    public int deletePrincipal(Number internalID) throws PrincipalCannotBeDeleted {

        if (principalIsInUse(internalID)) {
            throw new PrincipalCannotBeDeleted(this.getURIFromInternalID(internalID));
        }
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(principalTableName).append(" where ").append(principal_id).append(" = ?");
        return this.loggedUpdate(sql.toString(), internalID);

    }
}