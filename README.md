# dwan-backend
DASISH Task 5.6. Annotation framework backend

This repository contanins the las committed to svn source code for annotator backend. 

The logins, passwords and other authentication fragments have been removed from context.xml, web.xml and shaa.xml. They must be adjusted locally on the server where the software is installed. In web.xml you will need uncomment/comment the fragments switching on shibboleth or basic authentication mechanisms, depending on which sort of authentication you use.

If you do not set up annotation databse yet, you will need to run the script https://github.com/DASISH/dwan-backend/blob/master/annotator-backend/src/main/sql/DashishAnnotatorCreate.sql. If you up to basic authentication you will aslo run the script https://github.com/DASISH/dwan-backend/blob/master/annotator-backend/src/main/sql/principal_non-shibb.sql.  Note that in the second script security-sesitive information is XXX-ed. You need to set up it yourself. 

The DASISH schema is located at https://github.com/DASISH/dwan-backend/blob/master/annotator-backend/src/main/webapp/SCHEMA/DASISH-schema.xsd

The scripts and schema are also browsable from the this source package, since they are located at the standard subdirectories. 
