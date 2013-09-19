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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
/**
 *
 * @author olhsha
 */

public class JdbcResourceDaoTest {
   
   @Autowired
   private JdbcTemplate jdbcTemplate; 
   
   private String getNormalisedSql() throws FileNotFoundException, URISyntaxException {
        // remove the unsupported sql for the test
        final URL sqlUrl = JdbcNotebookDaoTest.class.getResource("/sql/DashishAnnotatorCreate.sql");
        String sqlString = new Scanner(new File(sqlUrl.toURI()), "UTF8").useDelimiter("\\Z").next();
        for (String unknownToken : new String[]{
            "SET client_encoding",
            "CREATE DATABASE",
            "\\\\connect",
            "SET default_with_oids",
            "ALTER SEQUENCE",
            "ALTER TABLE ONLY",
            "ADD CONSTRAINT",
            "CREATE INDEX", // "ALTER TABLE ONLY [a-z]* ALTER COLUMN",
        // "ALTER TABLE ONLY [^A]* ADD CONSTRAINT"
        }) {
            sqlString = sqlString.replaceAll(unknownToken, "-- " + unknownToken);
        }
        // obsolete(?) Peter's stuff, before body has been decided to be a text with its mimetype: sqlString = sqlString.replaceAll("body_xml xml", "body_xml text");
        sqlString = sqlString.replaceAll("CACHE 1;", "; -- CACHE 1;");
        sqlString = sqlString.replaceAll("UUID", "text");
        sqlString = sqlString.replaceAll("SERIAL NOT NULL", "INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY");
        return sqlString;
    }

    private String getTestDataInsertSql() throws FileNotFoundException, URISyntaxException {
        final URL sqlUrl = JdbcNotebookDaoTest.class.getResource("/test-data/InsertTestData.sql");
        String sqlString = new Scanner(new File(sqlUrl.toURI()), "UTF8").useDelimiter("\\Z").next();
        return sqlString;
    }

    @Before
    public void setUp() throws DataAccessException, FileNotFoundException, URISyntaxException {
        jdbcTemplate.execute("DROP SCHEMA PUBLIC CASCADE");
        // consume the DashishAnnotatorCreate sql script to create the database
        jdbcTemplate.execute(getNormalisedSql());
        jdbcTemplate.execute(getTestDataInsertSql());
    }

    @After
    public void tearDown() {
    }
    
   

}
