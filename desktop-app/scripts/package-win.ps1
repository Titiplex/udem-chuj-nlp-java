param(
    [string]$AppVersion = "0.2.0"
)

mvn -pl desktop-app -am clean package

jpackage `
  --type exe `
  --name "Chuj NLP Studio" `
  --app-version $AppVersion `
  --input ".\desktop-app\target" `
  --main-jar "chuj-nlp-desktop-$AppVersion.jar" `
  --main-class "org.titiplex.desktop.bootstrap.DesktopMain" `
  --dest ".\desktop-app\target\dist" `
  --win-dir-chooser `
  --win-menu `
  --win-shortcut
