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
import eu.dasish.annotation.backend.dao.ResourceDao;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 *
 * @author olhsha
 */
public class JdbcResourceDao extends SimpleJdbcDaoSupport implements ResourceDao {
    
    
    
    // base string constants: reTarget table Names
    final static protected String notebookTableName = "notebook";
    final static protected String annotationTableName = "annotation";
    final static protected String TargetTableName = "target";
    final static protected String cachedRepresentationTableName = "cached_representation";
    final static protected String versionTableName = "version";
    final static protected String principalTableName = "principal";
    // joint tablenames
    final static protected String notebooksAnnotationsTableName = "notebooks_annotations";
    final static protected String permissionsTableName = "annotations_principals_permissions";
    final static protected String annotationsTargetsTableName = "annotations_targets";
    final static protected String TargetsCachedRepresentationsTableName = "targets_cached_representations";
    // base string constants: field Names
    final static protected String annotation_id = "annotation_id";
    final static protected String notebook_id = "notebook_id";
    final static protected String target_id = "target_id";
    final static protected String external_id = "external_id";
    final static protected String owner_id = "owner_id";
    final static protected String headline = "headline";
    final static protected String body_text = "body_text";
    final static protected String body_mimetype = "body_mimetype";
    final static protected String title = "title";
    final static protected String principal_id = "principal_id";
    final static protected String time_stamp = "time_stamp";
    final static protected String version = "version";
    final static protected String permission = "permission_";
    final static protected String link_uri = "link_uri";
    final static protected String cached_representation_id = "cached_representation_id";
    final static protected String sibling_Target_class = "sibling_Target_class";
    final static protected String mime_type = "mime_type";
    final static protected String tool = "tool";
    final static protected String type_ = "type_";
    final static protected String file_ = "file_";
    final static protected String principal_name = "principal_name";
    final static protected String e_mail = "e_mail";
    final static protected String remote_id = "remote_id";
    final static protected String is_xml = "is_xml";
    
    // derived string constants: table+field names 
    final static protected String annotationStar = annotationTableName + ".*";
    final static protected String annotationAnnotation_id = annotationTableName + "." + annotation_id;
    final static protected String annotationExternal_id = annotationTableName + "." + external_id;
    final static protected String notebookStar = notebookTableName + ".*";
    final static protected String notebookNotebook_id = notebookTableName + "." + notebook_id;
    final static protected String notebookTitle = notebookTableName + "." + title;
    final static protected String notebookExternal_id = notebookTableName + "." + external_id;
    final static protected String notebookOwner_id = notebookTableName + "." + owner_id;
    final static protected String notebooksAnnotationsTableNameAnnotation_id = notebooksAnnotationsTableName + "." + annotation_id;
    final static protected String principalPrincipal_id = principalTableName + "." + principal_id;
    final static protected String principalExternal_id = principalTableName + "." + external_id;
    final static protected String cachedRepresentationStar = cachedRepresentationTableName + ".*";
    final static protected String TargetStar = TargetTableName + ".*";
    final static protected String principalStar = principalTableName + ".*";
    ///////////////////////////////////////////////////
    protected String internalIdName = null;
    protected String resourceTableName = null;
    protected String _serviceURI;
    ////////
    
  
    
    /////////////////// Class field SETTERS /////////////
    @Override
    public void setServiceURI(String serviceURI){
        _serviceURI = serviceURI;
    }
       
    
    

    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public Number getInternalID(UUID externalId) {
        if (externalId == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(internalIdName).append(" FROM ").append(resourceTableName).append(" WHERE ").append(external_id).append("= ? LIMIT 1");
        List<Number> sqlResult = getSimpleJdbcTemplate().query(sql.toString(), internalIDRowMapper, externalId.toString());
        return (sqlResult.isEmpty() ? null: sqlResult.get(0));
    }

    /////////////////////////////////////////////
    @Override
    public UUID getExternalID(Number internalId) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(external_id).append(" FROM ").append(resourceTableName).append(" WHERE ").append(internalIdName).append("= ? LIMIT 1");
        List<UUID> sqlResult = getSimpleJdbcTemplate().query(sql.toString(), externalIDRowMapper, internalId);
        return (sqlResult.isEmpty() ? null : sqlResult.get(0));
    }

    //////////////////////////////////////////////
    @Override
    public Number getInternalIDFromURI(String uri) {
        String externalID = stringURItoExternalID(uri);
        return getInternalID(UUID.fromString(externalID));
    }
    
    /////////////////////////////////////////////////////
    protected XMLGregorianCalendar retrieveTimeStamp(Number internalID) {
        StringBuilder sqlTime = new  StringBuilder("SELECT ");
        sqlTime.append(time_stamp).append(" FROM ").append(resourceTableName).append(" WHERE ").append(internalIdName).append("= ? LIMIT 1");
        List<XMLGregorianCalendar> timeStamp = getSimpleJdbcTemplate().query(sqlTime.toString(), timeStampRowMapper, internalID);
        if (timeStamp.isEmpty()) {
            return null;
        }
        return timeStamp.get(0);
    }
    protected final RowMapper<XMLGregorianCalendar> timeStampRowMapper = new RowMapper<XMLGregorianCalendar>() {
        @Override
        public XMLGregorianCalendar mapRow(ResultSet rs, int rowNumber) throws SQLException {
            try {
                XMLGregorianCalendar result = Helpers.setXMLGregorianCalendar(rs.getTimestamp(time_stamp));
                return result;
            } catch (DatatypeConfigurationException e) {
                System.out.println(e);
                return null;
            }
        }
    };
    ////////////////// ROW MAPPERS ///////////////////
    protected final RowMapper<Number> internalIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            int result = rs.getInt(internalIdName);
            Number resultNumber = result;
            return resultNumber;
        }
    };
    protected final RowMapper<UUID> externalIDRowMapper = new RowMapper<UUID>() {
        @Override
        public UUID mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return (UUID.fromString(rs.getString(external_id)));
        }
    };
    protected final RowMapper<Number> cachedIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getInt(cached_representation_id);
        }
    };
    protected final RowMapper<Number> TargetIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getInt(target_id);
        }
    };
   
    protected final RowMapper<Number> annotationIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getInt(annotation_id);
        }
    };
    protected final RowMapper<Number> notebookIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getInt(notebook_id);
        }
    };
    protected final RowMapper<Number> principalIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getInt(principal_id);
        }
    };

    protected final RowMapper<Number> ownerIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getInt(owner_id);
        }
    };

    
    
    @Override 
    public String externalIDtoURI(String externalID) {
        if (_serviceURI != null) {
            return _serviceURI + externalID;
        } else {
            return externalID;
        }
    }
    
    @Override 
    public String stringURItoExternalID(String stringURI) {
        return stringURI.substring(_serviceURI.length());
    }

    ////////////////////////////
    protected <T> String makeListOfValues(List<T> vals) {

        if (vals == null) {
            return null;
        }

        if (vals.isEmpty()) {
            return null;
        }

        String result = "(";
        int length = vals.size();
        for (int i = 0; i < length - 1; i++) {
            result = result + vals.get(i).toString() + ", ";
        }
        result = result + vals.get(length - 1).toString() + ")";
        return result;
    }
}
