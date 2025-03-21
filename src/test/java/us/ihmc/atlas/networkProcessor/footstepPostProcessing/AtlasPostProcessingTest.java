package us.ihmc.atlas.networkProcessor.footstepPostProcessing;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.atlas.parameters.AtlasWalkingControllerParameters;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.drcRobot.RobotTarget;
import us.ihmc.avatar.networkProcessor.footstepPostProcessing.AvatarPostProcessingTests;
import us.ihmc.commonWalkingControlModules.configurations.WalkingControllerParameters;
import us.ihmc.footstepPlanning.swing.DefaultSwingPlannerParameters;
import us.ihmc.footstepPlanning.swing.SwingPlannerParametersBasics;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.robotics.robotSide.SideDependentList;
import us.ihmc.simulationConstructionSetTools.tools.CITools;

public class AtlasPostProcessingTest extends AvatarPostProcessingTests
{

   @Override
   public DRCRobotModel getRobotModel()
   {
      return new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS, RobotTarget.SCS, false)
      {
         public WalkingControllerParameters getWalkingControllerParameters()
         {
            return new AtlasWalkingControllerParameters(RobotTarget.SCS, getJointMap(), getContactPointParameters())
            {
               public boolean createFootholdExplorationTools()
               {
                  return false;
               }
            };
         }

         @Override
         public SwingPlannerParametersBasics getSwingPlannerParameters()
         {
            SwingPlannerParametersBasics parametersBasics = new DefaultSwingPlannerParameters();
            parametersBasics.setDoInitialFastApproximation(true);
            parametersBasics.setMinimumSwingFootClearance(0.0);
            parametersBasics.setNumberOfChecksPerSwing(100);
            parametersBasics.setMaximumNumberOfAdjustmentAttempts(50);
            parametersBasics.setMaximumWaypointAdjustmentDistance(0.2);
            parametersBasics.setMinimumAdjustmentIncrementDistance(0.03);
            parametersBasics.setMinimumHeightAboveFloorForCollision(0.03);

            return parametersBasics;
         }
      };
   }


   public static double[] rightHandStraightSideJointAngles = new double[] {-0.5067668142160446, -0.3659876546358431, 1.7973796317575155,
                                                                           -1.2398714600960365, -0.005510224629709242, 0.6123343067479899, 0.12524505635696856};
   public static double[] leftHandStraightSideJointAngles = new double[] {0.61130147334225, 0.22680071472282162, 1.6270339908033258, 1.2703560974484844,
                                                                          0.10340544060719102, -0.6738299572358809, 0.13264785356924128};
   public static SideDependentList<double[]> straightArmConfigs = new SideDependentList<>();
   static
   {
      straightArmConfigs.put(RobotSide.LEFT, leftHandStraightSideJointAngles);
      straightArmConfigs.put(RobotSide.RIGHT, rightHandStraightSideJointAngles);
   }

   @Override
   public double[] getStraightArmConfig(RobotSide robotSide)
   {
      return straightArmConfigs.get(robotSide);
   }

   @Override
   public String getLeftAnkleXName()
   {
      return "l_leg_akx";
   }

   @Override
   public String getLeftAnkleYName()
   {
      return "l_leg_aky";
   }

   @Override
   public String getRightAnkleXName()
   {
      return "r_leg_akx";
   }

   @Override
   public String getRightAnkleYName()
   {
      return "r_leg_aky";
   }


   @Tag("humanoid-obstacle-2")
   @Override
   @Test
   public void testWalkingOffOfMediumPlatform()
   {
      super.testWalkingOffOfMediumPlatform();
   }

   @Disabled
   @Tag("humanoid-obstacle-2")
   @Override
   @Test
   public void testSwingOverPlanarRegions()
   {
      super.testSwingOverPlanarRegions();
   }

   @Tag("humanoid-obstacle-2")
   @Override
   @Test
   public void testWalkingOnStraightForwardLines()
   {
      super.testWalkingOnStraightForwardLines();
   }

   @Override
   public String getSimpleRobotName()
   {
      return CITools.getSimpleRobotNameFor(CITools.SimpleRobotNameKeys.ATLAS);
   }
}
