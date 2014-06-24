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

import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.XmlBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.ParserConfigurationException;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author olhsha
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/annotationDao.xml"})
public class JdbcAnnotationDaoTest extends JdbcResourceDaoTest {

    @Autowired
    JdbcAnnotationDao jdbcAnnotationDao;
    TestInstances testInstances = new TestInstances("/api");

    /**
     * Test of stringURItoExternalID method public String
     * stringURItoExternalID(String uri);
     */
    @Test
    public void testHrefToExternalID() {
        System.out.println("test stringURItoExternalID");
        jdbcAnnotationDao.setResourcePath("/api/annotations/");
        String randomUUID = UUID.randomUUID().toString();
        String uri = "/api/annotations/" + randomUUID;
        String uuid = jdbcAnnotationDao.hrefToExternalID(uri);
        assertEquals(randomUUID, uuid);
    }

    /**
     * Test of externalIDtoURI method public String externalIDtoURI(String
     * externalID);
     */
    @Test
    public void testExternalIDtoHref() {
        System.out.println("test externalIDtoHref");
        jdbcAnnotationDao.setResourcePath("/api/annotations/");
        String randomUUID = UUID.randomUUID().toString();
        String uri = "/api/annotations/" + randomUUID;
        String uriResult = jdbcAnnotationDao.externalIDtoHref(randomUUID);
        assertEquals(uri, uriResult);
    }

    /**
     * Test of retrieveTargetIDs method, of class JdbcAnnotationDao.
     */
    /**
     * Test of getAnnotations method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetAnnotations() {
        System.out.println("test getAnnotations");
        List<Number> expResult = new ArrayList<Number>();
        expResult.add(1);
        expResult.add(2);
        List<Number> result = jdbcAnnotationDao.getAnnotations(1);
        assertEquals(expResult, result);
    }

    ///////////////////////////////////////////
    @Test
    public void testDeleteAllAnnotationTarget() {
        System.out.println("test deleteAllAnnotationTargets");
        assertEquals(2, jdbcAnnotationDao.deleteAllAnnotationTarget(1));
        assertEquals(0, jdbcAnnotationDao.deleteAllAnnotationTarget(1));
    }

    ///////////////////////////////////////////
    @Test
    public void testDeleteAnnotationPrinciplePermissions() {
        System.out.println("test deleteAllAnnotationTargets");
        int result = jdbcAnnotationDao.deleteAnnotationPermissions(1);
        assertEquals(3, result);
        assertEquals(0, jdbcAnnotationDao.deleteAnnotationPermissions(1));
    }

    ///////////////////////////////////////////
    @Test
    public void testAddAnnotationPrincipalAccess() {
        System.out.println("test addAnnotationTargets");
        int result = jdbcAnnotationDao.addAnnotationPrincipalAccess(1, 1, Access.READ);
        assertEquals(1, result);
    }

    ///////////////////////////////////////////
    @Test
    public void testAddAnnotationTarget() {
        System.out.println("test addAnnotationTargetPair");
        assertEquals(1, jdbcAnnotationDao.addAnnotationTarget(1, 3));
    }

    /**
     * Test of getAnnotationREFs method, of class JdbcAnnotationDao.
     * List<ReTargetREF> getAnnotationREFs(List<Number> annotationIDs)
     */
    @Test
    public void testGetAnnotationREFs() {
        System.out.println("getAnnotationREFs");
        List<Number> annotIds = new ArrayList<Number>();
        annotIds.add(1);
        annotIds.add(2);
        annotIds.add(3);

        jdbcAnnotationDao.setResourcePath("/api/annotations/");
        final List<String> testList = jdbcAnnotationDao.getAnnotationREFs(annotIds);
        assertEquals(3, testList.size());
        assertEquals("/api/annotations/00000000-0000-0000-0000-000000000021", testList.get(0));
        assertEquals("/api/annotations/00000000-0000-0000-0000-000000000022", testList.get(1));
        assertEquals("/api/annotations/00000000-0000-0000-0000-000000000023", testList.get(2));

        final List<String> testListTwo = jdbcAnnotationDao.getAnnotationREFs(new ArrayList<Number>());
        assertEquals(0, testListTwo.size());

    }

    /**
     *
     * Test of getAnnotationID method, of class JdbcAnnotationDao. Integer
     * getAnnotationID(UUID externalID)
     */
    @Test
    public void getInternalID() throws NotInDataBaseException {
        System.out.println("test getInternalID");

        final Number annotaionId = jdbcAnnotationDao.getInternalID(UUID.fromString("00000000-0000-0000-0000-000000000021"));
        assertEquals(1, annotaionId.intValue());

        try {
            final Number annotaionIdNE = jdbcAnnotationDao.getInternalID(UUID.fromString("00000000-0000-0000-0000-0000000000cc"));
        } catch (NotInDataBaseException e) {
            System.out.println(e);
        }


    }

    /**
     * Test of getInternalIDFromURI method, public Number
     * getInternalIDFromURI(UUID externalID);
     */
    @Test
    public void testGetInternalIDFRomHref() throws NotInDataBaseException {
        System.out.println("test getInternalIDFromHref");
        jdbcAnnotationDao.setResourcePath("/api/annotations/");
        String uri = "/api/annotations/00000000-0000-0000-0000-000000000021";
        Number result = jdbcAnnotationDao.getInternalIDFromHref(uri);
        assertEquals(1, result.intValue());
        assertEquals(1, result);
    }

    /**
     *
     * Test of getAnnotationWithoutTargetsAndPemissions method, of class
     * JdbcAnnotationDao. Annotation getAnnotation(Number annotationlID)
     */
    @Test
    public void getAnnotationWithoutTargetsAndPermisions() throws DatatypeConfigurationException {
        System.out.println("test getAnnotationWithoutTargetsAndPermissions");
        jdbcAnnotationDao.setResourcePath("/api/annotations/");
        final Annotation result = jdbcAnnotationDao.getAnnotationWithoutTargetsAndPemissions(1);

        assertEquals("Sagrada Famiglia", result.getHeadline());
        assertEquals("<html><body>some html 1</body></html>", result.getBody().getTextBody().getBody());
        assertEquals("text/html", result.getBody().getTextBody().getMimeType());
        assertEquals("00000000-0000-0000-0000-000000000021", result.getId());
        assertEquals("/api/annotations/00000000-0000-0000-0000-000000000021", result.getHref());
        assertEquals(DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-08-12T09:25:00.383000Z"), result.getLastModified());
    }

    /**
     * Test of deletAnnotation method, of class JdbcAnnotationDao.
     */
    /**
     *
     * @param annotationId
     * @return removed annotation rows (should be 1)
     */
    @Test
    public void testDeleteAnnotation() {
        System.out.println("deleteAnnotation");

        // to provide integrity, first delete rows in the joint tables
        jdbcAnnotationDao.deleteAllAnnotationTarget(4);
        jdbcAnnotationDao.deleteAnnotationPermissions(4);

        assertEquals(1, jdbcAnnotationDao.deleteAnnotation(4));
        assertEquals(0, jdbcAnnotationDao.deleteAnnotation(4));
    }

    /**
     * Test of addAnnotation method, of class JdbcAnnotationDao.
     */
    @Test
    public void testAddAnnotation() throws SQLException, Exception {
        System.out.println("test_addAnnotation ");
        jdbcAnnotationDao.setResourcePath("/api/annotations/");
        final Annotation annotationToAdd = testInstances.getAnnotationToAdd();// existing Targets
        
        Number newAnnotationID = jdbcAnnotationDao.addAnnotation(annotationToAdd, 3);
        assertEquals(5, newAnnotationID);

        // checking
        Annotation addedAnnotation = jdbcAnnotationDao.getAnnotationWithoutTargetsAndPemissions(5);
        assertEquals(addedAnnotation.getHref(), "/api/annotations/"+addedAnnotation.getId());
        assertFalse(null == addedAnnotation.getLastModified());
        assertEquals(annotationToAdd.getBody().getTextBody().getMimeType(), addedAnnotation.getBody().getTextBody().getMimeType());
        assertEquals(annotationToAdd.getBody().getTextBody().getBody(), addedAnnotation.getBody().getTextBody().getBody());
        assertEquals(annotationToAdd.getHeadline(), addedAnnotation.getHeadline());
        System.out.println("creation time " + addedAnnotation.getLastModified());
    }

    

    //////////////////////////////////
    @Test
    public void testGetExternalID() {
        System.out.println("getExternalID");

        final UUID externalId = jdbcAnnotationDao.getExternalID(1);
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000021"), externalId);

    }

    /**
     * test public List<Number> getFilteredAnnotationIDs(String link, String
     * text, String access, String namespace, UUID owner, Timestamp after,
     * Timestamp before) {
     *
     */
    @Test
    public void testGetFilteredAnnotationIDs() {
        System.out.println(" test getFilteredAnnotationIDs");

        List<Number> result_1 = jdbcAnnotationDao.getFilteredAnnotationIDs(null, "some html", null, null, null);
        assertEquals(3, result_1.size());
        assertEquals(1, result_1.get(0));
        assertEquals(2, result_1.get(1));
        assertEquals(4, result_1.get(2));


        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

        List<Number> result_2 = jdbcAnnotationDao.getFilteredAnnotationIDs(1, "some html", null, after, before);
        assertEquals(1, result_2.size());
        assertEquals(1, result_2.get(0));

        final String after_1 = (new Timestamp(System.currentTimeMillis())).toString();// no annotations added after "now"       
        List<Number> result_3 = jdbcAnnotationDao.getFilteredAnnotationIDs(4, "some html", null, after_1, null);
        assertEquals(0, result_3.size());


    }

    //////////////////////////////////
    @Test
    public void testGetReprmissions() {
        System.out.println("test getPermissions");
        List<Map<Number, String>> result = jdbcAnnotationDao.getPermissions(1);
        assertEquals(3, result.size());
        assertEquals("write", result.get(0).get(2));
        assertEquals("read", result.get(1).get(3));
        assertEquals("read", result.get(2).get(11));

    }

    // getAnnotationIDsForPermission(Number principalID, Access access)
    @Test
    public void testAnnotationIDsForPermission() {
        System.out.println("test getAnnotationIDsForPermission");
        List<Number> result = jdbcAnnotationDao.getAnnotationIDsForPermission(1, Access.READ);
        assertEquals(3, result.size());
        assertEquals(2, result.get(0));
        assertEquals(3, result.get(1));
        assertEquals(4, result.get(2));

        List<Number> resultTwo = jdbcAnnotationDao.getAnnotationIDsForPermission(1, Access.WRITE);
        assertEquals(1, resultTwo.size());
        assertEquals(4, resultTwo.get(0));

        List<Number> resultThree = jdbcAnnotationDao.getAnnotationIDsForPermission(1, Access.NONE);
        assertEquals(0, resultThree.size());

    }

    // getAnnotationIDsForPublicAccess
    @Test
    public void testAnnotationIDsForPublicAccess() {
        System.out.println("test getAnnotationIDsForPublicAccess");
        List<Number> result = jdbcAnnotationDao.getAnnotationIDsForPublicAccess(Access.READ);
        assertEquals(2, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));

        List<Number> resultTwo = jdbcAnnotationDao.getAnnotationIDsForPublicAccess(Access.WRITE);
        assertEquals(1, resultTwo.size());
        assertEquals(1, resultTwo.get(0));


        List<Number> resultThree = jdbcAnnotationDao.getAnnotationIDsForPublicAccess(Access.NONE);
        assertEquals(2, resultThree.size());
        assertTrue(resultThree.contains(3));
        assertTrue(resultThree.contains(4));

    }

    @Test
    public void testGetPublicAttribute() {
        System.out.println("test getPublicAttribute");
        assertEquals(Access.WRITE, jdbcAnnotationDao.getPublicAttribute(1));
        assertEquals(Access.READ, jdbcAnnotationDao.getPublicAttribute(2));
        assertEquals(Access.NONE, jdbcAnnotationDao.getPublicAttribute(3));
    }

    @Test
    public void testGetAccess() {
        System.out.println("test getAccess");
        assertEquals(Access.READ, jdbcAnnotationDao.getAccess(1, 3));
        assertEquals(Access.WRITE, jdbcAnnotationDao.getAccess(2, 3));
        assertEquals(null, jdbcAnnotationDao.getAccess(3, 3));
    }

    @Test
    public void testSublistOrderedAnnotationIDs() {
        System.out.println("test getSublistOrderedAnnotationIDs");

        final List<Number> annotationIDs = new ArrayList<Number>();
        annotationIDs.add(1);
        annotationIDs.add(2);
        annotationIDs.add(3);
        annotationIDs.add(4);

        List<Number> result = jdbcAnnotationDao.sublistOrderedAnnotationIDs(annotationIDs, 1, 3, "last_modified", "DESC");
        assertEquals(3, result.size());
        assertEquals(3, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(1, result.get(2));
    }

    @Test
    public void testGetOwner() {
        System.out.println("test getOwner");
        assertEquals(1, jdbcAnnotationDao.getOwner(1));
    }

    @Test
    public void testGetAnnotationInfoWithoutTargetsAndOwner() throws DatatypeConfigurationException {
        System.out.println("test getAnnotationInfoWithoutTargets");
        jdbcAnnotationDao.setResourcePath("/api/annotations/");
        final AnnotationInfo result = jdbcAnnotationDao.getAnnotationInfoWithoutTargetsAndOwner(1);

        assertEquals("Sagrada Famiglia", result.getHeadline());
        assertEquals("/api/annotations/00000000-0000-0000-0000-000000000021", result.getHref());
        assertEquals(DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-08-12T09:25:00.383000Z"), result.getLastModified());
    }

    @Test
    public void testTargetIsInUse() {
        System.out.println("test targetIsInUse");
        assertTrue(jdbcAnnotationDao.targetIsInUse(1));
        assertFalse(jdbcAnnotationDao.targetIsInUse(6));
    }

    @Test
    public void testGetAllAnnotationIDs() {
        System.out.println("test getAllAnnotationIDs");
        List<Number> result = jdbcAnnotationDao.getAllAnnotationIDs();
        assertEquals(4, result.size());
        assertEquals(4, result.get(0));
        assertEquals(3, result.get(1));
        assertEquals(2, result.get(2));
        assertEquals(1, result.get(3));
    }

    @Test
    public void testUpdateAnnotationBody() {
        System.out.println("test updateAnnotationbody ");
        int result = jdbcAnnotationDao.updateAnnotationBody(1, "some html 1 updated", "text/plain", false);
        assertEquals(1, result);
        Annotation check = jdbcAnnotationDao.getAnnotationWithoutTargetsAndPemissions(1);
        assertEquals("some html 1 updated", check.getBody().getTextBody().getBody());
        assertEquals("text/plain", check.getBody().getTextBody().getMimeType());

        String testXml = "<xhtml:span style=\"background-color:rgb(0,0,153);color:rgb(255,255,255);border: thick solid rgb(0, 0, 153);\">test</xhtml:span>";
        int result2 = jdbcAnnotationDao.updateAnnotationBody(1, testXml, "application/xml", true);
        assertEquals(1, result2);
        Annotation check2 = jdbcAnnotationDao.getAnnotationWithoutTargetsAndPemissions(1);
        assertEquals("xhtml:span", check2.getBody().getXmlBody().getAny().getNodeName());
        assertTrue(check2.getBody().getXmlBody().getAny().hasAttribute("style"));
        assertEquals("test", check2.getBody().getXmlBody().getAny().getTextContent());
        assertEquals("application/xml", check2.getBody().getXmlBody().getMimeType());
    }
    
    @Test
    public void testUpdateAnnotationHeader() {
        System.out.println("test updateAnnotationHeader ");
        int result = jdbcAnnotationDao.updateAnnotationHeadline(1, "new Header");
        assertEquals(1, result);
        Annotation check = jdbcAnnotationDao.getAnnotationWithoutTargetsAndPemissions(1);
        assertEquals("new Header", check.getHeadline());
    }

    @Test
    public void testUpdateAnnotationPrincipalAccess() {
        System.out.println("test updateAnntationPrincipalAccess ");

        int result = jdbcAnnotationDao.updateAnnotationPrincipalAccess(1, 2, Access.NONE);
        assertEquals(1, result);
        assertEquals(Access.NONE, jdbcAnnotationDao.getAccess(1, 2));
    }

    @Test
    public void testUpdatPublicAttribute() {
        System.out.println("test updatePublicAtribute ");

        int result = jdbcAnnotationDao.updatePublicAttribute(1, Access.NONE);
        assertEquals(1, result);
        assertEquals(Access.NONE, jdbcAnnotationDao.getPublicAttribute(1));
    }

    @Test
    public void testRetrieveBodyComponents() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("test retrieveBodyComponents 1");

        AnnotationBody ab = testInstances.getAnnotationOne().getBody();

        String[] result = jdbcAnnotationDao.retrieveBodyComponents(ab);
        assertEquals(2, result.length);
        assertEquals("<html><body>some html 1</body></html>", result[0]);
        assertEquals("text/html", result[1]);


        String testXml = "<xhtml:span style=\"background-color:rgb(0,0,153);color:rgb(255,255,255);border: thick solid rgb(0, 0, 153);\">test</xhtml:span>";
        AnnotationBody ab2 = new AnnotationBody();
        XmlBody xb = new XmlBody();
        Element el = Helpers.stringToElement(testXml);
        String str = Helpers.elementToString(el);
        xb.setAny(el);
        xb.setMimeType("application/xml");
        ab2.setXmlBody(xb);
        System.out.println("test retrieveBodyComponents 2");
        String[] result2 = jdbcAnnotationDao.retrieveBodyComponents(ab2);
        assertEquals(2, result2.length);
        assertEquals(str, result2[0]);
        assertEquals("application/xml", result2[1]);
    }

    @Test
    public void testUpdateAnnotation() {
        System.out.println("test UpdateAnnotation");
        jdbcAnnotationDao.setResourcePath("/api/annotations/");
        
        Annotation annotation = testInstances.getAnnotationOne();
        annotation.setHeadline("updated headline 1");
        annotation.getBody().getTextBody().setBody("updated some html 1");
        annotation.getBody().getTextBody().setMimeType("text/plain");

        
        int result = jdbcAnnotationDao.updateAnnotation(annotation,1, 1);
        assertEquals(1, result);
        System.out.println(" annotation updated");
        Annotation check = jdbcAnnotationDao.getAnnotationWithoutTargetsAndPemissions(1);
        assertEquals("updated some html 1", check.getBody().getTextBody().getBody());
        assertEquals("text/plain", check.getBody().getTextBody().getMimeType());
        assertEquals("updated headline 1", check.getHeadline());
        assertEquals("/api/annotations/00000000-0000-0000-0000-000000000021", check.getHref());
        assertEquals("00000000-0000-0000-0000-000000000021", check.getId());
    }
    
    @Test
    public void helperReplaceString() {
       System.out.println("test Helpers.ReplaceString"); 
       StringBuilder source = new StringBuilder("va%?&b;v_wa%?&b;w");
       String oldFragment = "a%?&b;";
       String newFragment = ":;:";
       Helpers.replaceString(source, oldFragment, (Object) newFragment);
       assertEquals(source.toString(), "v:;:v_w:;:w");
    }
    
     @Test
    public void helperReplace() {
       System.out.println("test Helpers.Replace"); 
       String source = "va%?&b;v_xy:_wa%?&b;w";
       Map<String, String> replacements = new HashMap<String,String>();
       replacements.put("a%?&b;", ":;:");
       replacements.put("xy:", ":yx");
       replacements.put("", ":;:");
       String sourceUPD = Helpers.replace(source, replacements);
       assertEquals(sourceUPD, "v:;:v_:yx_w:;:w");
    }
}
