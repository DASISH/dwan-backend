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
import eu.dasish.annotation.backend.dao.ResourceDao;
import eu.dasish.annotation.schema.Access;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 *
 * @author olhsha
 */
public class JdbcResourceDao extends SimpleJdbcDaoSupport implements ResourceDao {

    // base string constants: table Names
    final static protected String notebookTableName = "notebook";
    final static protected String annotationTableName = "annotation";
    final static protected String targetTableName = "target";
    final static protected String cachedRepresentationTableName = "cached_representation";
    final static protected String versionTableName = "version";
    final static protected String principalTableName = "principal";
    // joint tablenames
    final static protected String notebooksAnnotationsTableName = "notebooks_annotations";
    final static protected String permissionsTableName = "annotations_principals_accesss";
    final static protected String notebookAccesssTableName = "notebooks_principals_accesss";
    final static protected String annotationsTargetsTableName = "annotations_targets";
    final static protected String targetsCachedRepresentationsTableName = "targets_cached_representations";
    // base string constants: field Names
    final static protected String account = "account";
    final static protected String admin = "admin";
    final static protected String developer = "developer";
    final static protected String principal = "principal";
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
    final static protected String notebookOwner_id = "owner_id";
    final static protected String last_modified = "last_modified";
    final static protected String version = "version";
    final static protected String access = "access_";
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
    final static protected String public_ = "public_";
    final static protected String fragment_descriptor = "fragment_descriptor";
    final static protected String fragment_descriptor_in_cached = "fragment_descriptor_in_cached";
    final static protected String annotationStar = annotationTableName + ".*";
    final static protected String cachedRepresentationStar = cachedRepresentationTableName + ".*";
    final static protected String targetStar = targetTableName + ".*";
    final static protected String principalStar = principalTableName + ".*";
    ////////////////////////////////
    ///////////////////////////////////////////////////
    protected String internalIdName = null;
    protected String resourceTableName = null;
    protected String _serviceURI;
    private final Logger _logger = LoggerFactory.getLogger(JdbcResourceDao.class);
    protected String nullArgument = "Null argument is given to the method.";

    protected <T> List<T> loggedQuery(String sql, RowMapper<T> rm, Map<String, ?> args) {
        List<T> result = getSimpleJdbcTemplate().query(sql, rm, args);
        String sqlStatement = replaceInString(sql, args);
        logger.debug("\n SQL query: " + sqlStatement + "\n");
        return result;
    }

    protected <T, E> List<T> loggedQuery(String sql, RowMapper<T> rm, E arg) {
        List<T> result = getSimpleJdbcTemplate().query(sql, rm, arg);
        logger.debug("\n SQL query: " + this.replaceQuestionmarkInString(sql, arg) + "\n");
        return result;
    }

    protected <T> List<T> loggedQuery(String sql, RowMapper<T> rm) {
        logger.debug("\n SQL query: " + sql + "\n");
        return getSimpleJdbcTemplate().query(sql, rm);
    }

    protected int loggedUpdate(String sql, Map<String, ?> args) {
        int result = getSimpleJdbcTemplate().update(sql, args);
        String sqlStatement = replaceInString(sql, args);
        logger.debug("\n SQL query: " + sqlStatement + "\n");
        return result;
    }

    protected <T> int loggedUpdate(String sql, T arg) {
        int result = getSimpleJdbcTemplate().update(sql, arg);
        logger.debug("\n SQL query: " + this.replaceQuestionmarkInString(sql, arg) + "\n");
        return result;
    }

    private String replaceInString(String string, Map<String, ?> map) {
        String result = (new StringBuilder(string)).toString();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (map.get(key) != null) {
                result = result.replaceAll(":" + key, map.get(key).toString());
            } else {
                result = result.replaceAll(":" + key, "null!!");
            }
        }
        return result;
    }

    private <T> String replaceQuestionmarkInString(String string, T arg) {
        String result = (new StringBuilder(string)).toString();
        CharSequence questionMark = "?";
        CharSequence replacement = arg.toString();
        result = result.replace(questionMark, replacement);
        return result;
    }

    /////////////////// Class field SETTERS /////////////
    @Override
    public void setServiceURI(String serviceURI) {
        _serviceURI = serviceURI;
    }

    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public Number getInternalID(UUID externalId) throws NotInDataBaseException {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(internalIdName).append(" FROM ").append(resourceTableName).append(" WHERE ").append(external_id).append("= ? LIMIT 1");
        List<Number> sqlResult = this.loggedQuery(sql.toString(), internalIDRowMapper, externalId.toString());
        if (sqlResult.isEmpty()) {
            throw new NotInDataBaseException("resource", externalId.toString());
        }

        return sqlResult.get(0);

    }

    /////////////////////////////////////////////
    @Override
    public UUID getExternalID(Number internalID) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(external_id).append(" FROM ").append(resourceTableName).append(" WHERE ").append(internalIdName).append("= ? LIMIT 1");
        List<UUID> sqlResult = this.loggedQuery(sql.toString(), externalIDRowMapper, internalID);
        return sqlResult.get(0);
    }

    //////////////////////////////////////////////
    @Override
    public Number getInternalIDFromURI(String uri) throws NotInDataBaseException {
        String externalID = this.stringURItoExternalID(uri);
        try {
            UUID externalUUID = UUID.fromString(externalID);
            return this.getInternalID(externalUUID);
        } catch (IllegalArgumentException e) {
            _logger.debug(externalID + " is not a valid <uuid>.  Therefore, I expect that it is a temporary idendifier of a new resource that is not yet in the database and return null.");
            throw new NotInDataBaseException("resource", externalID);
        }
    }

    //////////////////////////////////////////////
    @Override
    public String getURIFromInternalID(Number internalID) {
        return externalIDtoURI(getExternalID(internalID).toString());
    }

    // override for notebooks and annotations
    @Override
    public List<Map<Number, String>> getPermissions(Number resourceID) {
        return (new ArrayList());
    }
    
    @Override
    public Access getPublicAttribute(Number resourceID) {
        return Access.NONE;
    }

    /////////////////////////////////////////////////////
    protected XMLGregorianCalendar retrieveTimeStamp(Number internalID) {
        StringBuilder sqlTime = new StringBuilder("SELECT ");
        sqlTime.append(last_modified).append(" FROM ").append(resourceTableName).append(" WHERE ").append(internalIdName).append("= ? LIMIT 1");
        List<XMLGregorianCalendar> timeStamp = this.loggedQuery(sqlTime.toString(), timeStampRowMapper, internalID);
        return timeStamp.get(0);
    }
    protected final RowMapper<XMLGregorianCalendar> timeStampRowMapper = new RowMapper<XMLGregorianCalendar>() {
        @Override
        public XMLGregorianCalendar mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return timeStampToXMLGregorianCalendar(rs.getString(last_modified));
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
    protected final RowMapper<Number> ownerIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            return rs.getInt(owner_id);
        }
    };
    protected final RowMapper<Map<Number, String>> principalsAccesssRowMapper = new RowMapper<Map<Number, String>>() {
        @Override
        public Map<Number, String> mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Map<Number, String> result = new HashMap<Number, String>();
            result.put(rs.getInt(principal_id), rs.getString(access));
            return result;
        }
    };
     protected final RowMapper<Access> public_RowMapper = new RowMapper<Access>() {
        @Override
        public Access mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Access result = Access.fromValue(rs.getString(public_));
            return result;
        }
    };

    ////// END ROW MAPPERS /////
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
        if (stringURI.length() > _serviceURI.length()) {
            return stringURI.substring(_serviceURI.length());
        } else {
            logger.debug(stringURI + " does not have the form <service url>/<isentifier>, therefore I return the input value.");
            return stringURI;
        }
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

    /////////////////////////
    protected XMLGregorianCalendar timeStampToXMLGregorianCalendar(String ts) {
        String tsAdjusted = ts.replace(' ', 'T') + "Z";
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(tsAdjusted);
        } catch (DatatypeConfigurationException dtce) {
            _logger.error(" ", dtce);
            return null;
        }

    }
}
