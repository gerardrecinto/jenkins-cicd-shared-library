# Tests

This library uses [JenkinsPipelineUnit](https://github.com/jenkinsci/JenkinsPipelineUnit) for unit testing Groovy steps without a running Jenkins instance.

## Running

```bash
gradle test
```

(No Gradle wrapper is checked into this repo yet, so this requires a local Gradle install. CI runs the same command.)

## Output

```
> Task :test

BuildPythonSpec > testDefaultConfig() PASSED
BuildPythonSpec > testCustomConfig() PASSED
DeployK8sSpec > testDeployWithRolloutCheck() PASSED
DeployK8sSpec > testDeployWithoutDeploymentName() PASSED

4 tests completed, 0 failed
```

## Coverage

Only two of the six `vars/` steps have actual spec files. The table below
used to claim the other four were tested too; they aren't, and grepping
`test/*.groovy` for their step names turns up nothing. Fixing that means
writing the missing specs, not editing the table again.

| Step | Tests |
|---|---|
| `buildPython` | default config, custom python version + coverage threshold |
| `deployK8s` | named deployment with rollout check, unnamed deployment fallback |
| `dockerBuildPush` | not tested |
| `notifySlack` | not tested |
| `notifyTeams` | not tested |
| `llmAnalyzeFailure` | not tested |
