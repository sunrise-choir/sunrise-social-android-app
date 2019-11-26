# [Sunrise Social](https://sunrise.social/)

> A native android app for [scuttlebutt](https://scuttlebutt.nz/), written by the [sunrise choir](https://github.com/sunrise-choir/).

## Overview

Sunrise social is a native android app written in [kotlin](https://kotlinlang.org/). It has three main parts:
- The android app (this repo)
- [patchql](https://github.com/sunrise-choir/ssb-patchql) (and the [android bindings to patchql](https://github.com/sunrise-choir/ssb-patchql-android))
- The [scuttlebutt server](), written in go.

The scuttlebutt server discovers peers on the local network, replicates feeds of friends and publishes new messages. It stores all the feed data in a binary append only log.

Patchql knows how to parse the server's append only log, and uses it to build indexes in a [sqlite3](https://www.sqlite.org/index.html) database using the [diesel-rs](http://diesel.rs/) ORM.

The app makes [graphql](https://graphql.org/) requests to patchql via the android [apollo](https://github.com/apollographql/apollo-android) library.

## Dev Setup

### Requirements:

- [Rust](https://rustup.rs/) using rustup
- [Android SDK + Android Studio](http://www.androiddocs.com/sdk/installing/index.html)
- [Android NDK](https://developer.android.com/studio/projects/install-ndk)
- [Go](https://golang.org/doc/install) (Optional, only needed if you want to develop the go stack)
- An android phone to develop on. I have tested that images for the emulator do run, but I haven't worked out how to get networking going.

### Install the rust cross-compilers for android:

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

### Build and install:

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

Note: This behaviour comes from the go sbot, it will change in the future but it's workable for dev.

## Todos

See the [project]()

## Hints, tips and known issues

- go dev
- app files location on the phone
- blob loading 
