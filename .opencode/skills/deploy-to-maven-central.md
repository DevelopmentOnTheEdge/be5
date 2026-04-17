# Deploy to Maven Central

## Description

This skill provides instructions for deploying the be5 project to Maven Central.

## When to Use

Use this skill when you need to deploy the be5 project or any of its modules to Maven Central.

## Instructions

### 1. Update Version Numbers

Before deploying, increment all version numbers across all modules:

- Parent POM: `0.4.x` -> `0.4.x+1`
- Independent modules (freemarker-patched, maven, util/app, util/jetty): bump their own versions

Update each module's `<parent><version>` from `0.4.x` to `0.4.x+1`:

```bash
# Update parent version in all module pom.xml files
sed -i 's/<version>0.4.x<\/version>/<version>0.4.x+1<\/version>/g' */pom.xml
```

Example changes for version 0.4.2 -> 0.4.3:
- `pom.xml`: `<version>0.4.2</version>` -> `<version>0.4.3</version>`
- `base/pom.xml`: `<version>0.4.2</version>` -> `<version>0.4.3</version>`
- And so on for all modules

### 2. Run Tests

```bash
mvn -B test
```

### 3. Deploy to Maven Central

```bash
mvn -B -DskipTests source:jar javadoc:jar deploy
```

This builds source and javadoc jars first, then deploys. IMPORTANT: Source and javadoc jars must be generated before deployment or be5-maven-plugin will fail.

This will:
- Build all modules
- Run checkstyle, tests, jacoco
- Generate sources and javadoc jars
- Sign artifacts with GPG
- Deploy to Maven Central using central-publishing-maven-plugin

### Notes

- Uses central-publishing-maven-plugin (version 0.5.0) with autoPublish
- Requires GPG signing for all artifacts
- Each module inherits from be5-parent
- Independent modules (freemarker-patched, maven, util/app, util/jetty) have their own versioning

## Verification

After deployment, verify artifacts are available on Maven Central:
- https://search.maven.org/artifact/com.developmentontheedge.be5/be5-parent