# RMLC: RDF Mapping Language for CSV files
RMLC is the RDF mapping language for CSV files based on the W3C standard R2RML. At this moment, RMLC main contributions are:
- Exploit the implicit joins between CSV files applying SQL transformation functions.
- Create an enriched database schema with the data types of each column (numbers, dates and datetimes) and the primary keys. 


## How it works?
Run RMLC is very simple, you only have to define along the  config.json file defining the following properties per each dataset:
- **databaseName:** The name of your relational database **Is a mandatory parameter**.
- **url:** The url or a local path where the data are **Is a mandatory parameter**.
- **format:** The format of the files **Is not a mandatory parameter**.
- **compression:** If the set of CSV files are compressing, which is their extension **Is not a mandatory parameter**.
- **mapping:** The URL or local path where the mapping is **Is a mandatory parameter**.


This is an example of the configuration file:
```json
{
  "databaseName": "tripsEMT",
  "url":"http://gtfs.es/emt.zip",
  "compression":"zip",
  "mapping":"http://gtfs.es/mapping.rmlc.ttl"
}
```

Also you have to define the connection properties of your MySQL server:

```json
{
  "mysqlURL": "jdbc:mysql://localhost:3306/",
  "mysqluser":"user",
  "mysqlpassword":"password"
}
```

### How to run it?
After the config.json file has been edited, run:
```bash
  mvn clean install
  cp target/RMLC-1.0.jar .
  mkdir sql
  java -jar RMLC-1.0.jar path/to/config/json/file
```

## Contributions (in progress)
Description of the main contributions of RMLC

- Apply transformation functions to columns is already possible. Based on the standard functions in SQL, it is possible to 
modified the object datatypes xsd:string, xsd:date, xsd:dateTime, xsd:decimal, xsd:integer or xsd:double in the CSV to 
get the data in the desired format. For example, it is possible to only get the year of a date or to get a string in 
lowercase without spaces. An example is shown:
```
rr:predicateObjectMap[
    rr:predicate foaf:phone;
    rr:objectMap [
      rr:datatype xsd:string;
      rmlc:columns ["agency_phone"];
      rmlc:functions "TRIM(LOWER(columns[1]))";
    ];
  ];
```
- Formalize the implicit joins between CSV is also possible. Extending the R2RML JoinCondition providing
the application of transformation functions and involving not only one column of each CSV, we are able to express 
joins that other mapping languages as R2RML or RML haven't taken into account. Lets put an example:
```
rr:joinCondition [
    rmlc:child [
      rmlc:colums ["ROUTES_ID"];
      rmlc:functions "LOWER(columns[0])";
    ];
    rmlc:parent [
      rmlc:columns ["SHORT_NAME","SHORT_ID"];
      rmlc:functions "CONCAT(TRIM(columns[0]),'-',LOWER(columns[1])";
    ];
];
```
- Obtain an enriched database schema to load the set of CSV files in a MySQL server: at this moment,
all mapping engines that provide a way to get virtualized RDF translating SPAQRL-to-SQL with CSV files
do not take into account the schema of this files. To improve the performance of this
queries, the primary keys and foreing keys of each table should be define during the load process of
the CSV files in a SQL engine. Also, datatypes of each column should be included to provide a way to
use transformation functions along the columns (e.g. get the year of a date is impossible if the column's
type is a string).


## Authors
