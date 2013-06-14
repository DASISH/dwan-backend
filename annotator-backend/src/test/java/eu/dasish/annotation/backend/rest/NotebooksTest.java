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
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created on : Jun 12, 2013, 11:31 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class NotebooksTest extends JerseyTest {

    private Mockery mockery;
    private NotebookDao notebookDao;

    public NotebooksTest() {
        super(new WebAppDescriptor.Builder(NotebookResource.class.getPackage().getName())
                .servletClass(SpringServlet.class)
                .contextParam("contextConfigLocation", "classpath*:spring-test-config/**/*.xml")
                .contextListenerClass(ContextLoaderListener.class)
                .build());

        // Get the web application context that has been instantiated in the Grizzly container
        final WebApplicationContext webAppContext = ContextLoaderListener.getCurrentWebApplicationContext();

        // Get the context and mock objects from the context by their type
        mockery = webAppContext.getBean(Mockery.class);
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
                oneOf(notebookDao).getNotebookInfos(null);
                will(returnValue(new ArrayList<NotebookInfo>()));
            }
        });
        client().addFilter(new HTTPBasicAuthFilter("userid", "userpass"));
        ClientResponse response = resource().path("notebooks").accept(MediaType.TEXT_XML).get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals(0, response.getEntity(new GenericType<List<NotebookInfo>>() {
        }).size());
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
                oneOf(notebookDao).getUsersNotebooks(null);
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
        System.out.println("testGetMetadata");
        ClientResponse response = resource().path("notebooks/_nid_/metadata").get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("metadata for _nid_", response.getEntity(String.class));
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
        System.out.println("testGetAllAnnotations");
        ClientResponse response = resource().path("notebooks/_nid_")
                .queryParam("maximumAnnotations", "123")
                .queryParam("startAnnotation", "456")
                .queryParam("orderby", "dc:789")
                .queryParam("orderingMode", "1")
                .get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("all annotations for _nid_ : 123 : 456 : dc:789 : 1", response.getEntity(String.class));
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
        ClientResponse response = resource().path("notebooks/_nid_").put(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("modifyNotebook _nid_", response.getEntity(String.class));
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
    public void testCreateNotebook() {
        System.out.println("testCreateNotebook");
        ClientResponse response = resource().path("notebooks").post(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("createNotebook _nid_ : api/notebooks/_nid_", response.getEntity(String.class));
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
        ClientResponse response = resource().path("notebooks/_nid_").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("deleteNotebook _nid_", response.getEntity(String.class));
    }
}