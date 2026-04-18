---
name: deploy-maven-central
description: Deploy the be5 project or any of its modules to Maven Central. Use this skill whenever the user mentions deploying be5, publishing to Maven Central, bumping versions for release, running `mvn deploy`, or preparing a be5 release. Trigger even if the user says things like "release a new version", "push to central", or "publish be5 artifacts".
---

# Deploy be5 to Maven Central

## Overview

This skill covers the full release workflow for the **be5** project: bumping versions, running tests, and deploying signed artifacts to Maven Central via `central-publishing-maven-plugin`.

---

## Step 1 — Update Version Numbers

Increment all version numbers **before** doing anything else.

### Pattern
- Parent POM: `0.4.x` → `0.4.x+1`
- Independent modules (`freemarker-patched`, `maven`, `util/app`, `util/jetty`): bump their own versions too
- Every module's `<parent><version>` must match the new parent version

### How to apply

First, confirm the current and target versions (check `pom.xml` if unsure):

```bash
grep -m1 '<version>' pom.xml
```

Then do a global sed replacement across all module POMs. Example for `0.4.2 → 0.4.3`:

```bash
sed -i 's/<version>0.4.2<\/version>/<version>0.4.3<\/version>/g' pom.xml */pom.xml
```

> ⚠️ Double-check the result — `sed` will replace **all** matching occurrences, including dependency version references. Review the diff before committing:
> ```bash
> git diff --stat
> ```

---

## Step 2 — Run Tests

```bash
mvn -B test
```

Fix any failures before proceeding. Do **not** skip tests for a release build.

---

## Step 3 — Deploy to Maven Central

```bash
mvn -B -DskipTests source:jar javadoc:jar deploy
```

**Order matters**: `source:jar` and `javadoc:jar` must be built *before* `deploy`, otherwise `be5-maven-plugin` will fail.

This command will:
1. Build all modules
2. Run Checkstyle, JaCoCo
3. Generate sources and Javadoc JARs
4. Sign all artifacts with GPG
5. Upload and auto-publish via `central-publishing-maven-plugin` (v0.5.0)

### Prerequisites
- GPG key configured and available in the environment
- Maven `settings.xml` has credentials for `central` server
- All module POMs inherit from `be5-parent`

---

## Step 4 — Verify on Maven Central

After deployment, confirm the artifacts are live (may take a few minutes):

```
https://search.maven.org/artifact/com.developmentontheedge.be5/be5-parent
```

Check that the new version appears in the version list.

---

## Notes

- Independent modules (`freemarker-patched`, `maven`, `util/app`, `util/jetty`) have **their own versioning** separate from the main parent version chain — bump them independently if needed.
- `autoPublish` is enabled in `central-publishing-maven-plugin`, so no manual promotion step is required.
- If GPG signing fails, ensure `gpg-agent` is running and the key is not expired.
