# CI: Jenkins Pipeline Setup

This documents the commands used to stand up the `my-cli` Jenkins pipeline
against a local Jenkins instance (running in Docker, `jenkins/jenkins:lts`,
exposed on `localhost:8080`).

Credentials are redacted below as `$JENKINS_USER` / `$JENKINS_PASS`.

## 1. Discover the running Jenkins instance

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080
curl -s http://localhost:8080 | head -30

brew services list | grep -i jenkins
docker ps | grep -i jenkins
launchctl list | grep -i jenkins
```

Confirmed Jenkins was running as a Docker container named `jenkins`.

## 2. Check available build tools inside the Jenkins agent

```bash
docker exec jenkins sh -c "which mvn java git; java -version 2>&1; mvn -version 2>&1"
```

Result: Java 21 (Temurin) and git were present, but `mvn` was not installed
in the container.

## 3. Check installed Jenkins plugins

```bash
curl -s -u $JENKINS_USER:$JENKINS_PASS "http://localhost:8080/pluginManager/api/json?depth=1" \
  | python3 -c "
import json,sys
d=json.load(sys.stdin)
names=[p['shortName'] for p in d['plugins']]
for n in ['maven-plugin','pipeline-maven','git','workflow-aggregator','workflow-cps','jdk-tool']:
    print(n, n in names)
"
```

Result: no `maven-plugin`, `pipeline-maven`, or `jdk-tool` plugin installed,
so a `tools { maven ...; jdk ... }` block in the Jenkinsfile would not
resolve. Decided to use the Maven Wrapper instead so the pipeline is
self-contained.

## 4. Generate the Maven Wrapper

```bash
mise exec -- mvn -q wrapper:wrapper -Dmaven=3.9.16
./mvnw -B -q -version
./mvnw -B clean package
```

This added `mvnw`, `mvnw.cmd`, and `.mvn/wrapper/maven-wrapper.properties`
to the repo.

## 5. Write the Jenkinsfile

Added a declarative `Jenkinsfile` at the repo root with `Checkout`,
`Build`, `Test`, and `Package` stages, each invoking `./mvnw` rather than a
named Jenkins tool installation. Test results are published via `junit`,
and the built jar is archived via `archiveArtifacts`.

## 6. Commit and push

```bash
git add Jenkinsfile mvnw mvnw.cmd .mvn/
git commit -m "Add Jenkinsfile and Maven wrapper for CI"
git push origin main
```

## 7. Create the Jenkins Pipeline job via the REST API

Fetch a CSRF crumb (bound to a session cookie) and POST the job config XML:

```bash
COOKIE_JAR=$(mktemp)
CRUMB=$(curl -s -c "$COOKIE_JAR" -u $JENKINS_USER:$JENKINS_PASS \
  "http://localhost:8080/crumbIssuer/api/json" \
  | python3 -c "import json,sys; print(json.load(sys.stdin)['crumb'])")

curl -s -b "$COOKIE_JAR" -u $JENKINS_USER:$JENKINS_PASS \
  -H "Jenkins-Crumb: $CRUMB" \
  -X POST "http://localhost:8080/createItem?name=my-cli" \
  --header "Content-Type: application/xml" \
  --data-binary @my-cli-job-config.xml

rm -f "$COOKIE_JAR"
```

The job config (`my-cli-job-config.xml`) defines a
`CpsScmFlowDefinition` pointing at:

- repo: `https://github.com/qamarm/my-cli.git`
- branch: `*/main`
- script path: `Jenkinsfile`

```xml
<?xml version="1.1" encoding="UTF-8"?>
<flow-definition plugin="workflow-job">
  <description>Clone and build qamarm/my-cli</description>
  <keepDependencies>false</keepDependencies>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps">
    <scm class="hudson.plugins.git.GitSCM" plugin="git">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>https://github.com/qamarm/my-cli.git</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
```

## 8. Verify the job and trigger a build

```bash
curl -s -u $JENKINS_USER:$JENKINS_PASS "http://localhost:8080/job/my-cli/api/json"

COOKIE_JAR=$(mktemp)
CRUMB=$(curl -s -c "$COOKIE_JAR" -u $JENKINS_USER:$JENKINS_PASS \
  "http://localhost:8080/crumbIssuer/api/json" \
  | python3 -c "import json,sys; print(json.load(sys.stdin)['crumb'])")
curl -s -b "$COOKIE_JAR" -u $JENKINS_USER:$JENKINS_PASS \
  -H "Jenkins-Crumb: $CRUMB" \
  -X POST "http://localhost:8080/job/my-cli/build?delay=0sec"
rm -f "$COOKIE_JAR"
```

## 9. Poll for build completion and inspect the console log

```bash
for i in $(seq 1 20); do
  RESULT=$(curl -s -u $JENKINS_USER:$JENKINS_PASS "http://localhost:8080/job/my-cli/1/api/json")
  BUILDING=$(echo "$RESULT" | python3 -c "import json,sys; print(json.load(sys.stdin).get('building'))")
  if [ "$BUILDING" = "False" ]; then
    echo "$RESULT" | python3 -c "import json,sys; print('result:', json.load(sys.stdin).get('result'))"
    break
  fi
  sleep 3
done

curl -s -u $JENKINS_USER:$JENKINS_PASS "http://localhost:8080/job/my-cli/1/consoleText" \
  | grep -E "^\[Pipeline\] stage|BUILD SUCCESS|Finished:|Archiving"
```

Result: build #1 finished with `SUCCESS`; `Checkout`, `Build`, `Test`, and
`Package` stages all ran and `my-cli.jar` was archived as a build artifact.
