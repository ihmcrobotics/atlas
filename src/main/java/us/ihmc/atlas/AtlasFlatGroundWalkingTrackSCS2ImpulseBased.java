package us.ihmc.atlas;

import us.ihmc.avatar.initialSetup.RobotInitialSetup;
import us.ihmc.avatar.scs2.SCS2AvatarSimulation;
import us.ihmc.avatar.scs2.SCS2AvatarSimulationFactory;
import us.ihmc.commonWalkingControlModules.desiredFootStep.footstepGenerator.HeadingAndVelocityEvaluationScriptParameters;
import us.ihmc.ros2.ROS2NodeBuilder;
import us.ihmc.ros2.RealtimeROS2Node;
import us.ihmc.simulationConstructionSetTools.util.HumanoidFloatingRootJointRobot;
import us.ihmc.simulationConstructionSetTools.util.environments.FlatGroundEnvironment;

public class AtlasFlatGroundWalkingTrackSCS2ImpulseBased
{
   private static boolean createYoVariableServer = System.getProperty("create.yovariable.server") != null
         && Boolean.parseBoolean(System.getProperty("create.yovariable.server"));

   private final RealtimeROS2Node realtimeROS2Node = new ROS2NodeBuilder().buildRealtime("flat_ground_walking_track_simulation");

   public AtlasFlatGroundWalkingTrackSCS2ImpulseBased()
   {
      AtlasRobotModel robotModel = new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS);
      FlatGroundEnvironment environment = new FlatGroundEnvironment();

      int recordFrequency = (int) Math.max(1.0, Math.round(robotModel.getControllerDT() / robotModel.getSimulateDT()));

      boolean useVelocityAndHeadingScript = true;
      HeadingAndVelocityEvaluationScriptParameters walkingScriptParameters = new HeadingAndVelocityEvaluationScriptParameters();

      double initialYaw = 0.3;
      RobotInitialSetup<HumanoidFloatingRootJointRobot> robotInitialSetup = robotModel.getDefaultRobotInitialSetup(0.0, initialYaw);

      SCS2AvatarSimulationFactory avatarSimulationFactory = new SCS2AvatarSimulationFactory();
      avatarSimulationFactory.setRobotModel(robotModel);
      avatarSimulationFactory.setRealtimeROS2Node(realtimeROS2Node);
      avatarSimulationFactory.setDefaultHighLevelHumanoidControllerFactory(useVelocityAndHeadingScript, walkingScriptParameters);
      avatarSimulationFactory.setCommonAvatarEnvrionmentInterface(environment);
      avatarSimulationFactory.setRobotInitialSetup(robotInitialSetup);
      avatarSimulationFactory.setSimulationDataRecordTickPeriod(recordFrequency);
      avatarSimulationFactory.setCreateYoVariableServer(createYoVariableServer);
      avatarSimulationFactory.setUseImpulseBasedPhysicsEngine(true);

      SCS2AvatarSimulation avatarSimulation = avatarSimulationFactory.createAvatarSimulation();

      avatarSimulation.start();
   }

   public static void main(String[] args)
   {
      new AtlasFlatGroundWalkingTrackSCS2ImpulseBased();
   }
}
