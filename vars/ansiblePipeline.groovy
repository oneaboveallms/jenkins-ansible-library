def call(Map config) {
    pipeline {
        agent any
        stages {
            stage('Clone Repository') {
                steps {
                    git branch: config.branch, url: config.repoUrl
                }
            }
            stage('User Approval') {
                steps {
                    input message: config.approvalMessage, ok: config.approvalButton
                }
            }
            stage('Playbook Execution') {
                steps {
                    script {
                        ansiblePlaybook(
                            playbook: config.playbookPath
                        )
                    }
                }
            }
        }
        post {
            success {
                slackSend channel: config.slackChannel, message: config.successMessage
            }
            failure {
                slackSend channel: config.slackChannel, message: config.failureMessage
            }
            unstable {
                slackSend channel: config.slackChannel, message: config.unstableMessage
            }
        }
    }
}
