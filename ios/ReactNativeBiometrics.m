//
//  ReactNativeBiometrics.m
//
//

#import "ReactNativeBiometrics.h"
#import <LocalAuthentication/LocalAuthentication.h>
#import <Security/Security.h>
#import <React/RCTConvert.h>

@implementation ReactNativeBiometrics

RCT_EXPORT_MODULE(ReactNativeBiometrics);

RCT_EXPORT_METHOD(isSensorAvailable: (NSDictionary *)params resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  LAContext *context = [[LAContext alloc] init];
  NSError *la_error = nil;
  BOOL allowDeviceCredentials = [RCTConvert BOOL:params[@"allowDeviceCredentials"]];
  LAPolicy laPolicy = LAPolicyDeviceOwnerAuthenticationWithBiometrics;

  if (allowDeviceCredentials == TRUE) {
    laPolicy = LAPolicyDeviceOwnerAuthentication;
  }

  BOOL canEvaluatePolicy = [context canEvaluatePolicy:laPolicy error:&la_error];

  if (canEvaluatePolicy) {
    NSString *biometryType = [self getBiometryType:context];
    NSDictionary *result = @{
      @"available": @(YES),
      @"biometryType": biometryType
    };

    resolve(result);
  } else {
    NSString *errorMessage = [NSString stringWithFormat:@"%@", la_error];
    NSDictionary *result = @{
      @"available": @(NO),
      @"error": errorMessage
    };

    resolve(result);
  }
}

RCT_EXPORT_METHOD(createKeys: (NSDictionary *)params resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    CFErrorRef error = NULL;
    BOOL allowDeviceCredentials = [RCTConvert BOOL:params[@"allowDeviceCredentials"]];

    // <-- Changed here to detect biometric changes -->
    SecAccessControlCreateFlags secCreateFlag = kSecAccessControlBiometryCurrentSet;

    if (allowDeviceCredentials == TRUE) {
      secCreateFlag = kSecAccessControlUserPresence;
    }

    SecAccessControlRef sacObject = SecAccessControlCreateWithFlags(kCFAllocatorDefault,
                                                                    kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
                                                                    secCreateFlag,
                                                                    &error);
    if (sacObject == NULL || error != NULL) {
      NSString *errorString = [NSString stringWithFormat:@"SecItemAdd can't create sacObject: %@", error];
      reject(@"storage_error", errorString, nil);
      return;
    }

    NSData *biometricKeyTag = [self getBiometricKeyTag];
    NSDictionary *keyAttributes = @{
                                    (id)kSecClass: (id)kSecClassKey,
                                    (id)kSecAttrKeyType: (id)kSecAttrKeyTypeRSA,
                                    (id)kSecAttrKeySizeInBits: @2048,
                                    (id)kSecPrivateKeyAttrs: @{
                                        (id)kSecAttrIsPermanent: @YES,
                                        (id)kSecUseAuthenticationUI: (id)kSecUseAuthenticationUIAllow,
                                        (id)kSecAttrApplicationTag: biometricKeyTag,
                                        (id)kSecAttrAccessControl: (__bridge_transfer id)sacObject
                                    }
                                  };

    [self deleteBiometricKey];
    NSError *gen_error = nil;
    id privateKey = CFBridgingRelease(SecKeyCreateRandomKey((__bridge CFDictionaryRef)keyAttributes, (void *)&gen_error));

    if(privateKey != nil) {
      id publicKey = CFBridgingRelease(SecKeyCopyPublicKey((SecKeyRef)privateKey));
      CFDataRef publicKeyDataRef = SecKeyCopyExternalRepresentation((SecKeyRef)publicKey, nil);
      NSData *publicKeyData = (__bridge NSData *)publicKeyDataRef;
      NSData *publicKeyDataWithHeader = [self addHeaderPublickey:publicKeyData];
      NSString *publicKeyString = [publicKeyDataWithHeader base64EncodedStringWithOptions:0];

      NSDictionary *result = @{
        @"publicKey": publicKeyString,
      };
      resolve(result);
    } else {
      NSString *message = [NSString stringWithFormat:@"Key generation error: %@", gen_error];
      reject(@"storage_error", message, nil);
    }
  });
}

RCT_EXPORT_METHOD(deleteKeys: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    BOOL biometricKeyExists = [self doesBiometricKeyExist];

    if (biometricKeyExists) {
      OSStatus status = [self deleteBiometricKey];

      if (status == noErr) {
        NSDictionary *result = @{
          @"keysDeleted": @(YES),
        };
        resolve(result);
      } else {
        NSString *message = [NSString stringWithFormat:@"Key not found: %@",[self keychainErrorToString:status]];
        reject(@"deletion_error", message, nil);
      }
    } else {
        NSDictionary *result = @{
          @"keysDeleted": @(NO),
        };
        resolve(result);
    }
  });
}

RCT_EXPORT_METHOD(createSignature: (NSDictionary *)params resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    NSString *promptMessage = [RCTConvert NSString:params[@"promptMessage"]];
    NSString *payload = [RCTConvert NSString:params[@"payload"]];

    NSData *biometricKeyTag = [self getBiometricKeyTag];
    NSDictionary *query = @{
                            (id)kSecClass: (id)kSecClassKey,
                            (id)kSecAttrApplicationTag: biometricKeyTag,
                            (id)kSecAttrKeyType: (id)kSecAttrKeyTypeRSA,
                            (id)kSecReturnRef: @YES,
                            (id)kSecUseOperationPrompt: promptMessage
                            };
    SecKeyRef privateKey;
    OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, (CFTypeRef *)&privateKey);

    if (status == errSecSuccess) {
      NSError *error;
      NSData *dataToSign = [payload dataUsingEncoding:NSUTF8StringEncoding];
      NSData *signature = CFBridgingRelease(SecKeyCreateSignature(privateKey, kSecKeyAlgorithmRSASignatureMessagePKCS1v15SHA256, (CFDataRef)dataToSign, (void *)&error));

      if (signature != nil) {
        NSString *signatureString = [signature base64EncodedStringWithOptions:0];
        NSDictionary *result = @{
          @"success": @(YES),
          @"signature": signatureString
        };
        resolve(result);
      } else if (error.code == errSecUserCanceled) {
        NSDictionary *result = @{
          @"success": @(NO),
          @"error": @"User cancellation"
        };
        resolve(result);
      } else {
        NSString *message = [NSString stringWithFormat:@"Signature error: %@", error];
        reject(@"signature_error", message, nil);
      }
    } else if (status == errSecInteractionNotAllowed || status == errSecAuthFailed ||status == errSecItemNotFound) {
      // <-- Detect biometric changes or key invalidation here -->
      reject(@"biometric_changed", @"Key permanently invalidated", nil);
    } else {
      NSString *message = [NSString stringWithFormat:@"Key not found: %@",[self keychainErrorToString:status]];
      reject(@"storage_error", message, nil);
    }
  });
}

RCT_EXPORT_METHOD(simplePrompt: (NSDictionary *)params resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    NSString *promptMessage = [RCTConvert NSString:params[@"promptMessage"]];
    NSString *fallbackPromptMessage = [RCTConvert NSString:params[@"fallbackPromptMessage"]];
    BOOL allowDeviceCredentials = [RCTConvert BOOL:params[@"allowDeviceCredentials"]];

    LAContext *context = [[LAContext alloc] init];
    LAPolicy laPolicy = LAPolicyDeviceOwnerAuthenticationWithBiometrics;

    if (allowDeviceCredentials == TRUE) {
      laPolicy = LAPolicyDeviceOwnerAuthentication;
      context.localizedFallbackTitle = fallbackPromptMessage;
    } else {
      context.localizedFallbackTitle = @"";
    }

    [context evaluatePolicy:laPolicy localizedReason:promptMessage reply:^(BOOL success, NSError *biometricError) {
      if (success) {
        NSDictionary *result = @{
          @"success": @(YES)
        };
        resolve(result);
      } else if (biometricError.code == LAErrorUserCancel) {
        NSDictionary *result = @{
          @"success": @(NO),
          @"error": @"User cancellation"
        };
        resolve(result);
      } else {
        NSString *message = [NSString stringWithFormat:@"%@", biometricError];
        reject(@"biometric_error", message, nil);
      }
    }];
  });
}

RCT_EXPORT_METHOD(biometricKeysExist: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    BOOL biometricKeyExists = [self doesBiometricKeyExist];

    if (biometricKeyExists) {
      NSDictionary *result = @{
        @"keysExist": @(YES)
      };
      resolve(result);
    } else {
      NSDictionary *result = @{
        @"keysExist": @(NO)
      };
      resolve(result);
    }
  });
}

- (NSData *) getBiometricKeyTag {
  NSString *biometricKeyAlias = @"com.rnbiometrics.biometricKey";
  return [biometricKeyAlias dataUsingEncoding:NSUTF8StringEncoding];
}

- (BOOL) doesBiometricKeyExist {
  NSData *biometricKeyTag = [self getBiometricKeyTag];
  NSDictionary *query = @{
                          (id)kSecClass: (id)kSecClassKey,
                          (id)kSecAttrApplicationTag: biometricKeyTag,
                          (id)kSecAttrKeyType: (id)kSecAttrKeyTypeRSA,
                          (id)kSecReturnRef: @YES
                          };
  SecKeyRef privateKey;
  OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, (CFTypeRef *)&privateKey);

  if (status == errSecSuccess) {
    if (privateKey != NULL) {
      CFRelease(privateKey);
    }
    return YES;
  } else {
    return NO;
  }
}

- (OSStatus) deleteBiometricKey {
  NSData *biometricKeyTag = [self getBiometricKeyTag];
  NSDictionary *query = @{
                          (id)kSecClass: (id)kSecClassKey,
                          (id)kSecAttrApplicationTag: biometricKeyTag,
                          (id)kSecAttrKeyType: (id)kSecAttrKeyTypeRSA
                          };
  OSStatus status = SecItemDelete((__bridge CFDictionaryRef)query);
  return status;
}

- (NSString *) getBiometryType:(LAContext *)context {
  if (@available(iOS 11.0, *)) {
    switch (context.biometryType) {
      case LABiometryTypeTouchID:
        return @"TouchID";
      case LABiometryTypeFaceID:
        return @"FaceID";
      default:
        return @"None";
    }
  } else {
    return @"TouchID";
  }
}

- (NSData *) addHeaderPublickey:(NSData *)publicKey {
  static unsigned char rsa2048Asn1Header[] = {
    0x30, 0x82, 0x01, 0x22, 0x30, 0x0d, 0x06, 0x09,
    0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01,
    0x01, 0x05, 0x00, 0x03, 0x82, 0x01, 0x0f, 0x00
  };

  NSMutableData *encodedKey = [[NSMutableData alloc] init];
  [encodedKey appendData:[NSData dataWithBytes:rsa2048Asn1Header length:sizeof(rsa2048Asn1Header)]];
  [encodedKey appendData:publicKey];
  return encodedKey;
}

- (NSString *) keychainErrorToString:(OSStatus)status {
  NSString *message;
  switch (status) {
    case errSecSuccess:
      message = @"Success";
      break;
    case errSecUnimplemented:
      message = @"Function or operation not implemented";
      break;
    case errSecParam:
      message = @"One or more parameters passed to a function were not valid";
      break;
    case errSecAllocate:
      message = @"Failed to allocate memory";
      break;
    case errSecNotAvailable:
      message = @"No keychain is available. You may need to restart your device";
      break;
    case errSecDuplicateItem:
      message = @"The item already exists";
      break;
    case errSecItemNotFound:
      message = @"The item cannot be found";
      break;
    case errSecInteractionNotAllowed:
      message = @"User interaction is not allowed";
      break;
    case errSecDecode:
      message = @"Unable to decode the provided data";
      break;
    default:
      message = [NSString stringWithFormat:@"Unknown error (%d)", (int)status];
      break;
  }
  return message;
}

@end
