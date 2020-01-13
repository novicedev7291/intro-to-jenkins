pipeline {
    agent {
        any()
    }
    stages {
        stage('Code Checkout') {
            steps {
                script{
                    currentBuild.displayName = "#ecs-analytics-svc:dev_v_${BUILD_NUMBER}"
                }
                checkout([$class: 'GitSCM', branches: [[name: '*/dev']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '8fa0eb33-4dd6-4626-8659-dc633ab92154', url: 'https://bitbucket.org/noosyn/analytics-svc.git']]])
            }
        }
        stage('MVN Build') {
            steps {
                script{
                    currentBuild.displayName = "#ecs-analytics-svc:dev_v_${BUILD_NUMBER}"
                }
                sh "/var/lib/jenkins/tools/hudson.tasks.Maven_MavenInstallation/maven-3.3.9/bin/mvn clean install"
            }
        }
        stage ("nexus Artifact Uploader") {
            steps {
                script{
                    currentBuild.displayName = "#ecs-analytics-svc:dev_v_${BUILD_NUMBER}"
                }
                // nexusArtifactUploader artifacts: [[artifactId: 'analytics-svc', classifier: '', file: 'target/analytics-svc-1.0.0-SNAPSHOT.jar', type: 'jar']], credentialsId: 'noosynnexus', groupId: 'com.noosyn.analytics', nexusUrl: 'nexus.frood.tech', nexusVersion: 'nexus3', protocol: 'http', repository: 'frood-erp-hosted-dev', version: '1.0.0-SNAPSHOT'
            }
        }
        stage('Docker Build and Publish') {
            steps {
                script{
                    currentBuild.displayName = "#ecs-analytics-svc:dev_v_${BUILD_NUMBER}"
                }

                script{
                    withDockerServer([uri: 'unix:///var/run/docker.sock']) {
                        withDockerRegistry([credentialsId: 'ecr:us-east-1:Devops_AWS', url: 'https://041385637391.dkr.ecr.us-east-1.amazonaws.com/analytics-svc']) {
                            sh '''DOCKER_LOGIN=`aws ecr get-login --region us-east-1 --no-include-email`
							sudo ${DOCKER_LOGIN}'''
                            sh 'sudo docker build -t 041385637391.dkr.ecr.us-east-1.amazonaws.com/analytics-svc:dev_v_$BUILD_NUMBER .'
                            sh 'sudo docker push 041385637391.dkr.ecr.us-east-1.amazonaws.com/analytics-svc:dev_v_$BUILD_NUMBER'
                        }
                    }
                }
            }
        }
        stage("Update Service/Task") {
            steps {
                script{
                    currentBuild.displayName = "#ecs-analytics-svc:dev_v_${BUILD_NUMBER}"
                }
                sh '''#!/bin/bash
				#Constants
				
				REGION=us-east-1
				REPOSITORY_NAME=analytics-svc
				CLUSTER=development
				FAMILY=`jq .family task-definitions/dev_taskdef.json | tr -d '"'`
				NAME=`jq .containerDefinitions[].name task-definitions/dev_taskdef.json| tr -d '"'`
				
				SERVICE_NAME=${NAME}-svc
				
				#Store the repositoryUri as a variable
				REPOSITORY_URI=`aws ecr describe-repositories --repository-names ${REPOSITORY_NAME} --region ${REGION} | jq .repositories[].repositoryUri | tr -d '"'`
				
				#Replace the build number and respository URI placeholders with the constants above
				sed -e "s;%BUILD_NUMBER%;${BUILD_NUMBER};g" -e "s;%REPOSITORY_URI%;${REPOSITORY_URI};g" task-definitions/dev_taskdef.json > ${NAME}-v_${BUILD_NUMBER}.json
				#Register the task definition in the repository
				aws ecs register-task-definition --family ${FAMILY} --cli-input-json file://${WORKSPACE}/${NAME}-v_${BUILD_NUMBER}.json --region ${REGION}
				SERVICES=`aws ecs describe-services --services ${SERVICE_NAME} --cluster ${CLUSTER} --region ${REGION} | jq .failures[]`
				#Get latest revision
				REVISION=`aws ecs describe-task-definition --task-definition ${NAME} --region ${REGION} | jq .taskDefinition.revision`
				
				#Create or update service
				if [ "$SERVICES" == "" ]; then
				echo "entered existing service"
				DESIRED_COUNT=`aws ecs describe-services --services ${SERVICE_NAME} --cluster ${CLUSTER} --region ${REGION} | jq .services[].desiredCount`
				if [ ${DESIRED_COUNT} = "0" ]; then
					DESIRED_COUNT="1"
				fi
				aws ecs update-service --cluster ${CLUSTER} --region ${REGION} --service ${SERVICE_NAME} --task-definition ${FAMILY}:${REVISION} --desired-count ${DESIRED_COUNT}
				else
				echo "entered new service"
				aws ecs create-service --service-name ${SERVICE_NAME} --desired-count 1 --task-definition ${FAMILY} --cluster ${CLUSTER} --region ${REGION}
				fi'''
            }
        }
        stage ("CleanUp Jar Files") {
            steps {
                script{
                    currentBuild.displayName = "#ecs-analytics-svc:dev_v_${BUILD_NUMBER}"
                }
                sh 'rm -rf target/*.jar'
            }
        }
    }
}