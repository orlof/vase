# VASE
Simple Java Framework for Object-Relational Database Mapping 
and Dependency Injection.

Vase is a minimalistic implementation of Java Framework that provides 
 - CRUD (Create, Read, Update and Delete) functions for java Objects and 
   Relational database Initially only PostgreSQL interface is implemented, 
   but adding support for new databases should be trivial.
 - Dependency Injection for setting up and shutting down application

##Object-Database mapping

Vase data model in build into java classes with Annotations. Database schema 
can be created automatically from the data model. The Principle is that 
there is one-to-one mapping between Java classes and database tables.

Example Java data model:

<pre>
package org.megastage.vase.example;
 
import org.megastage.vase.*;
import java.sql.Timestamp;
 
@SqlTableName("person")
public class Person {
    @SqlSerial @SqlKey
    public int id;
    
    @SqlNotNull
    public String name;
    
    @SqlUnique @SqlNotNull
    public String ssn;
    
    public Timestamp birthTime;
}
</pre>

<pre>
package org.megastage.vase.example;
 
import org.megastage.vase.*;
 
@SqlTableName("model")
public enum Brand {
    ModelS, ModelX, Model3;
}
</pre>

<pre>
package org.megastage.vase.example;
 
import org.megastage.vase.*;
 
@SqlTableName("car")
public class Car {
    @SqlSerial @SqlKey
    public int id;
    
    @SqlReferences(Person.class)
    public int owner;
    
    public Brand model;
}
</pre>

Running SchemaExportPSQL for above Java classes results following database
Schema:

<pre>
DROP TABLE IF EXISTS car;
DROP TABLE IF EXISTS person;
DROP TYPE IF EXISTS model;
 
CREATE TYPE model AS ENUM (ModelS, ModelX, Model3);
 
CREATE TABLE person (
    id SERIAL,
    name VARCHAR(255) NOT NULL,
    ssn VARCHAR(255) NOT NULL UNIQUE,
    birthTime TIMESTAMP
);
 
CREATE TABLE car (
    id SERIAL,
    owner INTEGER REFERENCES person,
    model model
);
</pre>