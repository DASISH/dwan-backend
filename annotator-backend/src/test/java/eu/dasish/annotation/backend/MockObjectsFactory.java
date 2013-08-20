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

import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.SourceDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.backend.dao.VersionDao;
import eu.dasish.annotation.backend.rest.DaoDispatcher;
import org.jmock.Mockery;

/**
 * Created on : Jun 12, 2013, 2:05:25 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class MockObjectsFactory {

    private final Mockery context;

    public MockObjectsFactory(Mockery context) {
        this.context = context;
    }

    public NotebookDao newNotebookDao() {
        return context.mock(NotebookDao.class);
    }

    public AnnotationDao newAnnotationDao() {
        return context.mock(AnnotationDao.class);
    }
    
    public UserDao newUserDao() {
        return context.mock(UserDao.class);
    }
    
   
    public SourceDao newSourceDao() {
        return context.mock(SourceDao.class);
    }
    
    public CachedRepresentationDao newCachedRepresentationDao() {
        return context.mock(CachedRepresentationDao.class);
    }
    
    public VersionDao newVersionDao() {
        return context.mock(VersionDao.class);
    }
    
    public DaoDispatcher newDaoDispatcher() {
        return context.mock(DaoDispatcher.class);
    }
}
