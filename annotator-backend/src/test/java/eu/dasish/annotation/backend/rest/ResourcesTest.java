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

import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.jmock.Mockery;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author olhsha
 */
public class ResourcesTest extends JerseyTest{
    
    protected Mockery mockeryRest;    
    final protected WebApplicationContext webAppContext;
    
    public ResourcesTest(String packageName) {
        
        // debug
        
        
        super(new WebAppDescriptor.Builder(packageName)
                .servletClass(SpringServlet.class)
                .contextParam("contextConfigLocation", "classpath*:spring-test-config/**/*.xml")
                .contextListenerClass(ContextLoaderListener.class)
                .build());

        // Get the web application context that has been instantiated in the Grizzly container
        webAppContext = ContextLoaderListener.getCurrentWebApplicationContext();

        // Get the context and mock objects from the context by their type
        mockeryRest = webAppContext.getBean(Mockery.class);
    }

}
