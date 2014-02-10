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

import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.Permission;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Created on : Jun 14, 2013, 3:27:04 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
// TODO: not updated fully yet. 
public class JdbcNotebookDao extends JdbcResourceDao implements NotebookDao {

//    @Autowired
//    private AnnotationDao jdbcAnnotationDao;
    public JdbcNotebookDao(DataSource dataSource) {
        setDataSource(dataSource);
        internalIdName = notebook_id;
        resourceTableName = notebookTableName;
    }

    /// GETTERS /////////
    ////////////////////////////////////////////////
    @Override
    public List<Number> getNotebookIDs(Number userID, Permission acessMode) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("principalID", userID);
        params.put("accessMode", acessMode.value());
        String sql = "SELECT " + notebook_id + " FROM " + notebookPermissionsTableName + " WHERE " + principalPrincipal_id + " = :principalID" + " AND " + permission + " = :accessMode";
        return getSimpleJdbcTemplate().query(sql, internalIDRowMapper, params);
    }

    ////////////////////////////////////////////////
    @Override
    public List<Number> getOwnerNotebookIDs(Number userID) {
        String sql = "SELECT " + notebook_id + " FROM " + notebookTableName + " WHERE " + owner_id + " = ?";
        return getSimpleJdbcTemplate().query(sql, internalIDRowMapper, userID);
    }

  

    @Override
    public List<Number> getPrincipalIDsWithPermission(Number notebookID, Permission permission) {
         if (notebookID == null) {
            return null;
        }         
        Map<String, Object> params= new HashMap<String, Object>();
        params.put("notebookID", notebookID);
        params.put("permission", permission.value());
        
        String sql = "SELECT  " + owner_id + " FROM " + notebookPermissionsTableName + " WHERE " + notebook_id +" = :notebookID AND " +this.permission + " = :permission";
        return getSimpleJdbcTemplate().query(sql, principalIDRowMapper, params);
       
    }

    @Override
    public NotebookInfo getNotebookInfo(Number notebookID) {
        if (notebookID == null) {
            return null;
        }
        String sql = "SELECT  " + notebookExternal_id + "," + notebookTitle + " FROM " + notebookTableName + " where " + notebook_id + " = ? LIMIT 1";
        List<NotebookInfo> result = getSimpleJdbcTemplate().query(sql, notebookInfoRowMapper, notebookID);
        return (!result.isEmpty() ? result.get(0) : null);
    }
    private final RowMapper<NotebookInfo> notebookInfoRowMapper = new RowMapper<NotebookInfo>() {
        @Override
        public NotebookInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
            NotebookInfo notebookInfo = new NotebookInfo();
            notebookInfo.setRef(externalIDtoURI(rs.getString(external_id)));
            notebookInfo.setTitle(rs.getString(title));
            return notebookInfo;
        }
    };

    @Override
    public Notebook getNotebookWithoutAnnotationsAndPermissions(Number notebookID) {
        if (notebookID == null) {
            return null;
        }
        String sql = "SELECT  " + notebookExternal_id + "," + notebookTitle + "," + last_modified + " FROM " + notebookTableName + " where " + notebook_id + " = ? LIMIT 1";
        List<Notebook> result = getSimpleJdbcTemplate().query(sql, notebookRowMapper, notebookID);
        return (!result.isEmpty() ? result.get(0) : null);
    }
    private final RowMapper<Notebook> notebookRowMapper = new RowMapper<Notebook>() {
        @Override
        public Notebook mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Notebook notebook = new Notebook();
            notebook.setTitle(rs.getString(title));
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(rs.getTimestamp(last_modified));
            try {
                XMLGregorianCalendar gregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
                notebook.setLastModified(gregorianCalendar);
            } catch (DatatypeConfigurationException exception) {
                throw new SQLException(exception);
            }
            notebook.setURI(externalIDtoURI(rs.getString(external_id)));
            return notebook;
        }
    };
    
    
    @Override
    public List<Number> getAnnotations(int maximumAnnotations, int startannotation, String orderedBy, boolean orderingMode){
        return null;
    }
    
    
       /**
     * 
     * UPDATERS 
     * 
     * 
     */
    
    
    
    /**
     * 
     * @param notebookID
     * @return true if updated, false otherwise. Logs the reason if the notebook is not updated.
     */
    @Override
    public boolean updateNotebookMetadata(Number notebookID){
        return false;
    }
     /**
     * 
     * ADDERS
     * 
     * 
     */
    
    @Override
    public Number createNotebook(Notebook notebook){
        return null;
    }
    
    @Override
    public boolean addAnnotationToNotebook(Number noteboookId, Number AnnotationID){
        return false;
    }
    
    
    /**
     * 
     * DELETERS (ADDER)
     * 
     * 
     */
    
    @Override
    public boolean deleteannotationFromNotebook(Number notebookID, Number annotationID){
        return false;
    }
    
    @Override
    public boolean deleteNotebook(Number notebookID){
        return false;
    }
}