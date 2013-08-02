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

import eu.dasish.annotation.backend.dao.ResourceDao;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 *
 * @author olhsha
 */
public class JdbcResourceDao extends SimpleJdbcDaoSupport implements ResourceDao{
    
    // base string constants: resource table Names
    final static protected String notebookTableName = "notebook";
    final static protected String annotationTableName = "annotation";
    final static protected String sourceTableName = "source";    
    final static protected String sourcesTableName = "target_source";
    final static protected String cachedRepresentationTableName = "cached_representation_info";
    final static protected String versionTableName = "version";
    final static protected String principalTableName = "principal";
    // joint tablenames
    
    final static protected String notebooksAnnotationsTableName = "notebooks_annotations";
    final static protected String permissionsTableName = "annotations_principals_permissions";
    final static protected String annotationsSourcesTableName = "annotations_target_sources";
    final static protected String versionsCachedRepresentationsTableName = "versions+cached_representations";
    
    
    // base string constants: field Names
    final static protected String annotation_id = "annotation_id";
    final static protected String notebook_id = "notebook_id";
    final static protected String source_id = "source_id";
    final static protected String external_id = "external_id";
    final static protected String owner_id = "owner_id";
    final static protected String headline = "headline";
    final static protected String body_xml = "body_xml";
    final static protected String title="title";
    final static protected String principal_id = "principal_id";
    final static protected String time_stamp = "time_stamp";
    final static protected String permission = "permission_";
    final static protected String target_source_id = "target_source_id";
    final static protected String link = "link";
    final static protected String version = "version";
    final static protected String cached_representation_id = "cached_representation_id";
    final static protected String version_id = "version_id";
    final static protected String mime_type = "mime_type";
    final static protected String tool = "tool";
    final static protected String type_ = "type_";
    final static protected String where_is_the_file = "where_is_the_file";
    
    // derived string constants: table+field names 
    
    final static protected String annotationStar = annotationTableName+".*";
    final static protected String annotationAnnotation_id = annotationTableName+"."+annotation_id;
    final static protected String annotationExternal_id = annotationTableName+"."+external_id;
    
    final static protected String notebookStar = notebookTableName+".*";
    final static protected String notebookNotebook_id = notebookTableName+"."+notebook_id;
    final static protected String notebookTitle=notebookTableName+"."+title;
    final static protected String notebookExternal_id = notebookTableName+"."+external_id;
    final static protected String notebookOwner_id = notebookTableName+"."+owner_id;
    
    
    final static protected String notebooksAnnotationsTableNameAnnotation_id = notebooksAnnotationsTableName+"."+annotation_id;
    
    
    final static protected String principalPrincipal_id = principalTableName+"."+principal_id;
    final static protected String principalExternal_id = principalTableName+"."+external_id;
    
    
    final static protected String cachedRepresentationStar = cachedRepresentationTableName+".*";
    
    final static protected String versionStar = versionTableName+".*";
    
    //////////////////////////////////////////
    /**
     * 
     * @param notebookID
     * @return false if notebookID == null or the notebook with notebookID is not in the DB;
     * @return true if the notebook with notebookID in the DB
     */
    @Override
    public boolean isNotebookInTheDataBase(Number notebookID){
        
        if (notebookID == null) {
           return false;
       } 
       String sql = "SELECT "+notebookNotebook_id+"  FROM notebook where "+notebook_id+" = ?";
       List<Number> result=getSimpleJdbcTemplate().query(sql, isNotebookInTheDataBaseRowMapper, notebookID.toString());
       if (result == null) {
           return false;
       }
       if (result.isEmpty()) {
           return false;
       }
       return true; 
    }
    
    private final RowMapper<Number> isNotebookInTheDataBaseRowMapper = new RowMapper<Number>() {        
        @Override
        public Integer mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Integer notebookId = rs.getInt(notebook_id);
            return notebookId;
        } 
    };
    
    
    protected <T> String makeListOfValues(List<T> vals) {
        
        if (vals == null) {
            return "()";
        }
        
        if (vals.isEmpty()) {            
            return "()";
        }
        
        String result = "(";
        int length = vals.size();
        for (int i=0; i<length-1; i++){
            result = result + vals.get(i).toString() +", ";
        }
        result = result +vals.get(length-1).toString()+")";
        return result;
    }
}
