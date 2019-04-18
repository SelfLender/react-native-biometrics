
# react-native-biometrics

React native biometrics is a simple bridge to native iOS and Android keystore management.  It allows you to create public private key pairs that are stored in native keystores and protected by biometric authentication.  Those keys can then be retrieved later, after proper authentication, and used to create a cryptographic signature.

## Getting started

`$ npm install react-native-biometrics --save`

### Automatic installation

`$ react-native link react-native-biometrics`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-biometrics` and add `ReactNativeBiometrics.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libReactNativeBiometrics.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.rnbiometrics.ReactNativeBiometricsPackage;` to the imports at the top of the file
  - Add `new ReactNativeBiometricsPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-biometrics'
  	project(':react-native-biometrics').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-biometrics/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-biometrics')
  	```

## Additional configuration

#### iOS

This package requires an iOS target SDK verion of iOS 10 or higher

Ensure that you have the `NSFaceIDUsageDescription` entry set in your react native iOS project, or Face ID will not work properly.  This description will be will be presented to the user the first time a biometrics action is taken, and the user will be asked if they want to allow the app to use Face ID.  If the user declines the usage of face id for the app, the `isSensorAvailable` function will return `null` until the face id permission is specifically allowed for the app by the user.

NOTE: No biometric prompt is displayed in iOS simulators when attempting to retrieve keys for signature generation, it only occurs on actual devices.

#### Android

This package requires a compiled SDK version of 23 (Android 6.0 Marshmallow) or higher

## Usage

This package is designed to make server authentication using biometrics easier.  Here is an image from https://android-developers.googleblog.com/2015/10/new-in-android-samples-authenticating.html illustrating the basic use case:

![react-native-biometrics](https://2.bp.blogspot.com/-Lp2zaAZietw/Vi59hb6k6SI/AAAAAAAABLk/HsXXBYiIwqU/s1600/image01.png)

When a user enrolls in biometrics, a key pair is generated.  The private key is stored securely on the device and the public key is sent to a server for registration.  When the user wishes to authenticate, the user is prompted for biometrics, which unlocks the securely stored private key.  Then a cryptographic signature is generated and sent to the server for verification.  The server then verifies the signature.  If the verification was successful, the server returns an appropriate response and authorizes the user.

## Constants

### TouchID

A constant for the touch id sensor type, evaluates to `'TouchID'`

__Example__

```js
import Biometrics from 'react-native-biometrics'

if (biometryType === Biometrics.TouchID) {
  //do something fingerprint specific
}
```

### FaceID

A constant for the face id sensor type, evaluates to `'FaceID'`

__Example__

```js
import Biometrics from 'react-native-biometrics'

if (biometryType === Biometrics.FaceID) {
  //do something face id specific
}
```

## Methods

### isSensorAvailable()

Detects what type of biometric sensor is available.  Returns a `Promise` that resolves to a string representing the sensor type (`TouchID`, `FaceID`, `null`)

__Example__

```js
import Biometrics from 'react-native-biometrics'

Biometrics.isSensorAvailable()
  .then((biometryType) => {
    if (biometryType === Biometrics.TouchID) {
      console.log('TouchID is supported')
    } else if (biometryType === Biometrics.FaceID) {
      console.log('FaceID is supported')
    } else {
      console.log('Biometrics not supported')
    }
  })
```

### createKeys([promptMessage])

Prompts the user for their fingerprint or face id, then generates a public private RSA 2048 key pair that will be stored in the device keystore.  Returns a `Promise` that resolves to a base64 encoded string representing the public key.

__Arguments__

- `promptMessage` - optional string that will be displayed in the fingerprint or face id prompt, if no prompt message is provided, no prompt will be displayed.

__Example__

```js
import Biometrics from 'react-native-biometrics'

Biometrics.createKeys('Confirm fingerprint')
  .then((publicKey) => {
    console.log(publicKey)
    sendPublicKeyToServer(publicKey)
  })
```

### deleteKeys()

Deletes the generated keys from the device keystore.  Returns a `Promise` that resolves to `true` or `false` indicating if the deletion was successful

__Example__

```js
import Biometrics from 'react-native-biometrics'

Biometrics.deleteKeys()
  .then((success) => {
    if (success) {
      console.log('Successful deletion')
    } else {
      console.log('Unsuccessful deletion')
    }
  })
```

### createSignature(promptMessage, payload)

Prompts the user for their fingerprint or face id in order to retrieve the private key from the keystore, then uses the private key to generate a RSA PKCS#1v1.5 SHA 256 signature.  Returns a `Promise` that resolves to a base64 encoded string representing the signature.

__Arguments__

- `promptMessage` - string that will be displayed in the fingerprint or face id prompt
- `payload` - string of data to be signed by the RSA signature

__Example__

```js
import Biometrics from 'react-native-biometrics'

let epochTimeSeconds = Math.round((new Date()).getTime() / 1000).toString()
let payload = epochTimeSeconds + 'some message'

Biometrics.createSignature('Sign in', payload)
  .then((signature) => {
    console.log(signature)
    verifySignatureWithServer(signature, payload)
  })
```

### simplePrompt(promptMessage)

Prompts the user for their fingerprint or face id. Returns a `Promise` that resolves if the user provides a valid fingerprint or face id, otherwise the promise rejects.

NOTE: This only validates a user's biometrics.  This should not be used to log a user in or authenticate with a server, instead use `createSignature`.  It should only be used to gate certain user actions within an app.

__Arguments__

- `promptMessage` - string that will be displayed in the fingerprint or face id prompt

__Example__

```js
import Biometrics from 'react-native-biometrics'

Biometrics.simplePrompt('Confirm fingerprint')
  .then(() => {
    console.log('successful fingerprint provided')
  })
  .catch(() => {
    console.log('fingerprint failed or prompt was cancelled')
  })
```
