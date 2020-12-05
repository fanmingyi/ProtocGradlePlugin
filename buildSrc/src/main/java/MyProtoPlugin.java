import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.LibraryVariant;
import com.android.build.gradle.api.TestVariant;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.wrapper.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import javafx.application.Application;

public class MyProtoPlugin implements Plugin<Project> {

    //gradle 日志输出工具。参数表示是否输出日志，false表示输出
    Logger logger = new Logger(false);

    @Override
    public void apply(Project project) {
        logger.log("MyProtoPlugin 插件被激活");

        /**
         * 创建一个插件DSL扩展，第一个参数为DSL名字，第二个为配置类
         *  比如:
         *     protoConfig{
         *             // protoDirPath为MyProtoConfigExtension内部属性
         *             protoDirPath = "张三"
         *
         *     }
         */
        project.getExtensions()
                .create("protoConfig", MyProtoConfigExtension.class);

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {

                /**
                 * 创建任务并设置输出目录
                 */
                CompilerProtoTask compilerProto = project.getTasks().create("compilerProto", CompilerProtoTask.class);
//                compilerProto.onlyIf(new );
                compilerProto.setGroup("proto");
                MyProtoConfigExtension myProtoConfigExtension = project.getExtensions().getByType(MyProtoConfigExtension.class);
                compilerProto.protoDir = myProtoConfigExtension.protoDirPath;


                if (isAndroidProject(project)) {
                    linkAndroidProject(project);
                } else {
                    linkJavaProject(project);
                }
            }
        });


    }

    void linkAndroidProject(Project project) {

        if (project.getPlugins().hasPlugin(com.android.build.gradle.AppPlugin.class)) {
            //当前是Android 应用工程
            //Android 插件提供了扩展
            // 也就是我们经常的写法
            //
            //  android{
            //
            //  }
            //
            AppExtension extension = (AppExtension) (project.getExtensions().getByName("android"));
            extension.getApplicationVariants().all(configurationAndroidVariant(project));
            extension.getTestVariants().all(configurationAndroidVariant(project));

            extension.getApplicationVariants().all(new Action<ApplicationVariant>() {
                @Override
                public void execute(ApplicationVariant applicationVariant) {
                    System.out.println("Android 正式环境变体  "+applicationVariant.getName() );

                }
            });
            extension.getTestVariants().all(new Action<TestVariant>() {
                @Override
                public void execute(TestVariant testVariant) {
                    System.out.println("Android 测试环境变体 "+testVariant.getName() );
                }
            });
        } else {
            //当前是Android lib工程
            LibraryExtension extension = (LibraryExtension) (project.getExtensions().getByName("android"));
            extension.getLibraryVariants().all(configurationAndroidVariant(project));
            extension.getLibraryVariants().all(configurationAndroidVariant(project));
        }
    }

    @NotNull
    private Action<BaseVariant> configurationAndroidVariant(Project project) {

        return new Action<BaseVariant>() {
            @Override
            public void execute(BaseVariant libraryVariant) {
                CompilerProtoTask compilerProto = (CompilerProtoTask) project.getTasks().getByName("compilerProto");
                //applicationVariant.addJavaSourceFoldersToModel();
                libraryVariant.registerJavaGeneratingTask(compilerProto, new File(compilerProto.outGeneratedDir));

            }
        };
    }


    void linkJavaProject(Project project) {
        SourceSetContainer container = project.getExtensions().getByType(SourceSetContainer.class);
        for (SourceSet sourceSet : container) {
            String compileName = sourceSet.getCompileTaskName("java");
            JavaCompile javaCompile = (JavaCompile) project.getTasks().getByName(compileName);
            Task compilerProto = project.getTasks().getByName("compilerProto");
            javaCompile.dependsOn(compilerProto);
            sourceSet.getJava().srcDirs(compilerProto.getOutputs().getFiles().getFiles());
        }

    }

    //是Android工程
    boolean isAndroidProject(Project project) {
        return project.getPlugins().hasPlugin(com.android.build.gradle.AppPlugin.class) || project.getPlugins().hasPlugin(com.android.build.gradle.LibraryPlugin.class);
    }
}