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
NotifySlackSpec > testDefaultConfig() PASSED
NotifySlackSpec > testMessageContainingQuotesAndApostrophesDoesNotBreakPayloadOrShellCommand() PASSED
NotifyTeamsSpec > testDefaultConfig() PASSED
NotifyTeamsSpec > testCustomMessageContainingQuotesDoesNotBreakPayloadOrShellCommand() PASSED

8 tests completed, 0 failed
```

## Coverage

Four of the six `vars/` steps have actual spec files. `dockerBuildPush`
and `llmAnalyzeFailure` still don't; fixing that means writing the
missing specs, not editing this table.

| Step | Tests |
|---|---|
| `buildPython` | default config, custom python version + coverage threshold |
| `deployK8s` | named deployment with rollout check, unnamed deployment fallback |
| `notifySlack` | default config posts via `-d @file`; a message with quotes/apostrophes round-trips through JSON and never reaches the shell command raw |
| `notifyTeams` | default config posts via `-d @file`; a message with quotes round-trips through JSON and never reaches the shell command raw |
| `dockerBuildPush` | not tested |
| `llmAnalyzeFailure` | not tested |
