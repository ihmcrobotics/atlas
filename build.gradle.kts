plugins {
   id("us.ihmc.ihmc-build")
}

ihmc {
   group = "us.ihmc"
   version = "0.0.1"
   vcsUrl = "https://github.com/ihmcrobotics/atlas"
   openSource = false

   configureDependencyResolution()
   configurePublications()
}

mainDependencies {
   api("org.apache.xmlgraphics:batik-dom:1.14")

   api("us.ihmc:ihmc-avatar-interfaces-visualizers:0.14.0-250815")
   api("us.ihmc:robotiq-hand-drivers:0.14.0-250815")
   api("us.ihmc:ihmc-model-file-loader:0.14.0-250815")
   api("us.ihmc:ihmc-manipulation-planning:0.14.0-250815")
   api("us.ihmc:ihmc-parameter-tuner:0.15.3")
   api("us.ihmc:ihmc-footstep-planning-visualizers:0.14.0-250815")
   api("us.ihmc:ihmc-high-level-behaviors:0.14.0-250815")
}

testDependencies {
   api("us.ihmc:ihmc-avatar-interfaces-test:0.14.0-250815")
   api("us.ihmc:ihmc-sensor-processing-test:0.14.0-250815")
   api("us.ihmc:ihmc-simulation-toolkit-test:0.14.0-250815")
   api("us.ihmc:ihmc-messager-test:0.2.1")
}
