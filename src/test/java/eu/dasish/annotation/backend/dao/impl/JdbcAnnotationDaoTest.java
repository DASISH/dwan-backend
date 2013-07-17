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
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.ResourceREF;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author olhsha
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/annotationDao.xml"})

public class JdbcAnnotationDaoTest extends JdbcResourceDaoTest{
    
    @Autowired
    JdbcAnnotationDao jdbcAnnotationDao;    
    
    @Test
    public void testIsNotebookInTheDataBase(){
        super.testIsNotebookInTheDataBase(jdbcAnnotationDao);
    }
    
  

    /**
     * Test of getAnnotationInfos method, of class JdbcAnnotationDao.
     * List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs)
     */
    @Test 
    public void testGetAnnotationInfos() {
        System.out.println("getAnnotationInfos");
        List<Number> annotIds = new ArrayList<Number>(); 
        annotIds.add(TestBackendConstants._TEST_ANNOT_1_INT);
        annotIds.add(TestBackendConstants._TEST_ANNOT_2_INT);
        annotIds.add(TestBackendConstants._TEST_ANNOT_3_INT);
        
        final List<AnnotationInfo> annotationInfos = jdbcAnnotationDao.getAnnotationInfos(annotIds);
        assertEquals(3, annotationInfos.size());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_1_HEADLINE, annotationInfos.get(0).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_1_OWNER), annotationInfos.get(0).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_1_TARGETS, annotationInfos.get(0).getTargetSources());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, annotationInfos.get(1).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_OWNER), annotationInfos.get(1).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_2_TARGETS, annotationInfos.get(1).getTargetSources());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_3_HEADLINE, annotationInfos.get(2).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_OWNER), annotationInfos.get(2).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_3_TARGETS, annotationInfos.get(2).getTargetSources());
        
        final List<AnnotationInfo> annotationInfosNull = jdbcAnnotationDao.getAnnotationInfos(null);
        assertEquals(null, annotationInfosNull);
        
        final List<AnnotationInfo> annotationInfosZeroSize = jdbcAnnotationDao.getAnnotationInfos(new ArrayList<Number>());
        assertEquals(0, annotationInfosZeroSize.size());
        
        
    }

    
    /**
     * Test of getAnnotationREFs method, of class JdbcAnnotationDao.
     * List<ResourceREF> getAnnotationREFs(List<Number> annotationIDs)
     */
    @Test  
    public void testGetAnnotationREFs() {
        System.out.println("getAnnotationREFs");
        List<Number> annotIds = new ArrayList<Number>(); 
        annotIds.add(TestBackendConstants._TEST_ANNOT_1_INT);
        annotIds.add(TestBackendConstants._TEST_ANNOT_2_INT);
        annotIds.add(TestBackendConstants._TEST_ANNOT_3_INT);
        annotIds.add(TestBackendConstants._TEST_ANNOT_4_INT_NOT_IN_THE_DB);
        
        final  List<ResourceREF> testList = jdbcAnnotationDao.getAnnotationREFs(annotIds);
        assertEquals(3, testList.size());        
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_1_INT), testList.get(0).getRef());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_INT), testList.get(1).getRef());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_INT), testList.get(2).getRef());
        
        final  List<ResourceREF> testListTwo = jdbcAnnotationDao.getAnnotationREFs(new ArrayList<Number>());
        assertEquals(0, testListTwo.size());
        
        final List<ResourceREF> testListThree = jdbcAnnotationDao.getAnnotationREFs(null);
        assertEquals(null, testListThree);
        
    }

    
}
