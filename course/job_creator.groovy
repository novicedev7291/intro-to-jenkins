import javaposse.jobdsl.dsl.DslFactory

DslFactory factory = this

repos = [
        [name: "analytics-svc", url: "https://bitbucket.org/noosyn/analytics-svc.git", branch: "dev"]
]

repos.forEach({

    factory.pipelineJob(it.name + "-" + it.branch){
        triggers {
            githubPush()
        }
        logRotator{
            logRotator(0, 20)
        }
        definition {
            cps {
                script(readFileFromWorkspace("jenkins/intro-tojenkins/course/example_job.groovy"))
                sandbox()
            }
        }

    }
});