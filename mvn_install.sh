gradle pom
cd karma-core
~/working/maven/bin/mvn -DskipTests install
~/working/maven/bin/mvn -DskipTests source:jar install
cd ../karma-trace
~/working/maven/bin/mvn -DskipTests -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true install
~/working/maven/bin/mvn -DskipTests -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true source:jar install
cd ../karma-http
~/working/maven/bin/mvn -DskipTests -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true install
~/working/maven/bin/mvn -DskipTests -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true source:jar install
cd ../karma-cluster
~/working/maven/bin/mvn -DskipTests -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true install
~/working/maven/bin/mvn -DskipTests -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true source:jar install
#cd ../karma-demo
#~/working/maven/bin/mvn -DskipTests -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true install
#~/working/maven/bin/mvn -DskipTests -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true source:jar install
