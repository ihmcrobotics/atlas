package us.ihmc.atlas.obstacleCourseTests;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.Test;
import us.ihmc.atlas.AtlasRobotModel;
import us.ihmc.atlas.AtlasRobotVersion;
import us.ihmc.atlas.parameters.AtlasWalkingControllerParameters;
import us.ihmc.avatar.drcRobot.DRCRobotModel;
import us.ihmc.avatar.obstacleCourseTests.AvatarBigStepDownTest;
import us.ihmc.commonWalkingControlModules.configurations.WalkingControllerParameters;
import us.ihmc.simulationConstructionSetTools.tools.CITools;

/**
 * Tests in this class have been replaced with Nadia tests so they are all disabled
 */
@Tag("humanoid-obstacle")
public class AtlasBigStepDownTest extends AvatarBigStepDownTest
{

   @Override
   public DRCRobotModel getRobotModel()
   {
      DRCRobotModel robotModel = new AtlasRobotModel(AtlasRobotVersion.ATLAS_UNPLUGGED_V5_NO_HANDS)
      {
         @Override
         public WalkingControllerParameters getWalkingControllerParameters()
         {
            return new AtlasWalkingControllerParameters(getTarget(), getJointMap(), getContactPointParameters())
            {
               @Override
               public double nominalHeightAboveAnkle()
               {
                  return 0.75;
               }
            };
         }
      };

      return robotModel;
   }

   @Override
   public String getSimpleRobotName()
   {
      return CITools.getSimpleRobotNameFor(CITools.SimpleRobotNameKeys.ATLAS);
   }

   @Disabled
   @Test
   public void testSplitFractionInBigStepDown()
   {
      super.testSplitFractionInBigStepDown();
   }

   @Disabled
   @Test
   public void testWalkingOffOfLargePlatform()
   {
      super.testWalkingOffOfLargePlatform();
   }
}
