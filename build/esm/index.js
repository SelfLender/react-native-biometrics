import { NativeModules } from "react-native";
var bridge = NativeModules.ReactNativeBiometrics;
var ReactNativeBiometrics;
(function (ReactNativeBiometrics) {
    /**
     * Enum for touch id sensor type
     */
    ReactNativeBiometrics.TouchID = "TouchID";
    /**
     * Enum for face id sensor type
     */
    ReactNativeBiometrics.FaceID = "FaceID";
    /**
     * Enum for generic biometrics (this is the only value available on android)
     */
    ReactNativeBiometrics.Biometrics = "Biometrics";
    /**
     * Returns promise that resolves to an object with object.biometryType = Biometrics | TouchID | FaceID
     * @returns {Promise<Object>} Promise that resolves to an object with details about biometrics available
     */
    function isSensorAvailable() {
        return bridge.isSensorAvailable();
    }
    ReactNativeBiometrics.isSensorAvailable = isSensorAvailable;
    /**
     * Creates a public private key pair,returns promise that resolves to
     * an object with object.publicKey, which is the public key of the newly generated key pair
     * @returns {Promise<Object>}  Promise that resolves to object with details about the newly generated public key
     */
    function createKeys() {
        return bridge.createKeys();
    }
    ReactNativeBiometrics.createKeys = createKeys;
    /**
     * Returns promise that resolves to an object with object.keysExists = true | false
     * indicating if the keys were found to exist or not
     * @returns {Promise<Object>} Promise that resolves to object with details aobut the existence of keys
     */
    function biometricKeysExist() {
        return bridge.biometricKeysExist();
    }
    ReactNativeBiometrics.biometricKeysExist = biometricKeysExist;
    /**
     * Returns promise that resolves to an object with true | false
     * indicating if the keys were properly deleted
     * @returns {Promise<Object>} Promise that resolves to an object with details about the deletion
     */
    function deleteKeys() {
        return bridge.deleteKeys();
    }
    ReactNativeBiometrics.deleteKeys = deleteKeys;
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
    function createSignature(createSignatureOptions) {
        if (!createSignatureOptions.cancelButtonText) {
            createSignatureOptions.cancelButtonText = "Cancel";
        }
        return bridge.createSignature(createSignatureOptions);
    }
    ReactNativeBiometrics.createSignature = createSignature;
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
    function biometricPrompt(biometricPromptOptions) {
        if (!biometricPromptOptions.cancelButtonText) {
            biometricPromptOptions.cancelButtonText = "Cancel";
        }
        if (!biometricPromptOptions.allowDeviceCredentials) {
            biometricPromptOptions.allowDeviceCredentials = false;
        }
        return bridge.biometricPrompt(biometricPromptOptions);
    }
    ReactNativeBiometrics.biometricPrompt = biometricPrompt;
    /**
     * [ANDROID ONLY!] Prompts user with a device credentials dialog
     * @param {Object} deviceCredentialsPromptOptions
     * @param {string} biometricPromptOptions.promptMessage
     * @param {string} biometricPromptOptions.cancelButtonText
     * @returns {Promise<Object>}  Promise that resolves an object with details about the authentication result
     */
    function deviceCredentialsPrompt(deviceCredentialsPromptOptions) {
        return bridge.deviceCredentialsPrompt(deviceCredentialsPromptOptions);
    }
    ReactNativeBiometrics.deviceCredentialsPrompt = deviceCredentialsPrompt;
})(ReactNativeBiometrics || (ReactNativeBiometrics = {}));
export default ReactNativeBiometrics;
//# sourceMappingURL=index.js.map