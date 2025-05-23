package us.ihmc.atlas.behaviors;

import controller_msgs.msg.dds.StereoVisionPointCloudMessage;
import perception_msgs.msg.dds.LidarScanMessage;
import perception_msgs.msg.dds.PlanarRegionsListMessage;
import us.ihmc.atlas.sensors.AtlasSLAMBasedREAStandaloneLauncher;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.behaviors.tools.PlanarRegionSLAMMapper;
import us.ihmc.behaviors.tools.perception.MultisenseHeadStereoSimulator;
import us.ihmc.behaviors.tools.perception.MultisenseLidarSimulator;
import us.ihmc.behaviors.tools.perception.RealsensePelvisSimulator;
import us.ihmc.commons.exception.DefaultExceptionHandler;
import us.ihmc.commons.exception.ExceptionTools;
import us.ihmc.communication.PerceptionAPI;
import us.ihmc.communication.packets.PlanarRegionMessageConverter;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.log.LogTools;
import us.ihmc.robotEnvironmentAwareness.communication.converters.PointCloudMessageTools;
import us.ihmc.robotEnvironmentAwareness.io.FilePropertyHelper;
import us.ihmc.robotEnvironmentAwareness.updaters.LIDARBasedREAModule;
import us.ihmc.robotEnvironmentAwareness.updaters.REANetworkProvider;
import us.ihmc.robotEnvironmentAwareness.updaters.REAPlanarRegionPublicNetworkProvider;
import us.ihmc.robotics.geometry.PlanarRegionsList;
import us.ihmc.ros2.ROS2Node;
import us.ihmc.ros2.ROS2NodeBuilder;
import us.ihmc.ros2.ROS2Publisher;
import us.ihmc.tools.thread.PausablePeriodicThread;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static us.ihmc.atlas.behaviors.AtlasPerceptionSimulation.Fidelity.LOW;
import static us.ihmc.robotEnvironmentAwareness.communication.REACommunicationProperties.*;

public class AtlasPerceptionSimulation
{
   public enum Fidelity { LOW, HIGH }

   private final boolean runRealsenseSLAM;
   private final boolean runLidarREA;
   private PausablePeriodicThread multisenseLidarPublisher;
   private PausablePeriodicThread multisenseRegionsPublisher;
   private PausablePeriodicThread realsenseRegionsPublisher;
   private AtlasSLAMBasedREAStandaloneLauncher realsenseSLAMFramework;
   private PausablePeriodicThread realsensePointCloudPublisher;
   private LIDARBasedREAModule lidarREA;

   public AtlasPerceptionSimulation(PlanarRegionsList map,
                                    boolean runRealsenseSLAM,
                                    boolean spawnUIs,
                                    boolean runLidarREA,
                                    DRCRobotModel robotModel,
                                    Fidelity fidelity)
   {
      this.runRealsenseSLAM = runRealsenseSLAM;
      this.runLidarREA = runLidarREA;
      ROS2Node ros2Node = new ROS2NodeBuilder().build("perception");

      // might be a weird delay with threads at 0.5 hz depending on each other
      double period = 1.0;

      int multisenseLidarScanSize;
      int multisenseStereoSphereScanSize;
      int realsenseSphereScanSize;
      double realsenseRange = 3.0;
      double multisenseStereoRange = 10.0;
      if (fidelity == LOW)
      {
         multisenseLidarScanSize = 200;
         multisenseStereoSphereScanSize = 8000;
         realsenseSphereScanSize = 4000;
      }
      else
      {
         multisenseLidarScanSize = 500;
         multisenseStereoSphereScanSize = 50000;
         realsenseSphereScanSize = 30000;
      }

      if (runLidarREA)
      {
         MultisenseLidarSimulator multisenseLidar = new MultisenseLidarSimulator(robotModel, ros2Node, map, multisenseLidarScanSize);
         ROS2Publisher<LidarScanMessage> publisher = ros2Node.createPublisher(PerceptionAPI.MULTISENSE_LIDAR_SCAN);
         multisenseLidar.addLidarScanListener(scan -> publisher.publish(PointCloudMessageTools.toLidarScanMessage(scan, multisenseLidar.getSensorPose())));

         ExceptionTools.handle(() ->
         {
            REANetworkProvider networkProvider = new REAPlanarRegionPublicNetworkProvider(
                  new ROS2NodeBuilder().build(PerceptionAPI.REA_NODE_NAME),
                  outputTopic,
                  lidarOutputTopic,
                  stereoOutputTopic,
                  depthOutputTopic
            );
            File reaConfigurationFile = Paths.get(System.getProperty("user.home")).resolve(".ihmc/REAModuleConfiguration.txt").toFile();
            lidarREA = LIDARBasedREAModule.createRemoteModule(new FilePropertyHelper(reaConfigurationFile), networkProvider);
            lidarREA.start();
         }, DefaultExceptionHandler.PRINT_STACKTRACE);
      }
      else
      {
         MultisenseHeadStereoSimulator multisenseStereo = new MultisenseHeadStereoSimulator(map,
                                                                                            robotModel,
                                                                                            ros2Node,
                                                                                            multisenseStereoRange,
                                                                                            multisenseStereoSphereScanSize);
         ROS2Publisher<PlanarRegionsListMessage> publisher = ros2Node.createPublisher(PerceptionAPI.LIDAR_REA_REGIONS);
         multisenseRegionsPublisher = new PausablePeriodicThread("MultisenseREARegionsPublisher", period,
            () -> publisher.publish(PlanarRegionMessageConverter.convertToPlanarRegionsListMessage(multisenseStereo.computeRegions())));
         multisenseRegionsPublisher.start();
      }

      RealsensePelvisSimulator realsense = new RealsensePelvisSimulator(map, robotModel, ros2Node, realsenseRange, realsenseSphereScanSize);
      if (runRealsenseSLAM)
      {
         ROS2Publisher<StereoVisionPointCloudMessage> publisher = ros2Node.createPublisher(PerceptionAPI.D435_POINT_CLOUD);
         realsensePointCloudPublisher = new PausablePeriodicThread("RealsensePointCloudPublisher", period,
            () ->
            {
               List<Point3DReadOnly> pointCloud = realsense.getPointCloud();
               if (!pointCloud.isEmpty())
                  publisher.publish(PointCloudMessageTools.toStereoVisionPointCloudMessage(pointCloud, realsense.getSensorPose()));
            });
         realsensePointCloudPublisher.start();
         realsenseSLAMFramework = new AtlasSLAMBasedREAStandaloneLauncher(spawnUIs);
      }
      else
      {
         PlanarRegionSLAMMapper realsenseSLAM = new PlanarRegionSLAMMapper();
         ROS2Publisher<PlanarRegionsListMessage> publisher = ros2Node.createPublisher(PerceptionAPI.REALSENSE_SLAM_REGIONS);
         realsenseRegionsPublisher = new PausablePeriodicThread("RealsenseSLAMPublisher", period,
            () -> publisher.publish(PlanarRegionMessageConverter.convertToPlanarRegionsListMessage(realsenseSLAM.update(realsense.computeRegions()))));
         realsenseRegionsPublisher.start();
      }
   }

   public void destroy()
   {
      LogTools.info("Shutting down...");
      if (runLidarREA)
      {
         lidarREA.stop();
      }
      else
      {
         multisenseRegionsPublisher.stop();
      }
      if (runRealsenseSLAM)
      {
         realsensePointCloudPublisher.stop();
         realsenseSLAMFramework.stop();
      }
      else
      {
         realsenseRegionsPublisher.stop();
      }
   }
}
