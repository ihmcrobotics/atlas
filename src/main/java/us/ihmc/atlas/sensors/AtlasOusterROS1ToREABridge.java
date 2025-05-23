package us.ihmc.atlas.sensors;

import perception_msgs.msg.dds.LidarScanMessage;
import sensor_msgs.PointCloud2;
import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.avatar.drcRobot.ROS2SyncedRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.behaviors.tools.CommunicationHelper;
import us.ihmc.commons.UnitConversions;
import us.ihmc.commons.thread.Throttler;
import us.ihmc.communication.PerceptionAPI;
import us.ihmc.communication.configuration.NetworkParameters;
import us.ihmc.euclid.referenceFrame.interfaces.FramePose3DReadOnly;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.humanoidRobotics.frames.HumanoidReferenceFrames;
import us.ihmc.perception.depthData.PointCloudData;
import us.ihmc.ros2.ROS2Node;
import us.ihmc.ros2.ROS2NodeBuilder;
import us.ihmc.tools.thread.MissingThreadTools;
import us.ihmc.tools.thread.ResettableExceptionHandlingExecutorService;
import us.ihmc.utilities.ros.RosMainNode;
import us.ihmc.utilities.ros.RosTools;
import us.ihmc.utilities.ros.subscriber.AbstractRosTopicSubscriber;

public class AtlasOusterROS1ToREABridge
{
   private static final double REA_OUTPUT_FREQUENCY = UnitConversions.hertzToSeconds(10.0);

   public AtlasOusterROS1ToREABridge()
   {
      RosMainNode ros1Node = RosTools.createRosNode(NetworkParameters.getROSURI(), "ouster_to_rea");
      ROS2Node ros2Node = new ROS2NodeBuilder().build("ouster_to_rea");
      AtlasRobotModel robotModel = new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_DUAL_ROBOTIQ, RobotTarget.REAL_ROBOT);
      CommunicationHelper ros2Helper = new CommunicationHelper(robotModel, ros2Node);
      ROS2SyncedRobotModel syncedRobot = ros2Helper.newSyncedRobot();
      RigidBodyTransform transformToWorld = new RigidBodyTransform();
      Throttler throttler = new Throttler();

      ResettableExceptionHandlingExecutorService executor = MissingThreadTools.newSingleThreadExecutor("OusterToREABridge", true);

      AbstractRosTopicSubscriber<PointCloud2> ousterSubscriber = new AbstractRosTopicSubscriber<PointCloud2>(PointCloud2._TYPE)
      {
         @Override
         public void onNewMessage(PointCloud2 pointCloud2)
         {
            if (throttler.run(REA_OUTPUT_FREQUENCY))
            {
               executor.submit(() ->
               {
                  syncedRobot.update();
//                  if (syncedRobot.getDataReceptionTimerSnapshot().isRunning(3.0))
                  {
                     PointCloudData pointCloudData = new PointCloudData(pointCloud2, 1600000, false);
                     FramePose3DReadOnly ousterPose = syncedRobot.getFramePoseReadOnly(HumanoidReferenceFrames::getOusterLidarFrame);
                     ousterPose.get(transformToWorld);
                     pointCloudData.applyTransform(transformToWorld);
                     LidarScanMessage lidarScanMessage = pointCloudData.toLidarScanMessage();
                     lidarScanMessage.getLidarPosition().set(ousterPose.getPosition());
                     lidarScanMessage.getLidarOrientation().set(ousterPose.getOrientation());
                     lidarScanMessage.setSensorPoseConfidence(1.0);
                     ros2Helper.publish(PerceptionAPI.MULTISENSE_LIDAR_SCAN, lidarScanMessage);
                  }
               });
            }
         }
      };
      ros1Node.attachSubscriber(RosTools.OUSTER_POINT_CLOUD, ousterSubscriber);
      ros1Node.execute();

      Runtime.getRuntime().addShutdownHook(new Thread(() ->
      {
         executor.destroy();
         ros1Node.shutdown();
      }, "IHMC-OusterROS1ToREABridgeShutdown"));
   }

   public static void main(String[] args)
   {
      new AtlasOusterROS1ToREABridge();
   }
}
