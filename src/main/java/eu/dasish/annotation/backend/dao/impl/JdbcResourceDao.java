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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 *
 * @author olhsha
 */
public class JdbcResourceDao extends SimpleJdbcDaoSupport implements ResourceDao{
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
       String sql = "SELECT notebook.notebook_id  FROM notebook where notebook_id = ?";
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
            Integer notebookId = rs.getInt("notebook_id");
            return notebookId;
        } 
    };
    
}
