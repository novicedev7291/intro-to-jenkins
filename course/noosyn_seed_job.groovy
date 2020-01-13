import javaposse.jobdsl.dsl.DslFactory

DslFactory factory = this

repos = [
        [name: "analytics-svc", url: "https://bitbucket.org/noosyn/analytics-svc.git", branch: "dev"]
]


factory.job('noosyn-seed-job'){
    triggers {
        githubPush()
    }
    scm {
        github('novicedev7291/intro-to-jenkins')
    }
    steps {
        dsl(
                """
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
               script(readFileFromWorkspace("course/example_job.groovy"))
               sandbox()
           }
       }

   }
});
                """
        )
    }
}
