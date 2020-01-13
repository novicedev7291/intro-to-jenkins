import javaposse.jobdsl.dsl.MemoryJobManagement;
import javaposse.jobdsl.dsl.DslScriptLoader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public class JobScriptsTest{

    @Test
    public void should_compile_scripts() throws IOException{
        MemoryJobManagement jm = new MemoryJobManagement();
        DslScriptLoader loader = new DslScriptLoader(jm);
        String scriptText = new String(Files.readAllBytes(
                new File("course/job_dsl.groovy").toPath()
        ));
        loader.runScript(scriptText);
    }

    @Test
    public void should_compile_noosyn_scripts () throws IOException{
        MemoryJobManagement jm = new MemoryJobManagement();
        DslScriptLoader loader = new DslScriptLoader(jm);
        String scriptText = new String (Files.readAllBytes(
                new File("course/noosyn_seed_job.groovy").toPath()
        ));
        loader.runScript(scriptText);
    }
}