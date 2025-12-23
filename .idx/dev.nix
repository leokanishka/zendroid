{ pkgs, ... }: {
  # Which nixpkgs channel to use.
  channel = "stable-24.05"; # Updated for better Android support

  # Use https://search.nixos.org/packages to find packages
  packages = [
    pkgs.gradle
    pkgs.jdk17
    pkgs.android-tools
  ];

  # Sets environment variables in the workspace
  env = {
    JAVA_HOME = pkgs.jdk17;
  };

  idx = {
    # Search for the extensions you want on https://open-vsx.org/ and use "publisher.id"
    extensions = [
      "mathiasfroehlich.Kotlin"
      "google.android-idx"
    ];

    # Enable previews
    previews = {
      enable = true;
      previews = {
        # web = {
        #   # Example: run "npm run dev" with PORT set to IDX's defined port for previews,
        #   # and configure it to allow access from any host
        #   command = ["npm" "run" "dev" "--" "--port" "$PORT" "--host" "0.0.0.0"];
        #   manager = "web";
        # };
      };
    };

    # Workspace lifecycle hooks
    workspace = {
      # Runs when a workspace is first created
      onCreate = {
        # Example: install JS dependencies from NPM
        # npm-install = "npm install";
      };
      # Runs when a workspace is (re)started
      onStart = {
        # Example: start a continuous build process
        # build-project = "gradle assembleDebug";
      };
    };
  };
}
