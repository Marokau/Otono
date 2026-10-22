package server.bots;
import client.Character;
import org.junit.jupiter.api.Test;
import server.life.Monster;
import server.maps.*;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotZakumArmLanesTest {
    @Test void noReachableStanceStopsOldRouteAndWaitsWithNormalGroundPhysics() {
        var bot=botAt(new Point(499,-530));when(bot.getJob()).thenReturn(client.Job.HERO);
        var arm=arm(8800004);when(bot.getMap().getAllMonsters()).thenReturn(List.of(arm));
        var entry=new BotEntry(bot,null,null);entry.moveDir=1;entry.downJumpPending=true;entry.ropeEntryPending=true;
        try(var weapons=mockStatic(BotAttackExecutionProvider.class);var combat=mockStatic(BotCombatManager.class);
                var graph=mockStatic(BotNavigationGraphProvider.class);var nav=mockStatic(BotNavigationManager.class);
                var movement=mockStatic(BotMovementManager.class);var travel=mockStatic(BotTravelManager.class);
                var fidget=mockStatic(BotFidgetManager.class)) {
            weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(bot)).thenReturn(client.inventory.WeaponType.SWORD1H);
            BotZakumCombat.tick(entry,bot,true);
            nav.verify(()->BotNavigationManager.clearCommittedRoute(entry));
            movement.verify(()->BotMovementManager.tickGrounded(entry,null));
            fidget.verify(()->BotFidgetManager.clear(entry));
            assertEquals(0,entry.moveDir);assertFalse(entry.downJumpPending);assertFalse(entry.ropeEntryPending);
        }
    }

    @Test void warriorWithoutCachedStanceDoesNotHoldItsOwnRopePositionForever() {
        var bot=botAt(new Point(-447,-260));when(bot.getJob()).thenReturn(client.Job.DARKKNIGHT);
        var human=mock(Character.class);var sameMap=bot.getMap();when(human.getMap()).thenReturn(sameMap);
        when(human.getPosition()).thenReturn(new Point(-496,-422));
        var entry=new BotEntry(bot,human,null);entry.climbing=true;
        try(var weapons=mockStatic(BotAttackExecutionProvider.class);var navigation=mockStatic(BotNavigationGraphProvider.class);
                var walking=mockStatic(BotZakumPqRun.class);var manager=mockStatic(BotManager.class)) {
            weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(bot)).thenReturn(client.inventory.WeaponType.SPEAR_STAB);
            BotZakumCombat.tick(entry,bot,true);BotZakumCombat.tick(entry,bot,false);
            walking.verify(()->BotZakumPqRun.walkTo(entry,bot,new Point(-496,-422),true));
            walking.verify(()->BotZakumPqRun.walkTo(entry,bot,new Point(-496,-422),false));
            walking.verify(()->BotZakumPqRun.walkTo(entry,bot,new Point(-447,-260),true),never());
        }
    }

    @Test void meleeCanHelpOnAReachableArmWhileRangedKeepsTheOrderedLane() {
        var bot=botAt(new Point(457,-391));var earlier=arm(8800007);var reachable=arm(8800008);
        when(bot.getMap().getAllMonsters()).thenReturn(List.of(earlier,reachable));
        assertFalse(BotZakumArmLanes.canTarget(bot,reachable));
        when(bot.getJob()).thenReturn(client.Job.HERO);assertTrue(BotZakumArmLanes.canTarget(bot,reachable));
        when(bot.getJob()).thenReturn(client.Job.DARKKNIGHT);assertTrue(BotZakumArmLanes.canTarget(bot,reachable));
    }
    @Test void warriorActuallyAttacksLaterReachableArmInsteadOfWaitingForUnreachableFocus() {
        var bot=botAt(new Point(457,-391));when(bot.getJob()).thenReturn(client.Job.HERO);
        var earlier=arm(8800007);var reachable=arm(8800008);
        when(bot.getMap().getAllMonsters()).thenReturn(List.of(earlier,reachable));
        var entry=new BotEntry(bot,null,null);
        var plan=new BotCombatManager.AttackPlan(1121008,30,2,new Rectangle(287,-518,340,255),List.of(reachable),
                BotCombatManager.AttackRoute.CLOSE,0,12,12,128,4,0,600,null);
        try(var weapons=mockStatic(BotAttackExecutionProvider.class);var combat=mockStatic(BotCombatManager.class);
                var hold=mockStatic(BotOpqRun.class);var travel=mockStatic(BotTravelManager.class);var walking=mockStatic(BotZakumPqRun.class)) {
            weapons.when(()->BotAttackExecutionProvider.getEquippedWeaponType(bot)).thenReturn(client.inventory.WeaponType.SWORD1H);
            combat.when(()->BotCombatManager.planAttack(entry,bot,reachable)).thenReturn(plan);
            combat.when(()->BotCombatManager.isTargetInAttackRange(plan,bot,reachable)).thenReturn(true);
            combat.when(()->BotCombatManager.canUseAttackPlanNow(entry,client.inventory.WeaponType.SWORD1H,plan)).thenReturn(true);
            combat.when(()->BotCombatManager.betterReachRepositionTarget(entry,bot,reachable,plan)).thenReturn(new Point(300,-391));
            BotZakumCombat.tick(entry,bot,true);
            combat.verify(()->BotCombatManager.attackMonster(entry,bot,plan));
            walking.verifyNoInteractions();
        }
    }

    @Test void crewUsesConfirmedArmOrderAndWaitsUntilEarlierArmDies() {
        var bot=botAt(new Point(499,-530));
        int[] order={5,7,4,6,8,3,1,2};
        for(int i=0;i<order.length;i++)assertEquals(i,BotZakumCombat.priority(bot,8800002+order[i]));
        var first=arm(8800007);var later=arm(8800004);
        when(bot.getMap().getAllMonsters()).thenReturn(List.of(later,first));
        assertFalse(BotZakumArmLanes.canTarget(bot,later),"Cannot attack top-right early merely because already standing there");
        when(first.isAlive()).thenReturn(false);
        assertTrue(BotZakumArmLanes.canTarget(bot,later),"Advance to the earliest arm that is still alive");
    }

    @Test void wideSummonSplashCannotHitOppositeOrLowerArms() {
        var bot=botAt(new Point(499,-391));var right=arm(8800006);var left=arm(8800003);var lower=arm(8800010);
        when(bot.getMap().getAllMonsters()).thenReturn(List.of(left,lower,right));
        var summon=mock(Summon.class);when(summon.getPosition()).thenReturn(new Point(499,-391));
        when(summon.getSkill()).thenReturn(constants.skills.Bowmaster.PHOENIX);
        var boxes=mock(server.bots.combat.BotMobHitboxProvider.class);
        when(boxes.getMobBounds(left)).thenReturn(new Rectangle(-153,-616,96,134));
        when(boxes.getMobBounds(right)).thenReturn(new Rectangle(86,-625,91,153));
        when(boxes.getMobBounds(lower)).thenReturn(new Rectangle(169,-500,139,128));
        try(var provider=mockStatic(server.bots.combat.BotMobHitboxProvider.class)) {
            provider.when(server.bots.combat.BotMobHitboxProvider::getInstance).thenReturn(boxes);
            assertEquals(List.of(right),BotSummonCombat.targets(bot,summon,
                    new BotSummonCombat.Spec(new Rectangle(-1000,-1000,2000,2000),2000,new Point(),3,0),mock(server.StatEffect.class)));
        }
    }

    private Character botAt(Point p) {
        var bot=mock(Character.class);when(bot.getPosition()).thenReturn(p);
        when(bot.getMapId()).thenReturn(BotZakumBattle.ALTAR);
        when(bot.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));
        when(bot.getJob()).thenReturn(client.Job.NIGHTLORD);
        var map=mock(MapleMap.class);when(bot.getMap()).thenReturn(map);
        var footholds=mock(FootholdTree.class);when(map.getFootholds()).thenReturn(footholds);
        when(footholds.findBelow(any())).thenReturn(new Foothold(new Point(p.x-10,p.y),new Point(p.x+10,p.y),1));
        BotPqDialogue.register(bot);return bot;
    }
    private Monster arm(int id) {
        var m=mock(Monster.class);when(m.getId()).thenReturn(id);when(m.getObjectId()).thenReturn(id);
        when(m.isAlive()).thenReturn(true);when(m.getPosition()).thenReturn(new Point(-9,-216));return m;
    }
    @Test void eachOfEightPositionsPermitsExactlyItsOneArm() {
        var points=List.of(new Point(-400,-556),new Point(499,-530),new Point(-350,-488),new Point(480,-391),
                new Point(-400,-422),new Point(440,-327),new Point(-400,-213),new Point(480,-214));
        for(int index=0;index<points.size();index++) {
            var bot=botAt(points.get(index));
            for(int id=8800003;id<=8800010;id++)assertEquals(id==8800003+index,
                    BotZakumArmLanes.canTarget(bot,arm(id)),points.get(index)+" -> "+id);
        }
        assertEquals(0,BotZakumArmLanes.armForSurface(new Point(470,-266)),"Extra low ledge is not a combat tier");
    }
    @Test void secondaryAndCachedTargetsCannotCrossSidesOrTiers() {
        var bot=botAt(new Point(499,-530));var rightTop=arm(8800004);var leftTop=arm(8800003);var rightLower=arm(8800008);
        var plan=new BotCombatManager.AttackPlan(3121004,30,1,new Rectangle(-200,-650,800,500),
                List.of(leftTop,rightTop,rightLower),BotCombatManager.AttackRoute.RANGED,0,12,12,128,4,0,120,null);
        var filtered=BotZakumArmLanes.filter(bot,plan);
        assertNotNull(filtered);assertEquals(List.of(rightTop),filtered.targets);
        when(bot.getPosition()).thenReturn(new Point(499,-391));
        assertNull(BotZakumArmLanes.filter(bot,filtered),"Cached target is rejected after platform change");
    }
    @Test void projectileRetargetingDoesNotReplaceRightArmWithLeftArmSharingOrigin() {
        var bot=botAt(new Point(499,-530));var right=arm(8800004);var left=arm(8800003);
        when(bot.getMap().getAllMonsters()).thenReturn(List.of(left,right));
        var bounds=mock(server.bots.combat.BotMobHitboxProvider.class);
        when(bounds.getMobBounds(left)).thenReturn(new Rectangle(-153,-616,96,134));
        when(bounds.getMobBounds(right)).thenReturn(new Rectangle(86,-625,91,153));
        try(var provider=mockStatic(server.bots.combat.BotMobHitboxProvider.class)) {
            provider.when(server.bots.combat.BotMobHitboxProvider::getInstance).thenReturn(bounds);
            assertSame(right,BotCombatManager.resolveEffectivePrimary(bot,right,new Rectangle(-101,-580,595,100)));
        }
    }
    @Test void noArmAttackWhileAirborneAndUnregisteredPopulationIsUnchanged() {
        var bot=botAt(new Point(499,-530));var top=arm(8800004);
        when(bot.getPosition()).thenReturn(new Point(499,-550));assertFalse(BotZakumArmLanes.canTarget(bot,top));
        var population=mock(Character.class);when(population.getMapId()).thenReturn(BotZakumBattle.ALTAR);
        when(population.getEventInstance()).thenReturn(mock(scripting.event.EventInstanceManager.class));
        assertTrue(BotZakumArmLanes.canTarget(population,top));
        assertTrue(BotZakumArmLanes.canTarget(bot,arm(8800002)),"Body phases are not arm lanes");
    }
}
