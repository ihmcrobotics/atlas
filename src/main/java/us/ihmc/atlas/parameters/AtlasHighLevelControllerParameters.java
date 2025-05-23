package us.ihmc.atlas.parameters;

import static us.ihmc.sensorProcessing.outputData.JointDesiredControlMode.EFFORT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import us.ihmc.atlas.AtlasJointMap;
import us.ihmc.commonWalkingControlModules.configurations.GroupParameter;
import us.ihmc.commonWalkingControlModules.configurations.HighLevelControllerParameters;
import us.ihmc.commonWalkingControlModules.controllerCore.parameters.JointAccelerationIntegrationParameters;
import us.ihmc.commonWalkingControlModules.controllerCore.parameters.JointAccelerationIntegrationParametersReadOnly;
import us.ihmc.commonWalkingControlModules.controllerCore.parameters.JointVelocityIntegratorResetMode;
import us.ihmc.commonWalkingControlModules.highLevelHumanoidControl.highLevelStates.WholeBodySetpointParameters;
import us.ihmc.humanoidRobotics.communication.packets.dataobjects.HighLevelControllerName;
import us.ihmc.robotics.partNames.ArmJointName;
import us.ihmc.robotics.partNames.HumanoidJointNameMap;
import us.ihmc.robotics.partNames.LegJointName;
import us.ihmc.robotics.partNames.NeckJointName;
import us.ihmc.robotics.partNames.SpineJointName;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.sensorProcessing.outputData.JointDesiredBehavior;
import us.ihmc.sensorProcessing.outputData.JointDesiredBehaviorReadOnly;
import us.ihmc.sensorProcessing.outputData.JointDesiredControlMode;
import us.ihmc.yoVariables.filters.AlphaFilteredYoVariable;

public class AtlasHighLevelControllerParameters implements HighLevelControllerParameters
{
   private final AtlasJointMap jointMap;
   private boolean runningOnRealRobot;

   public AtlasHighLevelControllerParameters(boolean runningOnRealRobot, AtlasJointMap jointMap)
   {
      this.runningOnRealRobot = runningOnRealRobot;
      this.jointMap = jointMap;
   }

   @Override
   public WholeBodySetpointParameters getStandPrepParameters()
   {
      return new AtlasStandPrepSetpoints(jointMap);
   }

   @Override
   public List<GroupParameter<JointDesiredBehaviorReadOnly>> getDesiredJointBehaviors(HighLevelControllerName state)
   {
      switch (state)
      {
         case WALKING:
            return getDesiredJointBehaviorForWalking();
         case DO_NOTHING_BEHAVIOR:
         case FALLING_STATE:
            return getDesiredJointBehaviorForDoNothing();
         case STAND_PREP_STATE:
         case STAND_READY:
         case STAND_TRANSITION_STATE:
            return getDesiredJointBehaviorForHangingAround();
         default:
            throw new RuntimeException("Implement a desired joint behavior for the high level state " + state);
      }
   }

   private List<GroupParameter<JointDesiredBehaviorReadOnly>> getDesiredJointBehaviorForHangingAround()
   {
      if (runningOnRealRobot)
         return null;

      List<GroupParameter<JointDesiredBehaviorReadOnly>> behaviors = new ArrayList<>();

      configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_YAW, JointDesiredControlMode.POSITION, 500.0, 50.0);
      configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_ROLL, JointDesiredControlMode.POSITION, 500.0, 50.0);
      configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_PITCH, JointDesiredControlMode.POSITION, 500.0, 50.0);
      configureSymmetricBehavior(behaviors, jointMap, LegJointName.KNEE_PITCH, JointDesiredControlMode.POSITION, 100.0, 50.0);
      configureSymmetricBehavior(behaviors, jointMap, LegJointName.ANKLE_PITCH, JointDesiredControlMode.POSITION, 40.0, 10.0);
      configureSymmetricBehavior(behaviors, jointMap, LegJointName.ANKLE_ROLL, JointDesiredControlMode.POSITION, 40.0, 10.0);

      configureBehavior(behaviors, jointMap, SpineJointName.SPINE_YAW, JointDesiredControlMode.POSITION, 1500.0, 150.0);
      configureBehavior(behaviors, jointMap, SpineJointName.SPINE_PITCH, JointDesiredControlMode.POSITION, 1500.0, 150.0);
      configureBehavior(behaviors, jointMap, SpineJointName.SPINE_ROLL, JointDesiredControlMode.POSITION, 1500.0, 150.0);

      configureSymmetricBehavior(behaviors, jointMap, ArmJointName.SHOULDER_YAW, JointDesiredControlMode.POSITION, 250.0, 25.0);
      configureSymmetricBehavior(behaviors, jointMap, ArmJointName.SHOULDER_ROLL, JointDesiredControlMode.POSITION, 500.0, 50.0);
      configureSymmetricBehavior(behaviors, jointMap, ArmJointName.ELBOW_PITCH, JointDesiredControlMode.POSITION, 250.0, 25.0);
      configureSymmetricBehavior(behaviors, jointMap, ArmJointName.ELBOW_ROLL, JointDesiredControlMode.POSITION, 50.0, 10.0);
      configureSymmetricBehavior(behaviors, jointMap, ArmJointName.FIRST_WRIST_PITCH, JointDesiredControlMode.POSITION, 3.0, 0.5);
      configureSymmetricBehavior(behaviors, jointMap, ArmJointName.WRIST_ROLL, JointDesiredControlMode.POSITION, 3.0, 0.5);
      configureSymmetricBehavior(behaviors, jointMap, ArmJointName.SECOND_WRIST_PITCH, JointDesiredControlMode.POSITION, 3.0, 0.5);

      configureBehavior(behaviors, jointMap, NeckJointName.PROXIMAL_NECK_PITCH, JointDesiredControlMode.POSITION, 50.0, 10.0);

      return behaviors;
   }

   @Override
   public List<GroupParameter<JointDesiredBehaviorReadOnly>> getDesiredJointBehaviorsUnderLoad(HighLevelControllerName state)
   {
      if (state == HighLevelControllerName.WALKING)
         return getDesiredJointBehaviorForWalkingUnderLoad();
      else
         return null;
   }

   private List<GroupParameter<JointDesiredBehaviorReadOnly>> getDesiredJointBehaviorForWalkingUnderLoad()
   {
      List<GroupParameter<JointDesiredBehaviorReadOnly>> behaviors = new ArrayList<>();

      if (runningOnRealRobot)
      {
         double maxVelocityError = 1.5;
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_YAW, EFFORT, 0.0, 25.0, maxVelocityError);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_ROLL, EFFORT, 0.0, 60.0, maxVelocityError);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_PITCH, EFFORT, 0.0, 100.0, maxVelocityError);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.KNEE_PITCH, EFFORT, 0.0, 100.0, maxVelocityError);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.ANKLE_PITCH, EFFORT, 0.0, 0.0, maxVelocityError);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.ANKLE_ROLL, EFFORT, 0.0, 0.0, maxVelocityError);
      }
      else
      {
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_YAW, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_ROLL, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_PITCH, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.KNEE_PITCH, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.ANKLE_PITCH, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.ANKLE_ROLL, EFFORT, 0.0, 0.0);
      }

      return behaviors;
   }

   private List<GroupParameter<JointDesiredBehaviorReadOnly>> getDesiredJointBehaviorForWalking()
   {
      List<GroupParameter<JointDesiredBehaviorReadOnly>> behaviors = new ArrayList<>();

      if (runningOnRealRobot)
      {
         configureBehavior(behaviors, jointMap, NeckJointName.PROXIMAL_NECK_PITCH, JointDesiredControlMode.POSITION, 0.0, 0.0);
         //
         JointDesiredControlMode armControlMode = JointDesiredControlMode.POSITION;
         double armDamping = 0.0 * 100.0;
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.SHOULDER_YAW, armControlMode, 0.0, armDamping);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.SHOULDER_ROLL, armControlMode, 0.0, armDamping);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.ELBOW_PITCH, armControlMode, 0.0, armDamping);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.ELBOW_ROLL, armControlMode, 0.0, armDamping);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.FIRST_WRIST_PITCH, armControlMode, 0.0, armDamping);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.WRIST_ROLL, armControlMode, 0.0, armDamping);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.SECOND_WRIST_PITCH, armControlMode, 0.0, armDamping);

         configureBehavior(behaviors, jointMap, SpineJointName.SPINE_YAW, EFFORT, 0.0, 0.0);
         configureBehavior(behaviors, jointMap, SpineJointName.SPINE_PITCH, EFFORT, 0.0, 80.0);
         // Setting the back x high improves icp tracking when stepping side to side...
         configureBehavior(behaviors, jointMap, SpineJointName.SPINE_ROLL, EFFORT, 0.0, 200.0);

         double maxVelocityError = 1.5;
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_YAW, EFFORT, 0.0, 25.0, maxVelocityError);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_ROLL, EFFORT, 0.0, 60.0, maxVelocityError);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_PITCH, EFFORT, 0.0, 100.0, maxVelocityError);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.KNEE_PITCH, EFFORT, 0.0, 100.0, maxVelocityError);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.ANKLE_PITCH, EFFORT, 0.0, 20.0, maxVelocityError);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.ANKLE_ROLL, EFFORT, 0.0, 20.0, maxVelocityError);
      }
      else
      {
         configureBehavior(behaviors, jointMap, NeckJointName.PROXIMAL_NECK_PITCH, EFFORT, 0.0, 0.0);

         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.SHOULDER_YAW, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.SHOULDER_ROLL, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.ELBOW_PITCH, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.ELBOW_ROLL, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.FIRST_WRIST_PITCH, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.WRIST_ROLL, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, ArmJointName.SECOND_WRIST_PITCH, EFFORT, 0.0, 0.0);

         configureBehavior(behaviors, jointMap, SpineJointName.SPINE_YAW, EFFORT, 0.0, 0.0);
         configureBehavior(behaviors, jointMap, SpineJointName.SPINE_PITCH, EFFORT, 0.0, 0.0);
         configureBehavior(behaviors, jointMap, SpineJointName.SPINE_ROLL, EFFORT, 0.0, 0.0);

         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_YAW, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_ROLL, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.HIP_PITCH, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.KNEE_PITCH, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.ANKLE_PITCH, EFFORT, 0.0, 0.0);
         configureSymmetricBehavior(behaviors, jointMap, LegJointName.ANKLE_ROLL, EFFORT, 0.0, 0.0);
      }

      return behaviors;
   }

   private List<GroupParameter<JointDesiredBehaviorReadOnly>> getDesiredJointBehaviorForDoNothing()
   {
      List<String> allJoints = Arrays.asList(jointMap.getOrderedJointNames());
      JointDesiredBehavior allJointBehaviors = new JointDesiredBehavior(EFFORT);

      List<GroupParameter<JointDesiredBehaviorReadOnly>> behaviors = new ArrayList<>();
      behaviors.add(new GroupParameter<>("", allJointBehaviors, allJoints));
      return behaviors;
   }

   @Override
   public HighLevelControllerName getDefaultInitialControllerState()
   {
      return runningOnRealRobot ? HighLevelControllerName.DO_NOTHING_BEHAVIOR : HighLevelControllerName.WALKING;
   }

   @Override
   public HighLevelControllerName getFallbackControllerState()
   {
      return HighLevelControllerName.DO_NOTHING_BEHAVIOR;
   }

   @Override
   public boolean automaticallyTransitionToWalkingWhenReady()
   {
      return false;
   }

   @Override
   public double getTimeToMoveInStandPrep()
   {
      return 0.5;
   }

   @Override
   public double getMinimumTimeInStandReady()
   {
      return 0;
   }

   @Override
   public double getTimeInStandTransition()
   {
      return 1.0;
   }

   @Override
   public double getCalibrationDuration()
   {
      return 0;
   }

   /** {@inheritDoc} */
   @Override
   public List<GroupParameter<JointAccelerationIntegrationParametersReadOnly>> getJointAccelerationIntegrationParameters(HighLevelControllerName state)
   {
      List<GroupParameter<JointAccelerationIntegrationParametersReadOnly>> integrationSettings = new ArrayList<>();

      { // Neck joints:
         JointAccelerationIntegrationParameters settings = new JointAccelerationIntegrationParameters();
         settings.setPositionBreakFrequency(AlphaFilteredYoVariable.computeBreakFrequencyGivenAlpha(0.9996, 0.004));
         settings.setVelocityBreakFrequency(AlphaFilteredYoVariable.computeBreakFrequencyGivenAlpha(0.95, 0.004));
         settings.setMaxPositionError(0.2);
         settings.setMaxVelocityError(2.0);

         List<String> neckJointNames = new ArrayList<>();
         for (NeckJointName name : jointMap.getNeckJointNames())
            neckJointNames.add(jointMap.getNeckJointName(name));
         integrationSettings.add(new GroupParameter<>("NeckAccelerationIntegration", settings, neckJointNames));
      }

      { // Shoulder joints:
         JointAccelerationIntegrationParameters settings = new JointAccelerationIntegrationParameters();
         settings.setPositionBreakFrequency(1.2);
         settings.setVelocityBreakFrequency(2.4);
         settings.setMaxPositionError(0.2);
         settings.setMaxVelocityError(2.0);

         List<String> shoulderJointNames = new ArrayList<>();
         for (ArmJointName name : new ArmJointName[] {ArmJointName.SHOULDER_YAW, ArmJointName.SHOULDER_ROLL})
         {
            for (RobotSide robotSide : RobotSide.values)
            {
               shoulderJointNames.add(jointMap.getArmJointName(robotSide, name));
            }
         }
         integrationSettings.add(new GroupParameter<>("ShoulderAccelerationIntegration", settings, shoulderJointNames));
      }

      { // Elbow joints:
         JointAccelerationIntegrationParameters settings = new JointAccelerationIntegrationParameters();
         settings.setPositionBreakFrequency(1.0);
         settings.setVelocityBreakFrequency(1.25);
         settings.setMaxPositionError(0.2);
         settings.setMaxVelocityError(2.0);

         ArmJointName[] elbowJoints = new ArmJointName[] {ArmJointName.ELBOW_PITCH, ArmJointName.ELBOW_ROLL};
         List<String> elbowJointNames = new ArrayList<>();
         for (ArmJointName name : elbowJoints)
         {
            for (RobotSide robotSide : RobotSide.values)
            {
               elbowJointNames.add(jointMap.getArmJointName(robotSide, name));
            }
         }
         integrationSettings.add(new GroupParameter<>("ElbowAccelerationIntegration", settings, elbowJointNames));
      }

      { // Wrist joints:
         JointAccelerationIntegrationParameters settings = new JointAccelerationIntegrationParameters();
         settings.setPositionBreakFrequency(AlphaFilteredYoVariable.computeBreakFrequencyGivenAlpha(0.9999, 0.004));
         settings.setVelocityBreakFrequency(AlphaFilteredYoVariable.computeBreakFrequencyGivenAlpha(0.95, 0.004));
         settings.setMaxPositionError(0.2);
         settings.setMaxVelocityError(2.0);

         ArmJointName[] wristJoints = new ArmJointName[] {ArmJointName.FIRST_WRIST_PITCH, ArmJointName.WRIST_ROLL, ArmJointName.SECOND_WRIST_PITCH};
         List<String> wristJointNames = new ArrayList<>();
         for (ArmJointName name : wristJoints)
         {
            for (RobotSide robotSide : RobotSide.values)
            {
               wristJointNames.add(jointMap.getArmJointName(robotSide, name));
            }
         }
         integrationSettings.add(new GroupParameter<>("WristAccelerationIntegration", settings, wristJointNames));
      }

      { // Spine joints:
         JointAccelerationIntegrationParameters settings = new JointAccelerationIntegrationParameters();
         settings.setVelocityBreakFrequency(AlphaFilteredYoVariable.computeBreakFrequencyGivenAlpha(0.985, 0.004));
         settings.setMaxVelocityError(2.0);
         integrationSettings.add(new GroupParameter<>("SpineAccelerationIntegration", settings, jointMap.getSpineJointNamesAsStrings()));
      }

      { // Leg joints (but knee):
         List<String> legJoints = new ArrayList<>();
         for (RobotSide robotSide : RobotSide.values)
         {
            legJoints.add(jointMap.getLegJointName(robotSide, LegJointName.HIP_YAW));
            legJoints.add(jointMap.getLegJointName(robotSide, LegJointName.HIP_ROLL));
            legJoints.add(jointMap.getLegJointName(robotSide, LegJointName.HIP_PITCH));
            legJoints.add(jointMap.getLegJointName(robotSide, LegJointName.ANKLE_PITCH));
            legJoints.add(jointMap.getLegJointName(robotSide, LegJointName.ANKLE_ROLL));
         }
         JointAccelerationIntegrationParameters settings = new JointAccelerationIntegrationParameters();
         settings.setVelocityBreakFrequency(AlphaFilteredYoVariable.computeBreakFrequencyGivenAlpha(0.985, 0.004));
         settings.setMaxVelocityError(5.0);
         integrationSettings.add(new GroupParameter<>("LegAccelerationIntegration", settings, legJoints));
      }

      { // Knee joints:
         List<String> kneeJoints = new ArrayList<>();
         for (RobotSide robotSide : RobotSide.values)
            kneeJoints.add(jointMap.getLegJointName(robotSide, LegJointName.KNEE_PITCH));
         JointAccelerationIntegrationParameters settings = new JointAccelerationIntegrationParameters();
         settings.setVelocityBreakFrequency(AlphaFilteredYoVariable.computeBreakFrequencyGivenAlpha(0.985, 0.004));
         settings.setMaxVelocityError(5.0);
         settings.setVelocityResetMode(JointVelocityIntegratorResetMode.ZERO_VELOCITY);
         integrationSettings.add(new GroupParameter<>("KneeAccelerationIntegration", settings, kneeJoints));
      }

      return integrationSettings;
   }

   public static void configureBehavior(List<GroupParameter<JointDesiredBehaviorReadOnly>> behaviors,
                                        HumanoidJointNameMap jointMap,
                                        SpineJointName jointName,
                                        JointDesiredControlMode controlMode,
                                        double stiffness,
                                        double damping)
   {
      JointDesiredBehavior jointBehavior = new JointDesiredBehavior(controlMode, stiffness, damping);
      List<String> names = Collections.singletonList(jointMap.getSpineJointName(jointName));
      behaviors.add(new GroupParameter<>(jointName.toString(), jointBehavior, names));
   }

   public static void configureBehavior(List<GroupParameter<JointDesiredBehaviorReadOnly>> behaviors,
                                        HumanoidJointNameMap jointMap,
                                        NeckJointName jointName,
                                        JointDesiredControlMode controlMode,
                                        double stiffness,
                                        double damping)
   {
      JointDesiredBehavior jointBehavior = new JointDesiredBehavior(controlMode, stiffness, damping);
      List<String> names = Collections.singletonList(jointMap.getNeckJointName(jointName));
      behaviors.add(new GroupParameter<>(jointName.toString(), jointBehavior, names));
   }

   public static void configureSymmetricBehavior(List<GroupParameter<JointDesiredBehaviorReadOnly>> behaviors,
                                                 HumanoidJointNameMap jointMap,
                                                 LegJointName jointName,
                                                 JointDesiredControlMode controlMode,
                                                 double stiffness,
                                                 double damping)
   {
      configureSymmetricBehavior(behaviors, jointMap, jointName, controlMode, stiffness, damping, Double.POSITIVE_INFINITY);
   }

   public static void configureSymmetricBehavior(List<GroupParameter<JointDesiredBehaviorReadOnly>> behaviors,
                                                 HumanoidJointNameMap jointMap,
                                                 LegJointName jointName,
                                                 JointDesiredControlMode controlMode,
                                                 double stiffness,
                                                 double damping,
                                                 double maxVelocityError)
   {
      JointDesiredBehavior jointBehavior = new JointDesiredBehavior(controlMode, stiffness, damping);
      jointBehavior.setMaxVelocityError(maxVelocityError);
      behaviors.add(new GroupParameter<>(jointName.toString(), jointBehavior, getLeftAndRightJointNames(jointMap, jointName)));
   }

   public static void configureSymmetricBehavior(List<GroupParameter<JointDesiredBehaviorReadOnly>> behaviors,
                                                 HumanoidJointNameMap jointMap,
                                                 ArmJointName jointName,
                                                 JointDesiredControlMode controlMode,
                                                 double stiffness,
                                                 double damping)
   {
      JointDesiredBehavior jointBehavior = new JointDesiredBehavior(controlMode, stiffness, damping);
      behaviors.add(new GroupParameter<>(jointName.toString(), jointBehavior, getLeftAndRightJointNames(jointMap, jointName)));
   }

   public static List<String> getLeftAndRightJointNames(HumanoidJointNameMap jointMap, LegJointName legJointName)
   {
      List<String> jointNames = new ArrayList<>();
      for (RobotSide side : RobotSide.values)
      {
         jointNames.add(jointMap.getLegJointName(side, legJointName));
      }
      return jointNames;
   }

   public static List<String> getLeftAndRightJointNames(HumanoidJointNameMap jointMap, ArmJointName armJointName)
   {
      List<String> jointNames = new ArrayList<>();
      for (RobotSide side : RobotSide.values)
      {
         jointNames.add(jointMap.getArmJointName(side, armJointName));
      }
      return jointNames;
   }
}
