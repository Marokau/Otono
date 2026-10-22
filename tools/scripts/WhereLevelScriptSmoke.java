import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.nio.file.Files;
import java.nio.file.Path;

// Run from the repository root with the same packaged runtime used by launch.bat:
// java --class-path target/Cosmic.jar tools/scripts/WhereLevelScriptSmoke.java
class WhereLevelScriptSmoke {
    public static void main(String[] args) throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal.js");
        if (engine == null) throw new IllegalStateException("Packaged GraalJS engine is unavailable");
        try (var reader = Files.newBufferedReader(Path.of("scripts/npc/wherelevel.js"))) {
            engine.eval(reader);
        }
        engine.eval("""
            var output = '';
            var cm = {
                getPlayer: function() { return {
                    getLevel: function() { return 120; },
                    getJob: function() { return {name: function() { return 'NIGHTLORD'; }}; },
                    isGM: function() { return true; }
                }; },
                sendSimple: function(text) { output = text; }
            };
            start();
            if (output.indexOf('Browse all chapters') < 0) throw new Error('Home did not open');
            action(1, 0, 5);
            action(1, 0, 6);
            if (output.indexOf('#k#eMenu navigation#n') < 0) throw new Error('Chapter did not open');
            action(1, 0, 0);
            action(1, 0, 0);
            if (output.indexOf('Skelegon') < 0) throw new Error('Leafre monsters did not open');
            """);
        System.out.println("PASS: guide loads and opens home, chapter, and monster menus in packaged GraalJS.");
    }
}
