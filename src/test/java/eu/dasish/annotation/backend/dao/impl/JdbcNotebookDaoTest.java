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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:spring-test-config/**/*.xml"})
public class JdbcNotebookDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate.execute("DROP SCHEMA PUBLIC CASCADE");
        jdbcTemplate.execute("CREATE TABLE annotation (\n"
                + "    annotation_id integer NOT NULL,\n"
                + "    uri text,\n"
                + "    time_stamp timestamp with time zone,\n"
                + "    owner_id integer,\n"
                + "    headline text,\n"
                + "    body_xml text\n"
                + ");\n"); // todo: consume the DashishAnnotatorCreate sql script here 
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getNotebookInfos method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetNotebookInfos() {
//        dataSourceer
    }

    /**
     * Test of getUsersNotebooks method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetUsersNotebooks() {
    }

    /**
     * Test of addNotebook method, of class JdbcNotebookDao.
     */
    @Test
    public void testAddNotebook() {
    }
}