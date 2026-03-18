#!/usr/bin/env bash
set -euo pipefail

APP_VERSION="${1:-0.2.0}"

mvn -pl desktop-app -am clean package

jpackage       --type dmg       --name "Chuj NLP Studio"       --app-version "${APP_VERSION}"       --input "./desktop-app/target"       --main-jar "chuj-nlp-desktop-${APP_VERSION}.jar"       --main-class "org.titiplex.desktop.bootstrap.DesktopMain"       --dest "./desktop-app/target/dist"
