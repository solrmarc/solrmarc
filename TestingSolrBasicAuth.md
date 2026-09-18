# Testing Solr Basic Authentication Support

This documents how to manually verify SolrMarc's `-solrUser` / `-solrPassword`
/ `-solrPasswordFile` support against a real, genuinely secured Solr
instance. This is deliberately a manual procedure, not an automated CI job
- see "Why this isn't automated" at the bottom.

## Prerequisites

- Docker
- **Java 11 or later.** Java 8 cannot load Solr 9's solrj classes at all
  (`UnsupportedClassVersionError: ... Unsupported major.minor version 55.0`)
  - this is a hard requirement, not a bug to work around.
- A Solr-9-compatible `lib-solrj` directory (see below).

## 1. Start a real, secured Solr 9 instance

```bash
docker run -d --name solrtest -p 8983:8983 \
  -e SOLR_OPTS="-Dbasicauth=solr:SolrRocks" \
  solr:9 solr-fg -c
```

**Gotchas that cost significant time to discover, worth reading before
you hit them yourself:**

- **Use `solr-fg -c`, not `solr -c`.** The official Solr Docker image only
  adds the implicit `-f` (foreground) flag when you use the *default*
  command. If you specify a command explicitly (`solr -c`), no `-f` is
  added - `bin/solr start` runs, backgrounds itself, and the wrapping
  process exits, which Docker interprets as "container's job is done" and
  stops it (exit code 0, not a crash - it genuinely did start Solr, just
  not in a way that keeps the container alive). `solr-fg` is a dedicated
  wrapper that adds `-f` for you.

- **`SOLR_AUTH_TYPE`/`SOLR_AUTHENTICATION_OPTS` do not reliably work** as
  Docker environment variables with this image (see
  [docker-solr/docker-solr#220](https://github.com/docker-solr/docker-solr/issues/220)
  for another report of the identical symptom). Use `SOLR_OPTS` directly
  instead, as shown above - it's the mechanism actually confirmed to work
  in Solr's own Docker FAQ.

- **`bin/solr auth enable` run via `docker exec` after the fact does NOT
  retroactively enable auth enforcement on an already-running Solr
  process.** It successfully pushes `security.json` to ZooKeeper, but the
  JVM needs `-Dbasicauth=...` (or equivalent) present *at startup* to
  actually check it. The `SOLR_OPTS` env var above handles this
  correctly since it's applied before Solr's JVM ever starts.

## 2. Create a collection and push the security config

```bash
docker exec -it solrtest bin/solr create -c mycollection
docker exec -it solrtest bin/solr auth enable --type basicAuth --credentials solr:SolrRocks
```

## 3. Verify auth is actually being enforced

**Critical gotcha: do NOT test against `/admin/ping`.** It's deliberately
left unauthenticated by design (so load balancers/monitoring tools can
check liveness without credentials) - testing against it will return
`200` regardless of whether auth is configured correctly, and proves
nothing. This cost significant debugging time before being caught.

Test against a real data endpoint instead:

```bash
# Expect 401 - no credentials
curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8983/solr/mycollection/select?q=*:*"

# Expect 200 - correct credentials
curl -s -o /dev/null -w "%{http_code}\n" -u solr:SolrRocks "http://localhost:8983/solr/mycollection/select?q=*:*"
```

## 4. Getting a Solr-9-compatible `lib-solrj` directory

**Modern Solr (9.x+) no longer ships a `dist/solrj-lib` directory** - that
layout was removed in [SOLR-15916](https://github.com/apache/solr).
Solrj and its dependencies now live inside the server's own
`server/solr-webapp/webapp/WEB-INF/lib/` in the official binary
distribution, bundled together with everything the whole Solr *server*
needs (Lucene, ZooKeeper, the admin UI's dependency-injection framework,
etc.) - far more than a client actually needs.

For a minimal, correct set of just what solrj itself declares as its own
dependencies, use Maven's dependency resolver against solrj's real
published POM rather than guessing at versions by hand:

```xml
<!-- throwaway-pom.xml -->
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>temp</groupId><artifactId>temp</artifactId><version>1.0</version>
  <dependencies>
    <dependency>
      <groupId>org.apache.solr</groupId>
      <artifactId>solr-solrj</artifactId>
      <version>9.10.1</version> <!-- check for current version -->
    </dependency>
  </dependencies>
</project>
```

```bash
mvn -f throwaway-pom.xml dependency:copy-dependencies -DoutputDirectory=lib-solrj-9.x
```

**Note on overlapping jars, now that `Boot.java` is fixed:** check for jars
that overlap between SolrMarc's own `lib/` directory and the `-solrj`
directory (e.g. `slf4j-api`) if you want to avoid a different, subtler
problem. Before the `Boot.loadClass()` fix described below, having the
same class name in two jars on the combined classpath caused a hard
`LinkageError: attempted duplicate class definition` crash - loud, but at
least unambiguous about the cause. With the fix in place, that crash can
no longer happen: the loop now stops at the *first* matching jar it
finds and never attempts a second definition. But that means if `lib/`
and `-solrj` disagree on version for some shared library, whichever one
happens to come first in Boot's internal URL list (currently: `-solrj`'s
jars are added before `lib/`'s, so `-solrj`'s version generally wins for
anything overlapping) is used *silently* - the other copy is just
ignored, with no warning. If the "losing" version happens to be missing
a method something else actually needs, you'd get a different, often
harder-to-diagnose failure (`NoSuchMethodError`, `AbstractMethodError`)
instead of a clear crash pointing at the conflict. Avoiding duplicate
jars entirely (rather than relying on which one happens to win) is still
the safer choice - just no longer a hard requirement to avoid a crash.

## 5. Run the actual verification, four ways

```bash
BASE="java -jar $solrmarc_jar IndexDriver -dir $solrmarc_dir,./test/data -config \"$solrmarcconfig\" -solrURL http://localhost:8983/solr/mycollection -solrj ./lib-solrj-9.x/"

# 1. No credentials - should fail (401 surfaced as an indexing error)
$BASE ./test/data/records/u399.mrc

# 2. Wrong credentials - should also fail
$BASE -solrUser solr -solrPassword wrongpassword ./test/data/records/u399.mrc

# 3. Correct credentials via -solrPassword - should succeed
$BASE -solrUser solr -solrPassword SolrRocks ./test/data/records/u399.mrc

# 4. Correct credentials via -solrPasswordFile - should also succeed
echo -n "SolrRocks" > /tmp/solr-pw.txt
$BASE -solrUser solr -solrPasswordFile /tmp/solr-pw.txt ./test/data/records/u399.mrc
```

**What success/failure actually looks like:** watch the final summary
line, not just whether an ERROR appeared - a successful run ends with
`N records sent to Solr`, a rejected one ends with `0 records sent to
Solr` (the record was read and processed, but the actual Solr write was
rejected). A genuine auth rejection shows an HTTP 401 body
(`Bad credentials`) in the log around the `Indexer.java` error lines.

## Related bugs found and fixed while establishing this test

Two real, previously-latent bugs were found during initial verification
of this feature - neither was specific to auth, and both could affect
any user hitting the same conditions:

- **`Boot.java`'s `loadClass()` was missing a `return` after successfully
  defining a class**, causing it to keep scanning remaining classpath
  URLs even after a match. If the same class name existed in more than
  one jar on Boot's combined classpath (e.g. the same library present in
  both a `-dir` library folder and the `-solrj` folder), this caused
  `LinkageError: attempted duplicate class definition`. Fixed by adding
  the missing `return` immediately after a successful `defineClass()`.

- **`IndexDriver`'s Solr-connection-error handler was swallowing the real
  exception cause**, logging only `sre.getMessage()` as a string instead
  of passing the `Throwable` itself to the logger. This made every
  connection-related failure much harder to diagnose than necessary.
  Fixed by passing `sre` as a second argument to `logger.error(...)`, so
  log4j prints the full cause chain.

## Why this isn't automated as a CI test

This procedure genuinely requires Docker, a real Solr container,
ZooKeeper, and a specific solrj jar bundle - none of which are
appropriate for a fast, reliable unit-test suite. Getting this working
even once (manually, with a human iterating on each failure) took
significant back-and-forth due to Docker image quirks, environment
variable timing, and Solr's own auth-enable semantics - none of which
have anything to do with whether SolrMarc's *code* is correct. Automating
this into CI risks a flaky job that fails on infrastructure/timing issues
far more often than it catches real regressions.

The actual code paths (option parsing, the reflective
`withBasicAuthCredentials` wiring, and the "unsupported client" error
paths) already have real automated coverage that runs on every commit -
see `BootableMainSolrPasswordTest` and `SolrCoreLoaderAuthTest`. This
manual procedure exists specifically to catch the one class of thing
those can't: real solrj version compatibility and real classloading
behavior under production-like conditions.
