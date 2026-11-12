package server.bots;

import client.Job;
import org.junit.jupiter.api.Test;
import javax.script.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WhereLevelClassGuideTest {
    ScriptEngine open(WhereLevelPqMenuTest.Npc npc) throws Exception {
        when(npc.player.getLevel()).thenReturn(140);
        when(npc.player.getJob()).thenReturn(Job.BEGINNER);
        var engine = new ScriptEngineManager().getEngineByName("graal.js");
        var bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", true);
        bindings.put("cm", npc);
        engine.eval(Files.readString(Path.of("scripts/npc/wherelevel.js")));
        return engine;
    }

    @Test void eachGuideHasWorkingSkillsGearStagesAndBackLinks() throws Exception {
        var npc = new WhereLevelPqMenuTest.Npc(); var e = open(npc);
        String[] names = {"Night Lord", "Permanent Beginner", "Dark Knight", "Bowmaster", "Ice/Lightning Mage", "Hero", "Bishop", "Corsair"};
        for (int i = 0; i < names.length; i++) {
            e.eval("home(); action(1,4,2);");
            for (String name : names) assertTrue(npc.text.contains(name));
            assertTrue(npc.text.indexOf("Permanent Beginner") < npc.text.indexOf("Night Lord"));
            e.eval("action(1,4," + i + ");"); assertTrue(npc.text.contains("Class: " + names[i]));
            e.eval("action(1,4,0);"); assertTrue(npc.text.contains("Points for this level"));
            assertTrue(npc.text.contains("Back to Class: " + names[i]));
            e.eval("action(1,4,3);"); assertTrue(npc.text.contains("Choose a job stage"));
            e.eval("action(1,4,0); action(1,4,901); action(1,4,1);");
            assertTrue(npc.text.contains(names[i] + " gear"));
            e.eval("action(1,4,2); action(1,4,0);");
            assertFalse(npc.text.contains("More slots"));
            assertFalse(npc.text.contains("Previous slots"));
            assertFalse(npc.text.contains(" | Page "));
            // Every range shows its last slot together with the first, before navigation.
            int ranges = ((Number)e.eval("gearBuilds.length")).intValue();
            for (int range=0;range<ranges;range++) {
                e.eval("gearBand="+range+"; gearMenu();");
                int rows = ((Number)e.eval("gearBuilds[gearBand].rows.length")).intValue();
                for (int row=0;row<rows;row++) {
                    String label = e.eval("gearBuilds[gearBand].rows["+row+"][0]").toString();
                    assertTrue(npc.text.contains("#e"+label+":#n"));
                }
            }
            e.eval("action(1,4,901); action(1,4,700);");
            assertTrue(npc.text.contains("Class Guides"));
        }
        when(npc.player.getJob()).thenReturn(Job.NIGHTLORD);
        e.eval("selectClassGuide(0); stage=3; buildLevel=140; buildMenu();");
        assertTrue(npc.text.contains("Night Lord - level 140"));
        assertTrue(npc.text.contains("Triple Throw"));
        assertFalse(npc.text.contains("Spear Crusher"));
    }

    @Test void currentPointsAreComparedOnlyWithTheSelectedClass() throws Exception {
        var npc = new WhereLevelPqMenuTest.Npc(); var e = open(npc);
        when(npc.player.getJob()).thenReturn(Job.DARKKNIGHT);
        e.eval("selectClassGuide(2); action(1,4,0);");
        assertTrue(npc.text.contains("Dark Knight - level 140"));
        assertTrue(npc.text.contains("your current points"));
        assertTrue(npc.text.contains("Berserk"));
        e.eval("selectClassGuide(3); action(1,4,0);");
        assertFalse(npc.text.contains("your current points"));
        assertTrue(npc.text.contains("not your current class"));
        when(npc.player.getJob()).thenReturn(Job.BOWMASTER);
        e.eval("action(1,4,901); action(1,4,0);");
        assertTrue(npc.text.contains("Bowmaster - level 140"));
        assertTrue(npc.text.contains("your current points"));
    }

    @Test void newClassPathsOpenAtTheirOwnJobStageAndLevel() throws Exception {
        var npc = new WhereLevelPqMenuTest.Npc(); var e = open(npc);
        int[] guides = {4,4,5,6,6,6,7,7};
        Job[] jobs = {Job.IL_MAGE,Job.IL_ARCHMAGE,Job.HERO,Job.CLERIC,Job.PRIEST,Job.BISHOP,Job.OUTLAW,Job.CORSAIR};
        int[] levels = {85,140,140,45,85,140,85,140};
        String[] stages = {"Mage (I/L)","Arch Mage (I/L)","Hero","Cleric","Priest","Bishop","Outlaw","Corsair"};
        for (int i=0;i<guides.length;i++) {
            when(npc.player.getJob()).thenReturn(jobs[i]);
            when(npc.player.getLevel()).thenReturn(levels[i]);
            e.eval("selectClassGuide("+guides[i]+"); action(1,4,0);");
            assertTrue(npc.text.contains(stages[i]+" - level "+levels[i]));
            assertTrue(npc.text.contains("your current points"));
            assertTrue(npc.text.contains("Suggested SP route"));
        }
    }

    @Test void newBuildsRespectEveryLocalSkillCapPrerequisiteAndSpBudget() throws Exception {
        var e = open(new WhereLevelPqMenuTest.Npc());
        var js = new StringBuilder("var limits={};");
        for (String job : new String[]{"000","100","130","131","132","300","310","311","312","200","220","221","222","110","111","112","230","231","232","500","520","521","522"}) {
            var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(Path.of("wz/Skill.wz/" + job + ".img.xml").toFile());
            var nodes = doc.getElementsByTagName("imgdir");
            for (int i=0; i<nodes.getLength(); i++) {
                var n = (org.w3c.dom.Element)nodes.item(i);
                if (!(n.getParentNode() instanceof org.w3c.dom.Element parent)
                        || !parent.getAttribute("name").equals("skill")) continue;
                int max = 0; var req = new StringBuilder();
                for (var child = n.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (!(child instanceof org.w3c.dom.Element c)) continue;
                    if (c.getAttribute("name").equals("level"))
                        max = c.getElementsByTagName("imgdir").getLength();
                    if (c.getAttribute("name").equals("req")) {
                        var requirements = c.getElementsByTagName("int");
                        for (int k=0; k<requirements.getLength(); k++) {
                            var r = (org.w3c.dom.Element)requirements.item(k);
                            req.append("'").append(Integer.parseInt(r.getAttribute("name")))
                                    .append("':").append(r.getAttribute("value")).append(",");
                        }
                    }
                }
                js.append("limits['").append(Integer.parseInt(n.getAttribute("name")))
                        .append("']={max:").append(max).append(",req:{").append(req).append("}};");
            }
        }
        e.eval(js.toString());
        e.eval("""
            for (var g=1;g<guideDefinitions.length;g++) {
                selectClassGuide(g);
                for (var s=0;s<builds.length;s++) {
                    for (var level=builds[s].start;level<=builds[s].end;level++) {
                        var totals=totalsFor(s,level), byId={}, spent=0;
                        for (var key in totals) {
                            var id=skills[key][0], cap=limits[id];
                            if (!cap || totals[key]<0 || totals[key]>cap.max) throw Error('Invalid cap '+g+' '+id);
                            byId[id]=totals[key]; spent+=totals[key];
                        }
                        var expected=g===1?Math.min(6,level-1):0;
                        if(g!==1) for(var b=0;b<=s;b++) expected+=builds[b].initial+3*((b<s?builds[b].end:level)-builds[b].start);
                        if(spent!==expected) throw Error('SP budget '+g+' '+s+' '+level+' '+spent+' != '+expected);
                        for(var id in byId) if(byId[id]>0) for(var need in limits[id].req)
                            if((byId[need]||0)<limits[id].req[need]) throw Error('Missing prerequisite '+id+' '+need);
                    }
                }
            }
            selectClassGuide(1);
            if(totalsFor(0,1).feet) throw Error('SP at level one');
            if(totalsFor(0,200).feet!==3 || totalsFor(0,200).snails!==3) throw Error('Beginner six-point cap');
            """);
    }
}
