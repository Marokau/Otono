package server.bots;

import client.Character;
import org.junit.jupiter.api.Test;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;
import server.maps.Portal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotLpqBoxHintTest {
    @Test void fixedOrderMatchesVisibleBoxLabelsRatherThanPortalPositions() throws Exception {
        var path=java.nio.file.Path.of("wz/Map.wz/Map/Map9/922010600.img.xml");
        org.junit.jupiter.api.Assumptions.assumeTrue(java.nio.file.Files.isRegularFile(path));
        var doc=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile());
        var xp=javax.xml.xpath.XPathFactory.newInstance().newXPath();
        var nodes=(org.w3c.dom.NodeList)xp.evaluate("/imgdir/imgdir[@name='portal']/imgdir",doc,javax.xml.xpath.XPathConstants.NODESET);
        var answer=new StringBuilder();
        var labels=(org.w3c.dom.NodeList)xp.evaluate("/imgdir/imgdir/imgdir[@name='obj']/imgdir[string[@name='l0']/@value='toyTowerInsideQuest' and string[@name='l1']/@value='box' and string[@name='l2']/@value!='0']",doc,javax.xml.xpath.XPathConstants.NODESET);
        for(int row=0;row<15;row++) {
            String next=row==14 ? "next00" : String.format("h%03d",(row+1)*3);
            org.w3c.dom.Node correct=null;
            for(int i=0;i<nodes.getLength();i++) {
                var node=nodes.item(i);
                String name=xp.evaluate("string[@name='pn']/@value",node);
                if(name.matches("h[0-9]{3}") && Integer.parseInt(name.substring(1))/3==row
                        && next.equals(xp.evaluate("string[@name='tn']/@value",node))) correct=node;
            }
            assertNotNull(correct);
            int px=Integer.parseInt(xp.evaluate("int[@name='x']/@value",correct));
            int py=Integer.parseInt(xp.evaluate("int[@name='y']/@value",correct));
            double nearest=Double.MAX_VALUE;int label=0;
            for(int i=0;i<labels.getLength();i++) {
                var node=labels.item(i);
                int x=Integer.parseInt(xp.evaluate("int[@name='x']/@value",node));
                int y=Integer.parseInt(xp.evaluate("int[@name='y']/@value",node));
                double distance=Math.hypot(x-px,y-py);
                if(distance<nearest) {
                    nearest=distance;label=Integer.parseInt(xp.evaluate("string[@name='l2']/@value",node));
                }
            }
            assertTrue(nearest<40);
            answer.append(label>=10 ? label-9 : label);
            if(row%3==2 && row<14) answer.append(' ');
        }
        assertEquals("133 221 333 123 111",answer.toString());
        assertEquals(answer.toString(),BotLpqBoxHint.order(mock(MapleMap.class)));
    }
    @Test void leaderAtBottomCanAskCollectorAlreadyWaitingInNextStage() {
        var bot=mock(Character.class);var human=mock(Character.class);var event=mock(EventInstanceManager.class);
        var map=mock(MapleMap.class);var entry=new BotEntry(bot,human,null);
        when(human.getMapId()).thenReturn(922010600);when(bot.getMapId()).thenReturn(922010700);
        when(bot.getEventInstance()).thenReturn(event);when(human.getEventInstance()).thenReturn(event);
        when(human.getMap()).thenReturn(map);when(bot.getName()).thenReturn("LPqSpear1");
        try(var crew=mockStatic(BotLpqHuman.class)) {
            crew.when(()->BotLpqHuman.leader(bot)).thenReturn(human);
            crew.when(()->BotLpqHuman.isCollector(bot)).thenReturn(true);
            assertFalse(BotLpqBoxHint.handle(entry,"hello"));verifyNoInteractions(map);
            assertTrue(BotLpqBoxHint.handle(entry,"box order?"));
            verify(human).dropMessage(eq(6),contains("LPqSpear1:"));
            clearInvocations(human);
            crew.when(()->BotLpqHuman.isCollector(bot)).thenReturn(false);
            assertTrue(BotLpqBoxHint.handle(entry,"box order"));
            verify(human,never()).dropMessage(anyInt(),anyString());
            when(human.getMapId()).thenReturn(922010700);
            assertFalse(BotLpqBoxHint.handle(entry,"box order"));
        }
    }
}
