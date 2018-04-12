//
//  ReactNativeBiometrics.m
//
//  Created by Brandon Hines on 4/3/18.
//

#import "ReactNativeBiometrics.h"
#import <LocalAuthentication/LocalAuthentication.h>
#import <Security/Security.h>

@implementation ReactNativeBiometrics

RCT_EXPORT_MODULE(ReactNativeBiometrics);

RCT_EXPORT_METHOD(isSensorAvailable:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  LAContext *context = [[LAContext alloc] init];

  if ([context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:NULL]) {
    resolve([self getBiometryType:context]);
  } else {
    resolve(Nil);
  }
}

RCT_EXPORT_METHOD(createKeys: (NSString *)promptMessage resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    LAContext *context = [[LAContext alloc] init];

    [context evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics localizedReason:promptMessage reply:^(BOOL success, NSError *fingerprintError) {
      if (success) {
        CFErrorRef error = NULL;

        SecAccessControlRef sacObject = SecAccessControlCreateWithFlags(kCFAllocatorDefault,
                                                                        kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
                                                                        kSecAccessControlTouchIDAny, &error);
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
          NSString *publicKeyString = [publicKeyData base64EncodedStringWithOptions:0];
          resolve(publicKeyString);
        } else {
          NSString *message = [NSString stringWithFormat:@"Key generation error: %@", gen_error];
          reject(@"storage_error", message, nil);
        }
      } else {
        reject(@"fingerprint_error", @"Could not confirm fingerprint", nil);
      }
    }];
  });
}

RCT_EXPORT_METHOD(deleteKeys: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    OSStatus status = [self deleteBiometricKey];

    if (status == noErr) {
      resolve(@(YES));
    } else {
      NSString *message = [NSString stringWithFormat:@"Key not found: %@",[self keychainErrorToString:status]];
      reject(@"deletion_error", message, nil);
    }
  });
}

RCT_EXPORT_METHOD(createSignature: (NSString *)promptMessage payload:(NSString *)payload resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
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
        resolve(signatureString);
      } else {
        NSString *message = [NSString stringWithFormat:@"Signature error: %@", error];
        reject(@"signature_error", message, nil);
      }
    }
    else {
      NSString *message = [NSString stringWithFormat:@"Key not found: %@",[self keychainErrorToString:status]];
      reject(@"storage_error", message, nil);
    }
  });
}

- (NSData *) getBiometricKeyTag {
  NSString *biometricKeyAlias = @"com.rnbiometrics.biometricKey";
  NSData *biometricKeyTag = [biometricKeyAlias dataUsingEncoding:NSUTF8StringEncoding];
  return biometricKeyTag;
}

-(OSStatus) deleteBiometricKey {
  NSData *biometricKeyTag = [self getBiometricKeyTag];
  NSDictionary *deleteQuery = @{
                                (id)kSecClass: (id)kSecClassKey,
                                (id)kSecAttrApplicationTag: biometricKeyTag,
                                (id)kSecAttrKeyType: (id)kSecAttrKeyTypeRSA
                                };

  OSStatus status = SecItemDelete((__bridge CFDictionaryRef)deleteQuery);
  return status;
}

- (NSString *)getBiometryType:(LAContext *)context
{
  if (@available(iOS 11, *)) {
    return (context.biometryType == LABiometryTypeFaceID) ? @"FaceID" : @"TouchID";
  }

  return @"TouchID";
}

- (NSString *)keychainErrorToString:(OSStatus)error {
  NSString *message = [NSString stringWithFormat:@"%ld", (long)error];

  switch (error) {
    case errSecSuccess:
      message = @"success";
      break;

    case errSecDuplicateItem:
      message = @"error item already exists";
      break;

    case errSecItemNotFound :
      message = @"error item not found";
      break;

    case errSecAuthFailed:
      message = @"error item authentication failed";
      break;

    default:
      break;
  }

  return message;
}

@end
