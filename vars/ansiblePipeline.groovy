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
                    sh 'sudo chmod -R 400 /var/lib/jenkins/workspace/assignment-6/ohio.pem'
                }
            }
            stage('Playbook Execution') {
                steps {
                    script {
                        withEnv(['ANSIBLE_HOST_KEY_CHECKING=False']) {
                            ansiblePlaybook(
                                playbook: config.playbookPath,
                                inventory: 'inventory.ini'
                            )
                        }
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
