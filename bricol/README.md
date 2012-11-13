Packaging for deployment of Bricol control webapp, as used by the Bricolage project
http://bricolage.ilrt.bris.ac.uk/

Build war:

    > mvn clean
    > mvn package [-Pprod]

Default profile: localhost

Configuration
-------------
- src/main/filters/filter-<env>.properties
    Per-environment properties
    
- src/main/webapp/WEB-INF/web.xml
    Basic security settings

