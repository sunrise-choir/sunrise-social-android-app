# [Sunrise Social](https://sunrise.social/)

> A native android app for [scuttlebutt](https://scuttlebutt.nz/), written by the [sunrise choir](https://github.com/sunrise-choir/).

## Contents

- [Overview](#overview)
- [Cool features](#cool-features)
- [Dev Setup](#dev-setup)
  - [Requirements](#requirements)
- [Project Status](#project-status)
- [Hints, tips and known issues](#hints-tips-and-known-issues)
- [Contributing](#contributing)
- [Thanks](#thanks)


## Overview

Sunrise social is a native android app written in [kotlin](https://kotlinlang.org/). It has three main parts:
- The android app (this repo)
- [patchql](https://github.com/sunrise-choir/ssb-patchql) (and the [android bindings to patchql](https://github.com/sunrise-choir/ssb-patchql-android)), written in Rust.
- The [scuttlebutt server](https://github.com/cryptoscope/ssb/) (and the [bindings]()), written in go by [@cryptix](https://github.com/cryptix), [@keks](https://github.com/keks) and [@PragmaticCypher](https://github.com/PragmaticCypher).

The scuttlebutt server discovers peers on the local network, replicates feeds of friends and publishes new messages. It stores all the feed data in a binary append only log.

Patchql knows how to parse the server's append only log, and uses it to build indexes in a [sqlite3](https://www.sqlite.org/index.html) database using the [diesel-rs](http://diesel.rs/) ORM.

The app makes [graphql](https://graphql.org/) requests to patchql via the android [apollo](https://github.com/apollographql/apollo-android) library to build views.


## Cool features

- _A lot_ of effort has gone into designing a system that has a responsive ui _even when downloading and indexing lots of feeds_. This is an ongoing pain with the [js flume](https://github.com/flumedb/flumedb) that [manyverse](https://www.manyver.se/) is still suffering with.
  - patchql has a database connection pool internally that has a single writer and multiple readers. This means multiple threads can be making graphql queries while a different thread can be processing the offset log into the database.
- Type safety is a good thing. The type safety enforced by rust + diesel goes so well with graphql. We generate a graphql schema from patchql and then copy it into the [ssb-patchql-android](https://github.com/sunrise-choir/ssb-patchql-android) module. Then the [apollo](https://github.com/apollographql/apollo-android) plugin uses the schema to generate classes for us to use in the app. 


## Project status 

This app is not ready for the play store yet. 

In some ways it's proof that sunrise choir is making a stack that _will_ be useful and production ready. But it needs more work!  Can **you** help us out? Please chip in. We're actively seeking [contributors](#Contributing). 

### Todos

See the [project](https://github.com/sunrise-choir/sunrise-social-android-app/projects)


## Dev Setup

### Requirements:

- [Rust](https://rustup.rs/) using rustup
- [Android SDK + Android Studio](http://www.androiddocs.com/sdk/installing/index.html)
- [Android NDK](https://developer.android.com/studio/projects/install-ndk)
- [Go](https://golang.org/doc/install) and [go-mobile](https://github.com/golang/go/wiki/Mobile) (Optional, only needed if you want to develop the go stack)
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

### Go development

If you need to extend the go code, follow the [instructions](https://github.com/sunrise-choir/sunrise-social-gobot) in the readme to build bindings with [go-mobile](https://github.com/golang/go/wiki/Mobile).

You need to manually copy the generated bindings into this project. Copy the `.aar` and `.jar` files in the `app` folder of the go project into the app folder of this project eg: In the go project directory: `cp app/* <path-to-this-repo>/app`

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

Note: This behaviour comes from the go sbot, it will change in the future but it's workable for development.


## Hints, tips and known issues

- replication in go is unexpectedly slow. Cryptix gets replication speeds ~100 times faster on an iphone with comparable specs to my andoid phone. We're not sure if it's about fs access or what.
- If you update the graphql schema or queries, or you change the gobot .aar file, you should do a `File->Invalidate Caches / Restart` in android studio. In fact, do this any time you think android studio is being _spooky_.
- If you can find the files the app creates in `/data/data/social.sunrise.app/`
- blob loading needs some work. It asks the sbot if it has the blob, if it doesn't we `want` it and then that's it. So later if we ask for it again (when we scroll or refresh or navigate) we might get it then.


## Contributing

- Check out the sunrise choir [contribution](https://github.com/sunrise-choir/meta/blob/master/CONTRIBUTING.md) guidelines.
- We have a [code of conduct](https://github.com/sunrise-choir/meta/blob/master/CODE_OF_CONDUCT.md)
- **Important** your must accept [the contributor licence agreement](https://github.com/sunrise-choir/meta/blob/master/processes/cla.md) if you're going to submit code.


## Thanks

- Many many many thanks to [@cryptix](https://github.com/cryptix). I truly couldn't have done it without his kind and generous help. I'm sure at times supporting this has been a cause of extra stress on top of lots of other stuff, so I'm very grateful.
- Of course, none of this would exist without the generosity and vision of [@ahdinosaur](https://github.com/ahdinosaur/). Please support [sunrise choir on open collective](https://opencollective.com/sunrise-choir)
