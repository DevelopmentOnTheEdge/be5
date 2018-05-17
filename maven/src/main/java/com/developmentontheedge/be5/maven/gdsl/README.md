
### GdslGenegator
Генерирует GroovyDSL для таблиц в проекте    
GroovyDSL - добавляет поддержку мета классов для groovy:
```groovy
contribute(context(ctype: "com.developmentontheedge.be5.databasemodel.impl.DatabaseModel")) {
    property(name: "companies", type: 'com.developmentontheedge.be5.databasemodel.EntityModel')    
    ...
}

contribute(context(ctype: "com.developmentontheedge.be5.databasemodel.EntityModel")) {
    method name: 'leftShift', type: 'java.lang.String', params: [values: 'Map<String, ? super Object>']
    ...
}

```

Добавление в проект:
```xml
 <dependency>
  <groupId>com.developmentontheedge.be5</groupId>
  <artifactId>be5-maven-plugin</artifactId>
  <version>0.1.1-SNAPSHOT</version>
 </dependency>
``` 
```xml
 <plugins>
   <plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>1.6.0</version>
    <executions>
     <execution>
      <id>GdslGenegator</id>
      <goals>
       <goal>java</goal>
      </goals>
      <phase>compile</phase>
      <configuration>
       <mainClass>com.developmentontheedge.be5.maven.gdsl.GdslGenegator</mainClass>
       <arguments>
        <argument>${project.build.directory}/generated-sources/java/</argument>
        <argument/>
        <argument>${project.artifactId}</argument>
       </arguments>
      </configuration>
     </execution>
    </executions>
   </plugin>
   <plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.0.0</version>
    <executions>
     <execution>
      <phase>generate-sources</phase>
      <goals>
       <goal>add-source</goal>
      </goals>
      <configuration>
       <sources>
        <source>${project.build.directory}/generated-sources/java/</source>
       </sources>
      </configuration>
     </execution>
    </executions>
   </plugin>    
 </plugins>
```

