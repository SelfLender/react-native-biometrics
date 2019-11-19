export default interface ReactNativeBiometrics {
  /**
   * Enum for touch id sensor type
   */
  TouchID: string;

  /**
   * Enum for face id sensor type
   */
  FaceID: string;

  /**
   * Enum for generic biometrics (this is the only value available on android)
   */
  Biometrics: string;

  /**
   * Returns promise that resolves to an object with object.biometryType = Biometrics | TouchID | FaceID
   * @returns {Promise<Object>} Promise that resolves to null, TouchID, or FaceID
   */
  isSensorAvailable(): Promise<Object>;

  /**
   * Creates a public private key pair,returns promise that resolves to
   * an object with object.publicKey, which is the public key of the newly generated key pair
   * @returns {Promise<Object>}  Promise that resolves to newly generated public key
   */
  createKeys(): Promise<Object>;

  /**
   * Returns promise that resolves to an object with object.keysExists = true | false
   * indicating if the keys were found to exist or not
   * @returns {Promise<Object>} Promise that resolves to true or false
   */
  biometricKeysExist(): Promise<Object>;

  /**
   * Returns promise that resolves to an object with true | false
   * indicating if the keys were properly deleted
   * @returns {Promise<Object>} Promise that resolves to true or false
   */
  deleteKeys(): Promise<Object>;

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
  createSignature(createSignatureOptions: Object): Promise<Object>;

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
  simplePrompt(simplePromptOptions: Object): Promise<Object>;
}
