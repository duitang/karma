## due to gradle's bug, compile test java in karma-core first
find . -name ".classpath" -exec rm {} \;
find . -name ".project" -exec rm {} \;
find . -name ".settings" -exec rm -rf {} \;
gradle clean
gradle eclipse
gradle idea
gradle :karma-core:compileTestJava
gradle build -x test
