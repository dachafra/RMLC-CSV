# RMLC: RDF Mapping Language for CSV files
RMLC is the RDF mapping language for CSV files based on the W3C standard R2RML. At this moment, RMLC main contributions are:
- Exploit the implicit joins between CSV files applying general and individual transformation functions.
- Create an enriched database schema with the data types of each column (numbers, dates and datetimes) and
the primary and foreign keys. 


## How it works?
Run RMLC is very simple, you only have to define along the 
config.json file defining the following properties per each dataset:
- **databaseName:** The name of your relational database **Is a mandatory parameter**.
- **url:** The url or a local path where the data are **Is a mandatory parameter**.
- **format:** The format of the files **Is not a mandatory parameter**.
- **compression:** If the set of CSV files are compressing, which is their extension **Is not a mandatory parameter**.
- **mapping:** The URL or local path where the mapping is **Is a mandatory parameter**.


This is an example of the configuration file:
```json
{
  "databaseName": "cardValidationsBus",
  "url":"http://mappingpedia.es/cardValidations.zip",
  "compression":"zip",
  "mapping":"http://mappingpedia.es/cardValidationsMapping.rmlc.ttl"
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
      rml:reference "agency_phone";
      rmlc:individualTransFunct rmlcf:lower;
      rmlc:individualTransFunct rmlcf:trim;
    ];
  ];
```
- Formalize the implicit joins between CSV is also possible. Extending the R2RML JoinCondition providing
the application of transformation functions and involving not only one column of each CSV, we are able to express 
joins that other mapping languages as R2RML or RML haven't taken into account. Lets put an example:
```
rr:joinCondition [
    rmlc:child [
      rmlc:columFunction [
        rr:child 'ROUTE_ID';
        rmlc:individualTransFunct rmlcf:lower;
      ]
    ]
    rmlc:parent [
      rmlc:generalTransFunct rmlcf:concat;  
      rmlc:columFunction [
        rr:child 'SHORT_NAME';
        rmlc:individualTransFunct rmlcf:ltrim;
      ]
      rmlc:columFunction [
        rr:child 'SHORT_ID';
        rmlc:individualTransFunct rmlcf:lower;
      ]
    ]
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
David Chaves-Fraga - dchaves@fi.upm.es