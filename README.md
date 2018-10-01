# vase
Super Simple Java Framework for Object to Relational Database Mapping  and Dependency Injection

Vase is a minimalistic implementation of Java Framework that provides 
 - CRUD (Create, Read, Update and Delete) functions for java Objects and Relational database 
   Initially only PostgreSQL interface is implemented, but adding support for new databases should be trivial.
 - Dependency Injection for setting up and shutting down application

Object-Database mapping

Vase data model in build into java classes with Annotations. Database schema can be created automatically from
the data model. The Only Principle is that One Java Class is mapped directly to One Database Table.
