package us.ihmc.atlas.sensors;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.sensors.realsense.MapSensePlanarRegionROS1Bridge;
import us.ihmc.communication.configuration.NetworkParameters;
import us.ihmc.ros2.ROS2Node;
import us.ihmc.ros2.ROS2NodeBuilder;
import us.ihmc.utilities.ros.RosMainNode;
import us.ihmc.utilities.ros.RosTools;

public class AtlasMapSenseROS1Bridge
{
   public static void main(String[] args)
   {
      AtlasRobotModel robotModel = new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS);
      RosMainNode ros1Node = RosTools.createRosNode(NetworkParameters.getROSURI(), "mapsense_bridge");
      ROS2Node ros2Node = new ROS2NodeBuilder().build("mapsense_bridge");
      new MapSensePlanarRegionROS1Bridge(robotModel, ros1Node, ros2Node);
      ros1Node.execute();
   }
}
