find . -name ".classpath" -exec rm {} \;
find . -name ".project" -exec rm {} \;
find . -name ".settings" -exec rm {} \;
gradle eclipse
