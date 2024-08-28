#!/usr/bin/env groovy

def bob = "./bob/bob"
def LOCKABLE_RESOURCE_LABEL = "kaas"
def validateSdk = 'false'

def String job_type = ""
def String pr_type = ""
@Library('oss-common-pipeline-lib@dVersion-2.0.0-hybrid') _   // Shared library from the OSS/com.ericsson.oss.ci/oss-common-ci-utils

pipeline {
    agent { label env.NODE_LABEL }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    }

    environment {
        RELEASE = "true"
        KUBECONFIG = "${WORKSPACE}/.kube/config"
        MAVEN_CLI_OPTS = "-Duser.home=${env.HOME} -B -s ${env.WORKSPACE}/settings.xml"
        OPEN_API_SPEC_DIRECTORY = "src/main/resources/v1"
        VHUB_API_TOKEN = credentials('vhub-api-key-id')
        ANCHORE_ENABLED = "true"
    }

    stages {
        stage('Prepare') {
            steps {
                deleteDir()
                script {
                    ci_pipeline_init.clone_project()
                    ci_pipeline_init.setBuildName()
                }
                sh "${bob} --help"
                sh "${bob} -lq"
                echo 'Inject settings.xml into workspace:'
                configFileProvider([configFile(fileId: "${env.SETTINGS_CONFIG_FILE_NAME}", targetLocation: "${env.WORKSPACE}")]) {}
            }
        }

        stage('Clean') {
            steps {
                sh "${bob} clean"
            }
        }

        stage('Init') {
            steps {
                sh "${bob} init-drop"
            }
        }

        stage('Lint') {
            steps {
                parallel(
                    "lint markdown": {
                        sh "${bob} lint:markdownlint lint:vale"
                    },
                    "lint helm": {
                        sh "${bob} lint:helm"
                    },
                    "lint helm design rule checker": {
                        sh "${bob} lint:helm-chart-check"
                    },
                    "lint code": {
                        sh "${bob} lint:license-check"
                    },
                    "lint OpenAPI spec": {
                        sh "${bob} lint:oas-bth-linter"
                    },
                    "lint metrics": {
                        sh "${bob} lint:metrics-check"
                    },
                    "SDK Validation": {
                        script {
                            if (validateSdk == "true") {
                                sh "${bob} validate-sdk"
                            }
                        }
                    }
                )
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: '**/*bth-linter-output.html, **/design-rule-check-report.*'
                }
            }
        }

        stage('Generate') {
            steps {
                parallel(
                    "Open API Spec": {
                        sh "${bob} rest-2-html:check-has-open-api-been-modified"
                        script {
                            def val = readFile '.bob/var.has-openapi-spec-been-modified'
                            if (val.trim().equals("true")) {
                                sh "${bob} rest-2-html:zip-open-api-doc"
                                sh "${bob} rest-2-html:generate-html-output-files"

                                manager.addInfoBadge("OpenAPI spec has changed. HTML Output files will be published to the CPI library.")
                                archiveArtifacts artifacts: "rest_conversion_log.txt"
                            }
                        }
                    },
                    "Generate Docs": {
                        sh "${bob} generate-docs"
                        archiveArtifacts "build/doc/**/*.*"
                        publishHTML (target: [
                            allowMissing: false,
                            alwaysLinkToLastBuild: false,
                            keepAll: true,
                            reportDir: 'build/doc',
                            reportFiles: 'CTA_api.html',
                            reportName: 'REST API Documentation'
                        ])
                    }
                )
            }
        }

        stage('Build') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                    sh "${bob} build"
                }
            }
        }

        stage('Test') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]){
                    sh "${bob} test"
                }
            }
        }

        stage('SonarQube') {
            when {
                expression { env.SQ_ENABLED == "true"}
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]){
                    withSonarQubeEnv("${env.SQ_SERVER}") {
                        sh "${bob} sonar-enterprise-release"
                    }
                }
            }
        }

        stage('Image') {
            steps {
                sh "${bob} image"
                sh "${bob} image-dr-check"
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: '**/image-design-rule-check-report*'
                }
            }
        }

        stage('Package') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                    sh "${bob} package"
                    sh "${bob} package-jars"
                    sh "${bob} delete-images-from-agent:delete-internal-image"
                }
            }
        }

        stage('K8S Resource Lock') {
            options {
                lock(label: LOCKABLE_RESOURCE_LABEL, variable: 'RESOURCE_NAME', quantity: 1)
            }
            environment {
                K8S_CLUSTER_ID = sh(script: "echo \${RESOURCE_NAME} | cut -d'_' -f1", returnStdout: true).trim()
                K8S_NAMESPACE = sh(script: "echo \${RESOURCE_NAME} | cut -d',' -f1 | cut -d'_' -f2", returnStdout: true).trim()
            }
            stages {
                stage('Helm Install') {
                    steps {
                        echo "Inject kubernetes config file (${env.K8S_CLUSTER_ID}) based on the Lockable Resource name: ${env.RESOURCE_NAME}"
                        configFileProvider([configFile(fileId: "${env.K8S_CLUSTER_ID}", targetLocation: "${env.KUBECONFIG}")]) {}
                        echo "The namespace (${env.K8S_NAMESPACE}) is reserved and locked based on the Lockable Resource name: ${env.RESOURCE_NAME}"

                        sh "${bob} helm-dry-run"
                        sh "${bob} create-namespace"

                        sh "${bob} helm-install"
                        sh "${bob} healthcheck"
                    }
                    post {
                        always {
                            sh "${bob} kaas-info || true"
                            archiveArtifacts allowEmptyArchive: true, artifacts: 'build/kaas-info.log'
                        }
                        unsuccessful {
                            withCredentials([usernamePassword(credentialsId: 'SERO_ARTIFACTORY', usernameVariable: 'SERO_ARTIFACTORY_REPO_USER', passwordVariable: 'SERO_ARTIFACTORY_REPO_PASS')]) {
                                sh "${bob} collect-k8s-logs || true"
                            }
                            archiveArtifacts allowEmptyArchive: true, artifacts: "k8s-logs/*"
                            sh "${bob} delete-namespace"
                        }
                    }
                }
                stage('K8S Test') {
                    steps {
                        sh "${bob} helm-test"
                    }
                }
                stage('Vulnerability Analysis') {
                    steps {
                        parallel(
                            "Hadolint": {
                                script {
                                    sh "${bob} hadolint-scan"
                                    echo "Evaluating Hadolint Scan Resultcodes..."
                                    sh "${bob} evaluate-design-rule-check-resultcodes"
                                    archiveArtifacts "build/va-reports/hadolint-scan/**.*"
                                }
                            },
                            "Kubehunter": {
                                script {
                                    configFileProvider([configFile(fileId: "${K8S_CLUSTER_ID}", targetLocation: "${env.KUBECONFIG}")]) {}
                                    sh "${bob} kubehunter-scan"
                                    archiveArtifacts "build/va-reports/kubehunter-report/**/*"
                                }
                            },
                            "Kubeaudit": {
                                script {
                                    sh "${bob} kube-audit"
                                    archiveArtifacts "build/va-reports/kube-audit-report/**/*"
                                }
                            },
                            "Kubsec": {
                                script {
                                    sh "${bob} kubesec-scan"
                                    archiveArtifacts "build/va-reports/kubesec-reports/*"
                                }
                            },
                            "Trivy": {
                                script {
                                    sh "${bob} trivy-inline-scan"
                                    archiveArtifacts "build/va-reports/trivy-reports/**.*"
                                    archiveArtifacts "trivy_metadata.properties"
                                }
                            },
                            "X-Ray": {
                                script {
                                    sleep(60)
                                    withCredentials([usernamePassword(credentialsId: 'XRAY_SELI_ARTIFACTORY', usernameVariable: 'XRAY_USER', passwordVariable: 'XRAY_APIKEY')]) {
                                        sh "${bob} fetch-xray-report"
                                    }
                                    archiveArtifacts "build/va-reports/xray-reports/xray_report.json"
                                    archiveArtifacts "build/va-reports/xray-reports/raw_xray_report.json"
                                }
                            },
                            "Anchore-Grype": {
                                script {
                                    if (env.ANCHORE_ENABLED == "true") {
                                        sh "${bob} anchore-grype-scan"
                                        archiveArtifacts "build/va-reports/anchore-reports/**.*"
                                    }else{
                                        echo "stage Anchore-Grype skipped"
                                    }
                                }
                            },
                           "NMAP-Unicorn": {
                                script {
                                    withCredentials([usernamePassword(credentialsId: 'SERO_ARTIFACTORY', usernameVariable: 'SERO_ARTIFACTORY_REPO_USER', passwordVariable: 'SERO_ARTIFACTORY_REPO_PASS')]){
                                        if (env.NMAP_ENABLED == "true") {
                                            configFileProvider([configFile(fileId: "${K8S_CLUSTER_ID}", targetLocation: "${env.KUBECONFIG}")]) {}
                                            sh "${bob} nmap-port-scanning"
                                            archiveArtifacts "build/va-reports/nmap_reports/**/**.*"
                                        }else{
                                            echo "stage NMAP Unicorn skipped"
                                        }
                                    }
                                }
                            },
                            "ZAP": {
                                script {
                                    if (env.ZAP_ENABLED == "true") {
                                        withCredentials([usernamePassword(credentialsId: 'SERO_ARTIFACTORY', usernameVariable: 'SERO_ARTIFACTORY_REPO_USER', passwordVariable: 'SERO_ARTIFACTORY_REPO_PASS')]) {
                                            sh "${bob} zap-scan"
                                            archiveArtifacts "build/va-reports/zap-reports/**/**.*"
                                        }
                                    } else {
                                        echo "stage ZAP skipped"
                                    }
                                }
                            }
                        )
                    }
                    post {
                        unsuccessful {
                            withCredentials([usernamePassword(credentialsId: 'SERO_ARTIFACTORY', usernameVariable: 'SERO_ARTIFACTORY_REPO_USER', passwordVariable: 'SERO_ARTIFACTORY_REPO_PASS')]) {
                                sh "${bob} collect-k8s-logs || true"
                            }
                            archiveArtifacts allowEmptyArchive: true, artifacts: 'k8s-logs/**/*.*'
                        }
                        cleanup {
                            sh "${bob} delete-namespace"
                            sh "${bob} delete-images-from-agent:cleanup-anchore-trivy-images"
                            sh "rm -f ${env.KUBECONFIG}"
                        }
                    }
                }
            }
        }
        stage('Generate Vulnerability report V2.0'){
            steps {
                sh "${bob} generate-VA-report-V2:upload"
                archiveArtifacts allowEmptyArchive: true, artifacts: 'build/va-reports/**_Vulnerability_Report_2.0.md'
            }
        }
        stage('Publish') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS')]) {
                    sh "${bob} publish"
                    sh "${bob} publish-md-oas"
                }
            }
            post {
                cleanup {
                    sh "${bob} delete-images-from-agent:delete-internal-image delete-images-from-agent:delete-drop-image"
                }
            }
        }
        stage('FOSSA Analyze') {
            steps {
                withCredentials([string(credentialsId: 'FOSSA_API_token', variable: 'FOSSA_API_KEY')]){
                    sh "${bob} fossa-analyze"
                }
            }
        }
        stage('FOSSA Fetch Report') {
            steps {
                withCredentials([string(credentialsId: 'FOSSA_API_token', variable: 'FOSSA_API_KEY')]){
                    sh "${bob} fossa-scan-status-check"
                    sh "${bob} fetch-fossa-report-attribution"
                    archiveArtifacts "*fossa-report.json"
                }
            }
        }
        stage('FOSSA Dependency Validate') {
            steps {
                withCredentials([string(credentialsId: 'FOSSA_API_token', variable: 'FOSSA_API_KEY')]){
                    sh "${bob} dependency-validate"
                }
            }
        }
        stage('Upload Marketplace Documentation') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'SELI_ARTIFACTORY', usernameVariable: 'SELI_ARTIFACTORY_REPO_USER', passwordVariable: 'SELI_ARTIFACTORY_REPO_PASS'),
                string(credentialsId: 'CSAC_MARKETPLACE_TOKEN', variable: 'CSAC_MARKETPLACE_TOKEN')]) {
                    sh "${bob} generate-doc-zip-package"
                    archiveArtifacts "build/doc-marketplace/**"
                    sh "${bob} marketplace-upload-release"
                    sh "${bob} marketplace-refresh"
                }
            }
        }
    }
    post {
        success {
            withCredentials([usernamePassword(credentialsId: 'GERRIT_PASSWORD', usernameVariable: 'GERRIT_USERNAME', passwordVariable: 'GERRIT_PASSWORD')]){
                sh "${bob} create-git-tag"
                sh "git pull origin HEAD:master"
                script{
                    ci_pipeline_post.bumpVersion("publish")
                }
            }
            script {
                sh "${bob} helm-chart-check-report-warnings"
                ci_pipeline_post.sendHelmDRWarningEmail("publish")
                ci_pipeline_post.modifyBuildDescription("publish")
            }
        }
        always{
            sh "${bob} delete-images-from-agent"
            archiveArtifacts allowEmptyArchive: true, artifacts: "publish.Jenkinsfile, ruleset2.0.yaml, artifact.properties"
        }
    }
}
