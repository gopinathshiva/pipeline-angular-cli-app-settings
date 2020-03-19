import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.1"

project {

    vcsRoot(HttpsGithubComGopinathshivaAngularCliAppRefsHeadsMaster)

    buildType(id02ChromeTesting)
    buildType(id01Lint)
    buildType(id02PhantomTest)
    buildType(id03Build)

    template(Testing)

    features {
        feature {
            id = "PROJECT_EXT_3"
            type = "project-graphs"
            param("series", """
                [
                  {
                    "type": "valueType",
                    "title": "Build Duration (excluding Checkout Time)",
                    "sourceBuildTypeId": "AngularCliApp_Build",
                    "key": "BuildDurationNetTime"
                  }
                ]
            """.trimIndent())
            param("format", "text")
            param("hideFilters", "")
            param("title", "Build Duration")
            param("defaultFilters", "")
            param("seriesTitle", "Serie")
        }
    }
}

object id01Lint : BuildType({
    id("01Lint")
    name = "01. Check Lint"

    vcs {
        root(HttpsGithubComGopinathshivaAngularCliAppRefsHeadsMaster)
    }

    steps {
        script {
            name = "Install & Run Lint"
            scriptContent = """
                npm install
                npm run lint
            """.trimIndent()
        }
    }
})

object id02ChromeTesting : BuildType({
    templates(Testing)
    id("02ChromeTesting")
    name = "02. Chrome Testing"

    params {
        param("test_script_name", "test-chrome")
    }

    dependencies {
        snapshot(id01Lint) {
        }
    }
})

object id02PhantomTest : BuildType({
    templates(Testing)
    id("02PhantomTest")
    name = "02. Phantom Test"

    params {
        param("test_name", "test-phantomjs")
    }

    steps {
        script {
            name = "Install & Run PhantomJS testing"
            id = "RUNNER_12"
            scriptContent = """
                npm install
                npm run %test_name%
            """.trimIndent()
        }
    }

    dependencies {
        snapshot(id01Lint) {
        }
    }
})

object id03Build : BuildType({
    id("03Build")
    name = "03. Build"

    vcs {
        root(HttpsGithubComGopinathshivaAngularCliAppRefsHeadsMaster)
    }

    steps {
        script {
            executionMode = BuildStep.ExecutionMode.RUN_ON_SUCCESS
            scriptContent = """
                npm install
                npm run build
            """.trimIndent()
        }
    }

    triggers {
        vcs {
        }
    }

    dependencies {
        snapshot(id02ChromeTesting) {
        }
        snapshot(id02PhantomTest) {
        }
    }
})

object Testing : Template({
    name = "testing"

    vcs {
        root(HttpsGithubComGopinathshivaAngularCliAppRefsHeadsMaster)
    }

    steps {
        script {
            name = "Install & Run testing"
            id = "RUNNER_12"
            scriptContent = """
                npm install
                npm run %test_script_name%
            """.trimIndent()
        }
    }
})

object HttpsGithubComGopinathshivaAngularCliAppRefsHeadsMaster : GitVcsRoot({
    name = "https://github.com/gopinathshiva/angular-cli-app#refs/heads/master"
    url = "https://github.com/gopinathshiva/angular-cli-app"
})
