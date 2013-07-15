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
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.Annotations;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.Sources;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 * Created on : Jun 27, 2013, 10:30:52 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */

/*
 *  annotation (
    annotation_id SERIAL UNIQUE NOT NULL,
    external_id UUID UNIQUE NOT NULL,
    time_stamp timestamp with time zone default now(),
    owner_id integer,
    headline tCRext,
    body_xml xml
);
 * 
 * 
 */


public class JdbcAnnotationDao extends SimpleJdbcDaoSupport implements AnnotationDao {

    public JdbcAnnotationDao(DataSource dataSource) {
        setDataSource(dataSource);
    }
    
    ////////////////////////////////////////////////////////////////////////
     @Override            
    public List<Number> getAnnotationIDs(Number notebookID) {
       String sql = "SELECT notebooks_annotations.annotation_id  FROM notebooks_annotations where notebook_id = ?";
       return getSimpleJdbcTemplate().query(sql, annotationIDRowMapper, notebookID.toString()); 
    }
    
    private final RowMapper<Number> annotationIDRowMapper = new RowMapper<Number>() {        
        @Override
        public Integer mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Integer annotationId = rs.getInt("annotation_id");
            return annotationId;
        }
    };
    
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public List<AnnotationInfo> getAnnotationInfos(Number notebookID) {        
        String sql = "SELECT annotation.* FROM annotation WHERE annotation.annotation_id  IN ?";
        return getSimpleJdbcTemplate().query(sql, annotationInfoRowMapper, getAnnotationIDs(notebookID)); 
    }
    
    private final RowMapper<AnnotationInfo> annotationInfoRowMapper = new RowMapper<AnnotationInfo>() {        
        @Override
        public AnnotationInfo mapRow(ResultSet rs, int rowNumber) throws SQLException {
           AnnotationInfo annotationInfo = new AnnotationInfo();
           annotationInfo.setOwner(getResourceREF(Integer.toString(rs.getInt("owner_id"))));
           annotationInfo.setHeadline(rs.getString("headline")); 
           annotationInfo.setTargetSources(getSources(rs.getString("body_xml")));
           return annotationInfo;
        }
    };
    
    
    /////////////////////////////////////////////////
    
    @Override
    public List<ResourceREF> getAnnotationREFs(Number notebookID) {        
        String sql = "SELECT annotation.annotation_id FROM annotation WHERE annotation.annotation_id  IN ?";
        return getSimpleJdbcTemplate().query(sql, annotationREFRowMapper, getAnnotationIDs(notebookID)); 
    }
    
    private final RowMapper<ResourceREF> annotationREFRowMapper = new RowMapper<ResourceREF>() {        
        @Override
        public ResourceREF mapRow(ResultSet rs, int rowNumber) throws SQLException {
           ResourceREF annotationREF = new ResourceREF();
           annotationREF.setRef(Integer.toString(rs.getInt("annotation_id")));
           return annotationREF;
        }
    };
    
    /////////////////////////////////////////////////
    private ResourceREF getResourceREF(String resourceID){
       ResourceREF result = new ResourceREF();
       result.setRef(resourceID); 
       return result;
    }
    
    //TODO implement when xml-body stucture is discussed!
    //BTW do we have to get source REF, not the whole sources here??
    private Sources getSources(String some_xml) {
        Sources result = new Sources();
        return result;
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public Annotations getAnnotations(Number notebookID) {        
        String sql = "SELECT annotation.* FROM annotation WHERE annotation.annotation_id  IN ?";
        return null; // not omplemented yet
        
    }
    
}
