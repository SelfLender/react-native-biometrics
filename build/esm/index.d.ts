/**
 * Type alias for possible biometry types
 */
export declare type BiometryType = "TouchID" | "FaceID" | "Biometrics";
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
declare module ReactNativeBiometrics {
    /**
     * Enum for touch id sensor type
     */
    const TouchID = "TouchID";
    /**
     * Enum for face id sensor type
     */
    const FaceID = "FaceID";
    /**
     * Enum for generic biometrics (this is the only value available on android)
     */
    const Biometrics = "Biometrics";
    /**
     * Returns promise that resolves to an object with object.biometryType = Biometrics | TouchID | FaceID
     * @returns {Promise<Object>} Promise that resolves to an object with details about biometrics available
     */
    function isSensorAvailable(): Promise<IsSensorAvailableResult>;
    /**
     * Creates a public private key pair,returns promise that resolves to
     * an object with object.publicKey, which is the public key of the newly generated key pair
     * @returns {Promise<Object>}  Promise that resolves to object with details about the newly generated public key
     */
    function createKeys(): Promise<CreateKeysResult>;
    /**
     * Returns promise that resolves to an object with object.keysExists = true | false
     * indicating if the keys were found to exist or not
     * @returns {Promise<Object>} Promise that resolves to object with details aobut the existence of keys
     */
    function biometricKeysExist(): Promise<BiometricKeysExistResult>;
    /**
     * Returns promise that resolves to an object with true | false
     * indicating if the keys were properly deleted
     * @returns {Promise<Object>} Promise that resolves to an object with details about the deletion
     */
    function deleteKeys(): Promise<DeleteKeysResult>;
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
    function createSignature(createSignatureOptions: CreateSignatureOptions): Promise<CreateSignatureResult>;
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
    function biometricPrompt(biometricPromptOptions: biometricPromptOptions): Promise<biometricPromptResult>;
    /**
     * [ANDROID ONLY!] Prompts user with a device credentials dialog
     * @param {Object} deviceCredentialsPromptOptions
     * @param {string} biometricPromptOptions.promptMessage
     * @param {string} biometricPromptOptions.cancelButtonText
     * @returns {Promise<Object>}  Promise that resolves an object with details about the authentication result
     */
    function deviceCredentialsPrompt(deviceCredentialsPromptOptions: deviceCredentialsPromptOptions): Promise<deviceCredentialsPromptResult>;
}
export default ReactNativeBiometrics;
