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
package eu.dasish.annotation.backend.rest;

import eu.dasish.annotation.backend.dao.DBIntegrityService;
import org.jmock.Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-test-config/mockeryRest.xml", "/spring-test-config/mockDBIntegrityService.xml", "/spring-config/jaxbMarshallerFactory.xml",
    "/spring-test-config/mockUriInfo.xml"})
public class NotebookResourceTest {

    @Autowired
    private Mockery mockeryRest;
    @Autowired
    private DBIntegrityService daoDispatcher;
    @Autowired
    private NotebookResource notebookResource;

    public NotebookResourceTest() {
    }
    
    @Test
    public void dummy(){
        
    }
//
//    /**
//     * Test of getNotebookInfo method, of class NotebookResource.
//     */
//    @Test
//    public void testGetNotebookInfo() {
//        System.out.println("getNotebookInfo");
//        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
//        httpServletRequest.setRemotePrincipal(TestBackendConstants._TEST_UID_1_);  
//        
//        
//        mockery.checking(new Expectations() {
//            {
//                oneOf(notebookDao).getNotebookInfos(UUID.fromString(httpServletRequest.getRemotePrincipal()));                
//                will(returnValue(new ArrayList<NotebookInfo>()));
//            }
//        });
//        JAXBElement<NotebookInfoList> result = notebookResource.getNotebookInfo(httpServletRequest);
//        assertEquals(0, result.getValue().getNotebook().size()); // todo: shoudnt this return 3 infos?
//    }
//
//    /**
//     * Test of getPrincipalsNotebooks method, of class NotebookResource.
//     */
//    @Test
//    public void testGetPrincipalsNotebooks() {
//        System.out.println("getPrincipalsNotebooks");
//        mockery.checking(new Expectations() {
//            {
//                oneOf(notebookDao).getPrincipalsNotebooks(UUID.fromString(TestBackendConstants._TEST_UID_2_));
//                will(returnValue(new ArrayList<Notebook>()));
//            }
//        });
//        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
//        httpServletRequest.setRemotePrincipal(TestBackendConstants._TEST_UID_2_); 
//        List result = notebookResource.getPrincipalsNotebooks(httpServletRequest);
//        assertEquals(0, result.size());
//    }
//
//    /**
//     * Test of createNotebook method, of class NotebookResource.
//     */
//    @Test
//    public void testCreateNotebook() throws Exception {
//        System.out.println("createNotebook");
//        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
//        httpServletRequest.setRemotePrincipal(TestBackendConstants._TEST_UID_2_);
//        
//        mockery.checking(new Expectations() {
//            {
//                oneOf(notebookDao).addNotebook(UUID.fromString(httpServletRequest.getRemotePrincipal()), null);
//                will(returnValue(UUID.fromString(TestBackendConstants._TEST_NOTEBOOK_1_EXT_ID)));
//            }
//        });
//        String expResult = "/api/notebooks/00000000-0000-0000-0000-000000000001";
//        String result = notebookResource.createNotebook(httpServletRequest);  
//        assertEquals(expResult, result.substring(result.length() - expResult.length()));
//    }
//
//    /**
//     * Test of deleteNotebook method, of class NotebookResource.
//     */
//    @Test
//    public void testDeleteNotebook() {
//        System.out.println("deleteNotebook");
//        final UUID externalID = UUID.fromString(TestBackendConstants._TEST_NOTEBOOK_1_EXT_ID);
//        mockery.checking(new Expectations() {
//            {
//                oneOf(notebookDao).deleteNotebook(externalID);
//                will(returnValue(1));
//            }
//        });
//        String expResult = "1";
//        String result = notebookResource.deleteNotebook(externalID);
//        assertEquals(expResult, result);
//    }
//    
//    /**
//     * Test of getMetadata method, of class NotebookResource. Get all metadata
//     * about a specified notebook <nid>, including the information if it is
//     * private or not. GET api/notebooks/<nid>/metadata
//     */
//    @Test
//    public void testGetMetadata() {
//        System.out.println("test GetMetadata");
//        
//        final String externalID= TestBackendConstants._TEST_NOTEBOOK_3_EXT;
//        final int notebookID = 3;
//        final NotebookInfo testInfo = new ObjectFactory().createNotebookInfo();
//        
//        mockery.checking(new Expectations() {
//            {
//                oneOf(notebookDao).getInternalID(UUID.fromString(externalID));                
//                will(returnValue(notebookID));
//                
//                oneOf(notebookDao).getNotebookInfo(notebookID); 
//               will(returnValue(testInfo)); 
//            }
//        });
//        
//        JAXBElement<NotebookInfo> result = notebookResource.getMetadata(externalID);
//        NotebookInfo entity = result.getValue();
//        assertEquals(testInfo.getRef(), entity.getRef());
//        assertEquals(testInfo.getTitle(), entity.getTitle());
//    }
//
//    
//    @Test
//    public void testGetAllAnnotations() {
//        System.out.println("test getAllAnnotations");        
//        final String externalID= TestBackendConstants._TEST_NOTEBOOK_3_EXT; 
//        final UUID aIdOne= UUID.fromString("00000000-0000-0000-0000-000000000021");
//        final UUID aIdTwo= UUID.fromString("00000000-0000-0000-0000-000000000022");
//        final List<UUID> annotationIds = new ArrayList<UUID>();
//        annotationIds.add(aIdOne);
//        annotationIds.add(aIdTwo);
//        
//        mockery.checking(new Expectations() {
//            {
//                oneOf(notebookDao).getAnnotationExternalIDs(UUID.fromString(externalID));                
//                will(returnValue(annotationIds));
//                
//            }
//        });
//        
//        List<JAXBElement<UUID>> result= notebookResource.getAllAnnotations(externalID, 0, 0, null, 0);
//        assertFalse(null==result);
//        assertEquals(aIdOne, result.get(0).getValue());
//        assertEquals(aIdTwo, result.get(1).getValue());
//        
//        
//    }
}