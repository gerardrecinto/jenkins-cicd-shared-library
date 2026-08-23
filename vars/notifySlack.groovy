#!/usr/bin/env groovy

import groovy.json.JsonOutput

def call(Map config = [:]) {
    def status      = config.get('status', 'UNKNOWN')
    def channel     = config.get('channel', '#ci-cd')
    def webhookCred = config.get('webhookCredential', 'slack-webhook-url')
    def customMsg   = config.get('message', '')

    def colorMap = [
        SUCCESS  : 'good',
        FAILURE  : 'danger',
        UNSTABLE : 'warning',
        UNKNOWN  : '#808080'
    ]
    def color = colorMap.get(status, '#808080')

    def jobLink = "${env.BUILD_URL}"
    def text = customMsg ?: "${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"

    // Build the payload as a real map and let JsonOutput escape it -- the
    // old version string-interpolated channel/text straight into the JSON
    // and then into a single-quoted shell string, so a quote in a commit
    // message or custom message either broke the JSON or broke out of the
    // shell quoting entirely.
    def payload = JsonOutput.toJson([
        channel: channel,
        attachments: [[
            color : color,
            text  : text,
            footer: "<${jobLink}|View Build>",
            ts    : (System.currentTimeMillis() / 1000) as long
        ]]
    ])

    withCredentials([string(credentialsId: webhookCred, variable: 'WEBHOOK_URL')]) {
        writeFile file: 'slack-payload.json', text: payload
        sh '''
            curl -s -X POST "$WEBHOOK_URL" \
                -H 'Content-Type: application/json' \
                -d @slack-payload.json
        '''
        sh 'rm -f slack-payload.json'
    }
}
