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

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.Access;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on : Jun 14, 2013, 3:27:04 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
// TODO: not updated fully yet. 
public class JdbcNotebookDao extends JdbcResourceDao implements NotebookDao {

    private final Logger loggerNotebookDao = LoggerFactory.getLogger(JdbcNotebookDao.class);

    public JdbcNotebookDao(DataSource dataSource) {
        setDataSource(dataSource);
        internalIdName = notebook_id;
        resourceTableName = notebookTableName;
    }

    /// GETTERS /////////
    ////////////////////////////////////////////////
    ////////////////////////////////////////////////
    @Override
    public Number getOwner(Number notebookID){
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(owner_id).append(" FROM ").append(notebookTableName).append(" WHERE ").
                append(notebook_id).append(" = ?");
        List<Number> result = this.loggedQuery(sql.toString(), ownerIDRowMapper, notebookID);
        return result.get(0);

    }

    //////////////////////////////////////////////////
    @Override
    public List<Map<Number, String>> getPermissions(Number notebookID) {

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(principal_id).append(",").append(access).append(" FROM ").append(notebookPermissionsTableName).append(" WHERE ").append(notebook_id).append("  = ?");
        return this.loggedQuery(sql.toString(), principalsAccesssRowMapper, notebookID);
    }

    /////////////
    @Override
    public List<Number> getNotebookIDs(Number principalID, Access access) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("principalID", principalID);
        params.put("accessMode", access.value());
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(notebook_id).append(" FROM ").append(notebookPermissionsTableName).append(" WHERE ").
                append(principal_id).append(" = :principalID AND ").append(this.access).append(" = :accessMode");
        return this.loggedQuery(sql.toString(), internalIDRowMapper, params);
    }

    ////////////////////////////////////////////////
    @Override
    public List<Number> getNotebookIDsOwnedBy(Number principalID) {

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(notebook_id).append(" FROM ").append(notebookTableName).append(" WHERE ").
                append(owner_id).append(" = ?");
        return this.loggedQuery(sql.toString(), internalIDRowMapper, principalID);
    }

    @Override
    public NotebookInfo getNotebookInfoWithoutOwner(Number notebookID){
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(external_id).append(" , ").append(title).
                append(" FROM ").append(notebookTableName).append(" WHERE ").
                append(notebook_id).append(" = :notebookID");
        List<NotebookInfo> result = this.loggedQuery(sql.toString(), notebookInfoRowMapper, notebookID);
        return result.get(0);
    }
    private final RowMapper<NotebookInfo> notebookInfoRowMapper = new RowMapper<NotebookInfo>() {
        @Override
        public NotebookInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            NotebookInfo notebookInfo = new NotebookInfo();
            String externalId = rs.getString(external_id);
            notebookInfo.setHref(externalIDtoHref(externalId));
            notebookInfo.setTitle(rs.getString(title));
            return notebookInfo;
        }
    };

    @Override
    public Notebook getNotebookWithoutAnnotationsAndAccesssAndOwner(Number notebookID){
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(external_id).append(" , ").append(title).append(" , ").append(last_modified).
                append(" FROM ").append(notebookTableName).append(" WHERE ").
                append(notebook_id).append(" = :notebookID");
        List<Notebook> result = this.loggedQuery(sql.toString(), notebookRowMapper, notebookID);
        return result.get(0);
    }
    private final RowMapper<Notebook> notebookRowMapper = new RowMapper<Notebook>() {
        @Override
        public Notebook mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Notebook notebook = new Notebook();
            notebook.setTitle(rs.getString(title));
            notebook.setLastModified(timeStampToXMLGregorianCalendar(rs.getString(last_modified)));
            String externalId = rs.getString(external_id);
            notebook.setId(externalId);
            notebook.setHref(externalIDtoHref(externalId));
            return notebook;
        }
    };

    /**
     *
     * UPDATERS
     *
     *
     */
    /**
     *
     * @param notebookID
     * @return true if updated, false otherwise. Logs the reason if the notebook
     * is not updated.
     */
    @Override
    public boolean updateNotebookMetadata(Number notebookID, String title, Number ownerID) {


        Map<String, Object> params = new HashMap<String, Object>();
        params.put("notebookID", notebookID);
        params.put("title", title);
        params.put("owner", ownerID);

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(notebookTableName).append(" SET ").
                append(last_modified).append("=  default,").
                append(this.title).append("= :title, ").
                append(owner_id).append("= :owner").
                append(" WHERE ").append(notebook_id).append("= :notebookID");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        if (affectedRows <= 0) {
            logger.info("For some reason no rows in the table notebooks were updated. ");
            return false;
        } else {
            if (affectedRows > 1) {
                logger.info("For some reason more than 1 row in the table notebooks were updated. that's strange.");
                return true;
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean setOwner(Number notebookID, Number ownerID) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("notebookID", notebookID);
        params.put("ownerID", ownerID);

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(notebookTableName).append(" SET ").
                append(last_modified).append("=  default,").
                append(owner_id).append("= :ownerID").
                append(" WHERE ").append(notebook_id).append("= :notebookID");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        if (affectedRows <= 0) {
            logger.info("For some reason no rows in the table notebooks were updated. ");
            return false;
        } else {
            if (affectedRows > 1) {
                logger.info("For some reason more than 1 row in the table notebooks were updated. that's strange.");
                return true;
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean updatePrincipalAccessForNotebook(Number notebookID, Number principalID, Access access) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("notebookID", notebookID);
        params.put("principalID", principalID);
        params.put("access", access.value());

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(notebookPermissionsTableName).append(" SET ").
                append(this.access).append("= :access ").
                append(" WHERE ").append(notebook_id).append("= :notebookID AND ").
                append(principal_id).append("= :principalID");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        if (affectedRows <= 0) {
            logger.info("For some reason no rows in the table notebooks-accesss were updated. ");
            return false;
        } else {
            if (affectedRows > 1) {
                logger.info("For some reason more than 1 row in the table notebooks-accesss were updated. that's strange.");
                return true;
            } else {
                return true;
            }
        }
    }

    /**
     *
     * ADDERS
     *
     *
     */
    @Override
    public Number createNotebookWithoutAccesssAndAnnotations(Notebook notebook, Number ownerID) throws NotInDataBaseException {

        UUID externalID = UUID.randomUUID();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("externalId", externalID.toString());
        params.put("owner", ownerID);
        params.put("title", notebook.getTitle());

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(notebookTableName).append("(").append(external_id).append(",").append(owner_id);
        sql.append(",").append(title).
                append(" ) VALUES (:externalId, :owner, :title)");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        return this.getInternalID(externalID);
    }

    @Override
    public boolean addAnnotationToNotebook(Number notebookID, Number annotationID) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("notebookID", notebookID);
        params.put("annotationID", annotationID);

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(notebooksAnnotationsTableName).append("(").append(notebook_id).append(",").append(annotation_id);
        sql.append(" ) VALUES (:notebookID, :annotationID)");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        return (affectedRows > 0);
    }

    @Override
    public boolean addAccessToNotebook(Number notebookID, Number principalID, Access access) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("notebookID", notebookID);
        params.put("principalID", principalID);
        params.put("access", access.value());

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(notebookPermissionsTableName).append("(").append(notebook_id).append(",").append(principal_id);
        sql.append(",").append(this.access).
                append(" ) VALUES (:notebookID, :principalID, :access)");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        return (affectedRows > 0);
    }

    /**
     *
     * DELETERS
     *
     *
     */
    @Override
    public boolean deleteAnnotationFromNotebook(Number notebookID, Number annotationID) {

        Map<String, Number> params = new HashMap();
        params.put("notebookID", notebookID);
        params.put("annotationID", annotationID);
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(notebooksAnnotationsTableName).append(" WHERE ").append(notebook_id).append(" = :notebookID AND ").
                append(annotation_id).append(" = :annotationID");
        int affectedRows = this.loggedUpdate(sql.toString(), params);
        return (affectedRows > 0);

    }

    @Override
    public boolean deleteNotebookPrincipalAccess(Number notebookID, Number principalID) {

        Map<String, Number> params = new HashMap();
        params.put("notebookID", notebookID);
        params.put("principalID", principalID);
        StringBuilder sqlAccesss = new StringBuilder("DELETE FROM ");
        sqlAccesss.append(notebookPermissionsTableName).append(" WHERE ").append(notebook_id).append(" = :notebookID AND ").
                append(principal_id).append(" = :principalID");
        int affectedRows = this.loggedUpdate(sqlAccesss.toString(), params);
        return (affectedRows > 0);

    }

    @Override
    public boolean deleteAllAnnotationsFromNotebook(Number notebookID) {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(notebooksAnnotationsTableName).append(" WHERE ").append(notebook_id).append(" = ? ");
        int affectedRows = this.loggedUpdate(sql.toString(), notebookID);
        return (affectedRows > 0);

    }

    @Override
    public boolean deleteAllAccesssForNotebook(Number notebookID) {

        StringBuilder sqlAccesss = new StringBuilder("DELETE FROM ");
        sqlAccesss.append(notebookPermissionsTableName).append(" WHERE ").append(notebook_id).append(" = ? ");
        int affectedRows = this.loggedUpdate(sqlAccesss.toString(), notebookID);
        return (affectedRows > 0);

    }

    @Override
    public boolean deleteNotebook(Number notebookID) {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(notebookTableName).append(" WHERE ").append(notebook_id).append(" = ? ");
        int affectedRows = this.loggedUpdate(sql.toString(), notebookID);
        return (affectedRows > 0);

    }
}