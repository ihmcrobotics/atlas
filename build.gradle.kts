plugins {
   id("us.ihmc.ihmc-build")
}

ihmc {
   group = "us.ihmc"
   version = "0.0.1"
   vcsUrl = "https://github.com/ihmcrobotics/alexander"
   openSource = false

   configureDependencyResolution()
   configurePublications()
}

mainDependencies {
   api("us.ihmc:atlas:source")
   api("us.ihmc:ihmc-closed-source-control:source")
   api("us.ihmc:ihmc-avatar-interfaces:source")
   api("us.ihmc:open-alexander:source")
   api("us.ihmc:robotiq-hand-drivers:source")
   api("us.ihmc:ihmc-robotics-tools-joint-kinematics:0.15.3")
}

testDependencies {
   api("us.ihmc:ihmc-avatar-interfaces-test:source")
}

if (ihmc.artifactIsIncludedBuild("ihmc-crocoddyl-wrapper"))
{
   // Dynamically create main-mpc and test-mpc source sets
   sourceSets.create("main-mpc") {
      java.srcDir("src/main-mpc/java")
      compileClasspath += sourceSets["main"].output
      runtimeClasspath += sourceSets["main"].output
   }

   sourceSets.create("test-mpc") {
      java.srcDir("src/test-mpc/java")
      compileClasspath += sourceSets["test"].output + sourceSets["main"].output + sourceSets["main-mpc"].output
      runtimeClasspath += sourceSets["test"].output + sourceSets["main"].output + sourceSets["main-mpc"].output
   }

   // Add dependencies for main-mpc and test-mpc
   // Add dependencies for main-mpc
   dependencies {
      "mainMpcImplementation"(sourceSets["main"].output)
      "mainMpcImplementation"("us.ihmc:ihmc-avatar-interfaces:source")
      "mainMpcImplementation"("us.ihmc:ihmc-mpc-core:source")
   }

   // Add dependencies for test-mpc
   dependencies {
      "testMpcImplementation"(sourceSets["main"].output)
      "testMpcImplementation"(sourceSets["main-mpc"].output)
      "testMpcImplementation"("us.ihmc:ihmc-avatar-interfaces-test:source")
      "testMpcImplementation"("us.ihmc:ihmc-mpc-core-test:source")
   }
}
else
{
   println("Excluding main-mpc and test-mpc from the build because ihmc-crocoddyl-wrapper is not in the workspace.")
}
