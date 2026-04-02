let config = require("semantic-release-preconfigured-conventional-commits");
config.branches = ["release"]
config.plugins.push(
  [
    "@terrestris/maven-semantic-release",
    {
      "mavenTarget": "deploy",
      "settingsPath": "./settings.xml",
      "updateSnapshotVersion": true,
      "mvnw": true
    }
  ],
  "@semantic-release/github",
  "@semantic-release/git"
)
module.exports = config