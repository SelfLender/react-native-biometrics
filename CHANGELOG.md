# Changelog
All notable changes to this project will be documented in this file.

## [1.2.0] - 2018-11-29
### Changed
- upgraded default android SDK version to 28
- upgraded gradle version and added the gradle wrapper
- removed npmignore files in favor of gitignore
### Added
- Added the ability to override android SDK and build versions using gradle extra properties extension

## [1.1.3] - 2018-08-09
### Changed
- fixed typo in readme
- fixed reported security issues from npm
### Added
- Added type script definitions

## [1.1.2] - 2018-06-14
### Changed
- fixed public key format in iOS

## [1.1.1] - 2018-06-11
### Changed
- fixed potential null pointer exception that could occur from saved android dialog fragments

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
