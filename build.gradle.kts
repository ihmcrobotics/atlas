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

   api("us.ihmc:ihmc-avatar-interfaces-visualizers:source")
   api("us.ihmc:robotiq-hand-drivers:source")
   api("us.ihmc:ihmc-model-file-loader:source")
   api("us.ihmc:ihmc-manipulation-planning:source")
   api("us.ihmc:ihmc-parameter-tuner:0.15.3")
   api("us.ihmc:ihmc-footstep-planning-visualizers:source")
   api("us.ihmc:ihmc-high-level-behaviors:source")
}

testDependencies {
   api("us.ihmc:ihmc-avatar-interfaces-test:source")
   api("us.ihmc:ihmc-sensor-processing-test:source")
   api("us.ihmc:ihmc-simulation-toolkit-test:source")
   api("us.ihmc:ihmc-messager-test:0.2.1")
}
