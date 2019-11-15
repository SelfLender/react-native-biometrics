
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
   * Returns promise that resolves to null, TouchID, or FaceID
   * @returns {Promise} Promise that resolves to null, TouchID, or FaceID
   */
  isSensorAvailable: () => {
    return ReactNativeBiometrics.isSensorAvailable()
  },
  /**
   * Prompts user with biometrics dialog using the passed in prompt message if
   * it is provided, returns promise that resolves to the public key of the
   * newly generated key pair
   * @param {string} promptMessage
   * @returns {Promise}  Promise that resolves to newly generated public key
   */
  createKeys: () => {
    return ReactNativeBiometrics.createKeys()
  },

  /**
   * Returns promise that resolves to true or false indicating if the keys
   * were found to exists or not
   * @returns {Promise} Promise that resolves to true or false
   */
  biometricKeyExists: () => {
    return ReactNativeBiometrics.biometricKeyExists()
  },

  /**
   * Returns promise that resolves to true or false indicating if the keys
   * were properly deleted
   * @returns {Promise} Promise that resolves to true or false
   */
  deleteKeys: () => {
    return ReactNativeBiometrics.deleteKeys()
  },
  /**
   * Prompts user with biometrics dialog using the passed in prompt message and
   * returns promise that resolves to a cryptographic signature of the payload
   * @param {Object} createSignatureOptions
   * @param {string} createSignatureOptions.promptMessage
   * @param {string} createSignatureOptions.payload
   * @param {string} createSignatureOptions.cancelButtonText (Android only)
   * @returns {Promise}  Promise that resolves to cryptographic signature
   */
  createSignature: (createSignatureOptions) => {
    if (!createSignatureOptions.cancelButtonText) {
      createSignatureOptions.cancelButtonText = 'Cancel'
    }

    return ReactNativeBiometrics.createSignature(createSignatureOptions)
  },
  /**
   * Prompts user with biometrics dialog using the passed in prompt message and
   * returns promise that resolves to true if the user passes, resolves to false
   * if the user cancels, and rejects if anything fails
   * @param {Object} simplePromptOptions
   * @param {string} simplePromptOptions.promptMessage
   * @param {string} simplePromptOptions.cancelButtonText (Android only)
   * @returns {Promise}  Promise that resolves to true if the user passes, resolves to false
   * if the user cancels, and rejects if anything fails
   */
  simplePrompt: (simplePromptOptions) => {
    if (!simplePromptOptions.cancelButtonText) {
      simplePromptOptions.cancelButtonText = 'Cancel'
    }

    return ReactNativeBiometrics.simplePrompt(simplePromptOptions)
  }
}
