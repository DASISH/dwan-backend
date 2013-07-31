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
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author olhsha
 */
public class JdbcUserDao extends JdbcResourceDao implements UserDao {

    public JdbcUserDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public Number getInternalID(UserIdentifier userIdentifier) {
        if (userIdentifier == null) {
            return null;
        }
        String sql = "SELECT " + principal_id + " FROM " + principalTableName + " WHERE " + external_id + "= ?";
        List<Number> result = getSimpleJdbcTemplate().query(sql, userIDRowMapper, userIdentifier.toString());

        if (result == null) {
            return null;
        }

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }
    private final RowMapper<Number> userIDRowMapper = new RowMapper<Number>() {
        @Override
        public Number mapRow(ResultSet rs, int rowNumber) throws SQLException {
            Number result = rs.getInt(principal_id);
            return result;
        }
    };
    
    /////////////////////////////////////////////////////////////////// 
    @Override
    public UserIdentifier getExternalID(Number internalId) {
        if (internalId == null) {
            return null;
        }
        String sql = "SELECT " + external_id + " FROM " + principalTableName + " WHERE " + principal_id + "= ?";
        List<UserIdentifier> result = getSimpleJdbcTemplate().query(sql, internalIDRowMapper, internalId.toString());

        if (result == null) {
            return null;
        }

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }
    private final RowMapper<UserIdentifier> internalIDRowMapper = new RowMapper<UserIdentifier>() {
        @Override
        public UserIdentifier mapRow(ResultSet rs, int rowNumber) throws SQLException {
            UserIdentifier result = new UserIdentifier(rs.getString(external_id));
            return result;
        }
    };
}
