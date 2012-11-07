Deployment package for Elda as used by the Bricolage project
http://bricolage.ilrt.bris.ac.uk/

Build war:

    > mvn clean
    > mvn package [-Pprod]

Default profile: localhost

Configuration
-------------
- src/main/filters/filter-<env>.properties
    Per-environment properties
    
- src/main/webapp/specs/bricolage.ttl
    Linked Data API settings.

