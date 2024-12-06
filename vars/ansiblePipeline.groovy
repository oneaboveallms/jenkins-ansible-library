def call(Map config) {
    pipeline {
        agent any
        stages {
            stage('Clone Repository') {
                steps {
                    git branch: config.branch, url: config.repoUrl
                }
            }
            stage('Setup Inventory') {
                steps {
                    script {
                        writeFile file: 'inventory.ini', text: '''
                        localhost ansible_host=127.0.0.1 ansible_user=ubuntu
                        '''
                    }
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
                            playbook: config.playbookPath,
                            inventory: 'inventory.ini'
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
