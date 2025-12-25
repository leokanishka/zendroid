# To learn more about how to use Nix to configure your environment
# see: https://firebase.google.com/docs/studio/customize-workspace
{ pkgs, ... }: {
  channel = "stable-24.05";

  # Use https://search.nixos.org/packages to find packages
  packages = [
    pkgs.jdk17
    pkgs.gradle
  ];

  # Sets environment variables in the workspace
  env = {
    JAVA_HOME = "${pkgs.jdk17}";
  };

  # Android SDK configuration
  android = {
    enable = true;
    # Android API levels to install
    platforms = [ 34 ];
    # Build tools versions
    buildTools = [ "34.0.0" ];
    # Additional SDK packages
    extras = [
      "platform-tools"
      "emulator"
    ];
    # Enable Android emulator
    emulator = {
      enable = true;
    };
  };

  idx = {
    # Search for the extensions you want on https://open-vsx.org/ and use "publisher.id"
    extensions = [
      "mathiasfrohlich.Kotlin"
      "vscjava.vscode-gradle"
    ];

    # Enable previews
    previews = {
      enable = true;
      previews = {};
    };

    # Workspace lifecycle hooks
    workspace = {
      onCreate = {
        # Run gradle wrapper on workspace creation
        gradle-wrapper = "chmod +x ./gradlew";
      };
      onStart = {
        # Sync gradle on start
        gradle-sync = "./gradlew --version";
      };
    };
  };
}
