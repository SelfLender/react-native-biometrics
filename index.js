
import { NativeModules } from 'react-native'

const { ReactNativeBiometrics } = NativeModules

export default {
  /**
   * Enum for touch id sensor type
   */
  TouchID: 'TouchID',
  /**
   * Enum for face id sensor type
   */
  FaceID: 'FaceID',
  /**
   * Enum for generic biometrics (this is the only value available on android)
   */
  Biometrics: 'Biometrics',

  /**
   * Returns promise that resolves to an object with object.biometryType = Biometrics | TouchID | FaceID
   * @returns {Promise<Object>} Promise that resolves to null, TouchID, or FaceID
   */
  isSensorAvailable: () => {
    return ReactNativeBiometrics.isSensorAvailable()
  },
  /**
   * Creates a public private key pair,returns promise that resolves to
   * an object with object.publicKey, which is the public key of the newly generated key pair
   * @returns {Promise<Object>}  Promise that resolves to newly generated public key
   */
  createKeys: () => {
    return ReactNativeBiometrics.createKeys()
  },

  /**
   * Returns promise that resolves to an object with object.keysExists = true | false
   * indicating if the keys were found to exist or not
   * @returns {Promise<Object>} Promise that resolves to true or false
   */
  biometricKeysExist: () => {
    return ReactNativeBiometrics.biometricKeysExist()
  },

  /**
   * Returns promise that resolves to an object with true | false
   * indicating if the keys were properly deleted
   * @returns {Promise<Object>} Promise that resolves to true or false
   */
  deleteKeys: () => {
    return ReactNativeBiometrics.deleteKeys()
  },

  /**
   * Prompts user with biometrics dialog using the passed in prompt message and
   * returns promise that resolves to an object with object.signature,
   * which is cryptographic signature of the payload
   * @param {Object} createSignatureOptions
   * @param {string} createSignatureOptions.promptMessage
   * @param {string} createSignatureOptions.payload
   * @param {string} createSignatureOptions.cancelButtonText (Android only)
   * @returns {Promise<Object>}  Promise that resolves to cryptographic signature
   */
  createSignature: (createSignatureOptions) => {
    if (!createSignatureOptions.cancelButtonText) {
      createSignatureOptions.cancelButtonText = 'Cancel'
    }

    return ReactNativeBiometrics.createSignature(createSignatureOptions)
  },

  /**
   * Prompts user with biometrics dialog using the passed in prompt message and
   * returns promise that resolves to an object with object.success = true if the user passes,
   * object.success = false if the user cancels, and rejects if anything fails
   * @param {Object} simplePromptOptions
   * @param {string} simplePromptOptions.promptMessage
   * @param {string} simplePromptOptions.cancelButtonText (Android only)
   * @returns {Promise<Object>}  Promise that resolves to true if the user passes, resolves to false
   * if the user cancels, and rejects if anything fails
   */
  simplePrompt: (simplePromptOptions) => {
    if (!simplePromptOptions.cancelButtonText) {
      simplePromptOptions.cancelButtonText = 'Cancel'
    }

    return ReactNativeBiometrics.simplePrompt(simplePromptOptions)
  }
}
