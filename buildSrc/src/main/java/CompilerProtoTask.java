import com.google.gradle.osdetector.OsDetector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;


public class CompilerProtoTask extends DefaultTask {

    Logger logger = Logging.getLogger(CompilerProtoTask.class);

    @Input
    String protoDir;

    @OutputDirectory
    String outGeneratedDir;

    {
        outGeneratedDir = getProject().getBuildDir() + "/generated/source/protos/";
    }

    @TaskAction
    void action() {

        OsDetector osDetector = new OsDetector();


        //这个名字用于创建一个configuration，configuration可以简单理解为管理一组依赖的管理器
        String MycName = "customPluginConfiguration";
        //创建一个管理器名字为customPluginConfiguration
        Configuration configuration = getProject().getConfigurations().create(MycName);

        //这个是构造proto的artifact的信息,这些信息可以查看pom文件得知
        HashMap<String, String> protocArtifactMap = new HashMap<>();
        protocArtifactMap.put("group", "com.google.protobuf");
        protocArtifactMap.put("name", "protoc");
        protocArtifactMap.put("version", "3.14.0");
        protocArtifactMap.put("classifier", osDetector.getClassifier());
        protocArtifactMap.put("ext", "exe");

        //添加依赖到MycName这个管理器中
        Dependency protoDependency = getProject().getDependencies().add(MycName, protocArtifactMap);

        //用管理器返回这个依赖的所有的文件
        FileCollection files = configuration.fileCollection(protoDependency);
        //因为这个依赖只会存在一个文件也就是编译器
        File protoExe = files.getFiles().stream().findFirst().get();


        try {
            //获得扩展类对象实例，主要用于获取用户配置的proto文件路径
            String protoDirPath = protoDir;

            File file1 = new File(getProject().getProjectDir(), protoDirPath);
            //得到用户配置proto文件目录下的所有后缀为proto的文件
            String[] extensionFilter = {"proto"};
            Collection<File> protoDifFile = FileUtils.listFiles(new File(getProject().getProjectDir(), protoDirPath), extensionFilter, false);

            //拼接命令行字符串
            StringBuilder cmd = new StringBuilder(protoExe.getAbsolutePath() + " ");

            File outFile = new File(outGeneratedDir);
            if (!outFile.exists()) {
                outFile.mkdirs();
            }
            cmd.append("--java_out=" + outGeneratedDir);
            for (File file : protoDifFile) {
                String replaceFilePath = " " + file.getPath().replaceFirst(file1.getAbsolutePath() + "/", "") + " ";
                cmd.append(replaceFilePath);
            }
            cmd.append(" -I" + protoDirPath + " ");

            logger.info("运行编译命令 " + cmd);
            //防止编译器无权限运行
            if (!protoExe.canExecute() && !protoExe.setExecutable(true)) {
                throw new GradleException("protoc编译器无法执行");
            }


            //执行命令行
            Process exec = null;
            try {
                String[] strings = new String[0];
                exec = Runtime.getRuntime().exec(cmd.toString(), strings, getProject().getProjectDir());
                int resultCode = exec.waitFor();

                //执行成功
                if (resultCode == 0) {

                } else {
                    throw new GradleException("编译proto文件错误" + IOUtils.toString(exec.getErrorStream()));
                }
            } finally {
                if (exec != null) {
                    exec.destroy();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
