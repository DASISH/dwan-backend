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

import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:spring-test-config/**/*.xml"})
public class JdbcNotebookDaoTest {

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
        sqlString = sqlString.replaceAll("body_xml xml", "body_xml text");
        sqlString = sqlString.replaceAll("CACHE 1;", "; -- CACHE 1;");
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

    /**
     * Test of getNotebookInfos method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetNotebookInfos() {
        JdbcNotebookDao notebookDao = new JdbcNotebookDao(jdbcTemplate.getDataSource());
        final List<NotebookInfo> notebookInfoList = notebookDao.getNotebookInfos(1);
        assertEquals(2, notebookInfoList.size());
        assertEquals("a notebook", notebookInfoList.get(0).getTitle());
    }

    /**
     * Test of getUsersNotebooks method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetUsersNotebooks() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        JdbcNotebookDao notebookDao = new JdbcNotebookDao(jdbcTemplate.getDataSource());
        final List<Notebook> notebooks = notebookDao.getUsersNotebooks(1);
        assertEquals(2, notebooks.size());
        assertEquals("a notebook", notebooks.get(0).getTitle());
        assertEquals("http://123456", notebooks.get(0).getURI());
        assertNotNull(notebooks.get(0).getTimeStamp());
        assertEquals(year, notebooks.get(0).getTimeStamp().getYear());
        assertEquals(month + 1, notebooks.get(0).getTimeStamp().getMonth());
        assertEquals(day, notebooks.get(0).getTimeStamp().getDay());
        final List<Notebook> notebooksEmpty = notebookDao.getUsersNotebooks(0);
        assertEquals(0, notebooksEmpty.size());
    }

    /**
     * Test of addNotebook method, of class JdbcNotebookDao.
     */
    @Test
    public void testAddNotebook() throws URISyntaxException {
        JdbcNotebookDao notebookDao = new JdbcNotebookDao(jdbcTemplate.getDataSource());
        final Number addedNotebookId = notebookDao.addNotebook(1, new URI("http://123456"), "a title");
        assertEquals(2, addedNotebookId);
    }

    /**
     * Test of deleteNotebook method, of class JdbcNotebookDao.
     */
    @Test
    public void testDeleteNotebook() {
        System.out.println("deleteNotebook");
        Number notebookId = 1;
        JdbcNotebookDao instance = new JdbcNotebookDao(jdbcTemplate.getDataSource());;
        int result = instance.deleteNotebook(notebookId);
        assertEquals(1, result);
    }
}