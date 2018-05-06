WebJars
=======

1. Overview
-----------
This tutorial introduces WebJars and how to use them in a Java application.

Simply put, WebJars are client side dependencies packaged into JAR
archive files. They work with most JVM containers and web frameworks.

Here’s a few popular WebJars: Twitter Bootstrap, jQuery, Angular JS,
Chart.js etc; a full list is available on the www.webjars.org.

2. Why Use WebJars?
-------------------
This question has a very simple answer – because it’s easy.

Manually adding and managing client side dependencies often results
in difficult to maintain codebases.

Also, most Java developers prefer to use Maven and Gradle as build
and dependency management tools.

The main problem WebJars solves is making client side dependencies
available on Maven Central and usable in any standard Maven project.

Here are a few interesting advantages of WebJars:

1. We can explicitly and easily manage the client-side dependencies in
   JVM-based web applications
2. We can use them with any commonly used build tool, eg: Maven, Gradle, etc
   WebJars behave like any other Maven dependency – which means that we get
   transitive dependencies as well
3. The Maven/Gradle Dependency
Let’s jump right into it and add Twitter Bootstrap and jQuery to pom.xml:
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>bootstrap</artifactId>
    <version>3.3.7-1</version>
</dependency>
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>jquery</artifactId>
    <version>3.1.1</version>
</dependency>

Now Twitter Bootstrap and jQuery are available on the project classpath;
we can simply reference them and use them in our application.

Note: You can check the latest version of the Twitter Bootstrap and the
jQuery dependencies on Maven Central.

3. Spring Boot and Webjars
--------------------------
Spring Boot automatically configures Spring to map requests for
/webjars to the /META-INF/resources/webjars directory of all the JARs
in the CLASSPATH.

Things can also work out even without specifying the WebJar
(client-side library) versions while including them in HTML view files,
 like we mentioned in the previous snippet. For this to work, we need
 to additionally insert the below mentioned webjar-locator dependency
 into the pom file.

<!-- webjars-locator -->
<dependency>
	<groupId>org.webjars</groupId>
	<artifactId>webjars-locator</artifactId>
	<version>0.30</version>
</dependency>

Spring Boot automatically detects the webjars-locator library in the
classpath and uses it to automatically resolve the version of WebJars
we are trying to insert into our HTML page.
