# Changelog
All notable changes to this project will be documented in this file.

## [1.6.1] - 2019-8-12
### Changed
- Fixed reported security issues from npm in dev dependencies

## [1.6.0] - 2019-7-10
### Changed
- Disabled use password option on iOS by default
- Detected if keys exists before trying to delete them and returned false in promise result in order to prevent error from occurring

## [1.5.2] - 2019-5-9
### Changed
- Fixed android compilation error by re-organizing order of gradle repositories

## [1.5.1] - 2019-4-26
### Changed
- Updated doc strings and type definition for the createKeys function

## [1.5.0] - 2019-4-17
### Added
- Added the ability to not display a biometrics prompt when creating keys

## [1.4.0] - 2019-4-3
### Changed
- Fixed reported security issues from npm
- Added a dependency on appcompat-v7 in android to ensure required UI libraries are available
### Added
- Added a podspec file

## [1.3.0] - 2019-1-24
### Changed
- Removed src directory and moved index.js to the root
- Made sure all android error messages start with a capital letter
### Added
- Added a function for simply displaying a biometric prompt

## [1.2.0] - 2018-11-29
### Changed
- Upgraded default android SDK version to 28
- Upgraded gradle version and added the gradle wrapper
- Removed npmignore files in favor of gitignore
### Added
- Added the ability to override android SDK and build versions using gradle extra properties extension

## [1.1.3] - 2018-08-09
### Changed
- Fixed typo in readme
- Fixed reported security issues from npm
### Added
- Added type script definitions

## [1.1.2] - 2018-06-14
### Changed
- Fixed public key format in iOS

## [1.1.1] - 2018-06-11
### Changed
- Fixed potential null pointer exception that could occur from saved android dialog fragments

## [1.1.0] - 2018-05-03
### Added
- Added enums for sensor types

### Changed
- Fixed IllegalState exception that occurred in android when dialog is dismissed improperly
- Fixed issue where promise rejection could be called more than once on android

## [1.0.2] - 2018-04-12
### Changed
- fixed typo in readme documentation

## 1.0.1 - 2018-04-12
### Added
- Initial release
- Added native code to detect sensor type
- Added native code to create private public key pairs
- Added native code to use private key to create a signature given a payload

[1.0.2]: https://github.com/SelfLender/react-native-biometrics/compare/1.0.1...1.0.2
[1.1.0]: https://github.com/SelfLender/react-native-biometrics/compare/1.0.2...1.1.0
[1.1.1]: https://github.com/SelfLender/react-native-biometrics/compare/1.1.0...1.1.1
[1.1.2]: https://github.com/SelfLender/react-native-biometrics/compare/1.1.1...1.1.2
[1.1.3]: https://github.com/SelfLender/react-native-biometrics/compare/1.1.2...1.1.3
[1.2.0]: https://github.com/SelfLender/react-native-biometrics/compare/1.1.3...1.2.0
[1.3.0]: https://github.com/SelfLender/react-native-biometrics/compare/1.2.0...1.3.0
[1.4.0]: https://github.com/SelfLender/react-native-biometrics/compare/1.3.0...1.4.0
[1.5.0]: https://github.com/SelfLender/react-native-biometrics/compare/1.4.0...1.5.0
[1.5.1]: https://github.com/SelfLender/react-native-biometrics/compare/1.5.0...1.5.1
[1.5.2]: https://github.com/SelfLender/react-native-biometrics/compare/1.5.1...1.5.2
[1.6.0]: https://github.com/SelfLender/react-native-biometrics/compare/1.5.2...1.6.0
[1.6.1]: https://github.com/SelfLender/react-native-biometrics/compare/1.6.0...1.6.1
