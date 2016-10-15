## due to gradle's bug, compile test java in karma-core first
find . -name ".classpath" -exec rm {} \;
find . -name ".project" -exec rm {} \;
gradle clean
gradle eclipse
gradle :karma-core:compileTestJava
gradle build
