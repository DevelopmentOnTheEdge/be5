---
name: deploy-maven-central
description: Deploy the be5 project or any of its modules to Maven Central. Use this skill whenever the user mentions deploying be5, publishing to Maven Central, bumping versions for release, running `mvn deploy`, or preparing a be5 release. Trigger even if the user says things like "release a new version", "push to central", or "publish be5 artifacts". Always consult this skill before running any Maven deployment commands for be5.
---

# Deploy be5 to Maven Central

## Overview

Full release workflow for the **be5** project: bump versions, run tests, deploy signed artifacts to Maven Central via `central-publishing-maven-plugin`.

---

## Step 1 — Confirm Current Version

Check the current version before making any changes:

```bash
grep -m1 '<version>' pom.xml
```

Ask the user to confirm the target version if not already specified (e.g., `0.4.2 → 0.4.3`).

---

## Step 2 — Update Version Numbers

Do a global replacement across all module POMs. Example for `0.4.2 → 0.4.3`:

```bash
sed -i 's/<version>0.4.2<\/version>/<version>0.4.3<\/version>/g' pom.xml */pom.xml
```

**Then review the diff** — `sed` replaces all matching occurrences, including dependency references:

```bash
git diff --stat
git diff
```

Fix any unintended replacements before proceeding.

### Versioning rules

- **Parent POM** (`be5-parent`): bump `0.4.x` → `0.4.x+1`
- **Every module's `<parent><version>`**: must match the new parent version
- **Independent modules** (`freemarker-patched`, `maven`, `util/app`, `util/jetty`): have their own version lines — bump independently if needed, they are not tied to the parent chain

---

## Step 3 — Run Tests

```bash
mvn -B test
```

Fix all failures before proceeding. **Do not skip tests for a release build.**

---

## Step 4 — Deploy to Maven Central

```bash
mvn -B -DskipTests source:jar javadoc:jar deploy
```

> ⚠️ **Order matters**: `source:jar` and `javadoc:jar` must appear *before* `deploy`. If `deploy` runs first, `be5-maven-plugin` will fail.

This command:
1. Builds all modules
2. Runs Checkstyle and JaCoCo
3. Generates sources and Javadoc JARs
4. Signs all artifacts with GPG
5. Uploads and auto-publishes via `central-publishing-maven-plugin` (v0.5.0)

### Prerequisites

- GPG key configured and available (`gpg --list-secret-keys` to verify)
- `~/.m2/settings.xml` has credentials for the `central` server
- All module POMs inherit from `be5-parent`

If GPG signing fails, check that `gpg-agent` is running and the key is not expired:

```bash
gpg-agent --daemon   # start if not running
gpg --list-secret-keys --keyid-format LONG
```

---

## Step 5 — Verify on Maven Central

After deployment, confirm the artifacts are live (may take a few minutes to index):

```
https://search.maven.org/artifact/com.developmentontheedge.be5/be5-parent
```

Check that the new version appears in the version list. No manual promotion step is needed — `autoPublish` is enabled in `central-publishing-maven-plugin`.

---

## Quick Reference

| Step | Command |
|------|---------|
| Check version | `grep -m1 '<version>' pom.xml` |
| Bump versions | `sed -i 's/<version>OLD<\/version>/<version>NEW<\/version>/g' pom.xml */pom.xml` |
| Review diff | `git diff` |
| Run tests | `mvn -B test` |
| Deploy | `mvn -B -DskipTests source:jar javadoc:jar deploy` |
| Verify | https://search.maven.org/artifact/com.developmentontheedge.be5/be5-parent |
