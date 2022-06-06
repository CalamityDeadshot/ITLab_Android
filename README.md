# ITLab - приложение под Android

## Trick to sign app everywhere
> A simple tutorial to sign your app on remote build machine
1. Get your keystore file. For example, `key.jks`
1. Convert keystore to `.txt` file via `openssl` and `base64` tools:
    ```bash
    openssl base64 < `key.jks` | tr -d '\n' | tee keystore.txt
    ```
    > Linux has this tools preinstalled
1. Content of `keystore.txt` use as you want:
    1. Copy to github secrets
    1. Copy to Azure DevOps pipeline variables
    1. Copy somewhere else in a secure place
1. Parse `keystore.txt` back
    ```bash
    echo keystore.txt | base64 --decode > keystore.jks
    ```
1. Use `keystore.jks` as it was in step 1

## Build android apk with docker
1. Set environment variables
    1. Mode `keystore.jks` file in root folder
    1. Create `.env` file:
        ```env
        SIGNING_KEY_ALIAS=set-key-alias-here
        SIGNING_KEY_PASSWORD=set-key-password-here
        SIGNING_STORE_PASSWORD=set-keystore-password-here
        ```
1. Build and run image
    > This command uses `compose.yaml` file with `deploy/Dockerfile-release`
    ```bash
    docker compose up --build
    ```
1. Check `output-release` folder for `*.apk` files
1. To use `debug` build task or `dev api`, you can override `CMD` command in Dockerfile via `docker compose`. Example:
    ```yml
    services:
      android-builder:
        build:
          dockerfile: deploy/Dockerfile-release
          context: .
        command: ./gradlew :app:assemble[Prod|Dev]Api[Release|Debug]
    ```
