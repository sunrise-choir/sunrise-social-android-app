# [Sunrise Social](https://sunrise.social/)

> A native android app for scuttlebutt.

## Overview



## Dev Setup

### Requirements:

- [Rust](https://rustup.rs/) using rustup
- [Android SDK + Android Studio](http://www.androiddocs.com/sdk/installing/index.html)
- [Android NDK](https://developer.android.com/studio/projects/install-ndk)
- [Go](https://golang.org/doc/install) (Optional, only needed if you want to develop the go stack)
- An android phone to develop on. I have tested that images for the emulator do run, but I haven't worked out how to get networking going.

### Install the rust cross compilers for android:

```sh
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

### Get the repos:

```sh
mkdir sunrise_social; cd sunrise_social
git clone git@github.com:sunrise-choir/ssb-patchql-android.git
git clone git@github.com:sunrise-choir/sunrise-social-android-app.git 
```

### Open the project in Android Studio:

Open android studio -> close any open projects -> "Open existing Android Studio Project" -> browse to "sunrise_social" folder and select the "sunrise-social-android-app" folder (it should have a special icon in the file explorer.)

### Build and install

The first build will take a while because it needs to build all the rust code. Builds after that are much faster.

## Doing the first onboarding after an install.

**The order of this is important, follow it exactly. It matters that patchwork follows the phone first.**

- with your phone and you computer on the same local network
- open the app
- open patchwork
- in patchwork, wait for the phone to be discovered as a local peer (takes a minute or so)
- follow the phone in patchwork
- in the app, use the drawer menu (swipes in from the left) and select "Peers"
- You should see the IP and Pub key of patchwork. Select it.
- select follow
- this should trigger the phone to start replicating. 
- Wait for a minute while the sbot reconnects to patchwork. Then try pushing the home button on the bottom menu every so often, it will refresh with new content as it is downloaded and indexed. The ui will never block while indexing / replicating is happening.

## Todos

See the [project]()

## Hints, tips and known issues

- 
