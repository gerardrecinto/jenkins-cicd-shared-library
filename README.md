![demo](docs/assets/demo2.gif)

# groovylibrary

Jenkins Shared Library for standardizing CI/CD across Python microservice teams. Used by 100+ engineers across 10 product lines in 5 global regions, replacing per-team Jenkinsfile sprawl with reusable steps that enforce consistent build, test, Docker, Kubernetes, and notification patterns.

Cut pipeline setup time by 80%. 68% of new WorkflowJob pipelines adopted it within the first quarter.

![Groovy](https://img.shields.io/badge/Groovy-Jenkins%20DSL-4298B8?logo=apache-groovy&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-Shared%20Library-D24939?logo=jenkins&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Deploy-Kubernetes-326CE5?logo=kubernetes&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## Library structure

```
vars/
├── buildPython.groovy        # pip install, ruff lint, pytest + coverage enforcement
├── dockerBuildPush.groovy    # build + push with SHA and :latest tags
├── deployK8s.groovy          # kubectl apply + rollout watch + auto-rollback
├── notifySlack.groovy        # color-coded Slack build status
├── notifyTeams.groovy        # color-coded Teams Actionable Message Card
└── llmAnalyzeFailure.groovy  # tail build log → LLM triage → Slack thread reply
```

---

## Jenkins setup

Go to **Manage Jenkins → Configure System → Global Pipeline Libraries** and add:

| Field | Value |
|---|---|
| Name | `groovylibrary` |
| Default version | `main` |
| Retrieval method | Modern SCM (GitHub) |
| Repository URL | `https://github.com/gerardrecinto/groovylibrary` |

---

## Steps

### `buildPython(config)`

Installs requirements, runs ruff, pytest with coverage. Fails if coverage drops below threshold.

```groovy
buildPython(
    pythonVersion: '3.11',
    requirementsFile: 'requirements.txt',
    testDir: 'tests/',
    coverageThreshold: 80
)
```

### `dockerBuildPush(config)`

Builds and pushes two tags: the commit SHA and `latest`.

```groovy
dockerBuildPush(
    imageName: 'my-service',
    registry: 'registry.example.com',
    credentialsId: 'docker-registry-creds',
    dockerfile: 'Dockerfile'
)
```

### `deployK8s(config)`

Applies the manifest, waits on `kubectl rollout status`. Auto-rolls back on timeout.

```groovy
deployK8s(
    manifestPath: 'k8s/deployment.yaml',
    namespace: 'production',
    kubeConfigCredential: 'kubeconfig-prod',
    timeoutMinutes: 5
)
```

### `notifySlack(config)`

Color-coded build status to Slack. Accepts SUCCESS, FAILURE, UNSTABLE.

```groovy
notifySlack(
    status: 'SUCCESS',
    channel: '#ci-cd',
    webhookCredential: 'slack-webhook-url',
    message: "Build #${env.BUILD_NUMBER} deployed to production"
)
```

### `notifyTeams(config)`

Color-coded Actionable Message Card to Teams via Incoming Webhook.

```groovy
notifyTeams(
    status: 'SUCCESS',
    webhookCredential: 'teams-webhook-url',
    message: "Build #${env.BUILD_NUMBER} deployed to production"
)
```

### `llmAnalyzeFailure(config)`

On failure, grabs the last N log lines, sends them to an LLM with a triage prompt, posts the root cause as a Slack thread reply.

```groovy
llmAnalyzeFailure(
    apiCredential: 'openai-api-key',
    slackChannel: '#ci-failures',
    logLines: 100,
    model: 'gpt-4o'
)
```

Sample output:
```
Root cause: ModuleNotFoundError on 'boto3' in src/uploader.py.
boto3 is not in requirements.txt.
Fix: add boto3>=1.34.0 to requirements.txt and rerun.
```

---

## Full pipeline example

```groovy
@Library('groovylibrary') _

pipeline {
    agent { label 'python-agent' }

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['staging', 'production'])
        booleanParam(name: 'SKIP_TESTS', defaultValue: false)
    }

    environment {
        IMAGE_NAME = 'my-python-service'
        REGISTRY   = 'registry.example.com'
    }

    stages {
        stage('Build') {
            steps {
                buildPython(pythonVersion: '3.11', coverageThreshold: 80)
            }
        }
        stage('Docker') {
            steps {
                dockerBuildPush(
                    imageName: env.IMAGE_NAME,
                    registry: env.REGISTRY,
                    credentialsId: 'docker-creds'
                )
            }
        }
        stage('Deploy') {
            steps {
                deployK8s(
                    manifestPath: 'k8s/',
                    namespace: params.ENVIRONMENT,
                    kubeConfigCredential: 'kubeconfig'
                )
            }
        }
    }

    post {
        success {
            notifySlack(status: 'SUCCESS', channel: '#deployments', webhookCredential: 'slack-webhook')
        }
        failure {
            llmAnalyzeFailure(apiCredential: 'openai-api-key', slackChannel: '#ci-failures')
            notifySlack(status: 'FAILURE', channel: '#deployments', webhookCredential: 'slack-webhook')
        }
    }
}
```

---

## License

MIT
