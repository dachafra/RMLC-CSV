DROP TABLE IF EXISTS SHAPES;
CREATE TABLE SHAPES (`SHAPE_ID` VARCHAR(200),`SHAPE_PT_LAT` DOUBLE,`SHAPE_PT_LON` DOUBLE,`SHAPE_PT_SEQUENCE` VARCHAR(200),PRIMARY KEY (SHAPE_ID,SHAPE_PT_SEQUENCE));
DROP TABLE IF EXISTS TRIPS;
CREATE TABLE TRIPS (`ROUTE_ID` VARCHAR(200),`TRIP_ID` VARCHAR(200),`SERVICE_ID` VARCHAR(200),`TRIP_SHORT_NAME` VARCHAR(200),`DIRECTION_ID` BOOL,`SHAPE_ID` VARCHAR(200),`WHEELCHAIR_ACCESSIBLE` VARCHAR(200),PRIMARY KEY (TRIP_ID));
DROP TABLE IF EXISTS STOPS;
CREATE TABLE STOPS (`STOP_ID` VARCHAR(200),`STOP_NAME` VARCHAR(200),`STOP_LAT` DOUBLE,`STOP_LON` DOUBLE,`STOP_URL` VARCHAR(200),`WHEELCHAIR_BOARDING` VARCHAR(200),PRIMARY KEY (STOP_ID));
DROP TABLE IF EXISTS CALENDAR;
CREATE TABLE CALENDAR (`SERVICE_ID` VARCHAR(200),`MONDAY` BOOL,`TUESDAY` BOOL,`WEDNESDAY` BOOL,`THURSDAY` BOOL,`FRIDAY` BOOL,`SATURDAY` BOOL,`SUNDAY` BOOL,`START_DATE` DATE,`END_DATE` DATE,PRIMARY KEY (SERVICE_ID));
DROP TABLE IF EXISTS STOP_TIMES;
CREATE TABLE STOP_TIMES (`TRIP_ID` VARCHAR(200),`ARRIVAL_TIME` VARCHAR(200),`DEPARTURE_TIME` VARCHAR(200),`STOP_ID` VARCHAR(200),`STOP_SEQUENCE` VARCHAR(200),`PICKUP_TYPE` VARCHAR(200),PRIMARY KEY (TRIP_ID,STOP_ID,ARRIVAL_TIME));
DROP TABLE IF EXISTS FREQUENCIES;
CREATE TABLE FREQUENCIES (`TRIP_ID` VARCHAR(200),`START_TIME` VARCHAR(200),`END_TIME` VARCHAR(200),`HEADWAY_SECS` VARCHAR(200),PRIMARY KEY (TRIP_ID,START_TIME));
DROP TABLE IF EXISTS CALENDAR_DATES;
CREATE TABLE CALENDAR_DATES (`SERVICE_ID` VARCHAR(200),`DATE` DATE,`EXCEPTION_TYPE` BOOL,PRIMARY KEY (SERVICE_ID,DATE));
DROP TABLE IF EXISTS FEED_INFO;
CREATE TABLE FEED_INFO (`FEED_PUBLISHER_NAME` VARCHAR(200),`FEED_PUBLISHER_URL` VARCHAR(200),`FEED_LANG` VARCHAR(200));
DROP TABLE IF EXISTS AGENCY;
CREATE TABLE AGENCY (`AGENCY_ID` VARCHAR(200),`AGENCY_NAME` VARCHAR(200),`AGENCY_URL` VARCHAR(200),`AGENCY_TIMEZONE` VARCHAR(200),`AGENCY_LANG` VARCHAR(200),PRIMARY KEY (AGENCY_ID));
DROP TABLE IF EXISTS ROUTES;
CREATE TABLE ROUTES (`ROUTE_ID` VARCHAR(200),`AGENCY_ID` VARCHAR(200),`ROUTE_SHORT_NAME` VARCHAR(200),`ROUTE_LONG_NAME` VARCHAR(200),`ROUTE_TYPE` VARCHAR(200),`ROUTE_URL` VARCHAR(200),PRIMARY KEY (ROUTE_ID));