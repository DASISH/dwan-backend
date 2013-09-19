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

import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;
import org.hsqldb.jdbc.JDBCBlobClient;
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
public class JdbcCachedRepresentationDaoTest extends JdbcResourceDaoTest{
    
    @Autowired
    JdbcCachedRepresentationDao jdbcCachedRepresentationDao; 
    
   
    public JdbcCachedRepresentationDaoTest() {
    }
    
    

    /**
     * Test of getExternalId method, of class JdbcCachedRepresentationDao.
     * public UUID getExternalId(Number internalID);
    
     */
    @Test  
    public void testGetExternalId() {
        System.out.println("getExternalId");
        Number internalID = 1;
        UUID expResult = UUID.fromString(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_EXT_ID_);
        UUID result = jdbcCachedRepresentationDao.getExternalID(internalID);
        assertEquals(expResult, result);
    }
    

    /**
     * Test of getInternalId method, of class JdbcCachedRepresentationDao.
     * public  Number getInternalId(UUID externalID);
     */
    @Test
    public void testGetInternalId() {
        System.out.println("getInternalId");
        UUID externalID = UUID.fromString(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_EXT_ID_);
        Number result = jdbcCachedRepresentationDao.getInternalID(externalID);
        assertEquals(1, result.intValue());
    }

    /**
     * Test of getCachedRepresentationInfo method, of class JdbcCachedRepresentationDao.
     *  public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID);
     */
    @Test  
    public void testGetCachedRepresentationInfo() {
        System.out.println("getCachedRepresentationInfo");
        
        jdbcCachedRepresentationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI);
        CachedRepresentationInfo result = jdbcCachedRepresentationDao.getCachedRepresentationInfo(1);        
        assertEquals(TestBackendConstants._TEST_SERVLET_URI+TestBackendConstants._TEST_CACHED_REPRESENTATION_1_EXT_ID_, result.getRef());
        assertEquals(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_MIME_TYPE_, result.getMimeType());
        assertEquals(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_TOOL_, result.getTool());
        assertEquals(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_TYPE_, result.getType());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI+TestBackendConstants._TEST_CACHED_REPRESENTATION_1_EXT_ID_, result.getRef());
    }

    /**
     * Test of getCachedRepresentationBlob method, of class JdbcCachedRepresentationDao.
     *  public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID);
     */
    @Test  
    public void testGetCachedRepresentationBlob() throws SQLException, UnsupportedEncodingException{
        System.out.println("getCachedRepresentationBlob ");
        Blob result = jdbcCachedRepresentationDao.getCachedRepresentationBlob(1);
        int lengthBlob = 2;
        byte[] resultBytes = result.getBytes(1, lengthBlob);
        assertEquals(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_BLOB_BYTE_1, resultBytes[0]);
        assertEquals(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_BLOB_BYTE_2, resultBytes[1]);
    }
    
    
    /**
     * Test of deleteCachedRepresentationInfo method, of class JdbcCachedRepresentationDao.
     *  public int deleteCachedRepresentationInfo(Number internalID);
     */
    @Test  
    public void testDeleteCachedRepresentationInfo() {
        System.out.println("deleteCachedRepresentationInfo");
        Number internalID = 6; /// deleted because no version refers to it
        int result = jdbcCachedRepresentationDao.deleteCachedRepresentation(internalID);
        assertEquals(1, result);
        
        int resultTwo = jdbcCachedRepresentationDao.deleteCachedRepresentation(internalID);
        assertEquals(0, resultTwo);
        
        Number internalIDDoNotDelete = 5;
        int resultThree =jdbcCachedRepresentationDao.deleteCachedRepresentation(internalIDDoNotDelete);
        assertEquals(0, resultThree);
    }

    /**
     * Test of addCachedRepresentationInfo method, of class JdbcCachedRepresentationDao.
     * public CachedRepresentationInfo addCachedRepresentationInfo(CachedRepresentationInfo cached);    
     */
    @Test  
    public void testAddCachedRepresentation() throws SerialException, SQLException{
        System.out.println("addCachedRepresentation");
        
        CachedRepresentationInfo cachedInfo = new CachedRepresentationInfo();
        cachedInfo.setMimeType("text/plain");
        cachedInfo.setTool("vi");
        cachedInfo.setType("text");
        cachedInfo.setRef(null);
        
        String  blobString = "111";
        byte[] blobBytes = blobString.getBytes();        
        final Blob cachedBlob = new SerialBlob(blobBytes);
        
        Number result = jdbcCachedRepresentationDao.addCachedRepresentation(cachedInfo, cachedBlob);
        // checking
        CachedRepresentationInfo addedCachedInfo = jdbcCachedRepresentationDao.getCachedRepresentationInfo(result);
        assertEquals(8, result.intValue());
        assertEquals("text/plain", addedCachedInfo.getMimeType());
        assertEquals("vi", addedCachedInfo.getTool());
        assertEquals("text", addedCachedInfo.getType());
        assertFalse(addedCachedInfo.getRef() == null); // new non-null external identifier should be assigned
        
        Blob addedBlob = jdbcCachedRepresentationDao.getCachedRepresentationBlob(result);
        int lengthBlob = 3;
        String addedBlobString = new String(addedBlob.getBytes(1, lengthBlob));
        assertEquals(blobString, addedBlobString);
      
    }
    
  
}
