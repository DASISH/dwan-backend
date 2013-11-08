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
package eu.dasish.annotation.backend;

import eu.dasish.annotation.backend.dao.DBIntegrityService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import org.jmock.Mockery;

/**
 * Created on : Jun 12, 2013, 2:05:25 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class MockObjectsFactoryRest {

    private final Mockery context;

    public MockObjectsFactoryRest(Mockery context) {
        this.context = context;
    }

    
    public DBIntegrityService newDBIntegrityService() {
        return context.mock(DBIntegrityService.class);
    }
    
    public UriInfo newUriInfo() {
        return context.mock(UriInfo.class);
    }
    
     public Providers newProviders() {
        return context.mock(Providers.class);
    }
   
    public HttpServletRequest newHttpServletRequest() {
        return context.mock(HttpServletRequest.class);
    }
    
    public HttpServletResponse newHttpServletResponce() {
        return context.mock(HttpServletResponse.class);
    }
   
}
