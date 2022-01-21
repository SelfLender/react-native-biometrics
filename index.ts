import { NativeModules } from "react-native";

const { ReactNativeBiometrics: bridge } = NativeModules;

/**
 * Type alias for possible biometry types
 */
export type BiometryType = "TouchID" | "FaceID" | "Biometrics";

interface IsSensorAvailableResult {
  available: boolean;
  biometryType?: BiometryType;
  error?: string;
}

interface CreateKeysResult {
  publicKey: string;
}

interface BiometricKeysExistResult {
  keysExist: boolean;
}

interface DeleteKeysResult {
  keysDeleted: boolean;
}

interface CreateSignatureOptions {
  promptMessage: string;
  payload: string;
  cancelButtonText?: string;
}

interface CreateSignatureResult {
  success: boolean;
  signature?: string;
  error?: string;
}

interface biometricPromptOptions {
  promptMessage: string;
  cancelButtonText?: string;
  allowDeviceCredentials?: boolean;
}

interface deviceCredentialsPromptResult {
  success: boolean;
  error?: string;
}

interface deviceCredentialsPromptOptions {
  promptMessage: string;
}

interface biometricPromptResult {
  success: boolean;
  error?: string;
}

module ReactNativeBiometrics {
  /**
   * Enum for touch id sensor type
   */
  export const TouchID = "TouchID";
  /**
   * Enum for face id sensor type
   */
  export const FaceID = "FaceID";
  /**
   * Enum for generic biometrics (this is the only value available on android)
   */
  export const Biometrics = "Biometrics";

  /**
   * Returns promise that resolves to an object with object.biometryType = Biometrics | TouchID | FaceID
   * @returns {Promise<Object>} Promise that resolves to an object with details about biometrics available
   */
  export function isSensorAvailable(): Promise<IsSensorAvailableResult> {
    return bridge.isSensorAvailable();
  }
  /**
   * Creates a public private key pair,returns promise that resolves to
   * an object with object.publicKey, which is the public key of the newly generated key pair
   * @returns {Promise<Object>}  Promise that resolves to object with details about the newly generated public key
   */
  export function createKeys(): Promise<CreateKeysResult> {
    return bridge.createKeys();
  }

  /**
   * Returns promise that resolves to an object with object.keysExists = true | false
   * indicating if the keys were found to exist or not
   * @returns {Promise<Object>} Promise that resolves to object with details aobut the existence of keys
   */
  export function biometricKeysExist(): Promise<BiometricKeysExistResult> {
    return bridge.biometricKeysExist();
  }

  /**
   * Returns promise that resolves to an object with true | false
   * indicating if the keys were properly deleted
   * @returns {Promise<Object>} Promise that resolves to an object with details about the deletion
   */
  export function deleteKeys(): Promise<DeleteKeysResult> {
    return bridge.deleteKeys();
  }

  /**
   * Prompts user with biometrics dialog using the passed in prompt message and
   * returns promise that resolves to an object with object.signature,
   * which is cryptographic signature of the payload
   * @param {Object} createSignatureOptions
   * @param {string} createSignatureOptions.promptMessage
   * @param {string} createSignatureOptions.payload
   * @param {string} createSignatureOptions.cancelButtonText (Android only)
   * @returns {Promise<Object>}  Promise that resolves to an object cryptographic signature details
   */
  export function createSignature(
    createSignatureOptions: CreateSignatureOptions
  ): Promise<CreateSignatureResult> {
    if (!createSignatureOptions.cancelButtonText) {
      createSignatureOptions.cancelButtonText = "Cancel";
    }

    return bridge.createSignature(createSignatureOptions);
  }

  /**
   * Prompts user with biometrics dialog using the passed in prompt message and
   * returns promise that resolves to an object with object.success = true if the user passes,
   * object.success = false if the user cancels, and rejects if anything fails
   * @param {Object} biometricPromptOptions
   * @param {string} biometricPromptOptions.promptMessage
   * @param {string} biometricPromptOptions.cancelButtonText (Android only)
   * @param {string} biometricPromptOptions.allowDeviceCredentials (Android only)
   * @returns {Promise<Object>}  Promise that resolves an object with details about the biometrics result
   */
  export function biometricPrompt(
    biometricPromptOptions: biometricPromptOptions
  ): Promise<biometricPromptResult> {
    if (!biometricPromptOptions.cancelButtonText) {
      biometricPromptOptions.cancelButtonText = "Cancel";
    }

    if (!biometricPromptOptions.allowDeviceCredentials) {
      biometricPromptOptions.allowDeviceCredentials = false;
    }

    return bridge.biometricPrompt(biometricPromptOptions);
  }

  /**
   * [ANDROID ONLY!] Prompts user with a device credentials dialog
   * @param {Object} deviceCredentialsPromptOptions
   * @param {string} biometricPromptOptions.promptMessage
   * @param {string} biometricPromptOptions.cancelButtonText
   * @returns {Promise<Object>}  Promise that resolves an object with details about the authentication result
   */
  export function deviceCredentialsPrompt(
    deviceCredentialsPromptOptions: deviceCredentialsPromptOptions
  ): Promise<deviceCredentialsPromptResult> {
    return bridge.deviceCredentialsPrompt(deviceCredentialsPromptOptions);
  }
}

export default ReactNativeBiometrics;
