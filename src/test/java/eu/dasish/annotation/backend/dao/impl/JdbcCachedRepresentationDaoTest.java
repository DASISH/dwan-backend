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
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/cachedRepresentationDao.xml"})
public class JdbcCachedRepresentationDaoTest extends JdbcResourceDaoTest {

    @Autowired
    JdbcCachedRepresentationDao jdbcCachedRepresentationDao;

     /**
     * Test of stringURItoExternalID method
     * public String stringURItoExternalID(String uri);
     */
    @Test
    public void testStringURItoExternalID() {
        System.out.println("test stringURItoExternalID");
        jdbcCachedRepresentationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_cached);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_cached + randomUUID;
        String externalID = jdbcCachedRepresentationDao.stringURItoExternalID(uri);
        assertEquals(randomUUID, externalID);
    }
    
    /**
     * Test of externalIDtoURI method
     * public String externalIDtoURI(String externalID);
     */
    @Test
    public void testExternalIDtoURI() {
        System.out.println("test stringURItoExternalID");
        jdbcCachedRepresentationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_cached);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_cached+randomUUID;
        String uriResult = jdbcCachedRepresentationDao.externalIDtoURI(randomUUID);
        assertEquals(uri, uriResult);
    }
    
    
    public JdbcCachedRepresentationDaoTest() {
    }

    /**
     * Test of getExternalId method, of class JdbcCachedRepresentationDao.
     * public UUID getExternalId(Number internalID);
     *
     */
    @Test
    public void testGetExternalId() {
        System.out.println("getExternalId");
        Number internalID = 1;
        UUID expResult = UUID.fromString("00000000-0000-0000-0000-000000000051");
        UUID result = jdbcCachedRepresentationDao.getExternalID(internalID);
        assertEquals(expResult, result);
    }

    /**
     * Test of getInternalId method, of class JdbcCachedRepresentationDao.
     * public Number getInternalId(UUID externalID);
     */
    @Test
    public void testGetInternalId() throws NotInDataBaseException{
        System.out.println("test getInternalID");
        UUID externalID = UUID.fromString("00000000-0000-0000-0000-000000000051");
        Number result = jdbcCachedRepresentationDao.getInternalID(externalID);
        assertEquals(1, result.intValue());
    }
    
    
    /**
     * Test of getInternalIDFromURI method,
     * public Number getInternalIDFromURI(UUID externalID);
     */
    @Test
    public void testGetInternalIDFRomURI() throws NotInDataBaseException{
        System.out.println("test getInternalIDFromURI");
        jdbcCachedRepresentationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_cached);
        String uri = TestBackendConstants._TEST_SERVLET_URI_cached +"00000000-0000-0000-0000-000000000051";
        Number result = jdbcCachedRepresentationDao.getInternalIDFromURI(uri);
        assertEquals(1, result.intValue());
    }
   
    /**
     * Test of getCachedRepresentationInfo method, of class
     * JdbcCachedRepresentationDao. public CachedRepresentationInfo
     * getCachedRepresentationInfo(Number internalID);
     */
    @Test
    public void testGetCachedRepresentationInfo() {
        System.out.println("getCachedRepresentationInfo");

        jdbcCachedRepresentationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_cached);
        CachedRepresentationInfo result = jdbcCachedRepresentationDao.getCachedRepresentationInfo(1);
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_cached + "00000000-0000-0000-0000-000000000051", result.getURI());
        assertEquals("image/png", result.getMimeType());
        assertEquals("screen-shot", result.getTool());
        assertEquals("image", result.getType());
       }

 
    
    /* Test of getCachedRepresentationsForTarget method, of class JdbcTargetDao.
     */
    @Test
    public void testGetCachedRepresentations() {
        System.out.println("test getCachedRepresentationsForTarget");
        Number TargetID = 1;
        List<Number> result = jdbcCachedRepresentationDao.getCachedRepresentationsForTarget(TargetID);
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
    }

    /**
     * Test of deleteCachedRepresentationInfo method, of class
     * JdbcCachedRepresentationDao. public int
     * deleteCachedRepresentationInfo(Number internalID);
     */
    @Test
    public void testDeleteCachedRepresentation() {
        System.out.println("deleteCachedRepresentationInfo");
        Number internalID = 6; /// deleted because no version refers to it
        int result = jdbcCachedRepresentationDao.deleteCachedRepresentation(internalID);
        assertEquals(1, result);

    }

    @Test
    public void testUpdateCachedRepresentationMetadata(){
        System.out.println("test updateCachedRepresentationInfo");
        jdbcCachedRepresentationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_cached);
        CachedRepresentationInfo cachedInfo = new CachedRepresentationInfo();
        cachedInfo.setMimeType("update mime type  1");
        cachedInfo.setType("update type  1");
        cachedInfo.setTool("update tool  1");
        cachedInfo.setURI(TestBackendConstants._TEST_SERVLET_URI_cached + "00000000-0000-0000-0000-00000000005c");
        
        int result = jdbcCachedRepresentationDao.updateCachedRepresentationMetadata(1, cachedInfo);
        assertEquals(1, result);
        
        CachedRepresentationInfo newCached = jdbcCachedRepresentationDao.getCachedRepresentationInfo(1);
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_cached + "00000000-0000-0000-0000-000000000051", newCached.getURI());
        assertEquals("update mime type  1", newCached.getMimeType());
        assertEquals("update tool  1", newCached.getTool());
        assertEquals("update type  1", newCached.getType());
        
    }
    
    @Test
    public void testUpdateCachedRepresentationBlob() throws IOException{
        System.out.println("test updateCachedRepresentationBlob");
        jdbcCachedRepresentationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_cached);
        String blobString = " test blobbie ";
        byte[] blobBytes = blobString.getBytes();
        final ByteArrayInputStream newCachedBlob = new ByteArrayInputStream(blobBytes);
        int result = jdbcCachedRepresentationDao.updateCachedRepresentationBlob(1, newCachedBlob);
        assertEquals(1, result);
        InputStream is = jdbcCachedRepresentationDao.getCachedRepresentationBlob(1);
        byte[] newBytes = new byte[blobBytes.length];
        is.read(newBytes);
        assertEquals(blobString, new String(newBytes));
        
    }
   
}
