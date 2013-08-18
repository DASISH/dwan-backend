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

import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.SourceDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.backend.dao.VersionDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.CachedRepresentationIdentifier;
import eu.dasish.annotation.backend.identifiers.SourceIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.backend.identifiers.VersionIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewSourceInfo;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jmock.Expectations;
import org.jmock.Mockery;
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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-test-config/mockery.xml", "/spring-test-config/mockAnnotationDao.xml",
    "/spring-test-config/mockUserDao.xml", "/spring-test-config/mockNotebookDao.xml",
    "/spring-test-config/mockSourceDao.xml", "/spring-test-config/mockVersionDao.xml", "/spring-test-config/mockCachedRepresentationDao.xml"})
public class DaoDispatcherTest {

    
    
    @Autowired
    private DaoDispatcher daoDispatcher;
    
    @Autowired
    private Mockery mockery;
    
    @Autowired
    private UserDao userDao;
    @Autowired
    private CachedRepresentationDao cachedRepresentationDao;
    @Autowired
    private VersionDao versionDao;
    @Autowired
    private SourceDao sourceDao;
    @Autowired
    private AnnotationDao annotationDao;
    @Autowired
    private NotebookDao notebookDao;

    public DaoDispatcherTest() {
    }

    /**
     * Test of getAnnotationInternalIdentifier method, of class DaoDispatcher.
     */
    @Test
    public void testGetAnnotationInternalIdentifier() {
        System.out.println("getAnnotationInternalIdentifier");
        final AnnotationIdentifier annotationIdentifier = new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_2_EXT);

        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getInternalID(annotationIdentifier);
                will(returnValue(2));
            }
        });
        assertEquals(2, daoDispatcher.getAnnotationInternalIdentifier(annotationIdentifier));
    }

    /**
     * Test of getAnnotationExternalIdentifier method, of class DaoDispatcher.
     */
    @Test
    public void testGetAnnotationExternalIdentifier() {
        System.out.println("getAnnotationExternalIdentifier");
        final AnnotationIdentifier annotationIdentifier = new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_2_EXT);

        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getExternalID(2);
                will(returnValue(annotationIdentifier));
            }
        });
        assertEquals(TestBackendConstants._TEST_ANNOT_2_EXT, daoDispatcher.getAnnotationExternalIdentifier(2).toString());
    }

    /**
     * Test of getUserInternalIdentifier method, of class DaoDispatcher.
     */
    @Test
    public void testGetUserInternalIdentifier() {
        System.out.println("getUserInternalIdentifier");

        final UserIdentifier userIdentifier = new UserIdentifier(TestBackendConstants._TEST_USER_5_EXT_ID);

        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getInternalID(userIdentifier);
                will(returnValue(5));
            }
        });
        assertEquals(5, daoDispatcher.getUserInternalIdentifier(userIdentifier));
    }

    /**
     * Test of getUserExternalIdentifier method, of class DaoDispatcher.
     */
    @Test
    public void testGetUserExternalIdentifier() {
        System.out.println("getUserExternalIdentifier");
        final UserIdentifier userIdentifier = new UserIdentifier(TestBackendConstants._TEST_USER_5_EXT_ID);

        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getExternalID(5);
                will(returnValue(userIdentifier));
            }
        });
        assertEquals(TestBackendConstants._TEST_USER_5_EXT_ID, daoDispatcher.getUserExternalIdentifier(5));
    }

    /**
     * Test of deleteCachedForVersion method, of class DaoDispatcher.
     */
    @Test
    public void testDeleteCachedForVersion() {
        System.out.println("deleteCachedForVersion");
        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).deleteVersionCachedRepresentation(6, 5);
                will(returnValue(1));

                oneOf(cachedRepresentationDao).deleteCachedRepresentationInfo(5);
                will(returnValue(0)); // cached is used by another version

            }
        });

        int[] result = daoDispatcher.deleteCachedForVersion(6, 5);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(0, result[1]);
    }

    /**
     * Test of addCachedForVersion method, of class DaoDispatcher.
     */
    @Test
    public void testAddCachedForVersion() {
        System.out.println("addCachedForVersion");
        String mime = "text/html";
        String type = "text";
        String tool = "latex";
        final CachedRepresentationInfo newCached = new CachedRepresentationInfo();
        newCached.setMimeType(mime);
        newCached.setType(type);
        newCached.setTool(tool);
        final Number newID = 8;
        final Number versionID = 1;
        mockery.checking(new Expectations() {
            {
                oneOf(cachedRepresentationDao).getInternalID(new CachedRepresentationIdentifier(newCached.getRef()));
                will(returnValue(null));

                oneOf(cachedRepresentationDao).addCachedRepresentationInfo(newCached);
                will(returnValue(newID));

                one(versionDao).addVersionCachedRepresentation(versionID, newID);
                will(returnValue(1));

            }
        });


        Number[] result = daoDispatcher.addCachedForVersion(6, newCached);
        assertEquals(2, result.length);
        assertEquals(newID, result[0]);
        assertEquals(1, result[1]);
    }

    /////////////////////////////////////////////
    public void testDeleteVersionWithCachedRepresentations() {
        System.out.println("deleteVersion");
        final List<Number> cachedList = new ArrayList<Number>();
        cachedList.add(5);

        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).versionIsInUse(6);
                will(returnValue(false));
                
                oneOf(versionDao).retrieveCachedRepresentationList(6);
                will(returnValue(cachedList));

                oneOf(versionDao).deleteAllVersionCachedRepresentation(6);
                will(returnValue(1));
                
                oneOf(versionDao).deleteVersion(6);
                will(returnValue(1));

                oneOf(cachedRepresentationDao).deleteCachedRepresentationInfo(5);
                will(returnValue(0)); // cached is used by another version
                
                

            }
        });

        int[] result = daoDispatcher.deleteVersionWithCachedRepresentations(6);
        assertEquals(1, result[0]); //version
        assertEquals(1, result[1]); // versions-cached
        assertEquals(0, result[2]);//cached 5 is in use
        
        //Another test
        
        
        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).versionIsInUse(5);
                will(returnValue(true));
            }
        });


        int[] resultTwo = daoDispatcher.deleteVersionWithCachedRepresentations(5); // version is in use by the source 4
        assertEquals(0, resultTwo[0]);
        assertEquals(0, resultTwo[1]);
        assertEquals(0, resultTwo[2]);

    }

    @Test
    public void testDeleteSourceSourceWithVersions() throws SQLException {
        // test 1
         mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).sourceIsInUse(5);
                will(returnValue(true));
            }
        });

        int[] result = daoDispatcher.deleteSourceWithVersions(1); //the source is in use, should not be deleted
        assertEquals(0, result[0]); // 
        assertEquals(0, result[1]);

        // test 2
        
        final List<Number> versionList = new ArrayList<Number>();
        versionList.add(7);
        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).sourceIsInUse(5);
                will(returnValue(false));
                
                oneOf(sourceDao).retrieveVersionList(5);
                will(returnValue(versionList));

                oneOf(sourceDao).deleteAllSourceVersion(5);
                will(returnValue(1));
                
                oneOf(sourceDao).deleteSource(5);
                will(returnValue(1));
                
                oneOf(versionDao).versionIsInUse(7);
                will(returnValue(false));
            }
        });
        
        
        int[] resultTwo = daoDispatcher.deleteSourceWithVersions(5);// the source will be deleted because it is not referred by any annotation
        assertEquals(3, resultTwo.length);
        assertEquals(1, resultTwo[0]); // source 7 is deleted
        assertEquals(1, resultTwo[1]); // row (5,7) in "sorces_versions" is deleted
        assertEquals(0, resultTwo[2]); // version 7 is not foub=nd, not in use
    }

  
   

    /**
     * Test of addSourceAndPairSourceVersion method, of class DaoDispatcher.
     */
    @Test
    public void testAddSourceAndPairSourceVersion() throws Exception {
        System.out.println("addSourceAndPairSourceVersion");
       final  NewSourceInfo newSource = new NewSourceInfo();
       newSource.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
       newSource.setVersion(TestBackendConstants._TEST_VERSION_3_EXT_ID);// already added version, existing
        
        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).getInternalID(new VersionIdentifier(TestBackendConstants._TEST_VERSION_3_EXT_ID));
                will(returnValue(3));

                oneOf(sourceDao).addSource(with(aNonNull(Source.class)));
                will(returnValue(6));
                
                oneOf(sourceDao).addSourceVersion(6, 3);
                will(returnValue(1));

            }
        });
        
        Number result = daoDispatcher.addSourceAndPairSourceVersion(newSource);
        assertEquals(6, result.intValue());
        
        // Another test
        
        final  NewSourceInfo newSourceTwo = new NewSourceInfo();
        newSourceTwo.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
        newSourceTwo.setVersion(TestBackendConstants._TEST_VERSION_NONEXIST_EXT_ID);
        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).getInternalID(new VersionIdentifier(TestBackendConstants._TEST_VERSION_NONEXIST_EXT_ID));
                will(returnValue(null));

            }
        });
        Number resultTwo = daoDispatcher.addSourceAndPairSourceVersion(newSourceTwo);
        assertEquals(-1, resultTwo.intValue());
        
        
    }

    /**
     * Test of addTargetSourcesToAnnotation method, of class DaoDispatcher.
     */
    @Test
    public void testAddTargetSourcesToAnnotation() throws Exception {
        System.out.println("addTargetSourcesToAnnotation");
        
        NewOrExistingSourceInfo noesi = new NewOrExistingSourceInfo();
        NewSourceInfo nsi = new NewSourceInfo();
        nsi.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
        nsi.setId(TestBackendConstants._TEST_TEMP_SOURCE_ID);
        nsi.setVersion(null);
        noesi.setNewSource(nsi);


        NewOrExistingSourceInfo noesiTwo = new NewOrExistingSourceInfo();
        SourceInfo si = new SourceInfo();
        si.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
        final SourceIdentifier sourceIdentifier = new SourceIdentifier();
        si.setRef(sourceIdentifier.toString());
        si.setVersion(null);
        noesiTwo.setSource(si);

        final Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo> map = new HashMap<NewOrExistingSourceInfo, NewOrExistingSourceInfo>();
        map.put(noesi, noesiTwo);


//        
//        mockery.checking(new Expectations() {
//            {
//                oneOf(sourceDao.getInternalID(sourceIdentifier));
//                will(returnValue());
//                
//                oneOf(annotationDao).addAnnotationSourcePair(with(aNonNull(Number.class)), with(aNonNull(Number.class)));
//                will(returnValue(1));
//                
//                oneOf(sourceDao).addTargetSources(with(aNonNull(Number.class)), with(aNonNull(List.class)));
//                will(returnValue(map));
//            }
//        });


        
    }

    /**
     * Test of getFilteredAnnotationIDs method, of class DaoDispatcher.
     */
    @Test
    public void testGetFilteredAnnotationIDs() {
        System.out.println("getFilteredAnnotationIDs");
        String link = "";
        String text = "";
        String access = "";
        String namespace = "";
        UserIdentifier owner = null;
        Timestamp after = null;
        Timestamp before = null;
        DaoDispatcher instance = new DaoDispatcher();
        List expResult = null;
        List result = instance.getFilteredAnnotationIDs(link, text, access, namespace, owner, after, before);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteAnnotationWithSources method, of class DaoDispatcher.
     */
    @Test
    public void testDeleteAnnotationWithSources() throws Exception {
        System.out.println("deleteAnnotationWithSources");
        Number annotationID = null;
        DaoDispatcher instance = new DaoDispatcher();
        int[] expResult = null;
        int[] result = instance.deleteAnnotationWithSourcesAndPermissions(annotationID);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAnnotation method, of class DaoDispatcher.
     */
    @Test
    public void testGetAnnotation() throws Exception {
        System.out.println("getAnnotation");
        Number annotationID = null;
        DaoDispatcher instance = new DaoDispatcher();
        Annotation expResult = null;
        Annotation result = instance.getAnnotation(annotationID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     *
     * @param annotationId
     * @return result[0] = # removed "annotations_principals_permissions" rows
     * result[1] = # removed "annotations_target_sources" rows result[2] = #
     * removed annotation rows (should be 1)
     */
    @Test
    public void testDeleteAnnotationWithSourcesAndPermissions() throws SQLException {
        System.out.println("test deleteAnnotationWithSourcesAndPermissions");
        // result[0] = # removed "annotations_principals_perissions" rows
        // result[1] = # removed "annotatiobs_target_sources" rows
        // result[2] = # removed annotation rows (should be 1)

        int[] result = daoDispatcher.deleteAnnotationWithSourcesAndPermissions(5);
        assertEquals(3, result[0]);
        assertEquals(2, result[1]);
        assertEquals(1, result[2]);

        // now, try to delete the same annotation one more time
        // if it has been already deleted then the method under testing should return 0

        result = daoDispatcher.deleteAnnotationWithSourcesAndPermissions(5);
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
        assertEquals(0, result[2]);
    }

    /**
     * Test of contructNewOrExistingSourceInfo method, of class JdbcSourceDao.
     */
    @Test
    public void testContructNewOrExistingSourceInfo() {
        System.out.println("contructNewOrExistingSourceInfo");

        List<SourceInfo> sourceInfoList = new ArrayList<SourceInfo>();

        SourceInfo sourceInfoOne = new SourceInfo();
        sourceInfoOne.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
        sourceInfoOne.setRef(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        sourceInfoOne.setRef(TestBackendConstants._TEST_VERSION_1_EXT_ID);

        SourceInfo sourceInfoTwo = new SourceInfo();
        sourceInfoTwo.setLink(TestBackendConstants._TEST_SOURCE_2_LINK);
        sourceInfoTwo.setRef(TestBackendConstants._TEST_SOURCE_2_EXT_ID);
        sourceInfoTwo.setRef(TestBackendConstants._TEST_VERSION_3_EXT_ID);

        sourceInfoList.add(sourceInfoOne);
        sourceInfoList.add(sourceInfoTwo);

//        NewOrExistingSourceInfos result = jdbcSourceDao.contructNewOrExistingSourceInfo(sourceInfoList);
//        assertEquals(2, result.getTarget().size());
//        assertEquals(sourceInfoOne, result.getTarget().get(0).getSource());
//        assertEquals(sourceInfoTwo, result.getTarget().get(1).getSource());

    }
//    /**
//     * Test of addTargetSources method, of class JdbcSourceDao. public
//     * Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo>
//     * addTargetSources(Number annotationID, List<NewOrExistingSourceInfo>
//     * sources)
//     */
//    @Test
//    public void testAddTargetSourcesOnExistingSource() {
//        System.out.println("addTargetSources : adding the old source");
//
//        NewOrExistingSourceInfo noesi = new NewOrExistingSourceInfo();
//        SourceInfo si = new SourceInfo();
//        si.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
//        si.setRef(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
//        si.setVersion(TestBackendConstants._TEST_VERSION_1_EXT_ID);
//        noesi.setSource(si);
//
//        List<NewOrExistingSourceInfo> listnoesi = new ArrayList<NewOrExistingSourceInfo>();
//        listnoesi.add(noesi);
//
//        try {
//            Map<String, String> result = jdbcSourceDao.addTargetSources(5, listnoesi);
//            assertEquals(0, result.size()); // no new peristsent source IDs are produced
//        } catch (SQLException e) {
//            System.out.println(e);
//        }
//    }
//
//    /**
//     * Test of addTargetSources method, of class JdbcSourceDao. public
//     * Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo>
//     * addTargetSources(Number annotationID, List<NewOrExistingSourceInfo>
//     * sources)
//     */
//    @Test
//    public void testAddTargetSourcesOnNewSource() {
//        System.out.println("addTargetSources : adding the new source");
//
//        NewOrExistingSourceInfo noesi = new NewOrExistingSourceInfo();
//        NewSourceInfo nsi = new NewSourceInfo();
//        nsi.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
//        nsi.setId(TestBackendConstants._TEST_TEMP_SOURCE_ID);
//        nsi.setVersion(TestBackendConstants._TEST_VERSION_1_EXT_ID);
//        noesi.setNewSource(nsi);
//
//        List<NewOrExistingSourceInfo> listnoesiTwo = new ArrayList<NewOrExistingSourceInfo>();
//        listnoesiTwo.add(noesi);
//        
//        mockery.checking(new Expectations() {
//            {
//                oneOf(versionDao).getInternalID(new VersionIdentifier(TestBackendConstants._TEST_VERSION_1_EXT_ID));
//                will(returnValue(1));
//            }
//        });
//
//        try {
//            Map<String, String> result = jdbcSourceDao.addTargetSources(5, listnoesiTwo);
//            assertEquals(1, result.size());// a new identifier must be produced
//            SourceIdentifier sourceIdentifier = new SourceIdentifier(result.get(TestBackendConstants._TEST_TEMP_SOURCE_ID));
//            assertFalse(null == sourceIdentifier.getUUID()); // check if a proper uuid has been assigned 
//        } catch (SQLException e) {
//            System.out.print(e);
//        }
//
//    }
//    
}
