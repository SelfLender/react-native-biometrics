"use strict";
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
Object.defineProperty(exports, "__esModule", { value: true });
var react_native_1 = require("react-native");
var bridge = react_native_1.NativeModules.ReactNativeBiometrics;
/**
 * Enum for touch id sensor type
 */
exports.TouchID = 'TouchID';
/**
 * Enum for face id sensor type
 */
exports.FaceID = 'FaceID';
/**
 * Enum for generic biometrics (this is the only value available on android)
 */
exports.Biometrics = 'Biometrics';
exports.BiometryTypes = {
    TouchID: exports.TouchID,
    FaceID: exports.FaceID,
    Biometrics: exports.Biometrics
};
var ReactNativeBiometricsLegacy;
(function (ReactNativeBiometricsLegacy) {
    /**
     * Returns promise that resolves to an object with object.biometryType = Biometrics | TouchID | FaceID
     * @returns {Promise<Object>} Promise that resolves to an object with details about biometrics available
     */
    function isSensorAvailable() {
        return new ReactNativeBiometrics().isSensorAvailable();
    }
    ReactNativeBiometricsLegacy.isSensorAvailable = isSensorAvailable;
    /**
     * Creates a public private key pair,returns promise that resolves to
     * an object with object.publicKey, which is the public key of the newly generated key pair
     * @returns {Promise<Object>}  Promise that resolves to object with details about the newly generated public key
     */
    function createKeys() {
        return new ReactNativeBiometrics().createKeys();
    }
    ReactNativeBiometricsLegacy.createKeys = createKeys;
    /**
     * Returns promise that resolves to an object with object.keysExists = true | false
     * indicating if the keys were found to exist or not
     * @returns {Promise<Object>} Promise that resolves to object with details aobut the existence of keys
     */
    function biometricKeysExist() {
        return new ReactNativeBiometrics().biometricKeysExist();
    }
    ReactNativeBiometricsLegacy.biometricKeysExist = biometricKeysExist;
    /**
     * Returns promise that resolves to an object with true | false
     * indicating if the keys were properly deleted
     * @returns {Promise<Object>} Promise that resolves to an object with details about the deletion
     */
    function deleteKeys() {
        return new ReactNativeBiometrics().deleteKeys();
    }
    ReactNativeBiometricsLegacy.deleteKeys = deleteKeys;
    /**
     * Prompts user with biometrics dialog using the passed in prompt message and
     * returns promise that resolves to an object with object.signature,
     * which is cryptographic signature of the payload
     * @param {Object} createSignatureOptions
     * @param {string} createSignatureOptions.promptMessage
     * @param {string} createSignatureOptions.payload
     * @returns {Promise<Object>}  Promise that resolves to an object cryptographic signature details
     */
    function createSignature(createSignatureOptions) {
        return new ReactNativeBiometrics().createSignature(createSignatureOptions);
    }
    ReactNativeBiometricsLegacy.createSignature = createSignature;
    /**
     * Prompts user with biometrics dialog using the passed in prompt message and
     * returns promise that resolves to an object with object.success = true if the user passes,
     * object.success = false if the user cancels, and rejects if anything fails
     * @param {Object} simplePromptOptions
     * @param {string} simplePromptOptions.promptMessage
     * @param {string} simplePromptOptions.fallbackPromptMessage
     * @returns {Promise<Object>}  Promise that resolves an object with details about the biometrics result
     */
    function simplePrompt(simplePromptOptions) {
        return new ReactNativeBiometrics().simplePrompt(simplePromptOptions);
    }
    ReactNativeBiometricsLegacy.simplePrompt = simplePrompt;
})(ReactNativeBiometricsLegacy = exports.ReactNativeBiometricsLegacy || (exports.ReactNativeBiometricsLegacy = {}));
var ReactNativeBiometrics = /** @class */ (function () {
    /**
     * @param {Object} rnBiometricsOptions
     * @param {boolean} rnBiometricsOptions.allowDeviceCredentials
     */
    function ReactNativeBiometrics(rnBiometricsOptions) {
        var _a, _b;
        this.allowDeviceCredentials = false;
        var allowDeviceCredentials = (_b = (_a = rnBiometricsOptions) === null || _a === void 0 ? void 0 : _a.allowDeviceCredentials, (_b !== null && _b !== void 0 ? _b : false));
        this.allowDeviceCredentials = allowDeviceCredentials;
    }
    /**
     * Returns promise that resolves to an object with object.biometryType = Biometrics | TouchID | FaceID
     * @returns {Promise<Object>} Promise that resolves to an object with details about biometrics available
     */
    ReactNativeBiometrics.prototype.isSensorAvailable = function () {
        return bridge.isSensorAvailable({
            allowDeviceCredentials: this.allowDeviceCredentials
        });
    };
    /**
     * Creates a public private key pair,returns promise that resolves to
     * an object with object.publicKey, which is the public key of the newly generated key pair
     * @returns {Promise<Object>}  Promise that resolves to object with details about the newly generated public key
     */
    ReactNativeBiometrics.prototype.createKeys = function () {
        return bridge.createKeys({
            allowDeviceCredentials: this.allowDeviceCredentials
        });
    };
    /**
     * Returns promise that resolves to an object with object.keysExists = true | false
     * indicating if the keys were found to exist or not
     * @returns {Promise<Object>} Promise that resolves to object with details aobut the existence of keys
     */
    ReactNativeBiometrics.prototype.biometricKeysExist = function () {
        return bridge.biometricKeysExist();
    };
    /**
     * Returns promise that resolves to an object with true | false
     * indicating if the keys were properly deleted
     * @returns {Promise<Object>} Promise that resolves to an object with details about the deletion
     */
    ReactNativeBiometrics.prototype.deleteKeys = function () {
        return bridge.deleteKeys();
    };
    /**
     * Prompts user with biometrics dialog using the passed in prompt message and
     * returns promise that resolves to an object with object.signature,
     * which is cryptographic signature of the payload
     * @param {Object} createSignatureOptions
     * @param {string} createSignatureOptions.promptMessage
     * @param {string} createSignatureOptions.payload
     * @returns {Promise<Object>}  Promise that resolves to an object cryptographic signature details
     */
    ReactNativeBiometrics.prototype.createSignature = function (createSignatureOptions) {
        var _a;
        createSignatureOptions.cancelButtonText = (_a = createSignatureOptions.cancelButtonText, (_a !== null && _a !== void 0 ? _a : 'Cancel'));
        return bridge.createSignature(__assign({ allowDeviceCredentials: this.allowDeviceCredentials }, createSignatureOptions));
    };
    /**
     * Prompts user with biometrics dialog using the passed in prompt message and
     * returns promise that resolves to an object with object.success = true if the user passes,
     * object.success = false if the user cancels, and rejects if anything fails
     * @param {Object} simplePromptOptions
     * @param {string} simplePromptOptions.promptMessage
     * @param {string} simplePromptOptions.fallbackPromptMessage
     * @returns {Promise<Object>}  Promise that resolves an object with details about the biometrics result
     */
    ReactNativeBiometrics.prototype.simplePrompt = function (simplePromptOptions) {
        var _a, _b;
        simplePromptOptions.cancelButtonText = (_a = simplePromptOptions.cancelButtonText, (_a !== null && _a !== void 0 ? _a : 'Cancel'));
        simplePromptOptions.fallbackPromptMessage = (_b = simplePromptOptions.fallbackPromptMessage, (_b !== null && _b !== void 0 ? _b : 'Use Passcode'));
        return bridge.simplePrompt(__assign({ allowDeviceCredentials: this.allowDeviceCredentials }, simplePromptOptions));
    };
    return ReactNativeBiometrics;
}());
exports.default = ReactNativeBiometrics;
//# sourceMappingURL=index.js.map