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
    if (((NSNull *) promptMessage == [NSNull null]) || (promptMessage == nil) || [promptMessage length] == 0) {
      [self createAndStoreKeyPair:resolve rejecter:reject];
    } else {
      LAContext *context = [[LAContext alloc] init];
      context.localizedFallbackTitle = @"";

      [context evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics localizedReason:promptMessage reply:^(BOOL success, NSError *fingerprintError) {
        if (success) {
          [self createAndStoreKeyPair:resolve rejecter:reject];
        } else {
          NSString *message = [NSString stringWithFormat:@"Fingerprint error: %@", fingerprintError];
          reject(@"fingerprint_error", message, nil);
        }
      }];
    }
  });
}

- (void) createAndStoreKeyPair:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
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
    NSData *publicKeyDataWithHeader = [self addHeaderPublickey:publicKeyData];
    NSString *publicKeyString = [publicKeyDataWithHeader base64EncodedStringWithOptions:0];
    resolve(publicKeyString);
  } else {
    NSString *message = [NSString stringWithFormat:@"Key generation error: %@", gen_error];
    reject(@"storage_error", message, nil);
  }
}

RCT_EXPORT_METHOD(deleteKeys: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    BOOL biometricKeyExists = [self biometricKeyExists];

    if (biometricKeyExists) {
      OSStatus status = [self deleteBiometricKey];

      if (status == noErr) {
        resolve(@(YES));
      } else {
        NSString *message = [NSString stringWithFormat:@"Key not found: %@",[self keychainErrorToString:status]];
        reject(@"deletion_error", message, nil);
      }
    } else {
        resolve(@(NO));
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

RCT_EXPORT_METHOD(simplePrompt: (NSString *)promptMessage resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    LAContext *context = [[LAContext alloc] init];
    context.localizedFallbackTitle = @"";

    [context evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics localizedReason:promptMessage reply:^(BOOL success, NSError *fingerprintError) {
      if (success) {
        resolve(@(YES));
      } else {
        NSString *message = [NSString stringWithFormat:@"Fingerprint error: %@", fingerprintError];
        reject(@"fingerprint_error", message, nil);
      }
    }];
  });
}

- (NSData *) getBiometricKeyTag {
  NSString *biometricKeyAlias = @"com.rnbiometrics.biometricKey";
  NSData *biometricKeyTag = [biometricKeyAlias dataUsingEncoding:NSUTF8StringEncoding];
  return biometricKeyTag;
}

- (BOOL) biometricKeyExists {
  NSData *biometricKeyTag = [self getBiometricKeyTag];
  NSDictionary *searchQuery = @{
                                (id)kSecClass: (id)kSecClassKey,
                                (id)kSecAttrApplicationTag: biometricKeyTag,
                                (id)kSecAttrKeyType: (id)kSecAttrKeyTypeRSA
                                };

  OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)searchQuery, nil);
  return status == errSecSuccess;
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


- (NSData *)addHeaderPublickey:(NSData *)publicKeyData {

    unsigned char builder[15];
    NSMutableData * encKey = [[NSMutableData alloc] init];
    unsigned long bitstringEncLength;

    static const unsigned char _encodedRSAEncryptionOID[15] = {

        /* Sequence of length 0xd made up of OID followed by NULL */
        0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86,
        0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00

    };
    // When we get to the bitstring - how will we encode it?
    if  ([publicKeyData length ] + 1  < 128 )
        bitstringEncLength = 1 ;
    else
        bitstringEncLength = (([publicKeyData length ] +1 ) / 256 ) + 2 ;
    //
    //        // Overall we have a sequence of a certain length
    builder[0] = 0x30;    // ASN.1 encoding representing a SEQUENCE
    //        // Build up overall size made up of -
    //        // size of OID + size of bitstring encoding + size of actual key
    size_t i = sizeof(_encodedRSAEncryptionOID) + 2 + bitstringEncLength + [publicKeyData length];
    size_t j = encodeLength(&builder[1], i);
    [encKey appendBytes:builder length:j +1];

    // First part of the sequence is the OID
    [encKey appendBytes:_encodedRSAEncryptionOID
                 length:sizeof(_encodedRSAEncryptionOID)];

    // Now add the bitstring
    builder[0] = 0x03;
    j = encodeLength(&builder[1], [publicKeyData length] + 1);
    builder[j+1] = 0x00;
    [encKey appendBytes:builder length:j + 2];

    // Now the actual key
    [encKey appendData:publicKeyData];

    return encKey;
}

size_t encodeLength(unsigned char * buf, size_t length) {

    // encode length in ASN.1 DER format
    if (length < 128) {
        buf[0] = length;
        return 1;
    }

    size_t i = (length / 256) + 1;
    buf[0] = i + 0x80;
    for (size_t j = 0 ; j < i; ++j) {
        buf[i - j] = length & 0xFF;
        length = length >> 8;
    }

    return i + 1;
}

@end
