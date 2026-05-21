# Tests

This library uses [JenkinsPipelineUnit](https://github.com/jenkinsci/JenkinsPipelineUnit) for unit testing Groovy steps without a running Jenkins instance.

## Running

```bash
./gradlew test
```

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

| Step | Tests |
|---|---|
| `buildPython` | default config, custom python version + coverage threshold |
| `deployK8s` | named deployment with rollout check, unnamed deployment fallback |
| `dockerBuildPush` | covered by integration with `deployK8s` in pipeline tests |
| `notifySlack` | Slack webhook payload format |
| `notifyTeams` | Teams MessageCard payload format |
| `llmAnalyzeFailure` | log extraction + OpenAI API call + Slack post |
