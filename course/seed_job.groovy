import javaposse.jobdsl.dsl.DslFactory

DslFactory factory = this

factory.job("seed-job"){
    triggers {
        githubPush()
    }
    scm {github("novicedev7291/intro-to-jenkins")}
    steps {
        dsl{
            external("jenkins/intro-to-jenkins/course/job_dsl.groovy")
            removeAction("DISABLE")
            removeViewAction("DELETE")
            ignoreExisting(false)
        }
    }

}