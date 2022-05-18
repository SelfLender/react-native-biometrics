import { NativeModules } from 'react-native';

const { ReactNativeBiometrics: bridge } = NativeModules;

/** 
 * Type alias for possible biometry types
 */
export type BiometryType = 'TouchID' | 'FaceID' | 'Biometrics';

interface IsSensorAvailableOptions {
    allowDeviceCredentials?: boolean
}

interface IsSensorAvailableResult {
    available: boolean
    biometryType?: BiometryType
    error?: string
}

interface CreateKeysResult {
    publicKey: string
}

interface BiometricKeysExistResult {
    keysExist: boolean
}

interface DeleteKeysResult {
    keysDeleted: boolean
}

interface CreateSignatureOptions {
    promptMessage: string
    payload: string
    cancelButtonText?: string
    allowDeviceCredentials?: boolean
}

interface CreateSignatureResult {
    success: boolean
    signature?: string
    error?: string
}

interface SimplePromptOptions {
    promptMessage: string
    fallbackPromptMessage?: string
    cancelButtonText?: string
    allowDeviceCredentials?: boolean
}

interface SimplePromptResult {
    success: boolean
    error?: string
}

module ReactNativeBiometrics {
    /**
     * Enum for touch id sensor type
     */
    export const TouchID = 'TouchID';
    /**
     * Enum for face id sensor type
     */
    export const FaceID = 'FaceID';
    /**
     * Enum for generic biometrics (this is the only value available on android)
     */
    export const Biometrics = 'Biometrics';

    /**
     * Returns promise that resolves to an object with object.biometryType = Biometrics | TouchID | FaceID
     * @returns {Promise<Object>} Promise that resolves to an object with details about biometrics available
     */
    export function isSensorAvailable(isSensorAvailableOptions?: IsSensorAvailableOptions): Promise<IsSensorAvailableResult> {
        const options = {
          allowDeviceCredentials: isSensorAvailableOptions?.allowDeviceCredentials ?? false
        }

        return bridge.isSensorAvailable(options);
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
     * @returns {Promise<Object>}  Promise that resolves to an object cryptographic signature details
     */
    export function createSignature(createSignatureOptions: CreateSignatureOptions): Promise<CreateSignatureResult> {
        createSignatureOptions.cancelButtonText = createSignatureOptions.cancelButtonText ?? 'Cancel';
        createSignatureOptions.allowDeviceCredentials = createSignatureOptions.allowDeviceCredentials ?? false

        return bridge.createSignature(createSignatureOptions);
    }

    /**
     * Prompts user with biometrics dialog using the passed in prompt message and
     * returns promise that resolves to an object with object.success = true if the user passes,
     * object.success = false if the user cancels, and rejects if anything fails
     * @param {Object} simplePromptOptions
     * @param {string} simplePromptOptions.promptMessage
     * @param {string} simplePromptOptions.fallbackPromptMessage
     * @returns {Promise<Object>}  Promise that resolves an object with details about the biometrics result
     */
    export function simplePrompt(simplePromptOptions: SimplePromptOptions): Promise<SimplePromptResult> {
        simplePromptOptions.cancelButtonText = simplePromptOptions.cancelButtonText ?? 'Cancel';
        simplePromptOptions.fallbackPromptMessage = simplePromptOptions.fallbackPromptMessage ?? 'Use Passcode';
        simplePromptOptions.allowDeviceCredentials = simplePromptOptions.allowDeviceCredentials ?? false

        return bridge.simplePrompt(simplePromptOptions);
    }
}

export default ReactNativeBiometrics;
