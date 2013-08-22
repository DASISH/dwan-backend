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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.NotebookInfos;
import eu.dasish.annotation.schema.ObjectFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 * Created on : Jun 12, 2013, 11:31 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class NotebooksTest extends ResourcesTest {

    private NotebookDao notebookDao;

    public NotebooksTest() {
        super(NotebookResource.class.getPackage().getName());
        notebookDao = webAppContext.getBean(NotebookDao.class);
    }

    /**
     * Test of getNotebookInfo method, of class NotebookResource. Returns
     * notebook-infos for the notebooks accessible to the current user. GET
     * api/notebooks
     */
    @Test
    public void testGetNotebookInfo() {
        System.out.println("testGetNotebookInfo");
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookInfos(with(any(UUID.class)));
                will(returnValue(new ArrayList<NotebookInfo>()));
            }
        });
        client().addFilter(new HTTPBasicAuthFilter("userid", "userpass"));
        ClientResponse response = resource().path("notebooks").accept(MediaType.TEXT_XML).get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals(0, response.getEntity(new GenericType<NotebookInfos>() {
        }).getNotebook().size());
    }

    /**
     * Test of getUsersNotebooks method, of class NotebookResource. Returns the
     * list of all notebooks owned by the current logged user. GET
     * api/notebooks/owned
     */
    @Test
    public void testGetUsersNotebooks() {
        System.out.println("testGetUsersNotebooks");
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).getUsersNotebooks(with(any(UUID.class)));
                will(returnValue(new ArrayList<Notebook>()));
            }
        });
        client().addFilter(new HTTPBasicAuthFilter("userid", "userpass"));
        ClientResponse response = resource().path("notebooks/owned").accept(MediaType.TEXT_XML).get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals(0, response.getEntity(new GenericType<List<Notebook>>() {
        }).size());
    }

    /**
     * Test of getReaders method, of class NotebookResource. Returns the list of
     * <uid>
     * who allowed to read the annotations from notebook. GET
     * api/notebooks/<nid>/readers
     */
    @Test
    public void testGetReaders() {
        System.out.println("testGetReaders");
        ClientResponse response = resource().path("notebooks/_nid_/readers").get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("readers for _nid_", response.getEntity(String.class));
    }

    /**
     * Test of getWriters method, of class NotebookResource. Returns the list of
     * <uid>
     * that can add annotations to the notebook. GET api/notebooks/<nid>/writers
     */
    @Test
    public void testGetWriters() {
        System.out.println("testGetWriters");
        ClientResponse response = resource().path("notebooks/_nid_/writers").get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("writers for _nid_", response.getEntity(String.class));
    }

    /**
     * Test of getMetadata method, of class NotebookResource. Get all metadata
     * about a specified notebook <nid>, including the information if it is
     * private or not. GET api/notebooks/<nid>/metadata
     */
    @Test
    public void testGetMetadata() {
        System.out.println("test GetMetadata");
        
        final String externalIDstring= TestBackendConstants._TEST_NOTEBOOK_3_EXT;
        final int notebookID = 3;
        final NotebookInfo testInfo = new ObjectFactory().createNotebookInfo();
        
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).getInternalID(UUID.fromString(externalIDstring));                
                will(returnValue(notebookID));
                
                oneOf(notebookDao).getNotebookInfo(notebookID); 
               will(returnValue(testInfo)); 
            }
        });
        
        final String requestUrl = "notebooks/" + externalIDstring+"/metadata";
        System.out.println("requestUrl: " + requestUrl);
        ClientResponse response = resource().path(requestUrl).type(MediaType.TEXT_XML).get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        NotebookInfo entity = response.getEntity(NotebookInfo.class);
        assertEquals(testInfo.getRef(), entity.getRef());
        assertEquals(testInfo.getTitle(), entity.getTitle());
    }

    /**
     * Test of getAllAnnotations method, of class NotebookResource. Get the list
     * of all annotations <aid>-s contained within a Notebook with related
     * metadata. Parameters: <nid>, optional maximumAnnotations specifies the
     * maximum number of annotations to retrieve (default -1, all annotations),
     * optional startAnnotation specifies the starting point from which the
     * annotations will be retrieved (default: -1, start from the first
     * annotation), optional orderby, specifies the RDF property used to order
     * the annotations (default: dc:created ), optional orderingMode specifies
     * if the results should be sorted using a descending order desc=1 or an
     * ascending order desc=0 (default: 0 ). GET
     * api/notebooks/<nid>?maximumAnnotations=limit&startAnnotation=offset&orderby=orderby&orderingMode=1|0
     */
    @Test
    public void testGetAllAnnotations() {
        System.out.println("test GetMetadata");       
        final String externalIDstring= TestBackendConstants._TEST_NOTEBOOK_3_EXT; 
        final UUID aIdOne= UUID.fromString(TestBackendConstants._TEST_ANNOT_2_EXT);
        final UUID aIdTwo= UUID.fromString(TestBackendConstants._TEST_ANNOT_3_EXT);
        final List<UUID> annotationIds = new ArrayList<UUID>();
        annotationIds.add(aIdOne);
        annotationIds.add(aIdTwo);
        
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).getAnnotationExternalIDs(with(aNonNull(UUID.class)));                
                will(returnValue(annotationIds));
                
            }
        });
        
        final String requestUrl = "notebooks/"+externalIDstring;
        ClientResponse response = resource().path(requestUrl)
                .queryParam("maximumAnnotations", "123")
                .queryParam("startAnnotation", "456")
                .queryParam("orderby", "dc:789")
                .queryParam("orderingMode", "1")
                .get(ClientResponse.class);
        System.out.println("requestUrl: " + requestUrl);
        assertEquals(200, response.getStatus());
        List<JAXBElement<UUID>> result = response.getEntity(new GenericType<List<JAXBElement<UUID>>>() {});
        assertEquals(aIdOne, result.get(0).getValue());
        assertEquals(aIdTwo, result.get(1).getValue());
    }

    /**
     * Test of modifyNotebook method, of class NotebookResource. Modify metadata
     * of
     * <nid>. The new notebook’s name must be sent in request’s body. PUT
     * /notebooks/<nid>
     */
    @Test
    public void testModifyNotebook_String() {
        System.out.println("testModifyNotebook_String");
        final Notebook notebook = new Notebook();
        // this JAXBElement should be returned by the generated ObjectFactory, however it is not. This could be due to name space polution in the schema?
        // todo: this JAXBElement should be removed and replaced by the same as returned by the ObjectFactory when cause of it not being auto generated is resolved, in the mean time this line below makes it clear where the issue starts from.
        // see ticket #348
        final JAXBElement<Notebook> jaxbElement = new JAXBElement<Notebook>(new QName("http://www.dasish.eu/ns/addit", "notebook"), Notebook.class, null, notebook);
        notebook.setTitle("a title");
        ClientResponse response = resource().path("notebooks/_nid_").type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).put(ClientResponse.class, jaxbElement);
        assertEquals(200, response.getStatus());
        assertEquals("modifyNotebook _nid_a title", response.getEntity(String.class));
    }

    /**
     * Test of modifyNotebook method, of class NotebookResource. Adds an
     * annotation
     * <aid> to the list of annotations of <nid>. PUT /notebooks/<nid>/<aid>
     */
    @Test
    public void testAddAnnotation_String_String() {
        System.out.println("testAddAnnotation_String_String");
        ClientResponse response = resource().path("notebooks/_nid_/_aid_").put(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("addAnnotation _nid_ : _aid_", response.getEntity(String.class));
    }

    /**
     * Test of createNotebook method, of class NotebookResource. Creates a new
     * notebook. This API returns the <nid> of the created Notebook in
     * response’s payload and the full URL of the notebook adding a Location
     * header into the HTTP response. The name of the new notebook can be
     * specified sending a specific payload. POST api/notebooks/
     */
    @Test
    public void testCreateNotebook() throws URISyntaxException {
        System.out.println("testCreateNotebook");
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).addNotebook(with(any(UUID.class)), with(any(String.class)));
                will(returnValue(UUID.fromString(TestBackendConstants._TEST_NOTEBOOK_1_EXT_ID)));
            }
        });
        ClientResponse response = resource().path("notebooks").post(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("/api/notebooks/00000000-0000-0000-0000-000000000001", new URI(response.getEntity(String.class)).getPath());
    }

    /**
     * Test of createAnnotation method, of class NotebookResource. Creates a new
     * annotation in <nid>. The content of an annotation is given in the request
     * body. In fact this is a short cut of two actions: POST
     * api/notebooks/<nid>
     */
    @Test
    public void testCreateAnnotation() {
        System.out.println("testCreateAnnotation");
        ClientResponse response = resource().path("notebooks/_nid_").post(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("annotation _nid_ : api/notebooks/_nid_", response.getEntity(String.class));
    }

    /**
     * Test of deleteNotebook method, of class NotebookResource. Delete <nid>.
     * Annotations stay, they just lose connection to <nid>. DELETE
     * api/notebooks/<nid>
     */
    @Test
    public void testDeleteNotebook() {
        System.out.println("testModifyNotebook_String");
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).deleteNotebook(UUID.fromString(TestBackendConstants._TEST_NOTEBOOK_2_EXT_ID));
                will(returnValue(1));
            }
        });
        final String requestUrl = "notebooks/" + TestBackendConstants._TEST_NOTEBOOK_2_EXT_ID;
        System.out.println("requestUrl: " + requestUrl);
        ClientResponse response = resource().path(requestUrl).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("1", response.getEntity(String.class));
    }
}