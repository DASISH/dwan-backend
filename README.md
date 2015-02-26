# dwan-backend
DASISH Task 5.6. Annotation framework backend

This repository contains the last committed to svn source code for the annotator backend. 

The logins, passwords and other authentication fragments have been XXX-ed in the context.xml and web.xml. The file shhaa.xml has been removed. These files must be adjusted locally on the server where the software is installed. In web.xml you will need uncomment/comment the fragments switching on shibboleth or basic authentication mechanisms, depending on which sort of authentication you use.

If you do not set up your annotation database yet, you will need to run the script https://github.com/DASISH/dwan-backend/blob/master/annotator-backend/src/main/sql/DashishAnnotatorCreate.sql. If you are up to basic authentication you will also run the script https://github.com/DASISH/dwan-backend/blob/master/annotator-backend/src/main/sql/principal_non-shibb.sql.  Note that in the second script security-sensitive information is XXX-ed. You need to set it up yourself. 

The DASISH schema is located at https://github.com/DASISH/dwan-backend/blob/master/annotator-backend/src/main/webapp/SCHEMA/DASISH-schema.xsd

The scripts and schema are also searchable within this source package. They are located at the standard subdirectories. 

