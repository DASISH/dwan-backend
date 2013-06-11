/**
 * Copyright (C) 2013 DASISH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.dasish.annotation.backend;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class MyResourceTest extends JerseyTest {

    public MyResourceTest() throws TestContainerException {
        super(new WebAppDescriptor.Builder(MyResource.class.getPackage().getName())
                // .servletClass(SpringServlet.class)
                // .contextParam("contextConfigLocation", "classpath:testApplicationContext.xml")
                // .contextListenerClass(ContextLoaderListener.class)
                .build());
    }

    /**
     * Test of getIt method, of class MyResource.
     */
    @Test
    public void testGetIt() {
        System.out.println("getIt");
        ClientResponse response = resource().path("myresource").get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("Hi there!", response.getEntity(String.class));
//        final String hello = target("hello").request().get(String.class);
//        assertEquals("Hello World!", hello);
    }
}