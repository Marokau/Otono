param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $ProbeArgs
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot

$javaArgs = [System.Collections.Generic.List[string]]::new()
$noBuild = $false
foreach ($arg in $ProbeArgs) {
    if ($arg -eq "--no-build") {
        $noBuild = $true
        continue
    }
    $javaArgs.Add($arg)
}

Push-Location $repoRoot
try {
    if (-not $noBuild) {
        & .\mvnw.cmd -q -DskipTests compile
    }

    & .\mvnw.cmd -q dependency:build-classpath "-Dmdep.outputFile=target/botnav.cp" "-DincludeScope=runtime"
    $classpath = (Get-Content "target/botnav.cp" -Raw).Trim()

    & java -cp "target/classes;$classpath" server.bots.BotNavigationProbe @javaArgs
}
finally {
    Pop-Location
}
