
### GroovyDSLGenerator
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
 <plugins>
   <plugin>
       <groupId>com.developmentontheedge.be5</groupId>
       <artifactId>be5-maven-plugin</artifactId>
       <version>0.1.1-SNAPSHOT</version>
       <configuration>
        <projectPath>./</projectPath>
       </configuration>
       <executions>
        <execution>
         <id>generate-groovy-dsl</id>
         <phase>compile</phase>
         <goals>
          <goal>generate-groovy-dsl</goal>
         </goals>
         <configuration>
          <fileName>${project.build.directory}/generated-sources/java/${project.artifactId}</fileName>
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

