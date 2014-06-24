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
import eu.dasish.annotation.backend.PrincipalCannotBeDeleted;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author olhsha
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/principalDao.xml"})
public class JdbcPrincipalDaoTest extends JdbcResourceDaoTest {

    @Autowired
    JdbcPrincipalDao jdbcPrincipalDao;
    
     /**
     * Test of stringURItoExternalID method
     * public String stringURItoExternalID(String uri);
     */
    @Test
    public void testHrefToExternalID() {
        System.out.println("test hrefToExternalID");
        jdbcPrincipalDao.setResourcePath("/api/principals/");
        String randomUUID = UUID.randomUUID().toString();
        String uri = "/api/principals/" + randomUUID;
        String externalID = jdbcPrincipalDao.hrefToExternalID(uri).toString();
        assertEquals(randomUUID, externalID);
    }
    
    /**
     * Test of externalIDtoURI method
     * public String externalIDtoURI(String externalID);
     */
    @Test
    public void testExternalIDtoURI() {
        System.out.println("test stringURItoExternalID");
        jdbcPrincipalDao.setResourcePath("/api/principals/");
        String randomUUID = UUID.randomUUID().toString();
        String uri = "/api/principals/"+randomUUID;
        String uriResult = jdbcPrincipalDao.externalIDtoHref(randomUUID);
        assertEquals(uri, uriResult);
    }

    /**
     * Test of getInternalID method, of class JdbcPrincipalDao. Number
     * getInternalID(UUID UUID);
     */
    @Test
    public void testGetInternalID() throws NotInDataBaseException{
        Number testOne = jdbcPrincipalDao.getInternalID(UUID.fromString("00000000-0000-0000-0000-000000000113"));
        assertEquals(3, testOne.intValue());

        try {
        Number testTwo = jdbcPrincipalDao.getInternalID(UUID.fromString("00000000-0000-0000-0000-000000000ccc"));
        assertEquals(null, testTwo);
        } catch (NotInDataBaseException e){
            System.out.println(e);
        }

    }

    /**
     * public UUID getExternalID(Number internalId)
     */
    @Test
    public void testGetExternalID() {
        assertEquals("00000000-0000-0000-0000-000000000113", jdbcPrincipalDao.getExternalID(3).toString());

    }

    @Test
    public void testGetPrincipal() {
        System.out.println("test getPrincipal");
        jdbcPrincipalDao.setResourcePath("/api/principals/");
        Principal result = jdbcPrincipalDao.getPrincipal(1);
        assertEquals("Twan", result.getDisplayName());
        assertEquals("Twan.Goosen@mpi.nl", result.getEMail());
        assertEquals("/api/principals/00000000-0000-0000-0000-000000000111", result.getHref());
        assertEquals("00000000-0000-0000-0000-000000000111", result.getId());
    }

    @Test
    public void testAddPrincipal() throws NotInDataBaseException{
        System.out.println("test addPrincipal");
        jdbcPrincipalDao.setResourcePath("/api/principals/");
        String freshPrincipalName = "Guilherme";
        String freshPrincipalEmail = "guisil@mpi.nl";

        Principal freshPrincipal = new Principal();
        freshPrincipal.setDisplayName(freshPrincipalName);
        freshPrincipal.setEMail(freshPrincipalEmail);

        Number result = jdbcPrincipalDao.addPrincipal(freshPrincipal, "secret X");
        assertEquals(12, result.intValue());
        Principal addedPrincipal = jdbcPrincipalDao.getPrincipal(result);
        assertEquals(freshPrincipalName, addedPrincipal.getDisplayName());
        assertEquals(freshPrincipalEmail, addedPrincipal.getEMail());
        assertEquals(addedPrincipal.getHref(), "/api/principals/"+addedPrincipal.getId());
        
    }

    @Test
    public void testDeletePrincipal() throws PrincipalCannotBeDeleted{
        System.out.println("test deletePrincipal");
        jdbcPrincipalDao.setResourcePath("/api/principals/");

        int result = jdbcPrincipalDao.deletePrincipal(10);
        assertEquals(1, result);
        assertEquals(0, jdbcPrincipalDao.deletePrincipal(10));
    }
    
    
    @Test
    public void testPrincipalIsInUse(){
        assertTrue(jdbcPrincipalDao.principalIsInUse(1));
        assertTrue(jdbcPrincipalDao.principalIsInUse(3));
        assertFalse(jdbcPrincipalDao.principalIsInUse(10));
    }

    @Test
    public void tesPrincipalExists() {
        System.out.println("test principalExists");      
        assertEquals(false,jdbcPrincipalDao.principalExists("guisil@mpi.nl"));       
        assertTrue(jdbcPrincipalDao.principalExists("olhsha@mpi.nl"));
    }
    
      /**
     * Test of getPrincipalIDsWithAccess method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetPrincipalIDsWithAccess() {
        System.out.println("test getPrincipalIDsWithAccess");
        List<Number> expResult = new ArrayList<Number>();
        expResult.add(2);
        expResult.add(4);
        List result = jdbcPrincipalDao.getPrincipalIDsWithAccessForNotebook(1, Access.WRITE);
        assertEquals(expResult, result);
    }
//    
//   @Test
//   public void generateHashes() {
//        System.out.println("*****"); 
//        System.out.println("generate hashes");
//        System.out.println(Helpers.hashPswd("1234", 512, "olhsha@mpi.nl"));
//        System.out.println(Helpers.hashPswd("5678", 512, "olasei@mpi.nl"));
//        System.out.println("*****");
//        
//    }

}
