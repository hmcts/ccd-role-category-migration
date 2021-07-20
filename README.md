# CCD Role Category Migration Application

This application back-populates Role_Category for each record in Data Store's case_users table to either JUDICIAL, PROFESSIONAL, CITIZEN or EXCEPTION in preparation for the migration of case role assignment data from CCD Data Store to the Role Assignment Service.

## Environment variables:
The following environment variables are required:

| Name | Default | Description |
|------|---------|-------------|
| DATA_STORE_DB_URL | jdbc:postgresql://localhost:5055/ccd_data?stringtype=unspecified | Host for database |
| DATA_STORE_DB_USERNAME | ccd | Username for database |
| DATA_STORE_DB_PASSWORD | ccd | Password for database |
| IDAM_API_URL | http://localhost:5000 | Host for IdAM |
| IDAM_MIGRATION_THREADS | 10 | Number of concurrent calls to IdAM |
| IDAM_MIGRATION_CLIENT_ID | ccd-role-category-migration | IdAM client ID |
| IDAM_MIGRATION_SECRET | test_secret | IdAM client secret |
| IDAM_MIGRATION_URI | http://ccd-role-category-migration | IdAM client re-direct uri |
| IDAM_MIGRATION_USERNAME | master.caseworker@gmail.com | IdAM user's username |
| IDAM_MIGRATION_PASSWORD | Pa55word11 | IdAM user's password |
| MIGRATION_PAGE_NUMBER | 0 | Page number of request |
| MIGRATION_PAGE_SIZE | 5000 | Number of records per page |



