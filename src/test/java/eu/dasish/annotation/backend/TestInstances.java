/**
 * Copyright (C) 2013 DASISH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package eu.dasish.annotation.backend;

import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.TextBody;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.TargetInfoList;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.PermissionList;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

/**
 *
 * @author olhsha
 */
public class TestInstances {

    final private Annotation _annotationOne;
    final private Annotation _annotationToAdd;

    public TestInstances(String baseURI) {
        _annotationOne = makeAnnotationOne(baseURI);
        _annotationToAdd = makeAnnotationToAdd(baseURI);
    }

    private Annotation makeAnnotationOne(String baseURI) {
        Annotation result = makeAnnotation(baseURI, "<html><body>some html 1</body></html>", "text/html", "Sagrada Famiglia", "00000000-0000-0000-0000-000000000111");
        result.setURI(baseURI+"annotations/00000000-0000-0000-0000-000000000021");
        
        try {
            result.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-08-12T09:25:00.383000Z"));
        } catch (DatatypeConfigurationException dce) {
            System.out.println("wrongly-formatted test timestamp " + "2013-08-12T09:25:00.383000Z");
            result.setLastModified(null);
        }
        PermissionList upL = new PermissionList();
        TargetInfoList targets = new TargetInfoList();
        result.setPermissions(upL);
        result.setTargets(targets);

        Permission up1 = new Permission();
        up1.setPrincipalRef(baseURI + "principals/00000000-0000-0000-0000-000000000112");
        up1.setLevel(Access.WRITE);

        Permission up2 = new Permission();
        up2.setPrincipalRef(baseURI + "principals/00000000-0000-0000-0000-000000000113");
        up2.setLevel(Access.READ);

        upL.getPermission().add(up1);
        upL.getPermission().add(up2);

        TargetInfo target1 = new TargetInfo();
        target1.setLink("http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia");
        target1.setRef(baseURI + "targets/00000000-0000-0000-0000-000000000031");
        target1.setVersion("version 1.0");

        TargetInfo target2 = new TargetInfo();
        target2.setLink("http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD");
        target2.setRef(baseURI + "targets/00000000-0000-0000-0000-000000000032");
        target2.setVersion("version 1.1");

        targets.getTargetInfo().add(target1);
        targets.getTargetInfo().add(target2);

        return result;
    }

    private Annotation makeAnnotationToAdd(String baseURI) {
        Annotation result = makeAnnotation(baseURI, "<html><body>some html 3</body></html>", "text/plain", "Annotation to add to test DAO", "00000000-0000-0000-0000-000000000113");

        TargetInfo TargetInfo = new TargetInfo();
        TargetInfo.setLink("http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia");
        TargetInfo.setRef(TestBackendConstants._TEST_SERVLET_URI_targets + "00000000-0000-0000-0000-000000000031");
        TargetInfo.setVersion("version 1.0");

        TargetInfoList targetInfos = new TargetInfoList();
        targetInfos.getTargetInfo().add(TargetInfo);
        result.setTargets(targetInfos);
        
        PermissionList permissions = new PermissionList();
        permissions.setPublic(Access.WRITE);
        result.setPermissions(permissions);
        
        return result;
    }

    private Annotation makeAnnotation(String baseURI, String bodyTxt, String bodyMimeType, String headline, String ownerExternalId) {
        Annotation result = new Annotation();
        AnnotationBody body = new AnnotationBody();
        result.setBody(body);
        TextBody textBody = new TextBody();
        body.setTextBody(textBody);
        textBody.setMimeType(bodyMimeType);
        textBody.setBody(bodyTxt);

        result.setHeadline(headline);

        if (baseURI != null) {
            result.setOwnerRef(baseURI + "principals/" + ownerExternalId);
        } else {
            result.setOwnerRef("principals/" + ownerExternalId);
        }

        result.setLastModified(null);
        result.setURI(null);
        result.setTargets(null);
        result.setURI(null);

        return result;
    }

    public Annotation getAnnotationOne() {
        return _annotationOne;
    }

    public Annotation getAnnotationToAdd() {
        return _annotationToAdd;
    }
}
